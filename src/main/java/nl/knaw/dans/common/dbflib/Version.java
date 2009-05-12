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

import java.util.ArrayList;
import java.util.List;

/**
 * Enumerates the supported table versions.
 * <p>
 * <b>Note:</b>  Not all versions listed here are supported yet.
 *
 * @author Jan van Mansum
 * @author Vesa Åkerman
 */
public enum Version
{

    DBASE_3(253, 19, 0, 0x1a1a, 2,
            getDbase3Types()),
    DBASE_4(253, 20, 8, 0x00, 0,
            getDbase4Types()), 
    DBASE_5(253, 20, 8, 0x00, 0,
            getDbase5Types());
    // Trick Jalopy formatter into behaving.
    private static Type[] getDbase3Types()
    {
        return new Type[] { Type.CHARACTER, Type.NUMBER, Type.DATE, Type.LOGICAL, Type.MEMO };
    }

    private static Type[] getDbase4Types()
    {
        return new Type[] { Type.CHARACTER, Type.NUMBER, Type.DATE, Type.LOGICAL, Type.MEMO, Type.FLOAT };
    }

    private static Type[] getDbase5Types()
    {
        return new Type[]
               {
                   Type.CHARACTER, Type.NUMBER, Type.DATE, Type.LOGICAL, Type.MEMO, Type.FLOAT, Type.BINARY,
                   Type.GENERAL
               };
    }

    private final int maxLengthCharField;
    private final int maxLengthNumberField;
    private final int memoDataOffset;
    private final int memoFieldEndMarker;
    private final int memoFieldEndMarkerLength;
    final List<Type> fieldTypes = new ArrayList<Type>();

    Version(final int aMaxLengthCharField, final int aMaxLengthNumberField, final int aMemoDataOffset,
            final int aMemoFieldEndMarker, final int aMemoFieldEndMarkerLength, final Type[] aFieldTypes)
    {
        maxLengthCharField = aMaxLengthCharField;
        maxLengthNumberField = aMaxLengthNumberField;
        memoDataOffset = aMemoDataOffset;
        memoFieldEndMarker = aMemoFieldEndMarker;
        memoFieldEndMarkerLength = aMemoFieldEndMarkerLength;

        for (Type type : aFieldTypes)
        {
            fieldTypes.add(type);
        }
    }

    int getMemoDataOffset()
    {
        return memoDataOffset;
    }

    int getMemoFieldEndMarker()
    {
        return memoFieldEndMarker;
    }

    int getMemoFieldEndMarkerLength()
    {
        return memoFieldEndMarkerLength;
    }

    static int getVersionByte(Version aVersion, boolean aHasMemo)
    {
        if (aVersion == DBASE_3)
        {
            if (aHasMemo)
            {
                return 0x83;
            }
            else
            {
                return 0x03;
            }
        }
        else if (aVersion == DBASE_4 || aVersion == DBASE_5)
        {
            if (aHasMemo)
            {
                return 0x8B;
            }
            else
            {
                return 0x03;
            }
        }

        return 0;
    }

    static Version getVersion(final int aVersionByte)
    {
        switch (aVersionByte)
        {
            case 0x83:
                return DBASE_3;

            default:
                return DBASE_5;
        }
    }

    int getMaxLengthCharField()
    {
        return maxLengthCharField;
    }

    int getMaxLengthNumberField()
    {
        return maxLengthNumberField;
    }
}
