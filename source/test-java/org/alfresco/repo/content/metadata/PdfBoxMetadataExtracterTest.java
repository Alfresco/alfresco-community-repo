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
import java.util.Calendar;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.namespace.QName;
import org.apache.pdfbox.util.DateConverter;

/**
 * @see org.alfresco.repo.content.metadata.PdfBoxMetadataExtracter
 * 
 * @author Jesper Steen Møller
 */
public class PdfBoxMetadataExtracterTest extends AbstractMetadataExtracterTest
{
    private PdfBoxMetadataExtracter extracter;

    @Override
    public void setUp() throws Exception
    {
        super.setUp();
        extracter = new PdfBoxMetadataExtracter();
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
        for (String mimetype : PdfBoxMetadataExtracter.SUPPORTED_MIMETYPES)
        {
            boolean supports = extracter.isSupported(mimetype);
            assertTrue("Mimetype should be supported: " + mimetype, supports);
        }
    }

    public void testPdfExtraction() throws Exception
    {
        testExtractFromMimetype(MimetypeMap.MIMETYPE_PDF);
    }
    
    /**
     * This test method extracts metadata from an Adobe Illustrator file (which in recent versions is a pdf file).
     * @since 3.5.0
     */
    public void testAiExtraction() throws Exception
    {
        testExtractFromMimetype(MimetypeMap.MIMETYPE_APPLICATION_ILLUSTRATOR);
    }
    
    /**
     * We can also return a created date
     */
    protected void testFileSpecificMetadata(String mimetype,
         Map<QName, Serializable> properties) {
       assertEquals(
             "Property " + ContentModel.PROP_CREATED + " not found for mimetype " + mimetype,
             "2005-05-26T20:52:58.000+01:00",
             DefaultTypeConverter.INSTANCE.convert(String.class, properties.get(ContentModel.PROP_CREATED)));
    }
    
    /**
     * Test that will show when the workaround is in place.
     */
    public void testDateConversion() throws Exception {
       Calendar c = DateConverter.toCalendar("D:20050526205258+01'00'");
       assertEquals(2005, c.get(Calendar.YEAR));
       assertEquals(05-1, c.get(Calendar.MONTH));
       assertEquals(26, c.get(Calendar.DAY_OF_MONTH));
       assertEquals(20, c.get(Calendar.HOUR_OF_DAY));
       assertEquals(52, c.get(Calendar.MINUTE));
       assertEquals(58, c.get(Calendar.SECOND));
       //assertEquals(0, c.get(Calendar.MILLISECOND));
    }
}
