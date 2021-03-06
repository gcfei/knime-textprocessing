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
 * Created on 23.10.2013 by Kilian Thiel
 */

package org.knime.ext.textprocessing.nodes.transformation.bow;

import java.util.Arrays;
import java.util.List;

import org.knime.core.data.DataTableSpec;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeDialog;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnFilter2;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.DialogComponentString;
import org.knime.core.node.defaultnodesettings.SettingsModelColumnFilter2;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.ext.textprocessing.data.DocumentValue;
import org.knime.ext.textprocessing.util.CommonColumnNames;

/**
 * The {@link NodeDialog} for the Bag Of Words Creator node. This node dialog provides a Document column selection, a
 * String component to define the name of the created Term column and general column selection to specify the output
 * columns.
 *
 * @author Kilian Thiel, KNIME AG, Zurich, Switzerland
 * @since 3.5
 */
final class BagOfWordsNodeDialog2 extends DefaultNodeSettingsPane {

    /**
     * Creates and returns a {@link SettingsModelString} containing the name of the column with the documents to create
     * the bag of words from.
     *
     * @return {@code SettingsModelString} containing the name of the document column.
     */
    static final SettingsModelString getDocumentColumnModel() {
        return new SettingsModelString(BagOfWordsConfigKeys2.CFG_KEY_DOCUMENT_COL, "");
    }

    /**
     * Creates and returns a {@link SettingsModelColumnFilter2} containing the name of the columns that will be carried
     * over to the output table.
     *
     * @return {@code SettingsModelColumnFilter2} containing the name of the columns that will be carried over to the
     *         output table.
     */
    static final SettingsModelColumnFilter2 getColumnSelectionModel() {
        return new SettingsModelColumnFilter2(BagOfWordsConfigKeys2.CFG_KEY_COLUMN_FILTER);
    }

    /**
     * Creates and returns a {@link SettingsModelString} containing the name of the term column that will be created.
     *
     * @return {@code SettingsModelString} containing the name of the term column.
     */
    static final SettingsModelString getTermColumnModel() {
        return new SettingsModelString(BagOfWordsConfigKeys2.CFG_KEY_TERM_COL, CommonColumnNames.DEF_TERM_COLNAME);
    }

    private DataTableSpec m_spec;

    private SettingsModelString m_termColumnModel = getTermColumnModel();

    private SettingsModelColumnFilter2 m_colSelectionModel = getColumnSelectionModel();

    /**
     * Constructor for class {@link BagOfWordsNodeDialog2}.
     */
    @SuppressWarnings("unchecked")
    BagOfWordsNodeDialog2() {

        setHorizontalPlacement(true);
        // document col to create bow from
        final DialogComponentColumnNameSelection docColSelectionComp =
            new DialogComponentColumnNameSelection(getDocumentColumnModel(), "Document column", 0, DocumentValue.class);
        docColSelectionComp.setToolTipText("Column containing the documents to create bow from");
        addDialogComponent(docColSelectionComp);

        // string component to define term column name
        final DialogComponentString termColStringComp = new DialogComponentString(m_termColumnModel, "Term column");
        termColStringComp.setToolTipText("Name of the term column to be created");
        addDialogComponent(termColStringComp);

        setHorizontalPlacement(false);
        // column filter component to select output columns
        addDialogComponent(new DialogComponentColumnFilter2(m_colSelectionModel, 0));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void loadAdditionalSettingsFrom(final NodeSettingsRO settings, final DataTableSpec[] specs)
        throws NotConfigurableException {
        m_spec = specs[0];
        super.loadAdditionalSettingsFrom(settings, specs);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void saveAdditionalSettingsTo(final NodeSettingsWO settings) throws InvalidSettingsException {
        super.saveAdditionalSettingsTo(settings);
        if (checkIncludes(m_colSelectionModel, m_spec, m_termColumnModel.getStringValue())) {
            throw new InvalidSettingsException("Can't create new column '" + m_termColumnModel.getStringValue()
            + "' as input spec already contains column named '" + m_termColumnModel.getStringValue() + "'!");
        }
        if (m_termColumnModel.getStringValue() == null || m_termColumnModel.getStringValue().trim().isEmpty()) {
            throw new InvalidSettingsException("Can't create new term column! Column name can't be empty!");
        }
    }

    static boolean checkIncludes(final SettingsModelColumnFilter2 colFilter, final DataTableSpec spec, final String colName) {
        List<String> includes = Arrays.asList(colFilter.applyTo(spec).getIncludes());
        if (includes.contains(colName.trim())) {
            return true;
        }
        return false;
    }

}
