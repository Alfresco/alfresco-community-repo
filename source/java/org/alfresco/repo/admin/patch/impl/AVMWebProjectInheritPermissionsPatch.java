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
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing
 */
package org.alfresco.repo.admin.patch.impl;

import java.util.List;

import org.springframework.extensions.surf.util.I18NUtil;
import org.alfresco.model.WCMAppModel;
import org.alfresco.repo.admin.patch.AbstractPatch;
import org.alfresco.repo.importer.ImporterBootstrap;
import org.alfresco.repo.search.IndexerAndSearcher;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.RegexQNamePattern;

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
    private IndexerAndSearcher indexerAndSearcher;
    private PermissionService permissionService;

    public void setIndexerAndSearcher(IndexerAndSearcher indexerAndSearcher)
    {
        this.indexerAndSearcher = indexerAndSearcher;
    }
    
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
