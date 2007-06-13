/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
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
package org.alfresco.repo.model.ml;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.archive.NodeArchiveService;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.version.VersionModel;
import org.alfresco.repo.version.common.VersionUtil;
import org.alfresco.service.cmr.ml.EditionService;
import org.alfresco.service.cmr.ml.MultilingualContentService;
import org.alfresco.service.cmr.model.FileExistsException;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionHistory;
import org.alfresco.service.cmr.version.VersionService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Edition support implementation
 *
 * @author Yannick Pignot
 */
public class EditionServiceImpl implements EditionService
{
    private static Log logger = LogFactory.getLog(EditionServiceImpl.class);

    private VersionService versionService;
    private NodeService nodeService;
    private BehaviourFilter policyBehaviourFilter;
    private MultilingualContentService multilingualContentService;
    private NodeArchiveService nodeArchiveService;
    private NodeService versionNodeService;
    private FileFolderService fileFolderService;

    /**
     * List of properties to set persistent when an edition of the mlContainer is created
     */
    public static final QName[] ML_CONTAINER_PROPERTIES_TO_VERSION = {
                        ContentModel.PROP_AUTHOR,
                        ContentModel.PROP_LOCALE
                    };

    /** @inheritDoc */
    public NodeRef createEdition(NodeRef startingTranslationNodeRef, Map<String, Serializable> versionProperties)
    {
        if (nodeService.hasAspect(startingTranslationNodeRef, ContentModel.ASPECT_MULTILINGUAL_DOCUMENT))
        {
            return createEditionImpl(
                    startingTranslationNodeRef,
                    versionProperties
                );
        }
        else
        {
            throw new IllegalArgumentException("The node " + startingTranslationNodeRef + " is not multilingual.");
        }
    }

    private NodeRef createEditionImpl(NodeRef startingTranslationNodeRef, Map<String, Serializable> versionProperties)
    {

        // 1. First step: prepare and version the mlContainer

        // Get the ml container to version
        NodeRef mlContainerToVersion = multilingualContentService.getTranslationContainer(startingTranslationNodeRef);
        // Get all the container's children
        List<ChildAssociationRef> childAssocRefs = nodeService.getChildAssocs(
                mlContainerToVersion, ContentModel.ASSOC_MULTILINGUAL_CHILD,
                RegexQNamePattern.MATCH_ALL);

        // get the last edition and add it the Version Histories property to the version
        Version currentEdition = versionService.getCurrentVersion(mlContainerToVersion);
        addVersionHitoryProperty(currentEdition, childAssocRefs);

        if(versionProperties == null)
        {
            versionProperties = new HashMap<String, Serializable>();
        }

        // get the properties to add to the edition history
        addPropertiesToVersion(versionProperties, mlContainerToVersion);

        // Version the container and its translations
        versionService.createVersion(mlContainerToVersion, versionProperties, true);

        // 2.   Third step: prepare the current edition of the mlContainer

        // Get the new starting point node, it will be returned
        NodeRef startNode;

        // copy the translation before its deletion and get usefull properties
        NodeRef space = nodeService.getPrimaryParent(startingTranslationNodeRef).getParentRef();
        String name   = (String) nodeService.getProperty(startingTranslationNodeRef, ContentModel.PROP_NAME);
        Locale locale = (Locale) nodeService.getProperty(startingTranslationNodeRef, ContentModel.PROP_LOCALE);
        String author = (String) nodeService.getProperty(startingTranslationNodeRef, ContentModel.PROP_AUTHOR);

        for (int count = 0;; count++)
        {
            try
            {
                // genererate a temporary name.
                String tempName = "TEMP_NAME" + System.currentTimeMillis() + "_" + count;

                // try to copy the node
                startNode = fileFolderService.copy(startingTranslationNodeRef, space, tempName).getNodeRef();

                // copy completed without exception
                break;

            }
            catch (FileExistsException e)
            {
                // try again with a new name
            }
            catch (FileNotFoundException e)
            {
                throw new IllegalStateException(e);
            }
        }

        // remove the current translations of the mlContainer
        removeTranslations(childAssocRefs);

        // restore the original name of the node
        nodeService.setProperty(startNode, ContentModel.PROP_NAME, name);


        // add the starting node to the mlContainer, and set the author
        multilingualContentService.addTranslation(startNode, mlContainerToVersion, locale);
        nodeService.setProperty(startNode, ContentModel.PROP_AUTHOR, author);

        // set the starting translation become the pivot.
        nodeService.setProperty(mlContainerToVersion, ContentModel.PROP_LOCALE, locale);
        nodeService.setProperty(mlContainerToVersion, ContentModel.PROP_AUTHOR, author);

        // Done
        if (logger.isDebugEnabled())
        {
            // Get the version information
            Version mlContainerVersion = versionService.getCurrentVersion(mlContainerToVersion);
            String mlContainerVersionLabel = mlContainerVersion.getVersionLabel();

            logger.debug("Versioned multilingual container: \n"
                    + "   Container:       " + mlContainerToVersion + "\n"
                    + "   Current Version: " + mlContainerVersionLabel);
        }

        return startNode;
    }

    /** @inheritDoc */
    public VersionHistory getEditions(NodeRef mlContainer)
    {
        VersionHistory editionHistory = null;

        // Only the mlContainer can have editions
        if (nodeService.getType(mlContainer).equals(
                ContentModel.TYPE_MULTILINGUAL_CONTAINER))
        {
            // get the editions of the mlContainer
            editionHistory = versionService.getVersionHistory(mlContainer);
        }

        else
        {
            throw new IllegalArgumentException("The type of the node must be "
                    + ContentModel.TYPE_CONTAINER);
        }

        if (logger.isDebugEnabled())
        {
            logger.debug("Found all editions: \n" + "   Node:     "
                    + mlContainer + " (type "
                    + ContentModel.TYPE_MULTILINGUAL_CONTAINER + ")\n"
                    + "   Editions: " + editionHistory);
        }

        return editionHistory;
    }

    /** @inheritDoc */
    public Map<QName, Serializable> getVersionedMetadatas(Version version)
    {
        NodeRef frozenNodeRef = version.getFrozenStateNodeRef();

        if(ContentModel.TYPE_MULTILINGUAL_CONTAINER.equals(nodeService.getType(frozenNodeRef)))
        {
            // for the mlContainer, the properties are set as a version properties
            Map<String, Serializable> properties = version.getVersionProperties();

            // The returned map of this method need a QName type key, not a String.
            Map<QName, Serializable> convertedProperties = new HashMap<QName, Serializable>(properties.size());

            // perform the convertion
            for(Map.Entry<String, Serializable> entry : properties.entrySet())
            {
                convertedProperties.put(
                        QName.createQName(entry.getKey()),
                        entry.getValue());
            }

            return convertedProperties;
        }
        else
        {
            // for any other type of node, the properties are set as versioned metadata
            return versionNodeService.getProperties(frozenNodeRef);
        }
    }

    /** @inheritDoc */
    public List<VersionHistory> getVersionedTranslations(Version mlContainerEdition)
    {
        // Ensure that the given version is an Edition of an mlContainer
        if(!ContentModel.TYPE_MULTILINGUAL_CONTAINER.equals(nodeService.getType(mlContainerEdition.getVersionedNodeRef())))
        {
            throw new IllegalArgumentException("The type of the node must be " + ContentModel.TYPE_CONTAINER);
        }

        Map<QName, Serializable> properties = versionNodeService.getProperties(mlContainerEdition.getFrozenStateNodeRef());

        // get the serialisation of the version histories in the version properties
        List<VersionHistory> versionHistories = (List<VersionHistory>)
                    properties.get(VersionModel.PROP_QNAME_TRANSLATION_VERIONS);

        if (versionHistories == null)
        {
           // the initial edition doesn't content translations (at the creation time of the mlContainer).
           versionHistories = new ArrayList<VersionHistory>();
        }

        if (logger.isDebugEnabled())
        {
            logger.debug("Found all translations : \n"
                    + "   Versioned mlContainer: " + mlContainerEdition.getVersionedNodeRef() + "\n"
                    + "   Edition:                " + mlContainerEdition
                    + "   Translations:           " + versionHistories);
        }

        return versionHistories;
    }

    /**
     * Util method to add the version histories of translations as a property of the frozen mlContainer
     */
    private void addVersionHitoryProperty(Version edition, List<ChildAssociationRef> childAssocRefs)
    {
        List<VersionHistory> translationVersionHistories = new ArrayList<VersionHistory>(childAssocRefs.size());

        for (ChildAssociationRef ref : childAssocRefs)
        {
            NodeRef translation = ref.getChildRef();

            translationVersionHistories.add(versionService.getVersionHistory(translation));

        }

        // properties in which the version histories will be stored
        Map<QName, Serializable> properties = new HashMap<QName, Serializable>();

        // add the version history of the translation as property of the Edition
        properties.put(VersionModel.PROP_QNAME_QNAME, VersionModel.PROP_QNAME_TRANSLATION_VERIONS);
        properties.put(VersionModel.PROP_QNAME_IS_MULTI_VALUE, true);
        properties.put(VersionModel.PROP_QNAME_MULTI_VALUE, (Serializable) translationVersionHistories);

        // create the versioned property node
        this.nodeService.createNode(
                    VersionUtil.convertNodeRef(edition.getFrozenStateNodeRef()),
                    VersionModel.CHILD_QNAME_VERSIONED_ATTRIBUTES,
                    VersionModel.CHILD_QNAME_VERSIONED_ATTRIBUTES,
                    VersionModel.TYPE_QNAME_VERSIONED_PROPERTY,
                    properties);
    }

    /**
     * Util method to add the usefull properties to the existing properties of the given, mlContainer
     *
     * @link {@link EditionServiceImpl#ML_CONTAINER_PROPERTIES_TO_VERSION}
     */
    private void addPropertiesToVersion(Map<String, Serializable> versionProperties, NodeRef mlContainerToVersion)
    {
        // add useful properties
        for(QName prop : ML_CONTAINER_PROPERTIES_TO_VERSION)
        {
            versionProperties.put(prop.toString(), nodeService.getProperty(mlContainerToVersion, prop));
        }
    }

    /**
     * Util method to remove the given translations after making a new edition
     */
    private void removeTranslations(List<ChildAssociationRef> childAssocRefs)
    {
        // Turn off any auto-version policy behaviours. Without that,
        // the version history of the translations will be deleted.
        this.policyBehaviourFilter.disableBehaviour(ContentModel.ASPECT_VERSIONABLE);

        // Turn off any multilingual document policy behaviours. Without that,
        // the mlcontainer of the translations will be deleted.
        this.policyBehaviourFilter.disableBehaviour(ContentModel.ASPECT_MULTILINGUAL_DOCUMENT);

        try
        {
            for (ChildAssociationRef childAssoc : childAssocRefs)
            {
                NodeRef documentNodeRef = childAssoc.getChildRef();

                // Permanently delete it
                nodeService.deleteNode(documentNodeRef);
                if(nodeService.exists(nodeArchiveService.getArchivedNode(documentNodeRef)))
                {
                    nodeService.deleteNode(nodeArchiveService.getArchivedNode(documentNodeRef));
                }
            }
        }
        finally
        {
            // Turn auto-version and multinlingual document policies back on
            this.policyBehaviourFilter.enableBehaviour(ContentModel.ASPECT_VERSIONABLE);
            this.policyBehaviourFilter.enableBehaviour(ContentModel.ASPECT_MULTILINGUAL_DOCUMENT);
        }
    }

    /**
     * @param nodeService
     *            the Node Service to set
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    /**
     * @param versionService
     *            the Version Service to set
     */
    public void setVersionService(VersionService versionService)
    {
        this.versionService = versionService;
    }

    /**
     * @param multilingualContentService
     *            the Multilingual Content Service to set
     */
    public void setMultilingualContentService(MultilingualContentService multilingualContentService)
    {
        this.multilingualContentService = multilingualContentService;
    }

    /**
     * @param versionNodeService
     *            the Version Store Node Service to set
     */
    public void setVersionNodeService(NodeService versionNodeService)
    {
        this.versionNodeService = versionNodeService;
    }

    /**
     * @param policyBehaviourFilter the Behaviour Filter to set
     */
    public void setPolicyBehaviourFilter(BehaviourFilter policyBehaviourFilter)
    {
        this.policyBehaviourFilter = policyBehaviourFilter;
    }

    /**
     * @param nodeArchiveService the node Archive Service to set
     */
    public void setNodeArchiveService(NodeArchiveService nodeArchiveService)
    {
        this.nodeArchiveService = nodeArchiveService;
    }

    /**
     * @param fileFolderService the fileFolder Service to set
     */
    public void setFileFolderService(FileFolderService fileFolderService)
    {
        this.fileFolderService = fileFolderService;
    }
}