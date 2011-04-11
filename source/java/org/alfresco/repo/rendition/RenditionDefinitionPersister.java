/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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

package org.alfresco.repo.rendition;

import java.util.List;

import org.alfresco.service.cmr.rendition.RenditionDefinition;
import org.alfresco.service.namespace.QName;

/**
 * This class provides the implementation of {@link RenditionDefinition} persistence.
 * <p/>
 * Note that rendition definitions are saved underneath the Data Dictionary and therefore any code
 * which loads or saves rendition definitions must have the appropriate authorisation.
 * 
 * @author Nick Smith
 * @author Neil McErlean
 * @since 3.3
 */
public interface RenditionDefinitionPersister
{
    /**
     * This method serializes the {@link RenditionDefinition} and stores it in
     * the repository. {@link RenditionDefinition}s saved in this way may be
     * retrieved using the <code>load()</code> method.
     * 
     * @param renditionDefinition The {@link RenditionDefinition} to be
     *            persisted.
     */
    void saveRenditionDefinition(RenditionDefinition renditionDefinition);

    /**
     * This method retrieves a {@link RenditionDefinition} that has been stored
     * in the repository using the <code>save()</code> method. If no
     * {@link RenditionDefinition} exists in the repository with the specified
     * rendition name then this method returns null.
     * 
     * @param renditionName The unique identifier used to specify the
     *            {@link RenditionDefinition} to retrieve.
     * @return The specified {@link RenditionDefinition} or null.
     */
    RenditionDefinition loadRenditionDefinition(QName renditionName);

    /**
     * This method retrieves the {@link RenditionDefinition}s that have been
     * stored in the repository using the <code>save()</code> method.
     * <P/>
     * If there are no such {@link RenditionDefinition}s, an empty list is
     * returned.
     * 
     * @return The {@link RenditionDefinition}s.
     */
    List<RenditionDefinition> loadRenditionDefinitions();

    /**
     * This method retrieves the stored {@link RenditionDefinition}s that have
     * been registered for the specified rendering engine name.
     * <P/>
     * If there are no such rendering {@link RenditionDefinition}s, an empty
     * list is returned.
     * 
     * @param renderingEngineName the name of a rendering engine. This is
     *            usually the spring bean name.
     * @return The {@link RenditionDefinition}s.
     * @throws NullPointerException if the renderingEngineName is null.
     * @see #saveRenditionDefinition(RenditionDefinition)
     */
    List<RenditionDefinition> loadRenditionDefinitions(String renderingEngineName);
}
