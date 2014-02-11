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

import java.io.Serializable;
import java.util.Collections;
import java.util.UUID;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ApplicationContextHelper;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.ApplicationContext;

/**
 * Integration tests for the {@link WebDAVHelper} class.
 * 
 * @author Matt Ward
 */
public class WebDAVHelperIntegrationTest
{
   private static ApplicationContext ctx;
   private WebDAVHelper webDAVHelper;
   private FileFolderService fileFolderService;
   private NodeRef rootNodeRef;
   private NodeRef rootFolder;
   private NodeService nodeService;
   
   @BeforeClass
   public static void setUpSpring()
   {
       ctx = ApplicationContextHelper.getApplicationContext();
   }
   
   @Before
   public void setUp()
   {
       AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
       
       webDAVHelper = (WebDAVHelper) ctx.getBean("webDAVHelper");
       fileFolderService = (FileFolderService) ctx.getBean("FileFolderService");
       nodeService = (NodeService) ctx.getBean("NodeService");
       
       StoreRef storeRef = nodeService.createStore("workspace", "WebDAVHelperTest-"+UUID.randomUUID());
       rootNodeRef = nodeService.getRootNode(storeRef);
       
       rootFolder = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN,
                   ContentModel.ASSOC_CHILDREN, ContentModel.TYPE_FOLDER).getChildRef();
   }
   
   @Test
   public void canGetNodeForPathWithCorrectCase() throws FileNotFoundException
   {
       FileInfo folderInfo = fileFolderService.create(rootFolder, "my_folder", ContentModel.TYPE_FOLDER);
       FileInfo fileInfo = fileFolderService.create(folderInfo.getNodeRef(), "my_file.txt", ContentModel.TYPE_CONTENT);
       
       FileInfo found = webDAVHelper.getNodeForPath(rootFolder, "my_folder/my_file.txt");
       // Sanity check, but the main test is that we haven't have a FileNotFoundException thrown.
       assertEquals(fileInfo, found);
       
       found = webDAVHelper.getNodeForPath(rootFolder, "my_folder");
       assertEquals(folderInfo, found);
   }
   
   @Test
   public void cannotGetNodeForPathWithIncorrectCase() throws FileNotFoundException
   {
       FileInfo folderInfo = fileFolderService.create(rootFolder, "my_folder", ContentModel.TYPE_FOLDER);
       fileFolderService.create(folderInfo.getNodeRef(), "my_file.txt", ContentModel.TYPE_CONTENT);
       
       try
       {
           webDAVHelper.getNodeForPath(rootFolder, "My_Folder/My_File.txt");
           fail("FileNotFoundException should have been thrown.");
       }
       catch (FileNotFoundException e)
       {
           // Got here, good.
       }
   }
   
   @Test
   public void cannotGetNodeForFolderPathWithIncorrectCase() throws FileNotFoundException
   {
       FileInfo folderInfo = fileFolderService.create(rootFolder, "my_folder", ContentModel.TYPE_FOLDER);
       fileFolderService.create(folderInfo.getNodeRef(), "my_file.txt", ContentModel.TYPE_CONTENT);
       
       try
       {
           webDAVHelper.getNodeForPath(rootFolder, "My_Folder");
           fail("FileNotFoundException should have been thrown.");
       }
       catch (FileNotFoundException e)
       {
           // Got here, good.
       }
   }
   
   @Test
   public void canGetNodeForRootFolderPath() throws FileNotFoundException
   {
       FileInfo folderInfo = fileFolderService.create(rootFolder, "my_folder", ContentModel.TYPE_FOLDER);
       fileFolderService.create(folderInfo.getNodeRef(), "my_file.txt", ContentModel.TYPE_CONTENT);
       
       FileInfo found = webDAVHelper.getNodeForPath(rootFolder, "/");
       assertEquals(rootFolder, found.getNodeRef());
   }
}
