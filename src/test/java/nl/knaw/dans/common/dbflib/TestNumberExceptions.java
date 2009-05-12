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
public class TestNumberExceptions
    extends BaseTestingCase
{
    private Table table;

    /**
     * Creates a new TestCharacterExceptions object.
     *
     * @param aVersion DOCUMENT ME!
     * @param aVersionDirectory DOCUMENT ME!
     */
    public TestNumberExceptions(Version aVersion, String aVersionDirectory)
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
        final File outputDir = new File("target/test-output/" + versionDirectory + "/types/NUMBER");
        outputDir.mkdirs();

        final File tableFile = new File(outputDir, "VALTOOLARGE.DBF");
        UnitTestUtil.remove(tableFile);

        final List<Field> fields = new ArrayList<Field>();
        fields.add(new Field("INTFIELD", Type.NUMBER, 5, 0));
        fields.add(new Field("DECFIELD", Type.NUMBER, 5, 2));

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
    @Test(expected = ValueTooLargeException.class)
    public void firstValueTooLarge()
                            throws IOException,
                                   CorruptedTableException,
                                   ValueTooLargeException,
                                   RecordTooLargeException
    {
        table.addRecord(123456);
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
    public void secondValueTooLarge()
                             throws IOException,
                                    CorruptedTableException,
                                    ValueTooLargeException,
                                    RecordTooLargeException
    {
        table.addRecord(0, 123.45);
    }
}
