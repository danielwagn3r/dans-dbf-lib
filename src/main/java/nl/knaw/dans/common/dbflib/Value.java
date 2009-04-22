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


/**
 * Represents a value that can be stored in a record.  Values can be created by
 * instantiating sub-classes of this type.
 *
 * @author Jan van Mansum
 */
public abstract class Value
{
    protected byte[] raw;
    protected Object typed;

    Value(final byte[] aRawValue)
    {
        raw = aRawValue;
        typed = null;
    }

    Value(final Object aTypedValue)
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
                throws ValueTooLargeException
    {
        if (raw == null && typed != null)
        {
            raw = doGetRawValue(aField);
        }

        return raw;
    }

    protected abstract Object doGetTypedValue();

    protected abstract byte[] doGetRawValue(Field aField)
                                     throws ValueTooLargeException;
}
