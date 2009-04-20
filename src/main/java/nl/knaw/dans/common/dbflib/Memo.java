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
    private static final int OFFSET_VERSION = 16;

    /*
     * Lengths.
     */
    private static final int LENGTH_MEMO_BLOCK = 512;
    private static final int LENGTH_NEXT_AVAILABLE_BLOCK_INDEX = 4;

    /*
     * Markers.
     */
    private static final byte MARKER_SOFT_RETURN = (byte) 0x8d;
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
    String readMemo(final int aBlockIndex)
             throws IOException, CorruptedTableException
    {
        final StringBuilder sb = new StringBuilder();
        int c = 0;
        raf.seek(aBlockIndex * LENGTH_MEMO_BLOCK);

        while ((c = raf.read()) != MARKER_MEMO_END)
        {
            if (c == -1)
            {
                throw new CorruptedTableException("Corrupted memo file");
            }

            if ((byte) c == MARKER_SOFT_RETURN)
            {
                /**
                 * Ignore soft returns and the linefeed that succeeds them.
                 */
                raf.read();

                continue;
            }

            sb.append((char) c);
        }

        return sb.toString();
    }

    /**
     * Writes a string of characters to memo file.
     */
    int writeMemo(final String aMemoText)
           throws IOException
    {
        final int nrBytesToWrite = aMemoText.length() + 2;
        int nrBlocksToWrite = nrBytesToWrite / LENGTH_MEMO_BLOCK + 1;
        final int nrSpacesToPadLastBlock = nrBytesToWrite % LENGTH_MEMO_BLOCK;

        /*
         * Exact fit; we don't need an extra block.
         */
        if (nrSpacesToPadLastBlock == 0)
        {
            --nrBlocksToWrite;
        }

        final int blockIndex = nextAvailableBlock;

        /*
         * Write the string and end of file markers.
         */
        raf.seek(blockIndex * LENGTH_MEMO_BLOCK);
        raf.writeBytes(aMemoText); // Note: cuts off higher bytes, so assumes ASCII string
        raf.writeByte(MARKER_MEMO_END);
        raf.writeByte(MARKER_MEMO_END);

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
         * Fill the bytes up to version-field with zeros.
         */
        for (int i = LENGTH_NEXT_AVAILABLE_BLOCK_INDEX; i < OFFSET_VERSION; i++)
        {
            raf.writeByte(0x00);
        }

        /*
         * Write the version of the DBF-file.  Seems always to be 0x03?
         */
        raf.writeByte(0x03);

        /*
         * Rest of the header is filled with zeros
         */
        for (int i = OFFSET_VERSION; i < LENGTH_MEMO_BLOCK; i++)
        {
            raf.writeByte(0x00);
        }
    }
}
