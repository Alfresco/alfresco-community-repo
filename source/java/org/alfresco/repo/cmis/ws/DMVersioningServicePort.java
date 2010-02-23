/*
 * Copyright (C) 2005-2008 Alfresco Software Limited.
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

import java.util.LinkedList;
import java.util.List;

import javax.xml.ws.Holder;

import org.alfresco.cmis.CMISContentStreamAllowedEnum;
import org.alfresco.cmis.CMISDictionaryModel;
import org.alfresco.cmis.CMISTypeDefinition;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.cmis.PropertyFilter;
import org.alfresco.repo.cmis.ws.utils.AlfrescoObjectType;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.lock.LockStatus;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionHistory;
import org.alfresco.service.cmr.version.VersionType;

/**
 * Port for versioning service.
 * 
 * @author Dmitry Lazurkin
 * @author Dmitry Velichkevich
 */
@javax.jws.WebService(name = "VersioningServicePort", serviceName = "VersioningService", portName = "VersioningServicePort", targetNamespace = "http://docs.oasis-open.org/ns/cmis/ws/200908/", endpointInterface = "org.alfresco.repo.cmis.ws.VersioningServicePort")
public class DMVersioningServicePort extends DMAbstractServicePort implements VersioningServicePort
{
    private LockService lockService;

    public void setLockService(LockService lockService)
    {
        this.lockService = lockService;
    }

    /**
     * Reverses the effect of a check-out. Removes the private working copy of the checked-out document object, allowing other documents in the version series to be checked out
     * again.
     * 
     * @param repositoryId repository Id
     * @param objectId document Id
     * @throws CmisException (with following {@link EnumServiceException} : INVALID_ARGUMENT, OBJECT_NOT_FOUND, NOT_SUPPORTED, PERMISSION_DENIED, RUNTIME, CONSTRAINT,
     *         UPDATE_CONFLICT, VERSIONING)
     */
    // FIXME [~BUG]: may it is better returning id of the unchecked out document
    public void cancelCheckOut(String repositoryId, String objectId, Holder<CmisExtensionType> extension) throws CmisException
    {
        checkRepositoryId(repositoryId);
        NodeRef documentNodeRef = cmisObjectsUtils.getIdentifierInstance(objectId, AlfrescoObjectType.DOCUMENT_OBJECT);
        assertVersionableIsTrue(documentNodeRef);
        boolean checkedOut = propertiesUtil.getProperty(documentNodeRef, CMISDictionaryModel.PROP_IS_VERSION_SERIES_CHECKED_OUT, false);
        NodeRef workingCopyNodeRef = definitelyGetWorkingCopy(checkedOut, documentNodeRef);
        checkOutCheckInService.cancelCheckout(workingCopyNodeRef);
    }

    private void assertVersionableIsTrue(NodeRef workingCopyNodeRef) throws CmisException
    {
        if (!getTypeDefinition(workingCopyNodeRef).isVersionable())
        {
            // FIXME: uncomment this when CMIS dictionary model will be corrected
            // throw cmisObjectsUtils.createCmisException("Document that was specified is not versionable", EnumServiceException.CONSTRAINT);
        }
    }

    private CMISTypeDefinition getTypeDefinition(NodeRef nodeRef) throws CmisException
    {
        String typeId = propertiesUtil.getProperty(nodeRef, CMISDictionaryModel.PROP_OBJECT_TYPE_ID, null);
        CMISTypeDefinition typeDefinition = (null != typeId) ? (cmisDictionaryService.findType(typeId)) : (null);
        if (null == typeDefinition)
        {
            throw cmisObjectsUtils.createCmisException(("Object type property is invalid"), EnumServiceException.RUNTIME);
        }
        return typeDefinition;
    }

    /**
     * Makes the private working copy the current version of the document.
     * 
     * @param repositoryId repository Id
     * @param objectId document Id
     * @param major is major True (Default)
     * @param properties CMIS properties
     * @param contentStream content stream
     * @param checkinComment check in comment
     * @throws CmisException (with following {@link EnumServiceException} : INVALID_ARGUMENT, OBJECT_NOT_FOUND, NOT_SUPPORTED, PERMISSION_DENIED, RUNTIME, CONSTRAINT, STORAGE,
     *         STREAM_NOT_SUPPORTED, UPDATE_CONFLICT, VERSIONING)
     */
    // FIXME [~BUG]: it is better changing 'void' to 'PWC Id' result type
    public void checkIn(String repositoryId, Holder<String> objectId, Boolean major, CmisPropertiesType properties, CmisContentStreamType contentStream, String checkinComment,
            List<String> policies, CmisAccessControlListType addACEs, CmisAccessControlListType removeACEs, Holder<CmisExtensionType> extension) throws CmisException
    {
        checkRepositoryId(repositoryId);
        NodeRef workingCopyNodeRef = cmisObjectsUtils.getIdentifierInstance(objectId.value, AlfrescoObjectType.DOCUMENT_OBJECT);
        assertVersionableIsTrue(workingCopyNodeRef);
        assertLatestVersion(workingCopyNodeRef, true);

        String versionSeriesId = propertiesUtil.getProperty(workingCopyNodeRef, CMISDictionaryModel.PROP_VERSION_SERIES_ID, null);
        CMISTypeDefinition seriesObjectTypeDefinition = getTypeDefinition((NodeRef) cmisObjectsUtils.getIdentifierInstance(versionSeriesId, AlfrescoObjectType.DOCUMENT_OBJECT));

        if ((null != contentStream) && (CMISContentStreamAllowedEnum.NOT_ALLOWED == seriesObjectTypeDefinition.getContentStreamAllowed()))
        {
            throw cmisObjectsUtils.createCmisException("Content stream is not allowed", EnumServiceException.STREAM_NOT_SUPPORTED);
        }

        if (contentStream != null)
        {
            try
            {
                ContentWriter writer = fileFolderService.getWriter(workingCopyNodeRef);
                writer.setMimetype(contentStream.getMimeType());
                writer.putContent(contentStream.getStream().getInputStream());
            }
            catch (Exception e)
            {
                throw cmisObjectsUtils.createCmisException("Exception while updating content stream", EnumServiceException.UPDATE_CONFLICT, e);
            }
        }

        NodeRef nodeRef;
        try
        {
            nodeRef = checkOutCheckInService.checkin(workingCopyNodeRef, createVersionProperties(checkinComment, ((null == major) || major) ? (VersionType.MAJOR)
                    : (VersionType.MINOR)));
            propertiesUtil.setProperties(nodeRef, properties, null);
        }
        catch (Exception e)
        {
            throw cmisObjectsUtils.createCmisException("Unable to check in Private Working Copy object that was specified", EnumServiceException.STORAGE, e);
        }
        // TODO: applyPolicies
        applyAclCarefully(nodeRef, addACEs, removeACEs, EnumACLPropagation.PROPAGATE);
        objectId.value = propertiesUtil.getProperty(nodeRef, CMISDictionaryModel.PROP_OBJECT_ID, objectId.value);
    }

    /**
     * Create a private working copy of the object, copies the metadata and optionally content.
     * 
     * @param repositoryId repository Id
     * @param objectId ObjectID of document version to checkout
     * @param contentCopied
     * @return ObjectID of private working copy as documentId; True if succeed, False otherwise as contentCopied
     * @throws CmisException (with following {@link EnumServiceException} : INVALID_ARGUMENT, OBJECT_NOT_FOUND, NOT_SUPPORTED, PERMISSION_DENIED, RUNTIME, CONSTRAINT, STORAGE,
     *         UPDATE_CONFLICT, VERSIONING)
     */
    public void checkOut(String repositoryId, Holder<String> objectId, Holder<CmisExtensionType> extension, Holder<Boolean> contentCopied) throws CmisException
    {
        checkRepositoryId(repositoryId);
        NodeRef documentNodeRef = cmisObjectsUtils.getIdentifierInstance(objectId.value, AlfrescoObjectType.DOCUMENT_OBJECT);
        assertVersionableIsTrue(documentNodeRef);
        assertLatestVersion(documentNodeRef, false);

        LockStatus lockStatus = lockService.getLockStatus(documentNodeRef);
        if (lockStatus.equals(LockStatus.LOCKED) || lockStatus.equals(LockStatus.LOCK_OWNER) || nodeService.hasAspect(documentNodeRef, ContentModel.ASPECT_WORKING_COPY))
        {
            throw cmisObjectsUtils.createCmisException("Object is locked or already checked out", EnumServiceException.UPDATE_CONFLICT);
        }

        try
        {
            NodeRef pwcNodeRef = checkoutNode(documentNodeRef);
            objectId.value = propertiesUtil.getProperty(pwcNodeRef, CMISDictionaryModel.PROP_OBJECT_ID, objectId.value);
            contentCopied.value = null != fileFolderService.getReader(pwcNodeRef);
        }
        catch (Exception e)
        {
            throw cmisObjectsUtils.createCmisException(("Unable to execute Check Out services. Cause message: " + e.toString()), EnumServiceException.STORAGE);
        }
    }

    /**
     * Deletes all document versions in the specified version series.
     * 
     * @param repositoryId repository Id
     * @param versionSeriesId version series Id
     * @throws CmisException (with following {@link EnumServiceException} : INVALID_ARGUMENT, OBJECT_NOT_FOUND, NOT_SUPPORTED, PERMISSION_DENIED, RUNTIME)
     */
    public void deleteAllVersions(String repositoryId, String versionSeriesId) throws CmisException
    {
        checkRepositoryId(repositoryId);
        NodeRef documentNodeRef = cmisObjectsUtils.getIdentifierInstance(versionSeriesId, AlfrescoObjectType.DOCUMENT_OBJECT);
        NodeRef workingCopyRef = (cmisObjectsUtils.isWorkingCopy(documentNodeRef)) ? (documentNodeRef) : (checkOutCheckInService.getWorkingCopy(documentNodeRef));
        if ((null != workingCopyRef) && cmisObjectsUtils.isWorkingCopy(workingCopyRef))
        {
            documentNodeRef = checkOutCheckInService.cancelCheckout(workingCopyRef);
        }

        versionService.deleteVersionHistory(documentNodeRef);
    }

    /**
     * Gets the list of all document versions for the specified version series.
     * 
     * @param parameters repositoryId: repository Id; versionSeriesId: version series Id; filter: property filter; includeAllowableActions; includeRelationships;
     * @return list of CmisObjectType
     * @throws CmisException (with following {@link EnumServiceException} : INVALID_ARGUMENT, OBJECT_NOT_FOUND, NOT_SUPPORTED, PERMISSION_DENIED, RUNTIME, FILTER_NOT_VALID)
     */
    public List<CmisObjectType> getAllVersions(String repositoryId, String objectId, String filter, Boolean includeAllowableActions, CmisExtensionType extension)
            throws CmisException
    {
        checkRepositoryId(repositoryId);
        NodeRef documentNodeRef = cmisObjectsUtils.getIdentifierInstance(objectId, AlfrescoObjectType.DOCUMENT_OBJECT);
        documentNodeRef = cmisObjectsUtils.getLatestNode(documentNodeRef, false);
        PropertyFilter propertyFilter = createPropertyFilter(filter);
        List<CmisObjectType> objects = new LinkedList<CmisObjectType>();
        includeAllowableActions = (null == includeAllowableActions) ? (false) : (includeAllowableActions);

        try
        {
            NodeRef workingCopyNodeReference = cmisObjectsUtils.isWorkingCopy(documentNodeRef) ? documentNodeRef : checkOutCheckInService.getWorkingCopy(documentNodeRef);
            if (null != workingCopyNodeReference)
            {
                objects.add(createCmisObject(workingCopyNodeReference, propertyFilter, includeAllowableActions, null));
            }
        }
        catch (Exception e)
        {
            if (!(e instanceof AccessDeniedException))
            {
                throw cmisObjectsUtils.createCmisException(e.toString(), EnumServiceException.RUNTIME);
            }
        }

        VersionHistory versionHistory = versionService.getVersionHistory(documentNodeRef);
        if (null != versionHistory)
        {
            for (Version version = versionService.getCurrentVersion(documentNodeRef); null != version; version = versionHistory.getPredecessor(version))
            {
                objects.add(createCmisObject(version.getFrozenStateNodeRef(), propertyFilter, includeAllowableActions, null));
            }
        }
        // TODO: includeRelationships

        return objects;
    }

    /**
     * Gets the properties of the latest version, or the latest major version, of the specified version series.
     * 
     * @param parameters repositoryId: repository Id; versionSeriesId: version series Id; majorVersion: whether or not to return the latest major version. Default=FALSE; filter:
     *        property filter
     * @return CmisObjectType with properties
     * @throws CmisException (with following {@link EnumServiceException} : INVALID_ARGUMENT, OBJECT_NOT_FOUND, NOT_SUPPORTED, PERMISSION_DENIED, RUNTIME, FILTER_NOT_VALID)
     */
    public CmisPropertiesType getPropertiesOfLatestVersion(String repositoryId, String objectId, Boolean major, String filter, CmisExtensionType extension) throws CmisException
    {
        checkRepositoryId(repositoryId);
        NodeRef documentNodeRef = cmisObjectsUtils.getIdentifierInstance(objectId, AlfrescoObjectType.DOCUMENT_OBJECT);
        PropertyFilter propertyFilter = createPropertyFilter(filter);
        major = (null == major) ? (false) : (major);
        NodeRef latestVersionNodeRef = getAndCheckLatestNodeRef(documentNodeRef, major);
        return propertiesUtil.getPropertiesType(latestVersionNodeRef.toString(), propertyFilter);
    }

    private void assertLatestVersion(NodeRef nodeRef, boolean mustBePwc) throws CmisException
    {
        if (mustBePwc)
        {
            boolean checkedOut = propertiesUtil.getProperty(nodeRef, CMISDictionaryModel.PROP_IS_VERSION_SERIES_CHECKED_OUT, false);
            definitelyGetWorkingCopy(checkedOut, nodeRef);
        }
        else
        {
            Boolean latestVersion = propertiesUtil.getProperty(nodeRef, CMISDictionaryModel.PROP_IS_LATEST_VERSION, false);
            if (!latestVersion && !cmisObjectsUtils.isWorkingCopy(nodeRef))
            {
                throw cmisObjectsUtils.createCmisException("Operation can be executed only on the latest document version", EnumServiceException.VERSIONING);
            }
        }
    }

    private NodeRef definitelyGetWorkingCopy(boolean checkedOut, NodeRef nodeRef) throws CmisException
    {
        NodeRef workingCopy = null;
        String workingCopyId = propertiesUtil.getProperty(nodeRef, CMISDictionaryModel.PROP_VERSION_SERIES_CHECKED_OUT_ID, null);
        if (checkedOut && (null != workingCopyId) && !"".equals(workingCopyId))
        {
            workingCopy = cmisObjectsUtils.getIdentifierInstance(workingCopyId, AlfrescoObjectType.DOCUMENT_OBJECT);
        }
        if ((null == workingCopy) || !cmisObjectsUtils.isWorkingCopy(workingCopy))
        {
            throw cmisObjectsUtils.createCmisException("Object isn't checked out", EnumServiceException.UPDATE_CONFLICT);
        }
        return workingCopy;
    }

    /**
     * 
     */
    // TODO: it is necessary to add tests for this method
    public CmisObjectType getObjectOfLatestVersion(String repositoryId, String objectId, Boolean major, String filter, Boolean includeAllowableActions,
            EnumIncludeRelationships includeRelationships, String renditionFilter, Boolean includePolicyIds, Boolean includeACL, CmisExtensionType extension) throws CmisException
    {
        checkRepositoryId(repositoryId);
        NodeRef documentNodeRef = cmisObjectsUtils.getIdentifierInstance(objectId, AlfrescoObjectType.DOCUMENT_OBJECT);
        PropertyFilter propertyFilter = createPropertyFilter(filter);
        includeAllowableActions = (null == includeAllowableActions) ? (false) : (includeAllowableActions);
        major = (null == major) ? (false) : (major);
        NodeRef latestVersionNodeRef = getAndCheckLatestNodeRef(documentNodeRef, major);
        // TODO: includeRelationships
        // TODO: includePolicyIds
        CmisObjectType result = createCmisObject(latestVersionNodeRef.toString(), propertyFilter, includeAllowableActions, renditionFilter);
        if (includeACL)
        {
            appendWithAce(documentNodeRef, result);
        }
        return result;
    }

    private NodeRef getAndCheckLatestNodeRef(NodeRef documentNodeRef, Boolean major) throws CmisException
    {
        NodeRef latestVersionNodeRef = cmisObjectsUtils.getLatestNode(documentNodeRef, major);
        Boolean majorVersionProperty = propertiesUtil.getProperty(latestVersionNodeRef, CMISDictionaryModel.PROP_IS_MAJOR_VERSION, false);
        if (major && !majorVersionProperty)
        {
            throw cmisObjectsUtils.createCmisException("Object that was specified has no latest major version", EnumServiceException.OBJECT_NOT_FOUND);
        }
        return latestVersionNodeRef;
    }
}
