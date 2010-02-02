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
 * ---------------------------------------------------------------------
 * 
 * History
 *   14.10.2008 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.transformation.stringtoterm;

import org.knime.core.data.StringValue;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

/**
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public class StringToTermNodeDialog extends DefaultNodeSettingsPane {

    /**
     * Creates and returns the settings model, storing the column name of the
     * string column.
     * @return The settings model with the column name of the string column. 
     */
    static final SettingsModelString getStringColModel() {
        return new SettingsModelString(StringToTermConfigKeys.STRING_COLNAME,
                "");
    }
    
    /**
     * Creates new instance of <code>StringToTermNodeDialog</code>.
     */
    @SuppressWarnings("unchecked")
    public StringToTermNodeDialog() {
        addDialogComponent(new DialogComponentColumnNameSelection(
                getStringColModel(), "String column", 0, 
                StringValue.class));
    }
}
