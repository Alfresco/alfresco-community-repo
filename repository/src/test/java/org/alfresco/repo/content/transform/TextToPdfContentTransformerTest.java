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

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.Charset;

import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.content.filestore.FileContentWriter;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.TransformationOptions;
import org.alfresco.util.TempFileProvider;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

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
        transformer.setMimetypeService(mimetypeService);
        transformer.setTransformerDebug(transformerDebug);
        transformer.setTransformerConfig(transformerConfig);
        transformer.setStandardFont("Times-Roman");
        transformer.setFontSize(20);
        transformer.setBeanName("transformer.test"+System.currentTimeMillis()%100000);
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
        transformer.setPageLimit(-1);
        transformer.register();

        boolean reliability = transformer.isTransformable(MimetypeMap.MIMETYPE_PDF, -1, MimetypeMap.MIMETYPE_TEXT_PLAIN, new TransformationOptions());
        assertEquals("Mimetype should not be supported", false, reliability);
        reliability = transformer.isTransformable(MimetypeMap.MIMETYPE_TEXT_PLAIN, -1, MimetypeMap.MIMETYPE_PDF, new TransformationOptions());
        assertEquals("Mimetype should be supported", true, reliability);
        reliability = transformer.isTransformable(MimetypeMap.MIMETYPE_XML, -1, MimetypeMap.MIMETYPE_PDF, new TransformationOptions());
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
        
        for(String text : new String[] {allAscii, european})
        {
            for(String encoding : new String[] {"ISO-8859-1", "UTF-8", "UTF-16"})
            {
                // Newlines etc may be different, so zap them
                String checkText = clean(text);

                transformTextAndCheck(text, encoding, checkText);
            }
        }
    }

    public void testUnlimitedPages() throws Exception
    {
        transformTextAndCheckPageLength(-1);
    }

    public void testLimitedTo1Page() throws Exception
    {
        transformTextAndCheckPageLength(1);
    }
    
    public void testLimitedTo2Pages() throws Exception
    {
        transformTextAndCheckPageLength(2);
    }

    public void testLimitedTo50Pages() throws Exception
    {
        transformTextAndCheckPageLength(50);
    }

    private void transformTextAndCheckPageLength(int pageLimit) throws IOException
    {
        transformer.setPageLimit(pageLimit);
        transformer.register();
        
        int pageLength = 32;
        int lines = (pageLength+10) * ((pageLimit > 0) ? pageLimit : 1);
        StringBuilder sb = new StringBuilder();
        String checkText = null;
        int cutoff = pageLimit * pageLength;
        for (int i=1; i<=lines; i++)
        {
            sb.append(i);
            sb.append(" I must not talk in class or feed my homework to my cat.\n");
            if (i == cutoff)
                checkText = sb.toString();
        }
        sb.append("\nBart\n");
        String text = sb.toString();
        checkText = (checkText == null) ? clean(text) : clean(checkText);

        transformTextAndCheck(text, "UTF-8", checkText);
    }

    private void transformTextAndCheck(String text, String encoding, String checkText)
            throws IOException
    {
        // Get a reader for the text
        ContentReader reader = buildContentReader(text, Charset.forName(encoding));
        
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
        
        String roundTrip = clean(textWriter.toString());
        
        assertEquals(
                "Incorrect text in PDF when starting from text in " + encoding,
                checkText, roundTrip
        );
    }

    private String clean(String text)
    {
        text = text.replaceAll("\\s+\\r", "");
        text = text.replaceAll("\\s+\\n", "");
        text = text.replaceAll("\\r", "");
        text = text.replaceAll("\\n", "");
        return text;
    }

    public void testSetUp() throws Exception
    {
        transformer.setPageLimit(-1);
        transformer.register();
        
        super.testSetUp();
    }
    
    public void testAllConversions() throws Exception
    {
        transformer.setPageLimit(-1);
        transformer.register();
        
        super.testAllConversions();
    }
}
