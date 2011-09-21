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
import java.util.Properties;

import org.alfresco.repo.admin.patch.AbstractPatch;
import org.alfresco.repo.importer.ImporterBootstrap;
import org.alfresco.service.cmr.admin.PatchException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.PermissionService;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * Patch to remove the GROUP_EVERYONE Contributor permissions on the
 *  Sites Space (/ 
 * <p/>
 * Formerly, all users could create anything in this folder. As only
 * Sites should live there, and the SiteService handles permissions itself,
 * the Contributor permission can be removed.
 * 
 * @author Nick Burch
 * @since 4.0
 */
public class SitesSpacePermissionsPatch extends AbstractPatch
{
    // Message IDs
    private static final String MSG_SUCCESS = "patch.sitesSpacePermissions.result";
    
    // Folders' names for path building
    private static final String PROPERTY_COMPANY_HOME_CHILDNAME = "spaces.company_home.childname";
    private static final String PROPERTY_SITES_CHILDNAME = "spaces.sites.childname";
    
    // Things we've found
    private NodeRef companyHomeNodeRef;
    private NodeRef sitesNodeRef;
    
    // Dependencies
    private ImporterBootstrap importerBootstrap;
    private PermissionService permissionService;
    
    public void setImporterBootstrap(ImporterBootstrap importerBootstrap)
    {
        this.importerBootstrap = importerBootstrap;
    }
    public void setPermissionService(PermissionService permissionService)
    {
       this.permissionService = permissionService;
    }

    @Override
    protected void checkProperties()
    {
        super.checkProperties();
        checkPropertyNotNull(importerBootstrap, "importerBootstrap");
        checkPropertyNotNull(permissionService, "permissionService");
    }
    
    public SitesSpacePermissionsPatch()
    {
    }

    protected void setUp() throws Exception
    {
        // Get the node store that we must work against
        StoreRef storeRef = importerBootstrap.getStoreRef();
        if (storeRef == null)
        {
            throw new PatchException("Bootstrap store has not been set");
        }
        NodeRef storeRootNodeRef = nodeService.getRootNode(storeRef);

        // Build up the Assocation Names that form the path
        Properties configuration = importerBootstrap.getConfiguration();

        String companyHomeChildName = configuration.getProperty(PROPERTY_COMPANY_HOME_CHILDNAME);
        if (companyHomeChildName == null || companyHomeChildName.length() == 0)
        {
            throw new PatchException("Bootstrap property '" + PROPERTY_COMPANY_HOME_CHILDNAME + "' is not present");
        }
        String sitesChildName = configuration.getProperty(PROPERTY_SITES_CHILDNAME);
        if (sitesChildName == null || sitesChildName.length() == 0)
        {
            throw new PatchException("Bootstrap property '" + PROPERTY_SITES_CHILDNAME + "' is not present");
        }

        // Build the search string to get the company home node
        StringBuilder sb = new StringBuilder(256);
        sb.append("/").append(companyHomeChildName);
        String xpath = sb.toString();
        // get the company home
        List<NodeRef> nodeRefs = searchService.selectNodes(storeRootNodeRef, xpath, null, namespaceService, false);
        if (nodeRefs.size() == 0)
        {
            throw new PatchException("XPath didn't return any results: \n" + "   root: " + storeRootNodeRef + "\n" + "   xpath: " + xpath);
        }
        else if (nodeRefs.size() > 1)
        {
            throw new PatchException("XPath returned too many results: \n" + "   root: " + storeRootNodeRef + "\n" + "   xpath: " + xpath + "\n" + "   results: " + nodeRefs);
        }
        this.companyHomeNodeRef = nodeRefs.get(0);

        // build the search string to get the sites node
        sb.append("/").append(sitesChildName);
        xpath = sb.toString();
        // get the sites node
        nodeRefs = searchService.selectNodes(storeRootNodeRef, xpath, null, namespaceService, false);
        if (nodeRefs.size() == 0)
        {
            throw new PatchException("XPath didn't return any results: \n" + "   root: " + storeRootNodeRef + "\n" + "   xpath: " + xpath);
        }
        else if (nodeRefs.size() > 1)
        {
            throw new PatchException("XPath returned too many results: \n" + "   root: " + storeRootNodeRef + "\n" + "   xpath: " + xpath + "\n" + "   results: " + nodeRefs);
        }
        this.sitesNodeRef = nodeRefs.get(0);
    }
    
    @Override
    protected String applyInternal() throws Exception
    {
       setUp();
       
       // Get the sites space
       NodeRef sitesSpace = sitesNodeRef;
       if(sitesSpace == null || !nodeService.exists(sitesSpace))
       {
          throw new IllegalStateException("Sites Space not found in Company Home!");
       }
       
       // Remove the permission
       permissionService.deletePermission(
             sitesSpace,
             PermissionService.ALL_AUTHORITIES,
             PermissionService.CONTRIBUTOR
       );
       
       // All done
       String msg = I18NUtil.getMessage(MSG_SUCCESS);
       return msg;
    }
}
