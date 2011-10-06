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
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.namespace.QName;

/**
 * @see org.alfresco.repo.content.metadata.PoiMetadataExtracter
 * 
 * @author Neil McErlean
 */
public class PoiMetadataExtracterTest extends AbstractMetadataExtracterTest
{
    private PoiMetadataExtracter extracter;

    @Override
    public void setUp() throws Exception
    {
        super.setUp();
        extracter = new PoiMetadataExtracter();
        extracter.setDictionaryService(dictionaryService);
        extracter.register();
    }

    @Override
    protected MetadataExtracter getExtracter()
    {
        return extracter;
    }

    public void testSupports() throws Exception
    {
        for (String mimetype : PoiMetadataExtracter.SUPPORTED_MIMETYPES)
        {
            boolean supports = extracter.isSupported(mimetype);
            assertTrue("Mimetype should be supported: " + mimetype, supports);
        }
    }

    public void testOffice2007Extraction() throws Exception
    {
        for (String mimetype : PoiMetadataExtracter.SUPPORTED_MIMETYPES)
        {
        	testExtractFromMimetype(mimetype);
        }
    }

    @Override
    protected boolean skipDescriptionCheck(String mimetype) 
    {
    	// Our 3 OpenOffice 07 quick files have no description properties.
    	return true;
    }


    @Override
    protected void testFileSpecificMetadata(String mimetype,
         Map<QName, Serializable> properties) 
    {
    	// This test class is testing 3 files: quick.docx, quick.xlsx & quick.pptx.
    	// Their created times are hard-coded here for checking.
    	// Of course this means that if the files are updated, the test will break
    	// but those files are rarely modified - only added to.
    	if (MimetypeMap.MIMETYPE_OPENXML_WORDPROCESSING.equals(mimetype))
    	{
    		checkFileCreationDate(mimetype, properties, "2010-01-06T17:32:00.000Z");
    	}
    	else if (MimetypeMap.MIMETYPE_OPENXML_SPREADSHEET.equals(mimetype))
    	{
    		checkFileCreationDate(mimetype, properties, "1996-10-15T00:33:28.000+01:00");
    	}
    	else if (MimetypeMap.MIMETYPE_OPENXML_PRESENTATION.equals(mimetype))
    	{
    		// Extraordinary! This document predates Isaac Newton's Principia Mathematica by almost a century. ;)
    		checkFileCreationDate(mimetype, properties, "1601-01-01T00:00:00.000Z");
    	}
    }

	private void checkFileCreationDate(String mimetype, Map<QName, Serializable> properties, String date)
	{
		assertEquals("Property " + ContentModel.PROP_CREATED + " not found for mimetype " + mimetype, date,
				DefaultTypeConverter.INSTANCE.convert(String.class, properties.get(ContentModel.PROP_CREATED)));
	}
}
