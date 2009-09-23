/* ------------------------------------------------------------------
 * This source code, its documentation and all appendant files
 * are protected by copyright law. All rights reserved.
 *
 * Copyright, 2003 - 2009
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
 *   14.08.2007 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.preprocessing.stopwordfilter;

/**
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public final class StopwordFilterConfigKeys {
    
    private StopwordFilterConfigKeys() { }

    /**
     * Config Key for file containing the stop words.
     */
    public static final String CFGKEY_FILE = "File";
    
    /**
     * Config Key for the activation of case sensitivity.
     */
    public static final String CFGKEY_CASE_SENSITIVE = "CS";

    /**
     * Config Key for the "use build in list" flag.
     */
    public static final String CFGKEY_USE_BUILDIN_LIST = "UseBuildInList";

    /**
     * Config Key for the selected build in list.
     */
    public static final String CFGKEY_BUILDIN_LIST = "BuildInList";   
}
