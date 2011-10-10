/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
 * FLOSS exception.  You should have received a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
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
