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

import java.util.LinkedList;
import java.util.List;

import javax.xml.ws.Holder;

import org.alfresco.cmis.CMISContentStreamAllowedEnum;
import org.alfresco.cmis.CMISDictionaryModel;
import org.alfresco.cmis.CMISServiceException;
import org.alfresco.cmis.CMISTypeDefinition;
import org.alfresco.cmis.PropertyFilter;
import org.alfresco.repo.cmis.ws.utils.ExceptionUtil;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Port for versioning service.
 * 
 * @author Dmitry Lazurkin
 * @author Dmitry Velichkevich
 */
@javax.jws.WebService(name = "VersioningServicePort", serviceName = "VersioningService", portName = "VersioningServicePort", targetNamespace = "http://docs.oasis-open.org/ns/cmis/ws/200908/", endpointInterface = "org.alfresco.repo.cmis.ws.VersioningServicePort")
public class DMVersioningServicePort extends DMAbstractServicePort implements VersioningServicePort
{
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
        try
        {
            cmisService.cancelCheckOut(objectId);
        }
        catch (CMISServiceException e)
        {
            throw ExceptionUtil.createCmisException(e);
        }
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
        try
        {
            NodeRef workingCopyNodeRef = cmisService.getObject(objectId.value, NodeRef.class, true, true, true);
            CMISTypeDefinition seriesObjectTypeDefinition = cmisService.getTypeDefinition(workingCopyNodeRef);

            if ((null != contentStream) && (CMISContentStreamAllowedEnum.NOT_ALLOWED == seriesObjectTypeDefinition.getContentStreamAllowed()))
            {
                throw ExceptionUtil.createCmisException("Content stream is not allowed", EnumServiceException.STREAM_NOT_SUPPORTED);
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
                    throw ExceptionUtil.createCmisException("Exception while updating content stream", EnumServiceException.RUNTIME, e);
                }
            }

            propertiesUtil.setProperties(workingCopyNodeRef, properties, null);
            NodeRef nodeRef = cmisService.checkIn(objectId.value, checkinComment, major == null || major);

            applyAclCarefully(nodeRef, addACEs, removeACEs, EnumACLPropagation.PROPAGATE, policies);
            objectId.value = propertiesUtil.getProperty(nodeRef, CMISDictionaryModel.PROP_OBJECT_ID, objectId.value);
        }
        catch (CMISServiceException e)
        {
            throw ExceptionUtil.createCmisException(e);
        }
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
        try
        {
            NodeRef pwcNodeRef = cmisService.checkOut(objectId.value);
            objectId.value = propertiesUtil.getProperty(pwcNodeRef, CMISDictionaryModel.PROP_OBJECT_ID, objectId.value);
            contentCopied.value = null != fileFolderService.getReader(pwcNodeRef);
        }
        catch (CMISServiceException e)
        {
            throw ExceptionUtil.createCmisException(e);
        }
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
        PropertyFilter propertyFilter = createPropertyFilter(filter);
        List<CmisObjectType> objects = new LinkedList<CmisObjectType>();
        try
        {
            for (NodeRef nodeRef : cmisService.getAllVersions(objectId))
            {
                objects.add(createCmisObject(nodeRef, propertyFilter, null, includeAllowableActions, null));
            }
        }
        catch (CMISServiceException e)
        {
            throw ExceptionUtil.createCmisException(e);
        }
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
        NodeRef latestVersionNodeRef;
        try
        {
            latestVersionNodeRef = cmisService.getLatestVersion(objectId, major != null && major);
        }
        catch (CMISServiceException e)
        {
            throw ExceptionUtil.createCmisException(e);
        }
        PropertyFilter propertyFilter = createPropertyFilter(filter);
        return propertiesUtil.getProperties(latestVersionNodeRef, propertyFilter);
    }

    /**
     * 
     */
    // TODO: it is necessary to add tests for this method
    public CmisObjectType getObjectOfLatestVersion(String repositoryId, String objectId, Boolean major, String filter, Boolean includeAllowableActions,
            EnumIncludeRelationships includeRelationships, String renditionFilter, Boolean includePolicyIds, Boolean includeACL, CmisExtensionType extension) throws CmisException
    {
        checkRepositoryId(repositoryId);
        try
        {
            NodeRef latestVersionNodeRef = cmisService.getLatestVersion(objectId, major != null && major);
            // TODO: includePolicyIds
            PropertyFilter propertyFilter = createPropertyFilter(filter);
            CmisObjectType result = createCmisObject(latestVersionNodeRef, propertyFilter, includeRelationships,
                    includeAllowableActions, renditionFilter);
            if (includeACL)
            {
                appendWithAce(cmisService.getVersionSeries(objectId, NodeRef.class, false), result);
            }

            return result;
        }
        catch (CMISServiceException e)
        {
            throw ExceptionUtil.createCmisException(e);
        }
    }
}
