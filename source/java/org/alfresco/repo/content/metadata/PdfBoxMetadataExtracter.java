/*
 * Copyright (C) 2005 Jesper Steen Møller
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
package org.alfresco.repo.content.metadata;

import java.util.ArrayList;

import org.alfresco.repo.content.MimetypeMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tika.parser.Parser;
import org.apache.tika.parser.pdf.PDFParser;

/**
 * Metadata extractor for the PDF documents.
 * <pre>
 *   <b>author:</b>                 --      cm:author
 *   <b>title:</b>                  --      cm:title
 *   <b>subject:</b>                --      cm:description
 *   <b>created:</b>                --      cm:created
 *   <b>(custom metadata):</b>      --
 * </pre>
 * 
 * Uses Apache Tika
 * 
 * @author Jesper Steen Møller
 * @author Derek Hulley
 */
public class PdfBoxMetadataExtracter extends TikaPoweredMetadataExtracter
{
    protected static Log pdfLogger = LogFactory.getLog(PdfBoxMetadataExtracter.class);

    public static ArrayList<String> SUPPORTED_MIMETYPES = buildSupportedMimetypes(
             new String[] { MimetypeMap.MIMETYPE_PDF, MimetypeMap.MIMETYPE_APPLICATION_ILLUSTRATOR },
             new PDFParser()
    );

    public PdfBoxMetadataExtracter()
    {
        super(SUPPORTED_MIMETYPES);
    }
    
    @Override
    protected Parser getParser() 
    {
       return new PDFParser();
    }
}
