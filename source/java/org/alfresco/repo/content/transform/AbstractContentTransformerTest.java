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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import junit.framework.TestCase;

import org.alfresco.repo.content.ContentMinimalContextTestSuite;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.content.filestore.FileContentReader;
import org.alfresco.repo.content.filestore.FileContentWriter;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.TempFileProvider;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;

/**
 * Provides a base set of tests for testing
 * {@link org.alfresco.repo.content.transform.ContentTransformer}
 * implementations.
 * 
 * @author Derek Hulley
 */
public abstract class AbstractContentTransformerTest extends TestCase
{
    protected static String QUICK_CONTENT = "The quick brown fox jumps over the lazy dog";
    private static String[] QUICK_WORDS = new String[] {
            "quick", "brown", "fox", "jumps", "lazy", "dog"};
    
    private static Log logger = LogFactory.getLog(AbstractContentTransformerTest.class);
    
    /**
     * This context will be fetched each time, but almost always
     *  will have been cached by {@link ApplicationContextHelper}
     */
    protected ApplicationContext ctx;

    protected ServiceRegistry serviceRegistry;
    protected MimetypeService mimetypeService;

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
    protected void setUp() throws Exception
    {
        // Grab a suitably configured context
        ctx = ContentMinimalContextTestSuite.getContext();

        // Grab other useful beans
        serviceRegistry = (ServiceRegistry) ctx.getBean(ServiceRegistry.SERVICE_REGISTRY);
        mimetypeService = serviceRegistry.getMimetypeService();
        // perform a little cleaning up
        long now = System.currentTimeMillis();
        TempFileProvider.TempFileCleanerJob.removeFiles(now);
    }

    /**
     * Check that all objects are present
     */
    public void testSetUp() throws Exception
    {
        assertNotNull("MimetypeMap not present", mimetypeService);
        // check that the quick resources are available
        File sourceFile = AbstractContentTransformerTest.loadQuickTestFile("txt");
        assertNotNull(sourceFile);
    }
    
    /**
     * Helper method to load one of the "The quick brown fox" files from the
     * classpath.
     * 
     * @param the file required, eg <b>quick.txt</b>
     * @return Returns a test resource loaded from the classpath or <tt>null</tt> if
     *      no resource could be found.
     * @throws IOException
     */
    public static File loadNamedQuickTestFile(String quickname) throws IOException
    {
        URL url = AbstractContentTransformerTest.class.getClassLoader().getResource("quick/" + quickname);
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
     * Helper method to load one of the "The quick brown fox" files from the
     * classpath.
     * 
     * @param the file extension required, eg <b>txt</b> for the file quick.txt
     * @return Returns a test resource loaded from the classpath or <tt>null</tt> if
     *      no resource could be found.
     * @throws IOException
     */
    public static File loadQuickTestFile(String extension) throws IOException
    {
       return loadNamedQuickTestFile("quick."+extension);
    }
    
    /**
     * For the given mime type, returns one or more quick*
     *  files to be tested.
     * By default this is just quick + the default extension.
     * However, you can override this if you need special
     *  rules, eg quickOld.foo, quickMid.foo and quickNew.foo
     *  for differing versions of the file format.
     */
    protected String[] getQuickFilenames(String sourceMimetype) {
       String sourceExtension = mimetypeService.getExtension(sourceMimetype);
       return new String[] {
             "quick." + sourceExtension
       };
    }
    
    /**
     * Writes the supplied text out to a temporary file, and opens
     *  a content reader onto it. 
     */
    protected static ContentReader buildContentReader(String text, Charset encoding)
        throws IOException
    {
        File tmpFile = TempFileProvider.createTempFile("AlfrescoTest_", ".txt");
        FileOutputStream out = new FileOutputStream(tmpFile);
        OutputStreamWriter wout = new OutputStreamWriter(out, encoding);
        wout.write(text);
        wout.close();
        out.close();
        
        ContentReader reader = new FileContentReader(tmpFile);
        reader.setEncoding(encoding.displayName());
        reader.setMimetype("text/plain");
        return reader;
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
        Set<String> mimetypes = new TreeSet<String>(mimetypeService.getMimetypes());
        for (String sourceMimetype : mimetypes)
        {
            // attempt to get a source file for each mimetype
            String[] quickFiles = getQuickFilenames(sourceMimetype);
            sb.append("   Source Files: ").append(quickFiles).append("\n");

            for (String quickFile : quickFiles)
            {
               String sourceExtension = quickFile.substring(quickFile.lastIndexOf('.')+1);
               
               // attempt to convert to every other mimetype
               for (String targetMimetype : mimetypes)
               {
               	if (sourceMimetype.equals(targetMimetype))
               	{
               		// Don't test like-to-like transformations
               		continue;
               	}
                   ContentWriter targetWriter = null;
                   // construct a reader onto the source file
                   String targetExtension = mimetypeService.getExtension(targetMimetype);
                   
                   // must we test the transformation?
                   ContentTransformer transformer = getTransformer(sourceMimetype, targetMimetype);
                   if (transformer == null || transformer.isTransformable(sourceMimetype, targetMimetype, null) == false)
                   {
                       // no transformer
                       continue;
                   }
                   
                   if (isTransformationExcluded(sourceExtension, targetExtension))
                   {
                   	continue;
                   }
   
                   // dump
                   sb.append("      Target Extension: ").append(targetExtension);
                   sb.append(" <").append(transformer.getClass().getSimpleName()).append(">");
   
                   // is there a test file for this conversion?
                   File sourceFile = AbstractContentTransformerTest.loadNamedQuickTestFile(quickFile);
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
                       if (isQuickPhraseExpected(targetMimetype))
                       {
                           ContentReader targetReader = targetWriter.getReader();
                           String checkContent = targetReader.getContentString();
                           assertTrue("Quick phrase not present in document converted to text: \n" +
                                   "   transformer: " + transformer + "\n" +
                                   "   source: " + sourceReader + "\n" +
                                   "   target: " + targetWriter,
                                   checkContent.contains(QUICK_CONTENT));
                           
                           // Let subclasses do extra checks if they want
                           additionalContentCheck(sourceMimetype, targetMimetype, checkContent);
                       }
                       else if (isQuickWordsExpected(targetMimetype))
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
        }
        
        // dump to file
        File outputFile = TempFileProvider.createTempFile("AbstractContentTransformerTest-results-", ".txt");
        ContentWriter outputWriter = new FileContentWriter(outputFile);
        outputWriter.setEncoding("UTF8");
        outputWriter.putContent(sb.toString());
    }
    
    /**
     * Allows implementations to do some extra checks on the 
     *  results of the content as found by 
     *  {@link #testAllConversions()}
     */
    protected void additionalContentCheck(String sourceMimetype, String targetMimetype, String contents) {}

    /**
     * This method is an extension point for enabling/disabling an assertion that the "quick brown fox"
     * phrase is present in the transformed content.
     * By default, the phrase is expected in all text/plain outputs.
     * 
     * @param targetMimetype mimetype of the target of the transformation
     * @return <code>true</code> if phrase is expected else <code>false</code>.
     */
    protected boolean isQuickPhraseExpected(String targetMimetype)
    {
        return targetMimetype.equals(MimetypeMap.MIMETYPE_TEXT_PLAIN);
    }
         
    /**
     * This method is an extension point for enabling/disabling an assertion that the "quick brown fox"
     * words are <i>each</i> present in the transformed content.
     * By default, the words in the phrase are expected in all text/* outputs.
     * 
     * @param targetMimetype mimetype of the target of the transformation
     * @return <code>true</code> if each word is expected else <code>false</code>.
     */
     protected boolean isQuickWordsExpected(String targetMimetype)
     {
         return targetMimetype.startsWith(StringExtractingContentTransformer.PREFIX_TEXT);
     }

    /**
     * This method is an extension point for excluding certain transformations in a subclass.
     * The default implementation returns <code>false</code> for all mime type pairs.
     * 
     * @param sourceExtension
     * @param targetExtension
     * @return
     */
    protected boolean isTransformationExcluded(String sourceExtension, String targetExtension)
    {
    	return false;
    }
}
