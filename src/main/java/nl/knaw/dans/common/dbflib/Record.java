/*
 *  Copyright 2009
 *  Data Archiving and Networked Services (DANS), Netherlands.
 *
 *  This file is part of DANS DBF Library.
 *
 *  DANS DBF Library is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  DANS DBF Library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with DANS DBF Library.  If not, see <http://www.gnu.org/licenses/>.
 */
package nl.knaw.dans.common.dbflib;

import java.util.Date;
import java.util.Map;

/**
 * Represents a record in a table.  A record is basically maps a <tt>java.lang.String</tt>
 * key to a value object for a specified row in a table.  The type of the value object depends
 * on the field type.  To find out which DBF types map to which Java types, see
 * {@link Type}.
 *
 * @author Jan van Mansum
 * @author Vesa Åkerman
 */
public class Record
{
    private final Map<String, Object> valueMap;

    /**
     * Creates a new Record object.  <tt>aValueMape</tt> specifies the value of each field in
     * the record.
     *
     * @param aValueMap a mapping if field names to values
     */
    public Record(final Map<String, Object> aValueMap)
    {
        valueMap = aValueMap;
    }

    /**
     * Returns the value of the specified field in the record as a <tt>java.lang.Object</tt>.
     * The type of the object returned depends on the type of data in the field.  For the mappings
     * of xBase types to Java types see {@link Type}.
     *
     * @param aFieldName the name of the field for which to retrieve the value
     *
     * @return a value
     */
    public Object getValue(final String aFieldName)
    {
        return valueMap.get(aFieldName);
    }

    /**
     * Returns the value of the specified field as a <tt>java.lang.Number</tt>.  The exact class
     * used depends on the size of the corresponding {@link Field} and its <tt>decimalCount</tt>
     * property.  If <tt>decimalCount</tt> is zero an integral type is returned, otherwise a fractional
     * type.  Depending on the size <tt>java.lang.Integer</tt>, <tt>java.lang.Long</tt> or
     * <tt>java.math.BigInteger</tt> is used as an integral type.  For non-integral types the
     * classes used are either <tt>java.lang.Double</tt> or <tt>java.math.BigDecimal</tt>.
     * <p>
     * It is not necessary to know the exact type used.  You can use the conversion methods on the
     * <tt>java.lang.Number</tt> class to convert the value before using it.  (E.g., <tt>Number.intValue()</tt>.)
     * Of course you do need to know whether the value will fit in the chosen type.  Note that
     * comparisons may fail if you do not first convert the values.  For instance if you compare
     * the a <tt>java.math.BigInteger</tt> with a <tt>long</tt> using the <tt>equals</tt> method,
     * <tt>false</tt> wil be returned even if the values represent the same logical value.
     * <p>
     * Example:
     * <pre>
     *
     * public Record searchSomeNum(final double val)
     * {
     *      //
     *      //... SOME CODE HERE THAT RETRIEVES table1 ...
     *      //
     *
     *      // Get a record iterator to loop over all the records.
     *      Iterator<Record> ri = table1.recordIterator();
     *
     *      // Search for the record with SOMENUM = val
     *      while(ri.hasNext())
     *      {
     *         Record r = ri.next();
     *         Number n = r.getNumberValue("SOMENUM");
     *
     *         // Convert n to a double before comparing it.
     *         if(n.doubleValue() == val)
     *         {
     *            return r;
     *         }
     *      }
     *
     *      return null;
     * }
     *
     * </pre>
     *
     * @param aFieldName the name of the field with numerical data
     *
     * @return a <tt>java.lang.Number</tt> object
     */
    public Number getNumberValue(final String aFieldName)
    {
        return (Number) getValue(aFieldName);
    }

    /**
     * Returns the specified value as a <tt>java.lang.String</tt> object.
     *
     * @param aFieldName the name of the field with character data
     *
     * @return a <tt>java.lang.String</tt> object
     */
    public String getStringValue(final String aFieldName)
    {
        return (String) getValue(aFieldName);
    }

    /**
     * Returns the specified value as a <tt>java.lang.Boolean</tt> object.
     *
     * @param aFieldName the name of the field with logical data
     *
     * @return a <tt>java.lang.Boolean</tt> object
     */
    public Boolean getBooleanValue(final String aFieldName)
    {
        return (Boolean) getValue(aFieldName);
    }

    /**
     * Returns the specified value as a <tt>java.util.Date</tt> object.
     *
     * @param aFieldName the name of the field with date data
     *
     * @return a <tt>java.util.Date</tt> object
     */
    public Date getDateValue(final String aFieldName)
    {
        return (Date) getValue(aFieldName);
    }
}
