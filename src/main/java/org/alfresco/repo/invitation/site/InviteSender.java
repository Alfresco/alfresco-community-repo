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

import static org.alfresco.repo.invitation.WorkflowModelNominatedInvitation.wfVarResourceName;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.repo.i18n.MessageService;
import org.alfresco.repo.model.Repository;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.admin.RepoAdminService;
import org.alfresco.service.cmr.invitation.InvitationException;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.namespace.NamespaceService;

/**
 * Notifies the necessary user(s) of a pending site membership invitation/request.
 * 
 * @author Nick Smith
 */
public abstract class InviteSender
{
    protected final ActionService actionService;
    protected final NodeService nodeService;
    protected final PersonService personService;
    protected final SearchService searchService;
    protected final SiteService siteService;
    protected final Repository repository;
    protected final MessageService messageService;
    protected final FileFolderService fileFolderService;
    protected final RepoAdminService repoAdminService;
    protected final NamespaceService namespaceService;
    
    public InviteSender(ServiceRegistry services, Repository repository, MessageService messageService)
    {
        this.actionService = services.getActionService();
        this.nodeService = services.getNodeService();
        this.personService = services.getPersonService();
        this.searchService = services.getSearchService();
        this.siteService = services.getSiteService();
        this.fileFolderService = services.getFileFolderService();
        this.repoAdminService = services.getRepoAdminService();
        this.namespaceService = services.getNamespaceService();
        this.repository = repository;
        this.messageService = messageService;
    }
    
    /**
     * Sends an invitation email.
     * 
     * @param emailTemplateXpath the XPath to the email template in the repository
     * @param emailSubjectKey the subject of the email
     * @param properties A Map containing the properties needed to send the email.
     */
    public abstract void sendMail(String emailTemplateXpath, String emailSubjectKey, Map<String, String> properties);
    
    protected abstract Map<String, Serializable> buildMailTextModel(Map<String, String> properties);
    
    protected abstract List<String> getRequiredProperties();

    /**
     * @param properties Map<String, String>
     */
    protected void checkProperties(Map<String, String> properties)
    {
        Set<String> keys = properties.keySet();
        if (!keys.containsAll(getRequiredProperties()))
        {
            LinkedList<String> missingProperties = new LinkedList<String>(getRequiredProperties());
            missingProperties.removeAll(keys);
            throw new InvitationException("The following mandatory properties are missing:\n" + missingProperties);
        }
    }

    protected NodeRef getEmailTemplateNodeRef(String emailTemplateXPath)
    {
        List<NodeRef> nodeRefs = searchService.selectNodes(repository.getRootHome(), 
                    emailTemplateXPath, null, 
                    this.namespaceService, false);
        
        if (nodeRefs.size() == 1) 
        {
            // Now localise this
            NodeRef base = nodeRefs.get(0);
            NodeRef local = fileFolderService.getLocalizedSibling(base);
            return local;
        }
        else
        {
            throw new InvitationException("Cannot find the email template!");
        }
    }

    protected String getSiteName(Map<String, String> properties)
    {
        String siteFullName = properties.get(getWorkflowPropForSiteName());
        SiteInfo site = siteService.getSite(siteFullName);
        if (site == null)
            throw new InvitationException("The site " + siteFullName + " could not be found.");

        String siteName = site.getShortName();
        String siteTitle = site.getTitle();
        if (siteTitle != null && siteTitle.length() > 0)
        {
            siteName = siteTitle;
        }
        return siteName;
    }

    protected abstract String getWorkflowPropForSiteName();
    
}
