/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.content.metadata;

import java.io.File;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;

import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.util.TempFileProvider;
import org.farng.mp3.AbstractMP3FragmentBody;
import org.farng.mp3.MP3File;
import org.farng.mp3.id3.AbstractID3v2;
import org.farng.mp3.id3.AbstractID3v2Frame;
import org.farng.mp3.id3.ID3v1;
import org.farng.mp3.lyrics3.AbstractLyrics3;
import org.farng.mp3.lyrics3.Lyrics3v2;
import org.farng.mp3.lyrics3.Lyrics3v2Field;

/**
 * Extracts the following values from MP3 files:
 * <pre>
 *   <b>songTitle:</b>              --      {music}songTitle, cm:title
 *   <b>albumTitle:</b>             --      {music}albumTitle
 *   <b>artist:</b>                 --      {music}artist, cm:author
 *   <b>description:</b>            --      cm:description
 *   <b>comment:</b>                --      {music}comment
 *   <b>yearReleased:</b>           --      {music}yearReleased
 *   <b>trackNumber:</b>            --      {music}trackNumber
 *   <b>genre:</b>                  --      {music}genre
 *   <b>composer:</b>               --      {music}composer
 *   <b>lyrics:</b>                 --      {music}lyrics
 * </pre>
 * 
 * @author Roy Wetherall
 */
public class MP3MetadataExtracter extends AbstractMappingMetadataExtracter
{
    private static final String KEY_SONG_TITLE = "songTitle";
    private static final String KEY_ALBUM_TITLE = "albumTitle";
    private static final String KEY_ARTIST = "artist";
    private static final String KEY_DESCRIPTION = "description";
    private static final String KEY_COMMENT = "comment";
    private static final String KEY_YEAR_RELEASED = "yearReleased";
    private static final String KEY_TRACK_NUMBER = "trackNumber";
    private static final String KEY_GENRE = "genre";
    private static final String KEY_COMPOSER = "composer";
    private static final String KEY_LYRICS = "lyrics";

    public static String[] SUPPORTED_MIMETYPES = new String[] {MimetypeMap.MIMETYPE_MP3 };
    
    public MP3MetadataExtracter()
    {
        super(new HashSet<String>(Arrays.asList(SUPPORTED_MIMETYPES)));
    }

    @Override
    public Map<String, Serializable> extractRaw(ContentReader reader) throws Throwable
    {
        Map<String, Serializable> rawProperties = newRawMap();
        
        // Create a temp file
        File tempFile = TempFileProvider.createTempFile("MP3MetadataExtracter_", ".tmp");
        try
        {
            reader.getContent(tempFile);
            
            // Create the MP3 object from the file
            MP3File mp3File = new MP3File(tempFile);
            
            ID3v1 id3v1 = mp3File.getID3v1Tag();
            if (id3v1 != null)
            {
                putRawValue(KEY_ALBUM_TITLE, id3v1.getAlbum(), rawProperties);
                putRawValue(KEY_SONG_TITLE, id3v1.getTitle(), rawProperties);
                putRawValue(KEY_ARTIST, id3v1.getArtist(), rawProperties);
                putRawValue(KEY_COMMENT, id3v1.getComment(), rawProperties);
                putRawValue(KEY_YEAR_RELEASED, id3v1.getYear(), rawProperties);
                
                // TODO sort out the genre
                //putRawValue(MusicModel.KEY_GENRE, id3v1.getGenre());
                
                // TODO sort out the size
                //putRawValue(MusicModel.KEY_SIZE, id3v1.getSize());            
            }
            
            AbstractID3v2 id3v2 = mp3File.getID3v2Tag();
            if (id3v2 != null)
            {
                putRawValue(KEY_SONG_TITLE, getID3V2Value(id3v2, "TIT2"), rawProperties);
                putRawValue(KEY_ARTIST, getID3V2Value(id3v2, "TPE1"), rawProperties);
                putRawValue(KEY_ALBUM_TITLE, getID3V2Value(id3v2, "TALB"), rawProperties);
                putRawValue(KEY_YEAR_RELEASED, getID3V2Value(id3v2, "TDRC"), rawProperties);
                putRawValue(KEY_COMMENT, getID3V2Value(id3v2, "COMM"), rawProperties);
                putRawValue(KEY_TRACK_NUMBER, getID3V2Value(id3v2, "TRCK"), rawProperties);
                putRawValue(KEY_GENRE, getID3V2Value(id3v2, "TCON"), rawProperties);
                putRawValue(KEY_COMPOSER, getID3V2Value(id3v2, "TCOM"), rawProperties);
                
                // TODO sort out the lyrics
                //System.out.println("Lyrics: " + getID3V2Value(id3v2, "SYLT"));
                //System.out.println("Lyrics: " + getID3V2Value(id3v2, "USLT"));
            }
            
            AbstractLyrics3 lyrics3Tag = mp3File.getLyrics3Tag();
            if (lyrics3Tag != null)
            {
                System.out.println("Lyrics3 tag found.");
                if (lyrics3Tag instanceof Lyrics3v2)
                {
                    putRawValue(KEY_SONG_TITLE, getLyrics3v2Value((Lyrics3v2)lyrics3Tag, "TIT2"), rawProperties);
                    putRawValue(KEY_ARTIST, getLyrics3v2Value((Lyrics3v2)lyrics3Tag, "TPE1"), rawProperties);
                    putRawValue(KEY_ALBUM_TITLE, getLyrics3v2Value((Lyrics3v2)lyrics3Tag, "TALB"), rawProperties);
                    putRawValue(KEY_COMMENT, getLyrics3v2Value((Lyrics3v2)lyrics3Tag, "COMM"), rawProperties);
                    putRawValue(KEY_LYRICS, getLyrics3v2Value((Lyrics3v2)lyrics3Tag, "SYLT"), rawProperties);
                    putRawValue(KEY_COMPOSER, getLyrics3v2Value((Lyrics3v2)lyrics3Tag, "TCOM"), rawProperties);
                }
            }
            
        }
        finally
        {
            tempFile.delete();
        }
        
        String description = getDescription(rawProperties);
        if (description != null)
        {
            putRawValue(KEY_DESCRIPTION, description, rawProperties);
        }
        
        // Done
        return rawProperties;
    }
    

    /**
     * Generate the description
     * 
     * @param props     the properties extracted from the file
     * @return          the description
     */
    private String getDescription(Map<String, Serializable> props)
    {
        StringBuilder result = new StringBuilder();
        if (props.get(KEY_SONG_TITLE) != null && props.get(KEY_ARTIST) != null && props.get(KEY_ALBUM_TITLE) != null)
        {
            result
                .append(props.get(KEY_SONG_TITLE))
                .append(" - ")
                .append(props.get(KEY_ALBUM_TITLE))
                .append(" (")
                .append(props.get(KEY_ARTIST))
                .append(")");
                
        }
        
        return result.toString();
    }

    private String getLyrics3v2Value(Lyrics3v2 lyrics3Tag, String name) 
    {
        String result = "";
        Lyrics3v2Field field = lyrics3Tag.getField(name);
        if (field != null)
        {
            AbstractMP3FragmentBody body = field.getBody();
            if (body != null)
            {
                result = (String)body.getObject("Text");                
            }
        }
        return result;
    }

    /**
     * Get the ID3V2 tag value in a safe way
     */
    private String getID3V2Value(AbstractID3v2 id3v2, String name)
    {
        String result = "";
        
        AbstractID3v2Frame frame = id3v2.getFrame(name);
        if (frame != null)
        {
            AbstractMP3FragmentBody body = frame.getBody();
            if (body != null)
            {
                result = (String)body.getObject("Text");                
            }
        }
        
        return result;
    }
}
