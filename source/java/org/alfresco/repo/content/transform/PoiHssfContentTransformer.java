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

import java.io.Writer;
import java.util.regex.Pattern;

import javax.xml.transform.TransformerConfigurationException;

import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.content.TikaOfficeDetectParser;
import org.alfresco.service.cmr.repository.TransformationOptions;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * Uses {@link http://tika.apache.org/ Apache Tika} and
 *  {@link http://poi.apache.org/ Apache POI} to perform
 *  conversions from Excel spreadsheets.
 * <p>Will transform from Excel spreadsheets into Html,
 *  Xml or Text (space or comma separated)
 * <p>Handles all sheets in the file.
 * 
 * @author Nick Burch
 * @author Derek Hulley
 */
public class PoiHssfContentTransformer extends TikaPoweredContentTransformer
{
    /**
     * Error message to delegate to NodeInfoBean
     */
    public static final String WRONG_FORMAT_MESSAGE_ID = "transform.err.format_or_password";
    private static Log logger = LogFactory.getLog(PoiHssfContentTransformer.class);
    
    public PoiHssfContentTransformer() 
    {
       super(new String[] {
             MimetypeMap.MIMETYPE_EXCEL,
             MimetypeMap.MIMETYPE_OPENXML_SPREADSHEET
       });
    }
    
    @Override
    protected Parser getParser() 
    {
       return new TikaOfficeDetectParser();
    }
    
    /**
     * Can we do the requested transformation via Tika?
     * We support transforming to HTML, XML, Text or CSV
     */
    @Override
    public boolean isTransformable(String sourceMimetype, String targetMimetype, TransformationOptions options)
    {
       if(sourceMimeTypes.contains(sourceMimetype) && 
             MimetypeMap.MIMETYPE_TEXT_CSV.equals(targetMimetype))
       {
          // Special case for CSV
          return true;
       }
       
       // Otherwise fall back on the default Tika rules
       return super.isTransformable(sourceMimetype, targetMimetype, options);
    }
    
    /**
     * Make sure we win over openoffice when it comes to producing
     *  HTML
     */
    @Override
    public boolean isExplicitTransformation(String sourceMimetype, String targetMimetype, TransformationOptions options)
    {
       if(sourceMimeTypes.contains(sourceMimetype) &&
             (MimetypeMap.MIMETYPE_HTML.equals(targetMimetype) ||
              MimetypeMap.MIMETYPE_XHTML.equals(targetMimetype)) )
       {
          // Special case to win for HTML 
          return true;
       }
       
       // Otherwise fall back on the default Tika rules
       return super.isTransformable(sourceMimetype, targetMimetype, options);
    }
    
    @Override
    protected ContentHandler getContentHandler(String targetMimeType, Writer output) 
                   throws TransformerConfigurationException
    {
       if(MimetypeMap.MIMETYPE_TEXT_CSV.equals(targetMimeType))
       {
          return new CsvContentHandler(output);
       }
       
       // Otherwise use the normal Tika rules
       return super.getContentHandler(targetMimeType, output);
    }
    
    /**
     * A wrapper around the normal Tika BodyContentHandler,
     *  which causes things to be CSV encoded rather than
     *  tab separated
     */
    protected static class CsvContentHandler extends BodyContentHandler {
       private static final char[] comma = new char[]{ ',' };
       private static final Pattern all_nums = Pattern.compile("[\\d\\.\\-\\+]+");
       
       private boolean inCell = false;
       private boolean needsComma = false;
       
       protected CsvContentHandler(Writer output) {
          super(output);
       }

       @Override
       public void ignorableWhitespace(char[] ch, int start, int length)
            throws SAXException {
          if(length == 1 && ch[0] == '\t') {
             // Ignore tabs, as they mess up the CSV output
          } else {
             super.ignorableWhitespace(ch, start, length);
          }
       }

       @Override
       public void characters(char[] ch, int start, int length)
            throws SAXException {
         if(inCell) {
            StringBuffer t = new StringBuffer(new String(ch,start,length));
            
            // Quote if not all numbers
            if(all_nums.matcher(t).matches()) 
            {
               super.characters(ch, start, length);
            }
            else
            {
               for(int i=t.length()-1; i>=0; i--) {
                  if(t.charAt(i) == '\"') {
                     // Double up double quotes
                     t.insert(i, '\"');
                     i--;
                  }
               }
               t.insert(0, '\"');
               t.append('\"');
               char[] c = t.toString().toCharArray();
               super.characters(c, 0, c.length);
            }
         } else {
            super.characters(ch, start, length);
         }
       }

       @Override
       public void startElement(String uri, String localName, String name,
            Attributes atts) throws SAXException {
          if(localName.equals("td")) {
             inCell = true;
             if(needsComma) {
                super.characters(comma, 0, 1);
                needsComma = true;
             }
          } else {
             super.startElement(uri, localName, name, atts);
          }
       }

       @Override
       public void endElement(String uri, String localName, String name)
            throws SAXException {
          if(localName.equals("td")) {
             needsComma = true;
             inCell = false;
          } else {
             if(localName.equals("tr")) {
                needsComma = false;
             }
             super.endElement(uri, localName, name);
          }
       }
    }
}
