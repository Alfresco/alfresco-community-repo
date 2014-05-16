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

import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.repository.TransformationOptions;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.parser.pdf.PDFParser;
import org.apache.tika.parser.pdf.PDFParserConfig;

/**
 * Uses {@link http://tika.apache.org/ Apache Tika} and
 *  {@link http://pdfbox.apache.org/ Apache PDFBox} to perform
 *  conversions from PDF documents.
 * 
 * @author Nick Burch
 * @author Derek Hulley
 */
public class PdfBoxContentTransformer extends TikaPoweredContentTransformer
{
    protected PDFParserConfig pdfParserConfig;
    
    public PdfBoxContentTransformer() {
       super(new String[] {
             MimetypeMap.MIMETYPE_PDF
       });
    }

    @Override
    protected Parser getParser() {
       return new PDFParser();
    }
    
    /**
     * Sets the PDFParserConfig for inclusion in the ParseContext sent to the PDFBox parser,
     * useful for setting config like spacingTolerance.
     * 
     * @param pdfParserConfig
     */
    public void setPdfParserConfig(PDFParserConfig pdfParserConfig)
    {
        this.pdfParserConfig = pdfParserConfig;
    }

    @Override
    protected ParseContext buildParseContext(Metadata metadata, String targetMimeType, TransformationOptions options)
    {
        ParseContext context = super.buildParseContext(metadata, targetMimeType, options);
        if (pdfParserConfig != null)
        {
            System.out.println("**** spacingTolerance=" + pdfParserConfig.getSpacingTolerance() + ", averageCharTolerance=" + pdfParserConfig.getAverageCharTolerance());
            context.set(PDFParserConfig.class, pdfParserConfig);
        }
        // TODO: Possibly extend TransformationOptions to allow for per-transform PDFParserConfig?
        return context;
    }
}
