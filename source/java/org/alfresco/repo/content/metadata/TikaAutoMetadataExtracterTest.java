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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.content.transform.AbstractContentTransformerTest;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.namespace.QName;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.parser.dwg.DWGParser;
import org.apache.tika.parser.microsoft.OfficeParser;
import org.apache.tika.parser.microsoft.ooxml.OOXMLParser;
import org.apache.tika.parser.mp3.Mp3Parser;
import org.apache.tika.parser.odf.OpenDocumentParser;


/**
 * @see TikaAutoMetadataExtracter
 * 
 * @author Nick Burch
 */
public class TikaAutoMetadataExtracterTest extends AbstractMetadataExtracterTest
{
    private TikaAutoMetadataExtracter extracter;
    private static final QName TIKA_MIMETYPE_TEST_PROPERTY =
       QName.createQName("TikaMimeTypeTestProp");

    @Override
    public void setUp() throws Exception
    {
        super.setUp();
        extracter = new TikaAutoMetadataExtracter();
        extracter.setDictionaryService(dictionaryService);
        extracter.register();
        
        // Attach some extra mappings, using the Tika
        //  metadata keys namespace
        // These will be tested later
        HashMap<String, Set<QName>> newMap = new HashMap<String, Set<QName>>(
              extracter.getMapping()
        );
        
        Set<QName> tlaSet = new HashSet<QName>();
        tlaSet.add(TIKA_MIMETYPE_TEST_PROPERTY);
        newMap.put( Metadata.CONTENT_TYPE, tlaSet );
        
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
        ArrayList<String> mimeTypes = new ArrayList<String>();
        for (Parser p : new Parser[] {
                 new OfficeParser(), new OpenDocumentParser(),
                 new Mp3Parser(), new OOXMLParser()
        }) {
           Set<MediaType> mts = p.getSupportedTypes(new ParseContext());
           for (MediaType mt : mts) {
              mimeTypes.add(mt.toString());
           }
        }
        
        for (String mimetype : mimeTypes)
        {
            boolean supports = extracter.isSupported(mimetype);
            assertTrue("Mimetype should be supported: " + mimetype, supports);
        }
    }

    /**
     * Test several different files
     * Note - doesn't use extractFromMimetype
     */
    public void testSupportedMimetypes() throws Exception
    {
        String[] testFiles = new String[] {
              ".doc", ".docx", ".xls", ".xlsx",
              ".ppt", ".pptx", 
              //".vsd", // Not auto-detected properly yet
              //"2010.dwg", // Not auto-detected properly yet
              ".pdf",
              ".odt"
        };
           
        for (String fileBase : testFiles)
        {
           String filename = "quick" + fileBase;
           URL url = AbstractContentTransformerTest.class.getClassLoader().getResource("quick/" + filename);
           File file = new File(url.getFile());
           
           // Cheat and ask Tika for the mime type!
           AutoDetectParser ap = new AutoDetectParser();
           Metadata metadata = new Metadata();
           metadata.set(Metadata.RESOURCE_NAME_KEY, filename);
           MediaType mt = ap.getDetector().detect(
                 new BufferedInputStream(new FileInputStream(file)), metadata);
           String mimetype = mt.toString();

           // Have it processed
           Map<QName, Serializable> properties = extractFromFile(file, mimetype);
           
           // check we got something
           assertFalse("extractFromMimetype should return at least some properties, " +
           		"none found for " + mimetype + " - " + filename,
              properties.isEmpty());
           
           // check common metadata
           testCommonMetadata(mimetype, properties);
           // check file-type specific metadata
           testFileSpecificMetadata(mimetype, properties);
        }
    }
    
    @Override
    protected boolean skipAuthorCheck(String mimetype) { return true; }

   /**
    * We also provide the creation date - check that
    */
   protected void testFileSpecificMetadata(String mimetype,
         Map<QName, Serializable> properties) {
      
      // Check for extra fields
      // Author isn't there for the OpenDocument ones
      if(mimetype.indexOf(".oasis.") == -1) {
         assertEquals(
               "Property " + ContentModel.PROP_AUTHOR + " not found for mimetype " + mimetype,
               "Nevin Nollop",
               DefaultTypeConverter.INSTANCE.convert(String.class, properties.get(ContentModel.PROP_AUTHOR)));
      }
      
      // Ensure that we can also get things which are standard
      //  Tika metadata properties, if we so choose to
      assertTrue( 
            "Test Property " + TIKA_MIMETYPE_TEST_PROPERTY + " not found for mimetype " + mimetype,
            properties.containsKey(TIKA_MIMETYPE_TEST_PROPERTY)
      );
      // TODO - uncomment this when TIKA-391 is properly fixed
//      assertEquals(
//            "Test Property " + TIKA_MIMETYPE_TEST_PROPERTY + " incorrect for mimetype " + mimetype,
//            mimetype,
//            DefaultTypeConverter.INSTANCE.convert(String.class, properties.get(TIKA_MIMETYPE_TEST_PROPERTY)));
   }
    
}
