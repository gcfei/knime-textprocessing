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
 *   Aug 1, 2008 (Pierre-Francois Laquerre): created
 */
package org.knime.ext.textprocessing.util;

/**
 * Generic condition-testing interface used by collection filters.
 *
 * @author Pierre-Francois Laquerre, University of Konstanz
 * @param <T> the type of the objects to test the condition against
 */
public interface Condition<T> {
    /**
     * @param t the object to test
     * @return true if the test is successful, false otherwise
     */
    public boolean test(T t);
}
