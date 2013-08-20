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
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.namespace.QName;


/**
 * @see OpenDocumentMetadataExtracter
 * 
 * @author Derek Hulley
 */
public class OpenDocumentMetadataExtracterTest extends AbstractMetadataExtracterTest
{
    private OpenDocumentMetadataExtracter extracter;

    @Override
    public void setUp() throws Exception
    {
        super.setUp();
        extracter = new OpenDocumentMetadataExtracter();
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
        for (String mimetype : OpenDocumentMetadataExtracter.SUPPORTED_MIMETYPES)
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
        for (String mimetype : OpenDocumentMetadataExtracter.SUPPORTED_MIMETYPES)
        {
            testExtractFromMimetype(mimetype);
        }
    }
    
    @Override
    protected boolean skipAuthorCheck(String mimetype) { return true; }

   /**
    * We also provide the creation date - check that
    */
   protected void testFileSpecificMetadata(String mimetype,
         Map<QName, Serializable> properties) 
   {
      // Check for two cases
      if(mimetype.equals("application/vnd.oasis.opendocument.text")) 
      {
         assertEquals(
               "Property " + ContentModel.PROP_CREATED + " not found for mimetype " + mimetype,
               "2005-09-06T23:34:00.000+01:00",
               DefaultTypeConverter.INSTANCE.convert(String.class, properties.get(ContentModel.PROP_CREATED)));
      } 
      else if(mimetype.equals("application/vnd.oasis.opendocument.graphics")) 
      {
         assertEquals(
               "Property " + ContentModel.PROP_CREATED + " not found for mimetype " + mimetype,
               "2006-01-27T11:46:11.000Z",
               DefaultTypeConverter.INSTANCE.convert(String.class, properties.get(ContentModel.PROP_CREATED)));
      }
   }
    
}
