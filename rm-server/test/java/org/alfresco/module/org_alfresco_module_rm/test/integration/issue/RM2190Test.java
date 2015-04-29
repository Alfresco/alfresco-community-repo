package org.alfresco.module.org_alfresco_module_rm.test.integration.issue;

import static java.util.Arrays.asList;
import static org.alfresco.service.cmr.rule.RuleType.INBOUND;
import static org.alfresco.util.GUID.generate;
import static org.springframework.util.StringUtils.tokenizeToStringArray;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.alfresco.module.org_alfresco_module_rm.action.dm.CreateRecordAction;
import org.alfresco.module.org_alfresco_module_rm.action.impl.FileToAction;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.rule.Rule;
import org.alfresco.service.cmr.rule.RuleService;

/**
 * System test for RM-2190: Concurrency exception when upload document to several folders with rules configured to file records
 *
 * @author Tuna Aksoy
 * @since 2.2.1.1
 */
public class RM2190Test extends BaseRMTestCase
{
    private static final int NUMBER_OF_BATCHES = 1;
    private static final int NUMBER_IN_BATCH = 10;

    private static final String PATH = "/111/222/333";

    private RuleService ruleService;

    private NodeRef rootFolder;
    private NodeRef folder1;
    private NodeRef folder2;

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

    public void testUploadDocumentsSimultaneouslyWithRules()
    {
        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run()
            {
                rootFolder = fileFolderService.create(documentLibrary, generate(), TYPE_FOLDER).getNodeRef();

                Action createAction = actionService.createAction(CreateRecordAction.NAME);
                createAction.setParameterValue(CreateRecordAction.PARAM_FILE_PLAN, filePlan);

                Rule declareRule = new Rule();
                declareRule.setRuleType(INBOUND);
                declareRule.setTitle(generate());
                declareRule.setAction(createAction);
                declareRule.setExecuteAsynchronously(true);
                declareRule.applyToChildren(true);
                ruleService.saveRule(rootFolder, declareRule);

                folder1 = fileFolderService.create(rootFolder, generate(), TYPE_FOLDER).getNodeRef();
                folder2 = fileFolderService.create(rootFolder, generate(), TYPE_FOLDER).getNodeRef();

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
                assertFalse(ruleService.getRules(rootFolder).isEmpty());
                assertFalse(ruleService.getRules(unfiledContainer).isEmpty());
            }
        });

        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run() throws FileNotFoundException, InterruptedException
            {
                Thread thread1 = new Thread()
                {
                    public void run() {
                        List<NodeRef> files = addFilesToFolder(folder1);
                        waitForFilesToBeDeclared(files);
                    }
                };

                Thread thread2 = new Thread()
                {
                    public void run() {
                        List<NodeRef> files = addFilesToFolder(folder2);
                        waitForFilesToBeDeclared(files);
                    }
                };

                thread1.start();
                thread2.start();

                thread1.join(300000);
                thread2.join(300000);

                return null;
            }

            @Override
            public void test(Void result) throws Exception
            {
                FileInfo category = fileFolderService.resolveNamePath(filePlan, asList(tokenizeToStringArray(PATH, "/", false, true)));
                assertEquals(NUMBER_IN_BATCH * 2, nodeService.getChildAssocs(category.getNodeRef()).size());
            }
        });
    }

    private List<NodeRef> addFilesToFolder(final NodeRef folder)
    {
        List<NodeRef> records = new ArrayList<NodeRef>(NUMBER_OF_BATCHES * NUMBER_IN_BATCH);

        for (int i = 0; i < NUMBER_OF_BATCHES; i++)
        {
            final int finali = i;
            records.addAll(doTestInTransaction(new Test<List<NodeRef>>()
            {
                @Override
                public List<NodeRef> run() throws Exception
                {
                    List<NodeRef> files = new ArrayList<NodeRef>(NUMBER_IN_BATCH);
                    for (int j = 0; j < NUMBER_IN_BATCH; j++)
                    {
                        int count = (finali+1)*(j+1);
                        String name = folder.getId() + " - content" + count + ".txt";
                        System.out.println(name + " - creating");

                        NodeRef file = fileFolderService.create(folder, name, TYPE_CONTENT).getNodeRef();
                        files.add(file);
                    }
                    return files;
                }
            }));
        }

        return records;
    }

    private void waitForFilesToBeDeclared(List<NodeRef> files)
    {
        while (!files.isEmpty())
        {
            final Iterator<NodeRef> temp = files.iterator();
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
                            String name = (String) nodeService.getProperty(record, PROP_NAME);
                            System.out.println(name + " - complete");
                            temp.remove();
                        }
                    }

                    return null;
                }
            });
        }
    }
}
