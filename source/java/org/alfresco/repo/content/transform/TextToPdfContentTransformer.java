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

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.TransformationOptions;
import org.apache.pdfbox.TextToPDF;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.font.PDTrueTypeFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

/**
 * Makes use of the {@link http://www.pdfbox.org/ PDFBox} library's <code>TextToPDF</code> utility.
 * 
 * @author Derek Hulley
 * @since 2.1.0
 */
public class TextToPdfContentTransformer extends AbstractContentTransformer2
{
    private TextToPDF transformer;
    
    public TextToPdfContentTransformer()
    {
        transformer = new TextToPDF();
    }
    
    public void setStandardFont(String fontName)
    {
        try
        {
            transformer.setFont(PDType1Font.getStandardFont(fontName));
        }
        catch (Throwable e)
        {
            throw new AlfrescoRuntimeException("Unable to set Standard Font for PDF generation: " + fontName, e);
        }
    }
    
    public void setTrueTypeFont(String fontName)
    {
        try
        {
            transformer.setFont(PDTrueTypeFont.loadTTF(null, fontName));
        }
        catch (Throwable e)
        {
            throw new AlfrescoRuntimeException("Unable to set True Type Font for PDF generation: " + fontName, e);
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
    public boolean isTransformable(String sourceMimetype, String targetMimetype, TransformationOptions options)
    {
        if ( (!MimetypeMap.MIMETYPE_TEXT_PLAIN.equals(sourceMimetype) &&
              !MimetypeMap.MIMETYPE_TEXT_CSV.equals(sourceMimetype) &&
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
    protected void transformInternal(
            ContentReader reader,
            ContentWriter writer,
            TransformationOptions options) throws Exception
    {
        PDDocument pdf = null;
        InputStream is = null;
        OutputStream os = null;
        try
        {
            is = reader.getContentInputStream();
            pdf = transformer.createPDFFromText(new InputStreamReader(is));
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
}
