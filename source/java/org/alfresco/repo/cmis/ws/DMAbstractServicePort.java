/*
 * Copyright (C) 2005-2009 Alfresco Software Limited.
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
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.cmis.ws;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBElement;
import javax.xml.ws.Holder;

import org.alfresco.cmis.CMISAccessControlEntry;
import org.alfresco.cmis.CMISAccessControlFormatEnum;
import org.alfresco.cmis.CMISAccessControlReport;
import org.alfresco.cmis.CMISAccessControlService;
import org.alfresco.cmis.CMISAclPropagationEnum;
import org.alfresco.cmis.CMISChangeLogService;
import org.alfresco.cmis.CMISDictionaryModel;
import org.alfresco.cmis.CMISDictionaryService;
import org.alfresco.cmis.CMISQueryService;
import org.alfresco.cmis.CMISRendition;
import org.alfresco.cmis.CMISRenditionService;
import org.alfresco.cmis.CMISServices;
import org.alfresco.cmis.CMISTypeDefinition;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.cmis.PropertyFilter;
import org.alfresco.repo.cmis.ws.utils.AlfrescoObjectType;
import org.alfresco.repo.cmis.ws.utils.CmisObjectsUtils;
import org.alfresco.repo.cmis.ws.utils.PropertyUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.version.VersionModel;
import org.alfresco.repo.web.util.paging.Cursor;
import org.alfresco.repo.web.util.paging.Page;
import org.alfresco.repo.web.util.paging.Paging;
import org.alfresco.service.cmr.coci.CheckOutCheckInService;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionService;
import org.alfresco.service.cmr.version.VersionType;
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
    protected static final String INITIAL_VERSION_DESCRIPTION = "Initial version";

    private static final String INVALID_REPOSITORY_ID_MESSAGE = "Invalid repository id";
    private static final String INVALID_FOLDER_OBJECT_ID_MESSAGE = "OID for non-existent object or not folder object";

    private static final Map<EnumACLPropagation, CMISAclPropagationEnum> ACL_PROPAGATION_ENUM_MAPPGIN;
    static
    {
        ACL_PROPAGATION_ENUM_MAPPGIN = new HashMap<EnumACLPropagation, CMISAclPropagationEnum>();
        ACL_PROPAGATION_ENUM_MAPPGIN.put(EnumACLPropagation.OBJECTONLY, CMISAclPropagationEnum.OBJECT_ONLY);
        ACL_PROPAGATION_ENUM_MAPPGIN.put(EnumACLPropagation.PROPAGATE, CMISAclPropagationEnum.PROPAGATE);
        ACL_PROPAGATION_ENUM_MAPPGIN.put(EnumACLPropagation.REPOSITORYDETERMINED, CMISAclPropagationEnum.REPOSITORY_DETERMINED);
    }

    private Paging paging = new Paging();

    protected ObjectFactory cmisObjectFactory = new ObjectFactory();
    protected CMISDictionaryService cmisDictionaryService;
    protected CMISQueryService cmisQueryService;
    protected CMISServices cmisService;
    protected CMISChangeLogService cmisChangeLogService;
    protected CMISRenditionService cmisRenditionService;
    protected CMISAccessControlService cmisAclService;

    protected DescriptorService descriptorService;
    protected NodeService nodeService;
    protected VersionService versionService;
    protected FileFolderService fileFolderService;
    protected CheckOutCheckInService checkOutCheckInService;
    protected SearchService searchService;
    protected CmisObjectsUtils cmisObjectsUtils;
    protected PropertyUtil propertiesUtil;
    protected PermissionService permissionService;

    public void setCmisService(CMISServices cmisService)
    {
        this.cmisService = cmisService;
    }

    public void setCmisDictionaryService(CMISDictionaryService cmisDictionaryService)
    {
        this.cmisDictionaryService = cmisDictionaryService;
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

    public void setVersionService(VersionService versionService)
    {
        this.versionService = versionService;
    }

    public void setFileFolderService(FileFolderService fileFolderService)
    {
        this.fileFolderService = fileFolderService;
    }

    public void setCheckOutCheckInService(CheckOutCheckInService checkOutCheckInService)
    {

        this.checkOutCheckInService = checkOutCheckInService;
    }

    public void setCmisObjectsUtils(CmisObjectsUtils cmisObjectsUtils)
    {
        this.cmisObjectsUtils = cmisObjectsUtils;
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

    protected PropertyFilter createPropertyFilter(String filter) throws CmisException
    {
        return (filter == null) ? (new PropertyFilter()) : (new PropertyFilter(filter, cmisObjectsUtils));
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
     * This method converts Alfresco's <b>NodeRef</b>'s to CMIS objects those will be stored in <b>resultList</b>-parameter. Properties for returning filtering also performs
     * 
     * @param filter properties filter value for filtering objects returning properties
     * @param sourceList the list that contains all returning Node References
     * @param resultList the list of <b>CmisObjectType</b> values for end response result collecting
     * @throws CmisException
     */
    protected void createCmisObjectList(PropertyFilter filter, boolean includeAllowableActions, String renditionFilter, List<NodeRef> sourceList, List<CmisObjectType> resultList)
            throws CmisException
    {
        for (NodeRef objectNodeRef : sourceList)
        {
            resultList.add(createCmisObject(objectNodeRef, filter, includeAllowableActions, renditionFilter));
        }
    }

    /**
     * This method creates and configures CMIS object against appropriate Alfresco object (NodeRef or AssociationRef)
     * 
     * @param objectNodeRef the Alfresco object against those conversion must to be done
     * @param filter accepted properties filter
     * @return converted to CMIS object Alfresco object
     */
    protected CmisObjectType createCmisObject(Object identifier, PropertyFilter filter, boolean includeAllowableActions, String renditionFilter) throws CmisException
    {
        CmisObjectType result = new CmisObjectType();
        result.setProperties(propertiesUtil.getPropertiesType(identifier.toString(), filter));
        if (includeAllowableActions)
        {
            result.setAllowableActions(determineObjectAllowableActions(identifier));
        }
        if (renditionFilter != null)
        {
            List<CmisRenditionType> renditions = getRenditions(identifier, renditionFilter);
            if (renditions != null && !renditions.isEmpty())
            {
                result.getRendition().addAll(renditions);
            }
        }
        return result;
    }

    /**
     * Asserts "Folder with folderNodeRef exists"
     * 
     * @param folderNodeRef node reference
     * @throws FolderNotValidException folderNodeRef doesn't exist or folderNodeRef isn't for folder object
     */
    protected void assertExistFolder(NodeRef folderNodeRef) throws CmisException
    {
        if (!this.cmisObjectsUtils.isFolder(folderNodeRef))
        {
            throw new CmisException(INVALID_FOLDER_OBJECT_ID_MESSAGE, cmisObjectsUtils.createCmisException(INVALID_FOLDER_OBJECT_ID_MESSAGE, EnumServiceException.INVALID_ARGUMENT));
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
            throw cmisObjectsUtils.createCmisException(INVALID_REPOSITORY_ID_MESSAGE, EnumServiceException.INVALID_ARGUMENT);
        }
    }

    protected Map<String, Serializable> createVersionProperties(String versionDescription, VersionType versionType)
    {
        Map<String, Serializable> result = new HashMap<String, Serializable>();
        result.put(Version.PROP_DESCRIPTION, versionDescription);
        result.put(VersionModel.PROP_VERSION_TYPE, versionType);
        return result;
    }

    protected NodeRef checkoutNode(NodeRef documentNodeReference)
    {
        if (!this.nodeService.hasAspect(documentNodeReference, ContentModel.ASPECT_VERSIONABLE))
        {
            this.versionService.createVersion(documentNodeReference, createVersionProperties(INITIAL_VERSION_DESCRIPTION, VersionType.MAJOR));
        }
        return checkOutCheckInService.checkout(documentNodeReference);
    }

    protected CMISTypeDefinition getCmisTypeDefinition(String typeId) throws CmisException
    {
        try
        {
            return cmisDictionaryService.findType(typeId);
        }
        catch (Exception e)
        {
            throw new CmisException(("Invalid typeId " + typeId), cmisObjectsUtils.createCmisException(("Invalid typeId " + typeId), EnumServiceException.INVALID_ARGUMENT));
        }
    }

    protected List<CmisRenditionType> getRenditions(Object objectId, String renditionFilter) throws CmisException
    {
        List<CmisRenditionType> result = null;
        if (NodeRef.isNodeRef(objectId.toString()))
        {
            NodeRef document = new NodeRef(objectId.toString());

            List<CMISRendition> renditions = null;
            try
            {
                renditions = cmisRenditionService.getRenditions(document, renditionFilter);
            }
            catch (Exception e)
            {
                throw cmisObjectsUtils.createCmisException("Invalid rendition filter", EnumServiceException.FILTER_NOT_VALID);
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
        cmisRenditionType.setKind(rendition.getKind().getLabel());
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
        object.setExactACL(!aclReport.isExtract());
    }

    protected CmisACLType applyAclCarefully(NodeRef object, CmisAccessControlListType addACEs, CmisAccessControlListType removeACEs, EnumACLPropagation aclPropagation)
            throws CmisException
    {
        if (addACEs == null && removeACEs == null)
        {
            return null;
        }
        String typeId = propertiesUtil.getProperty(object, CMISDictionaryModel.PROP_OBJECT_TYPE_ID, null);
        CMISTypeDefinition objectType = (null == typeId) ? (null) : (cmisDictionaryService.findType(typeId));
        if (null == objectType)
        {
            throw cmisObjectsUtils.createCmisException("Type Definition for specified Object was not found", EnumServiceException.STORAGE);
        }
        if (!objectType.isControllableACL())
        {
            throw cmisObjectsUtils.createCmisException("Object that was specified is not ACL Controllable", EnumServiceException.CONSTRAINT);
        }
        CMISAclPropagationEnum propagation = (null == aclPropagation) ? (CMISAclPropagationEnum.PROPAGATE) : (ACL_PROPAGATION_ENUM_MAPPGIN.get(aclPropagation));
        List<CMISAccessControlEntry> acesToAdd = (null == addACEs) ? (null) : (convertToAlfrescoAceEntriesList(addACEs.getPermission()));
        List<CMISAccessControlEntry> acesToRemove = (null == removeACEs) ? (null) : (convertToAlfrescoAceEntriesList(removeACEs.getPermission()));
        CMISAccessControlReport aclReport = null;
        try
        {
            aclReport = cmisAclService.applyAcl(object, acesToRemove, acesToAdd, propagation, CMISAccessControlFormatEnum.REPOSITORY_SPECIFIC_PERMISSIONS);
        }
        catch (Exception e)
        {
            throw cmisObjectsUtils.createCmisException(("Can't perform updating of Object Permissions. Error cause message: " + e.toString()), EnumServiceException.CONSTRAINT);
        }
        CmisACLType result = convertAclReportToCmisAclType(aclReport);
        return result;
    }

    private List<CMISAccessControlEntry> convertToAlfrescoAceEntriesList(List<CmisAccessControlEntryType> source)
    {
        List<CMISAccessControlEntry> result = new LinkedList<CMISAccessControlEntry>();
        for (CmisAccessControlEntryType cmisEntry : source)
        {
            result.add(convertToAlfrescoAceEntry(cmisEntry));
        }
        return result;
    }

    private CMISAccessControlEntry convertToAlfrescoAceEntry(final CmisAccessControlEntryType entry)
    {
        final Holder<String> correctPrincipalId = new Holder<String>(entry.getPrincipal().getPrincipalId());
        if ("cmis:user".equals(correctPrincipalId))
        {
            correctPrincipalId.value = AuthenticationUtil.getFullyAuthenticatedUser();
        }
        CMISAccessControlEntry result = new CMISAccessControlEntry() // FIXME: It is better to use already implemented class of this interface
        {
            private String principalId;
            private String permission; // FIXME: [BUG] It MUST BE a List of permissions!!!
            private boolean direct;
            {

                principalId = correctPrincipalId.value;
                permission = entry.getPermission().iterator().next(); // FIXME: See line 66
                direct = entry.isDirect();
            }

            public String getPrincipalId()
            {
                return principalId;
            }

            public String getPermission()
            {
                return permission;
            }

            public boolean getDirect()
            {
                return direct;
            }

            @Override
            public int hashCode()
            {
                final int prime = 31;
                int result = 1;
                result = prime * result + ((permission == null) ? 0 : permission.hashCode());
                result = prime * result + ((principalId == null) ? 0 : principalId.hashCode());
                return result;
            }

            @Override
            public boolean equals(Object obj)
            {
                if (this == obj)
                {
                    return true;
                }
                if (obj == null)
                {
                    return false;
                }
                if (getClass() != obj.getClass())
                {
                    return false;
                }
                final CMISAccessControlEntry other = (CMISAccessControlEntry) obj;
                if (permission == null)
                {
                    if (other.getPermission() != null)
                    {
                        return false;
                    }
                }
                else if (!permission.equals(other.getPermission()))
                {
                    return false;
                }
                if (principalId == null)
                {
                    if (other.getPrincipalId() != null)
                    {
                        return false;
                    }
                }
                else if (!principalId.equals(other.getPrincipalId()))
                {
                    return false;
                }
                return true;
            }
        };
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
        result.setExact(!aclReport.isExtract());
        return result;
    }

    protected CmisAllowableActionsType determineObjectAllowableActions(Object objectIdentifier) throws CmisException
    {
        if (objectIdentifier instanceof String)
        {
            objectIdentifier = cmisObjectsUtils.getIdentifierInstance(objectIdentifier.toString(), AlfrescoObjectType.ANY_OBJECT);
        }
        if (objectIdentifier instanceof AssociationRef)
        {
            return determineRelationshipAllowableActions((AssociationRef) objectIdentifier);
        }

        switch (cmisObjectsUtils.determineObjectType(objectIdentifier.toString()))
        {
        case CMIS_DOCUMENT:
        {
            return determineDocumentAllowableActions((NodeRef) objectIdentifier);
        }
        case CMIS_FOLDER:
        {
            return determineFolderAllowableActions((NodeRef) objectIdentifier);
        }
        }

        // TODO: determinePolicyAllowableActions() when Policy functionality is ready
        throw cmisObjectsUtils.createCmisException("It is impossible to get Allowable actions for the specified Object", EnumServiceException.NOT_SUPPORTED);
    }

    private CmisAllowableActionsType determineBaseAllowableActions(NodeRef objectNodeReference)
    {
        CmisAllowableActionsType result = new CmisAllowableActionsType();
        result.setCanGetProperties(this.permissionService.hasPermission(objectNodeReference, PermissionService.READ_PROPERTIES) == AccessStatus.ALLOWED);
        result.setCanUpdateProperties(this.permissionService.hasPermission(objectNodeReference, PermissionService.WRITE_PROPERTIES) == AccessStatus.ALLOWED);
        result.setCanDeleteObject(this.permissionService.hasPermission(objectNodeReference, PermissionService.DELETE) == AccessStatus.ALLOWED);

        // TODO: response.setCanAddPolicy(value);
        // TODO: response.setCanRemovePolicy(value);
        // TODO: response.setCanGetAppliedPolicies(value);

        return result;
    }

    private CmisAllowableActionsType determineDocumentAllowableActions(NodeRef objectNodeReference)
    {
        CmisAllowableActionsType result = determineBaseAllowableActions(objectNodeReference);
        determineCommonFolderDocumentAllowableActions(objectNodeReference, result);
        result.setCanGetObjectParents(this.permissionService.hasPermission(objectNodeReference, PermissionService.READ_ASSOCIATIONS) == AccessStatus.ALLOWED);
        result.setCanGetContentStream(this.permissionService.hasPermission(objectNodeReference, PermissionService.READ_CONTENT) == AccessStatus.ALLOWED);
        result.setCanSetContentStream(this.permissionService.hasPermission(objectNodeReference, PermissionService.WRITE_CONTENT) == AccessStatus.ALLOWED);
        result.setCanCheckOut(this.permissionService.hasPermission(objectNodeReference, PermissionService.CHECK_OUT) == AccessStatus.ALLOWED);
        result.setCanCheckIn(this.permissionService.hasPermission(objectNodeReference, PermissionService.CHECK_IN) == AccessStatus.ALLOWED);
        result.setCanCancelCheckOut(this.permissionService.hasPermission(objectNodeReference, PermissionService.CANCEL_CHECK_OUT) == AccessStatus.ALLOWED);
        result.setCanDeleteContentStream(result.isCanUpdateProperties() && result.isCanSetContentStream());
        return result;
    }

    private CmisAllowableActionsType determineFolderAllowableActions(NodeRef objectNodeReference)
    {
        CmisAllowableActionsType result = determineBaseAllowableActions(objectNodeReference);
        determineCommonFolderDocumentAllowableActions(objectNodeReference, result);

        result.setCanGetChildren(this.permissionService.hasPermission(objectNodeReference, PermissionService.READ_CHILDREN) == AccessStatus.ALLOWED);
        result.setCanCreateDocument(this.permissionService.hasPermission(objectNodeReference, PermissionService.ADD_CHILDREN) == AccessStatus.ALLOWED);
        result.setCanGetDescendants(result.isCanGetChildren() && (this.permissionService.hasPermission(objectNodeReference, PermissionService.READ) == AccessStatus.ALLOWED));
        result.setCanDeleteTree(this.permissionService.hasPermission(objectNodeReference, PermissionService.DELETE_CHILDREN) == AccessStatus.ALLOWED);
        result.setCanGetFolderParent(result.isCanGetObjectRelationships());
        result.setCanCreateFolder(result.isCanCreateDocument());
        // TODO: response.setCanCreatePolicy(value);
        return result;
    }

    private void determineCommonFolderDocumentAllowableActions(NodeRef objectNodeReference, CmisAllowableActionsType allowableActions)
    {
        allowableActions.setCanAddObjectToFolder(this.permissionService.hasPermission(objectNodeReference, PermissionService.CREATE_ASSOCIATIONS) == AccessStatus.ALLOWED);
        allowableActions.setCanGetObjectRelationships(this.permissionService.hasPermission(objectNodeReference, PermissionService.READ_ASSOCIATIONS) == AccessStatus.ALLOWED);
        allowableActions.setCanMoveObject(allowableActions.isCanUpdateProperties() && allowableActions.isCanAddObjectToFolder());
        allowableActions.setCanRemoveObjectFromFolder(allowableActions.isCanUpdateProperties());
        allowableActions.setCanCreateRelationship(allowableActions.isCanAddObjectToFolder());
    }

    private CmisAllowableActionsType determineRelationshipAllowableActions(AssociationRef association)
    {
        CmisAllowableActionsType result = new CmisAllowableActionsType();
        result.setCanDeleteObject(this.permissionService.hasPermission(association.getSourceRef(), PermissionService.DELETE_ASSOCIATIONS) == AccessStatus.ALLOWED);
        result.setCanGetObjectRelationships(this.permissionService.hasPermission(association.getSourceRef(), PermissionService.READ_ASSOCIATIONS) == AccessStatus.ALLOWED);
        return result;
    }
}
