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
 * Created {@link DataValidator} objects.
 *
 * @author Jan van Mansum
 */
class DataFormatValidatorFactory
{
    static final DataValidator doNothingValidator =
        new DataValidator()
        {
            public void validate(Object aTypedObject)
                          throws DbfLibException
            {
            }
        };

    static DataValidator createValidator(final Field aField)
    {
        switch (aField.getType())
        {
            case BINARY:
            case GENERAL:
                return doNothingValidator;

            case CHARACTER:
                return new CharacterFormatValidator(aField);

            case DATE:
                return new DateFormatValidator(aField);

            case FLOAT:
            case NUMBER:
                return new NumberFormatValidator(aField);

            case LOGICAL:
                return new LogicalFormatValidator(aField);

            case MEMO:
                return new MemoFormatValidator(aField);

            default:
                assert false : "Programming error: Not all Types handled";
        }

        return null;
    }
}
