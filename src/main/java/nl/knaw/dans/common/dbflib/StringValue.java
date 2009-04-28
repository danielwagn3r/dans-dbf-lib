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

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Represents a string value in a record (a CHARACTER or MEMO type field value).
 *
 * @author Jan van Mansum
 */
public class StringValue
    extends Value
{
    static final int MAX_CHARFIELD_LENGTH_DBASE = 253;

    /**
     * Creates a new StringValue object.
     *
     * @param aStringValue aString
     */
    public StringValue(final String aStringValue)
    {
        super(aStringValue);
    }

    StringValue(final byte[] aRawValue)
    {
        super(aRawValue);
    }

    @Override
    protected Object doGetTypedValue()
    {
        final ByteArrayOutputStream bos = new ByteArrayOutputStream(raw.length);

        for (int i = 0; i < raw.length; ++i)
        {
            if (raw[i] == (byte) 0x8d)
            {
                if (raw[++i] == (byte) 0x0a)
                {
                    continue;
                }
            }

            bos.write(raw[i]);
        }

        return new String(bos.toByteArray());
    }

    @Override
    protected byte[] doGetRawValue(final Field aField)
                            throws ValueTooLargeException
    {
        final int fieldLength = aField.getLength();
        final byte[] stringBytes = ((String) typed).getBytes();
        final ByteArrayOutputStream bos = new ByteArrayOutputStream(fieldLength);

        if (stringBytes.length > fieldLength && aField.getType() != Type.MEMO)
        {
            throw new ValueTooLargeException("Character string exceeds the allowed length for this character field ");
        }

        try
        {
            bos.write(stringBytes);

            /*
             * A memo has no length, but otherwise fill up to the maximum length with zeros.
             */
            if (aField.getType() != Type.MEMO)
            {
                bos.write(Util.repeat((byte) 0x00, fieldLength - stringBytes.length));
            }
        }
        catch (IOException ex)
        {
            throw new Error("Writing to ByteArrayOutputStream should not cause an IOException", ex);
        }

        return bos.toByteArray();
    }
}
