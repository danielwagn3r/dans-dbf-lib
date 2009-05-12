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

import org.junit.runner.RunWith;

import org.junit.runners.Parameterized;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

/**
 * Tests reading date fields
 *
 * @author Vesa Åkerman
 */
@RunWith(Parameterized.class)
public class TestDate
    extends BaseTestingCase
{
    /**
     * Creates a new TestDate object.
     *
     * @param aVersion DOCUMENT ME!
     * @param aVersionDirectory DOCUMENT ME!
     */
    public TestDate(Version aVersion, String aVersionDirectory)
    {
        super(aVersion, aVersionDirectory);
    }

    /**
     * tests correctness of date fields
     *
     * @throws IOException DOCUMENT ME!
     * @throws CorruptedTableException DOCUMENT ME!
     */
    @Test
    public void readDate()
                  throws IOException, CorruptedTableException
    {
        final Table t1 = new Table(new File("src/test/resources/" + versionDirectory + "/types/DATE.DBF"));

        try
        {
            t1.open(IfNonExistent.ERROR);

            final Iterator<Record> recordIterator = t1.recordIterator();

            Record r = recordIterator.next();
            assertEquals(Util.createDate(1999, Calendar.JUNE, 10),
                         r.getDateValue("DATE"));

            r = recordIterator.next();
            assertEquals(Util.createDate(1901, Calendar.JANUARY, 1),
                         r.getDateValue("DATE"));

            r = recordIterator.next();
            assertEquals(Util.createDate(1970, Calendar.JANUARY, 1),
                         r.getDateValue("DATE"));
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
    @Test
    public void writeDate()
                   throws IOException, CorruptedTableException, ValueTooLargeException
    {
        final File outputDir = new File("target/test-output/" + versionDirectory + "/types/DATE");
        outputDir.mkdirs();

        final File tableFile = new File(outputDir, "WRITEDATE.DBF");
        UnitTestUtil.remove(tableFile);

        final List<Field> fields = new ArrayList<Field>();
        fields.add(new Field("DATE", Type.DATE, 8));

        final Table table = new Table(tableFile, version, fields);

        try
        {
            table.open(IfNonExistent.CREATE);

            table.addRecord(Util.createDate(1909, Calendar.MARCH, 18));
            table.addRecord(Util.createDate(1970, Calendar.JANUARY, 1));
            table.addRecord(Util.createDate(1990, Calendar.OCTOBER, 31));
            table.addRecord(Util.createDate(2030, Calendar.JUNE, 15));
            table.addRecord(Util.createDate(2222, Calendar.DECEMBER, 20));
        }
        catch (Exception e)
        {
            assertFalse("Unexpected Exception: " + e.toString(), true);
        }
        finally
        {
            table.close();
        }
    }
}
