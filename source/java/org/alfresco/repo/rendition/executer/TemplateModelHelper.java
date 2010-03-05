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
import java.util.Map;

import org.alfresco.repo.model.Repository;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.template.TemplateNode;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.TemplateImageResolver;
import org.alfresco.service.cmr.repository.TemplateService;

/**
 * @author Nick Smith
 */
public class TemplateModelHelper
{
    public static final String KEY_NODE = "node";

    private final Repository repository;
    private final ServiceRegistry serviceRegistry;
    private final TemplateService templateService;

    /**
     * @param templateService
     * @param repository
     * @param serviceRegistry
     */
    public TemplateModelHelper(TemplateService templateService, Repository repository, ServiceRegistry serviceRegistry)
    {
        super();
        this.templateService = templateService;
        this.repository = repository;
        this.serviceRegistry = serviceRegistry;
    }

    /**
     * Builds up the model map used by the {@link TemplateService} to process
     * FreeMarker templates.
     * 
     * @param sourceNode the node containing the content to be processed by the
     *            template.
     * @param templateNode the node containing the template. Can be
     *            <code>null</code>.
     * @param imgResolver the image resolver used to process images. Can be
     *            <code>null</code>.
     * @param paramMap a map of parameters to add to the model. Can be
     *            <code>null</code>.
     * @return the populated model {@link Map}.
     */
    public Map<String, Object> buildModelMap(NodeRef sourceNode,//
                NodeRef templateNode,//
                TemplateImageResolver imgResolver,//
                Map<String, Serializable> paramMap)
    {
        Map<String, Object> model = buildDefaultModel(templateNode, imgResolver);
        TemplateNode sourceTemplateNode = new TemplateNode(sourceNode, serviceRegistry, null);
        // TODO Add xml dom here.
        // model.put("xml", NodeModel.wrap(null));
        model.put(KEY_NODE, sourceTemplateNode);
        if (paramMap != null)
            model.putAll(paramMap);
        return model;
    }

    /**
     * Builds the default model populated with the current user, company home
     * and user home.
     * 
     * @param templateNode the node containing the template. Can be
     *            <code>null</code>.
     * @param imgResolver the image resolver used to process images. Can be
     *            <code>null</code>.
     * @return the default model {@link Map}.
     */
    public Map<String, Object> buildDefaultModel(NodeRef templateNode, TemplateImageResolver imgResolver)
    {
        // The templateNode can be null.
        NodeRef companyHome = repository.getCompanyHome();
        
        // The fully authenticated user below is the username of the person who logged in and
        // who requested the execution of the current rendition. This will not be the
        // same person as the current user as renditions are executed by the system user.
        String fullyAuthenticatedUser = AuthenticationUtil.getFullyAuthenticatedUser();
        NodeRef person = serviceRegistry.getPersonService().getPerson(fullyAuthenticatedUser);
        
        NodeRef userHome = repository.getUserHome(person);
        Map<String, Object> model = templateService.buildDefaultModel(person, companyHome, userHome, templateNode,
                    imgResolver);
        return model;
    }

}
