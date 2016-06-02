/*
 * Copyright (C) 2005-2016 Alfresco Software Limited.
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
package org.alfresco.rest.api.nodes;

import org.alfresco.rest.antlr.WhereClauseParser;
import org.alfresco.rest.api.Nodes;
import org.alfresco.rest.api.model.Assoc;
import org.alfresco.rest.api.model.AssocChild;
import org.alfresco.rest.api.model.Node;
import org.alfresco.rest.api.model.UserInfo;
import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Paging;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.rest.framework.resource.parameters.where.Query;
import org.alfresco.rest.framework.resource.parameters.where.QueryHelper;
import org.alfresco.rest.workflow.api.impl.MapBasedQueryWalker;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.QNamePattern;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.util.ParameterCheck;
import org.alfresco.util.PropertyCheck;
import org.springframework.beans.factory.InitializingBean;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author janv
 */
public class AbstractNodeRelation implements InitializingBean
{
    public final static String PARAM_ASSOC_TYPE = "assocType";

    // excluded namespaces (assoc types)
    protected static final List<String> EXCLUDED_NS = Arrays.asList(NamespaceService.SYSTEM_MODEL_1_0_URI);

    private final static Set<String> WHERE_PARAMS_ASSOC_TYPE =
            new HashSet<>(Arrays.asList(new String[] {PARAM_ASSOC_TYPE}));

    protected ServiceRegistry sr;
    protected NodeService nodeService;
    protected NamespaceService namespaceService;
    protected DictionaryService dictionaryService;
    protected Nodes nodes;

    public void setNodes(Nodes nodes)
    {
        this.nodes = nodes;
    }

    public void setServiceRegistry(ServiceRegistry sr)
    {
        this.sr = sr;
    }

    // Introduces permissions for Node Assoc (see public-rest-context.xml)
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    @Override
    public void afterPropertiesSet()
    {
        PropertyCheck.mandatory(this, "serviceRegistry", sr);
        ParameterCheck.mandatory("nodes", this.nodes);

        //this.nodeService = sr.getNodeService();
        this.namespaceService = sr.getNamespaceService();
        this.dictionaryService = sr.getDictionaryService();
    }

    protected QName getAssocType(String assocTypeQNameStr)
    {
        return getAssocType(assocTypeQNameStr, true);
    }

    protected QName getAssocType(String assocTypeQNameStr, boolean mandatory)
    {
        QName assocType = null;

        if ((assocTypeQNameStr != null) && (! assocTypeQNameStr.isEmpty()))
        {
            assocType = nodes.createQName(assocTypeQNameStr);
            if (dictionaryService.getAssociation(assocType) == null)
            {
                throw new InvalidArgumentException("Unknown assocType: " + assocTypeQNameStr);
            }

            if (EXCLUDED_NS.contains(assocType.getNamespaceURI()))
            {
                throw new InvalidArgumentException("Invalid assocType: " + assocTypeQNameStr);
            }
        }

        if (mandatory && (assocType == null))
        {
            throw new InvalidArgumentException("Missing "+PARAM_ASSOC_TYPE);
        }

        return assocType;
    }

    protected QNamePattern getAssocTypeFromWhereElseAll(Parameters parameters)
    {
        QNamePattern assocTypeQNamePattern = RegexQNamePattern.MATCH_ALL;

        Query q = parameters.getQuery();
        if (q != null)
        {
            MapBasedQueryWalker propertyWalker = new MapBasedQueryWalker(WHERE_PARAMS_ASSOC_TYPE, null);
            QueryHelper.walk(q, propertyWalker);

            String assocTypeQNameStr = propertyWalker.getProperty(PARAM_ASSOC_TYPE, WhereClauseParser.EQUALS, String.class);
            if (assocTypeQNameStr != null)
            {
                assocTypeQNamePattern = getAssocType(assocTypeQNameStr);
            }
        }

        return assocTypeQNamePattern;
    }

    protected CollectionWithPagingInfo<Node> listNodePeerAssocs(List<AssociationRef> assocRefs, Parameters parameters, boolean returnTarget)
    {
        Map<QName, String> qnameMap = new HashMap<>(3);

        Map<String, UserInfo> mapUserInfo = new HashMap<>(10);

        List<String> includeParam = parameters.getInclude();

        List<Node> collection = new ArrayList<Node>(assocRefs.size());
        for (AssociationRef assocRef : assocRefs)
        {
            // minimal info by default (unless "include"d otherwise)
            NodeRef nodeRef = (returnTarget ? assocRef.getTargetRef() : assocRef.getSourceRef());

            Node node = nodes.getFolderOrDocument(nodeRef, null, null, includeParam, mapUserInfo);

            QName assocTypeQName = assocRef.getTypeQName();

            if (! EXCLUDED_NS.contains(assocTypeQName.getNamespaceURI()))
            {
                String assocType = qnameMap.get(assocTypeQName);
                if (assocType == null)
                {
                    assocType = assocTypeQName.toPrefixString(namespaceService);
                    qnameMap.put(assocTypeQName, assocType);
                }

                node.setAssociation(new Assoc(assocType));

                collection.add(node);
            }
        }

        Paging paging = parameters.getPaging();
        return CollectionWithPagingInfo.asPaged(paging, collection, false, collection.size());
    }

    protected CollectionWithPagingInfo<Node> listNodeChildAssocs(List<ChildAssociationRef> childAssocRefs, Parameters parameters, Boolean isPrimary, boolean returnChild)
    {
        Map<QName, String> qnameMap = new HashMap<>(3);

        Map<String, UserInfo> mapUserInfo = new HashMap<>(10);

        List<String> includeParam = parameters.getInclude();

        List<Node> result = new ArrayList<Node>(childAssocRefs.size());
        for (ChildAssociationRef childAssocRef : childAssocRefs)
        {
            if (isPrimary == null || (isPrimary == childAssocRef.isPrimary()))
            {
                // minimal info by default (unless "include"d otherwise)
                NodeRef nodeRef = (returnChild ? childAssocRef.getChildRef() : childAssocRef.getParentRef());

                Node node = nodes.getFolderOrDocument(nodeRef, null, null, includeParam, mapUserInfo);

                QName assocTypeQName = childAssocRef.getTypeQName();

                if (!EXCLUDED_NS.contains(assocTypeQName.getNamespaceURI()))
                {
                    String assocType = qnameMap.get(assocTypeQName);
                    if (assocType == null)
                    {
                        assocType = assocTypeQName.toPrefixString(namespaceService);
                        qnameMap.put(assocTypeQName, assocType);
                    }

                    node.setAssociation(new AssocChild(assocType, childAssocRef.isPrimary()));


                    result.add(node);
                }
            }
        }

        Paging paging = parameters.getPaging();

        // return 'page' of results (note: depends on in-built/natural sort order of results)
        int skipCount = paging.getSkipCount();
        int pageSize = paging.getMaxItems();
        int pageEnd = skipCount + pageSize;

        final List<Node> page = new ArrayList<>(pageSize);
        Iterator<Node> it = result.iterator();
        for (int counter = 0; counter < pageEnd && it.hasNext(); counter++)
        {
            Node element = it.next();
            if (counter < skipCount)
            {
                continue;
            }
            if (counter > pageEnd - 1)
            {
                break;
            }
            page.add(element);
        }

        int totalCount = result.size();
        boolean hasMoreItems = ((skipCount + page.size()) < totalCount);

        return CollectionWithPagingInfo.asPaged(paging, page, hasMoreItems, totalCount);
    }
}
