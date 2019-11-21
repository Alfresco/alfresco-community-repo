package org.alfresco.rest;

import java.lang.reflect.Method;

import org.alfresco.dataprep.WorkflowService;
import org.alfresco.rest.core.RestProperties;
import org.alfresco.rest.core.RestWrapper;
import org.alfresco.utility.LogFactory;
import org.alfresco.utility.TasProperties;
import org.alfresco.utility.data.DataContent;
import org.alfresco.utility.data.DataGroup;
import org.alfresco.utility.data.DataLink;
import org.alfresco.utility.data.DataSite;
import org.alfresco.utility.data.DataUserAIS;
import org.alfresco.utility.data.DataWorkflow;
import org.alfresco.utility.model.SiteModel;
import org.alfresco.utility.network.ServerHealth;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;

@ContextConfiguration("classpath:alfresco-restapi-context.xml")
public abstract class RestTest extends AbstractTestNGSpringContextTests
{
    private static Logger LOG = LogFactory.getLogger();
    
    @Autowired
    protected RestProperties restProperties;

    @Autowired
    protected TasProperties properties;

    @Autowired
    protected ServerHealth serverHealth;

    @Autowired
    protected RestWrapper restClient;
    
    @Autowired
    protected DataUserAIS dataUser;

    @Autowired
    protected DataSite dataSite;
    
    @Autowired
    protected DataContent dataContent;
    
    @Autowired
    protected DataGroup dataGroup;

    @Autowired
    protected DataWorkflow dataWorkflow;

    @Autowired
    protected DataLink dataLink;
    
    @Autowired
    protected WorkflowService workflow;

    protected SiteModel testSite;

    @BeforeSuite(alwaysRun = true)
    public void checkServerHealth() throws Exception
    {
        super.springTestContextPrepareTestInstance();
        serverHealth.assertServerIsOnline();
        testSite = dataSite.createPublicRandomSite();
    }
    
    @BeforeMethod(alwaysRun=true)
    public void showStartTestInfo(Method method)
    {      
      LOG.info(String.format("*** STARTING Test: [%s] ***",method.getName()));      
    }
    
    @AfterMethod(alwaysRun=true)
    public void showEndTestInfo(Method method)
    {      
      LOG.info(String.format("*** ENDING Test: [%s] ***", method.getName()));
    }
}
