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
 * Tests reading and writing memo fields.
 *
 * @author Vesa Åkerman
 * @author Jan van Mansum
 */
public class TestFileExceptions
{
    /**
     * Tests that <tt>java.io.FileNotFoundException</tt> is thrown when opening a non-existent
     * table.
     *
     * @throws IOException expected
     * @throws CorruptedTableException not expected
     */
    @Test(expected = FileNotFoundException.class)
    public void nonExistingFile()
                         throws IOException, CorruptedTableException
    {
        final Table table = new Table(new File("NONEXISTENT.DBF"));

        try
        {
            table.open(IfNonExistent.ERROR);
        }
        finally
        {
            table.close();
        }
    }

    // TODO: Would it not be more appropriate to throw a CorruptedTableException?
    /**
     * Tests that an <tt>java.io.EOFException</tt> occurs when opening an empty DBF.
     *
     * @throws IOException expected
     * @throws CorruptedTableException not expected
     */
    @Test(expected = EOFException.class)
    public void emptyFile()
                   throws IOException, CorruptedTableException
    {
        final File emptyFile = new File("src/test/resources/dbase3plus/fileExceptions/EMPTY.DBF");
        final Table table = new Table(emptyFile);

        try
        {
            table.open(IfNonExistent.ERROR);
        }
        finally
        {
            table.close();
        }
    }

    // TODO: Would it not be more appropriate to open the memo on table.open and throw a
    // CorruptedTableException right there if the DBT is not found?
    /**
     * Tests that a <tt>RuntimeException</tt> is thrown when the DBT of a DBF file
     * is missing.
     *
     * @throws IOException not expected
     * @throws CorruptedTableException not expected
     */
    @Test(expected = RuntimeException.class)
    public void memoFileMissing()
                         throws IOException, CorruptedTableException
    {
        final File missingMemoDbf = new File("src/test/resources/dbase3plus/fileExceptions/MISSMEMO.DBF");
        final Table table = new Table(missingMemoDbf);

        try
        {
            table.open(IfNonExistent.ERROR);
            table.recordIterator().next();
        }
        finally
        {
            table.close();
        }
    }

    /**
     * Tests that an exception is thrown if the DBT with a DBF is empty.
     *
     * @throws IOException not expected
     * @throws CorruptedTableException not expected
     */
    @Test(expected = RuntimeException.class)
    public void emptyMemoFile()
                       throws IOException, CorruptedTableException
    {
        final File memEmptyDbf = new File("src/test/resources/dbase3plus/fileExceptions/MEMEMPTY.DBF");
        final Table table = new Table(memEmptyDbf);

        try
        {
            table.open(IfNonExistent.ERROR);
            table.recordIterator().next();
        }
        finally
        {
            table.close();
        }
    }

    /**
     * Tests that an exception is thrown if a memo index points to an invalid location
     * in the DBT.
     *
     * @throws IOException not expected
     * @throws CorruptedTableException not expected
     */
    @Test(expected = RuntimeException.class)
    public void corruptedMemoFilePointer()
                                  throws IOException, CorruptedTableException
    {
        final File pntrErrorDbf = new File("src/test/resources/dbase3plus/fileExceptions/PNTRERR.DBF");
        final Table table = new Table(pntrErrorDbf);

        try
        {
            table.open(IfNonExistent.ERROR);
            table.recordIterator().next();
        }
        finally
        {
            table.close();
        }
    }

    /**
     * Tests that an <tt>IllegalArgumentException</tt> is thrown if the database directory is
     * actually a file.
     */
    @Test(expected = IllegalArgumentException.class)
    public void directoryIsFile()
    {
        final File databaseDirectory = new File("src/test/resources/dbase3plus/fileExceptions/bogusDirectory");
        new Database(databaseDirectory, Version.DBASE_3);
    }
}
