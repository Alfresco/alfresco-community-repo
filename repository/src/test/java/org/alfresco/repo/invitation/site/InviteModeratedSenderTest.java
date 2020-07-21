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

import static org.alfresco.repo.invitation.activiti.SendModeratedInviteDelegate.ENTERPRISE_EMAIL_TEMPLATE_PATH;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.executer.MailActionExecuter;
import org.alfresco.repo.i18n.MessageService;
import org.alfresco.repo.invitation.WorkflowModelModeratedInvitation;
import org.alfresco.repo.invitation.activiti.SendModeratedInviteDelegate;
import org.alfresco.repo.model.Repository;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.QueryParameterDefinition;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.security.PersonService.PersonInfo;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.apache.commons.lang3.StringUtils;
import org.mockito.ArgumentCaptor;



/**
 * @author Constantin Popa
 */
public class InviteModeratedSenderTest extends TestCase
{
    private static final StoreRef testStore = new StoreRef(StoreRef.PROTOCOL_TEST, "test");

    private static final String requesterFirstName = "FirstName";
    private static final String requesterLastName = "LastName";
    private static final String requesterUserName = "JustAUserName";
    private static final String requesterRole = "SiteConsumer";
    private static final String requesterMail = "req@mail.alf";
    private static final NodeRef requesterNodeRef = new NodeRef(testStore, requesterUserName);
    
    private static final String SiteManagerGroup = "Group_site_manager";
    
    private static final NodeRef emailTemplateNodeRef = new NodeRef(testStore, "emailTemplate");

    private static final String fullSiteName = "Full Site Name";
    private static final String shortSiteName = "site-name";

    private static final String packageId = testStore + "/Package";
    
    private final MessageService messageService = mock(MessageService.class);

    private Action mailAction;
    private SiteInfo siteInfo = mock(SiteInfo.class);
    private InviteModeratedSender inviteModeratedSender;

    @SuppressWarnings("rawtypes")
    /**
     * Test that the mail action is correctly constructed when sending notifications emails about users requesting access to a specific site
     * @throws Exception
     */
    public void testSendModeratedEmail() throws Exception
    {                  
        Map<String, String> properties = buildDefaultProperties();
        inviteModeratedSender.sendMail(ENTERPRISE_EMAIL_TEMPLATE_PATH, SendModeratedInviteDelegate.EMAIL_SUBJECT_KEY, properties);

        verify(mailAction).setParameterValue(eq(MailActionExecuter.PARAM_FROM), eq(requesterMail));
        verify(mailAction).setParameterValue(eq(MailActionExecuter.PARAM_TO_MANY), eq(SiteManagerGroup));
        verify(mailAction).setParameterValue(eq(MailActionExecuter.PARAM_SUBJECT), eq(SendModeratedInviteDelegate.EMAIL_SUBJECT_KEY));
        verify(mailAction).setParameterValue(eq(MailActionExecuter.PARAM_SUBJECT_PARAMS), eq(new Object[]{fullSiteName}));
        verify(mailAction).setParameterValue(eq(MailActionExecuter.PARAM_TEMPLATE), eq(emailTemplateNodeRef));

        ArgumentCaptor<Map> modelC = ArgumentCaptor.forClass(Map.class);
        verify(mailAction).setParameterValue(eq(MailActionExecuter.PARAM_TEMPLATE_MODEL), (Serializable)modelC.capture());

        String pendingInvitesLink = StringUtils.stripStart(MessageFormat.format(InviteModeratedSender.SHARE_PENDING_INVITES_LINK, StringUtils.EMPTY, shortSiteName), "/");
        
        // Check the model
        Map model = modelC.getValue();
        assertNotNull(model);
        assertEquals(false, model.isEmpty());
        assertNotNull(model.get("productName"));
        assertEquals(model.get("inviteeName"), requesterFirstName + " " + requesterLastName);
        assertEquals(model.get("siteName"), fullSiteName);
        assertEquals(model.get("sharePendingInvitesLink"), pendingInvitesLink);
     
    }

    private Map<String, String> buildDefaultProperties()
    {
        Map<String, String> properties = new HashMap<String, String>();
        properties.put(WorkflowModelModeratedInvitation.wfVarInviteeUserName, requesterUserName);
        properties.put(WorkflowModelModeratedInvitation.wfVarInviteeRole, requesterRole);
        properties.put(WorkflowModelModeratedInvitation.wfVarResourceName, shortSiteName);
        properties.put(WorkflowModelModeratedInvitation.bpmGroupAssignee, SiteManagerGroup);
        properties.put(WorkflowModelModeratedInvitation.wfVarResourceType, "website");
        properties.put(InviteNominatedSender.WF_PACKAGE, packageId);
        
        return properties;
    }

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        ServiceRegistry services = mockServices();
        Repository repository = mockRepository();
        inviteModeratedSender = new InviteModeratedSender(services, repository, messageService);
    }

    /**
     * Mocks up a Repository that will return the inviter as the current user.
     * 
     * @return Repository
     */
    private Repository mockRepository()
    {
        Repository repository = mock(Repository.class);
        when(repository.getPerson()).thenReturn(requesterNodeRef);
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

        ServiceRegistry services = mock(ServiceRegistry.class);
        when(services.getActionService()).thenReturn(mockActionService);
        when(services.getNodeService()).thenReturn(mockNodeService);
        when(services.getPersonService()).thenReturn(mockPersonService);
        when(services.getSearchService()).thenReturn(mockSearchService);
        when(services.getSiteService()).thenReturn(mockSiteService);
        when(services.getFileFolderService()).thenReturn(mockFileFolderService);
        return services;
    }

    private FileFolderService mockFileFolderService()
    {
        FileFolderService fileFolderService = mock(FileFolderService.class);
        when(fileFolderService.getLocalizedSibling(emailTemplateNodeRef)).thenReturn(emailTemplateNodeRef);
        return fileFolderService;
    }

    /**
     * Mocks up a SiteService that returns appropriate SiteInfo.
     * 
     * @return SiteService
     */
    private SiteService mockSiteService()
    {
        SiteService siteService = mock(SiteService.class);
        when(siteInfo.getTitle()).thenReturn(fullSiteName);
        when(siteService.getSite(shortSiteName)).thenReturn(siteInfo);
        return siteService;
    }

    /**
     * Mocks up a SearchService that will return the template NodeRef when* queried.
     * 
     * @return SearchService
     */
    private SearchService mockSearchService()
    {
        SearchService searchService = mock(SearchService.class);
        ResultSet results = mock(ResultSet.class);
        List<NodeRef> nodeRefs = Arrays.asList(emailTemplateNodeRef);
        when(results.getNodeRefs()).thenReturn(nodeRefs);
        when(searchService.query((SearchParameters) any())).thenReturn(results);
        when(searchService.selectNodes(any(), any(String.class),
                    any(), any(), eq(false)))
                    .thenReturn(nodeRefs);
        return searchService;
    }

    /**
     * Mocks up a PersonService that returns the correct NodeRef when given a* user name.
     * 
     * @return PersonService
     */
    private PersonService mockPersonService()
    {
        PersonService personService = mock(PersonService.class);
        
        when(personService.getPerson(requesterUserName)).thenReturn(requesterNodeRef);
        when(personService.getPerson(requesterNodeRef)).thenReturn(new PersonInfo(requesterNodeRef, requesterUserName, requesterFirstName, requesterLastName));
        return personService;
    }

    /**
     * Mocks up NodeService to return email adresses for requester
     * 
     * @return NodeService
     */
    private NodeService mockNodeService()
    {
        NodeService nodeService = mock(NodeService.class);
        when(nodeService.getProperty(requesterNodeRef, ContentModel.PROP_EMAIL)).thenReturn(requesterMail);
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
        mailAction = mock(Action.class);
        
        ActionService actionService = mock(ActionService.class);
        when(actionService.createAction(MailActionExecuter.NAME)).thenReturn(mailAction);
        return actionService;
    }
}
