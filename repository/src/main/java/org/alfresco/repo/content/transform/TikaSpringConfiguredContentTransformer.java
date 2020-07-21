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

import org.alfresco.error.AlfrescoRuntimeException;
import org.apache.tika.mime.MediaType;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;

/**
 * A Content Extractor for XML, HTML and Text, which makes
 *  use of Apache Tika, and allows the selection of the
 *  Tika parser to be sprung-in.
 * Using spring, you list the Tika parser to use, which
 *  may well not be a standard Tika one. You should specify
 *  either a spring created bean, or a parser class name.
 * 
 * @author Nick Burch
 */
public class TikaSpringConfiguredContentTransformer extends TikaPoweredContentTransformer
{
    private Parser tikaParser;
    private String tikaParserClassName;
    private Class<? extends Parser> tikaParserClass;
   
    /**
     * Injects the name of the Tika parser to use
     * @param className String
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
     * @param tikaParser Parser
     */
    public void setTikaParser(Parser tikaParser)
    {
       this.tikaParser = tikaParser;
       
       // Build the mime types, updating the copy our parent
       //  holds for us as we go along
       for(MediaType mt : tikaParser.getSupportedTypes(new ParseContext()))
       {
          super.sourceMimeTypes.add( mt.toString() );
       }
    }
   
    public TikaSpringConfiguredContentTransformer() {
       super(new ArrayList<String>());
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
