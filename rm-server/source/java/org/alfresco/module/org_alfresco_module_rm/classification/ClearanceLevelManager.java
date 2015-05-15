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

import java.util.ArrayList;
import java.util.List;

import org.alfresco.module.org_alfresco_module_rm.classification.ClassificationServiceException.LevelIdNotFound;

import com.google.common.collect.ImmutableList;

/**
 * Container for the configured {@link ClearanceLevel} objects.
 *
 * @author tpage
 */
public class ClearanceLevelManager
{
	private static String NO_CLEARANCE_MSG = "rm.classification.noClearance";	
	public static final ClearanceLevel NO_CLEARANCE = new ClearanceLevel(ClassificationLevelManager.UNCLASSIFIED, NO_CLEARANCE_MSG);
	
    /** An immutable list of clearance levels ordered from most to least secure. */
    private ImmutableList<ClearanceLevel> clearanceLevels;

    /**
     * Constructor that stores an immutable copy of the given levels.
     *
     * @param clearanceLevels A list of clearance levels ordered from most to least secure.
     */
    public ClearanceLevelManager(List<ClearanceLevel> clearanceLevels)
    {
    	List<ClearanceLevel> temp = new ArrayList<ClearanceLevel>(clearanceLevels);
    	temp.add(temp.size(), NO_CLEARANCE);
        this.clearanceLevels = ImmutableList.copyOf(temp);
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

    /**
     * @return the highest security clearance level.
     */
    public ClearanceLevel getMostSecureLevel()
    {
        return clearanceLevels.get(0);
    }
}
