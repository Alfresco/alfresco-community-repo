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

import static org.junit.Assert.assertEquals;

import java.util.List;

import com.google.common.collect.ImmutableList;
import org.alfresco.module.org_alfresco_module_rm.classification.ClassificationServiceException.LevelIdNotFound;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for the {@link ClearanceLevelManager}.
 *
 * @author tpage
 */
public class ClearanceLevelManagerUnitTest
{
    static final ClassificationLevel TOP_SECRET = new ClassificationLevel("TS", "Top Secret Classification");
    static final ClassificationLevel SECRET = new ClassificationLevel("S", "Secret Classification");
    static final ClassificationLevel UNCLASSIFIED = new ClassificationLevel("U", "Unclassified Classification");

    static final ClearanceLevel TOP_SECRET_CLEARANCE = new ClearanceLevel(TOP_SECRET , "Top Secret Clearance");
    static final ClearanceLevel SECRET_CLEARANCE = new ClearanceLevel(SECRET, "Secret Clearance");
    static final ClearanceLevel NO_CLEARANCE = new ClearanceLevel(UNCLASSIFIED, "No Clearance");

    /** The class under test. */
    ClearanceLevelManager clearanceLevelManager;

    /** Reset the {@code ClearanceLevelManager} with the three clearance levels. */
    @Before
    public void setup()
    {
        List<ClearanceLevel> clearanceLevels = ImmutableList.of(TOP_SECRET_CLEARANCE, SECRET_CLEARANCE, NO_CLEARANCE);
        clearanceLevelManager = new ClearanceLevelManager(clearanceLevels);
    }

    /** Check that the secret clearance can be found from the classification level id "S". */
    @Test
    public void findLevelByClassificationLevelId_found()
    {
        ClearanceLevel actual = clearanceLevelManager.findLevelByClassificationLevelId("S");

        assertEquals(SECRET_CLEARANCE, actual);
    }

    /** Check that an exception is thrown when the classification level id is not found. */
    @Test(expected = LevelIdNotFound.class)
    public void findLevelByClassificationLevelId_notFound()
    {
        clearanceLevelManager.findLevelByClassificationLevelId("UNKNOWN ID");
    }
}
