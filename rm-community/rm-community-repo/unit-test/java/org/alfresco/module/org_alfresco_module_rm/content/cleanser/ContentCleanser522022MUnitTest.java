package org.alfresco.module.org_alfresco_module_rm.content.cleanser;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;

import java.io.File;

import org.alfresco.module.org_alfresco_module_rm.test.util.BaseUnitTest;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;

/**
 * Eager content store cleaner unit test.
 * 
 * @author Roy Wetherall
 * @since 2.4.a
 */
public class ContentCleanser522022MUnitTest extends BaseUnitTest
{
    @InjectMocks @Spy private ContentCleanser522022M contentCleanser522022M = new ContentCleanser522022M()
    {
        /** dummy implementations */
        protected void overwrite(File file, OverwriteOperation overwriteOperation) {};
    };
    
    @Mock private File mockedFile;
   
    /**
     * Given that a file exists
     * When I cleanse it
     * Then the content is overwritten
     */
    @Test
    public void cleanseFile()
    {
        when(mockedFile.exists())
            .thenReturn(true);
        when(mockedFile.canWrite())
            .thenReturn(true);
        
        contentCleanser522022M.cleanse(mockedFile);
        
        InOrder inOrder = inOrder(contentCleanser522022M);
        
        inOrder.verify(contentCleanser522022M)
            .overwrite(mockedFile, contentCleanser522022M.overwriteOnes);
        inOrder.verify(contentCleanser522022M)
            .overwrite(mockedFile, contentCleanser522022M.overwriteZeros);
        inOrder.verify(contentCleanser522022M)
            .overwrite(mockedFile, contentCleanser522022M.overwriteRandom);
    }
    
    /**
     * Given that the file does not exist
     * When I cleanse it
     * Then an exception is thrown
     */
    @Test
    (
       expected=ContentIOException.class
    )
    public void fileDoesNotExist()
    {
        when(mockedFile.exists())
            .thenReturn(false);
        when(mockedFile.canWrite())
            .thenReturn(true);
        
        contentCleanser522022M.cleanse(mockedFile);
    }
    
    /**
     * Given that I can not write to the file
     * When I cleanse it
     * Then an exception is thrown
     */
    @Test
    (
       expected=ContentIOException.class
    )
    public void cantWriteToFile()
    {
        when(mockedFile.exists())
            .thenReturn(true);
        when(mockedFile.canWrite())
            .thenReturn(false);
        
        contentCleanser522022M.cleanse(mockedFile);
    }
}
