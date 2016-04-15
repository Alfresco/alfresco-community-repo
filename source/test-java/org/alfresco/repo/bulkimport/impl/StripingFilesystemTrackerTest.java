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
package org.alfresco.repo.bulkimport.impl;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

import org.alfresco.repo.bulkimport.DirectoryAnalyser;
import org.alfresco.repo.bulkimport.ImportableItem;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.ApplicationContextHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.util.ResourceUtils;

/**
 * 
 * @since 4.0
 *
 */
public class StripingFilesystemTrackerTest
{
	private DirectoryAnalyser directoryAnalyser;
	private static ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();

    @Before
	public void setup() throws Exception
	{
    	directoryAnalyser = (DirectoryAnalyser)ctx.getBean("bfsiDirectoryAnalyser");
	}
    
    @After
	public void teardown() throws Exception
	{
    	
	}
    
    @Test
    public void test1() throws FileNotFoundException
    {
    	final File sourceFolder = ResourceUtils.getFile("classpath:bulkimport");
        final StripingFilesystemTracker tracker = new StripingFilesystemTracker(directoryAnalyser, new NodeRef("workspace", "SpacesStore", "123"), sourceFolder, Integer.MAX_VALUE);
        List<ImportableItem> items = tracker.getImportableItems(Integer.MAX_VALUE);
        assertEquals("", 11, items.size());

        tracker.incrementLevel();
        items = tracker.getImportableItems(Integer.MAX_VALUE);
        assertEquals("", 2, items.size());

        tracker.incrementLevel();
        items = tracker.getImportableItems(Integer.MAX_VALUE);
        assertEquals("", 31, items.size());
    }
}
