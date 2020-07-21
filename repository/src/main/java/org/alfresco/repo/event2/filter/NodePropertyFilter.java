/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2020 Alfresco Software Limited
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
package org.alfresco.repo.event2.filter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.service.namespace.QName;

/**
 * Implementation of the node properties filter.
 *
 * @author Jamal Kaabi-Mofrad
 */
public class NodePropertyFilter extends AbstractNodeEventFilter
{
    private static final String FILTERED_PROPERTIES = "sys:*";
    // These properties are included as top-level info,
    // so exclude them from the properties object
    private static final Set<QName> EXCLUDED_TOP_LEVEL_PROPS = Set.of(ContentModel.PROP_NAME,
                                                                      ContentModel.PROP_MODIFIER,
                                                                      ContentModel.PROP_MODIFIED,
                                                                      ContentModel.PROP_CREATOR,
                                                                      ContentModel.PROP_CREATED,
                                                                      ContentModel.PROP_CONTENT);

    private final List<String> nodeAspectsBlackList;

    public NodePropertyFilter()
    {
        this.nodeAspectsBlackList = parseFilterList(FILTERED_PROPERTIES);
    }

    @Override
    public Set<QName> getExcludedTypes()
    {
        Set<QName> result = new HashSet<>(EXCLUDED_TOP_LEVEL_PROPS);
        nodeAspectsBlackList.forEach(nodeAspect -> result.addAll(expandTypeDef(nodeAspect)));
        return result;
    }
}
