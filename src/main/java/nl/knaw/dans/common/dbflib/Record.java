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

import java.util.Map;

/**
 * Represents a record in a table.
 *
 * @author Jan van Mansum
 * @author Vesa Åkerman
 */
public class Record
{
    private final Map<String, Object> valueMap;

    /**
     * Creates a new Record object.
     *
     * @param aValueMap a mapping if field names to values
     */
    public Record(final Map<String, Object> aValueMap)
    {
        valueMap = aValueMap;
    }

    /**
     * Returns the value of the specified field in the record.  The type of the
     * object returned depends on the type of
     *
     * @param aFieldName the name of the field for which to retrieve the value
     *
     * @return a value
     */
    public Object getValue(final String aFieldName)
    {
        return valueMap.get(aFieldName);
    }
}
