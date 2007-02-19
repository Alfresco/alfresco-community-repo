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
import java.util.HashMap;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.namespace.QName;
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
 * @author Roy Wetherall
 */
public class MP3MetadataExtracter extends AbstractMetadataExtracter
{
    private static final QName PROP_ALBUM_TITLE = QName.createQName("{music}albumTitle");
    private static final QName PROP_SONG_TITLE = QName.createQName("{music}songTitle");;
    private static final QName PROP_ARTIST = QName.createQName("{music}artist");;
    private static final QName PROP_COMMENT = QName.createQName("{music}comment");;
    private static final QName PROP_YEAR_RELEASED = QName.createQName("{music}yearReleased");;
    private static final QName PROP_TRACK_NUMBER = QName.createQName("{music}trackNumber");;
    private static final QName PROP_GENRE = QName.createQName("{music}genre");;
    private static final QName PROP_COMPOSER = QName.createQName("{music}composer");;
    private static final QName PROP_LYRICS = QName.createQName("{music}lyrics");;

    public MP3MetadataExtracter()
    {
        super(MimetypeMap.MIMETYPE_MP3, 1.0, 1000);
    }

    public void extractInternal(
            ContentReader reader,
            Map<QName, Serializable> destination) throws Throwable
    {
        Map<QName, Serializable> props = new HashMap<QName, Serializable>();            
        
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
                setTagValue(props, PROP_ALBUM_TITLE, id3v1.getAlbum());
                setTagValue(props, PROP_SONG_TITLE, id3v1.getTitle());
                setTagValue(props, PROP_ARTIST, id3v1.getArtist());
                setTagValue(props, PROP_COMMENT, id3v1.getComment());
                setTagValue(props, PROP_YEAR_RELEASED, id3v1.getYear());
                
                // TODO sort out the genre
                //setTagValue(props, MusicModel.PROP_GENRE, id3v1.getGenre());
                
                // TODO sort out the size
                //setTagValue(props, MusicModel.PROP_SIZE, id3v1.getSize());            
            }
            
            AbstractID3v2 id3v2 = mp3File.getID3v2Tag();
            if (id3v2 != null)
            {
                setTagValue(props, PROP_SONG_TITLE, getID3V2Value(id3v2, "TIT2"));
                setTagValue(props, PROP_ARTIST, getID3V2Value(id3v2, "TPE1"));
                setTagValue(props, PROP_ALBUM_TITLE, getID3V2Value(id3v2, "TALB"));
                setTagValue(props, PROP_YEAR_RELEASED, getID3V2Value(id3v2, "TDRC"));
                setTagValue(props, PROP_COMMENT, getID3V2Value(id3v2, "COMM"));
                setTagValue(props, PROP_TRACK_NUMBER, getID3V2Value(id3v2, "TRCK"));
                setTagValue(props, PROP_GENRE, getID3V2Value(id3v2, "TCON"));
                setTagValue(props, PROP_COMPOSER, getID3V2Value(id3v2, "TCOM"));
                
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
                    setTagValue(props, PROP_SONG_TITLE, getLyrics3v2Value((Lyrics3v2)lyrics3Tag, "TIT2"));
                    setTagValue(props, PROP_ARTIST, getLyrics3v2Value((Lyrics3v2)lyrics3Tag, "TPE1"));
                    setTagValue(props, PROP_ALBUM_TITLE, getLyrics3v2Value((Lyrics3v2)lyrics3Tag, "TALB"));
                    setTagValue(props, PROP_COMMENT, getLyrics3v2Value((Lyrics3v2)lyrics3Tag, "COMM"));
                    setTagValue(props, PROP_LYRICS, getLyrics3v2Value((Lyrics3v2)lyrics3Tag, "SYLT"));
                    setTagValue(props, PROP_COMPOSER, getLyrics3v2Value((Lyrics3v2)lyrics3Tag, "TCOM"));
                }
            }
            
        }
        finally
        {
            tempFile.delete();
        }
        
        // Set the destination values
        if (props.get(PROP_SONG_TITLE) != null)
        {
            destination.put(ContentModel.PROP_TITLE, props.get(PROP_SONG_TITLE));
        }
        if (props.get(PROP_ARTIST) != null)
        {
            destination.put(ContentModel.PROP_AUTHOR, props.get(PROP_ARTIST));
        }
        String description = getDescription(props);
        if (description != null)
        {
            destination.put(ContentModel.PROP_DESCRIPTION, description);
        }
    }
    

    /**
     * Generate the description
     * 
     * @param props     the properties extracted from the file
     * @return          the description
     */
    private String getDescription(Map<QName, Serializable> props)
    {
        StringBuilder result = new StringBuilder();
        if (props.get(PROP_SONG_TITLE) != null && props.get(PROP_ARTIST) != null && props.get(PROP_ALBUM_TITLE) != null)
        {
            result
                .append(props.get(PROP_SONG_TITLE))
                .append(" - ")
                .append(props.get(PROP_ALBUM_TITLE))
                .append(" (")
                .append(props.get(PROP_ARTIST))
                .append(")");
                
        }
        
        return result.toString();
    }

    /**
     * 
     * @param props
     * @param propQName
     * @param propvalue
     */
    private void setTagValue(Map<QName, Serializable> props, QName propQName, String propvalue)
    {
        if (propvalue != null && propvalue.length() != 0)
        {
            trimPut(propQName, propvalue, props);
        }       
    }

    /**
     * 
     * @param lyrics3Tag
     * @param name
     * @return
     */
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
     * 
     * @param id3v2
     * @param name
     * @return
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
