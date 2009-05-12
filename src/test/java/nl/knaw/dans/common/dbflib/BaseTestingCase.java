package nl.knaw.dans.common.dbflib;

import org.junit.runners.Parameterized.Parameters;

import java.util.Arrays;
import java.util.Collection;

/**
 *
 * @author Vesa Åkerman
 */
public class BaseTestingCase
{
    protected Version version;
    protected String versionDirectory;

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    @Parameters
    public static Collection<Object[]> data()
    {
        final Object[][] testParameters =
            new Object[][]
            {
                { Version.DBASE_3, "dbase3plus" },
                { Version.DBASE_4, "dbase4" },
                { Version.DBASE_5, "dbase5" }
            };

        return Arrays.asList(testParameters);
    }

    protected BaseTestingCase(Version aVersion, String aVersionDirectory)
    {
        version = aVersion;
        versionDirectory = aVersionDirectory;
    }
}
