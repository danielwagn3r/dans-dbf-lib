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
import java.io.DataInput;
import java.io.DataOutput;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Calendar;
import java.util.Date;

class Util
{
    static final int NR_OF_DIGITS_IN_YEAR = 4;

    private Util()
    {
        // Disallow instantiation.
    }

    static int changeEndianness(final int aInteger)
    {
        boolean isNegative = false;
        int i = aInteger;

        if (i < 0)
        {
            isNegative = true;
            i &= 0x7fffffff;
        }

        int first = i >>> 24;

        if (isNegative)
        {
            first |= 0x80;
        }

        i = aInteger & 0x00ff0000;

        int second = i >>> 16;

        i = aInteger & 0x0000ff00;

        int third = i >>> 8;

        int fourth = aInteger & 0x000000ff;

        return (fourth << 24) + (third << 16) + (second << 8) + first;
    }

    static short changeEndianness(short aShort)
    {
        boolean isNegative = false;
        short s = aShort;

        if (s < 0)
        {
            isNegative = true;
            s &= 0x7fff;
        }

        int first = s >>> 8;

        if (isNegative)
        {
            first |= 0x80;
        }

        int second = s & 0x00ff;

        return (short) ((second << 8) + first);
    }

    static String stripExtension(final String aFileName)
    {
        int pointIndex = aFileName.lastIndexOf('.');

        if ((pointIndex == -1) || (pointIndex == 0) || (pointIndex == (aFileName.length() - 1)))
        {
            return aFileName;
        }

        return aFileName.substring(0, pointIndex);
    }

    /**
     * Given a .DBF file, returns the accompanying .DBT file or <tt>null</tt> if there is none.
     * The base name of the two files must match case sensitively.  The case of the characters
     * in the file names' respective extension does not matter.  However, if more than one
     * matching file is found (e.g., xxx.Dbt and xxx.dBt and xxx.DBT) <tt>null</tt> is returned.
     *
     * @param aDbfFile the .DBF file
     * @return .DBT file
     */
    static File getDbtFile(final File aDbfFile)
    {
        if (! aDbfFile.exists())
        {
            return null;
        }

        final String parentDirName = aDbfFile.getParent();
        final File parentDir = new File(parentDirName);
        final String dbfBaseName = stripExtension(aDbfFile.getName());

        final String[] candidates =
            parentDir.list(new FilenameFilter()
                {
                    public boolean accept(File aDir, String aName)
                    {
                        return dbfBaseName.equals(stripExtension(aName)) && aName.toLowerCase().endsWith(".dbt");
                    }
                });

        if (candidates.length == 1)
        {
            return new File(parentDir, candidates[0]);
        }

        return null;
    }

    /**
     * Writes a <tt>java.lang.String</tt> to a <tt>java.io.DataOutput</tt>.  The String is
     * truncate if it exceeds <tt>aMaxLength</tt>.  If it is shorter, the remaining bytes
     * are filled with null characters.
     *
     * @param aDataOutput the <tt>java.io.DataOutput</tt> to write to
     * @param aString the String to write
     * @param aMaxLength the maximum length of the string
     * @throws java.io.IOException
     */
    static void writeString(final DataOutput aDataOutput, final String aString, final int aLength)
                     throws IOException
    {
        char[] charArray = new char[aLength + 1];
        int lengthString = aString.length();
        int i = 0;

        charArray = aString.toCharArray();

        for (i = 0; (i < aLength) && (i < lengthString); i++)
        {
            aDataOutput.writeByte(charArray[i]);
        }

        for (; i < aLength; i++)
        {
            aDataOutput.writeByte(0x00);
        }
    }

    static void writeStringBytes(final DataOutput aDataOutput, final byte[] aString, final int aLength)
                          throws IOException
    {
        aDataOutput.write(aString);
        aDataOutput.write(repeat((byte) 0x00, aLength - aString.length));
    }

    static String readString(final DataInput aDataInput, final int aLength)
                      throws IOException
    {
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        int c = 0;
        int read = 1; // at least one byte will be read

        while (((c = aDataInput.readByte()) != 0) && read < aLength)
        {
            bos.write(c);
            ++read;
        }

        if (c != 0)
        {
            bos.write(c);
        }

        aDataInput.skipBytes(aLength - read);

        return new String(bos.toByteArray());
    }

    static byte[] readStringBytes(final DataInput aDataInput, final int aLength)
                           throws IOException
    {
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        int c = 0;
        int read = 1; // at least one byte will be read

        while (((c = aDataInput.readByte()) != 0) && read < aLength)
        {
            bos.write(c);
            ++read;
        }

        if (c != 0)
        {
            bos.write(c);
        }

        aDataInput.skipBytes(aLength - read);

        return bos.toByteArray();
    }

    /**
     * Creates a Date object with the specfied value and the time fields set to zero.
     * Note that month is zero-based.  The <tt>java.util.Calendar</tt> class has constants
     * for all the months.
     *
     * @param year the year
     * @param month zero-based month number
     * @param day one-based day number
     *
     * @return a <tt>java.util.Date</tt> object
     */
    static Date createDate(int year, int month, int day)
    {
        final Calendar cal = Calendar.getInstance();

        if (Integer.toString(year).length() > NR_OF_DIGITS_IN_YEAR)
        {
            throw new IllegalArgumentException("Year more than" + NR_OF_DIGITS_IN_YEAR + " digits long");
        }

        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.DAY_OF_MONTH, day);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        return cal.getTime();
    }

    /**
     * Returns the number of digits before the decimal point in a number.
     *
     * @param aNumber the number
     * @return the number of digits
     */
    static int getNumberOfIntDigits(final Number aNumber)
    {
        if (aNumber instanceof Float
                || aNumber instanceof Double
                || aNumber instanceof Short
                || aNumber instanceof Integer
                || aNumber instanceof Long)
        {
            long longValue = aNumber.longValue();

            if (longValue == 0)
            {
                return 1;
            }

            return (int) Math.floor(Math.log10(Math.abs(longValue))) + 1;
        }

        BigInteger bi = null;

        if (aNumber instanceof BigDecimal)
        {
            bi = ((BigDecimal) aNumber).toBigInteger();
        }

        if (aNumber instanceof BigInteger)
        {
            bi = (BigInteger) aNumber;
        }

        return bi.abs().toString().length();
    }

    /**
     * Returns the width in positions of the sign of <tt>aInteger</tt>.
     *
     * @param aNumber
     * @return 1 if <tt>aInteger</tt> is negative, 0 otherwise
     *
     */
    static int getSignWidth(Number aNumber)
    {
        if (aNumber instanceof Float
                || aNumber instanceof Double
                || aNumber instanceof Short
                || aNumber instanceof Integer
                || aNumber instanceof Long)
        {
            return aNumber.longValue() < 0 ? 1 : 0;
        }

        if (aNumber instanceof BigDecimal)
        {
            return ((BigDecimal) aNumber).signum() == -1 ? 1 : 0;
        }

        if (aNumber instanceof BigInteger)
        {
            return ((BigInteger) aNumber).signum() == -1 ? 1 : 0;
        }

        throw new IllegalArgumentException("Unsupported Number type");
    }

    static byte[] repeat(byte aByte, int aTimes)
    {
        byte[] result = new byte[aTimes];

        for (int i = 0; i < result.length; ++i)
        {
            result[i] = aByte;
        }

        return result;
    }
}
