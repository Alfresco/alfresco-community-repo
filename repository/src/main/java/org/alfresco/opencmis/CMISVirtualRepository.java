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
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

import java.util.List;
import java.util.Set;

/**
 * Responsible for limiting the CMIS access to a subset of the repository. By applying such limitations it allows to
 * create multiple CMIS repositories on a single Alfresco repository. It's just an extension point for the CMIS classes,
 * and it's up to the implementation to decide how to apply such limitations.
 * @see CMISConnector
 * @see AlfrescoCmisServiceImpl
 */
public interface CMISVirtualRepository {
    static CMISVirtualRepository noVirtualRepository() {
        return new NoVirtualRepository();
    }

    /**
     * Checks if given node is part of the repository.
     * @param nodeRef node's reference
     * @return {@code true} if given node belongs to the repository, {@code false} otherwise.
     */
    boolean contains(NodeRef nodeRef);

    /**
     * Allows to apply children listing filter.
     * @return {@link List} of {@link FilterProp}
     * @see org.alfresco.service.cmr.model.FileFolderService
     */
    List<FilterProp> getChildrenFilteringProperties();

    /**
     * Allows to apply CMIS query filter.
     * @see org.alfresco.opencmis.search.CMISQueryOptions.CMISQueryFilter
     */
    void applyQueryFiltering(CMISQueryOptions options);

    /**
     * Allows to indicate that some aspects are required by the implementation of the virtual repository.
     * These aspects might be applied outside the CMIS flow and will be retained.
     * @return Set of required aspects
     */
    Set<QName> getRequiredAspects();
}

/**
 * NOOP filter to avoid {@code null} checks.
 */
class NoVirtualRepository implements CMISVirtualRepository
{
    @Override
    public boolean contains(NodeRef nodeRef) {
        return true;
    }

    @Override
    public List<FilterProp> getChildrenFilteringProperties() {
        return List.of();
    }

    @Override
    public void applyQueryFiltering(CMISQueryOptions options) {
        //do nothing
    }

    @Override
    public Set<QName> getRequiredAspects() {
        return Set.of();
    }
}
