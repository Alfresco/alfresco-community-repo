/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2020 Alfresco Software Limited
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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.alfresco.api.AlfrescoPublicApi;   
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.content.filestore.FileContentReader;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tika.embedder.Embedder;
import org.apache.tika.extractor.DocumentSelector;
import org.apache.tika.io.TemporaryResources;
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
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;
import org.joda.time.format.DateTimeParser;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;


/**
 * @deprecated extractors have been moved to a T-Engine.
 *
 * The parent of all Metadata Extractors which use
 * Apache Tika under the hood. This handles all the
 * common parts of processing the files, and the common
 * mappings. Individual extractors extend from this
 * to do custom mappings. 

 * <pre>
 *   <b>author:</b>                 --      cm:author
 *   <b>title:</b>                  --      cm:title
 *   <b>subject:</b>                --      cm:description
 *   <b>created:</b>                --      cm:created
 *   <b>comments:</b>
 * </pre>
 * 
 * @since 3.4
 * @author Nick Burch
 */
@AlfrescoPublicApi
@Deprecated
public abstract class TikaPoweredMetadataExtracter
        extends AbstractMappingMetadataExtracter
        implements MetadataEmbedder
{
    protected static Log logger = LogFactory.getLog(TikaPoweredMetadataExtracter.class);

    protected static final String KEY_AUTHOR = "author";
    protected static final String KEY_TITLE = "title";
    protected static final String KEY_SUBJECT = "subject";
    protected static final String KEY_CREATED = "created";
    protected static final String KEY_DESCRIPTION = "description";
    protected static final String KEY_COMMENTS = "comments";
    protected static final String KEY_TAGS = "dc:subject";

    private DateTimeFormatter tikaUTCDateFormater;
    private DateTimeFormatter tikaDateFormater;
    protected DocumentSelector documentSelector;

    private String extractorContext = null;

    private String metadataSeparator = ","; // Default separator.

    public String getMetadataSeparator()
    {
        return metadataSeparator;
    }

    public void setMetadataSeparator(String metadataSeparator)
    {
        this.metadataSeparator = metadataSeparator;
    }

    /**
     * Builds up a list of supported mime types by merging
     * an explicit list with any that Tika also claims to support
     */
    protected static ArrayList<String> buildSupportedMimetypes(String[] explicitTypes, Parser... tikaParsers) 
    {
       ArrayList<String> types = new ArrayList<String>();
       for(String type : explicitTypes) 
       {
          if(!types.contains(type)) 
          {
             types.add(type);
          }
       }
       if(tikaParsers != null) 
       {
          for(Parser tikaParser : tikaParsers)
          {
             for(MediaType mt : tikaParser.getSupportedTypes(new ParseContext())) 
             {
                String type = mt.toString();
                if(!types.contains(type)) 
                {
                   types.add(type);
                }
             }
          }
       }
       return types;
    }

    public TikaPoweredMetadataExtracter(String extractorContext, ArrayList<String> supportedMimeTypes)
    {
       this(extractorContext, new HashSet<String>(supportedMimeTypes), null);
    }

    public TikaPoweredMetadataExtracter(ArrayList<String> supportedMimeTypes)
    {
       this(null, new HashSet<String>(supportedMimeTypes), null);
    }

    public TikaPoweredMetadataExtracter(ArrayList<String> supportedMimeTypes, ArrayList<String> supportedEmbedMimeTypes)
    {
       this(null, new HashSet<String>(supportedMimeTypes), new HashSet<String>(supportedEmbedMimeTypes));
    }

    public TikaPoweredMetadataExtracter(HashSet<String> supportedMimeTypes)
    {
       this(null, supportedMimeTypes, null);
    }

    public TikaPoweredMetadataExtracter(HashSet<String> supportedMimeTypes, HashSet<String> supportedEmbedMimeTypes)
    {
        this(null, supportedMimeTypes, supportedEmbedMimeTypes);
    }

    public TikaPoweredMetadataExtracter(String extractorContext, HashSet<String> supportedMimeTypes, HashSet<String> supportedEmbedMimeTypes)
    {
        super(supportedMimeTypes, supportedEmbedMimeTypes);

        this.extractorContext = extractorContext;

        // TODO Once TIKA-451 is fixed this list will get nicer
        DateTimeParser[] parsersUTC = {
            DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss'Z'").getParser(),
            DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ssZ").getParser()
        };
        DateTimeParser[] parsers = {
            DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss").getParser(),
            DateTimeFormat.forPattern("yyyy-MM-dd").getParser(),
            DateTimeFormat.forPattern("yyyy/MM/dd HH:mm:ss").getParser(),
            DateTimeFormat.forPattern("yyyy/MM/dd").getParser(),
                DateTimeFormat.forPattern("EEE MMM dd hh:mm:ss zzz yyyy").getParser()
        };

        this.tikaUTCDateFormater = new DateTimeFormatterBuilder().append(null, parsersUTC).toFormatter().withZone(DateTimeZone.UTC);
        this.tikaDateFormater = new DateTimeFormatterBuilder().append(null, parsers).toFormatter();
    }

    /**
     * Gets context for the current implementation
     * 
     * @return {@link String} value which determines current context
     */
    protected String getExtractorContext()
    {
        return extractorContext;
    }

    /**
     * Version which also tries the ISO-8601 formats (in order..),
     *  and similar formats, which Tika makes use of
     */
    @Override
    protected Date makeDate(String dateStr) 
    {
        // Try our formats first, in order
        try
        {
            return this.tikaUTCDateFormater.parseDateTime(dateStr).toDate();
        }
        catch (IllegalArgumentException e) {}

        try
        {
            return this.tikaUTCDateFormater.withLocale(Locale.US).parseDateTime(dateStr).toDate();
        }
        catch (IllegalArgumentException e) {}

        try
        {
            return this.tikaDateFormater.parseDateTime(dateStr).toDate();
        }
        catch (IllegalArgumentException e) {}

        try
        {
            return this.tikaDateFormater.withLocale(Locale.US).parseDateTime(dateStr).toDate();
        }
        catch (IllegalArgumentException e) {}

        // Fall back to the normal ones
        return super.makeDate(dateStr);
    }
    
    /**
     * Returns the correct Tika Parser to process the document.
     * If you don't know which you want, use {@link TikaAutoMetadataExtracter}
     * which makes use of the Tika auto-detection.
     */
    protected abstract Parser getParser();
    
    /**
     * Returns the Tika Embedder to modify
     * the document.
     * 
     * @return the Tika embedder
     */
    protected Embedder getEmbedder()
    {
        // TODO make this an abstract method once more extracters support embedding
        return null;
    }
    
    /**
     * Do we care about the contents of the
     *  extracted header, or nothing at all?
     */
    protected boolean needHeaderContents() 
    {
       return false;
    }
    
    /**
     * Allows implementation specific mappings to be done.
     */
    protected Map<String, Serializable> extractSpecific(Metadata metadata, 
          Map<String, Serializable> properties, Map<String,String> headers) 
    {
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
    protected InputStream getInputStream(ContentReader reader) throws IOException
    {
       // Prefer the File if available, it's generally quicker
       if(reader instanceof FileContentReader) 
       {
          return TikaInputStream.get( ((FileContentReader)reader).getFile() );
       }
       
       // Grab the InputStream for the Content
       InputStream input = reader.getContentInputStream();
       
       // Images currently always require a file
       if(MimetypeMap.MIMETYPE_IMAGE_JPEG.equals(reader.getMimetype()) ||
          MimetypeMap.MIMETYPE_IMAGE_TIFF.equals(reader.getMimetype())) 
       {
          TemporaryResources tmp = new TemporaryResources();
          TikaInputStream stream = TikaInputStream.get(input, tmp);
          stream.getFile(); // Have it turned into File backed
          return stream;
       }
       else
       {
          // The regular Content InputStream should be fine
          return input; 
       }
    }
    
    /**
     * Sets the document selector, used for determining whether to parse embedded resources.
     * 
     * @param documentSelector
     */
    public void setDocumentSelector(DocumentSelector documentSelector)
    {
        this.documentSelector = documentSelector;
    }
    /**
     * Gets the document selector, used for determining whether to parse embedded resources, 
     * null by default so parse all.
     * 
     * @param metadata
     * @param targetMimeType
     * @return the document selector
     */
    protected DocumentSelector getDocumentSelector(Metadata metadata, String targetMimeType)
    {
        return documentSelector;
    }
    
    /**
     * By default returns a new ParseContent
     * 
     * @param metadata
     * @param sourceMimeType
     * @return the parse context
     */
    protected ParseContext buildParseContext(Metadata metadata, String sourceMimeType)
    {
        ParseContext context = new ParseContext();
        DocumentSelector selector = getDocumentSelector(metadata, sourceMimeType);
        if (selector != null)
        {
            context.set(DocumentSelector.class, selector);
        }
        return context;
    }
    
    @SuppressWarnings("deprecation")
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
            metadata.add(Metadata.CONTENT_TYPE, reader.getMimetype());

            ParseContext context = buildParseContext(metadata, reader.getMimetype());
            
            ContentHandler handler;
            Map<String,String> headers = null;
            if(needHeaderContents()) 
            {
               MapCaptureContentHandler headerCapture = 
                  new MapCaptureContentHandler();
               headers = headerCapture.tags;
               handler = new HeadContentHandler(headerCapture);
            } 
            else 
            {
               handler = new NullContentHandler(); 
            }

            parser.parse(is, handler, metadata, context);
            
            // First up, copy all the Tika metadata over
            // This allows people to map any of the Tika
            //  keys onto their own content model
            for(String tikaKey : metadata.names()) 
            {
                // TODO review this change (part of MNT-15267) - should we really force string concatenation here !?
               putRawValue(tikaKey, getMetadataValue(metadata, tikaKey), rawProperties);
            }
            
            // Now, map the common Tika metadata keys onto
            //  the common Alfresco metadata keys. This allows
            //  existing mapping properties files to continue
            //  to work without needing any changes
            
            // The simple ones
            putRawValue(KEY_AUTHOR, getMetadataValue(metadata, Metadata.AUTHOR), rawProperties);
            putRawValue(KEY_TITLE, getMetadataValue(metadata, Metadata.TITLE), rawProperties);
            putRawValue(KEY_COMMENTS, getMetadataValue(metadata, Metadata.COMMENTS), rawProperties);

            // Tags
            putRawValue(KEY_TAGS, getMetadataValues(metadata, KEY_TAGS), rawProperties);

            // Get the subject and description, despite things not
            //  being nearly as consistent as one might hope
            String subject = getMetadataValue(metadata, Metadata.SUBJECT);
            String description = getMetadataValue(metadata, Metadata.DESCRIPTION);
            if(subject != null && description != null) 
            {
               putRawValue(KEY_DESCRIPTION, description, rawProperties);
               putRawValue(KEY_SUBJECT, subject, rawProperties);
            } 
            else if(subject != null) 
            {
               putRawValue(KEY_DESCRIPTION, subject, rawProperties);
               putRawValue(KEY_SUBJECT, subject, rawProperties);
            } 
            else if(description != null) 
            {
               putRawValue(KEY_DESCRIPTION, description, rawProperties);
               putRawValue(KEY_SUBJECT, description, rawProperties);
            }
            
            // Try for the dates two different ways too
            if(metadata.get(Metadata.CREATION_DATE) != null) 
            {
               putRawValue(KEY_CREATED, metadata.get(Metadata.CREATION_DATE), rawProperties);
            } 
            else if(metadata.get(Metadata.DATE) != null) 
            {
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
    
    @Override
    protected void embedInternal(Map<String, Serializable> properties, ContentReader reader, ContentWriter writer) throws Throwable
    {
        Embedder embedder = getEmbedder();
        if (embedder == null)
        {
            return;
        }

        Map<String, String> metadataAsStrings = convertMetadataToStrings(properties);
        Metadata metadataToEmbed = new Metadata();
        metadataAsStrings.forEach((k,v)->metadataToEmbed.add(k, v));

        InputStream inputStream = getInputStream(reader);
        OutputStream outputStream = writer.getContentOutputStream();
        embedder.embed(metadataToEmbed, inputStream, outputStream, null);
    }

    private Serializable getMetadataValues(Metadata metadata, String key)
    {
        // Use Set to prevent duplicates.
        Set<String> valuesSet = new LinkedHashSet<String>();
        String[] values = metadata.getValues(key);

        for (int i = 0; i < values.length; i++)
        {
            String[] parts = values[i].split(metadataSeparator);

            for (String subPart : parts)
            {
                valuesSet.add(subPart.trim());
            }
        }

        Object[] objArrayValues = valuesSet.toArray();
        values = Arrays.copyOf(objArrayValues, objArrayValues.length, String[].class);

        return values.length == 0 ? null : (values.length == 1 ? values[0] : values);
    }
    
    private String getMetadataValue(Metadata metadata, String key)
    {
        if (metadata.isMultiValued(key))
        {
            String[] parts = metadata.getValues(key);
            
            // use Set to prevent duplicates
            Set<String> value = new LinkedHashSet<String>(parts.length);
            
            for (int i = 0; i < parts.length; i++)
            {
                value.add(parts[i]);
            }
            
            String valueStr = value.toString();
            
            // remove leading/trailing braces []
            return valueStr.substring(1, valueStr.length() - 1);
        }
        else
        {
            return metadata.get(key);
        }
    }
    
    /**
     * Exif metadata for size also returns the string "pixels"
     * after the number value , this function will 
     * stop at the first non digit character found in the text
     * @param sizeText string text
     * @return the size value
     */
    protected String extractSize(String sizeText)
    {
        StringBuilder sizeValue = new StringBuilder();
        for(char c : sizeText.toCharArray())
        {
            if(Character.isDigit(c))
            {
                sizeValue.append(c);
            }
            else
            {
                break;
            }
        }
        return sizeValue.toString();
    }
    
    /**
     * This content handler will capture entries from within
     *  the header of the Tika content XHTML, but ignore the
     *  rest.
     */
    protected static class HeadContentHandler extends ContentHandlerDecorator 
    {
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
       protected HeadContentHandler(ContentHandler handler) 
       {
           super(new MatchingContentHandler(handler, MATCHER));
       }
    }
    /**
     * This content handler will grab all tags and attributes,
     *  and record the textual content of the last seen one
     *  of them.
     * Normally only used with {@link HeadContentHandler} 
     */
    protected static class MapCaptureContentHandler implements ContentHandler 
    {
        protected Map<String, String> tags = new HashMap<String, String>();
       private StringBuffer text;

      public void characters(char[] ch, int start, int len) 
      {
         if(text != null) 
         {
            text.append(ch, start, len);
         }
      }

      public void endElement(String namespace, String localname, String qname) 
      {
         if(text != null && text.length() > 0) 
         {
            tags.put(qname, text.toString());
         }
         text = null;
      }

      public void startElement(String namespace, String localname, String qname, Attributes attrs) 
      {
         for(int i=0; i<attrs.getLength(); i++) 
         {
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
    protected static class NullContentHandler implements ContentHandler 
    {
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
