package org.alfresco.webdav;

import java.lang.reflect.Method;

import org.alfresco.utility.LogFactory;
import org.alfresco.utility.data.DataContent;
import org.alfresco.utility.data.DataSite;
import org.alfresco.utility.data.DataUser;
import org.alfresco.utility.network.ServerHealth;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;

@ContextConfiguration("classpath:alfresco-webdav-context.xml")
public abstract class WebDavTest extends AbstractTestNGSpringContextTests
{
	private static final Logger LOG = LogFactory.getLogger();

    @Autowired
    protected DataSite dataSite;

    @Autowired
    protected DataUser dataUser;

    @Autowired
    protected DataContent dataContent;

    @Autowired
    ServerHealth serverHealth;

    @Autowired
    WebDavWrapper webDavProtocol;

    @BeforeSuite(alwaysRun=true)
    public void setup() throws Exception
    {
        super.springTestContextPrepareTestInstance();
        serverHealth.assertServerIsOnline();
        // Since alfresco 6.0 JMX connection is deprecated
        // The webdav protocol is enabled by default.
        //webDavProtocol.assertThat().protocolIsEnabled();
    }

    @BeforeMethod(alwaysRun=true)
    public void showStartTestInfo(Method method)
    {
        LOG.info(String.format("*** STARTING Test: [%s] ***", method.getName()));
    }

    @AfterMethod(alwaysRun=true)
    public void showEndTestInfo(Method method)
    {
        LOG.info(String.format("*** ENDING Test: [%s] ***", method.getName()));
    }
}
