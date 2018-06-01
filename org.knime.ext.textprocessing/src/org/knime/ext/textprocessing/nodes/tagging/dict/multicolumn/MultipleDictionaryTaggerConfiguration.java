/*
 * ------------------------------------------------------------------------
 *
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
 *   Apr 11, 2018 (Julian Bunzel): created
 */
package org.knime.ext.textprocessing.nodes.tagging.dict.multicolumn;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.knime.core.data.DataRow;
import org.knime.core.data.StringValue;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.ext.textprocessing.nodes.tagging.DocumentTaggerConfiguration;

/**
 * Stores {@link DocumentTaggerConfiguration}s and their identifiers in a map.
 *
 * @author Julian Bunzel, KNIME GmbH, Berlin, Germany
 * @since 3.6
 */
class MultipleDictionaryTaggerConfiguration {

    /**
     * List of {@code DictionaryTaggerConfiguration}s containing the configurations for all dictionaries used for
     * tagging.
     */
    private final List<DictionaryTaggerConfiguration> m_configs = new ArrayList<>();

    /**
     * List of {@code DictionaryTaggerConfiguration}s containing the configurations for all dictionaries coming from
     * columns that are available in the DataTableSpec.
     */
    private final List<DictionaryTaggerConfiguration> m_validConfigs = new ArrayList<>();

    /**
     * List of {@code DictionaryTaggerConfiguration}s containing the configurations for all dictionaries coming from
     * columns that are not available in the DataTableSpec.
     */
    private final List<DictionaryTaggerConfiguration> m_invalidConfigs = new ArrayList<>();

    /**
     * Warning message which is shown, if there are columns selected in the dialog that are not in the DataTableSpec
     * anymore.
     */
    private String m_warningMessage = null;

    /**
     * Creates an instance of {@code MultipleDocumentTaggerSettings}.
     *
     * @param settings The settings to store.
     * @throws InvalidSettingsException, if node settings could not be retrieved.
     */
    MultipleDictionaryTaggerConfiguration(final NodeSettingsRO settings) throws InvalidSettingsException {
        for (String identifier : settings) {
            NodeSettingsRO col = settings.getNodeSettings(identifier);
            m_configs.add(DictionaryTaggerConfiguration.createFrom(col));
        }
    }

    /**
     * Writes the settings to a {@code NodeSettingsWO} object.
     *
     * @param settings The {@code NodeSettingsWO} to write to.
     */
    final void save(final NodeSettingsWO settings) {
        for (DictionaryTaggerConfiguration entry : m_configs) {
            NodeSettingsWO subSub = settings.addNodeSettings(entry.getColumnName());
            entry.saveSettingsTo(subSub);
        }
    }

    /**
     * Validates the {@code DictionaryTaggerConfiguration}s on a given {@link BufferedDataTable}.
     *
     * @param inData The {@code BufferedDataTable} containing the dictionaries.
     */
    final void validate(final BufferedDataTable inData) {
        m_warningMessage = null;
        m_validConfigs.clear();
        m_invalidConfigs.clear();
        List<String> invalidColumns = new ArrayList<>();
        for (DictionaryTaggerConfiguration config : m_configs) {
            config.setEntities(null);
            final int dictIndex = inData.getDataTableSpec().findColumnIndex(config.getColumnName());
            if (dictIndex >= 0) {
                Set<String> dictionary = new HashSet<>();
                for (DataRow row : inData) {
                    if (!row.getCell(dictIndex).isMissing()) {
                        dictionary.add(((StringValue)row.getCell(dictIndex)).getStringValue());
                    }
                }
                config.setEntities(dictionary);
                m_validConfigs.add(config);
            } else {
                m_invalidConfigs.add(config);
                invalidColumns.add(config.getColumnName());
            }
        }
        if (!invalidColumns.isEmpty()) {
            m_warningMessage = "Could not find dictionary column(s) " + invalidColumns.toString() + " in input table.";
        }
    }

    /**
     * Returns list of {@code DictionaryTaggerConfiguration}s containing the configurations for all dictionaries coming
     * from columns that are available in the DataTableSpec.
     *
     * @return Returns valid configurations.
     */
    final List<DictionaryTaggerConfiguration> getValidConfigs() {
        return m_validConfigs;
    }

    /**
     * Returns list of {@code DictionaryTaggerConfiguration}s containing the configurations for all dictionaries coming
     * from columns that are not available in the DataTableSpec anymore.
     *
     * @return Returns invalid configurations.
     */
    final List<DictionaryTaggerConfiguration> getInvalidConfigs() {
        return m_invalidConfigs;
    }

    /**
     * Returns the warning message which is shown, if there are columns selected in the dialog that are not in the
     * DataTableSpec anymore.
     *
     * @return The warning message.
     */
    final String getWarningMessage() {
        return m_warningMessage;
    }
}
