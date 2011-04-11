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

import org.alfresco.service.cmr.repository.TransformationOptions;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.parser.pkg.PackageParser;

/**
 * This class transforms archive files (zip, tar etc) to text, which enables indexing
 *  and searching of archives as well as webpreviewing.
 * The transformation can simply list the names of the entries within the archive, or
 *  it can also include the textual content of the entries themselves.
 * The former is suggested for web preview, the latter for indexing.
 * This behaviour is controlled by the recurse flag. 
 * 
 * @author Neil McErlean
 * @author Nick Burch
 * @since 3.4
 */
public class ArchiveContentTransformer extends TikaPoweredContentTransformer
{ 
    /**
     * The logger
     */
    private static Log logger = LogFactory.getLog(ArchiveContentTransformer.class);

    private boolean includeContents = false;
    public void setIncludeContents(String includeContents)
    {
       // Spring really ought to be able to handle
       //  setting a boolean that might still be
       //  ${foo} (i.e. not overridden in a property).
       // As we can't do that with spring, we do it...
       this.includeContents = false;
       if(includeContents != null && includeContents.length() > 0)
       {
          this.includeContents = TransformationOptions.relaxedBooleanTypeConverter.convert(includeContents).booleanValue();
       }
    }
    
    /** 
     * We support all the archive mimetypes that the Tika
     *  office parser can handle
     */
    public static ArrayList<String> SUPPORTED_MIMETYPES;
    static {
       SUPPORTED_MIMETYPES = new ArrayList<String>();
       Parser p = new PackageParser();
       for(MediaType mt : p.getSupportedTypes(null)) {
          // Tika can probably do some useful text
          SUPPORTED_MIMETYPES.add( mt.toString() );
       }
    }
     
    public ArchiveContentTransformer() {
        super(SUPPORTED_MIMETYPES);
    }
    
    @Override
    protected Parser getParser() {
      return new PackageParser();
    }

    @Override
    protected ParseContext buildParseContext(Metadata metadata,
         String targetMimeType, TransformationOptions options) {
      ParseContext context = super.buildParseContext(metadata, targetMimeType, options);
      
      boolean recurse = includeContents;
      if(options.getIncludeEmbedded() != null)
      {
         recurse = options.getIncludeEmbedded();
      }
      if(recurse)
      {
         context.set(Parser.class, new AutoDetectParser());
      }
      
      return context;
    }
}
