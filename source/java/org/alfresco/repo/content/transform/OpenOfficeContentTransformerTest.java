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

import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.content.filestore.FileContentReader;
import org.alfresco.repo.content.filestore.FileContentWriter;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.TransformationOptions;
import org.alfresco.util.TempFileProvider;

/**
 * @see org.alfresco.repo.content.transform.OpenOfficeContentTransformerWorker
 * 
 * @author Derek Hulley
 */
public class OpenOfficeContentTransformerTest extends AbstractContentTransformerTest
{
    private static String MIMETYPE_RUBBISH = "text/rubbish";
    
    private ContentTransformerWorker worker;
    private ContentTransformer transformer;
    
    @Override
    public void setUp() throws Exception
    {
        super.setUp();
        
        this.worker = (ContentTransformerWorker) ctx.getBean("transformer.worker.OpenOffice");
        ProxyContentTransformer transformer = new ProxyContentTransformer();
        transformer.setMimetypeService(mimetypeService);
        transformer.setWorker(this.worker);
        this.transformer = transformer;
    }
    
    /**
     * @return Returns the same transformer regardless - it is allowed
     */
    protected ContentTransformer getTransformer(String sourceMimetype, String targetMimetype)
    {
        return transformer;
    }

    public void testSetUp() throws Exception
    {
        super.testSetUp();
        assertNotNull(mimetypeService);
    }
    
    public void testReliability() throws Exception
    {
        if (!worker.isAvailable())
        {
            // no connection
            return;
        }
        boolean reliability = transformer.isTransformable(MIMETYPE_RUBBISH, MimetypeMap.MIMETYPE_TEXT_PLAIN, new TransformationOptions());
        assertEquals("Mimetype should not be supported", false, reliability);
        reliability = transformer.isTransformable(MimetypeMap.MIMETYPE_TEXT_PLAIN, MIMETYPE_RUBBISH, new TransformationOptions());
        assertEquals("Mimetype should not be supported", false, reliability);
        reliability = transformer.isTransformable(MimetypeMap.MIMETYPE_TEXT_PLAIN, MimetypeMap.MIMETYPE_XHTML, new TransformationOptions());
        assertEquals("Mimetype should not be supported", false, reliability);
        reliability = transformer.isTransformable(MimetypeMap.MIMETYPE_TEXT_PLAIN, MimetypeMap.MIMETYPE_WORD, new TransformationOptions());
        assertEquals("Mimetype should be supported", true, reliability);
        reliability = transformer.isTransformable(MimetypeMap.MIMETYPE_WORD, MimetypeMap.MIMETYPE_TEXT_PLAIN, new TransformationOptions());
        assertEquals("Mimetype should be supported", true, reliability);
    }
    
    /**
     * Test what is up with HTML to PDF
     */
    public void testHtmlToPdf() throws Exception
    {
        if (!worker.isAvailable())
        {
            // no connection
            return;
        }
        File htmlSourceFile = loadQuickTestFile("html");
        File pdfTargetFile = TempFileProvider.createTempFile(getName() + "-target-", ".pdf");
        ContentReader reader = new FileContentReader(htmlSourceFile);
        reader.setMimetype(MimetypeMap.MIMETYPE_HTML);
        ContentWriter writer = new FileContentWriter(pdfTargetFile);
        writer.setMimetype(MimetypeMap.MIMETYPE_PDF);
        
        transformer.transform(reader, writer);
    }
}
