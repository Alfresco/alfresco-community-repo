/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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

import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.content.filestore.FileContentReader;
import org.alfresco.repo.content.filestore.FileContentWriter;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.TransformationOptions;
import org.alfresco.util.TempFileProvider;

/**
 * Tests for {@link PdfBoxPdfToImageContentTransformer}.
 * 
 * @author Neil Mc Erlean
 * @since 3.4.2.
 */
public class PdfBoxPdfToImageContentTransformerTest extends AbstractContentTransformerTest
{
    private ContentTransformer transformer;
    
    @Override
    public void setUp() throws Exception
    {
        super.setUp();
        
        transformer = new PdfBoxPdfToImageContentTransformer();
        ((ContentTransformerHelper)transformer).setMimetypeService(mimetypeService);
    }
    
    /**
     * @return Returns the same transformer regardless - it is allowed
     */
    protected ContentTransformer getTransformer(String sourceMimetype, String targetMimetype)
    {
        return transformer;
    }
    
    public void testIsTransformable() throws Exception
    {
        assertTrue(transformer.isTransformable(MimetypeMap.MIMETYPE_PDF, MimetypeMap.MIMETYPE_IMAGE_PNG, new TransformationOptions()));
    }
    
    /**
     * This test method checks that the PDFBox-based transformer is able to extract image content from a secured PDF file.
     * See ALF-6650.
     * 
     * @since 3.4.2
     */
    public void testExtractContentFromSecuredPdf() throws Exception
    {
        File securePdfFile = loadNamedQuickTestFile("quick-secured.pdf");
        assertNotNull("test file was null.", securePdfFile);
        
        ContentReader reader = new FileContentReader(securePdfFile);
        reader.setMimetype(MimetypeMap.MIMETYPE_PDF);
        reader.setEncoding("UTF-8");
        
        ContentWriter writer = new FileContentWriter(TempFileProvider.createTempFile(this.getClass().getSimpleName() + System.currentTimeMillis(), "txt"));
        writer.setMimetype(MimetypeMap.MIMETYPE_IMAGE_PNG);
        writer.setEncoding("UTF-8");
        
        transformer.transform(reader, writer);
        
        // get a reader onto the transformed content and check - although the real test here is that exceptions weren't thrown during transformation.
        ContentReader checkReader = writer.getReader();
        checkReader.setMimetype(MimetypeMap.MIMETYPE_IMAGE_PNG);
        assertTrue("PNG output was empty", checkReader.getContentData().getSize() != 0l);
    }

    /**
     * This test method checks that the PDFBox-based transformer is able to transform an Adobe Illustrator file to image.
     * Adobe Illustrator files (.ai) have been PostScript files in the past, but are now just pdf files.
     * 
     * @since 3.5.0
     */
    public void testTransformAdobeIllustrator() throws Exception
    {
        for (String quickFile : new String[]{"quickCS3.ai", "quickCS5.ai"})
        {
            File aiFile = loadNamedQuickTestFile(quickFile);
            assertNotNull("test file was null.", aiFile);
            ContentReader reader = new FileContentReader(aiFile);
            reader.setMimetype(MimetypeMap.MIMETYPE_APPLICATION_ILLUSTRATOR);
            reader.setEncoding("UTF-8");
            ContentWriter writer = new FileContentWriter(TempFileProvider
                    .createTempFile(this.getClass().getSimpleName()
                            + System.currentTimeMillis(), "txt"));
            writer.setMimetype(MimetypeMap.MIMETYPE_IMAGE_PNG);
            writer.setEncoding("UTF-8");
            transformer.transform(reader, writer);
            // get a reader onto the transformed content and check - although the real test here is that exceptions weren't thrown during transformation.
            ContentReader checkReader = writer.getReader();
            checkReader.setMimetype(MimetypeMap.MIMETYPE_IMAGE_PNG);
            assertTrue("PNG output was empty", checkReader.getContentData()
                    .getSize() != 0l);
        }
    }
}
