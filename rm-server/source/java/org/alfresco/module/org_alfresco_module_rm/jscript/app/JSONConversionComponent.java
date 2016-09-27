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
package org.alfresco.module.org_alfresco_module_rm.jscript.app;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.capability.CapabilityService;
import org.alfresco.module.org_alfresco_module_rm.capability.RMPermissionModel;
import org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanComponentKind;
import org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanService;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.module.org_alfresco_module_rm.record.RecordService;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.PathUtil;
import org.apache.commons.lang.ArrayUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 * Extend JSON conversion component to include RM specifics.
 *
 * @author Roy Wetherall
 */
public class JSONConversionComponent extends org.alfresco.repo.jscript.app.JSONConversionComponent
{
    /** Record service */
    private RecordService recordService;

    /** File plan service */
    private FilePlanService filePlanService;

    /** Capability service */
    private CapabilityService capabilityService;

    /** dictionary service */
    private DictionaryService dictionaryService;

    /** Indicators */
    private List<BaseEvaluator> indicators = new ArrayList<BaseEvaluator>();

    /** Actions */
    private List<BaseEvaluator> actions = new ArrayList<BaseEvaluator>();

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

    /**
     * @param capabilityService capability service
     */
    public void setCapabilityService(CapabilityService capabilityService)
    {
        this.capabilityService = capabilityService;
    }

    /**
     * @param dictionaryService dictionary service
     */
    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }

    /**
     * @param indicator registered indicator
     */
    public void registerIndicator(BaseEvaluator indicator)
    {
        indicators.add(indicator);
    }

    /**
     * @param action registered action
     */
    public void registerAction(BaseEvaluator action)
    {
        actions.add(action);
    }

    /**
     * @see org.alfresco.repo.jscript.app.JSONConversionComponent#setRootValues(org.alfresco.service.cmr.model.FileInfo,
     *      org.json.simple.JSONObject, boolean)
     */
    @SuppressWarnings("unchecked")
    @Override
    protected void setRootValues(FileInfo nodeInfo, JSONObject rootJSONObject, boolean useShortQNames)
    {
        if (nodeInfo != null)
        {
            // Set the base root values
            super.setRootValues(nodeInfo, rootJSONObject, useShortQNames);

            // Get the node reference for convenience
            NodeRef nodeRef = nodeInfo.getNodeRef();

            if (AccessStatus.ALLOWED.equals(capabilityService.getCapabilityAccessState(nodeRef,
                    RMPermissionModel.VIEW_RECORDS)))
            {
                // Indicate whether the node is a RM object or not
                boolean isFilePlanComponent = filePlanService.isFilePlanComponent(nodeInfo.getNodeRef());
                rootJSONObject.put("isRmNode", isFilePlanComponent);

                if (isFilePlanComponent)
                {
                    rootJSONObject.put("rmNode", setRmNodeValues(nodeRef, useShortQNames));

                    // FIXME: Is this the right place to add the information?
                    addInfo(nodeInfo, rootJSONObject);
                }
            }
        }
    }

    /**
     * Helper method to add information about node
     *
     * @param nodeInfo          node information
     * @param rootJSONObject    root JSON object
     */
    @SuppressWarnings("unchecked")
    private void addInfo(final FileInfo nodeInfo, JSONObject rootJSONObject)
    {
        String itemType = (String) rootJSONObject.get("type");
        final QName itemTypeQName = QName.createQName(itemType, namespaceService);

        NodeRef originatingLocation = AuthenticationUtil.runAsSystem(new RunAsWork<NodeRef>()
        {
            public NodeRef doWork()
            {
                NodeRef originatingLocation = null;

                if (dictionaryService.isSubClass(itemTypeQName, ContentModel.TYPE_CONTENT))
                {
                    NodeRef nodeRef = nodeInfo.getNodeRef();
                    List<ChildAssociationRef> parentAssocs = nodeService.getParentAssocs(nodeRef);

                    for (ChildAssociationRef parent : parentAssocs)
                    {
                        // FIXME: What if there is more than a secondary parent?
                        if (!parent.isPrimary())
                        {
                            originatingLocation = parent.getParentRef();

                            // only consider the non-RM parent otherwise we can
                            // run into issues with frozen or transferring records
                            if (!nodeService.hasAspect(originatingLocation, RecordsManagementModel.ASPECT_FILE_PLAN_COMPONENT))
                            {
                                // assume we have found the correct in-place location
                                // FIXME when we support multiple in-place locations
                                break;
                            }
                        }
                    }
                }

                return originatingLocation;
            }
        });

        if (originatingLocation != null)
        {
            String pathSeparator = "/";
            String displayPath = PathUtil.getDisplayPath(nodeService.getPath(originatingLocation), true);
            String[] displayPathElements = displayPath.split(pathSeparator);
            Object[] subPath = ArrayUtils.subarray(displayPathElements, 5, displayPathElements.length);
            StringBuffer originatingLocationPath = new StringBuffer();
            for (int i = 0; i < subPath.length; i++)
            {
                originatingLocationPath.append(pathSeparator).append(subPath[i]);
            }
            rootJSONObject.put("originatingLocationPath", originatingLocationPath.toString());
        }
    }

    /**
     * @param nodeRef
     * @param useShortQName
     * @return
     */
    @SuppressWarnings("unchecked")
    private JSONObject setRmNodeValues(NodeRef nodeRef, boolean useShortQName)
    {
        JSONObject rmNodeValues = new JSONObject();

        // UI convenience type
        rmNodeValues.put("uiType", getUIType(nodeRef));

        // Get the 'kind' of the file plan component
        FilePlanComponentKind kind = filePlanService.getFilePlanComponentKind(nodeRef);
        rmNodeValues.put("kind", kind.toString());

        // File plan node reference
        NodeRef filePlan = filePlanService.getFilePlan(nodeRef);
        rmNodeValues.put("filePlan", filePlan.toString());

        // Unfiled container node reference
        NodeRef unfiledRecordContainer = filePlanService.getUnfiledContainer(filePlan);
        if (unfiledRecordContainer != null)
        {
            rmNodeValues.put("unfiledRecordContainer", unfiledRecordContainer.toString());
            rmNodeValues.put("properties", propertiesToJSON(unfiledRecordContainer, nodeService.getProperties(unfiledRecordContainer), useShortQName));
            QName type = fileFolderService.getFileInfo(unfiledRecordContainer).getType();
            rmNodeValues.put("type", useShortQName ? type.toPrefixString(namespaceService) : type.toString());
        }

        // Set the indicators array
        setIndicators(rmNodeValues, nodeRef);

        // Set the actions array
        setActions(rmNodeValues, nodeRef);

        return rmNodeValues;
    }

    @SuppressWarnings("unchecked")
    private void setIndicators(JSONObject rmNodeValues, NodeRef nodeRef)
    {
        if (indicators != null && !indicators.isEmpty())
        {
            JSONArray jsonIndicators = new JSONArray();

            for (BaseEvaluator indicator : indicators)
            {
                if (indicator.evaluate(nodeRef))
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
        if (actions != null && !actions.isEmpty())
        {
            JSONArray jsonActions = new JSONArray();

            for (BaseEvaluator action : actions)
            {
                if (action.evaluate(nodeRef))
                {
                    jsonActions.add(action.getName());
                }
            }

            rmNodeValues.put("actions", jsonActions);
        }
    }
    
    /**
     * @see org.alfresco.repo.jscript.app.JSONConversionComponent#permissionsToJSON(org.alfresco.service.cmr.repository.NodeRef)
     */
    protected JSONObject permissionsToJSON(final NodeRef nodeRef)
    {
        JSONObject permissionsJSON = null;        
        if (!filePlanService.isFilePlanComponent(nodeRef))
        {
            permissionsJSON = super.permissionsToJSON(nodeRef);
        }
        else
        {
            permissionsJSON = new JSONObject();              
        }
        return permissionsJSON;
    }

    /**
     * Gets the rm 'type' used as a UI convenience and compatibility flag.
     */
    private String getUIType(NodeRef nodeRef)
    {
        String result = "unknown";

        FilePlanComponentKind kind = filePlanService.getFilePlanComponentKind(nodeRef);
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
                    if (recordService.isMetadataStub(nodeRef))
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
                    if (recordService.isMetadataStub(nodeRef))
                    {
                        result = "metadata-stub";
                    }
                    else
                    {
                        if (recordService.isDeclared(nodeRef))
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
                    result = "hold";
                    break;
                }
                case TRANSFER:
                {
                    result = "transfer-container";
                    break;
                }
                case UNFILED_RECORD_FOLDER:
                {
                    result = "unfiled-record-folder";
                    break;
                }
                default:
                {
                    break;
                }
            }
        }

        return result;
    }
}
