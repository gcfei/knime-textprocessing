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
 *   Apr 18, 2006 (Kilian Thiel): created
 */
package org.knime.ext.textprocessing.nodes.source.parser;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.util.FileUtil;

/**
 * Provides functionality to search a specified directory for files with given extensions. The directory can be searched
 * recursively or not.
 *
 * @author Andisa Dewi, KNIME.com
 * @since 3.5
 */
public class FileCollector2 {

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
     * @throws InvalidSettingsException When the directory cannot be accessed.
     */
    public static List<URL> listFiles(final String dir, final List<String> ext, final boolean recursive,
        final boolean ignoreHiddenFiles) throws InvalidSettingsException {
        try {
            URL url = FileUtil.toURL(dir);
            Path localPath = FileUtil.resolveToPath(url);
            if (localPath == null && !url.getProtocol().equalsIgnoreCase("knime")) {
                throw new InvalidSettingsException("Reading from remote directories is not supported.");
            }

            return FileUtil.listFiles(url, u -> filterExt(u, ext) && filterLocalFiles(u, ignoreHiddenFiles), recursive);

        } catch (IOException | URISyntaxException e) {
            throw new InvalidSettingsException("Directory path cannot be accessed!");
        }
    }

    private static boolean filterExt(final URL s, final List<String> exts) {
        if (exts.isEmpty()) {
            return true;
        }
        final String fileExt = FilenameUtils.getExtension(s.getFile());
        for (final String ext : exts) {
            if (fileExt.equalsIgnoreCase(ext)) {
                return true;
            }
        }
        return false;
    }

    private static boolean filterLocalFiles(final URL s, final boolean ignoreHiddenFiles) {
        try {
            final File file = FileUtil.getFileFromURL(s);
            if (file != null) {
                if (ignoreHiddenFiles && file.isHidden()) {
                    return false;
                }
                if (!file.isFile() || file.isDirectory()) {
                    return false;
                }
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    /**
     * Returns the string representation of a given URL.
     *
     * @param url the URL
     * @return the path of the URL, or the string format if the URL does not denote a local path
     * @throws URISyntaxException
     * @throws IOException
     * @since 3.5
     */
    public static String getStringRepresentation(final URL url) throws IOException, URISyntaxException {
        Path path = FileUtil.resolveToPath(url);
        if (path == null) {
            return url.toString();
        } else {
            return path.toString();
        }
    }

    /**
     * Returns the valid URL.
     *
     * @param urlStr the URL in string format.
     * @param shouldBeDir true if the URL should be a directory, false otherwise
     * @return the valid URL
     * @throws InvalidSettingsException if the URL is not valid
     * @since 3.5
     */
    public static URL getURL(final String urlStr, final boolean shouldBeDir) throws InvalidSettingsException {
        File localFile;
        URL url = null;
        try {
            url = FileUtil.toURL(urlStr);
            localFile = FileUtil.getFileFromURL(url);
        } catch (IOException e) {
            localFile = null;
        }

        if (localFile != null) {
            if (!shouldBeDir && localFile.isDirectory()) {
                throw new InvalidSettingsException("Selected file: " + urlStr + " is a directory.");
            } else if (shouldBeDir && localFile.isFile()) {
                throw new InvalidSettingsException("Selected file: " + urlStr + " is not a directory.");
            }

            if (!localFile.exists()) {
                throw new InvalidSettingsException("Selected file: " + urlStr + " does not exist.");
            }
            if (!localFile.canRead()) {
                throw new InvalidSettingsException("Selected file: " + urlStr + " cannot be accessed.");
            }
        }
        return url;
    }
}
