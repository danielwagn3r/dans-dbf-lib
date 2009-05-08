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
public class TestdBaseIII
{
    static final Version version = Version.DBASE_3;
    static final String versionDirectory = "dbase3plus";

    /**
     * DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     * @throws CorruptedTableException DOCUMENT ME!V
     */
    @Test
    public void testReadRoundTrip()
                           throws IOException, CorruptedTableException
    {
        GenericTestRoundTrip genericTestRoundTrip = new GenericTestRoundTrip(version, versionDirectory);
        genericTestRoundTrip.reading();
    }

    /**
     * DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     * @throws CorruptedTableException DOCUMENT ME!
     * @throws ValueTooLargeException DOCUMENT ME!
     */
    @Test
    public void testWriteRoundTrip()
                            throws IOException, CorruptedTableException, ValueTooLargeException
    {
        GenericTestRoundTrip genericTestRoundTrip = new GenericTestRoundTrip(version, versionDirectory);
        genericTestRoundTrip.writing();
    }

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
        GenericTestBoolean genericTestBoolean = new GenericTestBoolean(version, versionDirectory);
        genericTestBoolean.readBoolean();
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
        GenericTestBoolean genericTestBoolean = new GenericTestBoolean(version, versionDirectory);
        genericTestBoolean.writeBoolean();
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
        GenericTestCharacter genericTestCharacter = new GenericTestCharacter(version, versionDirectory);
        genericTestCharacter.readCharacter();
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
        GenericTestCharacter genericTestCharacter = new GenericTestCharacter(version, versionDirectory);
        genericTestCharacter.writeCharacter();
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
    public void testWriteTooLongField()
                               throws IOException,
                                      CorruptedTableException,
                                      ValueTooLargeException,
                                      RecordTooLargeException
    {
        GenericTestCharacter genericTestCharacter = new GenericTestCharacter(version, versionDirectory);
        genericTestCharacter.writeTooLongField();
    }

    /**
     * DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     * @throws CorruptedTableException DOCUMENT ME!
     */
    @Test
    public void testReadDate()
                      throws IOException, CorruptedTableException
    {
        GenericTestDate genericTestDate = new GenericTestDate(version, versionDirectory);
        genericTestDate.readDate();
    }

    /**
     * DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     * @throws CorruptedTableException DOCUMENT ME!
     */
    @Test
    public void testWriteDate()
                       throws IOException, CorruptedTableException
    {
        GenericTestDate genericTestDate = new GenericTestDate(version, versionDirectory);
        genericTestDate.writeDate();
    }

    /**
     * DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     * @throws FileNotFoundException DOCUMENT ME!
     * @throws CorruptedTableException DOCUMENT ME!
     */
    @Test
    public void testReadMemo()
                      throws IOException, FileNotFoundException, CorruptedTableException
    {
        GenericTestMemo genericTestMemo = new GenericTestMemo(version, versionDirectory);
        genericTestMemo.readMemo();
    }

    /**
     * DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     * @throws CorruptedTableException DOCUMENT ME!
     * @throws ValueTooLargeException DOCUMENT ME!
     */
    @Test
    public void testWriteMemo()
                       throws IOException, CorruptedTableException, ValueTooLargeException
    {
        GenericTestMemo genericTestMemo = new GenericTestMemo(version, versionDirectory);
        genericTestMemo.writeMemo();
    }

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
    public void testReadNumber()
                        throws IOException,
                               FileNotFoundException,
                               CorruptedTableException,
                               ValueTooLargeException,
                               ValueTooLargeException,
                               ValueTooLargeException
    {
        GenericTestNumber genericTestNumber = new GenericTestNumber(version, versionDirectory);
        genericTestNumber.reading_maximal_and_minimal_values();
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
    public void testWriteNumber()
                         throws IOException, FileNotFoundException, CorruptedTableException, ValueTooLargeException
    {
        GenericTestNumber genericTestNumber = new GenericTestNumber(version, versionDirectory);
        genericTestNumber.writing_maximal_and_minimal_values();
    }

    /**
     * DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     * @throws DbfLibException DOCUMENT ME!
     */
    @Test
    public void testValueTooLargeException()
                                    throws IOException, DbfLibException
    {
        GenericTestNumber genericTestNumber = new GenericTestNumber(version, versionDirectory);
        genericTestNumber.valueTooLargeException();
    }
}
