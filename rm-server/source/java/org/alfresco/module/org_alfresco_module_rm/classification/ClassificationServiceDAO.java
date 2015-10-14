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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.module.org_alfresco_module_rm.classification.ClassificationException.MalformedConfiguration;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 * This class is responsible for providing the configured classification scheme entities, dealing with JSON schema as
 * part of that.
 *
 * @author Neil Mc Erlean
 * @since 2.4.a
 */
class ClassificationServiceDAO
{
    /** A map from the simple name of a POJO type to the corresponding location of the configuration file. */
    private Map<String, String> configLocations = new HashMap<>();

    private ClassificationSchemeEntityFactory classificationSchemeEntityFactory = new ClassificationSchemeEntityFactory();

    /** Set the location of the reasons configuration file relative to the classpath. */
    public void setReasonConfigLocation(String reasonConfigLocation)
    {
        configLocations.put(ClassificationReason.class.getSimpleName(), reasonConfigLocation);
    }

    /** Set the location of the exemption categories configuration file relative to the classpath. */
    public void setExemptionCategoryConfigLocation(String exemptionCategoryConfigLocation)
    {
        configLocations.put(ExemptionCategory.class.getSimpleName(), exemptionCategoryConfigLocation);
    }

    /**
     * Gets the list of values as defined in the classpath.
     *
     * @return The configured values, or an empty list if there are none.
     */
    public <T extends ClassificationSchemeEntity> List<T> getConfiguredValues(Class<T> clazz)
    {
        List<T> result;
        try (final InputStream in = this.getClass().getResourceAsStream(configLocations.get(clazz.getSimpleName())))
        {
            if (in == null) { result = Collections.emptyList(); }
            else
            {
                final String jsonString = IOUtils.toString(in);
                final JSONArray jsonArray = new JSONArray(new JSONTokener(jsonString));

                result = new ArrayList<>(jsonArray.length());

                for (int i = 0; i < jsonArray.length(); i++)
                {
                    final JSONObject nextObj = jsonArray.getJSONObject(i);
                    result.add(classificationSchemeEntityFactory.create(clazz, nextObj));
                }
            }
        }
        catch (IOException | JSONException e)
        {
            String message = "Could not read " + clazz.getSimpleName() + " configuration: " + configLocations.get(clazz.getSimpleName());
            throw new MalformedConfiguration(message, e);
        }
        return result;
    }
}
