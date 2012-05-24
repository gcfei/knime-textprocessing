/*
 * ------------------------------------------------------------------------
 *
 *  Copyright (C) 2003 - 2011
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
 * ---------------------------------------------------------------------
 * 
 * History
 *   29.07.2008 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.transformation.stringstodocument;

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

/**
 * Provides the dialog for the String to Document node with all necessary
 * dialog components.
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public class StringsToDocumentNodeDialog extends DefaultNodeSettingsPane {
    
    /**
     * @return Creates and returns an instance of 
     * <code>SettingsModelString</code> specifying the column which has to
     * be used as authors column.
     */
    static final SettingsModelString getAuthorsStringModel() {
        return new SettingsModelString(
                StringsToDocumentConfigKeys.CFGKEY_AUTHORSCOL, "");
    }
    
    /**
     * @return Creates and returns an instance of 
     * <code>SettingsModelString</code> specifying the separator string.
     */
    static final SettingsModelString getAuthorSplitStringModel() {
        return new SettingsModelString(
                StringsToDocumentConfigKeys.CFGKEY_AUTHORSPLIT_STR,
                StringsToDocumentConfig.DEF_AUTHORS_SPLITCHAR);
    }    

    /**
     * @return Creates and returns an instance of 
     * <code>SettingsModelString</code> specifying the column which has to
     * be used as title column.
     */
    static final SettingsModelString getTitleStringModel() {
        return new SettingsModelString(
                StringsToDocumentConfigKeys.CFGKEY_TITLECOL, "");
    }
    
    /**
     * @return Creates and returns an instance of 
     * <code>SettingsModelString</code> specifying the column which has to
     * be used as full text column.
     */
    static final SettingsModelString getTextStringModel() {
        return new SettingsModelString(
                StringsToDocumentConfigKeys.CFGKEY_TEXTCOL, "");
    }
    
    /**
     * @return Creates and returns an instance of 
     * <code>SettingsModelString</code> specifying the document source.
     */
    static final SettingsModelString getDocSourceModel() {
        return new SettingsModelString(
                StringsToDocumentConfigKeys.CFGKEY_DOCSOURCE,
                StringsToDocumentConfig.DEF_DOCUMENT_SOURCE);
    } 
    
    /**
     * @return Creates and returns an instance of 
     * <code>SettingsModelString</code> specifying the document category.
     */
    static final SettingsModelString getDocCategoryModel() {
        return new SettingsModelString(
                StringsToDocumentConfigKeys.CFGKEY_DOCCAT,
                StringsToDocumentConfig.DEF_DOCUMENT_CATEGORY);
    }
    
    /**
     * @return Creates and returns an instance of 
     * <code>SettingsModelString</code> specifying the document type.
     */
    static final SettingsModelString getTypeModel() {
        return new SettingsModelString(
                StringsToDocumentConfigKeys.CFGKEY_DOCTYPE,
                StringsToDocumentConfig.DEF_DOCUMENT_TYPE);
    }
    
    /**
     * @return Creates and returns an instance of 
     * <code>SettingsModelString</code> specifying the document category.
     */
    static final SettingsModelString getPubDatModel() {
        return new SettingsModelString(
                StringsToDocumentConfigKeys.CFGKEY_PUBDATE,
                StringsToDocumentConfig.DEF_DOCUMENT_PUBDATE);
    }
    
    /**
     * @return Creates and returns an instance of 
     * <code>SettingsModelBoolean</code> specifying whether a column is used
     * for category values or not. 
     */
    static final SettingsModelBoolean getUseCategoryColumnModel() {
        return new SettingsModelBoolean(
        StringsToDocumentConfigKeys.CFGKEY_USE_CATCOLUMN,
        StringsToDocumentConfig.DEF_USE_CATCOLUMN);
    }
    
    /**
     * @return Creates and returns an instance of 
     * <code>SettingsModelBoolean</code> specifying whether a column is used
     * for source values or not. 
     */
    static final SettingsModelBoolean getUseSourceColumnModel() {
        return new SettingsModelBoolean(
        StringsToDocumentConfigKeys.CFGKEY_USE_SOURCECOLUMN,
        StringsToDocumentConfig.DEF_USE_SOURCECOLUMN);
    }
    
    /**
     * @return Creates and returns an instance of 
     * <code>SettingsModelString</code> specifying the column with the category
     * values.
     */
    static final SettingsModelString getCategoryColumnModel() {
        return new SettingsModelString(
                StringsToDocumentConfigKeys.CFGKEY_CATCOLUMN, "");
    }

    /**
     * @return Creates and returns an instance of 
     * <code>SettingsModelString</code> specifying the column with the source
     * values.
     */
    static final SettingsModelString getSourceColumnModel() {
        return new SettingsModelString(
                StringsToDocumentConfigKeys.CFGKEY_SOURCECOLUMN, "");
    }
    
    private SettingsModelString m_docCategoryModel;
    
    private SettingsModelString m_docSourceModel;
    
    private SettingsModelBoolean m_useCatColumnModel;
    
    private SettingsModelBoolean m_useSourceColumnModel;
    
    /**
     * Creates a new instance of <code>StringsToDocumentNodeDialog</code>.
     */
    @SuppressWarnings("unchecked")
    public StringsToDocumentNodeDialog() {
        
        createNewGroup("Text");
        setHorizontalPlacement(true);
        addDialogComponent(new DialogComponentColumnNameSelection(
                getTitleStringModel(), "Title", 0, StringValue.class));

        addDialogComponent(new DialogComponentColumnNameSelection(
                getTextStringModel(), "Full text", 0, StringValue.class));
        setHorizontalPlacement(false);
        closeCurrentGroup();
        
        createNewGroup("Authors");
        addDialogComponent(new DialogComponentColumnNameSelection(
                getAuthorsStringModel(), "Authors", 0, StringValue.class));
        
        addDialogComponent(new DialogComponentString(
                getAuthorSplitStringModel(), "Author names separator"));
        closeCurrentGroup();
        
        createNewGroup("Source and Category");
        m_docSourceModel = getDocSourceModel(); 
        addDialogComponent(new DialogComponentString(
                m_docSourceModel, "Document source"));
        setHorizontalPlacement(true);
        m_useSourceColumnModel = getUseSourceColumnModel();
        m_useSourceColumnModel.addChangeListener(
                new CategorySourceUsageChanceListener());
        addDialogComponent(new DialogComponentBoolean(
                m_useSourceColumnModel, "Use sources from column"));
        addDialogComponent(new DialogComponentColumnNameSelection(
                getSourceColumnModel(), "Document source column", 0, 
                StringValue.class));
        setHorizontalPlacement(false);
        
        m_docCategoryModel = getDocCategoryModel(); 
        addDialogComponent(new DialogComponentString(
                m_docCategoryModel, "Document category"));
        setHorizontalPlacement(true);
        m_useCatColumnModel = getUseCategoryColumnModel();
        m_useCatColumnModel.addChangeListener(
                new CategorySourceUsageChanceListener());
        addDialogComponent(new DialogComponentBoolean(
                m_useCatColumnModel, "Use categories from column"));
        addDialogComponent(new DialogComponentColumnNameSelection(
                getCategoryColumnModel(), "Document category column", 0, 
                StringValue.class));
        setHorizontalPlacement(false);
        closeCurrentGroup();
        
        createNewGroup("Type and Date");
        String[] types = DocumentType.asStringList().toArray(new String[0]);
        addDialogComponent(new DialogComponentStringSelection(
                getTypeModel(), "Document type", types));
        
        DialogComponentString dcs = new DialogComponentString(
                getPubDatModel(), "Publication date (dd-mm-yyyy)");
        dcs.setToolTipText("Date has to be specified like \"dd-mm-yyyy!\"");
        addDialogComponent(dcs);
        closeCurrentGroup();
    }
    
    /**
     * Enables and disables text fields of document source and category.
     * @author Kilian Thiel, KNIME.com, Berlin, Germany
     */
    class CategorySourceUsageChanceListener implements ChangeListener {

        /**
         * {@inheritDoc}
         */
        @Override
        public void stateChanged(final ChangeEvent e) {
                m_docCategoryModel.setEnabled(
                        !m_useCatColumnModel.getBooleanValue());
                m_docSourceModel.setEnabled(
                        !m_useSourceColumnModel.getBooleanValue());
        }
    }
}
