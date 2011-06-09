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

import org.apache.tika.config.TikaConfig;
import org.apache.tika.mime.MediaType;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.Parser;

/**
 * A Content Extractor for XML, HTML and Text,
 *  which makes use of the Apache Tika 
 *  auto-detection to select the best parser
 *  to process your document.
 * This will be used for all files which Tika can
 *  handle, but where no other more explicit
 *  extractor is defined. 
 * 
 * @author Nick Burch
 */
public class TikaAutoContentTransformer extends TikaPoweredContentTransformer
{
    private static AutoDetectParser parser;
    private static TikaConfig config;

    /** 
     * We support all the mimetypes that the Tika
     *  auto-detect parser can handle, except for
     *  Image, Audio and Video ones which don't 
     *  make much sense
     */
    public static ArrayList<String> SUPPORTED_MIMETYPES;
    private static ArrayList<String> buildMimeTypes(TikaConfig tikaConfig)
    {
       config = tikaConfig;
       parser = new AutoDetectParser(config);

       SUPPORTED_MIMETYPES = new ArrayList<String>();
       for(MediaType baseType : parser.getParsers().keySet()) 
       {
          // Register both the canonical type, and any alias it may have
          // Alfresco sometimes uses the canonical type, and sometimes an alias
          ArrayList<MediaType> types = new ArrayList<MediaType>();
          types.add(baseType);
          types.addAll( config.getMediaTypeRegistry().getAliases(baseType) );
           
          for(MediaType mt : types) 
          {
              if(mt.toString().startsWith("application/vnd.oasis.opendocument.formula")) {
                  // TODO Tika support for quick.odf, mimetype=application/vnd.oasis.opendocument.formula
                  // TODO Tika support for quick.otf, mimetype=application/vnd.oasis.opendocument.formula-template
                  continue;
              }
              if(mt.toString().startsWith("application/vnd.oasis.opendocument.graphics")) {
                  // TODO Tika support for quick.odg, mimetype=application/vnd.oasis.opendocument.graphics
                  // TODO Tika support for quick.otg, mimetype=application/vnd.oasis.opendocument.graphics-template
                  continue;
              }

              if(mt.getType().equals("image") ||
                      mt.getType().equals("audio") ||
                      mt.getType().equals("video")) 
              {
                  // Skip these, as Tika mostly just does
                  //  metadata rather than content
              }
              else if(mt.toString().equals("application/zip") ||
                      mt.toString().equals("application/tar") || 
                      mt.toString().equals("application/x-tar"))
              {
                  // Skip these, as we handle container formats in a different
                  //  transformer to give the user control over recursion
              }
              else if(mt.toString().equals("message/rfc822") ||
                      mt.toString().equals("application/vnd.ms-outlook"))
              {
                  // Skip these, as we want our textual representations to include
                  //  parts of the metadata (eg people, subjects, dates) too
              }
              else
              {
                  // Tika can probably do some useful text
                  SUPPORTED_MIMETYPES.add( mt.toString() );
              }
          }
       }
       return SUPPORTED_MIMETYPES;
    }
   
    public TikaAutoContentTransformer(TikaConfig tikaConfig)
    {
       super( buildMimeTypes(tikaConfig) );
    }
    
    /**
     * Returns the Tika Auto-Detection
     *  parser, which will try to 
     *  process all documents that Tika
     *  knows about
     */
    protected Parser getParser()
    {
       return parser;
    }
}
