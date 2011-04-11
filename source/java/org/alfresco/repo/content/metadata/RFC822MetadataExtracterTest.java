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

import java.io.Serializable;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.namespace.QName;

/**
 * Test for the RFC822 (imap/mbox) extractor
 */
public class RFC822MetadataExtracterTest extends AbstractMetadataExtracterTest
{
    private RFC822MetadataExtracter extracter;
    
    private static final QName MESSAGE_FROM_TEST_PROPERTY = 
             QName.createQName("MessageToTest");
    private static final QName MESSAGE_TO_TEST_PROPERTY = 
             QName.createQName("MessageFromTest");
    private static final QName MESSAGE_CC_TEST_PROPERTY = 
       QName.createQName("MessageCCTest");

    @Override
    public void setUp() throws Exception
    {
        super.setUp();
        
        // Ask Spring for the extractor, so it
        //  gets its date formats populated
        extracter = (RFC822MetadataExtracter)ctx.getBean("extracter.RFC822");
        
        // Attach a couple of extra mappings
        // These will be tested later
        HashMap<String, Set<QName>> newMap = new HashMap<String, Set<QName>>(
              extracter.getMapping()
        );
        
        Set<QName> fromSet = new HashSet<QName>();
        fromSet.add(MESSAGE_FROM_TEST_PROPERTY);
        fromSet.addAll( extracter.getCurrentMapping().get(RFC822MetadataExtracter.KEY_MESSAGE_FROM) );
        newMap.put( RFC822MetadataExtracter.KEY_MESSAGE_FROM, fromSet );
        
        Set<QName> toSet = new HashSet<QName>();
        toSet.add(MESSAGE_TO_TEST_PROPERTY);
        toSet.addAll( extracter.getCurrentMapping().get(RFC822MetadataExtracter.KEY_MESSAGE_TO) );
        newMap.put( RFC822MetadataExtracter.KEY_MESSAGE_TO, toSet );
        
        Set<QName> ccSet = new HashSet<QName>();
        ccSet.add(MESSAGE_CC_TEST_PROPERTY);
        ccSet.addAll( extracter.getCurrentMapping().get(RFC822MetadataExtracter.KEY_MESSAGE_CC) );
        newMap.put( RFC822MetadataExtracter.KEY_MESSAGE_CC, ccSet );
        
        extracter.setMapping(newMap);
    }

    /**
     * @return Returns the same transformer regardless - it is allowed
     */
    protected MetadataExtracter getExtracter()
    {
        return extracter;
    }

    /**
     * RFC822 has a non-standard date format.
     * Check that this was sprung-in - if not, then
     *  other tests will fail!
     */
    public void testHasDateFormats() throws Exception {
       Set<DateFormat> supportedDateFormats;
       
       Field sdf = RFC822MetadataExtracter.class.getSuperclass().
                         getDeclaredField("supportedDateFormats");
       sdf.setAccessible(true);
       supportedDateFormats = (Set<DateFormat>)sdf.get(extracter);
       
       if(supportedDateFormats.size() == 0) {
          fail("No supportedDateFormats injected into RFC822MetadataExtracter - " +
          		"spring setup broken and date parsing will break all of the extraction process");
       }
    }
    
    public void testSupports() throws Exception
    {
        for (String mimetype : RFC822MetadataExtracter.SUPPORTED_MIMETYPES)
        {
            boolean supports = extracter.isSupported(mimetype);
            assertTrue("Mimetype should be supported: " + mimetype, supports);
        }
    }

    public void testEmailExtraction() throws Exception
    {
        testExtractFromMimetype(MimetypeMap.MIMETYPE_RFC822);
    }

    /**
     * We have no author, and have the same title and description
     */
    protected void testCommonMetadata(String mimetype,
         Map<QName, Serializable> properties) {
       assertEquals(
             "Property " + ContentModel.PROP_TITLE + " not found for mimetype " + mimetype,
             QUICK_TITLE,
             DefaultTypeConverter.INSTANCE.convert(String.class, properties.get(ContentModel.PROP_TITLE)));
       assertEquals(
             "Property " + ContentModel.PROP_DESCRIPTION + " not found for mimetype " + mimetype,
             QUICK_TITLE,
             DefaultTypeConverter.INSTANCE.convert(String.class, properties.get(ContentModel.PROP_DESCRIPTION)));
    }

   /** 
     * Test our extra IMAP properties 
     */
    public void testFileSpecificMetadata(String mimetype, Map<QName, Serializable> properties) {
       // Check the other cm: ones
       assertEquals(
             "Property " + ContentModel.PROP_ORIGINATOR + " not found for mimetype " + mimetype,
             QUICK_CREATOR + " <" + QUICK_CREATOR_EMAIL + ">",
             DefaultTypeConverter.INSTANCE.convert(String.class, properties.get(ContentModel.PROP_ORIGINATOR)));
 //      assertEquals(
 //            "Property " + ContentModel.PROP_SENTDATE + " not found for mimetype " + mimetype,
 //            "2004-06-04T13:23:22.000+01:00",
 //            DefaultTypeConverter.INSTANCE.convert(String.class, properties.get(ContentModel.PROP_SENTDATE)));
       
       // Check some imap: ones
       assertEquals(
             "Test Property " + MESSAGE_FROM_TEST_PROPERTY + " incorrect for mimetype " + mimetype,
             "Nevin Nollop <nevin.nollop@alfresco.com>",
             DefaultTypeConverter.INSTANCE.convert(String.class, properties.get(MESSAGE_FROM_TEST_PROPERTY)));
       assertEquals(
             "Test Property " + MESSAGE_FROM_TEST_PROPERTY + " incorrect for mimetype " + mimetype,
             "Nevin Nollop <nevin.nollop@alfresco.com>",
             DefaultTypeConverter.INSTANCE.convert(String.class, properties.get(MESSAGE_FROM_TEST_PROPERTY)));
       assertEquals(
             "Test Property " + MESSAGE_TO_TEST_PROPERTY + " incorrect for mimetype " + mimetype,
             "Nevin Nollop <nevin.nollop@alfresco.com>",
             DefaultTypeConverter.INSTANCE.convert(String.class, properties.get(MESSAGE_TO_TEST_PROPERTY)));
       
       // Finally check our non-standard ones we added in at test time
       assertTrue( 
             "Test Property " + MESSAGE_FROM_TEST_PROPERTY + " not found for mimetype " + mimetype,
             properties.containsKey(MESSAGE_FROM_TEST_PROPERTY)
       );
       assertTrue( 
             "Test Property " + MESSAGE_TO_TEST_PROPERTY + " not found for mimetype " + mimetype,
             properties.containsKey(MESSAGE_TO_TEST_PROPERTY)
       );
       assertTrue( 
             "Test Property " + MESSAGE_CC_TEST_PROPERTY + " not found for mimetype " + mimetype,
             properties.containsKey(MESSAGE_CC_TEST_PROPERTY)
       );
       
       assertEquals(
             "Test Property " + MESSAGE_FROM_TEST_PROPERTY + " incorrect for mimetype " + mimetype,
             "Nevin Nollop <nevin.nollop@alfresco.com>",
             DefaultTypeConverter.INSTANCE.convert(String.class, properties.get(MESSAGE_FROM_TEST_PROPERTY)));
       assertEquals(
             "Test Property " + MESSAGE_TO_TEST_PROPERTY + " incorrect for mimetype " + mimetype,
             "Nevin Nollop <nevin.nollop@alfresco.com>",
             DefaultTypeConverter.INSTANCE.convert(String.class, properties.get(MESSAGE_TO_TEST_PROPERTY)));
       assertEquals(
             "Test Property " + MESSAGE_CC_TEST_PROPERTY + " incorrect for mimetype " + mimetype,
             "Nevin Nollop <nevinn@alfresco.com>",
             DefaultTypeConverter.INSTANCE.convert(String.class, properties.get(MESSAGE_CC_TEST_PROPERTY)));
    }
}
