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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.transaction.NotSupportedException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import org.alfresco.model.ContentModel;
import org.alfresco.query.CannedQueryPageDetails;
import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.rule.RuleService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.springframework.context.ApplicationContext;

/**
 * @since 4.0
 */
public class AbstractBulkImportTests
{
	protected static ApplicationContext ctx = null;

    protected FileFolderService fileFolderService;
    protected NodeService nodeService;
    protected TransactionService transactionService;
	protected ContentService contentService;
	protected UserTransaction txn = null;
	protected RuleService ruleService;
    protected ActionService actionService;
	protected MultiThreadedBulkFilesystemImporter bulkImporter;

	protected NodeRef rootNodeRef;
	protected FileInfo topLevelFolder;
	protected NodeRef top;

	protected static void startContext()
	{
		ctx = ApplicationContextHelper.getApplicationContext();
	}

	protected static void startContext(String[] configLocations)
	{
		ctx = ApplicationContextHelper.getApplicationContext(configLocations);		
	}

	protected static void stopContext()
	{
		ApplicationContextHelper.closeApplicationContext();		
	}

    @Before
	public void setup() throws SystemException, NotSupportedException
	{
    	try
    	{
	    	nodeService = (NodeService)ctx.getBean("nodeService");
	    	fileFolderService = (FileFolderService)ctx.getBean("fileFolderService");
	    	transactionService = (TransactionService)ctx.getBean("transactionService");
	    	bulkImporter = (MultiThreadedBulkFilesystemImporter)ctx.getBean("bulkFilesystemImporter");
	    	contentService = (ContentService)ctx.getBean("contentService");
	        actionService = (ActionService)ctx.getBean("actionService");
	    	ruleService = (RuleService)ctx.getBean("ruleService");

	        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());

			String s = "BulkFilesystemImport" + System.currentTimeMillis();
	
			txn = transactionService.getUserTransaction();
			txn.begin();
			
			AuthenticationUtil.pushAuthentication();
			AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());

	        StoreRef storeRef = nodeService.createStore(StoreRef.PROTOCOL_WORKSPACE, s);
	        rootNodeRef = nodeService.getRootNode(storeRef);
	        top = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}top"), ContentModel.TYPE_FOLDER).getChildRef();
	        
	        topLevelFolder = fileFolderService.create(top, s, ContentModel.TYPE_FOLDER);

	        txn.commit();
    	}
    	catch(Throwable e)
    	{
    		fail(e.getMessage());
    	}
	}

    @After
	public void teardown() throws Exception
	{
        AuthenticationUtil.popAuthentication();
		if(txn != null)
		{
			txn.commit();
		}
	}
    
    @AfterClass
    public static void afterTests()
    {
		stopContext();    	
    }

    protected List<FileInfo> getFolders(NodeRef parent, String pattern)
    {
		PagingResults<FileInfo> page = fileFolderService.list(parent, false, true, pattern, null, null, new PagingRequest(CannedQueryPageDetails.DEFAULT_PAGE_SIZE));
		List<FileInfo> folders = page.getPage();
		return folders;
    }

    protected List<FileInfo> getFiles(NodeRef parent, String pattern)
    {
    	PagingResults<FileInfo> page = fileFolderService.list(parent, true, false, pattern, null, null, new PagingRequest(CannedQueryPageDetails.DEFAULT_PAGE_SIZE));
		List<FileInfo> files = page.getPage();
		return files;
    }
    
    protected Map<String, FileInfo> toMap(List<FileInfo> list)
    {
    	Map<String, FileInfo> map = new HashMap<String, FileInfo>(list.size());
    	for(FileInfo fileInfo : list)
    	{
    		map.put(fileInfo.getName(), fileInfo);
    	}
    	return map;
    }

    protected void checkFolder(NodeRef folderNode, String childFolderName, String pattern, int numExpectedFolders, int numExpectedFiles, ExpectedFolder[] expectedFolders, ExpectedFile[] expectedFiles)
    {
		List<FileInfo> folders = getFolders(folderNode, childFolderName);
		assertEquals("", 1, folders.size());
		NodeRef folder1 = folders.get(0).getNodeRef();
		checkFiles(folder1, pattern, numExpectedFolders, numExpectedFiles, expectedFiles, expectedFolders);
    }

    protected void checkFiles(NodeRef parent, String pattern, int expectedNumFolders, int expectedNumFiles,
    		ExpectedFile[] expectedFiles, ExpectedFolder[] expectedFolders)
    {
    	Map<String, FileInfo> folders = toMap(getFolders(parent, pattern));
    	Map<String, FileInfo> files = toMap(getFiles(parent, pattern));
		assertEquals("", expectedNumFolders, folders.size());
		assertEquals("", expectedNumFiles, files.size());
		
		if(expectedFiles != null)
		{
			for(ExpectedFile expectedFile : expectedFiles)
			{
				FileInfo fileInfo = files.get(expectedFile.getName());
				assertNotNull("", fileInfo);
				assertNotNull("", fileInfo.getContentData());
				assertEquals(expectedFile.getMimeType(), fileInfo.getContentData().getMimetype());
				if(fileInfo.getContentData().getMimetype() == MimetypeMap.MIMETYPE_TEXT_PLAIN
						&& expectedFile.getContentContains() != null)
				{
					ContentReader reader = contentService.getReader(fileInfo.getNodeRef(), ContentModel.PROP_CONTENT);
					String contentContains = expectedFile.getContentContains();
					assertTrue("", reader.getContentString().indexOf(contentContains) != -1);
				}
			}
		}
		
		if(expectedFolders != null)
		{
			for(ExpectedFolder expectedFolder : expectedFolders)
			{
				FileInfo fileInfo = folders.get(expectedFolder.getName());
				assertNotNull("", fileInfo);
			}
		}
    }

    protected void checkContent(FileInfo file, String name, String mimeType)
    {
		assertEquals("", name, file.getName());
		assertEquals("", mimeType, file.getContentData().getMimetype());    	
    }
    
	
    protected static class ExpectedFolder
	{
		private String name;

		public ExpectedFolder(String name)
		{
			super();
			this.name = name;
		}

		public String getName()
		{
			return name;
		}
	}
	
	protected static class ExpectedFile
	{
		private String name;
		private String mimeType;
		private String contentContains = null;
		
		public ExpectedFile(String name, String mimeType, String contentContains)
		{
			this(name, mimeType);
			this.contentContains = contentContains;
		}
		
		public ExpectedFile(String name, String mimeType)
		{
			super();
			this.name = name;
			this.mimeType = mimeType;
		}

		public String getName()
		{
			return name;
		}

		public String getMimeType()
		{
			return mimeType;
		}

		public String getContentContains()
		{
			return contentContains;
		}
	}
}
