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
 *
 * @author Jan van Mansum
 */
public class ByteArrayValue
    extends Value
{
    /**
     * Creates a new ByteArrayValue object.
     *
     * @param aBinaryValue DOCUMENT ME!
     */
    public ByteArrayValue(final byte[] aBinaryValue)
    {
        super(aBinaryValue);
    }

    @Override
    protected Object doGetTypedValue()
    {
        return raw;
    }

    @Override
    protected byte[] doGetRawValue(Field aField)
                            throws ValueTooLargeException
    {
        return raw;
    }
}
