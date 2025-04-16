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

import org.junit.Before;
import org.junit.Test;
import org.springframework.transaction.annotation.Transactional;

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
@Transactional
public class HasAspectEvaluatorTest extends BaseSpringTest
{
    private NodeService nodeService;
    private StoreRef testStoreRef;
    private NodeRef rootNodeRef;
    private NodeRef nodeRef;
    private HasAspectEvaluator evaluator;

    private final static String ID = GUID.generate();

    @Before
    public void before() throws Exception
    {
        this.nodeService = (NodeService) this.applicationContext.getBean("nodeService");

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

        this.evaluator = (HasAspectEvaluator) this.applicationContext.getBean(HasAspectEvaluator.NAME);
    }

    @Test
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

    @Test
    public void testPass()
    {
        this.nodeService.addAspect(this.nodeRef, ContentModel.ASPECT_VERSIONABLE, null);
        ActionCondition condition = new ActionConditionImpl(ID, HasAspectEvaluator.NAME, null);
        condition.setParameterValue(HasAspectEvaluator.PARAM_ASPECT, ContentModel.ASPECT_VERSIONABLE);
        assertTrue(this.evaluator.evaluate(condition, this.nodeRef));
    }

    @Test
    public void testFail()
    {
        ActionCondition condition = new ActionConditionImpl(ID, HasAspectEvaluator.NAME, null);
        condition.setParameterValue(HasAspectEvaluator.PARAM_ASPECT, ContentModel.ASPECT_VERSIONABLE);
        assertFalse(this.evaluator.evaluate(condition, this.nodeRef));
    }
}
