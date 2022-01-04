/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

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
        protected void overwrite(File file, OverwriteOperation overwriteOperation) {}
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
