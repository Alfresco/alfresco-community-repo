package org.alfresco.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import junit.framework.TestCase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;

/**
 * Test class created for MNT-21472
 * Space in Windows installation path for ACS 6.2 causes repeated rendition exceptions in logs
 */
public class ConfigFileFinderTest extends TestCase
{
    private static final Log log = LogFactory.getLog(ConfigFileFinderTest.class);
    private static final String TEST_PATH_WITHOUT_SPACES = "org/alfresco/util/configfilefinder/folderwithoutspaces";
    private static final String TEST_PATH_WITH_SPACES = "org/alfresco/util/configfilefinder/folder with spaces";

    private final ObjectMapper jsonObjectMapper = new ObjectMapper();
    ConfigFileFinder fileFinder = new ConfigFileFinder(jsonObjectMapper)
    {
        @Override protected void readJson(JsonNode jsonNode, String readFromMessage, String baseUrl) throws IOException
        {
            // Not applicable for testReadFiles()
        }
    };

    public void testReadFiles()
    {
        Boolean succesRead;

        succesRead = fileFinder.readFiles(TEST_PATH_WITHOUT_SPACES, log);
        assertTrue("Expected to read files from folder without spaces successfully", succesRead);

        succesRead = fileFinder.readFiles(TEST_PATH_WITH_SPACES, log);
        assertTrue("Expected to read files from folder with spaces successfully", succesRead);
    }
}
