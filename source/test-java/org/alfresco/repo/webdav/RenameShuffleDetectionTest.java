/*
 * Copyright (C) 2005-2013 Alfresco Software Limited.
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
package org.alfresco.repo.webdav;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Tests for {@link WebDAVHelper}'s rename shuffle detection method.
 * 
 * @author Matt Ward
 */
@RunWith(Parameterized.class)
public class RenameShuffleDetectionTest
{
    private WebDAVHelper davHelper;
    private String path;
    private boolean expectedResult;
    
    public RenameShuffleDetectionTest(String path, boolean expectedResult)
    {
        this.path = path;
        this.expectedResult = expectedResult;
    }
    
    @Before
    public void setUp() throws Exception
    {
        davHelper = new WebDAVHelper();
    }

    @Parameters
    public static Collection<Object[]> data()
    {
        Object[][] data = new Object[][] {
            // Simple cases that shouldn't be detected as a rename shuffle
            { "a_file.doc", false },
            { "/path/a_file.doc", false },
            { "__file__name.xls", false },
            { "/some/parent/ffffffff.doc", false}, // ALF-19673
            // Paths that should be detected as "rename shuffles"
            { "/some/parent/.tmp", true },
            { "/some/parent/a.tmp", true },
            { "/some/parent/ffffffff.tmp", true },
            { "/some/parent/.any_hidden_file", true },
            { "/some/parent/any_file.wbk", true },            
            { "/some/parent/ends_in_tilda~", true },            
            { "/some/parent/junk_backup_morejunk.doc", true },            
            { "/some/parent/junk_backup_morejunk.docx", true },            
            { "/some/parent/junk_backup_morejunk.docm", true },            
            { "/some/parent/junk_backup_morejunk.docxm", true }, // MW: is this an intentional match?            
            { "/some/parent/junk_backup_morejunk.dotx", true },
            { "/some/parent/junk_backup_morejunk.dotm", true },
            { "/some/parent/junk_backup_morejunk.dotxm", true }, // MW: is this an intentional match?
            // TODO: review these cases, in context of ALF-19673
            { "/some/parent/aaaaaaaa", true },
            { "/some/parent/ffffffff", true },
            { "/some/parent/ffffffffff", true },
            { "/some/parent/ab0c1d2ef3456789", true},
            { "aaaaaaaa", true },
            { "abcdef01", true },
            // Photoshop (MNT-8971)
            { "/psC1DA.tmp", true },
            { "/path/psA1B5.tmp", true }
        };
        return Arrays.asList(data);
    }
    
    @Test
    public void testIsRenameShuffle()
    {
        assertEquals("Incorrect result for path: " + path, expectedResult, davHelper.isRenameShuffle(path));
    }
}
