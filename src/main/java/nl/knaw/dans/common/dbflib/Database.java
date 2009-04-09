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

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Represents a xBase database.  An xBase database is a directory containing table
 * files (.DBF files) and supporting files like memo (.DBT) or index (.NDX) files.
 * This class allows you to work with the database without having to open the
 * lower level files directly.  However, it is still possible to open individual tables
 * directly through the {@link Table} class.
 *
 * @author Jan van Mansum
 * @author Vesa Åkerman
 */
public class Database
{
    private final File databaseDirectory;
    private final Map<String, Table> tableMap = new HashMap<String, Table>();

    /**
     * Creates a new Database object.  A file representing the database directory
     * must be provided.  If the directory does not exist, it is created.  If the
     * file represents a regular file and not a directory, an <tt>IllegalArgumentException</tt>
     * is thrown.
     *
     * @param aDbDirectory a <tt>java.io.File</tt> object pointing to the directory containing
     *   the database
     */
    public Database(final File aDatabaseDirectory)
    {
        if ((aDatabaseDirectory == null) || aDatabaseDirectory.isFile())
        {
            throw new IllegalArgumentException("Database must be a directory");
        }

        databaseDirectory = aDatabaseDirectory;
    }

    /**
     * Returns the <tt>java.util.Set</tt> of table Names in a given directory.
     * The names include the extension.  Multiple invocations return different objects.
     * Although these objects are not immutable, they cannot be used to change the database.
     *
     * @return a set of table names
     */
    public Set<String> getTableNames()
    {
        final String[] fileNames = databaseDirectory.list();
        final Set tableNames = new HashSet();

        for (String fileName : fileNames)
        {
            if (fileName.toLowerCase().endsWith(".dbf") && (fileName.length() > ".dbf".length()))
            {
                tableNames.add(fileName);
            }
        }

        return tableNames;
    }

    /**
     * Returns a {@link Table} object representing the table with the specified name.
     * A <tt>Database</tt> object always returns the same object for the same table.
     * The underlying table file does not have to exist.  It can be created when the
     * table if opened with {@link Table#open(boolean) }.
     *
     * @param aName the name of the table
     *
     * @return a <tt>Table</tt> object
     */
    public Table getTable(final String aName)
    {
        Table table = tableMap.get(aName);

        if (table == null)
        {
            table = new Table(new File(databaseDirectory, aName));
            tableMap.put(aName, table);
        }

        return table;
    }
}
