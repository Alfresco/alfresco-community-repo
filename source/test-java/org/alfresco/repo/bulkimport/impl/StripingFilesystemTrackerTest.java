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
