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
import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.content.transform.AbstractContentTransformerTest;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.namespace.QName;

/**
 * @author Derek Hulley
 * @since 3.2
 */
public class MailMetadataExtracterTest extends AbstractMetadataExtracterTest
{
    private MailMetadataExtracter extracter;

    @Override
    public void setUp() throws Exception
    {
        super.setUp();
        extracter = new MailMetadataExtracter();
        extracter.setDictionaryService(dictionaryService);
        extracter.register();
    }

    /**
     * @return Returns the same transformer regardless - it is allowed
     */
    protected MetadataExtracter getExtracter()
    {
        return extracter;
    }

    public void testSupports() throws Exception
    {
        for (String mimetype : MailMetadataExtracter.SUPPORTED_MIMETYPES)
        {
            boolean supports = extracter.isSupported(mimetype);
            assertTrue("Mimetype should be supported: " + mimetype, supports);
        }
    }

    public void testOutlookMsgExtraction() throws Exception
    {
        // Check we can find the file
        File sourceFile = AbstractContentTransformerTest.loadQuickTestFile("msg");
        assertNotNull("quick.msg files should be available from Tests", sourceFile);
        
        // Now test
        testExtractFromMimetype(MimetypeMap.MIMETYPE_OUTLOOK_MSG);
    }
    
    /**
     * We have different things to normal, so
     *  do our own common tests.
     */
    protected void testCommonMetadata(String mimetype, Map<QName, Serializable> properties)
    {
        // Two equivalent ones
        assertEquals(
                "Property " + ContentModel.PROP_AUTHOR + " not found for mimetype " + mimetype,
                "Mark Rogers",
                DefaultTypeConverter.INSTANCE.convert(String.class, properties.get(ContentModel.PROP_AUTHOR)));
        assertEquals(
              "Property " + ContentModel.PROP_ORIGINATOR + " not found for mimetype " + mimetype,
              "Mark Rogers",
              DefaultTypeConverter.INSTANCE.convert(String.class, properties.get(ContentModel.PROP_ORIGINATOR)));
        // One other common bit
        assertEquals(
                "Property " + ContentModel.PROP_DESCRIPTION + " not found for mimetype " + mimetype,
                "This is a quick test",
                DefaultTypeConverter.INSTANCE.convert(String.class, properties.get(ContentModel.PROP_DESCRIPTION)));
    }

   /**
    * Test the outlook specific bits
    */
   protected void testFileSpecificMetadata(String mimetype,
         Map<QName, Serializable> properties) {
      // TODO Sent Date should be a date/time as per the contentModel.xml
      assertEquals(
            "Property " + ContentModel.PROP_SENTDATE + " not found for mimetype " + mimetype,
            "2013-01-18T13:44:20.000Z",
            DefaultTypeConverter.INSTANCE.convert(String.class, properties.get(ContentModel.PROP_SENTDATE)));
      
      // Addressee
      assertEquals(
            "Property " + ContentModel.PROP_ADDRESSEE + " not found for mimetype " + mimetype,
            "mark.rogers@alfresco.com",
            DefaultTypeConverter.INSTANCE.convert(String.class, properties.get(ContentModel.PROP_ADDRESSEE)));
      
      // Addressees
      assertTrue(
            "Property " + ContentModel.PROP_ADDRESSEES + " not found for mimetype " + mimetype,
            properties.get(ContentModel.PROP_ADDRESSEES) != null
      );
      
      Collection<String> addresses = DefaultTypeConverter.INSTANCE.getCollection(String.class, 
              properties.get(ContentModel.PROP_ADDRESSEES));
       
      assertTrue(
            "Property " + ContentModel.PROP_ADDRESSEES + " wrong content for mimetype " + mimetype + ", mark",
            addresses.contains("mark.rogers@alfresco.com"));
      
      assertTrue(
              "Property " + ContentModel.PROP_ADDRESSEES + " wrong content for mimetype " + mimetype + ", mrquick",
              addresses.contains("mrquick@nowhere.com"));

      // Feature: metadata extractor has normalised internet address ... from "Whizz <speedy@quick.com>"
      assertTrue(
              "Property " + ContentModel.PROP_ADDRESSEES + " wrong content for mimetype " + mimetype + ", Whizz",
              addresses.contains("speedy@quick.com"));
      
      // Subject Line  
      assertEquals(
            "Property " + ContentModel.PROP_SUBJECT + " not found for mimetype " + mimetype,
            "This is a quick test",
            DefaultTypeConverter.INSTANCE.convert(String.class, properties.get(ContentModel.PROP_SUBJECT)));
   }
}

