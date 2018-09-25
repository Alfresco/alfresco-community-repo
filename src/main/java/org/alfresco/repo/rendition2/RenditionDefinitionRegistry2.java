/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2018 Alfresco Software Limited
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
package org.alfresco.repo.rendition2;

import java.util.Set;

/**
 * A registry of rendition definitions.
 *
 * @author adavis
 */
public interface RenditionDefinitionRegistry2
{
    Set<String> getRenditionNames();

    /**
     * Obtains the names of renditions that are possible from a given source mimetype.
     * @param sourceMimetype the mimetype of the source.
     * @param size the size of the source. May be {@code -1} if the size should be ignored.
     */
    Set<String> getRenditionNamesFrom(String sourceMimetype, long size);

    RenditionDefinition2 getRenditionDefinition(String renditionName);
}
