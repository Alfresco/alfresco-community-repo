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
package org.alfresco.repo.version;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.model.ForumModel;
import org.alfresco.repo.policy.PolicyScope;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.version.VersionRevertCallback.RevertAspectAction;
import org.alfresco.repo.version.VersionRevertCallback.RevertAssocAction;
import org.alfresco.repo.version.common.VersionHistoryImpl;
import org.alfresco.repo.version.common.VersionImpl;
import org.alfresco.repo.version.common.VersionUtil;
import org.alfresco.repo.version.common.versionlabel.SerialVersionLabelPolicy;
import org.alfresco.repo.version.traitextender.VersionServiceExtension;
import org.alfresco.repo.version.traitextender.VersionServiceTrait;
import org.alfresco.service.cmr.dictionary.AspectDefinition;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.repository.AspectMissingException;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.version.ReservedVersionNameException;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionHistory;
import org.alfresco.service.cmr.version.VersionService;
import org.alfresco.service.cmr.version.VersionServiceException;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.traitextender.AJProxyTrait;
import org.alfresco.traitextender.Extend;
import org.alfresco.traitextender.ExtendedTrait;
import org.alfresco.traitextender.Extensible;
import org.alfresco.traitextender.Trait;
import org.alfresco.util.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.extensions.surf.util.ParameterCheck;

/**
 * Version2 Service - implements version2Store (a lighter implementation of the lightWeightVersionStore)
 */
public class Version2ServiceImpl extends VersionServiceImpl implements VersionService, Version2Model,Extensible
{
    private static Log logger = LogFactory.getLog(Version2ServiceImpl.class);
    
    private PermissionService permissionService;
    private boolean useVersionAssocIndex = false;

    private ExtendedTrait<VersionServiceTrait> versionServiceTrait;
    
    public Version2ServiceImpl()
    {
        versionServiceTrait=new ExtendedTrait<VersionServiceTrait>(AJProxyTrait.create(this, VersionServiceTrait.class));
    }
    
    public void setPermissionService(PermissionService permissionService)
    {
        this.permissionService = permissionService;
    }

    /**
     * Set to use child association index on versions. This helps ordering versions when sequential IDs are not
     * guaranteed by the DBMS.
     *
     * @param useVersionAssocIndex
     */
    public void setUseVersionAssocIndex(boolean useVersionAssocIndex)
    {
        this.useVersionAssocIndex = useVersionAssocIndex;
    }

    public boolean isUseVersionAssocIndex()
    {
        return useVersionAssocIndex;
    }

    /**
     * Initialise method
     */
    @Override
    public void initialise()
    {
        super.initialise();
    }
    

    @Override
    @Extend(extensionAPI=VersionServiceExtension.class,traitAPI=VersionServiceTrait.class)
    public StoreRef getVersionStoreReference()
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("Run as user " + AuthenticationUtil.getRunAsUser());
            logger.debug("Fully authenticated " + AuthenticationUtil.getFullyAuthenticatedUser());
        }
        
        return new StoreRef(StoreRef.PROTOCOL_WORKSPACE, Version2Model.STORE_ID);
    }
    
    @Override
    @Extend(extensionAPI=VersionServiceExtension.class,traitAPI=VersionServiceTrait.class)
    public Version createVersion(
            NodeRef nodeRef,
            Map<String, Serializable> versionProperties)
            throws ReservedVersionNameException, AspectMissingException
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("Run as user " + AuthenticationUtil.getRunAsUser());
            logger.debug("Fully authenticated " + AuthenticationUtil.getFullyAuthenticatedUser());
        }
        
        long startTime = System.currentTimeMillis();
        
        int versionNumber = 0; // deprecated (unused)
        
        // Create the version
        Version version = createVersion(nodeRef, versionProperties, versionNumber);
        
        if (logger.isDebugEnabled())
        {
            logger.debug("created version (" + VersionUtil.convertNodeRef(version.getFrozenStateNodeRef()) + ") in " + (System.currentTimeMillis()-startTime) + " ms");
        }
        
        return version;
    }
    
    @Override
    @Extend(extensionAPI=VersionServiceExtension.class,traitAPI=VersionServiceTrait.class)
    public Collection<Version> createVersion(
            Collection<NodeRef> nodeRefs,
            Map<String, Serializable> versionProperties)
            throws ReservedVersionNameException, AspectMissingException
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("Run as user " + AuthenticationUtil.getRunAsUser());
            logger.debug("Fully authenticated " + AuthenticationUtil.getFullyAuthenticatedUser());
        }
        
        /* 
         * Note: we can't control the order of the list, so if we have children and parents in the list and the
         * parents get versioned before the children and the children are not already versioned then the parents
         * child references will be pointing to the node ref, rather than the version history.
         */
        long startTime = System.currentTimeMillis();
        
        Collection<Version> result = new ArrayList<Version>(nodeRefs.size());

        int versionNumber = 0; // deprecated (unused)
        
        // Version each node in the list
        for (NodeRef nodeRef : nodeRefs)
        {
            result.add(createVersion(nodeRef, versionProperties, versionNumber));
        }

        if (logger.isDebugEnabled())
        {
            logger.debug("created version list (" + getVersionStoreReference() + ") in "+ (System.currentTimeMillis()-startTime) +" ms (with " + nodeRefs.size() + " nodes)");
        }
        
        return result;
    }
    
    @Override
    protected Version createVersion(
            NodeRef nodeRef,
            Map<String, Serializable> origVersionProperties,
            int versionNumber)
            throws ReservedVersionNameException
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("Run as user " + AuthenticationUtil.getRunAsUser());
            logger.debug("Fully authenticated " + AuthenticationUtil.getFullyAuthenticatedUser());
        }
        
        long startTime = System.currentTimeMillis();
        
        // Copy the version properties (to prevent unexpected side effects to the caller)
        Map<String, Serializable> versionProperties = new HashMap<String, Serializable>();
        if (origVersionProperties != null)
        {
            versionProperties.putAll(origVersionProperties);
        }
        
        // We don't want either the adding of the Versionable aspect, or the setting
        //  of the version label, to affect the auditable properties on the node that
        //  is being versioned.
        // So, disable the auditable aspect on that node for now
        policyBehaviourFilter.disableBehaviour(nodeRef, ContentModel.ASPECT_AUDITABLE);
        
        // If the version aspect is not there then add it to the 'live' (versioned) node
        if (this.nodeService.hasAspect(nodeRef, ContentModel.ASPECT_VERSIONABLE) == false)
        {
            // Add the versionable aspect to the node
            this.nodeService.addAspect(nodeRef, ContentModel.ASPECT_VERSIONABLE, null);
        }
        
        // Call the policy behaviour
        invokeBeforeCreateVersion(nodeRef);
        
        // version "description" property is added as a standard version property (if not null) rather than a metadata version property
        String versionDescription = (String)versionProperties.get(Version.PROP_DESCRIPTION);
        versionProperties.remove(Version.PROP_DESCRIPTION);
        
        // don't freeze previous version label
        versionProperties.remove(ContentModel.PROP_VERSION_LABEL);
        
        // Check that the supplied additional version properties do not clash with the reserved ones
        VersionUtil.checkVersionPropertyNames(versionProperties.keySet());
        
        // Check the repository for the version history for this node
        NodeRef versionHistoryRef = getVersionHistoryNodeRef(nodeRef);
        NodeRef currentVersionRef = null;
        
        Version currentVersion = null;
        
        if (versionHistoryRef == null)
        {
            // Create the version history
            versionHistoryRef = createVersionHistory(nodeRef);
        }
        else
        {
            // ALF-3962 fix
            // check for corrupted version histories that are marked with version label "0"
            checkForCorruptedVersions(versionHistoryRef, nodeRef);
            
            // Since we have an existing version history we should be able to lookup
            // the current version
            Pair<Boolean, Version> result = getCurrentVersionImpl(versionHistoryRef, nodeRef);
            boolean headVersion = false;
            
            if (result != null)
            {
                currentVersion = result.getSecond();
                headVersion = result.getFirst();
            }
            
            if (currentVersion == null)
            {
                throw new VersionServiceException(MSGID_ERR_NOT_FOUND);
            }
            
            // Need to check that we are not about to create branch since this is not currently supported
            if (! headVersion)
            {
                // belt-and-braces - remove extra check at some point
                // although child assocs should be in ascending time (hence version creation) order
                VersionHistory versionHistory = buildVersionHistory(versionHistoryRef, nodeRef);
                if (versionHistory.getSuccessors(currentVersion).size() == 0)
                {
                    if (logger.isDebugEnabled())
                    {
                        logger.debug("Belt-and-braces: current version does seem to be head version ["+versionHistoryRef+", "+nodeRef+"]");
                    }
                }
                else
                {
                    throw new VersionServiceException(MSGID_ERR_NO_BRANCHES);
                }
            }
        }
        
        // Create the node details
        QName classRef = this.nodeService.getType(nodeRef);
        PolicyScope nodeDetails = new PolicyScope(classRef);
        
        // Get the node details by calling the onVersionCreate policy behaviour
        invokeOnCreateVersion(nodeRef, versionProperties, nodeDetails);
        
        // Calculate the version label
        String versionLabel = invokeCalculateVersionLabel(classRef, currentVersion, versionNumber, versionProperties);
        
        // Extract Type Definition
        QName sourceTypeRef = nodeService.getType(nodeRef);
        
        long nodeDbId = (Long)this.nodeService.getProperty(nodeRef, ContentModel.PROP_NODE_DBID);
        Set<QName> nodeAspects = this.nodeService.getAspects(nodeRef);
        
        // Create the new version node (child of the version history)
        NodeRef newVersionRef = createNewVersion(
                sourceTypeRef,
                versionHistoryRef,
                getStandardVersionProperties(nodeRef, nodeDbId, nodeAspects, versionNumber, versionLabel, versionDescription),
                versionProperties,
                versionNumber,
                nodeDetails);

        if (currentVersionRef == null)
        {
            // Set the new version to be the root version in the version history
            this.dbNodeService.createAssociation(
                    versionHistoryRef,
                    newVersionRef,
                    Version2Model.ASSOC_ROOT_VERSION);
        }

        // Create the version data object
        Version version = getVersion(newVersionRef);

        // Disabling behavior to be able to create properties for a locked node, see ALF-16540
        policyBehaviourFilter.disableBehaviour(nodeRef, ContentModel.ASPECT_LOCKABLE);
        try
        {
            // Set the new version label on the 'live' (versioned) node
            this.nodeService.setProperty(
                    nodeRef,
                    ContentModel.PROP_VERSION_LABEL,
                    version.getVersionLabel());

            // Set the version type (MNT-14681 fix)
            this.nodeService.setProperty(
                    nodeRef,
                    ContentModel.PROP_VERSION_TYPE,
                    version.getVersionType());
        }
        finally
        {
            policyBehaviourFilter.enableBehaviour(nodeRef, ContentModel.ASPECT_LOCKABLE);
        }

        // Re-enable the auditable aspect (if we turned it off earlier)
        policyBehaviourFilter.enableBehaviour(nodeRef, ContentModel.ASPECT_AUDITABLE);
        
        // Invoke the policy behaviour
        invokeAfterCreateVersion(nodeRef, version);

        if (logger.isTraceEnabled())
        {
            logger.trace("created version (" + getVersionStoreReference() + ") " + newVersionRef + " " + (System.currentTimeMillis()-startTime) +" ms");
        }
        
        // Return the data object representing the newly created version
        return version;
    }

    /**
     * Creates a new version history node, applying the root version aspect is required
     *
     * @param nodeRef   the node ref
     * @return          the version history node reference
     */
    protected NodeRef createVersionHistory(NodeRef nodeRef)
    {
        long start = System.currentTimeMillis();
        
        HashMap<QName, Serializable> props = new HashMap<QName, Serializable>();
        props.put(ContentModel.PROP_NAME, nodeRef.getId());
        props.put(Version2Model.PROP_QNAME_VERSIONED_NODE_ID, nodeRef.getId());

        // Create a new version history node
        ChildAssociationRef childAssocRef = this.dbNodeService.createNode(
                getRootNode(),
                Version2Model.CHILD_QNAME_VERSION_HISTORIES,
                QName.createQName(Version2Model.NAMESPACE_URI, nodeRef.getId()),
                Version2Model.TYPE_QNAME_VERSION_HISTORY,
                props);
        
        if (logger.isTraceEnabled())
        {
            logger.trace("created version history nodeRef: " + childAssocRef.getChildRef() + " for " + nodeRef + " in "+(System.currentTimeMillis()-start)+" ms");
        }
        
        return childAssocRef.getChildRef();
    }

    @Override
    @Extend(extensionAPI=VersionServiceExtension.class,traitAPI=VersionServiceTrait.class)
    public VersionHistory getVersionHistory(NodeRef nodeRef)
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("Run as user " + AuthenticationUtil.getRunAsUser());
            logger.debug("Fully authenticated " + AuthenticationUtil.getFullyAuthenticatedUser());
        }
        
        VersionHistory versionHistory = null;
        
        // Get the version history regardless of whether the node is still 'live' or not
        NodeRef versionHistoryRef = getVersionHistoryNodeRef(nodeRef);
        if (versionHistoryRef != null)
        {
            versionHistory = buildVersionHistory(versionHistoryRef, nodeRef);
        }
        return versionHistory;
    }

    @Override
    @Extend(extensionAPI=VersionServiceExtension.class,traitAPI=VersionServiceTrait.class)
    public Version getCurrentVersion(NodeRef nodeRef)
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("Run as user " + AuthenticationUtil.getRunAsUser());
            logger.debug("Fully authenticated " + AuthenticationUtil.getFullyAuthenticatedUser());
        }
        
        Version version = null;
        
        // get the current version, if the 'live' (versioned) node has the "versionable" aspect
        if (this.nodeService.hasAspect(nodeRef, ContentModel.ASPECT_VERSIONABLE) == true)
        {
            NodeRef versionHistoryRef = getVersionHistoryNodeRef(nodeRef);
            if (versionHistoryRef != null)
            {
                Pair<Boolean, Version> result = getCurrentVersionImpl(versionHistoryRef, nodeRef);
                if (result != null)
                {
                    version = result.getSecond();
                }
            }
        }
        
        return version;
    }

    /**
     * Get a map containing the standard list of version properties populated.
     *
     * @param nodeRef               the node reference
     * @return                      the standard version properties
     */
    protected Map<QName, Serializable> getStandardVersionProperties(NodeRef nodeRef, long nodeDbId, Set<QName> nodeAspects, int versionNumber, String versionLabel, String versionDescription)
    {
        Map<QName, Serializable> result = new HashMap<QName, Serializable>(10);

        // deprecated (unused)
        //result.put(Version2Model.PROP_QNAME_VERSION_NUMBER, versionNumber);
        
        // Set the version label
        result.put(Version2Model.PROP_QNAME_VERSION_LABEL, versionLabel);
        
        // Set the version description (can be null)
        result.put(Version2Model.PROP_QNAME_VERSION_DESCRIPTION, versionDescription);
        
        // Set the versionable node store id
        result.put(Version2Model.PROP_QNAME_FROZEN_NODE_REF, nodeRef);
        
        // Set the versionable node store id
        result.put(Version2Model.PROP_QNAME_FROZEN_NODE_DBID, nodeDbId);
        
        return result;
    }

    /**
     * Creates a new version node, setting the properties both calculated and specified.
     *
     * @param sourceTypeRef  the reference to the node being versioned
     * @param versionHistoryRef   version history node reference
     * @param standardVersionProperties   version properties
     * @param versionProperties   version properties
     * @param versionNumber          the version number
     * @param nodeDetails          PolicyScope
     * @return                    the version node reference
     */
    protected NodeRef createNewVersion(
            QName sourceTypeRef,
            NodeRef versionHistoryRef,
            Map<QName, Serializable> standardVersionProperties,
            Map<String, Serializable> versionProperties,
            int versionNumber,
            PolicyScope nodeDetails)
    {
        ChildAssociationRef childAssocRef = null;
        
        // Disable auto-version behaviour
        this.policyBehaviourFilter.disableBehaviour(ContentModel.ASPECT_VERSIONABLE);
        
        this.policyBehaviourFilter.disableBehaviour(ContentModel.ASPECT_MULTILINGUAL_DOCUMENT);
        this.policyBehaviourFilter.disableBehaviour(ContentModel.TYPE_MULTILINGUAL_CONTAINER);
        
        NodeRef versionNodeRef = null;
        
        try
        {
            // "copy" type and properties
            childAssocRef = this.dbNodeService.createNode(
                    versionHistoryRef, 
                    Version2Model.CHILD_QNAME_VERSIONS,
                    QName.createQName(Version2Model.NAMESPACE_URI, Version2Model.CHILD_VERSIONS+"-"+versionNumber), // TODO - testing - note: all children (of a versioned node) will have the same version number, maybe replace with a version sequence of some sort 001-...00n
                    sourceTypeRef, 
                    nodeDetails.getProperties());
            if (isUseVersionAssocIndex())
            {
                nodeService.setChildAssociationIndex(childAssocRef, getAllVersions(versionHistoryRef).size());
            }
            versionNodeRef = childAssocRef.getChildRef();

            // NOTE: special ML case - see also MultilingualContentServiceImpl.makeMLContainer
            if (sourceTypeRef.equals(ContentModel.TYPE_MULTILINGUAL_CONTAINER))
            {
                // Set the permissions to allow anything by anyone
                permissionService.setPermission(
                        versionNodeRef,
                        PermissionService.ALL_AUTHORITIES,
                        PermissionService.ALL_PERMISSIONS, true);
                permissionService.setPermission(
                        versionNodeRef,
                        AuthenticationUtil.getGuestUserName(),
                        PermissionService.ALL_PERMISSIONS, true);
            }
            
            // add aspect with the standard version properties to the 'version' node
            nodeService.addAspect(versionNodeRef, Version2Model.ASPECT_VERSION, standardVersionProperties);
            
            // store the meta data
            storeVersionMetaData(versionNodeRef, versionProperties);
            
            freezeChildAssociations(versionNodeRef, nodeDetails.getChildAssociations());
            freezeAssociations(versionNodeRef, nodeDetails.getAssociations());
            freezeAspects(nodeDetails, versionNodeRef, nodeDetails.getAspects());
        }
        finally
        {
            // Enable auto-version behaviour
            this.policyBehaviourFilter.enableBehaviour(ContentModel.ASPECT_VERSIONABLE);
            
            this.policyBehaviourFilter.enableBehaviour(ContentModel.ASPECT_MULTILINGUAL_DOCUMENT);
            this.policyBehaviourFilter.enableBehaviour(ContentModel.TYPE_MULTILINGUAL_CONTAINER);
        }
        
        // If the auditable aspect is not there then add it to the 'version' node (after original aspects have been frozen)
        if (dbNodeService.hasAspect(versionNodeRef, ContentModel.ASPECT_AUDITABLE) == false)
        {
            dbNodeService.addAspect(versionNodeRef, ContentModel.ASPECT_AUDITABLE, null);
        }
        
        if (logger.isTraceEnabled())
        {
            logger.trace("newVersion created (" + versionNumber + ") " + versionNodeRef);
        }
        
        // Return the created node reference
        return versionNodeRef;
    }

    /**
     * Store the version meta data
     *
     * @param versionNodeRef        the version node reference
     * @param versionProperties     the version properties
     */
    private void storeVersionMetaData(NodeRef versionNodeRef, Map<String, Serializable> versionProperties)
    {
        // TODO - these are stored as "residual" properties (ie. without property type) - see also NodeBrowser
        //      - need to review, eg. how can we store arbitrary map of metadata properties, that could be indexed/searched (if configured against versionStore)    
        for (Map.Entry<String, Serializable> entry : versionProperties.entrySet())
        {
            dbNodeService.setProperty(versionNodeRef, QName.createQName(Version2Model.NAMESPACE_URI, Version2Model.PROP_METADATA_PREFIX+entry.getKey()), entry.getValue());
        }
    }

    /**
     * Freeze the aspects
     *
     * @param nodeDetails      the node details
     * @param versionNodeRef   the version node reference
     * @param aspects          the set of aspects
     */
    private void freezeAspects(PolicyScope nodeDetails, NodeRef versionNodeRef, Set<QName> aspects)
    {
        for (QName aspect : aspects)
        {
            if (logger.isTraceEnabled())
            {
                logger.trace("freezeAspect: " + versionNodeRef + " " + aspect);
            }
            
            if (aspect.equals(ContentModel.ASPECT_AUDITABLE))
            {
                // freeze auditable aspect properties (eg. created, creator, modifed, modifier, accessed)
                for (Map.Entry<QName, Serializable> entry : nodeDetails.getProperties(aspect).entrySet())
                {
                    if (entry.getKey().equals(ContentModel.PROP_CREATOR))
                    {
                        dbNodeService.setProperty(versionNodeRef, Version2Model.PROP_QNAME_FROZEN_CREATOR, entry.getValue());
                    }
                    else if (entry.getKey().equals(ContentModel.PROP_CREATED))
                    {
                        dbNodeService.setProperty(versionNodeRef, Version2Model.PROP_QNAME_FROZEN_CREATED, entry.getValue());
                    }
                    else if (entry.getKey().equals(ContentModel.PROP_MODIFIER))
                    {
                        dbNodeService.setProperty(versionNodeRef, Version2Model.PROP_QNAME_FROZEN_MODIFIER, entry.getValue());
                    }
                    else if (entry.getKey().equals(ContentModel.PROP_MODIFIED))
                    {
                        dbNodeService.setProperty(versionNodeRef, Version2Model.PROP_QNAME_FROZEN_MODIFIED, entry.getValue());
                    }
                    else if (entry.getKey().equals(ContentModel.PROP_ACCESSED))
                    {
                        dbNodeService.setProperty(versionNodeRef, Version2Model.PROP_QNAME_FROZEN_ACCESSED, entry.getValue());
                    }
                    else
                    {
                        throw new AlfrescoRuntimeException("Unexpected auditable property: " + entry.getKey());
                    }
                }
            }
            else
            {
                // Freeze the details of the aspect
                dbNodeService.addAspect(versionNodeRef, aspect, nodeDetails.getProperties(aspect));
            }

            // ALF-9638: Freeze the aspect specific associations
            freezeChildAssociations(versionNodeRef, nodeDetails.getChildAssociations(aspect));
            freezeAssociations(versionNodeRef, nodeDetails.getAssociations(aspect));            
        }
    }
    
    /**
     * Freeze child associations
     *
     * @param versionNodeRef       the version node reference
     * @param childAssociations    the child associations
     */
    private void freezeChildAssociations(NodeRef versionNodeRef, List<ChildAssociationRef> childAssociations)
    {
        for (ChildAssociationRef childAssocRef : childAssociations)
        {
            HashMap<QName, Serializable> properties = new HashMap<QName, Serializable>();
            
            NodeRef childRef = childAssocRef.getChildRef();
            
            QName sourceTypeRef = nodeService.getType(childRef);
            
            // Set the reference property to point to the child node
            properties.put(ContentModel.PROP_REFERENCE, childRef);
            
            // Create child version reference
            dbNodeService.createNode(
                    versionNodeRef,
                    childAssocRef.getTypeQName(),
                    childAssocRef.getQName(),
                    sourceTypeRef,
                    properties);
        }
    }
    
    /**
     * Freeze associations
     *
     * @param versionNodeRef   the version node reference
     * @param associations     the list of associations
     * 
     * @since 3.3 (Ent)
     */
    private void freezeAssociations(NodeRef versionNodeRef, List<AssociationRef> associations)
    {
        for (AssociationRef targetAssocRef : associations)
        {
            HashMap<QName, Serializable> properties = new HashMap<QName, Serializable>();
            
            QName sourceTypeRef = nodeService.getType(targetAssocRef.getSourceRef());
            
            NodeRef targetRef = targetAssocRef.getTargetRef();
            
            // Set the reference property to point to the target node
            properties.put(ContentModel.PROP_REFERENCE, targetRef);
            properties.put(Version2Model.PROP_QNAME_ASSOC_DBID, targetAssocRef.getId());
            
            // Create peer version reference
            dbNodeService.createNode(
                    versionNodeRef,
                    Version2Model.CHILD_QNAME_VERSIONED_ASSOCS,
                    targetAssocRef.getTypeQName(),
                    sourceTypeRef,
                    properties);
        }
    }

    /**
     * Gets all versions in version history
     * 
     * @param versionHistoryRef the version history nodeRef
     * @return list of all versions
     */
    protected List<Version> getAllVersions(NodeRef versionHistoryRef)
    {
        List<ChildAssociationRef> versionAssocs = getVersionAssocs(versionHistoryRef, true);
        
        List<Version> versions = new ArrayList<Version>(versionAssocs.size());
        
        for (ChildAssociationRef versionAssoc : versionAssocs)
        {
            versions.add(getVersion(versionAssoc.getChildRef()));
        }
        
        return versions;
    }
    
    private List<ChildAssociationRef> getVersionAssocs(NodeRef versionHistoryRef, boolean preLoad)
    {
        // note: resultant list is ordered by (a) explicit index and (b) association creation time
        return dbNodeService.getChildAssocs(versionHistoryRef, Version2Model.CHILD_QNAME_VERSIONS, RegexQNamePattern.MATCH_ALL, preLoad);
    }
    
    /**
     * Builds a version history object from the version history reference.
     * <p>
     * The node ref is passed to enable the version history to be scoped to the
     * appropriate branch in the version history.
     *
     * @param versionHistoryRef  the node ref for the version history
     * @param nodeRef            the node reference
     * @return                   a constructed version history object
     */
    @Override
    protected VersionHistory buildVersionHistory(NodeRef versionHistoryRef, NodeRef nodeRef)
    {
        VersionHistory versionHistory = null;
        
        // List of versions with current one last and root one first.
        List<Version> versions = getAllVersions(versionHistoryRef);
        
        if (versionComparatorDesc != null)
        {
            Collections.sort(versions, Collections.reverseOrder(versionComparatorDesc));
        }
        
        // Build the version history object
        boolean isRoot = true;
        Version preceeding = null;
        for (Version version : versions)
        {
            if (isRoot == true)
            {
                versionHistory = new VersionHistoryImpl(version, versionComparatorDesc);
                isRoot = false;
            }
            else
            {
                ((VersionHistoryImpl)versionHistory).addVersion(version, preceeding);
            }
            preceeding = version;
        }
        
        return versionHistory;
    }
    
    /**
     * Constructs the a version object to contain the version information from the version node ref.
     *
     * @param versionRef  the version reference
     * @return            object containing verison data
     */
    @Override
    protected Version getVersion(NodeRef versionRef)
    {
        if (versionRef == null)
        {
            return null;
        }
        Map<String, Serializable> versionProperties = new HashMap<String, Serializable>();
        
        // Get the standard node details and get the meta data
        Map<QName, Serializable> nodeProperties = this.dbNodeService.getProperties(versionRef);
        
        if (logger.isTraceEnabled())
        {
            logger.trace("getVersion: " + versionRef + " nodeProperties=\n" + nodeProperties.keySet());
        }
        
        // TODO consolidate with VersionUtil.convertFrozenToOriginalProps
        
        for (QName key : nodeProperties.keySet())
        {
            Serializable value = nodeProperties.get(key);
            
            String keyName = key.getLocalName();
            int idx = keyName.indexOf(Version2Model.PROP_METADATA_PREFIX);
            if (idx == 0)
            {
                // versioned metadata property - additional (optional) metadata, set during versioning
                versionProperties.put(keyName.substring(Version2Model.PROP_METADATA_PREFIX.length()), value);
            }
            else
            {
               if (key.equals(Version2Model.PROP_QNAME_VERSION_DESCRIPTION))
               {
                   versionProperties.put(Version.PROP_DESCRIPTION, (String)value);
               }
               else if (key.equals(Version2Model.PROP_QNAME_VERSION_LABEL))
               {
                   versionProperties.put(VersionBaseModel.PROP_VERSION_LABEL, (String)value);
               }
               else
               {
                   if (keyName.equals(Version.PROP_DESCRIPTION) || 
                       keyName.equals(VersionBaseModel.PROP_VERSION_LABEL))
                   {
                       // ignore reserved localname (including cm:description, cm:versionLabel)
                   }
                   else
                   {
                       // all other properties
                       versionProperties.put(keyName, value);
                   }
               }
            }
        }
        
        // Create and return the version object
        NodeRef newNodeRef = new NodeRef(new StoreRef(Version2Model.STORE_PROTOCOL, Version2Model.STORE_ID), versionRef.getId());
        Version result = new VersionImpl(versionProperties, newNodeRef);
        
        if (logger.isTraceEnabled())
        {
            logger.trace("getVersion: " + versionRef + " versionProperties=\n" + versionProperties.keySet());
        }
        
        // done
        return result;
    }

    /**
     * Gets a reference to the version history node for a given 'real' node.
     *
     * @param nodeRef  a node reference
     * @return         a reference to the version history node, null of none
     */
    @Override
    protected NodeRef getVersionHistoryNodeRef(NodeRef nodeRef)
    {
        // assume noderef is a 'live' node
        NodeRef vhNodeRef = this.dbNodeService.getChildByName(getRootNode(), Version2Model.CHILD_QNAME_VERSION_HISTORIES, nodeRef.getId());
        
        // DEPRECATED: for backwards compatibility, in case of a version node (eg. via getCurrentVersion) can lookup 'live' node via UUID
        if (vhNodeRef == null)
        {
            if (nodeService.exists(nodeRef))
            {
                vhNodeRef = this.dbNodeService.getChildByName(getRootNode(), Version2Model.CHILD_QNAME_VERSION_HISTORIES, (String) nodeService.getProperty(nodeRef, ContentModel.PROP_NODE_UUID));
            }
        }

        return vhNodeRef;
    }

    /**
     * Gets current version of the passed node reference. The node reference is expected to be the 'live' node.
     *
     * This uses the version label as a mechanism for looking up the version node.
     */
    private Pair<Boolean, Version> getCurrentVersionImpl(NodeRef versionHistoryRef, NodeRef nodeRef)
    {
        // The noderef should not be a version type node. 
        if (nodeRef.getStoreRef().getIdentifier().contains(Version2Model.STORE_ID))
        {
             throw new IllegalArgumentException("The node reference " + nodeRef + " is pointing to a version node, when a reference to a live node is expected.");
        }
        
        Pair<Boolean, Version> result = null;
        
        String versionLabel = (String)this.nodeService.getProperty(nodeRef, ContentModel.PROP_VERSION_LABEL);
        
        // Note: resultant list is ordered by (a) explicit index and (b) association creation time
        List<ChildAssociationRef> versionAssocs = getVersionAssocs(versionHistoryRef, false);
        
        // Current version should be head version (since no branching)
        int versionCount = versionAssocs.size();
        for (int i = versionCount; i > 0; i--)
        {
            ChildAssociationRef versionAssoc = versionAssocs.get(i-1);
            
            String tempLabel = (String)this.dbNodeService.getProperty(versionAssoc.getChildRef(), Version2Model.PROP_QNAME_VERSION_LABEL);
            if (tempLabel != null && tempLabel.equals(versionLabel) == true)
            {
                boolean headVersion = (i == versionCount);
                
                if (!headVersion)
                {
                    throw new ConcurrencyFailureException("Unexpected: current version does not appear to be 1st version in the list ["+versionHistoryRef+", "+nodeRef+"]");
                }
                
                result = new Pair<Boolean, Version>(headVersion, getVersion(versionAssoc.getChildRef()));
                break;
            }
        }
        
        return result;
    }
    
    /**
     * Check if versions are marked with invalid version label, if true > apply default serial version label (e.g. "1.0", "1.1") 
     * 
     * @param versionHistory a version history node reference
     * @param nodeRef a node reference
     */
    private void checkForCorruptedVersions(NodeRef versionHistory, NodeRef nodeRef)
    {
        // get the current version label in live store
        String versionLabel = (String) this.nodeService.getProperty(nodeRef, ContentModel.PROP_VERSION_LABEL);

        if (versionLabel != null && versionLabel.equals("0"))
        {
            // need to correct version labels
            List<Version> versions = getAllVersions(versionHistory);

            // sort versions by node id
            Collections.sort(versions, new Comparator<Version>()
            {

                public int compare(Version v1, Version v2)
                {
                    int result = v1.getFrozenModifiedDate().compareTo(v2.getFrozenModifiedDate());
                    if (result == 0)
                    {
                        Long dbid1 = (Long)nodeService.getProperty(v1.getFrozenStateNodeRef(), ContentModel.PROP_NODE_DBID);
                        Long dbid2 = (Long)nodeService.getProperty(v2.getFrozenStateNodeRef(), ContentModel.PROP_NODE_DBID);
                        
                        if (dbid1 != null && dbid2 != null)
                        {
                            result = dbid1.compareTo(dbid2);
                        }
                        else
                        {
                            result = 0;
                            
                            if (logger.isWarnEnabled())
                            {
                                logger.warn("node-dbid property is missing for versions: " + v1.toString() + " or " + v2.toString());
                            }
                        }
                    }
                    return result;
                }

            });

            SerialVersionLabelPolicy serialVersionLabelPolicy = new SerialVersionLabelPolicy();
            QName classRef = this.nodeService.getType(nodeRef);
            Version preceedingVersion = null;

            for (Version version : versions)
            {
                // re-calculate version label
                versionLabel = serialVersionLabelPolicy.calculateVersionLabel(classRef, preceedingVersion, 0, version.getVersionProperties());

                // update version with new version label
                NodeRef versionNodeRef = new NodeRef(StoreRef.PROTOCOL_WORKSPACE, version.getFrozenStateNodeRef().getStoreRef().getIdentifier(), version.getFrozenStateNodeRef()
                        .getId());
                this.dbNodeService.setProperty(versionNodeRef, Version2Model.PROP_QNAME_VERSION_LABEL, versionLabel);
                
                version.getVersionProperties().put(VersionBaseModel.PROP_VERSION_LABEL, versionLabel);

                // remember preceding version
                preceedingVersion = version;
            }

            // update current version label in live store
            this.nodeService.setProperty(nodeRef, ContentModel.PROP_VERSION_LABEL, versionLabel);
        }
    }

    @Override
    @Extend(extensionAPI=VersionServiceExtension.class,traitAPI=VersionServiceTrait.class)
    public void revert(NodeRef nodeRef)
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("Run as user " + AuthenticationUtil.getRunAsUser());
            logger.debug("Fully authenticated " + AuthenticationUtil.getFullyAuthenticatedUser());
        }
        
        revert(nodeRef, getCurrentVersion(nodeRef), true);
    }

    @Override
    @Extend(extensionAPI=VersionServiceExtension.class,traitAPI=VersionServiceTrait.class)
    public void revert(NodeRef nodeRef, boolean deep)
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("Run as user " + AuthenticationUtil.getRunAsUser());
            logger.debug("Fully authenticated " + AuthenticationUtil.getFullyAuthenticatedUser());
        }
        
        revert(nodeRef, getCurrentVersion(nodeRef), deep);
    }

    @Override
    @Extend(extensionAPI=VersionServiceExtension.class,traitAPI=VersionServiceTrait.class)
    public void revert(NodeRef nodeRef, Version version)
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("Run as user " + AuthenticationUtil.getRunAsUser());
            logger.debug("Fully authenticated " + AuthenticationUtil.getFullyAuthenticatedUser());
        }
        
        revert(nodeRef, version, true);
    }

    @Override
    @Extend(extensionAPI=VersionServiceExtension.class,traitAPI=VersionServiceTrait.class)
    public void revert(NodeRef nodeRef, Version version, boolean deep)
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("Run as user " + AuthenticationUtil.getRunAsUser());
            logger.debug("Fully authenticated " + AuthenticationUtil.getFullyAuthenticatedUser());
        }
        
        if(logger.isDebugEnabled())
        {
             logger.debug("revert nodeRef:" + nodeRef);
        }
        
        // Check the mandatory parameters
        ParameterCheck.mandatory("nodeRef", nodeRef);
        ParameterCheck.mandatory("version", version);

        // Cross check that the version provided relates to the node reference provided
        if (nodeRef.getId().equals(((NodeRef)version.getVersionProperty(Version2Model.PROP_FROZEN_NODE_REF)).getId()) == false)
        {
            // Error since the version provided does not correspond to the node reference provided
            throw new VersionServiceException(MSGID_ERR_REVERT_MISMATCH);
        }

        // Turn off any auto-version policy behaviours
        this.policyBehaviourFilter.disableBehaviour(nodeRef, ContentModel.ASPECT_VERSIONABLE);
        try
        {                
            // The current (old) values
            Map<QName, Serializable> oldProps = this.nodeService.getProperties(nodeRef);
            Set<QName> oldAspectQNames = this.nodeService.getAspects(nodeRef);
            QName oldNodeTypeQName = nodeService.getType(nodeRef);
            // Store the current version label
            String currentVersionLabel = (String)this.nodeService.getProperty(nodeRef, ContentModel.PROP_VERSION_LABEL);

            // The frozen (which will become new) values
            // Get the node that represents the frozen state
            NodeRef versionNodeRef = version.getFrozenStateNodeRef();
                boolean needToRestoreDiscussion = !this.nodeService.hasAspect(versionNodeRef, ForumModel.ASPECT_DISCUSSABLE) 
                        && this.nodeService.hasAspect(nodeRef, ForumModel.ASPECT_DISCUSSABLE);
    
                Map<QName, Serializable> forumProps = null;
                
                // always collect forum properties
                Map<QName, Serializable> currentVersionProp = this.nodeService.getProperties(nodeRef);
                forumProps = new HashMap<QName, Serializable>();
                for (QName key : currentVersionProp.keySet())
                {
                    if (key.getNamespaceURI().equals(NamespaceService.FORUMS_MODEL_1_0_URI))
                    {
                        forumProps.put(key, currentVersionProp.get(key));
                    }
                }
                    
            Map<QName, Serializable> newProps = this.nodeService.getProperties(versionNodeRef);
            VersionUtil.convertFrozenToOriginalProps(newProps);
            Set<QName> newAspectQNames = this.nodeService.getAspects(versionNodeRef);
                QName newNodeTypeQName = nodeService.getType(versionNodeRef);

            // RevertDetails - given to policy behaviours
            VersionRevertDetailsImpl revertDetails = new VersionRevertDetailsImpl();
            revertDetails.setNodeRef(nodeRef);
                revertDetails.setNodeType(newNodeTypeQName);
            
            //  Do we want to maintain any existing property values?
            Collection<QName> propsToLeaveAlone = new ArrayList<QName>();
            Collection<QName> assocsToLeaveAlone = new ArrayList<QName>();

                // The VersionRevertCallback was added in r50122 on HEAD-QA branch in ACE-1001
                // If it is required to preserve this callback when the type is changed,
                // this part should be reimplemented.
                // see MNT-14688
                if (newNodeTypeQName.equals(oldNodeTypeQName))
                {
                    // The node did not change the type, check the associations
                    TypeDefinition typeDef = dictionaryService.getType(oldNodeTypeQName);
                    if(typeDef != null)
                    {
                        for(QName assocName : typeDef.getAssociations().keySet())
                        {
                            if(getRevertAssocAction(oldNodeTypeQName, assocName, revertDetails) == RevertAssocAction.IGNORE)
                            {
                                assocsToLeaveAlone.add(assocName);
                            }
                        }
                    }
                }
            
            for (QName aspect : oldAspectQNames)
            {
                AspectDefinition aspectDef = dictionaryService.getAspect(aspect);
                if(aspectDef != null)
                {
                    if (getRevertAspectAction(aspect, revertDetails) == RevertAspectAction.IGNORE)
                    {
                         propsToLeaveAlone.addAll(aspectDef.getProperties().keySet());
                    }
                    for(QName assocName : aspectDef.getAssociations().keySet())
                    {
                        if(getRevertAssocAction(aspect, assocName, revertDetails) == RevertAssocAction.IGNORE)
                        {
                            assocsToLeaveAlone.addAll(aspectDef.getAssociations().keySet());
                        }
                    }
                }
            }
            
            for(QName prop : propsToLeaveAlone)
            {
                if(oldProps.containsKey(prop))
                {
                    newProps.put(prop, oldProps.get(prop));
                }
            }
            
            this.nodeService.setProperties(nodeRef, newProps);
                
                //Restore forum properties
                this.nodeService.addProperties(nodeRef, forumProps);

                // Restore the type
                this.nodeService.setType(nodeRef, newNodeTypeQName);

            Set<QName> aspectsToRemove = new HashSet<QName>(oldAspectQNames);
            aspectsToRemove.removeAll(newAspectQNames);
            
            Set<QName> aspectsToAdd = new HashSet<QName>(newAspectQNames);
            aspectsToAdd.removeAll(oldAspectQNames);
            
            // add aspects that are not on the current node
            for (QName aspect : aspectsToAdd)
            {
                if (getRevertAspectAction(aspect, revertDetails) != RevertAspectAction.IGNORE)
                {
                    this.nodeService.addAspect(nodeRef, aspect, null);
                }
                }
                // Don't remove forum aspects
                if (needToRestoreDiscussion)
                {
                    Set<QName> ignoredAspects = new HashSet<QName>();
                    for (QName aspectForCheck : aspectsToRemove)
                    {
                        if (aspectForCheck.getNamespaceURI().equals(NamespaceService.FORUMS_MODEL_1_0_URI))
                        {
                            ignoredAspects.add(aspectForCheck);
                        }
                    }
                    aspectsToRemove.removeAll(ignoredAspects);
            }
            
            // remove aspects that are not on the frozen node
            for (QName aspect : aspectsToRemove)
            {
                if (getRevertAspectAction(aspect, revertDetails) != RevertAspectAction.IGNORE)
                {
                    this.nodeService.removeAspect(nodeRef, aspect);
                }
            }
              
            // Re-add the versionable aspect to the reverted node
            if (this.nodeService.hasAspect(nodeRef, ContentModel.ASPECT_VERSIONABLE) == false)
            {
                this.nodeService.addAspect(nodeRef, ContentModel.ASPECT_VERSIONABLE, null);
            }

            // Re-set the version label property (since it should not be modified from the original)
            this.nodeService.setProperty(nodeRef, ContentModel.PROP_VERSION_LABEL, currentVersionLabel);

            // Add/remove the child nodes
            List<ChildAssociationRef> children = new ArrayList<ChildAssociationRef>(this.nodeService.getChildAssocs(nodeRef));
            List<ChildAssociationRef> versionedChildren = this.nodeService.getChildAssocs(versionNodeRef);
            for (ChildAssociationRef versionedChild : versionedChildren)
            {
                if (children.contains(versionedChild) == false)
                {
                    NodeRef childRef = null;
                    ChildAssociationRef assocToKeep = null;
                    if (this.nodeService.exists(versionedChild.getChildRef()) == true)
                    {
                        // The node was a primary child of the parent, but that is no longer the case.  Despite this
                        // the node still exits so this means it has been moved.
                        // The best thing to do in this situation will be to re-add the node as a child, but it will not
                        // be a primary child
                        String childRefName = (String) this.nodeService.getProperty(versionedChild.getChildRef(), ContentModel.PROP_NAME);
                        childRef = this.nodeService.getChildByName(nodeRef, versionedChild.getTypeQName(), childRefName);
                        // we can faced with association that allow duplicate names
                        if (childRef == null)
                        {
                            List<ChildAssociationRef> allAssocs = nodeService.getParentAssocs(versionedChild.getChildRef(), versionedChild.getTypeQName(), RegexQNamePattern.MATCH_ALL);
                            for (ChildAssociationRef assocToCheck : allAssocs)
                            {
                                if (children.contains(assocToCheck))
                                {
                                    childRef = assocToCheck.getChildRef();
                                    assocToKeep = assocToCheck;
                                    break;
                                }
                            }
                        }
                        if (childRef == null )
                        {
                            childRef = this.nodeService.addChild(nodeRef, versionedChild.getChildRef(), versionedChild.getTypeQName(), versionedChild.getQName()).getChildRef();
                         }
                    }
                    else
                    {
                        if (versionedChild.isPrimary() == true)
                        {
                            // Only try to restore missing children if we are doing a deep revert
                            // Look and see if we have a version history for the child node
                            if (deep == true && getVersionHistoryNodeRef(versionedChild.getChildRef()) != null)
                            {
                                // We're going to try and restore the missing child node and recreate the assoc
                                childRef = restore(
                                   versionedChild.getChildRef(),
                                   nodeRef,
                                   versionedChild.getTypeQName(),
                                   versionedChild.getQName());
                            }
                            // else the deleted child did not have a version history so we can't restore the child
                            // and so we can't revert the association
                        }
                        
                        // else
                        // Since this was never a primary assoc and the child has been deleted we won't recreate
                        // the missing node as it was never owned by the node and we wouldn't know where to put it.
                    }
                    if (childRef != null)
                    {
                        if (assocToKeep != null)
                        {
                            children.remove(assocToKeep);
                        }
                        else
                        {
                            children.remove(nodeService.getPrimaryParent(childRef));
                        }
                    }
                }
                else
                {
                    children.remove(versionedChild);
                } 
            }
                // Don't remove forum children
                if (needToRestoreDiscussion)
                {
                    List<ChildAssociationRef> ignoredChildren = new ArrayList<ChildAssociationRef>();
                    for (ChildAssociationRef childForCheck : children)
                    {
                        if (childForCheck.getTypeQName().getNamespaceURI().equals(NamespaceService.FORUMS_MODEL_1_0_URI))
                        {
                            ignoredChildren.add(childForCheck);
                        }
                    }
                    children.removeAll(ignoredChildren);
                }
            for (ChildAssociationRef ref : children)
            {
                if (!assocsToLeaveAlone.contains(ref.getTypeQName()))
                {
                    this.nodeService.removeChild(nodeRef, ref.getChildRef());
                }
            }
            
            // Add/remove the target associations
            for (AssociationRef assocRef : this.nodeService.getTargetAssocs(nodeRef, RegexQNamePattern.MATCH_ALL))
            {
                if (!assocsToLeaveAlone.contains(assocRef.getTypeQName()))
                {
                    this.nodeService.removeAssociation(assocRef.getSourceRef(), assocRef.getTargetRef(), assocRef.getTypeQName());
                }
            }
            for (AssociationRef versionedAssoc : this.nodeService.getTargetAssocs(versionNodeRef, RegexQNamePattern.MATCH_ALL))
            {
                if (!assocsToLeaveAlone.contains(versionedAssoc.getTypeQName()))
                {

                    if (this.nodeService.exists(versionedAssoc.getTargetRef()) == true)
                    {
                        this.nodeService.createAssociation(nodeRef, versionedAssoc.getTargetRef(), versionedAssoc.getTypeQName());
                    }
                }
                
                // else
                // Since the target of the assoc no longer exists we can't recreate the assoc
            }
        }
        finally
        {
            // Turn auto-version policies back on
            this.policyBehaviourFilter.enableBehaviour(nodeRef, ContentModel.ASPECT_VERSIONABLE);
        }
        
        invokeAfterVersionRevert(nodeRef, version);
    }

    @Override
    @Extend(extensionAPI=VersionServiceExtension.class,traitAPI=VersionServiceTrait.class)
    public NodeRef restore(
                NodeRef nodeRef,
                NodeRef parentNodeRef,
                QName assocTypeQName,
                QName assocQName)
     {
         return restore(nodeRef, parentNodeRef, assocTypeQName, assocQName, true);
     }

    @Override
    @Extend(extensionAPI=VersionServiceExtension.class,traitAPI=VersionServiceTrait.class)
    public NodeRef restore(
            NodeRef nodeRef,
            NodeRef parentNodeRef,
            QName assocTypeQName,
            QName assocQName,
            boolean deep)
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("Run as user " + AuthenticationUtil.getRunAsUser());
            logger.debug("Fully authenticated " + AuthenticationUtil.getFullyAuthenticatedUser());
        }
        
        NodeRef restoredNodeRef = null;

        // Check that the node does not exist
        if (this.nodeService.exists(nodeRef) == true)
        {
            // Error since you can not restore a node that already exists
            throw new VersionServiceException(MSGID_ERR_RESTORE_EXISTS, new Object[]{nodeRef.toString()});
        }

        // Try and get the version details that we want to restore to
        Version version = getHeadVersion(nodeRef);
        if (version == null)
        {
            // Error since there is no version information available to restore the node from
            throw new VersionServiceException(MSGID_ERR_RESTORE_NO_VERSION, new Object[]{nodeRef.toString()});
        }

        // Set the uuid of the new node
        Map<QName, Serializable> props = new HashMap<QName, Serializable>(1);
        props.put(ContentModel.PROP_NODE_UUID, ((NodeRef)version.getVersionProperty(Version2Model.PROP_FROZEN_NODE_REF)).getId());
        props.put(ContentModel.PROP_VERSION_LABEL, version.getVersionLabel()); 

        // Get the type of the frozen node
        QName type = (QName)dbNodeService.getType(VersionUtil.convertNodeRef(version.getFrozenStateNodeRef()));

        // Disable auto-version behaviour
        this.policyBehaviourFilter.disableBehaviour(ContentModel.ASPECT_VERSIONABLE);
        try
        {
            // Create the restored node
            restoredNodeRef = this.nodeService.createNode(
                    parentNodeRef,
                    assocTypeQName,
                    assocQName,
                    type,
                    props).getChildRef();
        }
        finally
        {
            // Enable auto-version behaviour
            this.policyBehaviourFilter.enableBehaviour(ContentModel.ASPECT_VERSIONABLE);
        }

        // Now we need to revert the newly restored node
        revert(restoredNodeRef, version, deep);

        return restoredNodeRef;
    }

    /**
     * Get the head version given a node reference
     *
     * @param nodeRef   the node reference
     * @return          the 'head' version
     */
    private Version getHeadVersion(NodeRef nodeRef)
    {
        NodeRef versionHistoryNodeRef = getVersionHistoryNodeRef(nodeRef);
        
        Version headVersion = null;
        if (versionHistoryNodeRef != null)
        {
            VersionHistory versionHistory = buildVersionHistory(versionHistoryNodeRef, nodeRef);
            if (versionHistory != null)
            {
                headVersion = versionHistory.getHeadVersion();
            }
        }
        return headVersion;
    }

    @Override
    @Extend(extensionAPI=VersionServiceExtension.class,traitAPI=VersionServiceTrait.class)
    public void deleteVersionHistory(NodeRef nodeRef)
                                     throws AspectMissingException
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("Run as user " + AuthenticationUtil.getRunAsUser());
            logger.debug("Fully authenticated " + AuthenticationUtil.getFullyAuthenticatedUser());
        }
        
        // Get the version history node for the node is question and delete it
        NodeRef versionHistoryNodeRef = getVersionHistoryNodeRef(nodeRef);
        
        if (versionHistoryNodeRef != null)
        {
            try
            {
                // Disable auto-version behaviour
                this.policyBehaviourFilter.disableBehaviour(ContentModel.ASPECT_VERSIONABLE);
                
                // Delete the version history node
                this.dbNodeService.deleteNode(versionHistoryNodeRef);
                
                if (this.nodeService.exists(nodeRef) == true && this.nodeService.hasAspect(nodeRef, ContentModel.ASPECT_VERSIONABLE) == true)
                {
                    
                        // Reset the version label property on the versionable node
                        this.nodeService.setProperty(nodeRef, ContentModel.PROP_VERSION_LABEL, null);
                }
                
            }
            finally
            {
                this.policyBehaviourFilter.enableBehaviour(ContentModel.ASPECT_VERSIONABLE);
            }
        }
    }
    
    @Override
    @Extend(extensionAPI=VersionServiceExtension.class,traitAPI=VersionServiceTrait.class)
    public void deleteVersion(NodeRef nodeRef, Version version)
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("Run as user " + AuthenticationUtil.getRunAsUser());
            logger.debug("Fully authenticated " + AuthenticationUtil.getFullyAuthenticatedUser());
        }
        
        // Check the mandatory parameters
        ParameterCheck.mandatory("nodeRef", nodeRef);
        ParameterCheck.mandatory("version", version);
        
        Version currentVersion = getCurrentVersion(nodeRef);
        
        // Delete the version node
        this.dbNodeService.deleteNode(VersionUtil.convertNodeRef(version.getFrozenStateNodeRef()));

        // If we try to delete the last version
        if (currentVersion.getVersionLabel().equals(version.getVersionLabel()))
        {
            Version headVersion = getHeadVersion(nodeRef);
            if (headVersion != null)
            {
                // Reset the version label property on the versionable node to new head version
                // Disable the VersionableAspect for this change though, we don't want
                //  to have this create a new version for the property change!
                policyBehaviourFilter.disableBehaviour(nodeRef, ContentModel.ASPECT_VERSIONABLE);
                try
                {
                    this.nodeService.setProperty(nodeRef, ContentModel.PROP_VERSION_LABEL, headVersion.getVersionLabel());
                    // MNT-13097 Content will be reverted as well
                    revert(nodeRef, headVersion);
                }
                finally
                {
                    policyBehaviourFilter.enableBehaviour(nodeRef, ContentModel.ASPECT_VERSIONABLE);
                }
            }
            else
            {
                deleteVersionHistory(nodeRef);
            }
        }
    }
    
    @Override
    @Extend(extensionAPI=VersionServiceExtension.class,traitAPI=VersionServiceTrait.class)
    public boolean isAVersion(NodeRef nodeRef)
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("Run as user " + AuthenticationUtil.getRunAsUser());
            logger.debug("Fully authenticated " + AuthenticationUtil.getFullyAuthenticatedUser());
        }
        
        NodeRef realNodeRef = nodeRef;
        if(nodeRef.getStoreRef().getProtocol().equals(VersionBaseModel.STORE_PROTOCOL))
        {
            realNodeRef = VersionUtil.convertNodeRef(nodeRef);            
        }
        return this.dbNodeService.hasAspect(realNodeRef, Version2Model.ASPECT_VERSION);
    }
    
    @Override
    @Extend(extensionAPI=VersionServiceExtension.class,traitAPI=VersionServiceTrait.class)
    public boolean isVersioned(NodeRef nodeRef)
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("Run as user " + AuthenticationUtil.getRunAsUser());
            logger.debug("Fully authenticated " + AuthenticationUtil.getFullyAuthenticatedUser());
        }
        
        NodeRef realNodeRef = nodeRef;
        if(nodeRef.getStoreRef().getProtocol().equals(VersionBaseModel.STORE_PROTOCOL))
        {
            realNodeRef = VersionUtil.convertNodeRef(nodeRef);            
        }
        return this.dbNodeService.hasAspect(realNodeRef, ContentModel.ASPECT_VERSIONABLE);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Trait> ExtendedTrait<T> getTrait(Class<? extends T> traitAPI)
    {
        return (ExtendedTrait<T>) versionServiceTrait;
    }
}
