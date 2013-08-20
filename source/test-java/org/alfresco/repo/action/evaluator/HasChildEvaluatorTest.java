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
package org.alfresco.repo.action.evaluator;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.ActionConditionImpl;
import org.alfresco.service.cmr.action.ActionCondition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.BaseSpringTest;
import org.alfresco.util.GUID;

/**
 * Test class for {@link HasChildEvaluator}.
 * 
 * @author Neil McErlean
 * @since 3.4
 */
public class HasChildEvaluatorTest extends BaseSpringTest
{
    private NodeService nodeService;
    private StoreRef testStoreRef;
    private NodeRef rootNodeRef;
    private NodeRef parentNodeRef;
    private NodeRef childNodeRef;
    private QName parentChildAssocName;
    private HasChildEvaluator evaluator;
    
    private final static String ID = GUID.generate();

    @SuppressWarnings("deprecation")
    @Override
    protected void onSetUpInTransaction() throws Exception
    {
        this.nodeService = (NodeService)this.applicationContext.getBean("nodeService");
        
        // Create the store and get the root node
        this.testStoreRef = this.nodeService.createStore(
                StoreRef.PROTOCOL_WORKSPACE, "Test_"
                        + System.currentTimeMillis());
        this.rootNodeRef = this.nodeService.getRootNode(this.testStoreRef);

        // Create the nodes used for tests
        this.parentNodeRef = this.nodeService.createNode(
                this.rootNodeRef,
                ContentModel.ASSOC_CHILDREN,
                QName.createQName("{test}testnode_p"),
                ContentModel.TYPE_CONTENT).getChildRef();
        
        parentChildAssocName = QName.createQName("{test}testnode_c");
        this.childNodeRef = this.nodeService.createNode(
                this.parentNodeRef,
                ContentModel.ASSOC_CHILDREN,
                parentChildAssocName,
                ContentModel.TYPE_CONTENT).getChildRef();
        
        this.evaluator = (HasChildEvaluator)this.applicationContext.getBean(HasChildEvaluator.NAME);
    }
    
    public void testPass()
    {
        ActionCondition condition = new ActionConditionImpl(ID, HasChildEvaluator.NAME, null);
        // no parameters means match all.
        assertTrue(this.evaluator.evaluate(condition, this.parentNodeRef));
        
        // should find child with specific assoc type
        condition.setParameterValue(HasChildEvaluator.PARAM_ASSOC_TYPE, ContentModel.ASSOC_CHILDREN);
        assertTrue(this.evaluator.evaluate(condition, this.parentNodeRef));
        
        // should find child with specific assoc type (and name)
        condition.setParameterValue(HasChildEvaluator.PARAM_ASSOC_NAME, this.parentChildAssocName);
        assertTrue(this.evaluator.evaluate(condition, this.parentNodeRef));

        // should find child with specific assoc name (no type specified)
        condition = new ActionConditionImpl(ID, HasChildEvaluator.NAME, null);
        condition.setParameterValue(HasChildEvaluator.PARAM_ASSOC_NAME, this.parentChildAssocName);
        assertTrue(this.evaluator.evaluate(condition, this.parentNodeRef));
    }
    
    public void testFail()
    {
        ActionCondition condition = new ActionConditionImpl(ID, HasChildEvaluator.NAME, null);
        
        // node has no children
        assertFalse(this.evaluator.evaluate(condition, this.childNodeRef));
        
        // node has child of unmatched assoc type
        condition.setParameterValue(HasChildEvaluator.PARAM_ASSOC_TYPE, ContentModel.ASSOC_ATTACHMENTS);
        assertFalse(this.evaluator.evaluate(condition, this.parentNodeRef));
        
        // node has child of unmatched assoc name
        condition = new ActionConditionImpl(ID, HasChildEvaluator.NAME, null);
        condition.setParameterValue(HasChildEvaluator.PARAM_ASSOC_NAME, QName.createQName("{foo}noSuchName"));
        assertFalse(this.evaluator.evaluate(condition, this.parentNodeRef));
    }
}
