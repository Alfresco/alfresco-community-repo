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

import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.TransformationOptions;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;

/**
 * Makes use of the {@link http://www.pdfbox.org/ PDFBox} library to
 * perform conversions from PDF files to text.
 * 
 * @author Derek Hulley
 */
public class PdfBoxContentTransformer extends AbstractContentTransformer2
{
    /**
     * Currently the only transformation performed is that of text extraction from PDF documents.
     */
    public boolean isTransformable(String sourceMimetype, String targetMimetype, TransformationOptions options)
    {
        // TODO: Expand PDFBox usage to convert images to PDF and investigate other conversions
        
        if (!MimetypeMap.MIMETYPE_PDF.equals(sourceMimetype) ||
            !MimetypeMap.MIMETYPE_TEXT_PLAIN.equals(targetMimetype))
        {
            // only support PDF -> Text
            return false;
        }
        else
        {
            return true;
        }
    }

    protected void transformInternal(
            ContentReader reader,
            ContentWriter writer,
            TransformationOptions options) throws Exception
    {
        PDDocument pdf = null;
        InputStream is = null;
        try
        {
            is = reader.getContentInputStream();
            // stream the document in
            pdf = PDDocument.load(is);
            // strip the text out
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(pdf);
            
            // dump it all to the writer
            writer.putContent(text);
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
        }
    }
}
