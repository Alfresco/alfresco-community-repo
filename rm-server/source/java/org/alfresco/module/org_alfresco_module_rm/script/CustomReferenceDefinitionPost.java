/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
package org.alfresco.module.org_alfresco_module_rm.script;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.alfresco.module.org_alfresco_module_rm.RecordsManagementAdminService;
import org.alfresco.service.namespace.QName;
import org.springframework.extensions.surf.util.ParameterCheck;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 * Implementation for Java backed webscript to add RM custom reference definitions
 * to the custom model.
 * 
 * @author Neil McErlean
 */
public class CustomReferenceDefinitionPost extends AbstractRmWebScript
{
    private static final String URL = "url";
    private static final String REF_ID = "refId";
    private static final String TARGET = "target";
    private static final String SOURCE = "source";
    private static final String LABEL = "label";
    private static final String REFERENCE_TYPE = "referenceType";

    private static Log logger = LogFactory.getLog(CustomReferenceDefinitionPost.class);
    
    private RecordsManagementAdminService rmAdminService;
    
    public void setRecordsManagementAdminService(RecordsManagementAdminService rmAdminService)
    {
        this.rmAdminService = rmAdminService;
    }

	/*
     * @see org.alfresco.web.scripts.DeclarativeWebScript#executeImpl(org.alfresco.web.scripts.WebScriptRequest, org.alfresco.web.scripts.Status, org.alfresco.web.scripts.Cache)
     */
    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
    {
        JSONObject json = null;
        Map<String, Object> ftlModel = null;
        try
        {
            json = new JSONObject(new JSONTokener(req.getContent().getContent()));
            
            ftlModel = addCustomReference(req, json);
        }
        catch (IOException iox)
        {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST,
                    "Could not read content from req.", iox);
        }
        catch (JSONException je)
        {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST,
                        "Could not parse JSON from req.", je);
        }
        catch (IllegalArgumentException iae)
        {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST,
                  iae.getMessage(), iae);
        }
        
        return ftlModel;
    }
    
    /**
     * Applies custom properties.
     */
    @SuppressWarnings("unchecked")
    protected Map<String, Object> addCustomReference(WebScriptRequest req, JSONObject json) throws JSONException
    {
        Map<String, Object> result = new HashMap<String, Object>();
        Map<String, Serializable> params = new HashMap<String, Serializable>();
        
        for (Iterator iter = json.keys(); iter.hasNext(); )
        {
            String nextKeyString = (String)iter.next();
            Serializable nextValue = (Serializable)json.get(nextKeyString);
            
            params.put(nextKeyString, nextValue);
        }
        String refTypeParam = (String)params.get(REFERENCE_TYPE);
        ParameterCheck.mandatory(REFERENCE_TYPE, refTypeParam);
        CustomReferenceType refTypeEnum = CustomReferenceType.getEnumFromString(refTypeParam);
        
        boolean isChildAssoc = refTypeEnum.equals(CustomReferenceType.PARENT_CHILD);
        
        if (logger.isDebugEnabled())
        {
            StringBuilder msg = new StringBuilder();
            msg.append("Creating custom ");
            if (isChildAssoc)
            {
                msg.append("child ");
            }
            msg.append("assoc");
            logger.debug(msg.toString());
        }

        QName generatedQName;
        if (isChildAssoc)
        {
            String source = (String)params.get(SOURCE);
            String target = (String)params.get(TARGET);
            
            generatedQName = rmAdminService.addCustomChildAssocDefinition(source, target);
        }
        else
        {
            String label = (String)params.get(LABEL);
            
            generatedQName = rmAdminService.addCustomAssocDefinition(label);
        }

        result.put(REFERENCE_TYPE, refTypeParam);

        String qnameLocalName;
        if (refTypeParam.equals(CustomReferenceType.BIDIRECTIONAL.toString()))
        {
            Serializable labelParam = params.get(LABEL);
            // label is mandatory for bidirectional refs only
            ParameterCheck.mandatory(LABEL, labelParam);

            qnameLocalName = generatedQName.getLocalName();
            result.put(REF_ID, qnameLocalName);
        }
        else if (refTypeParam.equals(CustomReferenceType.PARENT_CHILD.toString()))
        {
            Serializable sourceParam = params.get(SOURCE);
            Serializable targetParam = params.get(TARGET);
            // source,target mandatory for parent/child refs only
            ParameterCheck.mandatory(SOURCE, sourceParam);
            ParameterCheck.mandatory(TARGET, targetParam);

            qnameLocalName = generatedQName.getLocalName();
            result.put(REF_ID, qnameLocalName);
        }
        else
        {
            throw new WebScriptException("Unsupported reference type: " + refTypeParam);
        }
        result.put(URL, req.getServicePath() + "/" + qnameLocalName);
        
        result.put("success", Boolean.TRUE);
        
        return result;
    }
}