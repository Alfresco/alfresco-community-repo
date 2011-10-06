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
import java.util.Map;

import org.alfresco.repo.content.MimetypeMap;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.XMPDM;
import org.apache.tika.parser.Parser;
import org.apache.tika.parser.mp3.Mp3Parser;

/**
 * Extracts the following values from MP3 files:
 * <pre>
 *   <b>songTitle:</b>              --      cm:title
 *   <b>albumTitle:</b>             --      audio:album
 *   <b>artist:</b>                 --      audio:artist, cm:author
 *   <b>description:</b>            --      cm:description
 *   <b>comment:</b>                --      
 *   <b>yearReleased:</b>           --      audio:releaseDate
 *   <b>trackNumber:</b>            --      audio:trackNumber
 *   <b>genre:</b>                  --      audio:genre
 *   <b>composer:</b>               --      audio:composer
 *   <b>lyrics:</b>                 --      
 * </pre>
 * 
 * Note - XMPDM metadata keys are also emitted, in common with
 *  the other Tika powered extracters 
 * 
 * Uses Apache Tika
 * 
 * @author Nick Burch
 */
public class MP3MetadataExtracter extends TikaAudioMetadataExtracter
{
    private static final String KEY_SONG_TITLE = "songTitle";
    private static final String KEY_ALBUM_TITLE = "albumTitle";
    private static final String KEY_ARTIST = "artist";
    private static final String KEY_COMMENT = "comment";
    private static final String KEY_YEAR_RELEASED = "yearReleased";
    private static final String KEY_TRACK_NUMBER = "trackNumber";
    private static final String KEY_GENRE = "genre";
    private static final String KEY_COMPOSER = "composer";

    public static ArrayList<String> SUPPORTED_MIMETYPES = buildSupportedMimetypes(
          new String[] { MimetypeMap.MIMETYPE_MP3 },
          new Mp3Parser()
    );
    
    public MP3MetadataExtracter()
    {
        super(SUPPORTED_MIMETYPES);
    }
    
    @Override
    protected Parser getParser() {
       return new Mp3Parser();
    }

    @Override
    protected Map<String, Serializable> extractSpecific(Metadata metadata,
         Map<String, Serializable> properties, Map<String,String> headers) {
       // Do the normal Audio mappings
       super.extractSpecific(metadata, properties, headers);
       
       // Now do the compatibility ones
       // We only need these for people who had pre-existing mapping
       //  properties from before the proper audio model was added
       putRawValue(KEY_ALBUM_TITLE, metadata.get(XMPDM.ALBUM), properties);
       putRawValue(KEY_SONG_TITLE, metadata.get(Metadata.TITLE), properties);
       putRawValue(KEY_ARTIST, metadata.get(XMPDM.ARTIST), properties);
       putRawValue(KEY_COMMENT, metadata.get(XMPDM.LOG_COMMENT), properties);
       putRawValue(KEY_TRACK_NUMBER, metadata.get(XMPDM.TRACK_NUMBER), properties);
       putRawValue(KEY_GENRE, metadata.get(XMPDM.GENRE), properties);
       putRawValue(KEY_YEAR_RELEASED, metadata.get(XMPDM.RELEASE_DATE), properties);
       putRawValue(KEY_COMPOSER, metadata.get(XMPDM.COMPOSER), properties);

       // All done
       return properties;
    }
}
