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
import java.util.ArrayList;
import java.util.Map;

import org.alfresco.repo.content.MimetypeMap;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.Parser;
import org.apache.tika.parser.microsoft.OfficeParser;

/**
 * Office file format Metadata Extracter.  This extracter uses the POI library to extract
 * the following:
 * <pre>
 *   <b>author:</b>             --      cm:author
 *   <b>title:</b>              --      cm:title
 *   <b>subject:</b>            --      cm:description
 *   <b>createDateTime:</b>     --      cm:created
 *   <b>lastSaveDateTime:</b>   --      cm:modified
 *   <b>comments:</b>
 *   <b>editTime:</b>
 *   <b>format:</b>
 *   <b>keywords:</b>
 *   <b>lastAuthor:</b>
 *   <b>lastPrinted:</b>
 *   <b>osVersion:</b>
 *   <b>thumbnail:</b>
 *   <b>pageCount:</b>
 *   <b>wordCount:</b>
 * </pre>
 * 
 * Uses Apache Tika
 *
 * @author Derek Hulley
 * @author Nick Burch
 */
public class OfficeMetadataExtracter extends TikaPoweredMetadataExtracter
{
    public static final String KEY_CREATE_DATETIME = "createDateTime";
    public static final String KEY_LAST_SAVE_DATETIME = "lastSaveDateTime";
    public static final String KEY_EDIT_TIME = "editTime";
    public static final String KEY_FORMAT = "format";
    public static final String KEY_KEYWORDS = "keywords";
    public static final String KEY_LAST_AUTHOR = "lastAuthor";
    public static final String KEY_LAST_PRINTED = "lastPrinted";
    public static final String KEY_OS_VERSION = "osVersion"; // TODO
    public static final String KEY_THUMBNAIL = "thumbnail"; // TODO
    public static final String KEY_PAGE_COUNT = "pageCount";
    public static final String KEY_PARAGRAPH_COUNT = "paragraphCount";
    public static final String KEY_WORD_COUNT = "wordCount";
    
    public static ArrayList<String> SUPPORTED_MIMETYPES = buildSupportedMimetypes(
          new String[] {
              MimetypeMap.MIMETYPE_WORD,
              MimetypeMap.MIMETYPE_EXCEL,
              MimetypeMap.MIMETYPE_PPT},
          new OfficeParser()
    );
    static {
       // Outlook has it's own one!
       SUPPORTED_MIMETYPES.remove(MimetypeMap.MIMETYPE_OUTLOOK_MSG);
    }

    public OfficeMetadataExtracter()
    {
        super(SUPPORTED_MIMETYPES);
    }
    
    @Override
    protected Parser getParser() 
    {
      return new OfficeParser();
    }

    @Override
    protected Map<String, Serializable> extractSpecific(Metadata metadata,
         Map<String, Serializable> properties, Map<String,String> headers) 
    {
       putRawValue(KEY_CREATE_DATETIME, metadata.get(Metadata.CREATION_DATE), properties); 
       putRawValue(KEY_LAST_SAVE_DATETIME, metadata.get(Metadata.LAST_SAVED), properties);
       putRawValue(KEY_EDIT_TIME, metadata.get(Metadata.EDIT_TIME), properties);
       putRawValue(KEY_FORMAT, metadata.get(Metadata.FORMAT), properties);
       putRawValue(KEY_KEYWORDS, metadata.get(Metadata.KEYWORDS), properties);
       putRawValue(KEY_LAST_AUTHOR, metadata.get(Metadata.LAST_AUTHOR), properties);
       putRawValue(KEY_LAST_PRINTED, metadata.get(Metadata.LAST_PRINTED), properties);
//       putRawValue(KEY_OS_VERSION, metadata.get(Metadata.OS_VERSION), properties);
//       putRawValue(KEY_THUMBNAIL, metadata.get(Metadata.THUMBNAIL), properties);
       putRawValue(KEY_PAGE_COUNT, metadata.get(Metadata.PAGE_COUNT), properties);
       putRawValue(KEY_PARAGRAPH_COUNT, metadata.get(Metadata.PARAGRAPH_COUNT), properties);
       putRawValue(KEY_WORD_COUNT, metadata.get(Metadata.WORD_COUNT), properties);
       return properties;
    }
}
