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

import static org.alfresco.module.org_alfresco_module_rm.capability.RMPermissionModel.READ_RECORDS;
import static org.alfresco.repo.security.authentication.AuthenticationUtil.runAsSystem;
import static org.alfresco.service.cmr.security.AccessStatus.ALLOWED;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.capability.CapabilityService;
import org.alfresco.module.org_alfresco_module_rm.capability.RMPermissionModel;
import org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanComponentKind;
import org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanService;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.module.org_alfresco_module_rm.record.RecordService;
import org.alfresco.repo.cache.SimpleCache;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.namespace.NamespaceService;
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
public class JSONConversionComponent extends org.alfresco.repo.jscript.app.JSONConversionComponent implements NodeServicePolicies.OnDeleteNodePolicy,
                                                                                                              NodeServicePolicies.OnCreateNodePolicy
{
    /** Record service */
    private RecordService recordService;

    /** File plan service */
    private FilePlanService filePlanService;

    /** Capability service */
    private CapabilityService capabilityService;

    /** dictionary service */
    private DictionaryService dictionaryService;

    /** site service */
    private SiteService siteService;

    /** Indicators */
    private List<BaseEvaluator> indicators = new ArrayList<BaseEvaluator>();

    /** Actions */
    private List<BaseEvaluator> actions = new ArrayList<BaseEvaluator>();

    /** The policy component */
    private PolicyComponent policyComponent;

    /** JSON conversion component cache */
    private SimpleCache<String, Boolean> jsonConversionComponentCache;

    /** Constant for checking the cache */
    private static final String RM_SITE_EXISTS = "rmSiteExists";

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
     * @param siteService site service
     */
    public void setSiteService(SiteService siteService)
    {
        this.siteService = siteService;
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
     * @param policyComponent policy component
     */
    public void setPolicyComponent(PolicyComponent policyComponent)
    {
        this.policyComponent = policyComponent;
    }

    /**
     * Gets the json conversion component cache
     *
     * @return The json conversion component cache
     */
    protected SimpleCache<String, Boolean> getJsonConversionComponentCache()
    {
        return this.jsonConversionComponentCache;
    }

    /**
     * Sets the json conversion component cache
     *
     * @param jsonConversionComponentCache The json conversion component cache
     */
    public void setJsonConversionComponentCache(SimpleCache<String, Boolean> jsonConversionComponentCache)
    {
        this.jsonConversionComponentCache = jsonConversionComponentCache;
    }

    /**
     * The initialise method
     */
    public void init()
    {
        policyComponent.bindClassBehaviour(
                QName.createQName(NamespaceService.ALFRESCO_URI, "onDeleteNode"),
                RecordsManagementModel.TYPE_RM_SITE,
                new JavaBehaviour(this, "onDeleteNode"));

        policyComponent.bindClassBehaviour(
                QName.createQName(NamespaceService.ALFRESCO_URI, "onCreateNode"),
                RecordsManagementModel.TYPE_RM_SITE,
                new JavaBehaviour(this, "onCreateNode"));
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

            checkRmSiteExistence(rootJSONObject);

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

    @SuppressWarnings("unchecked")
    private void checkRmSiteExistence(JSONObject rootJSONObject)
    {
        if (!getJsonConversionComponentCache().contains(RM_SITE_EXISTS))
        {
            SiteInfo site = siteService.getSite(FilePlanService.DEFAULT_RM_SITE_ID);
            if (site != null)
            {
                getJsonConversionComponentCache().put(RM_SITE_EXISTS, true);
                rootJSONObject.put("isRmSiteCreated", true);
            }
            else
            {
                getJsonConversionComponentCache().put(RM_SITE_EXISTS, false);
                rootJSONObject.put("isRmSiteCreated", false);
            }
        }
        else
        {
            rootJSONObject.put("isRmSiteCreated", getJsonConversionComponentCache().get(RM_SITE_EXISTS));
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
            String displayPath = getDisplayPath(originatingLocation);
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
     * Helper method to get the display path.
     *
     * @param nodeRef   node reference
     * @return String   display path
     */
    private String getDisplayPath(final NodeRef nodeRef)
    {
        return AuthenticationUtil.runAs(new RunAsWork<String>()
        {
            public String doWork() throws Exception
            {
                return PathUtil.getDisplayPath(nodeService.getPath(nodeRef), true);
            }
        }, AuthenticationUtil.getAdminUserName());
    }

    /**
     * @param nodeRef
     * @param useShortQName
     * @return
     */
    @SuppressWarnings("unchecked")
    private JSONObject setRmNodeValues(final NodeRef nodeRef, final boolean useShortQName)
    {
    	JSONObject rmNodeValues = new JSONObject();

        // UI convenience type
        rmNodeValues.put("uiType", getUIType(nodeRef));

        // Get the 'kind' of the file plan component
        FilePlanComponentKind kind = filePlanService.getFilePlanComponentKind(nodeRef);
        rmNodeValues.put("kind", kind.toString());

        // File plan node reference
        NodeRef filePlan = getFilePlan(nodeRef);
        if (permissionService.hasPermission(filePlan, READ_RECORDS).equals(ALLOWED))
        {
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
        }

        // Set the indicators array
        setIndicators(rmNodeValues, nodeRef);

        // Set the actions array
        setActions(rmNodeValues, nodeRef);

        return rmNodeValues;
    }

    /**
     * Helper method to get the file plan as a system user for the given node
     *
     * @param nodeRef The node reference
     * @return The file plan where the node is in
     */
    private NodeRef getFilePlan(final NodeRef nodeRef)
    {
        return runAsSystem(new RunAsWork<NodeRef>()
        {
            public NodeRef doWork()
            {
                return filePlanService.getFilePlan(nodeRef);
            }
        });
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

    /**
     * @see org.alfresco.repo.node.NodeServicePolicies.OnDeleteNodePolicy#onDeleteNode(org.alfresco.service.cmr.repository.ChildAssociationRef, boolean)
     */
    @Override
    public void onDeleteNode(ChildAssociationRef childAssocRef, boolean isNodeArchived)
    {
        getJsonConversionComponentCache().put(RM_SITE_EXISTS, false);
    }

    /**
     * @see org.alfresco.repo.node.NodeServicePolicies.OnCreateNodePolicy#onCreateNode(org.alfresco.service.cmr.repository.ChildAssociationRef)
     */
    @Override
    public void onCreateNode(ChildAssociationRef childAssocRef)
    {
        getJsonConversionComponentCache().put(RM_SITE_EXISTS, true);
    }
}
