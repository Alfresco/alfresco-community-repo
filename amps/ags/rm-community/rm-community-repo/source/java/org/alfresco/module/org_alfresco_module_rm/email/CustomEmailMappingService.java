/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
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
     * @return  {@link Set}&lt;{@link CustomMapping}&gt;
     */
    Set<CustomMapping> getCustomMappings();

    /**
     * Add custom mapping
     *
     * @param from
     * @param to
     */
    void addCustomMapping(String from, String to);

    /**
     * Delete custom mapping
     *
     * @param from
     * @param to
     */
    void deleteCustomMapping(String from, String to);

    /**
     * Gets the list of email mapping keys
     *
     * @return Email mapping keys
     */
    List<String> getEmailMappingKeys();

    /**
     * Registers an email mapping key with the existing list of email mapping keys
     *
     * @param emailMappingKey  emailMappingKey to register
     */
    void registerEMailMappingKey(String emailMappingKey);
}
