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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Calendar;
import java.util.Iterator;

/**
 * Tests reading and writing memo fields
 *
 * @author Vesa Åkerman
 */
public class TestDate
{
    /**
     * tests correctness of date fields
     *
     * @throws FileNotFoundException DOCUMENT ME!
     * @throws IOException DOCUMENT ME!
     * @throws CorruptedTableException DOCUMENT ME!
     */
    @Test
    public void readDate()
                  throws FileNotFoundException, IOException, CorruptedTableException
    {
        final Database database = new Database(new File("src/test/resources/dbase3plus/types"));
        final Table t1 = database.addTable("DATE.DBF");

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
}
