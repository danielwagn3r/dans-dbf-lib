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

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

/**
 * Tests reading date fields
 *
 * @author Vesa Åkerman
 */
public class GenericTestFloat
    extends GenericTest
{
    GenericTestFloat(Version aVersion, String aVersionDirectory)
    {
        super(aVersion, aVersionDirectory);
    }

    /**
     * tests correctness of date fields
     *
     * @throws IOException DOCUMENT ME!
     * @throws CorruptedTableException DOCUMENT ME!
     */
    void readFloat()
            throws IOException, CorruptedTableException
    {
        final Table t1 = new Table(new File("src/test/resources/" + versionDirectory + "/types/FLOAT.DBF"));

        try
        {
            t1.open(IfNonExistent.ERROR);

            final Iterator<Record> recordIterator = t1.recordIterator();

            Record r = recordIterator.next();

            assertEquals(1,
                         r.getNumberValue("FLOAT1").intValue());
            assertEquals(1234567890123450000L,
                         r.getNumberValue("FLOAT2").longValue());
            assertEquals(1.111111111111110000,
                         r.getNumberValue("FLOAT3").doubleValue(),
                         0.0);
            assertEquals(4.44444444,
                         r.getNumberValue("FLOAT4").doubleValue(),
                         0.0);

            r = recordIterator.next();
            assertEquals(9,
                         r.getNumberValue("FLOAT1").intValue());
            assertEquals(999999999999990000L,
                         r.getNumberValue("FLOAT2").longValue());
            assertEquals(9.999999999990000000,
                         r.getNumberValue("FLOAT3").doubleValue(),
                         0.0);
            assertEquals(9.99999999,
                         r.getNumberValue("FLOAT4").doubleValue(),
                         0.0);

            r = recordIterator.next();
            assertNull(r.getNumberValue("FLOAT1"));
            assertNull(r.getNumberValue("FLOAT2"));
            assertNull(r.getNumberValue("FLOAT3"));
            assertNull(r.getNumberValue("FLOAT4"));

            r = recordIterator.next();
            assertEquals(0,
                         r.getNumberValue("FLOAT1").intValue());
            assertEquals(new BigInteger("10000000000000000000"),
                         (BigInteger) (r.getNumberValue("FLOAT2")));
            assertEquals(5.555555555555560000,
                         r.getNumberValue("FLOAT3").doubleValue(),
                         0.0);
            assertEquals(0.00000000,
                         r.getNumberValue("FLOAT4").doubleValue(),
                         0.0);
        }
        finally
        {
            t1.close();
        }
    }

    /**
    * tests writing of date fields
    *
    * @throws IOException DOCUMENT ME!
    * @throws CorruptedTableException DOCUMENT ME!
    */
    void writeFloat()
             throws IOException, CorruptedTableException, RecordTooLargeException
    {
        final File outputDir = new File("target/test-output/" + versionDirectory + "/types/FLOAT");
        outputDir.mkdirs();

        final File tableFile = new File(outputDir, "WRITEFLOAT.DBF");
        final List<Field> fields = new ArrayList<Field>();
        fields.add(new Field("FLOAT_1", Type.FLOAT, 20, 0));
        fields.add(new Field("FLOAT_2", Type.NUMBER, 20, 1));
        fields.add(new Field("FLOAT_3", Type.FLOAT, 20, 18));

        final Table table = new Table(tableFile, version, fields);

        try
        {
            table.open(IfNonExistent.CREATE);

            try
            {
                table.addRecord(0, 0.0, 0.0);
            }
            catch (Exception e)
            {
                assertFalse("Unexpected Exception 1", true);
            }

            try
            {
                table.addRecord(1234567890, 1234567890.1, 1.123456789);
            }
            catch (Exception e)
            {
                assertFalse("Unexpected Exception 2", true);
            }

            try
            {
                table.addRecord(new BigInteger("10000000000000000000"),
                                10000000000000000.0,
                                0.00000000000000001);
            }
            catch (Exception e)
            {
                assertFalse("Unexpected Exception 3", true);
            }

//            try
//            {
//                table.addRecord(new BigInteger("99999999999999999999999999"),
//                                0.0,
//                                0.0);
//                assertFalse("Expected exception ValueTooLargeException not thrown", false);
//            }
//            catch (ValueTooLargeException vtle)
//            {
//                assertTrue(true);
//            }
        }
        finally
        {
            table.close();
        }
    }
}
