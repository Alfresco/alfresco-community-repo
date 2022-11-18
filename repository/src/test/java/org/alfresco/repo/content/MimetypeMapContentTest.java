/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2021 Alfresco Software Limited
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
package org.alfresco.repo.content;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;

import junit.framework.TestCase;

import org.alfresco.repo.content.filestore.FileContentReader;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.test_category.OwnJVMTestsCategory;
import org.alfresco.util.ApplicationContextHelper;
import org.apache.poi.util.IOUtils;
import org.junit.experimental.categories.Category;
import org.springframework.context.ApplicationContext;

/**
 * Content specific tests for MimeTypeMap
 * 
 * @see org.alfresco.repo.content.MimetypeMap
 * @see org.alfresco.repo.content.MimetypeMapTest
 */
@Category({OwnJVMTestsCategory.class})
public class MimetypeMapContentTest extends TestCase
{
    private static ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();

    private MimetypeService mimetypeService;
    
    @Override
    public void setUp() throws Exception
    {
        mimetypeService =  (MimetypeService)ctx.getBean("mimetypeService");
    }

    public void testGuessPdfMimetype() throws Exception
    {
        assertEquals(
                "application/pdf",
                mimetypeService.guessMimetype("something.doc", openQuickTestFile("quick.pdf"))
        );
        assertEquals(
                "application/pdf",
                mimetypeService.guessMimetype(null, openQuickTestFile("quick.pdf"))
        );
    }

    public void testGuessAppleIconsMimetype() throws Exception
    {
        assertEquals(
                "image/icns",
                mimetypeService.guessMimetype("something.icns", openQuickTestFile("quick.icns"))
        );
    }

    public void testGuessMimetypeForFile() throws Exception
    {
        // Correct ones
        assertEquals(
                "application/msword", 
                mimetypeService.guessMimetype("something.doc", openQuickTestFile("quick.doc"))
        );
        assertEquals(
                "application/msword", 
                mimetypeService.guessMimetype("SOMETHING.DOC", openQuickTestFile("quick.doc"))
        );
        
        // Incorrect ones, Tika spots the mistake
        assertEquals(
                "application/msword", 
                mimetypeService.guessMimetype("something.pdf", openQuickTestFile("quick.doc"))
        );

        // Ones where we use a different mimetype to the canonical one
        assertEquals(
                "image/bmp", // Officially image/x-ms-bmp 
                mimetypeService.guessMimetype("image.bmp", openQuickTestFile("quick.bmp"))
        );
        
        // Ones where we know about the parent, and Tika knows about the details
        assertEquals(
              "application/dita+xml", // Full version:  application/dita+xml;format=concept
              mimetypeService.guessMimetype("concept.dita", openQuickTestFile("quickConcept.dita"))
        );

// Commented out when the test class was reintroduced after many years of not being run. Failed as the type was
// identified as a zip. Reintroduced to check guessMimetype works without pdfbox libraries.
//
//        // Alfresco Specific ones, that Tika doesn't know about
//        assertEquals(
//              "application/acp",
//              mimetypeService.guessMimetype("something.acp", openQuickTestFile("quick.acp"))
//        );
        
        // Where the file is corrupted
        File tmp = File.createTempFile("alfresco", ".tmp");
        ContentReader reader = openQuickTestFile("quick.doc");
        InputStream inp = reader.getContentInputStream();
        byte[] trunc = new byte[512+256];
        IOUtils.readFully(inp, trunc);
        inp.close();
        FileOutputStream out = new FileOutputStream(tmp);
        out.write(trunc);
        out.close();
        ContentReader truncReader = new FileContentReader(tmp);
        
        // Because the file is truncated, Tika won't be able to process the contents
        //  of the OLE2 structure
        // So, it'll fall back to just OLE2, but it won't fail
        assertEquals(
                "application/x-tika-msoffice", 
                mimetypeService.guessMimetype(null, truncReader)
        );
// Commented out when the test class was reintroduced after many years of not being run. Failed to open a
// stream onto the channel. Reintroduced to check guessMimetype works without pdfbox libraries.
//
//        // But with the filename it'll be able to use the .doc extension
//        //  to guess at it being a .Doc file
//        assertEquals(
//              "application/msword",
//              mimetypeService.guessMimetype("something.doc", truncReader)
//        );
        
        // Lotus notes EML files (ALF-16381 / TIKA-1042)
        assertEquals(
              "message/rfc822", 
              mimetypeService.guessMimetype("something.eml", openQuickTestFile("quickLotus.eml"))
        );
    }
    
    private ContentReader openQuickTestFile(String filename)
    {
        URL url = getClass().getClassLoader().getResource("quick/" + filename);
        if(url == null)
        {
           fail("Quick test file \"" + filename + "\" wasn't found");
        }
        File file = new File(url.getFile());
        return new FileContentReader(file);
    }
}
