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
import java.util.Iterator;
import java.util.List;

/**
 * Tests reading and writing boolean fields.
 *
 * @author Vesa Åkerman
 */
@RunWith(Parameterized.class)
public class TestBoolean
    extends BaseTestcase
{
    /**
     * Creates a new TestBoolean object.
     *
     * @param aVersion DOCUMENT ME!
     * @param aVersionDirectory DOCUMENT ME!
     */
    public TestBoolean(final Version aVersion, final String aVersionDirectory)
    {
        super(aVersion, aVersionDirectory);
    }

    /**
     * tests reading of boolean fields
     *
     * @throws IOException not expected
     * @throws CorruptedTableException not expected
     */
    @Test
    public void readBoolean()
                     throws IOException, CorruptedTableException
    {
        final Table table = new Table(new File("src/test/resources/" + versionDirectory + "/types/BOOLEAN.DBF"));

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
    * Tests writing of boolean fields.
    *
    * @throws IOException not expected
    * @throws CorruptedTableException DOCUMENT ME!
    */
    @Test
    public void writeBoolean()
                      throws IOException,
                             CorruptedTableException,
                             ValueTooLargeException,
                             RecordTooLargeException,
                             InvalidFieldTypeException,
                             InvalidFieldLengthException
    {
        final File outputDir = new File("target/test-output/" + versionDirectory + "/types/BOOLEAN");
        outputDir.mkdirs();

        final File tableFile = new File(outputDir, "WRITEBOOLEAN.DBF");
        UnitTestUtil.remove(tableFile);

        final List<Field> fields = new ArrayList<Field>();
        fields.add(new Field("BOOLEAN", Type.LOGICAL, 1));

        final Table table = new Table(tableFile, version, fields);

        try
        {
            table.open(IfNonExistent.CREATE);

            table.addRecord(true);
            table.addRecord(false);
            table.addRecord();
        }
        finally
        {
            table.close();
        }
    }
}
