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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.cmr.workflow.WorkflowTaskQuery;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.scripts.DeclarativeWebScript;
import org.alfresco.web.scripts.Status;
import org.alfresco.web.scripts.WebScriptException;
import org.alfresco.web.scripts.WebScriptRequest;

/**
 * Web Script which returns pending Site invitations matching at least one of
 *  
 * (1) inviter (inviter user name). i.e. pending invitations which have been sent 
 * by that inviter, but which have not been responded to (accepted or rejected)
 * by the invitee, and have not been cancelled by that inviter
 * 
 * (2) invitee (invitee user name), i.e. pending invitations which have not been accepted or
 * rejected yet by that inviter
 *  
 * (3) site (site short name), i.e. pending invitations sent out to join that Site.
 *       If only the site is given, then all pending invites are returned, irrespective of who 
 *       the inviters or invitees are
 *       
 * At least one of the above parameters needs to be passed to this web script
 * 
 * @author glen dot johnson at alfresco dot com
 */
public class Invites extends DeclarativeWebScript
{
    // request parameter names
    private static final String PARAM_INVITER_USER_NAME = "inviterUserName";
    private static final String PARAM_INVITEE_USER_NAME = "inviteeUserName";
    private static final String PARAM_SITE_SHORT_NAME = "siteShortName";
    
    // model key names
    private static final String MODEL_KEY_NAME_INVITES = "invites";
    
    // invite process definition name
    private static final QName WF_INVITE_PROCESS_DEFINITION_QNAME = QName.createQName("wf:invite");
    
    // service instances
    private WorkflowService workflowService;
    
    /**
     * Set the workflow service property
     * 
     * @param workflowService the workflow service to set
     */
    public void setWorkflowService(WorkflowService workflowService)
    {
        this.workflowService = workflowService;
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
        
        // get URL request parameters
        
        String inviterUserName = req.getParameter(PARAM_INVITER_USER_NAME);
        // check for 'inviterUserName' parameter not provided
        if ((inviterUserName == null) || (inviterUserName.length() == 0))
        {
            // handle inviterUserName URL parameter not provided
            throw new WebScriptException(Status.STATUS_BAD_REQUEST,
                    "'inviterUserName' parameter has not been provided in URL");
        }

        String inviteeUserName = req.getParameter(PARAM_INVITEE_USER_NAME);
        // check for 'inviteeUserName' parameter not provided
        if ((inviteeUserName == null) || (inviteeUserName.length() == 0))
        {
            // handle inviteeUserName URL parameter not provided
            throw new WebScriptException(Status.STATUS_BAD_REQUEST,
                    "'inviteeUserName' parameter has not been provided in URL");
        }

        String siteShortName = req.getParameter(PARAM_SITE_SHORT_NAME);
        // check for 'siteShortName' parameter not provided
        if ((siteShortName == null) || (siteShortName.length() == 0))
        {
            // handle siteShortName URL parameter not provided
            throw new WebScriptException(Status.STATUS_BAD_REQUEST,
                    "'siteShortName' parameter has not been provided in URL");
        }
        
        // query for workflow tasks by given parameters
        // create workflow task query
        WorkflowTaskQuery wfTaskQuery = new WorkflowTaskQuery();
        
        // query only active workflows
        wfTaskQuery.setActive(Boolean.TRUE);
        
        // create the query properties from the invite URL request parameters
        // - because this web script class will terminate if no 'invites' URL request
        // - parameters are set, at least one of these query properties will always be set
        // - at this point
        final HashMap<QName, Object> wfQueryProps = new HashMap<QName, Object>(3, 1.0f);
        if (inviterUserName != null)
        {
            wfQueryProps.put(QName.createQName(Invite.WF_PROP_INVITER_USER_NAME), inviterUserName);
        }
        if (inviteeUserName != null)
        {
            wfQueryProps.put(QName.createQName(Invite.WF_PROP_INVITEE_USER_NAME), inviteeUserName);
        }
        if (siteShortName != null)
        {
            wfQueryProps.put(QName.createQName(Invite.WF_PROP_SITE_SHORT_NAME), siteShortName);
        }
        
        // set workflow task query parameters
        wfTaskQuery.setProcessCustomProps(wfQueryProps);
        
        // set process name to "wf:invite" so that only tasks associated with invite workflow instances 
        // are returned by query
        wfTaskQuery.setProcessName(WF_INVITE_PROCESS_DEFINITION_QNAME);
        
        // query for invite workflow tasks
        List<WorkflowTask> wf_invite_tasks = this.workflowService.queryTasks(wfTaskQuery);
        
        // InviteInfo List to place onto model
        List<InviteInfo> inviteInfoList = new ArrayList<InviteInfo>();
        
        // Put InviteInfo objects (containing workflow path properties
        //    wf:inviterUserName, wf:inviteeUserName, wf:siteShortName,
        //    and invite id property (from workflow instance id))
        // onto model for each invite workflow task returned by the query
        for (WorkflowTask workflowTask : wf_invite_tasks)
        {
            // get wf:inviterUserName, wf:inviteeUserName, wf:siteShortName
            // properties from workflow path associated with workflow task
            Map<QName, Serializable> pathProperties = this.workflowService.getPathProperties(workflowTask.path.id);
            String inviterUserNameProp = (String)pathProperties.get(Invite.WF_PROP_INVITER_USER_NAME);
            String inviteeUserNameProp = (String)pathProperties.get(Invite.WF_PROP_INVITEE_USER_NAME);
            String siteShortNameProp = (String)pathProperties.get(Invite.WF_PROP_SITE_SHORT_NAME);
            
            // get workflow instance id (associated with workflow task) to place as "inviteId" onto model
            String workflowId = workflowTask.path.instance.id;
            
            // create and add InviteInfo to inviteInfoList
            InviteInfo inviteInfo = new InviteInfo(inviterUserNameProp, inviteeUserNameProp, siteShortNameProp, workflowId);
            inviteInfoList.add(inviteInfo);
        }
        
        // put the list of invite infos onto model to be passed onto template for rendering
        model.put(MODEL_KEY_NAME_INVITES, inviteInfoList);
        
        return model;
    }
}
