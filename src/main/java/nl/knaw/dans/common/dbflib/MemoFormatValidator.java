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

import java.util.Date;

/**
 *
 * @author Jan van Mansum
 */
class MemoFormatValidator
    extends AbstractDataValidator
{
    MemoFormatValidator(final Field aField)
    {
        super(aField);
        assert aField.getType() == Type.MEMO : "Can only be validator for MEMO fields";
    }

    /**
     * {@inheritDoc}
     *
     * For a MEMO field values of types <tt>String</tt>, <tt>Boolean</tt>, <tt>java.util.Date</tt>
     * and <tt>Number</tt> are acceptable.
     */
    public void validate(Object aTypedObject)
                  throws DbfLibException
    {
        if (aTypedObject instanceof String
                || aTypedObject instanceof Boolean
                || aTypedObject instanceof Date
                || aTypedObject instanceof Number)
        {
            return;
        }

        throw new DataMismatchException("Cannot write value of type " + aTypedObject.getClass().getName());
    }
}
