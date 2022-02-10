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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.action.dm.CreateRecordAction;
import org.alfresco.module.org_alfresco_module_rm.action.impl.FileToAction;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.rule.Rule;
import org.alfresco.service.cmr.rule.RuleService;
import org.alfresco.service.cmr.rule.RuleType;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

/**
 * System test for RM-4163
 *
 * @author Silviu Dinuta
 * @since 2.4.1
 */
public class RM4163Test extends BaseRMTestCase
{
    private RuleService ruleService;
    private NodeRef ruleFolder;
    private NodeRef nodeRefCategory1;

    @Override
    protected void initServices()
    {
        super.initServices();

        ruleService = (RuleService) applicationContext.getBean("RuleService");
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

    public void testDeclareRecordsConcurently() throws Exception
    {
        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run()
            {
                // create the folder
                ruleFolder = fileFolderService.create(documentLibrary, "mytestfolder", ContentModel.TYPE_FOLDER)
                            .getNodeRef();

                // create record category
                nodeRefCategory1 = filePlanService.createRecordCategory(filePlan, "category1");

                //define declare as record rule and apply it to the created folder from documentLibrary
                Action action = actionService.createAction(CreateRecordAction.NAME);
                action.setParameterValue(CreateRecordAction.PARAM_FILE_PLAN, filePlan);

                Rule rule = new Rule();
                rule.setRuleType(RuleType.INBOUND);
                rule.setTitle("declareAsRecordRule");
                rule.setAction(action);
                rule.setExecuteAsynchronously(true);
                ruleService.saveRule(ruleFolder, rule);

                //define filing rule and apply it to unfiled record container
                Action fileAction = actionService.createAction(FileToAction.NAME);
                fileAction.setParameterValue(FileToAction.PARAM_PATH,
                            "/category1/{node.cm:description}");
                fileAction.setParameterValue(FileToAction.PARAM_CREATE_RECORD_PATH, true);

                Rule fileRule = new Rule();
                fileRule.setRuleType(RuleType.INBOUND);
                fileRule.setTitle("filingRule");
                fileRule.setAction(fileAction);
                fileRule.setExecuteAsynchronously(true);
                ruleService.saveRule(filePlanService.getUnfiledContainer(filePlan), fileRule);

                return null;
            }

            @Override
            public void test(Void result) throws Exception
            {
                assertFalse(ruleService.getRules(ruleFolder).isEmpty());
            }
        });

        //create 4 documents in documentLibrary
        List<NodeRef> documents = new ArrayList<>(4);
        documents.addAll(doTestInTransaction(new Test<List<NodeRef>>()
        {
            @Override
            public List<NodeRef> run() throws Exception
            {
                List<NodeRef> documents = new ArrayList<>(4);
                NodeRef document = createFile(documentLibrary, "document1.txt", "desc1", ContentModel.TYPE_CONTENT);
                documents.add(document);
                document = createFile(documentLibrary, "document2.txt", "desc2", ContentModel.TYPE_CONTENT);
                documents.add(document);
                document = createFile(documentLibrary, "document3.txt", "desc1", ContentModel.TYPE_CONTENT);
                documents.add(document);
                document = createFile(documentLibrary, "document4.txt", "desc1", ContentModel.TYPE_CONTENT);
                documents.add(document);
                return documents;
            }
        }));

        //move created documents in the folder that has Declare as Record rule
        final Iterator<NodeRef> temp = documents.iterator();
        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run() throws Exception
            {
                while (temp.hasNext())
                {
                    NodeRef document = temp.next();
                    fileFolderService.move(document, ruleFolder, null);
                }
                return null;
            }
        });

        //give enough time for filing all records
        Thread.sleep(5000);

        //check that target category has in created record folders 4 records
        Integer numberOfRecords = AuthenticationUtil.runAsSystem(new RunAsWork<Integer>()
        {

            @Override
            public Integer doWork() throws Exception
            {
                List<NodeRef> containedRecordFolders = filePlanService.getContainedRecordFolders(nodeRefCategory1);
                int numberOfRecords = 0;
                for(NodeRef recordFolder : containedRecordFolders)
                {
                    numberOfRecords = numberOfRecords + fileFolderService.list(recordFolder).size();
                }
                return numberOfRecords;
            }
        });
        assertEquals(4, numberOfRecords.intValue());
    }

    private NodeRef createFile(NodeRef parentNodeRef, String name, String descrption, QName typeQName)
    {
        Map<QName, Serializable> properties = new HashMap<>(11);
        properties.put(ContentModel.PROP_NAME, (Serializable) name);
        properties.put(ContentModel.PROP_DESCRIPTION, (Serializable) descrption);
        QName assocQName = QName.createQName(
                    NamespaceService.CONTENT_MODEL_1_0_URI,
                    QName.createValidLocalName(name));
        ChildAssociationRef assocRef = nodeService.createNode(
                    parentNodeRef,
                    ContentModel.ASSOC_CONTAINS,
                    assocQName,
                    typeQName,
                    properties);
        NodeRef nodeRef = assocRef.getChildRef();
        return nodeRef;
    }
}
