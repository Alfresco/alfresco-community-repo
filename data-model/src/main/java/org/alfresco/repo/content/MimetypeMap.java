/*
 * #%L
 * Alfresco Data model classes
 * %%
 * Copyright (C) 2005 - 2021 Alfresco Software Limited
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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.alfresco.repo.content.encoding.ContentCharsetFinder;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.FileContentReader;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.util.ConfigFileFinder;
import org.alfresco.util.ConfigScheduler;
import org.alfresco.util.PropertyCheck;
import org.alfresco.util.ShutdownIndicator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.detect.DefaultDetector;
import org.apache.tika.detect.Detector;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.TikaCoreProperties;
import org.apache.tika.mime.MediaType;
import org.quartz.CronExpression;
import org.springframework.extensions.config.Config;
import org.springframework.extensions.config.ConfigElement;
import org.springframework.extensions.config.ConfigLookupContext;
import org.springframework.extensions.config.ConfigService;
import org.springframework.extensions.config.element.GenericConfigElement;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;

/**
 * Provides a bidirectional mapping between well-known mimetypes and the
 * registered file extensions. All mimetypes and extensions are stored and
 * handled as lowercase.
 * 
 * @author Derek Hulley
 */
public class MimetypeMap implements MimetypeService
{
    public static final String PREFIX_APPLICATION = "application/";
    
    public static final String PREFIX_AUDIO = "audio/";
    
    public static final String PREFIX_IMAGE = "image/";
    
    public static final String PREFIX_MESSAGE = "message/";
    
    public static final String PREFIX_MODEL = "model/";
    
    public static final String PREFIX_MULTIPART = "multipart/";
    
    public static final String PREFIX_TEXT = "text/";
    
    public static final String PREFIX_VIDEO = "video/";

    public static final String EXTENSION_BINARY = "bin";
    
    public static final String MACOS_RESOURCE_FORK_FILE_NAME_PREFIX = "._";

    public static final String MIMETYPE_MULTIPART_ALTERNATIVE = "multipart/alternative";

    public static final String MIMETYPE_TEXT_PLAIN = "text/plain";

    public static final String MIMETYPE_TEXT_MEDIAWIKI = "text/mediawiki";

    public static final String MIMETYPE_TEXT_CSS = "text/css";

    public static final String MIMETYPE_TEXT_CSV = "text/csv";

    public static final String MIMETYPE_TEXT_JAVASCRIPT = "text/javascript";

    public static final String MIMETYPE_XML = "text/xml";

    public static final String MIMETYPE_HTML = "text/html";

    public static final String MIMETYPE_XHTML = "application/xhtml+xml";

    public static final String MIMETYPE_PDF = "application/pdf";

    public static final String MIMETYPE_JSON = "application/json";

    public static final String MIMETYPE_WORD = "application/msword";

    public static final String MIMETYPE_EXCEL = "application/vnd.ms-excel";

    public static final String MIMETYPE_BINARY = "application/octet-stream";

    public static final String MIMETYPE_PPT = "application/vnd.ms-powerpoint";

    public static final String MIMETYPE_APP_DWG = "application/dwg";

    public static final String MIMETYPE_IMG_DWG = "image/vnd.dwg";

    public static final String MIMETYPE_VIDEO_AVI = "video/x-msvideo";

    public static final String MIMETYPE_VIDEO_QUICKTIME = "video/quicktime";

    public static final String MIMETYPE_VIDEO_WMV = "video/x-ms-wmv";

    public static final String MIMETYPE_VIDEO_3GP = "video/3gpp";

    public static final String MIMETYPE_VIDEO_3GP2 = "video/3gpp2";

    public static final String MIMETYPE_DITA = "application/dita+xml";

    // Flash
    public static final String MIMETYPE_FLASH = "application/x-shockwave-flash";

    public static final String MIMETYPE_VIDEO_FLV = "video/x-flv";

    public static final String MIMETYPE_APPLICATION_FLA = "application/x-fla";

    public static final String MIMETYPE_VIDEO_MPG = "video/mpeg";

    public static final String MIMETYPE_VIDEO_MP4 = "video/mp4";

    public static final String MIMETYPE_IMAGE_GIF = "image/gif";

    public static final String MIMETYPE_IMAGE_JPEG = "image/jpeg";

    public static final String MIMETYPE_IMAGE_RGB = "image/x-rgb";

    public static final String MIMETYPE_IMAGE_SVG = "image/svg+xml";

    public static final String MIMETYPE_IMAGE_PNG = "image/png";

    public static final String MIMETYPE_IMAGE_TIFF = "image/tiff";

    public static final String MIMETYPE_IMAGE_RAW_DNG = "image/x-raw-adobe";

    public static final String MIMETYPE_IMAGE_RAW_3FR = "image/x-raw-hasselblad";

    public static final String MIMETYPE_IMAGE_RAW_RAF = "image/x-raw-fuji";

    public static final String MIMETYPE_IMAGE_RAW_CR2 = "image/x-raw-canon";

    public static final String MIMETYPE_IMAGE_RAW_K25 = "image/x-raw-kodak";

    public static final String MIMETYPE_IMAGE_RAW_MRW = "image/x-raw-minolta";

    public static final String MIMETYPE_IMAGE_RAW_NEF = "image/x-raw-nikon";

    public static final String MIMETYPE_IMAGE_RAW_ORF = "image/x-raw-olympus";

    public static final String MIMETYPE_IMAGE_RAW_PEF = "image/x-raw-pentax";

    public static final String MIMETYPE_IMAGE_RAW_ARW = "image/x-raw-sony";

    public static final String MIMETYPE_IMAGE_RAW_X3F = "image/x-raw-sigma";

    public static final String MIMETYPE_IMAGE_RAW_RW2 = "image/x-raw-panasonic";

    public static final String MIMETYPE_IMAGE_RAW_RWL = "image/x-raw-leica";

    public static final String MIMETYPE_IMAGE_RAW_R3D = "image/x-raw-red";

    public static final String MIMETYPE_IMAGE_DWT = "image/x-dwt";

    public static final String MIMETYPE_IMAGE_ICNS = "image/icns";

    public static final String MIMETYPE_APPLICATION_EPS = "application/eps";

    public static final String MIMETYPE_APPLICATION_PS = "application/postscript";

    public static final String MIMETYPE_JAVASCRIPT = "application/x-javascript";

    public static final String MIMETYPE_ZIP = "application/zip";

    public static final String MIMETYPE_OPENSEARCH_DESCRIPTION = "application/opensearchdescription+xml";

    public static final String MIMETYPE_ATOM = "application/atom+xml";

    public static final String MIMETYPE_RSS = "application/rss+xml";

    public static final String MIMETYPE_RFC822 = "message/rfc822";

    public static final String MIMETYPE_OUTLOOK_MSG = "application/vnd.ms-outlook";

    public static final String MIMETYPE_VISIO = "application/vnd.visio";

    public static final String MIMETYPE_VISIO_2013 = "application/vnd.visio2013";

    // Adobe
    public static final String MIMETYPE_APPLICATION_ILLUSTRATOR = "application/illustrator";

    public static final String MIMETYPE_APPLICATION_PHOTOSHOP = "image/vnd.adobe.photoshop";
    
    //Encrypted office document
    public static final String MIMETYPE_ENCRYPTED_OFFICE = "application/x-tika-ooxml-protected";

    // Open Document
    public static final String MIMETYPE_OPENDOCUMENT_TEXT = "application/vnd.oasis.opendocument.text";

    public static final String MIMETYPE_OPENDOCUMENT_TEXT_TEMPLATE = "application/vnd.oasis.opendocument.text-template";

    public static final String MIMETYPE_OPENDOCUMENT_GRAPHICS = "application/vnd.oasis.opendocument.graphics";

    public static final String MIMETYPE_OPENDOCUMENT_GRAPHICS_TEMPLATE = "application/vnd.oasis.opendocument.graphics-template";

    public static final String MIMETYPE_OPENDOCUMENT_PRESENTATION = "application/vnd.oasis.opendocument.presentation";

    public static final String MIMETYPE_OPENDOCUMENT_PRESENTATION_TEMPLATE = "application/vnd.oasis.opendocument.presentation-template";

    public static final String MIMETYPE_OPENDOCUMENT_SPREADSHEET = "application/vnd.oasis.opendocument.spreadsheet";

    public static final String MIMETYPE_OPENDOCUMENT_SPREADSHEET_TEMPLATE = "application/vnd.oasis.opendocument.spreadsheet-template";

    public static final String MIMETYPE_OPENDOCUMENT_CHART = "application/vnd.oasis.opendocument.chart";

    public static final String MIMETYPE_OPENDOCUMENT_CHART_TEMPLATE = "applicationvnd.oasis.opendocument.chart-template";

    public static final String MIMETYPE_OPENDOCUMENT_IMAGE = "application/vnd.oasis.opendocument.image";

    public static final String MIMETYPE_OPENDOCUMENT_IMAGE_TEMPLATE = "applicationvnd.oasis.opendocument.image-template";

    public static final String MIMETYPE_OPENDOCUMENT_FORMULA = "application/vnd.oasis.opendocument.formula";

    public static final String MIMETYPE_OPENDOCUMENT_FORMULA_TEMPLATE = "applicationvnd.oasis.opendocument.formula-template";

    public static final String MIMETYPE_OPENDOCUMENT_TEXT_MASTER = "application/vnd.oasis.opendocument.text-master";

    public static final String MIMETYPE_OPENDOCUMENT_TEXT_WEB = "application/vnd.oasis.opendocument.text-web";

    public static final String MIMETYPE_OPENDOCUMENT_DATABASE = "application/vnd.oasis.opendocument.database";

    // Open Office
    public static final String MIMETYPE_OPENOFFICE1_WRITER = "application/vnd.sun.xml.writer";

    public static final String MIMETYPE_OPENOFFICE1_CALC = "application/vnd.sun.xml.calc";

    public static final String MIMETYPE_OPENOFFICE1_DRAW = "application/vnd.sun.xml.draw";

    public static final String MIMETYPE_OPENOFFICE1_IMPRESS = "application/vnd.sun.xml.impress";

    // Open XML
    public static final String MIMETYPE_OPENXML_WORDPROCESSING = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
    public static final String MIMETYPE_OPENXML_WORDPROCESSING_MACRO = "application/vnd.ms-word.document.macroenabled.12";
    public static final String MIMETYPE_OPENXML_WORD_TEMPLATE = "application/vnd.openxmlformats-officedocument.wordprocessingml.template";
    public static final String MIMETYPE_OPENXML_WORD_TEMPLATE_MACRO = "application/vnd.ms-word.template.macroenabled.12";
    public static final String MIMETYPE_OPENXML_SPREADSHEET = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    public static final String MIMETYPE_OPENXML_SPREADSHEET_TEMPLATE = "application/vnd.openxmlformats-officedocument.spreadsheetml.template";
    public static final String MIMETYPE_OPENXML_SPREADSHEET_MACRO = "application/vnd.ms-excel.sheet.macroenabled.12";
    public static final String MIMETYPE_OPENXML_SPREADSHEET_TEMPLATE_MACRO = "application/vnd.ms-excel.template.macroenabled.12";
    public static final String MIMETYPE_OPENXML_SPREADSHEET_ADDIN_MACRO = "application/vnd.ms-excel.addin.macroenabled.12";
    public static final String MIMETYPE_OPENXML_SPREADSHEET_BINARY_MACRO = "application/vnd.ms-excel.sheet.binary.macroenabled.12";
    public static final String MIMETYPE_OPENXML_PRESENTATION = "application/vnd.openxmlformats-officedocument.presentationml.presentation";
    public static final String MIMETYPE_OPENXML_PRESENTATION_MACRO = "application/vnd.ms-powerpoint.presentation.macroenabled.12";
    public static final String MIMETYPE_OPENXML_PRESENTATION_SLIDESHOW = "application/vnd.openxmlformats-officedocument.presentationml.slideshow";
    public static final String MIMETYPE_OPENXML_PRESENTATION_SLIDESHOW_MACRO = "application/vnd.ms-powerpoint.slideshow.macroenabled.12";
    public static final String MIMETYPE_OPENXML_PRESENTATION_TEMPLATE = "application/vnd.openxmlformats-officedocument.presentationml.template";
    public static final String MIMETYPE_OPENXML_PRESENTATION_TEMPLATE_MACRO = "application/vnd.ms-powerpoint.template.macroenabled.12";
    public static final String MIMETYPE_OPENXML_PRESENTATION_ADDIN = "application/vnd.ms-powerpoint.addin.macroenabled.12";
    public static final String MIMETYPE_OPENXML_PRESENTATION_SLIDE = "application/vnd.openxmlformats-officedocument.presentationml.slide";
    public static final String MIMETYPE_OPENXML_PRESENTATION_SLIDE_MACRO = "application/vnd.ms-powerpoint.slide.macroenabled.12";
    // Star Office
    public static final String MIMETYPE_STAROFFICE5_DRAW = "application/vnd.stardivision.draw";

    public static final String MIMETYPE_STAROFFICE5_CALC = "application/vnd.stardivision.calc";

    public static final String MIMETYPE_STAROFFICE5_IMPRESS = "application/vnd.stardivision.impress";

    public static final String MIMETYPE_STAROFFICE5_IMPRESS_PACKED = "application/vnd.stardivision.impress-packed";

    public static final String MIMETYPE_STAROFFICE5_CHART = "application/vnd.stardivision.chart";

    public static final String MIMETYPE_STAROFFICE5_WRITER = "application/vnd.stardivision.writer";

    public static final String MIMETYPE_STAROFFICE5_WRITER_GLOBAL = "application/vnd.stardivision.writer-global";

    public static final String MIMETYPE_STAROFFICE5_MATH = "application/vnd.stardivision.math";

    // Apple iWorks
    public static final String MIMETYPE_IWORK_KEYNOTE = "application/vnd.apple.keynote";

    public static final String MIMETYPE_IWORK_NUMBERS = "application/vnd.apple.numbers";

    public static final String MIMETYPE_IWORK_PAGES = "application/vnd.apple.pages";

    //MACOS
    public static final String MIMETYPE_APPLEFILE = "application/applefile";

    // WordPerfect
    public static final String MIMETYPE_WORDPERFECT = "application/wordperfect";

    // Audio
    public static final String MIMETYPE_MP3 = "audio/mpeg";

    public static final String MIMETYPE_AUDIO_MP4 = "audio/mp4";

    public static final String MIMETYPE_VORBIS = "audio/vorbis";

    public static final String MIMETYPE_FLAC = "audio/x-flac";

    // Alfresco
    public static final String MIMETYPE_ACP = "application/acp";

    private static final String CONFIG_AREA = "mimetype-map";

    private static final String CONFIG_CONDITION = "Mimetype Map";

    private static final String ELEMENT_MIMETYPES = "mimetypes";

    private static final String ATTR_MIMETYPE = "mimetype";

    private static final String ATTR_DISPLAY = "display";

    private static final String ATTR_EXTENSION = "extension";

    private static final String ATTR_DEFAULT = "default";

    private static final String ATTR_TEXT = "text";

    private static final Log logger = LogFactory.getLog(MimetypeMap.class);

    private ConfigService configService;

    private ContentCharsetFinder contentCharsetFinder;

    private TikaConfig tikaConfig;

    private Detector detector;

    private ObjectMapper jsonObjectMapper;

    private String mimetypeJsonConfigDir;

    private CronExpression cronExpression;

    private CronExpression initialAndOnErrorCronExpression;

    static class Data
    {
        private List<String> mimetypes = new ArrayList<String>(40);

        private Map<String, String> extensionsByMimetype = new HashMap<String, String>(59);

        private Map<String, String> mimetypesByExtension = new HashMap<String, String>(59);

        private Map<String, String> displaysByMimetype = new TreeMap<String, String>();

        private Map<String, String> displaysByExtension = new HashMap<String, String>(59);

        private Set<String> textMimetypes = new HashSet<String>(23);

        private int xmlCount;

        private int fileCount;

        private void makeCollectionsReadOnly()
        {
            mimetypes = Collections.unmodifiableList(mimetypes);
            extensionsByMimetype = Collections.unmodifiableMap(extensionsByMimetype);
            mimetypesByExtension = Collections.unmodifiableMap(mimetypesByExtension);
            displaysByMimetype = Collections.unmodifiableMap(displaysByMimetype);
            displaysByExtension = Collections.unmodifiableMap(displaysByExtension);
        }

        @Override
        public String toString()
        {
            int mimetypeCount = mimetypes.size();
            return "(mimetypes: "+mimetypeCount+" from XML: "+xmlCount+" from JSON: "+(mimetypeCount-xmlCount)+" files: "+fileCount+")";
        }
    }

    private static class MediaTypeDef
    {
        private String name;
        private String mediaType;
        private boolean text;
        private ExtensionDef[] extensions;

        public void setName(String name)
        {
            this.name = name;
        }

        public void setMediaType(String mediaType)
        {
            this.mediaType = mediaType;
        }

        public void setText(boolean text)
        {
            this.text = text;
        }

        public void setExtensions(ExtensionDef[] extensions)
        {
            this.extensions = extensions;
        }
    }

    private static class ExtensionDef
    {
        private String extension;
        private String name;
        private boolean isDefault;

        public void setExtension(String extension)
        {
            this.extension = extension;
        }

        public void setName(String name)
        {
            this.name = name;
        }

        public void setDefault(boolean theDefault)
        {
            this.isDefault = theDefault;
        }
    }

    private ConfigScheduler<Data> configScheduler = new ConfigScheduler(this)
    {
        @Override
        public boolean readConfig() throws IOException
        {
            if (jsonConfigFileFinder != null)
            {
                jsonConfigFileFinder.setFileCount(0);
            }
            return MimetypeMap.this.readConfig();
        }

        @Override
        public Object createData()
        {
            return MimetypeMap.this.createData();
        }
    };

    public Data createData()
    {
        return new Data();
    }

    public Data getData()
    {
        return configScheduler.getData();
    }

    private ConfigFileFinder jsonConfigFileFinder;

    /**
     * Default constructor
     * 
     * @since 2.1
     */
    public MimetypeMap()
    {
    }

    @Deprecated
    public MimetypeMap(ConfigService configService)
    {
        logger.warn("MimetypeMap(ConfigService configService) has been deprecated.  "
                + "Use the default constructor and property 'configService'");
        this.configService = configService;
    }

    public ConfigService getConfigService()
    {
        return configService;
    }

    /**
     * @param configService the config service to use to read mimetypes from
     */
    public void setConfigService(ConfigService configService)
    {
        this.configService = configService;
    }

    /**
     * {@inheritDoc}
     */
    public ContentCharsetFinder getContentCharsetFinder()
    {
        return contentCharsetFinder;
    }

    /**
     * Set the system default content characterset decoder
     */
    public void setContentCharsetFinder(ContentCharsetFinder contentCharsetFinder)
    {
        this.contentCharsetFinder = contentCharsetFinder;
    }

    /**
     * Injects the TikaConfig to use
     * 
     * @param tikaConfig The Tika Config to use
     */
    public void setTikaConfig(TikaConfig tikaConfig)
    {
        this.tikaConfig = tikaConfig;
    }

    public void setJsonObjectMapper(ObjectMapper jsonObjectMapper)
    {
        this.jsonObjectMapper = jsonObjectMapper;
    }

    public void setMimetypeJsonConfigDir(String mimetypeJsonConfigDir)
    {
        this.mimetypeJsonConfigDir = mimetypeJsonConfigDir;
    }

    public void setCronExpression(CronExpression cronExpression)
    {
        this.cronExpression = cronExpression;
    }

    public void setInitialAndOnErrorCronExpression(CronExpression initialAndOnErrorCronExpression)
    {
        this.initialAndOnErrorCronExpression = initialAndOnErrorCronExpression;
    }

    public void setShutdownIndicator(ShutdownIndicator shutdownIndicator)
    {
        configScheduler.setShutdownIndicator(shutdownIndicator);
    }

    /**
     * Initialises the map using the configuration service provided
     */
    public void init()
    {
        PropertyCheck.mandatory(this, "configService", configService);
        PropertyCheck.mandatory(this, "contentCharsetFinder", contentCharsetFinder);

        // Do we have any properties that indicate we will read JSON?
        if (mimetypeJsonConfigDir != null || jsonObjectMapper != null || cronExpression != null || initialAndOnErrorCronExpression != null)
        {
            PropertyCheck.mandatory(this, "jsonObjectMapper", jsonObjectMapper);
            // If we have a cronExpression it indicates that we will schedule reading.
            if (cronExpression != null)
            {
                PropertyCheck.mandatory(this, "initialAndOnErrorCronExpression", initialAndOnErrorCronExpression);
            }
            jsonConfigFileFinder = new ConfigFileFinder(jsonObjectMapper)
            {
                @Override
                protected void readJson(JsonNode jsonNode, String readFromMessage, String baseUrl) throws IOException
                {
                    try
                    {
                        JsonNode mediaTypes = jsonNode.get("mediaTypes");
                        if (mediaTypes != null && mediaTypes.isArray())
                        {
                            List<ConfigElement> mimetypes = new ArrayList<>();
                            for (JsonNode mediaType : mediaTypes)
                            {
                                MediaTypeDef def = jsonObjectMapper.convertValue(mediaType, MediaTypeDef.class);
                                GenericConfigElement mimetype = new GenericConfigElement(ATTR_MIMETYPE);
                                mimetype.addAttribute(ATTR_DISPLAY, def.name);
                                mimetype.addAttribute(ATTR_MIMETYPE, def.mediaType);
                                if (def.text)
                                {
                                    mimetype.addAttribute(ATTR_TEXT, Boolean.TRUE.toString());
                                }

                                GenericConfigElement ext = null;
                                int count = 0;
                                for (ExtensionDef extension : def.extensions)
                                {
                                    ext = new GenericConfigElement(ATTR_EXTENSION);
                                    ext.setValue(extension.extension);
                                    if (extension.name != null && !extension.name.isBlank())
                                    {
                                        ext.addAttribute(ATTR_DISPLAY, extension.name);
                                    }
                                    if (extension.isDefault)
                                    {
                                        ext.addAttribute(ATTR_DEFAULT, Boolean.TRUE.toString());
                                    }
                                    mimetype.addChild(ext);
                                    count++;
                                }
                                if (count == 1 && ext.getAttribute(ATTR_DEFAULT) == null)
                                {
                                    ext.addAttribute(ATTR_DEFAULT, Boolean.TRUE.toString());
                                }
                                mimetypes.add(mimetype);
                            }
                            registerMimetypes(mimetypes);
                            Data data = getData();
                            data.fileCount++;
                        }
                    }
                    catch (IllegalArgumentException e)
                    {
                        logger.error("Error reading "+readFromMessage+" "+e.getMessage());
                    }
                }
            };
        }

        // TikaConfig should be given, but work around it if not
        if (tikaConfig == null)
        {
            logger.warn("TikaConfig spring parameter not supplied, using default config");
            setTikaConfig(TikaConfig.getDefaultConfig());
        }
        // Create our Tika mimetype detector up-front
        // We can then be sure we only have the one, so it's quick (ALF-10813)
        detector = new DefaultDetector(tikaConfig.getMimeRepository());

        // Work out the mappings - only runs once and straight away if cronExpression is null
        configScheduler.run(true, logger, cronExpression, initialAndOnErrorCronExpression);
    }

    public boolean readConfig()
    {
        // Config in XML
        Config config = configService.getConfig(CONFIG_CONDITION, new ConfigLookupContext(CONFIG_AREA));
        ConfigElement mimetypesElement = config.getConfigElement(ELEMENT_MIMETYPES);
        List<ConfigElement> mimetypes = mimetypesElement.getChildren();
        registerMimetypes(mimetypes);
        Data data = getData();
        data.xmlCount = mimetypes.size();

        // Config in JSON
        boolean successReadingConfig = true;
        if (jsonConfigFileFinder != null)
        {
            successReadingConfig &= jsonConfigFileFinder.readFiles("alfresco/mimetypes", logger);
            if (mimetypeJsonConfigDir != null && !mimetypeJsonConfigDir.isBlank())
            {
                successReadingConfig &= jsonConfigFileFinder.readFiles(mimetypeJsonConfigDir, logger);
            }
        }

        data.makeCollectionsReadOnly();

        return successReadingConfig;
    }

    private void registerMimetypes(List<ConfigElement> mimetypes)
    {
        int count = 0;
        Data data = getData();
        for (ConfigElement mimetypeElement : mimetypes)
        {
            count++;
            // add to list of mimetypes
            String mimetype = mimetypeElement.getAttribute(ATTR_MIMETYPE);
            if (mimetype == null || mimetype.length() == 0)
            {
                logger.warn("Ignoring empty mimetype " + count);
                continue;
            }
            // we store it as lowercase
            mimetype = mimetype.toLowerCase();
            boolean replacement = data.mimetypes.contains(mimetype);
            if (!replacement)
            {
                data.mimetypes.add(mimetype);
            }
            // add to map of mimetype displays
            String mimetypeDisplay = mimetypeElement.getAttribute(ATTR_DISPLAY);
            if (mimetypeDisplay != null && mimetypeDisplay.length() > 0)
            {
                String prev = data.displaysByMimetype.put(mimetype, mimetypeDisplay);
                if (replacement && prev != null && !mimetypeDisplay.equals(prev))
                {
                    logger.warn("Replacing " + mimetype + " " + ATTR_DISPLAY + " value '" + prev + "' with '"
                            + mimetypeDisplay + "'");
                }
            }

            // Check if it is a text format
            String isTextStr = mimetypeElement.getAttribute(ATTR_TEXT);
            boolean isText = Boolean.parseBoolean(isTextStr) || mimetype.startsWith(PREFIX_TEXT);
            boolean prevIsText = replacement ? data.textMimetypes.contains(mimetype) : !isText;
            if (isText != prevIsText)
            {
                if (isText)
                {
                    data.textMimetypes.add(mimetype);
                }
                else if (replacement)
                {
                    data.textMimetypes.remove(mimetype);
                }
                if (replacement)
                {
                    logger.warn("Replacing " + mimetype + " " + ATTR_TEXT + " value "
                            + (prevIsText ? "'true' with 'false'" : "'false' with 'true'"));
                }
            }

            // get all the extensions
            List<ConfigElement> extensions = mimetypeElement.getChildren();
            for (ConfigElement extensionElement : extensions)
            {
                // add to map of mimetypes by extension
                String extension = extensionElement.getValue();
                if (extension == null || extension.length() == 0)
                {
                    logger.warn("Ignoring empty extension for mimetype: " + mimetype);
                    continue;
                }
                // put to lowercase
                extension = extension.toLowerCase();
                data.mimetypesByExtension.put(extension, mimetype);
                // add to map of extension displays
                String extensionDisplay = extensionElement.getAttribute(ATTR_DISPLAY);
                String prev = data.displaysByExtension.get(extension);
                // if no display defined for the extension - use the mimetype's
                // display
                if ((prev == null) && (extensionDisplay == null || extensionDisplay.length() == 0)
                        && (mimetypeDisplay != null && mimetypeDisplay.length() > 0))
                {
                    extensionDisplay = mimetypeDisplay;
                }
                if (extensionDisplay != null)
                {
                    data.displaysByExtension.put(extension, extensionDisplay);
                    if (prev != null && !extensionDisplay.equals(prev))
                    {
                        logger.warn("Replacing " + mimetype + " extension " + ATTR_DISPLAY + " value '" + prev
                                + "' with '" + extensionDisplay + "'");
                    }
                }

                // add to map of extensions by mimetype if it is the default or
                // first extension (prevExtension == null)
                String prevExtension = data.extensionsByMimetype.get(mimetype);
                String isDefaultStr = extensionElement.getAttribute(ATTR_DEFAULT);
                boolean isDefault = Boolean.parseBoolean(isDefaultStr) || prevExtension == null;
                if (isDefault)
                {
                    data.extensionsByMimetype.put(mimetype, extension);
                    if (prevExtension != null && !extension.equals(prevExtension))
                    {
                        logger.warn("Replacing " + mimetype + " default extension '" + prevExtension + "' with '"
                                + extension + "'");
                    }
                }
            }
            // check that there were extensions defined
            if (extensions.size() == 0)
            {
                logger.warn("No extensions defined for mimetype: " + mimetype);
            }
        }
    }

    /**
     * Get the file extension associated with the mimetype.
     * 
     * @param mimetype a valid mimetype
     * @return Returns the default extension for the mimetype. Returns the
     *         {@link #MIMETYPE_BINARY binary} mimetype extension.
     * 
     * @see #MIMETYPE_BINARY
     * @see #EXTENSION_BINARY
     */
    public String getExtension(String mimetype)
    {
        Data data = getData();
        String extension = data.extensionsByMimetype.get(mimetype);
        return (extension == null ? EXTENSION_BINARY : extension);
    }

    /**
     * Get the mimetype for the specified extension
     * 
     * @param extension a valid file extension
     * @return Returns a valid mimetype if found, or {@link #MIMETYPE_BINARY
     *         binary} as default.
     */
    @Override
    public String getMimetype(String extension)
    {
        return getMimetype(extension, MIMETYPE_BINARY);
    }
    
    /**
     * Get the mimetype for the specified extension or returns the supplied default if unknown.
     */
    private String getMimetype(String extension, String defaultMimetype)
    {
        String mimetype = null;
        if (extension != null)
        {
            extension = extension.toLowerCase();
            Data data = getData();
            if (data.mimetypesByExtension.containsKey(extension))
            {
                mimetype = data.mimetypesByExtension.get(extension);
            }
        }
        return mimetype == null ? defaultMimetype : mimetype;
    }

    public Map<String, String> getDisplaysByExtension()
    {
        Data data = getData();
        return data.displaysByExtension;
    }

    public Map<String, String> getDisplaysByMimetype()
    {
        Data data = getData();
        return data.displaysByMimetype;
    }

    public Map<String, String> getExtensionsByMimetype()
    {
        Data data = getData();
        return data.extensionsByMimetype;
    }

    public List<String> getMimetypes()
    {
        Data data = getData();
        return data.mimetypes;
    }

    public Map<String, String> getMimetypesByExtension()
    {
        Data data = getData();
        return data.mimetypesByExtension;
    }

    public boolean isText(String mimetype)
    {
        Data data = getData();
        return data.textMimetypes.contains(mimetype);
    }
    
    /**
     * Use Apache Tika to try to guess the type of the file.
     * 
     * @return The mimetype, or null if we can't tell.
     */
    private MediaType detectType(String filename, ContentReader reader)
    {
        TikaInputStream inp = null;
        try
        {
            if (reader != null)
            {
    	        if (reader instanceof FileContentReader)
    	        {
    	            try
    	            {
    	                inp = TikaInputStream.get(((FileContentReader) reader).getFile());
    	            }
    	            catch (FileNotFoundException e)
    	            {
    	                logger.warn("No backing file found for ContentReader " + e);
    	                return null;
    	            }
    	        }
    	        else
    	        {
    	        	inp = TikaInputStream.get(reader.getContentInputStream());
    	        }
            }
            return detectType(filename, inp);
        }
        finally
        {
            if (inp != null)
            {
                try
                {
                    inp.close();
                }
                catch (IOException e)
                {
                    logger.error("Error while closing TikaInputStream.", e);
                }
            }
        }
    }

    private MediaType detectType(String filename, InputStream input)
    {
    	TikaInputStream inp = null;
        if (input != null)
        {
        	inp = TikaInputStream.get(input);
        }
        return detectType(filename, inp);
    }

    /**
     * Use Apache Tika to try to guess the type of the file.
     * 
     * @return The mimetype, or null if we can't tell.
     */
    private MediaType detectType(String filename, TikaInputStream input)
    {
        Metadata metadata = new Metadata();
        if (filename != null)
        {
            //"resourceName"
            metadata.add(TikaCoreProperties.RESOURCE_NAME_KEY, filename);
        }

        InputStream inp = null;
        if (input != null)
        {
        	inp = TikaInputStream.get(input);
        }

        MediaType type;
        try
        {
            type = detector.detect(inp, metadata);
            type = typeBasedOnDetectedTypeAndExtension(type, filename);
            logger.debug(input + " detected by Tika as being " + type.toString());
        }
        catch (Exception e)
        {
            logger.warn("Error identifying content type of problem document", e);
            return null;
        }
        finally
        {
            if (inp != null)
            {
                try
                {
                    inp.close();
                }
                catch (Exception e)
                {
                    // noop
                }
            }
        }
        return type;
    }

    // We have a problem with .ai files, as Tika detects them as .pdf, but if we can use the filename
    // we can correct that. Similar problem with .eps and .ps.
    private MediaType typeBasedOnDetectedTypeAndExtension(MediaType type, String filename)
    {
        if (filename != null && type != null)
        {
            String[] detectedAndPossibleTypes = new String[]
            {
                MIMETYPE_PDF, MIMETYPE_APPLICATION_ILLUSTRATOR,
                MIMETYPE_APPLICATION_PS, MIMETYPE_APPLICATION_EPS
            };

            for (int i=detectedAndPossibleTypes.length-1; i>=0; i-=2)
            {
                String detectedType = detectedAndPossibleTypes[i-1];
                if (detectedType.equals(type.toString()))
                {
                    String possibleType = detectedAndPossibleTypes[i];
                    String extension = getExtension(possibleType);
                    if (filename.endsWith("."+extension))
                    {
                        type = MediaType.parse(possibleType);
                        break;
                    }
                }
            }
        }
        return type;
    }

    /**
     * Use Apache Tika to check if the mime type of the document really matches
     * what it claims to be. This is typically used when a transformation or
     * metadata extractions fails, and you want to know if someone has renamed a
     * file and consequently it has the wrong mime type.
     * 
     * @return Null if the mime type seems ok, otherwise the mime type it
     *         probably is
     */
    public String getMimetypeIfNotMatches(ContentReader reader)
    {
        MediaType type = detectType(null, reader);
        if (type == null)
        {
            // Tika doesn't know so we can't help, sorry...
            return null;
        }

        // Is it a good match?
        if (type.toString().equals(reader.getMimetype())) { return null; }

        // Is it close?
        MediaType claimed = MediaType.parse(reader.getMimetype());
        if (tikaConfig.getMediaTypeRegistry().isSpecializationOf(claimed, type)
                || tikaConfig.getMediaTypeRegistry().isSpecializationOf(type, claimed))
        {
            // Probably close enough
            return null;
        }
        
        // Check through known aliases of the type
        SortedSet<MediaType> aliases = tikaConfig.getMediaTypeRegistry().getAliases(type);
        for (MediaType alias : aliases)
        {
            String aliasType = alias.toString();
            if (aliasType.equals(claimed.toString())) 
            {
                return null; 
            }
        }

        // If we get here, then most likely the type is wrong
        return type.toString();
    }

    /**
     * Takes a guess at the mimetype based exclusively on the file extension,
     * which can (and often is) wrong...
     * 
     * @see #MIMETYPE_BINARY
     */
    public String guessMimetype(String filename)
    {
        String mimetype = MIMETYPE_BINARY;

        // Extract the extension
        if (filename != null && filename.length() > 0)
        {
            int index = filename.lastIndexOf('.');
            if (index > -1 && (index < filename.length() - 1))
            {
                String extension = filename.substring(index + 1).toLowerCase();
                Data data = getData();
                if (data.mimetypesByExtension.containsKey(extension))
                {
                    mimetype = data.mimetypesByExtension.get(extension);
                }
            }
        }
        return mimetype;
    }

    /**
     * Uses Tika to try to identify the mimetype of the file, falling back on
     * {@link #guessMimetype(String)} for an extension based one if Tika can't
     * help.
     */
    public String guessMimetype(String filename, ContentReader reader)
    {
        // ALF-10813: MimetypeMap.guessMimetype consumes 30% of file upload time
        // Let's only 'guess' if we need to
        if (reader != null && reader.getMimetype() != null && !reader.getMimetype().equals(MimetypeMap.MIMETYPE_BINARY))
        {
            // It was set to something other than the default.
            // Possibly someone used this method before (like the UI does) or
            // they just
            // know what their files are.
            return reader.getMimetype();
        }
        InputStream input = (reader != null ? reader.getContentInputStream() : null);
        return guessMimetype(filename, input);
    }

    /**
     * Uses Tika to try to identify the mimetype of the file, falling back on
     * {@link #guessMimetype(String)} for an extension based one if Tika can't
     * help.
     */
    public String guessMimetype(String filename, InputStream input)
    {
        MediaType type = detectType(filename, input);
        String filenameGuess = guessMimetype(filename);

        // If Tika doesn't know what the type is, or file is password protected, go with the filename one
        if (type == null || MediaType.OCTET_STREAM.equals(type) || MIMETYPE_ENCRYPTED_OFFICE.equals(type.toString())) { return filenameGuess; }

        // If Tika has supplied a very generic type, go with the filename one,
        // as it's probably a custom Text or XML format known only to Alfresco
        if ((MediaType.TEXT_PLAIN.equals(type) || MediaType.APPLICATION_XML.equals(type)) && (! filenameGuess.equals(MIMETYPE_BINARY)))
        { 
            return filenameGuess; 
        }

        // Alfresco doesn't support mimetype parameters
        // Use the form of the mimetype without any
        if (type.hasParameters())
        {
            type = type.getBaseType();
        }

        // Not all the mimetypes we use are the Tika Canonical one.
        // So, detect when this happens and use ours in preference
        String tikaType = type.toString();
        Data data = getData();
        if (data.mimetypes.contains(tikaType))
        {
            // Alfresco and Tika agree!
            return tikaType;
        }

        // Check the aliases
        SortedSet<MediaType> aliases = tikaConfig.getMediaTypeRegistry().getAliases(type);
        for (MediaType alias : aliases)
        {
            String aliasType = alias.toString();
            if (data.mimetypes.contains(aliasType)) { return aliasType; }
        }

        // If we get here, then Tika has identified something that
        // Alfresco doesn't really know about. Just trust Tika on it
        logger.info("Tika detected a type of " + tikaType + " for file " + filename
                + " which Alfresco doesn't know about. Consider " + " adding that type to your configuration");
        return tikaType;
    }
    
    /**
     * Returns a collection of mimetypes ordered by extension.
     * @param extension to restrict the collection to one entry
     */
    public Collection<String> getMimetypes(String extension)
    {
        Collection<String> sourceMimetypes;
        if (extension == null)
        {
            sourceMimetypes = getMimetypes();
            sourceMimetypes = sortMimetypesByExt(sourceMimetypes);
        }
        else
        {
            String mimetype = getMimetype(extension, null);
            if (mimetype == null)
            {
                sourceMimetypes = Collections.emptySet();
            }
            else
            {
                sourceMimetypes = Collections.singleton(mimetype);
            }
        }
        return sourceMimetypes;
    }

    /**
     * Copies and sorts the supplied mimetypes by their file extensions
     * @param mimetypes to be sorted
     * @return a new List of sorted mimetypes
     */
    private Collection<String> sortMimetypesByExt(Collection<String> mimetypes)
    {
        List<String> result = new ArrayList<String>(mimetypes);
        for (int i=result.size()-1; i>= 0; i--)
        {
            result.set(i, getExtension(result.get(i)));
        }
        Collections.sort(result);
        for (int i=result.size()-1; i>= 0; i--)
        {
            result.set(i, getMimetype(result.get(i)));
        }
        return result;
    }
}
