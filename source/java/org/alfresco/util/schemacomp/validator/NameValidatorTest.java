/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
package org.alfresco.util.schemacomp.validator;


import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.alfresco.util.schemacomp.DiffContext;
import org.alfresco.util.schemacomp.Results;
import org.alfresco.util.schemacomp.ValidationResult;
import org.alfresco.util.schemacomp.model.DbObject;
import org.alfresco.util.schemacomp.model.Index;
import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.Oracle10gDialect;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the NameValidator class.
 * 
 * @author Matt Ward
 */
public class NameValidatorTest
{
    private NameValidator validator;
    private DiffContext ctx;
    private List<ValidationResult> validationResults;
    
    @Before
    public void setUp() throws Exception
    {
        validator = new NameValidator();
        validationResults = new ArrayList<ValidationResult>();
        ctx = new DiffContext(new Oracle10gDialect(), new Results(), validationResults);
    }

    @Test
    public void canSpecifyDefaultRequiredPattern()
    {
        validator.setDefaultNamePattern(Pattern.compile("SYS_[A-Z_]+"));
        validator.validate(indexForName("SYS_MYINDEX"), ctx);
        validator.validate(indexForName("SYS_"), ctx);
        validator.validate(indexForName("SYS_MY_INDEX"), ctx);
        validator.validate(indexForName("MY_INDEX"), ctx);
        
        assertEquals(2, validationResults.size());
        assertEquals("SYS_", validationResults.get(0).getValue());
        assertEquals("MY_INDEX", validationResults.get(1).getValue());
    }
    
    @Test
    public void canValidateAgainstPatternForDialect()
    {
        Map<Class<? extends Dialect>, Pattern> patterns = new HashMap<Class<? extends Dialect>, Pattern>();
        patterns.put(Oracle10gDialect.class, Pattern.compile("ORA_[A-Z_]+"));
        validator.setNamePatterns(patterns);
        
        validator.validate(indexForName("ORA_MYINDEX"), ctx);
        validator.validate(indexForName("SYS_MYINDEX"), ctx);
        
        assertEquals(1, validationResults.size());
        assertEquals("SYS_MYINDEX", validationResults.get(0).getValue());
    }

    
    private DbObject indexForName(String name)
    {
        return new Index(null, name, new ArrayList<String>());
    }
}
