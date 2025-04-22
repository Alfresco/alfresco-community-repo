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
package org.alfresco.repo.rule;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.transaction.annotation.Transactional;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.rule.ruletrigger.RuleTrigger;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.test_category.BaseSpringTestsCategory;
import org.alfresco.util.BaseSpringTest;

/**
 * Parameter definition implementation unit test.
 * 
 * @author Roy Wetherall
 */
@Category(BaseSpringTestsCategory.class)
@Transactional
public class RuleTypeImplTest extends BaseSpringTest
{
    private static final String NAME = "name";

    private NodeService nodeService;
    private ContentService contentService;

    private StoreRef testStoreRef;
    private NodeRef rootNodeRef;

    @Before
    public void before() throws Exception
    {
        this.nodeService = (NodeService) this.applicationContext.getBean("nodeService");
        this.contentService = (ContentService) this.applicationContext.getBean("contentService");

        this.testStoreRef = this.nodeService.createStore(StoreRef.PROTOCOL_WORKSPACE, "Test_" + System.currentTimeMillis());
        this.rootNodeRef = this.nodeService.getRootNode(this.testStoreRef);
    }

    @Test
    public void testConstructor()
    {
        create();
    }

    private RuleTypeImpl create()
    {
        RuleTypeImpl temp = new RuleTypeImpl(null);
        temp.setBeanName(NAME);
        assertNotNull(temp);
        return temp;
    }

    @Test
    public void testGetName()
    {
        RuleTypeImpl temp = create();
        assertEquals(NAME, temp.getName());
    }

    // TODO Test the display label, ensuring that the label is retrieved from the resource

    // TODO Test setRuleTriggers

    // TODO Test triggerRuleType

    @Test
    public void testMockInboundRuleType()
    {
        List<RuleTrigger> triggers = new ArrayList<RuleTrigger>(2);
        triggers.add((RuleTrigger) this.applicationContext.getBean("on-content-create-trigger"));
        triggers.add((RuleTrigger) this.applicationContext.getBean("on-create-node-trigger"));
        triggers.add((RuleTrigger) this.applicationContext.getBean("on-create-child-association-trigger"));

        ExtendedRuleType ruleType = new ExtendedRuleType(triggers);
        assertFalse(ruleType.rulesTriggered);

        NodeRef nodeRef = this.nodeService.createNode(
                this.rootNodeRef,
                ContentModel.ASSOC_CHILDREN,
                ContentModel.ASSOC_CHILDREN,
                ContentModel.TYPE_CONTENT).getChildRef();

        // Update some content in order to trigger the rule type
        ContentWriter contentWriter = this.contentService.getWriter(nodeRef, ContentModel.PROP_CONTENT, true);
        contentWriter.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
        contentWriter.putContent("any old content");
        assertTrue(ruleType.rulesTriggered);

        NodeRef nodeRef2 = this.nodeService.createNode(
                this.rootNodeRef,
                ContentModel.ASSOC_CHILDREN,
                ContentModel.ASSOC_CHILDREN,
                ContentModel.TYPE_CONTAINER).getChildRef();

        // Reset
        ruleType.rulesTriggered = false;
        assertFalse(ruleType.rulesTriggered);

        // Create a child association in order to trigger the rule type
        this.nodeService.addChild(
                nodeRef2,
                nodeRef,
                ContentModel.ASSOC_CHILDREN,
                ContentModel.ASSOC_CHILDREN);
        assertTrue(ruleType.rulesTriggered);
    }

    private class ExtendedRuleType extends RuleTypeImpl
    {
        public boolean rulesTriggered = false;

        public ExtendedRuleType(List<RuleTrigger> ruleTriggers)
        {
            super(ruleTriggers);
        }

        @Override
        public void triggerRuleType(NodeRef nodeRef, NodeRef actionedUponNodeRef, boolean executeRuleImmediately)
        {
            this.rulesTriggered = true;
        }

    }
}
