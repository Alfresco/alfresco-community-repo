/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
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

package org.alfresco.repo.web.scripts.facet;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.namespace.NamespaceException;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * This class is the controller for the "facetable-properties.get" web script.
 * 
 * @since 5.0
 * @author Neil Mc Erlean
 */
public class FacetablePropertiesGet extends AbstractSolrFacetConfigAdminWebScript
{
    public static final Log logger = LogFactory.getLog(FacetablePropertiesGet.class);
    public static final String PROPERTIES_KEY = "properties";
    
    private NamespaceService namespaceService;
    
    public void setNamespaceService(NamespaceService service) { this.namespaceService = service; }
    
    @Override protected Map<String, Object> executeImpl(final WebScriptRequest req, final Status status, final Cache cache)
    {
        // Allow all authenticated users view the filters
        return unprotectedExecuteImpl(req, status, cache);
    }
    
    @Override protected Map<String, Object> unprotectedExecuteImpl(WebScriptRequest req, Status status, Cache cache)
    {
        // There are multiple defined URIs for this REST endpoint. Some define a "classname" template var.
        Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
        final String contentClassName = templateVars.get("classname");
        
        QName contentClassQName;
        try
        {
            contentClassQName = contentClassName == null ? null : QName.createQName(contentClassName, namespaceService);
        } catch (NamespaceException e)
        {
            throw new WebScriptException(Status.STATUS_NOT_FOUND, "Unrecognised classname: " + contentClassName, e);
        }
        
        final Map<String, Object> model = new HashMap<>();
        
        final Set<PropertyDefinition> facetableProperties;
        if (contentClassQName == null)
        {
            facetableProperties = facetService.getFacetableProperties();
        }
        else
        {
            facetableProperties = facetService.getFacetableProperties(contentClassQName);
        }
        
        model.put(PROPERTIES_KEY, facetableProperties);
        
        if (logger.isDebugEnabled())
        {
            logger.debug("Retrieved " + facetableProperties.size() + " available facets");
        }
        
        return model;
    }
}
