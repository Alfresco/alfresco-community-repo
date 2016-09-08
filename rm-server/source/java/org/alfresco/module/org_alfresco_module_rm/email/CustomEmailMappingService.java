/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
package org.alfresco.module.org_alfresco_module_rm.email;

import java.util.List;
import java.util.Set;

/**
 * Custom EMail Mapping Service
 */
public interface CustomEmailMappingService
{
    /**
     * Get the list of custom mappings
     *
     * @return  {@link Set}<{@link CustomMapping}>
     */
    public Set<CustomMapping> getCustomMappings();

    /**
     * Add custom mapping
     *
     * @param from
     * @param to
     */
    public void addCustomMapping(String from, String to);

    /**
     * Delete custom mapping
     *
     * @param from
     * @param to
     */
    public void deleteCustomMapping(String from, String to);

    /**
     * Gets the list of email mapping keys
     *
     * @return Email mapping keys
     */
    public List<String> getEmailMappingKeys();

    /**
     * Registers an email mapping key with the existing list of email mapping keys
     *
     * @param emailMappingKey  emailMappingKey to register
     */
    public void registerEMailMappingKey(String emailMappingKey);
}
