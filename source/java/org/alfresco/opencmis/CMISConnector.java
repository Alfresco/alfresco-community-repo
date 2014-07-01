/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
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
package org.alfresco.opencmis;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.SequenceInputStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.EnumSet;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeSet;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;

import org.alfresco.cmis.CMISDictionaryModel;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.events.types.ContentEvent;
import org.alfresco.events.types.ContentEventImpl;
import org.alfresco.events.types.ContentReadRangeEvent;
import org.alfresco.events.types.Event;
import org.alfresco.model.ContentModel;
import org.alfresco.opencmis.ActivityPosterImpl.ActivityInfo;
import org.alfresco.opencmis.dictionary.CMISActionEvaluator;
import org.alfresco.opencmis.dictionary.CMISAllowedActionEnum;
import org.alfresco.opencmis.dictionary.CMISDictionaryService;
import org.alfresco.opencmis.dictionary.CMISNodeInfo;
import org.alfresco.opencmis.dictionary.CMISObjectVariant;
import org.alfresco.opencmis.dictionary.CMISPropertyAccessor;
import org.alfresco.opencmis.dictionary.DocumentTypeDefinitionWrapper;
import org.alfresco.opencmis.dictionary.FolderTypeDefintionWrapper;
import org.alfresco.opencmis.dictionary.ItemTypeDefinitionWrapper;
import org.alfresco.opencmis.dictionary.PropertyDefinitionWrapper;
import org.alfresco.opencmis.dictionary.TypeDefinitionWrapper;
import org.alfresco.opencmis.mapping.DirectProperty;
import org.alfresco.opencmis.search.CMISQueryOptions;
import org.alfresco.opencmis.search.CMISQueryOptions.CMISQueryMode;
import org.alfresco.opencmis.search.CMISQueryService;
import org.alfresco.opencmis.search.CMISResultSet;
import org.alfresco.opencmis.search.CMISResultSetColumn;
import org.alfresco.opencmis.search.CMISResultSetRow;
import org.alfresco.repo.action.executer.ContentMetadataExtracter;
import org.alfresco.repo.cache.SimpleCache;
import org.alfresco.repo.events.EventPreparator;
import org.alfresco.repo.events.EventPublisher;
import org.alfresco.repo.model.filefolder.GetChildrenCannedQuery;
import org.alfresco.repo.model.filefolder.HiddenAspect;
import org.alfresco.repo.model.filefolder.HiddenAspect.Visibility;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.repo.security.permissions.PermissionReference;
import org.alfresco.repo.security.permissions.impl.AccessPermissionImpl;
import org.alfresco.repo.security.permissions.impl.ModelDAO;
import org.alfresco.repo.tenant.TenantAdminService;
import org.alfresco.repo.tenant.TenantDeployer;
import org.alfresco.repo.thumbnail.ThumbnailDefinition;
import org.alfresco.repo.thumbnail.ThumbnailHelper;
import org.alfresco.repo.thumbnail.ThumbnailRegistry;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.repo.version.VersionBaseModel;
import org.alfresco.repo.version.VersionModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.audit.AuditQueryParameters;
import org.alfresco.service.cmr.audit.AuditService;
import org.alfresco.service.cmr.audit.AuditService.AuditQueryCallback;
import org.alfresco.service.cmr.coci.CheckOutCheckInService;
import org.alfresco.service.cmr.dictionary.AspectDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.InvalidAspectException;
import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.model.FileExistsException;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.rendition.RenditionService;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.repository.Path.ChildAssocElement;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AccessPermission;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.thumbnail.ThumbnailService;
import org.alfresco.service.cmr.version.VersionHistory;
import org.alfresco.service.cmr.version.VersionService;
import org.alfresco.service.cmr.version.VersionType;
import org.alfresco.service.descriptor.Descriptor;
import org.alfresco.service.descriptor.DescriptorService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.FileFilterMode;
import org.alfresco.util.FileFilterMode.Client;
import org.alfresco.util.Pair;
import org.alfresco.util.TempFileProvider;
import org.apache.chemistry.opencmis.commons.BasicPermissions;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.Ace;
import org.apache.chemistry.opencmis.commons.data.Acl;
import org.apache.chemistry.opencmis.commons.data.AllowableActions;
import org.apache.chemistry.opencmis.commons.data.CmisExtensionElement;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.data.ObjectData;
import org.apache.chemistry.opencmis.commons.data.ObjectList;
import org.apache.chemistry.opencmis.commons.data.PermissionMapping;
import org.apache.chemistry.opencmis.commons.data.Properties;
import org.apache.chemistry.opencmis.commons.data.PropertyData;
import org.apache.chemistry.opencmis.commons.data.PropertyId;
import org.apache.chemistry.opencmis.commons.data.PropertyString;
import org.apache.chemistry.opencmis.commons.data.RenditionData;
import org.apache.chemistry.opencmis.commons.data.RepositoryInfo;
import org.apache.chemistry.opencmis.commons.definitions.DocumentTypeDefinition;
import org.apache.chemistry.opencmis.commons.definitions.PermissionDefinition;
import org.apache.chemistry.opencmis.commons.definitions.PropertyDefinition;
import org.apache.chemistry.opencmis.commons.enums.AclPropagation;
import org.apache.chemistry.opencmis.commons.enums.Action;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.enums.CapabilityAcl;
import org.apache.chemistry.opencmis.commons.enums.CapabilityChanges;
import org.apache.chemistry.opencmis.commons.enums.CapabilityContentStreamUpdates;
import org.apache.chemistry.opencmis.commons.enums.CapabilityJoin;
import org.apache.chemistry.opencmis.commons.enums.CapabilityQuery;
import org.apache.chemistry.opencmis.commons.enums.CapabilityRenditions;
import org.apache.chemistry.opencmis.commons.enums.Cardinality;
import org.apache.chemistry.opencmis.commons.enums.ChangeType;
import org.apache.chemistry.opencmis.commons.enums.CmisVersion;
import org.apache.chemistry.opencmis.commons.enums.ContentStreamAllowed;
import org.apache.chemistry.opencmis.commons.enums.IncludeRelationships;
import org.apache.chemistry.opencmis.commons.enums.PropertyType;
import org.apache.chemistry.opencmis.commons.enums.RelationshipDirection;
import org.apache.chemistry.opencmis.commons.enums.SupportedPermissions;
import org.apache.chemistry.opencmis.commons.enums.Updatability;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.exceptions.CmisBaseException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisConstraintException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisContentAlreadyExistsException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisInvalidArgumentException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisRuntimeException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisStreamNotSupportedException;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.AbstractPropertyData;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.AccessControlEntryImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.AccessControlListImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.AccessControlPrincipalDataImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.AclCapabilitiesDataImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.AllowableActionsImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ChangeEventInfoDataImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.CmisExtensionElementImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ContentStreamImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ObjectDataImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ObjectListImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PermissionDefinitionDataImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PermissionMappingDataImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PolicyIdListImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertiesImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyBooleanImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyDateTimeImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyDecimalImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyHtmlImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyIdImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyIntegerImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyStringImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyUriImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.RepositoryCapabilitiesImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.RepositoryInfoImpl;
import org.apache.chemistry.opencmis.commons.server.CallContext;
import org.apache.chemistry.opencmis.commons.server.CmisService;
import org.apache.chemistry.opencmis.commons.spi.Holder;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ApplicationContextEvent;
import org.springframework.extensions.surf.util.AbstractLifecycleBean;
import org.springframework.util.StringUtils;

/**
 * Bridge connecting Alfresco and OpenCMIS.
 * <p/>
 * This class  provides many of the typical services that the {@link CmisService} implementation
 * will need to use Alfresco.
 * 
 * @author florian.mueller
 * @author Derek Hulley
 * @author steveglover
 */
public class CMISConnector implements ApplicationContextAware, ApplicationListener<ApplicationContextEvent>, TenantDeployer
{
    private static Log logger = LogFactory.getLog(CMISConnector.class);

    // mappings from cmis property names to their Alfresco property name counterparts (used by getChildren)
    private static Map<String, QName> SORT_PROPERTY_MAPPINGS = new HashMap<String, QName>();
    
    static
    {
    	SORT_PROPERTY_MAPPINGS.put(PropertyIds.LAST_MODIFICATION_DATE, ContentModel.PROP_MODIFIED);
    	SORT_PROPERTY_MAPPINGS.put(PropertyIds.CREATION_DATE, ContentModel.PROP_CREATED);
    }

    public static final char ID_SEPERATOR = ';';
    public static final String ASSOC_ID_PREFIX = "assoc:";
    public static final String PWC_VERSION_LABEL = "pwc";
    public static final String UNVERSIONED_VERSION_LABEL = "1.0";

    public static final String RENDITION_NONE = "cmis:none";

    public static final String CMIS_CHANGELOG_AUDIT_APPLICATION = "CMISChangeLog";

    public static final String ALFRESCO_EXTENSION_NAMESPACE = "http://www.alfresco.org";
    public static final String CMIS_NAMESPACE = "http://docs.oasis-open.org/ns/cmis/core/200908/";
    public static final String ASPECTS = "aspects";
    public static final String SET_ASPECTS = "setAspects";
    public static final String APPLIED_ASPECTS = "appliedAspects";
    public static final String ASPECTS_TO_ADD = "aspectsToAdd";
    public static final String ASPECTS_TO_REMOVE = "aspectsToRemove";
    public static final String PROPERTIES = "properties";

    private static final BigInteger TYPES_DEFAULT_MAX_ITEMS = BigInteger.valueOf(50);
    private static final BigInteger TYPES_DEFAULT_DEPTH = BigInteger.valueOf(-1);
    private static final BigInteger OBJECTS_DEFAULT_MAX_ITEMS = BigInteger.valueOf(200);
    private static final BigInteger OBJECTS_DEFAULT_DEPTH = BigInteger.valueOf(10);

    private static final String QUERY_NAME_OBJECT_ID = "cmis:objectId";
    private static final String QUERY_NAME_OBJECT_TYPE_ID = "cmis:objectTypeId";
    private static final String QUERY_NAME_BASE_TYPE_ID = "cmis:baseTypeId";

    private static final String CMIS_USER = "cmis:user";

	private static final BigInteger maxInt = BigInteger.valueOf(Integer.MAX_VALUE);
	private static final BigInteger minInt = BigInteger.valueOf(Integer.MIN_VALUE);
	private static final BigInteger maxLong  = BigInteger.valueOf(Long.MAX_VALUE);
	private static final BigInteger minLong  = BigInteger.valueOf(Long.MIN_VALUE);

    // lifecycle
    private ProcessorLifecycle lifecycle = new ProcessorLifecycle();

    // Alfresco objects
    private DescriptorService descriptorService;
    private NodeService nodeService;
    private VersionService versionService;
    private CheckOutCheckInService checkOutCheckInService;
    private LockService lockService;
    private ContentService contentService;
    private RenditionService renditionService;
    private FileFolderService fileFolderService;
    private TenantAdminService tenantAdminService;
    private TransactionService transactionService;
    private AuthenticationService authenticationService;
    private PermissionService permissionService;
    private ModelDAO permissionModelDao;
    private CMISDictionaryService cmisDictionaryService;
    private CMISDictionaryService cmisDictionaryService11;
    private CMISQueryService cmisQueryService;
    private CMISQueryService cmisQueryService11;
    private MimetypeService mimetypeService;
    private AuditService auditService;
    private NamespaceService namespaceService;
    private SearchService searchService;
    private DictionaryService dictionaryService;
    private SiteService siteService;
    private ActionService actionService;
    private ThumbnailService thumbnailService;
    private ServiceRegistry serviceRegistry;
    private EventPublisher eventPublisher;

    private ActivityPoster activityPoster;

    private BehaviourFilter behaviourFilter;

    private HiddenAspect hiddenAspect;

    private StoreRef storeRef;
    private String rootPath;
    private Map<String, List<String>> kindToRenditionNames;
    
    // note: cache is tenant-aware (if using TransctionalCache impl)
    
    private SimpleCache<String, Object> singletonCache; // eg. for cmisRootNodeRef, cmisRenditionMapping
    private final String KEY_CMIS_ROOT_NODEREF = "key.cmisRoot.noderef";
    private final String KEY_CMIS_RENDITION_MAPPING_NODEREF = "key.cmisRenditionMapping.noderef";
    
    private String proxyUser;
    private boolean openHttpSession = false;

    // OpenCMIS objects
    private BigInteger typesDefaultMaxItems = TYPES_DEFAULT_MAX_ITEMS;
    private BigInteger typesDefaultDepth = TYPES_DEFAULT_DEPTH;
    private BigInteger objectsDefaultMaxItems = OBJECTS_DEFAULT_MAX_ITEMS;
    private BigInteger objectsDefaultDepth = OBJECTS_DEFAULT_DEPTH;

    private List<PermissionDefinition> repositoryPermissions;
    private Map<String, PermissionMapping> permissionMappings;

    private ObjectFilter objectFilter;

    // --------------------------------------------------------------
    // Configuration
    // --------------------------------------------------------------

    public void setObjectFilter(ObjectFilter objectFilter)
    {
		this.objectFilter = objectFilter;
	}

	/**
     * Sets the root store.
     * 
     * @param store
     *            store_type://store_id
     */
    public void setStore(String store)
    {
        this.storeRef = new StoreRef(store);
    }

    public void setSiteService(SiteService siteService)
    {
		this.siteService = siteService;
	}

	public void setActivityPoster(ActivityPoster activityPoster)
    {
		this.activityPoster = activityPoster;
	}
    
	public ActivityPoster getActivityPoster()
	{
		return activityPoster;
	}

	public void setHiddenAspect(HiddenAspect hiddenAspect)
    {
		this.hiddenAspect = hiddenAspect;
	}

    public boolean isHidden(NodeRef nodeRef)
    {
        final Client client = FileFilterMode.getClient();
    	return (hiddenAspect.getVisibility(client, nodeRef) == Visibility.NotVisible);
    }

    /**
     * Sets the root path.
     * 
     * @param path
     *            path within default store
     */
    public void setRootPath(String path)
    {
        rootPath = path;
    }

    public BigInteger getTypesDefaultMaxItems()
    {
        return typesDefaultMaxItems;
    }

    public void setTypesDefaultMaxItems(BigInteger typesDefaultMaxItems)
    {
        this.typesDefaultMaxItems = typesDefaultMaxItems;
    }

    public BigInteger getTypesDefaultDepth()
    {
        return typesDefaultDepth;
    }

    public void setTypesDefaultDepth(BigInteger typesDefaultDepth)
    {
        this.typesDefaultDepth = typesDefaultDepth;
    }

    public BigInteger getObjectsDefaultMaxItems()
    {
        return objectsDefaultMaxItems;
    }

    public void setObjectsDefaultMaxItems(BigInteger objectsDefaultMaxItems)
    {
        this.objectsDefaultMaxItems = objectsDefaultMaxItems;
    }

    public BigInteger getObjectsDefaultDepth()
    {
        return objectsDefaultDepth;
    }

    public void setObjectsDefaultDepth(BigInteger objectsDefaultDepth)
    {
        this.objectsDefaultDepth = objectsDefaultDepth;
    }

    /**
     * Set rendition kind mapping.
     */
    public void setRenditionKindMapping(Map<String, List<String>> renditionKinds)
    {
        this.kindToRenditionNames = renditionKinds;
    }

    public void setOpenHttpSession(boolean openHttpSession)
    {
        this.openHttpSession = openHttpSession;
    }

    public boolean openHttpSession()
    {
        return openHttpSession;
    }

    public void setThumbnailService(ThumbnailService thumbnailService)
    {
		this.thumbnailService = thumbnailService;
	}
    
	public void setServiceRegistry(ServiceRegistry serviceRegistry)
	{
		this.serviceRegistry = serviceRegistry;
	}

	/**
     * Sets the descriptor service.
     */
    public void setDescriptorService(DescriptorService descriptorService)
    {
        this.descriptorService = descriptorService;
    }

    public DescriptorService getDescriptorService()
    {
        return descriptorService;
    }

    public void setBehaviourFilter(BehaviourFilter behaviourFilter)
    {
        this.behaviourFilter = behaviourFilter;
    }

    /**
     * Sets the node service.
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    public NodeService getNodeService()
    {
        return nodeService;
    }

	public void setActionService(ActionService actionService)
	{
		this.actionService = actionService;
	}

	/**
     * Sets the version service.
     */
    public void setVersionService(VersionService versionService)
    {
        this.versionService = versionService;
    }

    public VersionService getVersionService()
    {
        return versionService;
    }

    /**
     * Sets the checkOut/checkIn service.
     */
    public void setCheckOutCheckInService(CheckOutCheckInService checkOutCheckInService)
    {
        this.checkOutCheckInService = checkOutCheckInService;
    }

    public CheckOutCheckInService getCheckOutCheckInService()
    {
        return checkOutCheckInService;
    }

    /**
     * Sets the lock service.
     */
    public LockService getLockService()
    {
        return lockService;
    }

    public void setLockService(LockService lockService)
    {
        this.lockService = lockService;
    }

    /**
     * Sets the content service.
     */
    public void setContentService(ContentService contentService)
    {
        this.contentService = contentService;
    }

    public ContentService getContentService()
    {
        return contentService;
    }
    
    /**
     * Sets the event publisher
     */
    public void setEventPublisher(EventPublisher eventPublisher)
    {
        this.eventPublisher = eventPublisher;
    }
    
    /**
     * Sets the rendition service.
     */
    public void setrenditionService(RenditionService renditionService)
    {
        this.renditionService = renditionService;
    }

    /**
     * Sets the file folder service.
     */
    public void setFileFolderService(FileFolderService fileFolderService)
    {
        this.fileFolderService = fileFolderService;
    }

    public FileFolderService getFileFolderService()
    {
        return fileFolderService;
    }

    /**
     * Sets the tenant admin service.
     */
    public void setTenantAdminService(TenantAdminService tenantAdminService)
    {
        this.tenantAdminService = tenantAdminService;
    }
    
    public void setSingletonCache(SimpleCache<String, Object> singletonCache)
    {
        this.singletonCache = singletonCache;
    }
    
    /**
     * Sets the transaction service.
     */
    public void setTransactionService(TransactionService transactionService)
    {
        this.transactionService = transactionService;
    }

    public TransactionService getTransactionService()
    {
        return transactionService;
    }

    /**
     * Sets the authentication service.
     */
    public void setAuthenticationService(AuthenticationService authenticationService)
    {
        this.authenticationService = authenticationService;
    }

    public AuthenticationService getAuthenticationService()
    {
        return authenticationService;
    }

    /**
     * Sets the permission service.
     */
    public void setPermissionService(PermissionService permissionService)
    {
        this.permissionService = permissionService;
    }

    /**
     * Sets the permission model DAO.
     */
    public void setPermissionModelDao(ModelDAO permissionModelDao)
    {
        this.permissionModelDao = permissionModelDao;
    }

    public void setOpenCMISDictionaryService(CMISDictionaryService cmisDictionaryService)
    {
        this.cmisDictionaryService = cmisDictionaryService;
    }

    public void setOpenCMISDictionaryService11(CMISDictionaryService cmisDictionaryService)
    {
        this.cmisDictionaryService11 = cmisDictionaryService;
    }

    public CMISDictionaryService getOpenCMISDictionaryService()
    {
        CmisVersion cmisVersion = getRequestCmisVersion();
        if(cmisVersion.equals(CmisVersion.CMIS_1_0))
        {
            return cmisDictionaryService;
        }
        else
        {
            return cmisDictionaryService11;
        }
    }

    /**
     * Sets the OpenCMIS query service.
     */
    public void setOpenCMISQueryService(CMISQueryService cmisQueryService)
    {
        this.cmisQueryService = cmisQueryService;
    }

    public void setOpenCMISQueryService11(CMISQueryService cmisQueryService)
    {
        this.cmisQueryService11 = cmisQueryService;
    }
    
    public CMISQueryService getOpenCMISQueryService()
    {
        CmisVersion cmisVersion = getRequestCmisVersion();
        if(cmisVersion.equals(CmisVersion.CMIS_1_0))
        {
            return cmisQueryService;
        }
        else
        {
            return cmisQueryService11;
        }
    }

    /**
     * Sets the MIME type service.
     */
    public void setMimetypeService(MimetypeService mimetypeService)
    {
        this.mimetypeService = mimetypeService;
    }

    public MimetypeService getMimetypeService()
    {
        return mimetypeService;
    }

    /**
     * Sets the audit service.
     */
    public void setAuditService(AuditService auditService)
    {
        this.auditService = auditService;
    }

    /**
     * Sets the namespace service.
     */
    public void setNamespaceService(NamespaceService namespaceService)
    {
        this.namespaceService = namespaceService;
    }

    /**
     * Sets the search service.
     */
    public void setSearchService(SearchService searchService)
    {
        this.searchService = searchService;
    }

    public SearchService getSearchService()
    {
        return searchService;
    }

    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }

    public DictionaryService getDictionaryService()
    {
        return dictionaryService;
    }

    /**
     * Not implemented
     * @throws UnsupportedOperationException always
     */
    public void setProxyUser(String proxyUser)
    {
//        this.proxyUser = proxyUser;
        throw new UnsupportedOperationException("proxyUser setting not implemented.  Please raise a JIRA request.");
    }

    public String getProxyUser()
    {
        return proxyUser;
    }

    // --------------------------------------------------------------
    // Lifecycle methods
    // --------------------------------------------------------------

    private File tmp;
    
    public void setup()
    {
    	File tempDir = TempFileProvider.getTempDir();
    	this.tmp = new File(tempDir, "CMISAppend");
    	if(!this.tmp.exists() && !this.tmp.mkdir())
    	{
    		throw new AlfrescoRuntimeException("Failed to create CMIS temporary directory");
    	}
    }
    
    public void init()
    {
        // register as tenant deployer
        tenantAdminService.register(this);

        // set up and cache rendition mapping
        getRenditionMapping();

        // cache root node ref
        getRootNodeRef();

        // cache permission definitions and permission mappings
        repositoryPermissions = getRepositoryPermissions();
        permissionMappings = getPermissionMappings();
    }

    public void destroy()
    {
        singletonCache.remove(KEY_CMIS_ROOT_NODEREF);
        singletonCache.remove(KEY_CMIS_RENDITION_MAPPING_NODEREF);
    }

    public void onEnableTenant()
    {
        init();
    }

    public void onDisableTenant()
    {
        destroy();
    }

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException
    {
        lifecycle.setApplicationContext(applicationContext);
    }

    public void onApplicationEvent(ApplicationContextEvent event)
    {
        lifecycle.onApplicationEvent(event);
    }

    /**
     * Hooks into Spring Application Lifecycle.
     */
    private class ProcessorLifecycle extends AbstractLifecycleBean
    {
        @Override
        protected void onBootstrap(ApplicationEvent event)
        {
            init();
        }

        @Override
        protected void onShutdown(ApplicationEvent event)
        {
        }
    }

    // --------------------------------------------------------------
    // Alfresco methods
    // --------------------------------------------------------------

    /*
     * For the given cmis property name get the corresponding Alfresco property name.
     * 
     * Certain CMIS properties (e.g. cmis:creationDate and cmis:lastModifiedBy) don't
     * have direct mappings to Alfresco properties through the CMIS dictionary, because
     * these mappings should not be exposed outside the repository through CMIS. For these,
     * however, this method provides the mapping so that the sort works.
     * 
     */
    public Pair<QName, Boolean> getSortProperty(String cmisPropertyName, String direction)
    {
    	QName sortPropName = null;
    	Pair<QName, Boolean> sortProp = null;

        PropertyDefinitionWrapper propDef = getOpenCMISDictionaryService().findPropertyByQueryName(cmisPropertyName);
        if (propDef != null)
        {
	        if (propDef.getPropertyId().equals(CMISDictionaryModel.PROP_BASE_TYPE_ID))
	        {
	            // special-case (see also ALF-13968) - for getChildren, using "cmis:baseTypeId" allows sorting of folders first and vice-versa (cmis:folder <-> cmis:document)
	        	sortPropName = GetChildrenCannedQuery.SORT_QNAME_NODE_IS_FOLDER;
	        }
	        else
	        {
	        	sortPropName = propDef.getPropertyAccessor().getMappedProperty();
	        }

	        if (sortPropName == null)
	        {
	        	// ok to map these properties because we are always getting current versions of nodes
	        	sortPropName = SORT_PROPERTY_MAPPINGS.get(cmisPropertyName);
	        }
	    }

    	if (sortPropName != null)
    	{
    		boolean sortAsc = (direction == null ? true : direction.equalsIgnoreCase("asc"));
    		sortProp = new Pair<QName, Boolean>(sortPropName, sortAsc);
    	}
    	else
    	{
    		if (logger.isDebugEnabled())
    		{
    			logger.debug("Ignore sort property '" + cmisPropertyName + " - mapping not found");
    		}
    	}

        return sortProp;
    }

    /**
     * Asynchronously generates thumbnails for the given node.
     *  
     * @param nodeRef
     */
    public void createThumbnails(NodeRef nodeRef, Set<String> thumbnailNames)
    {
    	if(thumbnailNames == null || thumbnailNames.size() == 0)
    	{
    		return;
    	}

        ThumbnailRegistry registry = thumbnailService.getThumbnailRegistry();

        // If there's nothing currently registered to generate thumbnails for the
        //  specified mimetype, then log a message and bail out
        Serializable value = this.nodeService.getProperty(nodeRef, ContentModel.PROP_CONTENT);
        ContentData contentData = DefaultTypeConverter.INSTANCE.convert(ContentData.class, value);
        if (contentData == null)
        {
        	logger.info("Unable to create thumbnails as there is no content");
        	return;
        }

    	long size = contentData.getSize();
    	String mimeType = contentData.getMimetype();

    	for(String thumbnailName : thumbnailNames)
    	{
	        // Use the thumbnail registy to get the details of the thumbail
	        ThumbnailDefinition details = registry.getThumbnailDefinition(thumbnailName);
	        if(details == null)
	        {
	            // Throw exception 
	            logger.warn("The thumbnail name '" + thumbnailName + "' is not registered");
	            continue;
	        }
	        else
	        {
		        if(registry.isThumbnailDefinitionAvailable(contentData.getContentUrl(), mimeType, size, nodeRef, details))
		        {
		            org.alfresco.service.cmr.action.Action action = ThumbnailHelper.createCreateThumbnailAction(details, serviceRegistry);
		            
		            // Queue async creation of thumbnail
		            actionService.executeAction(action, nodeRef, true, true);
		        }
		        else
		        {
		        	logger.info("Unable to create thumbnail '" + details.getName() + "' for " +
		        			mimeType + " as no transformer is currently available");
		        }
	        }
    	}
    }

    /**
     * Extracts metadata for the node.
     *  
     * @param nodeRef
     */
    public void extractMetadata(NodeRef nodeRef)
    {
    	org.alfresco.service.cmr.action.Action action = actionService.createAction(ContentMetadataExtracter.EXECUTOR_NAME);
    	actionService.executeAction(action, nodeRef, true, false);
    }

    public SiteInfo getSite(NodeRef nodeRef)
    {
    	return siteService.getSite(nodeRef);
    }
    
    /**
     *  Should the node be filtered?
     */
	public boolean filter(NodeRef nodeRef)
    {
    	return objectFilter.filter(nodeRef);
    }

    public boolean disableBehaviour(QName className)
    {
        boolean wasEnabled = behaviourFilter.isEnabled(className);
        if(wasEnabled)
        {
        	behaviourFilter.disableBehaviour(className);
        }
        return wasEnabled;
    }
	
    public boolean enableBehaviour(QName className)
    {
        boolean isEnabled = behaviourFilter.isEnabled(className);
        if(!isEnabled)
        {
            behaviourFilter.enableBehaviour(className);
        }
        return isEnabled;
    }

    
    public boolean disableBehaviour(QName className, NodeRef nodeRef)
    {
        boolean wasEnabled = behaviourFilter.isEnabled(nodeRef, className);
        if(wasEnabled)
        {
            behaviourFilter.disableBehaviour(nodeRef, className);
        }
        return wasEnabled;
    }
    
    public boolean enableBehaviour(QName className, NodeRef nodeRef)
    {
        boolean isEnabled = behaviourFilter.isEnabled(nodeRef, className);
        if(!isEnabled)
        {
            behaviourFilter.enableBehaviour(nodeRef, className);
        }
        return isEnabled;
    }

    public StoreRef getRootStoreRef()
    {
        return getRootNodeRef().getStoreRef();
    }

    public void deleteNode(NodeRef nodeRef, boolean postActivity)
    {
        ActivityInfo activityInfo = null;

        // post activity after removal of the node
        postActivity &= !hiddenAspect.hasHiddenAspect(nodeRef);
    	if(postActivity)
    	{
        	// get this information before the node is deleted
	    	activityInfo = activityPoster.getActivityInfo(nodeRef);
    	}

        getNodeService().deleteNode(nodeRef);

        // post activity after removal of the node
        if(postActivity && activityInfo != null)
        {
        	activityPoster.postFileFolderDeleted(activityInfo);
        }
    }

    public RetryingTransactionHelper getRetryingTransactionHelper()
    {
    	return transactionService.getRetryingTransactionHelper();
    }

    /**
     * Returns the root folder node ref.
     */
    public NodeRef getRootNodeRef()
    {
        NodeRef rootNodeRef = (NodeRef)singletonCache.get(KEY_CMIS_ROOT_NODEREF);
        if (rootNodeRef == null)
        {
            rootNodeRef = AuthenticationUtil.runAs(new RunAsWork<NodeRef>()
            {
                public NodeRef doWork() throws Exception
                {
                    return transactionService.getRetryingTransactionHelper().doInTransaction(
                            new RetryingTransactionCallback<NodeRef>()
                            {
                                public NodeRef execute() throws Exception
                                {
                                    NodeRef root = nodeService.getRootNode(storeRef);
                                    List<NodeRef> rootNodes = searchService.selectNodes(root, rootPath, null,
                                            namespaceService, false);
                                    if (rootNodes.size() != 1)
                                    {
                                        throw new CmisRuntimeException("Unable to locate CMIS root path " + rootPath);
                                    }
                                    return rootNodes.get(0);
                                };
                            }, true);
                }
            }, AuthenticationUtil.getSystemUserName());

            if (rootNodeRef == null)
            {
                throw new CmisObjectNotFoundException("Root folder path '" + rootPath + "' not found!");
            }

            singletonCache.put(KEY_CMIS_ROOT_NODEREF, rootNodeRef);
        }

        return rootNodeRef;
    }

    public String getName(NodeRef nodeRef)
    {
        try
        {
            Object name = nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
            return (name instanceof String ? (String) name : null);
        }
        catch(InvalidNodeRefException inre)
        {
            return null;
        }
    }

    /**
     * Cuts of the version information from an object id.
     */
    public String getCurrentVersionId(String objectId)
    {
        if (objectId == null)
        {
            return null;
        }

        int sepIndex = objectId.lastIndexOf(ID_SEPERATOR);
        if (sepIndex > -1)
        {
            return objectId.substring(0, sepIndex);
        }

        return objectId;
    }

    /**
     * Creates an object info object.
     */
    public CMISNodeInfoImpl createNodeInfo(String objectId)
    {
        return new CMISNodeInfoImpl(this, objectId);
    }

    /**
     * Creates an object info object.
     */
    public CMISNodeInfoImpl createNodeInfo(NodeRef nodeRef)
    {
        return createNodeInfo(nodeRef, null);
    }

    /**
     * Creates an object info object.
     */
    public CMISNodeInfoImpl createNodeInfo(NodeRef nodeRef, VersionHistory versionHistory)
    {
        return new CMISNodeInfoImpl(this, nodeRef, versionHistory);
    }

    /**
     * Creates an object info object.
     */
    public CMISNodeInfoImpl createNodeInfo(AssociationRef assocRef)
    {
        return new CMISNodeInfoImpl(this, assocRef);
    }

    /*
     * Strip store ref from the id, if there is one.
     */
    private String getGuid(String id)
    {
    	int idx = id.lastIndexOf("/");
    	if(idx != -1)
    	{
    		return id.substring(idx + 1);
    	}
    	else
    	{
    		return id;
    	}
    }
    
    /*
     * Construct an object id based on the incoming assocRef and versionLabel. The object id will always
     * be the assocRef guid.
     */
    public String constructObjectId(AssociationRef assocRef, String versionLabel)
    {
    	StringBuilder sb = new StringBuilder(CMISConnector.ASSOC_ID_PREFIX);
    	if(isPublicApi())
    	{
    		// always return the guid
    		sb.append(assocRef.getId());
    	}
    	else
    	{
    		sb.append(assocRef.toString());
    	}

    	if(versionLabel != null)
    	{
    		sb.append(CMISConnector.ID_SEPERATOR);
    		sb.append(versionLabel);
    	}
    	return sb.toString();
    }
    
    /*
     * Construct an object id based on the incoming incomingObjectId. The object id will always
     * be the node guid.
     */
    public String constructObjectId(String incomingObjectId)
    {
    	return constructObjectId(incomingObjectId, null);
    }

    /*
     * Construct an object id based on the incoming incomingNodeId and versionLabel. The object id will always
     * be the node guid.
     */
    public String constructObjectId(String incomingNodeId, String versionLabel)
    {
    	StringBuilder sb = new StringBuilder();
    	if(isPublicApi())
    	{
    		// always return the guid
    		sb.append(getGuid(incomingNodeId));
    	}
    	else
    	{
    	    sb.append(incomingNodeId);
    	}
    	if(versionLabel != null)
    	{
    		sb.append(CMISConnector.ID_SEPERATOR);
    		sb.append(versionLabel);
    	}
    	return sb.toString();
    }

    public void createVersion(NodeRef nodeRef, VersionType versionType, String reason)
    {
    	if(versionService.getVersionHistory(nodeRef) == null)
    	{
    		// no version history. Make sure we have an initial major version 1.0.
            Map<String, Serializable> versionProperties = new HashMap<String, Serializable>(2);
            versionProperties.put(VersionModel.PROP_VERSION_TYPE, VersionType.MAJOR);
            versionProperties.put(VersionModel.PROP_DESCRIPTION, "Initial version");
            versionService.createVersion(nodeRef, versionProperties);
    	}

        Map<String, Serializable> versionProperties = new HashMap<String, Serializable>(2);
        versionProperties.put(VersionModel.PROP_VERSION_TYPE, versionType);
        versionProperties.put(VersionModel.PROP_DESCRIPTION, reason);
        versionService.createVersion(nodeRef, versionProperties);
    }

    public CmisVersion getRequestCmisVersion()
    {
        CallContext callContext = AlfrescoCmisServiceCall.get();
        CmisVersion cmisVersion = (callContext != null ? callContext.getCmisVersion() : CmisVersion.CMIS_1_0);
        return cmisVersion;
    }

    private boolean isPublicApi()
    {
    	boolean isPublicApi = false;
    	CallContext callContext = AlfrescoCmisServiceCall.get();
    	if(callContext != null)
    	{
	    	String value = (String)callContext.get("isPublicApi");
	    	isPublicApi = (value == null ? false : Boolean.parseBoolean(value));
    	}
    	return isPublicApi;
    }

    /*
     * Construct an object id based on the incoming incomingNodeRef and versionLabel. The object id will always
     * be the incomingNodeRef guid.
     */
    public String constructObjectId(NodeRef incomingNodeRef, String versionLabel)
    {
    	StringBuilder sb = new StringBuilder();
    	sb.append(isPublicApi() ? incomingNodeRef.getId() : incomingNodeRef.toString());
    	if(versionLabel != null)
    	{
    		sb.append(CMISConnector.ID_SEPERATOR);
    		sb.append(versionLabel);
    	}
    	return sb.toString();
    }

    /**
     * Compiles a CMIS object if for a live node.
     */
    public String createObjectId(NodeRef currentVersionNodeRef)
    {
    	QName typeQName = nodeService.getType(currentVersionNodeRef);
        TypeDefinitionWrapper type = getOpenCMISDictionaryService().findNodeType(typeQName);
    
    	if(type instanceof ItemTypeDefinitionWrapper)
    	{
    		return constructObjectId(currentVersionNodeRef, null);
    	}
    	
        if(type instanceof FolderTypeDefintionWrapper)
        {
            return constructObjectId(currentVersionNodeRef, null);
        }
        
        Serializable versionLabel = getNodeService()
                .getProperty(currentVersionNodeRef, ContentModel.PROP_VERSION_LABEL);
        if (versionLabel == null)
        {
        	versionLabel = CMISConnector.UNVERSIONED_VERSION_LABEL;
        }

        return constructObjectId(currentVersionNodeRef, (String)versionLabel);
    }

    /**
     * Returns the type definition of a node or <code>null</code> if no type
     * definition could be found.
     */
    public TypeDefinitionWrapper getType(NodeRef nodeRef)
    {
        try
        {
            QName typeQName = nodeService.getType(nodeRef);
            return getType(typeQName);
        }
        catch(InvalidNodeRefException inre)
        {
            return null;
        }
    }

    private TypeDefinitionWrapper getType(QName typeQName)
    {
        return getOpenCMISDictionaryService().findNodeType(typeQName);
    }

    /**
     * Returns the type definition of an association or <code>null</code> if no
     * type definition could be found.
     */
    public TypeDefinitionWrapper getType(AssociationRef assocRef)
    {
        QName typeQName = assocRef.getTypeQName();
        return getOpenCMISDictionaryService().findAssocType(typeQName);
    }

    /**
     * Returns the type definition of a node or <code>null</code> if no type
     * definition could be found.
     */
    public TypeDefinitionWrapper getType(String cmisTypeId)
    {
        return getOpenCMISDictionaryService().findType(cmisTypeId);
    }

    /**
     * Returns the definition after it has checked if the type can be used for
     * object creation.
     */
    public TypeDefinitionWrapper getTypeForCreate(String cmisTypeId, BaseTypeId baseTypeId)
    {
        TypeDefinitionWrapper type = getType(cmisTypeId);
        if ((type == null) || (type.getBaseTypeId() != baseTypeId))
        {
            switch (baseTypeId)
            {
            case CMIS_DOCUMENT:
                throw new CmisConstraintException("Type is not a document type!");
            case CMIS_FOLDER:
                throw new CmisConstraintException("Type is not a folder type!");
            case CMIS_RELATIONSHIP:
                throw new CmisConstraintException("Type is not a relationship type!");
            case CMIS_POLICY:
                throw new CmisConstraintException("Type is not a policy type!");
            case CMIS_ITEM:
                throw new CmisConstraintException("Type is not an item type!");    
            }
        }

        if (!type.getTypeDefinition(false).isCreatable())
        {
            throw new CmisConstraintException("Type is not creatable!");
        }

        return type;
    }

    /**
     * Applies a versioning state to a document.
     */
    public void applyVersioningState(NodeRef nodeRef, VersioningState versioningState)
    {
        if (versioningState == VersioningState.CHECKEDOUT)
        {
            if (!nodeService.hasAspect(nodeRef, ContentModel.ASPECT_VERSIONABLE))
            {
                Map<QName, Serializable> props = new HashMap<QName, Serializable>();
                props.put(ContentModel.PROP_INITIAL_VERSION, false);
                props.put(ContentModel.PROP_AUTO_VERSION, false);
                nodeService.addAspect(nodeRef, ContentModel.ASPECT_VERSIONABLE, props);
            }

            Map<String, Serializable> versionProperties = new HashMap<String, Serializable>(5);
            versionProperties.put(VersionModel.PROP_VERSION_TYPE, VersionType.MAJOR);
            versionProperties.put(VersionModel.PROP_DESCRIPTION, "Initial Version");

            getCheckOutCheckInService().checkout(nodeRef);
        }
        else if ((versioningState == VersioningState.MAJOR) || (versioningState == VersioningState.MINOR))
        {
            if (!nodeService.hasAspect(nodeRef, ContentModel.ASPECT_VERSIONABLE))
            {
                Map<QName, Serializable> props = new HashMap<QName, Serializable>();
                props.put(ContentModel.PROP_INITIAL_VERSION, false);
                props.put(ContentModel.PROP_AUTO_VERSION, false);
                nodeService.addAspect(nodeRef, ContentModel.ASPECT_VERSIONABLE, props);
            }

            Map<String, Serializable> versionProperties = new HashMap<String, Serializable>(5);
            versionProperties.put(
                    VersionModel.PROP_VERSION_TYPE,
                    versioningState == VersioningState.MAJOR ? VersionType.MAJOR : VersionType.MINOR);
            versionProperties.put(VersionModel.PROP_DESCRIPTION, "Initial Version");

            versionService.createVersion(nodeRef, versionProperties);
        }
    }

    /**
     * Checks if a child of a given type can be added to a given folder.
     */
    @SuppressWarnings("unchecked")
    public void checkChildObjectType(CMISNodeInfo folderInfo, String childType)
    {
        TypeDefinitionWrapper targetType = folderInfo.getType();
        PropertyDefinitionWrapper allowableChildObjectTypeProperty = targetType
                .getPropertyById(PropertyIds.ALLOWED_CHILD_OBJECT_TYPE_IDS);
        List<String> childTypes = (List<String>) allowableChildObjectTypeProperty.getPropertyAccessor().getValue(
                folderInfo);

        if ((childTypes == null) || childTypes.isEmpty())
        {
            return;
        }
        
        if (!childTypes.contains(childType))
        {
            throw new CmisConstraintException("Objects of type '" + childType + "' cannot be added to this folder!");
        }
    }

    /**
     * Creates the CMIS object for a node.
     */
    public ObjectData createCMISObject(CMISNodeInfo info, FileInfo node, String filter,
            boolean includeAllowableActions, IncludeRelationships includeRelationships, String renditionFilter,
            boolean includePolicyIds, boolean includeAcl)
    {
        if (info.getType() == null)
        {
            throw new CmisObjectNotFoundException("No corresponding type found! Not a CMIS object?");
        }

        Properties nodeProps = getNodeProperties(info, node, filter, info.getType());

        return createCMISObjectImpl(info, nodeProps, filter, includeAllowableActions, includeRelationships,
                renditionFilter, includePolicyIds, includeAcl);
    }

    public ObjectData createCMISObject(CMISNodeInfo info, String filter, boolean includeAllowableActions,
            IncludeRelationships includeRelationships, String renditionFilter, boolean includePolicyIds,
            boolean includeAcl)
    {
        if (info.getType() == null)
        {
            throw new CmisObjectNotFoundException("No corresponding type found! Not a CMIS object?");
        }

        Properties nodeProps = (info.isRelationship() ? getAssocProperties(info, filter) : getNodeProperties(info,
                filter));

        return createCMISObjectImpl(info, nodeProps, filter, includeAllowableActions, includeRelationships,
                renditionFilter, includePolicyIds, includeAcl);
    }

    @SuppressWarnings("unchecked")
    private ObjectData createCMISObjectImpl(final CMISNodeInfo info, Properties nodeProps, String filter,
            boolean includeAllowableActions, IncludeRelationships includeRelationships, String renditionFilter,
            boolean includePolicyIds, boolean includeAcl)
    {
        final ObjectDataImpl result = new ObjectDataImpl();

        // set allowable actions
        if (includeAllowableActions)
        {
            result.setAllowableActions(getAllowableActions(info));
        }

        // set policy ids
        if (includePolicyIds)
        {
            result.setPolicyIds(new PolicyIdListImpl());
        }

        if (info.isRelationship())
        {
            // set properties
            result.setProperties(getAssocProperties(info, filter));

            // set ACL
            if (includeAcl)
            {
                // association have no ACL - return an empty list of ACEs
                result.setAcl(new AccessControlListImpl((List<Ace>) Collections.EMPTY_LIST));
            }
        }
        else
        {
            // set properties
            result.setProperties(nodeProps);

            // set relationships
            if (includeRelationships != IncludeRelationships.NONE)
            {
                result.setRelationships(getRelationships(info.getNodeRef(), includeRelationships));
            }

            // set renditions
            if (!RENDITION_NONE.equals(renditionFilter))
            {
                List<RenditionData> renditions = getRenditions(info.getNodeRef(), renditionFilter, null, null);
                if ((renditions != null) && (!renditions.isEmpty()))
                {
                    result.setRenditions(renditions);
                }
                else
                {
                	result.setRenditions(Collections.EMPTY_LIST);
                }
            }

            // set ACL
            if (includeAcl)
            {
            	AuthenticationUtil.runAsSystem(new RunAsWork<Void>()
            	{
					@Override
					public Void doWork() throws Exception
					{
		                result.setAcl(getACL(info.getCurrentNodeNodeRef(), false));
						return null;
					}
            	});
            }

            CmisVersion cmisVersion = getRequestCmisVersion();
            if(cmisVersion.equals(CmisVersion.CMIS_1_0))
            {
	            // add aspects (cmis 1.0)
	            List<CmisExtensionElement> extensions = getAspectExtensions(info, filter, result.getProperties()
	                    .getProperties().keySet());
	            if (!extensions.isEmpty())
	            {
	                result.getProperties().setExtensions(
	                        Collections.singletonList((CmisExtensionElement) new CmisExtensionElementImpl(
	                                ALFRESCO_EXTENSION_NAMESPACE, ASPECTS, null, extensions)));
	            }
            }
        }
        return result;
    }

    public String getPath(NodeRef nodeRef)
    {
        try
        {
            Path path = nodeService.getPath(nodeRef);

            // skip to CMIS root path
            NodeRef rootNode = getRootNodeRef();
            int i = 0;
            while (i < path.size())
            {
                Path.Element element = path.get(i);
                if (element instanceof ChildAssocElement)
                {
                    ChildAssociationRef assocRef = ((ChildAssocElement) element).getRef();
                    NodeRef node = assocRef.getChildRef();
                    if (node.equals(rootNode))
                    {
                        break;
                    }
                }
                i++;
            }

            StringBuilder displayPath = new StringBuilder(64);

            if (path.size() - i == 1)
            {
                // render root path
                displayPath.append("/");
            }
            else
            {
                // render CMIS scoped path
                i++;
                while (i < path.size())
                {
                    Path.Element element = path.get(i);
                    if (element instanceof ChildAssocElement)
                    {
                        ChildAssociationRef assocRef = ((ChildAssocElement) element).getRef();
                        NodeRef node = assocRef.getChildRef();
                        displayPath.append("/");
                        displayPath.append(nodeService.getProperty(node, ContentModel.PROP_NAME));
                    }
                    i++;
                }
            }

            return displayPath.toString();
        }
        catch(InvalidNodeRefException inre)
        {
            return null;
        }
    }

    /**
     * Gets the content from the repository.
     */
    public ContentStream getContentStream(CMISNodeInfo info, String streamId, BigInteger offset, BigInteger length)
    {
        // get the type and check if the object can have content
        TypeDefinitionWrapper type = info.getType();
        checkDocumentTypeForContent(type);

        // looks like a document, now get the content
        ContentStreamImpl result = new ContentStreamImpl();
        result.setFileName(info.getName());

        // if streamId is set, fetch other content
        NodeRef streamNodeRef = info.getNodeRef();
        if ((streamId != null) && (streamId.length() > 0))
        {
            CMISNodeInfo streamInfo = createNodeInfo(streamId);
            if (!streamInfo.isVariant(CMISObjectVariant.CURRENT_VERSION))
            {
                throw new CmisInvalidArgumentException("Stream id is invalid: " + streamId + ", expected variant " + CMISObjectVariant.CURRENT_VERSION + ", got variant " + streamInfo.getObjectVariant());
            }

            streamNodeRef = streamInfo.getNodeRef();
            type = streamInfo.getType();
            checkDocumentTypeForContent(type);
        }

        // get the stream now
        try
        {
            ContentReader contentReader = contentService.getReader(streamNodeRef, ContentModel.PROP_CONTENT);
            if (contentReader == null)
            {
                throw new CmisConstraintException("Document has no content!");
            }

            result.setMimeType(contentReader.getMimetype());
            long contentSize = contentReader.getSize();
            
            if ((offset == null) && (length == null))
            {
                result.setStream(contentReader.getContentInputStream());
                result.setLength(BigInteger.valueOf(contentSize));
                publishReadEvent(streamNodeRef, result.getMimeType(), contentSize, contentReader.getEncoding(), null);
            }
            else
            {
                long off = (offset == null ? 0 : offset.longValue());
                long len = (length == null ? contentSize : length.longValue());
                if (off + len > contentSize)
                {
                    len = contentReader.getSize() - off;
                }

                result.setStream(new RangeInputStream(contentReader.getContentInputStream(), off, len));
                result.setLength(BigInteger.valueOf(len));
                publishReadEvent(streamNodeRef, result.getMimeType(), contentSize, contentReader.getEncoding(), off+" - "+len);
            }
        }
        catch (Exception e)
        {
            if (e instanceof CmisBaseException)
            {
                throw (CmisBaseException) e;
            }
            else
            {
                StringBuilder msg = new StringBuilder("Failed to retrieve content: " + e.getMessage());
                Throwable cause = e.getCause();
                if(cause != null)
                {
                    // add the cause to the CMIS exception
                    msg.append(", ");
                    msg.append(cause.getMessage());
                }
                throw new CmisRuntimeException(msg.toString(), e);
            }
        }

        return result;
    }

    /**
     * Notifies listeners that a read has taken place.
     * 
     * @param streamNodeRef
     * @param type
     * @param mimeType
     * @param contentSize
     * @param encoding
     * @param string
     */
    protected void publishReadEvent(final NodeRef nodeRef, final String mimeType, final long contentSize, final String encoding, final String range)
    {
        final QName nodeType = nodeRef==null?null:nodeService.getType(nodeRef);
        
        eventPublisher.publishEvent(new EventPreparator(){
            @Override
            public Event prepareEvent(String user, String networkId, String transactionId)
            {
                if (StringUtils.hasText(range))
                {
                    return new ContentReadRangeEvent(user, networkId, transactionId,
                                nodeRef.getId(), null, nodeType.toString(), Client.cmis, mimeType, contentSize, encoding, range); 
                } 
                else 
                {
                    return new ContentEventImpl(ContentEvent.DOWNLOAD, user, networkId, transactionId,
                                nodeRef.getId(), null, nodeType.toString(), Client.cmis, mimeType, contentSize, encoding);            
                }
            }
        });
        

    }

    public void appendContent(CMISNodeInfo nodeInfo, ContentStream contentStream, boolean isLastChunk) throws IOException
    {
    	NodeRef nodeRef = nodeInfo.getNodeRef();

    	this.disableBehaviour(ContentModel.ASPECT_VERSIONABLE, nodeRef);
		
    	if(!nodeService.hasAspect(nodeRef, ContentModel.ASPECT_CMIS_UPDATE_CONTEXT))
    	{
    		Map<QName, Serializable> props = new HashMap<QName, Serializable>();
    		props.put(ContentModel.PROP_GOT_FIRST_CHUNK, true);
    		nodeService.addAspect(nodeRef, ContentModel.ASPECT_CMIS_UPDATE_CONTEXT, props);
    	}

		ContentReader reader = contentService.getReader(nodeRef, ContentModel.PROP_CONTENT);
		InputStream existingContentInput = (reader != null ? reader.getContentInputStream() : null);
		InputStream input = contentStream.getStream();

    	ContentWriter writer = contentService.getWriter(nodeRef, ContentModel.PROP_CONTENT, true);
    	OutputStream out = new BufferedOutputStream(writer.getContentOutputStream());
    	
    	InputStream in = null;
    	if(existingContentInput != null)
    	{
    		in = new SequenceInputStream(existingContentInput, input);
    	}
    	else
    	{
    		in = input;
    	}

		try
		{
	    	IOUtils.copy(in, out);

	    	if(isLastChunk)
	    	{
    			nodeService.removeAspect(nodeRef, ContentModel.ASPECT_CMIS_UPDATE_CONTEXT);
	    		getActivityPoster().postFileFolderUpdated(nodeInfo.isFolder(), nodeRef);

	    		createVersion(nodeRef, VersionType.MINOR, "Appended content stream");
	    	}
		}
		finally
		{
			if(in != null)
			{
				in.close();
			}
			if(out != null)
			{
    	    	out.close();	
			}
			
			this.enableBehaviour(ContentModel.ASPECT_VERSIONABLE, nodeRef);
		}
    }

    private void checkDocumentTypeForContent(TypeDefinitionWrapper type)
    {
        if (type == null)
        {
            throw new CmisObjectNotFoundException("No corresponding type found! Not a CMIS object?");
        }
        if (!(type instanceof DocumentTypeDefinitionWrapper))
        {
            throw new CmisStreamNotSupportedException("Object is not a document!");
        }
        if (((DocumentTypeDefinition) type.getTypeDefinition(false)).getContentStreamAllowed() == ContentStreamAllowed.NOTALLOWED)
        {
            throw new CmisConstraintException("Document cannot have content!");
        }
    }

    private void addAspectProperties(CMISNodeInfo info, String filter, PropertiesImpl result)
    {
        if(getRequestCmisVersion().equals(CmisVersion.CMIS_1_1))
        {
            Set<String> propertyIds = new HashSet<String>();
            Set<String> filterSet = splitFilter(filter);
    
            Set<QName> aspects = nodeService.getAspects(info.getNodeRef());
            for (QName aspect : aspects)
            {
                TypeDefinitionWrapper aspectType = getOpenCMISDictionaryService().findNodeType(aspect);
                if (aspectType == null)
                {
                    continue;
                }
    
                for (PropertyDefinitionWrapper propDef : aspectType.getProperties())
                {
                    if (propertyIds.contains(propDef.getPropertyId()))
                    {
                        // skip properties that have already been added
                        continue;
                    }
    
                    if ((filterSet != null) && (!filterSet.contains(propDef.getPropertyDefinition().getQueryName())))
                    {
                        // skip properties that are not in the filter
                        continue;
                    }
    
                    Serializable value = propDef.getPropertyAccessor().getValue(info);
                    result.addProperty(getProperty(propDef.getPropertyDefinition().getPropertyType(), propDef, value));
    
                    // mark property as 'added'
                    propertyIds.add(propDef.getPropertyId());
                }
            }
        }
    }

    public Properties getNodeProperties(CMISNodeInfo info, String filter)
    {
        PropertiesImpl result = new PropertiesImpl();

        Set<String> filterSet = splitFilter(filter);

        for (PropertyDefinitionWrapper propDef : info.getType().getProperties())
        {
            if (!propDef.getPropertyId().equals(PropertyIds.OBJECT_ID))
            {
                // don't filter the object id
                if ((filterSet != null) && (!filterSet.contains(propDef.getPropertyDefinition().getQueryName())))
                {
                    // skip properties that are not in the filter
                    continue;
                }
            }

            Serializable value = propDef.getPropertyAccessor().getValue(info);
            result.addProperty(getProperty(propDef.getPropertyDefinition().getPropertyType(), propDef, value));
        }

        addAspectProperties(info, filter, result);

        return result;
    }

    public Properties getNodeProperties(CMISNodeInfo info, FileInfo node, String filter, TypeDefinitionWrapper type)
    {
        PropertiesImpl result = new PropertiesImpl();

        Set<String> filterSet = splitFilter(filter);

        Map<QName, Serializable> nodeProps = node.getProperties();

        for (PropertyDefinitionWrapper propDef : type.getProperties())
        {
            if (!propDef.getPropertyId().equals(PropertyIds.OBJECT_ID))
            {
                // don't filter the object id
                if ((filterSet != null) && (!filterSet.contains(propDef.getPropertyDefinition().getQueryName())))
                {
                    // skip properties that are not in the filter
                    continue;
                }
            }

            Serializable value = null;

            CMISPropertyAccessor accessor = propDef.getPropertyAccessor();
            if (accessor instanceof DirectProperty)
            {
                value = nodeProps.get(accessor.getMappedProperty());
            }
            else
            {
                value = propDef.getPropertyAccessor().getValue(info);
            }

            result.addProperty(getProperty(propDef.getPropertyDefinition().getPropertyType(), propDef, value));
        }

        addAspectProperties(info, filter, result);

        return result;
    }

    public Properties getAssocProperties(CMISNodeInfo info, String filter)
    {
        PropertiesImpl result = new PropertiesImpl();

        Set<String> filterSet = splitFilter(filter);

        for (PropertyDefinitionWrapper propDefWrap : info.getType().getProperties())
        {
            PropertyDefinition<?> propDef = propDefWrap.getPropertyDefinition();
            if ((filterSet != null) && (!filterSet.contains(propDef.getQueryName())))
            {
                // skip properties that are not in the filter
                continue;
            }

            CMISPropertyAccessor cmisPropertyAccessor = propDefWrap.getPropertyAccessor();
            Serializable value = cmisPropertyAccessor.getValue(info);
            PropertyType propType = propDef.getPropertyType();
            PropertyData<?> propertyData = getProperty(propType, propDefWrap, value);
            result.addProperty(propertyData);
        }

        return result;
    }

    /**
     * Builds aspect extension.
     */
    public List<CmisExtensionElement> getAspectExtensions(CMISNodeInfo info, String filter,
            Set<String> alreadySetProperties)
    {
        List<CmisExtensionElement> extensions = new ArrayList<CmisExtensionElement>();
        Set<String> propertyIds = new HashSet<String>(alreadySetProperties);
        List<CmisExtensionElement> propertyExtensionList = new ArrayList<CmisExtensionElement>();
        Set<String> filterSet = splitFilter(filter);

        Set<QName> aspects = nodeService.getAspects(info.getNodeRef());
        for (QName aspect : aspects)
        {
            TypeDefinitionWrapper aspectType = getOpenCMISDictionaryService().findNodeType(aspect);
            if (aspectType == null)
            {
                continue;
            }

            extensions.add(new CmisExtensionElementImpl(ALFRESCO_EXTENSION_NAMESPACE, APPLIED_ASPECTS, null, aspectType
                    .getTypeId()));

            for (PropertyDefinitionWrapper propDef : aspectType.getProperties())
            {
                if (propertyIds.contains(propDef.getPropertyId()))
                {
                    // skip properties that have already been added
                    continue;
                }

                if ((filterSet != null) && (!filterSet.contains(propDef.getPropertyDefinition().getQueryName())))
                {
                    // skip properties that are not in the filter
                    continue;
                }

                Serializable value = propDef.getPropertyAccessor().getValue(info);
                propertyExtensionList.add(createAspectPropertyExtension(propDef.getPropertyDefinition(), value));

                // mark property as 'added'
                propertyIds.add(propDef.getPropertyId());
            }
        }

        if (!propertyExtensionList.isEmpty())
        {
            CmisExtensionElementImpl propertiesExtension = new CmisExtensionElementImpl(
                    ALFRESCO_EXTENSION_NAMESPACE, "properties", null, propertyExtensionList);
            extensions.addAll(Collections.singletonList(propertiesExtension));
        }

        return extensions;
    }

    /**
     * Creates a property extension element.
     */
    @SuppressWarnings("rawtypes")
    private CmisExtensionElement createAspectPropertyExtension(PropertyDefinition<?> propertyDefinition, Object value)
    {
        String name;
        switch (propertyDefinition.getPropertyType())
        {
        case BOOLEAN:
            name = "propertyBoolean";
            break;
        case DATETIME:
            name = "propertyDateTime";
            break;
        case DECIMAL:
            name = "propertyDecimal";
            break;
        case INTEGER:
            name = "propertyInteger";
            break;
        case ID:
            name = "propertyId";
            break;
        default:
            name = "propertyString";
        }

        Map<String, String> attributes = new HashMap<String, String>();
        attributes.put("propertyDefinitionId", propertyDefinition.getId());
        attributes.put("queryName", propertyDefinition.getQueryName());
        // optional value
        if (propertyDefinition.getDisplayName() !=null && propertyDefinition.getDisplayName().trim().length() > 0)
        {
            attributes.put("displayName", propertyDefinition.getDisplayName());
        }
        // optional value
        if (propertyDefinition.getLocalName() !=null && propertyDefinition.getLocalName().trim().length() > 0)
        {
            attributes.put("localName", propertyDefinition.getLocalName());
        }
        

        List<CmisExtensionElement> propertyValues = new ArrayList<CmisExtensionElement>();
        if (value != null)
        {
            if (value instanceof List)
            {
                for (Object o : ((List) value))
                {
                	if(o != null)
                	{
                		propertyValues.add(new CmisExtensionElementImpl(CMIS_NAMESPACE, "value", null,
                				convertAspectPropertyValue(o)));
                	}
                	else
                	{
                		logger.warn("Unexpected null entry in list value for property " + propertyDefinition.getDisplayName()
                				+ ", value = " + value);
                	}
                }
            }
            else
            {
                propertyValues.add(new CmisExtensionElementImpl(CMIS_NAMESPACE, "value", null,
                        convertAspectPropertyValue(value)));
            }
        }

        return new CmisExtensionElementImpl(CMIS_NAMESPACE, name, attributes, propertyValues);
    }

    private String convertAspectPropertyValue(Object value)
    {
        if (value instanceof Date)
        {
            GregorianCalendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
            cal.setTime((Date) value);
            value = cal;
        }

        if (value instanceof GregorianCalendar)
        {
            DatatypeFactory df;
            try
            {
                df = DatatypeFactory.newInstance();
            }
            catch (DatatypeConfigurationException e)
            {
                throw new IllegalArgumentException("Aspect conversation exception: " + e.getMessage(), e);
            }
            return df.newXMLGregorianCalendar((GregorianCalendar) value).toXMLFormat();
        }

        return value.toString();
    }

    @SuppressWarnings("unchecked")
    private AbstractPropertyData<?> getProperty(PropertyType propType, PropertyDefinitionWrapper propDef,
            Serializable value)
    {
        AbstractPropertyData<?> result = null;
        switch (propType)
        {
        case BOOLEAN:
            result = new PropertyBooleanImpl();
            if (value instanceof List)
            {
                ((PropertyBooleanImpl) result).setValues((List<Boolean>) value);
            }
            else
            {
                ((PropertyBooleanImpl) result).setValue((Boolean) value);
            }
            break;
        case DATETIME:
            result = new PropertyDateTimeImpl();
            if (value instanceof List)
            {
                ((PropertyDateTimeImpl) result).setValues((List<GregorianCalendar>) DefaultTypeConverter.INSTANCE
                        .convert(GregorianCalendar.class, (List<?>) value));
            }
            else
            {
                ((PropertyDateTimeImpl) result).setValue(DefaultTypeConverter.INSTANCE.convert(GregorianCalendar.class,
                        value));
            }
            break;
        case DECIMAL:
            result = new PropertyDecimalImpl();
            if (value instanceof List)
            {
                ((PropertyDecimalImpl) result).setValues((List<BigDecimal>) DefaultTypeConverter.INSTANCE.convert(
                        BigDecimal.class, (List<?>) value));
            }
            else
            {
                ((PropertyDecimalImpl) result).setValue(DefaultTypeConverter.INSTANCE.convert(BigDecimal.class, value));
            }
            break;
        case HTML:
            result = new PropertyHtmlImpl();
            if (value instanceof List)
            {
                ((PropertyHtmlImpl) result).setValues((List<String>) value);
            }
            else
            {
                ((PropertyHtmlImpl) result).setValue((String) value);
            }
            break;
        case ID:
            result = new PropertyIdImpl();
            if (value instanceof List)
            {
                ((PropertyIdImpl) result).setValues((List<String>) value);
            }
            else
            {
                if (value instanceof NodeRef)
                {
                    ((PropertyIdImpl) result).setValue(constructObjectId((NodeRef)value, null));
                }
                else
                {
                    ((PropertyIdImpl) result).setValue((String) value);
                }
            }
            break;
        case INTEGER:
            result = new PropertyIntegerImpl();
            if (value instanceof List)
            {
                ((PropertyIntegerImpl) result).setValues((List<BigInteger>) DefaultTypeConverter.INSTANCE.convert(
                        BigInteger.class, (List<?>) value));
            }
            else
            {
                ((PropertyIntegerImpl) result).setValue(DefaultTypeConverter.INSTANCE.convert(BigInteger.class, value));
            }
            break;
        case STRING:
            result = new PropertyStringImpl();
            if (value instanceof List)
            {
                ((PropertyStringImpl) result).setValues((List<String>) value);
            }
            else
            {
                ((PropertyStringImpl) result).setValue((String) value);
            }
            break;
        case URI:
            result = new PropertyUriImpl();
            if (value instanceof List)
            {
                ((PropertyUriImpl) result).setValues((List<String>) value);
            }
            else
            {
                ((PropertyUriImpl) result).setValue((String) value);
            }
            break;
        default:
            throw new RuntimeException("Unknown datatype! Spec change?");
        }

        if (propDef != null)
        {
            result.setId(propDef.getPropertyDefinition().getId());
            result.setQueryName(propDef.getPropertyDefinition().getQueryName());
            result.setDisplayName(propDef.getPropertyDefinition().getDisplayName());
            result.setLocalName(propDef.getPropertyDefinition().getLocalName());
        }

        return result;
    }

    private Set<String> splitFilter(String filter)
    {
        if (filter == null)
        {
            return null;
        }

        if (filter.trim().length() == 0)
        {
            return null;
        }

        Set<String> result = new HashSet<String>();
        for (String s : filter.split(","))
        {
            s = s.trim();
            if (s.equals("*"))
            {
                return null;
            }
            else if (s.length() > 0)
            {
                result.add(s);
            }
        }

        // set a few base properties
        result.add(QUERY_NAME_OBJECT_ID);
        result.add(QUERY_NAME_OBJECT_TYPE_ID);
        result.add(QUERY_NAME_BASE_TYPE_ID);

        return result;
    }

    public AllowableActions getAllowableActions(CMISNodeInfo info)
    {
        AllowableActionsImpl result = new AllowableActionsImpl();
        Set<Action> allowableActions = new HashSet<Action>();
        result.setAllowableActions(allowableActions);

        for (CMISActionEvaluator evaluator : info.getType().getActionEvaluators().values())
        {
            if (evaluator.isAllowed(info))
            {
                allowableActions.add(evaluator.getAction());
            }
        }

        return result;
    }

    public List<ObjectData> getRelationships(NodeRef nodeRef, IncludeRelationships includeRelationships/*, CmisVersion cmisVersion*/)
    {
        List<ObjectData> result = new ArrayList<ObjectData>();

        if (nodeRef.getStoreRef().getProtocol().equals(VersionBaseModel.STORE_PROTOCOL))
        {
            // relationships from and to versions are not preserved
            return result;
        }

        // get relationships
        List<AssociationRef> assocs = new ArrayList<AssociationRef>();
        if (includeRelationships == IncludeRelationships.SOURCE || includeRelationships == IncludeRelationships.BOTH)
        {
            assocs.addAll(nodeService.getTargetAssocs(nodeRef, RegexQNamePattern.MATCH_ALL));
        }
        if (includeRelationships == IncludeRelationships.TARGET || includeRelationships == IncludeRelationships.BOTH)
        {
            assocs.addAll(nodeService.getSourceAssocs(nodeRef, RegexQNamePattern.MATCH_ALL));
        }

        // filter relationships that not map the CMIS domain model
        for (AssociationRef assocRef : assocs)
        {
            TypeDefinitionWrapper assocTypeDef = getOpenCMISDictionaryService().findAssocType(assocRef.getTypeQName());
            if (assocTypeDef == null || getType(assocRef.getSourceRef()) == null
                    || getType(assocRef.getTargetRef()) == null)
            {
                continue;
            }

            try
            {
	            result.add(createCMISObject(createNodeInfo(assocRef), null, false, IncludeRelationships.NONE,
	                    RENDITION_NONE, false, false/*, cmisVersion*/));
            }
            catch(AccessDeniedException e)
            {
            	// PUBLICAPI-110
            	// ok, just skip it
            }
            catch(CmisObjectNotFoundException e)
            {
                // ignore objects that have not been found (perhaps because their type is unknown to CMIS)
            }
            // TODO: Somewhere this has not been wrapped correctly
            catch (net.sf.acegisecurity.AccessDeniedException e)
            {
                // skip
            }
        }

        return result;
    }

    public ObjectList getObjectRelationships(NodeRef nodeRef, RelationshipDirection relationshipDirection,
            String typeId, String filter, Boolean includeAllowableActions, BigInteger maxItems, BigInteger skipCount)
    {
        ObjectListImpl result = new ObjectListImpl();
        result.setHasMoreItems(false);
        result.setNumItems(BigInteger.ZERO);
        result.setObjects(new ArrayList<ObjectData>());

        if (nodeRef.getStoreRef().getProtocol().equals(VersionBaseModel.STORE_PROTOCOL))
        {
            // relationships from and to versions are not preserved
            return result;
        }

        // get relationships
        List<AssociationRef> assocs = new ArrayList<AssociationRef>();
        if (relationshipDirection == RelationshipDirection.SOURCE
                || relationshipDirection == RelationshipDirection.EITHER)
        {
            assocs.addAll(nodeService.getTargetAssocs(nodeRef, RegexQNamePattern.MATCH_ALL));
        }
        if (relationshipDirection == RelationshipDirection.TARGET
                || relationshipDirection == RelationshipDirection.EITHER)
        {
            assocs.addAll(nodeService.getSourceAssocs(nodeRef, RegexQNamePattern.MATCH_ALL));
        }

        int skip = (skipCount == null ? 0 : skipCount.intValue());
        int max = (maxItems == null ? Integer.MAX_VALUE : maxItems.intValue());
        int counter = 0;
        boolean hasMore = false;

        if (max > 0)
        {
            // filter relationships that not map the CMIS domain model
            for (AssociationRef assocRef : assocs)
            {
                TypeDefinitionWrapper assocTypeDef = getOpenCMISDictionaryService().findAssocType(assocRef.getTypeQName());
                if (assocTypeDef == null || getType(assocRef.getSourceRef()) == null
                        || getType(assocRef.getTargetRef()) == null)
                {
                    continue;
                }

                if ((typeId != null) && !assocTypeDef.getTypeId().equals(typeId))
                {
                    continue;
                }

                counter++;

                if (skip > 0)
                {
                    skip--;
                    continue;
                }

                max--;
                if (max > 0)
                {
                	try
                	{
	                    result.getObjects().add(
	                            createCMISObject(createNodeInfo(assocRef), filter, includeAllowableActions,
	                                    IncludeRelationships.NONE, RENDITION_NONE, false, false/*, cmisVersion*/));
                    }
                    catch(CmisObjectNotFoundException e)
                    {
                        // ignore objects that have not been found (perhaps because their type is unknown to CMIS)
                    }
                }
                else
                {
                    hasMore = true;
                }
            }
        }

        result.setNumItems(BigInteger.valueOf(counter));
        result.setHasMoreItems(hasMore);

        return result;
    }

    public List<RenditionData> getRenditions(NodeRef nodeRef, String renditionFilter, BigInteger maxItems,
            BigInteger skipCount)
    {
        CMISRenditionMapping mapping = getRenditionMapping();
        return mapping.getRenditions(nodeRef, renditionFilter, maxItems, skipCount);
    }

    public Acl getACL(NodeRef nodeRef, boolean onlyBasicPermissions)
    {
        AccessControlListImpl result = new AccessControlListImpl();

        // get the permissions and sort them
        ArrayList<AccessPermission> ordered = new ArrayList<AccessPermission>(
                permissionService.getAllSetPermissions(nodeRef));
        Collections.sort(ordered, new AccessPermissionComparator());

        // remove denied permissions and create OpenCMIS objects
        Map<String, Map<Boolean, AccessControlEntryImpl>> aceMap = new HashMap<String, Map<Boolean, AccessControlEntryImpl>>();
        for (AccessPermission entry : ordered)
        {
            if (entry.getAccessStatus() == AccessStatus.ALLOWED)
            {
                // add allowed entries
                Map<Boolean, AccessControlEntryImpl> directAce = aceMap.get(entry.getAuthority());
                if (directAce == null)
                {
                    directAce = new HashMap<Boolean, AccessControlEntryImpl>();
                    aceMap.put(entry.getAuthority(), directAce);
                }

                AccessControlEntryImpl ace = directAce.get(entry.isSetDirectly());
                if (ace == null)
                {
                    ace = new AccessControlEntryImpl();
                    ace.setPrincipal(new AccessControlPrincipalDataImpl(entry.getAuthority()));
                    ace.setPermissions(new ArrayList<String>());
                    ace.setDirect(entry.isSetDirectly());
                    directAce.put(entry.isSetDirectly(), ace);
                }

                ace.getPermissions().add(entry.getPermission());
            }
            else if (entry.getAccessStatus() == AccessStatus.DENIED)
            {
                // remove denied entries
                Map<Boolean, AccessControlEntryImpl> directAce = aceMap.get(entry.getAuthority());
                if (directAce != null)
                {
                    for (AccessControlEntryImpl ace : directAce.values())
                    {
                        ace.getPermissions().remove(entry.getPermission());
                    }
                }
            }
        }

        // adjust permissions, add CMIS permissions and add ACEs to ACL
        List<Ace> aces = new ArrayList<Ace>();
        result.setAces(aces);
        for (Map<Boolean, AccessControlEntryImpl> bothAces : aceMap.values())
        {
            // get, translate and set direct ACE
            AccessControlEntryImpl directAce = bothAces.get(true);
            if ((directAce != null) && (!directAce.getPermissions().isEmpty()))
            {
            	List<String> permissions = translatePermmissionsToCMIS(directAce.getPermissions(), onlyBasicPermissions);
            	if(permissions != null && !permissions.isEmpty())
            	{
                	// tck doesn't like empty permissions list
	                directAce.setPermissions(permissions);
	                aces.add(directAce);
            	}
            }

            // get, translate, remove duplicate permissions and set indirect ACE
            AccessControlEntryImpl indirectAce = bothAces.get(false);
            if ((indirectAce != null) && (!indirectAce.getPermissions().isEmpty()))
            {
            	List<String> permissions = translatePermmissionsToCMIS(indirectAce.getPermissions(), onlyBasicPermissions);
                indirectAce.setPermissions(permissions);

                // remove permissions that are already set in the direct ACE
                if ((directAce != null) && (!directAce.getPermissions().isEmpty()))
                {
                    indirectAce.getPermissions().removeAll(directAce.getPermissions());
                }

                if(!indirectAce.getPermissions().isEmpty())
                {
                	// tck doesn't like empty permissions list
                	aces.add(indirectAce);
                }
            }
        }

        result.setExact(!onlyBasicPermissions);

        return result;
    }

    private List<String> translatePermmissionsToCMIS(List<String> permissions, boolean onlyBasicPermissions)
    {
        Set<String> result = new TreeSet<String>();

        for (String permission : permissions)
        {
            PermissionReference permissionReference = permissionModelDao.getPermissionReference(null, permission);

            // check for full permissions
            if (permissionModelDao.hasFull(permissionReference))
            {
                result.add(BasicPermissions.READ);
                result.add(BasicPermissions.WRITE);
                result.add(BasicPermissions.ALL);
            }

            // check short forms
            Set<PermissionReference> longForms = permissionModelDao.getGranteePermissions(permissionReference);

            HashSet<String> shortForms = new HashSet<String>();
            for (PermissionReference longForm : longForms)
            {
                shortForms.add(permissionModelDao.isUnique(longForm) ? longForm.getName() : longForm.toString());
            }

            for (String perm : shortForms)
            {
                if (PermissionService.READ.equals(perm))
                {
                    result.add(BasicPermissions.READ);
                }
                else if (PermissionService.WRITE.equals(perm))
                {
                    result.add(BasicPermissions.WRITE);
                }
                else if (PermissionService.ALL_PERMISSIONS.equals(perm))
                {
                    result.add(BasicPermissions.READ);
                    result.add(BasicPermissions.WRITE);
                    result.add(BasicPermissions.ALL);
                }
            }

            // check the permission
            if (PermissionService.READ.equals(permission))
            {
                result.add(BasicPermissions.READ);
            }
            else if (PermissionService.WRITE.equals(permission))
            {
                result.add(BasicPermissions.WRITE);
            }
            else if (PermissionService.ALL_PERMISSIONS.equals(permission))
            {
                result.add(BasicPermissions.READ);
                result.add(BasicPermissions.WRITE);
                result.add(BasicPermissions.ALL);
            }

            // expand native permissions
            if (!onlyBasicPermissions)
            {
                if (permission.startsWith("{"))
                {
                    result.add(permission);
                }
                else
                {
                    result.add(permissionReference.toString());
                }
            }
        }

        return new ArrayList<String>(result);
    }

    public static class AccessPermissionComparator implements Comparator<AccessPermission>
    {
        public int compare(AccessPermission left, AccessPermission right)
        {
            if (left.getPosition() != right.getPosition())
            {
                return right.getPosition() - left.getPosition();
            }
            else
            {
                if (left.getAccessStatus() != right.getAccessStatus())
                {
                    return (left.getAccessStatus() == AccessStatus.DENIED) ? -1 : 1;
                }
                else
                {
                    int compare = left.getAuthority().compareTo(right.getAuthority());
                    if (compare != 0)
                    {
                        return compare;
                    }
                    else
                    {
                        return (left.getPermission().compareTo(right.getPermission()));
                    }

                }

            }
        }
    }

    /**
     * Applies the given ACLs.
     */
    public void applyACL(NodeRef nodeRef, TypeDefinitionWrapper type, Acl addAces, Acl removeAces)
    {
        boolean hasAdd = (addAces != null) && (addAces.getAces() != null) && !addAces.getAces().isEmpty();
        boolean hasRemove = (removeAces != null) && (removeAces.getAces() != null) && !removeAces.getAces().isEmpty();

        if (!hasAdd && !hasRemove)
        {
            return;
        }

        if (!type.getTypeDefinition(false).isControllableAcl())
        {
            throw new CmisConstraintException("Object is not ACL controllable!");
        }

        // remove permissions
        if (hasRemove)
        {
            Set<AccessPermission> permissions = permissionService.getAllSetPermissions(nodeRef);
            for (Ace ace : removeAces.getAces())
            {
                String principalId = ace.getPrincipalId();
                if (CMIS_USER.equals(principalId))
                {
                    principalId = AuthenticationUtil.getFullyAuthenticatedUser();
                }

                for (String permission : translatePermissionsFromCMIS(ace.getPermissions()))
                {

                    AccessPermission toCheck = new AccessPermissionImpl(permission, AccessStatus.ALLOWED, principalId,
                            0);
                    if (!permissions.contains(toCheck))
                    {
                        throw new CmisConstraintException("No matching ACE found to remove!");
                    }

                    permissionService.deletePermission(nodeRef, principalId, permission);
                }
            }
        }

        // add permissions
        if (hasAdd)
        {
            for (Ace ace : addAces.getAces())
            {
                String principalId = ace.getPrincipalId();
                if (CMIS_USER.equals(principalId))
                {
                    principalId = AuthenticationUtil.getFullyAuthenticatedUser();
                }

                for (String permission : translatePermissionsFromCMIS(ace.getPermissions()))
                {
                    permissionService.setPermission(nodeRef, principalId, permission, true);
                }
            }
        }
    }

    /**
     * Sets the given ACL.
     */
    public void applyACL(NodeRef nodeRef, TypeDefinitionWrapper type, Acl aces)
    {
        boolean hasAces = (aces != null) && (aces.getAces() != null) && !aces.getAces().isEmpty();

        if (!hasAces)
        {
            return;
        }

        if (!type.getTypeDefinition(false).isControllableAcl())
        {
            throw new CmisConstraintException("Object is not ACL controllable!");
        }

        Set<AccessPermission> currentAces = permissionService.getAllSetPermissions(nodeRef);
        Acl currentACL = getACL(nodeRef, false);

        // remove all permissions
        permissionService.deletePermissions(nodeRef);

        // set new permissions
        for (Ace ace : aces.getAces())
        {
            String principalId = ace.getPrincipalId();
            if (CMIS_USER.equals(principalId))
            {
                principalId = AuthenticationUtil.getFullyAuthenticatedUser();
            }

            List<String> acePermissions = ace.getPermissions();
            normaliseAcePermissions(currentACL, ace, acePermissions);
            List<String> permissions = translatePermissionsFromCMIS(acePermissions);
            normalisePermissions(currentAces, permissions);
            for (String permission : permissions)
            {
                permissionService.setPermission(nodeRef, principalId, permission, true);
            }
        }
    }

    /*
     * MNT-10165: CMIS 1.1 API: Impossible to remove ACL through Atom binding
     * 
     * Detect permission to delete for principal and 
     * also delete all the concomitant basic permissions
     */
    private void normaliseAcePermissions(Acl currentACL, Ace newAce, List<String> acePermissions)
    {
        for (Ace oldAce : currentACL.getAces())
        {
            if (oldAce.getPrincipalId().equals(newAce.getPrincipalId()))
            {
                // detect what permissions were deleted for principal
                Set<String> permissionsDeletedForPrincipal = new HashSet<String>(oldAce.getPermissions());
                Set<String> newPermissions = new HashSet<String>(newAce.getPermissions());
                permissionsDeletedForPrincipal.removeAll(newPermissions);
                for (String permissionDeleted : permissionsDeletedForPrincipal)
                {
                    // for deleted permission also delete all attendant basic permissions
                    List<String> onePermissionList = new ArrayList<String>();
                    onePermissionList.add(permissionDeleted);

                    List<String> cmisPermissions = translatePermmissionsToCMIS(onePermissionList, false);
                    for (String cmisPermission : cmisPermissions)
                    {
                        acePermissions.remove(cmisPermission);
                    }
                }
            }
        }
    }

    /*
     * ALF-11868: the cmis client library may incorrectly send READ or WRITE permissions to applyAcl.
     * This method works around this by "normalising" permissions:
     * 
     * <ul>
     *    <li> the WRITE permission is removed from permissions if the cmis:write permission is being removed i.e. is in currentAccessPermissions but not in newPermissions
     *    <li> the cmis:write permission is removed from permissions if the WRITE permission is being removed i.e. is in currentAccessPermissions but not in newPermissions
     *    <li> the READ permission is removed from permissions if the cmis:read permission is being removed i.e. is in currentAccessPermissions but not in newPermissions
     *    <li> the cmis:read permission is removed from permissions if the READ permission is being removed i.e. is in currentAccessPermissions but not in newPermissions
     * </ul>
     */
    private void normalisePermissions(Set<AccessPermission> currentAccessPermissions, List<String> newPermissions)
    {
    	Set<String> currentPermissions = new HashSet<String>(currentAccessPermissions.size());
    	for(AccessPermission accessPermission : currentAccessPermissions)
    	{
    		currentPermissions.add(accessPermission.getPermission());
    	}

    	if(currentPermissions.contains(PermissionService.WRITE) && !newPermissions.contains(BasicPermissions.WRITE) && newPermissions.contains(PermissionService.WRITE))
    	{
    		// cmis:write is being removed, so remove WRITE from permissions
    		newPermissions.remove(PermissionService.WRITE);
    	}
    	
    	if(currentPermissions.contains(PermissionService.WRITE) && !newPermissions.contains(PermissionService.WRITE) && newPermissions.contains(BasicPermissions.WRITE))
    	{
    		// WRITE is being removed, so remove cmis:write from permissions
    		newPermissions.remove(BasicPermissions.WRITE);
    	}
    	
    	if(currentPermissions.contains(PermissionService.READ) && !newPermissions.contains(BasicPermissions.READ) && newPermissions.contains(PermissionService.READ))
    	{
    		// cmis:read is being removed, so remove READ from permissions
    		newPermissions.remove(PermissionService.READ);
    	}
    	
    	if(currentPermissions.contains(PermissionService.READ) && !newPermissions.contains(PermissionService.READ) && newPermissions.contains(BasicPermissions.READ))
    	{
    		// READ is being removed, so remove cmis:read from permissions
    		newPermissions.remove(BasicPermissions.READ);
    	}
    }

    private List<String> translatePermissionsFromCMIS(List<String> permissions)
    {
        List<String> result = new ArrayList<String>();

        if (permissions == null)
        {
            return result;
        }

        for (String permission : permissions)
        {
            if (permission == null)
            {
                throw new CmisConstraintException("Invalid null permission!");
            }

            if (BasicPermissions.READ.equals(permission))
            {
                result.add(PermissionService.READ);
            }
            else if (BasicPermissions.WRITE.equals(permission))
            {
                result.add(PermissionService.WRITE);
            }
            else if (BasicPermissions.ALL.equals(permission))
            {
                result.add(PermissionService.ALL_PERMISSIONS);
            }
            else if (!permission.startsWith("{"))
            {
                result.add(permission);
            }
            else
            {
                int sepIndex = permission.lastIndexOf('.');
                if (sepIndex == -1)
                {
                    result.add(permission);
                }
                else
                {
                    result.add(permission.substring(sepIndex + 1));
                }
            }
        }

        return result;
    }

    public void applyPolicies(NodeRef nodeRef, TypeDefinitionWrapper type, List<String> policies)
    {
        if ((policies == null) || (policies.isEmpty()))
        {
            return;
        }

        if (!type.getTypeDefinition(false).isControllablePolicy())
        {
            throw new CmisConstraintException("Object is not policy controllable!");
        }

        // nothing else to do...
    }

    @SuppressWarnings("unchecked")
	public ObjectList query(String statement, Boolean includeAllowableActions,
            IncludeRelationships includeRelationships, String renditionFilter, BigInteger maxItems, BigInteger skipCount/*, CmisVersion cmisVersion*/)
    {
        // prepare results
        ObjectListImpl result = new ObjectListImpl();
        result.setObjects(new ArrayList<ObjectData>());

        // prepare query
        CMISQueryOptions options = new CMISQueryOptions(statement, getRootStoreRef());
        CmisVersion cmisVersion = getRequestCmisVersion();
        options.setCmisVersion(cmisVersion);
        options.setQueryMode(CMISQueryMode.CMS_WITH_ALFRESCO_EXTENSIONS);

        int skip = 0;
        if ((skipCount != null) && (skipCount.intValue() >= 0))
        {
            skip = skipCount.intValue();
            options.setSkipCount(skip);
        }

        if ((maxItems != null) && (maxItems.intValue() >= 0))
        {
            options.setMaxItems(maxItems.intValue());
        }

        boolean fetchObject = includeAllowableActions || (includeRelationships != IncludeRelationships.NONE)
                || (!RENDITION_NONE.equals(renditionFilter));

        // query
        CMISResultSet rs = getOpenCMISQueryService().query(options);
        try
        {
            CMISResultSetColumn[] columns = rs.getMetaData().getColumns();

            for (CMISResultSetRow row : rs)
            {
                NodeRef nodeRef = row.getNodeRef();
                
                if(!nodeService.exists(nodeRef) || filter(nodeRef))
                {
                    continue;
                }
                
                TypeDefinitionWrapper type = getType(nodeRef);
                if (type == null)
                {
                    continue;
                }
                
                ObjectDataImpl hit = new ObjectDataImpl();
                PropertiesImpl properties = new PropertiesImpl();
                hit.setProperties(properties);

                Map<String, Serializable> values = row.getValues();

                for (CMISResultSetColumn column : columns)
                {
                    AbstractPropertyData<?> property = getProperty(column.getCMISDataType(),
                            column.getCMISPropertyDefinition(), values.get(column.getName()));
                    property.setQueryName(column.getName());
                    properties.addProperty(property);
                }

                if (fetchObject)
                {
                    // set allowable actions
                    if (includeAllowableActions)
                    {
                        CMISNodeInfoImpl nodeInfo = createNodeInfo(nodeRef);
                        if(!nodeInfo.getObjectVariant().equals(CMISObjectVariant.NOT_EXISTING))
                        {
                            hit.setAllowableActions(getAllowableActions(nodeInfo));
                        }
                    }

                    // set relationships
                    if (includeRelationships != IncludeRelationships.NONE)
                    {
                        hit.setRelationships(getRelationships(nodeRef, includeRelationships/*, cmisVersion*/));
                    }

                    // set renditions
                    if (!RENDITION_NONE.equals(renditionFilter))
                    {
                        List<RenditionData> renditions = getRenditions(nodeRef, renditionFilter, null, null);
                        if ((renditions != null) && (!renditions.isEmpty()))
                        {
                            hit.setRenditions(renditions);
                        }
                        else
                        {
                        	hit.setRenditions(Collections.EMPTY_LIST);
                        }
                    }
                }

                result.getObjects().add(hit);
            }

            long numberFound = rs.getNumberFound();
            if(numberFound != -1)
            {
            	result.setNumItems(BigInteger.valueOf(numberFound));
            }
            result.setHasMoreItems(rs.hasMore());

        } finally
        {
            rs.close();
        }

        return result;
    }

    /**
     * Sets property values.
     */
    @SuppressWarnings({ "rawtypes" })
    public void setProperties(NodeRef nodeRef, TypeDefinitionWrapper type, Properties properties, String... exclude)
    {
        if (properties == null)
        {
            return;
        }
        
        Map<String, PropertyData<?>> incomingPropsMap = properties.getProperties();
        if (incomingPropsMap == null)
        {
            return;
        }

        // extract property data into an easier to use form
        Map<String, Pair<TypeDefinitionWrapper, Serializable>> propsMap = new HashMap<String, Pair<TypeDefinitionWrapper, Serializable>>();
        for (String propertyId : incomingPropsMap.keySet())
        {
            PropertyData<?> property = incomingPropsMap.get(propertyId);
            PropertyDefinitionWrapper propDef = type.getPropertyById(property.getId());
            if (propDef == null)
            {
                propDef = getOpenCMISDictionaryService().findProperty(propertyId);
                if (propDef == null)
                {
                    throw new CmisInvalidArgumentException("Property " + property.getId() + " is unknown!");
                }
            }

            Updatability updatability = propDef.getPropertyDefinition().getUpdatability();
            if ((updatability == Updatability.READONLY)
                    || (updatability == Updatability.WHENCHECKEDOUT && !checkOutCheckInService.isWorkingCopy(nodeRef)))
            {
                throw new CmisInvalidArgumentException("Property " + property.getId() + " is read-only!");
            }
            TypeDefinitionWrapper propType = propDef.getOwningType();
            Serializable value = getValue(property, propDef.getPropertyDefinition().getCardinality() == Cardinality.MULTI);
            Pair<TypeDefinitionWrapper, Serializable> pair = new Pair<TypeDefinitionWrapper, Serializable>(propType, value);
            propsMap.put(propertyId, pair);
        }

        // Need to do deal with secondary types first
        Pair<TypeDefinitionWrapper, Serializable> pair = propsMap.get(PropertyIds.SECONDARY_OBJECT_TYPE_IDS);
        Serializable secondaryTypesProperty = (pair != null ? pair.getSecond() : null);
        if(secondaryTypesProperty != null)
        {
            if (!(secondaryTypesProperty instanceof List))
            {
                throw new CmisInvalidArgumentException("Secondary types must be a list!");
            }
            List secondaryTypes = (List)secondaryTypesProperty;
            if(secondaryTypes != null && secondaryTypes.size() > 0)
            {
                // add/remove secondary types/aspects
                processSecondaryTypes(nodeRef, secondaryTypes, propsMap);
            }
        }

        for (String propertyId : propsMap.keySet())
        {
            if(propertyId.equals(PropertyIds.SECONDARY_OBJECT_TYPE_IDS))
            {
                // already handled above
                continue;
            }

            pair = propsMap.get(propertyId);
            TypeDefinitionWrapper propType = pair.getFirst();
            Serializable value = pair.getSecond();
            if (Arrays.binarySearch(exclude, propertyId) < 0)
            {
                setProperty(nodeRef, propType, propertyId, value);
            }
        }

        List<CmisExtensionElement> extensions = properties.getExtensions();
        if (extensions != null)
        {
        	boolean isNameChanging = properties.getProperties().containsKey(PropertyIds.NAME);

            for (CmisExtensionElement extension : extensions)
            {
                if (ALFRESCO_EXTENSION_NAMESPACE.equals(extension.getNamespace())
                        && SET_ASPECTS.equals(extension.getName()))
                {
                    setAspectProperties(nodeRef, isNameChanging, extension);
                    break;
                }
            }
        }
    }

    @SuppressWarnings("rawtypes")
    private void processSecondaryTypes(NodeRef nodeRef, List secondaryTypes, Map<String, Pair<TypeDefinitionWrapper, Serializable>> propsToAdd)
    {
        // diff existing aspects and secondaryTypes/aspects list
        Set<QName> existingAspects = nodeService.getAspects(nodeRef);
        Set<QName> secondaryTypeAspects = new HashSet<QName>();
        for(Object o : secondaryTypes)
        {
            String secondaryType = (String)o;
            
            TypeDefinitionWrapper wrapper = getOpenCMISDictionaryService().findType(secondaryType);
            if(wrapper != null)
            {
                QName aspectQName = wrapper.getAlfrescoName();
                secondaryTypeAspects.add(aspectQName);
            }
            else
            {
                throw new CmisInvalidArgumentException("Invalid secondary type id " + secondaryType);
            }
        }

        Set<QName> ignore = new HashSet<QName>();
        ignore.add(ContentModel.ASPECT_REFERENCEABLE);
        ignore.add(ContentModel.ASPECT_LOCALIZED);

        // aspects to add == the list of secondary types - existing aspects - ignored aspects
        Set<QName> toAdd = new HashSet<QName>(secondaryTypeAspects);
        toAdd.removeAll(existingAspects);
        toAdd.removeAll(ignore);

        // aspects to remove == existing aspects - secondary types
        Set<QName> aspectsToRemove = new HashSet<QName>();
        aspectsToRemove.addAll(existingAspects);
        aspectsToRemove.removeAll(ignore);
        Iterator<QName> it = aspectsToRemove.iterator();
        while(it.hasNext())
        {
            QName aspectQName = it.next();
            TypeDefinitionWrapper w = getOpenCMISDictionaryService().findNodeType(aspectQName);
            if(w == null || secondaryTypeAspects.contains(aspectQName))
            {
                // the type is not exposed or is in the secondary types to set, so remove it from the to remove set
                it.remove();
            }
        }

        // first, remove aspects
        for(QName aspectQName : aspectsToRemove)
        {
            nodeService.removeAspect(nodeRef, aspectQName);
            // aspect is being removed so remove all of its properties from the propsToAdd map
            TypeDefinitionWrapper w = getOpenCMISDictionaryService().findNodeType(aspectQName);
            for(PropertyDefinitionWrapper wr : w.getProperties())
            {
                String propertyId = wr.getPropertyId();
                propsToAdd.remove(propertyId);
            }
        }

        // add aspects and properties
        for(QName aspectQName : toAdd)
        {
            nodeService.addAspect(nodeRef, aspectQName, null);
            
            // get aspect properties
            AspectDefinition aspectDef = dictionaryService.getAspect(aspectQName);
            Map<QName, org.alfresco.service.cmr.dictionary.PropertyDefinition> aspectPropDefs = aspectDef.getProperties();
            TypeDefinitionWrapper w = getOpenCMISDictionaryService().findNodeType(aspectQName);
            // for each aspect property...
            for(QName propQName : aspectPropDefs.keySet())
            {
                // find CMIS property id
                PropertyDefinitionWrapper property = w.getPropertyByQName(propQName);
                String propertyId = property.getPropertyId();
                if(!propsToAdd.containsKey(propertyId))
                {
                    TypeDefinitionWrapper propType = property.getOwningType();
                    // CMIS 1.1 secondary types specification requires that all secondary type properties are set
                    // property not included in propsToAdd, add it with null value
                    Pair<TypeDefinitionWrapper, Serializable> pair = new Pair<TypeDefinitionWrapper, Serializable>(propType, null);
                    propsToAdd.put(propertyId, pair);
                }
            }
        }
    }

    public void addSecondaryTypes(NodeRef nodeRef, List<String> secondaryTypes)
    {
    	if(secondaryTypes != null && secondaryTypes.size() > 0)
    	{
	    	for(String secondaryType : secondaryTypes)
	    	{
	            TypeDefinitionWrapper type = getType(secondaryType);
	            if (type == null)
	            {
	                throw new CmisInvalidArgumentException("Invalid secondaryType: " + secondaryType);
	            }
	    		nodeService.addAspect(nodeRef, type.getAlfrescoName(), null);
	    	}
    	}
    }
    
    public void removeSecondaryTypes(NodeRef nodeRef, List<String> secondaryTypes)
    {
    	if(secondaryTypes != null && secondaryTypes.size() > 0)
    	{
	    	for(String secondaryType : secondaryTypes)
	    	{
	            TypeDefinitionWrapper type = getType(secondaryType);
	            if (type == null)
	            {
	                throw new CmisInvalidArgumentException("Invalid secondaryType: " + secondaryType);
	            }
	    		nodeService.removeAspect(nodeRef, type.getAlfrescoName());
	    	}
    	}
    }

    private void setAspectProperties(NodeRef nodeRef, boolean isNameChanging, CmisExtensionElement aspectExtension)
    {
        if (aspectExtension.getChildren() == null)
        {
            return;
        }

        List<String> aspectsToAdd = new ArrayList<String>();
        List<String> aspectsToRemove = new ArrayList<String>();
        Map<QName, List<Serializable>> aspectProperties = new HashMap<QName, List<Serializable>>();

        for (CmisExtensionElement extension : aspectExtension.getChildren())
        {
            if (!ALFRESCO_EXTENSION_NAMESPACE.equals(extension.getNamespace()))
            {
                continue;
            }

            if (ASPECTS_TO_ADD.equals(extension.getName()) && (extension.getValue() != null))
            {
                aspectsToAdd.add(extension.getValue());
            }
            else if (ASPECTS_TO_REMOVE.equals(extension.getName()) && (extension.getValue() != null))
            {
                aspectsToRemove.add(extension.getValue());
            }
            else if (PROPERTIES.equals(extension.getName()) && (extension.getChildren() != null))
            {
                for (CmisExtensionElement property : extension.getChildren())
                {
                    if (!property.getName().startsWith("property"))
                    {
                        continue;
                    }

                    String propertyId = (property.getAttributes() == null ? null : property.getAttributes().get(
                            "propertyDefinitionId"));
                    if ((propertyId == null) || (property.getChildren() == null))
                    {
                        continue;
                    }

                    PropertyType propertyType = PropertyType.STRING;
                    DatatypeFactory df = null;
                    if (property.getName().equals("propertyBoolean"))
                    {
                        propertyType = PropertyType.BOOLEAN;
                    }
                    else if (property.getName().equals("propertyInteger"))
                    {
                        propertyType = PropertyType.INTEGER;
                    }
                    else if (property.getName().equals("propertyDateTime"))
                    {
                        propertyType = PropertyType.DATETIME;
                        try
                        {
                            df = DatatypeFactory.newInstance();
                        }
                        catch (DatatypeConfigurationException e)
                        {
                            throw new CmisRuntimeException("Aspect conversation exception: " + e.getMessage(), e);
                        }
                    }
                    else if (property.getName().equals("propertyDecimal"))
                    {
                        propertyType = PropertyType.DECIMAL;
                    }

                    ArrayList<Serializable> values = new ArrayList<Serializable>();
                    if (property.getChildren() != null)
                    {
//                        try
//                        {
                            for (CmisExtensionElement valueElement : property.getChildren())
                            {
                                if ("value".equals(valueElement.getName()))
                                {
                                    switch (propertyType)
                                    {
                                    case BOOLEAN:
                                    	try
                                    	{
                                    		values.add(Boolean.parseBoolean(valueElement.getValue()));
	                                	}
	                                	catch (Exception e)
	                                	{
	                                		throw new CmisInvalidArgumentException("Invalid property aspect value: " + propertyId, e);
	                                	}
                                        break;
                                    case DATETIME:
                                    	try
                                    	{
	                                        values.add(df.newXMLGregorianCalendar(valueElement.getValue())
	                                                .toGregorianCalendar());
                                    	}
                                    	catch (Exception e)
                                    	{
                                    		throw new CmisInvalidArgumentException("Invalid property aspect value: " + propertyId, e);
                                    	}
                                        break;
                                    case INTEGER:
                                    	BigInteger value = null;
                                    	try
                                    	{
                                    		value = new BigInteger(valueElement.getValue());
                                    	}
                                    	catch (Exception e)
                                    	{
                                    		throw new CmisInvalidArgumentException("Invalid property aspect value: " + propertyId, e);
                                    	}

                                    	// overflow check
                                    	PropertyDefinitionWrapper propDef = getOpenCMISDictionaryService().findProperty(propertyId);
                                        if(propDef == null)
                                        {
                                            throw new CmisInvalidArgumentException("Property " + propertyId + " is unknown!");
                                        }
                                        
                                        QName propertyQName = propDef.getPropertyAccessor().getMappedProperty();
                                        if (propertyQName == null)
                                        {
                                            throw new CmisConstraintException("Unable to set property " + propertyId + "!");
                                        }

                                		org.alfresco.service.cmr.dictionary.PropertyDefinition def = dictionaryService.getProperty(propertyQName);
                                		QName dataDef = def.getDataType().getName();

                                		if (dataDef.equals(DataTypeDefinition.INT) && (value.compareTo(maxInt) > 0 || value.compareTo(minInt) < 0))
                                		{
                                			throw new CmisConstraintException("Value is out of range for property " + propertyId);
                                		}

                                		if (dataDef.equals(DataTypeDefinition.LONG) && (value.compareTo(maxLong) > 0 || value.compareTo(minLong) < 0 ))
                                		{
                                			throw new CmisConstraintException("Value is out of range for property " + propertyId);
                                		}
                                    	
                                        values.add(value);
                                        break;
                                    case DECIMAL:
                                    	try
                                    	{
                                    		values.add(new BigDecimal(valueElement.getValue()));
                                    	}
                                    	catch (Exception e)
                                    	{
                                    		throw new CmisInvalidArgumentException("Invalid property aspect value: " + propertyId, e);
                                    	}
                                        break;
                                    default:
                                        values.add(valueElement.getValue());
                                    }
                                }
                            }
                    }

                    aspectProperties.put(QName.createQName(propertyId, namespaceService), values);
                }
            }
        }

        // remove and add aspects
        String aspectType = null;
        try
        {
            for (String aspect : aspectsToRemove)
            {
                aspectType = aspect;

                TypeDefinitionWrapper type = getType(aspect);
                if (type == null)
                {
                    throw new CmisInvalidArgumentException("Invalid aspect: " + aspectType);
                }

                QName typeName = type.getAlfrescoName();
                // if aspect is hidden aspect, remove only if hidden node is not client controlled
                if(typeName.equals(ContentModel.ASPECT_HIDDEN))
                {
                	if(hiddenAspect.isClientControlled(nodeRef) || aspectProperties.containsKey(ContentModel.PROP_CLIENT_CONTROLLED))
                	{
                		// manipulate hidden aspect only if client controlled
                		nodeService.removeAspect(nodeRef, typeName);	
                	}
                	
                	
                	
//                	if(!isNameChanging && !hiddenAspect.isClientControlled(nodeRef) && !aspectProperties.containsKey(ContentModel.PROP_CLIENT_CONTROLLED))
//                	{
//                    	nodeService.removeAspect(nodeRef, typeName);	
//                	}
                }
                else
                {
                	nodeService.removeAspect(nodeRef, typeName);
                }
            }

            for (String aspect : aspectsToAdd)
            {
                aspectType = aspect;

                TypeDefinitionWrapper type = getType(aspect);
                if (type == null)
                {
                    throw new CmisInvalidArgumentException("Invalid aspect: " + aspectType);
                }

                QName typeName = type.getAlfrescoName();
                // if aspect is hidden aspect, remove only if hidden node is not client controlled
                if(typeName.equals(ContentModel.ASPECT_HIDDEN))
                {
                	if(hiddenAspect.isClientControlled(nodeRef) || aspectProperties.containsKey(ContentModel.PROP_CLIENT_CONTROLLED))
                	{
                		// manipulate hidden aspect only if client controlled
                		nodeService.addAspect(nodeRef, type.getAlfrescoName(),
    	                        Collections.<QName, Serializable> emptyMap());	
                	}
                	
//                	if(!isNameChanging && !hiddenAspect.isClientControlled(nodeRef) && !aspectProperties.containsKey(ContentModel.PROP_CLIENT_CONTROLLED))
//                	{
//                		nodeService.addAspect(nodeRef, type.getAlfrescoName(),
//    	                        Collections.<QName, Serializable> emptyMap());	
//                	}
                }
                else
                {
                	nodeService.addAspect(nodeRef, type.getAlfrescoName(),
	                        Collections.<QName, Serializable> emptyMap());
                }
            }
        }
        catch (InvalidAspectException e)
        {
            throw new CmisInvalidArgumentException("Invalid aspect: " + aspectType);
        }
        catch (InvalidNodeRefException e)
        {
            throw new CmisInvalidArgumentException("Invalid node: " + nodeRef);
        }

        // set property
        for (Map.Entry<QName, List<Serializable>> property : aspectProperties.entrySet())
        {
        	QName propertyQName = property.getKey();

            if (property.getValue().isEmpty())
            {
                if(HiddenAspect.HIDDEN_PROPERTIES.contains(property.getKey()))
                {
                	if(hiddenAspect.isClientControlled(nodeRef) || aspectProperties.containsKey(ContentModel.PROP_CLIENT_CONTROLLED))
                	{
                		// manipulate hidden aspect property only if client controlled
                        nodeService.removeProperty(nodeRef, propertyQName);
                	}
                }
                else
                {
                	nodeService.removeProperty(nodeRef, property.getKey());	
                }
            }
            else
            {
                if(HiddenAspect.HIDDEN_PROPERTIES.contains(property.getKey()))
                {
                	if(hiddenAspect.isClientControlled(nodeRef) || aspectProperties.containsKey(ContentModel.PROP_CLIENT_CONTROLLED))
                	{
                		// manipulate hidden aspect property only if client controlled
                    	nodeService.setProperty(nodeRef, property.getKey(), property.getValue().size() == 1 ? property
                    			.getValue().get(0) : (Serializable) property.getValue());
                	}
                }
                else
                {
            	Serializable value = (Serializable)property.getValue();
                	nodeService.setProperty(nodeRef, property.getKey(), property.getValue().size() == 1 ? property
                        .getValue().get(0) : value);
                }
            }
        }
    }

    /**
     * Sets a property value.
     */
	public void setProperty(NodeRef nodeRef, TypeDefinitionWrapper type, String propertyId, Serializable value)
    {
        if (propertyId == null)
        {
            throw new CmisInvalidArgumentException("Cannot process not null property!");
        }

        PropertyDefinitionWrapper propDef = type.getPropertyById(propertyId);
        if (propDef == null)
        {
            throw new CmisInvalidArgumentException("Property " + propertyId + " is unknown!");
        }

        Updatability updatability = propDef.getPropertyDefinition().getUpdatability();
        if ((updatability == Updatability.READONLY)
                || (updatability == Updatability.WHENCHECKEDOUT && !checkOutCheckInService.isWorkingCopy(nodeRef)))
        {
            throw new CmisInvalidArgumentException("Property " + propertyId + " is read-only!");
        }

        if(propDef.getPropertyId().equals(PropertyIds.SECONDARY_OBJECT_TYPE_IDS))
        {
            throw new IllegalArgumentException("Cannot process " + PropertyIds.SECONDARY_OBJECT_TYPE_IDS + " in setProperty");
        }
        else
        {
        	QName propertyQName = propDef.getPropertyAccessor().getMappedProperty();
        	if (propertyQName == null)
        	{
        		throw new CmisConstraintException("Unable to set property " + propertyId + "!");
        	}
        
        	if (propertyId.equals(PropertyIds.NAME))
        	{
        		if (!(value instanceof String))
        		{
        			throw new CmisInvalidArgumentException("Object name must be a string!");
        		}

        		try
        		{
        			fileFolderService.rename(nodeRef, value.toString());
        		}
        		catch (FileExistsException e)
        		{
        			throw new CmisContentAlreadyExistsException("An object with this name already exists!", e);
        		}
        		catch (FileNotFoundException e)
        		{
        			throw new CmisInvalidArgumentException("Object with id " + nodeRef.getId() + " not found!");
        		}
        	}
        	else
            {
                // overflow check
                if (propDef.getPropertyDefinition().getPropertyType() == PropertyType.INTEGER && value instanceof BigInteger)
                {
                    org.alfresco.service.cmr.dictionary.PropertyDefinition def = dictionaryService.getProperty(propertyQName);
                    QName dataDef = def.getDataType().getName();
                    BigInteger bigValue = (BigInteger) value;

                    if ((bigValue.compareTo(maxInt) > 0 || bigValue.compareTo(minInt) < 0) && dataDef.equals(DataTypeDefinition.INT))
                    {
                        throw new CmisConstraintException("Value is out of range for property " + propertyQName.getLocalName());
                    }

                    if ((bigValue.compareTo(maxLong) > 0 || bigValue.compareTo(minLong) < 0) && dataDef.equals(DataTypeDefinition.LONG))
                    {
                        throw new CmisConstraintException("Value is out of range for property " + propertyQName.getLocalName());
                    }
                }

                nodeService.setProperty(nodeRef, propertyQName, value);
            }
        }
    }

    private Serializable getValue(PropertyData<?> property, boolean isMultiValue)
    {
        if ((property.getValues() == null) || (property.getValues().isEmpty()))
        {
            return null;
        }

        if (isMultiValue)
        {
            return (Serializable) property.getValues();
        }

        return (Serializable) property.getValues().get(0);
    }

    /**
     * Returns content changes.
     */
    public ObjectList getContentChanges(Holder<String> changeLogToken, BigInteger maxItems)
    {
        final ObjectListImpl result = new ObjectListImpl();
        result.setObjects(new ArrayList<ObjectData>());

        EntryIdCallback changeLogCollectingCallback = new EntryIdCallback(true)
        {
            @Override
            public boolean handleAuditEntry(Long entryId, String user, long time, Map<String, Serializable> values)
            {
                result.getObjects().addAll(createChangeEvents(time, values));
                return super.handleAuditEntry(entryId, user, time, values);
            }
        };

        Long from = null;
        if ((changeLogToken != null) && (changeLogToken.getValue() != null))
        {
            try
            {
                from = Long.parseLong(changeLogToken.getValue());
            }
            catch (NumberFormatException e)
            {
                throw new CmisInvalidArgumentException("Invalid change log token: " + changeLogToken);
            }
        }

        AuditQueryParameters params = new AuditQueryParameters();
        params.setApplicationName(CMIS_CHANGELOG_AUDIT_APPLICATION);
        params.setForward(true);
        params.setFromId(from);

        int maxResults = (maxItems == null ? 0 : maxItems.intValue());
        maxResults = (maxResults < 1 ? 0 : maxResults + 1);

        auditService.auditQuery(changeLogCollectingCallback, params, maxResults);

        String newChangeLogToken = null;
        if (maxResults > 0)
        {
            if (result.getObjects().size() >= maxResults)
            {
            	StringBuilder clt = new StringBuilder();
                newChangeLogToken = (from == null ? clt.append(maxItems.intValue() + 1).toString() : clt.append(from.longValue() + maxItems.intValue()).toString());
                result.getObjects().remove(result.getObjects().size() - 1).getId();
                result.setHasMoreItems(true);
            }
            else
            {
                result.setHasMoreItems(false);
            }
        }

        if (changeLogToken != null)
        {
            changeLogToken.setValue(newChangeLogToken);
        }

        return result;
    }

    @SuppressWarnings("unchecked")
    private List<ObjectData> createChangeEvents(long time, Map<String, Serializable> values)
    {
        List<ObjectData> result = new ArrayList<ObjectData>();

        if ((values == null) || (values.size() == 0))
        {
            return result;
        }

        GregorianCalendar changeTime = new GregorianCalendar();
        changeTime.setTimeInMillis(time);

        String appPath = "/" + CMIS_CHANGELOG_AUDIT_APPLICATION + "/";

        for (Entry<String, Serializable> entry : values.entrySet())
        {
            if ((entry.getKey() == null) || (!(entry.getValue() instanceof Map)))
            {
                continue;
            }

            String path = entry.getKey();
            if (!path.startsWith(appPath))
            {
                continue;
            }

            ChangeType changeType = null;
            String changePath = path.substring(appPath.length()).toLowerCase();
            for (ChangeType c : ChangeType.values())
            {
                if (changePath.startsWith(c.value().toLowerCase()))
                {
                    changeType = c;
                    break;
                }
            }

            if (changeType == null)
            {
                continue;
            }

            Map<String, Serializable> valueMap = (Map<String, Serializable>) entry.getValue();
            String objectId = (String) valueMap.get(CMISChangeLogDataExtractor.KEY_OBJECT_ID);

            // build object
            ObjectDataImpl object = new ObjectDataImpl();
            result.add(object);

            PropertiesImpl properties = new PropertiesImpl();
            object.setProperties(properties);
            PropertyIdImpl objectIdProperty = new PropertyIdImpl(PropertyIds.OBJECT_ID, objectId);
            properties.addProperty(objectIdProperty);

            ChangeEventInfoDataImpl changeEvent = new ChangeEventInfoDataImpl();
            object.setChangeEventInfo(changeEvent);
            changeEvent.setChangeType(changeType);
            changeEvent.setChangeTime(changeTime);
        }

        return result;
    }

    private class EntryIdCallback implements AuditQueryCallback
    {
        private final boolean valuesRequired;
        private Long entryId;

        public EntryIdCallback(boolean valuesRequired)
        {
            this.valuesRequired = valuesRequired;
        }

        public String getEntryId()
        {
            return entryId == null ? null : entryId.toString();
        }

        public boolean valuesRequired()
        {
            return this.valuesRequired;
        }

        public final boolean handleAuditEntry(Long entryId, String applicationName, String user, long time,
                Map<String, Serializable> values)
        {
            if (applicationName.equals(CMIS_CHANGELOG_AUDIT_APPLICATION))
            {
                return handleAuditEntry(entryId, user, time, values);
            }
            return true;
        }

        public boolean handleAuditEntry(Long entryId, String user, long time, Map<String, Serializable> values)
        {
            this.entryId = entryId;
            return true;
        }

        public boolean handleAuditEntryError(Long entryId, String errorMsg, Throwable error)
        {
            throw new CmisRuntimeException("Audit entry " + entryId + ": " + errorMsg, error);
        }
    };

    // --------------------------------------------------------------
    // OpenCMIS methods
    // --------------------------------------------------------------

    /**
     * Returns the value of the given property if it exists and is of the
     * correct type.
     */
    public String getStringProperty(Properties properties, String propertyId)
    {
        if ((properties == null) || (properties.getProperties() == null))
        {
            return null;
        }

        PropertyData<?> property = properties.getProperties().get(propertyId);
        if (!(property instanceof PropertyString))
        {
            return null;
        }

        return ((PropertyString) property).getFirstValue();
    }

    /**
     * Returns the value of the given property if it exists and is of the
     * correct type.
     */
    public String getIdProperty(Properties properties, String propertyId)
    {
        if ((properties == null) || (properties.getProperties() == null))
        {
            return null;
        }

        PropertyData<?> property = properties.getProperties().get(propertyId);
        if (!(property instanceof PropertyId))
        {
            return null;
        }

        return ((PropertyId) property).getFirstValue();
    }

    public String getNameProperty(Properties properties, String fallback)
    {
        String name = getStringProperty(properties, PropertyIds.NAME);
        if ((name == null) || (name.trim().length() == 0))
        {
            if (fallback == null)
            {
                throw new CmisInvalidArgumentException("Property " + PropertyIds.NAME + " must be set!");
            }
            else
            {
                name = fallback;
            }
        }

        return name;
    }

    public String getObjectTypeIdProperty(Properties properties)
    {
        String objectTypeId = getIdProperty(properties, PropertyIds.OBJECT_TYPE_ID);
        if ((objectTypeId == null) || (objectTypeId.trim().length() == 0))
        {
            throw new CmisInvalidArgumentException("Property " + PropertyIds.OBJECT_TYPE_ID + " must be set!");
        }

        return objectTypeId;
    }

    public String getSourceIdProperty(Properties properties)
    {
        String id = getIdProperty(properties, PropertyIds.SOURCE_ID);
        if ((id == null) || (id.trim().length() == 0))
        {
            throw new CmisInvalidArgumentException("Property " + PropertyIds.SOURCE_ID + " must be set!");
        }

        return id;
    }

    public String getTargetIdProperty(Properties properties)
    {
        String id = getIdProperty(properties, PropertyIds.TARGET_ID);
        if ((id == null) || (id.trim().length() == 0))
        {
            throw new CmisInvalidArgumentException("Property " + PropertyIds.TARGET_ID + " must be set!");
        }

        return id;
    }

    /**
     * Returns the repository info object.
     */
    public RepositoryInfo getRepositoryInfo(CmisVersion cmisVersion)
    {
        return createRepositoryInfo(cmisVersion);
    }

    /**
     * Returns the repository id.
     */
    public String getRepositoryId()
    {
        return descriptorService.getCurrentRepositoryDescriptor().getId();
    }

    /**
     * Creates the repository info object.
     */
    private RepositoryInfo createRepositoryInfo(CmisVersion cmisVersion)
    {
        Descriptor currentDescriptor = descriptorService.getCurrentRepositoryDescriptor();

        // get change token
        boolean auditEnabled = auditService.isAuditEnabled(CMIS_CHANGELOG_AUDIT_APPLICATION, "/"
                + CMIS_CHANGELOG_AUDIT_APPLICATION);
        String latestChangeLogToken = null;

        if (auditEnabled)
        {
            EntryIdCallback auditQueryCallback = new EntryIdCallback(false);
            AuditQueryParameters params = new AuditQueryParameters();
            params.setApplicationName(CMIS_CHANGELOG_AUDIT_APPLICATION);
            params.setForward(false);
            auditService.auditQuery(auditQueryCallback, params, 1);
            latestChangeLogToken = auditQueryCallback.getEntryId();
        }

        // compile repository info
        RepositoryInfoImpl ri = new RepositoryInfoImpl();

        ri.setId(currentDescriptor.getId());
        ri.setName(currentDescriptor.getName());
        ri.setDescription(currentDescriptor.getName());
        ri.setVendorName("Alfresco");
        ri.setProductName("Alfresco " + descriptorService.getServerDescriptor().getEdition());
        ri.setProductVersion(currentDescriptor.getVersion());
        NodeRef rootNodeRef = getRootNodeRef();
        ri.setRootFolder(constructObjectId(rootNodeRef, null));
        ri.setCmisVersion(cmisVersion);

        ri.setChangesIncomplete(true);
        ri.setChangesOnType(Arrays.asList(new BaseTypeId[] { BaseTypeId.CMIS_DOCUMENT, BaseTypeId.CMIS_FOLDER }));
        ri.setLatestChangeLogToken(latestChangeLogToken);
        ri.setPrincipalAnonymous(AuthenticationUtil.getGuestUserName());
        ri.setPrincipalAnyone(PermissionService.ALL_AUTHORITIES);

        RepositoryCapabilitiesImpl repCap = new RepositoryCapabilitiesImpl();
        ri.setCapabilities(repCap);

        repCap.setAllVersionsSearchable(false);
        repCap.setCapabilityAcl(CapabilityAcl.MANAGE);
        repCap.setCapabilityChanges(auditEnabled ? CapabilityChanges.OBJECTIDSONLY : CapabilityChanges.NONE);
        repCap.setCapabilityContentStreamUpdates(CapabilityContentStreamUpdates.ANYTIME);
        repCap.setCapabilityJoin(CapabilityJoin.NONE);
        repCap.setCapabilityQuery(CapabilityQuery.BOTHCOMBINED);
        repCap.setCapabilityRendition(CapabilityRenditions.READ);
        repCap.setIsPwcSearchable(false);
        repCap.setIsPwcUpdatable(true);
        repCap.setSupportsGetDescendants(true);
        repCap.setSupportsGetFolderTree(true);
        repCap.setSupportsMultifiling(true);
        repCap.setSupportsUnfiling(false);
        repCap.setSupportsVersionSpecificFiling(false);

        AclCapabilitiesDataImpl aclCap = new AclCapabilitiesDataImpl();
        ri.setAclCapabilities(aclCap);

        aclCap.setAclPropagation(AclPropagation.PROPAGATE);
        aclCap.setSupportedPermissions(SupportedPermissions.BOTH);
        aclCap.setPermissionDefinitionData(repositoryPermissions);
        aclCap.setPermissionMappingData(permissionMappings);

        return ri;
    }

    private List<PermissionDefinition> getRepositoryPermissions()
    {
        ArrayList<PermissionDefinition> result = new ArrayList<PermissionDefinition>();

        Set<PermissionReference> all = permissionModelDao.getAllExposedPermissions();
        for (PermissionReference pr : all)
        {
            result.add(createPermissionDefinition(pr));
        }

        PermissionReference allPermission = permissionModelDao.getPermissionReference(null,
                PermissionService.ALL_PERMISSIONS);
        result.add(createPermissionDefinition(allPermission));

        PermissionDefinitionDataImpl cmisPermission;

        cmisPermission = new PermissionDefinitionDataImpl();
        cmisPermission.setPermission(BasicPermissions.READ);
        cmisPermission.setDescription("CMIS Read");
        result.add(cmisPermission);

        cmisPermission = new PermissionDefinitionDataImpl();
        cmisPermission.setPermission(BasicPermissions.WRITE);
        cmisPermission.setDescription("CMIS Write");
        result.add(cmisPermission);

        cmisPermission = new PermissionDefinitionDataImpl();
        cmisPermission.setPermission(BasicPermissions.ALL);
        cmisPermission.setDescription("CMIS All");
        result.add(cmisPermission);

        return result;
    }

    private PermissionDefinition createPermissionDefinition(PermissionReference pr)
    {
        PermissionDefinitionDataImpl permission = new PermissionDefinitionDataImpl();
        permission.setPermission(pr.getQName().toString() + "." + pr.getName());
        permission.setDescription(permission.getId());

        return permission;
    }

    private Map<String, PermissionMapping> getPermissionMappings()
    {
        Map<String, PermissionMapping> result = new HashMap<String, PermissionMapping>();

        for (CMISAllowedActionEnum e : EnumSet.allOf(CMISAllowedActionEnum.class))
        {
            for (Map.Entry<String, List<String>> m : e.getPermissionMapping().entrySet())
            {
                PermissionMappingDataImpl mapping = new PermissionMappingDataImpl();
                mapping.setKey(m.getKey());
                mapping.setPermissions(m.getValue());

                result.put(mapping.getKey(), mapping);
            }
        }

        return result;
    }
    
    private CMISRenditionMapping getRenditionMapping()
    {
        CMISRenditionMapping renditionMapping = (CMISRenditionMapping)singletonCache.get(KEY_CMIS_RENDITION_MAPPING_NODEREF);
        if (renditionMapping == null)
        {
            renditionMapping = new CMISRenditionMapping(nodeService, contentService, renditionService,
                    transactionService, kindToRenditionNames);
            
            singletonCache.put(KEY_CMIS_RENDITION_MAPPING_NODEREF, renditionMapping);
        }
        return renditionMapping;
    }
}
