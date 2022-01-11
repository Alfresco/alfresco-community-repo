/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
package org.alfresco.repo.imap;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.module.org_alfresco_module_rm.util.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.site.SiteModel;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;

/**
 * Extends the core service ImapServiceImpl functionality
 * 
 * @author Ana Bozianu
 * @since 2.4
 *
 */
public class ExtendedImapServiceImpl extends ImapServiceImpl
{
    private NodeService nodeService;
    private BehaviourFilter policyBehaviourFilter;
    private DictionaryService dictionaryService;
    private AuthenticationUtil authenticationUtil;

    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }

    public void setAuthenticationUtil(AuthenticationUtil authenticationUtil)
    {
        this.authenticationUtil = authenticationUtil;
    }

    @Override
    public void setPolicyFilter(BehaviourFilter policyBehaviourFilter)
    {
        this.policyBehaviourFilter = policyBehaviourFilter;
        super.setPolicyFilter(policyBehaviourFilter);
    }

    @Override
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
        super.setNodeService(nodeService);
    }

    /**
     * Overwrites the core functionality so we can list RM files in IMAP
     * @see https://issues.alfresco.com/jira/browse/RM-3216
     */
    @Override
    public String getPathFromSites(final NodeRef ref)
    {
        return doAsSystem(new RunAsWork<String>()
        {
            @Override
            public String doWork() throws Exception
            {
                String name = ((String) nodeService.getProperty(ref, ContentModel.PROP_NAME)).toLowerCase();
                if (dictionaryService.isSubClass(nodeService.getType(ref), SiteModel.TYPE_SITE))
                {
                    return name;
                }
                else
                {
                    NodeRef parent = nodeService.getPrimaryParent(ref).getParentRef();
                    return getPathFromSites(parent) + "/" + name;
                }
            }
        });
    }

    private <R> R doAsSystem(RunAsWork<R> work)
    {
        policyBehaviourFilter.disableBehaviour(ContentModel.ASPECT_AUDITABLE);
        policyBehaviourFilter.disableBehaviour(ContentModel.ASPECT_VERSIONABLE);
        try
        {
            return authenticationUtil.runAsSystem(work);
        }
        finally
        {
            policyBehaviourFilter.enableBehaviour(ContentModel.ASPECT_AUDITABLE);
            policyBehaviourFilter.enableBehaviour(ContentModel.ASPECT_VERSIONABLE);
        }
    }
}
