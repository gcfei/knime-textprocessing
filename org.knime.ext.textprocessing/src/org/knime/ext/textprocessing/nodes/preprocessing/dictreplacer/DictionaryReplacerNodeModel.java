/*
 * ------------------------------------------------------------------------
 *
 *  Copyright (C) 2003 - 2010
 *  University of Konstanz, Germany and
 *  KNIME GmbH, Konstanz, Germany
 *  Website: http://www.knime.org; Email: contact@knime.org
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License, version 2, as 
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * -------------------------------------------------------------------
 * 
 * History
 *   23.09.2009 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.preprocessing.dictreplacer;

import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.ext.textprocessing.nodes.preprocessing.PreprocessingNodeModel;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Hashtable;

/**
 * @author Kilian Thiel, University of Konstanz
 *
 */
public class DictionaryReplacerNodeModel extends PreprocessingNodeModel {

    private static final NodeLogger LOGGER = NodeLogger
    .getLogger(DictionaryReplacerNodeModel.class);
    
    /**
     * The default dictionary file path.
     */
    public static final String DEF_DICTFILE = System.getProperty("user.home");
    
    /**
     * The default valid dictionary file extensions (txt).
     */
    public static final String[] VALID_DICTFILE_EXTENIONS = new String[]{"txt"};
    
    /**
     * The default separator.
     */
    public static final String DEFAULT_SEPARATOR = ",";
    
    private SettingsModelString m_fileModel = 
        DictionaryReplacerNodeDialog.getDictionaryFileModel();    
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void initPreprocessing() {
        Hashtable<String, String> dictionary = new Hashtable<String, String>();
        File f = new File(m_fileModel.getStringValue());
        if (f.exists() && f.canRead() && f.isFile()) {
            try {
                BufferedReader reader = new BufferedReader(
                        new FileReader(f));
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] keyVal = line.trim().split(DEFAULT_SEPARATOR);
                    if (keyVal.length == 2) {
                        dictionary.put(keyVal[0], keyVal[1]);
                    }
                }
                reader.close();
            } catch (FileNotFoundException e) {
                LOGGER.warn("Not such file !");
            } catch (IOException e) {
                LOGGER.warn("Cant read from file");
            }
        }
        m_preprocessing = new DictionaryReplacer(dictionary);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        super.loadValidatedSettingsFrom(settings);
        m_fileModel.loadSettingsFrom(settings);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        super.saveSettingsTo(settings);
        m_fileModel.saveSettingsTo(settings);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        super.validateSettings(settings);
        m_fileModel.validateSettings(settings);
        
        String file = ((SettingsModelString)m_fileModel
                .createCloneWithValidatedValue(settings)).getStringValue();
        File f = new File(file);
        if (!f.isFile() || !f.exists() || !f.canRead()) {
            throw new InvalidSettingsException("Selected dictionary file: "
                    + file + " is not valid!");
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void reset() {
        // Nothing to do ...
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadInternals(final File nodeInternDir, 
            final ExecutionMonitor exec)
            throws IOException, CanceledExecutionException {
        // Nothing to do ...
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveInternals(final File nodeInternDir, 
            final ExecutionMonitor exec)
            throws IOException, CanceledExecutionException {
        // Nothing to do ...
    }
}
