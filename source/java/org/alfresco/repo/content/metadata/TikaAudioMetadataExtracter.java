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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import org.alfresco.repo.content.MimetypeMap;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.XMPDM;
import org.apache.tika.parser.CompositeParser;
import org.apache.tika.parser.Parser;
import org.gagravarr.tika.FlacParser;
import org.gagravarr.tika.VorbisParser;

/**
 * A Metadata Extractor which makes use of the Apache
 *  Tika Audio Parsers to extract metadata from your
 *  media files. 
 * For backwards compatibility reasons, this doesn't
 *  handle the MP3 format, which has its own dedicated
 *  extractor in {@link MP3MetadataExtracter}

 * <pre>
 *   <b>author:</b>                 --      cm:author
 *   <b>title:</b>                  --      cm:title
 *   <b>created:</b>                --      cm:created
 *   <b>xmpDM:artist</b>            --      audio:artist
 *   <b>xmpDM:composer</b>          --      audio:composer
 *   <b>xmpDM:engineer</b>          --      audio:engineer
 *   <b>xmpDM:genre</b>             --      audio:genre
 *   <b>xmpDM:trackNumber</b>       --      audio:trackNumber
 *   <b>xmpDM:releaseDate</b>       --      audio:releaseDate
 * </pre>
 * 
 * @since 4.0
 * @author Nick Burch
 */
public class TikaAudioMetadataExtracter extends TikaPoweredMetadataExtracter
{
    protected static final String KEY_LYRICS = "lyrics";
    
    private static Parser[] parsers = new Parser[] {
       new VorbisParser(),
       new FlacParser()
    };
    public static ArrayList<String> SUPPORTED_MIMETYPES = buildSupportedMimetypes(
          new String[] { MimetypeMap.MIMETYPE_VORBIS, MimetypeMap.MIMETYPE_FLAC }, 
          parsers
    );
    
    protected TikaConfig tikaConfig;
    public void setTikaConfig(TikaConfig tikaConfig)
    {
       this.tikaConfig = tikaConfig;
    }
    
    public TikaAudioMetadataExtracter()
    {
        this(SUPPORTED_MIMETYPES);
    }
    public TikaAudioMetadataExtracter(ArrayList<String> supportedMimeTypes)
    {
       super(supportedMimeTypes);
    }
    
    @Override
    protected Parser getParser() 
    {
       return new CompositeParser(
             tikaConfig.getMediaTypeRegistry(), parsers
       );
    }

    @Override
    protected Map<String, Serializable> extractSpecific(Metadata metadata,
         Map<String, Serializable> properties, Map<String,String> headers) 
    {
       // Most things can go with the default Tika -> Alfresco Mapping
       // Handle the few special cases here
       
       // The description is special
       putRawValue(KEY_DESCRIPTION, generateDescription(metadata), properties);
       
       // The release date can be fiddly
       Date releaseDate = generateReleaseDate(metadata);
       putRawValue(KEY_CREATED, releaseDate, properties);
       putRawValue(XMPDM.RELEASE_DATE.getName(), releaseDate, properties);
       
       // TODO Get the Lyrics from the content
       //putRawValue(KEY_LYRICS, getLyrics(), properties);
       
       // All done
       return properties;
    }
    
    /**
     * Generates the release date
     */
    private Date generateReleaseDate(Metadata metadata)
    {
       String date = metadata.get(XMPDM.RELEASE_DATE);
       if(date == null || date.length() == 0)
       {
          return null;
       }
          
       // Is it just a year?
       if(date.matches("\\d\\d\\d\\d"))
       {
          // Just a year, we need a full date
          // Go for the 1st of the 1st
          Calendar c = Calendar.getInstance();
          c.set(
                Integer.parseInt(date), Calendar.JANUARY, 1,
                0, 0, 0
          );
          c.set(Calendar.MILLISECOND, 0);
          return c.getTime();
       }
       
       // Treat as a normal date
       return makeDate(date);
    }
    
    /**
     * Generate the description
     * 
     * @param props     the properties extracted from the file
     * @return          the description
     */
    private String generateDescription(Metadata metadata)
    {
        StringBuilder result = new StringBuilder();
        if (metadata.get(Metadata.TITLE) != null)
        {
            result.append(metadata.get(Metadata.TITLE));
            if (metadata.get(XMPDM.ALBUM) != null)
            {
               result
                .append(" - ")
                .append(metadata.get(XMPDM.ALBUM));
            }
            if (metadata.get(XMPDM.ARTIST) != null)
            {
               result
                .append(" (")
                .append(metadata.get(XMPDM.ARTIST))
                .append(")");
            }
        }
        
        return result.toString();
    }
}
