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

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;

import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.repository.ContentReader;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.POIXMLDocument;
import org.apache.poi.POIXMLPropertiesTextExtractor;
import org.apache.poi.POIXMLProperties.CoreProperties;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xslf.XSLFSlideShow;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.xmlbeans.XmlException;

/**
 * POI-based metadata extractor for Office 07 documents.
 * See http://poi.apache.org/ for information on POI.
 * <pre>
 *   <b>author:</b>                 --      cm:author
 *   <b>title:</b>                  --      cm:title
 *   <b>subject:</b>                --      cm:description
 *   <b>created:</b>                --      cm:created
 *   <b>Any custom property:</b>    --      [not mapped]
 * </pre>
 * 
 * TIKA Note - all the fields (plus a few others) are present
 *  in the tika metadata.
 * 
 * @author Neil McErlean
 */
public class PoiMetadataExtracter extends AbstractMappingMetadataExtracter
{
    protected static Log logger = LogFactory.getLog(PoiMetadataExtracter.class);

    private static final String KEY_AUTHOR = "author";
    private static final String KEY_TITLE = "title";
    private static final String KEY_SUBJECT = "subject";
    private static final String KEY_CREATED = "created";
    private static final String KEY_DESCRIPTION = "description";
    
    public static String[] SUPPORTED_MIMETYPES = new String[] {MimetypeMap.MIMETYPE_OPENXML_WORDPROCESSING,
    	                                                       MimetypeMap.MIMETYPE_OPENXML_SPREADSHEET,
    	                                                       MimetypeMap.MIMETYPE_OPENXML_PRESENTATION};

    public PoiMetadataExtracter()
    {
        super(new HashSet<String>(Arrays.asList(SUPPORTED_MIMETYPES)));
    }
    
    @Override
    public Map<String, Serializable> extractRaw(ContentReader reader) throws Throwable
    {
        Map<String, Serializable> rawProperties = newRawMap();

        InputStream is = null;
        try
        {
            is = reader.getContentInputStream();
            POIXMLDocument document = readDocumentFromStream(is, reader.getMimetype());
            
            POIXMLPropertiesTextExtractor extracter = new POIXMLPropertiesTextExtractor(document);
            CoreProperties coreProps = extracter.getCoreProperties();

            putRawValue(KEY_AUTHOR, coreProps.getCreator(), rawProperties);
            putRawValue(KEY_TITLE, coreProps.getTitle(), rawProperties);
            putRawValue(KEY_SUBJECT, coreProps.getSubject(), rawProperties);
            putRawValue(KEY_DESCRIPTION, coreProps.getDescription(), rawProperties);
            putRawValue(KEY_CREATED, coreProps.getCreated(), rawProperties);
        }
        finally
        {
            if (is != null)
            {
                try { is.close(); } catch (IOException e) {}
            }
        }

        return rawProperties;
    }

	private POIXMLDocument readDocumentFromStream(InputStream is, String mimetype)
			throws IOException, OpenXML4JException, XmlException {
		POIXMLDocument document = null;
		if (MimetypeMap.MIMETYPE_OPENXML_WORDPROCESSING.equals(mimetype))
		{
			document = new XWPFDocument(OPCPackage.open(is));
		}
		else if (MimetypeMap.MIMETYPE_OPENXML_SPREADSHEET.equals(mimetype))
		{
			document = new XSSFWorkbook(OPCPackage.open(is));
		}
		else if (MimetypeMap.MIMETYPE_OPENXML_PRESENTATION.equals(mimetype))
		{
			document = new XSLFSlideShow(OPCPackage.open(is));
		}
		
		return document;
	}
}
