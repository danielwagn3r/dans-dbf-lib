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

import java.io.File;

/**
 * Tests the utility functions in the <tt>Util</tt> class.
 *
 * @author Jan van Mansum
 */
public class TestUtil
{
    /**
       * Tests {@link Util#changeEndianness(int) }.
       */
    @Test
    public void changeEndianness_integer()
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
    public void changeEndianness_short()
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

    /**
     * DOCUMENT ME!
     */
    @Test
    public void getDbtFile_simple_case()
    {
        File dbtFile = Util.getDbtFile(new File("src/test/resources/util/get_dbt_file1/x.DBF"));

        assertNotNull(".DBT file is null", dbtFile);
        assertEquals("x.DBT",
                     dbtFile.getName());
    }

    /**
     * DOCUMENT ME!
     */
    @Test
    public void getDbtFile_returns_null_if_dbt_non_existent()
    {
        File dbtFile = Util.getDbtFile(new File("src/test/resources/util/get_dbt_file2/x.DBF"));

        assertNull("Found non-existing .DBT", dbtFile);
    }

    /**
     * DOCUMENT ME!
     */
    @Test
    public void getDbtFile_returns_null_if_dbf_non_existent()
    {
        File dbtFile = Util.getDbtFile(new File("src/test/resources/util/get_dbt_file3/x.DBF"));
        assertNull("Found non-existing .DBT", dbtFile);
    }

    /**
     * DOCUMENT ME!
     */
    @Test
    public void getNumberOfDigits()
    {
        assertEquals(1,
                     Util.getNumberOfDigits(0));
        assertEquals(1,
                     Util.getNumberOfDigits(1));
        assertEquals(1,
                     Util.getNumberOfDigits(2));
        assertEquals(1,
                     Util.getNumberOfDigits(9));
        assertEquals(2,
                     Util.getNumberOfDigits(10));
        assertEquals(2,
                     Util.getNumberOfDigits(99));
        assertEquals(3,
                     Util.getNumberOfDigits(100));
        assertEquals(6,
                     Util.getNumberOfDigits(999999));
        assertEquals(7,
                     Util.getNumberOfDigits(1000000));
        assertEquals(10,
                     Util.getNumberOfDigits(Integer.MAX_VALUE));
    }
}
