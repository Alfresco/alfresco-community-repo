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
package org.alfresco.repo.content.transform;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.util.Map;

import net.sf.jooreports.converter.DocumentFamily;
import net.sf.jooreports.converter.DocumentFormat;
import net.sf.jooreports.converter.DocumentFormatRegistry;
import net.sf.jooreports.converter.XmlDocumentFormatRegistry;
import net.sf.jooreports.openoffice.connection.OpenOfficeConnection;
import net.sf.jooreports.openoffice.connection.OpenOfficeException;
import net.sf.jooreports.openoffice.converter.OpenOfficeDocumentConverter;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.util.PropertyCheck;
import org.alfresco.util.TempFileProvider;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.DefaultResourceLoader;

/**
 * Makes use of the {@link http://sourceforge.net/projects/joott/ JOOConverter} library to
 * perform OpenOffice-drive conversions.
 * 
 * @author Derek Hulley
 */
public class OpenOfficeContentTransformer extends AbstractContentTransformer
{
    private static Log logger = LogFactory.getLog(OpenOfficeContentTransformer.class);
    
    private OpenOfficeConnection connection;
    private boolean connected;
    private OpenOfficeDocumentConverter converter;
    private String documentFormatsConfiguration;
    private DocumentFormatRegistry formatRegistry;
    
    public OpenOfficeContentTransformer()
    {
        this.connected = false;
    }
    
    public void setConnection(OpenOfficeConnection connection)
    {
        this.connection = connection;
    }
    
    /**
     * Set a non-default location from which to load the document format mappings.
     * 
     * @param path a resource location supporting the <b>file:</b> or <b>classpath:</b> prefixes
     */
    public void setDocumentFormatsConfiguration(String path)
    {
        this.documentFormatsConfiguration = path;
    }

    public boolean isConnected()
    {
        return connected;
    }

    private synchronized void connect()
    {
        try
        {
            connection.connect();
            connected = true;
        }
        catch (ConnectException e)
        {
            logger.warn(e.getMessage());
            connected = false;
        }
    }

    @Override
    public void register()
    {
        PropertyCheck.mandatory("OpenOfficeContentTransformer", "connection", connection);
        
        // attempt to establish a connection
        connect();
        
        // set up the converter
        converter = new OpenOfficeDocumentConverter(connection);
        
        // load the document conversion configuration
        if (documentFormatsConfiguration != null)
        {
            DefaultResourceLoader resourceLoader = new DefaultResourceLoader();
            try
            {
                InputStream is = resourceLoader.getResource(documentFormatsConfiguration).getInputStream();
                formatRegistry = new XmlDocumentFormatRegistry(is);
            }
            catch (IOException e)
            {
                throw new AlfrescoRuntimeException(
                        "Unable to load document formats configuration file: " + documentFormatsConfiguration);
            }
        }
        else
        {
            formatRegistry = new XmlDocumentFormatRegistry();
        }
        
        if (connected)
        {
            // register
            super.register();
        }
    }

    /**
     * @see DocumentFormatRegistry
     */
    public double getReliability(String sourceMimetype, String targetMimetype)
    {
        if (!connected)
        {
            return 0.0;
        }
        
        // there are some conversions that fail, despite the converter believing them possible
        if (targetMimetype.equals(MimetypeMap.MIMETYPE_XHTML))
        {
            return 0.0;
        }
        else if (targetMimetype.equals(MimetypeMap.MIMETYPE_WORDPERFECT))
        {
            return 0.0;
        }
        
        MimetypeService mimetypeService = getMimetypeService();
        String sourceExtension = mimetypeService.getExtension(sourceMimetype);
        String targetExtension = mimetypeService.getExtension(targetMimetype);
        // query the registry for the source format
        DocumentFormat sourceFormat = formatRegistry.getFormatByFileExtension(sourceExtension);
        if (sourceFormat == null)
        {
            // no document format
            return 0.0;
        }
        // query the registry for the target format
        DocumentFormat targetFormat = formatRegistry.getFormatByFileExtension(targetExtension);
        if (targetFormat == null)
        {
            // no document format
            return 0.0;
        }

        // get the family of the target document
        DocumentFamily sourceFamily = sourceFormat.getFamily();
        // does the format support the conversion
        if (!targetFormat.isExportableFrom(sourceFamily))
        {
            // unable to export from source family of documents to the target format
            return 0.0;
        }
        else
        {
            return 1.0;
        }
    }

    protected void transformInternal(
            ContentReader reader,
            ContentWriter writer,
            Map<String, Object> options) throws Exception
    {
        String sourceMimetype = getMimetype(reader);
        String targetMimetype = getMimetype(writer);

        MimetypeService mimetypeService = getMimetypeService();
        String sourceExtension = mimetypeService.getExtension(sourceMimetype);
        String targetExtension = mimetypeService.getExtension(targetMimetype);
        // query the registry for the source format
        DocumentFormat sourceFormat = formatRegistry.getFormatByFileExtension(sourceExtension);
        if (sourceFormat == null)
        {
            // source format is not recognised
            throw new ContentIOException("No OpenOffice document format for source extension: " + sourceExtension);
        }
        // query the registry for the target format
        DocumentFormat targetFormat = formatRegistry.getFormatByFileExtension(targetExtension);
        if (targetFormat == null)
        {
            // target format is not recognised
            throw new ContentIOException("No OpenOffice document format for target extension: " + sourceExtension);
        }
        // get the family of the target document
        DocumentFamily sourceFamily = sourceFormat.getFamily();
        // does the format support the conversion
        if (!targetFormat.isExportableFrom(sourceFamily))
        {
            throw new ContentIOException(
                    "OpenOffice conversion not supported: \n" +
                    "   reader: " + reader + "\n" +
                    "   writer: " + writer);
        }

        // create temporary files to convert from and to
        File tempFromFile = TempFileProvider.createTempFile(
                "OpenOfficeContentTransformer-source-",
                "." + sourceExtension);
        File tempToFile = TempFileProvider.createTempFile(
                "OpenOfficeContentTransformer-target-",
                "." + targetExtension);
        // download the content from the source reader
        reader.getContent(tempFromFile);
        
        try
        {
            converter.convert(tempFromFile, sourceFormat, tempToFile, targetFormat);
            // conversion success
        }
        catch (OpenOfficeException e)
        {
            throw new ContentIOException("OpenOffice server conversion failed: \n" +
                    "   reader: " + reader + "\n" +
                    "   writer: " + writer + "\n" +
                    "   from file: " + tempFromFile + "\n" +
                    "   to file: " + tempToFile,
                    e);
        }
        
        // upload the temp output to the writer given us
        writer.putContent(tempToFile);
    }
}
