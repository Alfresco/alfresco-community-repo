/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.content.transform;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Map;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.pdfbox.TextToPDF;
import org.pdfbox.pdmodel.PDDocument;
import org.pdfbox.pdmodel.font.PDTrueTypeFont;
import org.pdfbox.pdmodel.font.PDType1Font;

/**
 * Makes use of the {@link http://www.pdfbox.org/ PDFBox} library's <code>TextToPDF</code> utility.
 * 
 * @author Derek Hulley
 * @since 2.1.0
 */
public class TextToPdfContentTransformer extends AbstractContentTransformer
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
    public double getReliability(String sourceMimetype, String targetMimetype)
    {
        if (!MimetypeMap.MIMETYPE_TEXT_PLAIN.equals(sourceMimetype) ||
            !MimetypeMap.MIMETYPE_PDF.equals(targetMimetype))
        {
            // only support Text -> PDF
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
