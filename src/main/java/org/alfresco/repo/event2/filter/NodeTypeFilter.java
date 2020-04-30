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

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.service.namespace.QName;

/**
 * Implementation of the node types filter.
 *
 * @author Jamal Kaabi-Mofrad
 */
public class NodeTypeFilter extends AbstractNodeEventFilter
{
    private final List<String> nodeTypesBlackList;

    public NodeTypeFilter(String filteredNodeTypes)
    {
        this.nodeTypesBlackList = parseFilterList(filteredNodeTypes);
    }

    @Override
    public Set<QName> getExcludedTypes()
    {
        // include all system folder types to be filtered out
        Set<QName> result = new HashSet<>(getSystemFolderTypes());

        // add node types defined in repository.properties/alfresco-global.properties
        nodeTypesBlackList.forEach(nodeType -> result.addAll(expandTypeDef(nodeType)));

        return result;
    }

    private Collection<QName> getSystemFolderTypes()
    {
        return dictionaryService.getSubTypes(ContentModel.TYPE_SYSTEM_FOLDER, true);
    }
}
