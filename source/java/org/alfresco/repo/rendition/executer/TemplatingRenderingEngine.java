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

package org.alfresco.repo.rendition.executer;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.io.Writer;
import java.util.Collection;
import java.util.Map;

import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.repo.model.Repository;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.rendition.RenditionServiceException;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.repository.TemplateImageResolver;
import org.alfresco.service.cmr.repository.TemplateService;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;

/**
 * @author Nick Smith
 */
public class TemplatingRenderingEngine//
            extends AbstractRenderingEngine//
            implements InitializingBean
{
    private final static Log log = LogFactory.getLog(TemplatingRenderingEngine.class);
    
    public static final String NAME = "templatingRenderingEngine";
    public static final String PARAM_MODEL = "model";
    public static final String PARAM_TEMPLATE = "template_string";
    public static final String PARAM_TEMPLATE_NODE = "template_node";
    public static final String PARAM_TEMPLATE_PATH = "template_path";
    private static final String PARAM_IMAGE_RESOLVER = "image_resolver";

    private TemplateService templateService;
    private Repository repository;
    private ServiceRegistry serviceRegistry;
    private TemplateModelHelper modelHelper;

    /*
     * @see
     * org.alfresco.repo.rendition.executer.AbstractRenderingEngine#render(org
     * .alfresco.service.cmr.repository.NodeRef,
     * org.alfresco.service.cmr.rendition.RenditionDefinition,
     * org.alfresco.service.cmr.repository.ContentReader,
     * org.alfresco.service.cmr.repository.ChildAssociationRef)
     */
    @SuppressWarnings("unchecked")
    @Override
    protected void render(RenderingContext context)
    {
        NodeRef sourceNode = context.getSourceNode();
        NodeRef templateNode = getTemplateNode(context);
        Map<String, Serializable> paramMap = context.getCheckedParam(PARAM_MODEL, Map.class);
        TemplateImageResolver imgResolver = context.getCheckedParam(PARAM_IMAGE_RESOLVER, TemplateImageResolver.class);
        Map<String, Object> model = modelHelper.buildModelMap(sourceNode, templateNode, imgResolver, paramMap);

        processTemplate(context, templateNode, model);
    }

    private void processTemplate(RenderingContext context, NodeRef templateNode, Map<String, Object> model)
    {
        String template = context.getCheckedParam(PARAM_TEMPLATE, String.class);
        if ((template == null) && (templateNode == null))
        {
            throwTemplateParamsNotFoundException();
        }

        ContentWriter contentWriter = context.makeContentWriter();
        Writer writer = new OutputStreamWriter(contentWriter.getContentOutputStream());
        try
        {
            if (template != null)
            {
                templateService.processTemplateString("freemarker", template, model, writer);
            }
            else if (templateNode != null)
            {
                templateService.processTemplate("freemarker", templateNode.toString(), model, writer);
            }
        }
        finally
        {
            try
            {
                writer.close();
            } catch (IOException ex)
            {
                // Nothing that can be done. Log it and move on.
                log.warn("Failed to close content writer: ", ex);
            }
        }
    }

    private void throwTemplateParamsNotFoundException()
    {
        StringBuilder msg = new StringBuilder("This action requires that either the ");
        msg.append(PARAM_TEMPLATE);
        msg.append(" parameter or the ");
        msg.append(PARAM_TEMPLATE_NODE);
        msg.append(" parameter be specified. ");
        throw new RenditionServiceException(msg.toString());
    }

    private NodeRef getTemplateNode(RenderingContext context)
    {
        NodeRef node = context.getCheckedParam(PARAM_TEMPLATE_NODE, NodeRef.class);
        if (node == null)
        {
            String path = context.getCheckedParam(PARAM_TEMPLATE_PATH, String.class);
            if (path != null && path.length() > 0)
            {
                SearchService searchService = serviceRegistry.getSearchService();
                StoreRef storeRef = context.getDestinationNode().getStoreRef();
                ResultSet result = searchService.query(storeRef, SearchService.LANGUAGE_XPATH, path);
                if (result.length() != 1)
                {
                    throw new RenditionServiceException("Could not find template node for path: " + path);
                }
                node = result.getNodeRef(0);
            }
        }
        return node;
    }

    /*
     * @seeorg.alfresco.repo.rendition.executer.AbstractRenderingEngine#
     * getParameterDefinitions()
     */
    @Override
    protected Collection<ParameterDefinition> getParameterDefinitions()
    {
        Collection<ParameterDefinition> paramList = super.getParameterDefinitions();
        ParameterDefinitionImpl modelParamDef = new ParameterDefinitionImpl(//
                    PARAM_MODEL,//
                    DataTypeDefinition.ANY,//
                    false,//
                    getParamDisplayLabel(PARAM_MODEL));
        ParameterDefinitionImpl templateParamDef = new ParameterDefinitionImpl(//
                    PARAM_TEMPLATE,//
                    DataTypeDefinition.TEXT,//
                    false,//
                    getParamDisplayLabel(PARAM_TEMPLATE));
        ParameterDefinitionImpl templateNodeParamDef = new ParameterDefinitionImpl(//
                    PARAM_TEMPLATE_NODE,//
                    DataTypeDefinition.NODE_REF,//
                    false,//
                    getParamDisplayLabel(PARAM_TEMPLATE_NODE));
        ParameterDefinitionImpl templatePathParamDef = new ParameterDefinitionImpl(//
                    PARAM_TEMPLATE_PATH,//
                    DataTypeDefinition.TEXT,//
                    false,//
                    getParamDisplayLabel(PARAM_TEMPLATE_PATH));
        ParameterDefinitionImpl imgResolverParamDef = new ParameterDefinitionImpl(//
                    PARAM_IMAGE_RESOLVER,//
                    DataTypeDefinition.ANY,//
                    false,//
                    getParamDisplayLabel(PARAM_IMAGE_RESOLVER));
        paramList.add(modelParamDef);
        paramList.add(templateParamDef);
        paramList.add(templateNodeParamDef);
        paramList.add(templatePathParamDef);
        paramList.add(imgResolverParamDef);
        return paramList;
    }

    /**
     * @param templateService the templateService to set
     */
    public void setTemplateService(TemplateService templateService)
    {
        this.templateService = templateService;
    }

    /**
     * @param repository the repository to set
     */
    public void setRepositoryHelper(Repository repository)
    {
        this.repository = repository;
    }

    /**
     * @param serviceRegistry the serviceRegistry to set
     */
    public void setServiceRegistry(ServiceRegistry serviceRegistry)
    {
        this.serviceRegistry = serviceRegistry;
    }

    /**
     * Sets up {@link TemplateModelHelper}.
     */
    public void afterPropertiesSet()
    {
        this.modelHelper = new TemplateModelHelper(templateService, repository, serviceRegistry);
    }
}
