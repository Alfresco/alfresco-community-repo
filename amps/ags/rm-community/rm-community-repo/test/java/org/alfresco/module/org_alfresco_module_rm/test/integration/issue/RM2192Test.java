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

import static org.alfresco.module.org_alfresco_module_rm.role.FilePlanRoleService.ROLE_RECORDS_MANAGER;
import static org.alfresco.repo.site.SiteModel.SITE_MANAGER;
import static org.alfresco.repo.site.SiteServiceImpl.getSiteContainer;
import static org.alfresco.service.cmr.rule.RuleType.INBOUND;
import static org.alfresco.service.cmr.security.AccessStatus.ALLOWED;
import static org.alfresco.service.cmr.site.SiteService.DOCUMENT_LIBRARY;
import static org.alfresco.service.cmr.site.SiteVisibility.PUBLIC;
import static org.alfresco.util.GUID.generate;

import org.alfresco.module.org_alfresco_module_rm.action.dm.CreateRecordAction;
import org.alfresco.module.org_alfresco_module_rm.action.impl.FileToAction;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase;
import org.alfresco.repo.jscript.app.JSONConversionComponent;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.rule.Rule;
import org.alfresco.service.cmr.rule.RuleService;

/**
 * Integration test for RM-2192
 *
 * @author Tuna Aksoy
 * @since 2.2.1.1
 */
public class RM2192Test extends BaseRMTestCase
{
    private static final String PATH = "/111/222/333";

    private RuleService ruleService;
    private JSONConversionComponent converter;

    private NodeRef folder;
    private String user;
    private NodeRef documentLibrary2;

    @Override
    protected void initServices()
    {
        super.initServices();

        ruleService = (RuleService) applicationContext.getBean("RuleService");
        converter = (JSONConversionComponent) applicationContext.getBean("jsonConversionComponent");
    }

    @Override
    protected boolean isCollaborationSiteTest()
    {
        return true;
    }

    @Override
    protected boolean isRecordTest()
    {
        return true;
    }

    @Override
    protected boolean isUserTest()
    {
        return true;
    }

    @Override
    protected void setupCollaborationSiteTestDataImpl()
    {
        super.setupCollaborationSiteTestDataImpl();

        String collabSiteId2 = generate();
        siteService.createSite("site-dashboard", collabSiteId2, generate(), generate(), PUBLIC);
        documentLibrary2 = getSiteContainer(
                collabSiteId2,
                DOCUMENT_LIBRARY,
                true,
                siteService,
                transactionService,
                taggingService);

        assertNotNull("Collaboration site document library component was not successfully created.", documentLibrary2);

        user = generate();
        createPerson(user);

        siteService.setMembership(collabSiteId2, user, SITE_MANAGER);

        filePlanRoleService.assignRoleToAuthority(filePlan, ROLE_RECORDS_MANAGER, user);
    }

    public void testAccessToRecordAfterDeclaring()
    {
        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run()
            {
                folder = fileFolderService.create(documentLibrary2, generate(), TYPE_FOLDER).getNodeRef();

                Action createAction = actionService.createAction(CreateRecordAction.NAME);
                createAction.setParameterValue(CreateRecordAction.PARAM_FILE_PLAN, filePlan);

                Rule declareRule = new Rule();
                declareRule.setRuleType(INBOUND);
                declareRule.setTitle(generate());
                declareRule.setAction(createAction);
                declareRule.setExecuteAsynchronously(true);
                declareRule.applyToChildren(true);
                ruleService.saveRule(folder, declareRule);

                Action fileAction = actionService.createAction(FileToAction.NAME);
                fileAction.setParameterValue(FileToAction.PARAM_PATH, PATH);
                fileAction.setParameterValue(FileToAction.PARAM_CREATE_RECORD_PATH, true);

                Rule fileRule = new Rule();
                fileRule.setRuleType(INBOUND);
                fileRule.setTitle(generate());
                fileRule.setAction(fileAction);
                fileRule.setExecuteAsynchronously(true);
                ruleService.saveRule(unfiledContainer, fileRule);

                return null;
            }

            @Override
            public void test(Void result) throws Exception
            {
                assertFalse(ruleService.getRules(folder).isEmpty());
                assertFalse(ruleService.getRules(unfiledContainer).isEmpty());
            }
        });

        doTestInTransaction(new Test<Void>()
        {
            NodeRef document;

            @Override
            public Void run()
            {
                document = fileFolderService.create(folder, generate(), TYPE_CONTENT).getNodeRef();

                return null;
            }

            @Override
            public void test(Void result) throws InterruptedException
            {
                Thread.sleep(10000);

                assertEquals(permissionService.hasPermission(document, READ_RECORDS), ALLOWED);
                assertTrue(recordService.isFiled(document));
                assertNotNull(converter.toJSON(document, true));
            }
        }, user);
    }
}
