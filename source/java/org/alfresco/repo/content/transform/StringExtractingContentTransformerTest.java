/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.repo.content.transform;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Random;

import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.content.filestore.FileContentReader;
import org.alfresco.repo.content.filestore.FileContentWriter;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.util.TempFileProvider;

/**
 * @see org.alfresco.repo.content.transform.StringExtractingContentTransformer
 * 
 * @author Derek Hulley
 */
public class StringExtractingContentTransformerTest extends AbstractContentTransformerTest
{
    private static final String SOME_CONTENT = "azAz10!�$%^&*()\t\r\n";
    
    private ContentTransformer transformer;
    /** the final destination of transformations */
    private ContentWriter targetWriter;
    
    @Override
    public void onSetUpInTransaction() throws Exception
    {
        transformer = new StringExtractingContentTransformer();
        targetWriter = new FileContentWriter(getTempFile());
        targetWriter.setMimetype("text/plain");
        targetWriter.setEncoding("UTF-8");
    }
    
    protected ContentTransformer getTransformer(String sourceMimetype, String targetMimetype)
    {
        return transformer;
    }

    public void testSetUp() throws Exception
    {
        assertNotNull(transformer);
    }
    
    /**
     * @return Returns a new temp file
     */
    private File getTempFile()
    {
        return TempFileProvider.createTempFile(getName(), ".txt");
    }
    
    /**
     * Writes some content using the mimetype and encoding specified.
     * 
     * @param mimetype
     * @param encoding
     * @return Returns a reader onto the newly written content
     */
    private ContentReader writeContent(String mimetype, String encoding)
    {
        ContentWriter writer = new FileContentWriter(getTempFile());
        writer.setMimetype(mimetype);
        writer.setEncoding(encoding);
        // put content
        writer.putContent(SOME_CONTENT);
        // return a reader onto the new content
        return writer.getReader();
    }
    
    public void testDirectTransform() throws Exception
    {
        ContentReader reader = writeContent("text/plain", "latin1");
        
        // check reliability
        double reliability = transformer.getReliability(reader.getMimetype(), targetWriter.getMimetype());
        assertEquals("Reliability incorrect", 1.0, reliability);   // plain text to plain text is 100%
        
        // transform
        transformer.transform(reader, targetWriter);
        
        // get a reader onto the transformed content and check
        ContentReader checkReader = targetWriter.getReader();
        String checkContent = checkReader.getContentString();
        assertEquals("Content check failed", SOME_CONTENT, checkContent);
    }
    
    public void testInterTextTransform() throws Exception
    {
        ContentReader reader = writeContent("text/xml", "UTF-16");
        
        // check reliability
        double reliability = transformer.getReliability(reader.getMimetype(), targetWriter.getMimetype());
        assertEquals("Reliability incorrect", 0.1, reliability);   // markup to plain text not 100%
        
        // transform
        transformer.transform(reader, targetWriter);
        
        // get a reader onto the transformed content and check
        ContentReader checkReader = targetWriter.getReader();
        String checkContent = checkReader.getContentString();
        assertEquals("Content check failed", SOME_CONTENT, checkContent);
    }
    
    /**
     * Generate a large file and then transform it using the text extractor.
     * We are not creating super-large file (1GB) in order to test the transform
     * as it takes too long to create the file in the first place.  Rather,
     * this test can be used during profiling to ensure that memory is not
     * being consumed.
     */
    public void testLargeFileStreaming() throws Exception
    {
        File sourceFile = TempFileProvider.createTempFile(getName(), ".txt");
        
        int chars = 1000000;  // a million characters should do the trick
        Random random = new Random();
        
        Writer charWriter = new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(sourceFile)));
        for (int i = 0; i < chars; i++)
        {
            char next = (char)(random.nextDouble() * 93D + 32D);
            charWriter.write(next);
        }
        charWriter.close();
        
        // get a reader and a writer
        ContentReader reader = new FileContentReader(sourceFile);
        reader.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
        
        File outputFile = TempFileProvider.createTempFile(getName(), ".txt");
        ContentWriter writer = new FileContentWriter(outputFile);
        writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
        
        // transform
        transformer.transform(reader, writer);
        
        // delete files
        sourceFile.delete();
        outputFile.delete();
    }
}
