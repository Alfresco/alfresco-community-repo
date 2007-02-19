/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
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
package org.alfresco.repo.audit;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.BaseSpringTest;
import org.alfresco.util.debug.NodeStoreInspector;

/**
 * Checks that the behaviour of the {@link org.alfresco.repo.audit.AuditableAspect auditable aspect}
 * is correct.
 * 
 * @author Roy Wetherall
 */
public class AuditableAspectTest extends BaseSpringTest 
{
	/**
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
	protected void onSetUpInTransaction() 
		throws Exception 
	{
		// Set the services
		this.nodeService = (NodeService)this.applicationContext.getBean("dbNodeService");
		
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

        // Assert the person is not auditable
        Set<QName> aspects = nodeService.getAspects(childAssocRef.getChildRef());
        assertFalse(aspects.contains(ContentModel.ASPECT_AUDITABLE));
        
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

        nodeService.addAspect(childAssocRef.getChildRef(), ContentModel.ASPECT_TITLED, null);
        
        // Assert the person is now audiable
        aspects = nodeService.getAspects(childAssocRef.getChildRef());
        assertTrue(aspects.contains(ContentModel.ASPECT_AUDITABLE));
        
        // Assert the person's auditable property
        assertAuditableProperties(childAssocRef.getChildRef());
        
        System.out.println(NodeStoreInspector.dumpNodeStore(nodeService, storeRef));
    }

    
    public void testAddAspect()
    {
        // Create a person (which doesn't have auditable capability by default)
        Map<QName, Serializable> personProps = new HashMap<QName, Serializable>();
        personProps.put(ContentModel.PROP_USERNAME, "test person");
        personProps.put(ContentModel.PROP_HOMEFOLDER, rootNodeRef);
        personProps.put(ContentModel.PROP_FIRSTNAME, "test first name ");
        personProps.put(ContentModel.PROP_LASTNAME, "test last name");
        
        ChildAssociationRef childAssocRef = nodeService.createNode(
                rootNodeRef,
                ContentModel.ASSOC_CHILDREN,
                QName.createQName("{test}testperson"),
                ContentModel.TYPE_PERSON,
                personProps);

        // Add auditable capability
        nodeService.addAspect(childAssocRef.getChildRef(), ContentModel.ASPECT_TITLED, null);

        System.out.println(NodeStoreInspector.dumpNodeStore(nodeService, storeRef));
    }


    private void assertAuditableProperties(NodeRef nodeRef)
    {
        Map<QName, Serializable> props = nodeService.getProperties(nodeRef);
        assertNotNull(props.get(ContentModel.PROP_CREATED));
        assertNotNull(props.get(ContentModel.PROP_MODIFIED));
        assertNotNull(props.get(ContentModel.PROP_CREATOR));
        assertNotNull(props.get(ContentModel.PROP_MODIFIER));
    }
    
}
