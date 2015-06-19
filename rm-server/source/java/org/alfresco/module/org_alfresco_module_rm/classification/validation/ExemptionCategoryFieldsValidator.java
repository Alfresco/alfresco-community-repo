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
import org.alfresco.module.org_alfresco_module_rm.classification.ClassificationException.MissingConfiguration;
import org.alfresco.module.org_alfresco_module_rm.classification.ExemptionCategory;

/**
 * Validator for the fields in {@link ExemptionCategory}.
 *
 * @author tpage
 * @since 3.0
 */
public class ExemptionCategoryFieldsValidator implements EntityFieldsValidator<ExemptionCategory>
{
    /** Validator for the length of the category id and display key. */
    private FieldValidator<String> lengthFieldValidator = new LengthFieldValidator(1);
    /** Validator for the start characters in the category id and display key. */
    private FieldValidator<String> startCharacterFieldValidator = new StartCharacterFieldValidator();

    /**
     * Validates the given {@link ExemptionCategory}.
     *
     * @param exemptionCategory the category to validate.
     * @throws MissingConfiguration if the category is missing.
     * @throws IllegalConfiguration if the category configuration is invalid.
     */
    @Override
    public void validate(ExemptionCategory exemptionCategory)
    {
        final String id = exemptionCategory.getId();
        lengthFieldValidator.validate(id, "exemption category id");
        startCharacterFieldValidator.validate(id, "exemption category id");

        final String displayKey = exemptionCategory.getDisplayLabelKey();
        lengthFieldValidator.validate(displayKey, "exemption category display key");
        startCharacterFieldValidator.validate(displayKey, "exemption category display key");
    }
}
