/*
 * Copyright (C) 2005 Alfresco, Inc.
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.alfresco.repo.content.transform;

import java.io.File;

import net.sf.jooreports.openoffice.connection.OpenOfficeConnection;

import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.content.filestore.FileContentReader;
import org.alfresco.repo.content.filestore.FileContentWriter;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.util.TempFileProvider;

/**
 * @see org.alfresco.repo.content.transform.OpenOfficeContentTransformer
 * 
 * @author Derek Hulley
 */
public class OpenOfficeContentTransformerTest extends AbstractContentTransformerTest
{
    private static String MIMETYPE_RUBBISH = "text/rubbish";
    
    private OpenOfficeContentTransformer transformer;
    
    public void onSetUpInTransaction() throws Exception
    {
        OpenOfficeConnection connection = (OpenOfficeConnection) applicationContext.getBean("openOfficeConnection");
        
        transformer = new OpenOfficeContentTransformer();
        transformer.setMimetypeService(mimetypeMap);
        transformer.setConnection(connection);
        transformer.setDocumentFormatsConfiguration("classpath:alfresco/mimetype/openoffice-document-formats.xml");
        transformer.register();
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
        assertNotNull(mimetypeMap);
    }
    
    public void testReliability() throws Exception
    {
        if (!transformer.isConnected())
        {
            // no connection
            return;
        }
        double reliability = 0.0;
        reliability = transformer.getReliability(MIMETYPE_RUBBISH, MimetypeMap.MIMETYPE_TEXT_PLAIN);
        assertEquals("Mimetype should not be supported", 0.0, reliability);
        reliability = transformer.getReliability(MimetypeMap.MIMETYPE_TEXT_PLAIN, MIMETYPE_RUBBISH);
        assertEquals("Mimetype should not be supported", 0.0, reliability);
        reliability = transformer.getReliability(MimetypeMap.MIMETYPE_TEXT_PLAIN, MimetypeMap.MIMETYPE_XHTML);
        assertEquals("Mimetype should not be supported", 0.0, reliability);
        reliability = transformer.getReliability(MimetypeMap.MIMETYPE_TEXT_PLAIN, MimetypeMap.MIMETYPE_WORD);
        assertEquals("Mimetype should be supported", 1.0, reliability);
        reliability = transformer.getReliability(MimetypeMap.MIMETYPE_WORD, MimetypeMap.MIMETYPE_TEXT_PLAIN);
        assertEquals("Mimetype should be supported", 1.0, reliability);
    }
    
    /**
     * Test what is up with HTML to PDF
     */
    public void testHtmlToPdf() throws Exception
    {
        if (!transformer.isConnected())
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
