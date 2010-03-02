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
package org.alfresco.repo.web.scripts.content;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.alfresco.cmis.CMISDictionaryModel;
import org.alfresco.cmis.CMISObjectReference;
import org.alfresco.cmis.CMISServiceException;
import org.alfresco.cmis.CMISServices;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.cmis.reference.ObjectPathReference;
import org.alfresco.repo.cmis.reference.ReferenceFactory;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;


/**
 * Content Write Service
 * 
 * Stream content to the Repository.
 * 
 * @author davidc
 */
public class ContentSet extends AbstractWebScript
{
    // Logger
    @SuppressWarnings("unused")
    private static final Log logger = LogFactory.getLog(ContentSet.class);
    
    // Component dependencies
    private ReferenceFactory referenceFactory;
    private DictionaryService dictionaryService;
    private NamespaceService namespaceService;
    private CMISServices cmisService;
    private MimetypeService mimetypeService;
    
    /**
     * @param reference factory
     */
    public void setReferenceFactory(ReferenceFactory referenceFactory)
    {
        this.referenceFactory = referenceFactory; 
    }

    /**
     * @param dictionaryService
     */
    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService; 
    }

    /**
     * @param namespaceService
     */
    public void setNamespaceService(NamespaceService namespaceService)
    {
        this.namespaceService = namespaceService; 
    }

    /**
     * @param cmisServices
     */
    public void setCmisService(CMISServices cmisService)
    {
        this.cmisService = cmisService;
    }

    /**
     * @param mimetypeService
     */
    public void setMimetypeService(MimetypeService mimetypeService)
    {
        this.mimetypeService = mimetypeService; 
    }

    /**
     * @see org.alfresco.web.scripts.WebScript#execute(org.alfresco.web.scripts.WebScriptRequest, org.alfresco.web.scripts.WebScriptResponse)
     */
    public void execute(WebScriptRequest req, WebScriptResponse res)
        throws IOException
    {   
        // create map of args
        String[] names = req.getParameterNames();
        Map<String, String> args = new HashMap<String, String>(names.length, 1.0f);
        for (String name : names)
        {
            args.put(name, req.getParameter(name));
        }
        
        // create map of template vars
        Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
        
        // create object reference from url
        CMISObjectReference reference = referenceFactory.createObjectReferenceFromUrl(args, templateVars);
        NodeRef nodeRef = reference.getNodeRef();
        if (nodeRef == null)
        {
            throw new WebScriptException(HttpServletResponse.SC_NOT_FOUND, "Unable to find " + reference.toString());
        }
        
        // determine content property
        QName propertyQName = ContentModel.PROP_CONTENT;
        String contentPart = templateVars.get("property");
        if (contentPart.length() > 0 && contentPart.charAt(0) == ';')
        {
            if (contentPart.length() < 2)
            {
                throw new WebScriptException(HttpServletResponse.SC_BAD_REQUEST, "Content property malformed");
            }
            String propertyName = contentPart.substring(1);
            if (propertyName.length() > 0)
            {
                propertyQName = QName.createQName(propertyName, namespaceService);
            }
        }
        PropertyDefinition propertyDef = dictionaryService.getProperty(propertyQName);
        if (propertyDef == null)
        {
            throw new WebScriptException(HttpServletResponse.SC_NOT_FOUND, "Unable to find content property " + propertyQName + " of " + reference.toString());
        }
        if (!propertyDef.getDataType().getName().equals(DataTypeDefinition.CONTENT))
        {
            throw new WebScriptException(HttpServletResponse.SC_NOT_FOUND, "Content stream not found");
        }

        // ensure content can be overwritten
        String overwrite = req.getParameter("overwriteFlag");
        
        // establish mimetype
        String mimetype = req.getContentType();
        if (mimetype == null)
        {
            if (reference instanceof ObjectPathReference)
            {
                mimetype = mimetypeService.guessMimetype(((ObjectPathReference)reference).getPath());
            }
        }

        try
        {
            boolean isUpdate = cmisService.setContentStream((String) cmisService.getProperty(nodeRef,
                    CMISDictionaryModel.PROP_OBJECT_ID), propertyQName, overwrite == null || overwrite.equals("true"),
                    req.getContent().getInputStream(), mimetype);

            // set status
            res.setStatus(isUpdate ? Status.STATUS_OK : Status.STATUS_CREATED);
        }
        catch (CMISServiceException e)
        {
            throw new WebScriptException(e.getStatusCode(), e.getMessage(), e);
        }
    }
}