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

import static java.util.Collections.unmodifiableList;

import org.alfresco.module.org_alfresco_module_rm.classification.model.ClassifiedContentModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Check that a {@link ClassifiedContentModel#PROP_LAST_RECLASSIFICATION_ACTION reclassifiction action }value is valid.
 *
 * @author Neil Mc Erlean
 * @since 2.4.a
 */
public class ReclassificationValueConstraint extends ClassificationSchemeEntityConstraint
{
    @Override
    protected List<String> getAllowedValues()
    {
        final Set<String> resultSet = classificationSchemeService.getReclassificationValues();
        List<String> result = new ArrayList<>(resultSet.size());
        result.addAll(resultSet);

        return unmodifiableList(result);
    }
}
