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

import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Tests reading and writing memo fields
 *
 * @author Vesa Åkerman
 */
public class TestFileExceptions
{
    /**
     * DOCUMENT ME!
     *
     * @throws FileNotFoundException DOCUMENT ME!
     * @throws IOException DOCUMENT ME!
     * @throws CorruptedTableException DOCUMENT ME!
     */
    @Test
    public void nonExistingFile()
                         throws IOException, CorruptedTableException
    {
        final File databaseDirectory = new File("src/test/resources/dbase3plus/fileExceptions");
        final Database database = new Database(databaseDirectory);
        final Table t1 = database.addTable("NONEXIST.DBF");

        try
        {
            t1.open(IfNonExistent.ERROR);
            assertTrue("Expected FileNotFoundException did not occur", false);
        }
        catch (FileNotFoundException e)
        {
            assertTrue(true);
        }
        finally
        {
            t1.close();
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @throws FileNotFoundException DOCUMENT ME!
     * @throws IOException DOCUMENT ME!
     * @throws CorruptedTableException DOCUMENT ME!
     */
    @Test
    public void emptyFile()
                   throws IOException, CorruptedTableException
    {
        final File databaseDirectory = new File("src/test/resources/dbase3plus/fileExceptions");
        final Database database = new Database(databaseDirectory);
        final Table t1 = database.addTable("EMPTY.DBF");

        try
        {
            t1.open(IfNonExistent.ERROR);
            assertTrue("Expected EOFException did not occur", false);
        }
        catch (EOFException e)
        {
            assertTrue(true);
        }
        finally
        {
            t1.close();
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @throws FileNotFoundException DOCUMENT ME!
     * @throws IOException DOCUMENT ME!
     * @throws CorruptedTableException DOCUMENT ME!
     */
    @Test
    public void memoFileMissing()
                         throws IOException, CorruptedTableException
    {
        final File databaseDirectory = new File("src/test/resources/dbase3plus/fileExceptions");
        final Database database = new Database(databaseDirectory);
        final Table t1 = database.addTable("MISSMEMO.DBF");

        try
        {
            t1.open(IfNonExistent.ERROR);
            t1.recordIterator().next();
            assertTrue("Expected RuntimeException did not occur", false);
        }
        catch (RuntimeException e)
        {
            assertTrue(true);
        }
        finally
        {
            t1.close();
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @throws FileNotFoundException DOCUMENT ME!
     * @throws IOException DOCUMENT ME!
     * @throws CorruptedTableException DOCUMENT ME!
     */
    @Test
    public void emptyMemoFile()
                       throws IOException, CorruptedTableException
    {
        final File databaseDirectory = new File("src/test/resources/dbase3plus/fileExceptions");
        final Database database = new Database(databaseDirectory);
        final Table t1 = database.addTable("MEMEMPTY.DBF");

        try
        {
            t1.open(IfNonExistent.ERROR);
            t1.recordIterator().next();
            assertTrue("Expected RuntimeException did not occur", false);
        }
        catch (RuntimeException e)
        {
            assertTrue(true);
        }
        finally
        {
            t1.close();
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @throws FileNotFoundException DOCUMENT ME!
     * @throws IOException DOCUMENT ME!
     * @throws CorruptedTableException DOCUMENT ME!
     */
    @Test
    public void corruptedMemoFilePointer()
                                  throws IOException, CorruptedTableException
    {
        final File databaseDirectory = new File("src/test/resources/dbase3plus/fileExceptions");
        final Database database = new Database(databaseDirectory);
        final Table t1 = database.addTable("PNTRERR.DBF");

        try
        {
            t1.open(IfNonExistent.ERROR);
            t1.recordIterator().next();
            assertTrue("Expected RuntimeException did not occur", false);
        }
        catch (RuntimeException e)
        {
            assertTrue(true);
        }
        finally
        {
            t1.close();
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @throws FileNotFoundException DOCUMENT ME!
     * @throws IOException DOCUMENT ME!
     * @throws CorruptedTableException DOCUMENT ME!
     */
    @Test
    public void directoryIsFile()
    {
        final File databaseDirectory = new File("src/test/resources/dbase3plus/fileExceptions/bogusDirectory");

        try
        {
            new Database(databaseDirectory);
            assertTrue("Expected IllegalArgumentException did not occur", false);
        }
        catch (IllegalArgumentException e)
        {
            assertTrue(true);
        }
    }
}
