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
package org.alfresco.repo.content;

import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.util.HashSet;
import java.util.Set;

import org.apache.poi.poifs.common.POIFSConstants;
import org.apache.poi.util.IOUtils;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.parser.microsoft.OfficeParser;
import org.apache.tika.parser.microsoft.ooxml.OOXMLParser;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * <a href="http://tika.apache.org/Apache Tika">Apache Tika</a> assumes that
 *  you either know exactly what your content is, or that
 *  you'll leave it to auto-detection.
 * Within Alfresco, we usually do know. However, from time
 *  to time, we don't know if we have one of the old or one
 *  of the new office files (eg .xls and .xlsx).
 * This class allows automatically selects the appropriate
 *  old (OLE2) or new (OOXML) Tika parser as required.
 *    
 * @author Nick Burch
 */
public class TikaOfficeDetectParser implements Parser {
   private Parser ole2Parser = new OfficeParser();
   private Parser ooxmlParser = new OOXMLParser();

   public Set<MediaType> getSupportedTypes(ParseContext parseContext) {
      Set<MediaType> types = new HashSet<MediaType>();
      types.addAll(ole2Parser.getSupportedTypes(parseContext));
      types.addAll(ooxmlParser.getSupportedTypes(parseContext));
      return types;
   }

   public void parse(InputStream stream,
         ContentHandler handler, Metadata metadata,
         ParseContext parseContext) throws IOException, SAXException,
         TikaException 
   {
      byte[] initial4 = new byte[4];
      InputStream wrapped;
      // Preserve TikaInputStreams as TikaInputStreams as they require less memory to process
      if (stream.markSupported())
      {
         stream.mark(initial4.length);
         IOUtils.readFully(stream, initial4);
         stream.reset();
         wrapped = stream;
      }
      else
      {
         PushbackInputStream inp = new PushbackInputStream(stream, 4);
         IOUtils.readFully(inp, initial4);
         inp.unread(initial4);
         wrapped = inp;
      }
      
      // Which is it?
      if(initial4[0] == POIFSConstants.OOXML_FILE_HEADER[0] &&
         initial4[1] == POIFSConstants.OOXML_FILE_HEADER[1] &&
         initial4[2] == POIFSConstants.OOXML_FILE_HEADER[2] &&
         initial4[3] == POIFSConstants.OOXML_FILE_HEADER[3])
      {
         ooxmlParser.parse(wrapped, handler, metadata, parseContext);
      }
      else
      {
         ole2Parser.parse(wrapped, handler, metadata, parseContext);
      }
   }

   /**
    * @deprecated This method will be removed in Apache Tika 1.0.
    */
   public void parse(InputStream stream,
         ContentHandler handler, Metadata metadata)
         throws IOException, SAXException, TikaException 
   {
      parse(stream, handler, metadata, new ParseContext());
   }
}
