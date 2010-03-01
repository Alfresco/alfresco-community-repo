/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.repo.node;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.springframework.context.ApplicationContext;

/**
 * Perform document loads and fetches of child associations in increasing numbers.
 * <p>
 * {@link https://issues.alfresco.com/jira/browse/ETWOTWO-744}
 * 
 * @author Derek Hulley
 * @author CACEIS
 * @since 2.2SP2
 */
public class FFCLoadsOfFiles 
{
	private static int totalNumDocs = 6000;
	private static int docsPerTx = 2000;
	private static int currentDoc = 0;

    public static void main(String[] args)
    {
    	
        // initialise app content 
        ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();
        // get registry of services
        final ServiceRegistry serviceRegistry = (ServiceRegistry) ctx.getBean(ServiceRegistry.SERVICE_REGISTRY);

        // authenticate
        AuthenticationService authenticationService = serviceRegistry.getAuthenticationService();
        authenticationService.authenticate(AuthenticationUtil.getAdminUserName(), "admin".toCharArray());

        
        // use TransactionWork to wrap service calls in a user transaction
        TransactionService transactionService = serviceRegistry.getTransactionService();
        RetryingTransactionCallback<Object> exampleWork = new RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Exception
            {
                doExample(serviceRegistry);
                return null;
            }
        };
        currentDoc = 0;
        while (currentDoc < totalNumDocs)
        {
            transactionService.getRetryingTransactionHelper().doInTransaction(exampleWork);
        }
        System.exit(0);
    }

    


	public static void doExample(ServiceRegistry serviceRegistry) throws Exception
    {
        //
        // locate the company home node
        //
        SearchService searchService = serviceRegistry.getSearchService();
        NodeService nodeService = serviceRegistry.getNodeService();
        NamespaceService namespaceService = serviceRegistry.getNamespaceService();
        StoreRef storeRef = new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore");
        NodeRef rootNodeRef = nodeService.getRootNode(storeRef);
        List<NodeRef> results = searchService.selectNodes(rootNodeRef, "/app:company_home", null, namespaceService, false);
        if (results.size() == 0)
        {
            throw new AlfrescoRuntimeException("Can't find /app:company_home");
        }
        NodeRef companyHomeNodeRef = results.get(0);
        results = searchService.selectNodes(companyHomeNodeRef, "./cm:LoadTest", null, namespaceService, false);
        final NodeRef loadTestHome;
        if (results.size() == 0)
        {
            loadTestHome = nodeService.createNode(
                    companyHomeNodeRef,
                    ContentModel.ASSOC_CHILDREN,
                    QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "LoadTest"),
                    ContentModel.TYPE_FOLDER).getChildRef();
        }
        else
        {
            loadTestHome = results.get(0);
        }

        if ((currentDoc + docsPerTx) > totalNumDocs)
        {
        	docsPerTx = totalNumDocs - currentDoc;
        }
        // Create new Space
        String spaceName = "Bulk Load Space (" + System.currentTimeMillis() + ") from " + currentDoc + " to " + (currentDoc + docsPerTx - 1) + " of " + totalNumDocs;
        Map<QName, Serializable> spaceProps = new HashMap<QName, Serializable>();
    	spaceProps.put(ContentModel.PROP_NAME, spaceName);
        NodeRef  newSpace = nodeService.createNode(loadTestHome, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_PREFIX, spaceName),ContentModel.TYPE_FOLDER,spaceProps).getChildRef();
        

        // create new content node within new Space home
        for (int k = 1;k<=docsPerTx;k++)
        {
    		currentDoc++;
    		System.out.println("About to start document " + currentDoc);
        	// assign name
        	String name = "BulkLoad (" + System.currentTimeMillis() + ") " + currentDoc ;
        	Map<QName, Serializable> contentProps = new HashMap<QName, Serializable>();
        	contentProps.put(ContentModel.PROP_NAME, name);
        	
        	// create content node
        	// NodeService nodeService = serviceRegistry.getNodeService();
        	ChildAssociationRef association = nodeService.createNode(newSpace, 
        			ContentModel.ASSOC_CONTAINS, 
        			QName.createQName(NamespaceService.CONTENT_MODEL_PREFIX, name),
        			ContentModel.TYPE_CONTENT,
        			contentProps);
        	NodeRef content = association.getChildRef();
        
        	// add titled aspect (for Web Client display)
        	Map<QName, Serializable> titledProps = new HashMap<QName, Serializable>();
        	titledProps.put(ContentModel.PROP_TITLE, name);
        	titledProps.put(ContentModel.PROP_DESCRIPTION, name);
        	nodeService.addAspect(content, ContentModel.ASPECT_TITLED, titledProps);
        	
        	//
        	// write some content to new node
        	//

        	ContentService contentService = serviceRegistry.getContentService();
        	ContentWriter writer = contentService.getWriter(content, ContentModel.PROP_CONTENT, true);
        	writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
        	writer.setEncoding("UTF-8");
        	String text = "This is some text in a doc";
        	writer.putContent(text);
    		System.out.println("About to get child assocs ");        	
        	//Circa
//        	nodeService.getChildAssocs(newSpace);
    	       for (int count=0;count<=10000;count++)
    	        {
    	        	nodeService.getChildAssocs(newSpace);
    	        }
       	
        }
    	//doSearch(searchService);
 		System.out.println("About to end transaction " );

    }
}
