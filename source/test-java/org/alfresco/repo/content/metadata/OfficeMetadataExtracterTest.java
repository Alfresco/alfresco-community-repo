/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.namespace.QName;


/**
 * @see OfficeMetadataExtracter
 * 
 * @author Jesper Steen Møller
 */
public class OfficeMetadataExtracterTest extends AbstractMetadataExtracterTest
{
    private OfficeMetadataExtracter extracter;
    
    private static final QName WORD_COUNT_TEST_PROPERTY = 
             QName.createQName("WordCountTest");
    private static final QName LAST_AUTHOR_TEST_PROPERTY = 
             QName.createQName("LastAuthorTest");

    @Override
    public void setUp() throws Exception
    {
        super.setUp();
        extracter = new OfficeMetadataExtracter();
        extracter.setDictionaryService(dictionaryService);
        extracter.register();
        
        // Attach a couple of extra mappings
        // These will be tested later
        HashMap<String, Set<QName>> newMap = new HashMap<String, Set<QName>>(
              extracter.getMapping()
        );
        
        Set<QName> wcSet = new HashSet<QName>();
        wcSet.add(WORD_COUNT_TEST_PROPERTY);
        newMap.put( OfficeMetadataExtracter.KEY_WORD_COUNT, wcSet );
        
        Set<QName> laSet = new HashSet<QName>();
        laSet.add(LAST_AUTHOR_TEST_PROPERTY);
        newMap.put( OfficeMetadataExtracter.KEY_LAST_AUTHOR, laSet );
        
        extracter.setMapping(newMap);
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
        for (String mimetype : OfficeMetadataExtracter.SUPPORTED_MIMETYPES)
        {
            boolean supports = extracter.isSupported(mimetype);
            assertTrue("Mimetype should be supported: " + mimetype, supports);
        }
    }

    /**
     * Test all the supported mimetypes
     */
    public void testSupportedMimetypes() throws Exception
    {
        for (String mimetype : OfficeMetadataExtracter.SUPPORTED_MIMETYPES)
        {
            testExtractFromMimetype(mimetype);
        }
    }
    
    /** 
     * We support all sorts of extra metadata. Check it all behaves.
     */
    public void testFileSpecificMetadata(String mimetype, Map<QName, Serializable> properties) {
       // Test the ones with a core alfresco mapping
       if(mimetype.equals(MimetypeMap.MIMETYPE_WORD)) {
          assertEquals(
                "Property " + ContentModel.PROP_CREATED + " not found for mimetype " + mimetype,
                "2005-05-26T12:57:00.000Z",
                DefaultTypeConverter.INSTANCE.convert(String.class, properties.get(ContentModel.PROP_CREATED)));
          assertEquals(
                "Property " + ContentModel.PROP_MODIFIED + " not found for mimetype " + mimetype,
                "2005-09-20T17:25:00.000Z",
                DefaultTypeConverter.INSTANCE.convert(String.class, properties.get(ContentModel.PROP_MODIFIED)));
       } else if(mimetype.equals(MimetypeMap.MIMETYPE_EXCEL)) {
          assertEquals(
                "Property " + ContentModel.PROP_CREATED + " not found for mimetype " + mimetype,
                "1996-10-14T23:33:28.000Z",
                DefaultTypeConverter.INSTANCE.convert(String.class, properties.get(ContentModel.PROP_CREATED)));
          assertEquals(
                "Property " + ContentModel.PROP_MODIFIED + " not found for mimetype " + mimetype,
                "2005-09-20T18:22:32.000Z",
                DefaultTypeConverter.INSTANCE.convert(String.class, properties.get(ContentModel.PROP_MODIFIED)));
       } else if(mimetype.equals(MimetypeMap.MIMETYPE_PPT)) {
          assertEquals(
                "Property " + ContentModel.PROP_CREATED + " not found for mimetype " + mimetype,
                "1601-01-01T00:00:00.000Z", // Seriously, that's what the file says!
                DefaultTypeConverter.INSTANCE.convert(String.class, properties.get(ContentModel.PROP_CREATED)));
          assertEquals(
                "Property " + ContentModel.PROP_MODIFIED + " not found for mimetype " + mimetype,
                "2005-09-20T18:23:41.000Z",
                DefaultTypeConverter.INSTANCE.convert(String.class, properties.get(ContentModel.PROP_MODIFIED)));
       }
       
       // Now check the non-standard ones we added in at test time
       assertTrue( 
             "Test Property " + LAST_AUTHOR_TEST_PROPERTY + " not found for mimetype " + mimetype,
             properties.containsKey(LAST_AUTHOR_TEST_PROPERTY)
       );
       
       if(mimetype.equals(MimetypeMap.MIMETYPE_WORD)) {
          assertTrue( 
                "Test Property " + WORD_COUNT_TEST_PROPERTY + " not found for mimetype " + mimetype,
                properties.containsKey(WORD_COUNT_TEST_PROPERTY)
          );
          
          assertEquals(
                "Test Property " + WORD_COUNT_TEST_PROPERTY + " incorrect for mimetype " + mimetype,
                "9",
                DefaultTypeConverter.INSTANCE.convert(String.class, properties.get(WORD_COUNT_TEST_PROPERTY)));
          assertEquals(
                "Test Property " + LAST_AUTHOR_TEST_PROPERTY + " incorrect for mimetype " + mimetype,
                AbstractMetadataExtracterTest.QUICK_PREVIOUS_AUTHOR,
                DefaultTypeConverter.INSTANCE.convert(String.class, properties.get(LAST_AUTHOR_TEST_PROPERTY)));
       } else if(mimetype.equals(MimetypeMap.MIMETYPE_EXCEL)) {
          assertEquals(
                "Test Property " + LAST_AUTHOR_TEST_PROPERTY + " not found for mimetype " + mimetype,
                AbstractMetadataExtracterTest.QUICK_PREVIOUS_AUTHOR,
                DefaultTypeConverter.INSTANCE.convert(String.class, properties.get(LAST_AUTHOR_TEST_PROPERTY)));
       } else if(mimetype.equals(MimetypeMap.MIMETYPE_PPT)) {
          assertTrue( 
                "Test Property " + WORD_COUNT_TEST_PROPERTY + " not found for mimetype " + mimetype,
                properties.containsKey(WORD_COUNT_TEST_PROPERTY)
          );

          assertEquals(
                "Test Property " + WORD_COUNT_TEST_PROPERTY + " not found for mimetype " + mimetype,
                "9",
                DefaultTypeConverter.INSTANCE.convert(String.class, properties.get(WORD_COUNT_TEST_PROPERTY)));
          assertEquals(
                "Test Property " + LAST_AUTHOR_TEST_PROPERTY + " not found for mimetype " + mimetype,
                AbstractMetadataExtracterTest.QUICK_PREVIOUS_AUTHOR,
                DefaultTypeConverter.INSTANCE.convert(String.class, properties.get(LAST_AUTHOR_TEST_PROPERTY)));
       }
    }
}
