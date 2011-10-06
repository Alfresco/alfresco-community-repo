/*
 * Copyright (C) 2005 Jesper Steen Møller
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
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

/**
 * Test for the audio metadata extraction.
 */
public class TikaAudioMetadataExtracterTest extends AbstractMetadataExtracterTest
{
    private TikaAudioMetadataExtracter extracter;
    private static final String ARTIST = "Hauskaz";
    private static final String ALBUM  = "About a dog and a fox";
    private static final String GENRE  = "Foxtrot";

    @Override
    public void setUp() throws Exception
    {
        super.setUp();
        extracter = (TikaAudioMetadataExtracter)ctx.getBean("extracter.Audio");
        extracter.register();
    }

    /**
     * @return Returns the same transformer regardless - it is allowed
     */
    protected MetadataExtracter getExtracter()
    {
        return extracter;
    }

    public void testSupports() throws Exception
    {
        for (String mimetype : TikaAudioMetadataExtracter.SUPPORTED_MIMETYPES)
        {
            boolean supports = extracter.isSupported(mimetype);
            assertTrue("Mimetype should be supported: " + mimetype, supports);
        }
    }

    public void testOggExtraction() throws Exception
    {
        testExtractFromMimetype(MimetypeMap.MIMETYPE_VORBIS);
    }
    public void testFlacExtraction() throws Exception
    {
        testExtractFromMimetype(MimetypeMap.MIMETYPE_FLAC);
    }

    /**
     * We don't have quite the usual metadata. Tests the descriptions one.
     * Other tests in {@link #testFileSpecificMetadata(String, Map)}
     */
    protected void testCommonMetadata(String mimetype, Map<QName, Serializable> properties) 
    {
       // Title is as normal
       assertEquals(
             "Property " + ContentModel.PROP_TITLE + " not found for mimetype " + mimetype,
             QUICK_TITLE,
             DefaultTypeConverter.INSTANCE.convert(String.class, properties.get(ContentModel.PROP_TITLE)));
       // Has Author, not Creator, and is different
       assertEquals(
             "Property " + ContentModel.PROP_AUTHOR + " not found for mimetype " + mimetype,
             "Hauskaz",
             DefaultTypeConverter.INSTANCE.convert(String.class, properties.get(ContentModel.PROP_AUTHOR)));
       
       // Description is a composite
       assertContains(
             "Property " + ContentModel.PROP_DESCRIPTION + " didn't contain " +  QUICK_TITLE + " for mimetype " + mimetype,
             QUICK_TITLE,
             DefaultTypeConverter.INSTANCE.convert(String.class, properties.get(ContentModel.PROP_DESCRIPTION)));
       // Check rest of it later
    }

   /** 
    * Tests for various Audio specific bits of metadata 
    */
    public void testFileSpecificMetadata(String mimetype, Map<QName, Serializable> properties) {
       QName album = QName.createQName(NamespaceService.AUDIO_MODEL_1_0_URI, "album");
       assertEquals(
             "Property " + album + " not found for mimetype " + mimetype,
             ALBUM,
             DefaultTypeConverter.INSTANCE.convert(String.class, properties.get(album)));
       
       QName artist = QName.createQName(NamespaceService.AUDIO_MODEL_1_0_URI, "artist");
       assertEquals(
             "Property " + artist + " not found for mimetype " + mimetype,
             ARTIST,
             DefaultTypeConverter.INSTANCE.convert(String.class, properties.get(artist)));
       
       QName genre = QName.createQName(NamespaceService.AUDIO_MODEL_1_0_URI, "genre");
       assertEquals(
             "Property " + genre + " not found for mimetype " + mimetype,
             GENRE,
             DefaultTypeConverter.INSTANCE.convert(String.class, properties.get(genre)));

       QName releaseDate = QName.createQName(NamespaceService.AUDIO_MODEL_1_0_URI, "releaseDate");
       assertEquals(
             "Property " + releaseDate + " not found for mimetype " + mimetype,
             "2009-01-01T00:00:00.000Z",
             DefaultTypeConverter.INSTANCE.convert(String.class, properties.get(releaseDate)));

       QName channels = QName.createQName(NamespaceService.AUDIO_MODEL_1_0_URI, "channelType");
       assertEquals(
             "Property " + channels + " not found for mimetype " + mimetype,
             "Stereo",
             DefaultTypeConverter.INSTANCE.convert(String.class, properties.get(channels)));

       
       // Description is a composite - check the artist part
       assertContains(
             "Property " + ContentModel.PROP_DESCRIPTION + " didn't contain " +  ARTIST + " for mimetype " + mimetype,
             ARTIST,
             DefaultTypeConverter.INSTANCE.convert(String.class, properties.get(ContentModel.PROP_DESCRIPTION)));
    }
}
