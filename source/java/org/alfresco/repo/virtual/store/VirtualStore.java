/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
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

package org.alfresco.repo.virtual.store;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.repo.security.permissions.NodePermissionEntry;
import org.alfresco.repo.security.permissions.PermissionReference;
import org.alfresco.repo.virtual.VirtualizationException;
import org.alfresco.repo.virtual.ref.Reference;
import org.alfresco.repo.virtual.template.FilingData;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.security.AccessPermission;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.QNamePattern;
import org.alfresco.util.Pair;

/**
 * Interface for public and internal <b>reference</b> operations.
 * <p>
 * Handles most virtualized {@link Reference} meta-data interactions analogous
 * to actual Alfresco public Repository Services.
 * 
 * @see ServiceRegistry The Public Repository Services
 * @author Bogdan Horje
 */
public interface VirtualStore
{
    static final int MATERIAL_ADHERENCE=1;
    
    static final int FILING_OR_MATERIAL_ADHERENCE=2;
    
    Collection<NodeRef> materializeIfPossible(Collection<NodeRef> nodeRefs) throws VirtualizationException;

    NodeRef materializeIfPossible(NodeRef nodeRef) throws VirtualizationException;

    boolean isVirtual(NodeRef nodeRef) throws VirtualizationException;

    boolean canVirtualize(NodeRef nodeRef) throws VirtualizationException;

    Reference virtualize(NodeRef nodeRef) throws VirtualizationException;

    NodeRef materialize(Reference reference) throws VirtualizationException;
    
    NodeRef adhere(Reference reference,int mode) throws VirtualizationException;

    boolean canMaterialize(Reference reference) throws VirtualizationException;

    Path getPath(Reference reference) throws VirtualizationException;

    /**
     * @param reference
     * @return all virtual properties of the referred virtualized artefact keyed
     *         by their qualified name
     * @throws VirtualizationException
     */
    Map<QName, Serializable> getProperties(Reference reference) throws VirtualizationException;

    /**
     * Get the reference of the virtualized artefact with the given name within
     * the virtual context (only) of the parent reference.<br>
     * The name is case-insensitive as Alfresco has to support case-insensitive
     * clients as standard.<br>
     * 
     * @param parentReference parent {@link Reference}
     * @param assocTypeQName
     * @param childName
     * @return the virtual child reference for the given name in the context of
     *         the given parent reference
     * @throws VirtualizationException
     * @see NodeService#getChildByName(org.alfresco.service.cmr.repository.NodeRef,
     *      QName, String)
     */
    Reference getChildByName(Reference parentReference, QName assocTypeQName, String childName)
                throws VirtualizationException;

    /**
     * Retrieve immediate children references of a given reference where the
     * child nodes are in the given inclusive list.
     * 
     * @param parentReference the parent node - usually a <b>container</b>
     * @param childNodeTypeQNames the types that the children may be. Subtypes
     *            are not automatically calculated and the list must therefore
     *            be exhaustive.
     * @return Returns a list of <code>ChildAssociationRef</code> instances.
     */
    List<ChildAssociationRef> getChildAssocs(Reference parentReference, Set<QName> childNodeTypeQNames);

    /**
     * Gets all child references associations where the pattern of the
     * association qualified name is an exact match.
     * 
     * @param parentReference the parent node - usually a <b>container</b>
     * @param typeQNamePattern the qualified name of the association (
     *            <tt>null</tt> to ignore)
     * @param qnamePattern the path qualified name (<tt>null</tt> to ignore)
     * @param maxResults the number of results to get
     * @param preload <tt>true</tt> if the nodes must be preloaded into the
     *            cache
     * @return Returns a list of <code>ChildAssociationRef</code> instances
     * @throws InvalidNodeRefException if the node could not be found
     * @see QName
     */
    List<ChildAssociationRef> getChildAssocs(Reference parentReference, final QNamePattern typeQNamePattern,
                final QNamePattern qnamePattern, final int maxResults, final boolean preload)
                throws InvalidNodeRefException;

    /**
     * Retrieve the immediate children of a given node based on the value of a
     * property of those children.
     * <p>
     * If the property to be searched is multi-valued then will match on any one
     * values.
     * <p>
     * Please note, the following system maintained properties that cannot be
     * used with this method.
     * <ul>
     * <li>cm:name - use getChildByName instead</li>
     * <li>cm:created</li>
     * <li>cm:creator</li>
     * <li>cm:modified</li>
     * <li>cm:modifier</li>
     * <li>sys:node-uuid</li>
     * <li>sys:node-dbid</li>
     * <li>sys:store-identifier</li>
     * <li>sys:store-protocol</li>
     * </ul>
     * 
     * @param parentReference the parent reference - usually a <b>container</b>
     * @param propertyQName the fully qualified name of the property
     * @param value the value to search for. Must be a simple type such as
     *            String, Number, Date or Boolean, it cannot be a collection, a
     *            content property, MLText or a float.
     * @return Returns a list of <code>ChildAssociationRef</code> instances.
     */
    List<ChildAssociationRef> getChildAssocsByPropertyValue(Reference parentReference, QName propertyQName,
                Serializable value);

    /**
     * Gets the set of child associations of a certain parent node without
     * parent associations of a certain type to other nodes with the same
     * parent! In effect the 'orphans' with respect to a certain association
     * type.
     * 
     * @param parent the parent reference
     * @param assocTypeQName the association type QName
     * @return a {@link Collection} of child associations
     */
    Collection<ChildAssociationRef> getChildAssocsWithoutParentAssocsOfType(Reference parentReference,
                QName assocTypeQName);

    /**
     * Lists page of immediate children of the referred virtualized artefact
     * with optional filtering (exclusion of certain child file/folder subtypes,
     * actual-virtual filtering) and sorting.<br>
     * Pattern uses '*' as a wildcard
     * 
     * @param ref
     * @param actual
     * @param virtual
     * @param files
     * @param folders
     * @param pattern
     * @param ignoreQNames
     * @param searchTypeQNames
     * @param ignoreAspectQNames
     * @param sortProps
     * @param pagingRequest
     * @throws VirtualizationException
     */
    PagingResults<Reference> list(Reference ref, boolean actual, boolean virtual, final boolean files,
                final boolean folders, final String pattern, final Set<QName> searchTypeQNames,
                final Set<QName> ignoreTypeQNames, final Set<QName> ignoreAspectQNames,
                final List<Pair<QName, Boolean>> sortProps, final PagingRequest pagingRequest)
                throws VirtualizationException;

    /**
     * Lists page of immediate children of the referred virtualized artefact
     * with optional filtering (exclusion of certain child file/folder subtypes,
     * actual-virtual filtering) and sorting.<br>
     * 
     * @param ref
     * @param actual
     * @param virtual
     * @param files
     * @param folders
     * @param pattern
     * @param ignoreQNames
     * @param sortProps
     * @param pagingRequest
     * @throws VirtualizationException
     */
    PagingResults<Reference> list(Reference ref, boolean actual, boolean virtual, boolean files, boolean folders,
                String pattern, Set<QName> ignoreTypeQNames, Set<QName> ignoreAspectQNames,
                List<Pair<QName, Boolean>> sortProps, PagingRequest pagingRequest) throws VirtualizationException;

    /**
     * Lists page of immediate children of the referred virtualized artefact
     * with optional filtering (exclusion of certain child file/folder subtypes,
     * actual-virtual filtering) and sorting.<br>
     * 
     * @throws VirtualizationException
     */
    PagingResults<Reference> list(Reference ref, boolean actual, boolean virtual, Set<QName> searchTypeQNames,
                Set<QName> ignoreTypeQNames, Set<QName> ignoreAspectQNames, List<Pair<QName, Boolean>> sortProps,
                PagingRequest pagingRequest) throws VirtualizationException;

    /**
     * Lists all immediate children of the referred virtualized artefact.<br>
     * Note: this could be a long list (and will be trimmed at a pre-configured
     * maximum). You should consider using a paging request.
     * 
     * @param reference
     * @throws VirtualizationException
     */
    List<Reference> list(Reference reference) throws VirtualizationException;

    /**
     * Perform a search against the name of the files or folders within a
     * virtualized artefact {@link Reference} hierarchy. <br>
     * Wildcard characters are <b>*</b> and <b>?</b>. <br>
     * Warning: Please avoid using this method with any "namePattern" other than
     * "*". Although it works, its performance is poor which is why this method
     * is deprecated.
     * 
     * @param reference
     * @param namePattern
     * @param fileSearch
     * @param folderSearch
     * @param includeSubFolders
     * @throws VirtualizationException
     * @deprecated {@link FileFolderService#search(NodeRef, String, boolean, boolean, boolean)}
     *             alignment : for shallow search use list, listFolders,
     *             listFiles, searchSimple. For deep listing use
     *             listDeepFolders. Avoid calling this method with any name
     *             pattern except for "*".
     */
    List<Reference> search(Reference reference, String namePattern, boolean fileSearch, boolean folderSearch,
                boolean includeSubFolders) throws VirtualizationException;

    /**
     * @param reference
     * @return qualified type of the referred virtualized artefact
     * @throws VirtualizationException
     */
    QName getType(Reference reference) throws VirtualizationException;

    /**
     * @param reference
     * @param assocTypeQName
     * @param assocQName
     * @param nodeTypeQName
     * @param properties
     * @return {@link FilingData} of the given parent location
     * @throws VirtualizationException
     */
    FilingData createFilingData(Reference reference, QName assocTypeQName, QName assocQName, QName nodeTypeQName,
                Map<QName, Serializable> properties) throws VirtualizationException;

    /**
     * Check that the given authentication has a particular permission for the
     * given virtualized artefact. (The default behaviour is to inherit
     * permissions)
     * 
     * @param reference
     * @param perm
     * @return an {@link AccessStatus}
     * @throws VirtualizationException
     */
    AccessStatus hasPermission(Reference reference, String perm) throws VirtualizationException;

    /**
     * Check that the given authentication has a particular permission for the
     * given virtualized artefact. (The default behaviour is to inherit
     * permissions)
     * 
     * @param reference
     * @param perm
     * @return an {@link AccessStatus}
     * @throws VirtualizationException
     */
    AccessStatus hasPermission(Reference reference, PermissionReference perm) throws VirtualizationException;

    NodePermissionEntry getSetPermissions(Reference reference) throws VirtualizationException;

    Set<AccessPermission> getAllSetPermissions(Reference reference);
}
