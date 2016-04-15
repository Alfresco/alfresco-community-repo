package org.alfresco.util.schemacomp;


import java.io.File;

import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.TempFileProvider;
import org.junit.Test;
import org.springframework.context.ApplicationContext;

/**
 * Tests for the DbToXML class.
 * 
 * @author Matt Ward
 */
public class DbToXMLTest
{
    @Test
    public void execute()
    {
        ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();
        File outFile = new File(TempFileProvider.getTempDir(), getClass().getSimpleName() + ".xml");
        System.out.println("Writing to temp file: " + outFile);
        DbToXML dbToXML = new DbToXML(ctx, outFile);
        dbToXML.execute();
    }
}
