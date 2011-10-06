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
import java.util.HashSet;

import org.alfresco.error.AlfrescoRuntimeException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tika.mime.MediaType;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;

/**
 * A Metadata Extractor which makes use of Apache Tika,
 *  and allows the selection of the Tika parser to be
 *  sprung-in to extract the metadata from your document.
 * This is typically used with custom Tika Parsers.

 * <pre>
 *   <b>author:</b>                 --      cm:author
 *   <b>title:</b>                  --      cm:title
 *   <b>subject:</b>                --      cm:description
 *   <b>created:</b>                --      cm:created
 *   <b>comments:</b>
 *   <p>geo:lat:</b>                --      cm:latitude
 *   <p>geo:long:</b>               --      cm:longitude
 * </pre>
 * 
 * @since 3.4
 * @author Nick Burch
 */
public class TikaSpringConfiguredMetadataExtracter extends TikaPoweredMetadataExtracter
{
    protected static Log logger = LogFactory.getLog(TikaSpringConfiguredMetadataExtracter.class);

    private Parser tikaParser;
    private String tikaParserClassName;
    private Class<? extends Parser> tikaParserClass;
   
    /**
     * Injects the name of the Tika parser to use
     * @param className
     */
    @SuppressWarnings("unchecked")
    public void setTikaParserName(String className)
    {
       tikaParserClassName = className;
       
       // Load the class
       try {
          tikaParserClass = (Class<? extends Parser>)Class.forName(tikaParserClassName);
          setTikaParser(getParser());
       } catch(ClassNotFoundException e) {
          throw new AlfrescoRuntimeException("Specified Tika Parser '" + tikaParserClassName + "' not found");
       }
    }
    
    /**
     * Injects the Tika parser to use
     * @param parser
     */
    public void setTikaParser(Parser tikaParser)
    {
       this.tikaParser = tikaParser;
       
       // Build the mime types, updating the copy our parent
       //  holds for us as we go along
       ArrayList<String> mimetypes = new ArrayList<String>();
       for(MediaType mt : tikaParser.getSupportedTypes(new ParseContext()))
       {
          mimetypes.add( mt.toString() );
       }
       super.setSupportedMimetypes(mimetypes);
    }
    
    public TikaSpringConfiguredMetadataExtracter()
    {
       super(new HashSet<String>());
    }
    
    /**
     * Returns the Tika parser
     */
    protected Parser getParser()
    {
       // If we were given a whole parser, return it
       if(tikaParser != null)
          return tikaParser;
       
       // Otherwise create a new one
       try {
          return tikaParserClass.newInstance();
       } catch (InstantiationException e) {
          throw new AlfrescoRuntimeException("Unable to create specified Parser", e);
       } catch (IllegalAccessException e) {
          throw new AlfrescoRuntimeException("Unable to create specified Parser", e);
       }
    }
}
