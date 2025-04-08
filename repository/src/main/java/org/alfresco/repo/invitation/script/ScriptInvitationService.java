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
package org.alfresco.repo.invitation.script;

import java.util.List;

import org.mozilla.javascript.Scriptable;
import org.springframework.beans.factory.InitializingBean;

import org.alfresco.repo.invitation.InvitationSearchCriteriaImpl;
import org.alfresco.repo.jscript.BaseScopableProcessorExtension;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.site.SiteModel;
import org.alfresco.service.cmr.invitation.Invitation;
import org.alfresco.service.cmr.invitation.Invitation.ResourceType;
import org.alfresco.service.cmr.invitation.InvitationSearchCriteria.InvitationType;
import org.alfresco.service.cmr.invitation.InvitationService;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.util.ParameterCheck;

/**
 * Script object representing the invitation service.
 * 
 * Provides access to invitations from outside the context of a web project or a web site
 * 
 * @author Mark Rogers
 */
public class ScriptInvitationService extends BaseScopableProcessorExtension
        implements InitializingBean
{

    static final int DEFAULT_MAX_LIST_INVITATIONS_RETURN_SIZE = 200;

    /** The invitation service */
    private InvitationService invitationService;

    /** The node Service */
    private NodeService nodeService;

    /** The person Service */
    private PersonService personService;

    /** The Script Invitation Factory */
    private ScriptInvitationFactory scriptInvitationFactory;

    private SiteService siteService;

    /**
     * Set the invitation service
     * 
     * @param invitationService
     *            the invitation service
     */
    public void setInvitationService(InvitationService invitationService)
    {
        this.invitationService = invitationService;
    }

    /**
     * Set the node service
     * 
     * @param nodeService
     *            the nodeService to set
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    /**
     * Sets the person service.
     * 
     * @param personService
     *            the personService to set
     */
    public void setPersonService(PersonService personService)
    {
        this.personService = personService;
    }

    public void setSiteService(SiteService siteService)
    {
        this.siteService = siteService;
    }

    /**
     * List the open invitations. props specifies optional properties to constrain the search.
     * 
     * By default, if no "resultsLimit" property is specified in the props argument, this method will return a maximum of DEFAULT_MAX_LIST_INVITATIONS_RETURN_SIZE (200) results
     * 
     * @param props
     *            inviteeUserName
     * @param props
     *            resourceName
     * @param props
     *            resourceType
     * @param props
     *            invitationType
     *
     * @return the invitations
     */

    public ScriptInvitation<?>[] listInvitations(Scriptable props)
    {
        InvitationSearchCriteriaImpl crit = new InvitationSearchCriteriaImpl();

        int resultsLimit = DEFAULT_MAX_LIST_INVITATIONS_RETURN_SIZE;

        if (props.has("resourceName", props))
        {
            crit.setResourceName((String) props.get("resourceName", props));
        }
        if (props.has("resourceType", props))
        {
            crit.setResourceType(ResourceType.valueOf((String) props.get("resourceType", props)));
        }
        if (props.has("inviteeUserName", props))
        {
            crit.setInvitee((String) props.get("inviteeUserName", props));
        }
        if (props.has("invitationType", props))
        {
            String invitationType = (String) props.get("invitationType", props);
            crit.setInvitationType(InvitationType.valueOf(invitationType));
        }
        if (props.has("resultsLimit", props))
        {
            String resultsLimitStr = (String) props.get("resultsLimit", props);
            try
            {
                if (resultsLimitStr != null && !resultsLimitStr.isEmpty())
                {
                    resultsLimit = Integer.parseInt(resultsLimitStr);
                }
            }
            catch (Exception e)
            {
                // ignore any parse exceptions; no need to log them
            }
        }

        // MNT-9905 Pending Invites created by one site manager aren't visible to other site managers
        String currentUser = AuthenticationUtil.getRunAsUser();
        String siteShortName = crit.getResourceName();
        List<Invitation> invitations;

        if (siteShortName != null && (SiteModel.SITE_MANAGER).equals(siteService.getMembersRole(siteShortName, currentUser)))
        {
            final InvitationSearchCriteriaImpl criteria = crit;
            final int resultsLimitFinal = resultsLimit;
            RunAsWork<List<Invitation>> runAsSystem = new RunAsWork<List<Invitation>>() {
                public List<Invitation> doWork() throws Exception
                {
                    return invitationService.searchInvitation(criteria, resultsLimitFinal);
                }
            };

            invitations = AuthenticationUtil.runAs(runAsSystem, AuthenticationUtil.getSystemUserName());
        }
        else
        {
            invitations = invitationService.searchInvitation(crit, resultsLimit);
        }

        ScriptInvitation<?>[] ret = new ScriptInvitation[invitations.size()];
        int i = 0;
        for (Invitation item : invitations)
        {
            ret[i++] = scriptInvitationFactory.toScriptInvitation(item);
        }
        return ret;
    }

    /* (non-Javadoc)
     * 
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet() */
    public void afterPropertiesSet() throws Exception
    {
        ParameterCheck.mandatory("nodeService", nodeService);
        ParameterCheck.mandatory("personService", personService);
        ParameterCheck.mandatory("invitationService", invitationService);
        this.scriptInvitationFactory = new ScriptInvitationFactory(invitationService, nodeService, personService);
    }
}
