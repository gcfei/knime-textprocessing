/*
 * ------------------------------------------------------------------------
 *
 *  Copyright by KNIME GmbH, Konstanz, Germany
 *  Website: http://www.knime.org; Email: contact@knime.org
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
 * ---------------------------------------------------------------------
 *
 * History
 *   17.01.2017 (Julian): created
 */
package org.knime.ext.textprocessing.nodes.transformation.documentdataassigner;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.knime.core.data.StringValue;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.DialogComponentString;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.ext.textprocessing.data.DocumentType;
import org.knime.ext.textprocessing.data.DocumentValue;

/**
 * The node dialog for the Document Data Assigner.
 *
 * @author Julian Bunzel, KNIME.com, Berlin, Germany
 */
public class DocumentDataAssignerNodeDialog extends DefaultNodeSettingsPane {

    /**
     * @return Creates and returns an instance of {@SettingsModelString} containing the document column name.
     */
    static final SettingsModelString getDocumentColumnModel() {
        return new SettingsModelString(DocumentDataAssignerConfigKeys.CFGKEY_DOCCOL, "");
    }

    /**
     * @return Creates and returns an instance of {@SettingsModelString} containing the authors column name.
     */
    static final SettingsModelString getAuthorsColumnModel() {
        return new SettingsModelString(DocumentDataAssignerConfigKeys.CFGKEY_AUTHORSCOL, "");
    }

    /**
     * @return Creates and returns an instance of {@SettingsModelString} containing the category column name.
     */
    static final SettingsModelString getCategoryColumnModel() {
        return new SettingsModelString(DocumentDataAssignerConfigKeys.CFGKEY_CATCOLUMN, "");
    }

    /**
     * @return Creates and returns an instance of {@SettingsModelString} containing the source column name.
     */
    static final SettingsModelString getSourceColumnModel() {
        return new SettingsModelString(DocumentDataAssignerConfigKeys.CFGKEY_SOURCECOLUMN, "");
    }

    /**
     * @return Creates and returns an instance of {@SettingsModelString} containing the publication date column name.
     */
    static final SettingsModelString getPubDateColumnModel() {
        return new SettingsModelString(DocumentDataAssignerConfigKeys.CFGKEY_PUBDATECOL, "");
    }

    /**
     * @return Creates and returns an instance of {@SettingsModelBoolean} containing the value whether to use the
     *         authors column or not.
     */
    static final SettingsModelBoolean getUseAuthorsColumnModel() {
        return new SettingsModelBoolean(DocumentDataAssignerConfigKeys.CFGKEY_USE_AUTHORSCOLUMN,
            DocumentDataAssignerConfig.DEF_USE_AUTHORSCOLUMN);
    }

    /**
     * @return Creates and returns an instance of {@SettingsModelBoolean} containing the value whether to use the
     *         category column or not.
     */
    static final SettingsModelBoolean getUseCategoryColumnModel() {
        return new SettingsModelBoolean(DocumentDataAssignerConfigKeys.CFGKEY_USE_CATCOLUMN,
            DocumentDataAssignerConfig.DEF_USE_CATCOLUMN);
    }

    /**
     * @return Creates and returns an instance of {@SettingsModelBoolean} containing the value whether to use the source
     *         column or not.
     */
    static final SettingsModelBoolean getUseSourceColumnModel() {
        return new SettingsModelBoolean(DocumentDataAssignerConfigKeys.CFGKEY_USE_SOURCECOLUMN,
            DocumentDataAssignerConfig.DEF_USE_SOURCECOLUMN);
    }

    /**
     * @return Creates and returns an instance of {@SettingsModelBoolean} containing the value whether to use the
     *         publication date column or not.
     */
    static final SettingsModelBoolean getUsePubDateColumnModel() {
        return new SettingsModelBoolean(DocumentDataAssignerConfigKeys.CFGKEY_USE_PUBDATECOLUMN,
            DocumentDataAssignerConfig.DEF_USE_PUBDATECOLUMN);
    }

    /**
     * @return Creates and returns an instance of {@SettingsModelString} containing the document source.
     */
    static final SettingsModelString getSourceModel() {
        return new SettingsModelString(DocumentDataAssignerConfigKeys.CFGKEY_DOCSOURCE,
            DocumentDataAssignerConfig.DEF_DOCUMENT_SOURCE);
    }

    /**
     * @return Creates and returns an instance of {@SettingsModelString} containing the document category.
     */
    static final SettingsModelString getCategoryModel() {
        return new SettingsModelString(DocumentDataAssignerConfigKeys.CFGKEY_DOCCAT,
            DocumentDataAssignerConfig.DEF_DOCUMENT_CATEGORY);
    }

    /**
     * @return Creates and returns an instance of {@SettingsModelString} containing the delimiter for author names.
     */
    static final SettingsModelString getAuthorsSplitStringModel() {
        return new SettingsModelString(DocumentDataAssignerConfigKeys.CFGKEY_AUTHORSPLIT_STR,
            DocumentDataAssignerConfig.DEF_AUTHORSSPLIT_STR);
    }

    /**
     * @return Creates and returns an instance of {@SettingsModelString} containing the document type.
     */
    static final SettingsModelString getTypeModel() {
        return new SettingsModelString(DocumentDataAssignerConfigKeys.CFGKEY_DOCTYPE,
            DocumentDataAssignerConfig.DEF_DOCUMENT_TYPE);
    }

    /**
     * @return Creates and returns an instance of {@SettingsModelString} containing the publication date.
     */
    static final SettingsModelString getPubDateModel() {
        return new SettingsModelString(DocumentDataAssignerConfigKeys.CFGKEY_PUBDATE,
            DocumentDataAssignerConfig.DEF_DOCUMENT_PUBDATE);
    }

    /**
     * @return Creates and returns an instance of {@SettingsModelBoolean} containing the value whether to replace the
     *         old document column or append a new one.
     */
    static final SettingsModelBoolean getReplaceDocColumnModel() {
        return new SettingsModelBoolean(DocumentDataAssignerConfigKeys.CFGKEY_REPLACE_DOCCOL,
            DocumentDataAssignerConfig.DEF_REPLACE_DOCCOL);
    }

    /**
     * @return Creates and returns an instance of {@SettingsModelBoolean} containing the value whether to replace the
     *         old document column or append a new one.
     */
    static final SettingsModelBoolean getAppendDocColumnModel() {
        return new SettingsModelBoolean(DocumentDataAssignerConfigKeys.CFGKEY_APPEND_DOCCOL,
            DocumentDataAssignerConfig.DEF_APPEND_DOCCOL);
    }

    /**
     * @return Creates and returns an instance of {@SettingsModelString} containing the publication date.
     */
    static final SettingsModelString getReplacedColNameModel() {
        return new SettingsModelString(DocumentDataAssignerConfigKeys.CFGKEY_REPLACE_COLNAME,
            DocumentDataAssignerConfig.DEF_REPLACE_COLNAME);
    }

    /**
     * @return Creates and returns an instance of {@SettingsModelString} containing the publication date.
     */
    static final SettingsModelString getAppendedColNameModel() {
        return new SettingsModelString(DocumentDataAssignerConfigKeys.CFGKEY_APPEND_COLNAME,
            DocumentDataAssignerConfig.DEF_APPEND_COLNAME);
    }

    private SettingsModelBoolean m_useAuthorsColumnModel = getUseAuthorsColumnModel();

    private SettingsModelString m_authorsColumnModel = getAuthorsColumnModel();

    private SettingsModelString m_sourceModel = getSourceModel();

    private SettingsModelString m_sourceColumnModel = getSourceColumnModel();

    private SettingsModelBoolean m_useSourceColumnModel = getUseSourceColumnModel();

    private SettingsModelString m_categoryModel = getCategoryModel();

    private SettingsModelString m_categoryColumnModel = getCategoryColumnModel();

    private SettingsModelBoolean m_useCategoryColumnModel = getUseCategoryColumnModel();

    private SettingsModelString m_pubDateModel = getPubDateModel();

    private SettingsModelBoolean m_usePubDateColumnModel = getUsePubDateColumnModel();

    private SettingsModelString m_pubDateColumnModel = getPubDateColumnModel();

    private SettingsModelString m_authorsSplitStrModel = getAuthorsSplitStringModel();

    private SettingsModelBoolean m_replaceDocColModel = getReplaceDocColumnModel();

    private SettingsModelBoolean m_appendDocColModel = getAppendDocColumnModel();

    private SettingsModelString m_replacedColNameModel = getReplacedColNameModel();

    private SettingsModelString m_appendedColNameModel = getAppendedColNameModel();

    /**
     *
     */
    @SuppressWarnings("unchecked")
    public DocumentDataAssignerNodeDialog() {

        // dialog for document column selection and author
        createNewGroup("Text");
        addDialogComponent(new DialogComponentColumnNameSelection(getDocumentColumnModel(), "Document column", 0,
            DocumentValue.class));
        setHorizontalPlacement(true);
        m_useAuthorsColumnModel.addChangeListener(new ChangeStateListener());
        addDialogComponent(new DialogComponentBoolean(m_useAuthorsColumnModel, "Use authors from column"));
        addDialogComponent(
            new DialogComponentColumnNameSelection(m_authorsColumnModel, "Authors column", 0, StringValue.class));
        setHorizontalPlacement(false);
        setHorizontalPlacement(true);
        addDialogComponent(new DialogComponentString(m_authorsSplitStrModel, "Author names separator"));
        closeCurrentGroup();

        // dialog for source and category
        createNewGroup("Source and Category");
        addDialogComponent(new DialogComponentString(m_sourceModel, "Document source"));
        setHorizontalPlacement(false);
        setHorizontalPlacement(true);
        m_useSourceColumnModel.addChangeListener(new ChangeStateListener());
        addDialogComponent(new DialogComponentBoolean(m_useSourceColumnModel, "Use sources from column"));
        addDialogComponent(new DialogComponentColumnNameSelection(m_sourceColumnModel, "Document source column", 0,
            StringValue.class));
        setHorizontalPlacement(false);
        addDialogComponent(new DialogComponentString(m_categoryModel, "Document category"));
        setHorizontalPlacement(true);
        m_useCategoryColumnModel.addChangeListener(new ChangeStateListener());
        addDialogComponent(new DialogComponentBoolean(m_useCategoryColumnModel, "Use categories from column"));
        addDialogComponent(new DialogComponentColumnNameSelection(m_categoryColumnModel, "Document category column", 0,
            StringValue.class));
        setHorizontalPlacement(false);
        closeCurrentGroup();

        // dialog for type and date
        createNewGroup("Type and Date");
        String[] types = DocumentType.asStringList().toArray(new String[0]);
        addDialogComponent(new DialogComponentStringSelection(getTypeModel(), "Document type", types));
        DialogComponentString dcs = new DialogComponentString(m_pubDateModel, "Publication date (dd-mm-yyyy)");
        dcs.setToolTipText("Date has to be specified like \"dd-mm-yyyy!\"");
        addDialogComponent(dcs);

        // dialog for publication date
        setHorizontalPlacement(true);
        m_usePubDateColumnModel.addChangeListener(new ChangeStateListener());
        addDialogComponent(new DialogComponentBoolean(m_usePubDateColumnModel, "Use publication date from column"));
        addDialogComponent(new DialogComponentColumnNameSelection(m_pubDateColumnModel, "Publication date column", 0,
            StringValue.class));
        closeCurrentGroup();

        // dialog for output column settings
        createNewGroup("Column Settings");
        m_replaceDocColModel.addChangeListener(new ChangeStateListener());
        m_appendDocColModel.addChangeListener(new ChangeStateListener());
        setHorizontalPlacement(true);
        addDialogComponent(new DialogComponentBoolean(m_replaceDocColModel, "Replace document column:"));
        addDialogComponent(new DialogComponentString(m_replacedColNameModel, ""));
        setHorizontalPlacement(false);
        setHorizontalPlacement(true);
        addDialogComponent(new DialogComponentBoolean(m_appendDocColModel, "Append document column:"));
        addDialogComponent(new DialogComponentString(m_appendedColNameModel, ""));
        closeCurrentGroup();
        checkState();

    }

    /** Sets the state of some SettingModels to enabled/disabled depending on the related 'use ... column' value. */
    private void checkState() {
        m_categoryColumnModel.setEnabled(m_useCategoryColumnModel.getBooleanValue());
        m_categoryModel.setEnabled(!m_useCategoryColumnModel.getBooleanValue());
        m_pubDateColumnModel.setEnabled(m_usePubDateColumnModel.getBooleanValue());
        m_pubDateModel.setEnabled(!m_usePubDateColumnModel.getBooleanValue());
        m_sourceColumnModel.setEnabled(m_useSourceColumnModel.getBooleanValue());
        m_sourceModel.setEnabled(!m_useSourceColumnModel.getBooleanValue());
        m_authorsColumnModel.setEnabled(m_useAuthorsColumnModel.getBooleanValue());
        m_authorsSplitStrModel.setEnabled(m_useAuthorsColumnModel.getBooleanValue());
        m_replacedColNameModel.setEnabled(m_replaceDocColModel.getBooleanValue());
        m_appendedColNameModel.setEnabled(m_appendDocColModel.getBooleanValue());
    }

    private void checkReplaced(final ChangeEvent e) {
        SettingsModelBoolean settingsModel = (SettingsModelBoolean)e.getSource();
        if (settingsModel.getConfigName().equals(DocumentDataAssignerConfigKeys.CFGKEY_REPLACE_DOCCOL)) {
            m_appendDocColModel.setBooleanValue(!m_replaceDocColModel.getBooleanValue());
        } else {
            m_replaceDocColModel.setBooleanValue(!m_appendDocColModel.getBooleanValue());
        }
    }

    class ChangeStateListener implements ChangeListener {
        /**
         * {@inheritDoc}
         */
        @Override
        public void stateChanged(final ChangeEvent e) {
            checkState();
            checkReplaced(e);
        }
    }
}
