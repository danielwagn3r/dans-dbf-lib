/*
 * Copyright 2009 Data Archiving and Networked Services (DANS), Netherlands.
 *
 * This file is part of DANS DBF Library.
 *
 * DANS DBF Library is free software: you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * DANS DBF Library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with DANS DBF Library. If
 * not, see <http://www.gnu.org/licenses/>.
 */
package nl.knaw.dans.common.dbflib;


/**
 * Represents a value that can be stored in a record. Values can be created by instantiating
 * sub-classes of this type. They have a raw and typed value. The raw value is the byte array that
 * is stored in the .DBF file. The typed value is a Java object. The class of this object is by
 * convention named in the first part of the Value's class name (e.g., <tt>BooleanValue</tt> has
 * <tt>Boolean</tt> as its typed value class).
 *
 * @author Jan van Mansum
 */
public abstract class Value
{
    protected byte[] raw;
    protected Object typed;

    /**
     * Constructs a <tt>Value</tt> object with the specified raw value. The subclass must take care
     * of converting the raw value to a Java object by implementing {@link #doGetTypedValue() }.
     *
     * @param aRawValue the bytes that constitute the raw value
     */
    protected Value(final byte[] aRawValue)
    {
        raw = aRawValue;
        typed = null;
    }

    /**
     * Constructs a <tt>Value</tt> object with the specified typed value, i.e. Java object. The
     * subclass must take care of converting the typed value to a byte array by implementing
     * {@link #doGetRawValue(nl.knaw.dans.common.dbflib.Field) }.
     *
     * @param aTypedValue the value as a Java object
     */
    protected Value(final Object aTypedValue)
    {
        raw = null;
        typed = aTypedValue;
    }

    Object getTypedValue()
    {
        if (typed == null && raw != null)
        {
            typed = doGetTypedValue();
        }

        return typed;
    }

    byte[] getRawValue(final Field aField)
                throws DbfLibException
    {
        if (raw == null && typed != null)
        {
            aField.validateTypedValue(typed);
            raw = doGetRawValue(aField);
        }

        return raw;
    }

    /**
     * Converts the raw bytes to a Java object. The class of Java object to create is determined by
     * the subclass of <tt>Value</tt>.
     *
     * @return the value as a Java object
     */
    protected abstract Object doGetTypedValue();

    /**
     * Converts the typed value to a byte array, according to the field specifications provided.
     *
     * @param aField the field specifications
     * @return a byte array containing the raw value
     * @throws nl.knaw.dans.common.dbflib.ValueTooLargeException if the value is too large for the
     *             field
     */
    protected abstract byte[] doGetRawValue(Field aField)
                                     throws ValueTooLargeException;
}
