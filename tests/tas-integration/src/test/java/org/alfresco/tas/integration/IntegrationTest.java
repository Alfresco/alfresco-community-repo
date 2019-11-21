package org.alfresco.tas.integration;

import java.lang.reflect.Method;

import org.alfresco.cmis.CmisProperties;
import org.alfresco.cmis.CmisWrapper;
import org.alfresco.dataprep.WorkflowService;
import org.alfresco.email.EmailProperties;
import org.alfresco.email.ImapWrapper;
import org.alfresco.email.SmtpWrapper;
import org.alfresco.ftp.FTPWrapper;
import org.alfresco.rest.core.RestProperties;
import org.alfresco.rest.core.RestWrapper;
import org.alfresco.utility.LogFactory;
import org.alfresco.utility.data.DataContent;
import org.alfresco.utility.data.DataGroup;
import org.alfresco.utility.data.DataSite;
import org.alfresco.utility.data.DataUser;
import org.alfresco.utility.data.DataWorkflow;
import org.alfresco.utility.extension.ExtentionPointTestUtility;
import org.alfresco.utility.network.ServerHealth;
import org.alfresco.webdav.WebDavWrapper;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;

@ContextConfiguration("classpath:alfresco-integration-context.xml")
public abstract class IntegrationTest extends AbstractTestNGSpringContextTests
{
    private static Logger LOG = LogFactory.getLogger();
    
    @Autowired
    protected DataUser dataUser;

    @Autowired
    protected DataSite dataSite;
    
    @Autowired
    protected DataContent dataContent;
    
    @Autowired
    protected DataGroup dataGroup;

    @Autowired
    protected DataWorkflow dataWorkflow;

    @Autowired 
    protected WorkflowService workflow;

    @Autowired
    protected FTPWrapper ftpProtocol;

    @Autowired
    protected WebDavWrapper webDavProtocol;

    @Autowired
    protected CmisWrapper cmisAPI;
    
    @Autowired
    protected CmisProperties cmisProperties;

    @Autowired
    protected ImapWrapper imapProtocol;

    @Autowired
    protected RestWrapper restAPI;

    @Autowired
    protected ServerHealth serverHealth;

    @Autowired
    protected RestProperties restProperties;

    @Autowired
    protected ExtentionPointTestUtility extentionPointTestUtility;

    @Autowired
    protected SmtpWrapper smtpProtocol;

    @Autowired
    EmailProperties emailProperties;

    @BeforeSuite(alwaysRun = true)
    public void checkServerHealth() throws Exception
    {
        super.springTestContextPrepareTestInstance();
        serverHealth.assertServerIsOnline();
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
