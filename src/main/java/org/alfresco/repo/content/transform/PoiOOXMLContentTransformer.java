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
package org.alfresco.repo.content.transform;

import java.util.ArrayList;

import org.apache.tika.mime.MediaType;
import org.apache.tika.parser.Parser;
import org.apache.tika.parser.microsoft.ooxml.OOXMLParser;

/**
 * Uses <a href="http://tika.apache.org/">Apache Tika</a> and
 *  <a href="@link http://poi.apache.org/">Apache POI</a> to perform
 *  conversions from the newer OOXML Office documents.
 *
 * @author Nick Burch
 *
 * @deprecated The transformations code is being moved out of the codebase and replaced by the new async RenditionService2 or other external libraries.
 */
@Deprecated
public class PoiOOXMLContentTransformer extends TikaPoweredContentTransformer
{
   /** 
    * We support all the office mimetypes that the Tika
    *  office parser can handle
    */
   public static ArrayList<String> SUPPORTED_MIMETYPES;
   static {
      SUPPORTED_MIMETYPES = new ArrayList<String>();
      Parser p = new OOXMLParser();
      for(MediaType mt : p.getSupportedTypes(null)) {
         SUPPORTED_MIMETYPES.add( mt.toString() );
      }
   }
    
    public PoiOOXMLContentTransformer() {
       super(SUPPORTED_MIMETYPES);
       setUseTimeoutThread(true);
       setTransformerName("OOXML");
    }

    @Override
    protected Parser getParser() {
       return new OOXMLParser();
    }
}
