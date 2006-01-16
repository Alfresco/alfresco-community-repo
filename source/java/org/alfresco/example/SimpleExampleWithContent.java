/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.example;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.transaction.TransactionUtil;
import org.alfresco.repo.transaction.TransactionUtil.TransactionWork;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.GUID;
import org.alfresco.util.TempFileProvider;
import org.alfresco.util.debug.NodeStoreInspector;
import org.springframework.context.ApplicationContext;

/**
 * A quick example of how to
 * <ul>
 *   <li>get hold of the repository service</li>
 *   <li>initialise a model</li>
 *   <li>create nodes</li>
 *   <li>load in some content</li>
 * </ul>
 * <p>
 * <i>
 * All the normal checks for missing resources and so forth have been left out in the interests
 * of clarity of demonstration.
 * </i>
 * <p>
 * To change the model being used, make changes to the <b>dictionaryDAO</b> bean in the
 * application contenxt XML file.  For now, this example is written against the
 * generic <code>alfresco/model/contentModel.xml</code>.
 * <p>
 * The content store location can also be set in the application context.
 * 
 * 
 * @author Derek Hulley
 */
public class SimpleExampleWithContent
{
    private static final String NAMESPACE = "http://www.alfresco.org/test/SimpleExampleWithContent";
    
    public static void main(String[] args)
    {
        // initialise app content 
        ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();
        // get registry of services
        final ServiceRegistry serviceRegistry = (ServiceRegistry) ctx.getBean(ServiceRegistry.SERVICE_REGISTRY);
        
        // begin a UserTransaction
        // All the services are set to create or propogate the transaction.
        // This transaction will be recognised and propogated
        // The TransactionUtil takes care of the catching and rollback, etc
        TransactionService transactionService = serviceRegistry.getTransactionService();
        TransactionWork<Object> exampleWork = new TransactionWork<Object>()
        {
            public Object doWork() throws Exception
            {
                doExample(serviceRegistry);
                return null;
            }
        };
        TransactionUtil.executeInUserTransaction(transactionService, exampleWork);
        System.exit(0);
    }

    private static void doExample(ServiceRegistry serviceRegistry) throws Exception
    {
        // get individual, required services
        NodeService nodeService = serviceRegistry.getNodeService();
        ContentService contentService = serviceRegistry.getContentService();

        // authenticate
        AuthenticationService authenticationService = serviceRegistry.getAuthenticationService();
        authenticationService.authenticate("admin", "admin".toCharArray());
        
        // create a store, if one doesn't exist
        StoreRef storeRef = new StoreRef(
                StoreRef.PROTOCOL_WORKSPACE,
                "SimpleExampleWithContent-" + GUID.generate());
        if (!nodeService.exists(storeRef))
        {
            nodeService.createStore(storeRef.getProtocol(), storeRef.getIdentifier());
        }
        
        // get the root node from which to hang the next level of nodes
        NodeRef rootNodeRef = nodeService.getRootNode(storeRef);
        
        Map<QName, Serializable> nodeProperties = new HashMap<QName, Serializable>(7);
        
        // add a simple folder to the root node
        nodeProperties.clear();
        nodeProperties.put(ContentModel.PROP_NAME, "My First Folder");
        ChildAssociationRef assocRef = nodeService.createNode(
                rootNodeRef,
                ContentModel.ASSOC_CHILDREN,
                QName.createQName(NAMESPACE, QName.createValidLocalName("My First Folder")),
                ContentModel.TYPE_FOLDER,
                nodeProperties);
        NodeRef folderRef = assocRef.getChildRef();
        
        // create a file
        nodeProperties.clear();
        nodeProperties.put(ContentModel.PROP_NAME, "My First File");
        assocRef = nodeService.createNode(
                folderRef,
                ContentModel.ASSOC_CONTAINS,
                QName.createQName(NAMESPACE, QName.createValidLocalName("My First File")),
                ContentModel.TYPE_CONTENT,
                nodeProperties);
        NodeRef fileRef = assocRef.getChildRef();
        
        ContentWriter writer = contentService.getWriter(fileRef, ContentModel.PROP_CONTENT, true);
        // the mimetype will up pushed onto the node automatically once the stream closes
        writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
        // store string content as UTF-8
        writer.setEncoding("UTF-8");
        
        // write some content - this API allows streaming and direct loading,
        // but for now we'll just upload a string
        // The writer, being updating, will take care of updating the node once the stream
        // closes.
        String content = "The quick brown fox jumps over the lazy dog";
        writer.putContent(content);
        
        // dump the content to a file
        File file = TempFileProvider.createTempFile("sample", ".txt");
        ContentReader reader = contentService.getReader(fileRef, ContentModel.PROP_CONTENT);
        reader.getContent(file);
        
        // just to demonstrate the node structure, dump it to the file
        String dump = NodeStoreInspector.dumpNodeStore(nodeService, storeRef);
        System.out.println("Node Store: \n" + dump);
        
        // and much, much more ...
    }
}
