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
package org.alfresco.repo.site;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.activities.ActivityType;
import org.alfresco.repo.admin.SysAdminParams;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.search.impl.lucene.AbstractLuceneQueryParser;
import org.alfresco.repo.security.authentication.AuthenticationContext;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.tenant.TenantAdminService;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.activities.ActivityService;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AccessPermission;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.NoSuchPersonException;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteVisibility;
import org.alfresco.service.cmr.tagging.TaggingService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.util.PropertyCheck;
import org.alfresco.util.PropertyMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.surf.util.ParameterCheck;

/**
 * Site Service Implementation. Also bootstraps the site AVM and DM stores.
 * 
 * @author Roy Wetherall
 */
public class SiteServiceImpl implements SiteServiceInternal, SiteModel
{
    /** Logger */
    private static Log logger = LogFactory.getLog(SiteServiceImpl.class);
    
    /** The DM store where site's are kept */
    public static final StoreRef SITE_STORE = new StoreRef("workspace://SpacesStore");

    /** Activity tool */
    private static final String ACTIVITY_TOOL = "siteService";
    
    private static final String SITE_PREFIX = "site_";
    private static final String GROUP_SITE_PREFIX = PermissionService.GROUP_PREFIX + SITE_PREFIX;
    private static final int GROUP_PREFIX_LENGTH = PermissionService.GROUP_PREFIX.length();
    private static final int GROUP_SITE_PREFIX_LENGTH = GROUP_SITE_PREFIX.length();
    
    /** Site home ref cache (Tennant aware) */
    private Map<String, NodeRef> siteHomeRefs = new ConcurrentHashMap<String, NodeRef>(4);
    
    /** Site node ref cache (Tennant aware) */
    private Map<String, NodeRef> siteNodeRefs = new ConcurrentHashMap<String, NodeRef>(256);

    private String sitesXPath;
    
    /** Messages */
    private static final String MSG_UNABLE_TO_CREATE = "site_service.unable_to_create";
    private static final String MSG_VISIBILITY_GROUP_MISSING = "site_service.visibility_group_missing";
    private static final String MSG_CAN_NOT_UPDATE = "site_service.can_not_update";
    private static final String MSG_CAN_NOT_DELETE = "site_service.can_not_delete";
    private static final String MSG_SITE_NO_EXIST = "site_service.site_no_exist";
    private static final String MSG_CAN_NOT_REMOVE_MSHIP = "site_service.can_not_remove_membership";
    private static final String MSG_DO_NOT_CHANGE_MGR = "site_service.do_not_change_manager";
    private static final String MSG_CAN_NOT_CHANGE_MSHIP="site_service.can_not_change_membership";
    private static final String MSG_SITE_CONTAINER_NOT_FOLDER = "site_service.site_container_not_folder";
    private static final String MSG_INVALID_SITE_TYPE = "site_service.invalid_site_type";

    /* Services */
    private NodeService nodeService;
    private NodeService directNodeService;
    private FileFolderService fileFolderService;
    private SearchService searchService;
    private NamespaceService namespaceService;
    private PermissionService permissionService;
    private ActivityService activityService;
    private PersonService personService;
    private AuthenticationContext authenticationContext;
    private TaggingService taggingService;
    private AuthorityService authorityService;
    private DictionaryService dictionaryService;
    private TenantService tenantService;
    private TenantAdminService tenantAdminService;
    private RetryingTransactionHelper retryingTransactionHelper;
    private Comparator<String> roleComparator;
    private SysAdminParams sysAdminParams;
    private BehaviourFilter behaviourFilter;
    private SitesPermissionCleaner sitesPermissionsCleaner;


    /**
     * Set the path to the location of the sites root folder.  For example:
     * <pre>
     * ./app:company_home/st:sites
     * </pre>
     * @param sitesXPath            a valid XPath
     */
    public void setSitesXPath(String sitesXPath)
    {
        this.sitesXPath = sitesXPath;
    }

    /**
     * Set node service
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    /**
     * Set the unprotected node service
     */
    public void setDirectNodeService(NodeService directNodeService)
    {
        this.directNodeService = directNodeService;
    }

    /**
     * Set file folder service
     */
    public void setFileFolderService(FileFolderService fileFolderService)
    {
        this.fileFolderService = fileFolderService;
    }

    /**
     * Set search service
     */
    public void setSearchService(SearchService searchService)
    {
        this.searchService = searchService;
    }

    /**
     * Set Namespace service
     */
    public void setNamespaceService(NamespaceService namespaceService)
    {
        this.namespaceService = namespaceService;
    }

    /**
     * Set permission service
     */
    public void setPermissionService(PermissionService permissionService)
    {
        this.permissionService = permissionService;
    }

    /**
     * Set activity service
     */
    public void setActivityService(ActivityService activityService)
    {
        this.activityService = activityService;
    }

    /**
     * Set person service
     */
    public void setPersonService(PersonService personService)
    {
        this.personService = personService;
    }

    /**
     * Set authentication component
     */
    public void setAuthenticationContext(
            AuthenticationContext authenticationContext)
    {
        this.authenticationContext = authenticationContext;
    }

    /**
     * Set the tagging service
     */
    public void setTaggingService(TaggingService taggingService)
    {
        this.taggingService = taggingService;
    }

    /**
     * Set the authority service
     */
    public void setAuthorityService(AuthorityService authorityService)
    {
        this.authorityService = authorityService;
    }
    
    /**
     * Set the dictionary service 
     * 
     * @param dictionaryService     dictionary service
     */
    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }
    
    /**
     * Set the tenant service 
     * 
     * @param tenantService     tenant service
     */
    public void setTenantService(TenantService tenantService)
    {
        this.tenantService = tenantService;
    }
    
    /**
     * Sets the tenant admin service
     */
    public void setTenantAdminService(TenantAdminService tenantAdminService)
    {
        this.tenantAdminService = tenantAdminService;
    }
    
    /**
     * Sets helper that provides transaction callbacks
     */
    public void setTransactionHelper(RetryingTransactionHelper retryingTransactionHelper)
    {
        this.retryingTransactionHelper = retryingTransactionHelper;
    }

    public void setRoleComparator(Comparator<String> roleComparator)
    {
        this.roleComparator = roleComparator;
    }
    
    public void setSysAdminParams(SysAdminParams sysAdminParams)
    {
    	this.sysAdminParams = sysAdminParams;
    }
    
    public void setBehaviourFilter(BehaviourFilter behaviourFilter)
    {
        this.behaviourFilter = behaviourFilter;
    }

    public void setSitesPermissionsCleaner(SitesPermissionCleaner sitesPermissionsCleaner)
    {
        this.sitesPermissionsCleaner = sitesPermissionsCleaner;
    }


    public Comparator<String> getRoleComparator()
    {
        return roleComparator;
    }

    /**
     * Checks that all necessary properties and services have been provided.
     */
    public void init()
    {
        PropertyCheck.mandatory(this, "nodeService", nodeService);
        PropertyCheck.mandatory(this, "directNodeService", directNodeService);
        PropertyCheck.mandatory(this, "fileFolderService", fileFolderService);
        PropertyCheck.mandatory(this, "searchService", searchService);
        PropertyCheck.mandatory(this, "namespaceService", namespaceService);
        PropertyCheck.mandatory(this, "permissionService", permissionService);
        PropertyCheck.mandatory(this, "authenticationContext", authenticationContext);
        PropertyCheck.mandatory(this, "personService", personService);
        PropertyCheck.mandatory(this, "activityService", activityService);
        PropertyCheck.mandatory(this, "taggingService", taggingService);
        PropertyCheck.mandatory(this, "authorityService", authorityService);
        PropertyCheck.mandatory(this, "sitesXPath", sitesXPath);
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.service.cmr.site.SiteService#hasCreateSitePermissions()
     */
    public boolean hasCreateSitePermissions()
    {
        final NodeRef siteRoot = getSiteRoot();
        if (siteRoot == null)
        {
            throw new SiteServiceException("No root sites folder exists");
        }
        boolean result = permissionService.hasPermission(siteRoot, PermissionService.CONTRIBUTOR).equals(AccessStatus.ALLOWED);
		return result;
    }
    
    /**
     * @see org.alfresco.service.cmr.site.SiteService#createSite(java.lang.String, java.lang.String, java.lang.String, java.lang.String, boolean)
     */
    public SiteInfo createSite(final String sitePreset, 
                               String passedShortName, 
                               final String title, 
                               final String description, 
                               final boolean isPublic)
    {
        // Determine the site visibility
        SiteVisibility visibility = SiteVisibility.PRIVATE;
        if (isPublic == true)
        {
            visibility = SiteVisibility.PUBLIC;
        }
        
        // Create the site
        return createSite(sitePreset, passedShortName, title, description, visibility);
    }

    /**
     * @see org.alfresco.service.cmr.site.SiteService#createSite(java.lang.String, java.lang.String, java.lang.String, java.lang.String, boolean)
     */
    public SiteInfo createSite(final String sitePreset, 
                               String passedShortName, 
                               final String title, 
                               final String description, 
                               final SiteVisibility visibility)
    {
    	return createSite(sitePreset, passedShortName, title, description, visibility, SiteModel.TYPE_SITE);
    }
    
    public SiteInfo createSite(final String sitePreset, 
                			   String passedShortName, 
                			   final String title, 
                               final String description, 
                               final SiteVisibility visibility,
                               final QName siteType)
    {	
    	// Check that the provided site type is a subtype of TYPE_SITE
    	if (SiteModel.TYPE_SITE.equals(siteType) == false &&
    	    dictionaryService.isSubClass(siteType, TYPE_SITE) == false)
    	{
    		throw new SiteServiceException(MSG_INVALID_SITE_TYPE, new Object[]{siteType});
    	}
    	
        // Remove spaces from shortName
        final String shortName = passedShortName.replaceAll(" ", "");
        
        /**
         * Check that the site does not already exist
         */
    	// Check to see if we already have a site of this name
    	NodeRef existingSite = getSiteNodeRef(shortName, false);
    	if (existingSite != null)
    	{
    		// Throw an exception since we have a duplicate site name
    		throw new SiteServiceException(MSG_UNABLE_TO_CREATE, new Object[]{shortName});
    	}

        // Get the site parent node reference
        NodeRef siteParent = getSiteParent(shortName);
        if (siteParent == null)
        {
            throw new SiteServiceException("No root sites folder exists");
        }

        // Create the site node
        PropertyMap properties = new PropertyMap(4);
        properties.put(ContentModel.PROP_NAME, shortName);
        properties.put(SiteModel.PROP_SITE_PRESET, sitePreset);
        properties.put(SiteModel.PROP_SITE_VISIBILITY, visibility.toString());
        properties.put(ContentModel.PROP_TITLE, title);
        properties.put(ContentModel.PROP_DESCRIPTION, description);
        
        final NodeRef siteNodeRef = this.nodeService.createNode(
                siteParent,
                ContentModel.ASSOC_CONTAINS,
                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, shortName), 
                siteType, 
                properties).getChildRef();

        // Make the new site a tag scope
        this.taggingService.addTagScope(siteNodeRef);

        // Clear the sites inherited permissions
        this.permissionService.setInheritParentPermissions(siteNodeRef, false);

        // Get the current user
        final String currentUser = authenticationContext.getCurrentUserName();
        
        // Create the relevant groups and assign permissions
        AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Object>()
        {
            public String doWork() throws Exception
            {
                Set<String> shareZones = new HashSet<String>(2, 1.0f);
                shareZones.add(AuthorityService.ZONE_APP_SHARE);
                shareZones.add(AuthorityService.ZONE_AUTH_ALFRESCO);
            	
                // Create the site's groups
                String siteGroup = authorityService
                        .createAuthority(AuthorityType.GROUP, getSiteGroup(shortName, false), shortName, shareZones);
                QName siteType = directNodeService.getType(siteNodeRef);
                Set<String> permissions = permissionService.getSettablePermissions(siteType);
                for (String permission : permissions)
                {
                    // Create a group for the permission
                    String permissionGroup = authorityService.createAuthority(AuthorityType.GROUP, getSiteRoleGroup(
                            shortName, permission, false), shortName, shareZones);
                    authorityService.addAuthority(siteGroup, permissionGroup);

                    // Assign the group the relevant permission on the site
                    permissionService.setPermission(siteNodeRef, permissionGroup, permission, true);
                }

                // Set the memberships details
                // - give all authorities site consumer if site is public
                // - give all authorities read properties if site is moderated
                // - give all authorities read permission on permissions so
                // memberships can be calculated
                // - add the current user to the site manager group
                if (SiteVisibility.PUBLIC.equals(visibility) == true &&
                    permissions.contains(SITE_CONSUMER))
                {
                	// From Alfresco 3.4 the 'site public' group is configurable. Out of the box it is
                	// GROUP_EVERYONE so unconfigured behaviour is unchanged. But from 3.4 admins
                	// can change the value of property site.public.group via JMX/properties files
                	// to be another group of their choosing.
                	// This then is the group that is given SiteConsumer access to newly created sites.
                	final String sitePublicGroup = sysAdminParams.getSitePublicGroup();
                	
					boolean groupExists = authorityService.authorityExists(sitePublicGroup);
                	if (!PermissionService.ALL_AUTHORITIES.equals(sitePublicGroup) && !groupExists)
                	{
                		// If the group does not exist, we cannot create the site.
                		throw new SiteServiceException(MSG_VISIBILITY_GROUP_MISSING, new Object[]{sitePublicGroup});
                	}
                	
                    permissionService.setPermission(siteNodeRef, sitePublicGroup, SITE_CONSUMER, true);
                }
                else if (SiteVisibility.MODERATED.equals(visibility) == true &&
                         permissions.contains(SITE_CONSUMER))
                {
                    // for moderated site EVERYONE has consumer access but site components do not.
                    permissionService.setPermission(siteNodeRef, PermissionService.ALL_AUTHORITIES, SITE_CONSUMER, true);
                }
                permissionService.setPermission(siteNodeRef,
                        PermissionService.ALL_AUTHORITIES,
                        PermissionService.READ_PERMISSIONS, true);
                authorityService.addAuthority(getSiteRoleGroup(shortName,
                        SiteModel.SITE_MANAGER, true), currentUser);

                // Return nothing
                return null;
            }

        }, AuthenticationUtil.getSystemUserName());

        // Return created site information
        Map<QName, Serializable> customProperties = getSiteCustomProperties(siteNodeRef);
        SiteInfo siteInfo = new SiteInfoImpl(sitePreset, shortName, title, description, visibility, customProperties, siteNodeRef);
        return siteInfo;
    }

    /**
     * Gets a map containing the site's custom properties
     * 
     * @return  Map<QName, Serializable>    map containing the custom properties of the site
     */
    private Map<QName, Serializable> getSiteCustomProperties(Map<QName, Serializable> properties)
    {
        Map<QName, Serializable> customProperties = new HashMap<QName, Serializable>(4);
        
        for (Map.Entry<QName, Serializable> entry : properties.entrySet())                
        {
            if (entry.getKey().getNamespaceURI().equals(SITE_CUSTOM_PROPERTY_URL) == true)
            {                
                customProperties.put(entry.getKey(), entry.getValue());
            }
        }  
        
        return customProperties;
    }
    
    /**
     * Gets a map containing the site's custom properties
     * 
     * @return  Map<QName, Serializable>    map containing the custom properties of the site
     */
    private Map<QName, Serializable> getSiteCustomProperties(NodeRef siteNodeRef)
    {
        Map<QName, Serializable> customProperties = new HashMap<QName, Serializable>(4);
        Map<QName, Serializable> properties = directNodeService.getProperties(siteNodeRef);
        
        for (Map.Entry<QName, Serializable> entry : properties.entrySet())                
        {
            if (entry.getKey().getNamespaceURI().equals(SITE_CUSTOM_PROPERTY_URL) == true)
            {                
                customProperties.put(entry.getKey(), entry.getValue());
            }
        }  
        
        return customProperties;
    }
    
    /**
     * @see org.alfresco.service.cmr.site.SiteService#getSiteGroup(java.lang.String)
     */
    public String getSiteGroup(String shortName)
    {
        return getSiteGroup(shortName, true);
    }

    /**
     * @see org.alfresco.service.cmr.site.SiteService#getSiteRoleGroup(java.lang.String,
     *      java.lang.String)
     */
    public String getSiteRoleGroup(String shortName, String role)
    {
        return getSiteRoleGroup(shortName, role, true);
    }

    /**
     * Helper method to get the name of the site group
     * 
     * @param shortName     site short name
     * @return String site group name
     */
    public String getSiteGroup(String shortName, boolean withGroupPrefix)
    {
        StringBuffer sb = new StringBuffer(64);
        if (withGroupPrefix == true)
        {
            sb.append(PermissionService.GROUP_PREFIX);
        }
        sb.append(SITE_PREFIX);
        sb.append(shortName);
        return sb.toString();
    }

    /**
     * Helper method to get the name of the site permission group
     * 
     * @param shortName     site short name
     * @param permission    permission name
     * @param withGroupPrefix - should the name have the GROUP_ prefix?
     * @return String site permission group name
     */
    public String getSiteRoleGroup(String shortName, String permission, boolean withGroupPrefix)
    {
        return getSiteGroup(shortName, withGroupPrefix) + '_' + permission;
    }

    /**
     * Gets a sites parent folder based on it's short name
     * 
     * @param shortName site short name
     * @return NodeRef the site's parent
     */
    private NodeRef getSiteParent(String shortName)
    {
        // TODO: For now just return the site root, later we may build folder
        //       structure based on the shortname to spread the sites about
        return getSiteRoot();
    }

    /**
    * {@inheritDoc}
     */
    public NodeRef getSiteRoot()
    {
        String tenantDomain = tenantAdminService.getCurrentUserDomain();
        NodeRef siteHomeRef = siteHomeRefs.get(tenantDomain);
        if (siteHomeRef == null)
        {
            siteHomeRef = AuthenticationUtil.runAs(new RunAsWork<NodeRef>()
            {
                public NodeRef doWork() throws Exception
                {
                    return retryingTransactionHelper.doInTransaction(new RetryingTransactionCallback<NodeRef>()
                    {
                        public NodeRef execute() throws Exception
                        {    
                            NodeRef result = null;
                            
                            // Get the root 'sites' folder
                            NodeRef rootNodeRef = directNodeService.getRootNode(SITE_STORE);
                            List<NodeRef> results = searchService.selectNodes(
                                    rootNodeRef,
                                    sitesXPath,
                                    null,
                                    namespaceService,
                                    false,
                                    SearchService.LANGUAGE_XPATH);
                            if (results.size() != 0)
                            {
                                result = results.get(0);
                            }
                            
                            return result;
                        }
                    }, true);
                }
            }, AuthenticationUtil.getSystemUserName());
            
            // There may be domains with no sites (e.g. JSF-only clients).
            if (siteHomeRef != null)
            {
                siteHomeRefs.put(tenantDomain, siteHomeRef);
            }
        }
        return siteHomeRef;
    }

    /**
     * @see org.alfresco.service.cmr.site.SiteService#listSites(java.lang.String, java.lang.String)
     */
    public List<SiteInfo> listSites(String nameFilter, String sitePresetFilter)
    {
        return listSites(nameFilter, sitePresetFilter, 0);
    }

    /**
     * @see org.alfresco.service.cmr.site.SiteService#listSites(java.lang.String, java.lang.String, int)
     */
    public List<SiteInfo> listSites(String nameFilter, String sitePresetFilter, int size)
    {
        List<SiteInfo> result;
        
        NodeRef siteRoot = getSiteRoot();
        if (siteRoot == null)
        {
            result = Collections.emptyList();
        }
        else
        {
            if (nameFilter != null && nameFilter.length() != 0 || sitePresetFilter != null && sitePresetFilter.length() > 0)
            {
                // get the sites that match the specified names
                StringBuilder query = new StringBuilder(128);
                query.append("+PARENT:\"").append(siteRoot.toString())
                     .append("\" +(");
                
                if (nameFilter != null && nameFilter.length() > 0)
                {
                    String escNameFilter = AbstractLuceneQueryParser.escape(nameFilter.replace('"', ' '));
                
                    query.append(" @cm\\:name:\"*" + escNameFilter + "*\"")
                    .append(" @cm\\:title:\"" + escNameFilter + "\"")
                    .append(" @cm\\:description:\"" + escNameFilter + "\"");
                }

                if (sitePresetFilter != null && sitePresetFilter.length() > 0)
                {
                    String escPresetFilter = AbstractLuceneQueryParser.escape(sitePresetFilter.replace('"', ' '));
                    query.append(" @st\\:sitePreset:\"" + escPresetFilter + "\"");
                }
                
                query.append(")");
                
                ResultSet results = this.searchService.query(
                        siteRoot.getStoreRef(),
                        SearchService.LANGUAGE_LUCENE,
                        query.toString(),
                        null);                        
                try
                {
                    result = new ArrayList<SiteInfo>(results.length());
                    for (NodeRef site : results.getNodeRefs())
                    {
                        // Ignore any node type that is not a "site"
                        QName siteClassName = this.directNodeService.getType(site);
                        if (this.dictionaryService.isSubClass(siteClassName, SiteModel.TYPE_SITE) == true)
                        {
                            result.add(createSiteInfo(site));
                            // break on max size limit reached
                            if (result.size() == size) break;
                        }
                    }
                }
                finally
                {
                    results.close();
                }
            }
            else
            {
                // Get ALL sites - this may be a very slow operation if there are many sites...
                List<ChildAssociationRef> assocs = this.nodeService.getChildAssocs(
                        siteRoot, ContentModel.ASSOC_CONTAINS,
                        RegexQNamePattern.MATCH_ALL);
                result = new ArrayList<SiteInfo>(assocs.size());
                for (ChildAssociationRef assoc : assocs)
                {
                    // Ignore any node type that is not a "site"
                    NodeRef site = assoc.getChildRef();
                    QName siteClassName = this.directNodeService.getType(site);
                    if (this.dictionaryService.isSubClass(siteClassName, SiteModel.TYPE_SITE) == true)
                    {            
                        result.add(createSiteInfo(site));
                        // break on max size limit reached
                        if (result.size() == size) break;
                    }
                }
            }
        }
        
        return result;
    }

    /**
     * @see org.alfresco.service.cmr.site.SiteService#listSites(java.lang.String)
     */
    public List<SiteInfo> listSites(final String userName)
    {
        // MT share - for activity service system callback
        if (tenantService.isEnabled() && (AuthenticationUtil.SYSTEM_USER_NAME.equals(AuthenticationUtil.getRunAsUser())) && tenantService.isTenantUser(userName))
        {
            final String tenantDomain = tenantService.getUserDomain(userName);
            
            return AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<List<SiteInfo>>()
            {
                public List<SiteInfo> doWork() throws Exception
                {
                    return listSitesImpl(userName);
                }
            }, tenantService.getDomainUser(AuthenticationUtil.getSystemUserName(), tenantDomain));
        }
        else
        {
            return listSitesImpl(userName);
        }
    }
    
    private List<SiteInfo> listSitesImpl(String userName)
    {
        List<SiteInfo> result = null;
        
        // get the Groups this user is contained within (at any level)
        Set<String> groups = this.authorityService.getContainingAuthorities(null, userName, false);
        Set<String> siteNames = new HashSet<String>(groups.size());
        // purge non Site related Groups and strip the group name down to the site "shortName" it relates too
        for (String group : groups)
        {
            if (group.startsWith(GROUP_SITE_PREFIX))
            {
                int roleIndex = group.lastIndexOf('_');
                String siteName;
                if (roleIndex + 1 <= GROUP_SITE_PREFIX_LENGTH)
                {
                    // There is no role associated
                    siteName = group.substring(GROUP_SITE_PREFIX_LENGTH);
                }
                else
                {
                    siteName = group.substring(GROUP_SITE_PREFIX_LENGTH, roleIndex);
                }
                siteNames.add(siteName);
            }
        }
        
        // retrieve the site nodes based on the list from the containing site groups
        NodeRef siteRoot = getSiteRoot();
        if (siteRoot == null)
        {
            result = new ArrayList<SiteInfo>(0);
        }
        else
        {
            List<String> siteList = new ArrayList<String>(siteNames);
            // ensure we do not trip over the getChildrenByName() 1000 item API limit!
            if (siteList.size() > 1000)
            {
                siteList = siteList.subList(0, 1000);
            }
            List<ChildAssociationRef> assocs = this.nodeService.getChildrenByName(
                    siteRoot,
                    ContentModel.ASSOC_CONTAINS,
                    siteList);
            result = new ArrayList<SiteInfo>(assocs.size());
            for (ChildAssociationRef assoc : assocs)
            {
                // Ignore any node that is not a "site" type
                NodeRef site = assoc.getChildRef();
                QName siteClassName = this.directNodeService.getType(site);
                if (this.dictionaryService.isSubClass(siteClassName, SiteModel.TYPE_SITE))
                {
                    result.add(createSiteInfo(site));
                }
            }
        }
        
        return result;
    }

    /**
     * Creates a site information object given a site node reference
     * 
     * @param siteNodeRef
     *            site node reference
     * @return SiteInfo site information object
     */
    private SiteInfo createSiteInfo(NodeRef siteNodeRef)
    {
        SiteInfo siteInfo = null;
        
        // Get the properties
        Map<QName, Serializable> properties = this.directNodeService.getProperties(siteNodeRef);
        String shortName = (String) properties.get(ContentModel.PROP_NAME);
        String sitePreset = (String) properties.get(PROP_SITE_PRESET);
        String title = (String) properties.get(ContentModel.PROP_TITLE);
        String description = (String) properties.get(ContentModel.PROP_DESCRIPTION);

        // Get the visibility of the site
        SiteVisibility visibility = getSiteVisibility(siteNodeRef);
        
        // Create and return the site information
        Map<QName, Serializable> customProperties = getSiteCustomProperties(properties);
        siteInfo = new SiteInfoImpl(sitePreset, shortName, title, description, visibility, customProperties, siteNodeRef);
        
        return siteInfo;
    }
    
    /**
     * Helper method to get the visibility of the site.  If no value is present in the repository then it is calculated from the 
     * set permissions.  This will maintain backwards compatibility with earlier versions of the service implementation.
     * 
     * @param siteNodeRef       site node reference
     * @return SiteVisibility   site visibility
     */
    private SiteVisibility getSiteVisibility(NodeRef siteNodeRef)
    {
        SiteVisibility visibility = SiteVisibility.PRIVATE;
        
        // Get the visibility value stored in the repo
        String visibilityValue = (String)this.directNodeService.getProperty(siteNodeRef, SiteModel.PROP_SITE_VISIBILITY);
        
        // To maintain backwards compatibility calculate the visibility from the permissions
        // if there is no value specified on the site node
        if (visibilityValue == null)
        {
            // Examine each permission to see if this is a public site or not
            Set<AccessPermission> permissions = this.permissionService.getAllSetPermissions(siteNodeRef);
            for (AccessPermission permission : permissions)
            {
                if (permission.getAuthority().equals(PermissionService.ALL_AUTHORITIES) == true && 
                    permission.getPermission().equals(SITE_CONSUMER) == true)
                {
                    visibility = SiteVisibility.PUBLIC;
                    break;
                }
            }            
        }
        else
        {
            // Create the enum value from the string
            visibility = SiteVisibility.valueOf(visibilityValue);
        }
        
        return visibility;
    }

    /**
     * @see org.alfresco.service.cmr.site.SiteService#getSite(java.lang.String)
     */
    public SiteInfo getSite(final String shortName)
    {
        // MT share - for activity service system callback
        if (tenantService.isEnabled() && (AuthenticationUtil.SYSTEM_USER_NAME.equals(AuthenticationUtil.getRunAsUser())) && tenantService.isTenantName(shortName))
        {
            final String tenantDomain = tenantService.getDomain(shortName);
            final String sName = tenantService.getBaseName(shortName, true);
            
            return AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<SiteInfo>()
            {
                public SiteInfo doWork() throws Exception
                {
                    SiteInfo site = getSiteImpl(sName);
                    return new SiteInfoImpl(site.getSitePreset(), shortName, site.getTitle(), site.getDescription(), site.getVisibility(), site.getCustomProperties(), site.getNodeRef());
                }
            }, tenantService.getDomainUser(AuthenticationUtil.getSystemUserName(), tenantDomain));
        }
        else
        {
            return getSiteImpl(shortName);
        }
    }
    
    /**
     * Get the site implementation given a short name
     * 
     * @param shortName
     * @return
     */
    private SiteInfo getSiteImpl(String shortName)
    {
        SiteInfo result = null;

        // Get the site node
        NodeRef siteNodeRef = getSiteNodeRef(shortName);
        if (siteNodeRef != null)
        {
            // Create the site info
            result = createSiteInfo(siteNodeRef);
        }

        // Return the site information
        return result;
    }
    
    /**
     * @see org.alfresco.service.cmr.site.SiteService#getSite(org.alfresco.service.cmr.repository.NodeRef)
     */
    public SiteInfo getSite(NodeRef nodeRef)
    {
        SiteInfo siteInfo = null;
        NodeRef siteNodeRef = getSiteNodeRef(nodeRef);
        if (siteNodeRef != null)
        {
            siteInfo = createSiteInfo(siteNodeRef);
        }
        return siteInfo;
    }
    
    /**
     * This method gets the <code>st:site</code> NodeRef for the Share Site which contains the given NodeRef.
     * If the given NodeRef is not contained within a Share Site, then <code>null</code> is returned.
     * 
     * @param nodeRef   the node whose containing site is to be found.
     * @return NodeRef  site node reference or null if node is not in a site
     */
    private NodeRef getSiteNodeRef(NodeRef nodeRef)
    {
        NodeRef siteNodeRef = null;        
        QName nodeRefType = directNodeService.getType(nodeRef);
        if (dictionaryService.isSubClass(nodeRefType, TYPE_SITE) == true)
        {
            siteNodeRef = nodeRef;
        }
        else
        {
            ChildAssociationRef primaryParent = nodeService.getPrimaryParent(nodeRef);
            if (primaryParent != null && primaryParent.getParentRef() != null)
            {
                siteNodeRef = getSiteNodeRef(primaryParent.getParentRef());
            }
        }        
        return siteNodeRef;
    }

    /**
     * Gets the site's node reference based on its short name
     * 
     * @param shortName    short name
     * 
     * @return NodeRef node reference
     */
    private NodeRef getSiteNodeRef(final String shortName)
    {
        return getSiteNodeRef(shortName, true);
    }
    
    /**
     * Gets the site's node reference based on its short name
     * 
     * @param shortName    short name
     * @param enforcePermissions should we ensure that we have access to this node?
     * 
     * @return NodeRef node reference
     */
    private NodeRef getSiteNodeRef(final String shortName, boolean enforcePermissions)
    {
        final String cacheKey = this.tenantAdminService.getCurrentUserDomain() + '_' + shortName;
        NodeRef siteNodeRef = this.siteNodeRefs.get(cacheKey);
        if (siteNodeRef != null)
        {
            // test for existance - and remove from cache if no longer exists
            if (!this.directNodeService.exists(siteNodeRef))
            {
                this.siteNodeRefs.remove(cacheKey);
                siteNodeRef = null;
            }
        }
        else
        {
            // not in cache - find and store
            final NodeRef siteRoot = getSiteParent(shortName);
            
            siteNodeRef = AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<NodeRef>()
            {
                public NodeRef doWork() throws Exception
                {
                    // the site "short name" directly maps to the cm:name property
                    NodeRef siteNode = directNodeService.getChildByName(siteRoot, ContentModel.ASSOC_CONTAINS, shortName);
                    
                    // cache the result if found - null results will be required to ensure new sites are found later
                    if (siteNode != null)
                    {
                        siteNodeRefs.put(cacheKey, siteNode);
                    }
                    return siteNode;
                }
            }, AuthenticationUtil.getSystemUserName());
        }
        if (enforcePermissions)
        {
            return siteNodeRef == null
                    || !this.permissionService.hasPermission(siteNodeRef, PermissionService.READ_PROPERTIES).equals(
                            AccessStatus.ALLOWED) ? null : siteNodeRef;
        }
        else
        {
            return siteNodeRef;
        }
    }

    /**
     * @see org.alfresco.service.cmr.site.SiteService#updateSite(org.alfresco.service.cmr.site.SiteInfo)
     */
    public void updateSite(SiteInfo siteInfo)
    {
        String shortName = siteInfo.getShortName();
        NodeRef siteNodeRef = getSiteNodeRef(shortName);
        if (siteNodeRef == null)
        {
            throw new SiteServiceException(MSG_CAN_NOT_UPDATE, new Object[]{siteInfo.getShortName()});            
        }
        
        // Get the sites properties
        Map<QName, Serializable> properties = this.directNodeService.getProperties(siteNodeRef);
        
        // Update the properties of the site
        // Note: the site preset and short name should never be updated!
        properties.put(ContentModel.PROP_TITLE, siteInfo.getTitle());
        properties.put(ContentModel.PROP_DESCRIPTION, siteInfo.getDescription());

        // Update the isPublic flag
        SiteVisibility currentVisibility = getSiteVisibility(siteNodeRef);
        SiteVisibility updatedVisibility = siteInfo.getVisibility();
        if (currentVisibility.equals(updatedVisibility) == false)
        {
            // visibility has changed   
            logger.debug("site:" + shortName + " visibility has changed from: " + currentVisibility + "to: " + updatedVisibility);
            
            // visibility has changed.
            // Remove current visibility permissions
            if (SiteVisibility.PUBLIC.equals(currentVisibility) == true)
            {
                this.permissionService.deletePermission(siteNodeRef, PermissionService.ALL_AUTHORITIES, SITE_CONSUMER);
            }
            else if (SiteVisibility.MODERATED.equals(currentVisibility) == true)
            {
                this.permissionService.deletePermission(siteNodeRef, PermissionService.ALL_AUTHORITIES, SITE_CONSUMER);
                
                /**
                 * update the containers
                 */
                List<FileInfo> folders = fileFolderService.listFolders(siteNodeRef);
                for(FileInfo folder : folders)
                {
                    NodeRef containerNodeRef = folder.getNodeRef();
                    this.permissionService.setInheritParentPermissions(containerNodeRef, true);   
                }
            }
            
            // Add new visibility permissions
            if (SiteVisibility.PUBLIC.equals(updatedVisibility) == true)
            {
                this.permissionService.setPermission(siteNodeRef, PermissionService.ALL_AUTHORITIES, SITE_CONSUMER, true);
            }
            else if (SiteVisibility.MODERATED.equals(updatedVisibility) == true)
            {
                this.permissionService.setPermission(siteNodeRef, PermissionService.ALL_AUTHORITIES, SITE_CONSUMER, true);
                /**
                 * update the containers
                 */
                List<FileInfo> folders = fileFolderService.listFolders(siteNodeRef);
                for(FileInfo folder : folders)
                {
                    NodeRef containerNodeRef = folder.getNodeRef();
                    setModeratedPermissions(shortName, containerNodeRef);
                }
            }
            
            // Update the site node reference with the updated visibility value
            properties.put(SiteModel.PROP_SITE_VISIBILITY, siteInfo.getVisibility().toString());
        }
        
        // Set the updated properties back onto the site node reference
        this.nodeService.setProperties(siteNodeRef, properties);
    }
    
    /**
     * @see org.alfresco.service.cmr.site.SiteService#deleteSite(java.lang.String)
     */
    public void deleteSite(final String shortName)
    {
        logger.debug("delete site :" + shortName);
        final NodeRef siteNodeRef = getSiteNodeRef(shortName);
        if (siteNodeRef == null)
        {
            throw new SiteServiceException(MSG_CAN_NOT_DELETE, new Object[]{shortName});
        }
        final QName siteType = directNodeService.getType(siteNodeRef);

        // Delete the cached reference
        String cacheKey = this.tenantAdminService.getCurrentUserDomain() + '_' + shortName;
        this.siteNodeRefs.remove(cacheKey);
        
        // The default behaviour is that sites cannot be deleted. But we disable that behaviour here
        // in order to allow site deletion only via this service. Share calls this service for deletion.
        //
        // See ALF-7888 for some background on this issue
        behaviourFilter.disableBehaviour(siteNodeRef, ContentModel.ASPECT_UNDELETABLE);
        try
        {
            // Delete the site node, marking it as "not to be archived" on the way.
            // The site node will be permanently deleted.
            
            this.nodeService.addAspect(siteNodeRef, ContentModel.ASPECT_TEMPORARY, null);
            this.nodeService.deleteNode(siteNodeRef);
        }
        finally
        {
            behaviourFilter.enableBehaviour(siteNodeRef, ContentModel.ASPECT_UNDELETABLE);
        }

        // Delete the associated groups
        AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Object>()
        {
            public Object doWork() throws Exception
            {
                // Delete the master site group
                authorityService.deleteAuthority(getSiteGroup(shortName, true), false);
                
                // Iterate over the role related groups and delete then
                Set<String> permissions = permissionService.getSettablePermissions(siteType);
                for (String permission : permissions)
                {
                    String siteRoleGroup = getSiteRoleGroup(shortName, permission, true);
                    authorityService.deleteAuthority(siteRoleGroup);
                }
                
                return null;
            }
        }, AuthenticationUtil.getSystemUserName());
        
        logger.debug("site deleted :" + shortName);
    }

    /**
     * @see org.alfresco.service.cmr.site.SiteService#listMembers(java.lang.String, java.lang.String, java.lang.String, int)
     */
    public Map<String, String> listMembers(String shortName, String nameFilter, String roleFilter, int size)
    {
        return listMembers(shortName, nameFilter, roleFilter, size, false);
    }
    
    /**
     * @see org.alfresco.service.cmr.site.SiteService#listMembers(String, String, String, int, boolean)
     */
    public Map<String, String> listMembers(String shortName, final String nameFilter, final String roleFilter, final int size, final boolean collapseGroups)
    {
        // MT share - for activity service system callback
        if (tenantService.isEnabled() && (AuthenticationUtil.SYSTEM_USER_NAME.equals(AuthenticationUtil.getRunAsUser())) && tenantService.isTenantName(shortName))
        {
            final String tenantDomain = tenantService.getDomain(shortName);
            final String sName = tenantService.getBaseName(shortName, true);
            
            return AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Map<String, String>>()
            {
                public Map<String, String> doWork() throws Exception
                {
                    return listMembersImpl(sName, nameFilter, roleFilter, size, collapseGroups);
                }
            }, tenantService.getDomainUser(AuthenticationUtil.getSystemUserName(), tenantDomain));
        }
        else
        {
            return listMembersImpl(shortName, nameFilter, roleFilter, size, collapseGroups);
        }
    }
    
    private Map<String, String> listMembersImpl(String shortName, String nameFilter, String roleFilter, int size, boolean collapseGroups)
    {
        NodeRef siteNodeRef = getSiteNodeRef(shortName);
        if (siteNodeRef == null)
        {
            throw new SiteServiceException(MSG_SITE_NO_EXIST, new Object[]{shortName});
        }
        
        // Build an array of name filter tokens pre lowercased to test against person properties
        // We require that matching people have at least one match against one of these on
        //  either their firstname or last name
        // For groups, we require a match against the whole filter on the group name
        String[] nameFilters = new String[0];
        if (nameFilter != null && nameFilter.length() != 0)
        {
            StringTokenizer t = new StringTokenizer(nameFilter, " ");
            nameFilters = new String[t.countTokens()];
            for (int i=0; t.hasMoreTokens(); i++)
            {
                nameFilters[i] = t.nextToken().toLowerCase();
            }
        }
        
        Map<String, String> members = new HashMap<String, String>(32);

        QName siteType = directNodeService.getType(siteNodeRef);
        Set<String> permissions = this.permissionService.getSettablePermissions(siteType);
        Map<String, String> groupsToExpand = new HashMap<String, String>(32);
        for (String permission : permissions)
        {
            if (roleFilter == null || roleFilter.length() == 0 || roleFilter.equals(permission))
            {
                String groupName = getSiteRoleGroup(shortName, permission, true);
                Set<String> authorities = this.authorityService.getContainedAuthorities(null, groupName, true);
                for (String authority : authorities)
                {
                    switch (AuthorityType.getAuthorityType(authority))
                    {
                    case USER:
                        boolean addUser = true;
                        if (nameFilter != null && nameFilter.length() != 0 && !nameFilter.equals(authority))
                        {
                            // found a filter - does it match person first/last name?
                            addUser = matchPerson(nameFilters, authority);
                        }
                        if (addUser)
                        {
                            // Add the user and their permission to the returned map
                            members.put(authority, permission);
                            
                            // break on max size limit reached
                            if (members.size() == size) break;
                        }
                        break;
                    case GROUP:
                        if (collapseGroups)
                        {
                            if (!groupsToExpand.containsKey(authority))
                            {
                                groupsToExpand.put(authority, permission);
                            }
                        }
                        else
                        {
                            if (nameFilter != null && nameFilter.length() != 0)
                            {
                                // found a filter - does it match Group name part?
                                if (authority.substring(GROUP_PREFIX_LENGTH).toLowerCase().contains(nameFilter.toLowerCase()))
                                {
                                    members.put(authority, permission);
                                }
                            }
                            else
                            {
                                // No name filter add this group
                                members.put(authority, permission);
                            }
                        }
                        break;
                    }
                }
            }
        }
        
        if (collapseGroups)
        {
            for (Map.Entry<String,String> entry : groupsToExpand.entrySet())
            {                
                Set<String> subUsers = this.authorityService.getContainedAuthorities(AuthorityType.USER, entry.getKey(), false);
                for (String subUser : subUsers)
                {
                    boolean addUser = true;
                    if (nameFilter != null && nameFilter.length() != 0 && !nameFilter.equals(subUser))
                    {
                        // found a filter - does it match person first/last name?
                        addUser = matchPerson(nameFilters, subUser);
                    }
                    if (addUser)
                    {
                        // Add the collapsed user into the members list if they do not already appear in the list 
                        if (members.containsKey(subUser) == false)
                        {
                            members.put(subUser, entry.getValue());
                        }
                        
                        // break on max size limit reached
                        if (members.size() == size) break;
                    }
                }
            }         
        }

        return members;
    }

    /**
     * Helper to match name filters to Person properties.
     * 
     * One of the user's firstname or lastname must match at least
     *  one of the filters given.
     * 
     * @param filter
     * @param username
     * @return
     */
    private boolean matchPerson(final String[] nameFilters, final String username)
    {
        boolean addUser = false;
        
        try
        {
           NodeRef person = personService.getPerson(username, false);
           String firstName = (String)directNodeService.getProperty(person, ContentModel.PROP_FIRSTNAME);
           String lastName = (String)directNodeService.getProperty(person, ContentModel.PROP_LASTNAME);

           final String lowFirstName = (firstName != null ? firstName.toLowerCase() : "");
           final String lowLastName = (lastName != null ? lastName.toLowerCase() : "");
           for (int i=0; i<nameFilters.length; i++)
           {
              if (lowFirstName.indexOf(nameFilters[i]) != -1)
              {
                 addUser = true;
                 break;
              }
              else if (lowLastName.indexOf(nameFilters[i]) != -1)
              {
                 addUser = true;
                 break;
              }
           }
        }
        catch(NoSuchPersonException e)
        {
           // Group references a deleted user, shouldn't normally happen
        }
        
        return addUser;
    }

    /**
     * @see org.alfresco.service.cmr.site.SiteService#getMembersRole(java.lang.String,
     *      java.lang.String)
     */
    public String getMembersRole(String shortName, String authorityName) 
    {
        String result = null;
        List<String> roles = getMembersRoles(shortName, authorityName);
        if (roles.size() != 0)
        {
        	if (roles.size() > 1 && roleComparator != null)
        	{
        		// Need to sort the roles into the most important first.
        		SortedSet<String> sortedRoles = new TreeSet<String>(roleComparator);
        		for (String role : roles)
        		{
        			sortedRoles.add(role);
        		}
        		result = sortedRoles.first();
        	}
        	else
        	{
        		// don't search on precedence or only one result
        		result = roles.get(0);
        	}
        }
        return result;
    }
        
    public List<String> getMembersRoles(String shortName, String authorityName)
    {
        List<String> result = new ArrayList<String>(5);
        List<String> groups = getPermissionGroups(shortName, authorityName);
        for (String group : groups)
        {
            int index = group.lastIndexOf('_');
            if (index != -1)
            {
                result.add(group.substring(index + 1));
            }
        }
        return result;
    }
    
    /**
     * Helper method to get the permission groups for a given authority on a site.
     * Returns empty List if the user does not have a explicit membership to the site.
     * 
     * A user permission will take precedence over a permission obtained via a group.
     * 
     * @param siteShortName     site short name
     * @param authorityName     authority name
     * @return List<String>     Permission groups, empty list if no explicit membership set
     */
    private List<String> getPermissionGroups(String siteShortName, String authorityName)
    {
        NodeRef siteNodeRef = getSiteNodeRef(siteShortName);
        if (siteNodeRef == null) 
        { 
            throw new SiteServiceException(MSG_SITE_NO_EXIST, new Object[] { siteShortName });
        }
        
        List<String> fullResult = new ArrayList<String>(5);
        QName siteType = directNodeService.getType(siteNodeRef);
        Set<String> roles = this.permissionService.getSettablePermissions(siteType);

        // First use the authority's cached recursive group memberships to answer the question quickly
        Set<String> authorityGroups = this.authorityService.getContainingAuthorities(AuthorityType.GROUP,
                authorityName, false);
        for (String role : roles)
        {
            String roleGroup = getSiteRoleGroup(siteShortName, role, true);
            if (authorityGroups.contains(roleGroup))
            {
                fullResult.add(roleGroup);
            }
        }
        
        // Unfortunately, due to direct membership taking precendence, we can't answer the question quickly if more than one role has been inherited
        if (fullResult.size() <= 1)
        {
            return fullResult;
        }
        
        // Check direct group memberships
        List<String> result = new ArrayList<String>(5);
        authorityGroups = this.authorityService.getContainingAuthorities(AuthorityType.GROUP,
                authorityName, true);
        for (String role : roles)
        {
            String roleGroup = getSiteRoleGroup(siteShortName, role, true);
            if (authorityGroups.contains(roleGroup))
            {
                result.add(roleGroup);
            }
        }
        
        // If there are user permissions then they take priority
        return result.size() > 0 ? result : fullResult;
    }

    /**
     * @see org.alfresco.service.cmr.site.SiteService#getSiteRoles()
     */
    public List<String> getSiteRoles()
    {
        return getSiteRoles(SiteModel.TYPE_SITE);
    }

    /**
     * @see org.alfresco.service.cmr.site.SiteService#getSiteRoles(String)
     */
    public List<String> getSiteRoles(String shortName)
    {
        NodeRef siteNodeRef = getSiteNodeRef(shortName);
        if (siteNodeRef == null) 
        { 
            throw new SiteServiceException(MSG_SITE_NO_EXIST, new Object[] { shortName });
        }
        QName siteType = directNodeService.getType(siteNodeRef);
        return getSiteRoles(siteType);
    }

    /**
     * @see org.alfresco.service.cmr.site.SiteService#getSiteRoles()
     * @see org.alfresco.service.cmr.site.SiteService#getSiteRoles(String)
     */
    public List<String> getSiteRoles(QName type)
    {
        Set<String> permissions = permissionService.getSettablePermissions(type);
        return new ArrayList<String>(permissions);
    }

    /**
     * @see org.alfresco.service.cmr.site.SiteService#isMember(java.lang.String, java.lang.String)
     */
    public boolean isMember(String shortName, String authorityName)
    {
        return (!getPermissionGroups(shortName, authorityName).isEmpty());
    }

    /**
     * @see org.alfresco.service.cmr.site.SiteService#removeMembership(java.lang.String, java.lang.String)
     */
    public void removeMembership(final String shortName, final String authorityName)
    {
        final NodeRef siteNodeRef = getSiteNodeRef(shortName);
        if (siteNodeRef == null)
        {
            throw new SiteServiceException(MSG_SITE_NO_EXIST, new Object[]{shortName});
        }

        // TODO what do we do about the user if they are in a group that has
        // rights to the site?

        // Get the current user
        String currentUserName = AuthenticationUtil.getFullyAuthenticatedUser();

        // Get the user current role
        final String role = getMembersRole(shortName, authorityName);
        if (role != null)
        {
            // Check that we are not about to remove the last site manager
            checkLastManagerRemoval(shortName, authorityName, role);
            
            // If ...
            // -- the current user has change permissions rights on the site
            // or
            // -- the user is ourselves
            if ((currentUserName.equals(authorityName) == true) ||
                (permissionService.hasPermission(siteNodeRef, PermissionService.CHANGE_PERMISSIONS) == AccessStatus.ALLOWED))
            {
                // Run as system user
                AuthenticationUtil.runAs(
                    new AuthenticationUtil.RunAsWork<Object>()
                    {
                        public Object doWork() throws Exception
                        {
                            // Remove the user from the current permission
                            // group
                            String currentGroup = getSiteRoleGroup(shortName, role, true);
                            authorityService.removeAuthority(currentGroup, authorityName);
                            
                            return null;
                        }
                    }, AuthenticationUtil.SYSTEM_USER_NAME);

                // Raise events
                AuthorityType authorityType = AuthorityType.getAuthorityType(authorityName);
                if (authorityType == AuthorityType.USER)
                {
                    activityService.postActivity(
                            ActivityType.SITE_USER_REMOVED, shortName,
                            ACTIVITY_TOOL, getActivityUserData(authorityName, ""));
                }
                else if (authorityType == AuthorityType.GROUP)
                {
                    activityService.postActivity(
                            ActivityType.SITE_GROUP_REMOVED, shortName,
                            ACTIVITY_TOOL, getActivityGroupData(authorityName, ""));
                }
            }
            else
            {
                // Throw an exception
                throw new SiteServiceException(MSG_CAN_NOT_REMOVE_MSHIP, new Object[]{shortName});
            }
        } 
        else
        {
            // Throw an exception
            throw new SiteServiceException(MSG_CAN_NOT_REMOVE_MSHIP, new Object[]{shortName});
        }
    }

    /**
     * @see org.alfresco.service.cmr.site.SiteService#setMembership(java.lang.String,
     *      java.lang.String, java.lang.String)
     */
    public void setMembership(final String shortName, 
                              final String authorityName,
                              final String role)
    {
        final NodeRef siteNodeRef = getSiteNodeRef(shortName);
        if (siteNodeRef == null)
        {
            throw new SiteServiceException(MSG_SITE_NO_EXIST, new Object[]{shortName});
        }

        // Get the user's current role
        final String currentRole = getMembersRole(shortName, authorityName);

        // Do nothing if the role of the user is not being changed
        if (currentRole == null || role.equals(currentRole) == false)
        {
            // TODO if this is the only site manager do not down grade their
            // permissions
            
            // Get the visibility of the site
            SiteVisibility visibility = getSiteVisibility(siteNodeRef);

            // If we are ...
            // -- the current user has change permissions rights on the site
            // or we are ...
            // -- referring to a public site and
            // -- the role being set is consumer and
            // -- the user being added is ourselves and
            // -- the member does not already have permissions
            // ... then we can set the permissions as system user
            final String currentUserName = AuthenticationUtil.getFullyAuthenticatedUser();
            if ((permissionService.hasPermission(siteNodeRef, PermissionService.CHANGE_PERMISSIONS) == AccessStatus.ALLOWED) || 
                (SiteVisibility.PUBLIC.equals(visibility) == true && 
                 role.equals(SiteModel.SITE_CONSUMER) == true && 
                 authorityName.equals(currentUserName) == true && 
                 currentRole == null))
            {
                // Check that we are not about to remove the last site manager
                checkLastManagerRemoval(shortName, authorityName, currentRole);
                
                // Run as system user
                AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Object>()
                {
                    public Object doWork() throws Exception
                    {
                        if (currentRole != null)
                        {
                            // Remove the user from the current
                            // permission group
                            String currentGroup = getSiteRoleGroup(shortName, currentRole, true);
                            authorityService.removeAuthority(currentGroup, authorityName);
                        }

                        // Add the user to the new permission group
                        String newGroup = getSiteRoleGroup(shortName, role, true);
                        authorityService.addAuthority(newGroup, authorityName);

                        return null;
                    }

                }, AuthenticationUtil.SYSTEM_USER_NAME);

                if (currentRole == null)
                {
                    AuthorityType authorityType = AuthorityType.getAuthorityType(authorityName);
                    if (authorityType == AuthorityType.USER)
                    {
                        activityService.postActivity(
                                ActivityType.SITE_USER_JOINED, shortName,
                                ACTIVITY_TOOL, getActivityUserData(authorityName, role));
                    } 
                    else if (authorityType == AuthorityType.GROUP)
                    {
                        activityService.postActivity(
                                ActivityType.SITE_GROUP_ADDED, shortName,
                                ACTIVITY_TOOL, getActivityGroupData(authorityName, role));                   
                    }
                } 
                else
                {
                    AuthorityType authorityType = AuthorityType.getAuthorityType(authorityName);
                    if (authorityType == AuthorityType.USER)
                    {
                        activityService.postActivity(
                                ActivityType.SITE_USER_ROLE_UPDATE, shortName,
                                ACTIVITY_TOOL, getActivityUserData(authorityName, role));
                    } 
                    else if (authorityType == AuthorityType.GROUP)
                    {
                        activityService.postActivity(
                                ActivityType.SITE_GROUP_ROLE_UPDATE, shortName,
                                ACTIVITY_TOOL, getActivityGroupData(authorityName, role));
                    }
                }
            } 
            else
            {
                // Raise a permission exception
                throw new SiteServiceException(MSG_CAN_NOT_CHANGE_MSHIP, new Object[]{shortName});
            }
        }
    }

    /**
     * @see org.alfresco.service.cmr.site.SiteService#createContainer(java.lang.String,
     *      java.lang.String, org.alfresco.service.namespace.QName,
     *      java.util.Map)
     */
    public NodeRef createContainer(String shortName, 
                                   String componentId,
                                   QName containerType, 
                                   Map<QName, Serializable> containerProperties)
    {
        // Check for the component id
        ParameterCheck.mandatoryString("componentId", componentId);

        // retrieve site
        NodeRef siteNodeRef = getSiteNodeRef(shortName);
        if (siteNodeRef == null)
        {
            throw new SiteServiceException(MSG_SITE_NO_EXIST, new Object[]{shortName});
        }
        
        // Update the isPublic flag
        SiteVisibility siteVisibility = getSiteVisibility(siteNodeRef);

        // retrieve component folder within site
        NodeRef containerNodeRef = null;
        try
        {
            containerNodeRef = findContainer(siteNodeRef, componentId);
        } 
        catch (FileNotFoundException e)
        {
            //NOOP
        }

        // create the container node reference
        if (containerNodeRef == null)
        {
            if (containerType == null)
            {
                containerType = ContentModel.TYPE_FOLDER;
            }

            // create component folder
            FileInfo fileInfo = fileFolderService.create(siteNodeRef,
                    componentId, containerType);

            // Get the created container
            containerNodeRef = fileInfo.getNodeRef();

            // Set the properties if they have been provided
            if (containerProperties != null)
            {
                Map<QName, Serializable> props = this.directNodeService
                        .getProperties(containerNodeRef);
                props.putAll(containerProperties);
                this.nodeService.setProperties(containerNodeRef, props);
            }

            // Add the container aspect
            Map<QName, Serializable> aspectProps = new HashMap<QName, Serializable>(1, 1.0f);
            aspectProps.put(SiteModel.PROP_COMPONENT_ID, componentId);
            this.nodeService.addAspect(containerNodeRef, ASPECT_SITE_CONTAINER,
                    aspectProps);
            
            // Set permissions on the container
            if(SiteVisibility.MODERATED.equals(siteVisibility))
            {
                setModeratedPermissions(shortName, containerNodeRef);
            }
            
            // Make the container a tag scope
            this.taggingService.addTagScope(containerNodeRef);
        }

        return containerNodeRef;
    }
    
    /**
     * This method recursively cleans the site permissions on the specified NodeRef and all its primary
     * descendants. This consists of
     * <ul>
     * <li>the removal of all site permissions pertaining to a site other than the containingSite</li>
     * </ul>
     * If the containingSite is <code>null</code> then the targetNode's current containing site is used.
     * 
     * @param targetNode
     * @param containingSite the site which the site is a member of. If <code>null</code>, it will be calculated.
     */
    @Override
    public void cleanSitePermissions(final NodeRef targetNode, SiteInfo containingSite)
    {
        this.sitesPermissionsCleaner.cleanSitePermissions(targetNode, containingSite);
    }
    
    /**
     * Moderated sites have separate ACLs on each component and don't inherit from the
     * site which has consumer role for everyone.
     */    
    private void setModeratedPermissions(String shortName, NodeRef containerNodeRef)   
    {
        NodeRef siteNodeRef = getSiteNodeRef(shortName);
        if (siteNodeRef == null) 
        { 
            throw new SiteServiceException(MSG_SITE_NO_EXIST, new Object[] { shortName });
        }
        
        QName siteType = directNodeService.getType(siteNodeRef);
        Set<String> permissions = permissionService.getSettablePermissions(siteType);
        for (String permission : permissions)
        {
            String permissionGroup = getSiteRoleGroup(shortName, permission, true);
            // Assign the group the relevant permission on the site
            permissionService.setPermission(containerNodeRef, permissionGroup, permission, true);
        }  
        permissionService.setPermission(containerNodeRef,
            PermissionService.ALL_AUTHORITIES,
            PermissionService.READ_PERMISSIONS, true);
        
        this.permissionService.setInheritParentPermissions(containerNodeRef, false);
    }


    /**
     * @see org.alfresco.service.cmr.site.SiteService#getContainer(java.lang.String)
     */
    public NodeRef getContainer(String shortName, String componentId)
    {
        ParameterCheck.mandatoryString("componentId", componentId);

        // retrieve site
        NodeRef siteNodeRef = getSiteNodeRef(shortName);
        if (siteNodeRef == null)
        {
            throw new SiteServiceException(MSG_SITE_NO_EXIST, new Object[]{shortName});
        }

        // retrieve component folder within site
        // NOTE: component id is used for folder name
        NodeRef containerNodeRef = null;
        try
        {
            containerNodeRef = findContainer(siteNodeRef, componentId);
        } 
        catch (FileNotFoundException e)
        {
            //NOOP
        }

        return containerNodeRef;
    }

    /**
     * @see org.alfresco.service.cmr.site.SiteService#hasContainer(java.lang.String)
     */
    public boolean hasContainer(final String shortName, final String componentId)
    {
        ParameterCheck.mandatoryString("componentId", componentId);

        // retrieve site
        final NodeRef siteNodeRef = getSiteNodeRef(shortName);
        if (siteNodeRef == null)
        {
            throw new SiteServiceException(MSG_SITE_NO_EXIST, new Object[]{shortName});
        }

        // retrieve component folder within site
        // NOTE: component id is used for folder name
        boolean hasContainer = false;
        
        NodeRef containerRef = AuthenticationUtil.runAs(new RunAsWork<NodeRef>()
        {
            public NodeRef doWork() throws Exception
            {
                return retryingTransactionHelper.doInTransaction(new RetryingTransactionCallback<NodeRef>()
                {
                    public NodeRef execute() throws Exception
                    {
                        try 
                        {
                            return findContainer(siteNodeRef, componentId);
                        }
                        catch (FileNotFoundException e)
                        {
                            return null;
                        }
                    }
                }, true);
            }
        }, AuthenticationUtil.getSystemUserName());
            
        if(containerRef != null)
        {
            hasContainer = true;
        }
        
        return hasContainer;
    }

    /**
     * Locate site "container" folder for component
     * 
     * @param siteNodeRef
     *            site
     * @param componentId
     *            component id
     * @return "container" node ref, if it exists
     * @throws FileNotFoundException
     */
    private NodeRef findContainer(NodeRef siteNodeRef, String componentId)
            throws FileNotFoundException
    {
        List<String> paths = new ArrayList<String>(1);
        paths.add(componentId);
        FileInfo fileInfo = fileFolderService.resolveNamePath(siteNodeRef,
                paths);
        if (!fileInfo.isFolder())
        {
            throw new SiteServiceException(MSG_SITE_CONTAINER_NOT_FOLDER, new Object[]{fileInfo.getName()});
        }
        return fileInfo.getNodeRef();
    }

    /**
     * Helper method to get the activity data for a user
     * 
     * @param userName      user name
     * @param role          role
     * @return
     */
    private String getActivityUserData(String userName, String role)
    {
        String memberFN = "";
        String memberLN = "";
        NodeRef person = personService.getPerson(userName);
        if (person != null)
        {
            memberFN = (String) directNodeService.getProperty(person,
                    ContentModel.PROP_FIRSTNAME);
            memberLN = (String) directNodeService.getProperty(person,
                    ContentModel.PROP_LASTNAME);
        }

        try
        {
            JSONObject activityData = new JSONObject();
            activityData.put("role", role);
            activityData.put("memberUserName", userName);
            activityData.put("memberFirstName", memberFN);
            activityData.put("memberLastName", memberLN);
            activityData.put("title", (memberFN + " " + memberLN + " ("
                    + userName + ")").trim());
            return activityData.toString();
        } catch (JSONException je)
        {
            // log error, subsume exception
            logger.error("Failed to get activity data: " + je);
            return "";
        }
    }
    
    /**
     * Helper method to get the activity data for a group
     * 
     * @param groupName      user name
     * @param role          role
     * @return Activity data in JSON format
     */
    private String getActivityGroupData(String groupName, String role)
    {
        try
        {
            JSONObject activityData = new JSONObject();
            activityData.put("role", role);
            activityData.put("groupName", groupName);

            return activityData.toString();
        } 
        catch (JSONException je)
        {
            // log error, subsume exception
            logger.error("Failed to get activity data: " + je);
            return "";
        }
    }
    
    /**
     * Helper to check that we are not removing the last Site Manager from a site
     * 
     * @param shortName
     * @param authorityName
     * @param role
     */
    private void checkLastManagerRemoval(final String shortName, final String authorityName, final String role)
    {
        // Check that we are not about to remove the last site manager
        if (SiteModel.SITE_MANAGER.equals(role) == true)
        {
            String mgrGroup = getSiteRoleGroup(shortName, SITE_MANAGER, true);
            Set<String> siteUserMangers = this.authorityService.getContainedAuthorities(
                    AuthorityType.USER, mgrGroup, true);
            if (siteUserMangers.size() <= 1)
            {
                Set<String> siteGroupManagers = this.authorityService.getContainedAuthorities(
                        AuthorityType.GROUP, mgrGroup, true);
                
                if (siteUserMangers.size() + siteGroupManagers.size() == 1)
                {
                    throw new SiteServiceException(MSG_DO_NOT_CHANGE_MGR, new Object[] {authorityName});
                }
            }
        }
    }
}
