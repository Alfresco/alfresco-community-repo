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
package org.alfresco.repo.jscript;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.ScriptLocation;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.BaseSpringTest;

/**
 * 
 * 
 * @author Roy Wetherall
 */
public class ScriptBehaviourTest extends BaseSpringTest 
{
	private ServiceRegistry serviceRegistry;
	private NodeService nodeService;
	private PolicyComponent policyComponent;
	
	private StoreRef storeRef;
	private NodeRef folderNodeRef;
	
	protected String[] getConfigLocations()
    {
        return new String[] { "classpath:org/alfresco/repo/jscript/test-context.xml" };
    }
	
	/**
	 * On setup in transaction implementation
	 */
	@Override
	protected void onSetUpInTransaction() 
		throws Exception 
	{
		// Get the required services
		this.nodeService = (NodeService)this.applicationContext.getBean("nodeService");
		this.policyComponent = (PolicyComponent)this.applicationContext.getBean("policyComponent");
		this.serviceRegistry = (ServiceRegistry)this.applicationContext.getBean("ServiceRegistry");
		
		AuthenticationComponent authenticationComponent = (AuthenticationComponent)this.applicationContext.getBean("authenticationComponent");
		authenticationComponent.setCurrentUser("admin");
		
		// Create the store and get the root node reference
		this.storeRef = this.nodeService.createStore(StoreRef.PROTOCOL_WORKSPACE, "Test_" + System.currentTimeMillis());
		NodeRef rootNodeRef = this.nodeService.getRootNode(storeRef);
		
		// Create folder node
		Map<QName, Serializable> props = new HashMap<QName, Serializable>(1);
		props.put(ContentModel.PROP_NAME, "TestFolder");
		ChildAssociationRef childAssocRef = this.nodeService.createNode(
				rootNodeRef,
				ContentModel.ASSOC_CHILDREN,
				QName.createQName("{test}TestFolder"),
				ContentModel.TYPE_FOLDER,
				props);
		this.folderNodeRef = childAssocRef.getChildRef();
	}
	
	public void testEnableDiableBehaviour()
	{
		// Register the onCreateNode behaviour script
		ScriptLocation location = new ClasspathScriptLocation("org/alfresco/repo/jscript/test_onCreateNode_cmContent.js");
		ScriptBehaviour behaviour = new ScriptBehaviour(this.serviceRegistry, location);
		
		this.policyComponent.bindClassBehaviour(
				QName.createQName(NodeServicePolicies.OnCreateNodePolicy.NAMESPACE, "onCreateNode"),
				ContentModel.TYPE_CONTENT,
				behaviour);
		
		behaviour.disable();
		
		// Create a content node
		Map<QName, Serializable> props = new HashMap<QName, Serializable>(1);
		props.put(ContentModel.PROP_NAME, "myDoc.txt");
		ChildAssociationRef childAssoc = this.nodeService.createNode(
				this.folderNodeRef,
				ContentModel.ASSOC_CONTAINS,
				QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "myDoc.txt"),
				ContentModel.TYPE_CONTENT,
				props);
		assertFalse(this.nodeService.hasAspect(childAssoc.getChildRef(), ContentModel.ASPECT_TITLED));
		
		behaviour.enable();
		
		Map<QName, Serializable> props2 = new HashMap<QName, Serializable>(1);
		props2.put(ContentModel.PROP_NAME, "myDoc1.txt");
		ChildAssociationRef childAssoc2 = this.nodeService.createNode(
				this.folderNodeRef,
				ContentModel.ASSOC_CONTAINS,
				QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "myDoc1.txt"),
				ContentModel.TYPE_CONTENT,
				props2);
		assertTrue(this.nodeService.hasAspect(childAssoc2.getChildRef(), ContentModel.ASPECT_TITLED));		
	}
	
	public void testClasspathLocationBehaviour()
	{
		// Register the onCreateNode behaviour script
		ScriptLocation location = new ClasspathScriptLocation("org/alfresco/repo/jscript/test_onCreateNode_cmContent.js");
		ScriptBehaviour behaviour = new ScriptBehaviour(this.serviceRegistry, location);
		
		this.policyComponent.bindClassBehaviour(
				QName.createQName(NodeServicePolicies.OnCreateNodePolicy.NAMESPACE, "onCreateNode"),
				ContentModel.TYPE_CONTENT,
				behaviour);
		
		// Create a content node
		Map<QName, Serializable> props = new HashMap<QName, Serializable>(1);
		props.put(ContentModel.PROP_NAME, "myDoc.txt");
		ChildAssociationRef childAssoc = this.nodeService.createNode(
				this.folderNodeRef,
				ContentModel.ASSOC_CONTAINS,
				QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "myDoc.txt"),
				ContentModel.TYPE_CONTENT,
				props);
		
		// Since the behavoiour will have been run check that the titled aspect has been applied
		assertTrue(this.nodeService.hasAspect(childAssoc.getChildRef(), ContentModel.ASPECT_TITLED));
	}
	
	public void testSpringConfiguredBehaviour()
	{
		this.nodeService.addAspect(this.folderNodeRef, ContentModel.ASPECT_COUNTABLE, null);
		assertTrue(this.nodeService.hasAspect(this.folderNodeRef, ContentModel.ASPECT_TITLED));
		
		// Create a couple of nodes
		Map<QName, Serializable> props = new HashMap<QName, Serializable>(1);
		props.put(ContentModel.PROP_NAME, "myDoc.txt");
		ChildAssociationRef childAssoc = this.nodeService.createNode(
				this.folderNodeRef,
				ContentModel.ASSOC_CONTAINS,
				QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "myDoc.txt"),
				ContentModel.TYPE_CONTENT,
				props);
		Map<QName, Serializable> props2 = new HashMap<QName, Serializable>(1);
		props2.put(ContentModel.PROP_NAME, "folder2");
		ChildAssociationRef childAssoc2 = this.nodeService.createNode(
				this.folderNodeRef,
				ContentModel.ASSOC_CONTAINS,
				QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "folder2"),
				ContentModel.TYPE_FOLDER,
				props2);
		
		this.nodeService.addChild(childAssoc2.getChildRef(), childAssoc.getChildRef(), ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "linked"));
		assertTrue(this.nodeService.hasAspect(childAssoc.getChildRef(), ContentModel.ASPECT_VERSIONABLE));
	}
}
