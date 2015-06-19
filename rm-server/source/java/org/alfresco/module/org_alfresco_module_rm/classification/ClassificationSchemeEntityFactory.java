/*
 * Copyright (C) 2005-2015 Alfresco Software Limited.
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
package org.alfresco.module.org_alfresco_module_rm.classification;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Factory to create classification entities from JSON objects.
 *
 * @author tpage
 * @since 3.0
 */
public class ClassificationSchemeEntityFactory
{
    /**
     * Create the classification entity from the supplied JSON.
     *
     * @param clazz The class to create.
     * @param jsonObject The JSON object from the configuration file.
     * @return The new entity.
     * @throws JSONException If there is an error in the JSON.
     */
    @SuppressWarnings("unchecked")
    public <T extends ClassificationSchemeEntity> T create(Class<T> clazz, JSONObject jsonObject) throws JSONException
    {
        if (clazz == ClassificationLevel.class)
        {
            String id = jsonObject.getString("name");
            String displayLabelKey = jsonObject.getString("displayLabel");
            return (T) new ClassificationLevel(id, displayLabelKey);
        }
        else if (clazz == ClassificationReason.class)
        {
            String id = jsonObject.getString("id");
            String displayLabelKey = jsonObject.getString("displayLabel");
            return (T) new ClassificationReason(id, displayLabelKey);
        }
        else if (clazz == ExemptionCategory.class)
        {
            String id = jsonObject.getString("id");
            String displayLabelKey = jsonObject.getString("displayLabel");
            return (T) new ExemptionCategory(id, displayLabelKey);
        }
        throw new IllegalStateException("Unsupported entity type: " + clazz);
    }
}
