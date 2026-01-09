/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2026 Alfresco Software Limited
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

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;

import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;

import org.alfresco.module.org_alfresco_module_rm.test.util.BaseUnitTest;
import org.alfresco.service.cmr.repository.ContentIOException;

/**
 * Eager content store cleaner unit test.
 * 
 */
public class ContentCleanserSevenPassUnitTest extends BaseUnitTest
{
    @InjectMocks
    @Spy
    private ContentCleanserSevenPass contentCleanserSevenPass = new ContentCleanserSevenPass() 
    {
        /** dummy implementations */
        @Override
        protected void overwrite(File file, OverwriteOperation overwriteOperation)
        {
            // Intentionally left empty
        }
    };

    @Mock
    private File mockedFile;

    /**
     * Given that a file exists When I cleanse it Then the content is overwritten
     */
    @Test
    public void cleanseFile()
    {
        when(mockedFile.exists()).thenReturn(true);
        when(mockedFile.canWrite()).thenReturn(true);
        contentCleanserSevenPass.cleanse(mockedFile);
        verify(contentCleanserSevenPass, times(2)).overwrite(mockedFile, contentCleanserSevenPass.overwriteOnes);
        verify(contentCleanserSevenPass, times(3)).overwrite(mockedFile, contentCleanserSevenPass.overwriteZeros);
        verify(contentCleanserSevenPass, times(2)).overwrite(mockedFile, contentCleanserSevenPass.overwriteRandom);

    }

    /**
     * Given that the file does not exist When I cleanse it Then an exception is thrown
     */
    @Test(expected = ContentIOException.class)
    public void fileDoesNotExist()
    {
        when(mockedFile.exists()).thenReturn(false);
        when(mockedFile.canWrite()).thenReturn(true);
        contentCleanserSevenPass.cleanse(mockedFile);
    }

    /**
     * Given that I can not write to the file When I cleanse it Then an exception is thrown
     */
    @Test(expected = ContentIOException.class)
    public void cantWriteToFile()
    {
        when(mockedFile.exists()).thenReturn(true);
        when(mockedFile.canWrite()).thenReturn(false);
        contentCleanserSevenPass.cleanse(mockedFile);
    }
}
