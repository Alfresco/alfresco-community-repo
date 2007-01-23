/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.repo.content;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.config.Config;
import org.alfresco.config.ConfigElement;
import org.alfresco.config.ConfigLookupContext;
import org.alfresco.config.ConfigService;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Provides a bidirectional mapping between well-known mimetypes and
 * the registered file extensions.  All mimetypes and extensions
 * are stored and handled as lowercase.
 * 
 * @author Derek Hulley
 */
public class MimetypeMap implements MimetypeService
{
    public static final String EXTENSION_BINARY = "bin";
    
    public static final String MIMETYPE_TEXT_PLAIN = "text/plain";
    public static final String MIMETYPE_TEXT_CSS = "text/css";    
    public static final String MIMETYPE_XML = "text/xml";
    public static final String MIMETYPE_HTML = "text/html";
    public static final String MIMETYPE_XHTML = "application/xhtml+xml";
    public static final String MIMETYPE_PDF = "application/pdf";
    public static final String MIMETYPE_WORD = "application/msword";
    public static final String MIMETYPE_EXCEL = "application/vnd.excel";
    public static final String MIMETYPE_BINARY = "application/octet-stream";
    public static final String MIMETYPE_PPT = "application/vnd.powerpoint";
    public static final String MIMETYPE_FLASH = "application/x-shockwave-flash";
    public static final String MIMETYPE_IMAGE_GIF = "image/gif";
    public static final String MIMETYPE_IMAGE_JPEG = "image/jpeg";
    public static final String MIMETYPE_IMAGE_RGB = "image/x-rgb";
    public static final String MIMETYPE_IMAGE_SVG = "image/svg";
    public static final String MIMETYPE_JAVASCRIPT = "application/x-javascript";
    public static final String MIMETYPE_ZIP = "application/zip";
    public static final String MIMETYPE_OPENSEARCH_DESCRIPTION = "application/opensearchdescription+xml";
    public static final String MIMETYPE_ATOM = "application/atom+xml";
    public static final String MIMETYPE_RSS = "application/rss+xml";
    // Open Document
    public static final String MIMETYPE_OPENDOCUMENT_TEXT = "application/vnd.oasis.opendocument.text";
    public static final String MIMETYPE_OPENDOCUMENT_TEXT_TEMPLATE = "application/vnd.oasis.opendocument.text-template";
    public static final String MIMETYPE_OPENDOCUMENT_GRAPHICS = "application/vnd.oasis.opendocument.graphics";
    public static final String MIMETYPE_OPENDOCUMENT_GRAPHICS_TEMPLATE= "application/vnd.oasis.opendocument.graphics-template";
    public static final String MIMETYPE_OPENDOCUMENT_PRESENTATION= "application/vnd.oasis.opendocument.presentation";
    public static final String MIMETYPE_OPENDOCUMENT_PRESENTATION_TEMPLATE= "application/vnd.oasis.opendocument.presentation-template";
    public static final String MIMETYPE_OPENDOCUMENT_SPREADSHEET= "application/vnd.oasis.opendocument.spreadsheet";
    public static final String MIMETYPE_OPENDOCUMENT_SPREADSHEET_TEMPLATE= "application/vnd.oasis.opendocument.spreadsheet-template";
    public static final String MIMETYPE_OPENDOCUMENT_CHART= "application/vnd.oasis.opendocument.chart";
    public static final String MIMETYPE_OPENDOCUMENT_CHART_TEMPLATE= "applicationvnd.oasis.opendocument.chart-template";
    public static final String MIMETYPE_OPENDOCUMENT_IMAGE= "application/vnd.oasis.opendocument.image";
    public static final String MIMETYPE_OPENDOCUMENT_IMAGE_TEMPLATE= "applicationvnd.oasis.opendocument.image-template";
    public static final String MIMETYPE_OPENDOCUMENT_FORMULA= "application/vnd.oasis.opendocument.formula";
    public static final String MIMETYPE_OPENDOCUMENT_FORMULA_TEMPLATE= "applicationvnd.oasis.opendocument.formula-template";
    public static final String MIMETYPE_OPENDOCUMENT_TEXT_MASTER= "application/vnd.oasis.opendocument.text-master";
    public static final String MIMETYPE_OPENDOCUMENT_TEXT_WEB= "application/vnd.oasis.opendocument.text-web";
    public static final String MIMETYPE_OPENDOCUMENT_DATABASE= "application/vnd.oasis.opendocument.database";
    // Open Office
    public static final String MIMETYPE_OPENOFFICE1_WRITER = "application/vnd.sun.xml.writer";
    public static final String MIMETYPE_OPENOFFICE1_CALC = "application/vnd.sun.xml.calc";
    public static final String MIMETYPE_OPENOFFICE1_DRAW = "application/vnd.sun.xml.draw";
    public static final String MIMETYPE_OPENOFFICE1_IMPRESS = "application/vnd.sun.xml.impress";
    // Star Office
    public static final String MIMETYPE_STAROFFICE5_DRAW = "application/vnd.stardivision.draw";
    public static final String MIMETYPE_STAROFFICE5_CALC = "application/vnd.stardivision.calc";
    public static final String MIMETYPE_STAROFFICE5_IMPRESS = "application/vnd.stardivision.impress";
    public static final String MIMETYPE_STAROFFICE5_IMPRESS_PACKED = "application/vnd.stardivision.impress-packed";
    public static final String MIMETYPE_STAROFFICE5_CHART = "application/vnd.stardivision.chart";
    public static final String MIMETYPE_STAROFFICE5_WRITER = "application/vnd.stardivision.writer";
    public static final String MIMETYPE_STAROFFICE5_WRITER_GLOBAL = "application/vnd.stardivision.writer-global";
    public static final String MIMETYPE_STAROFFICE5_MATH = "application/vnd.stardivision.math";
    // WordPerfect
    public static final String MIMETYPE_WORDPERFECT = "application/wordperfect";
    // Audio
    public static final String MIMETYPE_MP3 = "audio/x-mpeg";
    // Alfresco
    public static final String MIMETYPE_ACP = "application/acp";
    
    private static final String CONFIG_AREA = "mimetype-map";
    private static final String CONFIG_CONDITION = "Mimetype Map";
    private static final String ELEMENT_MIMETYPES = "mimetypes";
    private static final String ATTR_MIMETYPE = "mimetype";
    private static final String ATTR_DISPLAY = "display";
    private static final String ATTR_DEFAULT = "default";
    
    private static final Log logger = LogFactory.getLog(MimetypeMap.class);
    
    private ConfigService configService;
    
    private List<String> mimetypes;
    private Map<String, String> extensionsByMimetype;
    private Map<String, String> mimetypesByExtension;
    private Map<String, String> displaysByMimetype;
    private Map<String, String> displaysByExtension;
    
    /**
     * @param configService the config service to use to read mimetypes from
     */
    public MimetypeMap(ConfigService configService)
    {
        this.configService = configService;
    }    
    
    /**
     * Initialises the map using the configuration service provided
     */
    public void init()
    {
        this.mimetypes = new ArrayList<String>(40);
        this.extensionsByMimetype = new HashMap<String, String>(59);
        this.mimetypesByExtension = new HashMap<String, String>(59);
        this.displaysByMimetype = new HashMap<String, String>(59);
        this.displaysByExtension = new HashMap<String, String>(59);

        Config config = configService.getConfig(CONFIG_CONDITION, new ConfigLookupContext(CONFIG_AREA));
        ConfigElement mimetypesElement = config.getConfigElement(ELEMENT_MIMETYPES);
        List<ConfigElement> mimetypes = mimetypesElement.getChildren();
        int count = 0;
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
            if (this.mimetypes.contains(mimetype))
            {
                throw new AlfrescoRuntimeException("Duplicate mimetype definition: " + mimetype);
            }
            this.mimetypes.add(mimetype);
            // add to map of mimetype displays
            String mimetypeDisplay = mimetypeElement.getAttribute(ATTR_DISPLAY);
            if (mimetypeDisplay != null && mimetypeDisplay.length() > 0)
            {
                this.displaysByMimetype.put(mimetype, mimetypeDisplay);
            }
            
            // get all the extensions
            boolean isFirst = true;
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
                this.mimetypesByExtension.put(extension, mimetype);
                // add to map of extension displays
                String extensionDisplay = extensionElement.getAttribute(ATTR_DISPLAY);
                if (extensionDisplay != null && extensionDisplay.length() > 0)
                {
                    this.displaysByExtension.put(extension, extensionDisplay);
                }
                else if (mimetypeDisplay != null && mimetypeDisplay.length() > 0)
                {
                    // no display defined for the extension - use the mimetype's display
                    this.displaysByExtension.put(extension, mimetypeDisplay);
                }
                // add to map of extensions by mimetype if it is the default or first extension
                String isDefaultStr = extensionElement.getAttribute(ATTR_DEFAULT);
                boolean isDefault = Boolean.parseBoolean(isDefaultStr);
                if (isDefault || isFirst)
                {
                    this.extensionsByMimetype.put(mimetype, extension);
                }
                isFirst = false;
            }
            // check that there were extensions defined
            if (extensions.size() == 0)
            {
                logger.warn("No extensions defined for mimetype: " + mimetype);
            }
        }
        
        // make the collections read-only
        this.mimetypes = Collections.unmodifiableList(this.mimetypes);
        this.extensionsByMimetype = Collections.unmodifiableMap(this.extensionsByMimetype);
        this.mimetypesByExtension = Collections.unmodifiableMap(this.mimetypesByExtension);
        this.displaysByMimetype = Collections.unmodifiableMap(this.displaysByMimetype);
        this.displaysByExtension = Collections.unmodifiableMap(this.displaysByExtension);
    }
    
    /**
     * Get the file extension associated with the mimetype.
     * 
     * @param mimetype a valid mimetype
     * @return Returns the default extension for the mimetype.  Returns the {@link #MIMETYPE_BINARY binary}
     *      mimetype extension.
     * 
     * @see #MIMETYPE_BINARY
     * @see #EXTENSION_BINARY
     */
    public String getExtension(String mimetype)
    {
        String extension = extensionsByMimetype.get(mimetype);
        if (extension == null)
        {
            return EXTENSION_BINARY;
        }
        else
        {
            return extension;
        }
    }

    public Map<String, String> getDisplaysByExtension()
    {
        return displaysByExtension;
    }

    public Map<String, String> getDisplaysByMimetype()
    {
        return displaysByMimetype;
    }

    public Map<String, String> getExtensionsByMimetype()
    {
        return extensionsByMimetype;
    }

    public List<String> getMimetypes()
    {
        return mimetypes;
    }

    public Map<String, String> getMimetypesByExtension()
    {
        return mimetypesByExtension;
    }

    /**
     * @see #MIMETYPE_BINARY
     */
    public String guessMimetype(String filename)
    {
        filename = filename.toLowerCase();
        String mimetype = MIMETYPE_BINARY;
        // extract the extension
        int index = filename.lastIndexOf('.');
        if (index > -1 && (index < filename.length() - 1))
        {
            String extension = filename.substring(index + 1);
            if (mimetypesByExtension.containsKey(extension))
            {
                mimetype = mimetypesByExtension.get(extension);
            }
        }
        return mimetype;
    }
}
