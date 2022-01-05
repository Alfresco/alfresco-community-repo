/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
package org.alfresco.module.org_alfresco_module_rm.test.integration.issue;

import java.util.UUID;

import org.alfresco.module.org_alfresco_module_rm.action.impl.LinkToAction;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.rule.Rule;
import org.alfresco.service.cmr.rule.RuleService;
import org.alfresco.service.cmr.rule.RuleType;

/**
 * Tests issue #4101: Link to, Copy to and File to rules fail when not run in background
 *
 * @author Tuna Aksoy
 * @since 2.3.0.8
 */
public class RM4101Test extends BaseRMTestCase
{
    private RuleService ruleService;

    @Override
    protected void initServices()
    {
        super.initServices();

        ruleService = (RuleService) applicationContext.getBean("RuleService");
    }

    @Override
    protected boolean isRecordTest()
    {
        return true;
    }

    public void testRunRuleNotInBackground() throws Exception
    {
        final String categoryName = "category1" + UUID.randomUUID().toString();
        final NodeRef category1 = doTestInTransaction(new Test<NodeRef>()
        {
            @Override
            public NodeRef run()
            {
                return filePlanService.createRecordCategory(filePlan, categoryName);
            }
        });

        final NodeRef folder1 = doTestInTransaction(new Test<NodeRef>()
        {
            @Override
            public NodeRef run()
            {
                return recordFolderService.createRecordFolder(category1, "folder1WithRule" + UUID.randomUUID().toString());
            }
        });

        final String folder2Name = "folder2FolderToLinkTo" + UUID.randomUUID().toString();
        final NodeRef folder2 = doTestInTransaction(new Test<NodeRef>()
        {
            @Override
            public NodeRef run()
            {
                return recordFolderService.createRecordFolder(category1, folder2Name);
            }
        });

        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run()
            {
                Action linkToAction = actionService.createAction(LinkToAction.NAME);
                linkToAction.setParameterValue(LinkToAction.PARAM_PATH, "/" + categoryName + "/" + folder2Name);

                Rule rule = new Rule();
                rule.setRuleType(RuleType.INBOUND);
                rule.setTitle("LinkTo");
                rule.setAction(linkToAction);
                rule.setExecuteAsynchronously(false);
                ruleService.saveRule(folder1, rule);

                return null;
            }
        });

        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run()
            {
                utils.createRecord(folder1, "record1" + UUID.randomUUID().toString());
                return null;
            }

            @Override
            public void test(Void result) throws Exception
            {
                assertEquals(1, nodeService.getChildAssocs(folder2).size());
            }
        });
    }
}
