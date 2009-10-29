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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBElement;

import org.alfresco.cmis.CMISDictionaryService;
import org.alfresco.cmis.CMISQueryService;
import org.alfresco.cmis.CMISServices;
import org.alfresco.cmis.CMISTypeDefinition;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.cmis.PropertyFilter;
import org.alfresco.repo.cmis.ws.utils.CmisObjectsUtils;
import org.alfresco.repo.cmis.ws.utils.PropertyUtil;
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
 */
public class DMAbstractServicePort
{
    protected static final String INITIAL_VERSION_DESCRIPTION = "Initial version";

    private static final String INVALID_REPOSITORY_ID_MESSAGE = "Invalid repository id";
    private static final String INVALID_FOLDER_OBJECT_ID_MESSAGE = "OID for non-existent object or not folder object";

    private Paging paging = new Paging();

    protected ObjectFactory cmisObjectFactory = new ObjectFactory();
    protected CMISDictionaryService cmisDictionaryService;
    protected CMISQueryService cmisQueryService;
    protected CMISServices cmisService;
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
    protected void createCmisObjectList(PropertyFilter filter, boolean includeAllowableActions, List<NodeRef> sourceList, List<CmisObjectType> resultList) throws CmisException
    {
        for (NodeRef objectNodeRef : sourceList)
        {
            resultList.add(createCmisObject(objectNodeRef, filter, includeAllowableActions));
        }
    }

    /**
     * This method creates and configures CMIS object against appropriate Alfresco object (NodeRef or AssociationRef)
     * 
     * @param objectNodeRef the Alfresco object against those conversion must to be done
     * @param filter accepted properties filter
     * @return converted to CMIS object Alfresco object
     */
    protected CmisObjectType createCmisObject(Object identifier, PropertyFilter filter, boolean includeAllowableActions) throws CmisException
    {
        CmisObjectType result = new CmisObjectType();
        result.setProperties(propertiesUtil.getPropertiesType(identifier.toString(), filter));
        if (includeAllowableActions)
        {
            result.setAllowableActions(determineObjectAllowableActions(identifier));
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

    protected CmisAllowableActionsType determineObjectAllowableActions(Object objectIdentifier) throws CmisException
    {
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
