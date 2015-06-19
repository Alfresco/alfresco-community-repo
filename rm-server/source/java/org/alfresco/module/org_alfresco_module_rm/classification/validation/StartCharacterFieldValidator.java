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

import org.alfresco.module.org_alfresco_module_rm.classification.ClassificationException;
import org.alfresco.module.org_alfresco_module_rm.classification.ClassificationException.IllegalConfiguration;
import org.apache.commons.lang3.StringUtils;

/**
 * Validator that fails if the first character of a field is non-alphanumeric.
 *
 * @author tpage
 * @since 3.0
 */
public class StartCharacterFieldValidator implements FieldValidator<String>
{
    @Override
    public void validate(String field, String fieldName) throws ClassificationException
    {
        if (field == null || field.length() == 0)
        {
            // If the first character doesn't exist then don't fail.
            return;
        }
        if (!StringUtils.isAlphanumeric(field.substring(0, 1)))
        {
            throw new IllegalConfiguration("Illegal " + fieldName + ": First character of '" + field + "' is not alphanumeric.");
        }
    }
}
