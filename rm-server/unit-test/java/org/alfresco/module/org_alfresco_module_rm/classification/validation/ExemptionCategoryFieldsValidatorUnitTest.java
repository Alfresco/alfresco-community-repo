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

import org.alfresco.module.org_alfresco_module_rm.classification.ClassificationException.IllegalConfiguration;
import org.alfresco.module.org_alfresco_module_rm.classification.ExemptionCategory;
import org.junit.Test;

/**
 * Unit tests for the {@link ExemptionCategoryFieldsValidator}.
 *
 * @author Oana Nechiforescu
 * @since 2.4.a
 */
public class ExemptionCategoryFieldsValidatorUnitTest
{
    /** Exemption category fields validator */
    ExemptionCategoryFieldsValidator exemptionCategoryFieldsValidator = new ExemptionCategoryFieldsValidator();

    @Test(expected = IllegalArgumentException.class)
    public void testMissingExemptionCategoryID()
    {
        ExemptionCategory exemptionCategory = new ExemptionCategory("", "label");
        exemptionCategoryFieldsValidator.validate(exemptionCategory);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMissingExemptionCategoryLabel()
    {
        ExemptionCategory exemptionCategory = new ExemptionCategory("12@1", "");
        exemptionCategoryFieldsValidator.validate(exemptionCategory);
    }

    @Test(expected = IllegalConfiguration.class)
    public void testExemptionIDStartingWithNonAlphanumericCharacter()
    {
        ExemptionCategory exemptionCategory = new ExemptionCategory(" 12", "critical");
        exemptionCategoryFieldsValidator.validate(exemptionCategory);
    }
}