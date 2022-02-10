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
import java.util.Random;

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
 * System test for RM-3993: Exceptions thrown when concurrently creating identical folder structure
 *
 * @author Silviu Dinuta
 * @since 2.3.0.7
 */
public class RM3993Test extends BaseRMTestCase
{
    private static final int NUMBER_OF_BATCHES = 4;
    private static final int NUMBER_IN_BATCH = 500;

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

    /**
     * Given that I have auto declare configured And that I have auto file configured to a path where only the record
     * folder needs to be created When I add lots of documents in the same transaction Then the rules should fire And
     * the documents should be filed in the new record folder
     */
    public void testAutoDeclareAutoFileCreateRecordFolderOnly() throws Exception
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

                Action action = actionService.createAction(CreateRecordAction.NAME);
                action.setParameterValue(CreateRecordAction.PARAM_FILE_PLAN, filePlan);

                Rule rule = new Rule();
                rule.setRuleType(RuleType.INBOUND);
                rule.setTitle("my rule");
                rule.setAction(action);
                rule.setExecuteAsynchronously(true);
                ruleService.saveRule(ruleFolder, rule);

                Action fileAction = actionService.createAction(FileToAction.NAME);
                fileAction.setParameterValue(FileToAction.PARAM_PATH,
                            "/category1/{node.cm:description}");
                fileAction.setParameterValue(FileToAction.PARAM_CREATE_RECORD_PATH, true);

                Rule fileRule = new Rule();
                fileRule.setRuleType(RuleType.INBOUND);
                fileRule.setTitle("my rule");
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

        List<NodeRef> records = new ArrayList<>(NUMBER_OF_BATCHES * NUMBER_IN_BATCH);

        for (int i = 0; i < NUMBER_OF_BATCHES; i++)
        {
            final int finali = i;
            records.addAll(doTestInTransaction(new Test<List<NodeRef>>()
            {
                @Override
                public List<NodeRef> run() throws Exception
                {
                    List<NodeRef> records = new ArrayList<>(NUMBER_IN_BATCH);
                    for (int j = 0; j < NUMBER_IN_BATCH; j++)
                    {
                        int count = (finali)* NUMBER_IN_BATCH + (j + 1);
                        String name = "content" + count + ".txt";
                        System.out.println(name + " - creating");

                        Random rand = new Random();
                        int descInt = rand.nextInt(2)+1;
                        NodeRef record = createFile(ruleFolder, name, Integer.toString(descInt), ContentModel.TYPE_CONTENT);
                        records.add(record);
                    }
                    return records;
                }
            }));
        }

        try
        {
            while (!records.isEmpty())
            {
                Thread.sleep(1000);

                final Iterator<NodeRef> temp = records.iterator();
                doTestInTransaction(new Test<Void>()
                {
                    @Override
                    public Void run() throws Exception
                    {
                        while (temp.hasNext())
                        {
                            NodeRef record = temp.next();
                            if (nodeService.hasAspect(record, ASPECT_RECORD) && recordService.isFiled(record))
                            {
                                String name = (String) nodeService.getProperty(record, ContentModel.PROP_NAME);
                                System.out.println(name + " - complete");
                                temp.remove();
                            }
                        }

                        return null;
                    }
                });
            }
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
            throw exception;
        }

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
        assertTrue(numberOfRecords == NUMBER_OF_BATCHES * NUMBER_IN_BATCH);
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
