/*
 * ------------------------------------------------------------------------
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
 *  KNIME and ECLIPSE being a combined program, KNIME GMBH herewith grants
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
 * -------------------------------------------------------------------
 *
 * History
 *   Dec, 2018 (Andisa Dewi): created
 */
package org.knime.ext.textprocessing.util;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;
import org.knime.core.util.FileUtil;

/**
 * Provides functionality to search a specified directory for files with given extensions. The directory can be searched
 * recursively or not.
 *
 * @author Andisa Dewi, KNIME.com
 * @since 4.0
 */
public final class UrlFileUtil {

    /** Empty constructor. */
    private UrlFileUtil() {
        // Nothing to do here...
    }

    /**
     * This method returns a list of files in URL form that are contained in a given directory path. The extension of
     * the files should match at least one in the given list of extensions. If the given list of extensions is empty,
     * then all extensions will be accepted. If <code>recursive</code> is set <code>true</code> the directory will be
     * searched recursively which means that subdirectories will by searched too. If the directory path is a local path
     * and <code>ignoreHiddenFiles</code> is set to true, any hidden files will be ignored. This method supports the
     * knime protocol and remote directories (only with knime protocol).
     *
     * @param dir Directory to search for files.
     * @param ext Extensions of file to search for.
     * @param recursive if set <code>true</code> the directory will be
     * @param ignoreHiddenFiles if set <code>true</code> hidden files will not be collected. searched recursively.
     * @return The list of URLs of all files that are inside the given directory path.
     * @throws InvalidPathException
     * @throws URISyntaxException
     * @throws IOException
     */
    public static final List<URL> listFiles(final String dir, final Set<String> ext, final boolean recursive,
        final boolean ignoreHiddenFiles) throws IOException, URISyntaxException {
        final URL url = FileUtil.toURL(dir);
        final Path localPath = FileUtil.resolveToPath(url);
        if ((localPath == null) && !url.getProtocol().equalsIgnoreCase("knime")) {
            throw new IOException("Reading from remote directories is not supported.");
        }

        return FileUtil.listFiles(url, u -> filterExt(u, ext) && filterLocalFiles(u, ignoreHiddenFiles), recursive);
    }

    /**
     * This method returns a list of files in URL form that are contained in a given directory path. All extensions will
     * be accepted. Use {@link UrlFileUtil#listFiles(String, Set, boolean, boolean)} to define the accepted extensions.
     * If <code>recursive</code> is set <code>true</code> the directory will be searched recursively which means that
     * subdirectories will by searched too. If the directory path is a local path and <code>ignoreHiddenFiles</code> is
     * set to true, any hidden files will be ignored. This method supports the knime protocol and remote directories
     * (only with knime protocol).
     *
     * @param dir Directory to search for files.
     * @param recursive if set <code>true</code> the directory will be
     * @param ignoreHiddenFiles if set <code>true</code> hidden files will not be collected. searched recursively.
     * @return The list of URLs of all files that are inside the given directory path.
     * @throws URISyntaxException
     * @throws IOException
     */
    public static final List<URL> listFiles(final String dir, final boolean recursive, final boolean ignoreHiddenFiles)
        throws IOException, URISyntaxException {
        return listFiles(dir, Collections.emptySet(), recursive, ignoreHiddenFiles);
    }

    private static boolean filterExt(final URL s, final Set<String> exts) {
        if (exts.isEmpty()) {
            return true;
        }
        final String fileExt = FilenameUtils.getExtension(s.getFile());
        return exts.stream().anyMatch(e -> e.equalsIgnoreCase(fileExt));
    }

    private static boolean filterLocalFiles(final URL s, final boolean ignoreHiddenFiles) {
        final File file = FileUtil.getFileFromURL(s);
        if (file != null) {
            if (ignoreHiddenFiles && file.isHidden()) {
                return false;
            }
            if (!file.isFile()) {
                return false;
            }
        }
        return true;
    }

    /**
     * If possible, this method returns the string representation of a path denoted by the given URL or the string
     * representation of the URL otherwise.
     *
     * @param url the URL
     * @return the path of the URL as a String, or the string format if the URL does not denote a local path
     * @throws URISyntaxException
     * @throws IOException
     * @since 4.0
     */
    public static final String getStringRepresentation(final URL url) throws IOException, URISyntaxException {
        final Path path = FileUtil.resolveToPath(url);
        return path == null ? url.toString() : path.toString();
    }

    /**
     * Returns the valid URL.
     *
     * @param urlStr the URL in string format.
     * @param shouldBeDir true if the URL should be a directory, false otherwise
     * @return the valid URL
     * @throws IOException If the url is not valid
     * @since 4.0
     */
    public static final URL getURL(final String urlStr, final boolean shouldBeDir) throws IOException {
        File localFile;
        URL url = null;
        try {
            url = FileUtil.toURL(urlStr);
            localFile = FileUtil.getFileFromURL(url);
        } catch (final IllegalArgumentException e) {
            throw new IOException(e.getMessage());
        }

        if (localFile != null) {
            if (!shouldBeDir && localFile.isDirectory()) {
                throw new IOException("Selected file: " + urlStr + " is a directory.");
            } else if (shouldBeDir && localFile.isFile()) {
                throw new IOException("Selected file: " + urlStr + " is not a directory.");
            }
            if (!localFile.exists()) {
                throw new IOException("Selected file: " + urlStr + " does not exist.");
            }
            if (!localFile.canRead()) {
                throw new IOException("Selected file: " + urlStr + " cannot be accessed.");
            }
        }
        return url;
    }
}
