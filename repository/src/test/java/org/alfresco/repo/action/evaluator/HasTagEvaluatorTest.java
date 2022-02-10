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
package org.alfresco.repo.action.evaluator;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.ActionConditionImpl;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.action.ActionCondition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.tagging.TaggingService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.BaseSpringTest;
import org.alfresco.util.GUID;
import org.alfresco.util.testing.category.NeverRunsTests;
import org.junit.Before;
import org.junit.experimental.categories.Category;
import org.springframework.transaction.annotation.Transactional;

/**
 * Has tag evaluator unit test
 * 
 * @author Roy Wetherall
 */
@Transactional
@Category(NeverRunsTests.class)
public class HasTagEvaluatorTest extends BaseSpringTest
{
    private NodeService nodeService;
    private TaggingService taggingService;
    private StoreRef testStoreRef;
    private NodeRef rootNodeRef;
    private NodeRef nodeRef;
    private HasTagEvaluator evaluator;
    
    private final static String ID = GUID.generate();

    @Before
    public void before() throws Exception
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
