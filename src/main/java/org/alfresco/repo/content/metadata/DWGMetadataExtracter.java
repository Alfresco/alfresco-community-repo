/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.repo.content.metadata;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;

import org.alfresco.repo.content.MimetypeMap;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.Parser;
import org.apache.tika.parser.dwg.DWGParser;


/**
 * Metadata extractor for the
 * {@link org.alfresco.repo.content.MimetypeMap#MIMETYPE_APP_DWG MIMETYPE_APP_DWG}
 * and
 * {@link org.alfresco.repo.content.MimetypeMap#MIMETYPE_IMG_DWG MIMETYPE_IMG_DWG}
 * mimetypes.
 * <pre>
 *   <b>title:</b>           --      cm:title
 *   <b>description:</b>     --      cm:description
 *   <b>author:</b>          --      cm:author
 *   <b>keywords:</b>
 *   <b>comments:</b>
 *   <b>lastauthor:</b>
 * </pre>
 * 
 * Uses Apache Tika
 * 
 * @since 3.4
 * @author Nick Burch
 */
public class DWGMetadataExtracter extends TikaPoweredMetadataExtracter
{
    private static final String KEY_KEYWORD = "keyword";
    private static final String KEY_LAST_AUTHOR = "lastAuthor";
 
    public static ArrayList<String> SUPPORTED_MIMETYPES = buildSupportedMimetypes( 
          new String[] {
              MimetypeMap.MIMETYPE_APP_DWG,
              MimetypeMap.MIMETYPE_IMG_DWG,
              "image/x-dwg", // Was used before IANA registration
          }, 
          new DWGParser() 
    );
    
    public DWGMetadataExtracter()
    {
        super(SUPPORTED_MIMETYPES);
    }
    
    @SuppressWarnings("deprecation")
    @Override
    protected Map<String, Serializable> extractSpecific(Metadata metadata,
         Map<String, Serializable> properties, Map<String,String> headers) 
    {
       putRawValue(KEY_KEYWORD, metadata.get(Metadata.KEYWORDS), properties);
       putRawValue(KEY_LAST_AUTHOR, metadata.get(Metadata.LAST_AUTHOR), properties);
       return properties;
    }

    @Override
    protected Parser getParser() 
    {
      return new DWGParser();
    }
}
