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
package org.alfresco.repo.content.transform;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import net.sf.jooreports.converter.DocumentFamily;
import net.sf.jooreports.converter.DocumentFormat;
import net.sf.jooreports.converter.DocumentFormatRegistry;
import net.sf.jooreports.converter.XmlDocumentFormatRegistry;
import net.sf.jooreports.openoffice.connection.OpenOfficeConnection;
import net.sf.jooreports.openoffice.connection.OpenOfficeException;
import net.sf.jooreports.openoffice.converter.AbstractOpenOfficeDocumentConverter;
import net.sf.jooreports.openoffice.converter.OpenOfficeDocumentConverter;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.TransformationOptions;
import org.alfresco.util.PropertyCheck;
import org.alfresco.util.TempFileProvider;
import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.edit.PDPageContentStream;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.DefaultResourceLoader;

/**
 * Makes use of the {@link http://sourceforge.net/projects/joott/JOOConverter} library to perform 
 *  OpenOffice-driven conversions.
 * This requires that OpenOffice be running, but delivers a wider range of transformations
 *  than Tika is able to (Tika just translates into Text, HTML and XML)
 * 
 * @author Derek Hulley
 */
public class OpenOfficeContentTransformerWorker extends OOoContentTransformerHelper implements ContentTransformerWorker, InitializingBean
{
    private OpenOfficeConnection connection;
    private AbstractOpenOfficeDocumentConverter converter;
    private String documentFormatsConfiguration;
    private DocumentFormatRegistry formatRegistry;

    /**
     * @param connection
     *            the connection that the converter uses
     */
    public void setConnection(OpenOfficeConnection connection)
    {
        this.connection = connection;
    }

    /**
     * Explicitly set the converter to be used. The converter must use the same connection set in
     * {@link #setConnection(OpenOfficeConnection)}.
     * <p>
     * If not set, then the <code>OpenOfficeDocumentConverter</code> will be used.
     * 
     * @param converter
     *            the converter to use.
     */
    public void setConverter(AbstractOpenOfficeDocumentConverter converter)
    {
        this.converter = converter;
    }

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

    public boolean isAvailable()
    {
        return this.connection.isConnected();
    }

    public void afterPropertiesSet() throws Exception
    {
        PropertyCheck.mandatory("OpenOfficeContentTransformerWorker", "connection", this.connection);

        // load the document conversion configuration
        if (this.documentFormatsConfiguration != null)
        {
            DefaultResourceLoader resourceLoader = new DefaultResourceLoader();
            try
            {
                InputStream is = resourceLoader.getResource(this.documentFormatsConfiguration).getInputStream();
                this.formatRegistry = new XmlDocumentFormatRegistry(is);
            }
            catch (IOException e)
            {
                throw new AlfrescoRuntimeException("Unable to load document formats configuration file: "
                        + this.documentFormatsConfiguration);
            }
        }
        else
        {
            this.formatRegistry = new XmlDocumentFormatRegistry();
        }

        // set up the converter
        if (this.converter == null)
        {
            this.converter = new OpenOfficeDocumentConverter(this.connection);
        }
    }

    /**
     * @see DocumentFormatRegistry
     */
    public boolean isTransformable(String sourceMimetype, String targetMimetype, TransformationOptions options)
    {
        if (!isAvailable())
        {
            // The connection management is must take care of this
            return false;
        }

        if (isTransformationBlocked(sourceMimetype, targetMimetype))
        {
            return false;
        }
        
        MimetypeService mimetypeService = getMimetypeService();
        String sourceExtension = mimetypeService.getExtension(sourceMimetype);
        String targetExtension = mimetypeService.getExtension(targetMimetype);
        // query the registry for the source format
        DocumentFormat sourceFormat = this.formatRegistry.getFormatByFileExtension(sourceExtension);
        if (sourceFormat == null)
        {
            // no document format
            return false;
        }
        // query the registry for the target format
        DocumentFormat targetFormat = this.formatRegistry.getFormatByFileExtension(targetExtension);
        if (targetFormat == null)
        {
            // no document format
            return false;
        }

        // get the family of the target document
        DocumentFamily sourceFamily = sourceFormat.getFamily();
        // does the format support the conversion
        if (!targetFormat.isExportableFrom(sourceFamily))
        {
            // unable to export from source family of documents to the target format
            return false;
        }
        else
        {
            return true;
        }
    }

    public void transform(ContentReader reader, ContentWriter writer, TransformationOptions options) throws Exception
    {
        String sourceMimetype = getMimetype(reader);
        String targetMimetype = getMimetype(writer);

        MimetypeService mimetypeService = getMimetypeService();
        String sourceExtension = mimetypeService.getExtension(sourceMimetype);
        String targetExtension = mimetypeService.getExtension(targetMimetype);
        // query the registry for the source format
        DocumentFormat sourceFormat = this.formatRegistry.getFormatByFileExtension(sourceExtension);
        if (sourceFormat == null)
        {
            // source format is not recognised
            throw new ContentIOException("No OpenOffice document format for source extension: " + sourceExtension);
        }
        // query the registry for the target format
        DocumentFormat targetFormat = this.formatRegistry.getFormatByFileExtension(targetExtension);
        if (targetFormat == null)
        {
            // target format is not recognised
            throw new ContentIOException("No OpenOffice document format for target extension: " + targetExtension);
        }
        // get the family of the target document
        DocumentFamily sourceFamily = sourceFormat.getFamily();
        // does the format support the conversion
        if (!targetFormat.isExportableFrom(sourceFamily))
        {
            throw new ContentIOException("OpenOffice conversion not supported: \n" + "   reader: " + reader + "\n"
                    + "   writer: " + writer);
        }

        // create temporary files to convert from and to
        File tempFromFile = TempFileProvider.createTempFile("OpenOfficeContentTransformer-source-", "."
                + sourceExtension);
        File tempToFile = TempFileProvider
                .createTempFile("OpenOfficeContentTransformer-target-", "." + targetExtension);

        
        final long documentSize = reader.getSize();
        
        // download the content from the source reader
        reader.getContent(tempFromFile);
        
        // There is a bug (reported in ALF-219) whereby JooConverter (the Alfresco Community Edition's 3rd party
        // OpenOffice connector library) struggles to handle zero-size files being transformed to pdf.
        // For zero-length .html files, it throws NullPointerExceptions.
        // For zero-length .txt files, it produces a pdf transformation, but it is not a conformant
        // pdf file and cannot be viewed (contains no pages).
        //
        // For these reasons, if the file is of zero length, we will not use JooConverter & OpenOffice
        // and will instead ask Apache PDFBox to produce an empty pdf file for us.
		if (documentSize == 0L)
        {
			produceEmptyPdfFile(tempToFile);
        }
        else
        {
        	// We have some content, so we'll use OpenOffice to render the pdf document.
        	// Currently, OpenOffice does a better job of rendering documents into PDF and so
        	// it is preferred over PDFBox.
        	try
        	{
        		this.converter.convert(tempFromFile, sourceFormat, tempToFile, targetFormat);
        		// conversion success
        	}
        	catch (OpenOfficeException e)
        	{
        		throw new ContentIOException("OpenOffice server conversion failed: \n" + "   reader: " + reader + "\n"
        				+ "   writer: " + writer + "\n" + "   from file: " + tempFromFile + "\n" + "   to file: "
        				+ tempToFile, e);
        	}
        }
        

        // upload the temp output to the writer given us
        writer.putContent(tempToFile);
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
        catch (COSVisitorException cvx)
        {
        	throw new ContentIOException("Error creating empty PDF file", cvx);
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
				} catch (IOException ignored) {
					// Intentionally empty
				}
        	}
        	if (pdfDoc != null)
        	{
        		try
        		{
					pdfDoc.close();
				} catch (IOException ignored) {
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
        // Actual version information owned by OpenOfficeConnectionTester
        return "";
    }
}
