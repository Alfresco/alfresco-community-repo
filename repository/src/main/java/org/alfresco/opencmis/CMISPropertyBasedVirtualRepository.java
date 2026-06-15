/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2026 Alfresco Software Limited
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
package org.alfresco.opencmis;

import org.alfresco.opencmis.search.CMISQueryOptions;
import org.alfresco.repo.node.getchildren.FilterProp;
import org.alfresco.repo.node.getchildren.FilterPropString;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.QName;

import java.util.List;
import java.util.Objects;

/**
 * A CMIS Virtual repository which limits the access to the nodes based on a single property value. Only nodes
 * (including root) with the given value are accessible through the CMIS API. This class is not responsible for setting
 * the value of this property.
 */
public class CMISPropertyBasedVirtualRepository implements CMISVirtualRepository {
    private final NodeService nodeService;
    private final NamespacePrefixResolver namespacePrefixResolver;
    private final QName propertyName;
    private final String propertyValue;

    public CMISPropertyBasedVirtualRepository(NodeService nodeService, NamespacePrefixResolver namespacePrefixResolver, String propertyName, String propertyValue)
    {
        this(nodeService, namespacePrefixResolver, getPropertyQName(namespacePrefixResolver, propertyName), propertyValue);
    }

    public CMISPropertyBasedVirtualRepository(NodeService nodeService, NamespacePrefixResolver namespacePrefixResolver, QName propertyName, String propertyValue)
    {
        this.nodeService = Objects.requireNonNull(nodeService);
        this.namespacePrefixResolver = Objects.requireNonNull(namespacePrefixResolver);
        this.propertyName = Objects.requireNonNull(propertyName);
        this.propertyValue = Objects.requireNonNull(propertyValue);
    }

    @Override
    public boolean contains(NodeRef nodeRef) {
        return propertyValue.equals(nodeService.getProperty(nodeRef, propertyName));
    }

    @Override
    public List<FilterProp> getChildrenFilteringProperties() {
        return List.of(new FilterPropString(propertyName, propertyValue, FilterPropString.FilterTypeString.EQUALS));
    }

    @Override
    public void applyQueryFiltering(CMISQueryOptions options) {
        options.setQueryFilter(CMISQueryOptions.CMISQueryFilter.propertyEquality(propertyName.toPrefixString(namespacePrefixResolver), propertyValue));
    }

    private static QName getPropertyQName(NamespacePrefixResolver namespacePrefixResolver, String propertyName) {
        return QName.createQName(propertyName, namespacePrefixResolver);
    }
}
