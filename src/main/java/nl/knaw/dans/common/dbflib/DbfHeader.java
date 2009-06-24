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

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Encapsulates the DBF table header.
 *
 * @author Jan van Mansum
 * @author Vesa Åkerman
 */
class DbfHeader
{
    /*
     *  Offsets from beginning of file.
     */
    static final int OFFSET_VERSION = 0;
    static final int OFFSET_MODIFIED_DATE = 1;
    static final int OFFSET_RECORD_COUNT = 4;
    static final int OFFSET_HEADER_LENGTH = 8;
    static final int OFFSET_RECORD_LENGTH = 10;
    static final int OFFSET_RESERVED_1 = 12;
    static final int OFFSET_INCOMPLETE_TRANSATION = 14;
    static final int OFFSET_ENCRYPTION_FLAG = 15;
    static final int OFFSET_FREE_RECORD_THREAD = 16;
    static final int OFFSET_RESERVED_2 = 20;
    static final int OFFSET_MDX_FLAG = 28;
    static final int OFFSET_LANGUAGE_DRIVER = 29;
    static final int OFFSET_RESERVED_3 = 30;
    static final int OFFSET_FIELD_DESCRIPTORS = 32;

    /*
     * Offsets within one field descriptor.
     */
    static final int FD_OFFSET_NAME = 0;
    static final int FD_OFFSET_TYPE = 11;
    static final int FD_OFFSET_DATA_ADDRESS = 12;
    static final int FD_OFFSET_LENGTH = 16;
    static final int FD_OFFSET_DECIMAL_COUNT = 17;
    static final int FD_OFFSET_RESERVED_MULTIUSER_1 = 18;
    static final int FD_OFFSET_WORK_AREA_ID = 20;
    static final int FD_OFFSET_RESERVED_MULTIUSER_2 = 21;
    static final int FD_OFFSET_SET_FIELDS_FLAG = 23;
    static final int FD_OFFSET_RESERVED = 24;
    static final int FD_OFFSET_INDEX_FIELD_FLAG = 31;
    static final int FD_OFFSET_NEXT_FIELD = 32;

    /*
     * Lengths of parts to skip.
     */
    private static final int LENGTH_FIELD_DESCRIPTOR = FD_OFFSET_NEXT_FIELD;
    private static final int LENGTH_FIELD_NAME = FD_OFFSET_TYPE - FD_OFFSET_NAME;
    private static final int LENGTH_FIELD_DATA_ADDRESS = FD_OFFSET_LENGTH - FD_OFFSET_DATA_ADDRESS;
    private static final int LENGTH_FIELD_DESCR_AFTER_DECIMAL_COUNT =
        FD_OFFSET_NEXT_FIELD - FD_OFFSET_RESERVED_MULTIUSER_1;
    private static final int LENGTH_TABLE_HEADER_AFTER_RECORD_COUNT = OFFSET_FIELD_DESCRIPTORS - OFFSET_RESERVED_1;
    private static final int LENGTH_TABLE_INFO_BLOCK = 32;
    private static final int LENGTH_DELETE_FLAG = 1;
    private static final int OFFSET_WORK_AREA_ID = 20;
    private static final int LENGTH_RESERVED_1 = OFFSET_INCOMPLETE_TRANSATION - OFFSET_RESERVED_1;
    private static final int LENGTH_RESERVED_2 = OFFSET_MDX_FLAG - OFFSET_RESERVED_2;
    private static final int LENGTH_RESERVED_3 = OFFSET_FIELD_DESCRIPTORS - OFFSET_RESERVED_3;

    /*
     * Maximum field lengths.
     * Notice: version dependent maximum field lengths defined in the Version class
     */
    private static final int MAX_LENGTH_FLOAT_FIELD = 20;
    private static final int MAX_LENGTH_LOGICAL_FIELD = 1;
    private static final int MAX_LENGTH_DATE_FIELD = 8;

    /*
     * Special bytes.
     */
    private static final byte FIELD_DESCRIPTOR_ARRAY_TERMINATOR = 0x0D;

    /*
     * Fields.
     */
    private Version version;
    private int versionByte;
    private int recordCount;
    private List<Field> fields = new ArrayList<Field>();
    private short headerLength;
    private short recordLength;
    private Date lastModifiedDate;
    private boolean hasMemo;

    void readAll(final DataInput aDataInput)
          throws IOException, CorruptedTableException
    {
        readVersionByte(aDataInput);
        readModifiedDate(aDataInput);
        readRecordCount(aDataInput);
        readHeaderLength(aDataInput);

        /*
         * In CLIPPER 5 there are two header terminator bytes, in dBase only one.
         *
         * The length of the first general table info block is 32 and of the subsequent
         * field descriptors is also 32.  The headerLength includes the table info block,
         * field descriptors and the final terminator byte(s).  To find out how many
         * terminator bytes there are we simply find the remainder after division by 32.
         */
        version = Version.getVersion(versionByte, headerLength % 32);

        readRecordLength(aDataInput);
        aDataInput.skipBytes(LENGTH_TABLE_HEADER_AFTER_RECORD_COUNT);
        readFieldDescriptors(aDataInput,
                             getFieldCount());
    }

    Date getLastModifiedDate()
    {
        return lastModifiedDate;
    }

    int getLength()
    {
        return headerLength;
    }

    int getRecordLength()
    {
        return recordLength;
    }

    void setHasMemo(final boolean aHasMemo)
    {
        hasMemo = aHasMemo;
    }

    private void calculateRecordLength()
    {
        for (final Field field : fields)
        {
            recordLength += field.getLength();
        }

        recordLength += LENGTH_DELETE_FLAG;
    }

    private void calculateHeaderLength()
    {
        headerLength = (short) (
                           LENGTH_TABLE_INFO_BLOCK + LENGTH_FIELD_DESCRIPTOR * fields.size()
                           + version.getLengthHeaderTerminator()
                       );
    }

    private int getFieldCount()
                       throws CorruptedTableException
    {
        final int nrBytesFieldDescriptorArray =
            headerLength - LENGTH_TABLE_INFO_BLOCK - version.getLengthHeaderTerminator();

        if ((nrBytesFieldDescriptorArray % LENGTH_FIELD_DESCRIPTOR) != 0)
        {
            throw new CorruptedTableException("Number of field descriptions in file could not be calculated.");
        }

        return (int) nrBytesFieldDescriptorArray / LENGTH_FIELD_DESCRIPTOR;
    }

    private void readHeaderLength(final DataInput aDataInput)
                           throws IOException
    {
        headerLength = Util.changeEndianness((short) aDataInput.readUnsignedShort());
    }

    private void readModifiedDate(final DataInput aDataInput)
                           throws IOException
    {
        /*
           * The number of years in the last modified date is actually the number of
           * years since 1900. (See also comment below.)
           */
        int year = aDataInput.readByte() + 1900;

        /*
         * DBase III+ (and presumable II) has a Year 2000 bug.  It stores the year
         * as simply the last two digits of the actual year.  DBFs created after 1999
         * will therefore have the wrong date.  To get around this, we add 100 for
         * all DBFs seemingly created before 1980 (when dBase II was launched).
         */
        if (year < 1980)
        {
            year += 100;
        }

        int month = aDataInput.readByte();
        int day = aDataInput.readByte();

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONDAY, month - 1 /* Calendar's months are zero-based */        );
        cal.set(Calendar.DAY_OF_MONTH, day);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        lastModifiedDate = cal.getTime();
    }

    private void readRecordLength(final DataInput aDataInput)
                           throws IOException
    {
        recordLength = Util.changeEndianness((short) aDataInput.readUnsignedShort());
    }

    void readFieldDescriptors(final DataInput aDataInput, final int aFieldCount)
                       throws IOException
    {
        for (int i = 0; i < aFieldCount; ++i)
        {
            fields.add(readField(aDataInput));
        }
    }

    private Field readField(final DataInput aDataInput)
                     throws IOException
    {
        int length = 0;
        int decimalCount = 0;

        final String name = Util.readString(aDataInput, LENGTH_FIELD_NAME);
        final char typeChar = (char) aDataInput.readByte();
        aDataInput.skipBytes(LENGTH_FIELD_DATA_ADDRESS);

        if (version == Version.CLIPPER_5 && typeChar == Type.CHARACTER.getCode())
        {
            length = Util.changeEndiannessUnsignedShort(aDataInput.readUnsignedShort());
        }
        else
        {
            length = aDataInput.readUnsignedByte();
            decimalCount = aDataInput.readUnsignedByte();
        }

        aDataInput.skipBytes(LENGTH_FIELD_DESCR_AFTER_DECIMAL_COUNT);

        return new Field(name,
                         Type.getTypeByCode(typeChar),
                         length,
                         decimalCount);
    }

    void readVersionByte(final DataInput aDataInput)
                  throws IOException
    {
        versionByte = aDataInput.readUnsignedByte();
    }

    void readRecordCount(final DataInput aDataInput)
                  throws IOException
    {
        recordCount = Util.changeEndianness(aDataInput.readInt());
    }

    void writeAll(final DataOutput aDataOutput)
           throws IOException
    {
        writeVersion(aDataOutput);
        writeModifiedDate(aDataOutput);
        writeRecordCount(aDataOutput);
        writeHeaderLength(aDataOutput);
        writeRecordLength(aDataOutput);
        writeZeros(aDataOutput, LENGTH_RESERVED_1);
        writeIncompleteTransaction(aDataOutput);
        writeEncryptionFlag(aDataOutput);
        writeFreeRecordThread(aDataOutput);
        writeZeros(aDataOutput, LENGTH_RESERVED_2);
        writeMdxFlag(aDataOutput);
        writeLanguageDriver(aDataOutput);
        writeZeros(aDataOutput, LENGTH_RESERVED_3);
        writeFieldDescriptors(aDataOutput);
    }

    void setFields(final List<Field> aFieldList)
            throws InvalidFieldTypeException, InvalidFieldLengthException
    {
        fields = aFieldList;
        checkFieldValidity(fields);
        calculateRecordLength();
        calculateHeaderLength();
    }

    void checkFieldValidity(List<Field> aFieldList)
                     throws InvalidFieldTypeException, InvalidFieldLengthException
    {
        boolean invalidLength = false;

        for (Field field : aFieldList)
        {
            if (! version.fieldTypes.contains(field.getType()))
            {
                throw new InvalidFieldTypeException("Invalid field type ('" + field.getType().toString()
                                                    + "') for this version ");
            }

            switch (field.getType())
            {
                case CHARACTER:

                    if (field.getLength() < 1 || field.getLength() > version.getMaxLengthCharField())
                    {
                        invalidLength = true;
                    }

                    break;

                case NUMBER:

                    if (field.getLength() < 1 || field.getLength() > version.getMaxLengthNumberField())
                    {
                        invalidLength = true;
                    }

                    break;

                case FLOAT:

                    if (field.getLength() < 1 || field.getLength() > MAX_LENGTH_FLOAT_FIELD)
                    {
                        invalidLength = true;
                    }

                    break;

                case LOGICAL:

                    if (field.getLength() < 1 || field.getLength() > MAX_LENGTH_LOGICAL_FIELD)
                    {
                        invalidLength = true;
                    }

                    break;

                case DATE:

                    if (field.getLength() < 1 || field.getLength() > MAX_LENGTH_DATE_FIELD)
                    {
                        invalidLength = true;
                    }

                    break;
            }

            if (invalidLength)
            {
                if (field.getLength() < 1)
                {
                    throw new InvalidFieldLengthException("Field length less than one (field '" + field.getName()
                                                          + "') ");
                }
                else
                {
                    throw new InvalidFieldLengthException("Field length exceeds the allowed field length (field '"
                                                          + field.getName() + "') ");
                }
            }
        }
    }

    void setVersion(final Version aVersion)
    {
        version = aVersion;
    }

    void setRecordCount(int aRecordCount)
    {
        recordCount = aRecordCount;
    }

    List<Field> getFields()
    {
        return Collections.unmodifiableList(new ArrayList<Field>(fields));
    }

    Version getVersion()
    {
        return version;
    }

    int getRecordCount()
    {
        return recordCount;
    }

    void writeEncryptionFlag(final DataOutput aDataOutput)
                      throws IOException
    {
        aDataOutput.writeByte(0x00); // not supported yet
    }

    void writeIncompleteTransaction(final DataOutput aDataOutput)
                             throws IOException
    {
        aDataOutput.writeByte(0x00); // not supported yet
    }

    void writeFieldDescriptor(final DataOutput aDataOutput, Field aField)
                       throws IOException
    {
        /*
         * Name of the field, terminated with 00h.
         * (in LENGTH_FIELD_NAME the terminating 00h byte is already taken in account)
         */
        Util.writeString(aDataOutput,
                         aField.getName(),
                         LENGTH_FIELD_NAME - 1);
        aDataOutput.writeByte(0x00);
        aDataOutput.writeByte(aField.getType().getCode());

        /*
         * Field data address.  Used only in FoxPro. In other cases fill with 00h
         */
        aDataOutput.writeInt(0x00);

        aDataOutput.writeByte(aField.getLength());
        aDataOutput.writeByte(aField.getDecimalCount());
        aDataOutput.writeByte(0x00);
        aDataOutput.writeByte(0x00);

        /*
         * Work area ID
         */
        aDataOutput.writeByte(0x01);

        for (int i = 0; i < (LENGTH_FIELD_DESCRIPTOR - OFFSET_WORK_AREA_ID - 1); i++)
        {
            aDataOutput.writeByte(0x00);
        }
    }

    void writeFieldDescriptors(final DataOutput aDataOutput)
                        throws IOException
    {
        for (final Field field : fields)
        {
            writeFieldDescriptor(aDataOutput, field);
        }

        aDataOutput.writeByte(FIELD_DESCRIPTOR_ARRAY_TERMINATOR);
    }

    void writeFreeRecordThread(final DataOutput aDataOutput)
                        throws IOException
    {
        writeZeros(aDataOutput, 4);
    }

    void writeHeaderLength(final DataOutput aDataOutput)
                    throws IOException
    {
        aDataOutput.writeShort(Util.changeEndianness(headerLength));
    }

    void writeLanguageDriver(final DataOutput aDataOutput)
                      throws IOException
    {
        writeZeros(aDataOutput, 1);
    }

    void writeMdxFlag(final DataOutput aDataOutput)
               throws IOException
    {
        writeZeros(aDataOutput, 1);
    }

    void writeModifiedDate(final DataOutput aDataOutput)
                    throws IOException
    {
        /*
         * The number of years in the last modified date is actually the number of
         * years since 1900.
         * DBase III+ (and presumable II) has a Year 2000 bug.  It stores the year
         * as simply the last two digits of the actual year.  DBFs created after 1999
         * will therefore have the wrong date.
         * To comply with this practise, we subtract 1900 from the given year if it is
         * less than 2000, and 2000 if it is greater than or equal to 2000
         */
        final Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR) - 1900;

        if (year >= 100)
        {
            year -= 100;
        }

        int month = cal.get(Calendar.MONTH) + 1;
        int day = cal.get(Calendar.DAY_OF_MONTH);

        aDataOutput.writeByte(year);
        aDataOutput.writeByte(month);
        aDataOutput.writeByte(day);

        lastModifiedDate = Util.createDate(year, month, day);
    }

    void writeRecordCount(final DataOutput aDataOutput)
                   throws IOException
    {
        aDataOutput.writeInt(Util.changeEndianness(recordCount));
    }

    void writeRecordLength(final DataOutput aDataOutput)
                    throws IOException
    {
        aDataOutput.writeShort(Util.changeEndianness(recordLength));
    }

    void writeVersion(final DataOutput aDataOutput)
               throws IOException
    {
        aDataOutput.writeByte(Version.getVersionByte(version, hasMemo));
    }

    void writeZeros(final DataOutput aDataOutput, int n)
             throws IOException
    {
        for (int i = 0; i < n; ++i)
        {
            aDataOutput.writeByte(0);
        }
    }
}
