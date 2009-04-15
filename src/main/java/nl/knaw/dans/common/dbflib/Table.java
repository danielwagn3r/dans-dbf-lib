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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
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
    private static final int LENGTH_RECORD_DATE = 8;
    private static final int LENGTH_MEMO_FIELD = 10;
    private static final int RECORD_DELETED = 0x2A;
    private static final int EOF_MARKER = 0x1A;
    private static final int RECORD_VALID = 0x20;

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
    private final DbfHeader header = new DbfHeader();
    private Memo memo = null;
    private RandomAccessFile raFile = null;

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
    public Table(final File aTableFile, int aVersion, final List<Field> aFields)
    {
        this(aTableFile);
        header.setVersion(aVersion);
        header.setFields(aFields);
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
            header.writeAll(raFile);
        }
        else if (tableFile.exists())
        {
            raFile = new RandomAccessFile(tableFile, "rw");
            header.readAll(raFile);
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
    public Date getLastModifiedDate()
    {
        checkOpen();

        return header.getLastModifiedDate();
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
        return header.getFields();
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
     * Adds a record to the database.
     *
     * @param aRecord the record to add.
     *
     * @throws IOException if the underlying file threw an I/O exception
     */
    public void addRecord(final Record aRecord)
                   throws IOException, CorruptedTableException
    {
        String name = null;

        raFile.seek(header.getLength() + (header.getRecordCount() * header.getRecordLength()));

        // write record deleted tag
        raFile.writeByte(RECORD_VALID);

        // write record contents
        for (Field field : header.getFields())
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
                    Util.writeString(raFile,
                                     (String) aRecord.getValue(name),
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
                        writeSpaces(LENGTH_RECORD_DATE);
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
        raFile.seek(DbfHeader.OFFSET_RECORD_COUNT);
        header.setRecordCount(header.getRecordCount() + 1);
        header.writeRecordCount(raFile);
    }

    private void checkOpen()
    {
        if (raFile == null)
        {
            throw new IllegalStateException("Table should be open for this operation");
        }
    }

    private Double readNextDouble(final int aLength)
                           throws IOException
    {
        String s = Util.readString(raFile, aLength);

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
        String yearString = Util.readString(raFile, 4);
        String monthString = Util.readString(raFile, 2);
        String dayString = Util.readString(raFile, 2);

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

        String sIndex = Util.readString(raFile, fieldLength);

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
        memo.open(aCreate,
                  header.getVersion());
    }

    private Record readRecord(final int aIndex)
                       throws IOException, CorruptedTableException
    {
        raFile.seek(header.getLength() + (aIndex * header.getRecordLength()));

        byte firstByteOfRecord = raFile.readByte();

        while (firstByteOfRecord == RECORD_DELETED)
        {
            raFile.skipBytes(header.getRecordLength() - 1);
        }

        if (firstByteOfRecord == EOF_MARKER)
        {
            return null;
        }

        final Map<String, Object> recordValues = new HashMap<String, Object>();

        for (final Field field : header.getFields())
        {
            switch (field.getType())
            {
                case NUMBER:
                    recordValues.put(field.getName(),
                                     readNextDouble(field.getLength()));

                    break;

                case CHARACTER:
                    recordValues.put(field.getName(),
                                     Util.readString(raFile,
                                                     field.getLength()));

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
        Util.writeString(raFile,
                         String.format(Locale.US, doubleFormatter, aValue),
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
        Util.writeString(raFile, recordDate, LENGTH_RECORD_DATE);
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
