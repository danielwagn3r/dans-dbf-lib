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

import java.util.regex.Pattern;

/**
 *
 * @author Jan van Mansum
 */
class LogicalFormatValidator
    extends AbstractDataValidator
{
    private static final Pattern booleanPattern = Pattern.compile("[YNTF ]");

    LogicalFormatValidator(final Field aField)
    {
        super(aField);
        assert aField.getType() == Type.LOGICAL : "Can only be validator for LOGICAL fields";
    }

    /**
     * {@inheritDoc}
     *
     * For a LOGICAL field a <tt>Boolean</tt>, or a <tt>String</tt> is acceptable. A <tt>String</tt>
     * is acceptable only if it contains one of Y, N, T, F. The String may contain leading and
     * trailing spaces.
     */
    public void validate(final Object aTypedObject)
                  throws DbfLibException
    {
        if (aTypedObject instanceof Boolean)
        {
            return;
        }

        if (aTypedObject instanceof String)
        {
            final String booleanString = (String) aTypedObject;

            if (! booleanPattern.matcher(booleanString).matches())
            {
                throw new DataMismatchException("Boolean must be one of Y, N, T, F or a space");
            }

            return;
        }

        throw new DataMismatchException("Cannot write objects of type '" + aTypedObject.getClass().getName()
                                        + "' to a LOGICAL field");
    }
}
