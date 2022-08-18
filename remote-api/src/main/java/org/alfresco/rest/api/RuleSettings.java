/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
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
 *  GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
package org.alfresco.rest.api;

import org.alfresco.rest.api.model.rules.RuleSetting;
import org.alfresco.service.Experimental;

/**
 * Rule settings API.
 */
@Experimental
public interface RuleSettings
{
    /**
     * Get the rule setting with the given key.
     *
     * @param folderId Folder node ID
     * @param ruleSettingKey Rule setting key
     * @return {@link RuleSetting} The retrieved rule setting object.
     */
    RuleSetting getRuleSetting(String folderId, String ruleSettingKey);

    /**
     * Set the rule setting against the specified folder.
     *
     * @param folderId The folder to update.
     * @param ruleSetting The new rule setting.
     * @return The updated rule setting object.
     */
    RuleSetting setRuleSetting(String folderId, RuleSetting ruleSetting);
}
