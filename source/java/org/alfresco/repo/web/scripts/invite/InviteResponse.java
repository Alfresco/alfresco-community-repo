/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have received a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.web.scripts.invite;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.MutableAuthenticationDao;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.site.SiteService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.cmr.workflow.WorkflowTaskQuery;
import org.alfresco.service.cmr.workflow.WorkflowTaskState;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.scripts.DeclarativeWebScript;
import org.alfresco.web.scripts.Status;
import org.alfresco.web.scripts.WebScriptException;
import org.alfresco.web.scripts.WebScriptRequest;

/**
 * Web Script invoked by Invitee to either accept (response='accept') an
 * invitation from a Site Manager (Inviter) to join a Site as a Site
 * Collaborator, or to reject (response='reject') an invitation that has already
 * been sent out
 * 
 * @author glen dot johnson at alfresco dot com
 */
public class InviteResponse extends DeclarativeWebScript
{
    /**
     * Inner class providing functionality (which needs to run under admin
     * rights) to set membership of invitee (given as invitee user name) to site
     * (given as site short name) as given site role
     */
    private class SetSiteMembershipWorker implements
            AuthenticationUtil.RunAsWork<Boolean>
    {
        private String siteShortName;
        private String inviteeUserName;
        private String siteRole;

        private SetSiteMembershipWorker(String siteShortName,
                String inviteeUserName, String siteRole)
        {
            this.siteShortName = siteShortName;
            this.inviteeUserName = inviteeUserName;
            this.siteRole = siteRole;
        }

        /**
         * Does the work to set the site membership
         */
        public Boolean doWork() throws Exception
        {
            InviteResponse.this.siteService.setMembership(this.siteShortName,
                    this.inviteeUserName, this.siteRole);

            return Boolean.TRUE;
        }
    }

    private static final String RESPONSE_ACCEPT = "accept";
    private static final String RESPONSE_REJECT = "reject";
    private static final String WF_TASK_ACCEPT_INVITE = "wf:acceptInviteTask";
    private static final String WF_TASK_REJECT_INVITE = "wf:rejectInviteTask";
    private static final String WF_TASK_INVITE_PENDING = "wf:invitePendingTask";
    private static final String WF_TRANSITION_ACCEPT = "accept";
    private static final String WF_TRANSITION_REJECT = "reject";
    private static final String WF_TRANSITION_ACCEPT_INVITE_END = "end";
    private static final String WF_TRANSITION_REJECT_INVITE_END = "end";
    private static final String MODEL_PROP_KEY_RESPONSE = "response";
    private static final String MODEL_PROP_KEY_SITE_SHORT_NAME = "siteShortName";
    private static final String USER_ADMIN = "admin";

    // properties for services
    private WorkflowService workflowService;
    private MutableAuthenticationDao mutableAuthenticationDao;
    private SiteService siteService;
    private PersonService personService;
    private NamespaceService namespaceService;

    /**
     * Sets the workflow service property
     * 
     * @param workflowService
     *            the workflow service instance assign to the property
     */
    public void setWorkflowService(WorkflowService workflowService)
    {
        this.workflowService = workflowService;
    }

    /**
     * Sets the mutableAuthenticationDao service property
     * 
     * @param mutableAuthenticationDao
     *            the MutableAuthenticationDao service to set
     */
    public void setMutableAuthenticationDao(
            MutableAuthenticationDao mutableAuthenticationDao)
    {
        this.mutableAuthenticationDao = mutableAuthenticationDao;
    }

    /**
     * Sets the siteService property
     * 
     * @param siteService
     *            the siteService to set
     */
    public void setSiteService(SiteService siteService)
    {
        this.siteService = siteService;
    }

    /**
     * Sets the personService property
     * 
     * @param personService
     *            the person service to set
     */
    public void setPersonService(PersonService personService)
    {
        this.personService = personService;
    }
    
    /**
     * Sets the namespaceService property
     * 
     * @param namespaceService
     *            the namespace service to set
     */
    public void setNamespaceService(NamespaceService namespaceService)
    {
        this.namespaceService = namespaceService;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.alfresco.web.scripts.DeclarativeWebScript#executeImpl(org.alfresco
     * .web.scripts.WebScriptRequest,
     * org.alfresco.web.scripts.WebScriptResponse)
     */
    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req,
            Status status)
    {
        // initialise model to pass on for template to render
        Map<String, Object> model = new HashMap<String, Object>();

        // get the URL parameter values
        String inviteId = req.getParameter("inviteId");
        String inviteeUserName = req.getParameter("inviteeUserName");
        String siteShortName = req.getParameter("siteShortName");

        // get the invite response value
        String response = req.getExtensionPath();

        // check that response has been provided
        if ((response == null) || (response.length() == 0))
        {
            // handle response not provided
            throw new WebScriptException(Status.STATUS_BAD_REQUEST,
                    "response has not been provided as part of URL.");
        }
        // check that invite id URL parameter has been provided
        else if ((inviteId == null) || (inviteId.length() == 0))
        {
            // handle invite id not provided
            throw new WebScriptException(Status.STATUS_BAD_REQUEST,
                    "invite id parameter has not been provided in the URL.");
        }
        // check that inviteeUserName URL parameter has been provided
        else if ((inviteeUserName == null) || (inviteeUserName.length() == 0))
        {
            // handle inviteeUserName not provided
            throw new WebScriptException(Status.STATUS_BAD_REQUEST,
                    "inviteeUserName parameter has not been provided in the URL.");
        }
        // check that siteShortName URL parameter has been provided
        else if ((siteShortName == null) || (siteShortName.length() == 0))
        {
            // handle siteShortName not provided
            throw new WebScriptException(Status.STATUS_BAD_REQUEST,
                    "siteShortName parameter has not been provided in the URL.");
        } else
        {
            // process response
            if (response.equals(RESPONSE_ACCEPT))
            {
                acceptInvite(model, inviteId, inviteeUserName, siteShortName);
            } else if (response.equals(RESPONSE_REJECT))
            {
                rejectInvite(model, inviteId, inviteeUserName, siteShortName);
            } else
            {
                /* handle unrecognised response */
                throw new WebScriptException(Status.STATUS_BAD_REQUEST,
                        "response, " + response
                                + ", provided in URL has not been recognised.");
            }
        }

        return model;
    }

    /**
     * Processes 'accept' response from invitee
     * 
     * @param model
     *            model to add objects to, which will be passed to the template
     *            for rendering
     * @param inviteId
     *            ID of invite
     * @param inviteeUserName
     *            user name of invitee
     * @param siteShortName
     *            short name of site for which invitee is accepting
     *            invitation to join
     */
    private void acceptInvite(Map<String, Object> model, String inviteId,
            String inviteeUserName, String siteShortName)
    {
        // complete the wf:invitePendingTask task because the invitation has been accepted
        completeInviteTask(QName.createQName(WF_TASK_INVITE_PENDING, this.namespaceService), WF_TRANSITION_ACCEPT);
        
        // TODO glen dot johnson at alfresco dot com - farm the code that follows (up until adding properties onto
        // the model) out into workflow action class that gets run when task wf:acceptInviteTask
        // is completed by this web script

        // if there is already a user account for the invitee and that account
        // is disabled, then enable the account because he/she has accepted the
        // site invitation
        if ((this.mutableAuthenticationDao.userExists(inviteeUserName))
            && (this.mutableAuthenticationDao.getEnabled(inviteeUserName) == false))
        {
            this.mutableAuthenticationDao.setEnabled(inviteeUserName, true);
        }
        
        // retrieve the site role with which the invitee was invited to the site
        String inviteeSiteRole = getInviteeSiteRoleFromInvite(inviteId);
        
        // add Invitee to Site with the site role that the inviter "started" the invite process with
        RunAsWork<Boolean> setSiteMembershipWorker = new InviteResponse.SetSiteMembershipWorker(
                siteShortName, inviteeUserName, inviteeSiteRole);
        AuthenticationUtil.runAs(setSiteMembershipWorker, USER_ADMIN);
        
        // complete the wf:acceptInviteTask because the operations that need to be performed
        // when the invitee has accepted the invitation have now been performed (code block
        // starting from above where wf:invitePendingTask is completed, up to here). This code 
        // block will soon be farmed out into a workflow action which gets executed when 
        // wf:acceptInviteTask gets completed
        completeInviteTask(QName.createQName(WF_TASK_ACCEPT_INVITE, this.namespaceService), WF_TRANSITION_ACCEPT_INVITE_END);

        // add model properties for template to render
        model.put(MODEL_PROP_KEY_RESPONSE, RESPONSE_ACCEPT);
        model.put(MODEL_PROP_KEY_SITE_SHORT_NAME, siteShortName);
    }

    /**
     * Processes 'reject' invite response from invitee
     * 
     * @param model
     *            model to add objects to, which will be passed to the template
     *            for rendering
     * @param inviteId
     *            ID of invite
     * @param inviteeUserName
     *            user name of invitee
     * @param siteShortName
     *            short name of site for which invitee is rejecting
     *            invitation to join
     */
    private void rejectInvite(Map<String, Object> model, String inviteId,
            String inviteeUserName, String siteShortName)
    {
        // complete the wf:invitePendingTask task because the invitation has been accepted
        completeInviteTask(QName.createQName(WF_TASK_INVITE_PENDING, this.namespaceService), WF_TRANSITION_REJECT);
        
        // TODO glen dot johnson at alfresco dot com - farm the code that follows (up until adding properties onto
        // the model) out into workflow action class that gets run when task wf:rejectInviteTask
        // is completed by this web script

        // if invitee's user account is still disabled then remove the account and 
        // delete the invitee's person node
        if ((this.mutableAuthenticationDao.userExists(inviteeUserName))
                && (this.mutableAuthenticationDao.getEnabled(inviteeUserName) == false))
        {
            // delete the invitee's user account
            this.mutableAuthenticationDao.deleteUser(inviteeUserName);
            
            // delete the invitee's person node if one exists
            if (this.personService.personExists(inviteeUserName))
            {
                this.personService.deletePerson(inviteeUserName);
            }
        }

        // complete the wf:rejectInviteTask because the operations that need to be performed
        // when the invitee has rejected the invitation have now been performed (code block
        // starting from above where wf:invitePendingTask is completed, up to here). This code 
        // block will soon be farmed out into a workflow action which gets executed when 
        // wf:rejectInviteTask gets completed
        completeInviteTask(QName.createQName(WF_TASK_REJECT_INVITE, this.namespaceService), WF_TRANSITION_REJECT_INVITE_END);

        // add model properties for template to render
        model.put(MODEL_PROP_KEY_RESPONSE, RESPONSE_REJECT);
        model.put(MODEL_PROP_KEY_SITE_SHORT_NAME, siteShortName);
    }
    
    /**
     * Complete the specified Invite Workflow Task and follow the given
     * transition upon completing the task
     * 
     * @param fullTaskName qualified name of invite workflow task to complete
     * @param transitionId the task transition to take on completion of 
     *          the task (or null, for the default transition)
     */
    private void completeInviteTask(QName fullTaskName, String transitionId)
    {
        // create workflow task query
        WorkflowTaskQuery wfTaskQuery = new WorkflowTaskQuery();

        // find incomplete invite workflow tasks with given task name 
        wfTaskQuery.setActive(Boolean.TRUE);
        wfTaskQuery.setTaskState(WorkflowTaskState.IN_PROGRESS);
        wfTaskQuery.setTaskName(fullTaskName);

        // set process name to "wf:invite" so that only
        // invite workflow instances are considered by this query
        wfTaskQuery.setProcessName(QName.createQName("wf:invite", this.namespaceService));

        // query for invite workflow tasks with the constructed query
        List<WorkflowTask> wf_invite_tasks = this.workflowService
                .queryTasks(wfTaskQuery);
        
        // end all tasks found with this name 
        for (WorkflowTask workflowTask : wf_invite_tasks)
        {
            this.workflowService.endTask(workflowTask.id, transitionId);
        }
    }
    
    /**
     * Gets the invitee site role from the invite
     * workflow instance associated with the given invite ID.
     * i.e. if the inviter 'starts' an invite (which is allocated some invite ID
     * '12345' when it is processed), and that invite is requesting an invitee to
     * to join some site under a given site role, then that site role is returned 
     * by this method when invite ID '12345' is passed in.
     * 
     * @param inviteId the ID of the invitation (invite workflow instance)
     *          from which to retrieve the invitee site role
     * @return the site role under which the invitee was invited to 
     *          join the site. Returns <pre>null</pre> if no invite
     *          workflow instance was found matching the given invite ID
     */
    private String getInviteeSiteRoleFromInvite(String inviteId)
    {
        // create workflow task query
        WorkflowTaskQuery wfTaskQuery = new WorkflowTaskQuery();

        wfTaskQuery.setProcessId(inviteId);
        
        // set process name to "wf:invite" so that only tasks associated with
        // invite workflow instances are returned by query
        wfTaskQuery.setProcessName(QName.createQName("wf:invite", this.namespaceService));

        // pick up the start task because it has the "wf:inviteeSiteRole" property set with the
        // site role value that we want to retrieve 
        wfTaskQuery.setTaskState(WorkflowTaskState.COMPLETED);
        wfTaskQuery.setTaskName(QName.createQName(Invite.WF_INVITE_TASK_INVITE_TO_SITE, this.namespaceService));

        // query for invite workflow task associate
        List<WorkflowTask> inviteStartTasks = this.workflowService
                .queryTasks(wfTaskQuery);

        // if no results were returned for given inviteID, then return
        // site role as null
        if (inviteStartTasks.size() == 0)
        {
            return null;
        }
        else
        {
            // there should be only one start task returned for the given invite ID
            // so just take the first one in the list
            WorkflowTask inviteStartTask = inviteStartTasks.get(0);
            
            String inviteeSiteRole = (String) inviteStartTask.properties.get(
                    QName.createQName(Invite.WF_PROP_INVITEE_SITE_ROLE, this.namespaceService));
            
            return inviteeSiteRole;
        }
    }
}
