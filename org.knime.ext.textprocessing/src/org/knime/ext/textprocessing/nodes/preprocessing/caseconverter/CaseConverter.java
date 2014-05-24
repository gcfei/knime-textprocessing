/*
 * ------------------------------------------------------------------------
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
 *   13.08.2007 (Kilian Thiel): created
 */
package org.knime.ext.textprocessing.nodes.preprocessing.caseconverter;

import java.util.ArrayList;
import java.util.List;

import org.knime.ext.textprocessing.data.Term;
import org.knime.ext.textprocessing.data.Word;
import org.knime.ext.textprocessing.nodes.preprocessing.StringPreprocessing;
import org.knime.ext.textprocessing.nodes.preprocessing.TermPreprocessing;

/**
 *
 * @author Kilian Thiel, University of Konstanz
 */
public class CaseConverter implements TermPreprocessing, StringPreprocessing {

    /**
     * Constant for lower case conversion.
     */
    public static final String LOWER_CASE = "Lower case";

    /**
     * Constant for upper case conversion.
     */
    public static final String UPPER_CASE = "Upper case";


    private String m_case = CaseConverter.LOWER_CASE;

    /**
     * Creates new instance of <code>CaseConverter</code> with given case
     * to convert to.
     *
     * @param caseConversion The case to convert to.
     */
    public CaseConverter(final String caseConversion) {
        m_case = caseConversion;
    }

    /**
     * Creates new instance of <code>CaseConverter</code> which converts to
     * lower case by default.
     *
     */
    public CaseConverter() {
        this(CaseConverter.LOWER_CASE);
    }

    /**
     * @param caseConversion The case to convert to.
     */
    public void setCase(final String caseConversion) {
        m_case = caseConversion;
    }

    /**
     * @return The case to convert to.
     */
    public String getCase() {
        return m_case;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Term preprocessTerm(final Term term) {
        List<Word> words = term.getWords();
        List<Word> newWords = new ArrayList<Word>();
        for (Word w : words) {
            newWords.add(new Word(CaseConverter.convert(w.getWord(), m_case), w.getWhitespaceSuffix()));
        }
        return new Term(newWords, term.getTags(), term.isUnmodifiable());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String preprocessString(final String str) {
        return CaseConverter.convert(str, m_case);
    }

    /**
     * Converts the case of the given string to lower or upper case depending
     * on the second string parameter, which specifies whether the conversion
     * is to lower case or to upper case.
     *
     * @param str The string to convert
     * @param convCase The string specifying the was of conversion, to upper
     * or to lower case.
     * @return The converted string.
     */
    public static String convert(final String str, final String convCase) {
        String newTerm;
        if (convCase.equals(CaseConverter.LOWER_CASE)) {
            newTerm = str.toLowerCase();
        } else {
            newTerm = str.toUpperCase();
        }
        return newTerm;
    }
}
