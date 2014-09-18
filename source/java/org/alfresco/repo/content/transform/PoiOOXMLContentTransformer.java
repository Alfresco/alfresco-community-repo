/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
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
package org.alfresco.repo.content.transform;

import java.util.ArrayList;

import org.apache.tika.mime.MediaType;
import org.apache.tika.parser.Parser;
import org.apache.tika.parser.microsoft.ooxml.OOXMLParser;

/**
 * Uses {@link http://tika.apache.org/ Apache Tika} and
 *  {@link http://poi.apache.org/ Apache POI} to perform
 *  conversions from the newer OOXML Office documents.
 *
 * @author Nick Burch
 */
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
    }

    @Override
    protected Parser getParser() {
       return new OOXMLParser();
    }
}
