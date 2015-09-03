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

import java.util.Collection;

import com.google.common.collect.ImmutableList;
import org.alfresco.module.org_alfresco_module_rm.classification.ClassificationException.ExemptionCategoryIdNotFound;

/**
 * Container for the configured {@link ExemptionCategory} objects.
 *
 * @author tpage
 * @since 2.4.a
 */
public class ExemptionCategoryManager
{
    /** An immutable list of exemption categories. */
    private ImmutableList<ExemptionCategory> exemptionCategories;

    /**
     * Store an immutable copy of the given categories.
     *
     * @param exemptionCategories The exemption categories.
     */
    public void setExemptionCategories(Collection<ExemptionCategory> exemptionCategories)
    {
        this.exemptionCategories = ImmutableList.copyOf(exemptionCategories);
    }

    /** @return An immutable list of exemption categories. */
    public ImmutableList<ExemptionCategory> getExemptionCategories()
    {
        return exemptionCategories;
    }

    /**
     * Get a <code>ExemptionCategory</code> using its id.
     *
     * @param id The id of an exemption category.
     * @return The exemption category.
     * @throws ExemptionCategoryIdNotFound If the exemption category cannot be found.
     */
    public ExemptionCategory findCategoryById(String id) throws ExemptionCategoryIdNotFound
    {
        for (ExemptionCategory exemptionCategory : exemptionCategories)
        {
            if (exemptionCategory.getId().equals(id))
            {
                return exemptionCategory;
            }
        }
        throw new ExemptionCategoryIdNotFound(id);
    }
}
