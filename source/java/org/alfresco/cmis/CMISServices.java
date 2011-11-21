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
package org.alfresco.cmis;

import java.io.InputStream;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.query.PagingResults;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;


/**
 * CMIS Services.
 * 
 * @author davidc
 * @author dward
 */
public interface CMISServices
{
    /**
     * The prefix we give to association object IDs.
     */
    public static final String ASSOC_ID_PREFIX = "assoc:";

    /**
     * Gets the supported CMIS Version
     * 
     * @return  version of CMIS specification supported
     */
    public String getCMISVersion();

    /**
     * Gets the supported CMIS Specification Title
     * 
     * @return  CMIS pecification Title
     */
    public String getCMISSpecTitle();
    
    /**
     * Gets the default root node path
     * 
     * @return  root node path
     */
    public String getDefaultRootPath();
    
    /**
     * Gets the default root node ref
     *  
     * @return  root node ref
     */
    public NodeRef getDefaultRootNodeRef();

    /**
     * Gets the default store ref
     * 
     * @return  store ref
     */
    public StoreRef getDefaultRootStoreRef();
    
    /**
     * Finds a NodeRef given a repository reference
     * 
     * @param referenceType  node, path
     * @param reference  node => id, path => path
     * @return  nodeRef (or null, if not found)
     */
    public NodeRef getNode(String referenceType, String[] reference);
    
    /**
     * Gets a map of node attributes relating to renditions.
     * 
     * @param nodeRef
     *            the node ref
     * @param renditionFilter
     *            the rendition filter
     * @return the attribute map
     * @throws CMISFilterNotValidException
     *             if the rendition filter is invalid
     */
    public Map<String, Object> getRenditions(NodeRef nodeRef, String renditionFilter)
            throws CMISFilterNotValidException;
    
    /**
     * Get node children
     * 
     * @param parent
     *            parent node
     * @param typesFilter
     *            types filter
     * @param orderBy
     *            comma-separated list of sort names and the ascending modifier "ASC" or the descending modifier "DESC"
     *            for each sort name
     * @return children of node
     */
    public NodeRef[] getChildren(NodeRef parent, CMISTypesFilterEnum typesFilter, String orderBy)
            throws CMISInvalidArgumentException;
    
    /**
     * Get node children
     * 
     * @param parent
     *            parent node
     * @param typesFilter
     *            types filter
     * @param maxItems
     *            number of items (in page)
     * @param skipCount
     *            number of items to skip (page starts at next item)
     * @param orderBy
     *            comma-separated list of sort names and the ascending modifier "ASC" or the descending modifier "DESC"
     *            for each sort name
     * @return children of node
     */
    public PagingResults<FileInfo> getChildren(NodeRef parent, CMISTypesFilterEnum typesFilter, BigInteger maxItems, BigInteger skipCount, String orderBy)
            throws CMISInvalidArgumentException;

    /**
     * Query for checked out items
     * 
     * @param username
     *            for user
     * @param folder
     *            (optional) within folder
     * @param includeDescendants
     *            true => include descendants of folder, false => only children of folder
     * @param orderBy
     *            comma-separated list of query names and the ascending modifier "ASC" or the descending modifier "DESC"
     *            for each query name
     * @return checked out items
     */
    public NodeRef[] getCheckedOut(String username, NodeRef folder, boolean includeDescendants, String orderBy)
            throws CMISInvalidArgumentException;

    /**
     * Query for relationships.
     * 
     * @param relDef
     *            type of relationship to query (or null, for all relationships)
     * @param includeSubTypes
     *            the include sub types
     * @param direction
     *            limit direction of relationships to query (or null, for both directions)
     * @param node
     *            the node
     * @return relationships
     * @throws CMISInvalidArgumentException
     *             if an argument is invalid
     */
    public AssociationRef[] getRelationships(NodeRef node, CMISTypeDefinition relDef, boolean includeSubTypes, CMISRelationshipDirectionEnum direction) throws CMISInvalidArgumentException;
    
    /**
     * Get a single property belonging to the node's type.
     * 
     * @param nodeRef
     *            the node
     * @param propertyName
     *            the property name
     * @return value
     * @throws CMISInvalidArgumentException
     *             if an argument is invalid
     */
    public Serializable getProperty(NodeRef nodeRef, String propertyName) throws CMISInvalidArgumentException;

    /**
     * Get a single property, optionally constrained to a given node type or aspect
     * 
     * @param nodeRef
     *            the node
     * @param typeDef
     *            the node type or aspect or <code>null</code> if any property can be returned
     * @param propertyName
     *            the property name
     * @return value
     * @throws CMISInvalidArgumentException
     *             if an argument is invalid
     */
    public Serializable getProperty(NodeRef nodeRef, CMISTypeDefinition typeDef, String propertyName)
            throws CMISInvalidArgumentException;

    /**
     * Get a single property for an association.
     * 
     * @param assocRef
     *            the association
     * @param propertyName
     *            the property name
     * @return value
     * @throws CMISInvalidArgumentException
     *             if an argument is invalid
     */
    public Serializable getProperty(AssociationRef assocRef, String propertyName) throws CMISInvalidArgumentException;
    
    /**
     * Get all properties of a node's type.
     * 
     * @param nodeRef
     *            the node ref
     * @return the properties
     * @throws CMISInvalidArgumentException
     *             if an argument is invalid
     */
    public Map<String, Serializable> getProperties(NodeRef nodeRef) throws CMISInvalidArgumentException;
    
    /**
     * Get all properties of an association's type.
     * 
     * @param assocRef
     *            the association
     * @return the properties
     * @throws CMISInvalidArgumentException
     *             if an argument is invalid
     */
    public Map<String, Serializable> getProperties(AssociationRef assocRef) throws CMISInvalidArgumentException;

    /**
     * Get all of a node's values for the properties in the given type or aspect.
     * 
     * @param nodeRef
     *            the node ref
     * @param typeDef
     *            the type or aspect definition
     * @return the properties
     * @throws CMISInvalidArgumentException
     *             if an argument is invalid
     */
    public Map<String, Serializable> getProperties(NodeRef nodeRef, CMISTypeDefinition typeDef)
            throws CMISInvalidArgumentException;

    /**
     * Gets the aspects applied to a node.
     * 
     * @param nodeRef
     *            the node ref
     * @return the aspect definitions
     * @throws CMISInvalidArgumentException
     *             if an argument is invalid
     */
    public Set<CMISTypeDefinition> getAspects(NodeRef nodeRef);

    /**
     * Set a single property belonging to the node's type.
     * 
     * @param nodeRef
     *            the node ref
     * @param propertyName
     *            the property name
     * @param value
     *            the value
     * @throws CMISInvalidArgumentException
     *             if an argument is invalid
     * @throws CMISConstraintException
     *             if the property cannot be set
     */
    public void setProperty(NodeRef nodeRef, String propertyName, Serializable value)
            throws CMISInvalidArgumentException, CMISConstraintException;

    /**
     * Set a single property, optionally constrained to a given node type or aspect
     * 
     * @param nodeRef
     *            the node ref
     * @param typeDef
     *            the node type or aspect or <code>null</code> if any valid property should be set (corresponding aspect
     *            added automatically).
     * @param propertyName
     *            the property name
     * @param value
     *            the value
     * @throws CMISInvalidArgumentException
     *             if an argument is invalid
     * @throws CMISConstraintException
     *             if the property cannot be set
     */
    public void setProperty(NodeRef nodeRef, CMISTypeDefinition typeDef, String propertyName, Serializable value)
            throws CMISInvalidArgumentException, CMISConstraintException;

    /**
     * Sets the aspects on a node (Alfresco extension).
     * 
     * @param node
     *            the node
     * @param aspectsToRemove
     *            the aspects to remove
     * @param aspectsToAdd
     *            the aspects to add
     * @throws CMISInvalidArgumentException
     *             if an argument is invalid
     */
    public void setAspects(NodeRef node, Iterable<String> aspectsToRemove, Iterable<String> aspectsToAdd)
            throws CMISInvalidArgumentException;

    /**
     * Applies a versioning state to a new node, potentially resulting in a new node.
     * 
     * @param source
     *            the node
     * @param versioningState
     *            the versioning state
     * @return the node to write changes to
     * @throws CMISConstraintException
     *             if it's not possible to apply the state
     * @throws CMISInvalidArgumentException
     *             if an argument is invalid
     */
    public NodeRef applyVersioningState(NodeRef source, CMISVersioningStateEnum versioningState)
            throws CMISConstraintException, CMISInvalidArgumentException;

    /**
     * Gets a readable object of a required type from an object ID. The object may be immutable. Note that version
     * history nodes can be returned as Versions or Nodes.
     * 
     * @param objectId
     *            the object id
     * @param requiredType
     *            the required type (NodeRef.class, Version.class, AssociationRef.class or Object.class)
     * @return the readable object
     * @throws CMISConstraintException
     *             if the object can't be returned as the right type
     * @throws CMISVersioningException
     *             if the object's versioning state isn't as expected
     * @throws CMISObjectNotFoundException
     *             if the object does not exist
     * @throws CMISInvalidArgumentException
     *             if an argument is invalid
     * @throws CMISPermissionDeniedException
     *             if access to the object is denied
     */
    public <T> T getReadableObject(String objectId, Class<T> requiredType) throws CMISConstraintException,
            CMISVersioningException, CMISObjectNotFoundException, CMISInvalidArgumentException,
            CMISPermissionDeniedException;

    /**
     * Gets an object or a required type from an object ID. The object's mutability and versioning state will be
     * validated as required.
     * 
     * @param objectId
     *            the object id
     * @param requiredType
     *            the required type (NodeRef.class, Version.class, AssociationRef.class or Object.class)
     * @param forUpdate
     *            Do we require to write to this object? If <code>true</code> then the object must not be checked out
     *            and must not be a version history node unless the required type is assignable from Version.class.
     * @param isVersionable
     *            Should the object be versionable?
     * @param isPwc
     *            If isVersionable is <code>true</code> then the object should either be or not be a private working
     *            copy, as indicated by this flag
     * @return the object
     * @throws CMISConstraintException
     *             if the object can't be returned as the right type
     * @throws CMISVersioningException
     *             if the object's versioning state isn't as expected
     * @throws CMISObjectNotFoundException
     *             if the object does not exist
     * @throws CMISInvalidArgumentException
     *             if an argument is invalid
     * @throws CMISPermissionDeniedException
     *             if access to the object is denied
     */
    public <T> T getObject(String objectId, Class<T> requiredType, boolean forUpdate, boolean isVersionable,
            boolean isPwc) throws CMISConstraintException, CMISVersioningException, CMISObjectNotFoundException,
            CMISInvalidArgumentException, CMISPermissionDeniedException;

    /**
     * Gets the version series of an object.
     * 
     * @param objectId
     *            the object id
     * @param requiredType
     *            the required type (NodeRef.class, Version.class or AssociationRef.class)
     * @param isVersionable
     *            Should the object be versionable?
     * @return the version series
     * @throws CMISConstraintException
     *             if the object can't be returned as the right type
     * @throws CMISVersioningException
     *             if the object's versioning state isn't as expected
     * @throws CMISObjectNotFoundException
     *             if the object does not exist
     * @throws CMISInvalidArgumentException
     *             if an argument is invalid
     * @throws CMISPermissionDeniedException
     *             if access to the object is denied
     */
    public <T> T getVersionSeries(String objectId, Class<T> requiredType, boolean isVersionable)
            throws CMISConstraintException, CMISVersioningException, CMISObjectNotFoundException,
            CMISInvalidArgumentException, CMISPermissionDeniedException;

    /**
     * Gets a folder from an object ID.
     * 
     * @param objectId
     *            the object id
     * @return the folder
     * @throws CMISConstraintException
     *             if the object can't be returned as the right type
     * @throws CMISVersioningException
     *             if the object's versioning state isn't as expected
     * @throws CMISObjectNotFoundException
     *             if the object does not exist
     * @throws CMISInvalidArgumentException
     *             if an argument is invalid
     * @throws CMISPermissionDeniedException
     *             if access to the object is denied
     */
    public NodeRef getFolder(String objectId) throws CMISConstraintException, CMISVersioningException,
            CMISObjectNotFoundException, CMISInvalidArgumentException, CMISPermissionDeniedException;

    /**
     * Gets parent of a folder.
     * 
     * @param folderId
     *            the folder id
     * @return the folder parent
     * @throws CMISConstraintException
     *             if the object can't be returned as the right type
     * @throws CMISVersioningException
     *             if the object's versioning state isn't as expected
     * @throws CMISObjectNotFoundException
     *             if the object does not exist
     * @throws CMISInvalidArgumentException
     *             if an argument is invalid
     * @throws CMISPermissionDeniedException
     *             if access to the object is denied
     */
    public NodeRef getFolderParent(String folderId) throws CMISConstraintException, CMISVersioningException,
            CMISObjectNotFoundException, CMISInvalidArgumentException, CMISPermissionDeniedException;

    /**
     * Gets the type definition for a node.
     * 
     * @param nodeRef
     *            the node
     * @return the type definition
     * @throws CMISInvalidArgumentException
     *             if an argument is invalid
     */
    public CMISTypeDefinition getTypeDefinition(NodeRef nodeRef) throws CMISInvalidArgumentException;

    /**
     * Gets the type definition for an association.
     * 
     * @param associationRef
     *            the association
     * @return the type definition
     * @throws CMISInvalidArgumentException
     *             if an argument is invalid
     */
    public CMISTypeDefinition getTypeDefinition(AssociationRef associationRef) throws CMISInvalidArgumentException;

    /**
     * Gets the type definition for a given type ID.
     * 
     * @param typeId
     *            the type id
     * @return the type definition
     * @throws CMISInvalidArgumentException
     *             if an argument is invalid
     */
    public CMISTypeDefinition getTypeDefinition(String typeId) throws CMISInvalidArgumentException;

    /**
     * Gets the type definition for a given object.
     * 
     * @param object
     *            the object
     * @return the type definition
     * @throws CMISInvalidArgumentException
     *             if an argument is invalid
     */
    public CMISTypeDefinition getTypeDefinition(Object object) throws CMISInvalidArgumentException;

    /**
     * Gets the CMIS base types.
     * 
     * @return the base types
     */
    public Collection<CMISTypeDefinition> getBaseTypes();

    /**
     * Checks out an object.
     * 
     * @param objectId
     *            the object id
     * @return the resulting private working copy node
     * @throws CMISConstraintException
     *             if the object isn't of the right type
     * @throws CMISVersioningException
     *             if the object's versioning state isn't as expected
     * @throws CMISObjectNotFoundException
     *             if the object does not exist
     * @throws CMISInvalidArgumentException
     *             if an argument is invalid
     * @throws CMISPermissionDeniedException
     *             if access to the object is denied
     */
    public NodeRef checkOut(String objectId) throws CMISConstraintException, CMISVersioningException,
            CMISObjectNotFoundException, CMISInvalidArgumentException, CMISPermissionDeniedException;

    /**
     * Checks in a private working copy.
     * 
     * @param objectId
     *            the object id of the private working copy
     * @param checkinComment
     *            the checkin comment
     * @param isMajor
     *            Is this a major version?
     * @return the checked in node
     * @throws CMISConstraintException
     *             if the object isn't of the right type
     * @throws CMISVersioningException
     *             if the object's versioning state isn't as expected
     * @throws CMISObjectNotFoundException
     *             if the object does not exist
     * @throws CMISInvalidArgumentException
     *             if an argument is invalid
     * @throws CMISPermissionDeniedException
     *             if access to the object is denied
     */
    public NodeRef checkIn(String objectId, String checkinComment, boolean isMajor) throws CMISConstraintException,
            CMISVersioningException, CMISObjectNotFoundException, CMISInvalidArgumentException,
            CMISPermissionDeniedException;

    /**
     * Cancels check out of a private working copy.
     * 
     * @param objectId
     *            the object id of the private working copy
     * @throws CMISConstraintException
     *             if the object isn't of the right type
     * @throws CMISVersioningException
     *             if the object's versioning state isn't as expected
     * @throws CMISObjectNotFoundException
     *             if the object does not exist
     * @throws CMISInvalidArgumentException
     *             if an argument is invalid
     * @throws CMISPermissionDeniedException
     *             if access to the object is denied
     */
    public void cancelCheckOut(String objectId) throws CMISConstraintException, CMISVersioningException,
            CMISObjectNotFoundException, CMISInvalidArgumentException, CMISPermissionDeniedException;

    /**
     * Gets all versions of an object.
     * 
     * @param objectId
     *            the object id
     * @return the all versions
     * @throws CMISConstraintException
     *             if the object isn't of the right type
     * @throws CMISVersioningException
     *             if the object's versioning state isn't as expected
     * @throws CMISObjectNotFoundException
     *             if the object does not exist
     * @throws CMISInvalidArgumentException
     *             if an argument is invalid
     * @throws CMISPermissionDeniedException
     *             if access to the object is denied
     */
    public List<NodeRef> getAllVersions(String objectId) throws CMISConstraintException, CMISVersioningException,
            CMISObjectNotFoundException, CMISInvalidArgumentException, CMISPermissionDeniedException;

    /**
     * Gets the latest version of an object and optionally the latest major version.
     * 
     * @param objectId
     *            the object id
     * @param major
     *            Should we return the latest major version?
     * @return the latest version
     * @throws CMISConstraintException
     *             if the object isn't of the right type
     * @throws CMISVersioningException
     *             if the object's versioning state isn't as expected
     * @throws CMISObjectNotFoundException
     *             if the object does not exist
     * @throws CMISInvalidArgumentException
     *             if an argument is invalid
     * @throws CMISPermissionDeniedException
     *             if access to the object is denied
     */
    public NodeRef getLatestVersion(String objectId, boolean major) throws CMISConstraintException,
            CMISVersioningException, CMISObjectNotFoundException, CMISInvalidArgumentException,
            CMISPermissionDeniedException;

    /**
     * Deletes a folder and its children, without raising any exceptions.
     * 
     * @param objectId
     *            the folder's object id
     * @param continueOnFailure
     *            should we continue if an error occurs with one of the children?
     * @param unfile
     *            should we remove non-primary associations to nodes rather than delete them?
     * @param deleteAllVersions
     *            should we delete all the versions of the documents we delete?
     * @return list of object IDs of the children we failed to delete
     * @throws CMISConstraintException
     *             if the object isn't of the right type
     * @throws CMISVersioningException
     *             if the object's versioning state isn't as expected
     * @throws CMISObjectNotFoundException
     *             if the object does not exist
     * @throws CMISInvalidArgumentException
     *             if an argument is invalid
     * @throws CMISPermissionDeniedException
     *             if access to the object is denied
     */
    public List<String> deleteTree(String objectId, boolean continueOnFailure, boolean unfile, boolean deleteAllVersions)
            throws CMISConstraintException, CMISVersioningException, CMISObjectNotFoundException,
            CMISInvalidArgumentException, CMISPermissionDeniedException;

    /**
     * Deletes a folder and its children, raising an exception for the last error encountered.
     * 
     * @param objectId
     *            the folder's object id
     * @param continueOnFailure
     *            should we continue if an error occurs with one of the children?
     * @param unfile
     *            should we remove non-primary associations to nodes rather than delete them?
     * @param deleteAllVersions
     *            should we delete all the versions of the nodes we delete?
     * @return list of object IDs of the children we failed to delete
     * @throws CMISServiceException
     *             the last error encountered
     */
    public void deleteTreeReportLastError(String objectId, boolean continueOnFailure, boolean unfile,
            boolean deleteAllVersions) throws CMISServiceException;

    /**
     * Deletes a document's content stream.
     * 
     * @param objectId
     *            the object id of the document
     * @throws CMISConstraintException
     *             if the object isn't of the right type
     * @throws CMISVersioningException
     *             if the object's versioning state isn't as expected
     * @throws CMISObjectNotFoundException
     *             if the object does not exist
     * @throws CMISInvalidArgumentException
     *             if an argument is invalid
     * @throws CMISPermissionDeniedException
     *             if access to the object is denied
     */
    public void deleteContentStream(String objectId) throws CMISConstraintException, CMISVersioningException,
            CMISObjectNotFoundException, CMISInvalidArgumentException, CMISPermissionDeniedException;

    /**
     * Deletes an object.
     * 
     * @param objectId
     *            the object id
     * @param allVersions
     *            if the object is a document, should we delete all versions?
     * @throws CMISConstraintException
     *             if the object isn't of the right type
     * @throws CMISVersioningException
     *             if the object's versioning state isn't as expected
     * @throws CMISObjectNotFoundException
     *             if the object does not exist
     * @throws CMISInvalidArgumentException
     *             if an argument is invalid
     * @throws CMISPermissionDeniedException
     *             if access to the object is denied
     * @throws CMISRuntimeException
     *             on other exceptions
     */
    public void deleteObject(String objectId, boolean allVersions) throws CMISConstraintException,
            CMISVersioningException, CMISObjectNotFoundException, CMISInvalidArgumentException,
            CMISPermissionDeniedException, CMISRuntimeException, CMISServiceException;

    /**
     * Adds a secondary child association to an object from a folder.
     * 
     * @param objectId
     *            the object id
     * @param folderId
     *            the folder id
     * @throws CMISConstraintException
     *             if an object isn't of the right type
     * @throws CMISVersioningException
     *             if an object's versioning state isn't as expected
     * @throws CMISObjectNotFoundException
     *             if an object does not exist
     * @throws CMISInvalidArgumentException
     *             if an argument is invalid
     * @throws CMISPermissionDeniedException
     *             if access to an object is denied
     */
    public void addObjectToFolder(String objectId, String folderId) throws CMISConstraintException,
            CMISVersioningException, CMISObjectNotFoundException, CMISInvalidArgumentException,
            CMISPermissionDeniedException;

    /**
     * Removes a secondary child association to an object from a folder.
     * 
     * @param objectId
     *            the object id
     * @param folderId
     *            the folder id
     * @throws CMISNotSupportedException
     *             if the child association is primary
     * @throws CMISConstraintException
     *             if an object isn't of the right type
     * @throws CMISVersioningException
     *             if an object's versioning state isn't as expected
     * @throws CMISObjectNotFoundException
     *             if an object does not exist
     * @throws CMISInvalidArgumentException
     *             if an argument is invalid
     * @throws CMISPermissionDeniedException
     *             if access to an object is denied
     */
    public void removeObjectFromFolder(String objectId, String folderId) throws CMISNotSupportedException,
            CMISConstraintException, CMISVersioningException, CMISObjectNotFoundException,
            CMISInvalidArgumentException, CMISPermissionDeniedException;

    /**
     * Moves an object from one folder to another.
     * 
     * @param objectId
     *            the object id
     * @param targetFolderId
     *            the target folder id
     * @param sourceFolderId
     *            the source folder id
     * @throws CMISConstraintException
     *             if an object isn't of the right type
     * @throws CMISVersioningException
     *             if an object's versioning state isn't as expected
     * @throws CMISObjectNotFoundException
     *             if an object does not exist
     * @throws CMISInvalidArgumentException
     *             if an argument is invalid
     * @throws CMISPermissionDeniedException
     *             if access to an object is denied
     */
    public void moveObject(String objectId, String targetFolderId, String sourceFolderId)
            throws CMISConstraintException, CMISVersioningException, CMISObjectNotFoundException,
            CMISInvalidArgumentException, CMISPermissionDeniedException;

    /**
     * Sets the content stream on an object.
     * 
     * @param objectId
     *            the object id
     * @param propertyQName
     *            the property q name
     * @param overwriteFlag
     *            the overwrite flag
     * @param contentStream
     *            the content stream
     * @param mimeType
     *            the mime type
     * @return <code>true</code> if content was overwritten
     * @throws CMISContentAlreadyExistsException
     *             if overwrite was <code>false</code> and content already existed
     * @throws CMISStreamNotSupportedException
     *             if the object's type definition does not allow a content stream
     * @throws CMISConstraintException
     *             if an object isn't of the right type
     * @throws CMISVersioningException
     *             if an object's versioning state isn't as expected
     * @throws CMISObjectNotFoundException
     *             if an object does not exist
     * @throws CMISInvalidArgumentException
     *             if an argument is invalid
     * @throws CMISPermissionDeniedException
     *             if access to an object is denied
     */
    public boolean setContentStream(String objectId, QName propertyQName, boolean overwriteFlag,
            InputStream contentStream, String mimeType) throws CMISConstraintException, CMISVersioningException,
            CMISObjectNotFoundException, CMISContentAlreadyExistsException, CMISStreamNotSupportedException,
            CMISInvalidArgumentException, CMISPermissionDeniedException;
    
    /**
     * Creates a policy object of the specified type, and optionally adds the policy to a folder. Currently no policy
     * types can be created in Alfresco.
     * 
     * @param properties
     *            CMIS properties
     * @param folderId
     *            parent folder for this new policy
     * @param policies
     *            the policies
     * @return Id of the created policy object
     * @throws CMISConstraintException
     *             if there is a problem with the supplied properties
     * @throws CMISInvalidArgumentException
     *             if an argument is invalid
     * @throws CMISRuntimeException
     *             on other exceptions
     */
    public String createPolicy(Map<String, Serializable> properties, String folderId, List<String> policies)
            throws CMISConstraintException, CMISRuntimeException, CMISInvalidArgumentException;
    
    /**
     * Applies a policy object to a target object.
     * 
     * @param policyId
     *            policy Id
     * @param objectId
     *            target object Id
     * @throws CMISConstraintException
     *             if an object isn't of the right type
     * @throws CMISVersioningException
     *             if an object's versioning state isn't as expected
     * @throws CMISObjectNotFoundException
     *             if an object does not exist
     * @throws CMISInvalidArgumentException
     *             if an argument is invalid
     * @throws CMISPermissionDeniedException
     *             if access to an object is denied
     */
    public void applyPolicy(String policyId, String objectId) throws CMISConstraintException, CMISVersioningException,
            CMISObjectNotFoundException, CMISInvalidArgumentException, CMISPermissionDeniedException;
    
    /**
     * Gets the list of policy objects currently applied to a target object.
     * 
     * @param objectId
     *            the object id
     * @param filter
     *            property filter
     * @return the applied policies
     * @throws CMISConstraintException
     *             if an object isn't of the right type
     * @throws CMISVersioningException
     *             if an object's versioning state isn't as expected
     * @throws CMISObjectNotFoundException
     *             if an object does not exist
     * @throws CMISInvalidArgumentException
     *             if an argument is invalid
     * @throws CMISPermissionDeniedException
     *             if access to an object is denied
     * @throws CMISFilterNotValidException
     *             if the property filter is invalid
     */
    public List<CMISTypeDefinition> getAppliedPolicies(String objectId, String filter) throws CMISConstraintException,
            CMISVersioningException, CMISObjectNotFoundException, CMISInvalidArgumentException,
            CMISPermissionDeniedException, CMISFilterNotValidException;
    
    /**
     * Removes a previously applied policy from a target object. The policy object is not deleted, and may still be
     * applied to other objects.
     * 
     * @param policyId
     *            policy Id
     * @param objectId
     *            target object Id.
     * @throws CMISConstraintException
     *             if an object isn't of the right type
     * @throws CMISVersioningException
     *             if an object's versioning state isn't as expected
     * @throws CMISObjectNotFoundException
     *             if an object does not exist
     * @throws CMISInvalidArgumentException
     *             if an argument is invalid
     * @throws CMISPermissionDeniedException
     *             if access to an object is denied
     */
    public void removePolicy(String policyId, String objectId) throws CMISConstraintException, CMISVersioningException,
            CMISObjectNotFoundException, CMISInvalidArgumentException, CMISPermissionDeniedException;
    
}
