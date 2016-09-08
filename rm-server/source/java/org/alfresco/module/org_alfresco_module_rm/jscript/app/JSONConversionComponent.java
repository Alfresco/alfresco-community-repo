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
package org.alfresco.module.org_alfresco_module_rm.jscript.app;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.module.org_alfresco_module_rm.FilePlanComponentKind;
import org.alfresco.module.org_alfresco_module_rm.RecordsManagementService;
import org.alfresco.module.org_alfresco_module_rm.capability.CapabilityService;
import org.alfresco.module.org_alfresco_module_rm.capability.RMPermissionModel;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 * Extend JSON conversion component to include RM specifics.
 * 
 * @author Roy Wetherall
 */
public class JSONConversionComponent extends org.alfresco.repo.jscript.app.JSONConversionComponent
{
    /** Records management service */
    private RecordsManagementService recordsManagementService;
    
    /** Capability service */
    private CapabilityService capabilityService;
    
    /** Indicators */
    private List<BaseEvaluator> indicators = new ArrayList<BaseEvaluator>();
    
    /** Actions */
    private List<BaseEvaluator> actions = new ArrayList<BaseEvaluator>();
    
    /**
     * @param recordsManagementService  records management service
     */
    public void setRecordsManagementService(RecordsManagementService recordsManagementService)
    {
        this.recordsManagementService = recordsManagementService;
    }    
    
    /**
     * @param capabilityService     capability service
     */
    public void setCapabilityService(CapabilityService capabilityService)
    {
        this.capabilityService = capabilityService;
    }
    
    /**
     * @param indicator  registered indicator
     */
    public void registerIndicator(BaseEvaluator indicator)
    {
        indicators.add(indicator);
    }
    
    /**
     * @param action    registered action
     */
    public void registerAction(BaseEvaluator action)
    {
        actions.add(action);
    }

    /**
     * @see org.alfresco.repo.jscript.app.JSONConversionComponent#setRootValues(org.alfresco.service.cmr.model.FileInfo, org.json.simple.JSONObject, boolean)
     */
    @SuppressWarnings("unchecked")
    @Override
    protected void setRootValues(FileInfo nodeInfo, JSONObject rootJSONObject, boolean useShortQNames)
    {
        // Set the base root values
        super.setRootValues(nodeInfo, rootJSONObject, useShortQNames);
        
        // Get the node reference for convenience
        NodeRef nodeRef = nodeInfo.getNodeRef();
                
        if (AccessStatus.ALLOWED.equals(capabilityService.getCapabilityAccessState(nodeRef, RMPermissionModel.VIEW_RECORDS)) == true)
        {
            // Indicate whether the node is a RM object or not
            boolean isFilePlanComponent = recordsManagementService.isFilePlanComponent(nodeInfo.getNodeRef());
            rootJSONObject.put("isRmNode", isFilePlanComponent);
            
            if (isFilePlanComponent == true)
            {                    
                rootJSONObject.put("rmNode", setRmNodeValues(nodeRef, rootJSONObject, useShortQNames));
            }
        }
    }
    
    /**
     * 
     * @param nodeRef
     * @param rootJSONObject
     * @param useShortQName
     * @return
     */
    @SuppressWarnings("unchecked")
    private JSONObject setRmNodeValues(NodeRef nodeRef, JSONObject rootJSONObject, boolean useShortQName)
    {
        JSONObject rmNodeValues = new JSONObject();
        
        // UI convenience type
        rmNodeValues.put("uiType", getUIType(nodeRef));

        // Get the 'kind' of the file plan component
        FilePlanComponentKind kind = recordsManagementService.getFilePlanComponentKind(nodeRef);
        rmNodeValues.put("kind", kind.toString());        
        
        // File plan node reference
        rmNodeValues.put("filePlan", recordsManagementService.getFilePlan(nodeRef).toString());
        
        // Set the indicators array
        setIndicators(rmNodeValues, nodeRef);
        
        // Set the actions array
        setActions(rmNodeValues, nodeRef);
        
        return rmNodeValues;
    }
  
    @SuppressWarnings("unchecked")
    private void setIndicators(JSONObject rmNodeValues, NodeRef nodeRef)
    {
        if (indicators != null && indicators.isEmpty() == false)
        {
            JSONArray jsonIndicators = new JSONArray();
            
            for (BaseEvaluator indicator : indicators)
            {
                if (indicator.evaluate(nodeRef) == true)
                {
                    jsonIndicators.add(indicator.getName());
                }
            }
            
            rmNodeValues.put("indicators", jsonIndicators);
        }
    }
    
    @SuppressWarnings("unchecked")
    private void setActions(JSONObject rmNodeValues, NodeRef nodeRef)
    {
        if (actions != null && actions.isEmpty() == false)
        {
            JSONArray jsonActions = new JSONArray();
            
            for (BaseEvaluator action : actions)
            {
                if (action.evaluate(nodeRef) == true)
                {
                    jsonActions.add(action.getName());
                }
            }

            rmNodeValues.put("actions", jsonActions);
        }
    }
    
    /**
     * Gets the rm 'type' used as a UI convenience and compatibility flag.
     */
    private String getUIType(NodeRef nodeRef)
    {
        String result = "unknown";
        
        FilePlanComponentKind kind = recordsManagementService.getFilePlanComponentKind(nodeRef); 
        if (kind != null)
        {
            switch (kind)
            {
                case FILE_PLAN:
                {       
                    result = "fileplan";
                    break;
                }
                case RECORD_CATEGORY:
                {
                    result = "record-category";
                    break;
                }
                case RECORD_FOLDER:
                {
                    if (recordsManagementService.isMetadataStub(nodeRef) == true)
                    {
                        result = "metadata-stub-folder";
                    }
                    else
                    {
                        result = "record-folder";
                    }
                    break;
                }
                case RECORD:
                {
                    if (recordsManagementService.isMetadataStub(nodeRef) == true)
                    {
                        result = "metadata-stub";
                    }
                    else
                    {
                        if (recordsManagementService.isRecordDeclared(nodeRef) == true)
                        {
                            result = "record";
                        }
                        else
                        {
                            result = "undeclared-record";
                        }
                    }
                    break;
                }
                case HOLD:
                {
                    result = "hold-container";
                    break;
                }
                case TRANSFER:
                {
                    result = "transfer-container";
                    break;
                }
            }        
        }
        
        return result;
    }
}
