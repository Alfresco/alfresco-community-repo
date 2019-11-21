package org.alfresco.cmis;

import java.lang.reflect.Method;

import org.alfresco.utility.LogFactory;
import org.alfresco.utility.data.DataContent;
import org.alfresco.utility.data.DataSite;
import org.alfresco.utility.data.DataUserAIS;
import org.alfresco.utility.network.ServerHealth;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;

@ContextConfiguration("classpath:alfresco-cmis-context.xml")
@Component
@Scope(value = "prototype")
public abstract class CmisTest extends AbstractTestNGSpringContextTests
{
    private static Logger LOG = LogFactory.getLogger();

    @Autowired
    protected CmisWrapper cmisApi;

    @Autowired
    protected DataUserAIS dataUser;

    @Autowired
    protected DataSite dataSite;

    @Autowired
    protected DataContent dataContent;

    @Autowired
    ServerHealth serverHealth;

    public String documentContent = "CMIS document content";

    @BeforeSuite(alwaysRun = true)
    public void checkServerHealth() throws Exception
    {        
        super.springTestContextPrepareTestInstance();
        serverHealth.assertServerIsOnline();
    }

    @BeforeMethod(alwaysRun = true)
    public void showStartTestInfo(Method method)
    {
        LOG.info(String.format("*** STARTING Test: [%s] ***", method.getName()));
    }

    @AfterMethod(alwaysRun = true)
    public void showEndTestInfo(Method method)
    {
        LOG.info(String.format("*** ENDING Test: [%s] ***", method.getName()));
    }
    
    public Integer getSolrWaitTimeInSeconds()
    {
        return cmisApi.cmisProperties.envProperty().getSolrWaitTimeInSeconds();
    }
}
