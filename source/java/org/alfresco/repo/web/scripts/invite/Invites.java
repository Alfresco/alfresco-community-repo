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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
 * Web Script which returns pending Site invitations matching at least one of
 * 
 * (1a) inviter (inviter user name). i.e. pending invitations which have been
 * sent by that inviter, but which have not been responded to (accepted or
 * rejected) by the invitee, and have not been cancelled by that inviter
 * 
 * (1b) invitee (invitee user name), i.e. pending invitations which have not
 * been accepted or rejected yet by that inviter
 * 
 * (1c) site (site short name), i.e. pending invitations sent out to join that
 * Site. If only the site is given, then all pending invites are returned,
 * irrespective of who the inviters or invitees are
 * 
 * or
 * 
 * (2) matching the given invite ID
 * 
 * 
 * @author glen dot johnson at alfresco dot com
 */
public class Invites extends DeclarativeWebScript
{
    // request parameter names
    private static final String PARAM_INVITER_USER_NAME = "inviterUserName";
    private static final String PARAM_INVITEE_USER_NAME = "inviteeUserName";
    private static final String PARAM_SITE_SHORT_NAME = "siteShortName";
    private static final String PARAM_INVITE_ID = "inviteId";

    // model key names
    private static final String MODEL_KEY_NAME_INVITES = "invites";

    // service instances
    private WorkflowService workflowService;
    private NamespaceService namespaceService;

    /**
     * Set the workflow service property
     * 
     * @param workflowService
     *            the workflow service to set
     */
    public void setWorkflowService(WorkflowService workflowService)
    {
        this.workflowService = workflowService;
    }

    /**
     * Set the namespace service
     * 
     * @param namespaceService the namespace service to set
     */
    public void setNamespaceService(
            NamespaceService namespaceService)
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

        // Get parameter names
        String[] paramNames = req.getParameterNames();

        // handle no parameters given on URL
        if ((paramNames == null) || (paramNames.length == 0))
        {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST,
                    "No parameters have been provided on URL");
        }

        // get URL request parameters, checking if they have been provided

        // check if 'inviterUserName' parameter provided
        String inviterUserName = req.getParameter(PARAM_INVITER_USER_NAME);
        boolean inviterUserNameProvided = (inviterUserName != null)
                && (inviterUserName.length() != 0);

        // check if 'inviteeUserName' parameter provided
        String inviteeUserName = req.getParameter(PARAM_INVITEE_USER_NAME);
        boolean inviteeUserNameProvided = (inviteeUserName != null)
                && (inviteeUserName.length() != 0);

        // check if 'siteShortName' parameter provided
        String siteShortName = req.getParameter(PARAM_SITE_SHORT_NAME);
        boolean siteShortNameProvided = (siteShortName != null)
                && (siteShortName.length() != 0);

        // check if 'inviteId' parameter provided
        String inviteId = req.getParameter(PARAM_INVITE_ID);
        boolean inviteIdProvided = (inviteId != null)
                && (inviteId.length() != 0);

        // throw web script exception if at least one of 'inviterUserName',
        // 'inviteeUserName', 'siteShortName',
        // 'inviteId' URL request parameters has not been provided
        if (!(inviterUserNameProvided || inviteeUserNameProvided
                || siteShortNameProvided || inviteIdProvided))
        {
            throw new WebScriptException(
                    Status.STATUS_BAD_REQUEST,
                    "At least one of the following URL request parameters must be provided in URL "
                            + "'inviterUserName', 'inviteeUserName', 'siteShortName' or 'inviteId'");
        }

        // query for workflow tasks by given parameters
        // create workflow task query
        WorkflowTaskQuery wfTaskQuery = new WorkflowTaskQuery();

        // if 'inviteId' has been provided then set that as the workflow query
        // process ID
        // - since this is unique don't bother about setting the other workflow
        // query
        // - properties
        if (inviteIdProvided)
        {
            wfTaskQuery.setProcessId(inviteId);
        } else
        // 'inviteId' has not been provided, so create the query properties from
        // the invite URL request
        // parameters
        // - because this web script class will terminate with a web script
        // exception if none of the required
        // request parameters are provided, at least one of these query
        // properties will be set
        // at this point
        {
            // workflow query properties
            HashMap<QName, Object> wfQueryProps = new HashMap<QName, Object>(3,
                    1.0f);
            if (inviterUserName != null)
            {
                wfQueryProps.put(QName.createQName(Invite.WF_PROP_INVITER_USER_NAME, this.namespaceService),
                        inviterUserName);
            }
            if (inviteeUserName != null)
            {
                wfQueryProps.put(QName.createQName(Invite.WF_PROP_INVITEE_USER_NAME, this.namespaceService),
                        inviteeUserName);
            }
            if (siteShortName != null)
            {
                wfQueryProps.put(QName.createQName(Invite.WF_PROP_SITE_SHORT_NAME, this.namespaceService),
                        siteShortName);
            }

            // set workflow task query parameters
            wfTaskQuery.setTaskCustomProps(wfQueryProps);
        }

        // query only active workflows
        wfTaskQuery.setActive(Boolean.TRUE);

        // pick up the start task
        wfTaskQuery.setTaskState(WorkflowTaskState.COMPLETED);
        wfTaskQuery.setTaskName(QName.createQName(Invite.WF_INVITE_TASK_INVITE_TO_SITE, this.namespaceService));

        // set process name to "wf:invite" so that only tasks associated with
        // invite workflow instances
        // are returned by query
        wfTaskQuery.setProcessName(QName.createQName("wf:invite", this.namespaceService));

        // query for invite workflow tasks
        List<WorkflowTask> wf_invite_tasks = this.workflowService
                .queryTasks(wfTaskQuery);

        // InviteInfo List to place onto model
        List<InviteInfo> inviteInfoList = new ArrayList<InviteInfo>();

        // Put InviteInfo objects (containing workflow path properties
        // wf:inviterUserName, wf:inviteeUserName, wf:siteShortName,
        // and invite id property (from workflow instance id))
        // onto model for each invite workflow task returned by the query
        for (WorkflowTask workflowTask : wf_invite_tasks)
        {
            // get wf:inviterUserName, wf:inviteeUserName, wf:siteShortName
            // properties from workflow path associated with workflow task
            String inviterUserNameProp = (String) workflowTask.properties.get(
                    QName.createQName(Invite.WF_PROP_INVITER_USER_NAME, this.namespaceService));
            String inviteeUserNameProp = (String) workflowTask.properties.get(
                    QName.createQName(Invite.WF_PROP_INVITEE_USER_NAME, this.namespaceService));
            String siteShortNameProp = (String) workflowTask.properties.get(
                    QName.createQName(Invite.WF_PROP_SITE_SHORT_NAME, this.namespaceService));

            // get workflow instance id (associated with workflow task) to place
            // as "inviteId" onto model
            String workflowId = workflowTask.path.instance.id;

            // create and add InviteInfo to inviteInfoList
            InviteInfo inviteInfo = new InviteInfo(inviterUserNameProp,
                    inviteeUserNameProp, siteShortNameProp, workflowId);
            inviteInfoList.add(inviteInfo);
        }

        // put the list of invite infos onto model to be passed onto template
        // for rendering
        model.put(MODEL_KEY_NAME_INVITES, inviteInfoList);

        return model;
    }
}
