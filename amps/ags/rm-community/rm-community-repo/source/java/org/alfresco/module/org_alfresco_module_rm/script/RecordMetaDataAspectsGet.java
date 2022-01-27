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

package org.alfresco.module.org_alfresco_module_rm.script;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanService;
import org.alfresco.module.org_alfresco_module_rm.record.RecordService;
import org.alfresco.service.cmr.dictionary.AspectDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Record metadata aspects GET
 * 
 * @author Roy Wetherall
 */
public class RecordMetaDataAspectsGet extends DeclarativeWebScript
{
    /** parameters */
    private static final String PARAM_NODEREF = "noderef";
    
    protected DictionaryService dictionaryService;
    protected NamespaceService namespaceService;
    protected RecordService recordService;
    protected FilePlanService filePlanService;
    
    /**
     * Set the dictionary service instance
     * 
     * @param dictionaryService the {@link DictionaryService} instance
     */
    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }
    
    /**
     * Sets the {@link NamespaceService} instance
     * 
     * @param namespaceService The {@link NamespaceService} instance
     */
    public void setNamespaceService(NamespaceService namespaceService)
    {
        this.namespaceService = namespaceService;
    }

    /**
     * @param recordService record service
     */
    public void setRecordService(RecordService recordService)
    {
        this.recordService = recordService;
    }
    
    /**
     * @param filePlanService   file plan service
     */
    public void setFilePlanService(FilePlanService filePlanService)
    {
        this.filePlanService = filePlanService;
    }
    
    /*
     * @see org.alfresco.web.scripts.DeclarativeWebScript#executeImpl(org.alfresco.web.scripts.WebScriptRequest, org.alfresco.web.scripts.Status, org.alfresco.web.scripts.Cache)
     */
    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
    {
        // Get the nodeRef and confirm it is valid        
        NodeRef nodeRef = null;
        String nodeRefValue = req.getParameter(PARAM_NODEREF);
        if (nodeRefValue == null || nodeRefValue.trim().length() == 0)
        {
            // get the file plan if a node ref hasn't been specified
            // TODO will need to remove when multi file plans supported
            nodeRef = filePlanService.getFilePlanBySiteId(FilePlanService.DEFAULT_RM_SITE_ID);
               
        }
        else
        {
            nodeRef = new NodeRef(nodeRefValue);
        }
        
        // Get the details of all the aspects
        Set<QName> aspectQNames = recordService.getRecordMetadataAspects(nodeRef);        
        List<Map<String, Object>> aspects = new ArrayList<>(aspectQNames.size() + 1);
        for (QName aspectQName : aspectQNames)
        {
            // Get the prefix aspect and default the label to the localname 
            String prefixString = aspectQName.toPrefixString(namespaceService);
            String label = aspectQName.getLocalName();
             
            Map<String, Object> aspect = new HashMap<>(2);
            aspect.put("id", prefixString);
            
            // Try and get the aspect definition 
            AspectDefinition aspectDefinition = dictionaryService.getAspect(aspectQName);
            if (aspectDefinition != null)
            {
                // Fet the label from the aspect definition
                label = aspectDefinition.getTitle(dictionaryService);
            }            
            aspect.put("value", label);
            
            // Add the aspect details to the aspects list
            aspects.add(aspect);
        }
        
        // create model object with the lists model
        Map<String, Object> model = new HashMap<>(1);
        model.put("aspects", aspects);
        return model;
    }
}
