/*
 * Copyright (C) 2007 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.repo.model.ml;

import java.util.List;
import java.util.Locale;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.ml.MultilingualContentService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
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
     * @return Returns the <b>cm:mlContainer</b> parent
     */
    private NodeRef getOrCreateMLContainer(NodeRef mlDocumentNodeRef)
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
            // Create a ML container
            mlContainerNodeRef = makeMLContainer();
            createAssociation = true;
        }
        else if (parentAssocRefs.size() == 1)
        {
            // Just get it
            ChildAssociationRef toKeepAssocRef = parentAssocRefs.get(0);
            mlContainerNodeRef = toKeepAssocRef.getParentRef();
            createAssociation = true;
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

    public NodeRef makeTranslation(NodeRef contentNodeRef, Locale locale)
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
        // Get or create the container
        NodeRef mlContainerNodeRef = getOrCreateMLContainer(contentNodeRef);
        // done
        return mlContainerNodeRef;
    }

    public NodeRef addTranslation(NodeRef newTranslationNodeRef, NodeRef translationOfNodeRef, Locale locale)
    {
        throw new UnsupportedOperationException();
    }

    public NodeRef getTranslationContainer(NodeRef translationNodeRef)
    {
        throw new UnsupportedOperationException();
    }

    public NodeRef createEdition(NodeRef mlContainerNodeRef, NodeRef translationNodeRef)
    {
        throw new UnsupportedOperationException();
    }
}
