/* 
========================================================================
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
 * -------------------------------------------------------------------
 * 
 * History
 *   15.12.2006 (Kilian Thiel): created
 */
package org.knime.ext.textprocessing.data;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Encapsulates a publication date of a
 * {@link org.knime.ext.textprocessing.data.Document} with the ability to handle
 * missing values. For example, an instance of <code>PublicationDate</code> can
 * be created without explicitly setting the day the month or even the year. If
 * only the publication year of a publication is known, an instance of
 * <code>PublicationDate</code> can be created nevertheless. If the complete
 * date is unknown a zero date can be created.
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public class PublicationDate implements Externalizable,
        Comparable<PublicationDate> {

    private int m_year = 0;

    private int m_month = 0;

    private int m_day = 0;

    /**
     * Creates a proper <code>PublicationDate</code> instance with the given
     * year, month and day values and returns it. If day is less or equals zero,
     * only the year and the month is taken into account, if the month is less
     * or equals zero too only the year is used to create the
     * <code>PublicationDate</code>. If the month number is not valid, i.e.
     * greater than 12 a <code>ParseException</code> is thrown.
     * 
     * @param year The year to create the <code>PublicationDate</code> with.
     * @param month The month to create the <code>PublicationDate</code> with.
     * @param day The day to create the <code>PublicationDate</code> with.
     * @return The new instance of <code>PublicationDate</code> with the given
     *         parameters.
     * @throws ParseException If any of the parameters is not valid, i.e. the
     *             number of the month is greater than 12 or the number of the
     *             day greater than 31 (or 30 according to the month).
     */
    public static PublicationDate createPublicationDate(final int year,
            final int month, final int day) throws ParseException {
        PublicationDate date = null;
        if (day > 0 && month > 0 && year > 0) {
            date = new PublicationDate(year, month, day);
        } else if (month > 0 && year > 0) {
            date = new PublicationDate(year, month);
        } else if (year > 0) {
            date = new PublicationDate(year);
        }

        return date;
    }

    /**
     * Creates a proper <code>PublicationDate</code> instance with the given
     * year, month and day values and returns it. If day is less or equals zero,
     * only the year and the month is taken into account. A
     * <code>ParseException</code> is thrown if the mont or day is not valid.
     * 
     * @param year The year to create the <code>PublicationDate</code> with.
     * @param month The month to create the <code>PublicationDate</code> with.
     * @param day The day to create the <code>PublicationDate</code> with.
     * @return The new instance of <code>PublicationDate</code> with the given
     *         parameters.
     * @throws ParseException If any of the parameters is not valid, i.e. the
     *             month is non of the 12 month names or the number of the day
     *             greater than 31 (or 30 according to the month).
     */
    public static PublicationDate createPublicationDate(final int year,
            final String month, final int day) throws ParseException {
        return createPublicationDate(year, monthStrToInt(month), day);
    }

    /**
     * Returns the proper number representation (1 - 12) for the given month
     * string. The string can be the complete name of the month like "February"
     * or the first three characters like "feb".
     * 
     * @param month A month represented as a string.
     * @return The number representation of the given month.
     */
    public static int monthStrToInt(final String month) {
        int m = 0;
        String monthLc = month.toLowerCase();
        if (monthLc.equals("jan") || monthLc.equals("january")) {
            m = 1;
        } else if (monthLc.equals("feb") || monthLc.equals("february")) {
            m = 2;
        } else if (monthLc.equals("mar") || monthLc.equals("march")) {
            m = 3;
        } else if (monthLc.equals("apr") || monthLc.equals("april")) {
            m = 4;
        } else if (monthLc.equals("may") || monthLc.equals("may")) {
            m = 5;
        } else if (monthLc.equals("jun") || monthLc.equals("june")) {
            m = 6;
        } else if (monthLc.equals("jul") || monthLc.equals("july")) {
            m = 7;
        } else if (monthLc.equals("aug") || monthLc.equals("august")) {
            m = 8;
        } else if (monthLc.equals("sep") || monthLc.equals("september")) {
            m = 9;
        } else if (monthLc.equals("oct") || monthLc.equals("october")) {
            m = 10;
        } else if (monthLc.equals("nov") || monthLc.equals("novemeber")) {
            m = 11;
        } else if (monthLc.equals("dec") || monthLc.equals("december")) {
            m = 12;
        }
        return m;
    }

    /**
     * Creates a new instance of <code>PublicationDate</code> with given year,
     * month and day. These parameters have to be valid, otherwise a
     * <code>ParseException</code> will be thrown.
     * 
     * @param year the dates year
     * @param month the dates month
     * @param day the dates day of month
     * @throws ParseException If given date parameters are not valid.
     */
    public PublicationDate(final int year, final int month, final int day)
            throws ParseException {
        // check default date
        if (year == 0 && month == 0 && day == 0) {
            m_year = 0;
            m_month = 0;
            m_day = 0;
        } else {
            Calendar cal = new GregorianCalendar();
            if (year > cal.get(Calendar.YEAR)) {
                throw new ParseException("Year " + year + " is not valid !", 0);
            }
            if (month > 12 || month < 1) {
                throw new ParseException("Month " + month + " is not valid !",
                        0);
            }
            if (day > 31 || day < 1) {
                throw new ParseException("Day " + day + " is not valid !", 0);
            }
            String dateStr = year + "/" + month + "/" + day;
            SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd");
            Date date = df.parse(dateStr);
            cal.setTime(date);

            m_year = cal.get(Calendar.YEAR);
            m_month = cal.get(Calendar.MONTH) + 1;
            m_day = cal.get(Calendar.DAY_OF_MONTH);
        }
    }

    /**
     * Creates a new instance of <code>PublicationDate</code> with given year.
     * and month. If given year is greater than the current year, or given month
     * is not valid a <code>IllegalArgumentException</code> will be thrown. The
     * day of month is set to zero.
     * 
     * @param year the dates year.
     * @param month the dates month.
     */
    public PublicationDate(final int year, final int month) {
        Calendar cal = new GregorianCalendar();
        if (year > cal.get(Calendar.YEAR)) {
            throw new IllegalArgumentException("Year " + year
                    + " is not valid !");
        }
        if (month > 12 || month < 1) {
            throw new IllegalArgumentException("Month " + month
                    + " is not valid !");
        }
        m_year = year;
        m_month = month;
    }

    /**
     * Creates a new instance of <code>PublicationDate</code> with given year.
     * If given year is greater than the current year, a
     * <code>IllegalArgumentException</code> will be thrown. The month and day
     * of month is set to zero.
     * 
     * @param year the dates year.
     */
    public PublicationDate(final int year) {
        Calendar cal = new GregorianCalendar();
        if (year > cal.get(Calendar.YEAR) || year <= 0) {
            throw new IllegalArgumentException("Year " + year
                    + " is not valid !");
        }
        m_year = year;
    }

    /**
     * Creates a new zeroed instance of <code>PublicationDate</code>.
     */
    public PublicationDate() { /* empty */
    }

    /**
     * Returns the String representation of a date, which is <i>yyyy-mm-dd</i>
     * by default. Missing values will be ignored, a zero or empty date is
     * represented by the String "0000-00-00".
     * 
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        String str;

        if (m_month > 0 && m_day > 0) {
            str = m_year + "-" + m_month + "-" + m_day;
        } else if (m_month > 0 && m_day < 1) {
            str = m_year + "-" + m_month;
        } else if (m_year > 0 && m_month < 1) {
            str = Integer.toString(m_year);
        } else {
            str = "0000-00-00";
        }

        return str;
    }

    /**
     * Returns the dates serialization String, which is similar to the String
     * returned by {@link PublicationDate#toString()} except that missing
     * values, like day or month, two zeros "00" will be replaced by zeros.
     * 
     * @return The dates serialization String.
     */
    public String serializationString() {
        String str;

        if (m_month > 0 && m_day > 0) {
            str = m_year + "-" + m_month + "-" + m_day;
        } else if (m_month > 0 && m_day < 1) {
            str = m_year + "-" + m_month + "-00";
        } else if (m_year > 0 && m_month < 1) {
            str = Integer.toString(m_year) + "-00-00";
        } else {
            str = "0000-00-00";
        }

        return str;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object o) {
        if (o == null) {
            return false;
        }
        if (!(o instanceof PublicationDate)) {
            return false;
        }
        PublicationDate d = (PublicationDate)o;

        if (d.getYear() != m_year) {
            return false;
        }
        if (d.getMonth() != m_month) {
            return false;
        }
        if (d.getDay() != m_day) {
            return false;
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int prime = 119;
        int hashCode = 1;

        hashCode *= prime + new Integer(m_year).hashCode();
        hashCode *= prime + new Integer(m_month).hashCode();
        hashCode *= prime + new Integer(m_day).hashCode();

        return hashCode;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(final PublicationDate o) throws ClassCastException {
        int yd = m_year - o.getYear();
        int md = m_month - o.getMonth();
        int dd = m_day - o.getDay();

        if (yd != 0) {
            return yd;
        } else if (md != 0) {
            return md;
        }
        return dd;
    }

    /**
     * @return the day
     */
    public int getDay() {
        return m_day;
    }

    /**
     * @return the month
     */
    public int getMonth() {
        return m_month;
    }

    /**
     * @return the year
     */
    public int getYear() {
        return m_year;
    }

    /**
     * @return The todays date formatted like "dd-MM-yyy".
     */
    public static final String getToday() {
        Date today = new Date();
        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");
        return format.format(today);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void writeExternal(final ObjectOutput out) throws IOException {
        out.writeInt(m_year);
        out.writeInt(m_month);
        out.writeInt(m_day);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void readExternal(final ObjectInput in) throws IOException,
            ClassNotFoundException {
        m_year = in.readInt();
        m_month = in.readInt();
        m_day = in.readInt();
    }
}
