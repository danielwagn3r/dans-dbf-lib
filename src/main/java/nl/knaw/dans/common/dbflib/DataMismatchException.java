/*
 * Copyright 2009 Data Archiving and Networked Services (DANS), Netherlands.
 *
 * This file is part of DANS DBF Library.
 *
 * DANS DBF Library is free software: you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * DANS DBF Library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with DANS DBF Library. If
 * not, see <http://www.gnu.org/licenses/>.
 */
package nl.knaw.dans.common.dbflib;


/**
 * Thrown to indicate that the data provided cannot be written to the database. The data was
 * provided as a Java object of incompatible type (for instance a <tt>Boolean</tt> object to a DATE
 * type field) or the format of the data was wrong (for instance a <tt>String</tt> object to a
 * NUMBER type field, which is allowed, but the <tt>String</tt> contains "1a", that is, it is not a
 * valid number.
 *
 * @author Jan van Mansum
 */
public class DataMismatchException
    extends DbfLibException
{
    DataMismatchException(final String aMessage)
    {
        super(aMessage);
    }
}
