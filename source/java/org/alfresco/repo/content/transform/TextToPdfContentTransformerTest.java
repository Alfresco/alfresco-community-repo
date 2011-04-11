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
import java.io.StringWriter;
import java.nio.charset.Charset;

import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.content.filestore.FileContentWriter;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.TransformationOptions;
import org.alfresco.util.TempFileProvider;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;

/**
 * @see org.alfresco.repo.content.transform.TextToPdfContentTransformer
 * 
 * @author Derek Hulley
 * @since 2.1.0
 */
public class TextToPdfContentTransformerTest extends AbstractContentTransformerTest
{
    private TextToPdfContentTransformer transformer;
    
    @Override
    public void setUp() throws Exception
    {
        super.setUp();
        
        transformer = new TextToPdfContentTransformer();
        transformer.setStandardFont("Times-Roman");
        transformer.setFontSize(20);
    }
    
    /**
     * @return Returns the same transformer regardless - it is allowed
     */
    @Override
    protected ContentTransformer getTransformer(String sourceMimetype, String targetMimetype)
    {
        return transformer;
    }
    
    public void testReliability() throws Exception
    {
        boolean reliability = transformer.isTransformable(MimetypeMap.MIMETYPE_PDF, MimetypeMap.MIMETYPE_TEXT_PLAIN, new TransformationOptions());
        assertEquals("Mimetype should not be supported", false, reliability);
        reliability = transformer.isTransformable(MimetypeMap.MIMETYPE_TEXT_PLAIN, MimetypeMap.MIMETYPE_PDF, new TransformationOptions());
        assertEquals("Mimetype should be supported", true, reliability);
        reliability = transformer.isTransformable(MimetypeMap.MIMETYPE_XML, MimetypeMap.MIMETYPE_PDF, new TransformationOptions());
        assertEquals("Mimetype should be supported", true, reliability);
    }
    
    /**
     * Tests that we can produce PDFs from a variety of different
     *  source encodings.
     * TODO Re-enable this test once we've fixed ALF-2534
     */
    public void DISABLEDtestUnicodeTextAndEncodings() throws Exception
    {
        String allAscii = "This is a simple set of text\n" +
            "It is all nice and dull\n";
        String european = "En français où les choses sont accentués\n" +
            "En español, así";
        
        ContentReader reader;
        for(String text : new String[] {allAscii, european})
        {
            for(String encoding : new String[] {"ISO-8859-1", "UTF-8", "UTF-16"})
            {
                // Get a reader for the text
                reader = buildContentReader(text, Charset.forName(encoding));
                
                // And a temp writer
                File out = TempFileProvider.createTempFile("AlfrescoTest_", ".pdf");
                ContentWriter writer = new FileContentWriter(out);
                writer.setMimetype("application/pdf");
                
                // Transform to PDF
                transformer.transform(reader, writer);
                
                // Read back in the PDF and check it
                PDDocument doc = PDDocument.load(out);
                PDFTextStripper textStripper = new PDFTextStripper();
                StringWriter textWriter = new StringWriter();
                textStripper.writeText(doc, textWriter);
                doc.close();
                
                // Newlines etc may be different, so zap them
                String checkText = clean(text);
                String roundTrip = clean(textWriter.toString());
                
                // Now check it
//                System.err.println("== " + encoding + " ==");
//                System.err.println(roundTrip);
//                System.err.println("====");
                assertEquals(
                        "Incorrect text in PDF when starting from text in " + encoding,
                        checkText, roundTrip
                );
            }
        }
    }
    private String clean(String text)
    {
        text = text.replaceAll("\\s+\\r", "");
        text = text.replaceAll("\\s+\\n", "");
        text = text.replaceAll("\\r", "");
        text = text.replaceAll("\\n", "");
        return text;
    }
}
