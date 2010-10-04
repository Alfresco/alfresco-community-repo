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
import java.io.IOException;

import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.content.filestore.FileContentReader;
import org.alfresco.repo.content.filestore.FileContentWriter;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.TransformationOptions;

/**
 * Test class for ArchiveContentTransformer.
 * 
 * @see org.alfresco.repo.content.transform.ArchiveContentTransformer
 * 
 * @author Neil McErlean
 */
public class ArchiveContentTransformerTest extends AbstractContentTransformerTest
{
    private ArchiveContentTransformer transformer;
    
    @Override
    public void setUp() throws Exception
    {
        super.setUp();
        
        transformer = new ArchiveContentTransformer();
    }

    protected ContentTransformer getTransformer(String sourceMimetype, String targetMimetype)
    {
        return transformer;
    }
    
    public void testIsTransformable() throws Exception
    {
        assertTrue(transformer.isTransformable(MimetypeMap.MIMETYPE_ZIP, MimetypeMap.MIMETYPE_TEXT_PLAIN, new TransformationOptions()));
        assertTrue(transformer.isTransformable("application/x-tar", MimetypeMap.MIMETYPE_TEXT_PLAIN, new TransformationOptions()));
        assertTrue(transformer.isTransformable("application/x-gtar", MimetypeMap.MIMETYPE_TEXT_PLAIN, new TransformationOptions()));
    }

    @Override
    protected boolean isQuickPhraseExpected(String targetMimetype)
    {
       // The Zip transformer produces names of the entries, not their contents.
       return false;
    }

    @Override
    protected boolean isQuickWordsExpected(String targetMimetype)
    {
       // The Zip transformer produces names of the entries, not their contents.
       return false;
    }
    
    public void testRecursing() throws Exception
    {
       ContentWriter writer;
       String contents;
       
       // Bean off, no options
       transformer.setIncludeContents("FALSE");
       
       writer = getTestWriter();
       transformer.transform(getTestReader(), writer);
       contents = writer.getReader().getContentString();
       testHasFiles(contents);
       testNested(contents, false);
       
       
       // Bean on, no options
       transformer.setIncludeContents("TRUE");

       writer = getTestWriter();
       transformer.transform(getTestReader(), writer);
       contents = writer.getReader().getContentString();
       testHasFiles(contents);
       testNested(contents, true);


       // Bean off, Transformation Options off
       TransformationOptions options = new TransformationOptions();
       transformer.setIncludeContents("FALSE");
       
       writer = getTestWriter();
       transformer.transform(getTestReader(), writer, options);
       contents = writer.getReader().getContentString();
       testHasFiles(contents);
       testNested(contents, false);

       
       // Bean on, Transformation Options off
       transformer.setIncludeContents("TRUE");

       writer = getTestWriter();
       transformer.transform(getTestReader(), writer, options);
       contents = writer.getReader().getContentString();
       testHasFiles(contents);
       testNested(contents, true);
       
       
       // Bean off, Transformation Options on - options win
       options.setIncludeEmbedded(true);
       transformer.setIncludeContents("FALSE");
       
       writer = getTestWriter();
       transformer.transform(getTestReader(), writer, options);
       contents = writer.getReader().getContentString();
       testHasFiles(contents);
       testNested(contents, true);
       
       
       // Bean on, Transformation Options on
       transformer.setIncludeContents("TRUE");

       writer = getTestWriter();
       transformer.transform(getTestReader(), writer, options);
       contents = writer.getReader().getContentString();
       testHasFiles(contents);
       testNested(contents, true);
    }
    private ContentReader getTestReader() throws IOException {
       ContentReader sourceReader = new FileContentReader(
             loadQuickTestFile("zip")
       );
       sourceReader.setMimetype(MimetypeMap.MIMETYPE_ZIP);
       return sourceReader;
    }
    private ContentWriter getTestWriter() throws IOException {
       ContentWriter writer = new FileContentWriter(File.createTempFile("test", ".txt"));
       writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
       return writer;
    }
    private void testHasFiles(String contents) 
    {
       assertTrue("Files not found in " + contents,
             contents.contains("quick.txt"));
       assertTrue("Files not found in " + contents,
             contents.contains("quick.doc"));
       assertTrue("Files not found in " + contents,
             contents.contains("subfolder/quick.jpg"));
    }
    private void testNested(String contents, boolean shouldHaveRecursed)
    {
       assertEquals(
             "Recursion was " + shouldHaveRecursed + 
             " but content was " + contents,
             shouldHaveRecursed,
             contents.contains("The quick brown fox jumps over the lazy dog")
       );
       assertEquals(
             "Recursion was " + shouldHaveRecursed + 
             " but content was " + contents,
             shouldHaveRecursed,
             contents.contains("Le renard brun rapide saute par-dessus le chien paresseux")
       );
    }
}
