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
import java.io.IOException;
import java.util.List;

/**
 * Tests reading and writing number values.
 *
 * @author Jan van Mansum
 */
public class TestNumber
{
    /**
     * Tests reading fields with the maximum and minimum lengths and decimal count respectively.
     *
     * @throws IOException if an I/O Exception occurs.
     * @throws CorruptedTableException if the table is corrupt (which it should not be).
     */
    @Test
    public strictfp void reading_maximal_and_minimal_values()
                                                     throws IOException, CorruptedTableException
    {
        final Table number = new Table(new File("src/test/resources/dbase3plus/types/NUMBER.DBF"));

        try
        {
            number.open();

            final List<Record> records = UnitTestUtil.createSortedRecordList(number.recordIterator(),
                                                                             "ID");

            final Record r0 = records.get(0);
            assertNull(r0.getValue("ID"));
            assertNull(r0.getValue("MININT"));
            assertNull(r0.getValue("MAXINT"));
            assertNull(r0.getValue("MAXDEC"));
            assertNull(r0.getValue("MINDEC"));

            final Record r1 = records.get(1);
            assertEquals(1.0,
                         r1.getValue("ID"));
            assertEquals(1.0,
                         r1.getValue("MININT"));
            assertEquals(1111111111111111040.0,
                         r1.getValue("MAXINT"));
            assertEquals(111.1111111111111,
                         r1.getValue("MAXDEC"));
            assertEquals(1.1,
                         r1.getValue("MINDEC"));

            final Record r2 = records.get(2);
            assertEquals(2.0,
                         r2.getValue("ID"));
            assertEquals(2.0,
                         r2.getValue("MININT"));
            assertEquals(2222222222222222080.0,
                         r2.getValue("MAXINT"));
            assertEquals(222.2222222222222,
                         r2.getValue("MAXDEC"));
            assertEquals(2.2,
                         r2.getValue("MINDEC"));

            final Record r3 = records.get(3);
            assertEquals(3.0,
                         r3.getValue("ID"));
            assertEquals(3.0,
                         r3.getValue("MININT"));
            assertEquals(3333333333333332992.0,
                         r3.getValue("MAXINT"));
            assertEquals(333.33333333333334,
                         r3.getValue("MAXDEC"));
            assertEquals(3.3,
                         r3.getValue("MINDEC"));

            final Record r4 = records.get(4);
            assertEquals(4.0,
                         r4.getValue("ID"));
            assertEquals(4.0,
                         r4.getValue("MININT"));
            assertEquals(4444444444444444160.0,
                         r4.getValue("MAXINT"));
            assertEquals(444.4444444444444,
                         r4.getValue("MAXDEC"));
            assertEquals(4.4,
                         r4.getValue("MINDEC"));

            final Record r5 = records.get(5);
            assertEquals(5.0,
                         r5.getValue("ID"));
            assertEquals(5.0,
                         r5.getValue("MININT"));
            assertEquals(5555555555555555328.0,
                         r5.getValue("MAXINT"));
            assertEquals(555.5555555555555,
                         r5.getValue("MAXDEC"));
            assertEquals(5.5,
                         r5.getValue("MINDEC"));

            final Record r6 = records.get(6);
            assertEquals(6.0,
                         r6.getValue("ID"));
            assertEquals(6.0,
                         r6.getValue("MININT"));
//            assertEquals(666666666666665984.0, r6.getValue("MAXINT"));
            assertEquals(666.66666666666668,
                         r6.getValue("MAXDEC"));
            assertEquals(6.6,
                         r6.getValue("MINDEC"));

            final Record r7 = records.get(7);
            assertEquals(7.0,
                         r7.getValue("ID"));
            assertEquals(7.0,
                         r7.getValue("MININT"));
            assertEquals(7777777777777777664.0,
                         r7.getValue("MAXINT"));
            assertEquals(777.777777777779,
                         r7.getValue("MAXDEC"));
            assertEquals(7.7,
                         r7.getValue("MINDEC"));

            final Record r8 = records.get(8);
            assertEquals(8.0,
                         r8.getValue("ID"));
            assertEquals(8.0,
                         r8.getValue("MININT"));
            assertEquals(8888888888888888320.0,
                         r8.getValue("MAXINT"));
            assertEquals(888.8888888888888,
                         r8.getValue("MAXDEC"));
            assertEquals(8.8,
                         r8.getValue("MINDEC"));

            final Record r9 = records.get(9);
            assertEquals(9.0,
                         r9.getValue("ID"));
            assertEquals(9.0,
                         r9.getValue("MININT"));
            assertEquals(9999999999999998.0,
                         r9.getValue("MAXINT"));
            assertEquals(999.9999999999998,
                         r9.getValue("MAXDEC"));
            assertEquals(9.9,
                         r9.getValue("MINDEC"));
        }
        finally
        {
            number.close();
        }
    }
}
