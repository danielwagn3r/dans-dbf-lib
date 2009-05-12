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
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Locale;

/**
 * Represents a number value in a record.
 *
 * @author Jan van Mansum
 */
public class NumberValue
    extends Value
{
    /**
     * Creates a new NumberValue object.
     *
     * @param aNumber a number
     */
    public NumberValue(final Number aNumber)
    {
        super(aNumber);
    }

    NumberValue(final byte[] aRaw)
    {
        super(aRaw);
    }

    @Override
    protected Object doGetTypedValue()
    {
        final String stringValue = new String(raw).trim();

        if (stringValue.isEmpty() || stringValue.equals("."))
        {
            return null;
        }

        int decimalPointIndex = stringValue.indexOf('.');

        if (decimalPointIndex == -1)
        {
            /*
             * Values less than 10 digits long can always be represented in an Integer, because
             * Integer.MAX_VALUE = 2 147 483 647 ...
             */
            if (stringValue.length() < 10)
            {
                return Integer.parseInt(stringValue);
            } /*
            * Longer values MAY need a Long.  To be on the safe side, we always use Long here.
            * Long can accomodate at least 18 digits.
            *
            * Long.MAX_VALUE = 9 223 372 036 854 77raFile5 807
            */
            else if (stringValue.length() < 19)
            {
                return Long.parseLong(stringValue);
            } /*
            * If all else fails, use a BigInteger.
            */
            else
            {
                return new BigInteger(stringValue);
            }
        }

        /*
         * Not sure yet what number of digits is safe to parse a value into a double.
         * 14 seems to be reasonably safe, but this needs to be proved.
         */
        if (stringValue.length() < 14)
        {
            return Double.parseDouble(stringValue);
        }

        /*
         * BigDecimal can hold anything.
         */
        return new BigDecimal(stringValue);
    }

    @Override
    protected byte[] doGetRawValue(final Field aField)
                            throws ValueTooLargeException
    {
        final Number numberValue = (Number) typed;

        /*
         * If too large to fit, throw exception.
         */
        int nrPositionsForDecimals = aField.getDecimalCount() == 0 ? 0 : aField.getDecimalCount() + 1;

        if (Util.getSignWidth(numberValue) + Util.getNumberOfIntDigits(numberValue) > aField.getLength()
                - nrPositionsForDecimals)
        {
            throw new ValueTooLargeException("Number does not fit in field: " + numberValue);
        }

        final ByteArrayOutputStream bos = new ByteArrayOutputStream();

        try
        {
            final byte[] bytes = String.format(Locale.US,
                                               aField.getFormatString(),
                                               numberValue).getBytes();

            bos.write(bytes);
            bos.write(Util.repeat((byte) 0x00, aField.getLength() - bytes.length));
        }
        catch (IOException ex)
        {
            throw new Error("Programming error: writing to ByteOutputStream should never cause and IOException", ex);
        }

        return bos.toByteArray();
    }
}
