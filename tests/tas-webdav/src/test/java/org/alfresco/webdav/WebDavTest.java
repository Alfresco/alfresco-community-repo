package org.alfresco.webdav;

import org.alfresco.utility.data.DataContent;
import org.alfresco.utility.data.DataSite;
import org.alfresco.utility.data.DataUser;
import org.alfresco.utility.network.ServerHealth;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeSuite;

@ContextConfiguration("classpath:alfresco-webdav-context.xml")
public abstract class WebDavTest extends AbstractTestNGSpringContextTests
{
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
}
