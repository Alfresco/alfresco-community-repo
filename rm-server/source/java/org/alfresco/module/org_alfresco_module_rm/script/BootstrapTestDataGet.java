/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
package org.alfresco.module.org_alfresco_module_rm.script;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.RecordsManagementService;
import org.alfresco.module.org_alfresco_module_rm.action.RecordsManagementActionService;
import org.alfresco.module.org_alfresco_module_rm.capability.RMPermissionModel;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionSchedule;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionService;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementSearchBehaviour;
import org.alfresco.module.org_alfresco_module_rm.model.RmSiteType;
import org.alfresco.module.org_alfresco_module_rm.security.RecordsManagementSecurityService;
import org.alfresco.module.org_alfresco_module_rm.security.Role;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.view.ImporterService;
import org.alfresco.service.cmr.view.Location;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * BootstrapTestData GET WebScript implementation.
 */
public class BootstrapTestDataGet extends DeclarativeWebScript
                                  implements RecordsManagementModel
{
    private static Log logger = LogFactory.getLog(BootstrapTestDataGet.class);
    
    private static final String ARG_SITE_NAME = "site";
    private static final String ARG_IMPORT = "import";
    
    private static final String XML_IMPORT = "alfresco/module/org_alfresco_module_rm/dod5015/DODExampleFilePlan.xml";
    
    private static final StoreRef SPACES_STORE = new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore");
    
    private NodeService nodeService;
    private SearchService searchService;
    private RecordsManagementService recordsManagementService;
    private RecordsManagementActionService recordsManagementActionService;
    private ImporterService importerService;
    private SiteService siteService;
    private PermissionService permissionService;
    private RecordsManagementSecurityService recordsManagementSecurityService;
    private AuthorityService authorityService;
    private RecordsManagementSearchBehaviour recordsManagementSearchBehaviour;
    private DispositionService dispositionService;
        
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }
    
    public void setSearchService(SearchService searchService)
    {
        this.searchService = searchService;
    }
    
    public void setDispositionService(DispositionService dispositionService)
    {
        this.dispositionService = dispositionService;
    }
    
    public void setRecordsManagementService(RecordsManagementService recordsManagementService)
    {
        this.recordsManagementService = recordsManagementService;
    }
    
    public void setRecordsManagementActionService(RecordsManagementActionService recordsManagementActionService)
    {
        this.recordsManagementActionService = recordsManagementActionService;
    }
    
    public void setImporterService(ImporterService importerService) 
    {
        this.importerService = importerService;
    }
    
    public void setSiteService(SiteService siteService)
    {
        this.siteService = siteService;
    }
    
    public void setPermissionService(PermissionService permissionService)
    {
        this.permissionService = permissionService;
    }
    
    public void setAuthorityService(AuthorityService authorityService)
    {
        this.authorityService = authorityService;
    }
    
    public void setRecordsManagementSecurityService(RecordsManagementSecurityService recordsManagementSecurityService)
    {
        this.recordsManagementSecurityService = recordsManagementSecurityService;
    }
    
    public void setRecordsManagementSearchBehaviour(RecordsManagementSearchBehaviour searchBehaviour)
    {
        this.recordsManagementSearchBehaviour = searchBehaviour;
    }
    
    @Override
    public Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
    {
        // resolve import argument
        boolean importData = false;
        if (req.getParameter(ARG_IMPORT) != null)
        {
            importData = Boolean.parseBoolean(req.getParameter(ARG_IMPORT));
        }
        
        // resolve rm site
        String siteName = RmSiteType.DEFAULT_SITE_NAME;
        if (req.getParameter(ARG_SITE_NAME) != null)
        {
            siteName = req.getParameter(ARG_SITE_NAME);
        }
        
        if (importData)
        {
            SiteInfo site = siteService.getSite(siteName);
            if (site == null)
            {
                throw new AlfrescoRuntimeException("Records Management site does not exist: " + siteName);
            }
            
            // resolve documentLibrary (filePlan) container
            NodeRef filePlan = siteService.getContainer(siteName, RmSiteType.COMPONENT_DOCUMENT_LIBRARY);
            if (filePlan == null)
            {
                filePlan = siteService.createContainer(siteName, RmSiteType.COMPONENT_DOCUMENT_LIBRARY, TYPE_FILE_PLAN, null);
            }
            
            // import the RM test data ACP into the the provided filePlan node reference
            InputStream is = BootstrapTestDataGet.class.getClassLoader().getResourceAsStream(XML_IMPORT);
            if (is == null)
            {
                throw new AlfrescoRuntimeException("The DODExampleFilePlan.xml import file could not be found");
            }
            Reader viewReader = new InputStreamReader(is);
            Location location = new Location(filePlan);
            importerService.importView(viewReader, location, null, null);
        }
        
        // Patch data
        BootstrapTestDataGet.patchLoadedData(searchService, nodeService, recordsManagementService, 
                                             recordsManagementActionService, permissionService,
                                             authorityService, recordsManagementSecurityService,
                                             recordsManagementSearchBehaviour,
                                             dispositionService);
        
        Map<String, Object> model = new HashMap<String, Object>(1, 1.0f);
    	model.put("success", true);
    	
        return model;
    }
    
    /**
     * Temp method to patch AMP'ed data
     * 
     * @param searchService
     * @param nodeService
     * @param recordsManagementService
     * @param recordsManagementActionService
     */
    public static void patchLoadedData( final SearchService searchService, 
                                        final NodeService nodeService, 
                                        final RecordsManagementService recordsManagementService,
                                        final RecordsManagementActionService recordsManagementActionService,
                                        final PermissionService permissionService,
                                        final AuthorityService authorityService,
                                        final RecordsManagementSecurityService recordsManagementSecurityService,
                                        final RecordsManagementSearchBehaviour recordManagementSearchBehaviour,
                                        final DispositionService dispositionService)
    {
        AuthenticationUtil.RunAsWork<Object> runAsWork = new AuthenticationUtil.RunAsWork<Object>()
        {
            public Object doWork() throws Exception
            {
                java.util.List<NodeRef> rmRoots = recordsManagementService.getFilePlans();
                logger.info("Bootstraping " + rmRoots.size() + " rm roots ...");
                for (NodeRef rmRoot : rmRoots)
                {
                    if (permissionService.getInheritParentPermissions(rmRoot) == true)
                    {
                        logger.info("Updating permissions for rm root: " + rmRoot);
                        permissionService.setInheritParentPermissions(rmRoot, false);
                    }
                    
                    String allRoleShortName = "AllRoles" + rmRoot.getId();
                    String allRoleGroupName = authorityService.getName(AuthorityType.GROUP, allRoleShortName);
                    
                    if (authorityService.authorityExists(allRoleGroupName) == false)
                    {       
                        logger.info("Creating all roles group for root node: " + rmRoot.toString());
                        
                        // Create "all" role group for root node
                        String allRoles = authorityService.createAuthority(AuthorityType.GROUP, 
                                                                           allRoleShortName, 
                                                                           "All Roles", 
                                                                           null);
                        
                        // Put all the role groups in it
                        Set<Role> roles = recordsManagementSecurityService.getRoles(rmRoot);
                        for (Role role : roles)
                        {
                            logger.info("   - adding role group " + role.getRoleGroupName() + " to all roles group");
                            authorityService.addAuthority(allRoles, role.getRoleGroupName());
                        }
                        
                        // Set the permissions
                        permissionService.setPermission(rmRoot, allRoles, RMPermissionModel.READ_RECORDS, true);
                    }
                }
                
                // Make sure all the containers do not inherit permissions
                ResultSet rs = searchService.query(SPACES_STORE, SearchService.LANGUAGE_LUCENE, "TYPE:\"rma:recordsManagementContainer\"");
                try
                {
                    logger.info("Bootstraping " + rs.length() + " record containers ...");
                    
                    for (NodeRef container : rs.getNodeRefs())
                    {
                        String containerName = (String)nodeService.getProperty(container, ContentModel.PROP_NAME);
                        
                        // Set permissions
                        if (permissionService.getInheritParentPermissions(container) == true)
                        {
                            logger.info("Updating permissions for record container: " + containerName);
                            permissionService.setInheritParentPermissions(container, false);
                        }
                    }
                }
                finally
                {
                    rs.close();
                }
                
                // fix up the test dataset to fire initial events for disposition schedules
                rs = searchService.query(SPACES_STORE, SearchService.LANGUAGE_LUCENE, "TYPE:\"rma:recordFolder\"");
                try
                {
                    logger.info("Bootstraping " + rs.length() + " record folders ...");
                    
                    for (NodeRef recordFolder : rs.getNodeRefs())
                    {
                        String folderName = (String)nodeService.getProperty(recordFolder, ContentModel.PROP_NAME);
                        
                        // Set permissions
                        if (permissionService.getInheritParentPermissions(recordFolder) == true)
                        {
                            logger.info("Updating permissions for record folder: " + folderName);
                            permissionService.setInheritParentPermissions(recordFolder, false);
                        }
                        
                        if (nodeService.hasAspect(recordFolder, ASPECT_DISPOSITION_LIFECYCLE) == false)
                        {
                            // See if the folder has a disposition schedule that needs to be applied
                            DispositionSchedule ds = dispositionService.getDispositionSchedule(recordFolder);
                            if (ds != null)
                            {
                                // Fire action to "set-up" the folder correctly
                                logger.info("Setting up bootstraped record folder: " + folderName);
                                recordsManagementActionService.executeRecordsManagementAction(recordFolder, "setupRecordFolder");
                            }
                        }
                        
                        // fixup the search behaviour aspect for the record folder
                        logger.info("Setting up search aspect for record folder: " + folderName);
                        recordManagementSearchBehaviour.fixupSearchAspect(recordFolder);
                    }
                }
                finally
                {
                    rs.close();
                }
                
                return null;
            }
        };
        
        AuthenticationUtil.runAs(runAsWork, AuthenticationUtil.getAdminUserName());
        
    }
}