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
import org.alfresco.repo.site.SiteModel;
import org.alfresco.repo.site.SiteService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.workflow.WorkflowPath;
import org.alfresco.service.cmr.workflow.WorkflowService;
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
    private static final String TRANSITION_ACCEPT = "accept";
    private static final String TRANSITION_REJECT = "reject";
    private static final String MODEL_PROP_KEY_RESPONSE = "response";
    private static final String MODEL_PROP_KEY_SITE_SHORT_NAME = "siteShortName";
    private static final String USER_ADMIN = "admin";

    // properties for services
    private WorkflowService workflowService;
    private MutableAuthenticationDao mutableAuthenticationDao;
    private SiteService siteService;
    private PersonService personService;

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
        String workflowId = req.getParameter("workflowId");
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
        // check that workflow id URL parameter has been provided
        else if ((workflowId == null) || (workflowId.length() == 0))
        {
            // handle workflow id not provided
            throw new WebScriptException(Status.STATUS_BAD_REQUEST,
                    "workflow id parameter has not been provided in the URL.");
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
                acceptInvite(model, workflowId, inviteeUserName, siteShortName);
            } else if (response.equals(RESPONSE_REJECT))
            {
                rejectInvite(model, workflowId, inviteeUserName, siteShortName);
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
     * @param workflowId
     *            string id of invite process workflow instance
     * @param inviteeUserName
     *            string user name of invitee
     * @param siteShortName
     *            string short name of site for which invitee is accepting
     *            invitation to join
     */
    private void acceptInvite(Map<String, Object> model, String workflowId,
            String inviteeUserName, String siteShortName)
    {
        // get workflow paths associated with given workflow ID
        List<WorkflowPath> wfPaths = this.workflowService
                .getWorkflowPaths(workflowId);

        // throw web script exception if there is not at least one workflow path
        // associated with this workflow ID
        if ((wfPaths == null) || (wfPaths.size() == 0))
        {
            throw new WebScriptException(Status.STATUS_INTERNAL_SERVER_ERROR,
                    "There are no workflow paths associated with workflow ID: "
                            + workflowId);
        }

        // get workflow path ID for path matching workflow ID
        WorkflowPath wfPath = wfPaths.get(0);
        String wfPathID = wfPath.id;

        this.workflowService.signal(wfPathID, TRANSITION_ACCEPT);

        // enable invitee person's user account because he/she has accepted the
        // site invitation
        this.mutableAuthenticationDao.setEnabled(inviteeUserName, true);

        // Add Invitee to Site as "Site Collaborator" role
        RunAsWork<Boolean> setSiteMembershipWorker = new InviteResponse.SetSiteMembershipWorker(
                siteShortName, inviteeUserName, SiteModel.SITE_COLLABORATOR);
        AuthenticationUtil.runAs(setSiteMembershipWorker, USER_ADMIN);

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
     * @param workflowId
     *            string id of invite process workflow instance
     * @param inviteeUserName
     *            string user name of invitee
     * @param siteShortName
     *            string short name of site for which invitee is rejecting
     *            invitation to join
     */
    private void rejectInvite(Map<String, Object> model, String workflowId,
            String inviteeUserName, String siteShortName)
    {
        // get workflow paths associated with given workflow ID
        List<WorkflowPath> wfPaths = this.workflowService
                .getWorkflowPaths(workflowId);

        // throw web script exception if there is not at least one workflow path
        // associated with this workflow ID
        if ((wfPaths == null) || (wfPaths.size() == 0))
        {
            throw new WebScriptException(Status.STATUS_INTERNAL_SERVER_ERROR,
                    "There are no workflow paths associated with workflow ID: "
                            + workflowId);
        }

        // get workflow path ID for path matching workflow ID
        WorkflowPath wfPath = wfPaths.get(0);
        String wfPathID = wfPath.id;

        this.workflowService.signal(wfPathID, TRANSITION_REJECT);

        // delete the person created for invitee
        this.personService.deletePerson(inviteeUserName);

        // add model properties for template to render
        model.put(MODEL_PROP_KEY_RESPONSE, RESPONSE_REJECT);
        model.put(MODEL_PROP_KEY_SITE_SHORT_NAME, siteShortName);
    }
}
