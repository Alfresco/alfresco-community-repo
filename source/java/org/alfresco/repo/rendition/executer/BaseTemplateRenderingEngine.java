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
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.rendition.RenditionServiceException;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.repository.TemplateService;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * This abstract class forms a basis for all rendering engines that are built
 * around the Template Service.<br>
 * A template must be specified either as a {@link String} using the
 * PARAM_TEMPLATE parameter, as a {@link NodeRef} using the PARAM_TEMPLATE_NODE
 * parameter or as a file path location using the PARAM_TEMPLATE_PATH parameter.
 * The RenderingEngine reads from these parameters with the following priority:
 * PARAM_TEMPLATE > PARAM_TEMPLATE_NODE > PARAM_TEMPLATE_PATH.
 * 
 * @author Brian Remmington
 * @since 3.3
 */
public abstract class BaseTemplateRenderingEngine extends AbstractRenderingEngine
{
    private static final Log log = LogFactory.getLog(BaseTemplateRenderingEngine.class);

    /**
     * This optional {@link Map}<{@link String}, {@link Serializable}> parameter
     * can be used to pass additional arguments to the templating engine when processing a
     * template. 
     */
    public static final String PARAM_MODEL = "model";

    /**
     * This optional {@link String} parameter specifies the template in a simple
     * {@link String} format.<br>
     * If this parameter is set the Rendering Engine will
     * use it in preference to templates specified by either the
     * PARAM_TEMPLATE_NODE or the PARAM_TEMPLATE_PATH parameters.
     */
    public static final String PARAM_TEMPLATE = "template_string";

    /**
     * This optional {@link NodeRef} parameter specifies a node containing the
     * template to be processed.<br>
     * If a value is specified for PARAM_TEMPLATE then this parameter will be
     * ignored.<br>
     * If a value is specified for this parameter it will be used in preference
     * to values specified for the PARAM_TEMPLATE_PATH parameter.
     */
    public static final String PARAM_TEMPLATE_NODE = "template_node";

    /**
     * This optional {@link String} parameter specifies a file path location for
     * the template to be processed.<br>
     * If a value is specified for PARAM_TEMPLATE or PARAM_TEMPLATE_NODE then this parameter will be
     * ignored.<br>
     */
    public static final String PARAM_TEMPLATE_PATH = "template_path";

    private TemplateService templateService;
    private SearchService searchService;

    /*
     * @see org.alfresco.repo.rendition.executer.AbstractRenderingEngine#render(org
     * .alfresco.service.cmr.repository.NodeRef, org.alfresco.service.cmr.rendition.RenditionDefinition,
     * org.alfresco.service.cmr.repository.ContentReader, org.alfresco.service.cmr.repository.ChildAssociationRef)
     */
    @Override
    protected void render(RenderingContext context)
    {
        NodeRef templateNode = getTemplateNode(context);
        Writer writer = null;
        try
        {
            Object model = buildModel(context);
            ContentWriter contentWriter = context.makeContentWriter();
            writer = new OutputStreamWriter(contentWriter.getContentOutputStream());
            processTemplate(context, templateNode, model, writer);
        }
        catch (RuntimeException ex)
        {
            throw ex;
        }
        catch (Exception ex)
        {
            log.warn("Unexpected error while rendering through XSLT rendering engine.", ex);
        }
        finally
        {
            if (writer != null)
            {
                try
                {
                    writer.flush();
                    writer.close();
                }
                catch (IOException ex)
                {
                    log.warn("Failed to correctly close content writer.", ex);
                }
            }
        }
    }

    private void processTemplate(RenderingContext context, NodeRef templateNode, Object model, Writer out)
    {
        String templateType = getTemplateType();
        String template = context.getCheckedParam(PARAM_TEMPLATE, String.class);
        if (template != null)
        {
            templateService.processTemplateString(templateType, template, model, out);
        }
        else if (templateNode != null)
        {
            templateService.processTemplate(templateType, templateNode.toString(), model, out);
        }
        else
        {
            throwTemplateParamsNotFoundException();
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

    protected NodeRef getTemplateNode(RenderingContext context)
    {
        NodeRef node = context.getCheckedParam(PARAM_TEMPLATE_NODE, NodeRef.class);
        if (node == null)
        {
            String path = context.getCheckedParam(PARAM_TEMPLATE_PATH, String.class);
            if (path != null && path.length() > 0)
            {
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

    /**
     * Create the model that will be passed to the template service for rendering
     * with the appropriate template.
     * @param context The context of the rendering request
     * @return The model that is to be passed to the template service
     */
    protected abstract Object buildModel(RenderingContext context);

    /**
     * Get the type of template that is to be used. This identifies the name of the template
     * processor that should be used, such as "freemarker" or "xslt".
     * @return
     */
    protected abstract String getTemplateType();

    /**
     * This method gets the parameter definition display label from the properties file.
     * It looks first for a property whose key has a fixed rendition service-specific
     * prefix and if that gets null, it then delegates to the standard bean name-based
     * approach.
     * 
     * @param paramName  the name of the parameter
     * @return           the display label of the parameter
     */
    @Override
    protected String getParamDisplayLabel(String paramName)
    {
        // First we try to get the message using a common prefix for all template-based rendering engines.
        final String commonPropertiesPrefix = "baseTemplateRenderingAction";
        String message = I18NUtil.getMessage(commonPropertiesPrefix + "." + paramName + "." + DISPLAY_LABEL);
        
        // And if that doesn't work we delegate to the superclass.
        if (message == null)
        {
            message = super.getParamDisplayLabel(paramName);
        }
        return message;
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.repo.rendition.executer.AbstractRenderingEngine#getParameterDefinitions()
     */
    @Override
    protected Collection<ParameterDefinition> getParameterDefinitions()
    {
        Collection<ParameterDefinition> paramList = super.getParameterDefinitions();
        ParameterDefinitionImpl modelParamDef = new ParameterDefinitionImpl(PARAM_MODEL, DataTypeDefinition.ANY, false,
                getParamDisplayLabel(PARAM_MODEL));
        ParameterDefinitionImpl templateParamDef = new ParameterDefinitionImpl(//
                PARAM_TEMPLATE, DataTypeDefinition.TEXT, false, getParamDisplayLabel(PARAM_TEMPLATE));
        ParameterDefinitionImpl templateNodeParamDef = new ParameterDefinitionImpl(PARAM_TEMPLATE_NODE,
                DataTypeDefinition.NODE_REF, false, getParamDisplayLabel(PARAM_TEMPLATE_NODE));
        ParameterDefinitionImpl templatePathParamDef = new ParameterDefinitionImpl(PARAM_TEMPLATE_PATH,
                DataTypeDefinition.TEXT, false, getParamDisplayLabel(PARAM_TEMPLATE_PATH));
        paramList.add(new ParameterDefinitionImpl(PARAM_MIME_TYPE, DataTypeDefinition.TEXT, false,
                getParamDisplayLabel(PARAM_MIME_TYPE)));
        paramList.add(modelParamDef);
        paramList.add(templateParamDef);
        paramList.add(templateNodeParamDef);
        paramList.add(templatePathParamDef);
        return paramList;
    }

    /**
     * @param templateService
     *            the templateService to set
     */
    public void setTemplateService(TemplateService templateService)
    {
        this.templateService = templateService;
    }

    /**
     * @param searchService
     *            the searchService to set
     */
    public void setSearchService(SearchService searchService)
    {
        this.searchService = searchService;
    }

    public TemplateService getTemplateService()
    {
        return templateService;
    }

    public SearchService getSearchService()
    {
        return searchService;
    }
}
