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
 * Represents a field description in a table.
 *
 * @author Jan van Mansum
 * @author Vesa Åkerman
 */
public class Field
{
    private final String name;
    private final Type type;
    private final int length;
    private final int decimalCount;
    private final String formatString;

    /**
     * Creates a new Field object.  <tt>aLength</tt> and <tt>aDecimalCount</tt> do not apply to all
     * field types.
     *
     * @param aName name of the field
     * @param aType the type of the field
     * @param aLength the length of the field
     * @param aDecimalCount the decimal count of the field.
     */
    public Field(final String aName, final Type aType, final int aLength, final int aDecimalCount)
    {
        name = aName;
        type = aType;
        length = aType.getLength() == -1 ? aLength : aType.getLength();
        decimalCount = aDecimalCount;

        formatString = "%" + length + (decimalCount == 0 ? "d" : "." + decimalCount + "f");
    }

    /**
     * Returns the name of the field.
     *
     * @return the name of the field
     */
    public String getName()
    {
        return name;
    }

    /**
     * Returns the type of the field
     *
     * @return the type of the field
     */
    public Type getType()
    {
        return type;
    }

    /**
     * Returns the length of the field, or -1 if not applicable
     *
     * @return the lenght of the field
     */
    public int getLength()
    {
        return length;
    }

    /**
     * Returns the decimal count of the field, or -1 if not applicable
     *
     * @return the lenght of the field
     */
    public int getDecimalCount()
    {
        return decimalCount;
    }

    String getFormatString()
    {
        return formatString;
    }
}
