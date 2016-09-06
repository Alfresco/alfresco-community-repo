/*-
 * #%L
 * Alfresco Remote API
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

package org.alfresco.rest.api.search.impl;

import org.alfresco.model.ContentModel;
import org.alfresco.rest.api.Nodes;
import org.alfresco.rest.api.model.Node;
import org.alfresco.rest.api.model.UserInfo;
import org.alfresco.rest.api.search.model.SearchEntry;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Paging;
import org.alfresco.rest.framework.resource.parameters.Params;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ParameterCheck;
import org.apache.commons.lang.NotImplementedException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.alfresco.rest.api.Nodes.PARAM_INCLUDE_ASPECTNAMES;
import static org.alfresco.rest.api.Nodes.PARAM_INCLUDE_PROPERTIES;

/**
 * Maps from a Solr ResultSet to a json public api representation.
 *
 * @author Gethin James
 */
public class ResultMapper
{

    private Nodes nodes;

    public ResultMapper(Nodes nodes)
    {
        this.nodes = nodes;
        ParameterCheck.mandatory("nodes", this.nodes);
    }

    /**
     *
     * @param params
     * @param results
     * @return
     */
    public CollectionWithPagingInfo<Node> toCollectionWithPagingInfo(Params params, ResultSet results)
    {
        Long totalItems = results.getNumberFound();
        List<Node> noderesults = new ArrayList();

        results.forEach(row ->
        {
            Node aNode = nodes.getFolderOrDocument(row.getNodeRef(), null, null, params.getInclude(), null);
            float f = row.getScore();
            aNode.setSearch(new SearchEntry(f));
            noderesults.add(aNode);
        }
        );

        Integer total = Integer.valueOf(totalItems.intValue());
        return CollectionWithPagingInfo.asPaged(params.getPaging(), noderesults, noderesults.size() < total, total);
    }

}
