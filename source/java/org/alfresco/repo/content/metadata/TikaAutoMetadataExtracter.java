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
 * </pre>
 * 
 * @author Nick Burch
 */
public class TikaAutoMetadataExtracter extends TikaPoweredMetadataExtracter
{
    protected static Log logger = LogFactory.getLog(TikaAutoMetadataExtracter.class);

    public static ArrayList<String> SUPPORTED_MIMETYPES;
    static {
       SUPPORTED_MIMETYPES = new ArrayList<String>();
       AutoDetectParser p = new AutoDetectParser();
       for(MediaType mt : p.getParsers().keySet()) {
          SUPPORTED_MIMETYPES.add( mt.toString() );
       }
    }
    
    public TikaAutoMetadataExtracter()
    {
       super(SUPPORTED_MIMETYPES);
    }
    
    /**
     * Does auto-detection to select the best Tika
     *  Parser.
     */
    @Override
    protected Parser getParser() {
       return new AutoDetectParser();
    }
}
