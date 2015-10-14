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

package org.alfresco.module.org_alfresco_module_rm.caveat.dao;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import org.alfresco.module.org_alfresco_module_rm.caveat.CaveatException.CaveatGroupNotFound;
import org.alfresco.module.org_alfresco_module_rm.caveat.CaveatException.MalformedConfiguration;
import org.alfresco.module.org_alfresco_module_rm.caveat.scheme.CaveatGroup;
import org.alfresco.module.org_alfresco_module_rm.caveat.scheme.CaveatGroupType;
import org.alfresco.module.org_alfresco_module_rm.caveat.scheme.CaveatMark;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An object that provides access to the configured caveat groups and marks, which it retrieves from JSON files.
 *
 * @author Tom Page
 * @since 2.4.a
 */
public class CaveatDAOFromJSON implements CaveatDAOInterface
{
    /** JSON key for the group id. */
    private static final String GROUP_ID_JSON_KEY = "id";
    /** JSON key for the group display label key. */
    private static final String GROUP_DISPLAY_LABEL_JSON_KEY = "displayLabel";
    /** JSON key for the group description key. */
    private static final String DESCRIPTION_JSON_KEY = "description";
    /** JSON key for the group type. */
    private static final String TYPE_JSON_KEY = "type";
    /** JSON key for the caveat marks array. */
    private static final String MARKS_JSON_KEY = "marks";
    /** JSON key for the mark id. */
    private static final String MARK_ID_JSON_KEY = "id";
    /** JSON key for the mark display label key. */
    private static final String MARK_DISPLAY_LABEL_JSON_KEY = "displayLabel";
    /** Logging utility for the class. */
    private static final Logger LOGGER = LoggerFactory.getLogger(CaveatDAOFromJSON.class);

    /** The location of the configuration file relative to the classpath. */
    private String configLocation;

    /** Set the location of the configuration file relative to the classpath. */
    public void setConfigLocation(String configLocation)
    {
        this.configLocation = configLocation;
    }

    /**
     * {@inheritDoc}
     *
     * @throws MalformedConfiguration If the configuration file cannot be interpreted.
     */
    @Override
    public ImmutableMap<String, CaveatGroup> getCaveatGroups()
    {
        Builder<String, CaveatGroup> builder = ImmutableMap.builder();
        try (final InputStream in = this.getClass().getResourceAsStream(configLocation))
        {
            if (in != null)
            {
                final String jsonString = IOUtils.toString(in);
                final JSONArray jsonArray = new JSONArray(new JSONTokener(jsonString));

                for (int i = 0; i < jsonArray.length(); i++)
                {
                    final JSONObject nextObj = jsonArray.getJSONObject(i);
                    CaveatGroup caveatGroup = createGroup(nextObj);
                    String caveatGroupId = caveatGroup.getId();
                    builder.put(caveatGroupId, caveatGroup);
                }
            }
            else
            {
                LOGGER.warn("Could not find caveat configuration file: " + configLocation);
            }
        }
        catch (IOException | JSONException | IllegalArgumentException e)
        {
            throw new MalformedConfiguration("Could not read caveat configuration: " + configLocation, e);
        }

        ImmutableMap<String, CaveatGroup> map;
        try
        {
            map = builder.build();
        }
        catch (IllegalArgumentException e)
        {
            throw new MalformedConfiguration("Configuration contains two caveat groups with the same id.", e);
        }
        return map;
    }

    /**
     * {@inheritDoc}
     * <p>
     * This method loads all caveat groups just to return a single group.
     */
    @Override
    public CaveatGroup getGroupById(String groupId)
    {
        CaveatGroup caveatGroup = getCaveatGroups().get(groupId);
        if (caveatGroup == null)
        {
            throw new CaveatGroupNotFound(groupId);
        }
        return caveatGroup;
    }

    /**
     * Create a caveat group from the supplied JSON.
     *
     * @param jsonGroup The JSON object corresponding to a single group and its marks.
     * @return The created group.
     * @throws JSONException If there is an issue reading the JSON.
     */
    protected CaveatGroup createGroup(JSONObject jsonGroup) throws JSONException
    {
        String id = jsonGroup.getString(GROUP_ID_JSON_KEY);
        String displayLabelKey = jsonGroup.getString(GROUP_DISPLAY_LABEL_JSON_KEY);
        String descriptionKey = jsonGroup.getString(DESCRIPTION_JSON_KEY);
        String caveatGroupTypeString = jsonGroup.getString(TYPE_JSON_KEY);
        CaveatGroupType caveatGroupType;
        try
        {
            caveatGroupType = CaveatGroupType.valueOf(caveatGroupTypeString);
        }
        catch (IllegalArgumentException e)
        {
            throw new MalformedConfiguration("Unrecognised caveat group type " + caveatGroupTypeString, e);
        }

        // Create a list of the configured caveat marks.
        List<CaveatMark> caveatMarks = new ArrayList<>();
        Set<String> markIds = new HashSet<>();
        JSONArray jsonMarks = jsonGroup.getJSONArray(MARKS_JSON_KEY);
        for (int i = 0; i < jsonMarks.length(); i++)
        {
            JSONObject jsonMark = jsonMarks.getJSONObject(i);
            CaveatMark caveatMark = createMark(jsonMark);
            caveatMarks.add(caveatMark);
            if (!markIds.contains(caveatMark.getId()))
            {
                markIds.add(caveatMark.getId());
            }
            else
            {
                throw new MalformedConfiguration("Duplicate caveat mark id " + caveatMark.getId() + " within a group.");
            }
        }

        // Instantiate the group (and associate the marks with the group).
        CaveatGroup caveatGroup = new CaveatGroup(id, displayLabelKey, descriptionKey, caveatGroupType, caveatMarks);

        return caveatGroup;
    }

    /**
     * Create a caveat mark from the supplied JSON. This does not set the group id of the caveat mark.
     *
     * @param jsonMark The JSON object corresponding to a single mark.
     * @return The created mark.
     * @throws JSONException If there is an issue reading the JSON.
     */
    private CaveatMark createMark(JSONObject jsonMark) throws JSONException
    {
        String id = jsonMark.getString(MARK_ID_JSON_KEY);
        String displayLabelKey = jsonMark.getString(MARK_DISPLAY_LABEL_JSON_KEY);
        return new CaveatMark(id, displayLabelKey);
    }
}
