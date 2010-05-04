/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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

package org.alfresco.repo.invitation.site;

import static org.alfresco.repo.invitation.WorkflowModelNominatedInvitation.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.executer.MailActionExecuter;
import org.alfresco.repo.i18n.MessageService;
import org.alfresco.repo.model.Repository;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.repository.TemplateService;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.mockito.ArgumentCaptor;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.extensions.surf.util.URLEncoder;

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
    private static final String role = "Role";
    private static final String password = "password";
    private static final String ticket = "Ticket";
    private static final String path = testStore + "/path";
    private static final String packageId = testStore + "/Package";
    private static final String instanceId = "InstanceId";

    private final MessageService messageService = mock(MessageService.class);
    private Action mailAction = mock(Action.class);
    private SiteInfo siteInfo = mock(SiteInfo.class);
    private TemplateService templateService;
    private InviteSender sender;

    public void testSendMailWorkingPath() throws Exception
    {
    	String rolePropertyName = "invitation.invitesender.email.role.Role";
    	String subjectPropertyName = "invitation.invitesender.email.subject";

    	String subjectMsg = "Subject message";
		when(messageService.getMessage(eq(subjectPropertyName), any())).thenReturn(subjectMsg);
    	
        Map<String, String> properties = buildDefaultProperties();
        sender.sendMail(properties);

        verify(messageService).getMessage(eq(subjectPropertyName), any());
		verify(messageService).getMessage(eq(rolePropertyName));

        verify(mailAction).setParameterValue(eq(MailActionExecuter.PARAM_FROM), eq(inviter.email));
        verify(mailAction).setParameterValue(eq(MailActionExecuter.PARAM_TO), eq(invitee.email));
        verify(mailAction).setParameterValue(eq(MailActionExecuter.PARAM_SUBJECT),
                    eq(subjectMsg));

        Map<String, String> argsMap = getArgsMap();
        assertNotNull(argsMap);
        assertEquals(siteShortName, argsMap.get("siteName"));
        assertEquals(invitee.node.toString(), argsMap.get("inviteePersonRef"));
        assertEquals(inviter.node.toString(), argsMap.get("inviterPersonRef"));
        assertEquals(siteShortName, argsMap.get("siteName"));
        assertEquals(invitee.name, argsMap.get("inviteeUserName"));
        assertEquals(password, argsMap.get("inviteeGenPassword"));
        assertEquals(
                    "test://test/path/accpet?inviteId=InstanceId&inviteeUserName=invitee&siteShortName=Full Site Name&inviteTicket=Ticket",
                    argsMap.get("acceptLink"));
        assertEquals(
                    "test://test/path/reject?inviteId=InstanceId&inviteeUserName=invitee&siteShortName=Full Site Name&inviteTicket=Ticket",
                    argsMap.get("rejectLink"));

        // When no role message is found then the role name is used.
        assertEquals(role, argsMap.get("inviteeSiteRole"));
        
        reset(templateService, messageService);
        String roleMsg = "role message";
        when(messageService.getMessage(rolePropertyName)).thenReturn(roleMsg);
        sender.sendMail(properties);
        // Check that when the role message is set then that role message is used.
        assertEquals(roleMsg, getArgsMap().get("inviteeSiteRole"));
    }

    public void testSendMailWithWhitespaceUserName() throws Exception
    {
        Map<String, String> properties = buildDefaultProperties();
        properties.put(wfVarInviteeUserName, whitespaceInvitee.name);
        sender.sendMail(properties);
        Map<String, String> argsMap = getArgsMap();
        String acceptLink = argsMap.get("acceptLink");
        assertEquals(
                    "test://test/path/accpet?inviteId=InstanceId&inviteeUserName=First%20Second%09third%0aFourth%0d%0aFifth&siteShortName=Full Site Name&inviteTicket=Ticket",
                    acceptLink);
        String rejectLink = argsMap.get("rejectLink");
        assertEquals(
                    "test://test/path/reject?inviteId=InstanceId&inviteeUserName=First%20Second%09third%0aFourth%0d%0aFifth&siteShortName=Full Site Name&inviteTicket=Ticket",
                    rejectLink);
    }

    public void testSendMailWithSpecialCharUserName() throws Exception
    {
        Map<String, String> properties = buildDefaultProperties();
        properties.put(wfVarInviteeUserName, specialCharInvitee.name);
        sender.sendMail(properties);
        Map<String, String> argsMap = getArgsMap();
        String acceptLink = argsMap.get("acceptLink");
        assertEquals(
                    "test://test/path/accpet?inviteId=InstanceId&inviteeUserName=%c3%a0%c3%a2%c3%a6%c3%a7%c3%a9%c3%a8%c3%aa%c3%ab%c3%ae%c3%af%c3%b4%c5%93%c3%b9%c3%bb%c3%bc%c3%bf%c3%b1&siteShortName=Full Site Name&inviteTicket=Ticket",
                    acceptLink);
        String rejectLink = argsMap.get("rejectLink");
        assertEquals(
                    "test://test/path/reject?inviteId=InstanceId&inviteeUserName=%c3%a0%c3%a2%c3%a6%c3%a7%c3%a9%c3%a8%c3%aa%c3%ab%c3%ae%c3%af%c3%b4%c5%93%c3%b9%c3%bb%c3%bc%c3%bf%c3%b1&siteShortName=Full Site Name&inviteTicket=Ticket",
                    rejectLink);
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
        properties.put(InviteSender.WF_PACKAGE, packageId);
        properties.put(InviteSender.WF_INSTANCE_ID, instanceId);
        return properties;
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> getArgsMap()
    {
        ArgumentCaptor<Map> modelCaptor = ArgumentCaptor.forClass(Map.class);
        verify(templateService).processTemplate(eq(template.toString()), modelCaptor.capture());
        Map<String, Serializable> model = modelCaptor.getValue();
        Map<String, String> argsMap = (Map<String, String>) model.get("args");
        return argsMap;
    }

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        ServiceRegistry services = mockServices();
        Repository repository = mockRepository();
        sender = new InviteSender(services, repository, messageService);
    }

    /**
     * Mocks up a Repository that will return the inviter as the current user.
     * 
     * @return
     */
    private Repository mockRepository()
    {
        Repository repository = mock(Repository.class);
        when(repository.getPerson()).thenReturn(inviter.node);
        return repository;
    }

    /**
     * @return
     */
    private ServiceRegistry mockServices()
    {
        ActionService mockActionService = mockActionService();
        NodeService mockNodeService = mockNodeService();
        PersonService mockPersonService = mockPersonService();
        SearchService mockSearchService = mockSearchService();
        SiteService mockSiteService = mockSiteService();
        TemplateService mockTemplateService = mockTemplateService();

        ServiceRegistry services = mock(ServiceRegistry.class);
        when(services.getActionService()).thenReturn(mockActionService);
        when(services.getNodeService()).thenReturn(mockNodeService);
        when(services.getPersonService()).thenReturn(mockPersonService);
        when(services.getSearchService()).thenReturn(mockSearchService);
        when(services.getSiteService()).thenReturn(mockSiteService);
        when(services.getTemplateService()).thenReturn(mockTemplateService);
        return services;
    }

    /**
     * Mocks up a TemplateService that returns an empty HashMap when
     * buildDefaultModel() is called.
     * 
     * @return
     */
    private TemplateService mockTemplateService()
    {
        this.templateService = mock(TemplateService.class);
        when(templateService.buildDefaultModel(inviter.node, null, null, null, null)).thenAnswer(
                    new Answer<Map<String, Object>>()
                    {
                        public Map<String, Object> answer(InvocationOnMock invocation) throws Throwable
                        {
                            return new HashMap<String, Object>();
                        }
                    });
        when(templateService.processTemplate(anyString(), any())).thenReturn(mailText);
        return templateService;
    }

    /**
     * Mocks up a SiteService that returns appropriate SiteInfo.
     * 
     * @return
     */
    private SiteService mockSiteService()
    {
        SiteService siteService = mock(SiteService.class);
        when(siteInfo.getShortName()).thenReturn(siteShortName);
        when(siteService.getSite(siteName)).thenReturn(siteInfo);
        return siteService;
    }

    /**
     * Mocks up a SearchService that will return the template NodeRef when
     * queried.
     * 
     * @return
     */
    private SearchService mockSearchService()
    {
        SearchService searchService = mock(SearchService.class);
        ResultSet results = mock(ResultSet.class);
        List<NodeRef> nodeRefs = Arrays.asList(template);
        when(results.getNodeRefs()).thenReturn(nodeRefs);
        when(searchService.query((SearchParameters) any())).thenReturn(results);
        return searchService;
    }

    /**
     * Mocks up a PersonService that returns the correct NodeRef when given a
     * user name.
     * 
     * @return
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
     * @return
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
     * Mocks up an ActionService which returns the mailAction field when
     * createAction() is called.
     * 
     * @return ActionService
     */
    private ActionService mockActionService()
    {
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
