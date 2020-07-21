package org.alfresco.email;

import java.lang.reflect.Method;

import org.alfresco.email.dsl.ServerConfiguration;
import org.alfresco.utility.LogFactory;
import org.alfresco.utility.data.*;
import org.alfresco.utility.model.*;
import org.alfresco.utility.network.ServerHealth;
import org.alfresco.utility.network.TenantConsole;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.*;

@ContextConfiguration("classpath:alfresco-email-context.xml")
public abstract class EmailTest extends AbstractTestNGSpringContextTests
{
    private static Logger LOG = LogFactory.getLogger();
    
    @Autowired
    ServerHealth serverHealth;

    @Autowired
    protected ImapWrapper imapProtocol;
    
    @Autowired
    protected SmtpWrapper smtpProtocol;

    @Autowired
    public DataUser dataUser;
    
    @Autowired
    public DataGroup dataGroup;

    @Autowired
    public DataSite dataSite;

    @Autowired
    public DataContent dataContent;

    @Autowired
    public DataLink dataLink;

    @Autowired
    public DataCalendarEvent dataCalendarEvent;

    @Autowired
    public DataWiki dataWiki;

    @Autowired
    public TenantConsole tenantConsole;

    protected UserModel adminUser;
    protected UserModel testUser;
    protected SiteModel adminSite;
    protected SiteModel testSite;
    protected FolderModel testFolder;
    protected FileModel testFile;
    protected ContentModel contentModel;

    @BeforeSuite(alwaysRun = true)
    public void checkServerHealth() throws Exception
    {
        super.springTestContextPrepareTestInstance();
        serverHealth.assertServerIsOnline();

        UserModel anonymousUser = new UserModel("anonymous", DataUser.PASSWORD);
        if (!dataUser.isUserInRepo(anonymousUser.getUsername()))
        {
            dataUser.createUser(anonymousUser);
            dataGroup.usingUser(anonymousUser).addUserToGroup(GroupModel.getEmailContributorsGroup());
        }

        String jmxUseJolokiaAgent = System.getProperty("jmx.useJolokiaAgent");
        if ("true".equals(jmxUseJolokiaAgent))
        {
            imapProtocol.assertThat().protocolIsEnabled();
            smtpProtocol.assertThat().protocolIsEnabled();
            ServerConfiguration.save(smtpProtocol.withJMX(), smtpProtocol.emailProperties);
        }
        else
        {
            LOG.warn("*** Jolokia is not used! To use jolokia, please add next system property when running the tests: jmx.useJolokiaAgent=true ***");
        }
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
