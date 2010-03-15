/*
 * Copyright (C) 2005 Antti Jokipii
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.namespace.QName;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.odf.OpenDocumentParser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.ContentHandler;


/**
 * Metadata extractor for the
 * {@link org.alfresco.repo.content.MimetypeMap#MIMETYPE_OPENDOCUMENT_TEXT MIMETYPE_OPENDOCUMENT_XXX}
 * mimetypes.
 * <pre>
 *   <b>creationDate:</b>           --      cm:created
 *   <b>creator:</b>                --      cm:author
 *   <b>date:</b>
 *   <b>description:</b>            --      cm:description
 *   <b>generator:</b>
 *   <b>initialCreator:</b>
 *   <b>keyword:</b>
 *   <b>language:</b>
 *   <b>printDate:</b>
 *   <b>printedBy:</b>
 *   <b>subject:</b>
 *   <b>title:</b>                  --      cm:title
 *   <b>All user properties</b>
 * </pre>
 * 
 * TIKA Note - this has been converted to deep-call into Tika.
 *  This will be replaced with proper calls to Tika at a later date.
 *  Everything except some Print info has been ported to Tika.
 * 
 * @author Antti Jokipii
 * @author Derek Hulley
 */
public class OpenDocumentMetadataExtracter extends AbstractMappingMetadataExtracter
{
    private static final String KEY_CREATION_DATE = "creationDate";
    private static final String KEY_CREATOR = "creator";
    private static final String KEY_DATE = "date";
    private static final String KEY_DESCRIPTION = "description";
    private static final String KEY_GENERATOR = "generator";
    private static final String KEY_INITIAL_CREATOR = "initialCreator";
    private static final String KEY_KEYWORD = "keyword";
    private static final String KEY_LANGUAGE = "language";
    private static final String KEY_PRINT_DATE = "printDate";
    private static final String KEY_PRINTED_BY = "printedBy";
    private static final String KEY_SUBJECT = "subject";
    private static final String KEY_TITLE = "title";
    
    private static final String CUSTOM_PREFIX = "custom:";

    public static String[] SUPPORTED_MIMETYPES = new String[] {
            MimetypeMap.MIMETYPE_OPENDOCUMENT_TEXT,
            MimetypeMap.MIMETYPE_OPENDOCUMENT_TEXT_TEMPLATE,
            MimetypeMap.MIMETYPE_OPENDOCUMENT_GRAPHICS,
            MimetypeMap.MIMETYPE_OPENDOCUMENT_GRAPHICS_TEMPLATE,
            MimetypeMap.MIMETYPE_OPENDOCUMENT_PRESENTATION,
            MimetypeMap.MIMETYPE_OPENDOCUMENT_PRESENTATION_TEMPLATE,
            MimetypeMap.MIMETYPE_OPENDOCUMENT_SPREADSHEET,
            MimetypeMap.MIMETYPE_OPENDOCUMENT_SPREADSHEET_TEMPLATE,
            MimetypeMap.MIMETYPE_OPENDOCUMENT_CHART,
            MimetypeMap.MIMETYPE_OPENDOCUMENT_CHART_TEMPLATE,
            MimetypeMap.MIMETYPE_OPENDOCUMENT_IMAGE,
            MimetypeMap.MIMETYPE_OPENDOCUMENT_IMAGE_TEMPLATE,
            MimetypeMap.MIMETYPE_OPENDOCUMENT_FORMULA,
            MimetypeMap.MIMETYPE_OPENDOCUMENT_FORMULA_TEMPLATE,
            MimetypeMap.MIMETYPE_OPENDOCUMENT_TEXT_MASTER,
            MimetypeMap.MIMETYPE_OPENDOCUMENT_TEXT_WEB,
            MimetypeMap.MIMETYPE_OPENDOCUMENT_DATABASE };

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss");

    public OpenDocumentMetadataExtracter()
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

            OpenDocumentParser docParser = new OpenDocumentParser();
            ContentHandler handler = new BodyContentHandler() ;
            Metadata metadata = new Metadata();
            ParseContext context = new ParseContext();
        	
            docParser.parse(is, handler, metadata, context);

            putRawValue(KEY_CREATION_DATE, getDateOrNull(metadata.get(Metadata.CREATION_DATE)), rawProperties);
            putRawValue(KEY_CREATOR, metadata.get(Metadata.CREATOR), rawProperties);
            putRawValue(KEY_DATE, getDateOrNull(metadata.get(Metadata.DATE)), rawProperties);
            putRawValue(KEY_DESCRIPTION, metadata.get(Metadata.DESCRIPTION), rawProperties);
            putRawValue(KEY_GENERATOR, metadata.get("generator"), rawProperties);
            putRawValue(KEY_INITIAL_CREATOR, metadata.get("initial-creator"), rawProperties);
            putRawValue(KEY_KEYWORD, metadata.get(Metadata.KEYWORDS), rawProperties);
            putRawValue(KEY_LANGUAGE, metadata.get(Metadata.LANGUAGE), rawProperties);
//          putRawValue(KEY_PRINT_DATE, getDateOrNull(metadata.get(Metadata.)), rawProperties);
//          putRawValue(KEY_PRINTED_BY, metadata.get(Metadata.), rawProperties);
            putRawValue(KEY_SUBJECT, metadata.get(Metadata.SUBJECT), rawProperties);
            putRawValue(KEY_TITLE, metadata.get(Metadata.TITLE), rawProperties);
                
            // Handle user-defined properties dynamically
            Map<String, Set<QName>> mapping = super.getMapping();
            for (String key : mapping.keySet())
            {
                if (metadata.get(CUSTOM_PREFIX + key) != null)
                {
                     putRawValue(key, metadata.get(CUSTOM_PREFIX + key), rawProperties);
                }
            }
        }
        finally
        {
            if (is != null)
            {
                try { is.close(); } catch (IOException e) {}
            }
        }
        // Done
        return rawProperties;
    }
	
    private Date getDateOrNull(String dateString) throws ParseException
    {
        if (dateString != null && dateString.length() != 0)
        {
            return dateFormat.parse(dateString);
        }

        return null;
    }
}
