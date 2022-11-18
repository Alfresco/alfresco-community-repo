/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
 * 
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
/*
 * Copyright (C) 2005 Jesper Steen Møller
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.repo.content.metadata;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import junit.framework.TestCase;

import org.alfresco.MiscContextTestSuite;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.content.filestore.FileContentReader;
import org.alfresco.repo.content.filestore.FileContentWriter;
import org.alfresco.repo.content.transform.AbstractContentTransformerTest;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.PropertyMap;
import org.alfresco.util.TempFileProvider;
import org.joda.time.DateTimeZone;
import org.springframework.context.ApplicationContext;

/**
 * @see org.alfresco.repo.content.metadata.MetadataExtracter
 *
 * @author Jesper Steen Møller
 */
public abstract class AbstractMetadataExtracterTest extends TestCase
{
   /**
    * This context will be fetched each time, but almost always
    *  will have been cached by {@link ApplicationContextHelper}
    */
    protected ApplicationContext ctx;
    
    public static final String QUICK_TITLE = "The quick brown fox jumps over the lazy dog";
    public static final String QUICK_DESCRIPTION = "Pangram, fox, dog, Gym class featuring a brown fox and lazy dog";
    public static final String QUICK_CREATOR = "Nevin Nollop";
    public static final String QUICK_CREATOR_EMAIL = "nevin.nollop@alfresco.com";
    public static final String QUICK_PREVIOUS_AUTHOR = "Derek Hulley";

    protected MimetypeMap mimetypeMap;
    protected DictionaryService dictionaryService;

    protected abstract MetadataExtracter getExtracter();

    /**
     * Ensures that the temp locations are cleaned out before the tests start
     */
    @Override
    public void setUp() throws Exception
    {
        // Grab the context, which will normally have been
        //  cached by the ApplicationContextHelper
        ctx = MiscContextTestSuite.getMinimalContext();
        
        this.mimetypeMap = (MimetypeMap) ctx.getBean("mimetypeService");
        this.dictionaryService = (DictionaryService) ctx.getBean("dictionaryService");
        
        // perform a little cleaning up
        long now = System.currentTimeMillis();
        TempFileProvider.TempFileCleanerJob.removeFiles(now);
        
        TimeZone tz = TimeZone.getTimeZone("GMT");
        TimeZone.setDefault(tz);
        // Joda time has already grabbed the JVM zone so re-set it here
        DateTimeZone.setDefault(DateTimeZone.forTimeZone(tz));
    }
    
    /**
     * Check that all objects are present
     */
    public void testSetUp() throws Exception
    {
        assertNotNull("MimetypeMap not present", mimetypeMap);
        // check that the quick resources are available
        File sourceFile = AbstractContentTransformerTest.loadQuickTestFile("txt");
        assertNotNull("quick.* files should be available from Tests", sourceFile);
    }
    
    protected void testExtractFromMimetype(String mimetype) throws Exception
    {
        try
        {
            Map<QName, Serializable> properties = extractFromMimetype(mimetype);
            // check we got something
            
            assertFalse("extractFromMimetype should return at least some properties, none found for " + mimetype,
                    properties.isEmpty());
            
            // check common metadata
            testCommonMetadata(mimetype, properties);
            // check file-type specific metadata
            testFileSpecificMetadata(mimetype, properties);
        }
        catch (FileNotFoundException e)
        {
            // The test file is not there.  We won't fail it.
           System.err.println("No test file found for mime type " + mimetype + 
                 ", skipping extraction test - " + e.getMessage());
        }
    }

    protected Map<QName, Serializable> extractFromMimetype(String mimetype) throws Exception
    {
        // get the extension for the mimetype
        String ext = mimetypeMap.getExtension(mimetype);

        // attempt to get a source file for each mimetype
        File sourceFile = AbstractContentTransformerTest.loadQuickTestFile(ext);
        if (sourceFile == null)
        {
            throw new FileNotFoundException("No quick." + ext + " file found for test");
        }
        return extractFromFile(sourceFile, mimetype);
    }

    protected Map<QName, Serializable> extractFromFile(File sourceFile, String mimetype) throws Exception
    {
        Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
        // construct a reader onto the source file
        ContentReader sourceReader = new FileContentReader(sourceFile);
        sourceReader.setMimetype(mimetype);
        getExtracter().extract(sourceReader, properties);
        return properties;
    }

    /**
     * Tests that we can get the common metadata correctly
     *  from the file.
     * You only need to override this if your test data file
     *  doesn't have the usual Nevin Nollop/quick brown fox 
     *  data in it.
     */
    protected void testCommonMetadata(String mimetype, Map<QName, Serializable> properties)
    {
       // One of Creator or Author
       if(!skipAuthorCheck(mimetype)) 
       {
          if(properties.containsKey(ContentModel.PROP_CREATOR)) 
          {
             assertEquals(
                   "Property " + ContentModel.PROP_CREATOR + " not found for mimetype " + mimetype,
                   QUICK_CREATOR,
                   DefaultTypeConverter.INSTANCE.convert(String.class, properties.get(ContentModel.PROP_CREATOR)));
          } 
          else if(properties.containsKey(ContentModel.PROP_AUTHOR)) 
          {
             assertEquals(
                   "Property " + ContentModel.PROP_AUTHOR + " not found for mimetype " + mimetype,
                   QUICK_CREATOR,
                   DefaultTypeConverter.INSTANCE.convert(String.class, properties.get(ContentModel.PROP_AUTHOR)));
          } 
          else 
          {
             fail("Expected one property out of " + ContentModel.PROP_CREATOR + " and " + 
                   ContentModel.PROP_AUTHOR + " but found neither of them for " + mimetype);
          }
       }
       
       // Title and description
       assertEquals(
                "Property " + ContentModel.PROP_TITLE + " not found for mimetype " + mimetype,
                QUICK_TITLE,
                DefaultTypeConverter.INSTANCE.convert(String.class, properties.get(ContentModel.PROP_TITLE)));
       if (!skipDescriptionCheck(mimetype)) 
       {
           assertEquals(
                   "Property " + ContentModel.PROP_DESCRIPTION + " not found for mimetype " + mimetype,
                   QUICK_DESCRIPTION,
                   DefaultTypeConverter.INSTANCE.convert(String.class, properties.get(ContentModel.PROP_DESCRIPTION)));
       }
    }
    protected abstract void testFileSpecificMetadata(String mimetype, Map<QName, Serializable> properties);
    
    /**
     * This method can be overridden to cause the author/creator property check to be skipped.
     * The default behaviour is for the check not to be skipped for all MIME types.
     * 
     * @param mimetype String
     * @return <code>true</code> to skip the checks, else <code>false</code>
     */
    protected boolean skipAuthorCheck(String mimetype)
    {
        return false;
    }
    
    /**
     * This method can be overridden to cause the description property check to be skipped.
     * The default behaviour is for the check not to be skipped for all MIME types.
     * 
     * @param mimetype String
     * @return <code>true</code> to skip the checks, else <code>false</code>
     */
    protected boolean skipDescriptionCheck(String mimetype)
    {
        return false;
    }
    
    
    public void testZeroLengthFile() throws Exception
    {
        MetadataExtracter extractor = getExtracter();
        File file = TempFileProvider.createTempFile(getName(), ".bin");
        ContentWriter writer = new FileContentWriter(file);
        writer.getContentOutputStream().close();
        ContentReader reader = writer.getReader();
        // Try the zero length file against all supported mimetypes.
        // Note: Normally the reader would need to be fetched for each access, but we need to be sure
        // that the content is not accessed on the reader AT ALL.
        PropertyMap properties = new PropertyMap();
        List<String> mimetypes = mimetypeMap.getMimetypes();
        for (String mimetype : mimetypes)
        {
            if (!extractor.isSupported(mimetype))
            {
                // Not interested
                continue;
            }
            reader.setMimetype(mimetype);
            extractor.extract(reader, properties);
            assertEquals("There should not be any new properties", 0, properties.size());
        }
    }
    
    
    protected static void assertContains(String message, String needle, String haystack) 
    {
       if(haystack.indexOf(needle) > -1) 
       {
          return;
       }
       fail(message);
    }
    protected static void assertContains(String needle, String haystack) 
    {
       assertContains("'" + needle + "' wasn't found in '" + haystack + "'", needle, haystack);
    }
}
