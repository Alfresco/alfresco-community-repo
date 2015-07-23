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

import org.alfresco.module.org_alfresco_module_rm.caveat.RMConstraintMessageKeys;
import org.alfresco.repo.dictionary.constraint.AbstractConstraint;
import org.alfresco.service.cmr.dictionary.ConstraintException;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.repository.datatype.TypeConversionException;

/**
 * Abstract for constraints the check that a value is a valid {@link ClassificationSchemeEntity} using the
 * {@link ClassificationSchemeService}.
 *
 * @author tpage
 * @since 3.0.a
 */
public abstract class ClassificationSchemeEntityConstraint extends AbstractConstraint
{
    /** The classification scheme service provides access to the valid classification levels. */
    protected ClassificationSchemeService classificationSchemeService;

    /** Constraints must use a default constructor. */
    public ClassificationSchemeEntityConstraint()
    {
        super();
        this.classificationSchemeService = ClassificationSchemeServiceProvider.getClassificationSchemeService();
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        // TODO Decide whether calling getAllowedValues() is a good idea.
        sb.append(this.getClass().getSimpleName())
          .append("[allowedValues=").append(getAllowedValues())
          .append("]");
        return sb.toString();
    }

    /**
     * Get the allowed values.  Note that these are <tt>String</tt> instances, but may
     * represent non-<tt>String</tt> values.  It is up to the caller to distinguish.
     *
     * @return Returns the values allowed
     */
    protected abstract List<String> getAllowedValues();

    /** {@inheritDoc} */
    @Override
    protected void evaluateSingleValue(Object value)
    {
        // convert the value to a String
        String valueStr = null;
        try
        {
            valueStr = DefaultTypeConverter.INSTANCE.convert(String.class, value);
        }
        catch (TypeConversionException e)
        {
            throw new ConstraintException(RMConstraintMessageKeys.ERR_NON_STRING, value);
        }
        // Check that the classification level is one of the configured levels.
        if (!getAllowedValues().contains(valueStr))
        {
            throw new ConstraintException(RMConstraintMessageKeys.ERR_INVALID_VALUE, value);
        }
    }
}
