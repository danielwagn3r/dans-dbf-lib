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
 *
 * @author Jan van Mansum
 * @author Vesa Åkerman
 */
public enum Type
{
    /**
     * Numeric value, mapped to <tt>java.lang.Double</tt>.
     */
    NUMBER('N'),
    /**
     * Float value, mapped to <tt>java.lang.Double</tt>.
     */
    FLOAT('F'), 
    /**
     * String values, mapped to <tt>java.lang.String</tt>.
     */
    CHARACTER('C'), 
    /**
     * Logical, or boolean value, mapped to <tt>java.lang.Boolean</tt>.
     */
    LOGICAL('L'), 
    /**
     * Date value, mapped to <tt>java.util.Date</tt>.  Note that in xBase the date does <em>not</em> have a
     * time component.  The time related fields of <tt>java.util.Date</tt> are therefore set to 0.
     */
    DATE('D'), 
    /**
     * A String value (without length limitations), mapped to <tt>java.lang.String</tt>.
     */
    MEMO('M');
    private static final Map<Character, Type> typeMap = new HashMap<Character, Type>();
    private final char code;

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

    Type(char aCode)
    {
        code = aCode;
    }

    char getCode()
    {
        return code;
    }

    static Type getTypeByCode(final char aCode)
    {
        return typeMap.get(aCode);
    }
}
