/* ------------------------------------------------------------------
 * This source code, its documentation and all appendant files
 * are protected by copyright law. All rights reserved.
 *
 * Copyright, 2003 - 2007
 * University of Konstanz, Germany
 * Chair for Bioinformatics and Information Mining (Prof. M. Berthold)
 * and KNIME GmbH, Konstanz, Germany
 *
 * You may not modify, publish, transmit, transfer or sell, reproduce,
 * create derivative works from, distribute, perform, display, or in
 * any way exploit any of the content, in whole or in part, except as
 * otherwise expressly permitted in writing by the copyright owner or
 * as specified in the license file distributed with this product.
 *
 * If you have any questions please contact the copyright holder:
 * website: www.knime.org
 * email: contact@knime.org
 * ---------------------------------------------------------------------
 * 
 * History
 *   08.08.2008 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.frequencies;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.ext.textprocessing.data.DocumentValue;

/**
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public class FrequenciesNodeSettingsPane extends DefaultNodeSettingsPane {

    /**
     * @return Creates and returns the string settings model containing
     * the name of the column with the documents to compute the frequencies.
     */
    public static SettingsModelString getDocumentColumnModel() {
        return new SettingsModelString(
                FrequenciesConfigKeys.CFG_KEY_DOCUMENT_COL,
                "Document");
    }
    
    /**
     * Creates new instance of <code>PreprocessingNodeSettingsPane</code>.
     */
    @SuppressWarnings("unchecked")
    public FrequenciesNodeSettingsPane() {
        removeTab("Options");
        createNewTabAt("Document Col", 1);
        
        DialogComponentColumnNameSelection comp1 = 
            new DialogComponentColumnNameSelection(getDocumentColumnModel(), 
                    "Document column", 0, DocumentValue.class);
        comp1.setToolTipText(
                "Column has to contain documents to compute frequiency of!");
        addDialogComponent(comp1);        
    }
}