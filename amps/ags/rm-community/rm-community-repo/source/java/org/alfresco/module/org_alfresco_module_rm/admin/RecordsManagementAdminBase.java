/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.module.org_alfresco_module_rm.admin;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementCustomModel;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.dictionary.DictionaryRepositoryBootstrap;
import org.alfresco.repo.dictionary.M2Model;
import org.alfresco.repo.dictionary.M2Namespace;
import org.alfresco.service.cmr.dictionary.AspectDefinition;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryException;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * Base class for RM admin services
 *
 * @author Tuna Aksoy
 * @since 2.3
 */
public class RecordsManagementAdminBase implements RecordsManagementCustomModel
{
    /** Logger */
    protected Log logger = LogFactory.getLog(this.getClass());

    /** Constants */
    private static final String SOURCE_TARGET_ID_SEPARATOR = "__";
    private static final NodeRef RM_CUSTOM_MODEL_NODE_REF = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "records_management_custom_model");

    /** I18N */
    private static final String MSG_CUSTOM_MODEL_NOT_FOUND = "rm.admin.custom-model-not-found";
    private static final String MSG_CUSTOM_MODEL_NO_CONTENT = "rm.admin.custom-model-no-content";
    private static final String MSG_ERROR_WRITE_CUSTOM_MODEL = "rm.admin.error-write-custom-model";
    private static final String MSG_ERROR_SPLIT_ID = "rm.admin.error-split-id";

    /** Dictionary service */
    private DictionaryService dictionaryService;

    /** Node service */
    private NodeService nodeService;

    /** Content service */
    private ContentService contentService;

    /** Namespace service */
    private NamespaceService namespaceService;

    /** Dictionary repository bootstrap */
    private DictionaryRepositoryBootstrap dictionaryRepositoryBootstrap;

    /**
     * Gets the dictionary service instance
     *
     * @return The dictionary service instance
     */
    protected DictionaryService getDictionaryService()
    {
        return this.dictionaryService;
    }

    /**
     * Gets the node service instance
     *
     * @return The node service instance
     */
    protected NodeService getNodeService()
    {
        return this.nodeService;
    }

    /**
     * Gets the content service instance
     *
     * @return The content service instance
     */
    protected ContentService getContentService()
    {
        return this.contentService;
    }

    /**
     * Gets the namespace service instance
     *
     * @return The namespace service instance
     */
    protected NamespaceService getNamespaceService()
    {
        return this.namespaceService;
    }

    /**
     * Gets the dictionary repository bootstrap instance
     *
     * @return The dictionary repository bootstrap instance
     */
    protected DictionaryRepositoryBootstrap getDictionaryRepositoryBootstrap()
    {
        return this.dictionaryRepositoryBootstrap;
    }

    /**
     * Sets the dictionary service instance
     *
     * @param dictionaryService The dictionary service instance
     */
    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }

    /**
     * Sets the node service instance
     *
     * @param nodeService The node service instance
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    /**
     * Sets the content service instance
     *
     * @param contentService The content service instance
     */
    public void setContentService(ContentService contentService)
    {
        this.contentService = contentService;
    }

    /**
     * Sets the namespace service instance
     *
     * @param namespaceService The namespace service instance
     */
    public void setNamespaceService(NamespaceService namespaceService)
    {
        this.namespaceService = namespaceService;
    }

    /**
     * Sets the dictionary repository bootstrap instance
     *
     * @param dictionaryRepositoryBootstrap The dictionary repository bootstrap instance
     */
    public void setDictionaryRepositoryBootstrap(DictionaryRepositoryBootstrap dictionaryRepositoryBootstrap)
    {
        this.dictionaryRepositoryBootstrap = dictionaryRepositoryBootstrap;
    }

    /**
     * Gets all the custom associations
     *
     * @return All custom associations
     */
    protected Map<QName, AssociationDefinition> getCustomAssociations()
    {
        Map<QName, AssociationDefinition> customAssociations = new HashMap<>();

        AspectDefinition aspectDefn = getDictionaryService().getAspect(ASPECT_CUSTOM_ASSOCIATIONS);
        if (aspectDefn != null)
        {
            customAssociations.putAll(aspectDefn.getAssociations());
        }

        return customAssociations;
    }

    /**
     * Gets the node reference of the custom model
     *
     * @param uri The URI of the model namespace
     * @return The node reference of the custom model
     */
    protected NodeRef getCustomModelRef(String uri)
    {
        if ((uri.equals("")) || (uri.equals(RecordsManagementModel.RM_CUSTOM_URI)))
        {
            // note: short-cut for "rmc" currently assumes that RM custom model does not define additional namespaces
            return RM_CUSTOM_MODEL_NODE_REF;
        }
        else
        {
            // ALF-5875
            List<NodeRef> modelRefs = getDictionaryRepositoryBootstrap().getModelRefs();

            for (NodeRef modelRef : modelRefs)
            {
                try
                {
                    M2Model model = readCustomContentModel(modelRef);

                    for (M2Namespace namespace : model.getNamespaces())
                    {
                        if (namespace.getUri().equals(uri))
                        {
                            return modelRef;
                        }
                    }
                }
                catch (DictionaryException de)
                {
                    logger.warn("readCustomContentModel: skip model ("+modelRef+") whilst searching for uri ("+uri+"): ", de);
                }
            }

            throw new AlfrescoRuntimeException(I18NUtil.getMessage(MSG_CUSTOM_MODEL_NOT_FOUND, uri));
        }
    }

    /**
     * Gets the deserialized model
     *
     * @param modelNodeRef The node reference of the model
     * @return The deserialized model
     */
    protected M2Model readCustomContentModel(NodeRef modelNodeRef)
    {
        ContentReader reader = getContentService().getReader(modelNodeRef, ContentModel.TYPE_CONTENT);
        if (!reader.exists())
        {
            throw new AlfrescoRuntimeException(I18NUtil.getMessage(MSG_CUSTOM_MODEL_NO_CONTENT, modelNodeRef.toString()));
        }

        InputStream contentIn = null;
        M2Model deserializedModel = null;

        try
        {
            contentIn = reader.getContentInputStream();
            deserializedModel = M2Model.createModel(contentIn);
        }
        finally
        {
            try
            {
                if (contentIn != null)
                {
                    contentIn.close();
                }
            }
            catch (IOException ignored)
            {
                // Intentionally empty.
            }
        }

        return deserializedModel;
    }

    /**
     * Updates the content of the custom model
     *
     * @param modelRef The node reference of the model
     * @param deserializedModel The deserialized model
     */
    protected void writeCustomContentModel(NodeRef modelRef, M2Model deserializedModel)
    {
        ContentWriter writer = getContentService().getWriter(modelRef, ContentModel.TYPE_CONTENT, true);
        writer.setMimetype(MimetypeMap.MIMETYPE_XML);
        writer.setEncoding("UTF-8");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        deserializedModel.toXML(baos);

        String updatedModelXml;
        try
        {
            updatedModelXml = baos.toString("UTF-8");
            writer.putContent(updatedModelXml);
            // putContent closes all resources.
            // so we don't have to.
        }
        catch (UnsupportedEncodingException uex)
        {
            throw new AlfrescoRuntimeException(I18NUtil.getMessage(MSG_ERROR_WRITE_CUSTOM_MODEL, modelRef.toString()), uex);
        }
    }

    /**
     * Checks if the given association definition title exists
     *
     * @param associationDefinitionTitle The association definition title
     * @return <code>true</code> if the association definition title exists, <code>false</code> otherwise
     */
    protected boolean existsTitle(String associationDefinitionTitle)
    {
        boolean existsLabel = false;
        Collection<AssociationDefinition> associationDefinitions = getCustomAssociations().values();
        for (AssociationDefinition associationDefinition : associationDefinitions)
        {
            if (associationDefinition.getTitle(getDictionaryService()).equalsIgnoreCase(associationDefinitionTitle))
            {
                existsLabel = true;
            }
        }
        return existsLabel;
    }

    /**
     * Splits the association definition title into source text and target text
     *
     * @param sourceTargetText The text to split into source text and target text
     * @return Splited association definition title which includes source text and target text
     */
    protected String[] splitAssociationDefinitionTitle(String sourceTargetText)
    {
        if (!sourceTargetText.contains(SOURCE_TARGET_ID_SEPARATOR))
        {
            throw new IllegalArgumentException(I18NUtil.getMessage(MSG_ERROR_SPLIT_ID, sourceTargetText, SOURCE_TARGET_ID_SEPARATOR));
        }

        return sourceTargetText.split(SOURCE_TARGET_ID_SEPARATOR);
    }

    /**
     * Creates the association definition title form the source text and target text
     *
     * @param sourceText The source text
     * @param targetText The target text
     * @return The association definition title created from the source text and target text
     */
    protected String composeAssociationDefinitionTitle(String sourceText, String targetText)
    {
        if (sourceText.contains(SOURCE_TARGET_ID_SEPARATOR))
        {
            throw new IllegalArgumentException("sourceId cannot contain '" + SOURCE_TARGET_ID_SEPARATOR + "': " + sourceText);
        }

        StringBuilder sb = new StringBuilder();
        sb.append(sourceText)
            .append(SOURCE_TARGET_ID_SEPARATOR)
            .append(targetText);

        return sb.toString();
    }
}
