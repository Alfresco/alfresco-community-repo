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

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;

import org.alfresco.service.cmr.repository.ContentReader;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.ContentHandler;

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
        
        this.tikaDateFormats = new DateFormat[] {
              new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ"),
              new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.US),
              new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss"),
              new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US),
              new SimpleDateFormat("yyyy-MM-dd"),
              new SimpleDateFormat("yyyy-MM-dd", Locale.US),
              new SimpleDateFormat("EEE MMM dd hh:mm:ss zzz yyyy"),
              new SimpleDateFormat("EEE MMM dd hh:mm:ss zzz yyyy", Locale.US)
        };
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
     * Allows implementation specific mappings
     *  to be done.
     */
    protected Map<String, Serializable> extractSpecific(Metadata metadata, Map<String, Serializable> properties) {
       return properties;
    }
    
    @Override
    protected Map<String, Serializable> extractRaw(ContentReader reader) throws Throwable
    {
        Map<String, Serializable> rawProperties = newRawMap();

        InputStream is = null;
        try
        {
            is = reader.getContentInputStream();
            Parser parser = getParser();
            ContentHandler handler = new BodyContentHandler() ;
            Metadata metadata = new Metadata();
            ParseContext context = new ParseContext();

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
            rawProperties = extractSpecific(metadata, rawProperties);
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
}
