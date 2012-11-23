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
package org.alfresco.repo.model.ml;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.TransactionListenerAdapter;
import org.alfresco.repo.transaction.TransactionalResourceHelper;
import org.alfresco.service.cmr.ml.ContentFilterLanguagesService;
import org.alfresco.service.cmr.ml.MultilingualContentService;
import org.alfresco.service.cmr.model.FileExistsException;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.version.VersionService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.util.EqualsHelper;
import org.alfresco.util.PropertyMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * Multilingual support implementation.
 * <p>
 * The basic structure supported is that of a hidden container of type
 * <b>cm:mlContainer</b> containing one or more secondary children of
 * type <b>cm:mlDocument</b>.  One of these will have a matching locale
 * and is referred to as the <i>pivot translation</i>.  It is also possible
 * to have several transient <b>cm:emptyTranslation</b> instances that
 * live and die with the container until they get their own content.
 * <p>
 * It is not possible to guarantee that there is always a pivot translation
 * available in the set of sibling translations.  The strategy is to hide
 * all translations when there isn't a pivot translation available.  A
 * background task should be cleaning up the empty or invalid <b>cm:mlContainer</b>
 * instances.
 *
 * @author Derek Hulley
 * @author Philippe Dubois
 * @author Yannick Pignot
 */
public class MultilingualContentServiceImpl implements MultilingualContentService
{
    private static final QName QNAME_ASSOC_ML_ROOT = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "multilingualRoot");
    private static final String KEY_ML_CONTAINERS_TO_DELETE = "MultilingualContentServiceImpl.mlContainersToDelete";
    
    private static Log logger = LogFactory.getLog(MultilingualContentServiceImpl.class);

    private NodeService nodeService;
    private PermissionService permissionService;
    private ContentFilterLanguagesService contentFilterLanguagesService;
    private FileFolderService fileFolderService;
    private VersionService versionService;

    private BehaviourFilter policyBehaviourFilter;
    private final MLContainerCleaner mlContainerCleaner;

    public MultilingualContentServiceImpl()
    {
        mlContainerCleaner = new MLContainerCleaner();
    }

    /**
     * @return Returns a reference to the node that will hold all the <b>cm:mlContainer</b> nodes.
     */
    private NodeRef getMLContainerRoot()
    {
        NodeRef rootNodeRef = nodeService.getRootNode(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
        List<ChildAssociationRef> assocRefs = nodeService.getChildAssocs(
                rootNodeRef,
                ContentModel.ASSOC_CHILDREN,
                QNAME_ASSOC_ML_ROOT);
        if (assocRefs.size() != 1)
        {
            throw new AlfrescoRuntimeException(
                    "Unable to find bootstrap location for ML Root using query: " + QNAME_ASSOC_ML_ROOT);
        }
        NodeRef mlRootNodeRef = assocRefs.get(0).getChildRef();
        // Done
        return mlRootNodeRef;
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
        PropertyMap versionProperties = new PropertyMap();
        //versionProperties.put(ContentModel.PROP_AUTO_VERSION, Boolean.FALSE);
        //versionProperties.put(ContentModel.PROP_INITIAL_VERSION, Boolean.FALSE);
        ChildAssociationRef assocRef = nodeService.createNode(
                mlContainerRootNodeRef,
                ContentModel.ASSOC_CHILDREN,
                QNAME_ML_CONTAINER,
                ContentModel.TYPE_MULTILINGUAL_CONTAINER,
                versionProperties);
        NodeRef mlContainerNodeRef = assocRef.getChildRef();
        // TODO: Examine the usage of versioning - why is autoversioning on and used in the UI?
        // The model makes the container versionable by default, but why?
        nodeService.addAspect(mlContainerNodeRef, ContentModel.ASPECT_VERSIONABLE, versionProperties);
        // Set the permissions to allow anything by anyone
        permissionService.setPermission(
                mlContainerNodeRef,
                PermissionService.ALL_AUTHORITIES,
                PermissionService.ALL_PERMISSIONS, true);
        permissionService.setPermission(
                mlContainerNodeRef,
                AuthenticationUtil.getGuestUserName(),
                PermissionService.ALL_PERMISSIONS, true);
        // Done
        return mlContainerNodeRef;
    }

    /**
     * Get the ML Container of the given node, allowing null
     * @param mlDocumentNodeRef     the translation
     * @param allowNull             true if a null value may be returned
     * @return                      Returns the <b>cm:mlContainer</b> or null if there isn't one
     * @throws AlfrescoRuntimeException if there is no container
     */
    private NodeRef getMLContainer(NodeRef mlDocumentNodeRef, boolean allowNull)
    {
        NodeRef mlContainerNodeRef = null;
        List<ChildAssociationRef> parentAssocRefs = nodeService.getParentAssocs(
                mlDocumentNodeRef,
                ContentModel.ASSOC_MULTILINGUAL_CHILD,
                RegexQNamePattern.MATCH_ALL);
        if (parentAssocRefs.size() == 0)
        {
            if (!allowNull)
            {
                throw new AlfrescoRuntimeException(
                        "No multilingual container exists for document node: " + mlDocumentNodeRef);
            }
            mlContainerNodeRef = null;
        }
        else if (parentAssocRefs.size() >= 1)
        {
            // Just get it
            ChildAssociationRef toKeepAssocRef = parentAssocRefs.get(0);
            mlContainerNodeRef = toKeepAssocRef.getParentRef();
        }
        // Done
        return mlContainerNodeRef;
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
        // Previous versions of the document are not compatible with the versioning requirements
        // dictated by the aspects about to be added.  A version has to be forced if the aspect
        // already exists.
        // https://issues.alfresco.com/jira/browse/ETHREEOH-1657
        boolean forceNewVersion = nodeService.hasAspect(contentNodeRef, ContentModel.ASPECT_VERSIONABLE);
        // Add the aspect using the given locale, of necessary
        if (!nodeService.hasAspect(contentNodeRef, ContentModel.ASPECT_MULTILINGUAL_DOCUMENT))
        {
            PropertyMap properties = new PropertyMap();
            properties.put(ContentModel.PROP_LOCALE, locale);
            nodeService.addAspect(contentNodeRef, ContentModel.ASPECT_LOCALIZED, properties);
            nodeService.addAspect(contentNodeRef, ContentModel.ASPECT_MULTILINGUAL_DOCUMENT, null);
        }
        else
        {
            // The aspect is present, so just ensure that the locale is correct
            nodeService.setProperty(contentNodeRef, ContentModel.PROP_LOCALE, locale);
        }
        
        if (forceNewVersion)
        {
            versionService.createVersion(contentNodeRef, null);
        }

        // Do we make use of an existing container?
        if (mlContainerNodeRef == null)
        {
            // Make one
            mlContainerNodeRef = getOrCreateMLContainer(contentNodeRef, true);

            Serializable containerFunctionalName = nodeService.getProperty(contentNodeRef, ContentModel.PROP_NAME);

            // set the pivot language and the functional name
            nodeService.setProperty(mlContainerNodeRef, ContentModel.PROP_LOCALE, locale);
            nodeService.setProperty(mlContainerNodeRef, ContentModel.PROP_NAME, containerFunctionalName);
        }
        else
        {
            // ALF-2200: Create the translation as the same type as the pivot
            NodeRef pivotNodeRef = this.getPivotTranslation(mlContainerNodeRef);
            if (pivotNodeRef != null && !pivotNodeRef.equals(contentNodeRef))
            {
                QName pivotNodeType = nodeService.getType(pivotNodeRef);
                QName contentNodeType = nodeService.getType(contentNodeRef);
                if (!pivotNodeType.equals(contentNodeType))
                {
                    nodeService.setType(contentNodeRef, pivotNodeType);
                }
            }
            
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
        
        // Make sure that we don't delete the container if it was previously scheduled for pre-commit deletion.
        // This arises when editions are going to be created
        TransactionalResourceHelper.getSet(KEY_ML_CONTAINERS_TO_DELETE).remove(mlContainerNodeRef);

        // done
        return mlContainerNodeRef;
    }

    private boolean isPivotTranslation(NodeRef contentNodeRef)
    {
        Locale locale = (Locale) nodeService.getProperty(contentNodeRef, ContentModel.PROP_LOCALE);
        // Get the container
        NodeRef containerNodeRef = getOrCreateMLContainer(contentNodeRef, false);
        Locale containerLocale = (Locale) nodeService.getProperty(containerNodeRef, ContentModel.PROP_LOCALE);
        boolean isPivot = EqualsHelper.nullSafeEquals(locale, containerLocale);
        // Done
        if (logger.isDebugEnabled())
        {
            logger.debug("Node " + (isPivot ? "is" : "is not") + " pivot: " + contentNodeRef);
        }
        return isPivot;
    }

    /** {@inheritDoc} */
    public boolean isTranslation(NodeRef contentNodeRef)
    {
        if (!nodeService.hasAspect(contentNodeRef, ContentModel.ASPECT_MULTILINGUAL_DOCUMENT))
        {
            // It doesn't have the aspect, so it isn't a translation
            if (logger.isDebugEnabled())
            {
                logger.debug("Document is not multilingual: " + contentNodeRef);
            }
            return false;
        }
        // Are there any associated translations
        Map<Locale, NodeRef> translations = getTranslations(contentNodeRef);
        if (translations.size() > 0)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Document is a translation: " + contentNodeRef);
            }
            return true;
        }
        else
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Document is not a translation: " + contentNodeRef);
            }
            return false;
        }
    }

    /** {@inheritDoc} */
    public void makeTranslation(NodeRef contentNodeRef, Locale locale)
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
    }


    /** @inheritDoc */
    public void deleteTranslationContainer(NodeRef mlContainerNodeRef)
    {
        if (!ContentModel.TYPE_MULTILINGUAL_CONTAINER.equals(nodeService.getType(mlContainerNodeRef)))
        {
            throw new IllegalArgumentException(
                    "Node type must be " + ContentModel.TYPE_MULTILINGUAL_CONTAINER);
        }

        // get the translations
        Map<Locale, NodeRef> translations = this.getTranslations(mlContainerNodeRef);

        // remember the number of childs
        int translationCount = translations.size();

        // remove the translations
        for(NodeRef translationToRemove : translations.values())
        {
            if (!nodeService.exists(translationToRemove))
            {
                // We've just queried for these
                throw new ConcurrencyFailureException("Translation has been deleted externally: " + translationToRemove);
            }
            nodeService.deleteNode(translationToRemove);
        }

        // Keep track of the container for pre-commit deletion
        TransactionalResourceHelper.getSet(KEY_ML_CONTAINERS_TO_DELETE).add(mlContainerNodeRef);
        AlfrescoTransactionSupport.bindListener(mlContainerCleaner);

        // done
        if (logger.isDebugEnabled())
        {
            logger.debug("ML container removed: \n" +
                    "   Container:  " + mlContainerNodeRef + "\n" +
                    "   Number of translations: " + translationCount);
        }
    }
    
    /**
     * Does the work of making the translation a simple node again.  No parent-child relationships
     * are modified and the pivot-container logic is not done here.
     * 
     * @param translationNodeRef                a translation
     */
    private void unmakeTranslationSimple(NodeRef translationNodeRef)
    {      
        try
        {
            this.policyBehaviourFilter.disableBehaviour(ContentModel.TYPE_MULTILINGUAL_CONTAINER);
            if (nodeService.hasAspect(translationNodeRef, ContentModel.ASPECT_MULTILINGUAL_EMPTY_TRANSLATION))
            {
                nodeService.removeAspect(translationNodeRef, ContentModel.ASPECT_MULTILINGUAL_EMPTY_TRANSLATION);
                nodeService.addAspect(translationNodeRef, ContentModel.ASPECT_TEMPORARY, null);
            }
            else
            {
                if (nodeService.hasAspect(translationNodeRef, ContentModel.ASPECT_MULTILINGUAL_DOCUMENT))
                {
                    nodeService.removeAspect(translationNodeRef, ContentModel.ASPECT_MULTILINGUAL_DOCUMENT);
                }
            }
            List<ChildAssociationRef> assocRefs = nodeService.getParentAssocs(
                    translationNodeRef,
                    ContentModel.ASSOC_MULTILINGUAL_CHILD, 
                    RegexQNamePattern.MATCH_ALL);
            if (assocRefs.size() != 1)
            {
                throw new AlfrescoRuntimeException(
                        "Unable to remove ASSOC_MULTILINGUAL_CHILD on : " + translationNodeRef.toString());
            }
        }
        finally
        {
            this.policyBehaviourFilter.enableBehaviour(ContentModel.TYPE_MULTILINGUAL_CONTAINER);
        }
    }

    /** @inheritDoc */
    public void unmakeTranslation(NodeRef translationNodeRef)
    {
        if ((nodeService.hasAspect(translationNodeRef, ContentModel.ASPECT_MULTILINGUAL_EMPTY_TRANSLATION) ||
                nodeService.hasAspect(translationNodeRef, ContentModel.ASPECT_MULTILINGUAL_DOCUMENT)) &&
                isPivotTranslation(translationNodeRef))
        {
            NodeRef containerNodeRef = getMLContainer(translationNodeRef, true);
            // We have not cleaned up all other translations
            // Mark the container for deletion
            TransactionalResourceHelper.getSet(KEY_ML_CONTAINERS_TO_DELETE).add(containerNodeRef);
            AlfrescoTransactionSupport.bindListener(mlContainerCleaner);
        }
        // Turns the document from a translation into a normal document by removing MLDocument aspect
        unmakeTranslationSimple(translationNodeRef);
    }

    /** {@inheritDoc} */
    public void addTranslation(NodeRef newTranslationNodeRef, NodeRef translationOfNodeRef, Locale locale)
    {
        // Get the container
        NodeRef mlContainerNodeRef = null;

        if(ContentModel.TYPE_MULTILINGUAL_CONTAINER.equals(nodeService.getType(translationOfNodeRef)))
        {
            mlContainerNodeRef = translationOfNodeRef;
        }
        else
        {
            mlContainerNodeRef = getOrCreateMLContainer(translationOfNodeRef, false);
        }

        // Use the existing container to make the new content into a translation
        makeTranslationImpl(mlContainerNodeRef, newTranslationNodeRef, locale);
        // done
        if (logger.isDebugEnabled())
        {
            logger.debug("Added a translation: \n" +
                    "   Translation of:  " + translationOfNodeRef + "\n" +
                    "   New translation: " + newTranslationNodeRef + "\n" +
                    "   Locale:          " + locale);
        }
    }

    /** {@inheritDoc} */
    public NodeRef getTranslationContainer(NodeRef translationNodeRef)
    {
        NodeRef mlContainerNodeRef = getOrCreateMLContainer(translationNodeRef, false);
        // done
        return mlContainerNodeRef;
    }

    /** {@inheritDoc} */
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

    /** {@inheritDoc} */
    public NodeRef getTranslationForLocale(NodeRef translationNodeRef, Locale locale)
    {
        // Get all the translations
        Map<Locale, NodeRef> nodeRefsByLocale = getTranslations(translationNodeRef);
        // Get the closest matching locale
        Set<Locale> locales = nodeRefsByLocale.keySet();
        Locale nearestLocale = I18NUtil.getNearestLocale(locale, locales);
        NodeRef nearestNodeRef = nodeRefsByLocale.get(nearestLocale);
        if (nearestNodeRef == null)
        {
            // There is no translation for the locale, so get the pivot translation
            nearestNodeRef = getPivotTranslation(translationNodeRef);
            if (nearestNodeRef == null)
            {
                // There is no pivot translation, so just use the given node
                nearestNodeRef = translationNodeRef;
            }
        }
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

    /** {@inheritDoc} */
    public List<Locale> getMissingTranslations(NodeRef localizedNodeRef, boolean addThisNodeLocale)
    {
        List<Locale> foundLocales   = new ArrayList<Locale>(getTranslations(localizedNodeRef).keySet());
        List<String> foundLanguages = new ArrayList<String>();


        // transform locales into languages codes
        for(Locale locale : foundLocales)
        {
            foundLanguages.add(locale.getLanguage());
        }

        //    add the locale of the given node if required
        if(addThisNodeLocale)
        {
            Locale localeNode = (Locale) nodeService.getProperty(localizedNodeRef, ContentModel.PROP_LOCALE);

            if(localeNode != null)
            {
                foundLanguages.remove(localeNode.toString());
            }
            else
            {
                logger.warn("No locale found for the node " + localizedNodeRef);
            }
        }

        List<String> missingLanguages = null;

        if(foundLanguages.size() == 0)
        {
            // The given node is the only one available translation and it must
            // be return.
            // MissingLanguages become the entire list pf languages.
            missingLanguages = contentFilterLanguagesService.getFilterLanguages();
        }
        else
        {
            // get the missing languages form the list of content filter languages
            missingLanguages = contentFilterLanguagesService.getMissingLanguages(foundLanguages);
        }
        // construct a list of locales
        List<Locale> missingLocales = new ArrayList<Locale>(missingLanguages.size() + 1);

        for(String lang : missingLanguages)
        {
            missingLocales.add(I18NUtil.parseLocale(lang));
        }

        return missingLocales;
    }

    /** {@inheritDoc} */
    public NodeRef getPivotTranslation(NodeRef nodeRef)
    {
        Locale containerLocale = null;
        if(nodeService.hasAspect(nodeRef, ContentModel.ASPECT_MULTILINGUAL_DOCUMENT))
        {
            NodeRef container = getTranslationContainer(nodeRef);
            containerLocale = (Locale) nodeService.getProperty(container, ContentModel.PROP_LOCALE);
        }
        else if(ContentModel.TYPE_MULTILINGUAL_CONTAINER.equals(nodeService.getType(nodeRef)))
        {
            containerLocale = (Locale) nodeService.getProperty(nodeRef, ContentModel.PROP_LOCALE);
        }
        else
        {
            logger.warn("The node is not multilingual " + nodeRef);
        }
        // Get all the translations
        Map<Locale, NodeRef> nodeRefsByLocale = getTranslations(nodeRef);
        // Get the closest matching locale
        Set<Locale> locales = nodeRefsByLocale.keySet();
        Locale nearestLocale = I18NUtil.getNearestLocale(containerLocale, locales);
        if (nearestLocale == null)
        {
            // There is no pivot translation
            return null;
        }
        else
        {
            return nodeRefsByLocale.get(nearestLocale);
        }
    }

    /**
     * {@inheritDoc}
     */
    public NodeRef addEmptyTranslation(NodeRef translationOfNodeRef, String name, Locale locale)
    {
        boolean hasMLAspect = nodeService.hasAspect(translationOfNodeRef, ContentModel.ASPECT_MULTILINGUAL_DOCUMENT);
        boolean isMLContainer = nodeService.getType(translationOfNodeRef).equals(ContentModel.TYPE_MULTILINGUAL_CONTAINER);

        if (hasMLAspect || isMLContainer)
        {
            // Get the pivot translation
            NodeRef pivotTranslationNodeRef = getPivotTranslation(translationOfNodeRef);
            if (pivotTranslationNodeRef != null)
            {
                // We found a pivot translation, so use it
                translationOfNodeRef = pivotTranslationNodeRef;
            }
            else
            {
                // We use the given translation
            }
        }
        else
        {
            throw new IllegalArgumentException(
                    "Node must have aspect " + ContentModel.ASPECT_MULTILINGUAL_DOCUMENT + ": \n" +
                    "   Translation: " + translationOfNodeRef + "\n" +
                    "   Locale:      " + locale);
        }

        FileInfo translationOfFileInfo = fileFolderService.getFileInfo(translationOfNodeRef);
        String translationOfName = translationOfFileInfo.getName();
        // If name is null, supply one
        if (name == null)
        {
            name = translationOfName;
        }
        // If there is a name clash, add the locale to the main portion of the filename
        if (name.equalsIgnoreCase(translationOfName))
        {
            String localeStr = locale.toString();
            if (localeStr.endsWith("_"))
            {
                localeStr = localeStr.substring(0, localeStr.length() - 1);
            }
            String rawName;
            String extension;
            int index = name.lastIndexOf('.');
            if (index > 0)
            {
                rawName = name.substring(0, index);
                extension = "." + name.substring(index + 1);
            }
            else
            {
                rawName = name;
                extension = "";                 // No extension
            }
            name = rawName + "_" + localeStr + extension;
        }

        // Create the document in the space of the node of reference
        NodeRef parentNodeRef = nodeService.getPrimaryParent(translationOfNodeRef).getParentRef();

        // Create the empty translation.
        // ALF-2200: Create the translation as the same type as the pivot
        QName newTranslationType = nodeService.getType(translationOfNodeRef);
        NodeRef newTranslationNodeRef = fileFolderService.create(
                parentNodeRef,
                name,
                newTranslationType).getNodeRef();

        // add the translation to the container
        addTranslation(newTranslationNodeRef, translationOfNodeRef, locale);

        // Although the content is spoofed from the pivot translation, it isn't done for all services
        // TODO: Fix http://issues.alfresco.com/browse/AR-1487
        ContentData translationOfContentData = (ContentData) nodeService.getProperty(translationOfNodeRef, ContentModel.PROP_CONTENT);
        if (translationOfContentData != null)
        {
            ContentData newTranslationContentData = new ContentData(
                    null,
                    translationOfContentData.getMimetype(),
                    translationOfContentData.getSize(),
                    translationOfContentData.getEncoding(),
                    translationOfContentData.getLocale());
            nodeService.setProperty(newTranslationNodeRef, ContentModel.PROP_CONTENT, newTranslationContentData);
        }

        // set it empty
        nodeService.addAspect(newTranslationNodeRef, ContentModel.ASPECT_MULTILINGUAL_EMPTY_TRANSLATION, null);
        // Initially, the file should be temporary.  This will be changed as soon as some content is added.
        nodeService.addAspect(newTranslationNodeRef, ContentModel.ASPECT_TEMPORARY, null);

        if (logger.isDebugEnabled())
        {
            logger.debug("Added an empty translation: \n" +
                    "   Translation of:  " + translationOfNodeRef + "\n" +
                    "   New translation: " + newTranslationNodeRef + "\n" +
                    "   Locale:          " + locale);
        }

        return newTranslationNodeRef;
    }

    /**
     * @inheritDoc
     */
    public NodeRef copyTranslationContainer(NodeRef mlContainerNodeRef, NodeRef newParentRef, String prefixName) throws Exception
    {
        // There is no need for the properties interceptor here
        boolean wasMLAware = MLPropertyInterceptor.setMLAware(true);
        
        if(!ContentModel.TYPE_MULTILINGUAL_CONTAINER.equals(nodeService.getType(mlContainerNodeRef)))
        {
            throw new IllegalArgumentException(
                    "Node type must be " + ContentModel.TYPE_MULTILINGUAL_CONTAINER);
        }

        // if the container has no translation: nothing to do
        if(nodeService.getChildAssocs(mlContainerNodeRef, ContentModel.ASSOC_MULTILINGUAL_CHILD, RegexQNamePattern.MATCH_ALL).size() < 1)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("MLContainer has no translation "  + mlContainerNodeRef);
            }

            return null;
        }

        // keep a reference to the containing space before copy
        NodeRef spaceBefore = nodeService.getPrimaryParent(getPivotTranslation(mlContainerNodeRef)).getParentRef();

        if(spaceBefore.equals(newParentRef))
        {
            throw new AlfrescoRuntimeException(
                    "Impossible to copy the mlContainer, source folder is the same as the destination container.");
        }

        // get the pivot translation and its locale
        NodeRef pivotNodeRef = getPivotTranslation(mlContainerNodeRef);
        Locale pivotLocale = (Locale) nodeService.getProperty(pivotNodeRef, ContentModel.PROP_LOCALE);
        String pivotName = prefixName + (String) nodeService.getProperty(pivotNodeRef, ContentModel.PROP_NAME);

        if(prefixName == null)
        {
            prefixName = "";
        }

        NodeRef pivotCopyNodeRef = null;

        pivotCopyNodeRef = fileFolderService.copy(pivotNodeRef, newParentRef, pivotName).getNodeRef();

        // make the new pivot multilingual
        this.makeTranslation(pivotCopyNodeRef, pivotLocale);

        // get a reference to the new mlContainer
        NodeRef newMLContainerNodeRef = getMLContainer(pivotCopyNodeRef, false);

        // copy each other translation and make them multilingual too
        for(Map.Entry<Locale, NodeRef> entry : getTranslations(mlContainerNodeRef).entrySet())
        {
            Locale translationLocale = entry.getKey();
            NodeRef translationNodeRef = entry.getValue();

            String name = prefixName + (String) nodeService.getProperty(translationNodeRef, ContentModel.PROP_NAME);

            if(!translationNodeRef.equals(pivotNodeRef))
            {
               if(nodeService.hasAspect(translationNodeRef, ContentModel.ASPECT_MULTILINGUAL_EMPTY_TRANSLATION))
                {
                    // Turn off any empty translation policy behaviours to enabled the copy.
                    this.policyBehaviourFilter.disableBehaviour(ContentModel.ASPECT_MULTILINGUAL_EMPTY_TRANSLATION);
                    this.policyBehaviourFilter.disableBehaviour(ContentModel.ASPECT_MULTILINGUAL_DOCUMENT);

                    try
                    {
                        // copy the translation
                        NodeRef copyNodeRef = fileFolderService.copy(translationNodeRef, newParentRef, name).getNodeRef();

                        // Add it to the newMLContainer
                        nodeService.addChild(
                                newMLContainerNodeRef,
                                copyNodeRef,
                                ContentModel.ASSOC_MULTILINGUAL_CHILD,
                                QNAME_ML_TRANSLATION);
                        
                        // Add the ML aspects back
                        nodeService.addAspect(translationNodeRef, ContentModel.ASPECT_MULTILINGUAL_DOCUMENT, null);
                        nodeService.addAspect(translationNodeRef, ContentModel.ASPECT_MULTILINGUAL_EMPTY_TRANSLATION, null);
                    }
                    finally
                    {
                        this.policyBehaviourFilter.enableBehaviour(ContentModel.ASPECT_MULTILINGUAL_EMPTY_TRANSLATION);
                        this.policyBehaviourFilter.enableBehaviour(ContentModel.ASPECT_MULTILINGUAL_DOCUMENT);
                    }
                }
                else
                {
                    // copy the translation
                    NodeRef copyNodeRef = fileFolderService.copy(translationNodeRef, newParentRef, name).getNodeRef();

                    // add it to the mlContainer
                    this.addTranslation(copyNodeRef, newMLContainerNodeRef, translationLocale);
                    // set its locale property
                    nodeService.setProperty(copyNodeRef, ContentModel.PROP_LOCALE, translationLocale);
                }
            }
            else
            {
                // the pivot is already created
            }
        }

        // The rest of the transaction can have properties modified
        MLPropertyInterceptor.setMLAware(wasMLAware);
        
        if (logger.isDebugEnabled())
        {
            logger.debug("MLContainer copied: \n" +
                    "   Copy of : " + mlContainerNodeRef + "(translations located in " + spaceBefore + ") \n" +
                    "   Copy :  " + newMLContainerNodeRef + "(translations located in " + newParentRef + ") \n");
        }

        return newMLContainerNodeRef;
    }

    /**
     * @inheritDoc
     */
    public void moveTranslationContainer(NodeRef mlContainerNodeRef, NodeRef newParentRef) throws FileExistsException, FileNotFoundException
    {
        if(!ContentModel.TYPE_MULTILINGUAL_CONTAINER.equals(nodeService.getType(mlContainerNodeRef)))
        {
            throw new IllegalArgumentException(
                    "Node type must be " + ContentModel.TYPE_MULTILINGUAL_CONTAINER);
        }

        // if the container has no translation: nothing to do
        if(nodeService.getChildAssocs(mlContainerNodeRef, ContentModel.ASSOC_MULTILINGUAL_CHILD, RegexQNamePattern.MATCH_ALL).size() < 1)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("MLContainer has no translation " + mlContainerNodeRef);
            }

            return;
        }

        // keep a reference to the containing space before moving
        NodeRef spaceBefore = nodeService.getPrimaryParent(getPivotTranslation(mlContainerNodeRef)).getParentRef();

        if(spaceBefore.equals(newParentRef))
        {
            // nothing to do
            return;
        }

        // move each translation
        for(NodeRef translationToMove : getTranslations(mlContainerNodeRef).values())
        {
            fileFolderService.move(translationToMove, newParentRef, null);
        }

        if (logger.isDebugEnabled())
        {
            logger.debug("MLContainer moved: \n" +
                    "   Old location of " + mlContainerNodeRef + " : " + spaceBefore + ") \n" +
                    "   New location of " + mlContainerNodeRef + " : " + newParentRef + ")");
        }
    }

    /**
     * Cleans up any <b>ml:container</b> types that are empty or have lost their pivot translation
     * 
     * @author Derek Hulley
     * @since 4.1.1
     */
    private class MLContainerCleaner extends TransactionListenerAdapter
    {
        @Override
        public void beforeCommit(boolean readOnly)
        {
            if (readOnly)
            {
                return;             // Don't ever expect to be here
            }
            Set<NodeRef> mlContainerNodeRefs = TransactionalResourceHelper.getSet(KEY_ML_CONTAINERS_TO_DELETE);
            for (NodeRef mlContainerNodeRef : mlContainerNodeRefs)
            {
                // Just delete it.
                // Any remaining translations will be cleaned up
                nodeService.deleteNode(mlContainerNodeRef);
            }
        }
    }

    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    public void setPermissionService(PermissionService permissionService)
    {
        this.permissionService = permissionService;
    }

    public void setContentFilterLanguagesService(ContentFilterLanguagesService contentFilterLanguagesService)
    {
        this.contentFilterLanguagesService = contentFilterLanguagesService;
    }

    public void setFileFolderService(FileFolderService fileFolderService)
    {
        this.fileFolderService = fileFolderService;
    }

    public void setVersionService(VersionService versionService)
    {
        this.versionService = versionService;
    }

    public void setPolicyBehaviourFilter(BehaviourFilter policyBehaviourFilter)
    {
        this.policyBehaviourFilter = policyBehaviourFilter;
    }
}