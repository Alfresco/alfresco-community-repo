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

import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.TransformationOptions;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.parser.pdf.PDFParser;
import org.apache.tika.parser.pdf.PDFParserConfig;

import static org.alfresco.repo.rendition2.RenditionDefinition2.TARGET_ENCODING;

/**
 * Uses <a href="http://tika.apache.org/">Apache Tika</a> and
 *  <a href="@link http://pdfbox.apache.org/">Apache PDFBox</a> to perform
 *  conversions from PDF documents.
 * 
 * @author Nick Burch
 * @author Derek Hulley
 *
 * @deprecated The transformations code is being moved out of the codebase and replaced by the new async RenditionService2 or other external libraries.
 */
@Deprecated
public class PdfBoxContentTransformer extends TikaPoweredContentTransformer
{
    /**
     * The logger
     */
    private static Log logger = LogFactory.getLog(PdfBoxContentTransformer.class);
    protected PDFParserConfig pdfParserConfig;
    private boolean extractBookmarksText = true;
    
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
    
    public void setExtractBookmarksText(boolean extractBookmarksText)
    {
        this.extractBookmarksText = extractBookmarksText;
    }


    @Override
    protected ParseContext buildParseContext(Metadata metadata, String targetMimeType, TransformationOptions options)
    {
        ParseContext context = super.buildParseContext(metadata, targetMimeType, options);
        if (pdfParserConfig != null)
        {
            pdfParserConfig.setExtractBookmarksText(extractBookmarksText);
            context.set(PDFParserConfig.class, pdfParserConfig);
        }
        // TODO: Possibly extend TransformationOptions to allow for per-transform PDFParserConfig?
        return context;
    }

    @Override
    protected void transformRemote(RemoteTransformerClient remoteTransformerClient, ContentReader reader,
                                   ContentWriter writer, TransformationOptions options,
                                   String sourceMimetype, String targetMimetype,
                                   String sourceExtension, String targetExtension,
                                   String targetEncoding) throws Exception
    {
        long timeoutMs = options.getTimeoutMs();
        String notExtractBookmarksText = null;

        if (!extractBookmarksText)
        {
            notExtractBookmarksText = Boolean.TRUE.toString();
        }

        remoteTransformerClient.request(reader, writer, sourceMimetype, sourceExtension, targetExtension,
                timeoutMs, logger,
                "transformName", "PdfBox",
                "notExtractBookmarksText", notExtractBookmarksText,
                "sourceMimetype", sourceMimetype,
                "targetMimetype", targetMimetype,
                "targetExtension", targetExtension,
                TARGET_ENCODING, targetEncoding);
    }
}
