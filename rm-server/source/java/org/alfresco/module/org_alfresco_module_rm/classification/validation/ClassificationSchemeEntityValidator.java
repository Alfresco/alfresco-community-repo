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
package org.alfresco.module.org_alfresco_module_rm.classification.validation;

import static org.alfresco.module.org_alfresco_module_rm.util.RMCollectionUtils.getDuplicateElements;

import java.util.List;

import org.alfresco.module.org_alfresco_module_rm.classification.ClassificationException.IllegalConfiguration;
import org.alfresco.module.org_alfresco_module_rm.classification.ClassificationException.MissingConfiguration;
import org.alfresco.module.org_alfresco_module_rm.classification.ClassificationSchemeEntity;

/**
 * This class is responsible for validating configured data objects for use in the classification feature.
 *
 * @author Neil Mc Erlean
 * @author tpage
 * @since 2.4.a
 */
public class ClassificationSchemeEntityValidator<T extends ClassificationSchemeEntity>
{
    private EntityFieldsValidator<T> entityFieldsValidator;

    public ClassificationSchemeEntityValidator(EntityFieldsValidator<T> entityFieldsValidator)
    {
        this.entityFieldsValidator = entityFieldsValidator;
    }

    /**
     * Validates the provided entities as a whole and individually.
     * @param objects the entities to validate.
     * @param entityName the simple name of the class to validate.
     * @throws MissingConfiguration if the configuration is missing.
     * @throws IllegalConfiguration if configuration is invalid.
     */
    public void validate(List<T> objects, String entityName)
    {
        if (objects == null || objects.isEmpty())
        {
            throw new MissingConfiguration(entityName + " configuration is missing.");
        }

        final List<T> duplicateObjects= getDuplicateElements(objects);
        if (!duplicateObjects.isEmpty())
        {
            throw new IllegalConfiguration("Illegal " + entityName + " configuration - duplicate values are not allowed: " + duplicateObjects);
        }

        for (T object : objects)
        {
            entityFieldsValidator.validate(object);
        }
    }
}
