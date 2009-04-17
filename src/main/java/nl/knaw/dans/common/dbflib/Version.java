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
    /**
     * Not supported yet.
     */
    DBASE_2((byte) 0x02),
    /**
     * dBase III(+) version of the .DBF format.
     */
    DBASE_3((byte) 0x03), 
    /**
     * Not supported yet.
     */
    DBASE_4((byte) 0x04), 
    /**
     * Not supported yet.
     */
    DBASE_5((byte) 0x05);
    private final byte versionByte;

    private Version(final byte aVersionByte)
    {
        versionByte = aVersionByte;
    }

    byte getVersionByte()
    {
        return versionByte;
    }

    static Version getVersion(final byte aVersionByte)
    {
        byte versionNr = (byte) (aVersionByte & 0x0F);

        if (versionNr == DBASE_3.getVersionByte())
        {
            return DBASE_3;
        }

        return null;
    }
}
