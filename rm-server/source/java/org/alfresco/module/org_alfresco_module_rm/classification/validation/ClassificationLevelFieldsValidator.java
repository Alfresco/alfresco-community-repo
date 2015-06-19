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

import org.alfresco.module.org_alfresco_module_rm.classification.ClassificationException.IllegalAbbreviationChars;
import org.alfresco.module.org_alfresco_module_rm.classification.ClassificationException.IllegalConfiguration;
import org.alfresco.module.org_alfresco_module_rm.classification.ClassificationException.MissingConfiguration;
import org.alfresco.module.org_alfresco_module_rm.classification.ClassificationLevel;

/**
 * Validator for the fields in {@link ClassificationLevel}.
 *
 * @author tpage
 * @since 3.0
 */
public class ClassificationLevelFieldsValidator implements EntityFieldsValidator<ClassificationLevel>
{
    /** The maximum number of characters allowed in a {@link ClassificationLevel#getId() level ID}. */
    private static final int ABBREVIATION_LENGTH_LIMIT = 10;
    /** Validator for the length of the level abbreviation. */
    private FieldValidator<String> lengthFieldValidator = new LengthFieldValidator(1, ABBREVIATION_LENGTH_LIMIT);
    /** Validator for the characters in the level abbreviation. */
    private FieldValidator<String> filenameFieldValidator = new FilenameFieldValidator();
    /** Validator that checks that the reserved symbol "U" is not configured as a classification level. */
    private FieldValidator<String> classificationLevelIsNotUnclassifiedValidator = new ClassificationLevelIsNotUnclassifiedValidator();

    /**
     * Validates the provided {@link ClassificationLevel}.
     *
     * @param classificationLevel the level to validate.
     * @throws MissingConfiguration if the level abbreviation is missing.
     * @throws IllegalConfiguration if the level abbreviation violates the standard restrictions.
     * @throws IllegalAbbreviationChars if the level abbreviation contains illegal characters.
     */
    @Override
    public void validate(ClassificationLevel classificationLevel)
    {
        final String levelId = classificationLevel.getId();

        lengthFieldValidator.validate(levelId, "level abbreviation");
        filenameFieldValidator.validate(levelId, "level abbreviation");
        classificationLevelIsNotUnclassifiedValidator.validate(levelId, "level abbreviation");
    }
}
