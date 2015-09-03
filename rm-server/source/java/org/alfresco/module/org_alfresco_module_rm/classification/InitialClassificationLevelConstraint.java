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

/**
 * Check that a value is a valid initial {@link ClassificationLevel} by checking the {@link ClassificationSchemeService}.
 * The initial classification level is allowed to be any level, regardless of the clearance of the current user.
 *
 * @author tpage
 * @since 2.4.a
 */
public class InitialClassificationLevelConstraint extends ClassificationSchemeEntityConstraint
{
    /**
     * Get the allowed values.  Note that these are <tt>String</tt> instances, but may
     * represent non-<tt>String</tt> values.  It is up to the caller to distinguish.
     *
     * @return the values allowed.
     */
    @Override
    protected List<String> getAllowedValues()
    {
        List<ClassificationLevel> classificationLevels = classificationSchemeService.getAllClassificationLevels();
        List<String> values = new ArrayList<String>();
        for (ClassificationLevel classificationLevel : classificationLevels)
        {
            values.add(classificationLevel.getId());
        }
        return values;
    }
}
