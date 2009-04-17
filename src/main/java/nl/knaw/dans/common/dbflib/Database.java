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
import java.util.Collections;
import java.util.HashMap;
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
     * file represents a regular file and not a directory, throws an <tt>IllegalArgumentException</tt>.
     * <p>
     * All tables that exist in the database directory are added as <tt>Table</tt> objects and can be
     * retrieved by {@link #getTable(java.lang.String) }
     *
     * @param aDatabaseDirectory a <tt>java.io.File</tt> object pointing to the directory containing
     *   the database
     */
    public Database(final File aDatabaseDirectory)
    {
        if (aDatabaseDirectory == null || aDatabaseDirectory.isFile())
        {
            throw new IllegalArgumentException("Database must be a directory");
        }

        if (! aDatabaseDirectory.exists())
        {
            aDatabaseDirectory.mkdirs();
        }

        databaseDirectory = aDatabaseDirectory;

        final String[] fileNames = aDatabaseDirectory.list();

        for (String fileName : fileNames)
        {
            if (fileName.toLowerCase().endsWith(".dbf") && (fileName.length() > ".dbf".length()))
            {
                addTable(fileName);
            }
        }
    }

    /**
      * Returns an unmodifiable <tt>java.util.Set</tt> of table names.
      *
      * @return a <tt>java.util.Set</tt> of <tt>Table</tt> objects.
      */
    public Set<String> getTableNames()
    {
        return Collections.unmodifiableSet(tableMap.keySet());
    }

    /**
     * Returns the <tt>Table</tt> object with the specified name of <tt>null</tt>
     * if it has not been added yet.
     *
     * @param aName the name of the table, including extension
     * @return a <tt>Table</tt> object
     */
    public Table getTable(final String aName)
    {
        return tableMap.get(aName);
    }

    /**
     * Adds a new {@link Table} object to the set of <tt>Table</tt>s maintained
     * by this <tt>Database</tt> object and returns it.  If a <tt>Table</tt>
     * object with <tt>aName</tt> already exists, it is returned.
     * <p>
     * Note that the actual table file (the .DBF file) may or may not exists.  To
     * create a new table on disk, see {@link Table#open(nl.knaw.dans.common.dbflib.IfNonExistent) }
     *
     * @param aName the name of the table
     *
     * @return a <tt>Table</tt> object
     */
    public Table addTable(final String aName)
    {
        Table table = tableMap.get(aName);

        if (table == null)
        {
            table = new Table(new File(databaseDirectory, aName));
            tableMap.put(aName, table);
        }

        return table;
    }

    /**
     * Removes a {@link Table} object from the list of <tt>Table</tt> objects maintained
     * by this <tt>Database</tt> object.
     * <p>
     * Note that the actual table file (the .DBF file) is not deleted by removing the
     * table object.  To delete a file on disk, see {@link Table#delete() }.
     *
     * @param aName the name of the table to remove
     */
    public void removeTable(final String aName)
    {
        tableMap.remove(aName);
    }

    /**
     * Removes a {@link Table} object from the list of <tt>Table</tt> objects maintained
     * by this <tt>Database</tt> object.
     * <p>
     * Note that the actual table file (the .DBF file) is not deleted by removing the
     * table object.  To delete a file on disk, see {@link Table#delete() }.
     *
     * @param aTable the table to remove
     */
    public void removeTable(final Table aTable)
    {
        tableMap.remove(aTable.getName());
    }
}
