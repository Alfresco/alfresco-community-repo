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
@javax.jws.WebService(name = "VersioningServicePort", serviceName = "VersioningService", portName = "VersioningServicePort", targetNamespace = "http://docs.oasis-open.org/ns/cmis/ws/200901", endpointInterface = "org.alfresco.repo.cmis.ws.VersioningServicePort")
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
     * @param documentId document Id
     * @throws CmisException (with following {@link EnumServiceException} : INVALID_ARGUMENT, OBJECT_NOT_FOUND, NOT_SUPPORTED, PERMISSION_DENIED, RUNTIME, CONSTRAINT,
     *         UPDATE_CONFLICT, VERSIONING)
     */
    public void cancelCheckOut(String repositoryId, String documentId) throws CmisException
    {
        checkRepositoryId(repositoryId);
        NodeRef workingCopyNodeRef = cmisObjectsUtils.getIdentifierInstance(documentId, AlfrescoObjectType.DOCUMENT_OBJECT);
        assertVersionableIsTrue(workingCopyNodeRef);
        assertLatestVersion(workingCopyNodeRef, true);
        checkOutCheckInService.cancelCheckout(workingCopyNodeRef);
    }

    private void assertVersionableIsTrue(NodeRef workingCopyNodeRef) throws CmisException
    {
        if (!getTypeDefinition(workingCopyNodeRef).isVersionable())
        {
            // TODO: uncomment this when CMIS dictionary model will be corrected
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
     * @param documentId document Id
     * @param major is major True (Default)
     * @param properties CMIS properties
     * @param contentStream content stream
     * @param checkinComment check in comment
     * @throws CmisException (with following {@link EnumServiceException} : INVALID_ARGUMENT, OBJECT_NOT_FOUND, NOT_SUPPORTED, PERMISSION_DENIED, RUNTIME, CONSTRAINT, STORAGE,
     *         STREAM_NOT_SUPPORTED, UPDATE_CONFLICT, VERSIONING)
     */
    public void checkIn(String repositoryId, Holder<String> documentId, Boolean major, CmisPropertiesType properties, CmisContentStreamType contentStream, String checkinComment,
            List<String> applyPolicies, CmisAccessControlListType addACEs, CmisAccessControlListType removeACEs) throws CmisException
    {
        checkRepositoryId(repositoryId);
        NodeRef workingCopyNodeRef = cmisObjectsUtils.getIdentifierInstance(documentId.value, AlfrescoObjectType.DOCUMENT_OBJECT);
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
                throw cmisObjectsUtils.createCmisException("Exception while updating content stream", EnumServiceException.RUNTIME, e);
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
        // TODO: applyPolicies, addACEs, removeACEs
        documentId.value = propertiesUtil.getProperty(nodeRef, CMISDictionaryModel.PROP_OBJECT_ID, documentId.value);
    }

    /**
     * Create a private working copy of the object, copies the metadata and optionally content.
     * 
     * @param repositoryId repository Id
     * @param documentId ObjectID of document version to checkout
     * @param contentCopied
     * @return ObjectID of private working copy as documentId; True if succeed, False otherwise as contentCopied
     * @throws CmisException (with following {@link EnumServiceException} : INVALID_ARGUMENT, OBJECT_NOT_FOUND, NOT_SUPPORTED, PERMISSION_DENIED, RUNTIME, CONSTRAINT, STORAGE,
     *         UPDATE_CONFLICT, VERSIONING)
     */
    public void checkOut(String repositoryId, Holder<String> documentId, Holder<Boolean> contentCopied) throws CmisException
    {
        checkRepositoryId(repositoryId);
        NodeRef documentNodeRef = cmisObjectsUtils.getIdentifierInstance(documentId.value, AlfrescoObjectType.DOCUMENT_OBJECT);
        assertVersionableIsTrue(documentNodeRef);
        assertLatestVersion(documentNodeRef, false);

        LockStatus lockStatus = lockService.getLockStatus(documentNodeRef);
        if (lockStatus.equals(LockStatus.LOCKED) || lockStatus.equals(LockStatus.LOCK_OWNER) || nodeService.hasAspect(documentNodeRef, ContentModel.ASPECT_WORKING_COPY))
        {
            throw cmisObjectsUtils.createCmisException("Object is already checked out", EnumServiceException.UPDATE_CONFLICT);
        }

        try
        {
            NodeRef pwcNodeRef = checkoutNode(documentNodeRef);
            documentId.value = propertiesUtil.getProperty(pwcNodeRef, CMISDictionaryModel.PROP_OBJECT_ID, documentId.value);
            contentCopied.value = null != nodeService.getProperty(pwcNodeRef, ContentModel.PROP_CONTENT);
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
    public List<CmisObjectType> getAllVersions(String repositoryId, String versionSeriesId, String filter, Boolean includeAllowableActions,
            EnumIncludeRelationships includeRelationships) throws CmisException
    {
        checkRepositoryId(repositoryId);

        NodeRef documentNodeRef = cmisObjectsUtils.getIdentifierInstance(versionSeriesId, AlfrescoObjectType.DOCUMENT_OBJECT);
        documentNodeRef = cmisObjectsUtils.getLatestNode(documentNodeRef, false);
        PropertyFilter propertyFilter = createPropertyFilter(filter);

        List<CmisObjectType> objects = new LinkedList<CmisObjectType>();

        try
        {
            NodeRef workingCopyNodeReference = cmisObjectsUtils.isWorkingCopy(documentNodeRef) ? documentNodeRef : checkOutCheckInService.getWorkingCopy(documentNodeRef);
            if (null != workingCopyNodeReference)
            {
                objects.add(createCmisObject(workingCopyNodeReference, propertyFilter));
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
                objects.add(createCmisObject(version.getFrozenStateNodeRef(), propertyFilter));
            }
        }
        // TODO: includeAllowableActions, includeRelationships

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
    public CmisObjectType getPropertiesOfLatestVersion(String repositoryId, String versionSeriesId, boolean major, String filter, Boolean includeACL) throws CmisException
    {
        checkRepositoryId(repositoryId);
        PropertyFilter propertyFilter = createPropertyFilter(filter);

        NodeRef documentNodeRef = cmisObjectsUtils.getIdentifierInstance(versionSeriesId, AlfrescoObjectType.DOCUMENT_OBJECT);
        NodeRef latestVersionNodeRef = cmisObjectsUtils.getLatestNode(documentNodeRef, major);

        Boolean majorVersionProperty = propertiesUtil.getProperty(latestVersionNodeRef, CMISDictionaryModel.PROP_IS_MAJOR_VERSION, false);
        if (major && !majorVersionProperty)
        {
            throw cmisObjectsUtils.createCmisException("Object that was specified has no latest major version", EnumServiceException.OBJECT_NOT_FOUND);
        }

        CmisObjectType result = new CmisObjectType();
        result.setProperties(propertiesUtil.getPropertiesType(latestVersionNodeRef.toString(), propertyFilter));
        // TODO: includeACL

        return result;
    }

    private void assertLatestVersion(NodeRef nodeRef, boolean shouldBePwc) throws CmisException
    {
        if (shouldBePwc)
        {
            if (!cmisObjectsUtils.isWorkingCopy(nodeRef))
            {
                throw cmisObjectsUtils.createCmisException("Object isn't checked out", EnumServiceException.UPDATE_CONFLICT);
            }
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
}
