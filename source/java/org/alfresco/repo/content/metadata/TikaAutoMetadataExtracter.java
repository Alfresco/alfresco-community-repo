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

import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.mime.MediaType;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.Parser;

/**
 * A Metadata Extractor which makes use of the Apache
 *  Tika auto-detection to select the best parser
 *  to extract the metadata from your document.
 * This will be used for all files which Tika can
 *  handle, but where no other more explicit
 *  extractor is defined. 

 * <pre>
 *   <b>author:</b>                 --      cm:author
 *   <b>title:</b>                  --      cm:title
 *   <b>subject:</b>                --      cm:description
 *   <b>created:</b>                --      cm:created
 *   <b>comments:</b>
 *   <p>geo:lat:</b>                --      cm:latitude
 *   <p>geo:long:</b>               --      cm:longitude
 * </pre>
 * 
 * @since 3.4
 * @author Nick Burch
 */
public class TikaAutoMetadataExtracter extends TikaPoweredMetadataExtracter
{
    protected static Log logger = LogFactory.getLog(TikaAutoMetadataExtracter.class);
    private static AutoDetectParser parser;
    private static TikaConfig config;

    public static ArrayList<String> SUPPORTED_MIMETYPES;
    private static ArrayList<String> buildMimeTypes(TikaConfig tikaConfig)
    {
       config = tikaConfig;
       parser = new AutoDetectParser(config);

       SUPPORTED_MIMETYPES = new ArrayList<String>();
       for(MediaType mt : parser.getParsers().keySet()) 
       {
          // Add the canonical mime type
          SUPPORTED_MIMETYPES.add( mt.toString() );
          
          // And add any aliases of the mime type too - Alfresco uses some
          //  non canonical forms of various mimetypes, so we need all of them
          for(MediaType alias : config.getMediaTypeRegistry().getAliases(mt)) 
          {
              SUPPORTED_MIMETYPES.add( alias.toString() );
          }
       }
       return SUPPORTED_MIMETYPES;
    }
    
    public TikaAutoMetadataExtracter(TikaConfig tikaConfig)
    {
       super( buildMimeTypes(tikaConfig) );
    }
    
    /**
     * Does auto-detection to select the best Tika
     *  Parser.
     */
    @Override
    protected Parser getParser() 
    {
       return parser;
    }
}
