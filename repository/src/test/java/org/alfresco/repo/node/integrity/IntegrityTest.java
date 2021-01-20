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
package org.alfresco.repo.node.integrity;

import java.io.InputStream;
import java.io.Serializable;
import java.util.Map;

import javax.transaction.UserTransaction;

import junit.framework.TestCase;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.dictionary.DictionaryDAO;
import org.alfresco.repo.dictionary.M2Model;
import org.alfresco.repo.node.BaseNodeServiceTest;
import org.alfresco.repo.node.encryption.MetadataEncryptor;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.test_category.OwnJVMTestsCategory;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.PropertyMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.experimental.categories.Category;
import org.springframework.context.ApplicationContext;

/**
 * Attempts to build faulty node structures in order to test integrity.
 * <p>
 * The entire application context is loaded as is, but the integrity fail-
 * mode is set to throw an exception.
 * 
 * TODO: Role name restrictions must be checked
 * 
 * @author Derek Hulley
 */
@SuppressWarnings("unused")
@Category(OwnJVMTestsCategory.class)
public class IntegrityTest extends TestCase
{
    private static Log logger = LogFactory.getLog(IntegrityTest.class);
    
    public static final String NAMESPACE = "http://www.alfresco.org/test/IntegrityTest";
    public static final String TEST_PREFIX = "integrity-test";
    
    public static final QName TEST_TYPE_WITHOUT_ANYTHING = QName.createQName(NAMESPACE, "typeWithoutAnything");
    public static final QName TEST_TYPE_WITH_ASPECT = QName.createQName(NAMESPACE, "typeWithAspect");
    public static final QName TEST_TYPE_WITH_PROPERTIES = QName.createQName(NAMESPACE, "typeWithProperties");
    public static final QName TEST_TYPE_WITH_ENCRYPTED_PROPERTIES = QName.createQName(NAMESPACE, "typeWithEncryptedProperties");
    public static final QName TEST_TYPE_WITH_ASSOCS = QName.createQName(NAMESPACE, "typeWithAssocs");
    public static final QName TEST_TYPE_WITH_CHILD_ASSOCS = QName.createQName(NAMESPACE, "typeWithChildAssocs");
    public static final QName TEST_TYPE_WITH_NON_ENFORCED_CHILD_ASSOCS = QName.createQName(NAMESPACE, "typeWithNonEnforcedChildAssocs");
    public static final QName TEST_TYPE_WITH_NON_ENFORCED_TARGET_ASSOCS = QName.createQName(NAMESPACE, "typeWithNonEnforcedTargetAssocs");
    
    public static final QName TEST_ASSOC_NODE_ZEROMANY_ZEROMANY = QName.createQName(NAMESPACE, "assoc-0to* - 0to*");
    public static final QName TEST_ASSOC_CHILD_ZEROMANY_ZEROMANY = QName.createQName(NAMESPACE, "child-0to* - 0to*");
    public static final QName TEST_ASSOC_NODE_ONE_ONE = QName.createQName(NAMESPACE, "assoc-1to1 - 1to1");
    public static final QName TEST_ASSOC_NODE_ONE_MANY = QName.createQName(NAMESPACE, "assoc-1to1 - 0to*");
    public static final QName TEST_ASSOC_CHILD_ONE_ONE = QName.createQName(NAMESPACE, "child-1to1 - 1to1");
    public static final QName TEST_ASSOC_ASPECT_ONE_ONE = QName.createQName(NAMESPACE, "aspect-assoc-1to1 - 1to1");
    public static final QName TEST_ASSOC_CHILD_NON_ENFORCED = QName.createQName(NAMESPACE, "child-non-enforced");
    public static final QName TEST_ASSOC_TARGET_NON_ENFORCED = QName.createQName(NAMESPACE, "target-non-enforced");
    
    public static final QName TEST_ASPECT_WITH_PROPERTIES = QName.createQName(NAMESPACE, "aspectWithProperties");
    public static final QName TEST_ASPECT_WITH_ASSOC = QName.createQName(NAMESPACE, "aspectWithAssoc");
    public static final QName TEST_ASPECT_WITH_ASPECT = QName.createQName(NAMESPACE, "aspectWithAspect");
    
    public static final QName TEST_PROP_TEXT_A = QName.createQName(NAMESPACE, "prop-text-a");
    public static final QName TEST_PROP_TEXT_B = QName.createQName(NAMESPACE, "prop-text-b");
    public static final QName TEST_PROP_TEXT_C = QName.createQName(NAMESPACE, "prop-text-c");
    public static final QName TEST_PROP_TEXT_D = QName.createQName(NAMESPACE, "prop-text-d");
    public static final QName TEST_PROP_INT_A = QName.createQName(NAMESPACE, "prop-int-a");
    public static final QName TEST_PROP_INT_B = QName.createQName(NAMESPACE, "prop-int-b");
    public static final QName TEST_PROP_INT_C = QName.createQName(NAMESPACE, "prop-int-c");
    public static final QName TEST_PROP_ENCRYPTED_A = QName.createQName(NAMESPACE, "prop-encrypted-a");
    public static final QName TEST_PROP_ENCRYPTED_B = QName.createQName(NAMESPACE, "prop-encrypted-b");
    public static final QName TEST_PROP_ENCRYPTED_C = QName.createQName(NAMESPACE, "prop-encrypted-c");

    private IntegrityChecker integrityChecker;
    private ServiceRegistry serviceRegistry;
    private NodeService nodeService;
    private NodeRef rootNodeRef;
    private Map<QName, Serializable> allProperties;
    private UserTransaction txn;
    private AuthenticationComponent authenticationComponent;
    
    public void setUp() throws Exception
    {
        ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();
        DictionaryDAO dictionaryDao = (DictionaryDAO) ctx.getBean("dictionaryDAO");
        ClassLoader cl = BaseNodeServiceTest.class.getClassLoader();
        // load the test model
        InputStream modelStream = cl.getResourceAsStream("org/alfresco/repo/node/integrity/IntegrityTest_model.xml");
        assertNotNull(modelStream);
        M2Model model = M2Model.createModel(modelStream);
        dictionaryDao.putModel(model);

        integrityChecker = (IntegrityChecker) ctx.getBean("integrityChecker");
        integrityChecker.setEnabled(true);
        integrityChecker.setFailOnViolation(true);
        integrityChecker.setTraceOn(true);
        integrityChecker.setMaxErrorsPerTransaction(100);   // we want to count the correct number of errors
        
        MetadataEncryptor encryptor = (MetadataEncryptor) ctx.getBean("metadataEncryptor");

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
        
        allProperties = new PropertyMap();
        allProperties.put(TEST_PROP_TEXT_A, "ABC");
        allProperties.put(TEST_PROP_TEXT_B, "DEF");
        allProperties.put(TEST_PROP_INT_A, "123");
        allProperties.put(TEST_PROP_INT_B, "456");
        allProperties.put(TEST_PROP_ENCRYPTED_A, "ABC");
        allProperties.put(TEST_PROP_ENCRYPTED_B, "DEF");
        allProperties = encryptor.encrypt(allProperties);
    }
    
    public void tearDown() throws Exception
    {
        try
        {
            authenticationComponent.clearCurrentSecurityContext();
        }
        catch (Throwable e)
        {
            e.printStackTrace();
        }
        try
        {
            txn.rollback();
        }
        catch (Throwable e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Create a node of the given type, and hanging off the root node
     */
    private NodeRef createNode(String name, QName type, Map<QName, Serializable> properties)
    {
        return nodeService.createNode(
                rootNodeRef,
                ContentModel.ASSOC_CHILDREN,
                QName.createQName(NAMESPACE, name),
                type,
                properties
                ).getChildRef();
    }
    
    private void checkIntegrityNoFailure() throws Exception
    {
        integrityChecker.checkIntegrity();
    }
    
    /**
     * 
     * @param failureMsg the fail message if an integrity exception doesn't occur
     * @param expectedCount the expected number of integrity failures, or -1 to ignore
     */
    private void checkIntegrityExpectFailure(String failureMsg, int expectedCount)
    {
        try
        {
            integrityChecker.checkIntegrity();
            fail(failureMsg);
        }
        catch (IntegrityException e)
        {
            if (expectedCount >= 0)
            {
                assertEquals("Incorrect number of integrity records generated", expectedCount, e.getRecords().size());
            }
        }
    }
    
    public void testSetUp() throws Exception
    {
        assertNotNull("Static IntegrityChecker not created", integrityChecker);
    }
    
    public void testTemporaryDowngrading() throws Exception
    {
        assertEquals("Per-transaction override not correct", false, IntegrityChecker.isWarnInTransaction());
        // create bad node
        NodeRef nodeRef = createNode("abc", TEST_TYPE_WITH_PROPERTIES, null);
        // switch it off
        IntegrityChecker.setWarnInTransaction();
        assertEquals("Per-transaction override not correct", true, IntegrityChecker.isWarnInTransaction());
        // now, only warnings should occur
        checkIntegrityNoFailure();
    }
    
    public void testCreateWithoutProperties() throws Exception
    {
        NodeRef nodeRef = createNode("abc", TEST_TYPE_WITH_PROPERTIES, null);
        checkIntegrityExpectFailure("Failed to detect missing properties", 1);
    }
    
    public void testCreateWithProperties() throws Exception
    {
        NodeRef nodeRef = createNode("abc", TEST_TYPE_WITH_PROPERTIES, allProperties);
        checkIntegrityNoFailure();
    }
    
    public void testCreateWithNullMandatoryProperty() throws Exception
    {
        allProperties.put(TEST_PROP_TEXT_A, null);
        NodeRef nodeRef = createNode("abc", TEST_TYPE_WITH_PROPERTIES, allProperties);
        checkIntegrityExpectFailure("Failed to detect null mandatory properties", 1);
    }
    
    public void testCreateWithEmptyMandatoryProperty() throws Exception
    {
        allProperties.put(TEST_PROP_TEXT_A, "");
        NodeRef nodeRef = createNode("abc", TEST_TYPE_WITH_PROPERTIES, allProperties);
        checkIntegrityNoFailure();
        checkIntegrityNoFailure();
    }
    
    public void testCreateWithIncorrectListValue() throws Exception
    {
        allProperties.put(TEST_PROP_TEXT_D, "DEF");
        NodeRef nodeRef = createNode("def", TEST_TYPE_WITH_PROPERTIES, allProperties);
        checkIntegrityExpectFailure("Failed to detect incorrect LoV without trailing space", 1);
    }
    
    public void testCreateWithcorrectListValue() throws Exception
    {
        allProperties.put(TEST_PROP_TEXT_D, " HIJ ");
        NodeRef nodeRef = createNode("def", TEST_TYPE_WITH_PROPERTIES, allProperties);
        checkIntegrityNoFailure();
    }
    
    public void testMandatoryPropertiesRemoved() throws Exception
    {
        NodeRef nodeRef = createNode("abc", TEST_TYPE_WITH_PROPERTIES, allProperties);
        
        // Remove the automatically-added aspect
        nodeService.removeAspect(nodeRef, TEST_ASPECT_WITH_PROPERTIES);
        // remove all the properties
        PropertyMap properties = new PropertyMap();
        nodeService.setProperties(nodeRef, properties);
        
        checkIntegrityExpectFailure("Failed to detect missing removed properties", 1);
    }
    
    public void testCreateWithoutEncryption() throws Exception
    {
        allProperties.put(TEST_PROP_ENCRYPTED_A, "Not encrypted");
        try
        {
            NodeRef nodeRef = createNode("abc", TEST_TYPE_WITH_ENCRYPTED_PROPERTIES, allProperties);
            fail("Current detection of unencrypted properties is done by NodeService.");
        }
        catch (Throwable e)
        {
            // Expected
        }
        // checkIntegrityExpectFailure("Failed to detect unencrypted properties", 2);
    }

    public void testCreateWithoutPropertiesForAspect() throws Exception
    {
        NodeRef nodeRef = createNode("abc", TEST_TYPE_WITH_ASPECT, null);
        
        checkIntegrityExpectFailure("Failed to detect missing properties for aspect", 1);
    }

    public void testCreateWithPropertiesForAspect() throws Exception
    {
        NodeRef nodeRef = createNode("abc", TEST_TYPE_WITH_ASPECT, allProperties);
        checkIntegrityNoFailure();
    }

    public void testRemoveMandatoryAspect() throws Exception
    {
        NodeRef nodeRef = createNode("abc", TEST_TYPE_WITH_ASPECT, allProperties);
        // just remove the aspect
        nodeService.removeAspect(nodeRef, TEST_ASPECT_WITH_PROPERTIES);
        
        checkIntegrityExpectFailure("Failed to removal of mandatory aspect", 1);
    }

    public void testCreateWithAspectForAspect() throws Exception
    {
        NodeRef nodeRef = createNode("abc", TEST_TYPE_WITHOUT_ANYTHING, null);
        // Add the aspect
        nodeService.addAspect(nodeRef, TEST_ASPECT_WITH_ASPECT, allProperties);
        assertTrue("Mandatory aspect missing", nodeService.hasAspect(nodeRef, TEST_ASPECT_WITH_PROPERTIES));
        checkIntegrityNoFailure();
        
        // Remove the implied mandatory aspect
        nodeService.removeAspect(nodeRef, TEST_ASPECT_WITH_PROPERTIES);
        checkIntegrityExpectFailure("Failed to detect missing aspect for aspect", 1);
    }

    public void testCreateTargetOfAssocsWithMandatorySourcesPresent() throws Exception
    {
        // this is the target of 3 assoc types where the source cardinality is 1..1
        NodeRef targetAndChild = createNode("targetAndChild", TEST_TYPE_WITHOUT_ANYTHING, null);
        
        NodeRef source = createNode("source", TEST_TYPE_WITH_ASSOCS, null);
        nodeService.createAssociation(source, targetAndChild, TEST_ASSOC_NODE_ONE_ONE);

        NodeRef parent = createNode("parent", TEST_TYPE_WITH_CHILD_ASSOCS, null);
        nodeService.addChild(parent, targetAndChild, TEST_ASSOC_CHILD_ONE_ONE, QName.createQName(NAMESPACE, "mandatoryChild"));

        NodeRef aspected = createNode("aspectNode", TEST_TYPE_WITHOUT_ANYTHING, null);
        nodeService.addAspect(aspected, TEST_ASPECT_WITH_ASSOC, null);
        nodeService.createAssociation(aspected, targetAndChild, TEST_ASSOC_ASPECT_ONE_ONE);
        
        checkIntegrityNoFailure();
    }

    /**
     * TODO: The dictionary support for the reverse lookup of mandatory associations will
     *       allow this method to go in
     * <p>
     * <b>Does nothing</b>.
     */
    public void testCreateTargetOfAssocsWithMandatorySourcesMissing() throws Exception
    {
//        // this is the target of 3 associations where the source cardinality is 1..1
//        NodeRef target = createNode("abc", TEST_TYPE_WITHOUT_ANYTHING, null);
//        
//        checkIntegrityExpectFailure("Failed to detect missing mandatory assoc sources", 3);
        logger.error("Method commented out: testCreateTargetOfAssocsWithMandatorySourcesMissing");
    }

    /**
     * TODO: Reactivate once cascade delete notifications are back on
     * <p>
     * <b>Does nothing</b>.
     */
    public void testRemoveSourcesOfMandatoryAssocs() throws Exception
    {
//        // this is the target of 3 assoc types where the source cardinality is 1..1
//        NodeRef targetAndChild = createNode("targetAndChild", TEST_TYPE_WITHOUT_ANYTHING, null);
//        
//        NodeRef source = createNode("source", TEST_TYPE_WITH_ASSOCS, null);
//        nodeService.createAssociation(source, targetAndChild, TEST_ASSOC_NODE_ONE_ONE);
//
//        NodeRef parent = createNode("parent", TEST_TYPE_WITH_CHILD_ASSOCS, null);
//        nodeService.addChild(parent, targetAndChild, TEST_ASSOC_CHILD_ONE_ONE, QName.createQName(NAMESPACE, "mandatoryChild"));
//
//        NodeRef aspectSource = createNode("aspectSource", TEST_TYPE_WITHOUT_ANYTHING, null);
//        nodeService.addAspect(aspectSource, TEST_ASPECT_WITH_ASSOC, null);
//        nodeService.createAssociation(aspectSource, targetAndChild, TEST_ASSOC_ASPECT_ONE_ONE);
//        
//        checkIntegrityNoFailure();
//        
//        // remove source nodes
//        nodeService.deleteNode(source);
//        nodeService.deleteNode(parent);
//        nodeService.deleteNode(aspectSource);
//        
//        checkIntegrityExpectFailure("Failed to detect removal of mandatory assoc sources", 3);
        logger.error("Method commented out: testRemoveSourcesOfMandatoryAssocs");
    }
    
    public void testCreateSourceOfAssocsWithMandatoryTargetsPresent() throws Exception
    {
        NodeRef source = createNode("abc", TEST_TYPE_WITH_ASSOCS, null);
        NodeRef target = createNode("target", TEST_TYPE_WITHOUT_ANYTHING, null);
        nodeService.createAssociation(source, target, TEST_ASSOC_NODE_ONE_ONE);
        
        NodeRef parent = createNode("parent", TEST_TYPE_WITH_CHILD_ASSOCS, null);
        NodeRef child = createNode("child", TEST_TYPE_WITHOUT_ANYTHING, null);
        nodeService.addChild(parent, child, TEST_ASSOC_CHILD_ONE_ONE, QName.createQName(NAMESPACE, "one-to-one"));
        
        NodeRef aspectSource = createNode("aspectSource", TEST_TYPE_WITHOUT_ANYTHING, null);
        nodeService.addAspect(aspectSource, TEST_ASPECT_WITH_ASSOC, null);
        NodeRef aspectTarget = createNode("aspectTarget", TEST_TYPE_WITHOUT_ANYTHING, null);
        nodeService.createAssociation(aspectSource, aspectTarget, TEST_ASSOC_ASPECT_ONE_ONE);
        
        checkIntegrityNoFailure();
    }

    public void testCreateSourceOfAssocsWithMandatoryTargetsMissing() throws Exception
    {
        NodeRef source = createNode("abc", TEST_TYPE_WITH_ASSOCS, null);
        
        NodeRef parent = createNode("parent", TEST_TYPE_WITH_CHILD_ASSOCS, null);

        NodeRef aspectSource = createNode("aspectSource", TEST_TYPE_WITHOUT_ANYTHING, null);
        nodeService.addAspect(aspectSource, TEST_ASPECT_WITH_ASSOC, null);
        
        checkIntegrityExpectFailure("Failed to detect missing assoc targets", 3);
    }

    public void testRemoveTargetsOfMandatoryAssocs() throws Exception
    {
        NodeRef source = createNode("abc", TEST_TYPE_WITH_ASSOCS, null);
        NodeRef target = createNode("target", TEST_TYPE_WITHOUT_ANYTHING, null);
        nodeService.createAssociation(source, target, TEST_ASSOC_NODE_ONE_ONE);
        
        NodeRef parent = createNode("parent", TEST_TYPE_WITH_CHILD_ASSOCS, null);
        NodeRef child = createNode("child", TEST_TYPE_WITHOUT_ANYTHING, null);
        nodeService.addChild(parent, child, TEST_ASSOC_CHILD_ONE_ONE, QName.createQName(NAMESPACE, "one-to-one"));
        
        NodeRef aspectSource = createNode("aspectSource", TEST_TYPE_WITHOUT_ANYTHING, null);
        nodeService.addAspect(aspectSource, TEST_ASPECT_WITH_ASSOC, null);
        NodeRef aspectTarget = createNode("aspectTarget", TEST_TYPE_WITHOUT_ANYTHING, null);
        nodeService.createAssociation(aspectSource, aspectTarget, TEST_ASSOC_ASPECT_ONE_ONE);
        
        checkIntegrityNoFailure();
        
        // remove target nodes
        nodeService.deleteNode(target);
        nodeService.deleteNode(child);
        nodeService.deleteNode(aspectTarget);
        
        checkIntegrityExpectFailure("Failed to detect removal of mandatory assoc targets", 3);
    }

    public void testExcessTargetsOfOneToOneAssocs() throws Exception
    {
        NodeRef source = createNode("abc", TEST_TYPE_WITH_ASSOCS, null);
        NodeRef target1 = createNode("target1", TEST_TYPE_WITHOUT_ANYTHING, null);
        NodeRef target2 = createNode("target2", TEST_TYPE_WITHOUT_ANYTHING, null);
        nodeService.createAssociation(source, target1, TEST_ASSOC_NODE_ONE_ONE);
        nodeService.createAssociation(source, target2, TEST_ASSOC_NODE_ONE_ONE);
        
        NodeRef parent = createNode("parent", TEST_TYPE_WITH_CHILD_ASSOCS, null);
        NodeRef child1 = createNode("child1", TEST_TYPE_WITHOUT_ANYTHING, null);
        NodeRef child2 = createNode("child2", TEST_TYPE_WITHOUT_ANYTHING, null);
        nodeService.addChild(parent, child1, TEST_ASSOC_CHILD_ONE_ONE, QName.createQName(NAMESPACE, "one-to-one-first"));
        nodeService.addChild(parent, child2, TEST_ASSOC_CHILD_ONE_ONE, QName.createQName(NAMESPACE, "one-to-one-second"));
        
        NodeRef aspectSource = createNode("aspectSource", TEST_TYPE_WITHOUT_ANYTHING, null);
        nodeService.addAspect(aspectSource, TEST_ASPECT_WITH_ASSOC, null);
        NodeRef aspectTarget1 = createNode("aspectTarget1", TEST_TYPE_WITHOUT_ANYTHING, null);
        NodeRef aspectTarget2 = createNode("aspectTarget2", TEST_TYPE_WITHOUT_ANYTHING, null);
        nodeService.createAssociation(aspectSource, aspectTarget1, TEST_ASSOC_ASPECT_ONE_ONE);
        nodeService.createAssociation(aspectSource, aspectTarget2, TEST_ASSOC_ASPECT_ONE_ONE);
        
        checkIntegrityExpectFailure("Failed to detect excess target cardinality for one-to-one assocs", 3);
    }

    public void testExcessTargetsOfOneToManyAssocs() throws Exception
    {
        NodeRef source = createNode("abc", TEST_TYPE_WITH_ASSOCS, null);
        NodeRef target1 = createNode("target1", TEST_TYPE_WITHOUT_ANYTHING, null);
        NodeRef target2 = createNode("target2", TEST_TYPE_WITHOUT_ANYTHING, null);
        nodeService.createAssociation(source, target1, TEST_ASSOC_NODE_ONE_MANY);
        nodeService.createAssociation(source, target2, TEST_ASSOC_NODE_ONE_MANY);
        
        checkIntegrityExpectFailure("Failed to detect excess source cardinality for one-to-many assocs", 1);
    }

    public void testSourceAssocAfterDeletion() throws Exception
    {
        NodeRef source1 = createNode("abc", TEST_TYPE_WITH_ASSOCS, null);
        NodeRef source2 = createNode("abc", TEST_TYPE_WITH_ASSOCS, null);
        NodeRef target1 = createNode("target1", TEST_TYPE_WITHOUT_ANYTHING, null);
        NodeRef target2 = createNode("target1", TEST_TYPE_WITHOUT_ANYTHING, null);
        nodeService.createAssociation(source1, target1, TEST_ASSOC_NODE_ONE_ONE);
        nodeService.createAssociation(source2, target2, TEST_ASSOC_NODE_ONE_ONE);
        checkIntegrityNoFailure();

        nodeService.createAssociation(source1, target1, TEST_ASSOC_NODE_ONE_MANY);
        nodeService.createAssociation(source2, target1, TEST_ASSOC_NODE_ONE_MANY);
        // Both (or either of) the associations are in violation
        checkIntegrityExpectFailure("Failed to detect excess source cardinality for one-to-many assocs", 1);
        
        // Now remove one of the associations
        nodeService.removeAssociation(source2, target1, TEST_ASSOC_NODE_ONE_MANY);
        checkIntegrityNoFailure();
        
        // Now remove the last association
        nodeService.removeAssociation(source1, target1, TEST_ASSOC_NODE_ONE_MANY);
        checkIntegrityNoFailure();
    }
    
    /**
     * Test for MNT-12367
     */
    public void testRelaxedMandatoryAssociation() throws Exception
    {
        assertEquals("Per-transaction override not correct", false, IntegrityChecker.isWarnInTransaction());
        // create node
        NodeRef nodeRef = createNode("relaxedAssocNode", TEST_TYPE_WITH_NON_ENFORCED_TARGET_ASSOCS, null);
        checkIntegrityNoFailure();
    }
}
