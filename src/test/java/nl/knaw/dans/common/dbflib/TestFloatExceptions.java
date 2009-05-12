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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.junit.runner.RunWith;

import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 *
 * @author Vesa Åkerman
 */
@RunWith(Parameterized.class)
public class TestFloatExceptions
    extends BaseTestcase
{
    private Table table;

    /**
     * Creates a new TestFloatExceptions object.
     *
     * @param aVersion test parameter
     * @param aVersionDirectory test parameter
     */
    public TestFloatExceptions(final Version aVersion, final String aVersionDirectory)
    {
        super(aVersion, aVersionDirectory);
    }

    /**
     * {@inheritDoc}
     *
     * No dBase III+, because it has no FLOAT type.
     */
    @Parameters
    public static Collection<Object[]> data()
    {
        final Object[][] testParameters =
            new Object[][]
            {
                { Version.DBASE_4, "dbase4" },
                { Version.DBASE_5, "dbase5" }
            };

        return Arrays.asList(testParameters);
    }

    /**
     * Sets up the testing environment.
     *
     * @throws IOException not expected
     * @throws CorruptedTableException not expected
     */
    @Before
    public void setUp()
               throws IOException, CorruptedTableException
    {
        final File outputDir = new File("target/test-output/" + versionDirectory + "/types/FLOAT");
        outputDir.mkdirs();

        final File tableFile = new File(outputDir, "WRITEFLOAT.DBF");
        UnitTestUtil.remove(tableFile);

        final List<Field> fields = new ArrayList<Field>();
        fields.add(new Field("FLOAT_1", Type.FLOAT, 20, 0));
        fields.add(new Field("FLOAT_2", Type.NUMBER, 20, 1));
        fields.add(new Field("FLOAT_3", Type.FLOAT, 20, 18));

        table = new Table(tableFile, version, fields);
        table.open(IfNonExistent.CREATE);
    }

    /**
     * Cleans up the test environment.
     *
     * @throws IOException not expected
     */
    @After
    public void tearDown()
                  throws IOException
    {
        table.close();
    }

    /**
     * Tests that adding a value that is too big triggers a <tt>ValueTooLargeException</tt>.
     *
     * @throws IOException not expected
     * @throws CorruptedTableException not expected
     * @throws ValueTooLargeException expected!
     * @throws RecordTooLargeException not expected
     */
    @Test(expected = ValueTooLargeException.class)
    public void tooBigIntegerValue()
                            throws IOException,
                                   CorruptedTableException,
                                   ValueTooLargeException,
                                   RecordTooLargeException
    {
        table.addRecord(new BigInteger("99999999999999999999999999"),
                        0.0,
                        0.0);
    }
}
