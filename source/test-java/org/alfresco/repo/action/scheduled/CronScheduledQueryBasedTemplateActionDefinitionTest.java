/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
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
package org.alfresco.repo.action.scheduled;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.transaction.Status;
import javax.transaction.UserTransaction;

import junit.framework.TestCase;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.nodelocator.CompanyHomeNodeLocator;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.util.ApplicationContextHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.quartz.Scheduler;
import org.springframework.context.ApplicationContext;

/**
 * Test for {@link CronScheduledQueryBasedTemplateActionDefinition} class. The test assumes that not all test-cases require scheduling. Hence,
 * {@link CronScheduledQueryBasedTemplateActionDefinition} instance is not registered as scheduled job. But in the same time this instance is unregistered as scheduled job after
 * every test-case execution. Also default query template is not set for {@link CronScheduledQueryBasedTemplateActionDefinition} instance
 * 
 * @author Dmitry Velichkevich
 * @see CronScheduledQueryBasedTemplateActionDefinitionTest#initializeScheduler(ServiceRegistry)
 */
public class CronScheduledQueryBasedTemplateActionDefinitionTest extends TestCase
{
    private static final int AMOUNT_OF_DAYS_BEFORE = -2;

    private static final int TEST_DOCUMENTS_AMOUNT = 5;


    /**
     * {@link SimpleTemplateActionDefinition#setActionName(String)}
     */
    private static final String SCRIPT_TEST_ACTION_NAME = "scriptTestActionName-" + System.currentTimeMillis();

    /**
     * {@link CronScheduledQueryBasedTemplateActionDefinition#setQueryLanguage(String)}
     */
    private static final String SCHEDULER_QUERY_LANGUAGE = "lucene";

    /**
     * {@link CronScheduledQueryBasedTemplateActionDefinition#setCronExpression(String)}
     */
    private static final String SCHEDULER_CRON_EXPRESSION = "0 50 * * * ?";

    /**
     * {@link CronScheduledQueryBasedTemplateActionDefinition#setCompensatingActionMode(String)}
     */
    private static final String SCHEDULER_COMPENSATING_ACTION_MODE = "IGNORE";

    /**
     * {@link CronScheduledQueryBasedTemplateActionDefinition#setTransactionMode(String)}
     */
    private static final String SCHEDULER_TRANSACTION_MODE = "UNTIL_FIRST_FAILURE";

    /**
     * {@link CronScheduledQueryBasedTemplateActionDefinition#setJobName(String)}
     */
    private static final String SCHEDULER_JOB_NAME = "jobTestName-" + System.currentTimeMillis();

    /**
     * {@link CronScheduledQueryBasedTemplateActionDefinition#setJobGroup(String)}
     */
    private static final String SCHEDULER_JOB_GROUP = "jobTestGroup-" + System.currentTimeMillis();

    /**
     * {@link CronScheduledQueryBasedTemplateActionDefinition#setTriggerName(String)}
     */
    private static final String SCHEDULER_TRIGGER_NAME = "triggerTestName-" + System.currentTimeMillis();

    /**
     * {@link CronScheduledQueryBasedTemplateActionDefinition#setTriggerGroup(String)}
     */
    private static final String SCHEDULER_TRIGGER_GROUP = "triggerTestGroup-" + System.currentTimeMillis();


    private static final String SCHEDULER_FACTORY_BEAN_NAME = "schedulerFactory";

    private static final String POLICY_BEHAVIOUR_FILTER_BEAN_NAME = "policyBehaviourFilter";

    private static final String ROOT_TEST_FOLDER_NAME_TEMPLATE = "RootTestFolder-%d";

    private static final String TEST_DOCUMENT_NAME_TEMPLATE = "TestDocument-%d-%d.txt";

    private static final String FRESH_TEST_DOCUMENT_NAME_TEMPLATE = "Fresh" + TEST_DOCUMENT_NAME_TEMPLATE;

    private static final String YESTERDAY_TEST_DOCUMENT_NAME_TEMPLATE = "Yesterday" + TEST_DOCUMENT_NAME_TEMPLATE;

    private static final String MNT_11598_QUERY_TEMPLATE = "@cm\\:created:\\$\\{luceneDateRange(yesterday, \"-P10Y\")\\}";
    
    private static final String TEST_FOLDER_NAME = String.format(ROOT_TEST_FOLDER_NAME_TEMPLATE, System.currentTimeMillis());

    private ApplicationContext applicationContext = ApplicationContextHelper.getApplicationContext();
    private ServiceRegistry registry;

    private UserTransaction transaction;


    private NodeRef rootTestFolder;

    private List<FileInfo> freshNodes = new LinkedList<FileInfo>();

    private List<FileInfo> yesterdayNodes = new LinkedList<FileInfo>();


    private CronScheduledQueryBasedTemplateActionDefinition scheduler;

    private StoreRef storeRef = new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore");

    @Before
    @Override
    public void setUp() throws Exception
    {
        this.registry = (ServiceRegistry) applicationContext.getBean(ServiceRegistry.SERVICE_REGISTRY);

        initializeScheduler();

        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();

        transaction = registry.getTransactionService().getUserTransaction(false);
        transaction.begin();

        createTestContent();
    }

    /**
     * Initializes target {@link CronScheduledQueryBasedTemplateActionDefinition} instance for testing. <b>Default query template is not set!</b> It must be set in test method. No
     * need to register for every test for scheduling. Hence, {@link CronScheduledQueryBasedTemplateActionDefinition#afterPropertiesSet()} is omitted here and must be invoked for
     * test which requires scheduling
     */
    private void initializeScheduler()
    {
        Scheduler factory = (Scheduler) applicationContext.getBean(SCHEDULER_FACTORY_BEAN_NAME);

        FreeMarkerWithLuceneExtensionsModelFactory templateActionModelFactory = new FreeMarkerWithLuceneExtensionsModelFactory();
        templateActionModelFactory.setServiceRegistry(registry);

        SimpleTemplateActionDefinition templateActionDefinition = new SimpleTemplateActionDefinition();
        templateActionDefinition.setApplicationContext(applicationContext);
        templateActionDefinition.setActionService(registry.getActionService());
        templateActionDefinition.setTemplateService(registry.getTemplateService());
        templateActionDefinition.setDictionaryService(registry.getDictionaryService());
        templateActionDefinition.setTemplateActionModelFactory(templateActionModelFactory);

        templateActionDefinition.setActionName(SCRIPT_TEST_ACTION_NAME);

        scheduler = new CronScheduledQueryBasedTemplateActionDefinition();
        scheduler.setScheduler(factory);
        scheduler.setTransactionService(registry.getTransactionService());
        scheduler.setActionService(registry.getActionService());
        scheduler.setSearchService(registry.getSearchService());
        scheduler.setTemplateService(registry.getTemplateService());
        scheduler.setRunAsUser(AuthenticationUtil.getSystemUserName());
        scheduler.setTemplateActionDefinition(templateActionDefinition);
        scheduler.setTemplateActionModelFactory(templateActionModelFactory);
        scheduler.setStores(Collections.singletonList(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE.toString()));

        scheduler.setQueryLanguage(SCHEDULER_QUERY_LANGUAGE);
        scheduler.setCronExpression(SCHEDULER_CRON_EXPRESSION);
        scheduler.setCompensatingActionMode(SCHEDULER_COMPENSATING_ACTION_MODE);
        scheduler.setTransactionMode(SCHEDULER_TRANSACTION_MODE);
        scheduler.setJobName(SCHEDULER_JOB_NAME);
        scheduler.setJobGroup(SCHEDULER_JOB_GROUP);
        scheduler.setTriggerName(SCHEDULER_TRIGGER_NAME);
        scheduler.setTriggerGroup(SCHEDULER_TRIGGER_GROUP);
    }

    /**
     * Creates <code>rootTestFolder</code> test folder as start of test content hierarchy. Then it creates
     * {@link CronScheduledQueryBasedTemplateActionDefinitionTest#TEST_DOCUMENTS_AMOUNT} documents in the root folder with "today" creation date and the same amount of documents
     * with "yesterday" creation date
     */
    private void createTestContent() throws Exception
    {
        FileFolderService fileFolderService = registry.getFileFolderService();

        NodeRef companyHomeNodeRef = registry.getNodeLocatorService().getNode(CompanyHomeNodeLocator.NAME, null, null);
        rootTestFolder = fileFolderService.create(companyHomeNodeRef, TEST_FOLDER_NAME, ContentModel.TYPE_FOLDER).getNodeRef();

        for (int i = 0; i < TEST_DOCUMENTS_AMOUNT; i++)
        {
            String freshDocName = String.format(FRESH_TEST_DOCUMENT_NAME_TEMPLATE, i, System.currentTimeMillis());
            FileInfo fileInfo = fileFolderService.create(rootTestFolder, freshDocName, ContentModel.TYPE_CONTENT);
            freshNodes.add(fileInfo);
        }

        Calendar calendar = GregorianCalendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.add(Calendar.DAY_OF_YEAR, AMOUNT_OF_DAYS_BEFORE);
        Date yesterdayTime = calendar.getTime();

        NodeService nodeService = registry.getNodeService();
        BehaviourFilter policyBehaviourFilter = (BehaviourFilter) applicationContext.getBean(POLICY_BEHAVIOUR_FILTER_BEAN_NAME);
        policyBehaviourFilter.disableBehaviour(ContentModel.ASPECT_AUDITABLE);
        try
        {
            for (int i = 0; i < TEST_DOCUMENTS_AMOUNT; i++)
            {
                String yesterdayDocName = String.format(YESTERDAY_TEST_DOCUMENT_NAME_TEMPLATE, i, System.currentTimeMillis());
                FileInfo fileInfo = fileFolderService.create(rootTestFolder, yesterdayDocName, ContentModel.TYPE_CONTENT);
                nodeService.setProperty(fileInfo.getNodeRef(), ContentModel.PROP_CREATED, yesterdayTime);
                yesterdayNodes.add(fileInfo);
            }
        }
        finally
        {
            policyBehaviourFilter.enableBehaviour(ContentModel.ASPECT_AUDITABLE);
        }
        checkNodes(freshNodes);
        checkNodes(yesterdayNodes);
    }

    /**
     * Check the nodes to be indexed
     * 
     * @param nodes
     * @throws Exception
     */
    private void checkNodes(List<FileInfo> nodes) throws Exception
    {
        SearchService searchService = registry.getSearchService();
        
        boolean notFound = false;
        for (int i = 1; i <= 40; i++)
        {
            notFound = false;
            for (FileInfo fileInfo : nodes)
            {
                ResultSet resultSet = searchService.query(storeRef, SearchService.LANGUAGE_LUCENE, "PATH:\"/app:company_home//cm:" + TEST_FOLDER_NAME + "//cm:" + fileInfo.getName() + "\"");
                if (resultSet.length() == 0)
                {
                    notFound = true;
                    break;
                }
            }
            if (notFound)
            {
                Thread.sleep(500);
            }
        }
        assertFalse("The content was not created or indexed correctly.", notFound);
    }
    
    @After
    @Override
    public void tearDown() throws Exception
    {
        scheduler.getScheduler().unscheduleJob(scheduler.getTriggerName(), scheduler.getJobGroup());

        if (Status.STATUS_ROLLEDBACK != transaction.getStatus())
        {
            transaction.rollback();
        }

        freshNodes.clear();
        yesterdayNodes.clear();

        AuthenticationUtil.clearCurrentSecurityContext();
    }

    @Test
    public void testQueryTemplateFunctionsUnescapingMnt11598() throws Exception
    {
        scheduler.setQueryTemplate(MNT_11598_QUERY_TEMPLATE);

        Set<NodeRef> actualNodes = new HashSet<NodeRef>(scheduler.getNodes());
        assertNotNull("Result set must not be null!", actualNodes);
        assertFalse("Result set must not be empty!", actualNodes.isEmpty());
        assertTrue(("Result set must contain at least " + TEST_DOCUMENTS_AMOUNT + " nodes!"), actualNodes.size() >= yesterdayNodes.size());

        for (FileInfo fileInfo : freshNodes)
        {
            assertFalse("No one of the nodes created \"today\" is expected in result set!", actualNodes.contains(fileInfo.getNodeRef()));
        }

        for (FileInfo fileInfo : yesterdayNodes)
        {
            assertTrue("One of the nodes created \"yesteday\" is missing in result set!", actualNodes.contains(fileInfo.getNodeRef()));
        }
    }
}
