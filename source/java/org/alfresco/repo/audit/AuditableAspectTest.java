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

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.BaseSpringTest;
import org.alfresco.util.debug.NodeStoreInspector;
import org.springframework.context.ApplicationContext;

/**
 * Checks that the behaviour of the {@link org.alfresco.repo.audit.AuditableAspect auditable aspect}
 * is correct.
 * 
 * @author Roy Wetherall
 */
public class AuditableAspectTest extends BaseSpringTest 
{
    private static final ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();
    
    /*
     * Services used by the tests
     */
    private NodeService nodeService;
    
    /**
     * Data used by the tests
     */
    private StoreRef storeRef;
    private NodeRef rootNodeRef;
    
    /**
     * On setup in transaction implementation
     */
    @Override
    protected void onSetUpInTransaction() throws Exception
    {
        // Set the services
        this.nodeService = (NodeService)ctx.getBean("dbNodeService");
        
        // Create the store and get the root node reference
        this.storeRef = this.nodeService.createStore(StoreRef.PROTOCOL_WORKSPACE, "Test_" + System.currentTimeMillis());
        this.rootNodeRef = this.nodeService.getRootNode(storeRef);
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
     * ALF-2565: Allow cm:auditable values to be set programmatically
     */
    public void testCreateNodeWithAuditableProperties_ALF_2565()
    {
        // Create a person (which doesn't have auditable capability by default)
        Map<QName, Serializable> personProps = new HashMap<QName, Serializable>();
        personProps.put(ContentModel.PROP_USERNAME, "test person");
        personProps.put(ContentModel.PROP_HOMEFOLDER, rootNodeRef);
        personProps.put(ContentModel.PROP_FIRSTNAME, "test first name ");
        personProps.put(ContentModel.PROP_LASTNAME, "test last name");
        // Add some auditable properties
        Map<QName, Serializable> auditableProps = new HashMap<QName, Serializable>();
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
        NodeRef nodeRef = childAssocRef.getChildRef();
        
        assertAuditableProperties(nodeRef, auditableProps);
    }
    
    private void assertAuditableProperties(NodeRef nodeRef)
    {
        assertAuditableProperties(nodeRef, null);
    }
    
    private void assertAuditableProperties(NodeRef nodeRef, Map<QName, Serializable> checkProps)
    {
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
