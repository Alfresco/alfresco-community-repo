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

package org.alfresco.rest.api.impl;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanService;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.repo.model.Repository;
import org.alfresco.rest.api.model.Node;
import org.alfresco.rest.api.model.PathInfo;
import org.alfresco.rest.api.model.RMNode;
import org.alfresco.rest.api.model.UserInfo;
import org.alfresco.rest.framework.core.exceptions.EntityNotFoundException;
import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;

/**
 * Centralizes access to the repository. 
 *
 * @author Ana Bozianu
 * @since 2.6
 */
public class RMNodesImpl extends NodesImpl
{
    String FILE_PLAN = "-filePlan-";
    String TRANSFERS = "-transfers-";
    String UNFILED = "-unfiled-";
    String HOLDS = "-holds-";

    private enum RMNodeType
    {
        // Note: ordered
        CATEGORY, RECORD_FOLDER, FILE
    }
    
    private FilePlanService filePlanService;
    private NodeService nodeService;
    private ServiceRegistry sr;
    private Repository repositoryHelper;
    private DictionaryService dictionaryService;
    private NamespaceService namespaceService;

    public void init()
    {
        super.init();
        this.nodeService = sr.getNodeService();
        this.dictionaryService = sr.getDictionaryService();
        this.namespaceService = sr.getNamespaceService();
    }

    public void setServiceRegistry(ServiceRegistry sr)
    {
        super.setServiceRegistry(sr);
        this.sr = sr;
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
        Map<QName, Serializable> properties = nodeService.getProperties(nodeRef);

        // Get general information
        if (nodeTypeQName == null)
        {
            nodeTypeQName = nodeService.getType(nodeRef);
        }

        if (parentNodeRef == null)
        {
            parentNodeRef = getParentNodeRef(nodeRef);
        }

        RMNodeType type = getType(nodeTypeQName, nodeRef);
        RMNode node;
        node = new RMNode(nodeRef, parentNodeRef, properties, mapUserInfo, sr);

        if (type == RMNodeType.CATEGORY)
        {
            node.setIsCategory(true);
        }
        else if (type == RMNodeType.RECORD_FOLDER)
        {
            node.setIsRecordFolder(true);
        }
        else if (type == RMNodeType.FILE)
        {
            node.setIsFile(true);
        }

        PathInfo pathInfo = null;
        if (includeParam.contains(PARAM_INCLUDE_PATH))
        {
            ChildAssociationRef archivedParentAssoc = (ChildAssociationRef) properties.get(ContentModel.PROP_ARCHIVED_ORIGINAL_PARENT_ASSOC);
            pathInfo = lookupPathInfo(nodeRef, archivedParentAssoc);
        }
        node.setPath(pathInfo);
        node.setNodeType(nodeTypeQName.toPrefixString(namespaceService));

        // Get optional fields
        if (includeParam.size() > 0)
        {
            node.setProperties(mapFromNodeProperties(properties, includeParam, mapUserInfo));
        }

        if (includeParam.contains(PARAM_INCLUDE_ASPECTNAMES))
        {
            node.setAspectNames(mapFromNodeAspects(nodeService.getAspects(nodeRef)));
        }

        return node;
    }

    @Override
    public NodeRef validateOrLookupNode(String nodeId, String path)
    {
        if ((nodeId == null) || (nodeId.isEmpty()))
        {
            throw new InvalidArgumentException("Missing nodeId");
        }

        if (nodeId.equals(FILE_PLAN))
        {
            NodeRef filePlan = filePlanService.getFilePlanBySiteId(FilePlanService.DEFAULT_RM_SITE_ID);
            if(filePlan != null)
            {
                return filePlan;
            }
            else
            {
                throw new EntityNotFoundException(nodeId);
            }
        }
        else if (nodeId.equals(TRANSFERS))
        {
            NodeRef filePlan = filePlanService.getFilePlanBySiteId(FilePlanService.DEFAULT_RM_SITE_ID);
            if(filePlan != null)
            {
                return filePlanService.getTransferContainer(filePlan);
            }
            else
            {
                throw new EntityNotFoundException(nodeId);
            }
        }
        else if (nodeId.equals(UNFILED))
        {
            NodeRef filePlan = filePlanService.getFilePlanBySiteId(FilePlanService.DEFAULT_RM_SITE_ID);
            if(filePlan != null)
            {
                return filePlanService.getUnfiledContainer(filePlan);
            }
            else
            {
                throw new EntityNotFoundException(nodeId);
            }
        }
        else if (nodeId.equals(HOLDS))
        {
            NodeRef filePlan = filePlanService.getFilePlanBySiteId(FilePlanService.DEFAULT_RM_SITE_ID);
            if(filePlan != null)
            {
                return filePlanService.getHoldContainer(filePlan);
            }
            else
            {
                throw new EntityNotFoundException(nodeId);
            }
        }

        return super.validateOrLookupNode(nodeId, path);
    }

    private NodeRef getParentNodeRef(NodeRef nodeRef)
    {
        if (repositoryHelper.getCompanyHome().equals(nodeRef))
        {
            return null; // note: does not make sense to return parent above C/H
        }

        return nodeService.getPrimaryParent(nodeRef).getParentRef();
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
        else if (typeQName.equals(ContentModel.TYPE_CONTENT) || dictionaryService.isSubClass(typeQName, ContentModel.TYPE_CONTENT))
        {
            return RMNodeType.FILE;
        }

        return null; // unknown
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

        return new Pair<>(searchTypeQNames, ignoreAspectQNames);
    }
}
