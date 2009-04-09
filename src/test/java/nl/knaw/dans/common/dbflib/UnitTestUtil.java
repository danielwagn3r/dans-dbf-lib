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
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * Utility functions for testing databases.
 *
 * @author Jan van Mansum
 */
class UnitTestUtil
{
    private UnitTestUtil()
    {
        // Disallow instantiation.
    }

    /**
     * Takes a record iterator and a field name and creates a list of the records sorted by the
     * field specified by the field name.  The type of this field must implement <tt>java.lang.Comparable</tt>.
     *
     * @param aRecordIterator a record iterator
     * @param aFieldName a field name
     *
     * @return a sorted list of records
     */
    public static List<Record> createSortedRecordList(final Iterator<Record> aRecordIterator, final String aFieldName)
    {
        final List<Record> recordList = new ArrayList<Record>();

        while (aRecordIterator.hasNext())
        {
            recordList.add(aRecordIterator.next());
        }

        Collections.sort(recordList,
                         new Comparator<Record>()
            {
                public int compare(Record r1, Record r2)
                {
                    Comparable c1 = (Comparable) r1.getValue(aFieldName);
                    Comparable c2 = (Comparable) r2.getValue(aFieldName);

                    if (c1 == null)
                    {
                        if (c2 == null)
                        {
                            return 0;
                        }

                        return -1;
                    }

                    if (c2 == null)
                    {
                        return 1;
                    }

                    return c1.compareTo(c2);
                }
            });

        return recordList;
    }

    /**
     * Creates a Date object with the specfied value and the time fields set to zero.
     * Note that month is zero-based.  The <tt>java.util.Calendar</tt> class has constants
     * for all the months.
     *
     * @param year the year
     * @param month zero-based month number
     * @param day one-based day number
     *
     * @return a <tt>java.util.Date</tt> object
     */
    public static Date createDate(int year, int month, int day)
    {
        final Calendar cal = Calendar.getInstance();

        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.DAY_OF_MONTH, day);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        return cal.getTime();
    }

    /**
     * Compares two files ignoring the specified byte ranges.  Returns the offset of the first
     * byte that is different between the two files, or -1 if the files where the same.
     *
     * @param aFile1 the first file
     * @param aFile2 the second file
     * @param aIgnoredRanges A list of pairs specifying byte ranges to ignore
     *
     * @return the offset of the first difference
     *
     * @throws IOException if one of the files could not be read
     */
    public static long compare(final File aFile1, final File aFile2, final List<Pair<Integer, Integer>> aIgnoredRanges)
                        throws IOException
    {
        final FileInputStream fis1 = new FileInputStream(aFile1);
        final FileInputStream fis2 = new FileInputStream(aFile2);

        try
        {
            long offset = 0;
            int c1 = fis1.read();
            int c2 = fis2.read();

            while (UnitTestUtil.inRange(offset, aIgnoredRanges) || c1 != -1 && c2 != -1 && c1 == c2)
            {
                ++offset;

                c1 = fis1.read();
                c2 = fis2.read();
            }

            if (c1 != c2)
            {
                return offset;
            }

            return -1;
        }
        finally
        {
            fis1.close();
            fis2.close();
        }
    }

    /**
     * Returns <tt>true</tt> if <tt>aOffset</tt> is within any of the ranges in the <tt>aRanges</tt> list.
     *
     * @param aOffset the offset to check
     * @param aRanges the ranges to check
     *
     * @return <tt>true</tt> if in range <tt>false</tt> otherwise
     */
    public static boolean inRange(final long aOffset, final List<Pair<Integer, Integer>> aRanges)
    {
        for (final Pair<Integer, Integer> range : aRanges)
        {
            if (aOffset >= range.getFirst() && aOffset <= range.getSecond())
            {
                return true;
            }
        }

        return false;
    }

    /**
     * Removes the file represented by <code>aFile</code>. If the file is a directory
     * removes the directory and all the files and subdirectories it contains.
     *
     * @param aFile the file to remove
     */
    public static void remove(final File aFile)
    {
        if (aFile.isDirectory())
        {
            if (".".equals(aFile.getName()) || "..".equals(aFile.getName()))
            {
                /*
                 * Not really necessary, but you can never be too careful.
                 */
                return;
            }

            final String[] files = aFile.list();

            for (String f : files)
            {
                remove(new File(aFile, f));
            }
        }

        aFile.delete();
    }

    /**
     * Recreates the directory specified by <code>aDirectory</code>. If the directory exists
     * it is deleted first. The result is always that <code>aDirectory</code> is empty.
     * If the parent directories of <code>aDirectory</code> do not exist they are first
     * created.
     *
     * @param aDirectory the directory to recreate
     * @return the directory created
     */
    public static File recreateDirectory(final String aDirectory)
    {
        final File dir = new File(aDirectory);

        if (dir.exists())
        {
            remove(dir);
        }

        dir.mkdirs();

        return dir;
    }
}
