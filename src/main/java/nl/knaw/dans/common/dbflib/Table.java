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
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.Format;
import java.text.ParseException;
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
    private static final int MARKER_RECORD_DELETED = 0x2A;
    private static final int MARKER_EOF = 0x1A;
    private static final int MARKER_RECORD_VALID = 0x20;
    private static final DecimalFormat decimalParser = new DecimalFormat();
    private static final Format dateFormat = new SimpleDateFormat("yyyyMMdd");

    static
    {
        decimalParser.setParseBigDecimal(true);

        /*
         * Set Locale to US so that decimal point (not comma) is always expected.
         */
        decimalParser.setDecimalFormatSymbols(new DecimalFormatSymbols(Locale.US));
    }

    private class RecordIterator
        implements Iterator<Record>
    {
        private int recordCounter = 0;
        private Record lastReadRecord = null;

        public boolean hasNext()
        {
            checkOpen();

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
     * must be provided.  To read from or write to the table it must first be opened.
     *
     * @param aTableFile a <tt>java.io.File</tt> object representing the .DBF file that
     *      stores this table's data.
     *
     * @see #open(nl.knaw.dans.common.dbflib.IfNonExistent)
     *
     * @throws IllegalArgumentException if <tt>aTableFiel</tt> is <tt>null</tt>
     */
    public Table(final File aTableFile)
          throws IllegalArgumentException
    {
        if (aTableFile == null)
        {
            throw new IllegalArgumentException("Table file must not be null");
        }

        tableFile = aTableFile;
    }

    /**
     * Creates a new Table object.  In order to read from or write to the table it
     * must first be opened.
     * <p>
     * <b>Note:</b> if the .DBF file already exists <tt>aFields</tt> will be overwritten by the values
     * in the existing file when opened.  To replace an existing table, first delete it and then create and
     * open a new <tt>Table</tt> object.
     *
     * @param aTableFile the .DBF file that contains the table data
     * @param version the dBase version to support
     * @param aFields the fields to create if this is a new table
     *
     * @see #open(nl.knaw.dans.common.dbflib.IfNonExistent)
     *
     * @throws IllegalArgumentException if <tt>aTableFiel</tt> is <tt>null</tt>
     */
    public Table(final File aTableFile, final Version aVersion, final List<Field> aFields)
          throws IllegalArgumentException
    {
        this(aTableFile);
        header.setVersion(aVersion);
        header.setHasMemo(hasMemo(aFields));
        header.setFields(aFields);
    }

    private static boolean hasMemo(final List<Field> aFields)
    {
        for (final Field f : aFields)
        {
            if (f.getType() == Type.MEMO)
            {
                return true;
            }
        }

        return false;
    }

    /**
     * Opens the table for reading and writing.  Equivalent to
     * {@link Table#open(nl.knaw.dans.common.dbflib.IfNonExistent)  Table.open(IfNonExistent.ERROR)}
     *
     * @throws IOException if the table file does not exist or could not be opened
     */
    public void open()
              throws IOException, CorruptedTableException
    {
        open(IfNonExistent.ERROR);
    }

    /**
     * Opens the table for reading and writing.
     *
     * @param aIfNonExistent what to do if the table file does not exist yet
     *
     * @throws java.io.IOException if the table does not exist or cannot be opened
     */
    public void open(final IfNonExistent aIfNonExistent)
              throws IOException, CorruptedTableException
    {
        if (tableFile.exists())
        {
            raFile = new RandomAccessFile(tableFile, "rw");
            header.readAll(raFile);
        }
        else if (aIfNonExistent.isCreate())
        {
            raFile = new RandomAccessFile(tableFile, "rw");
            header.writeAll(raFile);
        }
        else if (aIfNonExistent.isError())
        {
            throw new FileNotFoundException("Input file " + tableFile + " not found");
        }
    }

    /**
     * Closes this table for reading and writing.
     *
     * @throws java.io.IOException if the table file or an associated file cannot be closed
     */
    public void close()
               throws IOException
    {
        try
        {
            if (raFile != null)
            {
                raFile.close();
            }
        }
        finally
        {
            raFile = null;
            ensureMemoClosed();
        }
    }

    /**
     * Closes and deletes the underlying table file and associated files.
     *
     * @throws java.io.IOException if the table file or an associated file cannot be closed.
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
     * seconds and milliseconds fields are always set to zero.  Also, the date time
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
        checkOpen();

        return header.getFields();
    }

    /**
     * Returns a <tt>Record</tt> iterator.
     *
     * @return a <tt>Record</tt> iterator
     *
     * @see Record
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
     * @throws IOException if the record could not be written to the database file
     *
     * @see Record
     */
    public void addRecord(final Record aRecord)
                   throws IOException, CorruptedTableException
    {
        checkOpen();

        raFile.seek(header.getLength() + (header.getRecordCount() * header.getRecordLength()));
        raFile.writeByte(MARKER_RECORD_VALID);

        for (Field field : header.getFields())
        {
            final Object value = aRecord.getValue(field.getName());
            final int length = field.getLength();
            final int decimalCount = field.getDecimalCount();

            switch (field.getType())
            {
                case NUMBER:
                    writeDouble((Double) value, length, decimalCount);

                    break;

                case FLOAT:
                    writeFloat((Double) value, length, decimalCount);

                    break;

                case CHARACTER:
                    Util.writeString(raFile, (String) value, length);

                    break;

                case LOGICAL:
                    writeBoolean((Boolean) value);

                    break;

                case DATE:

                    if (value == null)
                    {
                        writeSpaces(LENGTH_RECORD_DATE);
                    }
                    else
                    {
                        writeRecordDate((Date) value);
                    }

                    break;

                case MEMO:
                    writeMemo((String) value);

                    break;

                default:

                    /*
                     * This should not be possible.
                     */
                    throw new Error("Error: not all enumerated constants in Type are handled.  Contact library developers.");
            }
        }

        raFile.writeByte(MARKER_EOF);
        writeRecordCount(header.getRecordCount() + 1);
    }

    private void writeRecordCount(final int aRecordCount)
                           throws IOException
    {
        raFile.seek(DbfHeader.OFFSET_RECORD_COUNT);
        header.setRecordCount(aRecordCount);
        header.writeRecordCount(raFile);
    }

    private void checkOpen()
    {
        if (raFile == null)
        {
            throw new IllegalStateException("Table should be open for this operation");
        }
    }

    private Double readDouble(final int aLength)
                       throws IOException
    {
        String s = Util.readString(raFile, aLength);

        if (s.trim().isEmpty())
        {
            return null;
        }

        return Double.parseDouble(s);
    }

    private Number readBigDecimal(final int aLength)
                           throws IOException, ParseException
    {
        String s = Util.readString(raFile, aLength);

        if (s.trim().isEmpty())
        {
            return null;
        }

        return decimalParser.parse(s);
    }

    private Double readFloat(final int aLength)
                      throws IOException
    {
        return readDouble(aLength);
    }

    private Boolean readBoolean()
                         throws IOException
    {
        byte c = raFile.readByte();

        if (c == ' ')
        {
            return null;
        }

        return (c == 'Y') || (c == 'y') || (c == 'T') || (c == 't');
    }

    private Date readRecordDate()
                         throws IOException
    {
        final String yearString = Util.readString(raFile, 4);
        final String monthString = Util.readString(raFile, 2);
        final String dayString = Util.readString(raFile, 2);
        final Calendar cal = Calendar.getInstance();

        if (yearString.trim().isEmpty())
        {
            return null;
        }
        else
        {
            final int year = Integer.parseInt(yearString);
            final int month = Integer.parseInt(monthString) - 1;
            final int day = Integer.parseInt(dayString);

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

    private String readMemo(final int aFieldLength)
                     throws IOException, CorruptedTableException
    {
        ensureMemoOpened(IfNonExistent.ERROR);

        String sIndex = Util.readString(raFile, aFieldLength);

        if (sIndex.trim().isEmpty())
        {
            return "";
        }

        return memo.readMemo(Integer.parseInt(sIndex.trim()));
    }

    private void ensureMemoOpened(final IfNonExistent aIfNonExistent)
                           throws CorruptedTableException, IOException
    {
        if (memo != null)
        {
            return;
        }

        openMemo(aIfNonExistent);
    }

    private void ensureMemoClosed()
                           throws IOException
    {
        if (memo != null)
        {
            try
            {
                memo.close();
            }
            finally
            {
                memo = null;
            }
        }
    }

    private void openMemo(final IfNonExistent aIfNonExistent)
                   throws CorruptedTableException, IOException
    {
        File dbtFile = Util.getDbtFile(tableFile);

        if (dbtFile == null)
        {
            if (aIfNonExistent.isError())
            {
                throw new CorruptedTableException("Could not find .DBT file or multiple matches for .DBT file");
            }
            else if (aIfNonExistent.isCreate())
            {
                final String tableFilePath = tableFile.getPath();
                dbtFile = new File(tableFilePath.substring(0, tableFilePath.length() - ".dbf".length()) + ".dbt");
            }
        }

        memo =
            new Memo(dbtFile,
                     header.getVersion());
        memo.open(aIfNonExistent);
    }

    private Record readRecord(final int aIndex)
                       throws IOException, CorruptedTableException
    {
        raFile.seek(header.getLength() + (aIndex * header.getRecordLength()));

        byte firstByteOfRecord = raFile.readByte();

        while (firstByteOfRecord == MARKER_RECORD_DELETED)
        {
            raFile.skipBytes(header.getRecordLength() - 1);
        }

        if (firstByteOfRecord == MARKER_EOF)
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
                                     readDouble(field.getLength()));

                    break;

                case FLOAT:
                    recordValues.put(field.getName(),
                                     readFloat(field.getLength()));

                    break;

                case CHARACTER:
                    recordValues.put(field.getName(),
                                     Util.readString(raFile,
                                                     field.getLength()));

                    break;

                case LOGICAL:
                    recordValues.put(field.getName(),
                                     readBoolean());

                    break;

                case DATE:
                    recordValues.put(field.getName(),
                                     readRecordDate());

                    break;

                case MEMO:
                    recordValues.put(field.getName(),
                                     readMemo(field.getLength()));

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

    private void writeFloat(final Double aValue, final int aLength, final int aDecimalCount)
                     throws IOException
    {
        writeDouble(aValue, aLength, aDecimalCount);
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

        Util.writeString(raFile,
                         dateFormat.format(aRecordDate),
                         LENGTH_RECORD_DATE);
    }

    private void writeMemo(final String aMemo)
                    throws IOException, CorruptedTableException
    {
        if (aMemo == null || aMemo.isEmpty())
        {
            writeSpaces(LENGTH_MEMO_FIELD);
        }
        else
        {
            ensureMemoOpened(IfNonExistent.CREATE);

            int index = memo.writeMemo(aMemo);
            writeDouble(Double.valueOf(index),
                        LENGTH_MEMO_FIELD,
                        0);
        }
    }
}
