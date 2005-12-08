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
 * Is sub class evaluator test
 * 
 * @author Roy Wetherall
 */
public class HasAspectEvaluatorTest extends BaseSpringTest
{
    private NodeService nodeService;
    private StoreRef testStoreRef;
    private NodeRef rootNodeRef;
    private NodeRef nodeRef;
    private HasAspectEvaluator evaluator;
    
    private final static String ID = GUID.generate();

    @Override
    protected void onSetUpInTransaction() throws Exception
    {
        this.nodeService = (NodeService)this.applicationContext.getBean("nodeService");
        
        // Create the store and get the root node
        this.testStoreRef = this.nodeService.createStore(
                StoreRef.PROTOCOL_WORKSPACE, "Test_"
                        + System.currentTimeMillis());
        this.rootNodeRef = this.nodeService.getRootNode(this.testStoreRef);

        // Create the node used for tests
        this.nodeRef = this.nodeService.createNode(
                this.rootNodeRef,
                ContentModel.ASSOC_CHILDREN,
                QName.createQName("{test}testnode"),
                ContentModel.TYPE_CONTENT).getChildRef();
        
        this.evaluator = (HasAspectEvaluator)this.applicationContext.getBean(HasAspectEvaluator.NAME);
    }
    
    public void testMandatoryParamsMissing()
    {
        ActionCondition condition = new ActionConditionImpl(ID, HasAspectEvaluator.NAME, null);
        
        try
        {
            this.evaluator.evaluate(condition, this.nodeRef);
            fail("The fact that a mandatory parameter has not been set should have been detected.");
        }
        catch (Throwable exception)
        {
            // Do nothing since this is correct
        }
    }
    
    public void testPass()
    {
        this.nodeService.addAspect(this.nodeRef, ContentModel.ASPECT_VERSIONABLE, null);
        ActionCondition condition = new ActionConditionImpl(ID, HasAspectEvaluator.NAME, null);
        condition.setParameterValue(HasAspectEvaluator.PARAM_ASPECT, ContentModel.ASPECT_VERSIONABLE);
        assertTrue(this.evaluator.evaluate(condition, this.nodeRef));
    }
    
    public void testFail()
    {
        ActionCondition condition = new ActionConditionImpl(ID, HasAspectEvaluator.NAME, null);
        condition.setParameterValue(HasAspectEvaluator.PARAM_ASPECT, ContentModel.ASPECT_VERSIONABLE);
        assertFalse(this.evaluator.evaluate(condition, this.nodeRef));
    }
}
