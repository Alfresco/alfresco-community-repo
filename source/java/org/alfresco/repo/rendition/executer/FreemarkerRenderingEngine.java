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

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.template.TemplateNode;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.TemplateImageResolver;

/**
 * @author Nick Smith
 * @since 3.3
 */
public class FreemarkerRenderingEngine
            extends BaseTemplateRenderingEngine
{
    public static final String NAME = "freemarkerRenderingEngine";
    private static final String PARAM_IMAGE_RESOLVER = "image_resolver";

    /**
     * The name of the source node as it appears in the model supplied to the freemarker template
     */
    public static final String KEY_NODE = "node";

    private Repository repository;
    private ServiceRegistry serviceRegistry;


    /*
     * @seeorg.alfresco.repo.rendition.executer.AbstractRenderingEngine#
     * getParameterDefinitions()
     */
    @Override
    protected Collection<ParameterDefinition> getParameterDefinitions()
    {
        Collection<ParameterDefinition> paramList = super.getParameterDefinitions();
        paramList.add(new ParameterDefinitionImpl(
                PARAM_IMAGE_RESOLVER,
                DataTypeDefinition.ANY,
                false,
                getParamDisplayLabel(PARAM_IMAGE_RESOLVER)));
        return paramList;
    }


    
    @SuppressWarnings("unchecked")
    @Override
    protected Object buildModel(RenderingContext context)
    {
        // The templateNode can be null.
        NodeRef companyHome = repository.getCompanyHome();
        NodeRef templateNode = getTemplateNode(context);
        Map<String, Serializable> paramMap = context.getCheckedParam(PARAM_MODEL, Map.class);
        TemplateImageResolver imgResolver = context.getCheckedParam(PARAM_IMAGE_RESOLVER, 
                TemplateImageResolver.class);
        
        // The fully authenticated user below is the username of the person who logged in and
        // who requested the execution of the current rendition. This will not be the
        // same person as the current user as renditions are executed by the system user.
        String fullyAuthenticatedUser = AuthenticationUtil.getFullyAuthenticatedUser();
        NodeRef person = serviceRegistry.getPersonService().getPerson(fullyAuthenticatedUser);
        
        NodeRef userHome = repository.getUserHome(person);
        Map<String, Object> model = getTemplateService().buildDefaultModel(person, companyHome, 
                userHome, templateNode, imgResolver);

        TemplateNode sourceTemplateNode = new TemplateNode(context.getSourceNode(), serviceRegistry, imgResolver);
        // TODO Add xml dom here.
        // model.put("xml", NodeModel.wrap(null));
        model.put(KEY_NODE, sourceTemplateNode);
        if (paramMap != null)
            model.putAll(paramMap);
        return model;
    }

    @Override
    protected String getTemplateType()
    {
        return "freemarker";
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
}
