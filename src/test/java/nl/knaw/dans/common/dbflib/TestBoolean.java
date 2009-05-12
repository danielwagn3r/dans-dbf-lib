package nl.knaw.dans.common.dbflib;

import static org.junit.Assert.*;

import org.junit.Test;

import org.junit.runner.RunWith;

import org.junit.runners.Parameterized;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Tests reading and writing boolean fields
 *
 * @author Vesa Åkerman
 */
@RunWith(Parameterized.class)
public class TestBoolean
    extends BaseTestingCase
{
    /**
     * Creates a new TestBooolean object.
     *
     * @param aVersion DOCUMENT ME!
     * @param aVersionDirectory DOCUMENT ME!
     */
    public TestBoolean(Version aVersion, String aVersionDirectory)
    {
        super(aVersion, aVersionDirectory);
    }

    /**
     * tests reading of boolean fields
     *
     * @throws IOException DOCUMENT ME!
     * @throws CorruptedTableException DOCUMENT ME!
     */
    @Test
    public void readBoolean()
                     throws IOException, CorruptedTableException
    {
        final Table table = new Table(new File("src/test/resources/" + versionDirectory + "/types/BOOLEAN.DBF"));

        try
        {
            table.open(IfNonExistent.ERROR);

            final Iterator<Record> recordIterator = table.recordIterator();

            Record r = recordIterator.next();
            assertEquals(true,
                         r.getBooleanValue("BOOLEAN"));

            r = recordIterator.next();
            assertEquals(false,
                         r.getBooleanValue("BOOLEAN"));
        }
        finally
        {
            table.close();
        }
    }

    /**
    * tests writing of boolean fields
    *
    * @throws IOException DOCUMENT ME!
    * @throws CorruptedTableException DOCUMENT ME!
    */
    @Test
    public void writeBoolean()
                      throws IOException, CorruptedTableException, ValueTooLargeException, RecordTooLargeException
    {
        final File outputDir = new File("target/test-output/" + versionDirectory + "/types/BOOLEAN");
        outputDir.mkdirs();

        final File tableFile = new File(outputDir, "WRITEBOOLEAN.DBF");
        UnitTestUtil.remove(tableFile);

        final List<Field> fields = new ArrayList<Field>();
        fields.add(new Field("BOOLEAN", Type.LOGICAL, 1));

        final Table table = new Table(tableFile, version, fields);

        try
        {
            table.open(IfNonExistent.CREATE);

            table.addRecord(true);
            table.addRecord(false);
            table.addRecord();
        }
        finally
        {
            table.close();
        }
    }
}
