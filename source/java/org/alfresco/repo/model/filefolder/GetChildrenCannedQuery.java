/*
 * #%L
 * Alfresco Repository
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
package org.alfresco.repo.model.filefolder;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.query.CannedQueryParameters;
import org.alfresco.repo.domain.node.NodeDAO;
import org.alfresco.repo.domain.node.NodePropertyHelper;
import org.alfresco.repo.domain.qname.QNameDAO;
import org.alfresco.repo.domain.query.CannedQueryDAO;
import org.alfresco.repo.model.filefolder.HiddenAspect.Visibility;
import org.alfresco.repo.node.getchildren.FilterProp;
import org.alfresco.repo.node.getchildren.GetChildrenCannedQueryParams;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.repo.security.permissions.impl.acegi.MethodSecurityBean;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.FileFilterMode;
import org.alfresco.util.FileFilterMode.Client;

/**
 * GetChidren canned query for files and folders.
 * 
 * To get paged list of files and folders of a parent folder filtered by child type.
 * Also optionally filtered and/or sorted by one or more properties (up to three).
 * 
 * This is the same as the nodes getchildren canned query, except it takes into account hidden files and folders (with respect to client visibility)
 * and other aspect(s) to ignore - eg. optionally "cm:checkedOut" in case of Share DocLib.
 * 
 * @since 4.1.1
 * @author steveglover, janv
 *
 */
public class GetChildrenCannedQuery extends org.alfresco.repo.node.getchildren.GetChildrenCannedQuery
{
    private HiddenAspect hiddenAspect;
    private DictionaryService dictionaryService;
    private NodeService nodeService;
    private Set<QName> ignoreAspectQNames;
    
    public GetChildrenCannedQuery(
            NodeDAO nodeDAO,
            QNameDAO qnameDAO,
            CannedQueryDAO cannedQueryDAO,
            NodePropertyHelper nodePropertyHelper,
            TenantService tenantService,
            NodeService nodeService,
            MethodSecurityBean<NodeRef> methodSecurity,
            CannedQueryParameters params,
            HiddenAspect hiddenAspect,
            DictionaryService dictionaryService,
            Set<QName> ignoreAspectQNames)
    {
        super(nodeDAO, qnameDAO, cannedQueryDAO, nodePropertyHelper, tenantService, nodeService, methodSecurity, params);
        
        this.hiddenAspect = hiddenAspect;
        this.dictionaryService = dictionaryService;
        
        this.nodeService = nodeService;
        this.ignoreAspectQNames = ignoreAspectQNames;
    }
    
    @Override
    protected UnsortedChildQueryCallback getUnsortedChildQueryCallback(final List<NodeRef> rawResult, final int requestedCount, GetChildrenCannedQueryParams paramBean)
    {
        Set<QName> inclusiveAspects = paramBean.getInclusiveAspects();
        Set<QName> exclusiveAspects = paramBean.getExclusiveAspects();
        
        UnsortedChildQueryCallback callback = new FileFolderUnsortedChildQueryCallback(rawResult, requestedCount, inclusiveAspects, exclusiveAspects);
        return callback;
    }
    
    @Override
    protected FilterSortChildQueryCallback getFilterSortChildQuery(final List<FilterSortNode> children, final List<FilterProp> filterProps, GetChildrenCannedQueryParams paramBean)
    {
        FilterSortChildQueryCallback callback = new FileFolderFilterSortChildQueryCallback(children, filterProps);
        return callback;
    }

    private class FileFolderFilterSortChildQueryCallback extends DefaultFilterSortChildQueryCallback
    {
        private Map<QName, Boolean> isTypeFolderMap = new HashMap<QName, Boolean>(10);
        
        public FileFolderFilterSortChildQueryCallback(List<FilterSortNode> children, List<FilterProp> filterProps)
        {
            super(children, filterProps);
        }
        
        @Override
        protected boolean include(FilterSortNode node)
        {
            boolean ret = super.include(node);
            return ret && includeImpl(ret, node.getNodeRef());
        }
        
        @Override
        public boolean handle(FilterSortNode node)
        {
            super.handle(node);
            
            Map<QName, Serializable> propVals = node.getPropVals();
            QName nodeTypeQName = (QName)propVals.get(GetChildrenCannedQuery.SORT_QNAME_NODE_TYPE);
            
            if (nodeTypeQName != null)
            {
                // ALF-13968
                Boolean isFolder = isTypeFolderMap.get(nodeTypeQName);
                if (isFolder == null)
                {
                    isFolder = dictionaryService.isSubClass(nodeTypeQName, ContentModel.TYPE_FOLDER);
                    isTypeFolderMap.put(nodeTypeQName, isFolder);
                }
                
                propVals.put(GetChildrenCannedQuery.SORT_QNAME_NODE_IS_FOLDER, isFolder);
            }
            
            return true;
        }
    }

    private class FileFolderUnsortedChildQueryCallback extends DefaultUnsortedChildQueryCallback
    {
        public FileFolderUnsortedChildQueryCallback(List<NodeRef> rawResult,int requestedCount, Set<QName> inclusiveAspects, Set<QName> exclusiveAspects)
        {
            super(rawResult, requestedCount, inclusiveAspects, exclusiveAspects);
        }
        
        @Override
        protected boolean include(NodeRef nodeRef)
        {
            boolean ret = super.include(nodeRef);
            
            return ret && includeImpl(ret, nodeRef);
        }
    }
    
    protected boolean includeImpl(boolean ret, NodeRef nodeRef)
    {
        // only visible files are returned, relative to the client type.
        try
        {
            if (!nodeService.exists(nodeRef))
            {
                // Node has disappeared
                return ret;
            }
            
            final Client client = FileFilterMode.getClient();
            boolean notHidden = hiddenAspect.getVisibility(client, nodeRef) != Visibility.NotVisible;
            
            boolean notIgnore = true;
            if (ignoreAspectQNames != null)
            {
                if (ignoreAspectQNames.size() > 1)
                {
                    Set<QName> nodeAspects = nodeService.getAspects(nodeRef);
                    notIgnore = (! nodeAspects.removeAll(ignoreAspectQNames));
                }
                else if (ignoreAspectQNames.size() == 1)
                {
                    if (nodeService.hasAspect(nodeRef, ignoreAspectQNames.iterator().next()))
                    {
                        notIgnore = false;
                    }
                }
            }
            
            return ret && notHidden && notIgnore;
        }
        catch (AccessDeniedException e)
        {
            // user may not have permission to determine the visibility of the node
            return ret;
        }
    }
}
