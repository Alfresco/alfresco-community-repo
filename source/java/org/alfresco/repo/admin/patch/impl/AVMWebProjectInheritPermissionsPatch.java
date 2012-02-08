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
package org.alfresco.repo.admin.patch.impl;

import java.util.List;

import org.alfresco.model.WCMAppModel;
import org.alfresco.repo.admin.patch.AbstractPatch;
import org.alfresco.repo.importer.ImporterBootstrap;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * Patch to break the inheritance of permissions on AVM Web Project Folders.
 * This removes the need for admins to write a script or similar to by default hide web folders
 * to all users except those explicitly invited (given permissions) to the project.
 * 
 * @author Kevin Roast
 */
public class AVMWebProjectInheritPermissionsPatch extends AbstractPatch
{
    private static final String MSG_SUCCESS = "patch.avmWebProjectInheritPermissions.result";
    
    private ImporterBootstrap spacesImporterBootstrap;
    private PermissionService permissionService;

    public void setSpacesImporterBootstrap(ImporterBootstrap spacesImporterBootstrap)
    {
        this.spacesImporterBootstrap = spacesImporterBootstrap;
    }
    
    public void setPermissionService(PermissionService permissionService)
    {
        this.permissionService = permissionService;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.admin.patch.AbstractPatch#applyInternal()
     */
    @Override
    protected String applyInternal() throws Exception
    {
        SearchParameters sp = new SearchParameters();
        sp.setLanguage(SearchService.LANGUAGE_LUCENE);
        sp.setQuery("TYPE:\"wca:webfolder\"");
        sp.addStore(spacesImporterBootstrap.getStoreRef());
        ResultSet rs = null;
        try
        {
            rs = searchService.query(sp);
            for (ResultSetRow row : rs)
            {
                // break permission inheritance for the Web Project nodes
                this.permissionService.setInheritParentPermissions(row.getNodeRef(), false);
                
                // ensure that permissions are explicitly assigned for all Content Manager roles
                List<ChildAssociationRef> userInfoRefs = this.nodeService.getChildAssocs(
                        row.getNodeRef(), WCMAppModel.ASSOC_WEBUSER, RegexQNamePattern.MATCH_ALL);
                for (ChildAssociationRef ref : userInfoRefs)
                {
                    NodeRef userInfoRef = ref.getChildRef();
                    String username = (String)nodeService.getProperty(userInfoRef, WCMAppModel.PROP_WEBUSERNAME);
                    String userrole = (String)nodeService.getProperty(userInfoRef, WCMAppModel.PROP_WEBUSERROLE);
                    
                    if ("ContentManager".equals(userrole))
                    {
                        this.permissionService.setPermission(row.getNodeRef(), username, "ContentManager", true);
                    }
                }
            }
        }
        finally
        {
            if (rs != null)
            {
                rs.close();
            }
        }
        
        return I18NUtil.getMessage(MSG_SUCCESS);
    }
}
