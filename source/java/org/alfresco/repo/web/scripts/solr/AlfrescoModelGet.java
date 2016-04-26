/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.repo.web.scripts.solr;

import java.io.IOException;
import java.util.Map;

import org.alfresco.repo.solr.AlfrescoModel;
import org.alfresco.repo.solr.SOLRTrackingComponent;
import org.alfresco.service.cmr.dictionary.ModelDefinition;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

/**
 * Support for SOLR: Get Alfresco model
 *
 * @since 4.0
 */
public class AlfrescoModelGet extends AbstractWebScript
{
    protected static final Log logger = LogFactory.getLog(AlfrescoModelGet.class);

    private NamespaceService namespaceService;
    private SOLRTrackingComponent solrTrackingComponent;

    public void setSolrTrackingComponent(SOLRTrackingComponent solrTrackingComponent)
    {
        this.solrTrackingComponent = solrTrackingComponent;
    }

    public void setNamespaceService(NamespaceService namespaceService)
    {
        this.namespaceService = namespaceService;
    }

    public void execute(WebScriptRequest req, WebScriptResponse res)
    {
        try
        {
            handle(req, res);
        }
        catch(IOException e)
        {
            throw new WebScriptException("IO exception parsing request", e);
        }
        catch(JSONException e)
        {
            throw new WebScriptException("Invalid JSON", e);
        }
    }
    
    private void handle(WebScriptRequest req, WebScriptResponse res) throws JSONException, IOException
    {
        // create map of template vars
        String modelQName = req.getParameter("modelQName");
        if(modelQName == null)
        {
            throw new WebScriptException(
                    Status.STATUS_BAD_REQUEST,
                    "URL parameter 'modelQName' not provided.");
        }

        ModelDefinition.XMLBindingType bindingType = ModelDefinition.XMLBindingType.DEFAULT;
        AlfrescoModel model = solrTrackingComponent.getModel(QName.createQName(modelQName));
        res.setHeader("XAlfresco-modelChecksum", String.valueOf(model.getModelDef().getChecksum(bindingType)));
        model.getModelDef().toXML(bindingType, res.getOutputStream());
    }

}
