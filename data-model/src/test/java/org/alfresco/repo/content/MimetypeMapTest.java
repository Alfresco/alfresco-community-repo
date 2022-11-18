/*
 * #%L
 * Alfresco Data model classes
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
package org.alfresco.repo.content;

import junit.framework.TestCase;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentStreamListener;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.util.DataModelTestApplicationContextHelper;
import org.springframework.context.ApplicationContext;
import org.springframework.extensions.config.ConfigDeployment;
import org.springframework.extensions.config.ConfigService;
import org.springframework.extensions.config.ConfigSource;
import org.springframework.extensions.config.xml.XMLConfigService;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * @see org.alfresco.repo.content.MimetypeMap
 * @see org.alfresco.repo.content.MimetypeMapContentTest
 * 
 * @author Derek Hulley
 */
public class MimetypeMapTest extends TestCase
{
    private static ApplicationContext ctx = DataModelTestApplicationContextHelper.getApplicationContext();
    
    private MimetypeService mimetypeService;
    private ConfigService configService;
    
    @Override
    public void setUp() throws Exception
    {
        mimetypeService =  (MimetypeService)ctx.getBean("mimetypeService");
        configService = ((MimetypeMap)mimetypeService).getConfigService();
    }
    
    @Override
    protected void tearDown() throws Exception
    {
        ((MimetypeMap)mimetypeService).setConfigService(configService);
        ((MimetypeMap)mimetypeService).setMimetypeJsonConfigDir(null);
        ((MimetypeMap)mimetypeService).init();
    }

    public void testExtensions() throws Exception
    {
        Map<String, String> extensionsByMimetype = mimetypeService.getExtensionsByMimetype();
        Map<String, String> mimetypesByExtension = mimetypeService.getMimetypesByExtension();
        
        // plain text
        assertEquals("txt", extensionsByMimetype.get("text/plain"));
        assertEquals("text/plain", mimetypesByExtension.get("txt"));
        assertEquals("text/plain", mimetypesByExtension.get("ftl"));
        
        // other text forms
        assertEquals("text/csv", mimetypesByExtension.get("csv"));
        assertEquals("text/html", mimetypesByExtension.get("html"));
        assertEquals("image/icns", mimetypesByExtension.get("icns"));

        // JPEG
        assertEquals("jpg", extensionsByMimetype.get("image/jpeg"));
        assertEquals("image/jpeg", mimetypesByExtension.get("jpg"));
        assertEquals("image/jpeg", mimetypesByExtension.get("jpeg"));
        assertEquals("image/jpeg", mimetypesByExtension.get("jpe"));
        
        // MS Word
        assertEquals("doc", extensionsByMimetype.get("application/msword"));
        assertEquals("application/msword", mimetypesByExtension.get("doc"));
        
        // Star Office
        assertEquals("sds", extensionsByMimetype.get("application/vnd.stardivision.chart"));
    }
    
    public void testIsText() throws Exception
    {
        assertTrue(mimetypeService.isText(MimetypeMap.MIMETYPE_HTML));
    }
    
    public void testGetContentCharsetFinder() throws Exception
    {
        assertNotNull("No charset finder", mimetypeService.getContentCharsetFinder());
    }

    public void testMimetypeFromExtension() throws Exception
    {
        // test known mimetype
        assertEquals("application/msword", mimetypeService.getMimetype("doc"));
        // test case insensitivity
        assertEquals("application/msword", mimetypeService.getMimetype("DOC"));
        
        // test fallback for unknown and missing
        assertEquals(MimetypeMap.MIMETYPE_BINARY, mimetypeService.getMimetype(null));
        assertEquals(MimetypeMap.MIMETYPE_BINARY, mimetypeService.getMimetype("unknownext"));
    }
 
    /**
     * Tests guessing the mimetype from a filename.
     * 
     * Note - The test for checking by filename + content are in the repo project
     * @see org.alfresco.repo.content.MimetypeMapContentTest
     */
    public void testGuessMimetypeForFilename() throws Exception
    {
        assertEquals("application/msword", mimetypeService.guessMimetype("something.doc"));
        assertEquals("application/msword", mimetypeService.guessMimetype("SOMETHING.DOC"));
        assertEquals(MimetypeMap.MIMETYPE_BINARY, mimetypeService.guessMimetype("noextension"));
        assertEquals(MimetypeMap.MIMETYPE_BINARY, mimetypeService.guessMimetype("file.unknownext"));
        
        // Without a content reader, the behaviour is the same
        assertEquals("application/msword", mimetypeService.guessMimetype("something.doc", (ContentReader)null));
        assertEquals("application/msword", mimetypeService.guessMimetype("SOMETHING.DOC", (ContentReader)null));
        assertEquals(MimetypeMap.MIMETYPE_BINARY, mimetypeService.guessMimetype("noextension", (ContentReader)null));
        assertEquals(MimetypeMap.MIMETYPE_BINARY, mimetypeService.guessMimetype("file.unknownext", (ContentReader)null));
    }
    
    private static final String MIMETYPE_1A =
        "      <mimetype mimetype=\"mimetype1\" display=\"Mimetype ONE\">" +
        "        <extension display=\"Extension ONE\">ext1a</extension>" +
        "      </mimetype>";
    private static final String MIMETYPE_1B =
        "      <mimetype mimetype=\"mimetype1\" display=\"Mimetype ONE\">" +
        "        <extension display=\"Extension 1\">ext1a</extension>" +
        "      </mimetype>";
    private static final String MIMETYPE_1C =
        "      <mimetype mimetype=\"mimetype1\" display=\"Mimetype ONE\">" +
        "        <extension>ext1a</extension>" +
        "      </mimetype>";
    private static final String MIMETYPE_1D =
        "      <mimetype mimetype=\"mimetype1\" display=\"Mimetype 1\">" +
        "        <extension>ext1a</extension>" +
        "      </mimetype>";
    private static final String MIMETYPE_1E =
        "      <mimetype mimetype=\"mimetype1\" text=\"true\">" +
        "        <extension>ext1a</extension>" +
        "      </mimetype>";
    private static final String MIMETYPE_1F =
        "      <mimetype mimetype=\"mimetype1\" text=\"true\">" +
        "        <extension>ext1a</extension>" +
        "        <extension default=\"true\">ext1b</extension>" +
        "        <extension>ext1c</extension>" +
        "      </mimetype>";
    private static final String MIMETYPE_1G =
        "      <mimetype mimetype=\"mimetype1\" text=\"true\">" +
        "        <extension>ext1c</extension>" +
        "        <extension>ext1b</extension>" +
        "        <extension>ext1a</extension>" +
        "      </mimetype>";
    
    private static final String MIMETYPE_2A =
        "      <mimetype mimetype=\"mimetype2\" display=\"Mimetype TWO\" text=\"true\">" +
        "        <extension>ext2a</extension>" +
        "      </mimetype>";
    private static final String MIMETYPE_2B =
        "      <mimetype mimetype=\"mimetype2\" display=\"Mimetype TWO\">" +
        "        <extension>ext2a</extension>" +
        "      </mimetype>";

    private static final String MIMETYPE_3A =
        "      <mimetype mimetype=\"mimetype3\" display=\"Mimetype THREE\">" +
        "        <extension>ext3a</extension>" +
        "        <extension>ext3b</extension>" +
        "      </mimetype>";

    public void testNoDuplicates() throws Exception
    {
        setConfigService(
                MIMETYPE_1A+
                MIMETYPE_2A+
                MIMETYPE_3A);
        ((MimetypeMap)mimetypeService).init();
        
        assertFalse("mimetype1 should not be text", mimetypeService.isText("mimetype1"));
        assertEquals("ext1a", mimetypeService.getExtension("mimetype1"));
        assertEquals("mimetype1", mimetypeService.getMimetype("ext1a"));
        assertEquals("Mimetype ONE", mimetypeService.getDisplaysByMimetype().get("mimetype1"));
        assertEquals("Extension ONE", mimetypeService.getDisplaysByExtension().get("ext1a"));

        assertTrue("mimetype2 should be text", mimetypeService.isText("mimetype2"));
        assertEquals("mimetype2", mimetypeService.getMimetype("ext2a"));

        assertEquals("mimetype3", mimetypeService.getMimetype("ext3a"));
    }

    public void testTypes() throws Exception
    {
        Collection<String> types = mimetypeService.getMimetypes(null);
        assertNotNull(types);
        types = mimetypeService.getMimetypes("txt");
        assertNotNull(types);

        assertNull(mimetypeService.getMimetypeIfNotMatches(new DummyContentReader()));
    }

    public void testMisc() throws Exception
    {
        MimetypeMap m = new MimetypeMap(null);
        ContentReader reader = new DummyContentReader(MimetypeMap.MIMETYPE_VIDEO_QUICKTIME);
        assertEquals(MimetypeMap.MIMETYPE_TEXT_PLAIN, mimetypeService.getMimetypeIfNotMatches(reader));
        assertEquals(MimetypeMap.MIMETYPE_TEXT_PLAIN, mimetypeService.guessMimetype("file.rm", reader.getContentInputStream()));
        assertEquals(MimetypeMap.MIMETYPE_VIDEO_QUICKTIME, mimetypeService.guessMimetype("file.rm", reader));
    }

    public void testTypeBasedOnDetectedTypeAndExtension() throws Exception
    {
        ContentReader reader = new DummyContentReader(MimetypeMap.MIMETYPE_PDF, "%PDF\r");
        assertEquals(MimetypeMap.MIMETYPE_APPLICATION_ILLUSTRATOR, mimetypeService.guessMimetype("file.ai",  reader.getContentInputStream()));
        assertEquals(MimetypeMap.MIMETYPE_PDF,                     mimetypeService.guessMimetype("file.pdf", reader.getContentInputStream()));

        reader = new DummyContentReader(MimetypeMap.MIMETYPE_APPLICATION_PS, "%!PS");
        assertEquals(MimetypeMap.MIMETYPE_APPLICATION_EPS, mimetypeService.guessMimetype("file.eps", reader.getContentInputStream()));
        assertEquals(MimetypeMap.MIMETYPE_APPLICATION_PS,  mimetypeService.guessMimetype("file.ps",  reader.getContentInputStream()));
    }

    public void testDuplicates() throws Exception
    {
        setConfigService(
                MIMETYPE_1A+MIMETYPE_1B+MIMETYPE_1C+MIMETYPE_1D+MIMETYPE_1E+MIMETYPE_1F+ // Change all values
                MIMETYPE_2A+MIMETYPE_2B+ // duplicate removes isText
                MIMETYPE_3A+MIMETYPE_3A); // identical
        ((MimetypeMap)mimetypeService).init();
        
        assertTrue("mimetype1 should have be reset to text", mimetypeService.isText("mimetype1"));
        assertEquals("ext1b", mimetypeService.getExtension("mimetype1"));
        assertEquals("mimetype1", mimetypeService.getMimetype("ext1a"));
        assertEquals("mimetype1", mimetypeService.getMimetype("ext1b"));
        assertEquals("mimetype1", mimetypeService.getMimetype("ext1c"));
        assertEquals("Mimetype 1", mimetypeService.getDisplaysByMimetype().get("mimetype1"));
        assertEquals("Extension 1", mimetypeService.getDisplaysByExtension().get("ext1a"));

        assertFalse("mimetype2 should have be reset to not text", mimetypeService.isText("mimetype2"));
        assertEquals("mimetype2", mimetypeService.getMimetype("ext2a"));

        assertEquals("mimetype3", mimetypeService.getMimetype("ext3a"));
    }

    private void setConfigService(final String mimetypes)
    {
        ConfigSource configSource = new ConfigSource()
        {
            @Override
            public List<ConfigDeployment> getConfigDeployments()
            {
                String xml =
                    "<alfresco-config area=\"mimetype-map\">" +
                    "  <config evaluator=\"string-compare\" condition=\"Mimetype Map\">" +
                    "    <mimetypes>" +
                           mimetypes +
                    "    </mimetypes>" +
                    "  </config>" +
                    "</alfresco-config>";
                List<ConfigDeployment> configs = new ArrayList<ConfigDeployment>();
                configs.add(new ConfigDeployment("name", new ByteArrayInputStream(xml.getBytes())));
                return configs;
            }
        };

        ConfigService configService = new XMLConfigService(configSource);
        ((XMLConfigService) configService).initConfig();
        ((MimetypeMap)mimetypeService).setConfigService(configService);
    }

    public static class DummyContentReader implements ContentReader
    {
        private String mimetype;
        private String content;

        public DummyContentReader()
        {
            this(MimetypeMap.MIMETYPE_HTML);
        }

        public DummyContentReader(String mimetype)
        {
            this(mimetype, "<X>@@/Y");
        }

        public DummyContentReader(String mimetype, String content)
        {
            this.mimetype = mimetype;
            this.content = content;
        }

        @Override
        public ContentReader getReader() throws ContentIOException
        {
            return this;
        }

        @Override
        public boolean exists()
        {
            return false;
        }

        @Override
        public long getLastModified()
        {
            return 0;
        }

        @Override
        public boolean isClosed()
        {
            return false;
        }

        @Override
        public ReadableByteChannel getReadableChannel() throws ContentIOException
        {
            return null;
        }

        @Override
        public FileChannel getFileChannel() throws ContentIOException
        {
            return null;
        }

        @Override
        public InputStream getContentInputStream() throws ContentIOException
        {
            return new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        }

        @Override
        public void getContent(OutputStream os) throws ContentIOException
        {

        }

        @Override
        public void getContent(File file) throws ContentIOException
        {

        }

        @Override
        public String getContentString() throws ContentIOException
        {
            return null;
        }

        @Override
        public String getContentString(int length) throws ContentIOException
        {
            return null;
        }

        @Override
        public boolean isChannelOpen()
        {
            return false;
        }

        @Override
        public void addListener(ContentStreamListener listener)
        {

        }

        @Override
        public long getSize()
        {
            return 5l;
        }

        @Override
        public ContentData getContentData()
        {
            return null;
        }

        @Override
        public String getContentUrl()
        {
            return null;
        }

        @Override
        public String getMimetype()
        {
            return mimetype;
        }

        @Override
        public void setMimetype(String mimetype)
        {

        }

        @Override
        public String getEncoding()
        {
            return StandardCharsets.UTF_8.toString();
        }

        @Override
        public void setEncoding(String encoding)
        {

        }

        @Override
        public Locale getLocale()
        {
            return null;
        }

        @Override
        public void setLocale(Locale locale)
        {

        }
    }

    public static class DummyContentWriter implements ContentWriter
    {
        private String mimetype = MimetypeMap.MIMETYPE_AUDIO_MP4;

        public DummyContentWriter()
        {
            super();
        }

        public DummyContentWriter(String mimetype)
        {
            this.mimetype = mimetype;
        }

        @Override
        public ContentReader getReader() throws ContentIOException
        {
            return new DummyContentReader();
        }

        @Override
        public boolean isClosed()
        {
            return false;
        }

        @Override
        public WritableByteChannel getWritableChannel() throws ContentIOException
        {
            return null;
        }

        @Override
        public FileChannel getFileChannel(boolean truncate) throws ContentIOException
        {
            return null;
        }

        @Override
        public OutputStream getContentOutputStream() throws ContentIOException
        {
            return null;
        }

        @Override
        public void putContent(ContentReader reader) throws ContentIOException
        {

        }

        @Override
        public void putContent(InputStream is) throws ContentIOException
        {

        }

        @Override
        public void putContent(File file) throws ContentIOException
        {

        }

        @Override
        public void putContent(String content) throws ContentIOException
        {

        }

        @Override
        public void guessMimetype(String filename)
        {

        }

        @Override
        public void guessEncoding()
        {

        }

        @Override
        public boolean isChannelOpen()
        {
            return false;
        }

        @Override
        public void addListener(ContentStreamListener listener)
        {

        }

        @Override
        public long getSize()
        {
            return 5l;
        }

        @Override
        public ContentData getContentData()
        {
            return null;
        }

        @Override
        public String getContentUrl()
        {
            return null;
        }

        @Override
        public String getMimetype()
        {
            return mimetype;
        }

        @Override
        public void setMimetype(String mimetype)
        {

        }

        @Override
        public String getEncoding()
        {
            return StandardCharsets.UTF_8.toString();
        }

        @Override
        public void setEncoding(String encoding)
        {

        }

        @Override
        public Locale getLocale()
        {
            return new Locale("es");
        }

        @Override
        public void setLocale(Locale locale)
        {

        }
    }

    public void testJsonRead() throws Exception
    {
        int beforeCount = mimetypeService.getMimetypes().size();

        // {
        //  "mediaTypes": [
        //    {
        //      "name": "Test MPEG4 Audio",
        //      "mediaType": "test audio/mp4",
        //      "extensions": [
        //        {"extension": "test m4a"}
        //      ]
        //    },
        //    {
        //      "name": "Test Plain Text",
        //      "mediaType": "test text/plain",
        //      "text": true,
        //      "extensions": [
        //        {"extension": "test txt", "default": true},
        //        {"extension": "test sql", "name": "SQL"},
        //        {"extension": "test properties", "name": "Java Properties"},
        //        {"extension": "test log", "name": "Log File"}
        //      ]
        //    }
        //  ]
        //}
        ((MimetypeMap)mimetypeService).setMimetypeJsonConfigDir("alfresco/test/mimetypes/testMimetype.json");
        ((MimetypeMap) mimetypeService).init();
        Map<String, String> displaysByExtension = mimetypeService.getDisplaysByExtension();

        int afterCount = mimetypeService.getMimetypes().size();
        assertEquals("There should be 2 more mimetypes from the JSON file", beforeCount+2, afterCount);

        String mimetype = "test audio/mp4";
        String defaultExtension = "test m4a";
        assertEquals("Test MPEG4 Audio", displaysByExtension.get(defaultExtension));
        assertEquals(defaultExtension, mimetypeService.getExtension(mimetype));
        assertEquals(mimetype, mimetypeService.getMimetype(defaultExtension));
        assertFalse(mimetypeService.isText(mimetype));

        mimetype = "test text/plain";
        defaultExtension = "test txt";
        assertEquals("Test Plain Text", displaysByExtension.get(defaultExtension));
        assertEquals(defaultExtension, mimetypeService.getExtension(mimetype));
        assertEquals(mimetype, mimetypeService.getMimetype(defaultExtension));
        assertEquals(mimetype, mimetypeService.getMimetype("test sql"));
        assertEquals(mimetype, mimetypeService.getMimetype("test properties"));
        assertEquals(mimetype, mimetypeService.getMimetype("test log"));
        assertTrue(mimetypeService.isText(mimetype));
    }
}
