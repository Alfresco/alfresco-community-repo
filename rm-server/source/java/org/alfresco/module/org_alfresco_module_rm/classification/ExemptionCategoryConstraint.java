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
 * Check that a value is a valid {@link ExemptionCategory} by checking the {@link ClassificationSchemeService}.
 *
 * @author tpage
 * @since 2.4.a
 */
public class ExemptionCategoryConstraint extends ClassificationSchemeEntityConstraint
{
    /**
     * Get the allowed values.  Note that these are <tt>String</tt> instances, but may
     * represent non-<tt>String</tt> values.  It is up to the caller to distinguish.
     *
     * @return Returns the values allowed
     */
    @Override
    protected List<String> getAllowedValues()
    {
        List<ExemptionCategory> exemptionCategories = classificationSchemeService.getExemptionCategories();
        List<String> values = new ArrayList<String>();
        for (ExemptionCategory exemptionCategory : exemptionCategories)
        {
            values.add(exemptionCategory.getId());
        }
        return values;
    }
}
