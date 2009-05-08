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
 * Tests reading and writing character fields
 *
 * @author Vesa Åkerman
 */
public class GenericTestCharacter
    extends GenericTest
{
    GenericTestCharacter(Version aVersion, String aVersionDirectory)
    {
        super(aVersion, aVersionDirectory);
    }

    /**
     * tests reading of character fields
     *
     * @throws IOException DOCUMENT ME!
     * @throws CorruptedTableException DOCUMENT ME!
     */
    void readCharacter()
                throws IOException, CorruptedTableException
    {
        final Table t1 = new Table(new File("src/test/resources/" + versionDirectory + "/types/CHARACTE.DBF"));

        try
        {
            t1.open(IfNonExistent.ERROR);

            final Iterator<Record> recordIterator = t1.recordIterator();

            Record r = recordIterator.next();
            assertEquals("first char",
                         r.getStringValue("CHAR1"));
            assertEquals("first character",
                         r.getStringValue("CHAR2").substring(0, 15));

            r = recordIterator.next();

            String longString = "";
            longString = "here we are using for many versions the maximum length of character field, that is 254 characters ";
            longString += "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx";
            longString += "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx!!!";
            assertEquals("long char ",
                         r.getStringValue("CHAR1"));
            assertEquals(longString,
                         r.getStringValue("CHAR2"));

            r = recordIterator.next();
            assertEquals("          ",
                         r.getStringValue("CHAR1"));
            assertEquals("          ",
                         r.getStringValue("CHAR2").substring(0, 10));
        }
        finally
        {
            t1.close();
        }
    }

    /**
     * tests writing character fields
     *
     * @throws IOException DOCUMENT ME!
     * @throws CorruptedTableException DOCUMENT ME!
     */
    void writeCharacter()
                 throws IOException, CorruptedTableException, ValueTooLargeException, RecordTooLargeException
    {
        final File outputDir = new File("target/test-output/" + versionDirectory + "/types/CHARACTER");
        outputDir.mkdirs();

        final File tableFile = new File(outputDir, "WRITECHAR.DBF");
        final List<Field> fields = new ArrayList<Field>();
        fields.add(new Field("CHAR1", Type.CHARACTER, 20, 0));
        fields.add(new Field("CHAR2", Type.CHARACTER, 253, 0));

        final Table table = new Table(tableFile, version, fields);

        try
        {
            table.open(IfNonExistent.CREATE);

            try
            {
                table.addRecord("Less than 20", "This is not at all long");
            }
            catch (ValueTooLargeException vtle)
            {
                assertTrue("ValueTooLargeException went off unexpectedly", false);
            }

            try
            {
                table.addRecord("This is exactly 20 c",
                                "This is exactly 253 characters, which is the limit for character fields in DBase products"
                                + "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"
                                + "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx!!!");

                assertFalse("Expected exception ValueTooLargeException not thrown", false);
            }
            catch (ValueTooLargeException vtle)
            {
                assertTrue(true);
            }

            try
            {
                table.addRecord("This is more than 20 characters", "This long field is ok");

                assertFalse("Expected exception ValueTooLargeException not thrown", false);
            }
            catch (ValueTooLargeException vtle)
            {
                assertTrue(true);
            }

            try
            {
                table.addRecord("This is ok",
                                "This is more than 253 characters, which is the limit for character fields in DBase products"
                                + "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"
                                + "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"
                                + "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"
                                + "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"
                                + "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"
                                + "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"
                                + "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"
                                + "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"
                                + "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"
                                + "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"
                                + "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx!!!");

                assertFalse("Expected exception ValueTooLargeException not thrown", false);
            }
            catch (ValueTooLargeException vtle)
            {
                assertTrue(true);
            }

            try
            {
                table.addRecord();
            }
            catch (Exception e)
            {
                assertFalse("Unexpected exception", true);
            }
        }
        finally
        {
            table.close();
        }
    }

    /**
     * tests writing field that exceeds the maximum allowed field length for the datatype
     *
     * @throws IOException DOCUMENT ME!
     * @throws CorruptedTableException DOCUMENT ME!
     */
    void writeTooLongField()
                    throws IOException, CorruptedTableException, ValueTooLargeException, RecordTooLargeException
    {
        final File outputDir = new File("target/test-output/" + versionDirectory + "/types/CHARACTER");
        outputDir.mkdirs();

        final File tableFile = new File(outputDir, "FIELDTOOLONG.DBF");
        final List<Field> fields = new ArrayList<Field>();
        fields.add(new Field("CHAR", Type.CHARACTER, 512, 0));

        final Table table = new Table(tableFile, version, fields);

        try
        {
            table.open(IfNonExistent.CREATE);

            try
            {
                table.addRecord("just this, but...");
                assertFalse("Expected exception ValueTooLargeException not thrown", false);
            }
            catch (ValueTooLargeException vtle)
            {
                assertTrue(true);
            }
        }
        finally
        {
            table.close();
        }
    }
}
