/*
 * #%L
 * Alfresco Repository
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
package org.alfresco.repo.content.transform;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;

import com.sun.star.task.ErrorCodeIOException;
import org.artofsolving.jodconverter.document.DefaultDocumentFormatRegistry;
import org.artofsolving.jodconverter.document.DocumentFamily;
import org.artofsolving.jodconverter.document.DocumentFormat;
import org.artofsolving.jodconverter.document.DocumentFormatRegistry;
import org.artofsolving.jodconverter.document.JsonDocumentFormatRegistry;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.TransformationOptions;
import org.alfresco.util.TempFileProvider;
import org.apache.commons.logging.Log;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.artofsolving.jodconverter.office.OfficeException;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.util.FileCopyUtils;

/**
 * A class providing basic OOo-related functionality shared by both
 * {@link ContentTransformer}s and {@link ContentTransformerWorker}s.
 */
public abstract class OOoContentTransformerHelper extends ContentTransformerHelper
{
    private String documentFormatsConfiguration;
    private DocumentFormatRegistry formatRegistry;
    protected TransformerDebug transformerDebug;
    private static final int JODCONVERTER_TRANSFORMATION_ERROR_CODE = 3088;

    /**
     * Set a non-default location from which to load the document format mappings.
     * 
     * @param path
     *            a resource location supporting the <b>file:</b> or <b>classpath:</b> prefixes
     */
    public void setDocumentFormatsConfiguration(String path)
    {
        this.documentFormatsConfiguration = path;
    }
    
    protected abstract Log getLogger();

    protected abstract String getTempFilePrefix();

    public abstract boolean isAvailable();

    protected abstract void convert(File tempFromFile, DocumentFormat sourceFormat, File tempToFile,
            DocumentFormat targetFormat);
    
    /**
     * Helper setter of the transformer debug. 
     * @param transformerDebug TransformerDebug
     */
    public void setTransformerDebug(TransformerDebug transformerDebug)
    {
        this.transformerDebug = transformerDebug;
    }

    public void afterPropertiesSet() throws Exception
    {
        // load the document conversion configuration
        if (documentFormatsConfiguration != null)
        {
            DefaultResourceLoader resourceLoader = new DefaultResourceLoader();
            try
            {
                InputStream is = resourceLoader.getResource(this.documentFormatsConfiguration).getInputStream();
                formatRegistry = new JsonDocumentFormatRegistry(is);
                // We do not need to explicitly close this InputStream as it is closed for us within the XmlDocumentFormatRegistry
            }
            catch (IOException e)
            {
                throw new AlfrescoRuntimeException(
                        "Unable to load document formats configuration file: "
                        + this.documentFormatsConfiguration);
            }
        }
        else
        {
            formatRegistry = new DefaultDocumentFormatRegistry();
        }
    }

    /**
     * There are some conversions that fail, despite the converter believing them possible.
     * This method can be used by subclasses to check if a targetMimetype or source/target
     * Mimetype pair are blocked.
     * 
     * @param sourceMimetype String
     * @param targetMimetype String
     * @return <code>true</code> if the mimetypes are blocked, else <code>false</code>
     */
    protected boolean isTransformationBlocked(String sourceMimetype, String targetMimetype)
    {
        if (targetMimetype.equals(MimetypeMap.MIMETYPE_XHTML))
        {
            return true;
        }
        else if (targetMimetype.equals(MimetypeMap.MIMETYPE_WORDPERFECT))
        {
            return true;
        }
        else if (targetMimetype.equals(MimetypeMap.MIMETYPE_FLASH))
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    /**
     * @see DocumentFormatRegistry
     */
    public boolean isTransformable(String sourceMimetype, String targetMimetype, TransformationOptions options)
    {
        // Use BinaryPassThroughContentTransformer if mimetypes are the same.
        if (sourceMimetype.equals(targetMimetype))
        {
            return false;
        }

        if (!isAvailable())
        {
            // The connection management is must take care of this
            return false;
        }

        if (isTransformationBlocked(sourceMimetype, targetMimetype))
        {
            if (getLogger().isDebugEnabled())
            {
                StringBuilder msg = new StringBuilder();
                msg.append("Transformation from ")
                   .append(sourceMimetype).append(" to ")
                   .append(targetMimetype)
                   .append(" is blocked and therefore unavailable.");
                getLogger().debug(msg.toString());
            }
            return false;
        }
        
        MimetypeService mimetypeService = getMimetypeService();
        String sourceExtension = mimetypeService.getExtension(sourceMimetype);
        String targetExtension = mimetypeService.getExtension(targetMimetype);
        // query the registry for the source format
        DocumentFormat sourceFormat = formatRegistry.getFormatByExtension(sourceExtension);
        if (sourceFormat == null)
        {
            // no document format
            return false;
        }
        // query the registry for the target format
        DocumentFormat targetFormat = formatRegistry.getFormatByExtension(targetExtension);
        if (targetFormat == null)
        {
            // no document format
            return false;
        }

        // get the family of the target document
        DocumentFamily sourceFamily = sourceFormat.getInputFamily();
        // does the format support the conversion
        boolean transformable = formatRegistry.getOutputFormats(sourceFamily).contains(targetFormat); // same as: targetFormat.getStoreProperties(sourceFamily) != null
        return transformable;
    }
    
    @Override
    public String getComments(boolean available)
    {
        return "# Transformations supported by OpenOffice/LibreOffice\n";
    }

    /**
     * This method produces an empty PDF file at the specified File location.
     * Apache's PDFBox is used to create the PDF file.
     */
    private void produceEmptyPdfFile(File tempToFile)
    {
        // If improvement PDFBOX-914 is incorporated, we can do this with a straight call to
        // org.apache.pdfbox.TextToPdf.createPDFFromText(new StringReader(""));
        // https://issues.apache.org/jira/browse/PDFBOX-914

        PDDocument pdfDoc = null;
        PDPageContentStream contentStream = null;
        try
        {
            pdfDoc = new PDDocument();
            PDPage pdfPage = new PDPage();
            // Even though, we want an empty PDF, some libs (e.g. PDFRenderer) object to PDFs
            // that have literally nothing in them. So we'll put a content stream in it.
            contentStream = new PDPageContentStream(pdfDoc, pdfPage);
            pdfDoc.addPage(pdfPage);

            // Now write the in-memory PDF document into the temporary file.
            pdfDoc.save(tempToFile.getAbsolutePath());

        }
        catch (IOException iox)
        {
            throw new ContentIOException("Error creating empty PDF file", iox);
        }
        finally
        {
            if (contentStream != null)
            {
                try
                {
                    contentStream.close();
                }
                catch (IOException ignored)
                {
                    // Intentionally empty
                }
            }
            if (pdfDoc != null)
            {
                try
                {
                    pdfDoc.close();
                }
                catch (IOException ignored)
                {
                    // Intentionally empty.
                }
            }
        }
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.repo.content.transform.ContentTransformerWorker#getVersionString()
     */
    public String getVersionString()
    {
        return "";
    }

    public void transform(
            ContentReader reader,
            ContentWriter writer,
            TransformationOptions options) throws Exception
    {
        if (isAvailable() == false)
        {
            throw new ContentIOException("Content conversion failed (unavailable): \n" +
                    "   reader: " + reader + "\n" +
                    "   writer: " + writer);
        }
        
        if (getLogger().isDebugEnabled())
        {
                StringBuilder msg = new StringBuilder();
                msg.append("transforming content from ")
                    .append(reader.getMimetype())
                    .append(" to ")
                    .append(writer.getMimetype());
                getLogger().debug(msg.toString());
        }
        
        String sourceMimetype = getMimetype(reader);
        String targetMimetype = getMimetype(writer);

        MimetypeService mimetypeService = getMimetypeService();
        String sourceExtension = mimetypeService.getExtension(sourceMimetype);
        String targetExtension = mimetypeService.getExtension(targetMimetype);
        // query the registry for the source format
        DocumentFormat sourceFormat = formatRegistry.getFormatByExtension(sourceExtension);
        if (sourceFormat == null)
        {
            // source format is not recognised
            throw new ContentIOException("No OpenOffice document format for source extension: " + sourceExtension);
        }
        // query the registry for the target format
        DocumentFormat targetFormat = formatRegistry.getFormatByExtension(targetExtension);
        if (targetFormat == null)
        {
            // target format is not recognised
            throw new ContentIOException("No OpenOffice document format for target extension: " + targetExtension);
        }
        // get the family of the target document
        DocumentFamily sourceFamily = sourceFormat.getInputFamily();
        // does the format support the conversion
        if (!formatRegistry.getOutputFormats(sourceFamily).contains(targetFormat)) // same as: targetFormat.getStoreProperties(sourceFamily) == null
        {
            throw new ContentIOException(
                    "OpenOffice conversion not supported: \n" +
                    "   reader: " + reader + "\n" +
                    "   writer: " + writer);
        }

        // create temporary files to convert from and to
        File tempFromFile = TempFileProvider.createTempFile(
                getTempFilePrefix()+"-source-",
                "." + sourceExtension);
        File tempToFile = TempFileProvider.createTempFile(
                getTempFilePrefix()+"-target-",
                "." + targetExtension);
        
        // There is a bug (reported in ALF-219) whereby JooConverter (the Alfresco Community Edition's 3rd party
        // OpenOffice connector library) struggles to handle zero-size files being transformed to pdf.
        // For zero-length .html files, it throws NullPointerExceptions.
        // For zero-length .txt files, it produces a pdf transformation, but it is not a conformant
        // pdf file and cannot be viewed (contains no pages).
        //
        // For these reasons, if the file is of zero length, we will not use JooConverter & OpenOffice
        // and will instead ask Apache PDFBox to produce an empty pdf file for us.
        final long documentSize = reader.getSize();
        if (documentSize == 0L || temporaryMsFile(options))
        {
            produceEmptyPdfFile(tempToFile);
        }
        else
        {
            // download the content from the source reader
            saveContentInFile(sourceMimetype, reader, tempFromFile);
            
            // We have some content, so we'll use OpenOffice to render the pdf document.
            // Currently, OpenOffice does a better job of rendering documents into PDF and so
            // it is preferred over PDFBox.
            try
            {
                convert(tempFromFile, sourceFormat, tempToFile, targetFormat);
            }
            catch (OfficeException e)
            {
                throw new ContentIOException("OpenOffice server conversion failed: \n" +
                        "   reader: " + reader + "\n" +
                        "   writer: " + writer + "\n" +
                        "   from file: " + tempFromFile + "\n" +
                        "   to file: " + tempToFile,
                        e);
            }
            catch (Throwable throwable)
            {
                // Because of the known bug with empty Spreadsheets in JodConverter try to catch exception and produce empty pdf file
                if (throwable.getCause() instanceof ErrorCodeIOException &&
                        ((ErrorCodeIOException) throwable.getCause()).ErrCode == JODCONVERTER_TRANSFORMATION_ERROR_CODE)
                {
                    getLogger().warn("Transformation failed: \n" +
                                             "from file: " + tempFromFile + "\n" +
                                             "to file: " + tempToFile +
                                             "Source file " + tempFromFile + " has no content");
                    produceEmptyPdfFile(tempToFile);
                }
				else
				{
                    throw throwable;
				}
            }
        }

        if (getLogger().isDebugEnabled())
        {
                StringBuilder msg = new StringBuilder();
                msg.append("transforming ")
                    .append(tempFromFile.getName())
                    .append(" to ")
                    .append(tempToFile.getName());
                getLogger().debug(msg.toString());
        }
        
        // upload the temp output to the writer given us
        writer.putContent(tempToFile);

        if (getLogger().isDebugEnabled())
        {
            getLogger().debug("transformation successful");
        }
    }
    
    private boolean temporaryMsFile(TransformationOptions options)
    {
        String fileName = transformerDebug == null ? null : transformerDebug.getFileName(options, true, -1);
        return fileName != null && fileName.startsWith("~$");
    }

    /**
     * Populates a file with the content in the reader.
     */
    public void saveContentInFile(String sourceMimetype, ContentReader reader, File file) throws ContentIOException
    {
        String encoding = reader.getEncoding();
        if (encodeAsUtf8(sourceMimetype, encoding))
        {
            saveContentInUtf8File(reader, file);
        }
        else
        {
            reader.getContent(file);
        }
    }
    
    /**
     * Returns {@code true} if the input file should be transformed to UTF8 encoding.<p>
     * 
     * OpenOffice/LibreOffice is unable to support the import of text files that are SHIFT JIS encoded
     * (and others: windows-1252...) so transformed to UTF-8.
     */
    protected boolean encodeAsUtf8(String sourceMimetype, String encoding)
    {
        return MimetypeMap.MIMETYPE_TEXT_PLAIN.equals(sourceMimetype) && !"UTF-8".equals(encoding);
    }

    /**
     * Populates a file with the content in the reader, but also converts the encoding to UTF-8.
     */
    private void saveContentInUtf8File(ContentReader reader, File file)
    {
        String encoding = reader.getEncoding();
        try
        {
            Reader in = new InputStreamReader(reader.getContentInputStream(), encoding);
            Writer out = new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(file)), "UTF-8");

            FileCopyUtils.copy(in, out);  // both streams are closed
        }
        catch (IOException e)
        {
            throw new ContentIOException("Failed to copy content to file and convert "+encoding+" to UTF-8: \n" +
                    "   file: " + file,
                    e);
        }
    }
}