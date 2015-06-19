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
import org.alfresco.module.org_alfresco_module_rm.classification.ClassificationException.MissingConfiguration;

/**
 * Validate the length of a field.
 *
 * @author tpage
 * @since 3.0
 */
public class LengthFieldValidator implements FieldValidator<String>
{
    /** The minimum allowed length for the field. */
    private int minimumLength;
    /** The maximum allowed length for the field, or {@code null} if there is no maximum length. */
    private Integer maximumLength = null;

    /**
     * Create a validator that only checks the minimum length.
     *
     * @param minimumLength The length of the shortest allowable field.
     */
    public LengthFieldValidator(int minimumLength)
    {
        this.minimumLength = minimumLength;
    }

    /**
     * Create a validator that checks the minimum and maximum length.
     *
     * @param minimumLength The length of the shortest allowable field.
     * @param maximumLength The length of the longest allowable field.
     */
    public LengthFieldValidator(int minimumLength, int maximumLength)
    {
        if (minimumLength > maximumLength)
        {
            throw new IllegalArgumentException("The minimum length may not be larger than the maximum length.");
        }

        this.minimumLength = minimumLength;
        this.maximumLength = maximumLength;
    }

    @Override
    public void validate(String field, String fieldName) throws ClassificationException
    {
        if (field == null || (field.length() == 0 && minimumLength > 0))
        {
            throw new MissingConfiguration(fieldName + " is missing.");
        }
        else if (field.length() < minimumLength)
        {
            throw new IllegalConfiguration("Illegal " + fieldName + ": Length " + field.length() + " < " + minimumLength);
        }
        else if (maximumLength != null && field.length() > maximumLength)
        {
            throw new IllegalConfiguration("Illegal " + fieldName + ": Length " + field.length() + " > " + maximumLength);
        }
    }

}
