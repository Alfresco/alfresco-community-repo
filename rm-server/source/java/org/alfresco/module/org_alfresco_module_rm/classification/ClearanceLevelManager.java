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

import java.util.List;

import com.google.common.collect.ImmutableList;
import org.alfresco.module.org_alfresco_module_rm.classification.ClassificationServiceException.LevelIdNotFound;

/**
 * Container for the configured {@link ClearanceLevel} objects.
 *
 * @author tpage
 */
public class ClearanceLevelManager
{
    /** An immutable list of clearance levels ordered from most to least secure. */
    private ImmutableList<ClearanceLevel> clearanceLevels;

    /**
     * Constructor that stores an immutable copy of the given levels.
     *
     * @param clearanceLevels A list of clearance levels ordered from most to least secure.
     */
    public ClearanceLevelManager(List<ClearanceLevel> clearanceLevels)
    {
        this.clearanceLevels = ImmutableList.copyOf(clearanceLevels);
    }

    /** @return An immutable list of clearance levels ordered from most to least secure. */
    public ImmutableList<ClearanceLevel> getClearanceLevels()
    {
        return clearanceLevels;
    }

    /**
     * Get a <code>ClearanceLevel</code> using its id.
     *
     * @param classificationLevelId The id of the highest classification level accessible by a clearance level.
     * @return The clearance level.
     * @throws LevelIdNotFound If the clearance level cannot be found.
     */
    public ClearanceLevel findLevelByClassificationLevelId(String classificationLevelId) throws LevelIdNotFound
    {
        for (ClearanceLevel clearanceLevel : clearanceLevels)
        {
            if (clearanceLevel.getHighestClassificationLevel().getId().equals(classificationLevelId))
            {
                return clearanceLevel;
            }
        }
        throw new LevelIdNotFound(classificationLevelId);
    }
}
