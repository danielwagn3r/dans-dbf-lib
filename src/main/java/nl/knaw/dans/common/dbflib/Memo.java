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
 */
class Memo
{
    private static final int MEMO_BLOCK_LENGTH = 512;
    private static final int OFFSET_VERSION = 16;
    private File memoFile = null;
    private RandomAccessFile raf = null;
    private int nextAvailableBlock = 0;
    private int version = 0;

    /**
     * Creates a new Memo object.
     *
     * @param aMemoFile the <tt>
     */
    public Memo(File aMemoFile)
    {
        if (aMemoFile == null)
        {
            throw new IllegalArgumentException("Memo file must not be null");
        }

        memoFile = aMemoFile;
    }

    /**
     * Opens the memo file for reading and writing.  Equivalent to
     * {@link Memo#open(boolean) Memo.open(false)}
     *
     * @throws IOException if the memo file does not exist
     */
    public void open()
              throws IOException
    {
        open(false, 0);
    }

    /**
     * Opens the memo file for reading and writing.
     *
     * @param aCreate create the memo file if it does not exist
     * @param aVersion .DBF file version
     *
     * @throws java.io.IOException if the memo file does not exist and <tt>aCreate</tt> is <tt>false</tt>
     */
    public void open(boolean aCreate, int aVersion)
              throws IOException
    {
        if (aCreate)
        {
            if (memoFile.exists())
            {
                throw new IOException("Memofile " + memoFile + " already exists");
            }

            version = aVersion;
            raf = new RandomAccessFile(memoFile, "rw");
            writeMemoHeader();
            nextAvailableBlock = 1;
        }
        else
        {
            if (memoFile == null)
            {
                throw new FileNotFoundException("Memo file not found");
            }

            raf = new RandomAccessFile(memoFile, "rw");
        }
    }

    /**
     * Closes the memo file for reading and writing.
     *
     * @throws java.io.IOException if the file cannot be closed
     */
    public void close()
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
    public void delete()
                throws IOException
    {
        close();
        memoFile.delete();
    }

    /**
     * Reads a string of characters from memo file.
     * 
     * @param aIndex blocknumber  where the
     *          string of characters starts
     *
     */
    public String readMemo(int aIndex)
                    throws IOException
    {
        long offset = aIndex * MEMO_BLOCK_LENGTH;
        int c = 0;
        StringBuilder sb = new StringBuilder();

        raf.seek(offset);

        while ((c = raf.read()) != 0x1a)
        {
            if (c == 0x8d)
            {
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
    public int writeMemo(String aMemo)
                  throws IOException
    {
        raf.seek(nextAvailableBlock * MEMO_BLOCK_LENGTH);

        /* Count the number of bytes to write. Memo fields are always written in
         * full blocks of 512 bytes. The end of the field is marked with two
         * 0x1a value bytes.
         */
        int nrBlocks = ((aMemo.length() + 2) / MEMO_BLOCK_LENGTH) + 1;

        if (((aMemo.length() + 2) % MEMO_BLOCK_LENGTH) == 0)
        {
            nrBlocks--;
        }

        writeString(aMemo, nrBlocks * MEMO_BLOCK_LENGTH);

        // update the number-of-next-available-block
        int indexToWrittenMemo = nextAvailableBlock;
        nextAvailableBlock += nrBlocks;
        raf.seek(0);
        raf.writeInt(Util.changeEndianness(nextAvailableBlock));

        return indexToWrittenMemo;
    }

    /*
    * Writes a header for a memo file.
    */
    private void writeMemoHeader()
                          throws IOException
    {
        // number of next available block intialized to zero
        raf.writeInt(0);

        // fill the bytes up to version-field with 00h
        for (int i = 4; i < OFFSET_VERSION; i++)
        {
            raf.writeByte(0x00);
        }

        // write the version of the DBF-file
        // for DBaseIII always '03h'?
        raf.writeByte(version);

        // rest of the header is filled with 00h
        for (int i = OFFSET_VERSION; i < MEMO_BLOCK_LENGTH; i++)
        {
            raf.writeByte(0x00);
        }
    }

    /*
    * Writes a string of characters to a memo file.
    * The string is terminated with two value 0x1a bytes
    */
    private void writeString(String s, int aLength)
                      throws IOException
    {
        char[] charArray = new char[aLength + 1];
        int lengthString = s.length();
        int i = 0;

        charArray = s.toCharArray();

        if (aLength <= 0)
        {
            aLength = lengthString;
        }

        // write the contents of the input string
        for (i = 0; (i < lengthString) && (i < (aLength - 2)); i++)
        {
            raf.writeByte(charArray[i]);
        }

        // write field terminators
        raf.writeByte(0x1a);
        raf.writeByte(0x1a);
        i += 2;

        // fill the rest of the block with 0x0h
        for (; i < aLength; i++)
        {
            raf.writeByte(0x00);
        }
    }
}
