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

package org.alfresco.module.org_alfresco_module_rm.jscript.app;

import static org.alfresco.module.org_alfresco_module_rm.capability.RMPermissionModel.READ_RECORDS;
import static org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel.PROP_COMBINE_DISPOSITION_STEP_CONDITIONS;
import static org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel.PROP_DISPOSITION_EVENT_COMBINATION;
import static org.alfresco.service.cmr.security.AccessStatus.ALLOWED;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.capability.CapabilityService;
import org.alfresco.module.org_alfresco_module_rm.capability.impl.ViewRecordsCapability;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionService;
import org.alfresco.module.org_alfresco_module_rm.event.EventCompletionDetails;
import org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanComponentKind;
import org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanService;
import org.alfresco.module.org_alfresco_module_rm.freeze.FreezeService;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.module.org_alfresco_module_rm.record.RecordService;
import org.alfresco.module.org_alfresco_module_rm.role.FilePlanRoleService;
import org.alfresco.module.org_alfresco_module_rm.role.Role;
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
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 * Extend JSON conversion component to include RM specifics.
 *
 * @author Roy Wetherall
 */
public class JSONConversionComponent extends    org.alfresco.repo.jscript.app.JSONConversionComponent
                                     implements NodeServicePolicies.OnDeleteNodePolicy,
                                                NodeServicePolicies.OnCreateNodePolicy
{
    /** JSON values */
    private static final String IS_RM_NODE = "isRmNode";
    private static final String RM_NODE = "rmNode";
    private static final String IS_RM_SITE_CREATED = "isRmSiteCreated";
    private static final String IS_RECORD_CONTRIBUTOR_GROUP_ENABLED = "isRecordContributorGroupEnabled";
    private static final String RECORD_CONTRIBUTOR_GROUP_NAME = "recordContributorGroupName";
    private static final String IS_VISIBLE_FOR_CURRENT_USER = "isVisibleForCurrentUser";
    private static final String FROZEN_ACTIVE_CONTENT = "frozencontent";

    /** true if record contributor group is enabled, false otherwise */
    private boolean isRecordContributorsGroupEnabled = false;

    /** record contributors group */
    private String recordContributorsGroupName = "RECORD_CONTRIBUTORS";

    /** Record service */
    private RecordService recordService;

    /** File plan service */
    private FilePlanService filePlanService;

    /** File plan role service */
    private FilePlanRoleService filePlanRoleService;

    /** Capability service */
    private CapabilityService capabilityService;

    /** dictionary service */
    private DictionaryService dictionaryService;

    /** site service */
    private SiteService siteService;

    /** freeze service */
    private FreezeService freezeService;

    /**
     * Disposition service
     */
    private DispositionService dispositionService;

    /** Indicators */
    private List<BaseEvaluator> indicators = new ArrayList<>();

    /** Actions */
    private List<BaseEvaluator> actions = new ArrayList<>();

    /** The policy component */
    private PolicyComponent policyComponent;

    /** JSON conversion component cache */
    private SimpleCache<String, Object> jsonConversionComponentCache;

    /** Constants for checking the cache */
    private static final String RM_SITE_EXISTS = "rmSiteExists";

    /**
     * @param enabled   true if enabled, false otherwise
     */
    public void setRecordContributorsGroupEnabled(boolean enabled)
    {
        isRecordContributorsGroupEnabled = enabled;
    }

    /**
     * @param recordContributorsGroupName   record contributors group name
     */
    public void setRecordContributorsGroupName(String recordContributorsGroupName)
    {
        this.recordContributorsGroupName = recordContributorsGroupName;
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

    /**
     * @param filePlanRoleService file plan role service
     */
    public void setFilePlanRoleService(FilePlanRoleService filePlanRoleService)
    {
        this.filePlanRoleService = filePlanRoleService;
    }

    /**
     * @param capabilityService capability service
     */
    public void setCapabilityService(CapabilityService capabilityService)
    {
        this.capabilityService = capabilityService;
    }

    /**
     * @return the filePlanService
     */
    protected FilePlanService getFileplanService()
    {
        return this.filePlanService;
    }

    /**
     * @return the capabilityService
     */
    protected CapabilityService getCapabilityService()
    {
        return this.capabilityService;
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
    protected SimpleCache<String, Object> getJsonConversionComponentCache()
    {
        return this.jsonConversionComponentCache;
    }

    /**
     * Sets the json conversion component cache
     *
     * @param jsonConversionComponentCache The json conversion component cache
     */
    public void setJsonConversionComponentCache(SimpleCache<String, Object> jsonConversionComponentCache)
    {
        this.jsonConversionComponentCache = jsonConversionComponentCache;
    }

    /**
     * @param dispositionService the disposition service
     */
    public void setDispositionService(DispositionService dispositionService)
    {
        this.dispositionService = dispositionService;
    }

    /**
     *
     * @param freezeService
     */
    public void setFreezeService(FreezeService freezeService) { this.freezeService = freezeService; }

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

            rootJSONObject.put("uiType", getUIType(nodeInfo.getNodeRef()));

            // check the existence of the RM site
            checkRmSiteExistence(rootJSONObject);

            // get the record contributor information
            rootJSONObject.put(IS_RECORD_CONTRIBUTOR_GROUP_ENABLED, isRecordContributorsGroupEnabled);
            rootJSONObject.put(RECORD_CONTRIBUTOR_GROUP_NAME, recordContributorsGroupName);

            // Get the node reference for convenience
            NodeRef nodeRef = nodeInfo.getNodeRef();

            if (AccessStatus.ALLOWED.equals(capabilityService.getCapabilityAccessState(nodeRef, ViewRecordsCapability.NAME)))
            {
                // Indicate whether the node is a RM object or not
                boolean isFilePlanComponent = filePlanService.isFilePlanComponent(nodeRef);
                rootJSONObject.put(IS_RM_NODE, isFilePlanComponent);

                if (isFilePlanComponent)
                {
                    rootJSONObject.put(RM_NODE, setRmNodeValues(nodeRef, useShortQNames));

                    // FIXME: Is this the right place to add the information?
                    addInfo(nodeInfo, rootJSONObject);
                }
            }
            Set<NodeRef> filePlans = filePlanService.getFilePlans();
            if (!CollectionUtils.isEmpty(filePlans))
            {
                NodeRef filePlanNodeRef = filePlans.stream().findFirst().orElse(null);
                if (filePlanNodeRef != null)
                {
                    Set<Role> roles = filePlanRoleService.getRolesByUser(filePlanNodeRef, AuthenticationUtil.getFullyAuthenticatedUser());
                    boolean hasFilingPermission = !CollectionUtils.isEmpty(roles);
                    rootJSONObject.put(IS_VISIBLE_FOR_CURRENT_USER, hasFilingPermission);
                }
            }
        }
    }

    /**
     * Checks for the existence of the RM site
     *
     * @param rootJSONObject    the root JSON object
     */
    @SuppressWarnings("unchecked")
    private void checkRmSiteExistence(JSONObject rootJSONObject)
    {
        if (!getJsonConversionComponentCache().contains(RM_SITE_EXISTS))
        {
            SiteInfo site = siteService.getSite(FilePlanService.DEFAULT_RM_SITE_ID);
            if (site != null)
            {
                getJsonConversionComponentCache().put(RM_SITE_EXISTS, true);
                rootJSONObject.put(IS_RM_SITE_CREATED, true);
            }
            else
            {
                getJsonConversionComponentCache().put(RM_SITE_EXISTS, false);
                rootJSONObject.put(IS_RM_SITE_CREATED, false);
            }
        }
        else
        {
            rootJSONObject.put(IS_RM_SITE_CREATED, getJsonConversionComponentCache().get(RM_SITE_EXISTS));
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
                        // RM-3930
                        if (!parent.isPrimary())
                        {
                            originatingLocation = parent.getParentRef();

                            // only consider the non-RM parent otherwise we can
                            // run into issues with frozen or transferring records
                            if (!nodeService.hasAspect(originatingLocation, RecordsManagementModel.ASPECT_FILE_PLAN_COMPONENT))
                            {
                                // assume we have found the correct in-place location
                                // FIXME when we support multiple in-place locations
                                // See RM-3929
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
            // add the originating location (if there is one)
            String pathSeparator = "/";
            String displayPath = getDisplayPath(originatingLocation);
            String[] displayPathElements = displayPath.split(pathSeparator);
            Object[] subPath = ArrayUtils.subarray(displayPathElements, 5, displayPathElements.length);
            StringBuilder originatingLocationPath = new StringBuilder();
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
        return AuthenticationUtil.runAsSystem(new RunAsWork<String>()
        {
            public String doWork() throws Exception
            {
                return PathUtil.getDisplayPath(nodeService.getPath(nodeRef), true);
            }
        });
    }

    /**
     * Helper method to set the RM node values
     *
     * @param nodeRef               node reference
     * @param useShortQName         indicates whether the short QName are used or not
     * @return {@link JSONObject}   JSON object containing values
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

        // set the primary parent node reference
        ChildAssociationRef assoc = nodeService.getPrimaryParent(nodeRef);
        if (assoc != null)
        {
            rmNodeValues.put("primaryParentNodeRef", assoc.getParentRef().toString());
        }

        Map<String, Object> values = AuthenticationUtil.runAsSystem(new RunAsWork<Map<String, Object>>()
        {
            public Map<String, Object> doWork() throws Exception
            {
                Map<String, Object> result = new HashMap<>();

                // File plan node reference
                NodeRef filePlan = filePlanService.getFilePlan(nodeRef);
                if (filePlan != null)
                {
                    result.put("filePlan", filePlan.toString());

                    // Unfiled container node reference
                    NodeRef unfiledRecordContainer = filePlanService.getUnfiledContainer(filePlan);
                    if (unfiledRecordContainer != null)
                    {
                        result.put("unfiledRecordContainer", unfiledRecordContainer.toString());
                        result.put("properties", propertiesToJSON(unfiledRecordContainer, nodeService.getProperties(unfiledRecordContainer), useShortQName));
                        QName type = fileFolderService.getFileInfo(unfiledRecordContainer).getType();
                        result.put("type", useShortQName ? type.toPrefixString(namespaceService) : type.toString());
                    }
                }

                return result;
            }
         });

        rmNodeValues.putAll(values);

        // Set the indicators array
        setIndicators(rmNodeValues, nodeRef);

        // Set the actions array
        setActions(rmNodeValues, nodeRef);

        AuthenticationUtil.runAsSystem((RunAsWork<Void>) () -> {
            //Add details of the next incomplete event in the disposition schedule
            if (dispositionService.getNextDispositionAction(nodeRef) != null)
            {
                for (EventCompletionDetails details : dispositionService.getNextDispositionAction(nodeRef).getEventCompletionDetails())
                {
                    if (!details.isEventComplete())
                    {
                        HashMap properties = (HashMap) rmNodeValues.get("properties");
                        properties.put("combineDispositionStepConditions", nodeService.getProperty(dispositionService.getNextDispositionAction(nodeRef).getDispositionActionDefinition().getNodeRef(), PROP_COMBINE_DISPOSITION_STEP_CONDITIONS));
                        properties.put("incompleteDispositionEvent", details.getEventName());
                        properties.put("dispositionEventCombination", nodeService.getProperty(dispositionService.getNextDispositionAction(nodeRef).getDispositionActionDefinition().getNodeRef(), PROP_DISPOSITION_EVENT_COMBINATION));

                        break;
                    }
                }
            }
            return null;
        });

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
    @SuppressWarnings("unchecked")
    protected JSONObject permissionsToJSON(final NodeRef nodeRef)
    {
        JSONObject permissionsJSON = new JSONObject();
        if (!filePlanService.isFilePlanComponent(nodeRef))
        {
            permissionsJSON = super.permissionsToJSON(nodeRef);
        }
        else
        {
            if (ALLOWED.equals(permissionService.hasPermission(nodeRef, READ_RECORDS)))
            {
                permissionsJSON.put("inherited", permissionService.getInheritParentPermissions(nodeRef));
                permissionsJSON.put("roles", allSetPermissionsToJSON(nodeRef));
                permissionsJSON.put("user", userPermissionsToJSON(nodeRef));
            }
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
        else if (freezeService.isFrozen(nodeRef))
        {
            result = FROZEN_ACTIVE_CONTENT;
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
