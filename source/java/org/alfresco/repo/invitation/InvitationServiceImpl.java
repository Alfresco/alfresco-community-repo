/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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

package org.alfresco.repo.invitation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.security.authentication.PasswordGenerator;
import org.alfresco.repo.security.authentication.UserNameGenerator;
import org.alfresco.repo.site.SiteModel;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.repo.workflow.activiti.ActivitiConstants;
import org.alfresco.repo.workflow.jbpm.JBPMEngine;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.invitation.Invitation;
import org.alfresco.service.cmr.invitation.InvitationException;
import org.alfresco.service.cmr.invitation.InvitationExceptionForbidden;
import org.alfresco.service.cmr.invitation.InvitationExceptionNotFound;
import org.alfresco.service.cmr.invitation.InvitationExceptionUserError;
import org.alfresco.service.cmr.invitation.InvitationSearchCriteria;
import org.alfresco.service.cmr.invitation.InvitationService;
import org.alfresco.service.cmr.invitation.ModeratedInvitation;
import org.alfresco.service.cmr.invitation.NominatedInvitation;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
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

    /**
     * Services
     */
    private WorkflowService workflowService;
    private WorkflowAdminService workflowAdminService;
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

    // maximum number of tries to generate a invitee user name which
    // does not already belong to an existing person
    public static final int MAX_NUM_INVITEE_USER_NAME_GEN_TRIES = 10;

    private int maxUserNameGenRetries = MAX_NUM_INVITEE_USER_NAME_GEN_TRIES;

    // Property determining whether emails should be sent.
    private boolean sendEmails = true;
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
     * Checks that all necessary properties and services have been provided.
     */
    public void init()
    {
        PropertyCheck.mandatory(this, "nodeService", nodeService);
        PropertyCheck.mandatory(this, "WorkflowService", workflowService);
        PropertyCheck.mandatory(this, "PersonService", personService);
        PropertyCheck.mandatory(this, "SiteService", siteService);
        PropertyCheck.mandatory(this, "AuthenticationService", authenticationService);
        PropertyCheck.mandatory(this, "PermissionService", permissionService);
        PropertyCheck.mandatory(this, "NamespaceService", namespaceService);
        PropertyCheck.mandatory(this, "NodeService", nodeService);
        PropertyCheck.mandatory(this, "UserNameGenerator", usernameGenerator);
        PropertyCheck.mandatory(this, "PasswordGenerator", passwordGenerator);
        PropertyCheck.mandatory(this, "PolicyComponent", policyComponent);

        //
        this.policyComponent.bindClassBehaviour(QName.createQName(NamespaceService.ALFRESCO_URI, "beforeDeleteNode"),
                    SiteModel.TYPE_SITE, new JavaBehaviour(this, "beforeDeleteNode"));
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
        ret.add(WorkflowModelNominatedInvitation.WORKFLOW_DEFINITION_NAME);
        ret.add(WorkflowModelNominatedInvitation.WORKFLOW_DEFINITION_NAME_ACTIVITI);
        ret.add(WorkflowModelModeratedInvitation.WORKFLOW_DEFINITION_NAME);
        ret.add(WorkflowModelModeratedInvitation.WORKFLOW_DEFINITION_NAME_ACTIVITI);
        // old deprecated invitation workflow.
        ret.add("jbpm$wf:invite");
        return ret;
    }

    /**
     * Start the invitation process for a NominatedInvitation
     * 
     * @param inviteeUserName Alfresco user name of the invitee
     * @param Invitation
     * @param ResourceType resourceType
     * @param resourceName
     * @param inviteeRole
     * @param serverPath
     * @param acceptUrl
     * @param rejectUrl
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
     * @param inviteeFirstName
     * @param inviteeLastName
     * @param inviteeEmail
     * @param inviteeUserName optional Alfresco user name of the invitee, null
     *            if not on system.
     * @param Invitation .ResourceType resourceType
     * @param resourceName
     * @param inviteeRole
     * @param serverPath
     * @param acceptUrl
     * @param rejectUrl
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
     * @param comments why does the invitee want access to the resource ?
     * @param inviteeUserName who is to be invited
     * @param Invitation .ResourceType resourceType what resource type ?
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
                WorkflowModelNominatedInvitation.WF_TASK_INVITE_PENDING, WorkflowModelNominatedInvitation.WF_TASK_ACTIVIT_INVITE_PENDING);
        return invitation;
    }

    private void endInvitation(WorkflowTask startTask, String transition, Map<QName, Serializable> properties, QName... taskTypes )
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
    
    /**
     * Moderator approves this invitation
     * 
     * @param request the request to approve
     * @param reason comments about the acceptance
     */
    public Invitation approve(String invitationId, String reason)
    {
        WorkflowTask startTask = getStartTask(invitationId);
        ModeratedInvitation invitation = getModeratedInvitation(startTask);
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
                WorkflowModelModeratedInvitation.WF_ACTIVITI_REVIEW_TASK, WorkflowModelModeratedInvitation.WF_REVIEW_TASK);
        return invitation;
    }

    /**
     * User or moderator rejects this request
     * 
     * @param invitationId
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
        ModeratedInvitation invitation = getModeratedInvitation(startTask);
        // Check rejecter is a site manager and throw and exception if not
        String rejecterUserName = this.authenticationService.getCurrentUserName();
        checkManagerRole(rejecterUserName, invitation.getResourceType(), invitation.getResourceName());

        Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
        properties.put(ContentModel.PROP_OWNER, rejecterUserName);
        properties.put(WorkflowModelModeratedInvitation.WF_PROP_REVIEW_COMMENTS, reason);

        endInvitation(startTask,
                WorkflowModelModeratedInvitation.WF_TRANSITION_REJECT,
                properties,
                WorkflowModelModeratedInvitation.WF_ACTIVITI_REVIEW_TASK, WorkflowModelModeratedInvitation.WF_REVIEW_TASK);
        return invitation;
    }

    private Invitation rejectNominatedInvitation(WorkflowTask startTask)
    {
        NominatedInvitation invitation = getNominatedInvitation(startTask);
        endInvitation(startTask,
                WorkflowModelNominatedInvitation.WF_TRANSITION_REJECT, null,
                WorkflowModelNominatedInvitation.WF_TASK_INVITE_PENDING, WorkflowModelNominatedInvitation.WF_TASK_ACTIVIT_INVITE_PENDING);
        return invitation;
    }

    /**
    * {@inheritDoc}
     */
    public Invitation cancel(String invitationId)
    {
        WorkflowTask startTask = getStartTask(invitationId);
        if(taskTypeMatches(startTask, WorkflowModelModeratedInvitation.WF_START_TASK))
        {
            return cancelModeratedInvitation(startTask);
        }
        else
        {
            return cancelNominatedInvitation(startTask);
        }
    }

    private Invitation cancelModeratedInvitation(WorkflowTask startTask)
    {
        ModeratedInvitation invitation = getModeratedInvitation(startTask);
        String currentUserName = this.authenticationService.getCurrentUserName();
        if (false == currentUserName.equals(invitation.getInviteeUserName()))
        {
            checkManagerRole(currentUserName, invitation.getResourceType(), invitation.getResourceName());
        }
        workflowService.cancelWorkflow(invitation.getInviteId());
        return invitation;
    }

    private Invitation cancelNominatedInvitation(WorkflowTask startTask)
    {
        NominatedInvitation invitation = getNominatedInvitation(startTask);
        String currentUserName = this.authenticationService.getCurrentUserName();
        if (false == currentUserName.equals(invitation.getInviterUserName()))
        {
            checkManagerRole(currentUserName, invitation.getResourceType(), invitation.getResourceName());
        }
        endInvitation(startTask, 
                WorkflowModelNominatedInvitation.WF_TRANSITION_CANCEL, null,
                WorkflowModelNominatedInvitation.WF_TASK_INVITE_PENDING, WorkflowModelNominatedInvitation.WF_TASK_ACTIVIT_INVITE_PENDING);
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
            invitation = getModeratedInvitation(startTask);
        }
        return invitation;
    }

    private ModeratedInvitation getModeratedInvitation(WorkflowTask startTask)
    {
        ModeratedInvitation invitation = null;
        if (taskTypeMatches(startTask, WorkflowModelModeratedInvitation.WF_START_TASK))
        {
            String invitationId = startTask.getPath().getInstance().getId();
            invitation = new ModeratedInvitationImpl(invitationId, startTask.getProperties());
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

    /**
     * list Invitations for a specific resource
     * 
     * @param resourceType
     * @param resourceName
     */
    public List<Invitation> listPendingInvitationsForResource(Invitation.ResourceType resourceType, String resourceName)
    {
        InvitationSearchCriteriaImpl crit = new InvitationSearchCriteriaImpl();
        crit.setInvitationType(InvitationSearchCriteria.InvitationType.ALL);
        crit.setResourceType(resourceType);
        crit.setResourceName(resourceName);
        return searchInvitation(crit);
    }

    /**
     * This is the general search invitation method
     * 
     * @param criteria
     * @return the list of invitations
     */
    public List<Invitation> searchInvitation(final InvitationSearchCriteria criteria)
    {
        List<WorkflowTask> searchResults = new ArrayList<WorkflowTask>();
        InvitationSearchCriteria.InvitationType toSearch = criteria.getInvitationType();
        if (toSearch == InvitationSearchCriteria.InvitationType.ALL
                    || toSearch == InvitationSearchCriteria.InvitationType.NOMINATED)
        {
             searchResults.addAll(searchNominatedInvitations(criteria));
        }
        if (toSearch == InvitationSearchCriteria.InvitationType.ALL
                    || toSearch == InvitationSearchCriteria.InvitationType.MODERATED)
        {
            searchResults.addAll(searchModeratedInvitations(criteria));
        }
        
        return CollectionUtils.transform(searchResults, new Function<WorkflowTask, Invitation>()
        {
            public Invitation apply(WorkflowTask task)
            {
                String invitationId = task.getPath().getInstance().getId();
                Invitation invitation = getInvitation(invitationId);
                return invitationMatches(invitation, criteria) ? invitation : null;
            }

        });
    }

    /**
     * Fix for ALF-2598
     * @param invitation
     * @param criteria
     * @return
     */
    private boolean invitationMatches(Invitation invitation, InvitationSearchCriteria criteria)
    {
        String invitee = criteria.getInvitee();
        if (invitee!= null && false == invitee.equals(invitation.getInviteeUserName()))
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
    
    private List<WorkflowTask> searchModeratedInvitations(InvitationSearchCriteria criteria)
    {
        WorkflowTaskQuery query = new WorkflowTaskQuery();
        query.setTaskState(WorkflowTaskState.IN_PROGRESS);
        
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

        query.setTaskName(WorkflowModelModeratedInvitation.WF_REVIEW_TASK);

        // query for invite workflow tasks
        List<WorkflowTask> results = new ArrayList<WorkflowTask>();
        if(workflowAdminService.isEngineEnabled(JBPMEngine.ENGINE_ID))
        {
            query.setTaskName(WorkflowModelModeratedInvitation.WF_REVIEW_TASK);
            List<WorkflowTask> jbpmTasks = this.workflowService.queryTasks(query);
            if(jbpmTasks !=null)
            {
                results.addAll(jbpmTasks);
            }
        }
        if(workflowAdminService.isEngineEnabled(ActivitiConstants.ENGINE_ID))
        {
            query.setTaskName(WorkflowModelModeratedInvitation.WF_ACTIVITI_REVIEW_TASK);
            List<WorkflowTask> jbpmTasks = this.workflowService.queryTasks(query);
            if(jbpmTasks !=null)
            {
                results.addAll(jbpmTasks);
            }
        }
        return results;
    }

    private List<WorkflowTask> searchNominatedInvitations(InvitationSearchCriteria criteria)
    {
        WorkflowTaskQuery query = new WorkflowTaskQuery();
        query.setTaskState(WorkflowTaskState.IN_PROGRESS);
        
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
        if(workflowAdminService.isEngineEnabled(JBPMEngine.ENGINE_ID))
        {
            query.setTaskName(WorkflowModelNominatedInvitation.WF_TASK_INVITE_PENDING);
            List<WorkflowTask> jbpmTasks = this.workflowService.queryTasks(query);
            if(jbpmTasks !=null)
            {
                results.addAll(jbpmTasks);
            }
        }
        if(workflowAdminService.isEngineEnabled(ActivitiConstants.ENGINE_ID))
        {
            query.setTaskName(WorkflowModelNominatedInvitation.WF_TASK_ACTIVIT_INVITE_PENDING);
            List<WorkflowTask> jbpmTasks = this.workflowService.queryTasks(query);
            if(jbpmTasks !=null)
            {
                results.addAll(jbpmTasks);
            }
        }
        return results;
    }

    // Implementation methods below

    /**
     * Set the workflow service
     * 
     * @param workflowService
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
            public Object doWork() throws Exception
            {
                NodeRef person = personService.createPerson(properties);
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
     * @param inviteeUserName
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
     * @param inviteeComments
     * @param inviteeUserName
     * @param resourceType
     * @param resourceName
     * @param inviteeRole
     * @return the new moderated invitation
     */
    private ModeratedInvitation startModeratedInvite(String inviteeComments, String inviteeUserName,
                Invitation.ResourceType resourceType, String resourceName, String inviteeRole)
    {
        // Get invitee person NodeRef to add as assignee
        NodeRef inviteeNodeRef = personService.getPerson(inviteeUserName);

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

        WorkflowDefinition wfDefinition = getWorkflowDefinition(false);
        return (ModeratedInvitation) startWorkflow(wfDefinition, workflowProps);
    }

    /**
     * Starts the Invite workflow
     * 
     * @param inviteeFirstName first name of invitee
     * @param inviteeLastNamme last name of invitee
     * @param inviteeEmail email address of invitee
     * @param siteShortName short name of site that the invitee is being invited
     *            to by the inviter
     * @param inviteeSiteRole role under which invitee is being invited to the
     *            site by the inviter
     * @param serverPath externally accessible server address of server hosting
     *            invite web scripts
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

            Set<NodeRef> peopleWithInviteeEmail = personService.getPeopleFilteredByProperty(ContentModel.PROP_EMAIL, inviteeEmail);

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

        WorkflowDefinition wfDefinition = getWorkflowDefinition(true);

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
            workflowService.endTask(startTask.getId(), null);
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
     * @param isNominated TODO
     * @return
     */
    private WorkflowDefinition getWorkflowDefinition(boolean isNominated)
    {
        String workflowName = isNominated ? getNominatedDefinitionName() : getModeratedDefinitionName();
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
            return WorkflowModelNominatedInvitation.WORKFLOW_DEFINITION_NAME_ACTIVITI;
        }
        else if(workflowAdminService.isEngineEnabled(JBPMEngine.ENGINE_ID))
        {
            return WorkflowModelNominatedInvitation.WORKFLOW_DEFINITION_NAME;
        }
        throw new IllegalStateException("None of the Workflow engines supported by teh InvitationService are currently enabled!");
    }
    
    private String getModeratedDefinitionName()
    {
        if(workflowAdminService.isEngineEnabled(ActivitiConstants.ENGINE_ID))
        {
            return WorkflowModelModeratedInvitation.WORKFLOW_DEFINITION_NAME_ACTIVITI;
        }
        else if(workflowAdminService.isEngineEnabled(JBPMEngine.ENGINE_ID))
        {
            return WorkflowModelModeratedInvitation.WORKFLOW_DEFINITION_NAME;
        }
        throw new IllegalStateException("None of the Workflow engines supported by teh InvitationService are currently enabled!");
    }

    /**
     * Check that the specified user has manager role over the resource.
     * 
     * @param userId
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
     * @param invitationId
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
        logger.debug("beforeDeleteNode");

        final NodeRef siteRef = nodeRef;

        // Run as system user
        AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Object>()
        {
            public Object doWork() throws Exception
            {
                QName type = nodeService.getType(siteRef);
                if (dictionaryService.isSubClass(type, SiteModel.TYPE_SITE))
                {
                    // this is a web site being deleted.
                    String siteName = (String) nodeService.getProperty(siteRef, ContentModel.PROP_NAME);
                    if (siteName != null)
                    {
                        logger.debug("Invitation service delete node fired " + type + ", " + siteName);
                        List<Invitation> invitations = listPendingInvitationsForResource(
                                    Invitation.ResourceType.WEB_SITE, siteName);
                        for (Invitation invitation : invitations)
                        {
                            logger.debug("cancel workflow " + invitation.getInviteId());
                            workflowService.cancelWorkflow(invitation.getInviteId());
                        }
                    }
                }
                return null;
            }
        }, AuthenticationUtil.SYSTEM_USER_NAME);
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
        
        return I18NUtil.getMessage(messageId, siteTitle);
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
}
