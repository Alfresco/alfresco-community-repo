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

import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.download.DownloadModel;
import org.alfresco.repo.node.archive.NodeArchiveService;
import org.alfresco.repo.node.db.traitextender.NodeServiceExtension;
import org.alfresco.repo.node.db.traitextender.NodeServiceTrait;
import org.alfresco.repo.virtual.ActualEnvironment;
import org.alfresco.repo.virtual.ActualEnvironmentException;
import org.alfresco.repo.virtual.VirtualContentModel;
import org.alfresco.repo.virtual.VirtualizationException;
import org.alfresco.repo.virtual.config.NodeRefExpression;
import org.alfresco.repo.virtual.ref.GetActualNodeRefMethod;
import org.alfresco.repo.virtual.ref.GetAspectsMethod;
import org.alfresco.repo.virtual.ref.GetParentReferenceMethod;
import org.alfresco.repo.virtual.ref.NodeProtocol;
import org.alfresco.repo.virtual.ref.Protocols;
import org.alfresco.repo.virtual.ref.Reference;
import org.alfresco.repo.virtual.store.VirtualStore;
import org.alfresco.repo.virtual.template.FilingData;
import org.alfresco.service.cmr.dictionary.InvalidAspectException;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.InvalidChildAssociationRefException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.InvalidStoreRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeRef.Status;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.repository.StoreExistsException;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.QNamePattern;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class VirtualNodeServiceExtension extends VirtualSpringBeanExtension<NodeServiceExtension, NodeServiceTrait>
            implements NodeServiceExtension
{
    private static Log logger = LogFactory.getLog(VirtualNodeServiceExtension.class);

    private VirtualStore smartStore;

    private ActualEnvironment environment;

    private NodeRefExpression downloadAssociationsFolder;

    public VirtualNodeServiceExtension()
    {
        super(NodeServiceTrait.class);
    }

    public void setEnvironment(ActualEnvironment environment)
    {
        this.environment = environment;
    }

    public void setSmartStore(VirtualStore smartStore)
    {
        this.smartStore = smartStore;
    }

    @Override
    public boolean hasAspect(NodeRef nodeRef, QName aspectQName)
    {
        if (Reference.isReference(nodeRef))
        {
            boolean isNodeProtocol = Reference.fromNodeRef(nodeRef).getProtocol().equals(Protocols.NODE.protocol);
            if (VirtualContentModel.ASPECT_VIRTUAL.equals(aspectQName) || ContentModel.ASPECT_TITLED.equals(aspectQName))
            {
                return !isNodeProtocol;
            }
            else if (VirtualContentModel.ASPECT_VIRTUAL_DOCUMENT.equals(aspectQName))
            {
                return isNodeProtocol;
            }
            else
            {
                Reference reference = Reference.fromNodeRef(nodeRef);
                NodeRef actualNodeRef = reference.execute(new GetActualNodeRefMethod(environment));
                return getTrait().hasAspect(actualNodeRef,
                                            aspectQName);
            }
        }
        else
        {
            return getTrait().hasAspect(nodeRef,
                                        aspectQName);
        }
    }

    @Override
    public QName getType(NodeRef nodeRef)
    {
        if (Reference.isReference(nodeRef))
        {
            QName type = smartStore.getType(Reference.fromNodeRef(nodeRef));
            return type;
        }
        else
        {
            return getTrait().getType(nodeRef);
        }
    }

    private Map<QName, Serializable> getVirtualProperties(Reference reference)
    {
        return smartStore.getProperties(reference);
    }

    @Override
    public Map<QName, Serializable> getProperties(NodeRef nodeRef)
    {
        if (Reference.isReference(nodeRef))
        {
            return getVirtualProperties(Reference.fromNodeRef(nodeRef));

        }
        else
        {
            return getTrait().getProperties(nodeRef);
        }
    }

    @Override
    public Serializable getProperty(NodeRef nodeRef, QName qname)
    {
        if (Reference.isReference(nodeRef))
        {
            return getVirtualProperties(Reference.fromNodeRef(nodeRef)).get(qname);
        }
        else
        {
            return getTrait().getProperty(nodeRef,
                                          qname);
        }
    }

    @Override
    public Set<QName> getAspects(NodeRef nodeRef)
    {
        NodeServiceTrait theTrait = getTrait();
        if (Reference.isReference(nodeRef))
        {
            Reference vRef = Reference.fromNodeRef(nodeRef);
            GetAspectsMethod method = new GetAspectsMethod(theTrait,
                                                           environment);

            return vRef.execute(method);
        }
        else
        {
            return theTrait.getAspects(nodeRef);
        }
    }

    @Override
    public Path getPath(NodeRef nodeRef)
    {
        if (Reference.isReference(nodeRef))
        {
            return Reference.fromNodeRef(nodeRef).execute(new GetPathMethod(smartStore,
                                                                            environment));
        }
        else
        {
            return getTrait().getPath(nodeRef);
        }
    }

    @Override
    public List<Path> getPaths(NodeRef nodeRef, boolean primaryOnly)
    {
        if (Reference.isReference(nodeRef))
        {
            Reference reference = Reference.fromNodeRef(nodeRef);
            NodeRef actualNodeRef = reference.execute(new GetActualNodeRefMethod(environment));
            return getTrait().getPaths(actualNodeRef,
                                       primaryOnly);
        }
        else
        {
            return getTrait().getPaths(nodeRef,
                                       primaryOnly);
        }
    }

    @Override
    public boolean exists(NodeRef nodeRef)
    {
        if (Reference.isReference(nodeRef))
        {
            // For now references last forever (i.e. there is no expiration
            // mechanism )
            return true;
        }
        else
        {
            return getTrait().exists(nodeRef);
        }
    }

    @Override
    public ChildAssociationRef createNode(NodeRef parentRef, QName assocTypeQName, QName assocQName,
                QName nodeTypeQName)
    {
        if (Reference.isReference(parentRef))
        {
            return createNode(parentRef,
                              assocTypeQName,
                              assocQName,
                              nodeTypeQName,
                              Collections.<QName, Serializable> emptyMap());
        }
        else
        {
            QName materialAssocQName = materializeAssocQName(assocQName);
            return getTrait().createNode(parentRef,
                                         assocTypeQName,
                                         materialAssocQName,
                                         nodeTypeQName);
        }
    }

    @Override
    public ChildAssociationRef createNode(NodeRef parentRef, QName assocTypeQName, QName assocQName,
                QName nodeTypeQName, Map<QName, Serializable> properties)
    {
        NodeServiceTrait theTrait = getTrait();
        if (Reference.isReference(parentRef) && !isVirtualContextFolder(parentRef,
                                                                        environment))
        {
            // CM-533 Suppress options to create folders in a virtual folder
            // (repo)
            if (environment.isSubClass(nodeTypeQName,
                                       ContentModel.TYPE_FOLDER))
            {
                throw new VirtualizationException("The creation of folders within virtual folders is disabled.");
            }

            try
            {
                Reference parentReference = Reference.fromNodeRef(parentRef);
                FilingData filingData = smartStore.createFilingData(parentReference,
                                                                      assocTypeQName,
                                                                      assocQName,
                                                                      nodeTypeQName,
                                                                      properties);

                NodeRef childParentNodeRef = filingData.getFilingNodeRef();

                if (childParentNodeRef != null)
                {
                    Map<QName, Serializable> filingDataProperties = filingData.getProperties();
                    QName changedAssocQName = assocQName;
                    if (filingDataProperties.containsKey(ContentModel.PROP_NAME))
                    {
                        String fileName = (String) filingDataProperties.get(ContentModel.PROP_NAME);
                        String changedFileName = handleExistingFile(childParentNodeRef,
                                                                    fileName);
                        if (!changedFileName.equals(fileName))
                        {
                            filingDataProperties.put(ContentModel.PROP_NAME,
                                                     changedFileName);
                            QName filingDataAssocQName = filingData.getAssocQName();
                            changedAssocQName = QName.createQName(filingDataAssocQName.getNamespaceURI(),
                                                                  QName.createValidLocalName(changedFileName));
                        }
                    }
                    ChildAssociationRef actualChildAssocRef = theTrait.createNode(childParentNodeRef,
                                                                                  filingData.getAssocTypeQName(),
                                                                                  changedAssocQName == null
                                                                                              ? filingData.getAssocQName()
                                                                                              : changedAssocQName,
                                                                                  filingData.getNodeTypeQName(),
                                                                                  filingDataProperties);

                    Reference nodeProtocolChildRef = NodeProtocol.newReference(actualChildAssocRef.getChildRef(),
                                                                               parentReference);
                    QName vChildAssocQName = QName
                                .createQNameWithValidLocalName(VirtualContentModel.VIRTUAL_CONTENT_MODEL_1_0_URI,
                                                               actualChildAssocRef.getQName().getLocalName());
                    ChildAssociationRef childAssocRef = new ChildAssociationRef(actualChildAssocRef.getTypeQName(),
                                                                                parentRef,
                                                                                vChildAssocQName,
                                                                                nodeProtocolChildRef.toNodeRef());
                    Set<QName> aspects = filingData.getAspects();

                    for (QName aspect : aspects)
                    {
                        theTrait.addAspect(actualChildAssocRef.getChildRef(),
                                           aspect,
                                           filingDataProperties);
                    }

                    return childAssocRef;
                }
                else
                {
                    throw new InvalidNodeRefException("Can not create node using parent ",
                                                      parentRef);
                }

            }
            catch (VirtualizationException e)
            {
                throw new InvalidNodeRefException("Could not create node in virtual context.",
                                                  parentRef,
                                                  e);
            }
        }
        else
        {
            QName materialAssocQName = materializeAssocQName(assocQName);
            if (isVirtualContextFolder(parentRef,
                                       environment))
            {
                parentRef = smartStore.materializeIfPossible(parentRef);
            }
            return theTrait.createNode(parentRef,
                                       assocTypeQName,
                                       materialAssocQName,
                                       nodeTypeQName,
                                       properties);
        }
    }

    private QName materializeAssocQName(QName assocQName)
    {
        // Version nodes have too long assocQNames so we try
        // to detect references with "material" protocols in order to
        // replace the assocQNames with material correspondents.
        try
        {
            String lName = assocQName.getLocalName();
            NodeRef nrAssocQName = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
                                               lName);
            if (Reference.isReference(nrAssocQName))
            {
                nrAssocQName = smartStore.materializeIfPossible(nrAssocQName);
                QName materialAssocQName = QName.createQName(assocQName.getNamespaceURI(),
                                                             nrAssocQName.getId());
                return materialAssocQName;
            }
            else
            {
                return assocQName;
            }
        }
        catch (VirtualizationException e)
        {
            // We assume it can not be put through the
            // isReference-virtualize-materialize.
            if (logger.isDebugEnabled())
            {
                logger.debug("Defaulting on materializeAssocQName due to error.",
                             e);
            }
            return assocQName;
        }
    }

    private String handleExistingFile(NodeRef parentNodeRef, String fileName)
    {
        NodeServiceTrait actualNodeService = getTrait();
        NodeRef existingFile = actualNodeService.getChildByName(parentNodeRef,
                                                                ContentModel.ASSOC_CONTAINS,
                                                                fileName);
        if (existingFile != null)
        {
            int counter = 1;
            int dotIndex;
            String tmpFilename = "";
            final String dot = ".";
            final String hyphen = "-";

            while (existingFile != null)
            {
                int beforeCounter = fileName.lastIndexOf(hyphen);
                dotIndex = fileName.lastIndexOf(dot);
                if (dotIndex == 0)
                {
                    // File didn't have a proper 'name' instead it had just a
                    // suffix and
                    // started with a ".", create "1.txt"
                    tmpFilename = counter + fileName;
                }
                else if (dotIndex > 0)
                {

                    if (beforeCounter > 0 && beforeCounter < dotIndex)
                    {
                        // does file have counter in it's name or it just
                        // contains -1
                        String originalFileName = fileName.substring(0,
                                                                     beforeCounter)
                                    + fileName.substring(dotIndex);
                        boolean doesOriginalFileExist = actualNodeService.getChildByName(parentNodeRef,
                                                                                         ContentModel.ASSOC_CONTAINS,
                                                                                         originalFileName) != null;
                        if (doesOriginalFileExist)
                        {
                            String counterStr = fileName.substring(beforeCounter + 1,
                                                                   dotIndex);
                            try
                            {
                                int parseInt = DefaultTypeConverter.INSTANCE.intValue(counterStr);
                                counter = parseInt + 1;
                                fileName = fileName.substring(0,
                                                              beforeCounter)
                                            + fileName.substring(dotIndex);
                                dotIndex = fileName.lastIndexOf(dot);

                            }
                            catch (NumberFormatException ex)
                            {
                                // "-" is not before counter
                            }

                        }
                    }
                    tmpFilename = fileName.substring(0,
                                                     dotIndex)
                                + hyphen + counter + fileName.substring(dotIndex);
                }
                else
                {
                    // Filename didn't contain a dot at all, create "filename-1"
                    tmpFilename = fileName + hyphen + counter;
                }
                existingFile = actualNodeService.getChildByName(parentNodeRef,
                                                                ContentModel.ASSOC_CONTAINS,
                                                                tmpFilename);
                counter++;
            }
            fileName = tmpFilename;
        }

        return fileName;
    }

    @Override
    public List<ChildAssociationRef> getParentAssocs(NodeRef nodeRef)
    {
        if (Reference.isReference(nodeRef))
        {
            return getParentAssocs(nodeRef,
                                   RegexQNamePattern.MATCH_ALL,
                                   RegexQNamePattern.MATCH_ALL);
        }
        else
        {
            return getTrait().getParentAssocs(nodeRef);
        }
    }

    @Override
    public List<ChildAssociationRef> getParentAssocs(NodeRef nodeRef, QNamePattern typeQNamePattern,
                QNamePattern qnamePattern)
    {
        NodeServiceTrait theTrait = getTrait();
        if (Reference.isReference(nodeRef))
        {
            Reference reference = Reference.fromNodeRef(nodeRef);
            Reference parent = reference.execute(new GetParentReferenceMethod());
            if (parent == null)
            {
                return theTrait.getParentAssocs(reference.execute(new GetActualNodeRefMethod(environment)),
                                                typeQNamePattern,
                                                qnamePattern);
            }
            else
            {
                if (typeQNamePattern.isMatch(ContentModel.ASSOC_CONTAINS))
                {
                    Reference parentsParent = parent.execute(new GetParentReferenceMethod());

                    NodeRef parentNodeRef = parent.toNodeRef();
                    if (parentsParent == null)
                    {
                        parentNodeRef = parent.execute(new GetActualNodeRefMethod(environment));

                    }

                    NodeRef referenceNodeRef = reference.toNodeRef();
                    Map<QName, Serializable> properties = smartStore.getProperties(reference);
                    Serializable name = properties.get(ContentModel.PROP_NAME);
                    QName assocChildName = QName
                                .createQNameWithValidLocalName(VirtualContentModel.VIRTUAL_CONTENT_MODEL_1_0_URI,
                                                               name.toString());
                    if (qnamePattern.isMatch(assocChildName))
                    {
                        ChildAssociationRef assoc = new ChildAssociationRef(ContentModel.ASSOC_CONTAINS,
                                                                            parentNodeRef,
                                                                            assocChildName,
                                                                            referenceNodeRef);
                        return Arrays.asList(assoc);
                    }
                    else
                    {
                        return Collections.emptyList();
                    }
                }
                else
                {
                    return Collections.emptyList();
                }
            }
        }
        else
        {
            return theTrait.getParentAssocs(nodeRef,
                                            typeQNamePattern,
                                            qnamePattern);
        }
    }

    @Override
    public List<ChildAssociationRef> getChildAssocs(NodeRef nodeRef)
    {
        NodeServiceTrait theTrait = getTrait();
        boolean canVirtualize = canVirtualizeAssocNodeRef(nodeRef);
        if (canVirtualize)
        {
            Reference reference = smartStore.virtualize(nodeRef);
            List<ChildAssociationRef> virtualAssociations = smartStore.getChildAssocs(reference,
                                                                                        RegexQNamePattern.MATCH_ALL,
                                                                                        RegexQNamePattern.MATCH_ALL,
                                                                                        Integer.MAX_VALUE,
                                                                                        false);
            List<ChildAssociationRef> associations = new LinkedList<>(virtualAssociations);
            if (smartStore.canMaterialize(reference))
            {
                NodeRef materialReference = smartStore.materialize(reference);
                List<ChildAssociationRef> actualAssociations = theTrait.getChildAssocs(materialReference,
                                                                                       RegexQNamePattern.MATCH_ALL,
                                                                                       RegexQNamePattern.MATCH_ALL,
                                                                                       Integer.MAX_VALUE,
                                                                                       false);
                associations.addAll(actualAssociations);
            }

            return associations;
        }
        else
        {
            return theTrait.getChildAssocs(nodeRef);
        }
    }

    @Override
    public List<ChildAssociationRef> getChildAssocs(NodeRef nodeRef, QNamePattern typeQNamePattern,
                QNamePattern qnamePattern)
    {
        NodeServiceTrait theTrait = getTrait();
        boolean canVirtualize = canVirtualizeAssocNodeRef(nodeRef);
        if (canVirtualize)
        {
            Reference reference = smartStore.virtualize(nodeRef);
            List<ChildAssociationRef> virtualAssociations = smartStore.getChildAssocs(reference,
                                                                                        typeQNamePattern,
                                                                                        qnamePattern,
                                                                                        Integer.MAX_VALUE,
                                                                                        false);
            List<ChildAssociationRef> associations = new LinkedList<>(virtualAssociations);

            if (smartStore.canMaterialize(reference))
            {
                NodeRef materialReference = smartStore.materialize(reference);
                List<ChildAssociationRef> actualAssociations = theTrait.getChildAssocs(materialReference,
                                                                                       typeQNamePattern,
                                                                                       qnamePattern);
                associations.addAll(actualAssociations);
            }
            return associations;
        }
        else
        {
            return theTrait.getChildAssocs(nodeRef,
                                           typeQNamePattern,
                                           qnamePattern);
        }
    }

    @Override
    public List<ChildAssociationRef> getChildAssocs(NodeRef nodeRef, QNamePattern typeQNamePattern,
                QNamePattern qnamePattern, int maxResults, boolean preload)
    {
        NodeServiceTrait theTrait = getTrait();
        boolean canVirtualize = canVirtualizeAssocNodeRef(nodeRef);
        if (canVirtualize)
        {
            Reference reference = smartStore.virtualize(nodeRef);
            List<ChildAssociationRef> virtualAssociations = smartStore.getChildAssocs(reference,
                                                                                        typeQNamePattern,
                                                                                        qnamePattern,
                                                                                        maxResults,
                                                                                        preload);
            List<ChildAssociationRef> associations = new LinkedList<>(virtualAssociations);

            if (associations.size() < maxResults)
            {
                if (smartStore.canMaterialize(reference))
                {
                    NodeRef materialReference = smartStore.materialize(reference);
                    List<ChildAssociationRef> actualAssociations = theTrait.getChildAssocs(materialReference,
                                                                                           typeQNamePattern,
                                                                                           qnamePattern,
                                                                                           maxResults - associations
                                                                                                       .size(),
                                                                                           preload);
                    associations.addAll(actualAssociations);
                }
            }
            return associations;
        }
        else
        {
            return theTrait.getChildAssocs(nodeRef,
                                           typeQNamePattern,
                                           qnamePattern,
                                           maxResults,
                                           preload);
        }
    }

    private boolean canVirtualizeAssocNodeRef(NodeRef nodeRef)
    {
        boolean canVirtualize = nodeRef.getId().contains("solr") ? false : smartStore.canVirtualize(nodeRef);
        return canVirtualize;
    }

    @Override
    public List<ChildAssociationRef> getChildAssocs(NodeRef nodeRef, QNamePattern typeQNamePattern,
                QNamePattern qnamePattern, boolean preload)
    {
        NodeServiceTrait theTrait = getTrait();
        boolean canVirtualize = canVirtualizeAssocNodeRef(nodeRef);
        if (canVirtualize)
        {
            Reference reference = smartStore.virtualize(nodeRef);
            List<ChildAssociationRef> virtualAssociations = smartStore.getChildAssocs(reference,
                                                                                        typeQNamePattern,
                                                                                        qnamePattern,
                                                                                        Integer.MAX_VALUE,
                                                                                        preload);
            List<ChildAssociationRef> associations = new LinkedList<>(virtualAssociations);

            if (smartStore.canMaterialize(reference))
            {
                NodeRef materialReference = smartStore.materialize(reference);
                List<ChildAssociationRef> actualAssociations = theTrait.getChildAssocs(materialReference,
                                                                                       typeQNamePattern,
                                                                                       qnamePattern,
                                                                                       preload);
                associations.addAll(actualAssociations);
            }
            return associations;
        }
        else
        {
            return theTrait.getChildAssocs(nodeRef,
                                           typeQNamePattern,
                                           qnamePattern,
                                           preload);
        }
    }

    @Override
    public List<ChildAssociationRef> getChildAssocs(NodeRef nodeRef, Set<QName> childNodeTypeQNames)
    {
        NodeServiceTrait theTrait = getTrait();
        boolean canVirtualize = canVirtualizeAssocNodeRef(nodeRef);
        if (canVirtualize)
        {
            Reference reference = smartStore.virtualize(nodeRef);
            List<ChildAssociationRef> virtualAssociations = smartStore.getChildAssocs(reference,
                                                                                        childNodeTypeQNames);
            List<ChildAssociationRef> associations = new LinkedList<>(virtualAssociations);
            if (smartStore.canMaterialize(reference))
            {
                NodeRef materialReference = smartStore.materialize(reference);
                List<ChildAssociationRef> actualAssociations = theTrait.getChildAssocs(materialReference,
                                                                                       childNodeTypeQNames);
                associations.addAll(actualAssociations);
            }

            return associations;
        }
        else
        {
            return theTrait.getChildAssocs(nodeRef,
                                           childNodeTypeQNames);
        }
    }

    @Override
    public List<ChildAssociationRef> getChildAssocsByPropertyValue(NodeRef nodeRef, QName propertyQName,
                Serializable value)
    {
        NodeServiceTrait theTrait = getTrait();
        boolean canVirtualize = canVirtualizeAssocNodeRef(nodeRef);
        if (canVirtualize)
        {
            Reference reference = smartStore.virtualize(nodeRef);
            List<ChildAssociationRef> virtualAssociations = smartStore.getChildAssocsByPropertyValue(reference,
                                                                                                       propertyQName,
                                                                                                       value);
            List<ChildAssociationRef> associations = new LinkedList<>(virtualAssociations);
            if (smartStore.canMaterialize(reference))
            {
                NodeRef materialReference = smartStore.materialize(reference);
                List<ChildAssociationRef> actualAssociations = theTrait.getChildAssocsByPropertyValue(materialReference,
                                                                                                      propertyQName,
                                                                                                      value);
                associations.addAll(actualAssociations);
            }

            return associations;
        }
        else
        {
            return theTrait.getChildAssocsByPropertyValue(nodeRef,
                                                          propertyQName,
                                                          value);
        }
    }

    @Override
    public NodeRef getChildByName(NodeRef nodeRef, QName assocTypeQName, String childName)
    {
        // TODO: optimize

        if (smartStore.canVirtualize(nodeRef))
        {
            Reference virtualNode = smartStore.virtualize(nodeRef);

            Reference theChild = smartStore.getChildByName(virtualNode,
                                                             assocTypeQName,
                                                             childName);

            if (theChild != null)
            {
                NodeRef childNodeRef = theChild.toNodeRef();

                return childNodeRef;
            }
            if (smartStore.isVirtual(nodeRef))
            {
                return null;
            }
        }

        // TODO: add virtualizable enabler
        nodeRef = materializeIfPossible(nodeRef);
        return getTrait().getChildByName(nodeRef,
                                         assocTypeQName,
                                         childName);
    }

    @Override
    public ChildAssociationRef getPrimaryParent(NodeRef nodeRef)
    {
        if (Reference.isReference(nodeRef))
        {
            Reference reference = Reference.fromNodeRef(nodeRef);
            Reference parent = reference.execute(new GetParentReferenceMethod());
            if (parent == null)
            {
                return getTrait().getPrimaryParent(reference.execute(new GetActualNodeRefMethod(environment)));
            }
            else
            {
                Reference parentsParent = parent.execute(new GetParentReferenceMethod());

                NodeRef parentNodeRef = parent.toNodeRef();
                if (parentsParent == null)
                {
                    parentNodeRef = parent.execute(new GetActualNodeRefMethod(environment));

                }

                NodeRef referenceNodeRef = reference.toNodeRef();
                Map<QName, Serializable> refProperties = smartStore.getProperties(reference);
                Serializable childName = refProperties.get(ContentModel.PROP_NAME);
                QName childAssocQName = QName
                            .createQNameWithValidLocalName(VirtualContentModel.VIRTUAL_CONTENT_MODEL_1_0_URI,
                                                           childName.toString());
                ChildAssociationRef assoc = new ChildAssociationRef(ContentModel.ASSOC_CONTAINS,
                                                                    parentNodeRef,
                                                                    childAssocQName,
                                                                    referenceNodeRef,
                                                                    true,
                                                                    -1);
                return assoc;
            }
        }
        else
        {
            return getTrait().getPrimaryParent(nodeRef);
        }
    }

    public void setDownloadAssociationsFolder(NodeRefExpression downloadAssocaiationsFolder)
    {
        this.downloadAssociationsFolder = downloadAssocaiationsFolder;
    }

    private void cleanUpDownloadTargetAssocs(NodeRef sourceNodeRef)
    {

        NodeRef tempFolderNodeRef = downloadAssociationsFolder.resolve();
        if (tempFolderNodeRef == null)
        {
            return;
        }

        String tempFileName = sourceNodeRef.getId();

        NodeRef tempFileNodeRef = environment.getChildByName(tempFolderNodeRef,
                                                             ContentModel.ASSOC_CONTAINS,
                                                             tempFileName);
        if (tempFileNodeRef == null)
        {
            return;
        }

        environment.delete(tempFileNodeRef);
    }

    private List<AssociationRef> getDownloadTargetAssocs(NodeRef sourceNodeRef)
    {
        try
        {
            List<AssociationRef> result = new ArrayList<AssociationRef>();

            NodeRef tempFolderNodeRef = downloadAssociationsFolder.resolve();
            if (tempFolderNodeRef == null)
            {
                return result;
            }

            String tempFileName = sourceNodeRef.getId();

            NodeRef tempFileNodeRef = environment.getChildByName(tempFolderNodeRef,
                                                                 ContentModel.ASSOC_CONTAINS,
                                                                 tempFileName);
            if (tempFileNodeRef == null)
            {
                return result;
            }
            List<String> readLines = IOUtils.readLines(environment.openContentStream(tempFileNodeRef),
                                                       StandardCharsets.UTF_8);
            for (String line : readLines)
            {
                NodeRef targetRef = new NodeRef(line);
                AssociationRef assocRef = new AssociationRef(sourceNodeRef,
                                                             DownloadModel.ASSOC_REQUESTED_NODES,
                                                             targetRef);
                result.add(assocRef);
            }

            return result;
        }
        catch (IOException e)
        {
            throw new VirtualizationException(e);
        }
    }

    private void createDownloadAssociation(NodeRef sourceNodeRef, NodeRef targetRef)
    {

        NodeRef tempFolderNodeRef = downloadAssociationsFolder.resolve(true);

        String tempFileName = sourceNodeRef.getId();
        NodeRef tempFileNodeRef = environment.getChildByName(tempFolderNodeRef,
                                                             ContentModel.ASSOC_CONTAINS,
                                                             tempFileName);
        if (tempFileNodeRef == null)
        {
            FileInfo newTempFileInfo = environment.create(tempFolderNodeRef,
                                                          tempFileName,
                                                          ContentModel.TYPE_CONTENT);
            tempFileNodeRef = newTempFileInfo.getNodeRef();
            ContentWriter writer = environment.getWriter(tempFileNodeRef,
                                                         ContentModel.PROP_CONTENT,
                                                         true);
            writer.setMimetype("text/plain");
            writer.putContent(targetRef.toString());

        }
        else
        {
            ContentWriter writer = environment.getWriter(tempFileNodeRef,
                                                         ContentModel.PROP_CONTENT,
                                                         true);
            try
            {
                List<String> readLines = IOUtils.readLines(environment.openContentStream(tempFileNodeRef),
                                                           StandardCharsets.UTF_8);

                String targetRefString = targetRef.toString();
                if (!readLines.contains(targetRefString))
                {
                    readLines.add(targetRefString);
                }
                String text = "";
                for (String line : readLines)
                {
                    if (text.isEmpty())
                    {
                        text = line;
                    }
                    else
                    {
                        text = text + IOUtils.LINE_SEPARATOR + line;
                    }

                }
                writer.putContent(text);
            }
            catch (IOException e)
            {
                throw new ActualEnvironmentException(e);
            }

        }

    }

    @Override
    public List<AssociationRef> getTargetAssocs(NodeRef sourceRef, QNamePattern qnamePattern)
    {
        NodeServiceTrait theTrait = getTrait();
        List<AssociationRef> targetAssocs = null;

        if (Reference.isReference(sourceRef))
        {
            Reference reference = Reference.fromNodeRef(sourceRef);
            if (smartStore.canMaterialize(reference))
            {
                NodeRef materializedReferece = smartStore.materialize(reference);
                targetAssocs = theTrait.getTargetAssocs(materializedReferece,
                                                        qnamePattern);
            }
            else
            {
                targetAssocs = new LinkedList<>();
            }
        }
        else
        {
            targetAssocs = theTrait.getTargetAssocs(sourceRef,
                                                    qnamePattern);
        }

        List<AssociationRef> virtualizedIfNeededTargetAssocs = null;

        if (Reference.isReference(sourceRef))
        {
            virtualizedIfNeededTargetAssocs = new LinkedList<>();

            Reference sourceReference = Reference.fromNodeRef(sourceRef);

            for (AssociationRef associationRef : targetAssocs)
            {
                NodeRef targetNodeRef = associationRef.getTargetRef();
                Reference targetReference = NodeProtocol.newReference(targetNodeRef,
                                                                      sourceReference
                                                                                  .execute(new GetParentReferenceMethod()));
                AssociationRef virtualAssocRef = new AssociationRef(associationRef.getId(),
                                                                    sourceRef,
                                                                    associationRef.getTypeQName(),
                                                                    targetReference.toNodeRef(targetNodeRef
                                                                                .getStoreRef()));
                virtualizedIfNeededTargetAssocs.add(virtualAssocRef);
            }

        }
        else
        {
            virtualizedIfNeededTargetAssocs = targetAssocs;

            if (DownloadModel.TYPE_DOWNLOAD.equals(environment.getType(sourceRef)))
            {
                if (qnamePattern.isMatch(DownloadModel.ASSOC_REQUESTED_NODES))
                {
                    List<AssociationRef> virtualTargetAssocs = getDownloadTargetAssocs(sourceRef);
                    virtualizedIfNeededTargetAssocs.addAll(virtualTargetAssocs);
                }
            }
        }

        return virtualizedIfNeededTargetAssocs;
    }

    @Override
    public List<AssociationRef> getSourceAssocs(NodeRef targetRef, QNamePattern qnamePattern)
    {
        NodeServiceTrait theTrait = getTrait();

        if (Reference.isReference(targetRef))
        {
            Reference reference = Reference.fromNodeRef(targetRef);

            List<AssociationRef> materialAssocs = new ArrayList<AssociationRef>();

            if (smartStore.canMaterialize(reference))
            {
                List<AssociationRef> sourceAssocs = theTrait.getSourceAssocs(smartStore.materialize(reference),
                                                                             qnamePattern);
                for (AssociationRef associationRef : sourceAssocs)
                {
                    NodeRef sourceRef = associationRef.getSourceRef();
                    Reference sourceReference = NodeProtocol.newReference(sourceRef,
                                                                          reference
                                                                                      .execute(new GetParentReferenceMethod()));
                    AssociationRef virtualAssocRef = new AssociationRef(associationRef.getId(),
                                                                        sourceReference.toNodeRef(),
                                                                        associationRef.getTypeQName(),
                                                                        targetRef);
                    materialAssocs.add(virtualAssocRef);
                }
            }

            // Download sources are deliberately not virtualized due to
            // performance and complexity issues.
            // However they could be detected using
            // if (qnamePattern.isMatch(DownloadModel.ASSOC_REQUESTED_NODES))

            return materialAssocs;
        }
        else
        {
            return theTrait.getSourceAssocs(targetRef,
                                            qnamePattern);
        }
    }

    @Override
    public ChildAssociationRef moveNode(NodeRef nodeToMoveRef, NodeRef newParentRef, QName assocTypeQName,
                QName assocQName)
    {
        if (Reference.isReference(nodeToMoveRef) || Reference.isReference(newParentRef))
        {
            throw new UnsupportedOperationException("Unsuported operation for virtual source or destination");
        }
        else
        {
            return getTrait().moveNode(nodeToMoveRef,
                                       newParentRef,
                                       assocTypeQName,
                                       assocQName);
        }
    }

    @Override
    public void addProperties(NodeRef nodeRef, Map<QName, Serializable> properties)
    {
        if (Reference.isReference(nodeRef))
        {
            Reference reference = Reference.fromNodeRef(nodeRef);
            NodeRef actualNodeRef = reference.execute(new GetActualNodeRefMethod(null));

            getTrait().addProperties(actualNodeRef,
                                     properties);
        }
        else
        {
            getTrait().addProperties(nodeRef,
                                     properties);
        }
    }

    @Override
    public AssociationRef createAssociation(NodeRef sourceRef, NodeRef targetRef, QName assocTypeQName)
    {
        if (Reference.isReference(targetRef)
                    && getTrait().getType(materializeIfPossible(sourceRef)).equals(DownloadModel.TYPE_DOWNLOAD))
        {
            // NOTE : this is enables downloads of virtual structures
            createDownloadAssociation(sourceRef,
                                      targetRef);

            AssociationRef assocRef = new AssociationRef(sourceRef,
                                                         assocTypeQName,
                                                         targetRef);
            return assocRef;
        }
        else
        {
            return getTrait().createAssociation(materializeIfPossible(sourceRef),
                                                materializeIfPossible(targetRef),
                                                assocTypeQName);
        }
    }

    private List<NodeRef> materializeIfPossible(Collection<NodeRef> nodeRefs)
    {
        List<NodeRef> nodeRefList = new LinkedList<>();
        for (NodeRef nodeRef : nodeRefs)
        {
            nodeRefList.add(materializeIfPossible(nodeRef));
        }

        return nodeRefList;
    }

    /**
     * @deprecated use {@link VirtualStore#materializeIfPossible(NodeRef)}
     *             instead
     */
    private NodeRef materializeIfPossible(NodeRef nodeRef)
    {
        if (Reference.isReference(nodeRef))
        {
            Reference ref = Reference.fromNodeRef(nodeRef);
            if (smartStore.canMaterialize(ref))
            {
                return smartStore.materialize(ref);
            }

        }
        return nodeRef;
    }

    @Override
    public List<StoreRef> getStores()
    {
        return getTrait().getStores();
    }

    @Override
    public StoreRef createStore(String protocol, String identifier) throws StoreExistsException
    {
        return getTrait().createStore(protocol,
                                      identifier);
    }

    @Override
    public void deleteStore(StoreRef storeRef)
    {
        getTrait().deleteStore(storeRef);
    }

    @Override
    public boolean exists(StoreRef storeRef)
    {
        return getTrait().exists(storeRef);
    }

    @Override
    public Status getNodeStatus(NodeRef nodeRef)
    {
        return getTrait().getNodeStatus(materializeIfPossible(nodeRef));
    }

    @Override
    public NodeRef getNodeRef(Long nodeId)
    {
        return getTrait().getNodeRef(nodeId);
    }

    @Override
    public NodeRef getRootNode(StoreRef storeRef) throws InvalidStoreRefException
    {
        return getTrait().getRootNode(storeRef);
    }

    @Override
    public Set<NodeRef> getAllRootNodes(StoreRef storeRef)
    {
        return getTrait().getAllRootNodes(storeRef);
    }

    @Override
    public void setChildAssociationIndex(ChildAssociationRef childAssocRef, int index)
                throws InvalidChildAssociationRefException
    {
        getTrait().setChildAssociationIndex(childAssocRef,
                                            index);
    }

    @Override
    public void setType(NodeRef nodeRef, QName typeQName) throws InvalidNodeRefException
    {
        getTrait().setType(materializeIfPossible(nodeRef),
                           typeQName);
    }

    @Override
    public void addAspect(NodeRef nodeRef, QName aspectTypeQName, Map<QName, Serializable> aspectProperties)
                throws InvalidNodeRefException, InvalidAspectException
    {
        getTrait().addAspect(materializeIfPossible(nodeRef),
                             aspectTypeQName,
                             aspectProperties);
    }

    @Override
    public void removeAspect(NodeRef nodeRef, QName aspectTypeQName)
                throws InvalidNodeRefException, InvalidAspectException
    {
        getTrait().removeAspect(materializeIfPossible(nodeRef),
                                aspectTypeQName);
    }

    @Override
    public void deleteNode(NodeRef nodeRef) throws InvalidNodeRefException
    {
        NodeServiceTrait theTrait = getTrait();
        NodeRef materialNode = smartStore.materializeIfPossible(nodeRef);
        boolean isDownload = DownloadModel.TYPE_DOWNLOAD.equals(theTrait.getType(materialNode));
        theTrait.deleteNode(materialNode);
        if (isDownload)
        {
            cleanUpDownloadTargetAssocs(nodeRef);
        }
    }

    @Override
    public ChildAssociationRef addChild(NodeRef parentRef, NodeRef childRef, QName assocTypeQName, QName qname)
                throws InvalidNodeRefException
    {
        return getTrait().addChild(materializeIfPossible(parentRef),
                                   materializeIfPossible(childRef),
                                   assocTypeQName,
                                   qname);
    }

    @Override
    public List<ChildAssociationRef> addChild(Collection<NodeRef> parentRefs, NodeRef childRef, QName assocTypeQName,
                QName qname) throws InvalidNodeRefException
    {
        return getTrait().addChild(materializeIfPossible(parentRefs),
                                   materializeIfPossible(childRef),
                                   assocTypeQName,
                                   qname);
    }

    @Override
    public void removeChild(NodeRef parentRef, NodeRef childRef) throws InvalidNodeRefException
    {
        getTrait().removeChild(materializeIfPossible(parentRef),
                               materializeIfPossible(childRef));
    }

    @Override
    public boolean removeChildAssociation(ChildAssociationRef childAssocRef)
    {
        NodeServiceTrait theTrait = getTrait();

        NodeRef childRef = childAssocRef.getChildRef();
        if (Reference.isReference(childRef))
        {
            List<ChildAssociationRef> assocsToRemove = revertVirtualAssociation(childAssocRef,
                                                                                theTrait,
                                                                                childRef);
            boolean removed = false;
            if (!assocsToRemove.isEmpty())
            {
                for (ChildAssociationRef assoc : assocsToRemove)
                {
                    removed = removed || theTrait.removeChildAssociation(assoc);
                }
            }
            return removed;
        }
        else
        {
            return theTrait.removeChildAssociation(childAssocRef);
        }
    }

    private List<ChildAssociationRef> revertVirtualAssociation(ChildAssociationRef childAssocRef,
                NodeServiceTrait theTrait, NodeRef childRef)
    {
        childRef = smartStore.materialize(Reference.fromNodeRef(childRef));
        ChildAssociationRef parent = theTrait.getPrimaryParent(childRef);
        final QName assocName = childAssocRef.getQName();
        List<ChildAssociationRef> assocsToRemove = theTrait.getChildAssocs(parent.getParentRef(),
                                                                           childAssocRef.getTypeQName(),
                                                                           new QNamePattern()
                                                                           {

                                                                               @Override
                                                                               public boolean isMatch(QName qname)
                                                                               {
                                                                                   return assocName
                                                                                               .getLocalName()
                                                                                                   .equals(qname
                                                                                                               .getLocalName());
                                                                               }
                                                                           });
        return assocsToRemove;
    }

    @Override
    public boolean removeSeconaryChildAssociation(ChildAssociationRef childAssocRef)
    {

        NodeServiceTrait theTrait = getTrait();

        NodeRef childRef = childAssocRef.getChildRef();
        if (Reference.isReference(childRef))
        {
            List<ChildAssociationRef> assocsToRemove = revertVirtualAssociation(childAssocRef,
                                                                                theTrait,
                                                                                childRef);
            boolean removed = false;
            if (!assocsToRemove.isEmpty())
            {
                for (ChildAssociationRef assoc : assocsToRemove)
                {
                    removed = removed || theTrait.removeSeconaryChildAssociation(assoc);
                }
            }
            return removed;
        }
        else
        {
            return theTrait.removeSeconaryChildAssociation(childAssocRef);
        }
    }

    @Override
    public boolean removeSecondaryChildAssociation(ChildAssociationRef childAssocRef)
    {
        NodeServiceTrait theTrait = getTrait();

        NodeRef childRef = childAssocRef.getChildRef();
        if (Reference.isReference(childRef))
        {
            List<ChildAssociationRef> assocsToRemove = revertVirtualAssociation(childAssocRef,
                                                                                theTrait,
                                                                                childRef);
            boolean removed = false;
            if (!assocsToRemove.isEmpty())
            {
                for (ChildAssociationRef assoc : assocsToRemove)
                {
                    removed = removed || theTrait.removeSecondaryChildAssociation(assoc);
                }
            }
            return removed;
        }
        else
        {
            return theTrait.removeSecondaryChildAssociation(childAssocRef);
        }
    }

    @Override
    public Long getNodeAclId(NodeRef nodeRef) throws InvalidNodeRefException
    {
        return getTrait().getNodeAclId(materializeIfPossible(nodeRef));
    }

    @Override
    public void setProperties(NodeRef nodeRef, Map<QName, Serializable> properties) throws InvalidNodeRefException
    {
        getTrait().setProperties(materializeIfPossible(nodeRef),
                                 properties);
    }

    @Override
    public void setProperty(NodeRef nodeRef, QName qname, Serializable value) throws InvalidNodeRefException
    {
        getTrait().setProperty(materializeIfPossible(nodeRef),
                               qname,
                               value);
    }

    @Override
    public void removeProperty(NodeRef nodeRef, QName qname) throws InvalidNodeRefException
    {
        getTrait().removeProperty(materializeIfPossible(nodeRef),
                                  qname);
    }

    @Override
    public List<ChildAssociationRef> getChildrenByName(NodeRef nodeRef, QName assocTypeQName,
                Collection<String> childNames)
    {
        return getTrait().getChildrenByName(materializeIfPossible(nodeRef),
                                            assocTypeQName,
                                            childNames);
    }

    @Override
    public Collection<ChildAssociationRef> getChildAssocsWithoutParentAssocsOfType(NodeRef nodeRef,
                QName assocTypeQName)
    {
        NodeServiceTrait theTrait = getTrait();
        boolean canVirtualize = canVirtualizeAssocNodeRef(nodeRef);
        if (canVirtualize)
        {
            Reference reference = smartStore.virtualize(nodeRef);
            Collection<ChildAssociationRef> virtualAssociations = smartStore
                        .getChildAssocsWithoutParentAssocsOfType(reference,
                                                                 assocTypeQName);
            List<ChildAssociationRef> associations = new LinkedList<>(virtualAssociations);
            if (smartStore.canMaterialize(reference))
            {
                NodeRef materialReference = smartStore.materialize(reference);
                Collection<ChildAssociationRef> actualAssociations = theTrait
                            .getChildAssocsWithoutParentAssocsOfType(materialReference,
                                                                     assocTypeQName);
                associations.addAll(actualAssociations);
            }

            return associations;
        }
        else
        {
            return theTrait.getChildAssocsWithoutParentAssocsOfType(nodeRef,
                                                                    assocTypeQName);
        }
    }

    @Override
    public void removeAssociation(NodeRef sourceRef, NodeRef targetRef, QName assocTypeQName)
                throws InvalidNodeRefException
    {
        getTrait().removeAssociation(materializeIfPossible(sourceRef),
                                     materializeIfPossible(targetRef),
                                     assocTypeQName);
    }

    @Override
    public void setAssociations(NodeRef sourceRef, QName assocTypeQName, List<NodeRef> targetRefs)
    {
        getTrait().setAssociations(materializeIfPossible(sourceRef),
                                   assocTypeQName,
                                   materializeIfPossible(targetRefs));
    }

    @Override
    public AssociationRef getAssoc(Long id)
    {
        return getTrait().getAssoc(id);
    }

    @Override
    public NodeRef getStoreArchiveNode(StoreRef storeRef)
    {
        return getTrait().getStoreArchiveNode(storeRef);
    }

    @Override
    public NodeRef restoreNode(NodeRef archivedNodeRef, NodeRef destinationParentNodeRef, QName assocTypeQName,
                QName assocQName)
    {
        return getTrait().restoreNode(materializeIfPossible(archivedNodeRef),
                                      materializeIfPossible(destinationParentNodeRef),
                                      assocTypeQName,
                                      assocQName);
    }

    @Override
    public List<NodeRef> findNodes(FindNodeParameters params)
    {
        return getTrait().findNodes(params);
    }

    @Override
    public int countChildAssocs(NodeRef nodeRef, boolean isPrimary) throws InvalidNodeRefException
    {
        return getTrait().countChildAssocs(nodeRef,
                                           isPrimary);
    }

    @Override
    public List<AssociationRef> getTargetAssocsByPropertyValue(NodeRef sourceRef, QNamePattern qnamePattern,
                QName propertyQName, Serializable propertyValue)
    {
        return getTrait().getTargetAssocsByPropertyValue(sourceRef,
                                                         qnamePattern,
                                                         propertyQName,
                                                         propertyValue);
    }

}
