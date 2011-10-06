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

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.Set;

import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.namespace.QName;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.Parser;
import org.apache.tika.parser.odf.OpenDocumentParser;


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
 * Uses Apache Tika
 * 
 * TODO decide if we need the few print info bits that
 *  Tika currently doesn't handle
 * 
 * @author Antti Jokipii
 * @author Derek Hulley
 */
public class OpenDocumentMetadataExtracter extends TikaPoweredMetadataExtracter
{
    private static final String KEY_CREATION_DATE = "creationDate";
    private static final String KEY_CREATOR = "creator";
    private static final String KEY_DATE = "date";
    private static final String KEY_GENERATOR = "generator";
    private static final String KEY_INITIAL_CREATOR = "initialCreator";
    private static final String KEY_KEYWORD = "keyword";
    private static final String KEY_LANGUAGE = "language";
    private static final String KEY_PRINT_DATE = "printDate";
    private static final String KEY_PRINTED_BY = "printedBy";
    
    private static final String CUSTOM_PREFIX = "custom:";

    public static ArrayList<String> SUPPORTED_MIMETYPES = buildSupportedMimetypes(
        new String[] {
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
            MimetypeMap.MIMETYPE_OPENDOCUMENT_DATABASE 
        }, new OpenDocumentParser()
    );

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss");

    public OpenDocumentMetadataExtracter()
    {
        super(SUPPORTED_MIMETYPES);
    }
    
    @Override
    protected Parser getParser() 
    {
       return new OpenDocumentParser();
    }

    @Override
    protected Map<String, Serializable> extractSpecific(Metadata metadata,
         Map<String, Serializable> properties, Map<String, String> headers) 
    {
       putRawValue(KEY_CREATION_DATE, getDateOrNull(metadata.get(Metadata.CREATION_DATE)), properties);
       putRawValue(KEY_CREATOR, metadata.get(Metadata.CREATOR), properties);
       putRawValue(KEY_DATE, getDateOrNull(metadata.get(Metadata.DATE)), properties);
       putRawValue(KEY_DESCRIPTION, metadata.get(Metadata.DESCRIPTION), properties);
       putRawValue(KEY_GENERATOR, metadata.get("generator"), properties);
       putRawValue(KEY_INITIAL_CREATOR, metadata.get("initial-creator"), properties);
       putRawValue(KEY_KEYWORD, metadata.get(Metadata.KEYWORDS), properties);
       putRawValue(KEY_LANGUAGE, metadata.get(Metadata.LANGUAGE), properties);
//     putRawValue(KEY_PRINT_DATE, getDateOrNull(metadata.get(Metadata.)), rawProperties);
//     putRawValue(KEY_PRINTED_BY, metadata.get(Metadata.), rawProperties);
           
       // Handle user-defined properties dynamically
       Map<String, Set<QName>> mapping = super.getMapping();
       for (String key : mapping.keySet())
       {
           if (metadata.get(CUSTOM_PREFIX + key) != null)
           {
                putRawValue(key, metadata.get(CUSTOM_PREFIX + key), properties);
           }
       }
       
       return properties;
    }
    private Date getDateOrNull(String dateString)
    {
        if (dateString != null && dateString.length() != 0)
        {
            try {
               return dateFormat.parse(dateString);
            } catch(ParseException e) {}
        }

        return null;
    }
}
