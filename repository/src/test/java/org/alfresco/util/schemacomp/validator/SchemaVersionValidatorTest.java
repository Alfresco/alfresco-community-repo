/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.util.schemacomp.validator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import org.junit.Before;
import org.junit.Test;

import org.alfresco.util.schemacomp.DiffContext;
import org.alfresco.util.schemacomp.Results;
import org.alfresco.util.schemacomp.ValidationResult;
import org.alfresco.util.schemacomp.model.DbObject;
import org.alfresco.util.schemacomp.model.Schema;

/**
 * Tests for the SchemaVersionValidator class.
 * 
 * @author Matt Ward
 */
public class SchemaVersionValidatorTest
{
    private SchemaVersionValidator validator;
    private DbObject reference;
    private DbObject target;
    private DiffContext ctx;
    private Results results;

    @Before
    public void setUp()
    {
        validator = new SchemaVersionValidator();
        results = new Results();
        ctx = new DiffContext(null, results, null, null);
    }

    @Test
    public void validateWhenTargetVersionPredatesReference()
    {
        reference = schemaWithVersion(501);
        target = schemaWithVersion(500);

        validator.validate(reference, target, ctx);

        assertEquals(1, results.size());
        ValidationResult result = (ValidationResult) results.get(0);
        assertEquals(500, result.getValue());
        assertEquals("version", result.getDbProperty().getPropertyName());
        assertSame(target, result.getDbProperty().getDbObject());
    }

    @Test
    public void validateWhenTargetVersionSameAsReference()
    {
        reference = schemaWithVersion(501);
        target = schemaWithVersion(501);

        validator.validate(reference, target, ctx);

        assertEquals(0, results.size());
    }

    @Test
    public void validateWhenTargetVersionAfterReference()
    {
        reference = schemaWithVersion(501);
        target = schemaWithVersion(502);

        validator.validate(reference, target, ctx);

        assertEquals(0, results.size());
    }

    @Test
    public void testValidates()
    {
        assertEquals(true, validator.validates("version"));
    }

    @Test
    public void testValidatesFullObject()
    {
        assertEquals(false, validator.validatesFullObject());
    }

    private DbObject schemaWithVersion(int version)
    {
        return new Schema("", "", version, true);
    }
}
