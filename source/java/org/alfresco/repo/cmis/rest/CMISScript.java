/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
 * FLOSS exception.  You should have received a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.cmis.rest;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.alfresco.cmis.CMISAccessControlEntry;
import org.alfresco.cmis.CMISAccessControlService;
import org.alfresco.cmis.CMISAclCapabilityEnum;
import org.alfresco.cmis.CMISAclPropagationEnum;
import org.alfresco.cmis.CMISAclSupportedPermissionEnum;
import org.alfresco.cmis.CMISBaseObjectTypeIds;
import org.alfresco.cmis.CMISCapabilityChanges;
import org.alfresco.cmis.CMISChangeEvent;
import org.alfresco.cmis.CMISChangeLog;
import org.alfresco.cmis.CMISChangeLogService;
import org.alfresco.cmis.CMISDictionaryService;
import org.alfresco.cmis.CMISJoinEnum;
import org.alfresco.cmis.CMISObjectReference;
import org.alfresco.cmis.CMISPermissionDefinition;
import org.alfresco.cmis.CMISPermissionMapping;
import org.alfresco.cmis.CMISPropertyDefinition;
import org.alfresco.cmis.CMISQueryEnum;
import org.alfresco.cmis.CMISQueryOptions;
import org.alfresco.cmis.CMISQueryService;
import org.alfresco.cmis.CMISRelationshipDirectionEnum;
import org.alfresco.cmis.CMISRelationshipReference;
import org.alfresco.cmis.CMISRepositoryReference;
import org.alfresco.cmis.CMISResultSet;
import org.alfresco.cmis.CMISServices;
import org.alfresco.cmis.CMISTypeDefinition;
import org.alfresco.cmis.CMISTypesFilterEnum;
import org.alfresco.cmis.CMISQueryOptions.CMISQueryMode;
import org.alfresco.cmis.acl.CMISAccessControlEntryImpl;
import org.alfresco.cmis.reference.NodeRefReference;
import org.alfresco.cmis.reference.ReferenceFactory;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.jscript.Association;
import org.alfresco.repo.jscript.BaseScopableProcessorExtension;
import org.alfresco.repo.jscript.ScriptNode;
import org.alfresco.repo.jscript.ValueConverter;
import org.alfresco.repo.web.util.paging.Cursor;
import org.alfresco.repo.web.util.paging.Page;
import org.alfresco.repo.web.util.paging.PagedResults;
import org.alfresco.repo.web.util.paging.Paging;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;


/**
 * CMIS Javascript API
 * 
 * @author davic
 */
public class CMISScript extends BaseScopableProcessorExtension
{
    //
    // Argument Names
    //
    
    public static final String ARG_CHILD_TYPES = "childTypes";
    public static final String ARG_CONTINUE_ON_FAILURE = "continueOnFailure";
    public static final String ARG_CHECKIN = "checkin";
    public static final String ARG_CHECKIN_COMMENT = "checkinComment";
    public static final String ARG_DEPTH = "depth";
    public static final String ARG_DIRECTION = "direction";
    public static final String ARG_FILTER = "filter";
    public static final String ARG_FOLDER_BY_PATH = "folderByPath";
    public static final String ARG_FOLDER_ID = "folderId";
    public static final String ARG_INCLUDE_ALLOWABLE_ACTIONS = "includeAllowableActions";
    public static final String ARG_INCLUDE_PROPERTY_DEFINITIONS = "includePropertyDefinitions";
    public static final String ARG_INCLUDE_RELATIONSHIPS = "includeRelationships";
    public static final String ARG_INCLUDE_SUB_RELATIONSHIP_TYPES = "includeSubRelationshipTypes";
    public static final String ARG_LENGTH = "length";
    public static final String ARG_MAJOR = "major";
    public static final String ARG_MAJOR_VERSION = "majorVersion";
    public static final String ARG_MAX_ITEMS = "maxItems";
    public static final String ARG_OFFSET = "offset";
    public static final String ARG_QUERY_STATEMENT = "q";
    public static final String ARG_REMOVE_FROM = "removeFrom";
    public static final String ARG_RELATIONSHIP_TYPE = "relationshipType";
    public static final String ARG_REPOSITORY_ID = "repositoryId";
    public static final String ARG_RETURN_VERSION = "returnVersion";
    public static final String ARG_SKIP_COUNT = "skipCount";
    public static final String ARG_THIS_VERSION = "thisVersion";
    public static final String ARG_TYPE_ID = "typeId";
    public static final String ARG_TYPES = "types";
    public static final String ARG_UNFILE_MULTIFILE_DOCUMENTS = "unfileMultiFiledDocuments";
    public static final String ARG_VERSIONING_STATE = "versioningState";
    public static final String ARG_SOURCE_FOLDER_ID = "sourceFolderId";
    public static final String ARG_INCLUDE_ACL = "includeACL";    
    public static final String ARG_RENDITION_FILTER = "renditionFilter";
    public static final String ARG_CHANGE_LOG_TOKEN = "changeLogToken";

    // service dependencies
    private ServiceRegistry services;
    private CMISServices cmisService;
    private CMISDictionaryService cmisDictionaryService;
    private CMISQueryService cmisQueryService;
    private CMISAccessControlService cmisAccessControlService;
    private CMISChangeLogService cmisChangeLogService;
    private Paging paging;
    private ReferenceFactory referenceFactory;
    private ValueConverter valueConverter = new ValueConverter();

    
    /**
     * Set the service registry
     * 
     * @param services  the service registry
     */
    public void setServiceRegistry(ServiceRegistry services)
    {
        this.services = services;
    }

    /**
     * Set the paging helper
     * 
     * @param paging
     */
    public void setPaging(Paging paging)
    {
        this.paging = paging;
    }
    
    /**
     * Set the CMIS Service
     * 
     * @param cmisService
     */
    public void setCMISService(CMISServices cmisService)
    {
        this.cmisService = cmisService;
    }

    /**
     * Set the CMIS Dictionary Service
     * 
     * @param cmisDictionaryService
     */
    public void setCMISDictionaryService(CMISDictionaryService cmisDictionaryService)
    {
        this.cmisDictionaryService = cmisDictionaryService;
    }

    /**
     * Set the CMIS Query Service
     * 
     * @param cmisQueryService
     */
    public void setCMISQueryService(CMISQueryService cmisQueryService)
    {
        this.cmisQueryService = cmisQueryService;
    }
    
    /**
     * Sets the CMIS access control service.
     * 
     * @param cmisAccessControlService
     *            the access control service
     */
    public void setCMISAccessControlService(CMISAccessControlService cmisAccessControlService)
    {
        this.cmisAccessControlService = cmisAccessControlService;
    }
    
    /**
     * Sets the CMIS change log service.
     * 
     * @param cmisChangeLogService
     *            the change log service
     */
    public void setCMISChangeLogService(CMISChangeLogService cmisChangeLogService) {
        this.cmisChangeLogService = cmisChangeLogService;
    }

    /**
     * Set the CMIS Reference Factory
     *  
     * @param referenceFactory
     */
    public void setCMISReferenceFactory(ReferenceFactory referenceFactory)
    {
        this.referenceFactory = referenceFactory;
    }
    
    /**
     * Gets the supported CMIS Version
     * 
     * @return  CMIS version
     */
    public String getVersion()
    {
        return cmisService.getCMISVersion();
    }
    
    /**
     * Gets the supported CMIS Specification Title
     * 
     * @return  CMIS Specification Title
     */
    public String getSpecTitle()
    {
        return cmisService.getCMISSpecTitle();
    }
    
    /**
     * Gets the default root folder path
     * 
     * @return  default root folder path
     */
    public String getDefaultRootFolderPath()
    {
        return cmisService.getDefaultRootPath();
    }
    
    /**
     * Gets the default root folder
     * 
     * @return  default root folder
     */
    public ScriptNode getDefaultRootFolder()
    {
        return new ScriptNode(cmisService.getDefaultRootNodeRef(), services, getScope());
    }

    /**
     * Gets the default Types filter
     * 
     * @return  default types filter
     */
    public String getDefaultTypesFilter()
    {
        return CMISTypesFilterEnum.FACTORY.getDefaultLabel();
    }

    /**
     * Is specified Types filter valid?
     * 
     * @param typesFilter  types filter
     * @return  true => valid
     */
    public boolean isValidTypesFilter(String typesFilter)
    {
        return CMISTypesFilterEnum.FACTORY.validLabel(typesFilter);
    }
    
    /**
     * Resolve to a Types Filter
     * 
     * NOTE: If specified types filter is not specified or invalid, the default types
     *       filter is returned
     *       
     * @param typesFilter  types filter
     * @return  resolved types filter
     */
    private CMISTypesFilterEnum resolveTypesFilter(String typesFilter)
    {
        return (CMISTypesFilterEnum)CMISTypesFilterEnum.FACTORY.toEnum(typesFilter);
    }
    
    /**
     * Create CMIS Repository Reference from URL segments
     * 
     * @param args  url arguments
     * @param templateArgs  url template arguments
     * @return  Repository Reference  (or null, in case of bad url)
     */
    public CMISRepositoryReference createRepoReferenceFromUrl(Map<String, String> args, Map<String, String> templateArgs)
    {
        return referenceFactory.createRepoReferenceFromUrl(args, templateArgs);
    }
        
    /**
     * Create CMIS Object Reference from URL segments
     * 
     * @param args  url arguments
     * @param templateArgs  url template arguments
     * @return  Repository Reference  (or null, in case of bad url)
     */
    public CMISObjectReference createObjectReferenceFromUrl(Map<String, String> args, Map<String, String> templateArgs)
    {
        return referenceFactory.createObjectReferenceFromUrl(args, templateArgs);
    }

    /**
     * Create CMIS Relationship Reference from URL segments
     * 
     * @param args  url arguments
     * @param templateArgs  url template arguments
     * @return  Repository Reference  (or null, in case of bad url)
     */
    public CMISRelationshipReference createRelationshipReferenceFromUrl(Map<String, String> args, Map<String, String> templateArgs)
    {
        return referenceFactory.createRelationshipReferenceFromUrl(args, templateArgs);
    }

    /**
     * Create Object Reference
     * 
     * @param repo  repository reference
     * @param object id  object id (NodeRef.toString() format)
     * @return  object id reference
     */
    public CMISObjectReference createObjectIdReference(String objectId)
    {
        return new NodeRefReference(cmisService, objectId);
    }

    /**
     * Get Node from Object Reference
     * 
     * @param ref  object reference
     * @return  node
     */
    public ScriptNode getNode(CMISObjectReference ref)
    {
        NodeRef nodeRef = ref.getNodeRef();
        if (nodeRef == null)
        {
            return null;
        }
        return new ScriptNode(nodeRef, services, getScope());
    }
    
    /**
     * Get Association from Relationship Reference
     *
     * @param ref  relationship reference
     * @return  association
     */
    public Association getAssociation(CMISRelationshipReference ref)
    {
        AssociationRef assocRef = ref.getAssocRef();
        if (assocRef == null)
        {
            return null;
        }
        return new Association(services, assocRef);
    }
    
    /**
     * Query for node children
     * 
     * @param parent  node to query children for
     * @param typesFilter  types filter
     * @param page  page to query for
     * @return  paged result set of children
     */
    public PagedResults queryChildren(ScriptNode parent, String typesFilter, Page page)
    {
        CMISTypesFilterEnum filter = resolveTypesFilter(typesFilter);
        NodeRef[] children = cmisService.getChildren(parent.getNodeRef(), filter);
        
        Cursor cursor = paging.createCursor(children.length, page);
        ScriptNode[] nodes = new ScriptNode[cursor.getRowCount()];
        for (int i = cursor.getStartRow(); i <= cursor.getEndRow(); i++)
        {
            nodes[i - cursor.getStartRow()] = new ScriptNode(children[i], services, getScope());
        }
        
        PagedResults results = paging.createPagedResults(nodes, cursor);
        return results;
    }

    /**
     * Query for node relationships
     * 
     * @param node
     * @param relDef
     * @param includeSubTypes
     * @param direction
     * @param page
     * @return
     */
    public PagedResults queryRelationships(ScriptNode node, CMISTypeDefinition relDef, boolean includeSubTypes, CMISRelationshipDirectionEnum direction, Page page)
    {
        AssociationRef[] relationships = cmisService.getRelationships(node.getNodeRef(), relDef, includeSubTypes, direction);
        
        Cursor cursor = paging.createCursor(relationships.length, page);
        Association[] assocs = new Association[cursor.getRowCount()];
        for (int i = cursor.getStartRow(); i <= cursor.getEndRow(); i++)
        {
            assocs[i - cursor.getStartRow()] = new Association(services, relationships[i], getScope());
        }
        
        PagedResults results = paging.createPagedResults(assocs, cursor);
        return results;
    }
    
    /**
     * Query for items checked-out to user
     * 
     * @param username  user
     * @param page
     * @return  paged result set of checked-out items
     */
    public PagedResults queryCheckedOut(String username, Page page)
    {
        return queryCheckedOut(username, null, false, page);
    }

    /**
     * Query for items checked-out to user within folder
     * 
     * @param username  user
     * @param folder  folder
     * @param page
     * @return  paged result set of checked-out items
     */
    public PagedResults queryCheckedOut(String username, ScriptNode folder, Page page)
    {
        return queryCheckedOut(username, folder, false, page);
    }

    /**
     * Query for items checked-out to user within folder (and possibly descendants)
     * 
     * @param username  user
     * @param folder  folder
     * @param includeDescendants  true = include descendants  
     * @param page
     * @return  paged result set of checked-out items
     */
    public PagedResults queryCheckedOut(String username, ScriptNode folder, boolean includeDescendants, Page page)
    {
        NodeRef[] checkedout = cmisService.getCheckedOut(username, (folder == null) ? null : folder.getNodeRef(), includeDescendants);
        Cursor cursor = paging.createCursor(checkedout.length, page);
        ScriptNode[] nodes = new ScriptNode[cursor.getRowCount()];
        for (int i = cursor.getStartRow(); i <= cursor.getEndRow(); i++)
        {
            nodes[i - cursor.getStartRow()] = new ScriptNode(checkedout[i], services, getScope());
        }
        
        PagedResults results = paging.createPagedResults(nodes, cursor);
        return results;
    }

    /**
     * Query for child types (of a given type), or the base types (if no type given)
     * 
     * @param typeDef
     * @param page
     * @return
     */
    public PagedResults queryTypeChildren(CMISTypeDefinition typeDef, Page page)
    {
        Collection<CMISTypeDefinition> children = (typeDef == null) ? cmisDictionaryService.getBaseTypes() : typeDef.getSubTypes(false);
        Cursor cursor = paging.createCursor(children.size(), page);
        
        // skip
        Iterator<CMISTypeDefinition> iterTypeDefs = children.iterator();
        for (int i = 0; i < cursor.getStartRow(); i++)
        {
            iterTypeDefs.next();
        }

        // get types for page
        CMISTypeDefinition[] types = new CMISTypeDefinition[cursor.getRowCount()];
        for (int i = cursor.getStartRow(); i <= cursor.getEndRow(); i++)
        {
            types[i - cursor.getStartRow()] = iterTypeDefs.next();
        }
        
        PagedResults results = paging.createPagedResults(types, cursor);
        return results;
    }
    
    /**
     * Query for a Type Definition given a CMIS Type Id
     * 
     * @param page
     * @return  CMIS Type Definition
     */
    public CMISTypeDefinition queryType(String typeId)
    {
        try
        {
            return cmisDictionaryService.findType(typeId);
        }
        catch(AlfrescoRuntimeException e)
        {
            return null;
        }
    }
    
    /**
     * Query the Type Definition for the given Node
     * 
     * @param node
     * @return  CMIS Type Definition
     */
    public CMISTypeDefinition queryType(ScriptNode node)
    {
        try
        {
            QName typeQName = node.getQNameType();
            return cmisDictionaryService.findTypeForClass(typeQName);
        }
        catch(AlfrescoRuntimeException e)
        {
            return null;
        }
    }
    
    /**
     * Query the Property Definition for the given Property
     * 
     * @param propertyName
     * @return
     */
    public CMISPropertyDefinition queryProperty(String propertyName)
    {
        return cmisDictionaryService.findProperty(propertyName, null);
    }
    
    //
    // SQL Query
    // 

    /**
     * Can you query the private working copy of a document.
     *  
     * @return
     */
    public boolean getPwcSearchable()
    {
        return cmisQueryService.getPwcSearchable();
    }

    /**
     * Can you query non-latest versions of a document.
     *  
     * The current latest version is always searchable according to  the type definition.
     * 
     * @return
     */
    public boolean getAllVersionsSearchable()
    {
        return cmisQueryService.getAllVersionsSearchable();
    }

    /**
     * Get the query support level.
     * 
     * @return
     */
    public CMISQueryEnum getQuerySupport()
    {
       return cmisQueryService.getQuerySupport(); 
    }

    /**
     * Get the join support level in queries.
     * 
     * @return
     */
    public CMISJoinEnum getJoinSupport()
    {
       return cmisQueryService.getJoinSupport(); 
    }
    
    /**
     * Issue query
     * 
     * @param statement  query statement
     * @param page
     * 
     * @return  paged result set
     */
    public PagedResults query(String statement, Page page)
    {
        Cursor unknownRows = paging.createCursor(Integer.MAX_VALUE, page);
        CMISQueryOptions options = new CMISQueryOptions(statement, cmisService.getDefaultRootStoreRef());
        options.setQueryMode(CMISQueryMode.CMS_WITH_ALFRESCO_EXTENSIONS);
        options.setSkipCount(unknownRows.getStartRow());
        options.setMaxItems(unknownRows.getPageSize());
        CMISResultSet resultSet = cmisQueryService.query(options);
        Cursor cursor = paging.createCursor(unknownRows.getStartRow() + resultSet.getLength() + (resultSet.hasMore() ? 1 : 0) , page);
        return paging.createPagedResult(resultSet, cursor);
    }
    
    /**
     * Gets the ACL capability.
     * 
     * @return the ACL capability
     */
    public CMISAclCapabilityEnum getAclCapability()
    {
        return cmisAccessControlService.getAclCapability();
    }    

    /**
     * Gets the supported permission types
     * 
     * @return the supported permission types
     */
    public CMISAclSupportedPermissionEnum getAclSupportedPermissions()
    {
        return cmisAccessControlService.getSupportedPermissions();
    }

    /**
     * Gets the ACL propagation.
     * 
     * @return the ACL propagation
     */
    public CMISAclPropagationEnum getAclPropagation()
    {
        return cmisAccessControlService.getAclPropagation();
    }
    
    /**
     * Get all the permissions defined by the repository.
     * @return a list of permissions
     */
    public List<CMISPermissionDefinition> getRepositoryPermissions()
    {
        return cmisAccessControlService.getRepositoryPermissions();
    }
    
    
    /**
     * Get the list of permission mappings.
     * @return get the permission mapping as defined by the CMIS specification.
     */
    public List<? extends CMISPermissionMapping> getPermissionMappings()
    {
       return cmisAccessControlService.getPermissionMappings(); 
    }
    
    /**
     * Gets the name of the principal who is used for anonymous access. This principal can then be passed to the ACL
     * services to specify what permissions anonymous users should have.
     * 
     * @return name of the principal who is used for anonymous access
     */
    public String getPrincipalAnonymous()
    {
        return cmisAccessControlService.getPrincipalAnonymous();
    }

    /**
     * Gets the name of the principal who is used to indicate any authenticated user. This principal can then be passed
     * to the ACL services to specify what permissions any authenticated user should have.
     * 
     * @return name of the principal who is used to indicate any authenticated user
     */
    public String getPrincipalAnyone()
    {
        return cmisAccessControlService.getPrincipalAnyone();
    }
    
    @SuppressWarnings("unchecked")
    public void applyACL(ScriptNode node, Serializable principalIds, Serializable permissions)
    {
        List<String> principalList = (List<String>) valueConverter.convertValueForRepo(principalIds);
        List<String> permissionList = (List<String>) valueConverter.convertValueForRepo(permissions);
        List<CMISAccessControlEntry> acesToApply = new ArrayList<CMISAccessControlEntry>(principalList.size());
        for(int i=0; i<principalList.size(); i++)
        {
            acesToApply.add(new CMISAccessControlEntryImpl(principalList.get(i), permissionList.get(i)));
        }
        cmisAccessControlService.applyAcl(node.getNodeRef(), acesToApply);
    }
    
    /**
     * Gets the change log capability.
     * 
     * @return the change log capability
     */
    public CMISCapabilityChanges getChangeLogCapability()
    {
        return cmisChangeLogService.getCapability();
    }

    /**
     * Gets the last change log token.
     * 
     * @return the last change log token
     */
    public String getLastChangeLogToken()
    {
        return cmisChangeLogService.getLastChangeLogToken();
    }
    
    /**
     * Gets the list of base types for which changes are available.
     * 
     * @return the list of base types for which changes are available
     */
    public String[] getChangesOnType()
    {
        List<CMISBaseObjectTypeIds> typeList = cmisChangeLogService.getChangesOnTypeCapability();
        String[] changesOnType = new String[typeList.size()];
        int i=0;
        for (CMISBaseObjectTypeIds type: typeList)
        {
            changesOnType[i++] = type.getLabel();
        }
        return changesOnType;
    }

    /**
     * Determines whether the repository's change log can return all changes ever made to any object in the repository
     * or only changes made after a particular point in time.
     * 
     * @return <code>false</code> if the change log can return all changes ever made to every object.<code>true</code>
     *         if the change log includes all changes made since a particular point in time, but not all changes ever
     *         made.
     */
    public boolean getChangesIncomplete()
    {
        return cmisChangeLogService.getChangesIncomplete();
    }
    
    /**
     * Gets the change log attributes.
     * 
     * @param changeLogToken
     *            the change log token
     * @param maxItems
     *            the maximum number of events to include to return or <code>null</code>
     * @return the change log attributes
     */
    public Scriptable getChangeLog(String changeLogToken, Integer maxItems)
    {
        Scriptable scope = getScope();
        Context cx = Context.enter();
        try
        {
            Scriptable changeLogMap = cx.newObject(scope);
            CMISChangeLog changeLog = cmisChangeLogService.getChangeLogEvents(changeLogToken, maxItems);
            List<CMISChangeEvent> changeEvents = changeLog.getChangeEvents();

            // Wrap the events
            int size = changeEvents.size();
            Scriptable changeEventArr = cx.newArray(scope, size);
            for (int i = 0; i < size; i++)
            {
                ScriptableObject.putProperty(changeEventArr, i, Context.javaToJS(changeEvents.get(i), scope));
            }
            ScriptableObject.putProperty(changeLogMap, "changeEvents", changeEventArr);

            // Wrap the nodes
            Scriptable changeNodes = cx.newArray(scope, size);
            for (int i = 0; i < size; i++)
            {
                ScriptableObject.putProperty(changeNodes, i, new ScriptNode(changeEvents.get(i).getNode(), services,
                        scope));
            }
            ScriptableObject.putProperty(changeLogMap, "changeNodes", changeNodes);

            // Provide the other attributes
            ScriptableObject.putProperty(changeLogMap, "hasMoreItems", Context
                    .javaToJS(changeLog.hasMoreItems(), scope));
            ScriptableObject
                    .putProperty(changeLogMap, "eventCount", Context.javaToJS(changeLog.getEventCount(), scope));

            // Handle paging parameters
            String nextChangeToken = changeLog.getNextChangeToken();
            if (nextChangeToken != null)
            {
                ScriptableObject.putProperty(changeLogMap, "nextChangeToken", Context.javaToJS(nextChangeToken, scope));
            }
            String previousPageChangeLogToken = cmisChangeLogService.getPreviousPageChangeLogToken(changeLogToken,
                    maxItems);
            if (previousPageChangeLogToken != null)
            {
                ScriptableObject.putProperty(changeLogMap, "previousPageToken", Context.javaToJS(
                        previousPageChangeLogToken, scope));
            }
            String lastPageChangeLogToken = cmisChangeLogService.getLastPageChangeLogToken(changeLogToken, maxItems);
            if (lastPageChangeLogToken != null)
            {
                ScriptableObject.putProperty(changeLogMap, "lastPageToken", Context.javaToJS(lastPageChangeLogToken,
                        scope));
            }
            return changeLogMap;
        }
        finally
        {
            Context.exit();
        }
    }
}
