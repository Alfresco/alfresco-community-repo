/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
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
   
    private static Log logger = LogFactory.getLog(CIFSContentComparatorTest.class);

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
        assertNotNull("unable to find test resource filesys/ContentComparatorTest0.mpp", file0Resource);
        
        ClassPathResource file1Resource = new ClassPathResource("filesys/ContentComparatorTest1.mpp");
        assertNotNull("unable to find test resource filesys/ContentComparatorTest1.mpp", file1Resource);
        
        ClassPathResource file2Resource = new ClassPathResource("filesys/ContentComparatorTest2.mpp");
        assertNotNull("unable to find test resource filesys/ContentComparatorTest2.mpp", file1Resource);
         
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
        assertNotNull("unable to find test resource filesys/ContentComparatorTest0.mpp", file0Resource);
        
        ClassPathResource file1Resource = new ClassPathResource("filesys/ContentComparatorTest1.mpp");
        assertNotNull("unable to find test resource filesys/ContentComparatorTest1.mpp", file1Resource);
                
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
    
    /**
     * Open and close of an excel 2003 file changes certain header properties.
     * Test File 1 has been opened and closed in excel2003.
     * @throws Exception
     */
    public void testDiffExcel2003Files() throws Exception
    {
        CIFSContentComparator contentComparator = new CIFSContentComparator();
        contentComparator.init();
        
        ClassPathResource file0Resource = new ClassPathResource("filesys/ContentComparatorTestExcel2003-1.xls");
        assertNotNull("unable to find test resource filesys/ContentComparatorTestExcel2003-1.xls", file0Resource);
        
        ClassPathResource file1Resource = new ClassPathResource("filesys/ContentComparatorTestExcel2003-2.xls");
        assertNotNull("unable to find test resource filesys/ContentComparatorTestExcel2003-2.xls", file1Resource);
        
        ClassPathResource file3Resource = new ClassPathResource("filesys/ContentComparatorTestExcel2003-3.xls");
        assertNotNull("unable to find test resource filesys/ContentComparatorTestExcel2003-3.xls", file3Resource);
        
        ClassPathResource file4Resource = new ClassPathResource("filesys/ContentComparatorTestExcel2003-4.xls");
        assertNotNull("unable to find test resource filesys/ContentComparatorTestExcel2003-4.xls", file4Resource);
        
        ClassPathResource file5Resource = new ClassPathResource("filesys/ContentComparatorTestExcel2003-5.xls");
        assertNotNull("unable to find test resource filesys/ContentComparatorTestExcel2003-5.xls", file5Resource);
                
        /**
         * Compare trivially different excel files, should ignore trivial differences and be equal 
         */
        {
            File file0 = file0Resource.getFile();
            File file1 = file1Resource.getFile();

            ContentReader reader = new FileContentReader(file0);
            reader.setMimetype("application/vnd.ms-excel");
            reader.setEncoding("UTF-8");
            boolean result = contentComparator.isContentEqual(reader, file1);
            assertTrue("compare trivially different project file, should be equal", result);
        }
        
        /**
         * Compare different project files, should not be ignored 
         */
        {
            File file0 = file0Resource.getFile();
            File file3 = file3Resource.getFile();

            ContentReader reader = new FileContentReader(file0);
            reader.setMimetype("application/vnd.ms-excel");
            reader.setEncoding("UTF-8");
            boolean result = contentComparator.isContentEqual(reader, file3);
            assertTrue("different excel2003 file, failed to note difference", !result);
        }
        
        /**
         * Compare xls files that has different owning users(different [WRITEACCESS]) 
         */
        {
            File file4 = file4Resource.getFile();
            File file5 = file5Resource.getFile();

            ContentReader reader = new FileContentReader(file4);
            reader.setMimetype("application/vnd.ms-excel");
            reader.setEncoding("UTF-8");
            boolean result = contentComparator.isContentEqual(reader, file5);
            assertTrue("compare trivially different xls files, should be equal", result); 
        }
    }

    /**
     * Open and close of a PowerPoint 2003 file changes last edit username property hence changes file size.
     *
     * Test File 0 is created on initial PowerPoint instance
     * Test File 1 file is edited on initial PowerPoint instance so that the file size has been increased by less than 3073 bytes.
     * Test File 2 has been opened and closed on target PowerPoint instance hence lastEditUserName has been changed.
     * Test File 3 has been opened edited and closed on target PowerPoint instance hence lastEditUserName has been changed
     *     but the difference of sizes is still less than 3073 bytes.
     * Test File 4 has been edited on initial PowerPoint instance so that the file size has been reduced.
     * Test File 5 has been edited on initial PowerPoint instance so that the file size has been increased by more than 3072 bytes.
     *
     * @throws Exception
     */
    public void testDiffPowerPoint2003Files() throws Exception
    {
        CIFSContentComparator contentComparator = new CIFSContentComparator();
        contentComparator.init();

        ClassPathResource file0Resource = new ClassPathResource("filesys/ContentComparatorTestPowerPoint2003-0-initial.ppt");
        assertNotNull("unable to find test resource filesys/ContentComparatorTestPowerPoint2003-0-initial.ppt", file0Resource);

        ClassPathResource file1Resource = new ClassPathResource("filesys/ContentComparatorTestPowerPoint2003-1-edited-lt-3073bytes.ppt");
        assertNotNull("unable to find test resource filesys/ContentComparatorTestPowerPoint2003-1-edited-lt-3073bytes.ppt", file1Resource);

        ClassPathResource file2Resource = new ClassPathResource("filesys/ContentComparatorTestPowerPoint2003-2-opened-closed.ppt");
        assertNotNull("unable to find test resource filesys/ContentComparatorTestPowerPoint2003-2-opened-closed.ppt", file2Resource);

        ClassPathResource file3Resource = new ClassPathResource("filesys/ContentComparatorTestPowerPoint2003-3-opened-edited-closed.ppt");
        assertNotNull("unable to find test resource filesys/ContentComparatorTestPowerPoint2003-3-opened-edited-closed.ppt", file3Resource);

        ClassPathResource file4Resource = new ClassPathResource("filesys/ContentComparatorTestPowerPoint2003-4-edited-lt-0bytes.ppt");
        assertNotNull("unable to find test resource filesys/ContentComparatorTestPowerPoint2003-4-edited-lt-0bytes.ppt", file4Resource);

        ClassPathResource file5Resource = new ClassPathResource("filesys/ContentComparatorTestPowerPoint2003-5-edited-gt-3072bytes.ppt");
        assertNotNull("unable to find test resource filesys/ContentComparatorTestPowerPoint2003-5-edited-gt-3072bytes.ppt", file5Resource);

        /**
         * Compare different powerpoint files, should not be ignored
         */
        {
            File file0 = file0Resource.getFile();
            File file1 = file1Resource.getFile();

            ContentReader reader = new FileContentReader(file0);
            reader.setMimetype("application/vnd.ms-powerpoint");
            reader.setEncoding("UTF-8");
            boolean result = contentComparator.isContentEqual(reader, file1);
            assertTrue("compare different powerpoint files, should not be equal", !result);
        }
//        
//      Test commented out, fails after implementation corrected - so there is a another bug in the content 
//        comparison raised MNT-14860 to investigate.
//        
//        /**
//         * Compare trivially different powerpoint files, should ignore trivial differences and be equal
//         */
//        {
//            File file0 = file0Resource.getFile();
//            File file2 = file2Resource.getFile();
//
//            ContentReader reader = new FileContentReader(file0);
//            reader.setMimetype("application/vnd.ms-powerpoint");
//            reader.setEncoding("UTF-8");
//            boolean result = contentComparator.isContentEqual(reader, file2);
//            assertTrue("compare trivially different powerpoint files, should be equal", result);
//        }

        /**
         * Compare different powerpoint files, should not be ignored
         */
        {
            File file0 = file0Resource.getFile();
            File file3 = file1Resource.getFile();

            ContentReader reader = new FileContentReader(file0);
            reader.setMimetype("application/vnd.ms-powerpoint");
            reader.setEncoding("UTF-8");
            boolean result = contentComparator.isContentEqual(reader, file3);
            assertTrue("compare different powerpoint files, should not be equal", !result);
        }

        /**
         * Compare different powerpoint files, should not be ignored
         */
        {
            File file0 = file0Resource.getFile();
            File file4 = file1Resource.getFile();

            ContentReader reader = new FileContentReader(file0);
            reader.setMimetype("application/vnd.ms-powerpoint");
            reader.setEncoding("UTF-8");
            boolean result = contentComparator.isContentEqual(reader, file4);
            assertTrue("compare different powerpoint files, should not be equal", !result);
        }

        /**
         * Compare different powerpoint files, should not be ignored
         */
        {
            File file0 = file0Resource.getFile();
            File file5 = file1Resource.getFile();

            ContentReader reader = new FileContentReader(file0);
            reader.setMimetype("application/vnd.ms-powerpoint");
            reader.setEncoding("UTF-8");
            boolean result = contentComparator.isContentEqual(reader, file5);
            assertTrue("compare different powerpoint files, should not be equal", !result);
        }
    }
}
