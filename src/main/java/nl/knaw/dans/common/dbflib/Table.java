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
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
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
    private static final int MARKER_RECORD_DELETED = 0x2A;
    private static final int MARKER_EOF = 0x1A;
    private static final int MARKER_RECORD_VALID = 0x20;

    private class RecordIterator
        implements Iterator<Record>
    {
        private int recordCounter = 0;
        private Record lastReadRecord = null;

        public boolean hasNext()
        {
            checkOpen();

            if (lastReadRecord == null && recordCounter < header.getRecordCount())
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
     * @param aVersion the dBase version to support
     * @param aFields the fields to create if this is a new table
     *
     * @see #open(nl.knaw.dans.common.dbflib.IfNonExistent)
     *
     * @throws IllegalArgumentException if <tt>aTableFiel</tt> is <tt>null</tt>
     */
    public Table(final File aTableFile, final Version aVersion, final List<Field> aFields)
          throws InvalidFieldTypeException, InvalidFieldLengthException
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
     * @throws CorruptedTableException if the header of the table file was corrupt
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
     * @throws java.io.IOException if the table does not exist or could be opened
     * @throws CorruptedTableException if the header of the table file was corrupt
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
     * @throws java.io.IOException if the table file or an associated file cannot be closed or deleted
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
     * Returns the date on which this table was last modified.  Note that the hours, minutes,
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
     * Returns a <tt>Record</tt> iterator.  Note that, to use the iterator the table
     * must be opened.
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
     * Constructs and adds a record.  The fields values for the record must be provided
     * as parameters in the same order that the fields are provided in the field list.
     *
     * @throws IOException if the record could not be written to the database file
     * @throws CorruptedTableException if the table was corrupt
     * @throws ValueTooLargeException if a field value exceeds the length of its corresponding field
     * @throws RecordTooLargeException if more field values are provided than there are field in this table
     */
    public void addRecord(final Object... aFieldValue)
                   throws IOException, DbfLibException
    {
        if (aFieldValue.length > header.getFields().size())
        {
            throw new RecordTooLargeException("Trying to add " + aFieldValue.length + " fields while there are only "
                                              + header.getFields().size() + " defined in the table file");
        }

        final Map<String, Value> map = new HashMap<String, Value>();
        final Iterator<Field> fieldIterator = header.getFields().iterator();

        for (final Object fieldValue : aFieldValue)
        {
            final Field field = fieldIterator.next();

            map.put(field.getName(),
                    createValueObject(fieldValue));
        }

        addRecord(new Record(map));
    }

    private Value createValueObject(final Object aValue)
    {
        if (aValue instanceof Number)
        {
            return new NumberValue((Number) aValue);
        }
        else if (aValue instanceof String)
        {
            return new StringValue((String) aValue);
        }
        else if (aValue instanceof Boolean)
        {
            return new BooleanValue((Boolean) aValue);
        }
        else if (aValue instanceof Date)
        {
            return new DateValue((Date) aValue);
        }
        else if (aValue instanceof byte[])
        {
            return new ByteArrayValue((byte[]) aValue);
        }

        return null;
    }

    /**
     * Adds a record to this table.
     *
     * @param aRecord the record to add.
     *
     * @throws IOException if the record could not be written to the database file
     * @throws CorruptedTableException if the table was corrupt
     * @throws ValueTooLargeException if a field value exceeds the length of its corresponding field
     *
     * @see Record
     */
    public void addRecord(final Record aRecord)
                   throws IOException, DbfLibException
    {
        checkOpen();

        raFile.seek(header.getLength() + (header.getRecordCount() * header.getRecordLength()));
        raFile.writeByte(MARKER_RECORD_VALID);

        for (final Field field : header.getFields())
        {
            byte[] raw = aRecord.getRawValue(field);

            if (raw == null)
            {
                raw = Util.repeat((byte) ' ',
                                  field.getLength());
            }
            else if (field.getType() == Type.MEMO || field.getType() == Type.BINARY || field.getType() == Type.GENERAL)
            {
                int index = writeMemo(raw);

                if (header.getVersion() == Version.DBASE_4 || header.getVersion() == Version.DBASE_5)
                {
                    raw = String.format("%0" + field.getLength() + "d", index).getBytes();
                }
                else
                {
                    raw = String.format("%" + field.getLength() + "d", index).getBytes();
                }
            }

            raFile.write(raw);
        }

        raFile.writeByte(MARKER_EOF);
        writeRecordCount(header.getRecordCount() + 1);
    }

    private int writeMemo(final byte[] aMemoText)
                   throws IOException, CorruptedTableException
    {
        ensureMemoOpened(IfNonExistent.CREATE);

        return memo.writeMemo(aMemoText);
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

    private byte[] readMemo(final String aMemoIndex)
                     throws IOException, CorruptedTableException
    {
        ensureMemoOpened(IfNonExistent.ERROR);

        if (aMemoIndex.trim().isEmpty())
        {
            return null;
        }

        return memo.readMemo(Integer.parseInt(aMemoIndex.trim()));
    }

    private void ensureMemoOpened(final IfNonExistent aIfNonExistent)
                           throws IOException, CorruptedTableException
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

    /**
     * Opens the memo of this table.
     *
     * @param aIfNonExistent what to do if the memo file doesn't exist. (Cannot be IGNORE.)
     * @throws java.io.IOException if the memo file could not be opened
     * @throws nl.knaw.dans.common.dbflib.CorruptedTableException if the memo file could not be found or multiple
     *   matches exist, or if it is corrupt
     */
    private void openMemo(final IfNonExistent aIfNonExistent)
                   throws IOException, CorruptedTableException
    {
        File memoFile = Util.getMemoFile(tableFile,
                                         header.getVersion());

        if (memoFile == null)
        {
            final String extension = (header.getVersion() == Version.FOXPRO_26 ? ".fpt" : ".dbt");

            if (aIfNonExistent.isError())
            {
                throw new CorruptedTableException("Could not find file '" + Util.stripExtension(tableFile.getPath())
                                                  + extension + "' (or multiple matches for the file)");
            }
            else if (aIfNonExistent.isCreate())
            {
                final String tableFilePath = tableFile.getPath();
                memoFile = new File(tableFilePath.substring(0, tableFilePath.length() - ".dbf".length()) + extension);
            }
            else
            {
                assert false : "Programming error: cannot ignore non existing memo.";
            }
        }

        memo =
            new Memo(memoFile,
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

        final Map<String, Value> recordValues = new HashMap<String, Value>();

        for (final Field field : header.getFields())
        {
            final byte[] rawData = Util.readStringBytes(raFile,
                                                        field.getLength());

            switch (field.getType())
            {
                case NUMBER:
                case FLOAT:
                    recordValues.put(field.getName(),
                                     new NumberValue(rawData));

                    break;

                case CHARACTER:
                    recordValues.put(field.getName(),
                                     new StringValue(rawData));

                    break;

                case LOGICAL:
                    recordValues.put(field.getName(),
                                     new BooleanValue(rawData));

                    break;

                case DATE:
                    recordValues.put(field.getName(),
                                     new DateValue(rawData));

                    break;

                case MEMO:
                    recordValues.put(field.getName(),
                                     new StringValue(readMemo(new String(rawData))));

                    break;

                case GENERAL:
                case BINARY:
                case PICTURE:
                    recordValues.put(field.getName(),
                                     new ByteArrayValue(readMemo(new String(rawData))));

                    break;

                default:
                    assert false : "Programming error: not all data types handled.";

                    return null;
            }
        }

        return new Record(recordValues);
    }
}
