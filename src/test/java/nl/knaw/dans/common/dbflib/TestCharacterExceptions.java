package nl.knaw.dans.common.dbflib;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.junit.runner.RunWith;

import org.junit.runners.Parameterized;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Vesa Åkerman
 */
@RunWith(Parameterized.class)
public class TestCharacterExceptions
    extends BaseTestCase
{
    private Table table;

    /**
     * Creates a new TestCharacterExceptions object.
     *
     * @param aVersion DOCUMENT ME!
     * @param aVersionDirectory DOCUMENT ME!
     */
    public TestCharacterExceptions(Version aVersion, String aVersionDirectory)
    {
        super(aVersion, aVersionDirectory);
    }

    /**
     * DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     * @throws CorruptedTableException DOCUMENT ME!
     */
    @Before
    public void setUp()
               throws IOException, CorruptedTableException
    {
        final File outputDir = new File("target/test-output/" + versionDirectory + "/types/CHARACTER");
        outputDir.mkdirs();

        final File tableFile = new File(outputDir, "WRITECHAR.DBF");
        UnitTestUtil.remove(tableFile);

        final List<Field> fields = new ArrayList<Field>();
        fields.add(new Field("CHAR1", Type.CHARACTER, 20, 0));
        fields.add(new Field("CHAR2", Type.CHARACTER, 253, 0));

        table = new Table(tableFile, version, fields);
        table.open(IfNonExistent.CREATE);
    }

    /**
     * DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     */
    @After
    public void tearDown()
                  throws IOException
    {
        table.close();
    }

    /**
     * DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     * @throws CorruptedTableException DOCUMENT ME!
     * @throws ValueTooLargeException DOCUMENT ME!
     * @throws RecordTooLargeException DOCUMENT ME!
     */
    @Test
    public void fitsComfortably()
                         throws IOException, CorruptedTableException, ValueTooLargeException, RecordTooLargeException
    {
        table.addRecord("Less than 20", "This is not at all long");
    }

    /**
     * DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     * @throws CorruptedTableException DOCUMENT ME!
     * @throws ValueTooLargeException DOCUMENT ME!
     * @throws RecordTooLargeException DOCUMENT ME!
     */
    @Test
    public void fitsExactly()
                     throws IOException, CorruptedTableException, ValueTooLargeException, RecordTooLargeException
    {
        table.addRecord("This is exactly 20 c",
                        "This is exactly 253 characters, which is the limit for character fields in DBase products"
                        + "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"
                        + "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx!!!");
    }

    /**
     * DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     * @throws CorruptedTableException DOCUMENT ME!
     * @throws ValueTooLargeException DOCUMENT ME!
     * @throws RecordTooLargeException DOCUMENT ME!
     */
    @Test(expected = ValueTooLargeException.class)
    public void firstFieldDoesNotFit()
                              throws IOException,
                                     CorruptedTableException,
                                     ValueTooLargeException,
                                     RecordTooLargeException
    {
        table.addRecord("This is more than 20 characters", "This long field is ok");
    }

    /**
     * DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     * @throws CorruptedTableException DOCUMENT ME!
     * @throws ValueTooLargeException DOCUMENT ME!
     * @throws RecordTooLargeException DOCUMENT ME!
     */
    @Test(expected = ValueTooLargeException.class)
    public void secondFieldDoesNotFit()
                               throws IOException,
                                      CorruptedTableException,
                                      ValueTooLargeException,
                                      RecordTooLargeException
    {
        table.addRecord("This is ok",
                        "This is more than 253 characters, which is the limit for character fields in DBase products"
                        + "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"
                        + "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"
                        + "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"
                        + "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"
                        + "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"
                        + "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"
                        + "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"
                        + "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"
                        + "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"
                        + "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"
                        + "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx!!!");
    }
}
