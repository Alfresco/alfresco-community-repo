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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.content.transform;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.content.filestore.FileContentReader;
import org.alfresco.repo.content.filestore.FileContentWriter;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.util.BaseSpringTest;
import org.alfresco.util.TempFileProvider;

/**
 * Provides a base set of tests for testing
 * {@link org.alfresco.repo.content.transform.ContentTransformer}
 * implementations.
 * 
 * @author Derek Hulley
 */
public abstract class AbstractContentTransformerTest extends BaseSpringTest
{
    private static String QUICK_CONTENT = "The quick brown fox jumps over the lazy dog";
    private static String[] QUICK_WORDS = new String[] {
            "quick", "brown", "fox", "jumps", "lazy", "dog"};

    protected MimetypeMap mimetypeMap;

    public final void setMimetypeMap(MimetypeMap mimetypeMap)
    {
        this.mimetypeMap = mimetypeMap;
    }
    
    /**
     * Fetches a transformer to test for a given transformation.  The transformer
     * does not <b>have</b> to be reliable for the given format - if it isn't
     * then it will be ignored.
     * 
     * @param sourceMimetype the sourceMimetype to be tested
     * @param targetMimetype the targetMimetype to be tested
     * @return Returns the <tt>ContentTranslators</tt> that will be tested by
     *      the methods implemented in this class.  A null return value is
     *      acceptable if the source and target mimetypes are not of interest.
     */
    protected abstract ContentTransformer getTransformer(String sourceMimetype, String targetMimetype);

    /**
     * Ensures that the temp locations are cleaned out before the tests start
     */
    @Override
    protected void onSetUpInTransaction() throws Exception
    {
        // perform a little cleaning up
        long now = System.currentTimeMillis();
        TempFileProvider.TempFileCleanerJob.removeFiles(now);
    }

    /**
     * Check that all objects are present
     */
    public void testSetUp() throws Exception
    {
        assertNotNull("MimetypeMap not present", mimetypeMap);
        // check that the quick resources are available
        File sourceFile = AbstractContentTransformerTest.loadQuickTestFile("txt");
        assertNotNull(sourceFile);
    }
    
    /**
     * Helper method to load one of the "The quick brown fox" files from the
     * classpath.
     * 
     * @param extension the extension of the file required
     * @return Returns a test resource loaded from the classpath or <tt>null</tt> if
     *      no resource could be found.
     * @throws IOException
     */
    public static File loadQuickTestFile(String extension) throws IOException
    {
        URL url = AbstractContentTransformerTest.class.getClassLoader().getResource("quick/quick." + extension);
        if (url == null)
        {
            return null;
        }
        File file = new File(url.getFile());
        if (!file.exists())
        {
            return null;
        }
        return file;
    }

    /**
     * Tests the full range of transformations available on the
     * {@link #getTransformer(String, String) transformer} subject to the
     * {@link org.alfresco.util.test.QuickFileTest available test files}
     * and the {@link ContentTransformer#getReliability(String, String) reliability} of
     * the {@link #getTransformer(String, String) transformer} itself.
     * <p>
     * Each transformation is repeated several times, with a transformer being
     * {@link #getTransformer(String, String) requested} for each transformation.  In the
     * case where optimizations are being done around the selection of the most
     * appropriate transformer, different transformers could be used during the iteration
     * process.
     * <p>
     * Results for the transformations are dumped to a temporary file named
     * <b>AbstractContentTransformerTest-results-1234.txt</b>.
     */
    public void testAllConversions() throws Exception
    {
        StringBuilder sb = new StringBuilder(2048);
        sb.append("Mimetype Conversion Tests \n")
          .append("========================= \n")
          .append("   Date: ").append(new Date()).append("\n")
          .append("\n");
        
        // get all mimetypes
        Set<String> mimetypes = new TreeSet<String>(mimetypeMap.getMimetypes());
        for (String sourceMimetype : mimetypes)
        {
            // attempt to get a source file for each mimetype
            String sourceExtension = mimetypeMap.getExtension(sourceMimetype);
            
            sb.append("   Source Extension: ").append(sourceExtension).append("\n");
            
            // attempt to convert to every other mimetype
            for (String targetMimetype : mimetypes)
            {
                ContentWriter targetWriter = null;
                // construct a reader onto the source file
                String targetExtension = mimetypeMap.getExtension(targetMimetype);

                // must we test the transformation?
                ContentTransformer transformer = getTransformer(sourceMimetype, targetMimetype);
                if (transformer == null || transformer.getReliability(sourceMimetype, targetMimetype) <= 0.0)
                {
                    // no transformer
                    continue;
                }

                // dump
                sb.append("      Target Extension: ").append(targetExtension);
                sb.append(" <").append(transformer.getClass().getSimpleName()).append(">");

                // is there a test file for this conversion?
                File sourceFile = AbstractContentTransformerTest.loadQuickTestFile(sourceExtension);
                if (sourceFile == null)
                {
                    sb.append(" <no source test file>\n");
                    continue;  // no test file available for that extension
                }
                ContentReader sourceReader = new FileContentReader(sourceFile);

                // perform the transformation several times so that we get a good idea of performance
                int count = 0;
                long before = System.currentTimeMillis();
                Set<String> transformerClasses = new HashSet<String>(2);
                for (int i = 0; i < 5; i++)
                {
                    // get the transformer repeatedly as it might be different each time around
                    transformer = getTransformer(sourceMimetype, targetMimetype);
                    // must we report on this class?
                    if (!transformerClasses.contains(transformer.getClass().getName()))
                    {
                        transformerClasses.add(transformer.getClass().getName());
                        sb.append(" <").append(transformer.getClass().getSimpleName()).append(">");
                    }

                    // make a writer for the target file
                    File targetFile = TempFileProvider.createTempFile(
                            getClass().getSimpleName() + "_" + getName() + "_" + sourceExtension + "_",
                            "." + targetExtension);
                    targetWriter = new FileContentWriter(targetFile);
                    
                    // do the transformation
                    sourceReader.setMimetype(sourceMimetype);
                    targetWriter.setMimetype(targetMimetype);
                    transformer.transform(sourceReader.getReader(), targetWriter);
                    
                    // if the target format is any type of text, then it must contain the 'quick' phrase
                    if (targetMimetype.equals(MimetypeMap.MIMETYPE_TEXT_PLAIN))
                    {
                        ContentReader targetReader = targetWriter.getReader();
                        String checkContent = targetReader.getContentString();
                        assertTrue("Quick phrase not present in document converted to text: \n" +
                                "   transformer: " + transformer + "\n" +
                                "   source: " + sourceReader + "\n" +
                                "   target: " + targetWriter,
                                checkContent.contains(QUICK_CONTENT));
                    }
                    else if (targetMimetype.startsWith(StringExtractingContentTransformer.PREFIX_TEXT))
                    {
                        ContentReader targetReader = targetWriter.getReader();
                        String checkContent = targetReader.getContentString();
                        // essentially check that FTS indexing can use the conversion properly
                        for (int word = 0; word < QUICK_WORDS.length; word++)
                        {
                            assertTrue("Quick phrase word not present in document converted to text: \n" +
                                    "   transformer: " + transformer + "\n" +
                                    "   source: " + sourceReader + "\n" +
                                    "   target: " + targetWriter + "\n" +
                                    "   word: " + word,
                                    checkContent.contains(QUICK_WORDS[word]));
                        }
                    }
                    // increment count
                    count++;
                }
                long after = System.currentTimeMillis();
                double average = (double) (after - before) / (double) count;
                
                // dump
                sb.append(String.format(" average %10.0f ms", average)).append("\n");
                
                if (logger.isDebugEnabled())
                {
                    logger.debug("Transformation performed " + count + " time: " +
                            sourceMimetype + " --> " + targetMimetype + "\n" +
                            "   source: " + sourceReader + "\n" +
                            "   target: " + targetWriter + "\n" +
                            "   transformer: " + getTransformer(sourceMimetype, targetMimetype));
                }
            }
        }
        
        // dump to file
        File outputFile = TempFileProvider.createTempFile("AbstractContentTransformerTest-results-", ".txt");
        ContentWriter outputWriter = new FileContentWriter(outputFile);
        outputWriter.setEncoding("UTF8");
        outputWriter.putContent(sb.toString());
    }
}
