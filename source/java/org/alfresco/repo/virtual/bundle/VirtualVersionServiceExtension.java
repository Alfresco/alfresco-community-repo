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

package org.alfresco.repo.virtual.bundle;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.alfresco.repo.version.Version2Model;
import org.alfresco.repo.version.VersionModel;
import org.alfresco.repo.version.VersionServicePolicies.CalculateVersionLabelPolicy;
import org.alfresco.repo.version.common.VersionImpl;
import org.alfresco.repo.version.traitextender.VersionServiceExtension;
import org.alfresco.repo.version.traitextender.VersionServiceTrait;
import org.alfresco.repo.virtual.ref.GetParentReferenceMethod;
import org.alfresco.repo.virtual.ref.NodeProtocol;
import org.alfresco.repo.virtual.ref.Reference;
import org.alfresco.repo.virtual.store.VirtualStore;
import org.alfresco.service.cmr.repository.AspectMissingException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.version.ReservedVersionNameException;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionHistory;
import org.alfresco.service.namespace.QName;
import org.alfresco.traitextender.SpringBeanExtension;

public class VirtualVersionServiceExtension extends SpringBeanExtension<VersionServiceExtension, VersionServiceTrait>
            implements VersionServiceExtension
{
    private VirtualStore smartStore;

    public class VirtualVersionHistory implements VersionHistory
    {
        /**
         * 
         */
        private static final long serialVersionUID = 2640439550254763191L;

        private Reference versionedReference;

        private VersionHistory actualHistory;

        public VirtualVersionHistory(Reference versionedReference, VersionHistory actualHistory)
        {
            super();
            this.versionedReference = versionedReference;
            this.actualHistory = actualHistory;
        }

        @Override
        public Version getRootVersion()
        {
            Version actualRootVersion = actualHistory.getRootVersion();
            return VirtualVersionServiceExtension.this.virtualizeVersion(versionedReference,
                                                                         actualRootVersion);
        }

        @Override
        public Version getHeadVersion()
        {
            Version actualHeadVersion = actualHistory.getRootVersion();
            return VirtualVersionServiceExtension.this.virtualizeVersion(versionedReference,
                                                                         actualHeadVersion);
        }

        @Override
        public Collection<Version> getAllVersions()
        {
            Collection<Version> allActualVersions = actualHistory.getAllVersions();
            return VirtualVersionServiceExtension.this.virtualizeVersions(versionedReference,
                                                                          allActualVersions);
        }

        @Override
        public Version getPredecessor(Version version)
        {
            Version actualVersion = VirtualVersionServiceExtension.this.materializeVersionIfReference(version);
            Version actualPredecesor = actualHistory.getPredecessor(actualVersion);

            return VirtualVersionServiceExtension.this.virtualizeVersion(versionedReference,
                                                                         actualPredecesor);
        }

        @Override
        public Collection<Version> getSuccessors(Version version)
        {
            Version actualVersion = VirtualVersionServiceExtension.this.materializeVersionIfReference(version);
            Collection<Version> actualSuccessors = actualHistory.getSuccessors(actualVersion);

            return VirtualVersionServiceExtension.this.virtualizeVersions(versionedReference,
                                                                          actualSuccessors);
        }

        @Override
        public Version getVersion(String versionLabel)
        {
            Version actualVersion = actualHistory.getVersion(versionLabel);
            return VirtualVersionServiceExtension.this.virtualizeVersion(versionedReference,
                                                                         actualVersion);
        }

    }

    public VirtualVersionServiceExtension()
    {
        super(VersionServiceTrait.class);
    }

    public void setSmartStore(VirtualStore smartStore)
    {
        this.smartStore = smartStore;
    }

    @Override
    public StoreRef getVersionStoreReference()
    {
        return getTrait().getVersionStoreReference();
    }

    @Override
    public boolean isAVersion(NodeRef nodeRef)
    {
        VersionServiceTrait theTrait = getTrait();
        if (!Reference.isReference(nodeRef))
        {
            return theTrait.isAVersion(nodeRef);
        }
        else
        {
            Reference reference = Reference.fromNodeRef(nodeRef);
            NodeRef materialNode = smartStore.materialize(reference);
            return theTrait.isAVersion(materialNode);
        }
    }

    @Override
    public boolean isVersioned(NodeRef nodeRef)
    {
        VersionServiceTrait theTrait = getTrait();
        if (!Reference.isReference(nodeRef))
        {
            return theTrait.isVersioned(nodeRef);
        }
        else
        {
            Reference reference = Reference.fromNodeRef(nodeRef);
            NodeRef materialNode = smartStore.materialize(reference);
            return theTrait.isVersioned(materialNode);
        }
    }

    private Version materializeVersionIfReference(Version virtualVersion)
    {
        NodeRef frozenStateNodeRef = virtualVersion.getFrozenStateNodeRef();
        StoreRef frozenStoreRef = frozenStateNodeRef.getStoreRef();

        NodeRef materialFrozenNodeRef = frozenStateNodeRef;

        if (Reference.isReference(frozenStateNodeRef))
        {
            Reference frozenReference = Reference.fromNodeRef(frozenStateNodeRef);
            materialFrozenNodeRef = smartStore.materialize(frozenReference);
        }

        Map<String, Serializable> virtualProperties = virtualVersion.getVersionProperties();
        Map<String, Serializable> actualProperties = new HashMap<>(virtualProperties);

        if (frozenStoreRef.getIdentifier().equals(Version2Model.STORE_ID))
        {
            // V2 version store (eg. workspace://version2Store)
            NodeRef propFrozenNode = (NodeRef) virtualProperties.get(Version2Model.PROP_FROZEN_NODE_REF);
            NodeRef propActualFrozenNode = propFrozenNode;

            if (Reference.isReference(propFrozenNode))
            {
                Reference propFrozenReference = Reference.fromNodeRef(propFrozenNode);
                propActualFrozenNode = smartStore.materialize(propFrozenReference);
            }

            actualProperties.put(Version2Model.PROP_FROZEN_NODE_REF,
                                 propActualFrozenNode);

        }
        else if (frozenStoreRef.getIdentifier().equals(VersionModel.STORE_ID))
        {
            // Deprecated V1 version store (eg.
            // workspace://lightWeightVersionStore)
            String frozenNodeStoreProtocol = (String) virtualProperties
                        .get(VersionModel.PROP_FROZEN_NODE_STORE_PROTOCOL);
            String frozenNodeStoreId = (String) virtualProperties.get(VersionModel.PROP_FROZEN_NODE_STORE_ID);
            String frozenNodeId = (String) virtualProperties.get(VersionModel.PROP_FROZEN_NODE_ID);

            NodeRef propFrozenNode = new NodeRef(frozenNodeStoreProtocol,
                                                 frozenNodeStoreId,
                                                 frozenNodeId);
            NodeRef propActualFrozenNode = propFrozenNode;

            if (Reference.isReference(propFrozenNode))
            {
                Reference propFrozenReference = Reference.fromNodeRef(propFrozenNode);
                propActualFrozenNode = smartStore.materialize(propFrozenReference);
            }
            StoreRef propActualStoreRef = propFrozenNode.getStoreRef();
            actualProperties.put(VersionModel.PROP_FROZEN_NODE_STORE_PROTOCOL,
                                 propActualStoreRef.getProtocol());
            actualProperties.put(VersionModel.PROP_FROZEN_NODE_STORE_ID,
                                 propActualStoreRef.getIdentifier());
            actualProperties.put(VersionModel.PROP_FROZEN_NODE_ID,
                                 propActualFrozenNode.getId());
        }

        Version actualVersion = new VersionImpl(actualProperties,
                                                materialFrozenNodeRef);

        return actualVersion;
    }

    private Version virtualizeVersion(Reference versionedReference, Version actualVersion)
    {
        if (actualVersion == null)
        {
            return null;
        }
        
        NodeRef frozenStateNodeRef = actualVersion.getFrozenStateNodeRef();
        StoreRef frozenStoreRef = frozenStateNodeRef.getStoreRef();

        Reference parentReference = versionedReference.execute(new GetParentReferenceMethod());
        Reference virtualFrozenReference = NodeProtocol.newReference(frozenStateNodeRef,
                                                                     parentReference);

        Map<String, Serializable> properties = actualVersion.getVersionProperties();
        Map<String, Serializable> virtualProperties = new HashMap<String, Serializable>(properties);

        // Switch VersionStore depending on configured impl
        if (frozenStoreRef.getIdentifier().equals(Version2Model.STORE_ID))
        {
            // V2 version store (eg. workspace://version2Store)
            NodeRef propFrozenNodeRef = (NodeRef) virtualProperties.get(Version2Model.PROP_FROZEN_NODE_REF);
            Reference virtualPropFrozenReference = NodeProtocol.newReference(propFrozenNodeRef,
                                                                             parentReference);
            virtualProperties.put(Version2Model.PROP_FROZEN_NODE_REF,
                                  virtualPropFrozenReference.toNodeRef(propFrozenNodeRef.getStoreRef()));
        }
        else if (frozenStoreRef.getIdentifier().equals(VersionModel.STORE_ID))
        {
            // Deprecated V1 version store (eg.
            // workspace://lightWeightVersionStore)
            String frozenNodeStoreProtocol = (String) virtualProperties
                        .get(VersionModel.PROP_FROZEN_NODE_STORE_PROTOCOL);
            String frozenNodeStoreId = (String) virtualProperties.get(VersionModel.PROP_FROZEN_NODE_STORE_ID);
            String frozenNodeId = (String) virtualProperties.get(VersionModel.PROP_FROZEN_NODE_ID);

            StoreRef propFrozenStoreRef = new StoreRef(frozenNodeStoreProtocol,
                                                       frozenNodeStoreId);

            NodeRef propFrozenNode = new NodeRef(propFrozenStoreRef,
                                                 frozenNodeId);

            Reference virtualPropFrozenReference = NodeProtocol.newReference(propFrozenNode,
                                                                             parentReference);
            NodeRef virtualPropFrozenNodeRef = virtualPropFrozenReference.toNodeRef(propFrozenStoreRef);

            virtualProperties.put(VersionModel.PROP_FROZEN_NODE_STORE_PROTOCOL,
                                  propFrozenStoreRef.getProtocol());
            virtualProperties.put(VersionModel.PROP_FROZEN_NODE_STORE_ID,
                                  propFrozenStoreRef.getIdentifier());
            virtualProperties.put(VersionModel.PROP_FROZEN_NODE_ID,
                                  virtualPropFrozenNodeRef.getId());
        }
        return new VersionImpl(virtualProperties,
                               virtualFrozenReference.toNodeRef(frozenStateNodeRef.getStoreRef()));
    }

    private Collection<Version> virtualizeVersions(Reference versionedReference, Collection<Version> actualVersions)
    {
        Collection<Version> virtualizedVersions = new LinkedList<>();

        for (Version actualVersion : actualVersions)
        {
            Version virtualizedVersion = virtualizeVersion(versionedReference,
                                                           actualVersion);
            virtualizedVersions.add(virtualizedVersion);
        }

        return virtualizedVersions;
    }

    @Override
    public Version createVersion(NodeRef nodeRef, Map<String, Serializable> versionProperties)
                throws ReservedVersionNameException, AspectMissingException
    {
        VersionServiceTrait theTrait = getTrait();
        if (!Reference.isReference(nodeRef))
        {
            return theTrait.createVersion(nodeRef,
                                          versionProperties);
        }
        else
        {
            NodeRef materialNode = smartStore.materializeIfPossible(nodeRef);
            Version actualVersion = theTrait.createVersion(materialNode,
                                                           versionProperties);
            Reference reference = Reference.fromNodeRef(nodeRef);
            return virtualizeVersion(reference,
                                     actualVersion);
        }
    }

    @Override
    public Collection<Version> createVersion(NodeRef nodeRef, Map<String, Serializable> versionProperties,
                boolean versionChildren) throws ReservedVersionNameException, AspectMissingException
    {
        VersionServiceTrait theTrait = getTrait();
        if (!Reference.isReference(nodeRef))
        {
            return theTrait.createVersion(nodeRef,
                                          versionProperties,
                                          versionChildren);
        }
        else
        {
            NodeRef materialNode = smartStore.materializeIfPossible(nodeRef);
            Collection<Version> actualVersions = theTrait.createVersion(materialNode,
                                                                        versionProperties,
                                                                        versionChildren);

            Reference reference = Reference.fromNodeRef(nodeRef);
            return virtualizeVersions(reference,
                                      actualVersions);
        }
    }

    @Override
    public Collection<Version> createVersion(Collection<NodeRef> nodeRefs, Map<String, Serializable> versionProperties)
                throws ReservedVersionNameException, AspectMissingException
    {
        VersionServiceTrait theTrait = getTrait();
        Collection<NodeRef> materialNodeRefs = new LinkedList<>();
        Map<NodeRef, Reference> materializedNodeRefs = new HashMap<>();
        for (NodeRef nodeRef : nodeRefs)
        {
            if (!Reference.isReference(nodeRef))
            {
                materialNodeRefs.add(nodeRef);
            }
            else
            {
                NodeRef materialNode = smartStore.materializeIfPossible(nodeRef);
                materialNodeRefs.add(materialNode);
                materializedNodeRefs.put(materialNode,
                                         Reference.fromNodeRef(nodeRef));
            }
        }

        Collection<Version> versions = theTrait.createVersion(materialNodeRefs,
                                                              versionProperties);
        Collection<Version> virtualizedVersions = new LinkedList<>();
        for (Version version : versions)
        {
            NodeRef versionedNodeRef = version.getVersionedNodeRef();
            Reference reference = materializedNodeRefs.get(versionedNodeRef);

            if (reference != null)
            {
                Version virtualizedVersion = virtualizeVersion(reference,
                                                               version);
                virtualizedVersions.add(virtualizedVersion);
            }
            else
            {
                virtualizedVersions.add(version);
            }
        }

        return virtualizedVersions;
    }

    @Override
    public VersionHistory getVersionHistory(NodeRef nodeRef) throws AspectMissingException
    {
        VersionServiceTrait theTrait = getTrait();
        if (!Reference.isReference(nodeRef))
        {
            return theTrait.getVersionHistory(nodeRef);
        }
        else
        {
            Reference reference = Reference.fromNodeRef(nodeRef);
            NodeRef materialNode = smartStore.materialize(reference);
            VersionHistory actualVersionHistory = theTrait.getVersionHistory(materialNode);
            if (actualVersionHistory == null)
            {
                return null;
            }
            else
            {
                Reference versionedReference = Reference.fromNodeRef(nodeRef);

                return new VirtualVersionHistory(versionedReference,
                                                 actualVersionHistory);
            }
        }
    }

    @Override
    public Version getCurrentVersion(NodeRef nodeRef)
    {
        VersionServiceTrait theTrait = getTrait();
        if (!Reference.isReference(nodeRef))
        {
            return theTrait.getCurrentVersion(nodeRef);
        }
        else
        {
            Reference reference = Reference.fromNodeRef(nodeRef);
            NodeRef materialNode = smartStore.materialize(reference);
            Reference versionedReference = Reference.fromNodeRef(nodeRef);
            Version actualVersion = theTrait.getCurrentVersion(materialNode);
            return virtualizeVersion(versionedReference,
                                     actualVersion);
        }
    }

    @Override
    public void revert(NodeRef nodeRef)
    {
        VersionServiceTrait theTrait = getTrait();
        if (!Reference.isReference(nodeRef))
        {
            theTrait.revert(nodeRef);
        }
        else
        {
            Reference reference = Reference.fromNodeRef(nodeRef);
            NodeRef materialNode = smartStore.materialize(reference);
            theTrait.revert(materialNode);
        }
    }

    @Override
    public void revert(NodeRef nodeRef, boolean deep)
    {
        VersionServiceTrait theTrait = getTrait();
        if (!Reference.isReference(nodeRef))
        {
            theTrait.revert(nodeRef,
                            deep);
        }
        else
        {
            Reference reference = Reference.fromNodeRef(nodeRef);
            NodeRef materialNode = smartStore.materialize(reference);
            theTrait.revert(materialNode,
                            deep);
        }
    }

    @Override
    public void revert(NodeRef nodeRef, Version version)
    {

        VersionServiceTrait theTrait = getTrait();
        if (!Reference.isReference(nodeRef))
        {
            theTrait.revert(nodeRef,
                            version);
        }
        else
        {
            Reference reference = Reference.fromNodeRef(nodeRef);
            NodeRef materialNode = smartStore.materialize(reference);
            Version actualVersion = VirtualVersionServiceExtension.this.materializeVersionIfReference(version);
            theTrait.revert(materialNode,
                            actualVersion);
        }
    }

    @Override
    public void revert(NodeRef nodeRef, Version version, boolean deep)
    {
        VersionServiceTrait theTrait = getTrait();
        if (!Reference.isReference(nodeRef))
        {
            theTrait.revert(nodeRef,
                            version,
                            deep);
        }
        else
        {
            Reference reference = Reference.fromNodeRef(nodeRef);
            NodeRef materialNode = smartStore.materialize(reference);
            Version actualVersion = VirtualVersionServiceExtension.this.materializeVersionIfReference(version);
            theTrait.revert(materialNode,
                            actualVersion,
                            deep);
        }
    }

    @Override
    public NodeRef restore(NodeRef nodeRef, NodeRef parentNodeRef, QName assocTypeQName, QName assocQName)
    {
        VersionServiceTrait theTrait = getTrait();
        if (!Reference.isReference(nodeRef))
        {
            return theTrait.restore(nodeRef,
                                    parentNodeRef,
                                    assocTypeQName,
                                    assocQName);
        }
        else
        {
            Reference reference = Reference.fromNodeRef(nodeRef);
            NodeRef materialNode = smartStore.materialize(reference);
            return theTrait.restore(materialNode,
                                    parentNodeRef,
                                    assocTypeQName,
                                    assocQName);
        }
    }

    @Override
    public NodeRef restore(NodeRef nodeRef, NodeRef parentNodeRef, QName assocTypeQName, QName assocQName, boolean deep)
    {
        VersionServiceTrait theTrait = getTrait();
        if (!Reference.isReference(nodeRef))
        {
            return theTrait.restore(nodeRef,
                                    parentNodeRef,
                                    assocTypeQName,
                                    assocQName,
                                    deep);
        }
        else
        {
            Reference reference = Reference.fromNodeRef(nodeRef);
            NodeRef materialNode = smartStore.materialize(reference);
            return theTrait.restore(materialNode,
                                    parentNodeRef,
                                    assocTypeQName,
                                    assocQName,
                                    deep);
        }
    }

    @Override
    public void deleteVersionHistory(NodeRef nodeRef) throws AspectMissingException
    {
        VersionServiceTrait theTrait = getTrait();
        if (!Reference.isReference(nodeRef))
        {
            theTrait.deleteVersionHistory(nodeRef);
        }
        else
        {
            Reference reference = Reference.fromNodeRef(nodeRef);
            NodeRef materialNode = smartStore.materialize(reference);
            theTrait.deleteVersionHistory(materialNode);
        }
    }

    @Override
    public void deleteVersion(NodeRef nodeRef, Version version)
    {
        VersionServiceTrait theTrait = getTrait();
        if (!Reference.isReference(nodeRef))
        {
            theTrait.deleteVersion(nodeRef,
                                   version);
        }
        else
        {
            Reference reference = Reference.fromNodeRef(nodeRef);
            NodeRef materialNode = smartStore.materialize(reference);
            Version actualVersion = materializeVersionIfReference(version);
            theTrait.deleteVersion(materialNode,
                                   actualVersion);
        }
    }

    @Override
    public void ensureVersioningEnabled(NodeRef nodeRef, Map<QName, Serializable> versionProperties)
    {
        VersionServiceTrait theTrait = getTrait();
        if (!Reference.isReference(nodeRef))
        {
            theTrait.ensureVersioningEnabled(nodeRef,
                                             versionProperties);
        }
        else
        {
            Reference reference = Reference.fromNodeRef(nodeRef);
            NodeRef materialNode = smartStore.materialize(reference);
            theTrait.ensureVersioningEnabled(materialNode,
                                             versionProperties);
        }
    }

    @Override
    public void registerVersionLabelPolicy(QName typeQName, CalculateVersionLabelPolicy policy)
    {
        getTrait().registerVersionLabelPolicy(typeQName,
                                              policy);
    }

}
