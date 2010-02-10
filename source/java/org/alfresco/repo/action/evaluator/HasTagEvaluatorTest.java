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
package org.alfresco.repo.action.evaluator;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.ActionConditionImpl;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.tagging.TaggingServiceImplTest;
import org.alfresco.service.cmr.action.ActionCondition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.tagging.TaggingService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.BaseSpringTest;
import org.alfresco.util.GUID;

/**
 * Has tag evaluator unit test
 * 
 * @author Roy Wetherall
 */
public class HasTagEvaluatorTest extends BaseSpringTest
{
    private NodeService nodeService;
    private TaggingService taggingService;
    private StoreRef testStoreRef;
    private NodeRef rootNodeRef;
    private NodeRef nodeRef;
    private HasTagEvaluator evaluator;
    
    private final static String ID = GUID.generate();

    @SuppressWarnings("deprecation")
    @Override
    protected void onSetUpInTransaction() throws Exception
    {
        this.nodeService = (NodeService)applicationContext.getBean("nodeService");
        this.taggingService = (TaggingService)applicationContext.getBean("taggingService");
        
        // Create the store and get the root node
        this.testStoreRef = new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore");
        this.rootNodeRef = this.nodeService.getRootNode(this.testStoreRef); 

        // Create the node used for tests
        this.nodeRef = this.nodeService.createNode(
                this.rootNodeRef,
                ContentModel.ASSOC_CHILDREN,
                QName.createQName("{test}testnode"),
                ContentModel.TYPE_CONTENT).getChildRef();
        
        this.evaluator = (HasTagEvaluator)applicationContext.getBean(HasTagEvaluator.NAME);        
        AuthenticationUtil.setFullyAuthenticatedUser("admin");
    }
    
    public void testPass()
    {
        taggingService.addTag(nodeRef, "testTag");        
        ActionCondition condition = new ActionConditionImpl(ID, HasTagEvaluator.NAME, null);
        condition.setParameterValue(HasTagEvaluator.PARAM_TAG, "testTag");
        boolean value = this.evaluator.evaluate(condition, this.nodeRef);
        assertTrue("Tag should have been set", value);
    }
    
    public void testFail()
    {
        ActionCondition condition = new ActionConditionImpl(ID, HasTagEvaluator.NAME, null);
        condition.setParameterValue(HasTagEvaluator.PARAM_TAG, "testTag");
        boolean value = this.evaluator.evaluate(condition, this.nodeRef);
        assertFalse(value);
    }
}
