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

import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.IOException;

/**
* tests reading and wrting dBaseIII files
 *
 * @author Vesa Åkerman
 */
public class TestdBaseIV
{
    static final String versionDirectory = "dbase4";

    /**
     * DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     * @throws CorruptedTableException DOCUMENT ME!
     */
    @Test
    public void testReadBoolean()
                         throws IOException, CorruptedTableException
    {
        GenericTestBoolean.readBoolean(versionDirectory);
    }

    /**
     * DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     * @throws CorruptedTableException DOCUMENT ME!
     * @throws ValueTooLargeException DOCUMENT ME!
     * @throws RecordTooLargeException DOCUMENT ME!
     */
    @Test
    public void testWriteBoolean()
                          throws IOException, CorruptedTableException, ValueTooLargeException, RecordTooLargeException
    {
        GenericTestBoolean.writeBoolean(versionDirectory);
    }

    /**
     * DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     * @throws CorruptedTableException DOCUMENT ME!
     */
    @Test
    public void testReadCharacter()
                           throws IOException, CorruptedTableException
    {
        GenericTestCharacter.readCharacter(versionDirectory);
    }

    /**
     * DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     * @throws CorruptedTableException DOCUMENT ME!
     * @throws ValueTooLargeException DOCUMENT ME!
     * @throws RecordTooLargeException DOCUMENT ME!
     */
    @Test
    public void testWriteCharacter()
                            throws IOException,
                                   CorruptedTableException,
                                   ValueTooLargeException,
                                   RecordTooLargeException
    {
        GenericTestCharacter.writeCharacter(versionDirectory);
    }

    /**
     * DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     * @throws CorruptedTableException DOCUMENT ME!
     * @throws ValueTooLargeException DOCUMENT ME!
     * @throws RecordTooLargeException DOCUMENT ME!
     */
    @Test
    public void writeTooLongField()
                           throws IOException,
                                  CorruptedTableException,
                                  ValueTooLargeException,
                                  RecordTooLargeException
    {
        GenericTestCharacter.writeTooLongField(versionDirectory);
    }

    /**
     * DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     * @throws CorruptedTableException DOCUMENT ME!
     */
    @Test
    public void readDate()
                  throws IOException, CorruptedTableException
    {
        GenericTestDate.readDate(versionDirectory);
    }

    /**
     * DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     * @throws FileNotFoundException DOCUMENT ME!
     * @throws CorruptedTableException DOCUMENT ME!
     */

//    @Test
//    public void readMemo()
//                  throws IOException, FileNotFoundException, CorruptedTableException
//    {
//        GenericTestMemo.readMemo(versionDirectory);
//    }
//
//
    /**
     * DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     * @throws CorruptedTableException DOCUMENT ME!
     * @throws ValueTooLargeException DOCUMENT ME!
     */

//    @Test
//    public void writeMemo()
//                   throws IOException, CorruptedTableException, ValueTooLargeException
//    {
//        GenericTestMemo.writeMemo(versionDirectory);
//    }

    /**
     * DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     * @throws FileNotFoundException DOCUMENT ME!
     * @throws CorruptedTableException DOCUMENT ME!
     * @throws ValueTooLargeException DOCUMENT ME!
     * @throws ValueTooLargeException DOCUMENT ME!
     * @throws ValueTooLargeException DOCUMENT ME!
     */
    @Test
    public void readNumber()
                    throws IOException,
                           FileNotFoundException,
                           CorruptedTableException,
                           ValueTooLargeException,
                           ValueTooLargeException,
                           ValueTooLargeException
    {
        GenericTestNumber.reading_maximal_and_minimal_values(versionDirectory);
    }

    /**
     * DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     * @throws FileNotFoundException DOCUMENT ME!
     * @throws CorruptedTableException DOCUMENT ME!
     * @throws ValueTooLargeException DOCUMENT ME!
     */
    @Test
    public void writeNumber()
                     throws IOException, FileNotFoundException, CorruptedTableException, ValueTooLargeException
    {
        GenericTestNumber.writing_maximal_and_minimal_values(versionDirectory);
    }

    /**
     * DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     * @throws DbfLibException DOCUMENT ME!
     */
    @Test
    public void valueTooLargeException()
                                throws IOException, DbfLibException
    {
        GenericTestNumber.valueTooLargeException(versionDirectory);
    }
}
