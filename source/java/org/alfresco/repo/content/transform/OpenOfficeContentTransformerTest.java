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
import java.net.URL;

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
    
    /**
     * ALF-219. Transforamtion from .html to .pdf for empty file.
     * @throws Exception
     */
    public void testEmptyHtmlToEmptyPdf() throws Exception
    {
        if (!worker.isAvailable())
        {
            // no connection
            return;
        }
        URL url = this.getClass().getClassLoader().getResource("misc/empty.html");
        assertNotNull("URL was unexpectedly null", url);

        File htmlSourceFile = new File(url.getFile());
        assertTrue("Test file does not exist.", htmlSourceFile.exists());
        
        File pdfTargetFile = TempFileProvider.createTempFile(getName() + "-target-", ".pdf");
        
        ContentReader reader = new FileContentReader(htmlSourceFile);
        reader.setMimetype(MimetypeMap.MIMETYPE_HTML);
        ContentWriter writer = new FileContentWriter(pdfTargetFile);
        writer.setMimetype(MimetypeMap.MIMETYPE_PDF);
        
        transformer.transform(reader, writer);
    }
    
    /**
     * Some transformations fail intermittently within OOo on our test server.
     * Rather than exclude these transformations from product code, where they
     * may work (e.g. due to different OOo version installed), they are excluded
     * from this test.
     */
    @Override
    protected boolean isTransformationExcluded(String sourceExtension, String targetExtension)
    {
        return ((sourceExtension.equals("doc") && targetExtension.equals("docx")) ||
        		(sourceExtension.equals("doc") && targetExtension.equals("html")) ||
        		(sourceExtension.equals("doc") && targetExtension.equals("odt")) ||
        		(sourceExtension.equals("doc") && targetExtension.equals("rtf")) ||
        		(sourceExtension.equals("doc") && targetExtension.equals("sxw")) ||
        		(sourceExtension.equals("doc") && targetExtension.equals("txt")) ||
        		(sourceExtension.equals("docx") && targetExtension.equals("sxw")) ||
        		(sourceExtension.equals("html") && targetExtension.equals("docx")) ||
        		(sourceExtension.equals("odp") && targetExtension.equals("pptx")) ||
        		(sourceExtension.equals("ods") && targetExtension.equals("html")) ||
        		(sourceExtension.equals("ods") && targetExtension.equals("sxc")) ||
        		(sourceExtension.equals("ods") && targetExtension.equals("xlsx")) ||
        		(sourceExtension.equals("ods") && targetExtension.equals("xls")) ||
        		(sourceExtension.equals("odt") && targetExtension.equals("docx")) ||
        		(sourceExtension.equals("odt") && targetExtension.equals("txt")) ||
        		(sourceExtension.equals("ppt") && targetExtension.equals("html")) ||
        		(sourceExtension.equals("ppt") && targetExtension.equals("pptx")) ||
        		(sourceExtension.equals("sxc") && targetExtension.equals("xlsx")) ||
        		(sourceExtension.equals("sxi") && targetExtension.equals("odp")) ||
        		(sourceExtension.equals("sxi") && targetExtension.equals("pptx")) ||
        		(sourceExtension.equals("sxw") && targetExtension.equals("docx")) ||
        		(sourceExtension.equals("txt") && targetExtension.equals("docx")) ||
        		(sourceExtension.equals("txt") && targetExtension.equals("html")) ||
        		(sourceExtension.equals("txt") && targetExtension.equals("odt")) ||
        		(sourceExtension.equals("txt") && targetExtension.equals("pdf")) ||
        		(sourceExtension.equals("txt") && targetExtension.equals("rtf")) ||
        		(sourceExtension.equals("txt") && targetExtension.equals("sxw")) ||
        		(sourceExtension.equals("wpd") && targetExtension.equals("docx")) ||
        		(sourceExtension.equals("xls") && targetExtension.equals("ods")) ||
        		(sourceExtension.equals("xls") && targetExtension.equals("pdf")) ||
        		(sourceExtension.equals("xls") && targetExtension.equals("sxc")) ||
        		(sourceExtension.equals("xls") && targetExtension.equals("xlsx")) ||

        		(sourceExtension.equals("txt") && targetExtension.equals("doc")) ||

        		(sourceExtension.equals("pptx") && targetExtension.equals("html")));
    }

}
