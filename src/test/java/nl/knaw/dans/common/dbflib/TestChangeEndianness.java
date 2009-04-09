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

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * Tests the changeEndianness functions.
 *
 * @author Jan van Mansum
 */
public class TestChangeEndianness
{
    /**
     * Tests {@link Util#changeEndianness(int) }.
     */
    @Test
    public void integerConversion()
    {
        int le;
        int be;

        /*
         * Simple case.
         */
        le = (0x01 << 24) + (0x02 << 16) + (0x03 << 8) + 0x04;
        be = (0x04 << 24) + (0x03 << 16) + (0x02 << 8) + 0x01;
        assertEquals(be,
                     Util.changeEndianness(le));

        /*
         * Negative bit set.
         */
        le = (0x80 << 24) + (0x02 << 16) + (0x03 << 8) + 0x04;
        be = (0x04 << 24) + (0x03 << 16) + (0x02 << 8) + 0x80;
        assertEquals(be,
                     Util.changeEndianness(le));

        /*
         * Negative bit becomes set.
         */
        le = (0x04 << 24) + (0x03 << 16) + (0x02 << 8) + 0x80;
        be = (0x80 << 24) + (0x02 << 16) + (0x03 << 8) + 0x04;
        assertEquals(be,
                     Util.changeEndianness(le));

        /*
         * Border cases.
         */
        assertEquals(0,
                     Util.changeEndianness(0));
        assertEquals(0xFFFFFFFF,
                     Util.changeEndianness(0xFFFFFFFF));
    }

    /**
     * Tests {@link Util#changeEndianness(short) }.
     */
    @Test
    public void shortConversion()
    {
        short le;
        short be;

        /*
         * Simple case.
         */
        le = (0x01 << 8) + 0x02;
        be = (0x02 << 8) + 0x01;
        assertEquals(be,
                     Util.changeEndianness(le));

        /*
         * Negative bit set.
         */
        le = (short) (0x80 << 8) + 0x01;
        be = (0x01 << 8) + 0x80;
        assertEquals(be,
                     Util.changeEndianness(le));

        /*
         * Negative bit becomes set.
         */
        le = (0x01 << 8) + 0x80;
        be = (short) (0x80 << 8) + 0x01;
        assertEquals(be,
                     Util.changeEndianness(le));

        /*
         * Border cases.
         */
        assertEquals((short) 0,
                     Util.changeEndianness((short) 0));
        assertEquals((short) 0xFFFF,
                     Util.changeEndianness((short) 0xFFFF));
    }
}
