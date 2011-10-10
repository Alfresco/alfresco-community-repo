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
import static org.junit.Assert.fail;

import java.util.List;

import javax.transaction.NotSupportedException;
import javax.transaction.SystemException;

import org.alfresco.repo.bulkimport.BulkImportParameters;
import org.alfresco.repo.bulkimport.NodeImporter;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.util.ResourceUtils;

/**
 * @since 4.0
 */
public class BulkImportTests extends AbstractBulkImportTests
{
	private StreamingNodeImporterFactory streamingNodeImporterFactory;

	@BeforeClass
	public static void beforeTests()
	{
		startContext();		
	}

    @Before
	public void setup() throws SystemException, NotSupportedException
	{
    	super.setup();
    	streamingNodeImporterFactory = (StreamingNodeImporterFactory)ctx.getBean("streamingNodeImporterFactory");
	}

	@Test
	public void testCopyImportStriping()
	{
		NodeRef folderNode = topLevelFolder.getNodeRef();

		try
		{
			NodeImporter nodeImporter = streamingNodeImporterFactory.getNodeImporter(ResourceUtils.getFile("classpath:bulkimport"));
            BulkImportParameters bulkImportParameters = new BulkImportParameters();
            bulkImportParameters.setTarget(folderNode);
            bulkImportParameters.setReplaceExisting(true);
            bulkImportParameters.setBatchSize(40);
			bulkImporter.bulkImport(bulkImportParameters, nodeImporter);
		}
		catch(Throwable e)
		{
			fail(e.getMessage());
		}

		System.out.println(bulkImporter.getStatus());

		checkFiles(folderNode, null, 2, 9,
				new ExpectedFile[]
				{
					new ExpectedFile("quickImg1.xls", MimetypeMap.MIMETYPE_EXCEL),
					new ExpectedFile("quickImg1.doc", MimetypeMap.MIMETYPE_WORD),
					new ExpectedFile("quick.txt", MimetypeMap.MIMETYPE_TEXT_PLAIN, "The quick brown fox jumps over the lazy dog"),
				},
				new ExpectedFolder[]
				{
					new ExpectedFolder("folder1"),
					new ExpectedFolder("folder2")
				});
		
		List<FileInfo> folders = getFolders(folderNode, "folder1");
		assertEquals("", 1, folders.size());
		NodeRef folder1 = folders.get(0).getNodeRef();
		checkFiles(folder1, null, 1, 0, null,
				new ExpectedFolder[]
				{
					new ExpectedFolder("folder1.1")
				});

		folders = getFolders(folderNode, "folder2");
		assertEquals("", 1, folders.size());
		NodeRef folder2 = folders.get(0).getNodeRef();
		checkFiles(folder2, null, 1, 0,
				new ExpectedFile[]
				{
				},
				new ExpectedFolder[]
				{
					new ExpectedFolder("folder2.1")
				});

		folders = getFolders(folder1, "folder1.1");
		assertEquals("", 1, folders.size());
		NodeRef folder1_1 = folders.get(0).getNodeRef();
		checkFiles(folder1_1, null, 2, 12,
				new ExpectedFile[]
				{
					new ExpectedFile("quick.txt", MimetypeMap.MIMETYPE_TEXT_PLAIN, "The quick brown fox jumps over the lazy dog"),
					new ExpectedFile("quick.sxw", MimetypeMap.MIMETYPE_OPENOFFICE1_WRITER),
					new ExpectedFile("quick.tar", "application/x-gtar"),
				},
				new ExpectedFolder[]
				{
					new ExpectedFolder("folder1.1.1"),
					new ExpectedFolder("folder1.1.2")
				});
		
		folders = getFolders(folder2, "folder2.1");
		assertEquals("", 1, folders.size());
		NodeRef folder2_1 = folders.get(0).getNodeRef();
		
		checkFiles(folder2_1, null, 0, 17,
				new ExpectedFile[]
				{
					new ExpectedFile("quick.png", MimetypeMap.MIMETYPE_IMAGE_PNG),
					new ExpectedFile("quick.pdf", MimetypeMap.MIMETYPE_PDF),
					new ExpectedFile("quick.odt", MimetypeMap.MIMETYPE_OPENDOCUMENT_TEXT),
				},
				new ExpectedFolder[]
				{
				});
	}

}
