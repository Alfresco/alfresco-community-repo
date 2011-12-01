package org.alfresco.filesys.repo;

import java.io.File;
import java.io.FileOutputStream;

import junit.framework.TestCase;

import org.alfresco.repo.content.filestore.FileContentReader;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.util.TempFileProvider;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.ClassPathResource;

/**
 * Junit test of CIFSContentComparatorTest
 * 
 * @author mrogers
 *
 */
public class CIFSContentComparatorTest extends TestCase
{
   
    private static Log logger = LogFactory.getLog(ContentDiskDriverTest.class);

    protected void setUp() throws Exception
    {
                
    }
    
    @Override
    protected void tearDown() throws Exception
    {
    }
    
    /**
     * Test Basic functionailty with a plain text file
     */
    public void testPlainTextCompareContent() throws Exception
    {
        CIFSContentComparator contentComparator = new CIFSContentComparator();
        contentComparator.init();
        
        /**
         * Test 1
         */
        File file1 = TempFileProvider.createTempFile("testCIFSContentComparator","txt");
        File file2 = TempFileProvider.createTempFile("testCIFSContentComparator","txt");
      
        /**
         * test a couple of empty files
         */
        {
        FileContentReader existingContent = new FileContentReader(file1);
        boolean result = contentComparator.isContentEqual(existingContent, file1);
        assertTrue("compare the same empty file, should be equal", result);
        }

        {
        ContentReader reader = new FileContentReader(file2);
        
        boolean result = contentComparator.isContentEqual(reader, file1);
        assertTrue("compare two empty files, should be equal", result);
        }
        
        /** 
         * Test quick brown fox "text/plain" with itself
         */
        {
        FileOutputStream os1 = new FileOutputStream(file1);
        os1.write("The quick brown fox".getBytes("UTF-8"));
        os1.close();

        ContentReader reader = new FileContentReader(file1);
        reader.setMimetype("text/plain");
        reader.setEncoding("UTF-8");
        boolean result = contentComparator.isContentEqual(reader, file1);
        assertTrue("compare plain text file, should be equal", result);
        }
        
        /**
         * Negative test - compare quick brown fox with empty file
         */
        {
        ContentReader reader = new FileContentReader(file1);
        reader.setMimetype("text/plain");
        reader.setEncoding("UTF-8");
        boolean result = contentComparator.isContentEqual(reader, file2);
        assertTrue("compare plain text file, should not be equal", !result);
        }
        
        /**
         * Compare same length plain text files 
         * 
         * "The quick brown fox" vs. "The quick fox brown"
         */
        {
        FileOutputStream os2 = new FileOutputStream(file2);
        os2.write("The quick fox brown".getBytes("UTF-8"));
        os2.close();
        assertTrue("test error test files different length", file1.length() == file2.length());
        ContentReader reader = new FileContentReader(file1);
        reader.setMimetype("text/plain");
        reader.setEncoding("UTF-8");
        boolean result = contentComparator.isContentEqual(reader, file2);
        assertTrue("compare different text file, should not be equal", !result);
 
        }
    }
    
    public void testProjectFiles() throws Exception
    {
        CIFSContentComparator contentComparator = new CIFSContentComparator();
        contentComparator.init();
        
        ClassPathResource file0Resource = new ClassPathResource("filesys/ContentComparatorTest0.mpp");
        assertNotNull("unable to find test resource filesys/filesys/ContentComparatorTest0.mpp", file0Resource);
        
        ClassPathResource file1Resource = new ClassPathResource("filesys/ContentComparatorTest1.mpp");
        assertNotNull("unable to find test resource filesys/filesys/ContentComparatorTest1.mpp", file1Resource);
        
        ClassPathResource file2Resource = new ClassPathResource("filesys/ContentComparatorTest2.mpp");
        assertNotNull("unable to find test resource filesys/filesys/ContentComparatorTest2.mpp", file1Resource);
         
        File textFile = TempFileProvider.createTempFile("testCIFSContentComparator","txt");
        FileOutputStream os1 = new FileOutputStream(textFile);
        os1.write("The quick brown fox".getBytes("UTF-8"));
        os1.close();

        /**
         * Compare same project file with itself 
         */
        {
            File file1 = file1Resource.getFile();

            ContentReader reader = new FileContentReader(file1);
            reader.setMimetype("application/vnd.ms-project");
            reader.setEncoding("UTF-8");
            boolean result = contentComparator.isContentEqual(reader, file1);
            assertTrue("compare same project file, should be equal", result);
        }
        
        /**
         * Compare project file with plain text file 
         */
        {
            File file1 = file1Resource.getFile();

            ContentReader reader = new FileContentReader(file1);
            reader.setMimetype("application/vnd.ms-project");
            reader.setEncoding("UTF-8");
            boolean result = contentComparator.isContentEqual(reader, textFile);
            assertTrue("compare project file with text file, should not be equal", !result);
        }
        
        /**
         * Compare different project files 
         */
        {
            File file1 = file1Resource.getFile();
            File file2 = file2Resource.getFile();

            ContentReader reader = new FileContentReader(file1);
            reader.setMimetype("application/vnd.ms-project");
            reader.setEncoding("UTF-8");
            boolean result = contentComparator.isContentEqual(reader, file2);
            assertTrue("compare different project file, should not be equal", !result);
        }
        
    }
    
    /**
     * Open and close of a project file changes certain header properties.
     * Test File 1 has been opened and closed.
     * @throws Exception
     */
    public void testProjectTrivialDiffProjectFiles() throws Exception
    {
        CIFSContentComparator contentComparator = new CIFSContentComparator();
        contentComparator.init();
        
        ClassPathResource file0Resource = new ClassPathResource("filesys/ContentComparatorTest0.mpp");
        assertNotNull("unable to find test resource filesys/filesys/ContentComparatorTest0.mpp", file0Resource);
        
        ClassPathResource file1Resource = new ClassPathResource("filesys/ContentComparatorTest1.mpp");
        assertNotNull("unable to find test resource filesys/filesys/ContentComparatorTest1.mpp", file1Resource);
                
        /**
         * Compare trivially different project files, should ignore trivial differences and be equal 
         */
        {
            File file0 = file0Resource.getFile();
            File file1 = file1Resource.getFile();

            ContentReader reader = new FileContentReader(file0);
            reader.setMimetype("application/vnd.ms-project");
            reader.setEncoding("UTF-8");
            boolean result = contentComparator.isContentEqual(reader, file1);
            assertTrue("compare trivially different project file, should be equal", result);
        }
    }



}
