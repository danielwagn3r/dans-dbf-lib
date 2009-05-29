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
 * Represents a record in a table.  A record basically maps a <tt>String</tt>
 * key to a value object for a specified row in a table.  The type of the value object depends
 * on the field type.  To find out which DBF types map to which Java types, see
 * {@link Type}.
 * <p>
 * Values that are too large to fit in their designated fields will cause a {@link ValueTooLargeException}.
 *
 * @author Jan van Mansum
 * @author Vesa Åkerman
 */
public class Record
{
    private final Map<String, Value> valueMap;

    /**
     * Creates a new Record object.  <tt>aValueMap</tt> must specify the values
     * for the fields in the record.  The concrete <tt>Value</tt> subclasses must
     * be compatible with the corresponding DBF field types, otherwise {@link DataMismatchException}
     * is thrown when trying to add the record.
     * <p>
     * The following is a table of the <tt>Value</tt> subclasses, the DBF field types and
     * the result of passing the one as a value for the other:
     *
     * <table border="1" cellpadding="4">
     * <tr>
     *      <td>&nbsp;</td>
     *      <td><b>CHARACTER</b></td>
     *      <td><b>LOGICAL</b></td>
     *      <td><b>NUMBER</b></td>
     *      <td><b>FLOAT</b></td>
     *      <td><b>DATE</b></td>
     *      <td><b>MEMO</b></td>
     *      <td><b>BINARY</b></td>
     *      <td><b>GENERAL</b></td>
     * </tr>
     * <tr>
     *      <td><b>StringValue</b></td>
     *      <td bgcolor="lightgreen">Accepted if within maximum length</td>
     *      <td bgcolor="lightgreen">Accepted if one of "Y", "N", "T", "F" or a space, no leading/trailing spaces allowed</td>
     *      <td bgcolor="lightgreen">Accepted if a valid number, that fits in the field and has exactly the number
     *          of decimals as the field's decimal count</td>
     *      <td bgcolor="lightgreen">See NUMBER</td>
     *      <td bgcolor="lightgreen">Accepted if in the format
     *          YYYYMMDD.  No leading or trailing spaces.
     *          No check is done whether the date is itself valid.</td>
     *      <td bgcolor="lightgreen">Accepted</td>
     *      <td bgcolor="lightgreen">Accepted</td>
     *      <td bgcolor="lightgreen">Accepted</td>
     * </tr>
     * <tr>
     *      <td><b>BooleanValue</b></td>
     *      <td bgcolor="lightgreen">Accepted, Y or N written as first character of the field</td>
     *      <td bgcolor="lightgreen">Accepted, Y or N written</td>
     *      <td bgcolor="pink">DME)<sup>*</sup></td>
     *      <td bgcolor="pink">DME</td>
     *      <td bgcolor="pink">DME</td>
     *      <td bgcolor="lightgreen">Accepted</td>
     *      <td bgcolor="lightgreen">Accepted</td>
     *      <td bgcolor="lightgreen">Accepted</td>
     * </tr>
     * <tr>
     *      <td><b>NumberValue</b></td>
     *      <td bgcolor="lightgreen">Accepted, if the number fits in the field</td>
     *      <td bgcolor="pink">DME</td>
     *      <td bgcolor="lightgreen">Accepted, if the digits before the decimal point and the minus sign (if any) together
     *          do not occupy more space than reserved for them by the field.  If there are too many digits after the
     *          decimal point they are rounded</td>
     *      <td bgcolor="lightgreen">See NUMBER</td>
     *      <td bgcolor="pink">DME</td>
     *      <td bgcolor="lightgreen">Accepted</td>
     *      <td bgcolor="lightgreen">Accepted</td>
     *      <td bgcolor="lightgreen">Accepted</td>
     * </tr>
     * <tr>
     *      <td><b>DateValue</b></td>
     *      <td bgcolor="lightgreen">Accepted, if the CHARACTER field is at least 10 long (the size of a DATE field in DBF).</td>
     *      <td bgcolor="pink">DME</td>
     *      <td bgcolor="pink">DME</td>
     *      <td bgcolor="pink">DME</td>
     *      <td bgcolor="lightgreen">Accepted</td>
     *      <td bgcolor="lightgreen">Accepted, Date written as YYYYMMDD</td>
     *      <td bgcolor="lightgreen">Accepted, Date written as YYYYMMDD</td>
     *      <td bgcolor="lightgreen">Accepted, Date written as YYYYMMDD</td>
     * </tr>
     * <tr>
     *      <td><b>ByteArrayValue</b></td>
     *      <td bgcolor="pink">DME</td>
     *      <td bgcolor="pink">DME</td>
     *      <td bgcolor="pink">DME</td>
     *      <td bgcolor="pink">DME</td>
     *      <td bgcolor="pink">DME</td>
     *      <td bgcolor="lightgreen">Accepted, Date written as YYYYMMDD</td>
     *      <td bgcolor="lightgreen">Accepted, Date written as YYYYMMDD</td>
     *      <td bgcolor="lightgreen">Accepted, Date written as YYYYMMDD</td>
     * </tr>
     * </table>
     * )<sup>*</sup> DataMismatchException
     *
     * @param aValueMap the mapping from field name to field value
     */
    public Record(final Map<String, Value> aValueMap)
    {
        valueMap = aValueMap;
    }

    /**
     * Returns the raw field value.  The raw field value is the bytes as stored
     * in the DBF file.  If the value is empty <tt>null</tt> or a series of
     * ASCII spaces may be returned.
     *
     * @param aField the field for which to get the raw value
     *
     * @return a byte array
     *
     * @throws ValueTooLargeException if the value was too large to be read
     */
    public byte[] getRawValue(final Field aField)
                       throws DbfLibException
    {
        final Value v = valueMap.get(aField.getName());

        if (v == null)
        {
            return null;
        }

        return v.getRawValue(aField);
    }

    /**
     * Returns the value as a Java object.  The type of Java object returned depends
     * on the field type in the xBase database.  See {@link Type} for the mapping between
     * the two.
     *
     * @param aFieldName the field for which to get the value
     *
     * @return a Java object
     */
    public Object getTypedValue(final String aFieldName)
    {
        final Value v = valueMap.get(aFieldName);

        if (v == null)
        {
            return null;
        }

        return v.getTypedValue();
    }

    /**
     * Returns the value of the specified field as a <tt>Number</tt>.  The exact class
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
     * <tt>false</tt> will be returned even if the values represent the same logical value.
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
        return (Number) getTypedValue(aFieldName);
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
        return (String) getTypedValue(aFieldName);
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
        return (Boolean) getTypedValue(aFieldName);
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
        return (Date) getTypedValue(aFieldName);
    }
}
