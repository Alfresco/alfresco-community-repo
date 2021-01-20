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
package org.alfresco.repo.node.db;

import java.io.InputStream;
import java.util.Date;
import java.util.Map;

import javax.transaction.UserTransaction;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.dictionary.DictionaryComponent;
import org.alfresco.repo.dictionary.DictionaryDAO;
import org.alfresco.repo.dictionary.M2Model;
import org.alfresco.repo.domain.dialect.Dialect;
import org.alfresco.repo.domain.node.NodeDAO;
import org.alfresco.repo.node.BaseNodeServiceTest;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.BaseSpringTest;
import org.alfresco.util.testing.category.DBTests;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.context.ApplicationContext;
import org.springframework.test.annotation.Commit;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Transactional;

/**
 * @see org.alfresco.repo.node.db.DbNodeServiceImpl#propagateTimeStamps(ChildAssociationRef)
 * 
 * @author sergey.shcherbovich
 */
@Category(DBTests.class)
@Transactional
public class DbNodeServiceImplPropagationTest extends BaseSpringTest 
{
    private TransactionService txnService;
    private NodeDAO nodeDAO;
    private NodeService nodeService;
    private AuthenticationComponent authenticationComponent;
    protected DictionaryService dictionaryService;

    private UserTransaction txn = null;
    private Dialect dialect;

    @Before
    public void before() throws Exception
    {
        txnService = (TransactionService) applicationContext.getBean("transactionComponent");
        nodeDAO = (NodeDAO) applicationContext.getBean("nodeDAO");
        dialect = (Dialect) applicationContext.getBean("dialect");
        nodeService = (NodeService) applicationContext.getBean("dbNodeService");
        
        authenticationComponent = (AuthenticationComponent) applicationContext.getBean("authenticationComponent");
        
        authenticationComponent.setSystemUserAsCurrentUser();
        
        DictionaryDAO dictionaryDao = (DictionaryDAO) applicationContext.getBean("dictionaryDAO");
        // load the system model
        ClassLoader cl = BaseNodeServiceTest.class.getClassLoader();
        InputStream modelStream = cl.getResourceAsStream("alfresco/model/contentModel.xml");
        assertNotNull(modelStream);
        M2Model model = M2Model.createModel(modelStream);
        dictionaryDao.putModel(model);
        // load the test model
        modelStream = cl.getResourceAsStream("org/alfresco/repo/node/BaseNodeServiceTest_model.xml");
        assertNotNull(modelStream);
        model = M2Model.createModel(modelStream);
        dictionaryDao.putModel(model);
        
        DictionaryComponent dictionary = new DictionaryComponent();
        dictionary.setDictionaryDAO(dictionaryDao);
        dictionaryService = loadModel(applicationContext);
        
        restartAuditableTxn();
    }

    @After
    public void after() throws Exception
    {
        try
        {
            authenticationComponent.clearCurrentSecurityContext();
        }
        catch (Throwable e)
        {
            // do nothing
        }
    }

    /**
     * Loads the test model required for building the node graphs
     */
    public static DictionaryService loadModel(ApplicationContext applicationContext)
    {
        DictionaryDAO dictionaryDao = (DictionaryDAO) applicationContext.getBean("dictionaryDAO");
        // load the system model
        ClassLoader cl = BaseNodeServiceTest.class.getClassLoader();
        InputStream modelStream = cl.getResourceAsStream("alfresco/model/contentModel.xml");
        assertNotNull(modelStream);
        M2Model model = M2Model.createModel(modelStream);
        dictionaryDao.putModel(model);
        // load the test model
        modelStream = cl.getResourceAsStream("org/alfresco/repo/node/BaseNodeServiceTest_model.xml");
        assertNotNull(modelStream);
        model = M2Model.createModel(modelStream);
        dictionaryDao.putModel(model);
        
        DictionaryComponent dictionary = new DictionaryComponent();
        dictionary.setDictionaryDAO(dictionaryDao);
        // done
        return dictionary;
    }
    /**
     * Tests that the auditable modification details (modified by, modified at)
     *  get correctly propagated to the parent, where appropriate, when children
     *  are added or removed.
     */
    @SuppressWarnings("deprecation")
    @Commit
    @Test
    public void testAuditablePropagation() throws Exception
    {
        String fullyAuthenticatedUser = AuthenticationUtil.getFullyAuthenticatedUser();

        final QName TYPE_NOT_AUDITABLE = ContentModel.TYPE_CONTAINER;
        final QName TYPE_AUDITABLE = ContentModel.TYPE_CONTENT;
        final QName ASSOC_NOT_AUDITABLE = ContentModel.ASSOC_CHILDREN;
        final QName ASSOC_AUDITABLE = ContentModel.ASSOC_CONTAINS;
        
        // create a first store directly
        StoreRef storeRef = nodeService.createStore(
                StoreRef.PROTOCOL_WORKSPACE,
                "Test_" + System.currentTimeMillis());
        NodeRef rootNodeRef = nodeService.getRootNode(storeRef);
        
        Map<QName, ChildAssociationRef> assocRefs = BaseNodeServiceTest.buildNodeGraph(nodeService, rootNodeRef);
        // UserTransaction txn = null;
        Date modifiedAt = null;

        // Get the root node to test against
        ChildAssociationRef n2pn4Ref = assocRefs.get(QName.createQName(BaseNodeServiceTest.NAMESPACE, "n2_p_n4"));
        final NodeRef n2Ref = n2pn4Ref.getParentRef();
        final long n2Id = nodeDAO.getNodePair(n2Ref).getFirst();

        // Doesn't start out auditable
        assertFalse("Shouldn't be auditable in " + nodeService.getAspects(n2Ref), 
            nodeService.getAspects(n2Ref).contains(ContentModel.ASPECT_AUDITABLE));

        QName typeBefore = nodeService.getType(n2Ref);

        // Get onto our own transactions
        TestTransaction.flagForCommit();
        restartAuditableTxn();

        // Create a non-auditable child, parent won't update
        NodeRef naC = nodeService.createNode(n2Ref, ASSOC_NOT_AUDITABLE, 
                   QName.createQName("not-auditable"), TYPE_NOT_AUDITABLE).getChildRef();
        logger.debug("Created non-auditable child " + naC);

        restartAuditableTxn();

        // Parent hasn't been updated
        assertNull(nodeService.getProperty(n2Ref, ContentModel.PROP_MODIFIED));
        assertNull(nodeService.getProperty(n2Ref, ContentModel.PROP_MODIFIER));

        // Create an auditable child, parent won't update either as still not auditable
        NodeRef adC = nodeService.createNode(n2Ref, ASSOC_NOT_AUDITABLE, 
                   QName.createQName("is-auditable"), TYPE_AUDITABLE).getChildRef();
        nodeService.addAspect(adC, ContentModel.ASPECT_AUDITABLE, null);
        logger.debug("Created auditable child " + naC + " of non-auditable parent " + n2Ref);
        restartAuditableTxn();

        // Parent hasn't been updated, but auditable child has
        assertNull(nodeService.getProperty(n2Ref, ContentModel.PROP_MODIFIED));
        assertNull(nodeService.getProperty(n2Ref, ContentModel.PROP_MODIFIER));
        assertNotNull(nodeService.getProperty(adC, ContentModel.PROP_MODIFIED));
        assertNotNull(nodeService.getProperty(adC, ContentModel.PROP_MODIFIER));
        
        // Make the parent auditable, and give it a special modified by
        nodeService.addAspect(n2Ref, ContentModel.ASPECT_AUDITABLE, null);
        nodeService.setType(n2Ref, ContentModel.TYPE_FOLDER);
        restartAuditableTxn();

        Date modified = new Date();
        nodeDAO.setModifiedProperties(n2Id, modified, "TestModifier");
        restartAuditableTxn();
        assertEquals(modified.getTime(), ((Date)nodeDAO.getNodeProperty(n2Id, ContentModel.PROP_MODIFIED)).getTime());
        assertEquals("TestModifier", nodeDAO.getNodeProperty(n2Id, ContentModel.PROP_MODIFIER));

        // Delete the non-auditable child
        // No change to the parent as non-auditable child
        logger.debug("Deleting non-auditable child " + naC + " of auditable parent " + n2Ref);
        nodeService.addAspect(naC, ContentModel.ASPECT_TEMPORARY, null);
        nodeService.deleteNode(naC);
        restartAuditableTxn();

        assertEquals(modified.getTime(), ((Date)nodeDAO.getNodeProperty(n2Id, ContentModel.PROP_MODIFIED)).getTime());
        assertEquals("TestModifier", nodeDAO.getNodeProperty(n2Id, ContentModel.PROP_MODIFIER));

        // Add an auditable child, parent will be updated
        adC = nodeService.createNode(n2Ref, ASSOC_AUDITABLE, 
                   QName.createQName("is-auditable"), TYPE_AUDITABLE).getChildRef();
        final long adCId = nodeDAO.getNodePair(adC).getFirst();

        restartAuditableTxn();
        modifiedAt = (Date)nodeService.getProperty(n2Ref, ContentModel.PROP_MODIFIED);
        assertNotNull(modifiedAt);
        assertEquals((double)new Date().getTime(), (double)modifiedAt.getTime(), 10000d);
        assertEquals(fullyAuthenticatedUser, nodeService.getProperty(n2Ref, ContentModel.PROP_MODIFIER));

        // Set well-known modified details on both nodes
        nodeDAO.setModifiedProperties(n2Id, new Date(Integer.MIN_VALUE), "TestModifierPrnt");
        nodeDAO.setModifiedProperties(adCId, new Date(Integer.MIN_VALUE), "TestModifierChld");
        restartAuditableTxn();

        // Now delete the auditable child
        // The parent's modified date will change, but not the modified by, as the child
        // has been deleted so the child's modified-by can't be read
        logger.debug("Deleting auditable child " + adC + " of auditable parent " + n2Ref);
        nodeService.deleteNode(adC);
        restartAuditableTxn();

        // Parent's date was updated, but not the modifier, since child was deleted
        //  which means the child's modifier wasn't available to read
        modifiedAt = (Date)nodeService.getProperty(n2Ref, ContentModel.PROP_MODIFIED);
        assertNotNull(modifiedAt);
        assertEquals((double)new Date().getTime(), (double)modifiedAt.getTime(), 10000d);
        assertEquals(fullyAuthenticatedUser, nodeService.getProperty(n2Ref, ContentModel.PROP_MODIFIER));

        // Set well-known modified detail on our parent again
        modified = new Date();
        nodeDAO.setModifiedProperties(n2Id, modified,  "ModOn2");
        restartAuditableTxn();
        assertEquals("ModOn2", nodeService.getProperty(n2Ref, ContentModel.PROP_MODIFIER));

        // Add two auditable children, both with special modifiers
        // Only the first child's update in a transaction will be used
        NodeRef ac1 = nodeService.createNode(n2Ref, ASSOC_AUDITABLE, 
                          QName.createQName("is-auditable-1"), TYPE_AUDITABLE).getChildRef();
        NodeRef ac2 = nodeService.createNode(n2Ref, ASSOC_AUDITABLE, 
                          QName.createQName("is-auditable-2"), TYPE_AUDITABLE).getChildRef();
        final long ac1Id = nodeDAO.getNodePair(ac1).getFirst();
        final long ac2Id = nodeDAO.getNodePair(ac2).getFirst();

        // Manually set different modifiers on the children, so that
        //  we can test to see if they propagate properly
        nodeDAO.setModifiedProperties(ac1Id, new Date(), "ModAC1");
        nodeDAO.setModifiedProperties(ac2Id, new Date(), "ModAC2");
        // Ensure the parent is "old", so that the propagation can take place
        nodeDAO.setModifiedProperties(n2Id, new Date(Integer.MIN_VALUE), "ModOn2");

        restartAuditableTxn();

        // Check that only the first reached the parent
        assertNotNull(nodeService.getProperty(n2Ref, ContentModel.PROP_MODIFIED));
        assertNotNull(nodeService.getProperty(ac1, ContentModel.PROP_MODIFIED));
        assertNotNull(nodeService.getProperty(ac2, ContentModel.PROP_MODIFIED));

        assertEquals(fullyAuthenticatedUser, nodeService.getProperty(n2Ref, ContentModel.PROP_MODIFIER));
        assertEquals(fullyAuthenticatedUser, nodeService.getProperty(ac1, ContentModel.PROP_MODIFIER));
        assertEquals(fullyAuthenticatedUser, nodeService.getProperty(ac2, ContentModel.PROP_MODIFIER));

        // Updates won't apply if the parent is newer than the child
        Date now = new Date();
        long futureShift = 4000l;
        Date future = new Date(now.getTime()+futureShift);
        nodeDAO.setModifiedProperties(n2Id, future, "TestModifierPrnt");
        
        NodeRef ac3 = nodeService.createNode(n2Ref, ASSOC_AUDITABLE, 
                QName.createQName("is-auditable-3"), TYPE_AUDITABLE).getChildRef();

        restartAuditableTxn();

        assertEquals("TestModifierPrnt", nodeService.getProperty(n2Ref, ContentModel.PROP_MODIFIER));
        assertEquals(fullyAuthenticatedUser, nodeService.getProperty(ac3, ContentModel.PROP_MODIFIER));

        modifiedAt = (Date)nodeService.getProperty(n2Ref, ContentModel.PROP_MODIFIED);
        assertEquals((double)future.getTime(), (double)modifiedAt.getTime(), 1000d);
        modifiedAt = (Date)nodeService.getProperty(ac3, ContentModel.PROP_MODIFIED);
        assertEquals((double)now.getTime(), (double)modifiedAt.getTime(), 1000d);

        // Parent-Child association needs to be a suitable kind to trigger
        nodeService.setType(n2Ref, typeBefore);

        restartAuditableTxn();

        try
        {
            Thread.sleep(futureShift);
        }
        catch(InterruptedException e)
        {
        }

        modified = new Date();
        nodeDAO.setModifiedProperties(n2Id, modified, "TestModifier");

        restartAuditableTxn();

        assertEquals("TestModifier", nodeService.getProperty(n2Ref, ContentModel.PROP_MODIFIER));

        NodeRef ac4 = nodeService.createNode(n2Ref, ASSOC_NOT_AUDITABLE, 
                QName.createQName("is-auditable-4"), TYPE_AUDITABLE).getChildRef();

        restartAuditableTxn();

        assertEquals("TestModifier", nodeService.getProperty(n2Ref, ContentModel.PROP_MODIFIER));
        assertEquals(fullyAuthenticatedUser, nodeService.getProperty(ac4, ContentModel.PROP_MODIFIER));

        modifiedAt = (Date)nodeService.getProperty(n2Ref, ContentModel.PROP_MODIFIED);
        assertEquals(modified.getTime(), modifiedAt.getTime());
        modifiedAt = (Date)nodeService.getProperty(ac4, ContentModel.PROP_MODIFIED);
        assertEquals((double)new Date().getTime(), (double)modifiedAt.getTime(), 3000d);
    }
    
    private void restartAuditableTxn() throws Exception
    {
        TestTransaction.end();

        // Wait long enough that AuditablePropertiesEntity.setAuditModified
        // will recognize subsequent changes as needing new audit entries
        try
        {
            Thread.sleep(1250L);
        }
        catch(InterruptedException e)
        {
        }
        TestTransaction.start();
    }
}
