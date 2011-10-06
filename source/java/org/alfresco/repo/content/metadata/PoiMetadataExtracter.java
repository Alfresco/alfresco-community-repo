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

import org.alfresco.repo.content.MimetypeMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tika.parser.Parser;
import org.apache.tika.parser.microsoft.ooxml.OOXMLParser;

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
 * Uses Apache Tika
 * 
 * @author Nick Burch
 * @author Neil McErlean
 */
public class PoiMetadataExtracter extends TikaPoweredMetadataExtracter
{
    protected static Log logger = LogFactory.getLog(PoiMetadataExtracter.class);

    public static ArrayList<String> SUPPORTED_MIMETYPES = buildSupportedMimetypes( 
       new String[] {MimetypeMap.MIMETYPE_OPENXML_WORDPROCESSING,
    	               MimetypeMap.MIMETYPE_OPENXML_SPREADSHEET,
    	               MimetypeMap.MIMETYPE_OPENXML_PRESENTATION},
    	 new OOXMLParser() 
    );

    public PoiMetadataExtracter()
    {
        super(SUPPORTED_MIMETYPES);
    }
    
    @Override
    protected Parser getParser() 
    {
        return new OOXMLParser();
    }
}
