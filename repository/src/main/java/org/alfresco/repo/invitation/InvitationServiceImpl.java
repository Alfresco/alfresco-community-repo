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

package org.alfresco.repo.invitation;

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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.executer.MailActionExecuter;
import org.alfresco.repo.admin.SysAdminParams;
import org.alfresco.repo.client.config.ClientAppConfig;
import org.alfresco.repo.i18n.MessageService;
import org.alfresco.repo.invitation.activiti.SendNominatedInviteDelegate;
import org.alfresco.repo.invitation.site.InviteModeratedSender;
import org.alfresco.repo.invitation.site.InviteNominatedSender;
import org.alfresco.repo.invitation.site.InviteSender;
import org.alfresco.repo.jscript.ScriptNode;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.node.NodeServicePolicies.BeforeDeleteNodePolicy;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.security.authentication.AuthenticationException;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.security.authentication.PasswordGenerator;
import org.alfresco.repo.security.authentication.UserNameGenerator;
import org.alfresco.repo.site.SiteModel;
import org.alfresco.repo.transaction.TransactionalResourceHelper;
import org.alfresco.repo.workflow.CancelWorkflowActionExecuter;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.repo.workflow.activiti.ActivitiConstants;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.invitation.Invitation;
import org.alfresco.service.cmr.invitation.Invitation.ResourceType;
import org.alfresco.service.cmr.invitation.InvitationException;
import org.alfresco.service.cmr.invitation.InvitationExceptionForbidden;
import org.alfresco.service.cmr.invitation.InvitationExceptionNotFound;
import org.alfresco.service.cmr.invitation.InvitationExceptionUserError;
import org.alfresco.service.cmr.invitation.InvitationSearchCriteria;
import org.alfresco.service.cmr.invitation.InvitationSearchCriteria.InvitationType;
import org.alfresco.service.cmr.invitation.InvitationService;
import org.alfresco.service.cmr.invitation.ModeratedInvitation;
import org.alfresco.service.cmr.invitation.NominatedInvitation;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.TemplateService;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.workflow.WorkflowAdminService;
import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.alfresco.service.cmr.workflow.WorkflowException;
import org.alfresco.service.cmr.workflow.WorkflowPath;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.cmr.workflow.WorkflowTaskQuery;
import org.alfresco.service.cmr.workflow.WorkflowTaskState;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.GUID;
import org.alfresco.util.PropertyCheck;
import org.alfresco.util.UrlUtil;
import org.alfresco.util.collections.CollectionUtils;
import org.alfresco.util.collections.Function;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * Implementation of invitation service.
 * 
 * @see org.alfresco.service.cmr.invitation.Invitation
 * @author mrogers
 * @author Nick Smith
 */
public class InvitationServiceImpl implements InvitationService, NodeServicePolicies.BeforeDeleteNodePolicy
{
    private static final Log logger = LogFactory.getLog(InvitationServiceImpl.class);
    private static final String REJECT_TEMPLATE = "/alfresco/bootstrap/invite/moderated-reject-email.ftl";
    private static final String MSG_NOT_SITE_MANAGER = "invitation.cancel.not_site_manager";
    private static final List<String> SEND_INVITE_NOMINATED_PROP_NAMES = Arrays.asList(wfVarInviteeUserName,//
            wfVarResourceName,//
            wfVarInviterUserName,//
            wfVarInviteeUserName,//
            wfVarRole,//
            wfVarInviteeGenPassword,//
            wfVarResourceName,//
            wfVarInviteTicket,//
            wfVarServerPath,//
            wfVarAcceptUrl,//
            wfVarRejectUrl,
            InviteNominatedSender.WF_INSTANCE_ID);
    
    private static final List<String> SEND_INVITE_MODERATED_PROP_NAMES = Arrays.asList(
            WorkflowModelModeratedInvitation.wfVarInviteeUserName,
            WorkflowModelModeratedInvitation.wfVarInviteeRole,
            WorkflowModelModeratedInvitation.wfVarResourceName,
            WorkflowModelModeratedInvitation.bpmGroupAssignee,
            WorkflowModelModeratedInvitation.wfVarResourceType,
            WorkflowModelModeratedInvitation.wfVarTemplateAssetsUrl,
            WorkflowModelModeratedInvitation.wfVarWorkspaceUrl);
    
    
    /**
     * Services
     */
    private WorkflowService workflowService;
    private WorkflowAdminService workflowAdminService;
    private ActionService actionService;
    private PersonService personService;
    private SiteService siteService;
    private MutableAuthenticationService authenticationService;
    private PermissionService permissionService;
    private DictionaryService dictionaryService;
    private NamespaceService namespaceService;
    private NodeService nodeService;
    // user name and password generation beans
    private UserNameGenerator usernameGenerator;
    private PasswordGenerator passwordGenerator;
    private PolicyComponent policyComponent;
    private SysAdminParams sysAdminParams;
    private TemplateService templateService;
    private Repository repositoryHelper;
    private ServiceRegistry serviceRegistry;
    private MessageService messageService;
    private InviteNominatedSender inviteNominatedSender;
    private InviteModeratedSender inviteModeratedSender;
    private ClientAppConfig clientAppConfig;

    // maximum number of tries to generate a invitee user name which
    // does not already belong to an existing person
    public static final int MAX_NUM_INVITEE_USER_NAME_GEN_TRIES = 10;

    private int maxUserNameGenRetries = MAX_NUM_INVITEE_USER_NAME_GEN_TRIES;

    // Property determining whether emails should be sent.
    private boolean sendEmails = true;
    
    private enum InvitationWorkflowType { NOMINATED, NOMINATED_EXTERNAL, MODERATED };
    
    // The nominated invite workflow definition to use for internal users
    private String nominatedInvitationWorkflowId = 
            WorkflowModelNominatedInvitation.WORKFLOW_DEFINITION_NAME_ACTIVITI_ADD_DIRECT;
    // The nominated invite workflow definition to use for external users
    private String nominatedInvitationExternalWorkflowId = 
            WorkflowModelNominatedInvitation.WORKFLOW_DEFINITION_NAME_ACTIVITI_INVITE;
    // The nominated invite workflow definition to use for internal users
    private String moderatedInvitationWorkflowId = 
            WorkflowModelModeratedInvitation.WORKFLOW_DEFINITION_NAME_ACTIVITI;
    
    public void setClientAppConfig(ClientAppConfig clientAppConfig)
    {
        this.clientAppConfig = clientAppConfig;
    }
         
    /**
     * Set the policy component
     * 
     * @param policyComponent policy component
     */
    public void setPolicyComponent(PolicyComponent policyComponent)
    {
        this.policyComponent = policyComponent;
    }

    /**
     * Sets the nominated invite activiti workflow definition for internal users
     * 
     * @param nominatedInvitationWorkflowId
     */
    public void setNominatedInvitationWorkflowId(String nominatedInvitationWorkflowId)
    {
        this.nominatedInvitationWorkflowId = nominatedInvitationWorkflowId;
    }

    /**
     * Sets the nominated invite activiti workflow definition for external users
     * 
     * @param nominatedInvitationExternalWorkflowId
     */
    public void setNominatedInvitationExternalWorkflowId(String nominatedInvitationExternalWorkflowId)
    {
        this.nominatedInvitationExternalWorkflowId = nominatedInvitationExternalWorkflowId;
    }

    /**
     * Sets the moderated invite activiti workflow definition
     * 
     * @param moderatedInvitationWorkflowId
     */
    public void setModeratedInvitationWorkflowId(String moderatedInvitationWorkflowId)
    {
        this.moderatedInvitationWorkflowId = moderatedInvitationWorkflowId;
    }

    /**
     * Checks that all necessary properties and services have been provided.
     */
    public void init()
    {
        PropertyCheck.mandatory(this, "nodeService", nodeService);
        PropertyCheck.mandatory(this, "WorkflowService", workflowService);
        PropertyCheck.mandatory(this, "ActionService", actionService);
        PropertyCheck.mandatory(this, "PersonService", personService);
        PropertyCheck.mandatory(this, "SiteService", siteService);
        PropertyCheck.mandatory(this, "AuthenticationService", authenticationService);
        PropertyCheck.mandatory(this, "PermissionService", permissionService);
        PropertyCheck.mandatory(this, "NamespaceService", namespaceService);
        PropertyCheck.mandatory(this, "NodeService", nodeService);
        PropertyCheck.mandatory(this, "UserNameGenerator", usernameGenerator);
        PropertyCheck.mandatory(this, "PasswordGenerator", passwordGenerator);
        PropertyCheck.mandatory(this, "PolicyComponent", policyComponent);
        PropertyCheck.mandatory(this, "templateService", templateService);
        PropertyCheck.mandatory(this, "clientAppConfig", clientAppConfig);

        this.inviteNominatedSender = new InviteNominatedSender(serviceRegistry, repositoryHelper, messageService);
        this.inviteModeratedSender = new InviteModeratedSender(serviceRegistry, repositoryHelper, messageService);
        
        //
        this.policyComponent.bindClassBehaviour(QName.createQName(NamespaceService.ALFRESCO_URI, "beforeDeleteNode"),
                    SiteModel.TYPE_SITE, new JavaBehaviour(this, "beforeDeleteNode"));
        this.policyComponent.bindClassBehaviour(BeforeDeleteNodePolicy.QNAME, ContentModel.TYPE_PERSON, new JavaBehaviour(this, "beforeDeleteNode"));
    }

    /**
     * Get the names of the workflows which are managed by the invitation
     * service
     * 
     * @return the workflows which are managed by the invitation service
     */
    public List<String> getInvitationServiceWorkflowNames()
    {
        List<String> ret = new ArrayList<String>(3);
        ret.add(WorkflowModelNominatedInvitation.WORKFLOW_DEFINITION_NAME_ACTIVITI_ADD_DIRECT);
        ret.add(WorkflowModelNominatedInvitation.WORKFLOW_DEFINITION_NAME_ACTIVITI_INVITE);
        ret.add(WorkflowModelModeratedInvitation.WORKFLOW_DEFINITION_NAME_ACTIVITI);
        return ret;
    }

    /**
     * Start the invitation process for a NominatedInvitation
     * 
     * @param inviteeUserName Alfresco user name of the invitee
     * @param resourceType resourceType
     * @param resourceName String
     * @param inviteeRole String
     * @param serverPath String
     * @param acceptUrl String
     * @param rejectUrl String
     * @return the nominated invitation which will contain the invitationId and
     *         ticket which will uniqely identify this invitation for the rest
     *         of the workflow.
     * @throws InvitationException
     * @throws InvitationExceptionUserError
     * @throws InvitationExceptionForbidden
     */
    public NominatedInvitation inviteNominated(String inviteeUserName, Invitation.ResourceType resourceType,
                String resourceName, String inviteeRole, String serverPath, String acceptUrl, String rejectUrl)
    {
        // inviteeUserName was specified
        NodeRef person = this.personService.getPerson(inviteeUserName);

        Serializable firstNameVal = this.getNodeService().getProperty(person, ContentModel.PROP_FIRSTNAME);
        Serializable lastNameVal = this.getNodeService().getProperty(person, ContentModel.PROP_LASTNAME);
        Serializable emailVal = this.getNodeService().getProperty(person, ContentModel.PROP_EMAIL);
        String firstName = DefaultTypeConverter.INSTANCE.convert(String.class, firstNameVal);
        String lastName = DefaultTypeConverter.INSTANCE.convert(String.class, lastNameVal);
        String email = DefaultTypeConverter.INSTANCE.convert(String.class, emailVal);

        return inviteNominated(firstName, lastName, email, inviteeUserName, resourceType, resourceName, inviteeRole,
                    serverPath, acceptUrl, rejectUrl);
    }
    /**
     * Start the invitation process for a NominatedInvitation
     * 
     * @param inviteeUserName Alfresco user name of the invitee
     * @param resourceType resourceType
     * @param resourceName String
     * @param inviteeRole String
     * @param acceptUrl String
     * @param rejectUrl String
     * @return the nominated invitation which will contain the invitationId and
     *         ticket which will uniqely identify this invitation for the rest
     *         of the workflow.
     * @throws InvitationException
     * @throws InvitationExceptionUserError
     * @throws InvitationExceptionForbidden
     */
    public NominatedInvitation inviteNominated(String inviteeUserName, Invitation.ResourceType resourceType,
                String resourceName, String inviteeRole, String acceptUrl, String rejectUrl)
    {
        // inviteeUserName was specified
        NodeRef person = this.personService.getPerson(inviteeUserName);

        Serializable firstNameVal = this.getNodeService().getProperty(person, ContentModel.PROP_FIRSTNAME);
        Serializable lastNameVal = this.getNodeService().getProperty(person, ContentModel.PROP_LASTNAME);
        Serializable emailVal = this.getNodeService().getProperty(person, ContentModel.PROP_EMAIL);
        String firstName = DefaultTypeConverter.INSTANCE.convert(String.class, firstNameVal);
        String lastName = DefaultTypeConverter.INSTANCE.convert(String.class, lastNameVal);
        String email = DefaultTypeConverter.INSTANCE.convert(String.class, emailVal);
        String serverPath = UrlUtil.getShareUrl(sysAdminParams);

        return inviteNominated(firstName, lastName, email, inviteeUserName, resourceType, resourceName, inviteeRole,
                    serverPath, acceptUrl, rejectUrl);
    }
    /**
     * Start the invitation process for a NominatedInvitation
     * 
     * @param inviteeFirstName String
     * @param inviteeLastName String
     * @param inviteeEmail String
     * @param resourceType Invitation.ResourceType
     * @param resourceName String
     * @param inviteeRole String
     * @param acceptUrl String
     * @param rejectUrl String
     * @return the nominated invitation which will contain the invitationId and
     *         ticket which will uniqely identify this invitation for the rest
     *         of the workflow.
     * @throws InvitationException
     * @throws InvitationExceptionUserError
     * @throws InvitationExceptionForbidden
     */
    public NominatedInvitation inviteNominated(String inviteeFirstName, String inviteeLastName, String inviteeEmail,
                Invitation.ResourceType resourceType, String resourceName, String inviteeRole, String acceptUrl, String rejectUrl)
    {
        String serverPath = UrlUtil.getShareUrl(sysAdminParams);
        return inviteNominated(inviteeFirstName, inviteeLastName, inviteeEmail, null, resourceType, resourceName,
                    inviteeRole, serverPath, acceptUrl, rejectUrl);
    }

    /**
     * Start the invitation process for a NominatedInvitation
     * 
     * @param inviteeFirstName String
     * @param inviteeLastName String
     * @param inviteeEmail String
     * @param resourceType Invitation.ResourceTyp
     * @param resourceName String
     * @param inviteeRole String
     * @param serverPath String
     * @param acceptUrl String
     * @param rejectUrl String
     * @return the nominated invitation which will contain the invitationId and
     *         ticket which will uniqely identify this invitation for the rest
     *         of the workflow.
     * @throws InvitationException
     * @throws InvitationExceptionUserError
     * @throws InvitationExceptionForbidden
     */
    public NominatedInvitation inviteNominated(String inviteeFirstName, String inviteeLastName, String inviteeEmail,
                Invitation.ResourceType resourceType, String resourceName, String inviteeRole, String serverPath,
                String acceptUrl, String rejectUrl)
    {
        return inviteNominated(inviteeFirstName, inviteeLastName, inviteeEmail, null, resourceType, resourceName,
                    inviteeRole, serverPath, acceptUrl, rejectUrl);
    }

    // Temporary method
    private NominatedInvitation inviteNominated(String inviteeFirstName, String inviteeLastName, String inviteeEmail,
                String inviteeUserName, Invitation.ResourceType resourceType, String resourceName, String inviteeRole,
                String serverPath, String acceptUrl, String rejectUrl)
    {
        // Validate the request

        // Check resource exists

        if (resourceType == Invitation.ResourceType.WEB_SITE)
        {
            return startNominatedInvite(inviteeFirstName, inviteeLastName, inviteeEmail, inviteeUserName, resourceType,
                        resourceName, inviteeRole, serverPath, acceptUrl, rejectUrl);
        }

        throw new InvitationException("unknown resource type");
    }

    /**
     * Start the invitation process for a ModeratedInvitation
     * 
     * @param inviteeComments why does the invitee want access to the resource ?
     * @param inviteeUserName who is to be invited
     * @param resourceType Invitation .ResourceType what resource type ?
     * @param resourceName which resource
     * @param inviteeRole which role ?
     */
    public ModeratedInvitation inviteModerated(String inviteeComments, String inviteeUserName,
                Invitation.ResourceType resourceType, String resourceName, String inviteeRole)
    {
        if (resourceType == Invitation.ResourceType.WEB_SITE)
        {
            return startModeratedInvite(inviteeComments, inviteeUserName, resourceType, resourceName, inviteeRole);
        }
        throw new InvitationException("unknown resource type");
    }

    /**
     * Invitee accepts this invitation Nominated Invitaton process only
     * 
     * @param invitationId the invitation id
     * @param ticket the ticket produced when creating the invitation.
     */
    public Invitation accept(String invitationId, String ticket)
    {
        WorkflowTask startTask = getStartTask(invitationId);
        NominatedInvitation invitation = getNominatedInvitation(startTask);
        if(invitation == null)
        {
            throw new InvitationException("State error, accept may only be called on a nominated invitation.");
        }
        // Check invitationId and ticket match
        if(invitation.getTicket().equals(ticket)==false)
        {
            //TODO localise msg
            String msg = "Response to invite has supplied an invalid ticket. The response to the invitation could thus not be processed";
            throw new InvitationException(msg);
        }
        endInvitation(startTask,
                WorkflowModelNominatedInvitation.WF_TRANSITION_ACCEPT, null,
                WorkflowModelNominatedInvitation.WF_TASK_ACTIVIT_INVITE_PENDING);
        
        //MNT-9101 Share: Cancelling an invitation for a disabled user, the user gets deleted in the process.
        NodeRef person = personService.getPersonOrNull(invitation.getInviteeUserName());
        if (person != null && nodeService.hasAspect(person, ContentModel.ASPECT_ANULLABLE))
        {
            nodeService.removeAspect(person, ContentModel.ASPECT_ANULLABLE);
        }
        
        return invitation;
    }

    private void endInvitation(WorkflowTask startTask, String transition, Map<QName, Serializable> properties, QName... taskTypes )
    {
        // Deleting a person can cancel their invitations. Cancelling invitations can delete inactive persons! So prevent infinite looping here
        if (TransactionalResourceHelper.getSet(getClass().getName()).add(startTask.getPath().getInstance().getId()))
        {        
            List<WorkflowTask> tasks = workflowService.getTasksForWorkflowPath(startTask.getPath().getId());
            if(tasks.size()==1)
            {
                WorkflowTask task = tasks.get(0);
                if(taskTypeMatches(task, taskTypes))
                {
                    if(properties != null)
                    {
                        workflowService.updateTask(task.getId(), properties, null, null);
                    }
                    workflowService.endTask(task.getId(), transition);
                    return;
                }
            }
            // Throw exception if the task not found.
            Object objs[] = { startTask.getPath().getInstance().getId() };
            throw new InvitationExceptionUserError("invitation.invite.already_finished", objs);
        }
    }
    
    /**
     * Moderator approves this invitation
     * 
     * @param invitationId the request id
     * @param reason comments about the acceptance
     */
    public Invitation approve(String invitationId, String reason)
    {
        WorkflowTask startTask = getStartTask(invitationId);
        ModeratedInvitation invitation = getModeratedInvitation(invitationId);
        if(invitation == null)
        {
            String msg = "State error, can only call approve on a Moderated invitation.";
            throw new InvitationException(msg);
        }

        // Check approver is a site manager
        String currentUser = this.authenticationService.getCurrentUserName();
        checkManagerRole(currentUser, invitation.getResourceType(), invitation.getResourceName());
        Map<QName, Serializable> wfReviewProps = new HashMap<QName, Serializable>();
        wfReviewProps.put(ContentModel.PROP_OWNER, currentUser);
        wfReviewProps.put(WorkflowModelModeratedInvitation.WF_PROP_REVIEW_COMMENTS, reason);
        endInvitation(startTask,
                WorkflowModelModeratedInvitation.WF_TRANSITION_APPROVE,
                wfReviewProps,
                WorkflowModelModeratedInvitation.WF_ACTIVITI_REVIEW_TASK);
        return invitation;
    }

    /**
     * User or moderator rejects this request
     * 
     * @param invitationId String
     * @param reason , optional reason for rejection
     */
    public Invitation reject(String invitationId, String reason)
    {
        WorkflowTask startTask = getStartTask(invitationId);
        if(taskTypeMatches(startTask, WorkflowModelModeratedInvitation.WF_START_TASK))
        {
            return rejectModeratedInvitation(startTask, reason);
        }
        else
        {
            return rejectNominatedInvitation(startTask);
        }
    }

    private Invitation rejectModeratedInvitation(WorkflowTask startTask, String reason)
    {
        ModeratedInvitation invitation = getModeratedInvitation(startTask.getPath().getId());
        // Check rejecter is a site manager and throw and exception if not
        String rejecterUserName = this.authenticationService.getCurrentUserName();
        checkManagerRole(rejecterUserName, invitation.getResourceType(), invitation.getResourceName());

        Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
        properties.put(ContentModel.PROP_OWNER, rejecterUserName);
        properties.put(WorkflowModelModeratedInvitation.WF_PROP_REVIEW_COMMENTS, reason);

        endInvitation(startTask,
                WorkflowModelModeratedInvitation.WF_TRANSITION_REJECT,
                properties,
                WorkflowModelModeratedInvitation.WF_ACTIVITI_REVIEW_TASK);
        return invitation;
    }

    private Invitation rejectNominatedInvitation(WorkflowTask startTask)
    {
        NominatedInvitation invitation = getNominatedInvitation(startTask);
        endInvitation(startTask,
                WorkflowModelNominatedInvitation.WF_TRANSITION_REJECT, null,
                WorkflowModelNominatedInvitation.WF_TASK_ACTIVIT_INVITE_PENDING);
        return invitation;
    }

    /**
    * {@inheritDoc}
     */
    public Invitation cancel(String invitationId)
    {
        try
        {
            WorkflowTask startTask = getStartTask(invitationId);
            if (taskTypeMatches(startTask, WorkflowModelModeratedInvitation.WF_START_TASK))
            {
                return cancelModeratedInvitation(startTask);
            }
            else
            {
                return cancelNominatedInvitation(startTask);
            }
        }
        catch (InvitationExceptionNotFound e)
        {
            // Invitation already deleted or deleted in background
            return null;
        }
    }

    private Invitation cancelModeratedInvitation(WorkflowTask startTask)
    {
        ModeratedInvitation invitation = getModeratedInvitation(startTask.getPath().getId());
        String currentUserName = this.authenticationService.getCurrentUserName();
        if (!AuthenticationUtil.isRunAsUserTheSystemUser())
        {
            if (false == currentUserName.equals(invitation.getInviteeUserName()))
            {
                checkManagerRole(currentUserName, invitation.getResourceType(), invitation.getResourceName());
            }
        }
        // Only proceed with the cancel if the site still exists (the site may have been deleted and invitations may be
        // getting cancelled in the background)
        if (this.siteService.getSite(invitation.getResourceName()) != null)
        {
            workflowService.cancelWorkflow(invitation.getInviteId());
        }
        return invitation;
    }

    private Invitation cancelNominatedInvitation(WorkflowTask startTask)
    {
        NominatedInvitation invitation = getNominatedInvitation(startTask);
        String currentUserName = this.authenticationService.getCurrentUserName();
        if (!AuthenticationUtil.isRunAsUserTheSystemUser())
        {
            if (false == currentUserName.equals(invitation.getInviterUserName()))
            {
                checkManagerRole(currentUserName, invitation.getResourceType(), invitation.getResourceName());
            }
        }
        // Only proceed with the cancel if the site still exists (the site may have been deleted and invitations may be
        // getting cancelled in the background)
        if (this.siteService.getSite(invitation.getResourceName()) != null)
        {
            endInvitation(startTask, WorkflowModelNominatedInvitation.WF_TRANSITION_CANCEL, null,
                    WorkflowModelNominatedInvitation.WF_TASK_ACTIVIT_INVITE_PENDING);
        }
        return invitation;
    }

    /**
     * Get an invitation from its invitation id <BR />
     * Invitations are returned which may be in progress or completed.
     * 
     * @throws InvitationExceptionNotFound the invitation does not exist.
     * @throws InvitationExceptionUserError
     * @return the invitation.
     */
    public Invitation getInvitation(String invitationId)
    {
        WorkflowTask startTask = getStartTask(invitationId);
        return getInvitation(startTask);
    }

    private Invitation getInvitation(WorkflowTask startTask)
    {
        Invitation invitation = getNominatedInvitation(startTask);
        if(invitation == null)
        {
            invitation = getModeratedInvitation(startTask.getPath().getId());
        }
        return invitation;
    }
    
    private Map<String, WorkflowTask> getInvitationTasks(List<String> invitationIds)
    {
        for (String invitationId: invitationIds)
        {
            validateInvitationId(invitationId);
        }

        // query for invite workflow task associate
        long start = (logger.isDebugEnabled()) ? System.currentTimeMillis() : 0;
        List<WorkflowTask> inviteStartTasks = workflowService.getStartTasks(invitationIds, true);
        if (logger.isDebugEnabled())
        {
            logger.debug("  getInvitationTask("+invitationIds.size()+") in "+ (System.currentTimeMillis()-start) + " ms");
        }
        
        Map<String, WorkflowTask> result = new HashMap<String, WorkflowTask>(inviteStartTasks.size() * 2);
        for(WorkflowTask inviteStartTask: inviteStartTasks)
        {
            String invitationId = inviteStartTask.getPath().getInstance().getId();
            // The following does not work for moderated tasks
            // String invitationId = (String)
            // inviteStartTask.getProperties().get(WorkflowModel.PROP_WORKFLOW_INSTANCE_ID);
            result.put(invitationId, inviteStartTask);
        }
        
        return result;
    }
    
	private WorkflowTask getModeratedInvitationReviewTask(String inviteeId, String siteShortName)
    {
		WorkflowTask reviewTask = null;

		// Is there an outstanding site invite request for the invitee?
		InvitationSearchCriteriaImpl criteria = new InvitationSearchCriteriaImpl();
		criteria.setInvitationType(InvitationType.MODERATED);
		criteria.setInvitee(inviteeId);
		criteria.setResourceName(siteShortName);
		criteria.setResourceType(ResourceType.WEB_SITE);
		
		// should be at most 1 invite
		List<String> invitationIds = searchInvitationsForIds(criteria, 1);
		if(invitationIds.size() == 1)
		{
			reviewTask = getModeratedInvitationReviewTask(invitationIds.get(0));
		}

		return reviewTask;
//		List<Invitation> invitations = searchInvitation(criteria);
//		if(invitations.size() > 1)
//		{
//			throw new AlfrescoRuntimeException("There should be only one outstanding site invitation");
//		}
//		return (invitations.size() == 0 ? null : (ModeratedInvitation)invitations.get(0));
    }
    
    private WorkflowTask getModeratedInvitationReviewTask(String invitationId)
    {
    	WorkflowTask reviewTask = null;

    	// since the invitation may have been updated e.g. invitee comments (and therefore modified date)
    	// we need to get the properties from the review task (which should be the only active
    	// task)
        List<WorkflowTask> tasks = workflowService.getTasksForWorkflowPath(invitationId);
        for(WorkflowTask task : tasks)
        {
            if(taskTypeMatches(task, WorkflowModelModeratedInvitation.WF_ACTIVITI_REVIEW_TASK))
        	{
        		reviewTask = task;
        		break;
        	}
        }

        return reviewTask;
    }

    private ModeratedInvitation getModeratedInvitation(String invitationId)
    {
        WorkflowTask reviewTask = getModeratedInvitationReviewTask(invitationId);
        ModeratedInvitation invitation = getModeratedInvitation(invitationId, reviewTask);
        return invitation;
    }
    
    private ModeratedInvitation getModeratedInvitation(String invitationId, WorkflowTask reviewTask)
    {
        ModeratedInvitation invitation = null;

        if(reviewTask != null)
        {
        	Map<QName, Serializable> properties = reviewTask.getProperties();
        	invitation = new ModeratedInvitationImpl(invitationId, properties);
        }

        return invitation;
    }

    private NominatedInvitation getNominatedInvitation(WorkflowTask startTask)
    {
        NominatedInvitation invitation = null;
        if (taskTypeMatches(startTask, WorkflowModelNominatedInvitation.WF_TASK_INVITE_TO_SITE))
        {
            Date inviteDate = startTask.getPath().getInstance().getStartDate();
            String invitationId = startTask.getPath().getInstance().getId();
            invitation = new NominatedInvitationImpl(invitationId, inviteDate, startTask.getProperties());
        }
        return invitation;
    }

    private boolean taskTypeMatches(WorkflowTask task, QName... types)
    {
        QName taskDefName = task.getDefinition().getMetadata().getName();
        return Arrays.asList(types).contains(taskDefName);
    }
    
    private WorkflowTask getStartTask(String invitationId)
    {
        validateInvitationId(invitationId);
        WorkflowTask startTask = null;
        try
        {
            startTask = workflowService.getStartTask(invitationId);
        }
        catch (WorkflowException we)
        {
            // Do nothing
        }
        if (startTask == null)
        {
            Object objs[] = { invitationId };
            throw new InvitationExceptionNotFound("invitation.error.not_found", objs);
        }
        return startTask;
    }

    /**
     * list Invitations for a specific person/invitee
     * 
     * @param invitee alfresco user id of person being invited
     */
    public List<Invitation> listPendingInvitationsForInvitee(String invitee)
    {
        InvitationSearchCriteriaImpl crit = new InvitationSearchCriteriaImpl();
        crit.setInvitationType(InvitationSearchCriteria.InvitationType.ALL);
        crit.setInvitee(invitee);
        return searchInvitation(crit);
    }
    
    public List<Invitation> listPendingInvitationsForInvitee(String invitee, Invitation.ResourceType resourceType)
    {
        InvitationSearchCriteriaImpl crit = new InvitationSearchCriteriaImpl();
        crit.setInvitationType(InvitationSearchCriteria.InvitationType.ALL);
        crit.setInvitee(invitee);
        crit.setResourceType(resourceType);
        return searchInvitation(crit);
    }

    /**
     * list Invitations for a specific resource
     * 
     * @param resourceType Invitation.ResourceType
     * @param resourceName String
     */
    public List<Invitation> listPendingInvitationsForResource(Invitation.ResourceType resourceType, String resourceName)
    {
        InvitationSearchCriteriaImpl criteria = getPendingInvitationCriteriaForResource(resourceType, resourceName);
        return searchInvitation(criteria);
    }

    /**
     * Returns search criteria to find pending invitations
     * @param resourceType Invitation.ResourceType
     * @param resourceName String
     * @return search criteria
     */
    private InvitationSearchCriteriaImpl getPendingInvitationCriteriaForResource(
            Invitation.ResourceType resourceType, String resourceName)
    {
        InvitationSearchCriteriaImpl criteria = new InvitationSearchCriteriaImpl();
        criteria.setInvitationType(InvitationSearchCriteria.InvitationType.ALL);
        criteria.setResourceType(resourceType);
        criteria.setResourceName(resourceName);
        return criteria;
    }

    /**
     * {@inheritDoc}
     * 
     * @deprecated
     */
    public List<Invitation> searchInvitation(final InvitationSearchCriteria criteria)
    {
        return searchInvitation(criteria, 200);
    }

    /**
     * {@inheritDoc}
     */
    public List<Invitation> searchInvitation(InvitationSearchCriteria criteria, int limit)
    {
        List<String> invitationIds = searchInvitationsForIds(criteria, limit);
        return invitationIds.isEmpty() ? Collections.<Invitation> emptyList() : searchInvitation(criteria, invitationIds);
    }

    private List<Invitation> searchInvitation(final InvitationSearchCriteria criteria, List<String> invitationIds)
    {
        final Map<String, WorkflowTask> taskCache = getInvitationTasks(invitationIds);
        return CollectionUtils.transform(invitationIds, new Function<String, Invitation>()
        {
            public Invitation apply(String invitationId)
            {
                WorkflowTask startTask = taskCache.get(invitationId);
                if (startTask == null)
                {
                    return null;
                }
                Invitation invitation = getInvitation(startTask);
                return invitationMatches(invitation, criteria) ? invitation : null;
            }
        });
    }

    /**
     * This is a general search invitation method returning IDs
     * 
     * @param criteria InvitationSearchCriteria
     * @param limit maximum number of IDs to return. If less than 1, there is no limit. 
     * @return the list of invitation IDs (the IDs of the invitations not the IDs of the invitation start tasks)
     */
    private List<String> searchInvitationsForIds(final InvitationSearchCriteria criteria, int limit)
    {
        List<String> invitationIds = new ArrayList<String>();
        InvitationSearchCriteria.InvitationType toSearch = criteria.getInvitationType();
        if (toSearch == InvitationSearchCriteria.InvitationType.ALL
                    || toSearch == InvitationSearchCriteria.InvitationType.NOMINATED)
        {
            for (WorkflowTask task : searchNominatedInvitations(criteria, limit))
            {
                String invitationId = task.getPath().getInstance().getId();
                invitationIds.add(invitationId);
                if (limit > 0 && invitationIds.size() >= limit)
                {
                    break;
                }
            }
        }
        if ((limit <= 0 || invitationIds.size() < limit) &&
            (toSearch == InvitationSearchCriteria.InvitationType.ALL
                     || toSearch == InvitationSearchCriteria.InvitationType.MODERATED))
        {
            for (WorkflowTask task: searchModeratedInvitations(criteria, limit))
            {
                String invitationId = task.getPath().getInstance().getId();
                invitationIds.add(invitationId);
                if (limit > 0 && invitationIds.size() >= limit)
                {
                    break;
                }
            } 
        }
        
        return invitationIds;

    }

    /**
     * Fix for ALF-2598
     * @param invitation Invitation
     * @param criteria InvitationSearchCriteria
     * @return boolean
     */
    private boolean invitationMatches(Invitation invitation, InvitationSearchCriteria criteria)
    {
        String invitee = criteria.getInvitee();
        if (invitation == null || (invitee!= null && false == invitee.equals(invitation.getInviteeUserName())))
        {
            return false;
        }
        String inviter = criteria.getInviter();
        if(inviter!= null)
        {
            if (invitation instanceof NominatedInvitation)
            {
                NominatedInvitation modInvite = (NominatedInvitation) invitation;
                if(false == inviter.equals(modInvite.getInviterUserName()))
                {
                    return false;
                }
            }
            else
            {
                return false;
            }
        }
        String resourceName= criteria.getResourceName();
        if (resourceName!= null && false == resourceName.equals(invitation.getResourceName()))
        {
            return false;
        }
        return true;
    }

    /**
     * 
     * @param criteria criteria to search by
     * @param limit maximum number of IDs to return. If less than 1, there is no limit.
     * @return list of WorkflowTask representing moderated invitations
     */
    private List<WorkflowTask> searchModeratedInvitations(InvitationSearchCriteria criteria, int limit)
    {
        long start = (logger.isDebugEnabled()) ? System.currentTimeMillis() : 0;

        WorkflowTaskQuery query = new WorkflowTaskQuery();
        query.setTaskState(WorkflowTaskState.IN_PROGRESS);
        if (limit > 0)
        {
            query.setLimit(limit);
        }
        Map<QName, Object> properties = new HashMap<QName, Object>();
        String invitee = criteria.getInvitee();
        if (invitee != null)
        {
            properties.put(WorkflowModelModeratedInvitation.WF_PROP_INVITEE_USER_NAME, invitee);
        }
        //TODO Uncomment if more than one ResourceType added.
//        ResourceType resourceType = criteria.getResourceType();
//        if (resourceType != null)
//        {
//            properties.put(WorkflowModelModeratedInvitation.WF_PROP_RESOURCE_TYPE, resourceType.toString());
//        }
        String resourceName = criteria.getResourceName();
        if (resourceName != null)
        {
            properties.put(WorkflowModelModeratedInvitation.WF_PROP_RESOURCE_NAME, resourceName);
        }
        query.setProcessCustomProps(properties);

        query.setTaskName(WorkflowModelModeratedInvitation.WF_ACTIVITI_REVIEW_TASK);

        // query for invite workflow tasks
        List<WorkflowTask> results = new ArrayList<WorkflowTask>();
        if(workflowAdminService.isEngineEnabled(ActivitiConstants.ENGINE_ID))
        {
            query.setTaskName(WorkflowModelModeratedInvitation.WF_ACTIVITI_REVIEW_TASK);
            List<WorkflowTask> activitiTasks = this.workflowService.queryTasks(query, true);
            if(activitiTasks !=null)
            {
                results.addAll(activitiTasks);
                
                if (logger.isTraceEnabled()) { logger.trace("Found " + activitiTasks.size() + " Activiti moderated invitation tasks."); }
            }
        }
        if (logger.isDebugEnabled())
        {
            logger.debug("  searchModeratedInvitations in "+ (System.currentTimeMillis()-start) + " ms");
        }
        return results;
    }

    /**
     * 
     * @param criteria
     * @param limit maximum number of IDs to return. If less than 1, there is no limit.
     * @return list of WorkflowTask representing nominated invitations
     */
    private List<WorkflowTask> searchNominatedInvitations(InvitationSearchCriteria criteria, int limit)
    {
        long start = (logger.isDebugEnabled()) ? System.currentTimeMillis() : 0;

        WorkflowTaskQuery query = new WorkflowTaskQuery();
        query.setTaskState(WorkflowTaskState.IN_PROGRESS);
        if (limit > 0)
        {
            query.setLimit(limit);
        }
        String invitee = criteria.getInvitee();
        if(invitee != null)
        {
            query.setActorId(invitee);
        }
        
        Map<QName, Object> queryProps = new HashMap<QName, Object>();
        String inviter = criteria.getInviter();
        if (inviter != null)
        {
            queryProps.put(WorkflowModelNominatedInvitation.WF_PROP_INVITER_USER_NAME, inviter);
        }
        String resourceName = criteria.getResourceName();
        if (resourceName != null)
        {
            queryProps.put(WorkflowModelNominatedInvitation.WF_PROP_RESOURCE_NAME, resourceName);
        }
        
        //TODO uncomment if more ResourceTypes are created.
//      ResourceType resourceType = criteria.getResourceType();
//      if (resourceType != null)
//      {
//          wfNominatedQueryProps.put(WorkflowModelNominatedInvitation.WF_PROP_RESOURCE_TYPE,
//                  resourceType.name());
//      }
        // set workflow task query parameters
        query.setProcessCustomProps(queryProps);

        List<WorkflowTask> results = new ArrayList<WorkflowTask>();
        if(workflowAdminService.isEngineEnabled(ActivitiConstants.ENGINE_ID))
        {
            query.setTaskName(WorkflowModelNominatedInvitation.WF_TASK_ACTIVIT_INVITE_PENDING);
            List<WorkflowTask> activitiTasks = this.workflowService.queryTasks(query, true);
            if(activitiTasks !=null)
            {
                results.addAll(activitiTasks);
                
                if (logger.isTraceEnabled()) { logger.trace("Found " + activitiTasks.size() + " Activiti nominated invitation tasks."); }
            }
        }
        if (logger.isDebugEnabled())
        {
            logger.debug("  searchNominatedInvitations in "+ (System.currentTimeMillis()-start) + " ms");
        }
        return results;
    }

    // Implementation methods below

    /**
     * Set the workflow service
     * 
     * @param workflowService WorkflowService
     */
    public void setWorkflowService(WorkflowService workflowService)
    {
        this.workflowService = workflowService;
    }

    /**
     * @param workflowAdminService the workflowAdminService to set
     */
    public void setWorkflowAdminService(WorkflowAdminService workflowAdminService)
    {
        this.workflowAdminService = workflowAdminService;
    }
    
    /**
     * @return the workflow service
     */
    public WorkflowService getWorkflowService()
    {
        return workflowService;
    }
    
    /**
     * @param actionService the actionService to set
     */
    public void setActionService(ActionService actionService)
    {
        this.actionService = actionService;
    }

    public void setPersonService(PersonService personService)
    {
        this.personService = personService;
    }

    public PersonService getPersonService()
    {
        return personService;
    }

    public void setSiteService(SiteService siteService)
    {
        this.siteService = siteService;
    }

    public SiteService getSiteService()
    {
        return siteService;
    }

    public void setAuthenticationService(MutableAuthenticationService authenticationService)
    {
        this.authenticationService = authenticationService;
    }

    public MutableAuthenticationService getAuthenticationService()
    {
        return authenticationService;
    }

    public void setUserNameGenerator(UserNameGenerator usernameGenerator)
    {
        this.usernameGenerator = usernameGenerator;
    }

    public UserNameGenerator getUserNameGenerator()
    {
        return usernameGenerator;
    }

    public void setPasswordGenerator(PasswordGenerator passwordGenerator)
    {
        this.passwordGenerator = passwordGenerator;
    }

    public PasswordGenerator getPasswordGenerator()
    {
        return passwordGenerator;
    }

    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }

    public void setNamespaceService(NamespaceService namespaceService)
    {
        this.namespaceService = namespaceService;
    }

    public NamespaceService getNamespaceService()
    {
        return namespaceService;
    }

    public void setPermissionService(PermissionService permissionService)
    {
        this.permissionService = permissionService;
    }

    public PermissionService getPermissionService()
    {
        return permissionService;
    }

    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    public NodeService getNodeService()
    {
        return nodeService;
    }

    /**
     * Creates a person for the invitee with a generated user name.
     * 
     * @param inviteeFirstName first name of invitee
     * @param inviteeLastName last name of invitee
     * @param inviteeEmail email address of invitee
     * @return invitee user name
     */
    private String createInviteePerson(String inviteeFirstName, String inviteeLastName, String inviteeEmail)
    {
        // Attempt to generate user name for invitee
        // which does not belong to an existing person
        // Tries up to MAX_NUM_INVITEE_USER_NAME_GEN_TRIES
        // at which point a web script exception is thrown
        String inviteeUserName = null;
        int i = 0;
        do
        {
            inviteeUserName = usernameGenerator.generateUserName(inviteeFirstName, inviteeLastName, inviteeEmail, i);
            i++;
        } while (this.personService.personExists(inviteeUserName) && (i < getMaxUserNameGenRetries()));

        // if after 10 tries is not able to generate a user name for a
        // person who doesn't already exist, then throw a web script exception
        if (this.personService.personExists(inviteeUserName))
        {

            logger.debug("Failed - unable to generate username for invitee.");

            Object[] objs = { inviteeFirstName, inviteeLastName, inviteeEmail };
            throw new InvitationException("invitation.invite.unable_generate_id", objs);
        }

        // create a person node for the invitee with generated invitee user name
        // and other provided person property values
        final Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
        properties.put(ContentModel.PROP_USERNAME, inviteeUserName);
        properties.put(ContentModel.PROP_FIRSTNAME, inviteeFirstName);
        properties.put(ContentModel.PROP_LASTNAME, inviteeLastName);
        properties.put(ContentModel.PROP_EMAIL, inviteeEmail);

        final String finalUserName = inviteeUserName;
        AuthenticationUtil.runAs(new RunAsWork<Object>()
        {
            @SuppressWarnings("synthetic-access")
            public Object doWork() throws Exception
            {
                NodeRef person = personService.createPerson(properties);
                //MNT-9101 Share: Cancelling an invitation for a disabled user, the user gets deleted in the process.
                nodeService.addAspect(person, ContentModel.ASPECT_ANULLABLE, null);
                permissionService.setPermission(person, finalUserName, PermissionService.ALL_PERMISSIONS, true);

                return null;
            }

        }, AuthenticationUtil.getSystemUserName());

        return inviteeUserName;
    }

    /**
     * Creates a disabled user account for the given invitee user name with a
     * generated password
     * 
     * @param inviteeUserName String
     * @return password generated for invitee user account
     */
    private String createInviteeDisabledAccount(String inviteeUserName)
    {
        // generate password using password generator
        char[] generatedPassword = passwordGenerator.generatePassword().toCharArray();

        // create disabled user account for invitee user name with generated
        // password
        this.authenticationService.createAuthentication(inviteeUserName, generatedPassword);
        this.authenticationService.setAuthenticationEnabled(inviteeUserName, false);

        return String.valueOf(generatedPassword);
    }

    /**
     * Moderated invitation implementation
     * 
     * @return the new moderated invitation
     */
    private ModeratedInvitation startModeratedInvite(String inviteeComments, String inviteeUserName,
                Invitation.ResourceType resourceType, String resourceName, String inviteeRole)
    {
        SiteInfo siteInfo = siteService.getSite(resourceName);

        if (siteService.isMember(resourceName, inviteeUserName))
        {
            if (logger.isDebugEnabled())
                logger.debug("Failed - invitee user is already a member of the site.");

            Object objs[] = { inviteeUserName, "", resourceName };
            throw new InvitationExceptionUserError("invitation.invite.already_member", objs);
        }

        String roleGroup = siteService.getSiteRoleGroup(resourceName, SiteModel.SITE_MANAGER);

        // get the workflow description
        String workflowDescription = generateWorkflowDescription(siteInfo, "invitation.moderated.workflow.description");
        
        Map<QName, Serializable> workflowProps = new HashMap<QName, Serializable>(16);
        workflowProps.put(WorkflowModel.PROP_WORKFLOW_DESCRIPTION, workflowDescription);
        workflowProps.put(WorkflowModelModeratedInvitation.ASSOC_GROUP_ASSIGNEE, roleGroup);
        workflowProps.put(WorkflowModelModeratedInvitation.WF_PROP_INVITEE_COMMENTS, inviteeComments);
        workflowProps.put(WorkflowModelModeratedInvitation.WF_PROP_INVITEE_ROLE, inviteeRole);
        workflowProps.put(WorkflowModelModeratedInvitation.WF_PROP_INVITEE_USER_NAME, inviteeUserName);
        workflowProps.put(WorkflowModelModeratedInvitation.WF_PROP_RESOURCE_NAME, resourceName);
        workflowProps.put(WorkflowModelModeratedInvitation.WF_PROP_RESOURCE_TYPE, resourceType.toString());

        // get the moderated workflow

        WorkflowDefinition wfDefinition = getWorkflowDefinition(InvitationWorkflowType.MODERATED);
        return (ModeratedInvitation) startWorkflow(wfDefinition, workflowProps);
    }

    /**
     * Starts the Invite workflow
     * 
     * @param inviteeFirstName first name of invitee
     * @param inviteeLastName last name of invitee
     * @param inviteeEmail email address of invitee
     * @param siteShortName short name of site that the invitee is being invited
     *            to by the inviter
     * @param inviteeSiteRole role under which invitee is being invited to the
     *            site by the inviter
     * @param serverPath externally accessible server address of server hosting
     *            invite web scripts
     * @param acceptUrl accept Url
     * @param rejectUrl reject Url
     */
    private NominatedInvitation startNominatedInvite(String inviteeFirstName, String inviteeLastName,
                String inviteeEmail, String inviteeUserName, Invitation.ResourceType resourceType,
                String siteShortName, String inviteeSiteRole, String serverPath, String acceptUrl, String rejectUrl)
    {

        // get the inviter user name (the name of user web script is executed
        // under)
        String inviterUserName = authenticationService.getCurrentUserName();
        boolean created = false;

        checkManagerRole(inviterUserName, resourceType, siteShortName);

        if (logger.isDebugEnabled())
        {
            logger.debug("startInvite() inviterUserName=" + inviterUserName + " inviteeUserName=" + inviteeUserName
                        + " inviteeFirstName=" + inviteeFirstName + " inviteeLastName=" + inviteeLastName
                        + " inviteeEmail=" + inviteeEmail + " siteShortName=" + siteShortName + " inviteeSiteRole="
                        + inviteeSiteRole);
        }
        //
        // if we have not explicitly been passed an existing user's user name
        // then ....
        //
        // if a person already exists who has the given invitee email address
        //
        // 1) obtain invitee user name from first person found having the
        // invitee email address, first name and last name
        // 2) handle error conditions -
        // (invitee already has an invitation in progress for the given site,
        // or he/she is already a member of the given site
        //        
        if (inviteeUserName == null || inviteeUserName.trim().length() == 0)
        {

            inviteeUserName = null;

            Set<NodeRef> peopleWithInviteeEmail = personService.getPeopleFilteredByProperty(ContentModel.PROP_EMAIL, inviteeEmail, 100);

            if (peopleWithInviteeEmail.size() > 0)
            {
                // get person already existing who has the given
                // invitee email address
                for (NodeRef personRef : peopleWithInviteeEmail)
                {
                    Serializable firstNameVal = this.getNodeService().getProperty(personRef,
                                ContentModel.PROP_FIRSTNAME);
                    Serializable lastNameVal = this.getNodeService().getProperty(personRef, ContentModel.PROP_LASTNAME);

                    String personFirstName = DefaultTypeConverter.INSTANCE.convert(String.class, firstNameVal);
                    String personLastName = DefaultTypeConverter.INSTANCE.convert(String.class, lastNameVal);

                    if (personFirstName != null && personFirstName.equalsIgnoreCase(inviteeFirstName))
                    {
                        if (personLastName != null && personLastName.equalsIgnoreCase(inviteeLastName))
                        {
                            // got a match on email, lastname, firstname
                            // get invitee user name of that person
                            Serializable userNamePropertyVal = this.getNodeService().getProperty(personRef,
                                        ContentModel.PROP_USERNAME);
                            inviteeUserName = DefaultTypeConverter.INSTANCE.convert(String.class, userNamePropertyVal);

                            if (logger.isDebugEnabled())
                            {
                                logger
                                            .debug("not explictly passed username - found matching email, resolved inviteeUserName="
                                                        + inviteeUserName);
                            }
                        }
                    }
                }
            }

            if (inviteeUserName == null)
            {
                // This shouldn't normally happen. Due to the fix for ETHREEOH-3268, the link to invite external users
                // should be disabled when the authentication chain does not allow it.
                if (!authenticationService.isAuthenticationCreationAllowed())
                {
                    throw new InvitationException("invitation.invite.authentication_chain");
                }
                // else there are no existing people who have the given invitee
                // email address so create new person
                inviteeUserName = createInviteePerson(inviteeFirstName, inviteeLastName, inviteeEmail);

                created = true;
                if (logger.isDebugEnabled())
                {
                    logger.debug("not explictly passed username - created new person, inviteeUserName="
                                + inviteeUserName);
                }
            }
        }

        /**
         * throw exception if person is already a member of the given site
         */
        if (this.siteService.isMember(siteShortName, inviteeUserName))
        {
            if (logger.isDebugEnabled())
                logger.debug("Failed - invitee user is already a member of the site.");

            Object objs[] = { inviteeUserName, inviteeEmail, siteShortName };
            throw new InvitationExceptionUserError("invitation.invite.already_member", objs);
        }   

        /**
         * throw exception if person is disabled
         */
        if (!personService.isEnabled(inviteeUserName))
        {
            if (logger.isDebugEnabled())
                logger.debug("Failed - invitee user is disabled.");
            
            Object objs[] = { inviteeUserName, inviteeEmail, siteShortName };
            throw new InvitationExceptionUserError("invitation.invite.user_disabled", objs);
        }

        //
        // If a user account does not already exist for invitee user name
        // then create a disabled user account for the invitee.
        // Hold a local reference to generated password if disabled invitee
        // account
        // is created, otherwise if a user account already exists for invitee
        // user name, then local reference to invitee password will be "null"
        //
        final String initeeUserNameFinal = inviteeUserName;
        
        String inviteePassword = created ? AuthenticationUtil.runAs(new RunAsWork<String>()
        {
            @SuppressWarnings("synthetic-access")
            public String doWork()
            {
                return createInviteeDisabledAccount(initeeUserNameFinal);
            }
        }, AuthenticationUtil.getSystemUserName()) : null;

        // create a ticket for the invite - this is used
        String inviteTicket = GUID.generate();

        //
        // Start the invite workflow with inviter, invitee and site properties
        //
        
        InvitationWorkflowType type = 
                created ? InvitationWorkflowType.NOMINATED_EXTERNAL : InvitationWorkflowType.NOMINATED;
        WorkflowDefinition wfDefinition = getWorkflowDefinition(type);

        if (logger.isDebugEnabled())
        {
           logger.debug("Using workflow definition " + wfDefinition.getId());
        }
        
        // Get invitee person NodeRef to add as assignee
        NodeRef inviteeNodeRef = personService.getPerson(inviteeUserName);
        SiteInfo siteInfo = this.siteService.getSite(siteShortName);
        String siteDescription = siteInfo.getDescription();
        if (siteDescription == null)
        {
            siteDescription = "";
        }
        else if (siteDescription.length() > 255)
        {
            siteDescription = siteDescription.substring(0, 255);
        }
        
        // get the workflow description
        String workflowDescription = generateWorkflowDescription(siteInfo, "invitation.nominated.workflow.description");
        
        // create workflow properties
        Map<QName, Serializable> workflowProps = new HashMap<QName, Serializable>(32);
        workflowProps.put(WorkflowModel.PROP_WORKFLOW_DESCRIPTION, workflowDescription);
        workflowProps.put(WorkflowModelNominatedInvitation.WF_PROP_INVITER_USER_NAME, inviterUserName);
        workflowProps.put(WorkflowModelNominatedInvitation.WF_PROP_INVITEE_USER_NAME, inviteeUserName);
        workflowProps.put(WorkflowModelNominatedInvitation.WF_PROP_INVITEE_EMAIL, inviteeEmail);
        workflowProps.put(WorkflowModel.ASSOC_ASSIGNEE, inviteeNodeRef);
        workflowProps.put(WorkflowModelNominatedInvitation.WF_PROP_INVITEE_FIRSTNAME, inviteeFirstName);
        workflowProps.put(WorkflowModelNominatedInvitation.WF_PROP_INVITEE_LASTNAME, inviteeLastName);
        workflowProps.put(WorkflowModelNominatedInvitation.WF_PROP_INVITEE_GEN_PASSWORD, inviteePassword);
        workflowProps.put(WorkflowModelNominatedInvitation.WF_PROP_RESOURCE_NAME, siteShortName);
        workflowProps.put(WorkflowModelNominatedInvitation.WF_PROP_RESOURCE_TITLE, siteInfo.getTitle());
        workflowProps.put(WorkflowModelNominatedInvitation.WF_PROP_RESOURCE_DESCRIPTION, siteDescription);
        workflowProps.put(WorkflowModelNominatedInvitation.WF_PROP_RESOURCE_TYPE, resourceType.toString());
        workflowProps.put(WorkflowModelNominatedInvitation.WF_PROP_INVITEE_ROLE, inviteeSiteRole);
        workflowProps.put(WorkflowModelNominatedInvitation.WF_PROP_SERVER_PATH, serverPath);
        workflowProps.put(WorkflowModelNominatedInvitation.WF_PROP_ACCEPT_URL, acceptUrl);
        workflowProps.put(WorkflowModelNominatedInvitation.WF_PROP_REJECT_URL, rejectUrl);
        workflowProps.put(WorkflowModelNominatedInvitation.WF_PROP_INVITE_TICKET, inviteTicket);

        return (NominatedInvitation) startWorkflow(wfDefinition, workflowProps);
    }

    @Override
    public ModeratedInvitation updateModeratedInvitation(String inviteeId, String siteShortName, String inviteeComments)
    {
    	ModeratedInvitation ret = null;

    	// find and update the review task with the new property values
    	WorkflowTask reviewTask = getModeratedInvitationReviewTask(inviteeId, siteShortName);
    	if(reviewTask == null)
    	{
            Object objs[] = { siteShortName, inviteeId };
            throw new InvitationExceptionNotFound("invitation.error.not_found_by_invitee", objs);
    	}
    	else
    	{
        	String invitationId = reviewTask.getPath().getInstance().getId();

        	if(inviteeComments != null)
        	{
	        	// update the properties on the review task
		        Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
		        properties.put(WorkflowModelModeratedInvitation.WF_PROP_INVITEE_COMMENTS, inviteeComments);
		        Date time = new Date();
		        properties.put(WorkflowModelModeratedInvitation.WF_PROP_MODIFIED_AT, time);
		        reviewTask = workflowService.updateTask(reviewTask.getId(), properties, null, null);
        	}

	    	ret = getModeratedInvitation(invitationId, reviewTask);
        }

        return ret;
    }

    private Invitation startWorkflow(WorkflowDefinition wfDefinition, Map<QName, Serializable> workflowProps)
    {
        NodeRef wfPackage = workflowService.createPackage(null);
        workflowProps.put(WorkflowModel.ASSOC_PACKAGE, wfPackage);

        // start the workflow
        WorkflowPath wfPath = this.workflowService.startWorkflow(wfDefinition.getId(), workflowProps);

        //
        // complete invite workflow start task to send out the invite email
        //

        // get the workflow tasks
        String workflowId = wfPath.getInstance().getId();
        WorkflowTask startTask = workflowService.getStartTask(workflowId);
        
        // attach empty package to start task, end it and follow with transition
        // that sends out the invite
        if (logger.isDebugEnabled())
            logger.debug("Starting Invite workflow task by attaching empty package...");

        if (logger.isDebugEnabled())
            logger.debug("Transitioning Invite workflow task...");
        try
        {
            if (startTask != null && startTask.getState() != WorkflowTaskState.COMPLETED)
            {
                workflowService.endTask(startTask.getId(), null);
            }
        }
        catch (RuntimeException err)
        {
            if (logger.isDebugEnabled())
                logger.debug("Failed - caught error during Invite workflow transition: " + err.getMessage());
            throw err;
        }

        Invitation invitation = getInvitation(startTask);
        return invitation;
    }

    /**
     * Return Activiti workflow definition unless Activiti engine is disabled.
     * @param type the workflow type
     * @return WorkflowDefinition
     */
    private WorkflowDefinition getWorkflowDefinition(InvitationWorkflowType type)
    {
        String workflowName = null;
        if (type == InvitationWorkflowType.MODERATED)
        {
            workflowName = getModeratedDefinitionName();
        }
        else if (type == InvitationWorkflowType.NOMINATED_EXTERNAL)
        {
            workflowName = getNominatedExternalDefinitionName();
        }
        else
        {
            workflowName = getNominatedDefinitionName();
        }
        WorkflowDefinition definition = workflowService.getDefinitionByName(workflowName);
        if (definition == null)
        {
            // handle workflow definition does not exist
            Object objs[] = {workflowName};
            throw new InvitationException("invitation.error.noworkflow", objs);
        }
        return definition;
    }

    private String getNominatedDefinitionName()
    {
        if(workflowAdminService.isEngineEnabled(ActivitiConstants.ENGINE_ID))
        {
            return nominatedInvitationWorkflowId;
        }
        throw new IllegalStateException("None of the Workflow engines supported by the InvitationService are currently enabled!");
    }
    
    private String getNominatedExternalDefinitionName()
    {
        if(workflowAdminService.isEngineEnabled(ActivitiConstants.ENGINE_ID))
        {
            return nominatedInvitationExternalWorkflowId;
        }
        throw new IllegalStateException("None of the Workflow engines supported by the InvitationService are currently enabled!");
    }
    
    private String getModeratedDefinitionName()
    {
        if(workflowAdminService.isEngineEnabled(ActivitiConstants.ENGINE_ID))
        {
            return moderatedInvitationWorkflowId;
        }
        throw new IllegalStateException("None of the Workflow engines supported by the InvitationService are currently enabled!");
    }

    /**
     * Check that the specified user has manager role over the resource.
     * 
     * @param userId user id
     * @throws InvitationException
     */
    private void checkManagerRole(String userId, Invitation.ResourceType resourceType, String siteShortName)
    {
        // if inviter is not the site manager then throw web script exception
        String inviterRole = this.siteService.getMembersRole(siteShortName, userId);
        if ((inviterRole == null) || (inviterRole.equals(SiteModel.SITE_MANAGER) == false))
        {

            Object objs[] = { userId, siteShortName };
            throw new InvitationExceptionForbidden("invitation.invite.not_site_manager", objs);
        }
    }

    /**
     * Validator for invitationId
     * 
     * @param invitationId String
     */
    private void validateInvitationId(String invitationId)
    {
        final String ID_SEPERATOR_REGEX = "\\$";
        String[] parts = invitationId.split(ID_SEPERATOR_REGEX);
        if (parts.length != 2)
        {
            Object objs[] = { invitationId };
            throw new InvitationExceptionUserError("invitation.error.invalid_inviteId_format", objs);
        }
    }

    private int getMaxUserNameGenRetries()
    {
        return maxUserNameGenRetries;
    }

    /**
     * NodeServicePolicies.BeforeDeleteNodePolicy Called immediatly prior to
     * deletion of a web site.
     */
    public void beforeDeleteNode(NodeRef nodeRef)
    {
        if (logger.isDebugEnabled()) { logger.debug("beforeDeleteNode"); }

        final NodeRef siteRef = nodeRef;

        // Run as system user
        AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Object>()
        {
            @SuppressWarnings("synthetic-access")
            public Object doWork() throws Exception
            {
                QName type = nodeService.getType(siteRef);
                if (dictionaryService.isSubClass(type, SiteModel.TYPE_SITE))
                {
                    // this is a web site being deleted.
                    String siteName = (String) nodeService.getProperty(siteRef, ContentModel.PROP_NAME);
                    if (siteName != null)
                    {
                        long start =0;
                        if (logger.isDebugEnabled())
                        {
                            logger.debug("Invitation service beforeDeleteNode fired " + type + ", " + siteName);
                            start = System.currentTimeMillis();
                        }
                        InvitationSearchCriteriaImpl criteria =
                            getPendingInvitationCriteriaForResource(Invitation.ResourceType.WEB_SITE, siteName);
                        List<String> invitationIds = searchInvitationsForIds(criteria, -1);
                        
                        if (logger.isDebugEnabled())
                        {
                            long end = System.currentTimeMillis();
                            logger.debug("Invitations found: " + invitationIds.size() + " in "+ ((end-start)/1000) + " seconds");
                            start = System.currentTimeMillis();
                        }
                        
                        // Create the action
                        Action action = actionService.createAction(CancelWorkflowActionExecuter.NAME);
                        action.setParameterValue(CancelWorkflowActionExecuter.PARAM_WORKFLOW_ID_LIST, (Serializable)invitationIds);
                                                
                        // Cancel the workflows asynchronously - see ALF-11872 (svn rev 32936 for details on why this is asynchronous).
                        actionService.executeAction(action, null, false, true);
                        
                        if (logger.isDebugEnabled())
                        {
                            long end = System.currentTimeMillis();
                            logger.debug("Invitation cancellations requested: " + invitationIds.size() + " in "+ (end-start) + " ms");
                        }
                    }
                }
                else if (dictionaryService.isSubClass(type, ContentModel.TYPE_PERSON))
                {
                 // this is a user being deleted.
                    String userName = (String) nodeService.getProperty(siteRef, ContentModel.PROP_USERNAME);
                    invalidateTasksByUser(userName);
                }
                return null;
            }
        }, AuthenticationUtil.SYSTEM_USER_NAME);
    }
    
    private void invalidateTasksByUser(String userName) throws AuthenticationException
    {
        List<Invitation> listForInvitee = listPendingInvitationsForInvitee(userName);
        for (Invitation inv : listForInvitee)
        {
            cancel(inv.getInviteId());
        }
    }
    
    /**
     * Generates a description for the workflow
     * 
     * @param siteInfo The site to generate a description for
     * @param messageId The resource bundle key to use for the description 
     * @return The workflow description
     */
    protected String generateWorkflowDescription(SiteInfo siteInfo, String messageId)
    {
        String siteTitle = siteInfo.getTitle();
        if (siteTitle == null || siteTitle.length() == 0)
        {
            siteTitle = siteInfo.getShortName();
        }
        
        Locale locale = (Locale) this.nodeService.getProperty(siteInfo.getNodeRef(), ContentModel.PROP_LOCALE);
        
        return I18NUtil.getMessage(messageId, locale == null ? I18NUtil.getLocale() : locale, siteTitle);
    }
    
    /**
     * @param sendEmails the sendEmails to set
     */
    public void setSendEmails(boolean sendEmails)
    {
        this.sendEmails = sendEmails;
    }
    
    /**
     * @return true if emails are sent on invite.
     */
    public boolean isSendEmails()
    {
        return sendEmails;
    }

    public void setSysAdminParams(SysAdminParams sysAdminParams)
    {
        this.sysAdminParams = sysAdminParams;
    }
    
    public void setTemplateService(TemplateService templateService)
    {
       this.templateService = templateService;
    }

    /**
     * @param messageService the messageService to set
     */
    public void setMessageService(MessageService messageService)
    {
        this.messageService = messageService;
    }
    
    /**
     * @param repositoryHelper the repositoryHelper to set
     */
    public void setRepositoryHelper(Repository repositoryHelper)
    {
        this.repositoryHelper = repositoryHelper;
    }
    
    /**
     * @param serviceRegistry the serviceRegistry to set
     */
    public void setServiceRegistry(ServiceRegistry serviceRegistry)
    {
        this.serviceRegistry = serviceRegistry;
    }
    
    @Override
    public void acceptNominatedInvitation(String siteName, final String invitee, String role, String inviter)
    {
        AuthenticationUtil.runAsSystem(new RunAsWork<Void>()
        {
            public Void doWork() throws Exception
            {
                if (authenticationService.isAuthenticationMutable(invitee))
                {
                    authenticationService.setAuthenticationEnabled(invitee, true);
                }
                return null;
            }
        });
        addSiteMembership(invitee, siteName, role, inviter, false);
    }
    
    @Override
    public void approveModeratedInvitation(String siteName, String invitee, String role, String reviewer)
    {
        addSiteMembership(invitee, siteName, role, reviewer, false);
    }
	
    /**
     * Add Invitee to Site with the site role that the inviter "started" the invite process with
     * @param invitee
     * @param siteName
     * @param role
     * @param runAsUser
     * @param siteService
     * @param overrideExisting
     */
    public void addSiteMembership(final String invitee, final String siteName, final String role, final String runAsUser, final boolean overrideExisting)
    {
        AuthenticationUtil.runAs(new RunAsWork<Void>()
        {
            public Void doWork() throws Exception
            {
                if (overrideExisting || !siteService.isMember(siteName, invitee))
                {
                    siteService.setMembership(siteName, invitee, role);
                }
                return null;
            }
            
        }, runAsUser);
    }

    @Override
    public void rejectModeratedInvitation(String siteName, String invitee, String role, String reviewer, String resourceType, String reviewComments)
    {
        // Do nothing if emails disabled.
        if (isSendEmails() == false)
        {
            return;
        }

        // send email to the invitee if possible - but don't fail the rejection if email cannot be sent
        try
        {
            // Build our model
            Map<String, Serializable> model = new HashMap<String, Serializable>(8, 1.0f);
            model.put("resourceName", siteName);
            model.put("resourceType", resourceType);
            model.put("inviteeRole", role);
            model.put("reviewComments", reviewComments);
            model.put("reviewer", reviewer);
            model.put("inviteeUserName", invitee);

            // Process the template
            // Note - because we use a classpath template, rather than a Data Dictionary
            // one, we can't have the MailActionExecutor do the template for us
            String emailMsg = templateService.processTemplate("freemarker", REJECT_TEMPLATE, model);

            // Send
            Action emailAction = actionService.createAction("mail");
            emailAction.setParameterValue(MailActionExecuter.PARAM_TO, nodeService.getProperty(personService.getPerson(invitee), ContentModel.PROP_EMAIL));
            emailAction.setParameterValue(MailActionExecuter.PARAM_FROM, nodeService.getProperty(personService.getPerson(reviewer), ContentModel.PROP_EMAIL));
            // TODO Localize this.
            emailAction.setParameterValue(MailActionExecuter.PARAM_SUBJECT, "Rejected invitation to web site:" + siteName);
            emailAction.setParameterValue(MailActionExecuter.PARAM_TEXT, emailMsg);
            emailAction.setExecuteAsynchronously(true);
            actionService.executeAction(emailAction, null);
        }
        catch (Exception e)
        {
            // Swallow exception
            logger.error("unable to send reject email", e);
        }
    }

    @Override
    public void deleteAuthenticationIfUnused(final String invitee, final String currentInviteId)
    {
        AuthenticationUtil.runAs(new RunAsWork<Object>()
        {
            public Object doWork() throws Exception
            {
                // see if there are any pending invites (invite workflow instances with invitePending task in-progress)
                // outstanding for given invitee user name
                List<Invitation> pendingInvites = listPendingInvitationsForInvitee(invitee);
                boolean invitesPending = CollectionUtils.isEmpty(pendingInvites)==false;
                if (invitesPending && pendingInvites.size() == 1)
                {
                    Invitation pendingInvite = pendingInvites.get(0);
                    if (pendingInvite.getInviteId().equals(currentInviteId))
                    {
                        invitesPending = false;
                    }
                }
                
                NodeRef person = personService.getPersonOrNull(invitee);
                
                // if invitee's user account is still disabled and there are no pending invites outstanding
                // for the invitee, then remove the account and delete the invitee's person node
                if (person != null
                        && (authenticationService.isAuthenticationMutable(invitee))
                        && (authenticationService.getAuthenticationEnabled(invitee) == false)
                        && (invitesPending == false)
                        && nodeService.hasAspect(person, ContentModel.ASPECT_ANULLABLE))
                {
                    // delete the invitee's user account
                    authenticationService.deleteAuthentication(invitee);
                    
                    // delete the invitee's person node if one exists
                    if (personService.personExists(invitee))
                    {
                        personService.deletePerson(invitee);
                    }
                }
                return null;
            }
        }, AuthenticationUtil.getSystemUserName());
    }
    
    @Override
    public void sendNominatedInvitation(String inviteId, Map<String, Object> executionVariables)
    {
        sendNominatedInvitation(
                inviteId, SendNominatedInviteDelegate.EMAIL_TEMPLATE_XPATH, SendNominatedInviteDelegate.EMAIL_SUBJECT_KEY, executionVariables);
    }
    
    @Override
    public void sendNominatedInvitation(String inviteId, String emailTemplateXpath, 
            String emailSubjectKey, Map<String, Object> executionVariables)
    {
        sendInviteEmail(inviteNominatedSender, SEND_INVITE_NOMINATED_PROP_NAMES, inviteId, emailTemplateXpath, emailSubjectKey, executionVariables);
    }

    private void sendInviteEmail(InviteSender inviteSender, List<String> invitePropNames, String inviteId, String emailTemplateXpath, String emailSubjectKey, Map<String, Object> executionVariables)
    {
        if (isSendEmails())
        {
            Map<String, String> properties = makePropertiesFromContextVariables(executionVariables, invitePropNames);

            String packageRef = getPackageRef(executionVariables);
            properties.put(InviteNominatedSender.WF_PACKAGE, packageRef);
            
            properties.put(InviteNominatedSender.WF_INSTANCE_ID, inviteId);
            
            inviteSender.sendMail(emailTemplateXpath, emailSubjectKey, properties);
        }
    }
    

    @Override
    public void sendModeratedInvitation(String inviteId, String emailTemplateXpath, String emailSubjectKey, Map<String, Object> executionVariables)
    {
        sendInviteEmail(inviteModeratedSender, SEND_INVITE_MODERATED_PROP_NAMES, inviteId, emailTemplateXpath, emailSubjectKey, executionVariables);
        
    }

    private String getPackageRef(Map<String, Object> executionVariables)
    {
        String packageName = WorkflowModel.ASSOC_PACKAGE.toPrefixString(namespaceService).replace(":", "_");
        ScriptNode packageNode = (ScriptNode) executionVariables.get(packageName);
        String packageRef = packageNode.getNodeRef().toString();
        return packageRef;
    }
    
    @Override
    public void cancelInvitation(String siteName, String invitee, String inviteId, String currentInviteId)
    {
        if (!AuthenticationUtil.isRunAsUserTheSystemUser())
        {
            String currentUserName = authenticationService.getCurrentUserName();
            String currentUserSiteRole = siteService.getMembersRole(siteName, currentUserName);
            if (SiteModel.SITE_MANAGER.equals(currentUserSiteRole)== false)
            {
                // The current user is not the site manager
                Object[] args = {currentUserName, inviteId, siteName};
                throw new InvitationExceptionForbidden(MSG_NOT_SITE_MANAGER, args);
            }
        }
        
        // Clean up invitee's user account and person node if they are not in use i.e.
        // account is still disabled and there are no pending invites outstanding for the
        // invitee
        deleteAuthenticationIfUnused(invitee, currentInviteId);
    }
    
    @SuppressWarnings("unchecked")
    private Map<String, String> makePropertiesFromContextVariables(Map<?, ?> executionVariables, Collection<String> propertyNames)
    {
        return CollectionUtils.filterKeys((Map<String, String>) executionVariables, CollectionUtils.containsFilter(propertyNames));
    }

     /**
     * Start the invitation process for a ModeratedInvitation
     *
     * @param inviteeComments why does the invitee want access to the resource ?
     * @param inviteeUserName who is to be invited
     * @param resourceType Invitation .ResourceType what resource type ?
     * @param resourceName which resource
     * @param inviteeRole which role ?
     * @param clientName which client
     */
    public ModeratedInvitation inviteModerated(String inviteeComments, String inviteeUserName,
                                               Invitation.ResourceType resourceType, String resourceName, String inviteeRole, String clientName)
    {
        if (resourceType == Invitation.ResourceType.WEB_SITE)
        {
            return startModeratedInvite(inviteeComments, inviteeUserName, resourceType, resourceName, inviteeRole, clientName);
        }
        throw new InvitationException("unknown resource type");
    }

    /**
     * Moderated invitation implementation for given client
     *
     * @return the new moderated invitation
     */
    private ModeratedInvitation startModeratedInvite(String inviteeComments, String inviteeUserName,
                                                     Invitation.ResourceType resourceType, String resourceName, String inviteeRole, String clientName)
    {
        SiteInfo siteInfo = siteService.getSite(resourceName);

        if (siteService.isMember(resourceName, inviteeUserName))
        {
            if (logger.isDebugEnabled())
                logger.debug("Failed - invitee user is already a member of the site.");

            Object objs[] = { inviteeUserName, "", resourceName };
            throw new InvitationExceptionUserError("invitation.invite.already_member", objs);
        }

        String roleGroup = siteService.getSiteRoleGroup(resourceName, SiteModel.SITE_MANAGER);

        // get the workflow description
        String workflowDescription = generateWorkflowDescription(siteInfo, "invitation.moderated.workflow.description");

        Map<QName, Serializable> workflowProps = new HashMap<QName, Serializable>(16);
        workflowProps.put(WorkflowModel.PROP_WORKFLOW_DESCRIPTION, workflowDescription);
        workflowProps.put(WorkflowModelModeratedInvitation.ASSOC_GROUP_ASSIGNEE, roleGroup);
        workflowProps.put(WorkflowModelModeratedInvitation.WF_PROP_INVITEE_COMMENTS, inviteeComments);
        workflowProps.put(WorkflowModelModeratedInvitation.WF_PROP_INVITEE_ROLE, inviteeRole);
        workflowProps.put(WorkflowModelModeratedInvitation.WF_PROP_INVITEE_USER_NAME, inviteeUserName);
        workflowProps.put(WorkflowModelModeratedInvitation.WF_PROP_RESOURCE_NAME, resourceName);
        workflowProps.put(WorkflowModelModeratedInvitation.WF_PROP_RESOURCE_TYPE, resourceType.toString());

        workflowProps.put(WorkflowModelModeratedInvitation.WF_PROP_CLIENT_NAME, clientName);
        if(clientName != null && clientAppConfig.getClient(clientName) != null)
        {
            ClientAppConfig.ClientApp client = clientAppConfig.getClient(clientName);
            workflowProps.put(WorkflowModelModeratedInvitation.WF_TEMPLATE_ASSETS_URL, client.getTemplateAssetsUrl());
            workflowProps.put(WorkflowModelModeratedInvitation.WF_WORKSPACE_URL,  client.getProperty("workspaceUrl"));
        }

        // get the moderated workflow

        WorkflowDefinition wfDefinition = getWorkflowDefinition(InvitationWorkflowType.MODERATED);
        return (ModeratedInvitation) startWorkflow(wfDefinition, workflowProps);
    }
}
