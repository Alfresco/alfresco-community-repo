package org.alfresco.rest.framework.tests.core;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.rest.framework.Api;
import org.alfresco.rest.framework.core.ResourceDictionaryBuilder;
import org.alfresco.rest.framework.core.ResourceLookupDictionary;
import org.alfresco.rest.framework.jacksonextensions.JacksonHelper;
import org.alfresco.rest.framework.resource.EntityResource;
import org.alfresco.rest.framework.resource.RelationshipResource;
import org.alfresco.rest.framework.resource.parameters.Params;
import org.alfresco.rest.framework.webscripts.AbstractResourceWebScript;
import org.alfresco.rest.framework.webscripts.ResourceWebScriptHelper;
import org.alfresco.service.transaction.TransactionService;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Map;

/**
 * Uses a test context and sets up some data.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:test-rest-context.xml" })
public abstract class AbstractContextTest
{
    @Autowired
    ResourceLookupDictionary locator;

    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    ResourceWebScriptHelper helper;

    @Autowired
    JacksonHelper jsonHelper;

    static Params NOT_USED = Params.valueOf("notUsed", null, mock(WebScriptRequest.class));
    static final Params.RecognizedParams NULL_PARAMS = new Params.RecognizedParams(null, null, null, null, null, null, null, false);
    static Api api = Api.valueOf("alfrescomock", "private", "1");

    @SuppressWarnings("unchecked")
    @Before
    public void setUp() throws Exception
    {
        Map<String, Object> entityResourceBeans = applicationContext.getBeansWithAnnotation(EntityResource.class);
        Map<String, Object> relationResourceBeans = applicationContext.getBeansWithAnnotation(RelationshipResource.class);
        locator.setDictionary(ResourceDictionaryBuilder.build(entityResourceBeans.values(), relationResourceBeans.values()));
        AbstractResourceWebScript executor = (AbstractResourceWebScript) applicationContext.getBean("executorOfGets");
        AbstractResourceWebScript postExecutor = (AbstractResourceWebScript) applicationContext.getBean("executorOfPost");
        AbstractResourceWebScript putExecutor = (AbstractResourceWebScript) applicationContext.getBean("executorOfPut");
        AbstractResourceWebScript deleteExecutor = (AbstractResourceWebScript) applicationContext.getBean("executorOfDelete");

        //Mock transaction service
        TransactionService transerv = mock(TransactionService.class);
        RetryingTransactionHelper tHelper = mock(RetryingTransactionHelper.class);
        when(transerv.getRetryingTransactionHelper()).thenReturn(tHelper);
        when(tHelper.doInTransaction(any(RetryingTransactionHelper.RetryingTransactionCallback.class), anyBoolean(), anyBoolean())).thenAnswer(new Answer<Object>() {
            @SuppressWarnings("rawtypes")
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                RetryingTransactionHelper.RetryingTransactionCallback cb = (RetryingTransactionHelper.RetryingTransactionCallback) args[0];
                return cb.execute();
            }
        });
        executor.setTransactionService(transerv);
        postExecutor.setTransactionService(transerv);
        putExecutor.setTransactionService(transerv);
        deleteExecutor.setTransactionService(transerv);
    }
}
