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


/**
 * Enumerates the supported table versions.
 * <p>
 * <b>Note:</b>  Not all versions listed here are supported yet.
 *
 * @author Jan van Mansum
 */
public enum Version
{
    DBASE_3(0, 0x1a1a, 2),
    DBASE_4(8, 0x00, 0),
    DBASE_5(8, 0x00, 0);

    final int memoDataOffset;
    final int memoFieldEndMarker;
    final int memoFieldEndMarkerLength;

    Version(int aMemoDataOffset, int aMemoFieldEndMarker, int aMemoFieldEndMarkerLength)
    {
        memoDataOffset = aMemoDataOffset;
        memoFieldEndMarker = aMemoFieldEndMarker;
        memoFieldEndMarkerLength = aMemoFieldEndMarkerLength;
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

    /*
     * The commented 'case' options found in documentation, but not yet
     * come across in using the library
     */
    static Version getVersion(final int aVersionByte)
    {
        switch (aVersionByte)
        {
            case 0x03:
            case 0x83:
                return DBASE_3;

//            case 0x04:
//            case 0x7B:
            case 0x8B:

//            case 0x8E:
                return DBASE_4;

//            case 0x05:
//                return DBASE_5;
            default:
                return null;
        }
    }
}
