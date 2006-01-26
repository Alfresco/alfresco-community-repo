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
import java.net.ConnectException;
import java.util.HashMap;
import java.util.Map;

import net.sf.joott.uno.DocumentConverter;
import net.sf.joott.uno.DocumentFormat;
import net.sf.joott.uno.UnoConnection;

import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.util.TempFileProvider;

/**
 * Makes use of the OpenOffice Uno interfaces to convert the content.
 * <p>
 * The conversions are slow but reliable.  Not <b>all</b> possible combinations of transformations
 * have been enabled because they don't necessarily work and need to be specifically tested before
 * being made available generally.  As the conversion process is mostly automated, the introduction
 * of faulty transformations can lead to unnecessary bugs.  Feel free to experiment and, assuming
 * that the unit test works, report any interesting conversions that can be enabled.
 * 
 * @author Derek Hulley
 */
public class UnoContentTransformer extends AbstractContentTransformer
{
    /** map of <tt>DocumentFormat</tt> instances keyed by mimetype conversion */
    private static Map<ContentTransformerRegistry.TransformationKey, DocumentFormatWrapper> formatsByConversion;
    
    static
    {
        // Build the map of known Uno document formats and store by conversion key
        formatsByConversion = new HashMap<ContentTransformerRegistry.TransformationKey, DocumentFormatWrapper>(17);
        
        formatsByConversion.put(
                new ContentTransformerRegistry.TransformationKey(MimetypeMap.MIMETYPE_OPENOFFICE_WRITER, MimetypeMap.MIMETYPE_TEXT_PLAIN),
                new DocumentFormatWrapper(DocumentFormat.TEXT, 1.0));
        formatsByConversion.put(
                new ContentTransformerRegistry.TransformationKey(MimetypeMap.MIMETYPE_OPENDOCUMENT_TEXT, MimetypeMap.MIMETYPE_TEXT_PLAIN),
                new DocumentFormatWrapper(DocumentFormat.TEXT, 1.0));
        formatsByConversion.put(
                new ContentTransformerRegistry.TransformationKey(MimetypeMap.MIMETYPE_TEXT_PLAIN, MimetypeMap.MIMETYPE_HTML),
                new DocumentFormatWrapper(DocumentFormat.HTML_WRITER, 1.0));
        formatsByConversion.put(
                new ContentTransformerRegistry.TransformationKey(MimetypeMap.MIMETYPE_TEXT_PLAIN, MimetypeMap.MIMETYPE_PDF),
                new DocumentFormatWrapper(DocumentFormat.PDF_WRITER, 1.0));
        formatsByConversion.put(
                new ContentTransformerRegistry.TransformationKey(MimetypeMap.MIMETYPE_TEXT_PLAIN, MimetypeMap.MIMETYPE_WORD),
                new DocumentFormatWrapper(DocumentFormat.TEXT, 1.0));
        formatsByConversion.put(
                new ContentTransformerRegistry.TransformationKey(MimetypeMap.MIMETYPE_WORD, MimetypeMap.MIMETYPE_TEXT_PLAIN),
                new DocumentFormatWrapper(DocumentFormat.TEXT, 1.0));
        formatsByConversion.put(
                new ContentTransformerRegistry.TransformationKey(MimetypeMap.MIMETYPE_WORD, MimetypeMap.MIMETYPE_PDF),
                new DocumentFormatWrapper(DocumentFormat.PDF_WRITER, 1.0));
        formatsByConversion.put(
                new ContentTransformerRegistry.TransformationKey(MimetypeMap.MIMETYPE_EXCEL, MimetypeMap.MIMETYPE_PDF),
                new DocumentFormatWrapper(DocumentFormat.PDF_CALC, 1.0));
        formatsByConversion.put(
                new ContentTransformerRegistry.TransformationKey(MimetypeMap.MIMETYPE_PPT, MimetypeMap.MIMETYPE_FLASH),
                new DocumentFormatWrapper(DocumentFormat.FLASH_IMPRESS, 1.0));
        formatsByConversion.put(
                new ContentTransformerRegistry.TransformationKey(MimetypeMap.MIMETYPE_PPT, MimetypeMap.MIMETYPE_PDF),
                new DocumentFormatWrapper(DocumentFormat.PDF_IMPRESS, 1.0));
        formatsByConversion.put(
                new ContentTransformerRegistry.TransformationKey(MimetypeMap.MIMETYPE_WORD, MimetypeMap.MIMETYPE_HTML),
                new DocumentFormatWrapper(DocumentFormat.HTML_WRITER, 1.0));
        formatsByConversion.put(
                new ContentTransformerRegistry.TransformationKey(MimetypeMap.MIMETYPE_HTML, MimetypeMap.MIMETYPE_PDF),
                new DocumentFormatWrapper(DocumentFormat.PDF_WRITER_WEB, 1.0));
        
        // there are many more formats available and therefore many more transformation combinations possible
//        DocumentFormat.FLASH_IMPRESS
//        DocumentFormat.HTML_CALC
//        DocumentFormat.HTML_WRITER
//        DocumentFormat.MS_EXCEL_97
//        DocumentFormat.MS_POWERPOINT_97
//        DocumentFormat.MS_WORD_97
//        DocumentFormat.PDF_CALC
//        DocumentFormat.PDF_IMPRESS
//        DocumentFormat.PDF_WRITER
//        DocumentFormat.PDF_WRITER_WEB
//        DocumentFormat.RTF
//        DocumentFormat.TEXT
//        DocumentFormat.TEXT_CALC
//        DocumentFormat.XML_CALC
//        DocumentFormat.XML_IMPRESS
//        DocumentFormat.XML_WRITER
//        DocumentFormat.XML_WRITER_WEB
    }
    
    private String connectionUrl = UnoConnection.DEFAULT_CONNECTION_STRING;
    private UnoConnection connection;
    private boolean isConnected;

    /**
     * Constructs the default transformer that will attempt to connect to the
     * Uno server using the default connect string.
     * 
     * @see UnoConnection#DEFAULT_CONNECTION_STRING
     */
    public UnoContentTransformer()
    {
    }

    /**
     * Override the default connection URL with a new one.
     * 
     * @param connectionUrl the connection string
     * 
     * @see UnoConnection#DEFAULT_CONNECTION_STRING
     */
    public void setConnectionUrl(String connectionUrl)
    {
        this.connectionUrl = connectionUrl;
    }

    /**
     * Connects to the OpenOffice server.  If successful, then
     * {@link AbstractContentTransformer#register() auto-registers}. 
     */
    public synchronized void init()
    {
        connection = new UnoConnection(connectionUrl);
        // attempt to make an connection
        try
        {
            connection.connect();
            isConnected = true;
            // register
            super.register();
        }
        catch (ConnectException e)
        {
            isConnected = false;
        }
    }
    
    /**
     * @return Returns true if a connection to the Uno server could be established
     */
    public boolean isConnected()
    {
        return isConnected;
    }

    /**
     * @param sourceMimetype
     * @param targetMimetype
     * @return Returns a document format wrapper that is valid for the given source and target mimetypes
     */
    private static DocumentFormatWrapper getDocumentFormatWrapper(String sourceMimetype, String targetMimetype)
    {
        // get the well-known document format for the specific conversion
        ContentTransformerRegistry.TransformationKey key =
                new ContentTransformerRegistry.TransformationKey(sourceMimetype, targetMimetype); 
        DocumentFormatWrapper wrapper = UnoContentTransformer.formatsByConversion.get(key);
        return wrapper;
    }
    
    /**
     * Checks how reliable the conversion will be when performed by the Uno server.
     * <p>
     * The connection for the Uno server is checked in order to have any chance of
     * being reliable.
     * <p>
     * The conversions' reliabilities are set up statically based on prior tests that
     * included checking performance as well as accuracy.
     */
    public double getReliability(String sourceMimetype, String targetMimetype)
    {
        // check if a connection to the Uno server can be established
        if (!isConnected())
        {
            // no connection means that conversion is not possible
            return 0.0;
        }
        // check if the source and target mimetypes are supported
        DocumentFormatWrapper docFormatWrapper = getDocumentFormatWrapper(sourceMimetype, targetMimetype);
        if (docFormatWrapper == null)
        {
            return 0.0;
        }
        else
        {
            return docFormatWrapper.getReliability();
        }
    }

    public void transformInternal(ContentReader reader, ContentWriter writer,  Map<String, Object> options)
            throws Exception
    {
        String sourceMimetype = getMimetype(reader);
        String targetMimetype = getMimetype(writer);

        // create temporary files to convert from and to
        File tempFromFile = TempFileProvider.createTempFile(
                "UnoContentTransformer",
                "." + getMimetypeService().getExtension(sourceMimetype));
        File tempToFile = TempFileProvider.createTempFile(
                "UnoContentTransformer",
                "." + getMimetypeService().getExtension(targetMimetype));
        // download the content from the source reader
        reader.getContent(tempFromFile);
        
        // get the document format that should be used
        DocumentFormatWrapper docFormatWrapper = getDocumentFormatWrapper(sourceMimetype, targetMimetype);
        try
        {
            docFormatWrapper.execute(tempFromFile, tempToFile, connection);
            // conversion success
        }
        catch (ConnectException e)
        {
            throw new ContentIOException("Connection to Uno server failed: \n" +
                    "   reader: " + reader + "\n" +
                    "   writer: " + writer,
                    e);
        }
        catch (IOException e)
        {
            throw new ContentIOException("Uno server conversion failed: \n" +
                    "   reader: " + reader + "\n" +
                    "   writer: " + writer + "\n" +
                    "   from file: " + tempFromFile + "\n" +
                    "   to file: " + tempToFile,
                    e);
        }
        
        // upload the temp output to the writer given us
        writer.putContent(tempToFile);
    }
    
    /**
     * Wraps a document format as well the reliability.  The source and target mimetypes
     * are not kept, but will probably be closely associated with the reliability.
     */
    private static class DocumentFormatWrapper
    {
        /*
         * Source and target mimetypes not kept -> class is private as it doesn't keep
         * enough info to be used safely externally
         */
        
        private DocumentFormat documentFormat;
        private double reliability;
        
        public DocumentFormatWrapper(DocumentFormat documentFormat, double reliability)
        {
            this.documentFormat = documentFormat;
            this.reliability = reliability;
        }
        
        public double getReliability()
        {
            return reliability;
        }

        /**
         * Executs the transformation
         */
        public void execute(File fromFile, File toFile, UnoConnection connection) throws ConnectException, IOException
        {
            DocumentConverter converter = new DocumentConverter(connection);
            converter.convert(fromFile, toFile, documentFormat);
        }
    }
}
