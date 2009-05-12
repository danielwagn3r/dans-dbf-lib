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
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Represents a memo (.DBT) file.
 *
 * @author Vesa Åkerman
 * @author Jan van Mansum
 */
class Memo
{
    /*
     * Offsets.
     */
    private static final int OFFSET_NEXT_AVAILABLE_BLOCK_INDEX = 0;
    private static final int OFFSET_FILE_NAME = 8;
    private static final int OFFSET_VERSION = 16;
    private static final int OFFSET_BLOCK_SIZE = 20;

    /*
     * Lengths.
     */
    private static final int LENGTH_MEMO_BLOCK = 512;
    private static final int LENGTH_NEXT_AVAILABLE_BLOCK_INDEX = 4;
    private static final int LENGTH_FILE_NAME = 8;

    /*
     * Markers.
     */
    private static final byte MARKER_MEMO_END = 0x1a;

    /*
     * Fields.
     */
    private final File memoFile;
    private RandomAccessFile raf = null;
    private int nextAvailableBlock = 0;
    private Version version;

    /**
     * Creates a new <tt>Memo</tt> object.
     *
     * @param aMemoFile the underlying .DBT file
     *
     * @throws IllegalArgumentException if <tt>aMemoFile</tt> is <tt>null</tt>
     */
    Memo(final File aMemoFile, final Version aVersion)
        throws IllegalArgumentException
    {
        if (aMemoFile == null)
        {
            throw new IllegalArgumentException("Memo file must not be null");
        }

        memoFile = aMemoFile;
        version = aVersion;
    }

    /**
     *
     * @throws java.io.IOException
     */
    void open()
       throws IOException
    {
        open(IfNonExistent.ERROR);
    }

    void open(final IfNonExistent aIfNonExistent)
       throws IOException
    {
        if (memoFile.exists())
        {
            raf = new RandomAccessFile(memoFile, "rw");
        }
        else if (aIfNonExistent.isCreate())
        {
            raf = new RandomAccessFile(memoFile, "rw");
            nextAvailableBlock = 1;
            writeMemoHeader();
        }
        else if (aIfNonExistent.isError())
        {
            throw new FileNotFoundException("Cannot find memo file");
        }
    }

    /**
     * Closes the memo file for reading and writing.
     *
     * @throws java.io.IOException if the file cannot be closed
     */
    void close()
        throws IOException
    {
        if (raf == null)
        {
            return;
        }

        raf.close();
    }

    /**
     * Closes and deletes the underlying memo file.
     *
     * @throws java.io.IOException if the file cannot be closed.
     */
    void delete()
         throws IOException
    {
        close();
        memoFile.delete();
    }

    /**
     * Reads a string of characters from memo file.
     *
     * @param aBlockIndex blocknumber  where the
     *          string of characters starts
     *
     */
    byte[] readMemo(final int aBlockIndex)
             throws IOException, CorruptedTableException
    {
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        int c = 0;

        raf.seek(aBlockIndex * LENGTH_MEMO_BLOCK);

        switch (version)
        {
            case DBASE_3:

                while ((c = raf.read()) != MARKER_MEMO_END)
                {
                    if (c == -1)
                    {
                        throw new CorruptedTableException("Corrupted memo file, EOF exception");
                    }

                    bos.write(c);
                }

                break;

            case DBASE_4:
            case DBASE_5:
                /* at the beginning of each memo there is a header of 8 bytes.
                 * 4 first bytes: FFFF0800h
                 * 4 last bytes: offset to the end of memo (length of data + 8)
                 */
                raf.skipBytes(4);

                int memoLength = Util.changeEndianness(raf.readInt()) - version.getMemoDataOffset();

                for (int i = 0; i < memoLength; i++)
                {
                    c = raf.read();

                    if (c == -1)
                    {
                        throw new CorruptedTableException("Corrupted memo file, EOF exception");
                    }

                    bos.write(c);
                }

                break;

            default:
                throw new IllegalArgumentException("Unsupported version " + version.toString());
        }

        return bos.toByteArray();
    }

    /**
     * Writes a string of characters to memo file.
     */
    int writeMemo(final byte[] aMemoBytes)
           throws IOException
    {
        final int nrBytesToWrite =
            aMemoBytes.length + version.getMemoFieldEndMarkerLength() + version.getMemoDataOffset();
        int nrBlocksToWrite = nrBytesToWrite / LENGTH_MEMO_BLOCK + 1;
        int nrSpacesToPadLastBlock = LENGTH_MEMO_BLOCK - nrBytesToWrite % LENGTH_MEMO_BLOCK;

        /*
         * Exact fit; we don't need an extra block.
         */
        if (nrSpacesToPadLastBlock == LENGTH_MEMO_BLOCK)
        {
            nrSpacesToPadLastBlock = -LENGTH_MEMO_BLOCK;
            --nrBlocksToWrite;
        }

        final int blockIndex = nextAvailableBlock;

        /*
         * Write the string and end of file markers.
         */
        raf.seek(blockIndex * LENGTH_MEMO_BLOCK);

        if (version == Version.DBASE_4 || version == Version.DBASE_5)
        {
            raf.writeInt(0xffff0800);
            raf.writeInt(Util.changeEndianness(aMemoBytes.length + version.getMemoDataOffset()));
        }

        raf.write(aMemoBytes); // Note: cuts off higher bytes, so assumes ASCII string

        if (version.getMemoFieldEndMarkerLength() != 0)
        {
            raf.writeShort(version.getMemoFieldEndMarker());
        }

        /*
         * Pad the last block with zeros.
         */
        for (int i = 0; i < nrSpacesToPadLastBlock; ++i)
        {
            raf.writeByte(0x00);
        }

        /*
         * Updated next available block to write.
         */
        raf.seek(OFFSET_NEXT_AVAILABLE_BLOCK_INDEX);
        nextAvailableBlock += nrBlocksToWrite;
        raf.writeInt(Util.changeEndianness(nextAvailableBlock));

        return blockIndex;
    }

    /*
     * Writes a header for a new memo file.
     */
    private void writeMemoHeader()
                          throws IOException
    {
        /*
         * Number of next available block intialized to zero.
         */
        raf.writeInt(Util.changeEndianness(nextAvailableBlock));

        /*
         * Fill the bytes up to file name with zeros.
         */
        for (int i = LENGTH_NEXT_AVAILABLE_BLOCK_INDEX; i < OFFSET_FILE_NAME; i++)
        {
            raf.writeByte(0x00);
        }

        /*
         * Write the file name. In dBaseIV and V.
         */
        Util.writeString(raf,
                         Util.stripExtension(memoFile.getName()).toUpperCase(),
                         LENGTH_FILE_NAME);

        /*
         * Meaning of the following bytes not clear.  These values in all .dbt files
         * that we have seen have the following values.  In dBaseIV and V.
         */
        raf.writeByte(0x00);
        raf.writeByte(0x00);
        raf.writeByte(0x02);
        raf.writeByte(0x01);

        /*
         * Write the block size.  In dBaseIV and V.
         */
        raf.writeShort(Util.changeEndianness((short) LENGTH_MEMO_BLOCK));

        /*
         * Rest of the header is filled with zeros
         */
        for (int i = OFFSET_BLOCK_SIZE + 2; i < LENGTH_MEMO_BLOCK; i++)
        {
            raf.writeByte(0x00);
        }
    }
}
