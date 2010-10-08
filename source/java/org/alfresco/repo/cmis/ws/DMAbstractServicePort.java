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
package org.alfresco.repo.cmis.ws;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.bind.JAXBElement;

import org.alfresco.cmis.CMISAccessControlEntry;
import org.alfresco.cmis.CMISAccessControlFormatEnum;
import org.alfresco.cmis.CMISAccessControlReport;
import org.alfresco.cmis.CMISAccessControlService;
import org.alfresco.cmis.CMISAclPropagationEnum;
import org.alfresco.cmis.CMISActionEvaluator;
import org.alfresco.cmis.CMISAllowedActionEnum;
import org.alfresco.cmis.CMISChangeLogService;
import org.alfresco.cmis.CMISDictionaryModel;
import org.alfresco.cmis.CMISFilterNotValidException;
import org.alfresco.cmis.CMISInvalidArgumentException;
import org.alfresco.cmis.CMISQueryService;
import org.alfresco.cmis.CMISRelationshipDirectionEnum;
import org.alfresco.cmis.CMISRendition;
import org.alfresco.cmis.CMISRenditionService;
import org.alfresco.cmis.CMISServiceException;
import org.alfresco.cmis.CMISServices;
import org.alfresco.cmis.CMISTypeDefinition;
import org.alfresco.cmis.PropertyFilter;
import org.alfresco.cmis.acl.CMISAccessControlEntryImpl;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.cmis.ws.utils.ExceptionUtil;
import org.alfresco.repo.cmis.ws.utils.PropertyUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.web.util.paging.Cursor;
import org.alfresco.repo.web.util.paging.Page;
import org.alfresco.repo.web.util.paging.Paging;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.descriptor.DescriptorService;

/**
 * Base class for all CMIS web services
 * 
 * @author Michael Shavnev
 * @author Dmitry Lazurkin
 * @author Dmitry Velichkevich
 * @author Stanislav Sokolovsky
 */
public class DMAbstractServicePort
{
    private static final String CMIS_USER = "cmis:user";

    private static final String INVALID_REPOSITORY_ID_MESSAGE = "Invalid repository id";

    private static final Map<EnumACLPropagation, CMISAclPropagationEnum> ACL_PROPAGATION_ENUM_MAPPGIN;
    protected static final Map<EnumIncludeRelationships, CMISRelationshipDirectionEnum> INCLUDE_RELATIONSHIPS_ENUM_MAPPING;
    private static final Map<CMISAllowedActionEnum, PropertyDescriptor> ALLOWED_ACTION_ENUM_MAPPING;
    static
    {
        ACL_PROPAGATION_ENUM_MAPPGIN = new HashMap<EnumACLPropagation, CMISAclPropagationEnum>(5);
        ACL_PROPAGATION_ENUM_MAPPGIN.put(EnumACLPropagation.OBJECTONLY, CMISAclPropagationEnum.OBJECT_ONLY);
        ACL_PROPAGATION_ENUM_MAPPGIN.put(EnumACLPropagation.PROPAGATE, CMISAclPropagationEnum.PROPAGATE);
        ACL_PROPAGATION_ENUM_MAPPGIN.put(EnumACLPropagation.REPOSITORYDETERMINED, CMISAclPropagationEnum.REPOSITORY_DETERMINED);

        INCLUDE_RELATIONSHIPS_ENUM_MAPPING = new HashMap<EnumIncludeRelationships, CMISRelationshipDirectionEnum>(5);
        INCLUDE_RELATIONSHIPS_ENUM_MAPPING.put(EnumIncludeRelationships.SOURCE, CMISRelationshipDirectionEnum.SOURCE);
        INCLUDE_RELATIONSHIPS_ENUM_MAPPING.put(EnumIncludeRelationships.TARGET, CMISRelationshipDirectionEnum.TARGET);
        INCLUDE_RELATIONSHIPS_ENUM_MAPPING.put(EnumIncludeRelationships.BOTH, CMISRelationshipDirectionEnum.BOTH);

        try
        {
            ALLOWED_ACTION_ENUM_MAPPING = new HashMap<CMISAllowedActionEnum, PropertyDescriptor>(97);
            for (PropertyDescriptor propertyDescriptor : Introspector.getBeanInfo(CmisAllowableActionsType.class, Object.class).getPropertyDescriptors())
            {
                String label = propertyDescriptor.getName();
                CMISAllowedActionEnum allowedActionEnum = CMISAllowedActionEnum.FACTORY.fromLabel(label);
                if (allowedActionEnum != null)
                {
                    ALLOWED_ACTION_ENUM_MAPPING.put(allowedActionEnum, propertyDescriptor);
                }
            }
        }
        catch (IntrospectionException e)
        {
            throw new ExceptionInInitializerError(e);
        }
    }

    private Paging paging = new Paging();

    protected ObjectFactory cmisObjectFactory = new ObjectFactory();
    protected CMISQueryService cmisQueryService;
    protected CMISServices cmisService;
    protected CMISChangeLogService cmisChangeLogService;
    protected CMISRenditionService cmisRenditionService;
    protected CMISAccessControlService cmisAclService;

    protected DescriptorService descriptorService;
    protected NodeService nodeService;
    protected FileFolderService fileFolderService;
    protected SearchService searchService;
    protected PropertyUtil propertiesUtil;
    protected PermissionService permissionService;
    protected AuthorityService authorityService;

    public void setCmisService(CMISServices cmisService)
    {
        this.cmisService = cmisService;
    }

    public void setCmisQueryService(CMISQueryService cmisQueryService)
    {
        this.cmisQueryService = cmisQueryService;
    }

    public void setCmisChangeLogService(CMISChangeLogService cmisChangeLogService)
    {
        this.cmisChangeLogService = cmisChangeLogService;
    }

    public void setCmisAclService(CMISAccessControlService cmisAclService)
    {
        this.cmisAclService = cmisAclService;
    }

    public void setCmisRenditionService(CMISRenditionService cmisRenditionService)
    {
        this.cmisRenditionService = cmisRenditionService;
    }

    public void setDescriptorService(DescriptorService descriptorService)
    {
        this.descriptorService = descriptorService;
    }

    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    public void setFileFolderService(FileFolderService fileFolderService)
    {
        this.fileFolderService = fileFolderService;
    }

    public void setPropertiesUtil(PropertyUtil propertiesUtil)
    {
        this.propertiesUtil = propertiesUtil;
    }

    public void setSearchService(SearchService searchService)
    {
        this.searchService = searchService;
    }

    public void setPermissionService(PermissionService permissionService)
    {
        this.permissionService = permissionService;
    }

    public void setAuthorityService(AuthorityService authorityService)
    {
        this.authorityService = authorityService;
    }

    protected PropertyFilter createPropertyFilter(String filter) throws CmisException
    {
        try
        {
            return new PropertyFilter(filter);
        }
        catch (CMISFilterNotValidException e)
        {
            throw ExceptionUtil.createCmisException(e);
        }
    }

    protected PropertyFilter createPropertyFilter(JAXBElement<String> element) throws CmisException
    {
        String filter = null;
        if (element != null)
        {
            filter = element.getValue();
        }
        return createPropertyFilter(filter);
    }

    protected Cursor createCursor(int totalRows, BigInteger skipCount, BigInteger maxItems)
    {
        Page window = paging.createPageOrWindow(null, null, skipCount != null ? skipCount.intValue() : null, maxItems != null ? maxItems.intValue() : null);
        return paging.createCursor(totalRows, window);
    }

    /**
     * Returns true if folder contains object
     * 
     * @param object object NodeRef
     * @param folder folder NodeRef
     * @return returns true if folder contains object
     */
    protected boolean isObjectInFolder(NodeRef object, NodeRef folder)
    {
        NodeRef searchedObjectNodeRef = fileFolderService.searchSimple(folder, (String) nodeService.getProperty(object, ContentModel.PROP_NAME));
        return (null != searchedObjectNodeRef) && (searchedObjectNodeRef.equals(object));
    }

    /**
     * This method converts Alfresco's <b>NodeRef</b>'s to CMIS objects those will be stored in <b>resultList</b>-parameter. Properties for returning filtering also performs
     * 
     * @param filter properties filter value for filtering objects returning properties
     * @param includeRelationships what relationships to include
     * @param sourceList the list that contains all returning Node References
     * @param resultList the list of <b>CmisObjectType</b> values for end response result collecting
     * @throws CmisException
     */
    protected void createCmisObjectList(PropertyFilter filter, CMISRelationshipDirectionEnum includeRelationships, boolean includeAllowableActions, String renditionFilter,
            List<NodeRef> sourceList, List<CmisObjectType> resultList) throws CmisException
    {
        for (NodeRef objectNodeRef : sourceList)
        {
            resultList.add(createCmisObject(objectNodeRef, filter, includeRelationships, includeAllowableActions, renditionFilter));
        }
    }

    /**
     * This method creates and configures CMIS object against appropriate Alfresco object (NodeRef or AssociationRef).
     * 
     * @param object the Alfresco object
     * @param filter accepted properties filter
     * @param includeRelationships what relationships to include
     * @param includeAllowableActions should we include allowable actions?
     * @param renditionFilter the rendition filter
     * @return the converted CMIS object
     * @throws CmisException on error
     */
    protected CmisObjectType createCmisObject(Object object, PropertyFilter filter, EnumIncludeRelationships includeRelationships, Boolean includeAllowableActions,
            String renditionFilter) throws CmisException
    {
        return createCmisObject(object, filter, includeRelationships == null ? null : INCLUDE_RELATIONSHIPS_ENUM_MAPPING.get(includeRelationships), includeAllowableActions != null
                && includeAllowableActions, renditionFilter);
    }

    /**
     * This method creates and configures CMIS object against appropriate Alfresco object (NodeRef or AssociationRef).
     * 
     * @param object the Alfresco object
     * @param filter accepted properties filter
     * @param includeRelationships what relationships to include
     * @param includeAllowableActions should we include allowable actions?
     * @param renditionFilter the rendition filter
     * @return the converted CMIS object
     * @throws CmisException on error
     */
    protected CmisObjectType createCmisObject(Object object, PropertyFilter filter, CMISRelationshipDirectionEnum includeRelationships, boolean includeAllowableActions,
            String renditionFilter) throws CmisException
    {
        // Get a NodeRef if we can
        if (object instanceof Version)
        {
            object = ((Version) object).getFrozenStateNodeRef();
        }
        CmisObjectType result = new CmisObjectType();
        result.setProperties(propertiesUtil.getProperties(object, filter));
        if (object instanceof NodeRef && includeRelationships != null)
        {
            appendWithRelationships((NodeRef) object, filter, includeRelationships, includeAllowableActions, renditionFilter, result);
        }
        if (includeAllowableActions)
        {
            result.setAllowableActions(determineObjectAllowableActions(object));
        }
        if (renditionFilter != null)
        {
            List<CmisRenditionType> renditions = getRenditions(object, renditionFilter);
            if (renditions != null && !renditions.isEmpty())
            {
                result.getRendition().addAll(renditions);
            }
        }
        return result;
    }

    protected void appendWithRelationships(NodeRef object, PropertyFilter filter, CMISRelationshipDirectionEnum includeRelationships, boolean includeAllowableActions,
            String renditionFilter, CmisObjectType result) throws CmisException
    {
        List<CmisObjectType> relationships = result.getRelationship();
        try
        {
            for (AssociationRef assoc : cmisService.getRelationships(object, null, true, includeRelationships))
            {
                relationships.add(createCmisObject(assoc, filter, includeRelationships, includeAllowableActions, renditionFilter));
            }
        }
        catch (CMISInvalidArgumentException e)
        {
            throw ExceptionUtil.createCmisException(e);
        }
    }

    /**
     * Checks specified in CMIS request parameters repository Id.
     * 
     * @param repositoryId repository id
     * @throws CmisException repository diesn't exist
     */
    protected void checkRepositoryId(String repositoryId) throws CmisException
    {
        if (!this.descriptorService.getCurrentRepositoryDescriptor().getId().equals(repositoryId))
        {
            throw ExceptionUtil.createCmisException(INVALID_REPOSITORY_ID_MESSAGE, EnumServiceException.INVALID_ARGUMENT);
        }
    }

    protected List<CmisRenditionType> getRenditions(Object object, String renditionFilter) throws CmisException
    {
        List<CmisRenditionType> result = null;
        if (object instanceof Version)
        {
            object = ((Version) object).getFrozenStateNodeRef();
        }
        if (object instanceof NodeRef)
        {
            NodeRef document = (NodeRef) object;

            List<CMISRendition> renditions = null;
            try
            {
                renditions = cmisRenditionService.getRenditions(document, renditionFilter);
            }
            catch (CMISFilterNotValidException e)
            {
                throw ExceptionUtil.createCmisException(e);
            }
            if (renditions != null && !renditions.isEmpty())
            {
                result = new ArrayList<CmisRenditionType>();
                for (CMISRendition rendition : renditions)
                {
                    if (rendition != null)
                    {
                        CmisRenditionType cmisRenditionType = convertToCmisRenditionType(rendition);
                        result.add(cmisRenditionType);
                    }
                }
            }
        }
        return result;
    }

    private CmisRenditionType convertToCmisRenditionType(CMISRendition rendition)
    {
        CmisRenditionType cmisRenditionType = new CmisRenditionType();
        cmisRenditionType.setStreamId(rendition.getStreamId());
        cmisRenditionType.setKind(rendition.getKind());
        cmisRenditionType.setMimetype(rendition.getMimeType());
        cmisRenditionType.setTitle(rendition.getTitle());
        cmisRenditionType.setWidth(rendition.getWidth() != null ? BigInteger.valueOf(rendition.getWidth()) : null);
        cmisRenditionType.setHeight(rendition.getHeight() != null ? BigInteger.valueOf(rendition.getHeight()) : null);
        cmisRenditionType.setLength(rendition.getLength() != null ? BigInteger.valueOf(rendition.getLength()) : null);
        cmisRenditionType.setRenditionDocumentId(rendition.getRenditionDocumentId());
        return cmisRenditionType;
    }

    protected void appendWithAce(NodeRef identifierInstance, CmisObjectType object)
    {
        CMISAccessControlReport aclReport = cmisAclService.getAcl(identifierInstance, CMISAccessControlFormatEnum.REPOSITORY_SPECIFIC_PERMISSIONS);
        object.setAcl(convertAclReportToCmisAclType(aclReport).getACL());
        object.setExactACL(aclReport.isExact());
    }

    protected void applyPolicies(String objectId, List<String> policies) throws CmisException
    {
        // Process any provided policy IDs (they will be rejected!)
        if (policies != null)
        {
            for (String policyId : policies)
            {
                try
                {
                    cmisService.applyPolicy(policyId, objectId);
                }
                catch (CMISServiceException e)
                {
                    throw ExceptionUtil.createCmisException(e);
                }
            }
        }
    }

    protected CmisACLType applyAclCarefully(NodeRef object, CmisAccessControlListType addACEs, CmisAccessControlListType removeACEs, EnumACLPropagation aclPropagation,
            List<String> policies) throws CmisException
    {
        if (addACEs == null && removeACEs == null)
        {
            return null;
        }
        try
        {
            CMISAclPropagationEnum propagation = (null == aclPropagation) ? (CMISAclPropagationEnum.PROPAGATE) : (ACL_PROPAGATION_ENUM_MAPPGIN.get(aclPropagation));
            List<CMISAccessControlEntry> acesToAdd = (null == addACEs) ? (null) : (convertToAlfrescoAceEntriesList(addACEs.getPermission()));
            List<CMISAccessControlEntry> acesToRemove = (null == removeACEs) ? (null) : (convertToAlfrescoAceEntriesList(removeACEs.getPermission()));
            CMISAccessControlReport aclReport = null;
            aclReport = cmisAclService.applyAcl(object, acesToRemove, acesToAdd, propagation, CMISAccessControlFormatEnum.REPOSITORY_SPECIFIC_PERMISSIONS);

            // Process any provided policy IDs (they will be rejected!)
            if (policies != null)
            {
                String objectId = (String) cmisService.getProperty(object, CMISDictionaryModel.PROP_OBJECT_ID);
                applyPolicies(objectId, policies);
            }
            CmisACLType result = convertAclReportToCmisAclType(aclReport);
            return result;
        }
        catch (CMISServiceException e)
        {
            throw ExceptionUtil.createCmisException(e);
        }
    }

    private List<CMISAccessControlEntry> convertToAlfrescoAceEntriesList(List<CmisAccessControlEntryType> source) throws CmisException
    {
        List<CMISAccessControlEntry> result = new LinkedList<CMISAccessControlEntry>();
        for (CmisAccessControlEntryType cmisEntry : source)
        {
            String principalId = cmisEntry.getPrincipal().getPrincipalId();
            if (CMIS_USER.equals(principalId))
            {
                principalId = AuthenticationUtil.getFullyAuthenticatedUser();
            }
            for (String permission : cmisEntry.getPermission())
            {
                result.add(new CMISAccessControlEntryImpl(principalId, permission));
            }
        }
        return result;
    }

    protected CmisACLType convertAclReportToCmisAclType(CMISAccessControlReport aclReport)
    {
        CmisACLType result = new CmisACLType();
        CmisAccessControlListType aceList = new CmisAccessControlListType();
        result.setACL(aceList);
        for (CMISAccessControlEntry ace : aclReport.getAccessControlEntries())
        {
            CmisAccessControlEntryType entry = new CmisAccessControlEntryType();
            entry.setDirect(ace.getDirect());
            entry.getPermission().add(ace.getPermission()); // FIXME: [BUG] Should be List<String> getPermission() instead of String getPermission()!!!
            CmisAccessControlPrincipalType principal = new CmisAccessControlPrincipalType();
            principal.setPrincipalId(ace.getPrincipalId());
            entry.setPrincipal(principal);
            aceList.getPermission().add(entry);
        }
        result.setExact(aclReport.isExact());
        return result;
    }

    @SuppressWarnings("unchecked")
    protected CmisAllowableActionsType determineObjectAllowableActions(Object objectIdentifier) throws CmisException
    {
        CMISTypeDefinition typeDef;
        try
        {
            if (objectIdentifier instanceof AssociationRef)
            {
                typeDef = cmisService.getTypeDefinition((AssociationRef) objectIdentifier);
            }
            else
            {
                if (objectIdentifier instanceof Version)
                {
                    objectIdentifier = ((Version) objectIdentifier).getFrozenStateNodeRef();
                }
                typeDef = cmisService.getTypeDefinition((NodeRef) objectIdentifier);
            }
        }
        catch (CMISInvalidArgumentException e)
        {
            throw ExceptionUtil.createCmisException(e);
        }
        CmisAllowableActionsType result = new CmisAllowableActionsType();
        for (Entry<CMISAllowedActionEnum, CMISActionEvaluator<? extends Object>> entry : typeDef.getActionEvaluators().entrySet())
        {
            PropertyDescriptor propertyDescriptor = ALLOWED_ACTION_ENUM_MAPPING.get(entry.getKey());
            if (propertyDescriptor != null)
            {
                // Let's assume that the evaluator will accept the object
                try
                {
                    propertyDescriptor.getWriteMethod().invoke(result, new Boolean(((CMISActionEvaluator) entry.getValue()).isAllowed(objectIdentifier)));
                }
                catch (IllegalArgumentException e)
                {
                    throw new AlfrescoRuntimeException("Exception setting allowable actions", e);
                }
                catch (IllegalAccessException e)
                {
                    throw new AlfrescoRuntimeException("Exception setting allowable actions", e);
                }
                catch (InvocationTargetException e)
                {
                    throw new AlfrescoRuntimeException("Exception setting allowable actions", e.getTargetException());
                }
            }
        }
        return result;
    }
}
