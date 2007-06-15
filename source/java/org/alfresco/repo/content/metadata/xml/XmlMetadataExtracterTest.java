/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.content.metadata.xml;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;

import junit.framework.TestCase;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.content.filestore.FileContentReader;
import org.alfresco.repo.content.transform.AbstractContentTransformerTest;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.util.PropertyMap;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Tests various aspects of XML metadata extraction.
 * 
 * @see XPathMetadataExtracter
 * 
 * @author Derek Hulley
 */
public class XmlMetadataExtracterTest extends TestCase
{
    private static final String FILE_ALFRESCO_MODEL = "xml-metadata/alfresco-model-sample.xml";
    private static final String FILE_ECLIPSE_PROJECT = "xml-metadata/eclipse-project-sample.xml";
    
    private static final String CTX_LOCATION = "classpath:xml-metadata/xml-metadata-test-context.xml";
    private static final ApplicationContext ctx = new ClassPathXmlApplicationContext(CTX_LOCATION);
    
    private XPathMetadataExtracter alfrescoModelMetadataExtractor;
    private XPathMetadataExtracter eclipseProjectMetadataExtractor;

    /**
     * Get a reader for a file that should be on the classpath.
     */
    private static final ContentReader getReader(String fileName) throws FileNotFoundException
    {
        URL url = AbstractContentTransformerTest.class.getClassLoader().getResource(fileName);
        if (url == null)
        {
            throw new FileNotFoundException("Could not find file on classpath: " + fileName);
        }
        File file = new File(url.getFile());
        if (!file.exists())
        {
            throw new FileNotFoundException("Could not find file on classpath: " + fileName);
        }
        ContentReader reader = new FileContentReader(file);
        reader.setMimetype(MimetypeMap.MIMETYPE_XML);
        return reader;
    }
    
    @Override
    public void setUp() throws Exception
    {
        alfrescoModelMetadataExtractor = (XPathMetadataExtracter) ctx.getBean("extracter.xml.AlfrescoModelMetadataExtracter");
        eclipseProjectMetadataExtractor = (XPathMetadataExtracter) ctx.getBean("extracter.xml.EclipseProjectMetadataExtracter");
    }

    public void testSetUp()
    {
        assertNotNull(alfrescoModelMetadataExtractor);
        assertNotNull(eclipseProjectMetadataExtractor);
    }
    
    public void testExtractAlfresocModel() throws Exception
    {
        // Load the example file
        ContentReader reader = getReader(FILE_ALFRESCO_MODEL);
        assertTrue(reader.exists());
        
        // Pass it to the extracter
        PropertyMap checkProperties = new PropertyMap();
        alfrescoModelMetadataExtractor.extract(reader, checkProperties);
        
        // Check the values
        assertEquals("Gavin Cornwell", checkProperties.get(ContentModel.PROP_AUTHOR));
        assertEquals("fm:forummodel", checkProperties.get(ContentModel.PROP_TITLE));
        assertEquals("Forum Model", checkProperties.get(ContentModel.PROP_DESCRIPTION));
    }
    
    public void testExtractEclipseProject() throws Exception
    {
        // Load the example file
        ContentReader reader = getReader(FILE_ECLIPSE_PROJECT);
        assertTrue(reader.exists());
        
        // Pass it to the extracter
        PropertyMap checkProperties = new PropertyMap();
        eclipseProjectMetadataExtractor.extract(reader, checkProperties);
        
        // Check the values
        assertEquals("Repository", checkProperties.get(ContentModel.PROP_TITLE));
        assertEquals("JavaCC Nature", checkProperties.get(ContentModel.PROP_DESCRIPTION));
    }
}
