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
import java.io.FilenameFilter;

class Util
{
    private Util()
    {
        // Disallow instantiation.
    }

    static int changeEndianness(final int aInteger)
    {
        boolean isNegative = false;
        int i = aInteger;

        if (i < 0)
        {
            isNegative = true;
            i &= 0x7fffffff;
        }

        int first = i >>> 24;

        if (isNegative)
        {
            first |= 0x80;
        }

        i = aInteger & 0x00ff0000;

        int second = i >>> 16;

        i = aInteger & 0x0000ff00;

        int third = i >>> 8;

        int fourth = aInteger & 0x000000ff;

        return (fourth << 24) + (third << 16) + (second << 8) + first;
    }

    static short changeEndianness(short aShort)
    {
        boolean isNegative = false;
        short s = aShort;

        if (s < 0)
        {
            isNegative = true;
            s &= 0x7fff;
        }

        int first = s >>> 8;

        if (isNegative)
        {
            first |= 0x80;
        }

        int second = s & 0x00ff;

        return (short) ((second << 8) + first);
    }

    static String stripExtension(final String aFileName)
    {
        int pointIndex = aFileName.lastIndexOf('.');

        if ((pointIndex == -1) || (pointIndex == 0) || (pointIndex == (aFileName.length() - 1)))
        {
            return aFileName;
        }

        return aFileName.substring(0, pointIndex);
    }

    /**
     * Given a .DBF file, returns the accompanying .DBT file or <tt>null</tt> if there is none.
     * The base name of the two files must match case sensitively.  The case of the characters
     * in the file names' respective extension does not matter.  However, if more than one
     * matching file is found (e.g., xxx.Dbt and xxx.dBt and xxx.DBT) <tt>null</tt> is returned.
     *
     * @param aDbfFile the .DBF file
     * @return .DBT file
     */
    static File getDbtFile(final File aDbfFile)
    {
        if (! aDbfFile.exists())
        {
            return null;
        }

        final String parentDirName = aDbfFile.getParent();
        final File parentDir = new File(parentDirName);
        final String dbfBaseName = stripExtension(aDbfFile.getName());

        final String[] candidates =
            parentDir.list(new FilenameFilter()
                {
                    public boolean accept(File aDir, String aName)
                    {
                        return dbfBaseName.equals(stripExtension(aName)) && aName.toLowerCase().endsWith(".dbt");
                    }
                });

        if (candidates.length == 1)
        {
            return new File(parentDir, candidates[0]);
        }

        return null;
    }
}
