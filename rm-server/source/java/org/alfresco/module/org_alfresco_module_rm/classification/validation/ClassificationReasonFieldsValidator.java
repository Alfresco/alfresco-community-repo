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
import org.alfresco.module.org_alfresco_module_rm.classification.ClassificationReason;

/**
 * Validator for the fields in {@link ClassificationReason}.
 *
 * @author tpage
 * @since 3.0.a
 */
public class ClassificationReasonFieldsValidator implements EntityFieldsValidator<ClassificationReason>
{
    /** Validator for the length of the reason id and display key. */
    private FieldValidator<String> lengthFieldValidator = new LengthFieldValidator(1);
    /** Validator for the start characters in the reason id and display key. */
    private FieldValidator<String> startCharacterFieldValidator = new StartCharacterFieldValidator();

    /**
     * Validates the given {@link ClassificationReason}.
     *
     * @param classificationReason the reason to validate.
     * @throws MissingConfiguration if the reason is missing.
     * @throws IllegalConfiguration if the reason configuration is invalid.
     */
    @Override
    public void validate(ClassificationReason classificationReason)
    {
        final String id = classificationReason.getId();
        lengthFieldValidator.validate(id, "reason id");
        startCharacterFieldValidator.validate(id, "reason id");

        final String displayKey = classificationReason.getDisplayLabelKey();
        lengthFieldValidator.validate(displayKey, "reason display key");
        startCharacterFieldValidator.validate(displayKey, "reason display key");
    }
}
