/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
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

package org.alfresco.rm.rest.api.impl;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.RecordsManagementServiceRegistry;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionSchedule;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionService;
import org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanService;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.site.SiteModel;
import org.alfresco.repo.tenant.TenantUtil;
import org.alfresco.rest.api.impl.NodesImpl;
import org.alfresco.rest.api.model.Node;
import org.alfresco.rest.api.model.UserInfo;
import org.alfresco.rest.framework.core.exceptions.EntityNotFoundException;
import org.alfresco.rest.framework.core.exceptions.PermissionDeniedException;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.rm.rest.api.RMNodes;
import org.alfresco.rm.rest.api.model.FileplanComponentNode;
import org.alfresco.rm.rest.api.model.RecordCategoryNode;
import org.alfresco.rm.rest.api.model.RecordFolderNode;
import org.alfresco.rm.rest.api.model.RecordNode;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.alfresco.util.ParameterCheck;

/**
 * Centralizes access to the repository.
 *
 * @author Ana Bozianu
 * @since 2.6
 */
public class RMNodesImpl extends NodesImpl implements RMNodes
{
    private enum RMNodeType
    {
        // Note: ordered
        CATEGORY, RECORD_FOLDER, FILE
    }

    private FilePlanService filePlanService;
    private NodeService nodeService;
    private RecordsManagementServiceRegistry serviceRegistry;
    private Repository repositoryHelper;
    private DictionaryService dictionaryService;
    private DispositionService dispositionService;
    private SiteService siteService;

    /**
     * TODO to remove this after isSpecialNode is made protected in core implementation(REPO-1459)
     */
    private ConcurrentHashMap<String,NodeRef> ddCache = new ConcurrentHashMap<>();
    public void init()
    {
        super.init();
        this.nodeService = serviceRegistry.getNodeService();
        this.dictionaryService = serviceRegistry.getDictionaryService();
        this.dispositionService = serviceRegistry.getDispositionService();
        this.siteService = serviceRegistry.getSiteService();
    }

    public void setRecordsManagementServiceRegistry(RecordsManagementServiceRegistry serviceRegistry)
    {
        this.serviceRegistry = serviceRegistry;
    }

    public void setRepositoryHelper(Repository repositoryHelper)
    {
        super.setRepositoryHelper(repositoryHelper);
        this.repositoryHelper = repositoryHelper;
    }

    public void setFilePlanService(FilePlanService filePlanService)
    {
        this.filePlanService = filePlanService;
    }

    @Override
    public Node getFolderOrDocument(final NodeRef nodeRef, NodeRef parentNodeRef, QName nodeTypeQName, List<String> includeParam, Map<String, UserInfo> mapUserInfo)
    {
        Node originalNode = super.getFolderOrDocument(nodeRef, parentNodeRef, nodeTypeQName, includeParam, mapUserInfo);

        if(nodeTypeQName == null)
        {
            nodeTypeQName = nodeService.getType(nodeRef);
        }

        //TODO to remove this part of code after isSpecialNode will be made protected on core, will not need this anymore since the right allowed operations will be returned from core(REPO-1459).
        if (includeParam.contains(PARAM_INCLUDE_ALLOWABLEOPERATIONS) && originalNode.getAllowableOperations() != null)
        {
            List<String> allowableOperations = originalNode.getAllowableOperations();
            List<String> modifiedAllowableOperations = new ArrayList<>();
            modifiedAllowableOperations.addAll(allowableOperations);

            for (String op : allowableOperations)
            {
                if (op.equals(OP_DELETE) && (isSpecialNode(nodeRef, nodeTypeQName)))
                {
                    // special case: do not return "delete" (as an allowable op) for specific system nodes
                    modifiedAllowableOperations.remove(op);
                }
            }

            originalNode.setAllowableOperations((modifiedAllowableOperations.size() > 0 )? modifiedAllowableOperations : null);
        }


        RMNodeType type = getType(nodeTypeQName, nodeRef);
        FileplanComponentNode node = null;
        if (mapUserInfo == null)
        {
            mapUserInfo = new HashMap<>(2);
        }

        if (type == null)
        {
            if (filePlanService.isFilePlanComponent(nodeRef))
            {
                node = new FileplanComponentNode(originalNode);
            }
            else
            {
                throw new InvalidParameterException("The provided node is not a fileplan component");
            }
        }
        else
        {
            switch(type)
            {
                case CATEGORY:
                    RecordCategoryNode categoryNode = new RecordCategoryNode(originalNode);
                    if (includeParam.contains(PARAM_INCLUDE_HAS_RETENTION_SCHEDULE))
                    {
                        DispositionSchedule ds = dispositionService.getDispositionSchedule(nodeRef);
                        categoryNode.setHasRetentionSchedule(ds!=null?true:false);
                    }
                    node = categoryNode;
                    break;
                case RECORD_FOLDER:
                    RecordFolderNode rfNode = new RecordFolderNode(originalNode);
                    if (includeParam.contains(PARAM_INCLUDE_IS_CLOSED))
                    {
                        rfNode.setIsClosed((Boolean) nodeService.getProperty(nodeRef, RecordsManagementModel.PROP_IS_CLOSED));
                    }
                    node = rfNode;
                    break;
                case FILE:
                    RecordNode rNode = new RecordNode(originalNode);
                    if (includeParam.contains(PARAM_INCLUDE_IS_COMPLETED))
                    {
                        rNode.setIsCompleted(nodeService.hasAspect(nodeRef, RecordsManagementModel.ASPECT_DECLARED_RECORD));
                    }
                    node = rNode;
                    break;
            }
        }

        return node;
    }

    @Override
    public NodeRef validateNode(String nodeId)
    {
        ParameterCheck.mandatoryString("nodeId", nodeId);

        if (nodeId.equals(PATH_FILE_PLAN))
        {
            NodeRef filePlan = filePlanService.getFilePlanBySiteId(FilePlanService.DEFAULT_RM_SITE_ID);
            if (filePlan != null)
            {
                return filePlan;
            }
            else
            {
                throw new EntityNotFoundException(nodeId);
            }
        }
        else if (nodeId.equals(PATH_TRANSFERS))
        {
            NodeRef filePlan = filePlanService.getFilePlanBySiteId(FilePlanService.DEFAULT_RM_SITE_ID);
            if (filePlan != null)
            {
                return filePlanService.getTransferContainer(filePlan);
            }
            else
            {
                throw new EntityNotFoundException(nodeId);
            }
        }
        else if (nodeId.equals(PATH_UNFILED))
        {
            NodeRef filePlan = filePlanService.getFilePlanBySiteId(FilePlanService.DEFAULT_RM_SITE_ID);
            if (filePlan != null)
            {
                return filePlanService.getUnfiledContainer(filePlan);
            }
            else
            {
                throw new EntityNotFoundException(nodeId);
            }
        }
        else if (nodeId.equals(PATH_HOLDS))
        {
            NodeRef filePlan = filePlanService.getFilePlanBySiteId(FilePlanService.DEFAULT_RM_SITE_ID);
            if (filePlan != null)
            {
                return filePlanService.getHoldContainer(filePlan);
            }
            else
            {
                throw new EntityNotFoundException(nodeId);
            }
        }

        return super.validateNode(nodeId);
    }

    private RMNodeType getType(QName typeQName, NodeRef nodeRef)
    {
        // quick check for common types
        if (typeQName.equals(RecordsManagementModel.TYPE_RECORD_FOLDER))
        {
            return RMNodeType.RECORD_FOLDER;
        }
        if (typeQName.equals(RecordsManagementModel.TYPE_RECORD_CATEGORY))
        {
            return RMNodeType.CATEGORY;
        }
        if (typeQName.equals(ContentModel.TYPE_CONTENT))
        {
            return RMNodeType.FILE;
        }

        // check subclasses
        if (dictionaryService.isSubClass(typeQName, ContentModel.TYPE_CONTENT))
        {
            return RMNodeType.FILE;
        }
        if (dictionaryService.isSubClass(typeQName, RecordsManagementModel.TYPE_RECORD_FOLDER))
        {
            return RMNodeType.RECORD_FOLDER;
        }

        return null;
    }

    @Override
    protected Pair<Set<QName>, Set<QName>> buildSearchTypesAndIgnoreAspects(QName nodeTypeQName, boolean includeSubTypes, Set<QName> ignoreQNameTypes, Boolean includeFiles, Boolean includeFolders)
    {
        Pair<Set<QName>, Set<QName>> searchTypesAndIgnoreAspects = super.buildSearchTypesAndIgnoreAspects(nodeTypeQName, includeSubTypes, ignoreQNameTypes, includeFiles, includeFolders);
        Set<QName> searchTypeQNames = searchTypesAndIgnoreAspects.getFirst();
        Set<QName> ignoreAspectQNames = searchTypesAndIgnoreAspects.getSecond();

        searchTypeQNames.remove(RecordsManagementModel.TYPE_HOLD_CONTAINER);
        searchTypeQNames.remove(RecordsManagementModel.TYPE_UNFILED_RECORD_CONTAINER);
        searchTypeQNames.remove(RecordsManagementModel.TYPE_TRANSFER_CONTAINER);

        searchTypeQNames.remove(RecordsManagementModel.TYPE_DISPOSITION_SCHEDULE);
        searchTypeQNames.remove(RecordsManagementModel.TYPE_DISPOSITION_ACTION);
        searchTypeQNames.remove(RecordsManagementModel.TYPE_DISPOSITION_ACTION_DEFINITION);

        return new Pair<>(searchTypeQNames, ignoreAspectQNames);
    }

    /**
     * TODO only override core method when will be made protected in core(REPO-1459).
     *
     * @param nodeRef
     * @param type
     * @return
     */
    private boolean isSpecialNode(NodeRef nodeRef, QName type)
    {
        // Check for File Plan, Transfers Container, Holds Container, Unfiled Records Container
        NodeRef filePlan = filePlanService.getFilePlanBySiteId(FilePlanService.DEFAULT_RM_SITE_ID);
        if(filePlan != null)
        {
            if(filePlan.equals(nodeRef))
            {
                return true;
            }
            else if(filePlanService.getTransferContainer(filePlan).equals(nodeRef))
            {
                return true;
            }
            else if(filePlanService.getHoldContainer(filePlan).equals(nodeRef))
            {
                return true;
            }
            else if(filePlanService.getUnfiledContainer(filePlan).equals(nodeRef))
            {
                return true;
            }
        }
        //TODO just run super after method after it will be made protected on core(REPO-1459)
        return isCoreSpecialNode(nodeRef, type);
    }

    /**
     * Copied from core implementation, because it is protected and we can't extend it with our special nodes.
     *
     * TODO to remove when isSpecialNode method will be made protected in core (REPO-1459).
     *
     * @param nodeRef
     * @param type
     * @return
     */
    private boolean isCoreSpecialNode(NodeRef nodeRef, QName type)
    {
        // Check for Company Home, Sites and Data Dictionary (note: must be tenant-aware)
        if (nodeRef.equals(repositoryHelper.getCompanyHome()))
        {
            return true;
        }
        else if (type.equals(SiteModel.TYPE_SITES) || type.equals(SiteModel.TYPE_SITE))
        {
            // note: alternatively, we could inject SiteServiceInternal and use getSitesRoot (or indirectly via node locator)
            return true;
        }
        else
        {
            String tenantDomain = TenantUtil.getCurrentDomain();
            NodeRef ddNodeRef = ddCache.get(tenantDomain);
            if (ddNodeRef == null)
            {
                List<ChildAssociationRef> ddAssocs = nodeService.getChildAssocs(
                        repositoryHelper.getCompanyHome(),
                        ContentModel.ASSOC_CONTAINS,
                        QName.createQName(NamespaceService.APP_MODEL_1_0_URI, "dictionary"));
                if (ddAssocs.size() == 1)
                {
                    ddNodeRef = ddAssocs.get(0).getChildRef();
                    ddCache.put(tenantDomain, ddNodeRef);
                }
            }

            if (nodeRef.equals(ddNodeRef))
            {
                return true;
            }
        }

        return false;
    }

    /**
     * Overridden this method just in order to use our isSpecialNode method since core method could not be overridden.
     *
     * TODO remove this after isSpecialNode will be made protected in core(REPO-1459).
     */
    @Override
    public void deleteNode(String nodeId, Parameters parameters)
    {
        NodeRef nodeRef = validateOrLookupNode(nodeId, null);
        QName nodeType = nodeService.getType(nodeRef);

        if (isSpecialNode(nodeRef, nodeType))
        {
            throw new PermissionDeniedException("Cannot delete: " + nodeId);
        }
        super.deleteNode(nodeId, parameters);
    }

    @Override
    public boolean isRMSite(String nodeId)
    {
        NodeRef nodeRef = validateOrLookupNode(nodeId, null);

        SiteInfo siteInfo = siteService.getSite(FilePlanService.DEFAULT_RM_SITE_ID);
        if(siteInfo !=null)
        {
            NodeRef rmNodeRef = siteInfo.getNodeRef();
            if(rmNodeRef.equals(nodeRef))
            {
                return true;
            }
        }
        return false;
    }
}
