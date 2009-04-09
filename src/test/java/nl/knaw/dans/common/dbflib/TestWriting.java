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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author Vesa Åkerman
 */
public class TestWriting
{
    private final int FILE_TYPE_83 = 0x83;
    private Table cars = null;
    private List<Field> fields = null;

    /**
     * DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     * @throws CorruptedTableException DOCUMENT ME!
     */
    @Test
    public void writeFile()
                   throws IOException, CorruptedTableException
    {
        File carsFile = new File("src/test/resources/dbase3plus/cars/cars.dbf");
        File outputDir = UnitTestUtil.recreateDirectory("target/TestWriting/cars");

        File carsFileCopy = new File(outputDir, "newCars.dbf");
        Table carsCopy = null;

        try
        {
            cars = new Table(carsFile);
            cars.open();
            fields = cars.getFields();
            carsCopy = new Table(carsFileCopy, FILE_TYPE_83, fields);
            carsCopy.open(true);

            Iterator<Record> recordIterator = cars.recordIterator();

            while (recordIterator.hasNext())
            {
                carsCopy.addRecord(recordIterator.next());
            }
        }
        finally
        {
            cars.close();
            carsCopy.close();
        }

        List<Pair<Integer, Integer>> ignoredRangesDbf = new ArrayList<Pair<Integer, Integer>>();
        ignoredRangesDbf.add(new Pair<Integer, Integer>(0x01, 0x03)); // modified
        ignoredRangesDbf.add(new Pair<Integer, Integer>(0x2c, 0x2f)); // field description "address in memory"
        ignoredRangesDbf.add(new Pair<Integer, Integer>(0x4c, 0x4f)); // idem
        ignoredRangesDbf.add(new Pair<Integer, Integer>(0x6c, 0x6f)); // idem
        ignoredRangesDbf.add(new Pair<Integer, Integer>(0x8c, 0x8f)); // idem
        ignoredRangesDbf.add(new Pair<Integer, Integer>(0xac, 0xaf)); // idem
        ignoredRangesDbf.add(new Pair<Integer, Integer>(0xcc, 0xcf)); // idem

        long diffOffset = UnitTestUtil.compare(carsFile, carsFileCopy, ignoredRangesDbf);
        assertEquals("Files differ at offset " + Integer.toHexString((int) diffOffset), -1, diffOffset);

        List<Pair<Integer, Integer>> ignoredRangesDbt = new ArrayList<Pair<Integer, Integer>>();
        ignoredRangesDbt.add(new Pair<Integer, Integer>(0x04, 0x1ff)); // reserved/garbage
        ignoredRangesDbt.add(new Pair<Integer, Integer>(0x432, 0x5ff)); // garbage beyond dbase eof bytes 
        diffOffset =
            UnitTestUtil.compare(new File("src/test/resources/dbase3plus/cars/cars.dbt"),
                                 new File(outputDir, "newCars.dbt"),
                                 ignoredRangesDbt);
    }
}
