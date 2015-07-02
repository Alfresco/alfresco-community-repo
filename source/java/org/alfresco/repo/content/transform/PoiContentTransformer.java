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
package org.alfresco.repo.content.transform;

import java.util.ArrayList;

import org.alfresco.repo.content.MimetypeMap;
import org.apache.tika.mime.MediaType;
import org.apache.tika.parser.Parser;
import org.apache.tika.parser.microsoft.OfficeParser;

/**
 * Uses <a href="http://tika.apache.org/">Apache Tika</a> and
 *  <a href="@link http://poi.apache.org/">Apache POI</a> to perform
 *  conversions from Office documents.
 *
 * {@link PoiHssfContentTransformer} handles the Excel
 *  transformations (mostly for compatibility), while
 *  this does all the other Office file formats.
 * 
 * @author Nick Burch
 */
public class PoiContentTransformer extends TikaPoweredContentTransformer
{
   /** 
    * We support all the office mimetypes that the Tika
    *  office parser can handle, except for excel
    *  (handled by {@link PoiHssfContentTransformer}
    */
   public static ArrayList<String> SUPPORTED_MIMETYPES;
   static {
      SUPPORTED_MIMETYPES = new ArrayList<String>();
      Parser p = new OfficeParser();
      for(MediaType mt : p.getSupportedTypes(null)) {
         if(mt.toString().equals(MimetypeMap.MIMETYPE_EXCEL))
         {
            // Skip, handled elsewhere
            continue;
         }
         // Tika can probably do some useful text
         SUPPORTED_MIMETYPES.add( mt.toString() );
      }
   }
    
    public PoiContentTransformer() {
       super(SUPPORTED_MIMETYPES);
    }

    @Override
    protected Parser getParser() {
       return new OfficeParser();
    }
}
