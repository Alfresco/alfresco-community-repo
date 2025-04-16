/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */

package org.alfresco.repo.invitation.site;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.alfresco.repo.invitation.WorkflowModelNominatedInvitation.wfVarAcceptUrl;
import static org.alfresco.repo.invitation.WorkflowModelNominatedInvitation.wfVarInviteTicket;
import static org.alfresco.repo.invitation.WorkflowModelNominatedInvitation.wfVarInviteeGenPassword;
import static org.alfresco.repo.invitation.WorkflowModelNominatedInvitation.wfVarInviteeUserName;
import static org.alfresco.repo.invitation.WorkflowModelNominatedInvitation.wfVarInviterUserName;
import static org.alfresco.repo.invitation.WorkflowModelNominatedInvitation.wfVarRejectUrl;
import static org.alfresco.repo.invitation.WorkflowModelNominatedInvitation.wfVarResourceName;
import static org.alfresco.repo.invitation.WorkflowModelNominatedInvitation.wfVarRole;
import static org.alfresco.repo.invitation.WorkflowModelNominatedInvitation.wfVarServerPath;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatcher;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.extensions.surf.util.URLEncoder;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.executer.MailActionExecuter;
import org.alfresco.repo.admin.SysAdminParams;
import org.alfresco.repo.admin.SysAdminParamsImpl;
import org.alfresco.repo.i18n.MessageService;
import org.alfresco.repo.invitation.activiti.SendNominatedInviteAddDirectDelegate;
import org.alfresco.repo.invitation.activiti.SendNominatedInviteDelegate;
import org.alfresco.repo.model.Repository;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.admin.RepoAdminService;
import org.alfresco.service.cmr.admin.RepoUsage;
import org.alfresco.service.cmr.admin.RepoUsage.LicenseMode;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;

/**
 * @author Nick Smith
 */
public class InviteSenderTest extends TestCase
{
    private static final StoreRef testStore = new StoreRef(StoreRef.PROTOCOL_TEST, "test");

    private static final Person inviter = new Person("inviter");
    private static final Person invitee = new Person("invitee");
    private static final Person whitespaceInvitee = new Person("First Second\tthird\nFourth\r\nFifth");
    private static final Person specialCharInvitee = new Person("àâæçéèêëîïôœùûüÿñ");

    private static final NodeRef template = new NodeRef(testStore, "template");

    private static final String siteName = "Full Site Name";
    private static final String siteShortName = "Site Name";
    private static final String mailText = "Mail Text";

    private static final String acceptUrl = "/accpet";
    private static final String rejectUrl = "/reject";
    private static final String dashboardUrl = "/dashboard";
    private static final String leaveSiteUrl = "/dashboard#leavesite";
    private static final String role = "Role";
    private static final String password = "password";
    private static final String ticket = "Ticket";
    private static final String path = testStore + "/path";
    private static final String packageId = testStore + "/Package";
    private static final String instanceId = "InstanceId";

    private final MessageService messageService = mock(MessageService.class);
    private Action mailAction;
    private SiteInfo siteInfo = mock(SiteInfo.class);
    private InviteNominatedSender sender;
    private Map<String, Serializable> lastSetMailModel;

    protected void testSendMailWorkingPath(
            String emailTemplateXpath, String emailSubjectKey, boolean requireAcceptance) throws Exception
    {
        String rolePropertyName = "invitation.invitesender.email.role.Role";
        String subjectPropertyName = requireAcceptance ? "invitation.invitesender.email.subject" : "invitation.invitesender.emailAddDirect.subject";

        String subjectMsg = "Subject message";

        Map<String, String> properties = buildDefaultProperties();
        sender.sendMail(emailTemplateXpath, emailSubjectKey, properties);

        verify(messageService).getMessage(eq(rolePropertyName));

        verify(mailAction).setParameterValue(eq(MailActionExecuter.PARAM_FROM), eq(inviter.email));
        verify(mailAction).setParameterValue(eq(MailActionExecuter.PARAM_TO), eq(invitee.email));
        verify(mailAction).setParameterValue(eq(MailActionExecuter.PARAM_SUBJECT), eq(subjectPropertyName));
        verify(mailAction).setParameterValue(eq(MailActionExecuter.PARAM_SUBJECT_PARAMS), argThat(new ArgumentMatcher<Object[]>() {

            @Override
            public boolean matches(Object[] arg)
            {
                if ((arg instanceof Object[]) == false)
                    return false;
                Object[] params = (Object[]) arg;
                return params.length == 2 &&
                        "Share".equals(params[0]) &&
                        siteShortName.equals(params[1]);
            }

        }));
        verify(mailAction).setParameterValue(eq(MailActionExecuter.PARAM_TEMPLATE), (Serializable) any());

        ArgumentCaptor<Map> modelC = ArgumentCaptor.forClass(Map.class);
        verify(mailAction).setParameterValue(eq(MailActionExecuter.PARAM_TEMPLATE_MODEL), (Serializable) modelC.capture());

        // Check the model
        Map model = modelC.getValue();
        assertNotNull(model);
        assertEquals(false, model.isEmpty());
        assertEquals(null, model.get("userhome"));
        assertNotNull(model.get("productName"));

        // And the args within it
        Map<String, String> argsMap = (Map) model.get("args");
        assertNotNull(argsMap);
        assertEquals(siteShortName, argsMap.get("siteName"));
        assertEquals(invitee.node.toString(), argsMap.get("inviteePersonRef"));
        assertEquals(inviter.node.toString(), argsMap.get("inviterPersonRef"));
        assertEquals(siteShortName, argsMap.get("siteName"));
        assertEquals(invitee.name, argsMap.get("inviteeUserName"));

        if (requireAcceptance)
        {
            assertEquals(password, argsMap.get("inviteeGenPassword"));
            assertEquals(
                    "test://test/path/accpet?inviteId=InstanceId&inviteeUserName=invitee&siteShortName=Full Site Name&inviteTicket=Ticket",
                    argsMap.get("acceptLink"));
            assertEquals(
                    "test://test/path/reject?inviteId=InstanceId&inviteeUserName=invitee&siteShortName=Full Site Name&inviteTicket=Ticket",
                    argsMap.get("rejectLink"));
        }
        else
        {
            assertEquals(
                    "test://test/path/page/site/Full Site Name" + dashboardUrl,
                    argsMap.get("siteDashboardLink"));
            assertEquals(
                    "test://test/path/page/site/Full Site Name" + leaveSiteUrl,
                    argsMap.get("siteLeaveLink"));
        }

        // When no role message is found then the role name is used.
        assertEquals(role, argsMap.get("inviteeSiteRole"));

        // Check that when the role message is set then that role message is used.
        reset(mailAction, messageService);
        String roleMsg = "role message";
        when(messageService.getMessage(rolePropertyName)).thenReturn(roleMsg);
        sender.sendMail(emailTemplateXpath, emailSubjectKey, properties);

        // Grab the args and check
        modelC = ArgumentCaptor.forClass(Map.class);
        verify(mailAction).setParameterValue(eq(MailActionExecuter.PARAM_TEMPLATE_MODEL), (Serializable) modelC.capture());
        model = modelC.getValue();
        assertNotNull(model);
        argsMap = (Map) model.get("args");
        assertNotNull(argsMap);

        assertEquals(roleMsg, argsMap.get("inviteeSiteRole"));
    }

    public void testSendMailWorkingPath() throws Exception
    {
        testSendMailWorkingPath(
                SendNominatedInviteAddDirectDelegate.EMAIL_TEMPLATE_XPATH,
                SendNominatedInviteAddDirectDelegate.EMAIL_SUBJECT_KEY,
                false);
    }

    public void testSendMailWorkingPathRequireAcceptance() throws Exception
    {
        testSendMailWorkingPath(
                SendNominatedInviteDelegate.EMAIL_TEMPLATE_XPATH,
                SendNominatedInviteDelegate.EMAIL_SUBJECT_KEY,
                true);
    }

    protected void testSendMailWithWhitespaceUserName(
            String emailTemplateXPath, String emailSubjectKey, boolean requireAcceptance) throws Exception
    {
        Map<String, String> properties = buildDefaultProperties();
        properties.put(wfVarInviteeUserName, whitespaceInvitee.name);
        sender.sendMail(emailTemplateXPath, emailSubjectKey, properties);

        ArgumentCaptor<Map> modelC = ArgumentCaptor.forClass(Map.class);
        verify(mailAction).setParameterValue(eq(MailActionExecuter.PARAM_TEMPLATE_MODEL), (Serializable) modelC.capture());

        // Check the model
        Map model = modelC.getValue();
        assertNotNull(model);
        assertEquals(false, model.isEmpty());
        assertEquals(null, model.get("userhome"));
        assertNotNull(model.get("productName"));

        if (requireAcceptance)
        {
            // And the args within it
            Map<String, String> argsMap = (Map) model.get("args");
            String acceptLink = argsMap.get("acceptLink");
            assertEquals(
                    "test://test/path/accpet?inviteId=InstanceId&inviteeUserName=First%20Second%09third%0aFourth%0d%0aFifth&siteShortName=Full Site Name&inviteTicket=Ticket",
                    acceptLink);
            String rejectLink = argsMap.get("rejectLink");
            assertEquals(
                    "test://test/path/reject?inviteId=InstanceId&inviteeUserName=First%20Second%09third%0aFourth%0d%0aFifth&siteShortName=Full Site Name&inviteTicket=Ticket",
                    rejectLink);
        }
    }

    public void testSendMailWithWhitespaceUserName() throws Exception
    {
        testSendMailWithWhitespaceUserName(
                SendNominatedInviteAddDirectDelegate.EMAIL_TEMPLATE_XPATH,
                SendNominatedInviteAddDirectDelegate.EMAIL_SUBJECT_KEY,
                false);
    }

    public void testSendMailWithWhitespaceUserNameRequireAcceptance() throws Exception
    {
        testSendMailWithWhitespaceUserName(
                SendNominatedInviteDelegate.EMAIL_TEMPLATE_XPATH,
                SendNominatedInviteDelegate.EMAIL_SUBJECT_KEY,
                true);
    }

    protected void testSendMailWithSpecialCharUserName(
            String emailTemplateXpath, String emailSubjectKey, boolean requireAcceptance) throws Exception
    {
        Map<String, String> properties = buildDefaultProperties();
        properties.put(wfVarInviteeUserName, specialCharInvitee.name);
        sender.sendMail(emailTemplateXpath, emailSubjectKey, properties);

        ArgumentCaptor<Map> modelC = ArgumentCaptor.forClass(Map.class);
        verify(mailAction).setParameterValue(eq(MailActionExecuter.PARAM_TEMPLATE_MODEL), (Serializable) modelC.capture());

        // Check the model
        Map model = modelC.getValue();
        assertNotNull(model);
        assertEquals(false, model.isEmpty());
        assertEquals(null, model.get("userhome"));
        assertNotNull(model.get("productName"));

        if (requireAcceptance)
        {
            // And the args within it
            Map<String, String> argsMap = (Map) model.get("args");
            String acceptLink = argsMap.get("acceptLink");
            assertEquals(
                    "test://test/path/accpet?inviteId=InstanceId&inviteeUserName=%c3%a0%c3%a2%c3%a6%c3%a7%c3%a9%c3%a8%c3%aa%c3%ab%c3%ae%c3%af%c3%b4%c5%93%c3%b9%c3%bb%c3%bc%c3%bf%c3%b1&siteShortName=Full Site Name&inviteTicket=Ticket",
                    acceptLink);
            String rejectLink = argsMap.get("rejectLink");
            assertEquals(
                    "test://test/path/reject?inviteId=InstanceId&inviteeUserName=%c3%a0%c3%a2%c3%a6%c3%a7%c3%a9%c3%a8%c3%aa%c3%ab%c3%ae%c3%af%c3%b4%c5%93%c3%b9%c3%bb%c3%bc%c3%bf%c3%b1&siteShortName=Full Site Name&inviteTicket=Ticket",
                    rejectLink);
        }
    }

    public void testSendMailWithSpecialCharUserName() throws Exception
    {
        testSendMailWithSpecialCharUserName(
                SendNominatedInviteAddDirectDelegate.EMAIL_TEMPLATE_XPATH,
                SendNominatedInviteAddDirectDelegate.EMAIL_SUBJECT_KEY,
                false);
    }

    public void testSendMailWithSpecialCharUserNameRequireAcceptance() throws Exception
    {
        testSendMailWithSpecialCharUserName(
                SendNominatedInviteDelegate.EMAIL_TEMPLATE_XPATH,
                SendNominatedInviteDelegate.EMAIL_SUBJECT_KEY,
                true);
    }

    public void testValidServerPath() throws Exception
    {
        String validPath = "test://test/path/accept?hello";
        String link = sender.makeLink("test://test/path", "accept", "hello", null);
        assertEquals(validPath, link);
        link = sender.makeLink("test://test/path/", "accept", "hello", null);
        assertEquals(validPath, link);
        link = sender.makeLink("test://test/path", "/accept", "hello", null);
        assertEquals(validPath, link);
        link = sender.makeLink("test://test/path/", "/accept", "hello", null);
        assertEquals(validPath, link);
        link = sender.makeLink("test://test/path", "/accept", "?hello", null);
        assertEquals(validPath, link);
    }

    protected static String ensureEndsWithSlash(String thePath)
    {
        return thePath.endsWith("/") ? thePath : thePath + "/";
    }

    private Map<String, String> buildDefaultProperties()
    {
        Map<String, String> properties = new HashMap<String, String>();
        properties.put(wfVarResourceName, siteName);
        properties.put(wfVarInviteeUserName, invitee.name);
        properties.put(wfVarInviterUserName, inviter.name);
        properties.put(wfVarAcceptUrl, acceptUrl);
        properties.put(wfVarRejectUrl, rejectUrl);
        properties.put(wfVarRole, role);
        properties.put(wfVarInviteeGenPassword, password);
        properties.put(wfVarInviteTicket, ticket);
        properties.put(wfVarServerPath, path);
        properties.put(InviteNominatedSender.WF_PACKAGE, packageId);
        properties.put(InviteNominatedSender.WF_INSTANCE_ID, instanceId);
        return properties;
    }

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        ServiceRegistry services = mockServices();
        Repository repository = mockRepository();
        sender = new InviteNominatedSender(services, repository, messageService);
        lastSetMailModel = null;
    }

    /**
     * Mocks up a Repository that will return the inviter as the current user.
     * 
     * @return Repository
     */
    private Repository mockRepository()
    {
        Repository repository = mock(Repository.class);
        when(repository.getPerson()).thenReturn(inviter.node);
        return repository;
    }

    /**
     * @return ServiceRegistry
     */
    private ServiceRegistry mockServices()
    {
        ActionService mockActionService = mockActionService();
        NodeService mockNodeService = mockNodeService();
        PersonService mockPersonService = mockPersonService();
        SearchService mockSearchService = mockSearchService();
        SiteService mockSiteService = mockSiteService();
        FileFolderService mockFileFolderService = mockFileFolderService();
        RepoAdminService mockRepoAdminService = mockRepoAdminService();
        SysAdminParams sysAdminParams = new SysAdminParamsImpl();

        ServiceRegistry services = mock(ServiceRegistry.class);
        when(services.getActionService()).thenReturn(mockActionService);
        when(services.getNodeService()).thenReturn(mockNodeService);
        when(services.getPersonService()).thenReturn(mockPersonService);
        when(services.getSearchService()).thenReturn(mockSearchService);
        when(services.getSiteService()).thenReturn(mockSiteService);
        when(services.getFileFolderService()).thenReturn(mockFileFolderService);
        when(services.getSysAdminParams()).thenReturn(sysAdminParams);
        when(services.getRepoAdminService()).thenReturn(mockRepoAdminService);
        return services;
    }

    /**
     * Mocks up a FileFolderService that claims there are no localised templates available
     */
    private FileFolderService mockFileFolderService()
    {
        FileFolderService fileFolderService = mock(FileFolderService.class);
        when(fileFolderService.getLocalizedSibling((NodeRef) null)).thenAnswer(
                new Answer<NodeRef>() {
                    public NodeRef answer(InvocationOnMock invocation) throws Throwable
                    {
                        Object[] o = invocation.getArguments();
                        if (o == null || o.length == 0)
                            return null;
                        return (NodeRef) o[0];
                    }
                });
        return fileFolderService;
    }

    private RepoAdminService mockRepoAdminService()
    {
        RepoUsage usage = new RepoUsage(System.currentTimeMillis(), 10l, 100l,
                LicenseMode.ENTERPRISE, System.currentTimeMillis(), false);

        RepoAdminService repoAdminService = mock(RepoAdminService.class);
        when(repoAdminService.getRestrictions()).thenReturn(usage);
        return repoAdminService;
    }

    /**
     * Mocks up a SiteService that returns appropriate SiteInfo.
     * 
     * @return SiteService
     */
    private SiteService mockSiteService()
    {
        SiteService siteService = mock(SiteService.class);
        when(siteInfo.getShortName()).thenReturn(siteShortName);
        when(siteService.getSite(siteName)).thenReturn(siteInfo);
        return siteService;
    }

    /**
     * Mocks up a SearchService that will return the template NodeRef when queried.
     * 
     * @return SearchService
     */
    private SearchService mockSearchService()
    {
        SearchService searchService = mock(SearchService.class);
        ResultSet results = mock(ResultSet.class);
        List<NodeRef> nodeRefs = Arrays.asList(template);
        when(results.getNodeRefs()).thenReturn(nodeRefs);
        when(searchService.query((SearchParameters) any())).thenReturn(results);
        when(searchService.selectNodes(any(), any(String.class),
                any(), any(), eq(false)))
                        .thenReturn(nodeRefs);
        return searchService;
    }

    /**
     * Mocks up a PersonService that returns the correct NodeRef when given a user name.
     * 
     * @return PersonService
     */
    private PersonService mockPersonService()
    {
        PersonService personService = mock(PersonService.class);
        when(personService.getPerson(inviter.name)).thenReturn(inviter.node);
        when(personService.getPerson(invitee.name)).thenReturn(invitee.node);
        when(personService.getPerson(whitespaceInvitee.name)).thenReturn(whitespaceInvitee.node);
        when(personService.getPerson(specialCharInvitee.name)).thenReturn(specialCharInvitee.node);
        return personService;
    }

    /**
     * Mocks up NodeService to return email adresses for inviter and invitee.
     * 
     * @return NodeService
     */
    private NodeService mockNodeService()
    {
        NodeService nodeService = mock(NodeService.class);
        when(nodeService.getProperty(inviter.node, ContentModel.PROP_EMAIL)).thenReturn(inviter.email);
        when(nodeService.getProperty(invitee.node, ContentModel.PROP_EMAIL)).thenReturn(invitee.email);
        when(nodeService.getProperty(whitespaceInvitee.node, ContentModel.PROP_EMAIL)).thenReturn(
                whitespaceInvitee.email);
        when(nodeService.getProperty(specialCharInvitee.node, ContentModel.PROP_EMAIL)).thenReturn(
                specialCharInvitee.email);
        return nodeService;
    }

    /**
     * Mocks up an ActionService which returns the mailAction field when createAction() is called.
     * 
     * @return ActionService
     */
    private ActionService mockActionService()
    {
        mailAction = mock(Action.class);

        ActionService actionService = mock(ActionService.class);
        when(actionService.createAction(MailActionExecuter.NAME)).thenReturn(mailAction);
        return actionService;
    }

    private static class Person
    {
        public final String name;
        public final String email;
        public final NodeRef node;

        public Person(String name)
        {
            this.name = name;
            String encName = URLEncoder.encode(name);
            this.email = encName + "@test.com";
            this.node = new NodeRef(testStore, encName);
        }

    }
}
