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

import java.util.Date;
import java.util.regex.Pattern;

/**
 * @author Jan van Mansum
 */
class DateFormatValidator
    extends AbstractDataValidator
{
    private final Pattern datePattern = Pattern.compile("\\d{8,8}");

    DateFormatValidator(final Field aField)
    {
        super(aField);
        assert aField.getType() == Type.DATE : "Can only be validator for DATE fields";
    }

    /**
     * {@inheritDoc}
     *
     * For DATE fields <tt>java.util.Date</tt> and <tt>String</tt> objects are
     * acceptable.  <tt>String</tt> objects are trimmed before validating them
     * and only the format of the date is checked, not whether the date itself
     * is valid (e.g., 20090229 will be accepted, even though it is invalid).
     */
    public void validate(Object aTypedObject)
                  throws DbfLibException
    {
        if (aTypedObject instanceof Date)
        {
            return;
        }

        if (aTypedObject instanceof String)
        {
            final String dateString = (String) aTypedObject;

            if (! datePattern.matcher(dateString).matches())
            {
                throw new DataMismatchException("'" + aTypedObject
                                                + " is not in the correct date format for DBF (YYYY-MM-DD)");
            }
        }

        throw new DataMismatchException("Cannot write objects of type '" + aTypedObject.getClass().getName()
                                        + "' to a DATE field");
    }
}
