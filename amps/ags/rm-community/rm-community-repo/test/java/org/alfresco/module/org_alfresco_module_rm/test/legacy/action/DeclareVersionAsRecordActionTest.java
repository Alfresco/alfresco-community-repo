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
package org.alfresco.module.org_alfresco_module_rm.test.legacy.action;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.action.dm.CreateRecordAction;
import org.alfresco.module.org_alfresco_module_rm.action.dm.DeclareAsVersionRecordAction;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase;
import org.alfresco.module.org_alfresco_module_rm.version.RecordableVersionModel;
import org.alfresco.module.org_alfresco_module_rm.version.RecordableVersionServiceImpl;
import org.alfresco.repo.version.VersionModel;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.rule.Rule;
import org.alfresco.service.cmr.rule.RuleService;
import org.alfresco.service.cmr.rule.RuleType;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionType;

public class DeclareVersionAsRecordActionTest extends BaseRMTestCase
{
    private RuleService ruleService;
    private NodeRef ruleFile;
    protected static final String DESCRIPTION = "description";


    @Override
    protected void initServices()
    {
        super.initServices();
        ruleService = (RuleService)applicationContext.getBean("RuleService");
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


    /**
     * Given a node set to auto-declare documents as records for minor and major versions
     * When I try to upload a minor or major version
     * Then the version record aspect is added
     */
    public void testUpdateNextDispositionAction_RM3060() throws Exception
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest(dmContributor)
        {
            Map<String, Serializable> versionProperties = new HashMap<>(4);
            Version recordedVersion;

            @Override
            public void given()
            {
                // create the file
                ruleFile = fileFolderService.create(documentLibrary, "mytestfile", ContentModel.TYPE_CONTENT).getNodeRef();

                Action action = actionService.createAction(DeclareAsVersionRecordAction.NAME);
                action.setParameterValue(CreateRecordAction.PARAM_FILE_PLAN, filePlan);

                Rule rule = new Rule();
                rule.setRuleType(RuleType.INBOUND);
                rule.setTitle("my rule");
                rule.setAction(action);
                rule.setExecuteAsynchronously(true);
                ruleService.saveRule(ruleFile, rule);

                // setup version properties
                versionProperties.put(Version.PROP_DESCRIPTION, DESCRIPTION);
                versionProperties.put(VersionModel.PROP_VERSION_TYPE, VersionType.MINOR);
                versionProperties.put(RecordableVersionServiceImpl.KEY_RECORDABLE_VERSION, true);
                versionProperties.put(RecordableVersionServiceImpl.KEY_FILE_PLAN, filePlan);
            }

            @Override
            public void when()
            {
                recordedVersion = versionService.createVersion(ruleFile, versionProperties);
            }

            @Override
            public void then() throws Exception
            {
                NodeRef recordedVersionNodeRef= (NodeRef)recordedVersion.getVersionProperties().get(RecordableVersionModel.PROP_RECORD_NODE_REF.getLocalName());
                assertNotNull("Recorded version shouldn't be null.", recordedVersionNodeRef);
                assertTrue(nodeService.hasAspect(recordedVersionNodeRef, RecordableVersionModel.ASPECT_VERSION_RECORD));
            }
        });
    }

}
