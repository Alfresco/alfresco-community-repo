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
package org.alfresco.repo.audit;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.debug.NodeStoreInspector;
import org.springframework.context.ApplicationContext;

/**
 * Checks that the behaviour of the {@link org.alfresco.repo.audit.AuditableAspect auditable aspect}
 * is correct.
 * 
 * @author Derek Hulley
 */
public class AuditableAspectTest extends TestCase
{
    private static final ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();
    
    private TransactionService transactionService;
    private NodeService nodeService;
    private ContentService contentService;
    private BehaviourFilter behaviourFilter;
    
    private StoreRef storeRef;
    private NodeRef rootNodeRef;
    
    @Override
    public void setUp() throws Exception
    {
        ServiceRegistry serviceRegistry = (ServiceRegistry) ctx.getBean(ServiceRegistry.SERVICE_REGISTRY);
        // Set the services
        this.transactionService = serviceRegistry.getTransactionService();
        this.nodeService = serviceRegistry.getNodeService();
        this.contentService = serviceRegistry.getContentService();
        this.behaviourFilter = (BehaviourFilter) ctx.getBean("policyBehaviourFilter");
        
        AuthenticationUtil.setRunAsUserSystem();
        
        // Create the store and get the root node reference
        this.storeRef = this.nodeService.createStore(StoreRef.PROTOCOL_WORKSPACE, "Test_" + System.currentTimeMillis());
        this.rootNodeRef = this.nodeService.getRootNode(storeRef);
    }
    
    @Override
    protected void tearDown() throws Exception
    {
        AuthenticationUtil.clearCurrentSecurityContext();
    }
    
    public void testAudit()
    {
        // Create a folder
        ChildAssociationRef childAssocRef = nodeService.createNode(
                rootNodeRef,
                ContentModel.ASSOC_CHILDREN,
                QName.createQName("{test}testfolder"),
                ContentModel.TYPE_FOLDER);
        
        // Assert auditable properties exist on folder
        assertAuditableProperties(childAssocRef.getChildRef());
        
        System.out.println(NodeStoreInspector.dumpNodeStore(nodeService, storeRef));
    }
    
    public void testNoAudit()
    {
        // Create a person (which doesn't have auditable capability by default)
        Map<QName, Serializable> personProps = new HashMap<QName, Serializable>();
        personProps.put(ContentModel.PROP_USERNAME, "test person");
        personProps.put(ContentModel.PROP_HOMEFOLDER, rootNodeRef);
        personProps.put(ContentModel.PROP_FIRSTNAME, "test first name");
        personProps.put(ContentModel.PROP_LASTNAME, "test last name");
        personProps.put(ContentModel.PROP_SIZE_CURRENT, 0);
        
        ChildAssociationRef childAssocRef = nodeService.createNode(
                rootNodeRef,
                ContentModel.ASSOC_CHILDREN,
                QName.createQName("{test}testperson"),
                ContentModel.TYPE_PERSON,
                personProps);
        NodeRef nodeRef = childAssocRef.getChildRef();
        
        // Assert the person is not auditable
        Set<QName> aspects = nodeService.getAspects(nodeRef);
        assertFalse("cm:auditable must not be present.", aspects.contains(ContentModel.ASPECT_AUDITABLE));
        Map<QName, Serializable> properties = nodeService.getProperties(nodeRef);
        assertFalse("cm:creator must not be present", properties.containsKey(ContentModel.PROP_CREATOR));
        assertFalse("cm:created must not be present", properties.containsKey(ContentModel.PROP_CREATED));
        
        assertNull(
                "Didn't expect to get single auditable property",
                nodeService.getProperty(nodeRef, ContentModel.PROP_CREATOR));
        
        System.out.println(NodeStoreInspector.dumpNodeStore(nodeService, storeRef));
    }
    
    public void testAddAudit()
    {
        // Create a person
        Map<QName, Serializable> personProps = new HashMap<QName, Serializable>();
        personProps.put(ContentModel.PROP_USERNAME, "test person");
        personProps.put(ContentModel.PROP_HOMEFOLDER, rootNodeRef);
        personProps.put(ContentModel.PROP_FIRSTNAME, "test first name");
        personProps.put(ContentModel.PROP_LASTNAME, "test last name");
        personProps.put(ContentModel.PROP_SIZE_CURRENT, 0);
        
        ChildAssociationRef childAssocRef = nodeService.createNode(
                rootNodeRef,
                ContentModel.ASSOC_CHILDREN,
                QName.createQName("{test}testperson"),
                ContentModel.TYPE_PERSON,
                personProps);
        
        // Assert the person is not auditable
        Set<QName> aspects = nodeService.getAspects(childAssocRef.getChildRef());
        assertFalse(aspects.contains(ContentModel.ASPECT_AUDITABLE));
        
        // Add auditable capability
        nodeService.addAspect(childAssocRef.getChildRef(), ContentModel.ASPECT_AUDITABLE, null);
        
        // Add (titled) aspect
        nodeService.addAspect(childAssocRef.getChildRef(), ContentModel.ASPECT_TITLED, null);
        
        // Assert the person is now audiable
        aspects = nodeService.getAspects(childAssocRef.getChildRef());
        assertTrue(aspects.contains(ContentModel.ASPECT_AUDITABLE));
        
        // Assert the person's auditable property
        assertAuditableProperties(childAssocRef.getChildRef());
        
        System.out.println(NodeStoreInspector.dumpNodeStore(nodeService, storeRef));
    }

    public synchronized void testAddAndRemoveAspect() throws Exception
    {
        // Create a person (which doesn't have auditable capability by default)
        Map<QName, Serializable> personProps = new HashMap<QName, Serializable>();
        personProps.put(ContentModel.PROP_USERNAME, "test person");
        personProps.put(ContentModel.PROP_HOMEFOLDER, rootNodeRef);
        personProps.put(ContentModel.PROP_FIRSTNAME, "test first name ");
        personProps.put(ContentModel.PROP_LASTNAME, "test last name");
        personProps.put(ContentModel.PROP_SIZE_CURRENT, 0);
        
        long t1 = System.currentTimeMillis();
        this.wait(100);                             // Needed for system clock inaccuracies
        
        ChildAssociationRef childAssocRef = nodeService.createNode(
                rootNodeRef,
                ContentModel.ASSOC_CHILDREN,
                QName.createQName("{test}testperson"),
                ContentModel.TYPE_PERSON,
                personProps);
        NodeRef nodeRef = childAssocRef.getChildRef();
        
        // Add auditable capability
        nodeService.addAspect(nodeRef, ContentModel.ASPECT_AUDITABLE, null);
        
        assertAuditableProperties(nodeRef);
        
        this.wait(100);                             // Needed for system clock inaccuracies
        long t2 = System.currentTimeMillis();
        
        // Check that the dates were set correctly
        Date aspectCreatedDate1 = (Date) nodeService.getProperty(nodeRef, ContentModel.PROP_CREATED);
        Date aspectModifiedDate1 = (Date) nodeService.getProperty(nodeRef, ContentModel.PROP_MODIFIED);
        assertTrue("Created date should be later than t1", t1 < aspectCreatedDate1.getTime());
        assertTrue(
                "Modified date must be after or on creation date",
                aspectCreatedDate1.getTime() <= aspectModifiedDate1.getTime() &&
                aspectModifiedDate1.getTime() < t2);
        
        long t3 = System.currentTimeMillis();
        this.wait(100);                             // Needed for system clock inaccuracies
        
        // Pause to allow for node modifiedDate tolerance (of 1000ms - refer to AbstractNodeDAOImpl.updateNodeImpl)
        this.wait(1500); //
        
        // Add (titled) aspect
        nodeService.addAspect(nodeRef, ContentModel.ASPECT_TITLED, null);
        
        // Check that the dates were set correctly
        Date aspectCreatedDate2 = (Date) nodeService.getProperty(nodeRef, ContentModel.PROP_CREATED);
        Date aspectModifiedDate2 = (Date) nodeService.getProperty(nodeRef, ContentModel.PROP_MODIFIED);
        assertEquals("The created date must not change", aspectCreatedDate1, aspectCreatedDate2);
        assertTrue("New modified date should be later than t3", t3 < aspectModifiedDate2.getTime());
        
        System.out.println(NodeStoreInspector.dumpNodeStore(nodeService, storeRef));
        
        nodeService.removeAspect(nodeRef, ContentModel.ASPECT_AUDITABLE);
        
        assertNotAuditableProperties(nodeRef);
        
        System.out.println(NodeStoreInspector.dumpNodeStore(nodeService, storeRef));
    }
    
    /**
     * ALF-2565: Allow cm:auditable values to be set programmatically<br/>
     * ALF-4117: NodeDAO: Allow cm:auditable to be set
     * ALF-3569: Alfresco repository CIFS driver not setting timestamps
     */
    public void testCreateAndUpdateAuditableProperties()
    {
        // Create a person (which doesn't have auditable capability by default)
        Map<QName, Serializable> personProps = new HashMap<QName, Serializable>();
        personProps.put(ContentModel.PROP_USERNAME, "test person");
        personProps.put(ContentModel.PROP_HOMEFOLDER, rootNodeRef);
        personProps.put(ContentModel.PROP_FIRSTNAME, "test first name ");
        personProps.put(ContentModel.PROP_LASTNAME, "test last name");
        personProps.put(ContentModel.PROP_SIZE_CURRENT, 0);
        // Add some auditable properties
        final Map<QName, Serializable> auditableProps = new HashMap<QName, Serializable>();
        auditableProps.put(ContentModel.PROP_CREATED, new Date(0L));
        auditableProps.put(ContentModel.PROP_CREATOR, "ZeroPerson");
        auditableProps.put(ContentModel.PROP_MODIFIED, new Date(1L));
        auditableProps.put(ContentModel.PROP_MODIFIER, "OnePerson");
        
        personProps.putAll(auditableProps);

        ChildAssociationRef childAssocRef = nodeService.createNode(
                    rootNodeRef,
                    ContentModel.ASSOC_CHILDREN,
                    QName.createQName("{test}testperson"),
                    ContentModel.TYPE_PERSON,
                    personProps);
        final NodeRef nodeRef = childAssocRef.getChildRef();
        // Check
        assertAuditableProperties(nodeRef, auditableProps);
        
        // Now modify the node so that the auditable values advance
        nodeService.setProperty(nodeRef, ContentModel.PROP_FIRSTNAME, "TEST-FIRST-NAME-" + System.currentTimeMillis());
        String modifiedBy = (String) nodeService.getProperty(nodeRef, ContentModel.PROP_MODIFIER);
        assertEquals(
                    "The modifier should have changed to reflect the current user",
                    AuthenticationUtil.getRunAsUser(), modifiedBy);
        
        RetryingTransactionCallback<Void> setAuditableCallback1 = new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                behaviourFilter.disableBehaviour(ContentModel.ASPECT_AUDITABLE);        // Lasts for txn
                // Set the auditable properties explicitly
                Long currentTime = System.currentTimeMillis();
                auditableProps.put(ContentModel.PROP_CREATOR, "Creator-" +currentTime);
                auditableProps.put(ContentModel.PROP_CREATED, new Date(currentTime - 1000L));
                auditableProps.put(ContentModel.PROP_MODIFIER, "Modifier-" + currentTime);
                auditableProps.put(ContentModel.PROP_MODIFIED, new Date(currentTime - 1000L));
                nodeService.addProperties(nodeRef, auditableProps);
                // Done
                return null;
            }
        };
        transactionService.getRetryingTransactionHelper().doInTransaction(setAuditableCallback1);
        // Check
        assertAuditableProperties(nodeRef, auditableProps);
        
        RetryingTransactionCallback<Void> setAuditableCallback2 = new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                behaviourFilter.disableBehaviour(ContentModel.ASPECT_AUDITABLE);        // Lasts for txn
                // Set some other property and ensure that the cm:auditable does not change
                nodeService.setProperty(nodeRef, ContentModel.PROP_FIRSTNAME, "TEST-FIRST-NAME-" + System.currentTimeMillis());
                // Done
                return null;
            }
        };
        transactionService.getRetryingTransactionHelper().doInTransaction(setAuditableCallback2);
        // Check
        assertAuditableProperties(nodeRef, auditableProps);
    }
    
    public void testPutContent() throws Exception
    {
        String fileName = "testContent";
        Map<QName, Serializable> props = new HashMap<QName, Serializable>();
        props.put(ContentModel.PROP_NAME, fileName);
        ChildAssociationRef childAssocRef = nodeService.createNode(
                rootNodeRef,
                ContentModel.ASSOC_CHILDREN,
                QName.createQNameWithValidLocalName(NamespaceService.CONTENT_MODEL_1_0_URI, fileName),
                ContentModel.TYPE_CONTENT,
                props);
        final NodeRef nodeRef = childAssocRef.getChildRef();
        
        final Date modified = (Date) nodeService.getProperty(nodeRef, ContentModel.PROP_MODIFIED);
        
        RetryingTransactionCallback<Date> dateCallback = new RetryingTransactionCallback<Date>()
        {
            
            @Override
            public Date execute() throws Throwable
            {
                ContentWriter writer = contentService.getWriter(nodeRef, ContentModel.PROP_CONTENT, true);
                writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
                writer.setEncoding("UTF-8");
                String text = "Test content";
                
                writer.putContent(text);
                
                return (Date) nodeService.getProperty(nodeRef, ContentModel.PROP_MODIFIED);
            }
        };

        final Date txnModified = transactionService.getRetryingTransactionHelper().doInTransaction(dateCallback);
        assertTrue("Last modified date should be changed in individual transaction", modified.getTime() < txnModified.getTime());
        
        RetryingTransactionCallback<Integer> countCallback = new RetryingTransactionCallback<Integer>()
        {
            
            @Override
            public Integer execute() throws Throwable
            {
                int count = 0;
                Date oldModified = txnModified;
                
                ContentWriter writer = contentService.getWriter(nodeRef, ContentModel.PROP_CONTENT, true);
                writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
                writer.setEncoding("UTF-8");
                String text = "Test content update";
                
                writer.putContent(text);
                Date newModified = (Date) nodeService.getProperty(nodeRef, ContentModel.PROP_MODIFIED);
                
                if (newModified.getTime() > oldModified.getTime())
                {
                    count++;
                }
                
                oldModified = newModified;

                writer = contentService.getWriter(nodeRef, ContentModel.PROP_CONTENT, true);
                writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
                writer.setEncoding("UTF-8");
                text = "Test content another update";
                
                writer.putContent(text);
                
                newModified = (Date) nodeService.getProperty(nodeRef, ContentModel.PROP_MODIFIED);
                
                if (newModified.getTime() > oldModified.getTime())
                {
                    count++;
                }
                
                return count;
            }
        };
        
        Integer count = transactionService.getRetryingTransactionHelper().doInTransaction(countCallback);
        
        assertTrue("Repeated content uploads in the same transaction should only modify the cm:modified once.", count == 1);
    }
    
    private void assertAuditableProperties(NodeRef nodeRef)
    {
        assertAuditableProperties(nodeRef, null);
    }
    
    private void assertAuditableProperties(NodeRef nodeRef, Map<QName, Serializable> checkProps)
    {
        assertTrue("Auditable aspect not present", nodeService.hasAspect(nodeRef, ContentModel.ASPECT_AUDITABLE));
        
        Map<QName, Serializable> props = nodeService.getProperties(nodeRef);
        assertNotNull(props.get(ContentModel.PROP_CREATED));
        assertNotNull(props.get(ContentModel.PROP_MODIFIED));
        assertNotNull(props.get(ContentModel.PROP_CREATOR));
        assertNotNull(props.get(ContentModel.PROP_MODIFIER));
        if (checkProps != null)
        {
            assertEquals("PROP_CREATED not correct",
                        checkProps.get(ContentModel.PROP_CREATED), props.get(ContentModel.PROP_CREATED));
            assertEquals("PROP_MODIFIED not correct",
                        checkProps.get(ContentModel.PROP_MODIFIED), props.get(ContentModel.PROP_MODIFIED));
            assertEquals("PROP_CREATOR not correct",
                        checkProps.get(ContentModel.PROP_CREATOR), props.get(ContentModel.PROP_CREATOR));
            assertEquals("PROP_MODIFIER not correct",
                        checkProps.get(ContentModel.PROP_MODIFIER), props.get(ContentModel.PROP_MODIFIER));
        }
    }
    
    private void assertNotAuditableProperties(NodeRef nodeRef)
    {
        Map<QName, Serializable> props = nodeService.getProperties(nodeRef);
        assertNull(props.get(ContentModel.PROP_CREATED));
        assertNull(props.get(ContentModel.PROP_MODIFIED));
        assertNull(props.get(ContentModel.PROP_CREATOR));
        assertNull(props.get(ContentModel.PROP_MODIFIER));
    }
}
