package nl.knaw.dans.common.dbflib;


/**
 *
 * @author Vesa Åkerman
 */
public abstract class GenericTest
{
    final Version version;
    final String versionDirectory;

    protected GenericTest(Version aVersion, String aVersionDirectory)
    {
        version = aVersion;
        versionDirectory = aVersionDirectory;
    }
}
