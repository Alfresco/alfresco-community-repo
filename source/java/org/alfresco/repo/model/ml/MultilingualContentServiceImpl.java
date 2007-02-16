/*
 * Copyright (C) 2007 Alfresco, Inc.
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

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.i18n.I18NUtil;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.ml.MultilingualContentService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.util.PropertyMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Multilingual support implementation
 * 
 * @author Derek Hulley
 * @author Philippe Dubois
 */
public class MultilingualContentServiceImpl implements MultilingualContentService
{
    private static Log logger = LogFactory.getLog(MultilingualContentServiceImpl.class);
    
    private NodeService nodeService;
    private SearchService searchService;
    private VersionService versionService;
    private SearchParameters searchParametersMLRoot;
    
    public MultilingualContentServiceImpl()
    {
        searchParametersMLRoot = new SearchParameters();
        searchParametersMLRoot.setLanguage(SearchService.LANGUAGE_XPATH);
        searchParametersMLRoot.setLimit(1);
        searchParametersMLRoot.addStore(new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore"));
        searchParametersMLRoot.setQuery("/cm:multilingualRoot");
    }
    
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    public void setSearchService(SearchService searchService)
    {
        this.searchService = searchService;
    }

    public void setVersionService(VersionService versionService)
    {
        this.versionService = versionService;
    }

    public void renameWithMLExtension(NodeRef translationNodeRef)
    {
        throw new UnsupportedOperationException();
    }
    
    /**
     * @return Returns a reference to the node that will hold all the <b>cm:mlContainer</b> nodes.
     */
    private NodeRef getMLContainerRoot()
    {
        ResultSet rs = searchService.query(searchParametersMLRoot);
        try
        {
            if (rs.length() > 0)
            {
                NodeRef mlRootNodeRef = rs.getNodeRef(0);
                // done
                return mlRootNodeRef;
            }
            else
            {
                throw new AlfrescoRuntimeException(
                        "Unable to find bootstrap location for ML Root using query: " + searchParametersMLRoot.getQuery());
            }
        }
        finally
        {
            rs.close();
        }
    }
    
    private static final QName QNAME_ML_CONTAINER = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "mlContainer");
    private static final QName QNAME_ML_TRANSLATION = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "mlTranslation");
    /**
     * @return Returns a new <b>cm:mlContainer</b>
     */
    private NodeRef makeMLContainer()
    {
        NodeRef mlContainerRootNodeRef = getMLContainerRoot();
        // Create the container
        ChildAssociationRef assocRef = nodeService.createNode(
                mlContainerRootNodeRef,
                ContentModel.ASSOC_CHILDREN,
                QNAME_ML_CONTAINER,
                ContentModel.TYPE_MULTILINGUAL_CONTAINER);
        // done
        return assocRef.getChildRef();
    }
    
    /**
     * Retrieve or create a <b>cm:mlDocument</b> container for the given node, which must have the
     * <b>cm:mlDocument</b> already applied.
     * 
     * @param mlDocumentNodeRef an existing <b>cm:mlDocument</b>
     * @param allowCreate <tt>true</tt> if a <b>cm:mlContainer</b> must be created if on doesn't exist,
     *      otherwise <tt>false</tt> if a parent <b>cm:mlContainer</b> is expected to exist.
     * @return Returns the <b>cm:mlContainer</b> parent
     */
    private NodeRef getOrCreateMLContainer(NodeRef mlDocumentNodeRef, boolean allowCreate)
    {
        if (!nodeService.hasAspect(mlDocumentNodeRef, ContentModel.ASPECT_MULTILINGUAL_DOCUMENT))
        {
            throw new IllegalArgumentException(
                    "Node must have aspect " + ContentModel.ASPECT_MULTILINGUAL_DOCUMENT + " applied");
        }
        // Now check if a parent mlContainer exists
        NodeRef mlContainerNodeRef = null;
        boolean createAssociation = false;
        List<ChildAssociationRef> parentAssocRefs = nodeService.getParentAssocs(
                mlDocumentNodeRef,
                ContentModel.ASSOC_MULTILINGUAL_CHILD,
                RegexQNamePattern.MATCH_ALL);
        if (parentAssocRefs.size() == 0)
        {
            if (allowCreate)
            {
                // Create a ML container
                mlContainerNodeRef = makeMLContainer();
                createAssociation = true;
            }
            else
            {
                throw new AlfrescoRuntimeException("No multilingual container exists for document node: " + mlDocumentNodeRef);
            }
        }
        else if (parentAssocRefs.size() == 1)
        {
            // Just get it
            ChildAssociationRef toKeepAssocRef = parentAssocRefs.get(0);
            mlContainerNodeRef = toKeepAssocRef.getParentRef();
        }
        else if (parentAssocRefs.size() > 1)
        {
            // This is a problem - destroy all but the first
            logger.warn("Cleaning up multiple multilingual containers on node: " + mlDocumentNodeRef);
            ChildAssociationRef toKeepAssocRef = parentAssocRefs.get(0);
            mlContainerNodeRef = toKeepAssocRef.getParentRef();
            // Remove all the associations to the container
            boolean first = true;
            for (ChildAssociationRef assocRef : parentAssocRefs)
            {
                if (first)
                {
                    first = false;
                    continue;
                }
                nodeService.removeChildAssociation(assocRef);
            }
        }
        // Associate the translation with the container
        if (createAssociation)
        {
            nodeService.addChild(
                    mlContainerNodeRef,
                    mlDocumentNodeRef,
                    ContentModel.ASSOC_MULTILINGUAL_CHILD,
                    QNAME_ML_TRANSLATION);
        }
        // done
        return mlContainerNodeRef;
    }

    private NodeRef makeTranslationImpl(NodeRef mlContainerNodeRef, NodeRef contentNodeRef, Locale locale)
    {
        // Add the aspect using the given locale, of necessary
        if (!nodeService.hasAspect(contentNodeRef, ContentModel.ASPECT_MULTILINGUAL_DOCUMENT))
        {
            PropertyMap properties = new PropertyMap();
            properties.put(ContentModel.PROP_LOCALE, locale);
            nodeService.addAspect(contentNodeRef, ContentModel.ASPECT_MULTILINGUAL_DOCUMENT, properties);
        }
        else
        {
            // The aspect is present, so just ensure that the locale is correct
            nodeService.setProperty(contentNodeRef, ContentModel.PROP_LOCALE, locale);
        }
        
        // Do we make use of an existing container?
        if (mlContainerNodeRef == null)
        {
            // Make one
            mlContainerNodeRef = getOrCreateMLContainer(contentNodeRef, true);
        }
        else
        {
            // Check that the language is not duplicated
            Map<Locale, NodeRef> existingLanguages = this.getTranslations(mlContainerNodeRef);
            if (existingLanguages.containsKey(locale))
            {
                throw new AlfrescoRuntimeException("Duplicate locale in document pool: " + locale);
            }
            
            // Use the existing container
            nodeService.addChild(
                    mlContainerNodeRef,
                    contentNodeRef,
                    ContentModel.ASSOC_MULTILINGUAL_CHILD,
                    QNAME_ML_TRANSLATION);
        }
        // done
        return mlContainerNodeRef;
    }

    /** @inheritDoc */
    public NodeRef makeTranslation(NodeRef contentNodeRef, Locale locale)
    {
        NodeRef mlContainerNodeRef = makeTranslationImpl(null, contentNodeRef, locale);
        // done
        if (logger.isDebugEnabled())
        {
            logger.debug("Made a translation: \n" +
                    "   content:   " + contentNodeRef + "\n" +
                    "   locale:    " + locale + "\n" +
                    "   container: " + mlContainerNodeRef);
        }
        return mlContainerNodeRef;
    }
    
    /** @inheritDoc */
    public NodeRef addTranslation(NodeRef newTranslationNodeRef, NodeRef translationOfNodeRef, Locale locale)
    {
        NodeRef mlContainerNodeRef = null;
        // Were we given the translation or the container
        QName typeQName = nodeService.getType(translationOfNodeRef);
        if (typeQName.equals(ContentModel.TYPE_MULTILINGUAL_CONTAINER))
        {
            // We have the container
            mlContainerNodeRef = translationOfNodeRef;
        }
        else
        {
            // Get the container
            mlContainerNodeRef = getOrCreateMLContainer(translationOfNodeRef, false);
        }
        // Use the existing container to make the new content into a translation
        makeTranslationImpl(mlContainerNodeRef, newTranslationNodeRef, locale);
        // done
        if (logger.isDebugEnabled())
        {
            logger.debug("Added a translation: \n" +
                    "   Translation of:  " + translationOfNodeRef + " of type " + typeQName + "\n" +
                    "   New translation: " + newTranslationNodeRef + "\n" +
                    "   Locale:          " + locale);
        }
        return mlContainerNodeRef;
    }

    /** @inheritDoc */
    public NodeRef getTranslationContainer(NodeRef translationNodeRef)
    {
        NodeRef mlContainerNodeRef = getOrCreateMLContainer(translationNodeRef, false);
        // done
        return mlContainerNodeRef;
    }

    /** @inheritDoc */
    public void createEdition( NodeRef translationNodeRef)
    {
    	NodeRef mlContainerNodeRef = getOrCreateMLContainer(translationNodeRef, false);
        // Ensure that the translation given is one of the children
        getOrCreateMLContainer(translationNodeRef, false);
        // Get all the container's children
        List<ChildAssociationRef> childAssocRefs = nodeService.getChildAssocs(
                mlContainerNodeRef,
                ContentModel.ASSOC_MULTILINGUAL_CHILD,
                RegexQNamePattern.MATCH_ALL);
        // Version the container and all its children
        versionService.createVersion(mlContainerNodeRef, null, true);
        // Remove all the child documents apart from the given node
        boolean found = false;
        for (ChildAssociationRef childAssoc : childAssocRefs)
        {
            NodeRef documentNodeRef = childAssoc.getChildRef();
            // Is this the node to keep?
            if (documentNodeRef.equals(translationNodeRef))
            {
                // It is, so keep it
                found = true;
                continue;
            }
            // Delete it
            nodeService.deleteNode(documentNodeRef);
        }
        // Check that we left a document
        if (!found)
        {
            throw new AlfrescoRuntimeException(
                    "The translation provided is not a child of the multilingual container: \n" +
                    "   Container:   " + mlContainerNodeRef + "\n" +
                    "   Translation: " + translationNodeRef);
        }
        // Done
        if (logger.isDebugEnabled())
        {
            // Get the version information
            Version mlContainerVersion = versionService.getCurrentVersion(mlContainerNodeRef);
            String mlContainerVersionLabel = mlContainerVersion.getVersionLabel();
            logger.debug(
                    "Versioned multilingual container: \n" +
                    "   Container:       " + mlContainerNodeRef + "\n" +
                    "   Current Version: " + mlContainerVersionLabel);
        }
    }

    /** @inheritDoc */
    public Map<Locale, NodeRef> getTranslations(NodeRef translationOfNodeRef)
    {
        NodeRef mlContainerNodeRef = null;
        // Were we given the translation or the container
        QName typeQName = nodeService.getType(translationOfNodeRef);
        if (typeQName.equals(ContentModel.TYPE_MULTILINGUAL_CONTAINER))
        {
            // We have the container
            mlContainerNodeRef = translationOfNodeRef;
        }
        else
        {
            // Get the container
            mlContainerNodeRef = getOrCreateMLContainer(translationOfNodeRef, false);
        }
        // Get all the children
        List<ChildAssociationRef> assocRefs = nodeService.getChildAssocs(
                mlContainerNodeRef,
                ContentModel.ASSOC_MULTILINGUAL_CHILD,
                RegexQNamePattern.MATCH_ALL);
        // Iterate over them and build the map
        Map<Locale, NodeRef> nodeRefsByLocale = new HashMap<Locale, NodeRef>(13);
        for (ChildAssociationRef assocRef : assocRefs)
        {
            NodeRef nodeRef = assocRef.getChildRef();
            // Get the locale
            Locale locale = (Locale) nodeService.getProperty(nodeRef, ContentModel.PROP_LOCALE);
            // Map it
            nodeRefsByLocale.put(locale, nodeRef);
        }
        // Done
        if (logger.isDebugEnabled())
        {
            logger.debug("Found all translations: \n" +
                    "   Node: " + translationOfNodeRef + " (type " + typeQName + ")\n" +
                    "   Map: " + nodeRefsByLocale);
        }
        return nodeRefsByLocale;
    }

    /** @inheritDoc */
    public NodeRef getTranslationForLocale(NodeRef translationNodeRef, Locale locale)
    {
        // Get the container
        getOrCreateMLContainer(translationNodeRef, false);
        // Get all the translations
        Map<Locale, NodeRef> nodeRefsByLocale = getTranslations(translationNodeRef);
        // Get the closest matching locale
        Set<Locale> locales = nodeRefsByLocale.keySet();
        Locale nearestLocale = I18NUtil.getNearestLocale(locale, locales);
        NodeRef nearestNodeRef = nodeRefsByLocale.get(nearestLocale);
        // Done
        if (logger.isDebugEnabled())
        {
            logger.debug("Found nearest locale: \n" +
                    "   Given node:   " + translationNodeRef + "\n" +
                    "   Given locale: " + locale + "\n" +
                    "   Found node:   " + nearestNodeRef + "\n" +
                    "   Found locale: " + nearestLocale);
        }
        return nearestNodeRef;
    }
}
