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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import org.alfresco.repo.content.filestore.FileContentReader;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.util.TempFileProvider;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.ContentHandlerDecorator;
import org.apache.tika.sax.XHTMLContentHandler;
import org.apache.tika.sax.xpath.Matcher;
import org.apache.tika.sax.xpath.MatchingContentHandler;
import org.apache.tika.sax.xpath.XPathParser;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

/**
 * The parent of all Metadata Extractors which use
 *  Apache Tika under the hood.
 * This handles all the common parts of processing the
 *  files, and the common mappings.
 * Individual extractors extend from this to do custom
 *  mappings. 

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
public abstract class TikaPoweredMetadataExtracter extends AbstractMappingMetadataExtracter
{
    protected static Log logger = LogFactory.getLog(TikaPoweredMetadataExtracter.class);

    protected static final String KEY_AUTHOR = "author";
    protected static final String KEY_TITLE = "title";
    protected static final String KEY_SUBJECT = "subject";
    protected static final String KEY_CREATED = "created";
    protected static final String KEY_DESCRIPTION = "description";
    protected static final String KEY_COMMENTS = "comments";

    private DateFormat[] tikaDateFormats;
    
    /**
     * Builds up a list of supported mime types by merging an explicit
     *  list with any that Tika also claims to support
     */
    protected static ArrayList<String> buildSupportedMimetypes(String[] explicitTypes, Parser tikaParser) {
       ArrayList<String> types = new ArrayList<String>();
       for(String type : explicitTypes) {
          if(!types.contains(type)) {
             types.add(type);
          }
       }
       if(tikaParser != null) {
          for(MediaType mt : tikaParser.getSupportedTypes(new ParseContext())) {
             String type = mt.toString();
             if(!types.contains(type)) {
                types.add(type);
             }
          }
       }
       return types;
    }
    
    public TikaPoweredMetadataExtracter(ArrayList<String> supportedMimeTypes)
    {
       this(new HashSet<String>(supportedMimeTypes));
    }
    public TikaPoweredMetadataExtracter(HashSet<String> supportedMimeTypes)
    {
        super(supportedMimeTypes);
        
        // TODO Once TIKA-451 is fixed this list will get nicer
        this.tikaDateFormats = new DateFormat[] {
              new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'"),
              new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US),
              new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ"),
              new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.US),
              new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss"),
              new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US),
              new SimpleDateFormat("yyyy-MM-dd"),
              new SimpleDateFormat("yyyy-MM-dd", Locale.US),
              new SimpleDateFormat("yyyy/MM/dd HH:mm:ss"),
              new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.US),
              new SimpleDateFormat("yyyy/MM/dd"),
              new SimpleDateFormat("yyyy/MM/dd", Locale.US),
              new SimpleDateFormat("EEE MMM dd hh:mm:ss zzz yyyy"),
              new SimpleDateFormat("EEE MMM dd hh:mm:ss zzz yyyy", Locale.US)
        };
        // Set the timezone on the UTC based formats 
        for(DateFormat df : this.tikaDateFormats)
        {
           if(df instanceof SimpleDateFormat)
           {
              SimpleDateFormat sdf = (SimpleDateFormat)df;
              if(sdf.toPattern().endsWith("'Z'"))
              {
                 sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
              }
           }
        }
    }
    
    /**
     * Version which also tries the ISO-8601 formats (in order..),
     *  and similar formats, which Tika makes use of
     */
    @Override
    protected Date makeDate(String dateStr) {
       // Try our formats first, in order
       for(DateFormat df : this.tikaDateFormats) {
          try
          {
              return df.parse(dateStr);
          }
          catch (ParseException ee)
          {
              // Didn't work
          }
       }
       
       // Fall back to the normal ones
       return super.makeDate(dateStr);
    }
    
    /**
     * Returns the correct Tika Parser to process
     *  the document.
     * If you don't know which you want, use
     *  {@link TikaAutoMetadataExtracter} which
     *  makes use of the Tika auto-detection.
     */
    protected abstract Parser getParser();
    
    /**
     * Do we care about the contents of the
     *  extracted header, or nothing at all?
     */
    protected boolean needHeaderContents() {
       return false;
    }
    
    /**
     * Allows implementation specific mappings
     *  to be done.
     */
    protected Map<String, Serializable> extractSpecific(Metadata metadata, 
          Map<String, Serializable> properties, Map<String,String> headers) {
       return properties;
    }
    
    /**
     * There seems to be some sort of issue with some downstream
     *  3rd party libraries, and input streams that come from
     *  a {@link ContentReader}. This happens most often with
     *  JPEG and Tiff files.
     * For these cases, buffer out to a local file if not
     *  already there
     */
    private InputStream getInputStream(ContentReader reader) throws IOException {
       if("image/jpeg".equals(reader.getMimetype()) ||
             "image/tiff".equals(reader.getMimetype())) {
          if(reader instanceof FileContentReader) {
             return TikaInputStream.get( ((FileContentReader)reader).getFile() );
          } else {
             File tmpFile = TempFileProvider.createTempFile("tika", "tmp");
             reader.getContent(tmpFile);
             return TikaInputStream.get(tmpFile);
          }
       }
       return reader.getContentInputStream(); 
    }
    
    @Override
    protected Map<String, Serializable> extractRaw(ContentReader reader) throws Throwable
    {
        Map<String, Serializable> rawProperties = newRawMap();

        InputStream is = null;
        try
        {
            is = getInputStream(reader); 
            Parser parser = getParser();
            Metadata metadata = new Metadata();
            ParseContext context = new ParseContext();
            
            ContentHandler handler;
            Map<String,String> headers = null;
            if(needHeaderContents()) {
               MapCaptureContentHandler headerCapture = 
                  new MapCaptureContentHandler();
               headers = headerCapture.tags;
               handler = new HeadContentHandler(headerCapture);
            } else {
               handler = new NullContentHandler(); 
            }

            parser.parse(is, handler, metadata, context);
            
            // First up, copy all the Tika metadata over
            // This allows people to map any of the Tika
            //  keys onto their own content model
            for(String tikaKey : metadata.names()) {
               putRawValue(tikaKey, metadata.get(tikaKey), rawProperties);
            }
            
            // Now, map the common Tika metadata keys onto
            //  the common Alfresco metadata keys. This allows
            //  existing mapping properties files to continue
            //  to work without needing any changes
            
            // The simple ones
            putRawValue(KEY_AUTHOR, metadata.get(Metadata.AUTHOR), rawProperties);
            putRawValue(KEY_TITLE, metadata.get(Metadata.TITLE), rawProperties);
            putRawValue(KEY_COMMENTS, metadata.get(Metadata.COMMENTS), rawProperties);
            
            // Get the subject and description, despite things not
            //  being nearly as consistent as one might hope
            String subject = metadata.get(Metadata.SUBJECT);
            String description = metadata.get(Metadata.DESCRIPTION);
            if(subject != null && description != null) {
               putRawValue(KEY_DESCRIPTION, description, rawProperties);
               putRawValue(KEY_SUBJECT, subject, rawProperties);
            } else if(subject != null) {
               putRawValue(KEY_DESCRIPTION, subject, rawProperties);
               putRawValue(KEY_SUBJECT, subject, rawProperties);
            } else if(description != null) {
               putRawValue(KEY_DESCRIPTION, description, rawProperties);
               putRawValue(KEY_SUBJECT, description, rawProperties);
            }
            
            // Try for the dates two different ways too
            if(metadata.get(Metadata.CREATION_DATE) != null) {
               putRawValue(KEY_CREATED, metadata.get(Metadata.CREATION_DATE), rawProperties);
            } else if(metadata.get(Metadata.DATE) != null) {
               putRawValue(KEY_CREATED, metadata.get(Metadata.DATE), rawProperties);
            }
            
            // If people created a specific instance 
            //  (eg OfficeMetadataExtractor), then allow that
            //  instance to map the Tika keys onto its 
            //  existing namespace so that older properties
            //  files continue to map correctly
            rawProperties = extractSpecific(metadata, rawProperties, headers);
        }
        finally
        {
            if (is != null)
            {
                try { is.close(); } catch (IOException e) {}
            }
        }

        return rawProperties;
    }
    
    /**
     * This content handler will capture entries from within
     *  the header of the Tika content XHTML, but ignore the
     *  rest.
     */
    protected static class HeadContentHandler extends ContentHandlerDecorator {
       /**
        * XHTML XPath parser.
        */
       private static final XPathParser PARSER =
           new XPathParser("xhtml", XHTMLContentHandler.XHTML);

       /**
        * The XPath matcher used to select the XHTML body contents.
        */
       private static final Matcher MATCHER =
           PARSER.parse("/xhtml:html/xhtml:head/descendant:node()");

       /**
        * Creates a content handler that passes all XHTML body events to the
        * given underlying content handler.
        *
        * @param handler content handler
        */
       protected HeadContentHandler(ContentHandler handler) {
           super(new MatchingContentHandler(handler, MATCHER));
       }
    }
    /**
     * This content handler will grab all tags and attributes,
     *  and record the textual content of the last seen one
     *  of them.
     * Normally only used with {@link HeadContentHandler} 
     */
    protected static class MapCaptureContentHandler implements ContentHandler {
       protected Map<String,String> tags =
          new HashMap<String, String>();
       private StringBuffer text;

      public void characters(char[] ch, int start, int len) {
         if(text != null) {
            text.append(ch, start, len);
         }
      }
      public void endElement(String namespace, String localname,
            String qname) {
         if(text != null && text.length() > 0) {
            tags.put(qname, text.toString());
         }
         text = null;
      }
      public void startElement(String namespace, String localname,
            String qname, Attributes attrs) {
         for(int i=0; i<attrs.getLength(); i++) {
            tags.put(attrs.getQName(i), attrs.getValue(i));
         }
         text = new StringBuffer();
      }
      
      public void endDocument() throws SAXException {}
      public void endPrefixMapping(String paramString) throws SAXException {}
      public void ignorableWhitespace(char[] paramArrayOfChar, int paramInt1,
            int paramInt2) throws SAXException {}
      public void processingInstruction(String paramString1, String paramString2)
            throws SAXException {}
      public void setDocumentLocator(Locator paramLocator) {}
      public void skippedEntity(String paramString) throws SAXException {}
      public void startDocument() throws SAXException {}
      public void startPrefixMapping(String paramString1, String paramString2)
            throws SAXException {}
    }
    /**
     * A content handler that ignores all the content it finds.
     * Normally used when we only want the metadata, and don't
     *  care about the file contents.
     */
    protected static class NullContentHandler implements ContentHandler {
      public void characters(char[] paramArrayOfChar, int paramInt1,
            int paramInt2) throws SAXException {}
      public void endDocument() throws SAXException {}
      public void endElement(String paramString1, String paramString2,
            String paramString3) throws SAXException {}
      public void endPrefixMapping(String paramString) throws SAXException {}
      public void ignorableWhitespace(char[] paramArrayOfChar, int paramInt1,
            int paramInt2) throws SAXException {}
      public void processingInstruction(String paramString1, String paramString2)
            throws SAXException {}
      public void setDocumentLocator(Locator paramLocator) {}
      public void skippedEntity(String paramString) throws SAXException {}
      public void startDocument() throws SAXException {}
      public void startElement(String paramString1, String paramString2,
            String paramString3, Attributes paramAttributes)
            throws SAXException {}
      public void startPrefixMapping(String paramString1, String paramString2)
            throws SAXException {}
    }
}
