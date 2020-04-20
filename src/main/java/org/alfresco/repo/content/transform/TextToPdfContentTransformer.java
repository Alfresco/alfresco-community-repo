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

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.TransformationOptionLimits;
import org.alfresco.service.cmr.repository.TransformationOptionPair;
import org.alfresco.service.cmr.repository.TransformationOptions;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.tools.TextToPDF;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.alfresco.repo.rendition2.RenditionDefinition2.SOURCE_ENCODING;

/**
 * Makes use of the <a href="http://www.pdfbox.org/">PDFBox</a> library's <code>TextToPDF</code> utility.
 * 
 * @author Derek Hulley
 * @since 2.1.0
 *
 * @deprecated The transformations code is being moved out of the codebase and replaced by the new async RenditionService2 or other external libraries.
 */
@Deprecated
public class TextToPdfContentTransformer extends AbstractRemoteContentTransformer
{
    private static final Log logger = LogFactory.getLog(TextToPdfContentTransformer.class);

    private PagedTextToPDF transformer;
    
    public TextToPdfContentTransformer()
    {
        setPageLimitsSupported(true);
        transformer = new PagedTextToPDF();
    }
    
    public void setStandardFont(String fontName)
    {
        try
        {
            transformer.setFont(transformer.getStandardFont(fontName));
        }
        catch (Throwable e)
        {
            throw new AlfrescoRuntimeException("Unable to set Standard Font for PDF generation: " + fontName, e);
        }
    }

    public void setFontSize(int fontSize)
    {
        try
        {
            transformer.setFontSize(fontSize);
        }
        catch (Throwable e)
        {
            throw new AlfrescoRuntimeException("Unable to set Font Size for PDF generation: " + fontSize);
        }
    }
    
    /**
     * Only supports Text to PDF
     */
    @Override
    public boolean isTransformableMimetype(String sourceMimetype, String targetMimetype, TransformationOptions options)
    {
        if ( (!MimetypeMap.MIMETYPE_TEXT_PLAIN.equals(sourceMimetype) &&
              !MimetypeMap.MIMETYPE_TEXT_CSV.equals(sourceMimetype) &&
              !MimetypeMap.MIMETYPE_DITA.equals(sourceMimetype) &&
              !MimetypeMap.MIMETYPE_XML.equals(sourceMimetype) ) ||
            !MimetypeMap.MIMETYPE_PDF.equals(targetMimetype))
        {
            // only support (text/plain OR text/csv OR text/xml) to (application/pdf)
            return false;
        }
        else
        {
            return true;
        }
    }

    @Override
    public String getComments(boolean available)
    {
        return getCommentsOnlySupports(
                Arrays.asList(new String[] {MimetypeMap.MIMETYPE_TEXT_PLAIN, MimetypeMap.MIMETYPE_TEXT_CSV,
                                            MimetypeMap.MIMETYPE_DITA, MimetypeMap.MIMETYPE_XML}),
                Arrays.asList(new String[] {MimetypeMap.MIMETYPE_PDF}), available);
    }

    @Override
    protected Log getLogger()
    {
        return logger;
    }

    @Override
    protected void transformLocal(
            ContentReader reader,
            ContentWriter writer,
            TransformationOptions options) throws Exception
    {
        PDDocument pdf = null;
        InputStream is = null;
        InputStreamReader ir = null;
        OutputStream os = null;
        try
        {
            is = reader.getContentInputStream();
            ir = buildReader(is, reader.getEncoding(), reader.getContentUrl());

            TransformationOptionLimits limits = getLimits(reader, writer, options);
            TransformationOptionPair pageLimits = limits.getPagesPair();
            pdf = transformer.createPDFFromText(ir, pageLimits, reader.getContentUrl(), transformerDebug);

            // dump it all to the writer
            os = writer.getContentOutputStream();
            pdf.save(os);
        }
        finally
        {
            if (pdf != null)
            {
                try { pdf.close(); } catch (Throwable e) {e.printStackTrace(); }
            }
            if (ir != null)
            {
                try { ir.close(); } catch (Throwable e) {e.printStackTrace(); }
            }
            if (is != null)
            {
                try { is.close(); } catch (Throwable e) {e.printStackTrace(); }
            }
            if (os != null)
            {
                try { os.close(); } catch (Throwable e) {e.printStackTrace(); }
            }
        }
    }

    @Override
    protected void transformRemote(RemoteTransformerClient remoteTransformerClient, ContentReader reader,
                                   ContentWriter writer, TransformationOptions options, String sourceMimetype,
                                   String targetMimetype, String sourceExtension, String targetExtension,
                                   String targetEncoding) throws Exception
    {
        String sourceEncoding = reader.getEncoding();
        long timeoutMs = options.getTimeoutMs();

        TransformationOptionLimits limits = getLimits(reader, writer, options);
        TransformationOptionPair pageLimits = limits.getPagesPair();
        int pageLimit = (int)pageLimits.getValue();

        remoteTransformerClient.request(reader, writer, sourceMimetype, sourceExtension, targetExtension,
                timeoutMs, logger,
                "transformName", "textToPdf",
                "sourceMimetype", sourceMimetype,
                "targetMimetype", targetMimetype,
                "targetExtension", targetExtension,
                SOURCE_ENCODING, sourceEncoding,
                "pageLimit", String.valueOf(pageLimit));
    }

    protected InputStreamReader buildReader(InputStream is, String encoding, String node)
    {
        // If they gave an encoding, try to use it
        if(encoding != null)
        {
            Charset charset = null;
            try
            {
                charset = Charset.forName(encoding);
            } catch(Exception e)
            {
                logger.warn("JVM doesn't understand encoding '" + encoding + 
                        "' when transforming " + node);
            }
            if(charset != null)
            {
                logger.debug("Processing plain text in encoding " + charset.displayName());
                return new InputStreamReader(is, charset);
            }
        }
        
        // Fall back on the system default
        logger.debug("Processing plain text using system default encoding");
        return new InputStreamReader(is);
    }
    
    private static class PagedTextToPDF extends TextToPDF
    {
        // REPO-1066: duplicating the following lines from org.apache.pdfbox.tools.TextToPDF because they made them private
        // before the upgrade to pdfbox 2.0.8, in pdfbox 1.8, this piece of code was public in org.apache.pdfbox.pdmodel.font.PDType1Font
        static PDType1Font getStandardFont(String name) {
            return (PDType1Font)STANDARD_14.get(name);
        }
        private static final Map<String, PDType1Font> STANDARD_14 = new HashMap<String, PDType1Font>();
        static
        {
            STANDARD_14.put(PDType1Font.TIMES_ROMAN.getBaseFont(), PDType1Font.TIMES_ROMAN);
            STANDARD_14.put(PDType1Font.TIMES_BOLD.getBaseFont(), PDType1Font.TIMES_BOLD);
            STANDARD_14.put(PDType1Font.TIMES_ITALIC.getBaseFont(), PDType1Font.TIMES_ITALIC);
            STANDARD_14.put(PDType1Font.TIMES_BOLD_ITALIC.getBaseFont(), PDType1Font.TIMES_BOLD_ITALIC);
            STANDARD_14.put(PDType1Font.HELVETICA.getBaseFont(), PDType1Font.HELVETICA);
            STANDARD_14.put(PDType1Font.HELVETICA_BOLD.getBaseFont(), PDType1Font.HELVETICA_BOLD);
            STANDARD_14.put(PDType1Font.HELVETICA_OBLIQUE.getBaseFont(), PDType1Font.HELVETICA_OBLIQUE);
            STANDARD_14.put(PDType1Font.HELVETICA_BOLD_OBLIQUE.getBaseFont(), PDType1Font.HELVETICA_BOLD_OBLIQUE);
            STANDARD_14.put(PDType1Font.COURIER.getBaseFont(), PDType1Font.COURIER);
            STANDARD_14.put(PDType1Font.COURIER_BOLD.getBaseFont(), PDType1Font.COURIER_BOLD);
            STANDARD_14.put(PDType1Font.COURIER_OBLIQUE.getBaseFont(), PDType1Font.COURIER_OBLIQUE);
            STANDARD_14.put(PDType1Font.COURIER_BOLD_OBLIQUE.getBaseFont(), PDType1Font.COURIER_BOLD_OBLIQUE);
            STANDARD_14.put(PDType1Font.SYMBOL.getBaseFont(), PDType1Font.SYMBOL);
            STANDARD_14.put(PDType1Font.ZAPF_DINGBATS.getBaseFont(), PDType1Font.ZAPF_DINGBATS);
        }
        //duplicating until here

        // The following code is based on the code in TextToPDF with the addition of
        // checks for page limits.
        // The calling code must close the PDDocument once finished with it.
        public PDDocument createPDFFromText(Reader text, TransformationOptionPair pageLimits, String contentUrl, TransformerDebug transformerDebug)
            throws IOException
        {
            int pageLimit = (int)pageLimits.getValue();
            PDDocument doc = null;
            int pageCount = 0;
            try
            {
                final int margin = 40;
                float height = getFont().getFontDescriptor().getFontBoundingBox().getHeight()/1000;

                //calculate font height and increase by 5 percent.
                height = height*getFontSize()*1.05f;
                doc = new PDDocument();
                BufferedReader data = new BufferedReader( text );
                String nextLine = null;
                PDPage page = new PDPage();
                PDPageContentStream contentStream = null;
                float y = -1;
                float maxStringLength = page.getMediaBox().getWidth() - 2*margin;
                
                // There is a special case of creating a PDF document from an empty string.
                boolean textIsEmpty = true;
                
                outer:
                while( (nextLine = data.readLine()) != null )
                {
                    
                    // The input text is nonEmpty. New pages will be created and added
                    // to the PDF document as they are needed, depending on the length of
                    // the text.
                    textIsEmpty = false;

                    String[] lineWords = nextLine.trim().split( " " );
                    int lineIndex = 0;
                    while( lineIndex < lineWords.length )
                    {
                        StringBuffer nextLineToDraw = new StringBuffer();
                        float lengthIfUsingNextWord = 0;
                        do
                        {
                            nextLineToDraw.append( lineWords[lineIndex] );
                            nextLineToDraw.append( " " );
                            lineIndex++;
                            if( lineIndex < lineWords.length )
                            {
                                String lineWithNextWord = nextLineToDraw.toString() + lineWords[lineIndex];
                                lengthIfUsingNextWord =
                                    (getFont().getStringWidth( lineWithNextWord )/1000) * getFontSize();
                            }
                        }
                        while( lineIndex < lineWords.length &&
                               lengthIfUsingNextWord < maxStringLength );
                        if( y < margin )
                        {
                            if (pageLimit > 0 && pageCount++ >= pageLimit)
                            {
                                pageLimits.getAction().throwIOExceptionIfRequired("Page limit ("+pageLimit+
                                        ") reached.", transformerDebug);
                                break outer;
                            }
                            
                            // We have crossed the end-of-page boundary and need to extend the
                            // document by another page.
                            page = new PDPage();
                            doc.addPage( page );
                            if( contentStream != null )
                            {
                                contentStream.endText();
                                contentStream.close();
                            }
                            contentStream = new PDPageContentStream(doc, page);
                            contentStream.setFont(getFont(), getFontSize());
                            contentStream.beginText();
                            y = page.getMediaBox().getHeight() - margin + height;
                            contentStream.moveTextPositionByAmount(
                                margin, y );
                        }
                        //System.out.println( "Drawing string at " + x + "," + y );

                        if( contentStream == null )
                        {
                            throw new IOException( "Error:Expected non-null content stream." );
                        }
                        contentStream.moveTextPositionByAmount( 0, -height);
                        y -= height;
                        contentStream.drawString( nextLineToDraw.toString() );
                    }
                }
                
                // If the input text was the empty string, then the above while loop will have short-circuited
                // and we will not have added any PDPages to the document.
                // So in order to make the resultant PDF document readable by Adobe Reader etc, we'll add an empty page.
                if (textIsEmpty)
                {
                    doc.addPage(page);
                }
                
                if( contentStream != null )
                {
                    contentStream.endText();
                    contentStream.close();
                }
            }
            catch( IOException io )
            {
                if( doc != null )
                {
                    doc.close();
                }
                throw io;
            }
            return doc;
        }
    }
}
