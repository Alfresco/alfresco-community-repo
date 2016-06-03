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
import org.alfresco.rest.api.model.AssocTarget;
import org.alfresco.rest.api.model.Node;
import org.alfresco.rest.api.model.UserInfo;
import org.alfresco.rest.framework.WebApiDescription;
import org.alfresco.rest.framework.core.exceptions.ConstraintViolatedException;
import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.alfresco.rest.framework.resource.RelationshipResource;
import org.alfresco.rest.framework.resource.actions.interfaces.RelationshipResourceAction;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Paging;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.rest.framework.resource.parameters.where.Query;
import org.alfresco.rest.framework.resource.parameters.where.QueryHelper;
import org.alfresco.rest.workflow.api.impl.MapBasedQueryWalker;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.AssociationExistsException;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
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
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author janv
 */
public class AbstractNodeRelation implements InitializingBean
{
    public final static String PARAM_ASSOC_TYPE = "assocType";

    private final static Set<String> WHERE_PARAMS =
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

    @Override
    public void afterPropertiesSet()
    {
        PropertyCheck.mandatory(this, "serviceRegistry", sr);
        ParameterCheck.mandatory("nodes", this.nodes);

        this.nodeService = sr.getNodeService();
        this.namespaceService = sr.getNamespaceService();
        this.dictionaryService = sr.getDictionaryService();
    }

    protected QName getAssocType(String prefixAssocTypeStr)
    {
        return getAssocType(prefixAssocTypeStr, true, true);
    }

    protected QName getAssocType(String prefixAssocTypeStr, boolean mandatory, boolean validate)
    {
        QName assocType = null;

        if ((prefixAssocTypeStr != null) && (! prefixAssocTypeStr.isEmpty()))
        {
            assocType = QName.createQName(prefixAssocTypeStr, namespaceService);

            if (validate)
            {
                if (dictionaryService.getAssociation(assocType) == null)
                {
                    throw new InvalidArgumentException("Unknown filter assocType: "+prefixAssocTypeStr);
                }

            }
        }
        else if (mandatory)
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
            MapBasedQueryWalker propertyWalker = new MapBasedQueryWalker(WHERE_PARAMS, null);
            QueryHelper.walk(q, propertyWalker);

            String assocTypeQNameStr = propertyWalker.getProperty(PARAM_ASSOC_TYPE, WhereClauseParser.EQUALS, String.class);
            if (assocTypeQNameStr != null)
            {
                assocTypeQNamePattern = getAssocType(assocTypeQNameStr);
            }
        }

        return assocTypeQNamePattern;
    }
}
