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
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.dwg.DWGParser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.ContentHandler;


/**
 * Metadata extractor for the
 * {@link org.alfresco.repo.content.MimetypeMap#MIMETYPE_DWG MIMETYPE_DWG}
 * mimetype.
 * <pre>
 *   <b>title:</b>           --      cm:title
 *   <b>description:</b>     --      cm:description
 *   <b>author:</b>          --      cm:author
 *   <b>keywords:</b>
 *   <b>comments:</b>
 *   <b>lastauthor:</b>
 * </pre>
 * 
 * TIKA Note - this has been converted to deep-call into Tika.
 *  This will be replaced with proper calls to Tika at a later date.
 * 
 * @author Nick Burch
 */
public class DWGMetadataExtracter extends AbstractMappingMetadataExtracter
{
    private static final String KEY_AUTHOR = "author";
    private static final String KEY_COMMENT = "comment";
    private static final String KEY_DESCRIPTION = "description";
    private static final String KEY_KEYWORD = "keyword";
    private static final String KEY_LAST_AUTHOR = "lastAuthor";
    private static final String KEY_TITLE = "title";

    public static String[] SUPPORTED_MIMETYPES = new String[] {
            MimetypeMap.MIMETYPE_APP_DWG,
            MimetypeMap.MIMETYPE_IMG_DWG,
            "image/x-dwg", // Was used before IANA registration
    };
    
    public DWGMetadataExtracter()
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

            DWGParser dwgParser = new DWGParser();
            ContentHandler handler = new BodyContentHandler() ;
            Metadata metadata = new Metadata();
            ParseContext context = new ParseContext();
        	
            dwgParser.parse(is, handler, metadata, context);

            putRawValue(KEY_AUTHOR, metadata.get(Metadata.AUTHOR), rawProperties);
            putRawValue(KEY_COMMENT, metadata.get(Metadata.COMMENTS), rawProperties);
            putRawValue(KEY_DESCRIPTION, metadata.get(Metadata.DESCRIPTION), rawProperties);
            putRawValue(KEY_KEYWORD, metadata.get(Metadata.KEYWORDS), rawProperties);
            putRawValue(KEY_LAST_AUTHOR, metadata.get(Metadata.LAST_AUTHOR), rawProperties);
            putRawValue(KEY_DESCRIPTION, metadata.get(Metadata.SUBJECT), rawProperties);
            putRawValue(KEY_TITLE, metadata.get(Metadata.TITLE), rawProperties);
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
}
