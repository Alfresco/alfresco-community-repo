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
package org.alfresco.repo.node.integrity;

import java.io.InputStream;

import javax.transaction.UserTransaction;

import junit.framework.TestCase;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.dictionary.DictionaryDAO;
import org.alfresco.repo.dictionary.M2Model;
import org.alfresco.repo.node.BaseNodeServiceTest;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.PropertyMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;

/**
 * Checks that tagging of <i>incomplete</i> nodes is done properly.
 * 
 * @author Derek Hulley
 */
@SuppressWarnings("unused")
public class IncompleteNodeTaggerTest extends TestCase
{
    private static Log logger = LogFactory.getLog(IncompleteNodeTaggerTest.class);

    private static ApplicationContext ctx;
    static
    {
        ctx = ApplicationContextHelper.getApplicationContext();
    }
    
    private IncompleteNodeTagger tagger;
    private ServiceRegistry serviceRegistry;
    private NodeService nodeService;
    private NodeRef rootNodeRef;
    private PropertyMap properties;
    private UserTransaction txn;
    private AuthenticationComponent authenticationComponent;
    
    public void setUp() throws Exception
    {
        DictionaryDAO dictionaryDao = (DictionaryDAO) ctx.getBean("dictionaryDAO");
        ClassLoader cl = BaseNodeServiceTest.class.getClassLoader();
        // load the test model
        InputStream modelStream = cl.getResourceAsStream("org/alfresco/repo/node/integrity/IntegrityTest_model.xml");
        assertNotNull(modelStream);
        M2Model model = M2Model.createModel(modelStream);
        dictionaryDao.putModel(model);

        tagger = (IncompleteNodeTagger) ctx.getBean("incompleteNodeTagger");

        serviceRegistry = (ServiceRegistry) ctx.getBean(ServiceRegistry.SERVICE_REGISTRY);
        nodeService = serviceRegistry.getNodeService();
        this.authenticationComponent = (AuthenticationComponent)ctx.getBean("authenticationComponent");
        
        this.authenticationComponent.setSystemUserAsCurrentUser();
        
        // begin a transaction
        TransactionService transactionService = serviceRegistry.getTransactionService();
        txn = transactionService.getUserTransaction();
        txn.begin();
        StoreRef storeRef = new StoreRef(StoreRef.PROTOCOL_WORKSPACE, getName());
        if (!nodeService.exists(storeRef))
        {
            nodeService.createStore(storeRef.getProtocol(), storeRef.getIdentifier());
        }
        rootNodeRef = nodeService.getRootNode(storeRef);
        
        properties = new PropertyMap();
        properties.put(IntegrityTest.TEST_PROP_TEXT_C, "abc");
    }
    
    public void tearDown() throws Exception
    {
        authenticationComponent.clearCurrentSecurityContext();
        txn.rollback();
    }

    /**
     * Create a node of the given type, and hanging off the root node
     */
    private NodeRef createNode(String name, QName type, PropertyMap properties)
    {
        return nodeService.createNode(
                rootNodeRef,
                ContentModel.ASSOC_CHILDREN,
                QName.createQName(IntegrityTest.NAMESPACE, name),
                type,
                properties
                ).getChildRef();
    }
    
    public void testSetUp() throws Exception
    {
        assertNotNull("IncompleteNodeTagger not created", tagger);
    }
    
    private void checkTagging(NodeRef nodeRef, boolean mustBeTagged)
    {
        tagger.beforeCommit(false);
        assertEquals(nodeService.hasAspect(nodeRef, ContentModel.ASPECT_INCOMPLETE), mustBeTagged);
    }
    
    public void testCreateWithoutProperties() throws Exception
    {
        NodeRef nodeRef = createNode("abc", IntegrityTest.TEST_TYPE_WITH_PROPERTIES, null);
        checkTagging(nodeRef, true);
    }
    
    public void testCreateWithProperties() throws Exception
    {
        NodeRef nodeRef = createNode("abc", IntegrityTest.TEST_TYPE_WITH_PROPERTIES, properties);
        checkTagging(nodeRef, false);
    }
}
