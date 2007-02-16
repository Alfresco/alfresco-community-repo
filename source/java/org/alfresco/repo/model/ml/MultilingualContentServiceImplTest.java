/*
 * Copyright (C) 2007 Alfresco, Inc.
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
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.model.ml;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import junit.framework.TestCase;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.transaction.TransactionUtil;
import org.alfresco.repo.transaction.TransactionUtil.TransactionWork;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.ml.MultilingualContentService;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionHistory;
import org.alfresco.service.cmr.version.VersionService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.springframework.context.ApplicationContext;

/**
 * @see org.alfresco.repo.ml.MultilingualContentServiceImpl
 * 
 * @author Derek Hulley
 * @author Philippe Dubois
 */
public class MultilingualContentServiceImplTest extends TestCase
{
    private static ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();

    private ServiceRegistry serviceRegistry;
    private AuthenticationComponent authenticationComponent;
    private TransactionService transactionService;
    private NodeService nodeService;
    private FileFolderService fileFolderService;
    private VersionService versionService;
    private MultilingualContentService multilingualContentService;
    private NodeRef folderNodeRef;
    
    @Override
    protected void setUp() throws Exception
    {
        serviceRegistry = (ServiceRegistry) ctx.getBean(ServiceRegistry.SERVICE_REGISTRY);
        authenticationComponent = (AuthenticationComponent) ctx.getBean("AuthenticationComponent");
        transactionService = serviceRegistry.getTransactionService();
        nodeService = serviceRegistry.getNodeService();
        fileFolderService = serviceRegistry.getFileFolderService();
        versionService = serviceRegistry.getVersionService();
        multilingualContentService = (MultilingualContentService) ctx.getBean("MultilingualContentService");
        
        // Run as admin
        authenticationComponent.setCurrentUser("admin");
        
        // Create a folder to work in
        TransactionWork<NodeRef> createFolderWork = new TransactionWork<NodeRef>()
        {
            public NodeRef doWork() throws Exception
            {
                StoreRef storeRef = new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore");
                NodeRef rootNodeRef = nodeService.getRootNode(storeRef);
                // Create the folder
                NodeRef folderNodeRef = nodeService.createNode(
                        rootNodeRef,
                        ContentModel.ASSOC_CHILDREN,
                        QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "testFolder"),
                        ContentModel.TYPE_FOLDER).getChildRef();
                // done
                return folderNodeRef;
            }
        };
        folderNodeRef = TransactionUtil.executeInUserTransaction(transactionService, createFolderWork);
    }
    
    @Override
    protected void tearDown() throws Exception
    {
        // Clear authentication
        try
        {
            authenticationComponent.clearCurrentSecurityContext();
        }
        catch (Throwable e)
        {
            e.printStackTrace();
        }
    }
    
    private NodeRef createContent()
    {
        NodeRef contentNodeRef = fileFolderService.create(
                folderNodeRef,
                "" + System.currentTimeMillis(),
                ContentModel.TYPE_CONTENT).getNodeRef();
        // add some content
        ContentWriter contentWriter = fileFolderService.getWriter(contentNodeRef);
        contentWriter.putContent("ABC");
        // done
        return contentNodeRef;
    }
    
    public void testSetup() throws Exception
    {
        // Ensure that content can be created
        createContent();
    }

    public void testMakeTranslation() throws Exception
    {
        NodeRef contentNodeRef = createContent();
        // Turn the content into a translation with the appropriate structures
        NodeRef mlContainerNodeRef = multilingualContentService.makeTranslation(contentNodeRef, Locale.CHINESE);
        // Check it
        assertNotNull("Container not created", mlContainerNodeRef);
        // Check the container child count
        assertEquals("Incorrect number of child nodes", 1, nodeService.getChildAssocs(mlContainerNodeRef).size());
    }
    
    public void testAddTranslationUsingContainer() throws Exception
    {
        // Make a container with a single translation
        NodeRef chineseContentNodeRef = createContent();
        NodeRef mlContainerNodeRef = multilingualContentService.makeTranslation(chineseContentNodeRef, Locale.CHINESE);
        // Create some more content
        NodeRef frenchContentNodeRef = createContent();
        // Make this a translation of the Chinese
        NodeRef newMLContainerNodeRef = multilingualContentService.addTranslation(
                frenchContentNodeRef,
                mlContainerNodeRef,
                Locale.FRENCH);
        // Make sure that the original container was used
        assertEquals("Existing container should have been used", mlContainerNodeRef, newMLContainerNodeRef);
        // Check the container child count
        assertEquals("Incorrect number of child nodes", 2, nodeService.getChildAssocs(mlContainerNodeRef).size());
    }
    
    public void testAddTranslationUsingContent() throws Exception
    {
        // Make a container with a single translation
        NodeRef chineseContentNodeRef = createContent();
        NodeRef mlContainerNodeRef = multilingualContentService.makeTranslation(chineseContentNodeRef, Locale.CHINESE);
        // Create some more content
        NodeRef frenchContentNodeRef = createContent();
        // Make this a translation of the Chinese
        NodeRef newMLContainerNodeRef = multilingualContentService.addTranslation(
                frenchContentNodeRef,
                chineseContentNodeRef,
                Locale.FRENCH);
        // Make sure that the original container was used
        assertEquals("Existing container should have been used", mlContainerNodeRef, newMLContainerNodeRef);
        // Check the container child count
        assertEquals("Incorrect number of child nodes", 2, nodeService.getChildAssocs(mlContainerNodeRef).size());
    }
    
    @SuppressWarnings("unused") 
    public void testCreateEdition() throws Exception
    {
        // Make some content
        NodeRef chineseContentNodeRef = createContent();
        NodeRef frenchContentNodeRef = createContent();
        NodeRef japaneseContentNodeRef = createContent();
        // Add to container
        NodeRef mlContainerNodeRef = multilingualContentService.makeTranslation(chineseContentNodeRef, Locale.CHINESE);
        multilingualContentService.addTranslation(frenchContentNodeRef, mlContainerNodeRef, Locale.FRENCH);
        multilingualContentService.addTranslation(japaneseContentNodeRef, mlContainerNodeRef, Locale.JAPANESE);
        // Check the container child count
        assertEquals("Incorrect number of child nodes", 3, nodeService.getChildAssocs(mlContainerNodeRef).size());

        // Version each of the documents
        List<NodeRef> nodeRefs = new ArrayList<NodeRef>(3);
        nodeRefs.add(chineseContentNodeRef);
        nodeRefs.add(frenchContentNodeRef);
        nodeRefs.add(japaneseContentNodeRef);
        versionService.createVersion(nodeRefs, null);
        // Get the current versions of each of the documents
        Version chineseVersionPreEdition = versionService.getCurrentVersion(chineseContentNodeRef);
        Version frenchVersionPreEdition = versionService.getCurrentVersion(frenchContentNodeRef);
        Version japaneseVersionPreEdition = versionService.getCurrentVersion(japaneseContentNodeRef);
        
        // Create the edition, keeping the Chinese translation as the basis
        multilingualContentService.createEdition(chineseContentNodeRef);
        // Check the container child count
        assertEquals("Incorrect number of child nodes", 1, nodeService.getChildAssocs(mlContainerNodeRef).size());
        
        // Get the document versions now
        Version chineseVersionPostEdition = versionService.getCurrentVersion(chineseContentNodeRef);
        assertFalse("Expected document to be gone", nodeService.exists(frenchContentNodeRef));
        assertFalse("Expected document to be gone", nodeService.exists(japaneseContentNodeRef));
        
        // Now be sure that we can get the required information using the version service
        VersionHistory mlContainerVersionHistory = versionService.getVersionHistory(mlContainerNodeRef);
        Collection<Version> mlContainerVersions = mlContainerVersionHistory.getAllVersions();
        // Loop through and get all the children of each version
        for (Version mlContainerVersion : mlContainerVersions)
        {
            NodeRef versionedMLContainerNodeRef = mlContainerVersion.getFrozenStateNodeRef();
            // Get all the children
            Map<Locale, NodeRef> translationsByLocale = multilingualContentService.getTranslations(
                    versionedMLContainerNodeRef);
            // Count the children
            int count = translationsByLocale.size();
        }
    }
}
