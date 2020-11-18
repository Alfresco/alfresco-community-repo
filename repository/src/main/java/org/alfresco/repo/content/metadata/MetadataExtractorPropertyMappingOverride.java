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
package org.alfresco.repo.content.metadata;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

import java.util.Map;
import java.util.Set;

/**
 * @deprecated as code running inside the content repository process that overrides metadata extract properties should
 * be moved out of process to reduce coupling of components, making upgrade simpler.
 *
 * @author adavis
 */
@Deprecated
public interface MetadataExtractorPropertyMappingOverride
{
    /**
     * Indicates if the {@link #getExtractMapping(NodeRef)} will provide extract properties
     * to override those in the T-Engine.
     *
     * @param sourceMimetype of the node.
     * @return {@code true} if there will be override extract properties.
     */
    boolean match(String sourceMimetype);

    /**
     * Returns the extract mapping to be passed to the T-Engine.
     *
     * @param nodeRef of the node having its metadata extracted.
     * @return the mapping of document properties to system properties
     */
    Map<String, Set<String>> getExtractMapping(NodeRef nodeRef);
}
