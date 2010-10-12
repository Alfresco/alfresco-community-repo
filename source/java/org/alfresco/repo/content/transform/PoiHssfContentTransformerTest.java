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
import java.io.InputStream;

import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.content.filestore.FileContentReader;
import org.alfresco.repo.content.filestore.FileContentWriter;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.TransformationOptions;
import org.alfresco.util.TempFileProvider;

/**
 * @see org.alfresco.repo.content.transform.PoiHssfContentTransformer
 * 
 * @author Derek Hulley
 */
public class PoiHssfContentTransformerTest extends TikaPoweredContentTransformerTest
{
    private ContentTransformer transformer;
    
    @Override
    public void setUp() throws Exception
    {
        super.setUp();
        
        transformer = new PoiHssfContentTransformer();
    }
    
    @Override
    protected String[] getQuickFilenames(String sourceMimetype) {
      return new String[] {
            "quick.xls", "quick.xlsx"
      };
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
        assertFalse(transformer.isTransformable(MimetypeMap.MIMETYPE_TEXT_PLAIN, MimetypeMap.MIMETYPE_EXCEL, new TransformationOptions()));
        assertTrue(transformer.isTransformable(MimetypeMap.MIMETYPE_EXCEL, MimetypeMap.MIMETYPE_TEXT_PLAIN, new TransformationOptions()));
        assertTrue(transformer.isTransformable(MimetypeMap.MIMETYPE_EXCEL, MimetypeMap.MIMETYPE_TEXT_CSV, new TransformationOptions()));
        assertTrue(transformer.isTransformable(MimetypeMap.MIMETYPE_EXCEL, MimetypeMap.MIMETYPE_HTML, new TransformationOptions()));
        assertTrue(transformer.isTransformable(MimetypeMap.MIMETYPE_EXCEL, MimetypeMap.MIMETYPE_XML, new TransformationOptions()));
    }
    
    public void testCsvOutput() throws Exception
    {
       File sourceFile = AbstractContentTransformerTest.loadQuickTestFile("xls");
       ContentReader sourceReader = new FileContentReader(sourceFile);

       File targetFile = TempFileProvider.createTempFile(
             getClass().getSimpleName() + "_" + getName() + "_xls_",
             ".csv");
       ContentWriter targetWriter = new FileContentWriter(targetFile);
       
       sourceReader.setMimetype(MimetypeMap.MIMETYPE_EXCEL);
       targetWriter.setMimetype(MimetypeMap.MIMETYPE_TEXT_CSV);
       transformer.transform(sourceReader, targetWriter);
       
       ContentReader targetReader = targetWriter.getReader();
       String checkContent = targetReader.getContentString();
       
       additionalContentCheck(
             MimetypeMap.MIMETYPE_EXCEL, 
             MimetypeMap.MIMETYPE_TEXT_CSV, 
             checkContent
       );
    }
    
    @Override
    protected void additionalContentCheck(String sourceMimetype,
         String targetMimetype, String contents) {
       if(targetMimetype.equals(MimetypeMap.MIMETYPE_TEXT_CSV)) {
          assertTrue(
                "Content not properly CSV'd",
                contents.contains("1,2,2")
          );
          assertTrue(
                "Content not properly CSV'd",
                contents.contains("\"The\",\"quick\",\"brown\",\"fox\"")
          );
       } else if(targetMimetype.equals(MimetypeMap.MIMETYPE_XML)) {
          // First check we got the usual bits
          super.additionalContentCheck(sourceMimetype, targetMimetype, contents);
          
          // Now check tables came out correctly
          assertTrue(
                "Content lacks XHTML table tags:\n" + contents,
                contents.contains("<table>")
          );
          assertTrue(
                "Content lacks XHTML table tags:\n" + contents,
                contents.contains("<tr>")
          );
          assertTrue(
                "Content lacks XHTML table tags:\n" + contents,
                contents.contains("<td>1</td>")
          );
          assertTrue(
                "Content lacks XHTML table tags:\n" + contents,
                contents.contains("<td>The</td>")
          );
       } else {
          super.additionalContentCheck(sourceMimetype, targetMimetype, contents);
       }
    }

    @Override
    protected boolean isQuickPhraseExpected(String targetMimetype) {
       if(targetMimetype.equals(MimetypeMap.MIMETYPE_TEXT_CSV)) {
          return true;
       }
       return super.isQuickPhraseExpected(targetMimetype);
    }

   /**
     * Tests a specific failure in the library
     */
    public void xxtestBugFixAR114() throws Exception
    {
        File tempFile = TempFileProvider.createTempFile(
                getClass().getSimpleName() + "_" + getName() + "_",
                ".xls");
        FileContentWriter writer = new FileContentWriter(tempFile);
        writer.setMimetype(MimetypeMap.MIMETYPE_EXCEL);
        // get the test resource and write it (Excel)
        InputStream is = getClass().getClassLoader().getResourceAsStream("Plan270904b.xls");
        assertNotNull("Test resource not found: Plan270904b.xls");
        writer.putContent(is);
        
        // get the source of the transformation
        ContentReader reader = writer.getReader(); 
        
        // make a new location of the transform output (plain text)
        tempFile = TempFileProvider.createTempFile(
                getClass().getSimpleName() + "_" + getName() + "_",
                ".txt");
        writer = new FileContentWriter(tempFile);
        writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
        
        // transform it
        transformer.transform(reader, writer);
    }
}
