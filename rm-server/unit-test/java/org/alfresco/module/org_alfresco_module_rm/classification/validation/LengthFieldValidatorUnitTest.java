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
import org.junit.Test;

/**
 * Unit tests for the {@link LengthFieldValidator}.
 *
 * @author tpage
 * @since 3.0
 */
public class LengthFieldValidatorUnitTest
{
    private static final String FIELD_NAME = "FIELD_NAME";
    /** The class under test. */
    LengthFieldValidator lengthFieldValidator;

    @Test
    public void testValidate_passMinOnly()
    {
        lengthFieldValidator = new LengthFieldValidator(1);
        lengthFieldValidator.validate("acceptable", FIELD_NAME);
    }

    @Test(expected = MissingConfiguration.class)
    public void testValidate_missingConfiguration()
    {
        lengthFieldValidator = new LengthFieldValidator(1);
        lengthFieldValidator.validate("", FIELD_NAME);
    }

    @Test(expected = IllegalConfiguration.class)
    public void testValidate_tooShort()
    {
        lengthFieldValidator = new LengthFieldValidator(6);
        lengthFieldValidator.validate("short", FIELD_NAME);
    }

    @Test
    public void testValidate_passMinAndMax()
    {
        lengthFieldValidator = new LengthFieldValidator(5,5);
        lengthFieldValidator.validate("super", FIELD_NAME);
    }

    @Test(expected = IllegalConfiguration.class)
    public void testValidate_tooLong()
    {
        lengthFieldValidator = new LengthFieldValidator(6, 7);
        lengthFieldValidator.validate("too long", FIELD_NAME);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValidate_invalidArguments()
    {
        lengthFieldValidator = new LengthFieldValidator(3, 1);
    }
}
