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

/**
 * Tests {@link Util#getDbtFile(java.io.File) }
 *
 * @author Jan van Mansum
 */
public class TestGetDbtFile
{
    /**
     * DOCUMENT ME!
     */
    @Test
    public void simpleCase()
    {
        File dbtFile = Util.getDbtFile(new File("src/test/resources/util/get_dbt_file1/x.DBF"));

        assertNotNull(".DBT file is null", dbtFile);
        assertEquals("x.DBT",
                     dbtFile.getName());
    }

    /**
     * DOCUMENT ME!
     */
    @Test
    public void notFound()
    {
        File dbtFile = Util.getDbtFile(new File("src/test/resources/util/get_dbt_file2/x.DBF"));

        assertNull("Found non-existing .DBT", dbtFile);
    }

    /**
     * DOCUMENT ME!
     */
    @Test
    public void notFoundForNonexistingDbf()
    {
        File dbtFile = Util.getDbtFile(new File("src/test/resources/util/get_dbt_file3/x.DBF"));
        assertNull("Found non-existing .DBT", dbtFile);
    }
}
