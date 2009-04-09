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

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Represents a single table in a xBase database.  A table is represented by a single
 * .DBF file.  Some tables have an associated .DBT file to store memo field data.
 *
 * @author Jan van Mansum
 * @author Vesa Åkerman
 */
public class Table
{
    private static final int OFFSET_NUMBER_OF_RECORDS = 4;
    private static final long OFFSET_LENGTH_OF_RECORDS = 10;
    private static final long OFFSET_FIELD_DESCRIPTOR_ARRAY = 32;
    private static final int OFFSET_FIELD_TYPE = 11;
    private static final int OFFSET_FIELD_LENGTH = 16;
    private static final int OFFSET_DECIMAL_COUNT = 17;
    private static final int OFFSET_WORK_AREA_ID = 20;
    private static final int LENGTH_FIELD_DESCRIPTOR = 32;
    private static final int LENGTH_FIELD_NAME = 11;
    private static final int LENGTH_RECORD_DATE = 8;
    private static final int LENGTH_TABLE_HEADER = 32;
    private static final int LENGTH_DELETE_FLAG = 1;
    private static final int LENGTH_MEMO_FIELD = 10;
    private static final int LENGTH_FIELD_DESCRIPTOR_ARRAY_TERMINATOR = 1;
    private static final int RECORD_DELETED = 0x2A;
    private static final int EOF_MARKER = 0x1A;
    static private final int RECORD_VALID = 0x20;
    private static final Map<Character, Type> typeMap = new HashMap<Character, Type>();

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

    private class RecordIterator
        implements Iterator<Record>
    {
        private int recordCounter = 0;
        private Record lastReadRecord = null;

        public boolean hasNext()
        {
            if (lastReadRecord == null)
            {
                try
                {
                    lastReadRecord = readRecord(recordCounter++);
                }
                catch (IOException ioe)
                {
                    throw new RuntimeException(ioe.getMessage(), ioe);
                }
                catch (CorruptedTableException cte)
                {
                    throw new RuntimeException(cte.getMessage(), cte);
                }
            }

            return lastReadRecord != null;
        }

        public Record next()
        {
            if (hasNext())
            {
                final Record next = lastReadRecord;
                lastReadRecord = null;

                return next;
            }

            throw new NoSuchElementException();
        }

        public void remove()
        {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    private final File tableFile;
    private Memo memo = null;
    private List<Field> fields = new ArrayList<Field>();
    private RandomAccessFile raFile = null;
    private int version;
    private Date lastUpdated;
    private int headerLength = 0;
    private int nrRecords;
    private int recordLength;

    /**
     * Creates a new Table object.  A <tt>java.io.File</tt> object representing the .DBF file
     * must be provided.  If the file does not exist it is created when {@link #open() } is called.
     *
     * @param aTableFile a <tt>java.io.File</tt> object representing the .DBF file that
     *      stores this table's data.
     *
     * @see #open(boolean)
     */
    public Table(final File aTableFile)
    {
        if (aTableFile == null)
        {
            throw new IllegalArgumentException("Table file must not be null");
        }

        tableFile = aTableFile;
    }

    /**
     * Creates a new Table object.
     *
     * @param aTableFile DOCUMENT ME!
     * @param version DOCUMENT ME!
     * @param aFields DOCUMENT ME!
     */
    public Table(final File aTableFile, int version, final List<Field> aFields)
    {
        this(aTableFile);
        this.version = version;
        fields = new ArrayList<Field>(aFields);
    }

    /**
     * Opens the table for reading and writing.  Equivalent to
     * {@link Table#open(boolean) Table.open(false)}
     *
     * @throws IOException if the table file does not exist
     */
    public void open()
              throws IOException, CorruptedTableException
    {
        open(false);
    }

    /**
     * Opens the table for reading and writing.
     *
     * @param aCreate create the table file if it does not exist
     *
     * @throws java.io.IOException if the table file does not exist and <tt>aCreate</tt> is <tt>false</tt>
     */
    public void open(final boolean aCreate)
              throws IOException, CorruptedTableException
    {
        if (aCreate)
        {
            if (tableFile.exists())
            {
                throw new IOException("Output file " + tableFile + " already exists");
            }

            raFile = new RandomAccessFile(tableFile, "rw");
            writeHeader();
        }
        else if (tableFile.exists())
        {
            raFile = new RandomAccessFile(tableFile, "rw");
            readHeader();
        }
        else
        {
            throw new FileNotFoundException("Input file " + tableFile + " not found");
        }
    }

    /**
     * Closes this table for reading and writing.  If a memo file
     * was opened, it is also closed.
     *
     * @throws java.io.IOException if the file cannot be closed
     */
    public void close()
               throws IOException
    {
        try
        {
            if (raFile == null)
            {
                return;
            }

            raFile.close();

            if (memo != null)
            {
                memo.close();
            }
        }
        finally
        {
            raFile = null;
            memo = null;
        }
    }

    /**
     * Closes and deletes the underlying table file.  If a memo file
     * was opened, it is also closed and deleted.
     *
     * @throws java.io.IOException if the table file cannot be closed.
     */
    public void delete()
                throws IOException
    {
        close();
        tableFile.delete();

        if (memo != null)
        {
            memo.delete();
        }
    }

    /**
     * Returns the last updated date of the table.  Note that the hours, minutes,
     * seconds and milliseconds fields are always set to zero.  Also the date time
     * is not normalized to UTC.
     *
     * @return the last modified date of the table
     */
    public Date getLastUpdated()
    {
        checkOpen();

        return lastUpdated;
    }

    /**
     * Returns the name of the table, including the extension.
     *
     * @return the name of the table
     */
    public String getName()
    {
        return tableFile.getName();
    }

    /**
     * Returns a <tt>java.util.List</tt> of {@link Field} objects, which provide
     * a description of each field (column) in the table.  The order of the <tt>Field</tt>
     * objects is guaranteed to be the same as the order of the fields in each record
     * returned.  A new copy of the field list is returned on each call.
     *
     * @return the list of field objects.
     */
    public List<Field> getFields()
    {
        return new ArrayList<Field>(fields);
    }

    /**
     * Returns a <tt>Record</tt> iterator.
     *
     *
     * @return a <tt>Record</tt> iterator
     */
    public Iterator<Record> recordIterator()
    {
        return new RecordIterator();
    }

    /**
     * DOCUMENT ME!
     *
     * @param aRecord DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     */
    public void addRecord(final Record aRecord)
                   throws IOException, CorruptedTableException
    {
        String name = null;

        raFile.seek(headerLength + (nrRecords * recordLength));

        // write record deleted tag
        raFile.writeByte(RECORD_VALID);

        // write record contents
        for (Field field : fields)
        {
            name = field.getName().trim();

            switch (field.getType())
            {
                case NUMBER:
                    writeDouble((Double) aRecord.getValue(name),
                                field.getLength(),
                                field.getDecimalCount());

                    break;

                case CHARACTER:
                    writeString((String) aRecord.getValue(name),
                                field.getLength());

                    break;

                case LOGICAL:
                    writeBoolean((Boolean) aRecord.getValue(name));

                    break;

                case DATE:

                    if (aRecord.getValue(name) != null)
                    {
                        writeRecordDate((Date) aRecord.getValue(name));
                    }
                    else
                    {
                        writeString("          ", LENGTH_RECORD_DATE);
                    }

                    break;

                case MEMO:
                    writeMemo((String) aRecord.getValue(name));

                    break;

                default:
                    System.out.println("Unknown field type encountered\n");

                    break;
            }
        }

        // rewrite EOF byte
        raFile.writeByte(0x1a);

        // increment number of records in the header
        nrRecords++;
        raFile.seek(OFFSET_NUMBER_OF_RECORDS);
        raFile.writeInt(Util.changeEndianness(nrRecords));
    }

    private void checkOpen()
    {
        if (raFile == null)
        {
            throw new IllegalStateException("Table should be open for this operation");
        }
    }

    private void readHeader()
                     throws IOException, CorruptedTableException
    {
        version = raFile.read();

        if (version == -1)
        {
            throw new EOFException("Input file is empty");
        }

        readLastUpdated();

        nrRecords = Util.changeEndianness(raFile.readInt());
        headerLength = Util.changeEndianness((short) raFile.readUnsignedShort());
        recordLength = Util.changeEndianness((short) raFile.readUnsignedShort());

        raFile.seek(OFFSET_FIELD_DESCRIPTOR_ARRAY);

        // read field definitions
        final int nrBytesFieldDescriptorArray =
            headerLength - LENGTH_TABLE_HEADER - LENGTH_FIELD_DESCRIPTOR_ARRAY_TERMINATOR;

        if ((nrBytesFieldDescriptorArray % LENGTH_FIELD_DESCRIPTOR) != 0)
        {
            throw new CorruptedTableException("Number of field descriptions in file could not be calculated.");
        }

        int numberOfFields = (int) nrBytesFieldDescriptorArray / LENGTH_FIELD_DESCRIPTOR;

        for (int i = 0; i < numberOfFields; ++i)
        {
            fields.add(readNextField());
        }
    }

    private void readLastUpdated()
                          throws IOException
    {
        /*
         * The number of years in the last modified date is actually the number of
         * years since 1900. (See also comment below.)
         */
        int year = raFile.read() + 1900;

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

        int month = raFile.read();
        int day = raFile.read();

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONDAY, month - 1 /* Calendar's months are zero-based */        );
        cal.set(Calendar.DAY_OF_MONTH, day);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        lastUpdated = cal.getTime();
    }

    private Field readNextField()
                         throws IOException
    {
        final String name = readNextString(LENGTH_FIELD_NAME);
        final char typeChar = (char) raFile.read();

        raFile.skipBytes(OFFSET_FIELD_LENGTH - OFFSET_FIELD_TYPE - 1);

        final int length = raFile.readUnsignedByte();

        final Type type = typeMap.get(typeChar);

        // TODO: What if type == null ?
        // read amount bytes given in length and write to log file?
        final int decimalCount = raFile.readUnsignedByte();

        raFile.skipBytes(LENGTH_FIELD_DESCRIPTOR - OFFSET_DECIMAL_COUNT - 1);

        return new Field(name, type, length, decimalCount);
    }

    private String readNextString(int maxLength)
                           throws IOException
    {
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        int c = 0;
        int read = 1; // at least one byte will be read

        while (((c = raFile.read()) != 0) && read < maxLength)
        {
            bos.write(c);
            ++read;
        }

        if (c != 0)
        {
            bos.write(c);
        }

        raFile.skipBytes(maxLength - read);

        return new String(bos.toByteArray());
    }

    private Double readNextDouble(final int aLength)
                           throws IOException
    {
        String s = readNextString(aLength);

        if (s.trim().isEmpty())
        {
            return null;
        }

        return Double.parseDouble(s);
    }

    private Boolean readNextBoolean()
                             throws IOException
    {
        byte c = raFile.readByte();

        if (c == ' ')
        {
            return null;
        }

        return (c == 'Y') || (c == 'y') || (c == 'T') || (c == 't');
    }

    private Date readNextRecordDate()
                             throws IOException
    {
        String yearString = readNextString(4);
        String monthString = readNextString(2);
        String dayString = readNextString(2);

        Calendar cal = Calendar.getInstance();

        if (yearString.trim().isEmpty())
        {
            return null;
        }
        else
        {
            int year = Integer.parseInt(yearString);
            int month = Integer.parseInt(monthString) - 1;
            int day = Integer.parseInt(dayString);

            cal.set(Calendar.YEAR, year);
            cal.set(Calendar.MONTH, month);
            cal.set(Calendar.DAY_OF_MONTH, day);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
        }

        return cal.getTime();
    }

    private String readNextMemo(int fieldLength)
                         throws IOException, CorruptedTableException
    {
        if (memo == null)
        {
            openMemo();
        }

        String sIndex = readNextString(fieldLength);

        if (sIndex.trim().isEmpty())
        {
            return "";
        }

        return memo.readMemo(Integer.parseInt(sIndex.trim()));
    }

    /**
     * Opens a memo file with the same name as the table file.
     * Equivalent to {@link Table#openMemo(boolean) Table.openMemo(false)}
     *
     * @throws CorruptedTableException if the table file does not exist
     */
    private void openMemo()
                   throws CorruptedTableException, IOException
    {
        openMemo(false);
    }

    /**
     * Opens a memo file with the same name as the table file.  When 'aCreate' parameter = false,
     * the memo file is looked up in the same directory where the table file is, and the search is
     * case insensitive.   @throws CorruptedTableException if the table file does not exist.
     * If the file is found, instance of a memo object is created.
     */
    private void openMemo(boolean aCreate)
                   throws CorruptedTableException, IOException
    {
        File dbtFile = null;

        if (aCreate)
        {
            String file = tableFile.getPath();
            dbtFile = new File(file.substring(0, file.length() - 3) + "dbt");
        }
        else
        {
            dbtFile = Util.getDbtFile(tableFile);

            if (dbtFile == null)
            {
                throw new CorruptedTableException("Could not find .DBT file or multiple matches for .DBT file");
            }
        }

        memo = new Memo(dbtFile);
        memo.open(aCreate, version);
    }

    private Record readRecord(final int aIndex)
                       throws IOException, CorruptedTableException
    {
        raFile.seek(headerLength + (aIndex * recordLength));

        byte firstByteOfRecord = raFile.readByte();

        while (firstByteOfRecord == RECORD_DELETED)
        {
            raFile.skipBytes(recordLength - 1);
        }

        if (firstByteOfRecord == EOF_MARKER)
        {
            return null;
        }

        final Map<String, Object> recordValues = new HashMap<String, Object>();

        for (final Field field : fields)
        {
            switch (field.getType())
            {
                case NUMBER:
                    recordValues.put(field.getName(),
                                     readNextDouble(field.getLength()));

                    break;

                case CHARACTER:
                    recordValues.put(field.getName(),
                                     readNextString(field.getLength()));

                    break;

                case LOGICAL:
                    recordValues.put(field.getName(),
                                     readNextBoolean());

                    break;

                case DATE:
                    recordValues.put(field.getName(),
                                     readNextRecordDate());

                    break;

                case MEMO:
                    recordValues.put(field.getName(),
                                     readNextMemo(field.getLength()));

                    break;

                default:
                    // TODO: Turn this into a proper logging called (using log4j?)
                    System.out.println("Unknown field type encountered\n");

                    break;
            }
        }

        return new Record(recordValues);
    }

    private void writeHeader()
                      throws IOException
    {
        // count number of fields and sum of field definition lengths
        int fieldCount = 0;
        int sumFieldLengths = 0;

        for (Field field : fields)
        {
            fieldCount++;
            sumFieldLengths += field.getLength();
        }

        // write first part of header
        writeFileHeader(fieldCount, sumFieldLengths + LENGTH_DELETE_FLAG);

        // write field definitions to the header
        for (Field field : fields)
        {
            writeFieldDefinition(field);
        }

        // write header termination byte and EOF byte
        raFile.writeByte(0x0d);
        raFile.writeByte(0x1a);
    }

    private void writeFileHeader(int fieldCount, int recordLength)
                          throws IOException
    {
        raFile.writeByte(version);

        /*
         * The number of years in the last modified date is actually the number of
         * years since 1900.
         * DBase III+ (and presumable II) has a Year 2000 bug.  It stores the year
         * as simply the last two digits of the actual year.  DBFs created after 1999
         * will therefore have the wrong date.
         * To comply with this practise, we subtract 1900 from the given year if it is
         * less than 2000, and 200 0if it is greater than or equal to 2000
         */
        final Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR) - 1900;

        if (year >= 100)
        {
            year -= 100;
        }

        raFile.writeByte(year);
        raFile.writeByte(cal.get(Calendar.MONTH) + 1);
        raFile.writeByte(cal.get(Calendar.DAY_OF_MONTH));

        // in the table object and in the file number of records initialized to zero
        nrRecords = 0;
        raFile.writeInt(0);

        // store in the table object and write to the file length of the header
        headerLength = (LENGTH_TABLE_HEADER + (fieldCount * LENGTH_FIELD_DESCRIPTOR) + 1);
        raFile.writeShort(Util.changeEndianness((short) (headerLength)));

        // store in the table object and write to the file length of records
        this.recordLength = recordLength;
        raFile.writeShort(Util.changeEndianness((short) recordLength));

        // rest of the header is filled with 00h
        for (int i = 0; i < (LENGTH_TABLE_HEADER - OFFSET_LENGTH_OF_RECORDS - 2); i++)
        {
            raFile.writeByte(0x00);
        }
    }

    private void writeFieldDefinition(Field field)
                               throws IOException
    {
        // name of the field, terminated with 00h
        // (in LENGTH_FIELD_NAME the terminating 00h byte is already taken in account)
        writeString(field.getName(),
                    LENGTH_FIELD_NAME - 1);
        raFile.writeByte(0x00);

        // field type
        raFile.writeByte(field.getType().getCode());

        // field data address.  Used only with FoxPro. In other cases fill with 00h
        raFile.writeInt(0x00);

        // field length
        raFile.writeByte(field.getLength());

        // field decimal count
        raFile.writeByte(field.getDecimalCount());

        // the next two bytes are set to 0x00
        raFile.writeByte(0x00);
        raFile.writeByte(0x00);

        // work area ID
        raFile.writeByte(0x01);

        // rest of the field description block is filled with 00h
        for (int i = 0; i < (LENGTH_FIELD_DESCRIPTOR - OFFSET_WORK_AREA_ID - 1); i++)
        {
            raFile.writeByte(0x00);
        }
    }

    private void writeString(String s, int aLength)
                      throws IOException
    {
        char[] charArray = new char[aLength + 1];
        int lengthString = s.length();
        int i = 0;

        charArray = s.toCharArray();

        // write the contents of the input string
        for (i = 0; (i < aLength) && (i < lengthString); i++)
        {
            raFile.writeByte(charArray[i]);
        }

        // fill the rest of the given length with 0x0h
        for (; i < aLength; i++)
        {
            raFile.writeByte(0x00);
        }
    }

    private void writeSpaces(final int aLength)
                      throws IOException
    {
        for (int i = 0; i < aLength; ++i)
        {
            raFile.writeByte(' ');
        }
    }

    private void writeDouble(final Double aValue, final int aLength, final int aDecimalCount)
                      throws IOException
    {
        if (aValue == null)
        {
            writeSpaces(aLength);

            return;
        }

        String doubleFormatter = "%" + Integer.toString(aLength) + "." + Integer.toString(aDecimalCount) + "f";
        writeString(String.format(Locale.US, doubleFormatter, aValue),
                    aLength);
    }

    private void writeBoolean(final Boolean aValue)
                       throws IOException
    {
        if (aValue == null)
        {
            raFile.writeByte(' ');

            return;
        }

        if (aValue)
        {
            raFile.writeByte('T');
        }
        else
        {
            raFile.writeByte('F');
        }
    }

    private void writeRecordDate(final Date aRecordDate)
                          throws IOException
    {
        if (aRecordDate == null)
        {
            writeSpaces(LENGTH_RECORD_DATE);

            return;
        }

        String recordDate;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        recordDate = sdf.format(aRecordDate);
        writeString(recordDate, LENGTH_RECORD_DATE);
    }

    private void writeMemo(final String aMemo)
                    throws IOException, CorruptedTableException
    {
        if ((aMemo == null) || aMemo.isEmpty())
        {
            writeSpaces(LENGTH_MEMO_FIELD);
        }
        else
        {
            if (memo == null)
            {
                openMemo(true);
            }

            int index = memo.writeMemo(aMemo);

            writeDouble(Double.valueOf(index),
                        LENGTH_MEMO_FIELD,
                        0);
        }
    }
}
