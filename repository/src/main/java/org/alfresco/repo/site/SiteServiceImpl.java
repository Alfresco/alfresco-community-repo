/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2018 Alfresco Software Limited
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
package org.alfresco.repo.site;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.search.QueryParserException;
import org.alfresco.sync.events.types.Event;
import org.alfresco.sync.events.types.SiteManagementEvent;
import org.alfresco.model.ContentModel;
import org.alfresco.query.CannedQuery;
import org.alfresco.query.CannedQueryFactory;
import org.alfresco.query.CannedQueryPageDetails;
import org.alfresco.query.CannedQueryParameters;
import org.alfresco.query.CannedQueryResults;
import org.alfresco.query.CannedQuerySortDetails;
import org.alfresco.query.CannedQuerySortDetails.SortOrder;
import org.alfresco.query.PageDetails;
import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.repo.activities.ActivityType;
import org.alfresco.repo.admin.SysAdminParams;
import org.alfresco.repo.cache.SimpleCache;
import org.alfresco.repo.domain.node.NodeDAO;
import org.alfresco.sync.repo.events.EventPreparator;
import org.alfresco.sync.repo.events.EventPublisher;
import org.alfresco.repo.node.NodeArchiveServicePolicies;
import org.alfresco.repo.node.NodeArchiveServicePolicies.BeforePurgeNodePolicy;
import org.alfresco.repo.node.NodeArchiveServicePolicies.BeforeRestoreArchivedNodePolicy;
import org.alfresco.repo.node.NodeArchiveServicePolicies.OnRestoreArchivedNodePolicy;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.node.NodeServicePolicies.OnRestoreNodePolicy;
import org.alfresco.repo.node.getchildren.FilterProp;
import org.alfresco.repo.node.getchildren.FilterPropString;
import org.alfresco.repo.node.getchildren.FilterPropString.FilterTypeString;
import org.alfresco.repo.node.getchildren.GetChildrenCannedQuery;
import org.alfresco.repo.node.getchildren.GetChildrenCannedQueryFactory;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.security.authentication.AuthenticationContext;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.repo.tenant.TenantUtil;
import org.alfresco.repo.tenant.TenantUtil.TenantRunAsWork;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.activities.ActivityService;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.preference.PreferenceService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.search.LimitBy;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AccessPermission;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.NoSuchPersonException;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.security.PublicServiceAccessService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteMemberInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.site.SiteVisibility;
import org.alfresco.service.cmr.tagging.TaggingService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.GUID;
import org.alfresco.util.Pair;
import org.alfresco.util.PropertyCheck;
import org.alfresco.util.PropertyMap;
import org.alfresco.util.SearchLanguageConversion;
import org.alfresco.util.registry.NamedObjectRegistry;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.context.ApplicationEvent;
import org.springframework.extensions.surf.util.AbstractLifecycleBean;
import org.springframework.extensions.surf.util.ParameterCheck;

/**
 * Site Service Implementation. Also bootstraps the site DM stores.
 * 
 * @author Roy Wetherall
 */
public class SiteServiceImpl extends AbstractLifecycleBean implements SiteServiceInternal, SiteModel, NodeArchiveServicePolicies.BeforePurgeNodePolicy, NodeServicePolicies.OnRestoreNodePolicy, NodeArchiveServicePolicies.BeforeRestoreArchivedNodePolicy, NodeArchiveServicePolicies.OnRestoreArchivedNodePolicy
{
    /** Logger */
    protected static Log logger = LogFactory.getLog(SiteServiceImpl.class);

    /** The DM store where site's are kept */
    public static final StoreRef SITE_STORE = new StoreRef("workspace://SpacesStore");

    /** Activity tool */
    private static final String ACTIVITY_TOOL = "siteService";
    
    private static final String SITE_PREFIX = "site_";
    private static final String GROUP_SITE_PREFIX = PermissionService.GROUP_PREFIX + SITE_PREFIX;
    private static final int GROUP_PREFIX_LENGTH = PermissionService.GROUP_PREFIX.length();
    private static final int GROUP_SITE_PREFIX_LENGTH = GROUP_SITE_PREFIX.length();
    
    /**
     * The authority that needs to contain the users who are allowed to administer the site.
     */
    private static final String SITE_ADMINISTRATORS_AUTHORITY = "SITE_ADMINISTRATORS";
    private static final String GROUP_SITE_ADMINISTRATORS_AUTHORITY = PermissionService.GROUP_PREFIX + SITE_ADMINISTRATORS_AUTHORITY;
    
    // note: caches are tenant-aware (if using EhCacheAdapter shared cache)
    
    private SimpleCache<String, Object> singletonCache; // eg. for siteHomeNodeRef
    private static final String KEY_SITEHOME_NODEREF = "key.sitehome.noderef";
    
    private SimpleCache<String, NodeRef> siteNodeRefCache; // for site shortname to nodeRef lookup
    
    private String sitesXPath;
    
    /** Messages */
    private static final String MSG_UNABLE_TO_CREATE = "site_service.unable_to_create";
    private static final String MSG_SITE_SHORT_NAME_TOO_LONG = "site_service.short_name_too_long";
    private static final String MSG_VISIBILITY_GROUP_MISSING = "site_service.visibility_group_missing";
    private static final String MSG_CAN_NOT_UPDATE = "site_service.can_not_update";
    private static final String MSG_CAN_NOT_DELETE = "site_service.can_not_delete";
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
    private RetryingTransactionHelper retryingTransactionHelper;
    private Comparator<String> roleComparator;
    private SysAdminParams sysAdminParams;
    private BehaviourFilter behaviourFilter;
    private SitesPermissionCleaner sitesPermissionsCleaner;
    private PolicyComponent policyComponent;
    private PublicServiceAccessService publicServiceAccessService;
    private NodeDAO nodeDAO;
    private EventPublisher eventPublisher;
    
    private NamedObjectRegistry<CannedQueryFactory<? extends Object>> cannedQueryRegistry;
    
    private static int GET_CHILD_ASSOCS_PAGE_SIZE = 512;

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
    
    @Deprecated
    public void setPreferenceService(PreferenceService preferenceService)
    {
        // Never used
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
    
    public void setSingletonCache(SimpleCache<String, Object> singletonCache)
    {
        this.singletonCache = singletonCache;
    }
    
    public void setSiteNodeRefCache(SimpleCache<String, NodeRef> siteNodeRefCache)
    {
        this.siteNodeRefCache = siteNodeRefCache;
    }
    
    /**
     * Sets helper that provides transaction callbacks
     */
    public void setTransactionHelper(RetryingTransactionHelper retryingTransactionHelper)
    {
        this.retryingTransactionHelper = retryingTransactionHelper;
    }
    
    public void setPolicyComponent(PolicyComponent policyComponent) 
    {
        this.policyComponent = policyComponent;
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
    
    public void setPublicServiceAccessService(PublicServiceAccessService publicServiceAccessService)
    {
        this.publicServiceAccessService = publicServiceAccessService;
    }
    
    public void setNodeDAO(NodeDAO nodeDAO)
    {
        this.nodeDAO = nodeDAO;
    }

    /**
     * Set the registry of {@link CannedQueryFactory canned queries}
     */
    public void setCannedQueryRegistry(NamedObjectRegistry<CannedQueryFactory<? extends Object>> cannedQueryRegistry)
    {
        this.cannedQueryRegistry = cannedQueryRegistry;
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
    
    /* (non-Javadoc)
     * @see org.springframework.extensions.surf.util.AbstractLifecycleBean#onBootstrap(org.springframework.context.ApplicationEvent)
     */
    @Override
    protected void onBootstrap(ApplicationEvent event)
    {
        this.policyComponent.bindClassBehaviour(
                BeforePurgeNodePolicy.QNAME,
                SiteModel.TYPE_SITE,
                new JavaBehaviour(this, "beforePurgeNode"));
        this.policyComponent.bindClassBehaviour(
                OnRestoreNodePolicy.QNAME,
                SiteModel.TYPE_SITE,
                new JavaBehaviour(this, "onRestoreNode"));
        this.policyComponent.bindClassBehaviour(
                BeforeRestoreArchivedNodePolicy.QNAME,
                SiteModel.TYPE_SITE,
                new JavaBehaviour(this, "beforeRestoreArchivedNode"));
        this.policyComponent.bindClassBehaviour(
                OnRestoreArchivedNodePolicy.QNAME,
                SiteModel.TYPE_SITE,
                new JavaBehaviour(this, "onRestoreArchivedNode"));
    }

    /* (non-Javadoc)
     * @see org.springframework.extensions.surf.util.AbstractLifecycleBean#onShutdown(org.springframework.context.ApplicationEvent)
     */
    @Override
    protected void onShutdown(ApplicationEvent event)
    {
    }  
    
    /*
     * (non-Javadoc)
     * @see org.alfresco.service.cmr.site.SiteService#hasCreateSitePermissions()
     */
    public boolean hasCreateSitePermissions()
    {
        // NOTE: see ALF-13580 - since 3.4.6 PermissionService.CONTRIBUTOR is no longer used as the default on the Sites folder
        // instead the ability to call createSite() and the Spring configured ACL is the mechanism used to protect access.
        return (publicServiceAccessService.hasAccess("SiteService", "createSite", "", "", "", "", true) == AccessStatus.ALLOWED);
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
        
        // Check to see if we already have a site of this name
        NodeRef existingSite = getSiteNodeRef(shortName, false);
        if (existingSite != null || authorityService.authorityExists(getSiteGroup(shortName, true)))
        {
            // Throw an exception since we have a duplicate site name
            throw new SiteServiceException(MSG_UNABLE_TO_CREATE, new Object[]{shortName});
        }
        
        // Check that the site name isn't too long
        // Authorities are limited to 100 characters by the PermissionService
        int longestPermissionLength = 0;
        for (String permission : permissionService.getSettablePermissions(siteType))
        {
            if (permission.length() > longestPermissionLength)
                longestPermissionLength = permission.length();
        }
        int maximumPermisionGroupLength = 99 - longestPermissionLength;

        if (getSiteGroup(shortName, true).length() > maximumPermisionGroupLength)
        {
            throw new SiteServiceException(MSG_SITE_SHORT_NAME_TOO_LONG, new Object[] {
                 shortName, maximumPermisionGroupLength - getSiteGroup("", true).length()
            });
        }

        // Get the site parent node reference
        final NodeRef siteParent = getSiteParent(shortName);
        if (siteParent == null)
        {
            throw new SiteServiceException("No root sites folder exists");
        }

        // Create the site node
        final PropertyMap properties = new PropertyMap(4);
        properties.put(ContentModel.PROP_NAME, shortName);
        properties.put(SiteModel.PROP_SITE_PRESET, sitePreset);
        properties.put(SiteModel.PROP_SITE_VISIBILITY, visibility.toString());
        properties.put(ContentModel.PROP_TITLE, title);
        properties.put(ContentModel.PROP_DESCRIPTION, description);
        
        final NodeRef siteNodeRef = AuthenticationUtil.runAs(new RunAsWork<NodeRef>() {
           @Override
           public NodeRef doWork() throws Exception {
               
               behaviourFilter.disableBehaviour(siteParent, ContentModel.ASPECT_AUDITABLE);
               try
               {
                   return nodeService.createNode(
                           siteParent,
                           ContentModel.ASSOC_CONTAINS,
                           QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, shortName), 
                           siteType, 
                           properties
                     ).getChildRef();
               }
               finally
               {
                   behaviourFilter.enableBehaviour(siteParent, ContentModel.ASPECT_AUDITABLE);
               }
           }
        }, AuthenticationUtil.getSystemUserName());
           
        // Make the new site a tag scope
        this.taggingService.addTagScope(siteNodeRef);

        // Clear the sites inherited permissions
        this.permissionService.setInheritParentPermissions(siteNodeRef, false);

        // Create the relevant groups and assign permissions
        setupSitePermissions(siteNodeRef, shortName, visibility, null);

        eventPublisher.publishEvent(new EventPreparator(){
            @Override
            public Event prepareEvent(String user, String networkId, String transactionId)
            {            
                return new SiteManagementEvent("site.create", transactionId, networkId, new Date().getTime(),
                            user, shortName,title,description, visibility.toString(),sitePreset);
            }
        });
        // Return created site information
        Map<QName, Serializable> customProperties = getSiteCustomProperties(siteNodeRef);
        SiteInfo siteInfo = new SiteInfoImpl(sitePreset, shortName, title, description, visibility, customProperties, siteNodeRef);
        return siteInfo;
    }
    
    /**
     * Setup the Site permissions.
     * <p>
     * Creates the top-level site group, plus all the Role groups required for users of the site.
     * <p>
     * Note - Changes here likely need to be replicated to the {@link #updateSite(SiteInfo)}
     *  method too, as that also has to deal with Site Permissions.
     * 
     * @param siteNodeRef NodeRef
     * @param shortName String
     * @param visibility SiteVisibility
     * @param memberships Map<String, Set<String>>
     */
    private void setupSitePermissions(
            final NodeRef siteNodeRef, final String shortName, final SiteVisibility visibility, final Map<String, Set<String>> memberships)
    {
        setupSitePermissions(siteNodeRef, shortName, visibility, memberships, false);
    }

    private void setupSitePermissions(
            final NodeRef siteNodeRef, final String shortName, final SiteVisibility visibility, final Map<String, Set<String>> memberships, final boolean ignoreExistingGroups)
    {
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
                
                // From Alfresco 3.4 the 'site public' group is configurable. Out of the box it is
                // GROUP_EVERYONE so unconfigured behaviour is unchanged. But from 3.4, admins
                // can change the value of property site.public.group via JMX/properties files
                // to be another group of their choosing.
                // This then is the group that is given SiteConsumer access to newly created 
                //  public and moderated sites.
                final String sitePublicGroup = sysAdminParams.getSitePublicGroup();
                boolean publicGroupExists = authorityService.authorityExists(sitePublicGroup);
                if (!PermissionService.ALL_AUTHORITIES.equals(sitePublicGroup) && !publicGroupExists
                    && !SiteVisibility.PRIVATE.equals(visibility))
                {
                    // If the group specified in the settings does not exist, we cannot create the site.
                    throw new SiteServiceException(MSG_VISIBILITY_GROUP_MISSING, new Object[]{sitePublicGroup});
                }
                
                // Create the site's groups
                String siteGroupShortName = getSiteGroup(shortName, false);
                /* MNT-11289 fix - group is probably already exists. we should check it for existence */
                String siteGroup = authorityService.getName(AuthorityType.GROUP, siteGroupShortName);
                if (!ignoreExistingGroups || !authorityService.authorityExists(siteGroup))
                {
                    siteGroup = authorityService.createAuthority(AuthorityType.GROUP, siteGroupShortName,
                        siteGroupShortName, shareZones);
                }
                QName siteType = directNodeService.getType(siteNodeRef);
                Set<String> permissions = permissionService.getSettablePermissions(siteType);
                for (String permission : permissions)
                {
                    // Create a group for the permission
                    String permissionGroupShortName = getSiteRoleGroup(shortName, permission, false);
                    /* MNT-11289 fix - group is probably already exists. we should check it for existence */
                    String authorityName = authorityService.getName(AuthorityType.GROUP, permissionGroupShortName);
                    if (ignoreExistingGroups && authorityService.authorityExists(authorityName))
                    {
                        continue;
                    }
                    
                    String permissionGroup = authorityService.createAuthority(AuthorityType.GROUP,
                            permissionGroupShortName, permissionGroupShortName, shareZones);
                    authorityService.addAuthority(siteGroup, permissionGroup);
                    
                    // add any supplied memberships to it
                    String siteRoleGroup = getSiteRoleGroup(shortName, permission, true);
                    if (memberships != null && memberships.containsKey(siteRoleGroup))
                    {
                        for (String authority : memberships.get(siteRoleGroup))
                        {
                            authorityService.addAuthority(siteRoleGroup, authority);
                        }
                    }
                    
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
                    // The public site group becomes the consumer
                    permissionService.setPermission(siteNodeRef, sitePublicGroup, SITE_CONSUMER, true);
                }
                else if (SiteVisibility.MODERATED.equals(visibility) == true &&
                         permissions.contains(SITE_CONSUMER))
                {
                    // For moderated sites, the Public Group has consumer access to the 
                    //  site root, but not to site components.
                    permissionService.setPermission(siteNodeRef, sitePublicGroup, SITE_CONSUMER, true);
                    
                    // Permissions will be set on the site components as they get created
                }
                
                // No matter what, everyone must be able to read permissions on 
                //  the site, so they can check to see if they're a member or not
                permissionService.setPermission(siteNodeRef,
                        PermissionService.ALL_AUTHORITIES,
                        PermissionService.READ_PERMISSIONS, true);
                
                // add the default site manager authority
                Set<String> currentManagers = 
                    authorityService.getContainedAuthorities(AuthorityType.USER, getSiteRoleGroup(shortName, SiteModel.SITE_MANAGER, true), false);
                if (currentManagers.isEmpty())
                {
                    authorityService.addAuthority(getSiteRoleGroup(shortName,
                            SiteModel.SITE_MANAGER, true), currentUser);
                }

                // Return nothing
                return null;
            }

        }, AuthenticationUtil.getSystemUserName());
    }

    /**
     * Gets a map containing the site's custom properties
     * 
     * @return  map containing the custom properties of the site
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
     * @return  map containing the custom properties of the site
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
        NodeRef siteHomeRef = (NodeRef)singletonCache.get(KEY_SITEHOME_NODEREF);
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
                singletonCache.put(KEY_SITEHOME_NODEREF, siteHomeRef);
            }
        }
        return siteHomeRef;
    }
    
    /*
     * (non-Javadoc)
     * @see org.alfresco.service.cmr.site.SiteService#findSites(java.lang.String, int)
     */
    @Override
    public List<SiteInfo> findSites(String filter, int size)
    {
        List<SiteInfo> result;
        
        NodeRef siteRoot = getSiteRoot();
        if (siteRoot == null)
        {
            result = Collections.emptyList();
        }
        else
        {
            // get the sites that match the specified names
            StringBuilder query = new StringBuilder(128);
            query.append("+TYPE:\"").append(SiteModel.TYPE_SITE).append('"');
            
            final boolean filterIsPresent = filter != null && filter.length() > 0;

            if (filterIsPresent)
            {
                query.append(" AND (");
                String escNameFilter = SearchLanguageConversion.escapeLuceneQuery(filter.replace('"', ' '));
                String[] tokenizedFilter = SearchLanguageConversion.tokenizeString(escNameFilter);
                
                //cm:name
                query.append(" cm:name:\" ");
                for( int i = 0; i < tokenizedFilter.length; i++)
                {
                  if (i!=0) //Not first element 
                  {
                      query.append("?");
                  }
                  query.append(tokenizedFilter[i].toLowerCase());
                }
                query.append("*\"");
                
                //cm:title
                query.append(" OR ")
                     .append(" cm:title: (");
                for( int i = 0; i < tokenizedFilter.length; i++)
                {
                  if (i!=0) //Not first element 
                  {
                      query.append(" AND ");
                  }
                  query.append("\""+tokenizedFilter[i]+"*\" ");
                }
                query.append(")");

                query.append(" OR cm:description:\"" + escNameFilter + "\"");            
                query.append(")");
            }
            
            SearchParameters sp = new SearchParameters();
            sp.addStore(siteRoot.getStoreRef());
            sp.setLanguage(SearchService.LANGUAGE_FTS_ALFRESCO);
            sp.setQuery(query.toString());
            if (size > 0)
            {
                sp.setLimit(size);
                sp.setLimitBy(LimitBy.FINAL_SIZE);
            }
            
            if(logger.isDebugEnabled())
            {
               logger.debug("Search parameters are: " + sp);
            }
            
            ResultSet results = null;
            try
            {
                results = this.searchService.query(sp);
                result = new ArrayList<SiteInfo>(results.length());
                for (NodeRef site : results.getNodeRefs())
                {
                  result.add(createSiteInfo(site));
                }
            }
            catch (QueryParserException lqpe)
            {
               //Log the error but suppress is from the user
               logger.error("LuceneQueryParserException with findSites()", lqpe);
               result = Collections.emptyList();
            }
            finally
            {
                if (results != null) results.close();
            }
        }
        
        return result;
    }
    /*
     * (non-Javadoc)
     * @see org.alfresco.service.cmr.site.SiteService#findSites(java.lang.String, java.lang.String, int)
     */
    @Override
    public List<SiteInfo> findSites(String filter, String sitePresetFilter, int size)
    {
        List<SiteInfo> result;
        
        NodeRef siteRoot = getSiteRoot();
        if (siteRoot == null)
        {
            result = Collections.emptyList();
        }
        else
        {
            // get the sites that match the specified names
            StringBuilder query = new StringBuilder(128);
            query.append("+PARENT:\"").append(siteRoot.toString()).append('"');

            final boolean filterIsPresent = filter != null && filter.length() > 0;
            // The filter string is only used in the Lucene query if it restricts results.
            // A search for name/title/description = "*" does not need to be put into the Lucene query.
            // This allows users to search for "*" in the site-finder.
            final boolean filterIsPresentAndNecessary = filterIsPresent && !filter.equals("*");
            final boolean sitePresetFilterIsPresent = sitePresetFilter != null && sitePresetFilter.length() > 0;
            
            if (filterIsPresentAndNecessary || sitePresetFilterIsPresent)
            {
                query.append(" +(");
                if (filterIsPresentAndNecessary)
                {
                    String escNameFilter = SearchLanguageConversion.escapeLuceneQuery(filter.replace('"', ' '));
                    
                    query.append(" @cm\\:name:\"*" + escNameFilter + "*\"")
                         .append(" @cm\\:title:\"" + escNameFilter + "\"")
                         .append(" @cm\\:description:\"" + escNameFilter + "\"");
                }
                if (sitePresetFilterIsPresent)
                {
                    String escPresetFilter = SearchLanguageConversion.escapeLuceneQuery(sitePresetFilter.replace('"', ' '));
                    query.append(" @st\\:sitePreset:\"" + escPresetFilter + "\"");
                }
                
                query.append(")");
            }
            
            SearchParameters sp = new SearchParameters();
            sp.addStore(siteRoot.getStoreRef());
            sp.setLanguage(SearchService.LANGUAGE_LUCENE);
            sp.setQuery(query.toString());
            if (size > 0)
            {
                sp.setLimit(size);
                sp.setLimitBy(LimitBy.FINAL_SIZE);
            }
            ResultSet results = this.searchService.query(sp);
            try
            {
                result = new ArrayList<SiteInfo>(results.length());
                for (NodeRef site : results.getNodeRefs())
                {
                    // Ignore any node type that is not a "site"
                    QName siteClassName = this.nodeService.getType(site);
                    if (this.dictionaryService.isSubClass(siteClassName, SiteModel.TYPE_SITE))
                    {
                        result.add(createSiteInfo(site));
                    }
                }
            }
            finally
            {
                results.close();
            }
        }
        
        return result;
    }
    
    /**
     * @see org.alfresco.service.cmr.site.SiteService#listSites(java.lang.String, java.lang.String)
     */
    public List<SiteInfo> listSites(String nameFilter, String sitePresetFilter)
    {
        return listSites(nameFilter, sitePresetFilter, -1);
    }

    /**
     * @see org.alfresco.service.cmr.site.SiteService#listSites(java.lang.String, java.lang.String, int)
     */
    public List<SiteInfo> listSites(final String filter, final String sitePresetFilter, int size)
    {
        List<SiteInfo> result = Collections.emptyList();
        
        NodeRef siteRoot = getSiteRoot();
        if (siteRoot != null)
        {
            final boolean filterHasValue = filter != null && filter.length() != 0;
            final boolean sitePresetFilterHasValue = sitePresetFilter != null && sitePresetFilter.length() > 0;
            
            List<Pair<QName, Boolean>> sortProps = null;
            
            PagingRequest pagingRequest = new PagingRequest(size <= 0 ? Integer.MAX_VALUE : size);
            List<FilterProp> filterProps = new ArrayList<FilterProp>();
            
            if (filterHasValue)
            {
                filterProps.add(new FilterPropString(ContentModel.PROP_NAME, filter, FilterTypeString.STARTSWITH_IGNORECASE));
                filterProps.add(new FilterPropString(ContentModel.PROP_TITLE, filter, FilterTypeString.STARTSWITH_IGNORECASE));
                filterProps.add(new FilterPropString(ContentModel.PROP_DESCRIPTION, filter, FilterTypeString.STARTSWITH_IGNORECASE));
            }
            if (sitePresetFilterHasValue)
            {
                filterProps.add(new FilterPropString(SiteModel.PROP_SITE_PRESET, sitePresetFilter, FilterTypeString.EQUALS));
            }
            
            PagingResults<SiteInfo> allSites = listSites(filterProps, sortProps, pagingRequest);
            result = allSites.getPage();
        }
        
        return result;
    }
    
    /*
     * (non-Javadoc)
     * @see org.alfresco.service.cmr.site.SiteService#listSites(java.lang.String)
     */
    public List<SiteInfo> listSites(final String userName)
    {
        return listSites(userName, 0);
    }
    
    /**
     * @see org.alfresco.service.cmr.site.SiteService#listSites(java.lang.String, int)
     */
    public List<SiteInfo> listSites(final String userName, final int size)
    {
        // MT share - for activity service remote system callback (deprecated)
        if (tenantService.isEnabled() &&
            TenantUtil.isCurrentDomainDefault() &&
            (AuthenticationUtil.SYSTEM_USER_NAME.equals(AuthenticationUtil.getRunAsUser())) && 
            tenantService.isTenantUser(userName))
        {
            final String tenantDomain = tenantService.getUserDomain(userName);
            
            return TenantUtil.runAsSystemTenant(new TenantRunAsWork<List<SiteInfo>>()
            {
                public List<SiteInfo> doWork() throws Exception
                {
                    return listSitesImpl(userName, size);
                }
            }, tenantDomain);
        }
        else
        {
            return listSitesImpl(userName, size);
        }
    }
    
    /**
     * This method uses {@link CannedQuery canned queries} to retrieve {@link SiteModel#TYPE_SITE st:site} NodeRefs
     * with support for {@link PagingRequest result paging}.
     */
    @Override
    public PagingResults<SiteInfo> listSites(List<FilterProp> filterProps, List<Pair<QName, Boolean>> sortProps, PagingRequest pagingRequest)
    {
        // Only search for "st:site" nodes.
        final Set<QName> searchTypeQNames = new HashSet<QName>(1);
        searchTypeQNames.add(SiteModel.TYPE_SITE);
        // ... and all subtypes of st:site
        searchTypeQNames.addAll(dictionaryService.getSubTypes(SiteModel.TYPE_SITE, true));

        // get canned query
        final String cQBeanName = "siteGetChildrenCannedQueryFactory";
        GetChildrenCannedQueryFactory getChildrenCannedQueryFactory = (GetChildrenCannedQueryFactory)cannedQueryRegistry.getNamedObject(cQBeanName);
        
        GetChildrenCannedQuery cq = (GetChildrenCannedQuery)getChildrenCannedQueryFactory.getCannedQuery(getSiteRoot(), null, null, searchTypeQNames,
                                                                                                         filterProps, sortProps, pagingRequest);
        
        // execute canned query
        final CannedQueryResults<NodeRef> results = cq.execute();
        
        // Now convert the CannedQueryResults<NodeRef> into a more useful PagingResults<SiteInfo>
        List<NodeRef> nodeRefs = Collections.emptyList();
        if (results.getPageCount() > 0)
        {
            nodeRefs = results.getPages().get(0);
        }
        
        // set total count
        final Pair<Integer, Integer> totalCount;
        if (pagingRequest.getRequestTotalCountMax() > 0)
        {
            totalCount = results.getTotalResultCount();
        }
        else
        {
            totalCount = null;
        }
        
        final List<SiteInfo> siteInfos = new ArrayList<SiteInfo>(nodeRefs.size());
        for (NodeRef nodeRef : nodeRefs)
        {
            siteInfos.add(createSiteInfo(nodeRef));
        }
        
        return new PagingResults<SiteInfo>()
        {
            @Override
            public String getQueryExecutionId()
            {
                return results.getQueryExecutionId();
            }

            @Override
            public List<SiteInfo> getPage()
            {
                return siteInfos;
            }

            @Override
            public boolean hasMoreItems()
            {
                return results.hasMoreItems();
            }

            @Override
            public Pair<Integer, Integer> getTotalResultCount()
            {
                return totalCount;
            }
        };
    }

    public String resolveSite(String group)
    {
        // purge non Site related Groups and strip the group name down to the site "shortName" it relates too
        if (group.startsWith(GROUP_SITE_PREFIX))
        {
            int roleIndex = group.lastIndexOf('_');
            if (roleIndex + 1 <= GROUP_SITE_PREFIX_LENGTH)
            {
                // There is no role associated
                return group.substring(GROUP_SITE_PREFIX_LENGTH);
            }
            else
            {
                return group.substring(GROUP_SITE_PREFIX_LENGTH, roleIndex);
            }
        }
        return null;
    }

    /**
     * This method returns the {@link SiteInfo siteInfos} for sites to which the specified user has access.
     * Note that if the user has access to more than 1000 sites, the list will be truncated to 1000 entries.
     * 
     * @param userName the username
     * @return a list of {@link SiteInfo site infos}.
     */
    private List<SiteInfo> listSitesImpl(final String userName, int size)
    {
        List<SiteMembership> siteMemberships = listSiteMemberships(userName, size);
        List<SiteInfo> result = new ArrayList<SiteInfo>(siteMemberships.size());
        for (SiteMembership membership : siteMemberships)
        {
            result.add(membership.getSiteInfo());
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
        String title = DefaultTypeConverter.INSTANCE.convert(String.class, properties.get(ContentModel.PROP_TITLE));
        String description = DefaultTypeConverter.INSTANCE.convert(String.class, properties.get(ContentModel.PROP_DESCRIPTION));

        // Get the visibility of the site
        SiteVisibility visibility = getSiteVisibility(siteNodeRef);
        
        // Create and return the site information
        Map<QName, Serializable> customProperties = getSiteCustomProperties(properties);
        
        siteInfo = new SiteInfoImpl(sitePreset, shortName, title, description, visibility, customProperties, siteNodeRef);
        siteInfo.setCreatedDate(DefaultTypeConverter.INSTANCE.convert(Date.class, properties.get(ContentModel.PROP_CREATED)));
        siteInfo.setLastModifiedDate(DefaultTypeConverter.INSTANCE.convert(Date.class, properties.get(ContentModel.PROP_MODIFIED)));
        
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
            Set<AccessPermission> permissions;
            try {
                 permissions = this.permissionService.getAllSetPermissions(siteNodeRef);
            } catch (AccessDeniedException ae){
                // We might not have permission to examine the permissions
                return visibility;
            }
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
        // MT share - for activity service remote system callback (deprecated)
        if (tenantService.isEnabled() &&
            TenantUtil.isCurrentDomainDefault() &&
            (AuthenticationUtil.SYSTEM_USER_NAME.equals(AuthenticationUtil.getRunAsUser())) &&
            tenantService.isTenantName(shortName))
        {
            final String tenantDomain = tenantService.getDomain(shortName);
            final String sName = tenantService.getBaseName(shortName, true);
            
            return TenantUtil.runAsSystemTenant(new TenantRunAsWork<SiteInfo>()
            {
                public SiteInfo doWork() throws Exception
                {
                    SiteInfo site = getSiteImpl(sName);
                    return new SiteInfoImpl(site.getSitePreset(), shortName, site.getTitle(), site.getDescription(), site.getVisibility(), site.getCustomProperties(), site.getNodeRef());
                }
            }, tenantDomain);
        }
        else
        {
            return getSiteImpl(shortName);
        }
    }
    
    /**
     * Get the site implementation given a short name
     * 
     * @param shortName String
     * @return SiteInfo
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
     * @see org.alfresco.service.cmr.site.SiteService#getSiteShortName(org.alfresco.service.cmr.repository.NodeRef)
     */
    public String getSiteShortName(NodeRef nodeRef)
    {
        String shortName = null;
        NodeRef siteNodeRef = getSiteNodeRef(nodeRef);
        if (siteNodeRef != null)
        {
            shortName = (String)this.directNodeService.getProperty(siteNodeRef, ContentModel.PROP_NAME);
        }
        return shortName;
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
        NodeRef siteNodeRef = siteNodeRefCache.get(shortName);
        if (siteNodeRef != null)
        {
            // test for existance - and remove from cache if no longer exists
            if (!this.directNodeService.exists(siteNodeRef))
            {
                siteNodeRefCache.remove(shortName);
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
                        siteNodeRefCache.put(shortName, siteNode);
                    }
                    return siteNode;
                }
            }, AuthenticationUtil.getSystemUserName());
        }
        if (enforcePermissions)
        {
            // Note: Here we use AuthenticationUtil.getRunAsUser() to check for siteAdmin,
            // as some codes like LocalFeedTaskProcessor.canReadSite dependent on runAsUser.
            return siteNodeRef == null
                        || !(this.permissionService.hasPermission(siteNodeRef, PermissionService.READ_PROPERTIES)
                                    .equals(AccessStatus.ALLOWED) || isSiteAdmin(AuthenticationUtil.getRunAsUser())) ? null : siteNodeRef;
        }
        else
        {
            return siteNodeRef;
        }
    }
    
    /**
     * @see org.alfresco.service.cmr.site.SiteService#hasSite(java.lang.String)
     */
    @Override
    public boolean hasSite(String shortName)
    {
        return (getSiteNodeRef(shortName, false) != null);
    }
    
    /**
     * @see org.alfresco.service.cmr.site.SiteService#updateSite(org.alfresco.service.cmr.site.SiteInfo)
     */
    public void updateSite(SiteInfo siteInfo)
    {
        final String shortName 		    = siteInfo.getShortName();
        final String title 	 		    = siteInfo.getTitle();
        final String description        = siteInfo.getDescription();
        
        NodeRef siteNodeRef = getSiteNodeRef(shortName);
        if (siteNodeRef == null)
        {
            throw new SiteServiceException(MSG_CAN_NOT_UPDATE, new Object[]{siteInfo.getShortName()});            
        }
        
        // Get the sites properties
        Map<QName, Serializable> properties = this.directNodeService.getProperties(siteNodeRef);
        
        // Update the properties of the site
        // Note: the site preset and short name should never be updated!
        properties.put(ContentModel.PROP_TITLE, title);
        properties.put(ContentModel.PROP_DESCRIPTION, description);

        // Update the permissions based on the visibility
        SiteVisibility currentVisibility = getSiteVisibility(siteNodeRef);
        SiteVisibility updatedVisibility = siteInfo.getVisibility();
        if (currentVisibility.equals(updatedVisibility) == false)
        {
            // visibility has changed   
            logger.debug("site:" + shortName + " visibility has changed from: " + currentVisibility + "to: " + updatedVisibility);
            
            // Grab the Public Site Group and validate
            final String sitePublicGroup = sysAdminParams.getSitePublicGroup();
            boolean publicGroupExists = authorityService.authorityExists(sitePublicGroup);
            if (!PermissionService.ALL_AUTHORITIES.equals(sitePublicGroup) && !publicGroupExists)
            {
                // If the group specified in the settings does not exist, we cannot update the site.
                throw new SiteServiceException(MSG_VISIBILITY_GROUP_MISSING, new Object[]{sitePublicGroup});
            }
            
            // The site Visibility has changed.
            // Remove current visibility permissions
            if (SiteVisibility.PUBLIC.equals(currentVisibility) == true ||
                SiteVisibility.MODERATED.equals(currentVisibility) == true)
            {
                // Remove the old Consumer permissions
                // (Always remove both EVERYONE and the Publci Site Group, just to be safe)
                this.permissionService.deletePermission(siteNodeRef, sitePublicGroup, SITE_CONSUMER);
                if (sitePublicGroup.equals(PermissionService.ALL_AUTHORITIES))
                {
                   this.permissionService.deletePermission(siteNodeRef, PermissionService.ALL_AUTHORITIES, SITE_CONSUMER);
                }
            }

            // If the site was moderated/private before, undo the work of #setModeratedPermissions/#setPrivatePermissions
            //   by restoring inherited permissions on the containers
            // (Leaving the old extra permissions on containers is fine)
            if (SiteVisibility.MODERATED.equals(currentVisibility) == true || SiteVisibility.PRIVATE.equals(currentVisibility) == true)
            {
                List<FileInfo> folders = fileFolderService.listFolders(siteNodeRef);
                for(FileInfo folder : folders)
                {
                    NodeRef containerNodeRef = folder.getNodeRef();
                    this.permissionService.setInheritParentPermissions(containerNodeRef, true);   
                }
            }
            
            // Add new visibility permissions
            // Note - these need to be kept in sync manually with those in #setupSitePermissions
            if (SiteVisibility.PUBLIC.equals(updatedVisibility) == true)
            {
                this.permissionService.setPermission(siteNodeRef, sitePublicGroup, SITE_CONSUMER, true);
            }
            else if (SiteVisibility.MODERATED.equals(updatedVisibility) == true)
            {
                this.permissionService.setPermission(siteNodeRef, sitePublicGroup, SITE_CONSUMER, true);
                
                // Set the moderated permissions on all the containers the site already has
                List<FileInfo> folders = fileFolderService.listFolders(siteNodeRef);
                for(FileInfo folder : folders)
                {
                    NodeRef containerNodeRef = folder.getNodeRef();
                    setModeratedPermissions(shortName, containerNodeRef);
                }
            }
            else if (SiteVisibility.PRIVATE.equals(updatedVisibility))
            {
                // Set the private permissions on all the containers the site already has
                List<FileInfo> folders = fileFolderService.listFolders(siteNodeRef);
                for(FileInfo folder : folders)
                {
                    NodeRef containerNodeRef = folder.getNodeRef();
                    setPrivatePermissions(shortName, containerNodeRef);
                }
            }
            
            // Update the site node reference with the updated visibility value
            properties.put(SiteModel.PROP_SITE_VISIBILITY, siteInfo.getVisibility().toString());
        }
        
        // Set the updated properties back onto the site node reference
        this.nodeService.setProperties(siteNodeRef, properties);
               
        final SiteVisibility visibility = siteInfo.getVisibility();
        final String sitePreset			= siteInfo.getSitePreset();
        
        eventPublisher.publishEvent(new EventPreparator(){
            @Override
            public Event prepareEvent(String user, String networkId, String transactionId)
            {         
            	return new SiteManagementEvent("site.update", transactionId, networkId, new Date().getTime(),
                            user, shortName,title,description, visibility.toString(),sitePreset);
            }
        });
    }
    
    /**
     * @see org.alfresco.service.cmr.site.SiteService#deleteSite(java.lang.String)
     */
    public void deleteSite(final String shortName)
    {     
        if (isSiteAdmin(AuthenticationUtil.getFullyAuthenticatedUser()))
        {
            AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Void>()
            {
                public Void doWork() throws Exception
                {
                    // Delete the site
                    deleteSiteImpl(shortName);
                    return null;
                }
            }, AuthenticationUtil.getAdminUserName());
        }
        else
        {
            // Delete the site
            deleteSiteImpl(shortName);
        }
    }
    
    protected void deleteSiteImpl(final String shortName)
    {
        // In deleting the site node, we have to jump through a few hoops to manage the site groups.
        // The order of execution is important here.
        logger.debug("delete site :" + shortName);
        final NodeRef siteNodeRef = getSiteNodeRef(shortName);
        if (siteNodeRef == null)
        {
            throw new SiteServiceException(MSG_CAN_NOT_DELETE, new Object[]{shortName});
        }

        // Delete the cached reference
        siteNodeRefCache.remove(shortName);
        
        // no need to retain the membership of the site as we postpone delete of authorities until purge from the trashcan 
        
        // The default behaviour is that sites cannot be deleted. But we disable that behaviour here
        // in order to allow site deletion only via this service. Share calls this service for deletion.
        //
        // See ALF-7888 for some background on this issue
        this.behaviourFilter.disableBehaviour(siteNodeRef, ContentModel.ASPECT_UNDELETABLE);
        this.behaviourFilter.disableBehaviour(ContentModel.ASPECT_LOCKABLE);
        
        NodeRef siteParent = getSiteParent(shortName);
        this.behaviourFilter.disableBehaviour(siteParent, ContentModel.ASPECT_AUDITABLE);
        
        try
        {
            this.nodeService.deleteNode(siteNodeRef);
        }
        finally
        {
            this.behaviourFilter.enableBehaviour(siteNodeRef, ContentModel.ASPECT_UNDELETABLE);
            this.behaviourFilter.enableBehaviour(ContentModel.ASPECT_LOCKABLE);
            this.behaviourFilter.enableBehaviour(siteParent, ContentModel.ASPECT_AUDITABLE);
        }
        
        // Postpone delete of associated groups to the time when NodeArchiveService purges site node 
        // because in case of recover ACLs and ACEs are needed which were set for documents
        
        logger.debug("site deleted :" + shortName);
    }
    
    @Override
    public void beforePurgeNode(NodeRef nodeRef)
    {
        final QName siteType = this.directNodeService.getType(nodeRef);
        final String shortName = getSite(nodeRef).getShortName();
        
        // Delete the associated groups
        AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Object>()
        {
            public Void doWork() throws Exception
            {
                // Delete the master site group
                final String siteGroup = getSiteGroup(shortName, true);
                if (authorityService.authorityExists(siteGroup))
                {
                    authorityService.deleteAuthority(siteGroup, false);

                    // Iterate over the role related groups and delete then
                    Set<String> permissions = permissionService.getSettablePermissions(siteType);
                    for (String permission : permissions)
                    {
                        String siteRoleGroup = getSiteRoleGroup(shortName, permission, true);

                        // Delete the site role group
                        authorityService.deleteAuthority(siteRoleGroup);
                    }
                }

                return null;
            }
        }, AuthenticationUtil.getSystemUserName());
    
    }

    public void listMembers(String shortName, final String nameFilter, final String roleFilter, final boolean collapseGroups, final SiteMembersCallback callback)
    {
        // MT share - for activity service system callback
        if (tenantService.isEnabled() && (AuthenticationUtil.SYSTEM_USER_NAME.equals(AuthenticationUtil.getRunAsUser())) && tenantService.isTenantName(shortName))
        {
            final String tenantDomain = tenantService.getDomain(shortName);
            final String sName = tenantService.getBaseName(shortName, true);
            
            TenantUtil.runAsSystemTenant(new TenantRunAsWork<Void>()
            {
                public Void doWork() throws Exception
                {
                    listMembersImpl(sName, nameFilter, roleFilter, collapseGroups, callback, true, true);
                    return null;
                }
            }, tenantDomain);
        }
        else
        {
            listMembersImpl(shortName, nameFilter, roleFilter, collapseGroups, callback, true, true);
        }
    }

    // note that this may return an authority more than once
    protected void listMembersImpl(String shortName, String nameFilter, String roleFilter, boolean expandGroups, SiteMembersCallback callback, boolean includeUsers, boolean includeGroups) {
        NodeRef siteNodeRef = getSiteNodeRef(shortName);
        if (siteNodeRef == null)
        {
            throw new SiteDoesNotExistException(shortName);
        }

        String[] nameFilters = tokenizeFilterLowercase(nameFilter);
        String nameFilterLower = nameFilters.length>0?nameFilter.toLowerCase():null;

        QName siteType = directNodeService.getType(siteNodeRef);
        Set<String> permissions = this.permissionService.getSettablePermissions(siteType);
        Map<String, String> groupsToExpand = new HashMap<String, String>(32);
        
        for (String permission : permissions)
        {
            String groupName = getSiteRoleGroup(shortName, permission, true);
            
            if ((roleFilter == null || roleFilter.length() == 0 || roleFilter.equals(permission)) && this.authorityService.authorityExists(groupName))
            {
                Set<String> authorities = this.authorityService.getContainedAuthorities(null, groupName, true);
                for (String authority : authorities)
                {
                    switch (AuthorityType.getAuthorityType(authority))
                    {

                    case USER:
                        if(includeUsers)
                        {
                            if (isAcceptedName(nameFilter, authority))
                            {
                                // Add the user and their permission to the returned map
                                callback.siteMember(authority, permission, false);
                            }
                            if(callback.isDone())
                            {
                                break;
                            }
                        }
                        break;
                    case GROUP:
                        if (expandGroups)
                        {
                            if (!groupsToExpand.containsKey(authority))
                            {
                                groupsToExpand.put(authority, permission);
                            }
                        }
                        else if (includeGroups)
                        {
                            if (nameFilter != null && nameFilter.length() != 0)
                            {
                                // found a filter - does it match Group name part?
                                if (authority.substring(GROUP_PREFIX_LENGTH).toLowerCase().contains(nameFilterLower))
                                {
                                    callback.siteMember(authority, permission, false);
                                }
                                else
                                {
                                   // Does it match on the Group Display Name part instead?
                                   String displayName = authorityService.getAuthorityDisplayName(authority);
                                   if(displayName != null && displayName.toLowerCase().contains(nameFilterLower))
                                   {
                                      callback.siteMember(authority, permission, false);
                                   }
                                }
                            }
                            else
                            {
                                // No name filter add this group
                                callback.siteMember(authority, permission, false);
                            }
                            
                            if(callback.isDone())
                            {
                                break;
                            }
                        }
                        break;
                    default:
                        // TODO: Should error but do not want to break anything here, so will fall through (DH)
                        break;
                    }
                }
            }
        }
        
        if (expandGroups && includeUsers)
        {
            for (Map.Entry<String,String> entry : groupsToExpand.entrySet())
            {                
                Set<String> subUsers = this.authorityService.getContainedAuthorities(AuthorityType.USER, entry.getKey(), false);
                for (String subUser : subUsers)
                {
                    if (isAcceptedName(nameFilter, subUser))
                    {
                        // Add the collapsed user into the members list if they do not already appear in the list 
                        callback.siteMember(subUser, entry.getValue(), true);
                    }
                    
                    if(callback.isDone())
                    {
                        break;
                    }
                }
            }         
        }
    }

    /*
    * Build an array of name filter tokens pre lowercased to test against person properties
    * We require that matching people have at least one match against one of these on
    * either their firstname or last name
    */
    static String[] tokenizeFilterLowercase(String nameFilter)
    {
        String[] nameFilters = new String[0];
        if (nameFilter != null && nameFilter.length() != 0)
        {
            String[] tokens = nameFilter.split(" ");
            nameFilters = new String[tokens.length];
            for (int i = 0; i <tokens.length ; i++)
            {
                nameFilters[i] = tokens[i].toLowerCase();
            }
        }
        return nameFilters;
    }

    public PagingResults<SiteMembership> listMembersPaged(String shortName, boolean collapseGroups, List<Pair<SiteService.SortFields, Boolean>> sortProps, PagingRequest pagingRequest)
    {
        CannedQueryParameters params = getCannedQueryParameters(shortName, collapseGroups, sortProps, pagingRequest);

		CannedQuery<SiteMembership> query = new SiteMembersCannedQuery(this, personService, nodeService, params);

        CannedQueryResults<SiteMembership> results = query.execute();

        return getPagingResults(pagingRequest, results);
    }

    /**
     * @see org.alfresco.repo.node.NodeServicePolicies.OnRestoreNodePolicy#onRestoreNode(org.alfresco.service.cmr.repository.ChildAssociationRef)
     */
    @SuppressWarnings("unchecked")
    @Override
    public void onRestoreNode(ChildAssociationRef childAssocRef)
    {
        // regenerate the groups for the site when it is restored from the Archive store
        NodeRef siteRef = childAssocRef.getChildRef();
        setupSitePermissions(
                siteRef,
                (String)directNodeService.getProperty(siteRef, ContentModel.PROP_NAME),
                getSiteVisibility(siteRef),
                (Map<String, Set<String>>)directNodeService.getProperty(siteRef, QName.createQName(null, "memberships")), true);
    }

    /**
     * @see org.alfresco.repo.node.NodeArchiveServicePolicies.BeforeRestoreArchivedNodePolicy#beforeRestoreArchivedNode(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public void beforeRestoreArchivedNode(NodeRef nodeRef)
    {
        this.behaviourFilter.disableBehaviour(ContentModel.ASPECT_LOCKABLE);
    }

    /**
     * @see org.alfresco.repo.node.NodeArchiveServicePolicies.OnRestoreArchivedNodePolicy#onRestoreArchivedNode(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public void onRestoreArchivedNode(NodeRef nodeRef)
    {
        this.behaviourFilter.enableBehaviour(ContentModel.ASPECT_LOCKABLE);
    }

    /**
     * @see org.alfresco.service.cmr.site.SiteService#listMembers(java.lang.String, java.lang.String, java.lang.String, int)
     */
    public Map<String, String> listMembers(String shortName, String nameFilter, String roleFilter, int size)
    {
        return listMembers(shortName, nameFilter, roleFilter, size, false);
    }
    
    public Map<String, String> listMembers(String shortName, final String nameFilter, final String roleFilter, final int size, final boolean expandGroups)
    {
        // MT share - for activity service remote system callback (deprecated)
        if (tenantService.isEnabled() &&
            TenantUtil.isCurrentDomainDefault() &&
            (AuthenticationUtil.SYSTEM_USER_NAME.equals(AuthenticationUtil.getRunAsUser())) &&
            tenantService.isTenantName(shortName))
        {
            final String tenantDomain = tenantService.getDomain(shortName);
            final String sName = tenantService.getBaseName(shortName, true);
            
            return TenantUtil.runAsSystemTenant(new TenantRunAsWork<Map<String, String>>()
            {
                public Map<String, String> doWork() throws Exception
                {
                    return listMembersImpl(sName, nameFilter, roleFilter, size, expandGroups);
                }
            }, tenantDomain);
        }
        else
        {
            return listMembersImpl(shortName, nameFilter, roleFilter, size, expandGroups);
        }
    }
    
    /**
     * @see org.alfresco.service.cmr.site.SiteService#listMembersInfo(String,
     *      String, String, int, boolean)
     */
    public List<SiteMemberInfo> listMembersInfo(String shortName, final String nameFilter, final String roleFilter, final int size, final boolean expandGroups)
    {
        // MT share - for activity service system callback
        if (tenantService.isEnabled()
                    && (AuthenticationUtil.SYSTEM_USER_NAME.equals(AuthenticationUtil
                                .getRunAsUser())) && tenantService.isTenantName(shortName))
        {
            final String tenantDomain = tenantService.getDomain(shortName);
            final String sName = tenantService.getBaseName(shortName, true);

            return AuthenticationUtil.runAs(
                    () -> listMembersInfoImpl(sName, nameFilter, roleFilter, size, expandGroups),
                    tenantService.getDomainUser(AuthenticationUtil.getSystemUserName(), tenantDomain));
        }
        else
        {
            return listMembersInfoImpl(shortName, nameFilter, roleFilter, size, expandGroups);
        }
    }
    
    protected Map<String, String> listMembersImpl(String shortName, String nameFilter, String roleFilter, int size, boolean expandGroups)
    {
        List<SiteMemberInfo> list = listMembersInfoImpl(shortName, nameFilter, roleFilter, size, expandGroups);
        Map<String, String> members = new HashMap<String, String>(list.size());
        
        for (SiteMemberInfo info : list)
            members.put(info.getMemberName(), info.getMemberRole());

        return members;
    }
    
    protected List<SiteMemberInfo> listMembersInfoImpl(String shortName, String nameFilter,
                String roleFilter, int size, boolean expandGroups)
    {
        NodeRef siteNodeRef = getSiteNodeRef(shortName);
        if (siteNodeRef == null)
        {
            throw new SiteDoesNotExistException(shortName);
        }
        
        // max size limit
        if (size <= 0)
        {
            size = Integer.MAX_VALUE;
        }

        String[] nameFilters = tokenizeFilterLowercase(nameFilter);
        String nameFilterLower = nameFilters.length>0?nameFilter.toLowerCase():null;
        
        List<SiteMemberInfo> members = new ArrayList<SiteMemberInfo>(32);

        QName siteType = directNodeService.getType(siteNodeRef);
        Set<String> permissions = this.permissionService.getSettablePermissions(siteType);
        Map<String, String> groupsToExpand = new HashMap<String, String>(32);

        AUTHORITY_FIND: for (String permission : permissions)
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
                                members.add(new SiteMemberInfoImpl(authority, permission, false));
                                
                                // break on max size limit reached
                                if (members.size() >= size) 
                                {
                                    break AUTHORITY_FIND;
                                }
                            }
                            break;
                        case GROUP:
                            if (expandGroups)
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
                                    if (matchByFilter(authority.substring(GROUP_PREFIX_LENGTH).toLowerCase(), nameFilterLower))
                                    {
                                        members.add(new SiteMemberInfoImpl(authority, permission, false));
                                    }
                                    else
                                    {
                                        // Does it match on the Group Display Name part instead?
                                        String displayName = authorityService.getAuthorityDisplayName(authority);
                                        if (displayName != null && matchByFilter(displayName.toLowerCase(), nameFilterLower))
                                        {
                                            members.add(new SiteMemberInfoImpl(authority, permission, false));
                                        }
                                    }
                                }
                                else
                                {
                                    // No name filter add this group
                                    members.add(new SiteMemberInfoImpl(authority, permission, false));
                                }
                                
                                // break on max size limit reached
                                if (members.size() >= size) 
                                {
                                    break AUTHORITY_FIND;
                                }
                            }
                            break;
                        default:
                            break;
                    }
                }
            }
        }
        
        if ((expandGroups) && (members.size() < size))
        {
            GROUP_EXPAND: for (Map.Entry<String, String> entry : groupsToExpand.entrySet())
            {
                Set<String> subUsers = this.authorityService.getContainedAuthorities(AuthorityType.USER, entry.getKey(), false);
                for (String subUser : subUsers)
                {
                    if (isAcceptedName(nameFilter, subUser))
                    {
                        SiteMemberInfo memberInfo = new SiteMemberInfoImpl(subUser,entry.getValue(), true);
                        // Add the collapsed user into the members list if they do not already appear in the list
                        if (members.contains(memberInfo) == false)
                        {
                            members.add(memberInfo);
                        }
                        else
                        {
                            int index = members.indexOf(memberInfo);
                            int priority = roleComparator.compare(members.get(index).getMemberRole(), memberInfo.getMemberRole());
                            if (priority > 0)
                            {
                                members.set(index, memberInfo);
                            }
                        }
                        
                        // break on max size limit reached
                        if (members.size() >= size) 
                        {
                            break GROUP_EXPAND;
                        }
                    }
                }
            }
        }
        return members;
    }

    private boolean isAcceptedName(String nameFilter, String name)
    {
        boolean addUser = true;
        if (nameFilter != null && nameFilter.length() != 0 && !nameFilter.equals(name))
        {
            // found a filter - does it match person first/last name?
            addUser = matchPerson(tokenizeFilterLowercase(nameFilter), name);
        }
        return addUser;
    }

    /**
     * Helper to match name filters to Person properties.
     * 
     * One of the user's firstname or lastname must match at least
     *  one of the filters given.
     * 
     * @param nameFilters String[]
     * @param username String
     * @return boolean
     */
    private boolean matchPerson(final String[] nameFilters, final String username)
    {
        boolean addUser = false;
        
        try
        {
           NodeRef person = personService.getPerson(username, false);
           String firstName = (String)directNodeService.getProperty(person, ContentModel.PROP_FIRSTNAME);
           String lastName = (String)directNodeService.getProperty(person, ContentModel.PROP_LASTNAME);
           String userName = (String)directNodeService.getProperty(person, ContentModel.PROP_USERNAME);

           final String lowFirstName = (firstName != null ? firstName.toLowerCase() : "");
           final String lowLastName = (lastName != null ? lastName.toLowerCase() : "");
           final String lowUserName = (userName != null ? userName.toLowerCase() : "");
           for (int i=0; i<nameFilters.length; i++)
           {
               if (matchByFilter(lowUserName, nameFilters[i]) ||
                   matchByFilter(lowFirstName, nameFilters[i]) ||
                   matchByFilter(lowLastName, nameFilters[i]))
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
    
    private boolean matchByFilter(String compareString, String patternString)
    {
        if (compareString==null || compareString.isEmpty())
        {
            return false;
        }
        if (patternString==null || patternString.isEmpty())
        {
            return true;
        }
        StringBuilder paternStr=new StringBuilder();
        for (char c: patternString.toCharArray())
        {
            if (c=='*')
            {
                paternStr.append(".*");
            }
            else if (c=='(' || c==')')
            {
                paternStr.append("\\"+c);
            }
            else if (Character.isLetterOrDigit(c) || c=='*')
            {
                paternStr.append(c);
            }
            else paternStr.append("\\"+c);

        }
        Pattern p=Pattern.compile(paternStr.toString(), Pattern.CASE_INSENSITIVE);
        Matcher matcher=p.matcher(compareString);
        return matcher.matches();
    }

    /**
     * @see org.alfresco.service.cmr.site.SiteService#getMembersRoleInfo(java.lang.String, java.lang.String)
     */
    public SiteMemberInfo getMembersRoleInfo(String shortName, String authorityName)
    {
        NodeRef siteNodeRef = getSiteNodeRef(shortName);
        if (siteNodeRef == null)
        {
            throw new SiteDoesNotExistException(shortName);
        }
        
        QName siteType = directNodeService.getType(siteNodeRef);
        Set<String> permissions = this.permissionService.getSettablePermissions(siteType);
        // This set is a lazily evaluated one, so merely getting it in advance as we do here is not expensive
        Set<String> userAuthoritySet = this.authorityService.getAuthoritiesForUser(authorityName);
        for (String role : permissions)
        {
            String roleGroup = getSiteRoleGroup(shortName, role, true);
            Set<String> authorities = this.authorityService.getContainedAuthorities(null, roleGroup, true);
            if (authorities.contains(authorityName))
            {
                // found a direct membership for this user - return this role info
                return new SiteMemberInfoImpl(authorityName, role, false);
            }
            // crawl the cache from the role group down to find the authority
            else if (userAuthoritySet.contains(roleGroup))
            {
                return new SiteMemberInfoImpl(authorityName, role, true);
            }
        }
        
        return null;
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
            String shortNamePattern = '_' + shortName + '_';
            int index = group.lastIndexOf(shortNamePattern) + (shortNamePattern).length();
            if (index != -1)
            {
                result.add(group.substring(index));
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
           throw new SiteDoesNotExistException(siteShortName);
        }
        
        List<String> fullResult = new ArrayList<String>(5);
        QName siteType = directNodeService.getType(siteNodeRef);
        Set<String> roles = this.permissionService.getSettablePermissions(siteType);

        // First use the authority's cached recursive group memberships to answer the question quickly
        Set<String> authorities = authorityService.getAuthoritiesForUser(authorityName);
        for (String role : roles)
        {
            String roleGroup = getSiteRoleGroup(siteShortName, role, true);
            if (authorities.contains(roleGroup))
            {
                fullResult.add(roleGroup);
            }
        }
        
        // Unfortunately, due to direct membership taking precedence, we can't answer the question quickly if more than one role has been inherited
        if (fullResult.size() <= 1)
        {
            return fullResult;
        }
        
        // Check direct group memberships
        List<String> result = new ArrayList<String>(5);
        Set <String> authorityGroups = this.authorityService.getContainingAuthorities(AuthorityType.GROUP,
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
           throw new SiteDoesNotExistException(shortName);
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
           throw new SiteDoesNotExistException(shortName);
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
            if ((currentUserName.equals(authorityName) == true) || isSiteAdmin(currentUserName) ||
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
                            ACTIVITY_TOOL, getActivityUserData(authorityName, ""), authorityName);
                }
                else if (authorityType == AuthorityType.GROUP)
                {
                    String authorityDisplayName = authorityService.getAuthorityDisplayName(authorityName);
                    activityService.postActivity(
                            ActivityType.SITE_GROUP_REMOVED, shortName,
                            ACTIVITY_TOOL, getActivityGroupData(authorityDisplayName, ""));
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
     * @see org.alfresco.service.cmr.site.SiteService#canAddMember(java.lang.String,
     *      java.lang.String, java.lang.String)
     */
    public boolean canAddMember(final String shortName, final String authorityName, final String role)
    {
        final NodeRef siteNodeRef = getSiteNodeRef(shortName);
        if (siteNodeRef == null)
        {
            throw new SiteDoesNotExistException(shortName);
        }

        // Get the user's current role
        final String currentRole = getMembersRole(shortName, authorityName);

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
        return ((permissionService.hasPermission(siteNodeRef, PermissionService.CHANGE_PERMISSIONS) == AccessStatus.ALLOWED)
                    || isSiteAdmin(currentUserName) || (SiteVisibility.PUBLIC.equals(visibility)
                    && role.equals(SiteModel.SITE_CONSUMER) && authorityName.equals(currentUserName) && currentRole == null));
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
           throw new SiteDoesNotExistException(shortName);
        }

        // Get the user's current role
        final String currentRole = getMembersRole(shortName, authorityName);

        // Do nothing if the role of the user is not being changed
        if (currentRole == null || role.equals(currentRole) == false)
        {
            // TODO if this is the only site manager do not down grade their
            // permissions
            if(canAddMember(shortName, authorityName, role))
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

                AuthorityType authorityType = AuthorityType.getAuthorityType(authorityName);
                String authorityDisplayName = authorityName;
                if (authorityType == AuthorityType.GROUP)
                {
                    authorityDisplayName = authorityService.getAuthorityDisplayName(authorityName);
                }

                if (currentRole == null)
                {
                    if (authorityType == AuthorityType.USER)
                    {
                        activityService.postActivity(
                                ActivityType.SITE_USER_JOINED, shortName,
                                ACTIVITY_TOOL, getActivityUserData(authorityDisplayName, role), authorityName);
                    } 
                    else if (authorityType == AuthorityType.GROUP)
                    { 
                        activityService.postActivity(
                                ActivityType.SITE_GROUP_ADDED, shortName,
                                ACTIVITY_TOOL, getActivityGroupData(authorityDisplayName, role));                   
                    }
                }
                else
                {
                    if (authorityType == AuthorityType.USER)
                    {
                        activityService.postActivity(
                                ActivityType.SITE_USER_ROLE_UPDATE, shortName,
                                ACTIVITY_TOOL, getActivityUserData(authorityDisplayName, role));
                    } 
                    else if (authorityType == AuthorityType.GROUP)
                    {
                        activityService.postActivity(
                                ActivityType.SITE_GROUP_ROLE_UPDATE, shortName,
                                ACTIVITY_TOOL, getActivityGroupData(authorityDisplayName, role));
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

        // use the correct container for doclib
        // MNT-15743
        componentId = componentId.toLowerCase().equals(DOCUMENT_LIBRARY.toLowerCase()) ? DOCUMENT_LIBRARY : componentId;

        // retrieve site
        NodeRef siteNodeRef = getSiteNodeRef(shortName);
        if (siteNodeRef == null)
        {
           throw new SiteDoesNotExistException(shortName);
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
            else if (SiteVisibility.PRIVATE.equals(siteVisibility))
            {
                setPrivatePermissions(shortName, containerNodeRef);
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
     * @param targetNode NodeRef
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
        setNonPublicSitePermissions(shortName, containerNodeRef);
    }


    /**
     * @see org.alfresco.service.cmr.site.SiteService#getContainer(java.lang.String, String)
     */
    public NodeRef getContainer(String shortName, String componentId)
    {
        ParameterCheck.mandatoryString("componentId", componentId);

        // retrieve site
        NodeRef siteNodeRef = getSiteNodeRef(shortName);
        if (siteNodeRef == null)
        {
           throw new SiteDoesNotExistException(shortName);
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

    @SuppressWarnings("unchecked")
    public PagingResults<FileInfo> listContainers(String shortName, PagingRequest pagingRequest)
    {
        SiteContainersCannedQueryFactory sitesContainersCannedQueryFactory = (SiteContainersCannedQueryFactory)cannedQueryRegistry.getNamedObject("siteContainersCannedQueryFactory");

        CannedQueryPageDetails pageDetails = new CannedQueryPageDetails(pagingRequest.getSkipCount(), pagingRequest.getMaxItems());
        CannedQuerySortDetails sortDetails = new CannedQuerySortDetails(new Pair<Object, SortOrder>(SiteContainersCannedQueryParams.SortFields.ContainerName, SortOrder.ASCENDING));
        SiteContainersCannedQueryParams parameterBean = new SiteContainersCannedQueryParams(getSiteNodeRef(shortName));
        CannedQueryParameters params = new CannedQueryParameters(parameterBean, pageDetails, sortDetails, pagingRequest.getRequestTotalCountMax(), pagingRequest.getQueryExecutionId());

        CannedQuery<FileInfo> query = sitesContainersCannedQueryFactory.getCannedQuery(params);
        
        CannedQueryResults<FileInfo> results = query.execute();

        return getPagingResults(pagingRequest, results);        
    }

    /**
     * @see org.alfresco.service.cmr.site.SiteService#hasContainer(java.lang.String, String)
     */
    public boolean hasContainer(final String shortName, final String componentId)
    {
        ParameterCheck.mandatoryString("componentId", componentId);

        // retrieve site
        final NodeRef siteNodeRef = getSiteNodeRef(shortName);
        if (siteNodeRef == null)
        {
           throw new SiteDoesNotExistException(shortName);
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
     * Helper method to create a container if missing, and mark it as a
     *  tag scope if it isn't already one
     */
    public static NodeRef getSiteContainer(final String siteShortName, 
          final String componentName, final boolean create, 
          final SiteService siteService, final TransactionService transactionService,
          final TaggingService taggingService)
    {
       // Does the site exist?
       if(siteService.getSite(siteShortName) == null)
       {
          // Either the site doesn't exist, or you're not allowed to see it
          if(! create)
          {
             // Just say there's no container
             return null;
          }
          else
          {
             // We can't create on a non-existant site
             throw new AlfrescoRuntimeException(
                   "Unable to create the " + componentName + " container from a hidden or non-existant site"
             );
          }
       }
       
       // Check about the container
       if(! siteService.hasContainer(siteShortName, componentName))
       {
          if(create)
          {
             if(transactionService.isReadOnly())
             {
                throw new AlfrescoRuntimeException(
                      "Unable to create the " + componentName + " container from a read only transaction"
                );
             }
             
             // Have the site container created
             if(logger.isDebugEnabled())
             {
                logger.debug("Creating " + componentName + " container in site " + siteShortName);
             }
             
             NodeRef container = AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<NodeRef>() 
                {
                   public NodeRef doWork() throws Exception
                   {
                      // Create the site container
                      NodeRef container = siteService.createContainer(
                            siteShortName, componentName, null, null
                      );
   
                      // Done
                      return container;
                   }
                }, AuthenticationUtil.getSystemUserName()
             );
             
             if(logger.isDebugEnabled())
             {
                logger.debug("Created " + componentName + " as " + container + " for " + siteShortName);
             }
             
             // Container is setup and ready to use
             return container;
          }
          else
          {
             // No container for this site, and not allowed to create
             // Have the site container created
             if(logger.isDebugEnabled())
             {
                logger.debug("No " + componentName + " component in " + siteShortName + " and not creating");
             }
             return null;
          }
       }
       else
       {
          // Container is already there
          NodeRef containerTmp = null;
          try
          {
             containerTmp = siteService.getContainer(siteShortName, componentName);
          }
          catch(AccessDeniedException e)
          {
             if(!create)
             {
                // Just pretend it isn't there, as they can't see it
                return null;
             }
             else
             {
                // It's there, they can't see it, and they need it
                throw e;
             }
          }
          final NodeRef container = containerTmp;
       
          // Ensure the calendar container has the tag scope aspect applied to it
          if (!taggingService.isTagScope(container))
          {
             if(logger.isDebugEnabled())
             {
                logger.debug("Attaching tag scope to " + componentName + " " + container.toString() + " for " + siteShortName);
             }
             AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Void>()
             {
                 public Void doWork() throws Exception
                 {
                     RetryingTransactionHelper txnHelper = transactionService.getRetryingTransactionHelper();
                     txnHelper.setForceWritable(true);
                     txnHelper.doInTransaction(
                             new RetryingTransactionCallback<Void>()
                             {
                                 public Void execute() throws Throwable
                                 {
                                     // Add the tag scope aspect
                                     taggingService.addTagScope(container);
                                     return null;
                                 }
                             }, false, true
                   );
                   return null;
                 }
             }, AuthenticationUtil.getSystemUserName());
          }
          
          // Container is appropriately setup and configured
          return container;
       }
    }

    /**
     * Helper method to get the activity data for a user
     * 
     * @param userName      user name
     * @param role          role
     * @return String
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
    
    public int countAuthoritiesWithRole(String shortName, String role)
    {
        // Check that we are not about to remove the last site manager
        String group = getSiteRoleGroup(shortName, role, true);
        Set<String> siteUsers = this.authorityService.getContainedAuthorities(
                AuthorityType.USER, group, true);
        Set<String> siteGroups = this.authorityService.getContainedAuthorities(
                AuthorityType.GROUP, group, true);
        return siteUsers.size() + siteGroups.size();
    }

    /**
     * Helper to check that we are not removing the last Site Manager from a site
     * 
     * @param shortName String
     * @param authorityName String
     * @param role String
     */
    private void checkLastManagerRemoval(final String shortName, final String authorityName, final String role)
    {
        // Check that we are not about to remove the last site manager
        if (SiteModel.SITE_MANAGER.equals(role) == true)
        {
            int siteAuthorities = countAuthoritiesWithRole(shortName, SiteModel.SITE_MANAGER);
            if (siteAuthorities <= 1)
            {
                throw new SiteServiceException(MSG_DO_NOT_CHANGE_MGR, new Object[] {authorityName});
            }       
        }
    }

    public List<SiteInfo> listSites(Set<String> siteNames)
    {
        List<ChildAssociationRef> assocs = this.nodeService.getChildrenByName(
                getSiteRoot(),
                ContentModel.ASSOC_CONTAINS,
                siteNames);
        List<SiteInfo> result = new ArrayList<SiteInfo>(assocs.size());
        for (ChildAssociationRef assoc : assocs)
        {
            // Ignore any node that is not a "site" type
            NodeRef site = assoc.getChildRef();
            QName siteClassName = this.nodeService.getType(site);
            if (dictionaryService.isSubClass(siteClassName, SiteModel.TYPE_SITE))
            {
                result.add(createSiteInfo(site));
            }
        }
        return result;
    }

    private String resolveRole(String group)
    {
        // purge non Site related Groups and strip the group name down to the role
        if (group.startsWith(GROUP_SITE_PREFIX))
        {
            int roleIndex = group.lastIndexOf('_');
            if (roleIndex + 1 <= GROUP_SITE_PREFIX_LENGTH)
            {
                // There is no role associated
                return null;
            }
            else
            {
                return group.substring(roleIndex + 1);
            }
        }
        return null;
    }
    
    @Override
    public List<SiteMembership> listSiteMemberships(String userName, int size)
    {
        final List<String> siteNames = new LinkedList<String>();
        Map<String, String> roleSitePairs = new HashMap<String, String>();
        
        String actualUserName = personService.getUserIdentifier(userName);
        if(actualUserName == null)
        {
            return Collections.emptyList();
        }
        
        /* Get the site names and the map between the site name and the role
           MNT-13198 - use the bridge table */
        Set<String> containingAuthorities = authorityService.getContainingAuthorities(AuthorityType.GROUP, actualUserName, false);
        for(String authority : containingAuthorities)
        {
                String siteName = resolveSite(authority);
                if(siteName == null)
                {
                    continue;
                }
                
                /* if the size is not 0 check site existence */
                if(size > 0)
                {
                    /* if we found enough sites ignore the others */
                    if(siteNames.size() >= size)
                    {
                        break;
                    }
                    
                    /* MNT-10836 fix, after MNT-10109 we should also check site existence.
                       A simple exists check would be better than getting the site properties etc - profiling suggests x2 faster.
                       If size = -1 the sites that don't exist will be removed when getting the child associations. */
                    if(!hasSite(siteName))
                    {
                        // if the site doesn't exist skip the current authority
                        continue;
                    }
                }
                
                /* resolve the role */
                String role = resolveRole(authority);
                if (role != null)
                {
                    siteNames.add(siteName);
                    roleSitePairs.put(siteName, role);
                }
        }
        
        if (siteNames.isEmpty())
        {
            return Collections.emptyList();
        }
        
        /* Get the child associations */
        List<ChildAssociationRef> assocs = getSitesAssocsByName(siteNames);
        
        /* Get the node refs and preload the nodes to get the properties faster */
        List<NodeRef> siteNodes = new LinkedList<NodeRef>();
        for (ChildAssociationRef assoc : assocs)
        {
            siteNodes.add(assoc.getChildRef());
        }
        nodeDAO.cacheNodes(siteNodes);

        // (MNT-19035) When querying the site memberships for users other than the authenticated user or site admins, an extra check on the site visibility is enforced.
        boolean isMe = authenticationContext.getCurrentUserName().equals(actualUserName);
        boolean isSiteAdmin = isSiteAdmin(authenticationContext.getCurrentUserName());
        boolean checkAccess = !isMe && !isSiteAdmin;

        /* Compute the site membership objects */
        List<SiteMembership> result = new LinkedList<>();
        for (NodeRef site : siteNodes)
        {
            /* Ignore any node that is not a "site" type */
            QName siteClassName = this.directNodeService.getType(site);
            if (this.dictionaryService.isSubClass(siteClassName, SiteModel.TYPE_SITE))
            {
                if (checkAccess)
                {
                    // Private sites are dismissed.
                    SiteVisibility visibility = getSiteVisibility(site);
                    if (visibility == SiteVisibility.PRIVATE)
                    {
                        continue;
                    }
                }
                SiteInfo siteInfo = createSiteInfo(site);
                String role = roleSitePairs.get(siteInfo.getShortName());

                /* Fix for ALF-21924. Role will be null in case the site id doesn't match the site added in roleSitePairs.
                   This will fix cases where there is a site in trashcan with different case than an existing site. */
                if (role != null)
                {
                    result.add(new SiteMembership(siteInfo, userName, role));
                }
            }
        }
        
        return result;
    }
    
    /**
     * Retrieves the child associations for requested site names 
     * @param siteNames names of the sites to be retrieved
     * @return list of child associations between the site root and sites having the requested site names
     */
    private List<ChildAssociationRef> getSitesAssocsByName(final List<String> siteNames)
    { 
        Iterator<String> sitesIterator = siteNames.iterator();
        List<String> iterationPage = new LinkedList<String>();
        List<ChildAssociationRef> assocs = new LinkedList<ChildAssociationRef>();
        List<ChildAssociationRef> childRefPage;
        
        NodeRef siteRoot = getSiteRoot();
        
        /* iterate the results and retrieve child associations for batches of 512 elements */
        while(sitesIterator.hasNext())
        {
            String element = sitesIterator.next();
            iterationPage.add(element);

            /* when the page is full, get the child associations and clean the page */
            if(iterationPage.size() >= GET_CHILD_ASSOCS_PAGE_SIZE)
            {
                /* get the child associations for the current page having siteRoot as parent
                   use directNodeService to avoid permission check */
                childRefPage = directNodeService.getChildrenByName(siteRoot, ContentModel.ASSOC_CONTAINS, iterationPage);
                assocs.addAll(childRefPage);
                iterationPage.clear();
            }
        }
        if(iterationPage.size() > 0)
        {
            /* use directNodeService to avoid permission check */
            childRefPage = directNodeService.getChildrenByName(siteRoot, ContentModel.ASSOC_CONTAINS, iterationPage);
            assocs.addAll(childRefPage);
        }
        
        return assocs;
    }
        
	public PagingResults<SiteMembership> listSitesPaged(final String userName, List<Pair<SiteService.SortFields, Boolean>> sortProps, final PagingRequest pagingRequest)
	{
		List<SiteMembership> siteMembers = listSiteMemberships (userName, 0);
	    final int totalSize = siteMembers.size();
	 	final PageDetails pageDetails = PageDetails.getPageDetails(pagingRequest, totalSize);
	 	final List<SiteMembership> resultList;
	 	if (sortProps == null)
	 	{
	 		resultList = siteMembers;
	 	}
	 	else
	 	{
	 		List<Pair<? extends Object, SortOrder>> sortPairs = new ArrayList<Pair<? extends Object, SortOrder>>(sortProps.size());
            for (Pair<SiteService.SortFields, Boolean> sortProp : sortProps)
            {
                sortPairs.add(new Pair<SiteService.SortFields, SortOrder>(sortProp.getFirst(), (sortProp.getSecond() ? SortOrder.ASCENDING : SortOrder.DESCENDING)));
            }
	 		TreeSet<SiteMembership> sortedSet = new TreeSet<SiteMembership>(new SiteMembershipComparator(sortPairs, SiteMembershipComparator.Type.SITES));
	 		sortedSet.addAll(siteMembers);
	 		resultList = new ArrayList<SiteMembership>(sortedSet);
	 	}
		
	 	PagingResults<SiteMembership> res = new PagingResults<SiteMembership>() {
			
			@Override
			public boolean hasMoreItems() {
				return pageDetails.hasMoreItems();
			}
			
			@Override
			public Pair<Integer, Integer> getTotalResultCount() {
				Integer size = totalSize;
				return new Pair<Integer, Integer>(size, size);
			}
			
			@Override
			public String getQueryExecutionId() {
				return GUID.generate();
			}
			
			@Override
			public List<SiteMembership> getPage() {
				return resultList;
			}
		};

	    return res;
    }

    private <T extends Object> PagingResults<T> getPagingResults(PagingRequest pagingRequest, final CannedQueryResults<T> results)
    {
        List<T> entities = null;
        if (results.getPageCount() > 0)
        {
            entities = results.getPages().get(0);
        }
        else
        {
            entities = Collections.emptyList();
        }
        
        // set total count
        final Pair<Integer, Integer> totalCount;
        if (pagingRequest.getRequestTotalCountMax() > 0)
        {
            totalCount = results.getTotalResultCount();
        }
        else
        {
            totalCount = null;
        }
        
        final List<T> members = new ArrayList<T>(entities.size());
        for (T entity : entities)
        {
            members.add(entity);
        }
        
        return new PagingResults<T>()
        {
            @Override
            public String getQueryExecutionId()
            {
                return results.getQueryExecutionId();
            }

            @Override
            public List<T> getPage()
            {
                return members;
            }
            
            @Override
            public boolean hasMoreItems()
            {
                return results.hasMoreItems();
            }

            @Override
            public Pair<Integer, Integer> getTotalResultCount()
            {
                return totalCount;
            }
        };
    }
	
    /**
     * Private sites have separate ACLs on each component and don't inherit from the
     * site which has consumer role for everyone.
     */    
    private void setPrivatePermissions(String shortName, NodeRef containerNodeRef)   
    {
        setNonPublicSitePermissions(shortName, containerNodeRef);
    }

    private void setNonPublicSitePermissions(String shortName, NodeRef containerNodeRef)
    {
        NodeRef siteNodeRef = getSiteNodeRef(shortName);
        if (siteNodeRef == null) 
        { 
           throw new SiteDoesNotExistException(shortName);
        }
        
        QName siteType = directNodeService.getType(siteNodeRef);
        Set<String> permissions = permissionService.getSettablePermissions(siteType);
        for (String permission : permissions)
        {
            String permissionGroup = getSiteRoleGroup(shortName, permission, true);
            // Assign the group the relevant permission on the site
            permissionService.setPermission(containerNodeRef, permissionGroup, permission, true);
        }  
        
        this.permissionService.setInheritParentPermissions(containerNodeRef, false);
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean isSiteAdmin(String userName)
    {
        if (userName == null)
        {
            return false;
        }
        return this.authorityService.isAdminAuthority(userName)
                    || this.authorityService.getAuthoritiesForUser(userName).contains(
                                GROUP_SITE_ADMINISTRATORS_AUTHORITY);
    }

    public void setEventPublisher(EventPublisher eventPublisher)
    {
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void listMembers(String shortName, final String nameFilter, final String roleFilter, boolean includeUsers, boolean includeGroups, boolean expandGroups, SiteMembersCallback callback)
    {
        // MT share - for activity service system callback
        if (tenantService.isEnabled() && (AuthenticationUtil.SYSTEM_USER_NAME.equals(AuthenticationUtil.getRunAsUser())) && tenantService.isTenantName(shortName))
        {
            final String tenantDomain = tenantService.getDomain(shortName);
            final String sName = tenantService.getBaseName(shortName, true);

            TenantUtil.runAsSystemTenant(() -> {
                listMembersImpl(sName, nameFilter, roleFilter, expandGroups, callback, includeUsers, includeGroups);
                return null;
            }, tenantDomain);
        }
        else
        {
            listMembersImpl(shortName, nameFilter, roleFilter, expandGroups, callback, includeUsers, includeGroups);
        }
    }

    @Override
    public PagingResults<SiteGroupMembership> listGroupMembersPaged(String shortName, List<Pair<SortFields, Boolean>> sortProps, PagingRequest pagingRequest)
    {
        CannedQueryParameters params = getCannedQueryParameters(shortName, false, sortProps, pagingRequest);
        CannedQuery<SiteGroupMembership> query = new SiteGroupCannedQuery(this, authorityService, params);

        CannedQueryResults<SiteGroupMembership> results = query.execute();

        return getPagingResults(pagingRequest, results);
    }

    private CannedQueryParameters getCannedQueryParameters(String shortName, boolean expandGroups, List<Pair<SortFields, Boolean>> sortProps, PagingRequest pagingRequest)
    {
        CannedQueryPageDetails pageDetails = new CannedQueryPageDetails(pagingRequest.getSkipCount(), pagingRequest.getMaxItems());

        List<Pair<? extends Object, SortOrder>> sortPairs = sortProps.stream().map(props -> new Pair<SortFields, SortOrder>(props.getFirst(), (props.getSecond() ? SortOrder.ASCENDING : SortOrder.DESCENDING))).collect(Collectors.toList());
        CannedQuerySortDetails sortDetails = new CannedQuerySortDetails(sortPairs);

        SiteMembersCannedQueryParams parameterBean = new SiteMembersCannedQueryParams(shortName, expandGroups);
        return new CannedQueryParameters(parameterBean, pageDetails, sortDetails, pagingRequest.getRequestTotalCountMax(), pagingRequest.getQueryExecutionId());
    }
}
