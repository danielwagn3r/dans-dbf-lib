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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Tests reading and writing boolean fields
 *
 * @author Vesa Åkerman
 */
public class GenericTestBoolean
{
    /**
     * tests reading of boolean fields
     *
     * @throws IOException DOCUMENT ME!
     * @throws CorruptedTableException DOCUMENT ME!
     */
    public static void readBoolean(String aVersionDirectory)
                            throws IOException, CorruptedTableException
    {
        final Table table = new Table(new File("src/test/resources/" + aVersionDirectory + "/types/BOOLEAN.DBF"));

        try
        {
            table.open(IfNonExistent.ERROR);

            final Iterator<Record> recordIterator = table.recordIterator();

            Record r = recordIterator.next();
            assertEquals(true,
                         r.getBooleanValue("BOOLEAN"));

            r = recordIterator.next();
            assertEquals(false,
                         r.getBooleanValue("BOOLEAN"));
        }
        finally
        {
            table.close();
        }
    }

    /**
    * tests writing of boolean fields
    *
    * @throws IOException DOCUMENT ME!
    * @throws CorruptedTableException DOCUMENT ME!
    */
    public static void writeBoolean(String aVersionDirectory)
                             throws IOException,
                                    CorruptedTableException,
                                    ValueTooLargeException,
                                    RecordTooLargeException
    {
        final File outputDir = new File("target/test-output/" + aVersionDirectory + "/types/BOOLEAN");
        outputDir.mkdirs();

        final File tableFile = new File(outputDir, "WRITEBOOLEAN.DBF");
        final List<Field> fields = new ArrayList<Field>();
        fields.add(new Field("BOOLEAN", Type.LOGICAL, 1, 0));

        final Table table = new Table(tableFile, Version.DBASE_3, fields);

        try
        {
            table.open(IfNonExistent.CREATE);

            try
            {
                table.addRecord(true);
            }
            catch (Exception e)
            {
                assertFalse("Unexpected Exception", true);
            }

            try
            {
                table.addRecord(false);
            }
            catch (Exception e)
            {
                assertFalse("Unexpected Exception", true);
            }

            try
            {
                table.addRecord();
            }
            catch (Exception e)
            {
                assertFalse("Unexpected Exception", true);
            }
        }
        finally
        {
            table.close();
        }
    }
}
