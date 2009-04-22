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

import java.util.HashMap;
import java.util.Map;

/**
 * Enumeration of the field types available in an xBase database.  xBase types are
 * mapped to Java types as specified in the enumarated constant descriptions below.
 * <p>
 * <b>Note on NUMBER and FLOAT values:</b>  In dBase III NUMBER and in dbBase IV
 * FLOAT values are represented internally as floating point values.  When parsed
 * by Java they may have slight rounding errors.
 *
 * @author Jan van Mansum
 * @author Vesa Åkerman
 */
public enum Type
{
    /**
     * Numeric value, mapped to <tt>java.lang.Number</tt> subclass.
     */
    NUMBER('N'),
    /**
     * Float value, mapped to <tt>java.lang.Number</tt> subclass.
     */
    FLOAT('F'), 
    /**
     * String values, mapped to <tt>java.lang.String</tt>.
     */
    CHARACTER('C'), 
    /**
     * Logical, or boolean value, mapped to <tt>java.lang.Boolean</tt>.
     */
    LOGICAL('L', 1), 
    /**
     * Date value, mapped to <tt>java.util.Date</tt>.  Note that in xBase the date does <em>not</em> have a
     * time component.  The time related fields of <tt>java.util.Date</tt> are therefore set to 0.
     */
    DATE('D', 8), 
    /**
     * A String value (without length limitations), mapped to <tt>java.lang.String</tt>.
     */
    MEMO('M', 10);
    private static final Map<Character, Type> typeMap = new HashMap<Character, Type>();
    private final char code;
    private final int length;

    static
    {
        /*
         * Maps the type characters from the .DBF file to type enum constants.
         */
        for (Type t : Type.values())
        {
            typeMap.put(t.getCode(),
                        t);
        }
    }

    Type(final char aCode)
    {
        this(aCode, -1);
    }

    Type(final char aCode, final int aLength)
    {
        code = aCode;
        length = aLength;
    }

    char getCode()
    {
        return code;
    }

    static Type getTypeByCode(final char aCode)
    {
        return typeMap.get(aCode);
    }

    int getLength()
    {
        return length;
    }
}
