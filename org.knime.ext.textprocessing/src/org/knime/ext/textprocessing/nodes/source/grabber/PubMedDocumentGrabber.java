/*
========================================================================
 *  Copyright by KNIME AG, Zurich, Switzerland
 *  Website: http://www.knime.com; Email: contact@knime.com
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License, Version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses>.
 *
 *  Additional permission under GNU GPL version 3 section 7:
 *
 *  KNIME interoperates with ECLIPSE solely via ECLIPSE's plug-in APIs.
 *  Hence, KNIME and ECLIPSE are both independent programs and are not
 *  derived from each other. Should, however, the interpretation of the
 *  GNU GPL Version 3 ("License") under any applicable laws result in
 *  KNIME and ECLIPSE being a combined program, KNIME AG herewith grants
 *  you the additional permission to use and propagate KNIME together with
 *  ECLIPSE with only the license terms in place for ECLIPSE applying to
 *  ECLIPSE and the GNU GPL Version 3 applying for KNIME, provided the
 *  license terms of ECLIPSE themselves allow for the respective use and
 *  propagation of ECLIPSE together with KNIME.
 *
 *  Additional permission relating to nodes for KNIME that extend the Node
 *  Extension (and in particular that are based on subclasses of NodeModel,
 *  NodeDialog, and NodeView) and that only interoperate with KNIME through
 *  standard APIs ("Nodes"):
 *  Nodes are deemed to be separate and independent programs and to not be
 *  covered works.  Notwithstanding anything to the contrary in the
 *  License, the License does not apply to Nodes, you are not required to
 *  license Nodes under the License, and you are granted a license to
 *  prepare and propagate Nodes, in each case even if such Nodes are
 *  propagated with or for interoperation with KNIME.  The owner of a Node
 *  may freely choose the license terms applicable to such Node, including
 *  when such Node is propagated with or for interoperation with KNIME.
 * ---------------------------------------------------------------------
 *
 * History
 *   18.07.2007 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.source.grabber;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.xml.parsers.ParserConfigurationException;

import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.util.CheckUtils;
import org.knime.core.util.FileUtil;
import org.knime.ext.textprocessing.data.Document;
import org.knime.ext.textprocessing.data.DocumentSource;
import org.knime.ext.textprocessing.nodes.source.parser.DocumentParsedEvent;
import org.knime.ext.textprocessing.nodes.source.parser.DocumentParsedEventListener;
import org.knime.ext.textprocessing.nodes.source.parser.DocumentParser;
import org.knime.ext.textprocessing.nodes.source.parser.FileCollector;
import org.knime.ext.textprocessing.nodes.source.parser.pubmed.PubMedDocumentParser;
import org.knime.ext.textprocessing.util.UrlFileUtil;
import org.xml.sax.SAXException;

/**
 *
 * @author Kilian Thiel, University of Konstanz
 */
public class PubMedDocumentGrabber extends AbstractDocumentGrabber {

    /**
     * The source of the documents grabbed by this grabber.
     */
    public static final String SOURCE = "PubMed";

    private static final NodeLogger LOGGER = NodeLogger.getLogger(PubMedDocumentGrabber.class);

    private static final String PROTOCOL = "https";

    private static final String HOST = "eutils.ncbi.nlm.nih.gov";

    private static final String SEARCH_PATH = "/entrez/eutils/esearch.fcgi";

    private static final String SEARCH_QUERY = "db=pubmed&term=";

    private static final String SEARCH_QUERY_POSTFIX = "&retmax=";

    private static final String FETCH_PATH = "/entrez/eutils/efetch.fcgi";

    private static final String FETCH_QUERY = "db=pubmed&id=";

    private static final String FETCH_QUERY_POSTFIX = "&retmode=xml&rettype=abstract";

    private static final String BASIC_FILE_NAME = "PubMedAbstracts";

    private static final String FILE_EXTENSION = "gz";

    private int m_stepSize = 100;

    private long m_delayMillis = 1000;

    private final List<Integer> m_idList = new ArrayList<>();

    private File m_tempDir;

    /**
     * Creates empty instance of <code>PubMedDocumentGrabber</code>.
     */
    PubMedDocumentGrabber() {
        super();
    }

    /**
     * {@inheritDoc}
     *
     * @since 2.8
     */
    @Override
    public String getName() {
        return "PUBMED";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int numberOfResults(final Query query) throws Exception {
        final URL pubmed = buildUrl(query, false);
        LOGGER.info("PubMed Query: " + pubmed.toString());
        return buildResultList(pubmed);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Deprecated
    public List<Document> grabDocuments(final File directory, final Query query) throws Exception {
        if ((directory != null) && (query != null)) {
            if (directory.exists() && directory.isDirectory()) {

                fetchDocuments(directory, query);

                List<Document> docs = new ArrayList<>();
                try {
                    docs = parseDocuments(directory);
                } catch (final URISyntaxException e) {
                    LOGGER.warn("Could not find file containing PubMed documents!");
                    throw (e);
                } catch (final Exception e) {
                    LOGGER.warn("Could not parse PubMed documents!");
                    throw (e);
                }

                return docs;
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     *
     * @deprecated
     */
    @Deprecated
    @Override
    public void fetchAndParseDocuments(final File directory, final Query query) throws Exception {
        if ((directory != null) && (query != null)) {
            if (directory.exists() && directory.isDirectory()) {

                fetchDocuments(directory, query);

                try {
                    parseDocumentsAndNotify(directory);
                } catch (final URISyntaxException e) {
                    LOGGER.warn("Could not find file containing PubMed documents!");
                    throw (e);
                } catch (final Exception e) {
                    LOGGER.warn("Could not parse PubMed documents!");
                    throw (e);
                }
            }
        }
    }

    @Override
    public void fetchAndParseDocuments(final URL directory, final Query query)
            throws IOException, CanceledExecutionException, URISyntaxException, InterruptedException,
            ParserConfigurationException, SAXException {
        CheckUtils.checkArgumentNotNull(directory);
        CheckUtils.checkArgumentNotNull(query);
        fetchDocuments(directory, query);
        try {
            parseDocumentsAndNotify(directory);
        } catch (final URISyntaxException e) {
            LOGGER.warn("Could not find file containing PubMed documents!");
            throw (e);
        } catch (final IOException e) {
            LOGGER.warn("Could not resolve url.");
            throw (e);
        } catch (final CanceledExecutionException e) {
            LOGGER.warn("Execution has been canceled.");
            throw (e);
        } catch (final ParserConfigurationException e) {
            LOGGER.warn("SAXParser could not be instantiated.");
            throw (e);
        }
    }

    @Deprecated
    private void fetchDocuments(final File directory, final Query query) throws Exception {
        if ((directory != null) && (query != null) && directory.exists() && directory.isDirectory()) {

            URL pubmed = buildUrl(query, true);
            LOGGER.info("PubMed Query: " + pubmed.toString());

            // Read search result xml
            buildResultList(pubmed);

            // go through all ids (with certain step size)
            // and download document
            int idStart = 0;
            int count = 1;
            while (idStart < m_idList.size()) {
                checkCanceled();
                final int idEnd = getEnd(idStart, m_idList.size() - 1);

                pubmed = buildQueryUrl(idStart, idEnd);

                LOGGER.info("PubMed fetching: " + pubmed.toString());

                final String filename = BASIC_FILE_NAME + count + "." + FILE_EXTENSION;
                try {
                    saveDocument(pubmed, directory, filename);
                } catch (final IOException e) {
                    LOGGER.warn("Could not read PubMed Xml-Website!");
                    throw (e);
                }

                Thread.sleep(m_delayMillis);
                idStart = idEnd + 1;
                count++;
            }
        }
    }

    private void fetchDocuments(final URL directory, final Query query)
            throws IOException, CanceledExecutionException, URISyntaxException, InterruptedException {
        if ((directory != null) && (query != null)) {

            URL pubmed = buildUrl(query, true);
            LOGGER.info("PubMed Query: " + pubmed.toString());

            // Read search result xml
            buildResultList(pubmed);

            URL dirURL = directory;
            // if the dir is remote and files should be deleted after parsing
            // store the files in a local temp dir instead, and delete after parsing
            if (getDeleteFiles() && (FileUtil.resolveToPath(directory) == null)) {
                m_tempDir = FileUtil.createTempDir("pubmed_doc_grabber", null, false);
                dirURL = FileUtil.toURL(m_tempDir.getAbsolutePath());
            }

            // go through all ids (with certain step size)
            // and download document
            int idStart = 0;
            int count = 1;
            while (idStart < m_idList.size()) {
                checkCanceled();
                final int idEnd = getEnd(idStart, m_idList.size() - 1);
                pubmed = buildQueryUrl(idStart, idEnd);
                LOGGER.info("PubMed fetching: " + pubmed.toString());
                final String filename = BASIC_FILE_NAME + count + "." + FILE_EXTENSION;
                try {
                    saveDocument(pubmed, dirURL, filename);
                } catch (final IOException e) {
                    LOGGER.warn("Could not read PubMed Xml-Website!");
                    throw (e);
                } catch (final URISyntaxException e) {
                    LOGGER.warn("URL Syntax is not valid!");
                    throw (e);
                }
                Thread.sleep(m_delayMillis);
                idStart = idEnd + 1;
                count++;
            }
        }
    }

    private URL buildQueryUrl(final int idStart, final int idEnd) throws URISyntaxException, MalformedURLException {
        URL pubmed;
        // setting progress
        final double progress = ((double)idEnd / (double)m_idList.size()) * 0.5;
        message("Fetching documents from " + idStart + " to " + idEnd + " of " + m_idList.size(), progress);

        final StringBuilder idString = new StringBuilder();
        for (int i = idStart; i <= idEnd; i++) {
            idString.append(m_idList.get(i));
            idString.append(",");
        }

        final String fetchStr = FETCH_QUERY + idString.toString() + FETCH_QUERY_POSTFIX;
        final URI uri = new URI(PROTOCOL, HOST, FETCH_PATH, fetchStr, "");
        pubmed = uri.toURL();
        return pubmed;
    }

    @Deprecated
    private void parseDocumentsAndNotify(final File dir) throws Exception {

        final DocumentParser parser = getDocumentParser();

        final List<String> validExtensions = new ArrayList<>();
        validExtensions.add(FILE_EXTENSION);

        final FileCollector fc = new FileCollector(dir, validExtensions, false, true);
        final List<File> files = fc.getFiles();
        final int fileCount = files.size();
        int currFile = 1;
        for (final File f : files) {
            final double progress = (double)currFile / (double)fileCount;
            setProgress(progress, "Parsing file " + currFile + " of " + fileCount);
            checkCanceled();
            currFile++;
            LOGGER.info("Parsing file: " + f.getAbsolutePath());
            final boolean compressed =
                    f.getName().toLowerCase().endsWith(".gz") || f.getName().toLowerCase().endsWith(".zip");
            try (final InputStream is =
                    compressed ? new GZIPInputStream(new FileInputStream(f)) : new FileInputStream(f)) {
                parser.setDocumentFilepath(f.getAbsolutePath());
                parser.parseDocument(is);
            }
        }

        if (getDeleteFiles()) {
            for (final File file : files) {
                if (file.isFile() && file.exists()) {
                    Files.delete(file.toPath());
                }
            }
        }
    }

    private void parseDocumentsAndNotify(final URL dir)
            throws CanceledExecutionException, IOException, URISyntaxException, SAXException, ParserConfigurationException {

        final PubMedDocumentParser parser = getDocumentParser();

        final Set<String> validExtensions = new HashSet<>();
        validExtensions.add(FILE_EXTENSION);

        final List<URL> files = UrlFileUtil.listFiles(UrlFileUtil.getStringRepresentation(dir), validExtensions, false, true);
        final int fileCount = files.size();
        int currFile = 1;
        for (final URL f : files) {
            final String filename = UrlFileUtil.getStringRepresentation(f);
            final double progress = (double)currFile / (double)fileCount;
            setProgress(progress, "Parsing file " + currFile + " of " + fileCount);
            checkCanceled();
            currFile++;
            LOGGER.info("Parsing file: " + filename);
            final boolean compressed =
                    filename.toLowerCase().endsWith(".gz") || filename.toLowerCase().endsWith(".zip");
            try (final InputStream is = compressed ? new GZIPInputStream(FileUtil.openStreamWithTimeout(f))
                : FileUtil.openStreamWithTimeout(f)) {
                parser.setDocumentFilepath(filename);
                parser.parseDocument(is);
            }
        }

        if (getDeleteFiles()) {
            for (final URL file : files) {
                final Path localPath = FileUtil.resolveToPath(file);
                if (localPath != null) {
                    try {
                        Files.delete(localPath);
                    } catch (final IOException ex) {
                        LOGGER.warn("Unable to delete file '" + UrlFileUtil.getStringRepresentation(file));
                    }
                } else {
                    // if delete files flag is true and file is remote, it means a temp dir has been
                    // created to store the files -> delete the temp dir
                    if ((m_tempDir != null) && !FileUtil.deleteRecursively(m_tempDir)) {
                        LOGGER.warn("Unable to delete temp dir " + m_tempDir.getAbsolutePath());
                    }
                }
            }
        }
    }

    private PubMedDocumentParser getDocumentParser() {
        final PubMedDocumentParser parser = new PubMedDocumentParser(getExtractMetaInfo(), getTokenizerName());

        parser.addDocumentParsedListener(new InternalDocumentParsedEventListener());

        parser.setDocumentSource(new DocumentSource(SOURCE));
        if (getDocumentCategory() != null) {
            parser.setDocumentCategory(getDocumentCategory());
        }
        if (getDocumentType() != null) {
            parser.setDocumentType(getDocumentType());
        }
        return parser;
    }

    @Deprecated
    private List<Document> parseDocuments(final File dir) throws Exception {
        final List<Document> docs = new ArrayList<>();

        final DocumentParser parser = new PubMedDocumentParser(getTokenizerName());
        parser.setDocumentSource(new DocumentSource(SOURCE));
        if (getDocumentCategory() != null) {
            parser.setDocumentCategory(getDocumentCategory());
        }
        if (getDocumentType() != null) {
            parser.setDocumentType(getDocumentType());
        }

        final List<String> validExtensions = new ArrayList<>();
        validExtensions.add(FILE_EXTENSION);

        final FileCollector fc = new FileCollector(dir, validExtensions, false, true);
        final List<File> files = fc.getFiles();
        final int fileCount = files.size();
        int currFile = 1;
        for (final File f : files) {
            final double progress = (double)currFile / (double)fileCount;
            setProgress(progress, "Parsing file " + currFile + " of " + fileCount);
            checkCanceled();
            currFile++;
            LOGGER.info("Parsing file: " + f.getAbsolutePath());

            try (final InputStream is =
                    (f.getName().toLowerCase().endsWith(".gz") || f.getName().toLowerCase().endsWith(".zip"))
                    ? new GZIPInputStream(new FileInputStream(f)) : new FileInputStream(f)) {
                parser.setDocumentFilepath(f.getAbsolutePath());
                docs.addAll(parser.parse(is));
            }
            parser.clean();
        }

        if (getDeleteFiles()) {
            for (final File file : files) {
                if (file.isFile() && file.exists()) {
                    Files.delete(file.toPath());
                }
            }
        }

        return docs;
    }

    @Deprecated
    private static void saveDocument(final URL url, final File dir, final String filename)
            throws IOException {

        final File dst = new File(dir.getAbsolutePath() + "/" + filename);
        if (!dst.exists()) {
            dst.createNewFile();
        }
        try (final FileOutputStream fos = new FileOutputStream(dst)) {
            saveDocument(url, fos);
        }
    }

    private static void saveDocument(final URL url, final URL dir, final String filename)
            throws IOException, URISyntaxException {
        // assume the dir is empty
        final URL outUrl = FileUtil.toURL(UrlFileUtil.getStringRepresentation(dir) + "/" + filename);
        final Path localPath = FileUtil.resolveToPath(outUrl);
        final boolean isLocalPath = localPath != null;
        URLConnection urlConnection = null;
        if (!isLocalPath) {
            urlConnection = FileUtil.openOutputConnection(outUrl, "PUT");
        }
        try (final OutputStream tempOut = !isLocalPath && (urlConnection != null) ? urlConnection.getOutputStream()
            : Files.newOutputStream(localPath)) {
            saveDocument(url, tempOut);
        }
    }

    private static void saveDocument(final URL url, final OutputStream tempOut) throws IOException {
        final URLConnection conn = url.openConnection();
        conn.setConnectTimeout(60000);
        try {
            conn.connect();
        } catch (final SocketTimeoutException e) {
            LOGGER.error("Timeout! Connection could not be established.");
            throw e;
        } catch (final IOException e) {
            LOGGER.error("Connection could not be opened.");
            throw e;
        }
        final InputStreamReader isr = new InputStreamReader(conn.getInputStream(), "UTF-8");

        // Transfer bytes from in to out
        try (BufferedReader in = new BufferedReader(isr);
                OutputStream out = new GZIPOutputStream(tempOut);
                OutputStreamWriter writer = new OutputStreamWriter(out, "UTF-8")) {
            String line;
            while ((line = in.readLine()) != null) {
                writer.write(line);
            }
        } catch (final IOException e) {
            LOGGER.error("Documents could not be downloaded.");
            throw e;
        }
    }

    private int getEnd(final int start, final int max) {
        int end = start + m_stepSize;
        if (end > max) {
            end = max;
        }
        return end;
    }

    private void message(final String str, final double progress) {
        if (getExec() != null) {
            getExec().setProgress(progress, str);
        }
    }

    private static URL buildUrl(final Query query, final boolean applyMaxResults)
            throws URISyntaxException, MalformedURLException {

        // Build search url
        String str = SEARCH_QUERY + query.getQuery();
        if (applyMaxResults) {
            str += SEARCH_QUERY_POSTFIX + query.getMaxResults();
        }
        final URI uri = new URI(PROTOCOL, HOST, SEARCH_PATH, str, "");

        return uri.toURL();
    }

    private int buildResultList(final URL url) throws IOException, CanceledExecutionException {
        // Read search result xml
        int results = -1;
        try (InputStreamReader isr = new InputStreamReader(url.openStream());
                BufferedReader r = new BufferedReader(isr)) {
            m_idList.clear();
            String line = null;
            while ((line = r.readLine()) != null) {
                checkCanceled();

                // regular expression to find the "id" field
                final String regex = "<Id>(\\d+)</Id>";
                Pattern p = Pattern.compile(regex);
                Matcher m = p.matcher(line);

                // find id sequences in search result
                if (m.find()) {
                    final int id = Integer.parseInt(m.group(1));
                    m_idList.add(id);
                }

                if (results == -1) {
                    // regular expression to find the "count" field
                    final String regexCount = "<Count>(\\d+)</Count>";
                    p = Pattern.compile(regexCount);
                    m = p.matcher(line);

                    // find count sequences in search result
                    if (m.find()) {
                        results = Integer.parseInt(m.group(1));
                    }
                }
            }
        }
        return results;
    }

    /**
     * @return The number of grabbed documents.
     */
    public int getNumberOfDocuments() {
        return m_idList.size();
    }

    /**
     * @return the delay time between two requests in milliseconds
     */
    public long getDelayMillis() {
        return m_delayMillis;
    }

    /**
     * @param delayMillis the delay time between two requests in milliseconds to set.
     */
    public void setDelayMillis(final long delayMillis) {
        m_delayMillis = delayMillis;
    }

    /**
     * @return the stepSize which specifies the number of abstracts stored in one file.
     */
    public int getStepSize() {
        return m_stepSize;
    }

    /**
     * @param stepSize the stepSize to set, which specifies the number of abstracts stored in one file.
     */
    public void setStepSize(final int stepSize) {
        m_stepSize = stepSize;
    }

    private class InternalDocumentParsedEventListener implements DocumentParsedEventListener {
        /**
         * {@inheritDoc}
         */
        @Override
        public void documentParsed(final DocumentParsedEvent event) {
            notifyAllListener(event);
        }
    }
}
