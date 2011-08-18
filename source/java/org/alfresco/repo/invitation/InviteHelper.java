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
import static org.alfresco.repo.invitation.WorkflowModelNominatedInvitation.wfVarWorkflowInstanceId;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.action.executer.MailActionExecuter;
import org.alfresco.repo.i18n.MessageService;
import org.alfresco.repo.invitation.site.InviteInfo;
import org.alfresco.repo.invitation.site.InviteSender;
import org.alfresco.repo.jscript.ScriptNode;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.site.SiteModel;
import org.alfresco.repo.template.TemplateNode;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.invitation.InvitationExceptionForbidden;
import org.alfresco.service.cmr.invitation.InvitationService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.TemplateService;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.cmr.workflow.WorkflowTaskQuery;
import org.alfresco.service.cmr.workflow.WorkflowTaskState;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;

/**
 * Helper class to house utility methods common to 
 * more than one Invite Service Web Script
 * 
 *  @author Nick Smith
 */
public class InviteHelper implements InitializingBean
{   
    public final static String NAME = "InviteHelper";
    private final static String MSG_NOT_SITE_MANAGER = "invitation.cancel.not_site_manager";
    private static final String REJECT_TEMPLATE = "/alfresco/bootstrap/invite/moderated-reject-email.ftl";
    
    private static final Log logger = LogFactory.getLog(ModeratedActionReject.class);

    private static final Collection<String> sendInvitePropertyNames = Arrays.asList(wfVarInviteeUserName,//
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
            InviteSender.WF_INSTANCE_ID);

    private Repository repositoryHelper;
    private ServiceRegistry serviceRegistry;
    
    private ActionService actionService;
    private InvitationService invitationService;
    private MutableAuthenticationService authenticationService;
    private MessageService messageService;
    private NamespaceService namespaceService;
    private PersonService personService;
    private SiteService siteService;
    private TemplateService templateService;
    private WorkflowService workflowService;

    private InviteSender inviteSender;

    public void afterPropertiesSet()
    {
        this.actionService = serviceRegistry.getActionService();
        this.authenticationService =serviceRegistry.getAuthenticationService();
        this.invitationService = serviceRegistry.getInvitationService();
        this.namespaceService = serviceRegistry.getNamespaceService();
        this.personService = serviceRegistry.getPersonService();
        this.siteService = serviceRegistry.getSiteService();
        this.templateService = serviceRegistry.getTemplateService();
        this.workflowService = serviceRegistry.getWorkflowService();
        this.inviteSender = new InviteSender(serviceRegistry, repositoryHelper, messageService);
    }

    public void acceptNominatedInvitation(Map<String, Object> executionVariables)
    {
        final String invitee = (String) executionVariables.get(WorkflowModelNominatedInvitation.wfVarInviteeUserName);
        String siteShortName = (String) executionVariables.get(WorkflowModelNominatedInvitation.wfVarResourceName);
        String inviter = (String) executionVariables.get(WorkflowModelNominatedInvitation.wfVarInviterUserName);
        String role = (String) executionVariables.get(WorkflowModelNominatedInvitation.wfVarRole);
        
        AuthenticationUtil.runAsSystem(new RunAsWork<Void>()
        {
            public Void doWork() throws Exception
            {
                if(false==authenticationService.getAuthenticationEnabled(invitee))
                {
                    authenticationService.setAuthenticationEnabled(invitee, true);
                }
                return null;
            }
        });
        addSiteMembership(invitee, siteShortName, role, inviter, false);
    }
    
    /**
     * Find an invite start task by the given task id.
     * 
     * @return a WorkflowTask or null if not found.
     */
    public WorkflowTask findInviteStartTask(String inviteId)
    {
        // create workflow task query
        WorkflowTaskQuery wfTaskQuery = new WorkflowTaskQuery();
        
        wfTaskQuery.setProcessId(inviteId);
        
        // set process name to "wf:invite" so that only tasks associated with
        // invite workflow instances are returned by query
        wfTaskQuery.setProcessName(WorkflowModelNominatedInvitation.WF_PROCESS_INVITE);

        // filter to find only the invite start task
        wfTaskQuery.setTaskState(WorkflowTaskState.COMPLETED);
        wfTaskQuery.setTaskName(WorkflowModelNominatedInvitation.WF_INVITE_TASK_INVITE_TO_SITE);

        // query for invite workflow task associate
        List<WorkflowTask> inviteStartTasks = workflowService
                .queryTasks(wfTaskQuery);

        // should also be 0 or 1
        if (inviteStartTasks.size() < 1)
        {
            return null;
        }
        else
        {
            return inviteStartTasks.get(0);
        }
    }
    
    /**
     * Find invitePending tasks (in-progress) by the given invitee user name
     * 
     * @return a list of workflow tasks
     */
    public List<WorkflowTask> findInvitePendingTasks(String inviteeUserName)
    {
            // create workflow task query
            WorkflowTaskQuery wfTaskQuery = new WorkflowTaskQuery();
            
            // set process name to "wf:invite" so that only tasks associated with
            // invite workflow instances are returned by query
            wfTaskQuery.setProcessName(WorkflowModelNominatedInvitation.WF_PROCESS_INVITE);
            
            // set query to only pick up invite workflow instances
            // associated with the given invitee user name
            Map<QName, Object> processCustomProps = new HashMap<QName, Object>(1, 1.0f);
            processCustomProps.put(WorkflowModelNominatedInvitation.WF_PROP_INVITEE_USER_NAME, inviteeUserName);
            wfTaskQuery.setProcessCustomProps(processCustomProps);

            // set query to only pick up in-progress invite pending tasks 
            wfTaskQuery.setTaskState(WorkflowTaskState.IN_PROGRESS);
            wfTaskQuery.setTaskName(WorkflowModelNominatedInvitation.WF_INVITE_TASK_INVITE_PENDING);

            // query for invite workflow task associate
            List<WorkflowTask> inviteStartTasks = workflowService
                    .queryTasks(wfTaskQuery);

            return inviteStartTasks;
    }
    
    /**
     * Returns an InviteInfo instance for the given startInvite task
     * (used for rendering the response).
     * 
     * @param startInviteTask startInvite task to get invite info properties from
     * @param serviceRegistry service registry instance
     * @return InviteInfo instance containing invite information
     */
    public InviteInfo getPendingInviteInfo(WorkflowTask startInviteTask)
    {
        Map<QName, Serializable> taskProps = startInviteTask.getProperties();
        // get the inviter, invitee, role and site short name
        String inviterUserName = (String) taskProps.get(WorkflowModelNominatedInvitation.WF_PROP_INVITER_USER_NAME);
        String inviteeUserName = (String) taskProps.get(WorkflowModelNominatedInvitation.WF_PROP_INVITEE_USER_NAME);
        String role = (String) taskProps.get(WorkflowModelNominatedInvitation.WF_PROP_INVITEE_ROLE);
        String siteShortName = (String) taskProps.get(WorkflowModelNominatedInvitation.WF_PROP_RESOURCE_NAME);

        // get the site info
        SiteInfo siteInfo = siteService.getSite(siteShortName);
        
        // get workflow instance id (associated with workflow task) to place
        // as "inviteId" onto model
        String workflowId = startInviteTask.getPath().getInstance().getId();

        // set the invite start date to the time the workflow instance
        // (associated with the task) was started
        Date sentInviteDate = startInviteTask.getPath().getInstance().getStartDate();
        
        // TODO: glen johnson at alfresco com - as this web script only returns
        // pending invites, this is hard coded to "pending" for now
        String invitationStatus = InviteInfo.INVITATION_STATUS_PENDING;
        
        // fetch the person node for the inviter
        NodeRef inviterRef = personService.getPerson(inviterUserName);
        TemplateNode inviterPerson = inviterRef == null ? null : new TemplateNode(inviterRef, serviceRegistry, null); 
        
        // fetch the person node for the invitee
        NodeRef inviteeRef = personService.getPerson(inviteeUserName);
        TemplateNode inviteePerson = inviteeRef == null ? null : new TemplateNode(inviteeRef, serviceRegistry, null);
        // create and return the invite info
        return new InviteInfo(invitationStatus,
                inviterUserName, inviterPerson,
                inviteeUserName, inviteePerson,
                role, siteShortName, siteInfo,
                sentInviteDate, workflowId);
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
    
    /**
     * Clean up invitee user account and person node when no longer in use.
     * They are deemed to no longer be in use when the invitee user account
     * is still disabled and there are no outstanding pending invites for that invitee.
     * 
     * @param inviteeUserName
     * @param authenticationservice
     * @param personService
     * @param workflowService
     */
    public void deleteAuthenticationIfUnused(final String inviteeUserName)
    {
        AuthenticationUtil.runAs(new RunAsWork<Object>()
        {
            public Object doWork() throws Exception
            {
                // see if there are any pending invites (invite workflow instances with invitePending task in-progress)
                // outstanding for given invitee user name
                List<WorkflowTask> pendingTasks = findInvitePendingTasks(inviteeUserName);
                boolean invitesPending = (pendingTasks != null) && (pendingTasks.size() > 0);
                
                // if invitee's user account is still disabled and there are no pending invites outstanding
                // for the invitee, then remove the account and delete the invitee's person node
                if ((authenticationService.authenticationExists(inviteeUserName))
                        && (authenticationService.getAuthenticationEnabled(inviteeUserName) == false)
                        && (invitesPending == false))
                {
                    // delete the invitee's user account
                    authenticationService.deleteAuthentication(inviteeUserName);
                    
                    // delete the invitee's person node if one exists
                    if (personService.personExists(inviteeUserName))
                    {
                        personService.deletePerson(inviteeUserName);
                    }
                }
                return null;
            }
        }, AuthenticationUtil.getSystemUserName());
    }
    
    /**
     * Complete the specified Invite Workflow Task for the invite workflow 
     * instance associated with the given invite ID, and follow the given
     * transition upon completing the task
     * 
     * @param inviteId the invite ID of the invite workflow instance for which
     *          we want to complete the given task
     * @param fullTaskName qualified name of invite workflow task to complete
     * @param transitionId the task transition to take on completion of 
     *          the task (or null, for the default transition)
     * @param workflowService TODO
     */
    public static void completeInviteTask(String inviteId, QName fullTaskName, String transitionId, WorkflowService workflowService)
    {
        // create workflow task query
        WorkflowTaskQuery wfTaskQuery = new WorkflowTaskQuery();
        
        // set the given invite ID as the workflow process ID in the workflow query
        wfTaskQuery.setProcessId(inviteId);

        // find incomplete invite workflow tasks with given task name 
        wfTaskQuery.setActive(Boolean.TRUE);
        wfTaskQuery.setTaskState(WorkflowTaskState.IN_PROGRESS);
        wfTaskQuery.setTaskName(fullTaskName);

        // set process name to "wf:invite" so that only
        // invite workflow instances are considered by this query
        wfTaskQuery.setProcessName(WorkflowModelNominatedInvitation.WF_PROCESS_INVITE);

        // query for invite workflow tasks with the constructed query
        List<WorkflowTask> wf_invite_tasks = workflowService
                .queryTasks(wfTaskQuery);
        
        // end all tasks found with this name 
        for (WorkflowTask workflowTask : wf_invite_tasks)
        {
            workflowService.endTask(workflowTask.getId(), transitionId);
        }
    }
    
    /**
     * @param executionVariables
     */
    public void cancelInvitation(Map<String, Object> executionVariables)
    {
        // Get the invitee user name and site short name variables off the execution context
        String inviteeUserName = (String) executionVariables.get(wfVarInviteeUserName);
        String siteShortName = (String) executionVariables.get(wfVarResourceName);
        
        String currentUserName = authenticationService.getCurrentUserName();
        String currentUserSiteRole = siteService.getMembersRole(siteShortName, currentUserName);
        if (SiteModel.SITE_MANAGER.equals(currentUserSiteRole)== false)
        {
            // The current user is not the site manager
            String inviteId = (String) executionVariables.get(wfVarWorkflowInstanceId);
            Object[] args = {currentUserName, inviteId, siteShortName};
            throw new InvitationExceptionForbidden(MSG_NOT_SITE_MANAGER, args);
        }
        
        // Clean up invitee's user account and person node if they are not in use i.e.
        // account is still disabled and there are no pending invites outstanding for the
        // invitee
        deleteAuthenticationIfUnused(inviteeUserName);
    }
    
    public  void sendNominatedInvitation(Map<String, Object> executionVariables)
    {
        if(invitationService.isSendEmails())
        {
            Map<String, String> properties = makePropertiesFromContextVariables(executionVariables, sendInvitePropertyNames);

            String packageName = WorkflowModel.ASSOC_PACKAGE.toPrefixString(namespaceService).replace(":", "_");
            ScriptNode packageNode = (ScriptNode) executionVariables.get(packageName);
            String packageRef = packageNode.getNodeRef().toString();
            properties.put(InviteSender.WF_PACKAGE, packageRef);
            
            String instanceName=WorkflowModel.PROP_WORKFLOW_INSTANCE_ID.toPrefixString(namespaceService).replace(":", "_");
            String instanceId = (String) executionVariables.get(instanceName);
            properties.put(InviteSender.WF_INSTANCE_ID, instanceId);
            
            inviteSender.sendMail(properties);
        }
    }
    
    public void approveModeratedInvitation(Map<String, Object> executionVariables)
    {
        String siteName = (String)executionVariables.get(WorkflowModelModeratedInvitation.wfVarResourceName);
        String invitee= (String)executionVariables.get(WorkflowModelModeratedInvitation.wfVarInviteeUserName);
        String role = (String)executionVariables.get(WorkflowModelModeratedInvitation.wfVarInviteeRole);
        String reviewer = (String)executionVariables.get(WorkflowModelModeratedInvitation.wfVarReviewer);
        
        // Add invitee to the site
        addSiteMembership(invitee, siteName, role, reviewer, true);
    }
    
    @SuppressWarnings("unchecked")
    private Map<String, String> makePropertiesFromContextVariables(Map<?, ?> executionVariables, Collection<String> propertyNames)
    {
        return CollectionUtils.filterKeys((Map<String, String>) executionVariables, CollectionUtils.containsFilter(propertyNames));
    }

    /**
     * @param vars
     */
    public void rejectModeratedInvitation(Map<String, Object> vars)
    {
        //Do nothing if emails disabled.
        if(invitationService.isSendEmails() == false)
        {
            return;
        }
        String resourceType = (String)vars.get(WorkflowModelModeratedInvitation.wfVarResourceType);
        String resourceName = (String)vars.get(WorkflowModelModeratedInvitation.wfVarResourceName);
        String inviteeUserName = (String)vars.get(WorkflowModelModeratedInvitation.wfVarInviteeUserName);
        String inviteeRole = (String)vars.get(WorkflowModelModeratedInvitation.wfVarInviteeRole);
        String reviewer = (String)vars.get(WorkflowModelModeratedInvitation.wfVarReviewer);
        String reviewComments = (String)vars.get(WorkflowModelModeratedInvitation.wfVarReviewComments);
        
        // send email to the invitee if possible - but don't fail the rejection if email cannot be sent
        try 
        {
            // Build our model
            Map<String, Serializable> model = new HashMap<String, Serializable>(8, 1.0f);
            model.put("resourceName", resourceName);
            model.put("resourceType", resourceType);
            model.put("inviteeRole", inviteeRole);
            model.put("reviewComments", reviewComments);
            model.put("reviewer", reviewer);
            model.put("inviteeUserName", inviteeUserName);
            
            // Process the template
            // Note - because we use a classpath template, rather than a Data Dictionary
            //        one, we can't have the MailActionExecutor do the template for us
            String emailMsg = templateService.processTemplate("freemarker", REJECT_TEMPLATE,  model);
                    
            // Send
            Action emailAction = actionService.createAction("mail");
            emailAction.setParameterValue(MailActionExecuter.PARAM_TO, inviteeUserName);
            emailAction.setParameterValue(MailActionExecuter.PARAM_FROM, reviewer);
            //TODO Localize this.
            emailAction.setParameterValue(MailActionExecuter.PARAM_SUBJECT, "Rejected invitation to web site:" + resourceName);
            emailAction.setParameterValue(MailActionExecuter.PARAM_TEXT, emailMsg);
            emailAction.setExecuteAsynchronously(true);
            actionService.executeAction(emailAction, null);
        }
        catch(Exception e)
        {
            // Swallow exception
            logger.error("unable to send reject email", e);
        }        
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

 }