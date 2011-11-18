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
import java.util.regex.Pattern;

import org.alfresco.util.schemacomp.DiffContext;
import org.alfresco.util.schemacomp.Results;
import org.alfresco.util.schemacomp.ValidationResult;
import org.alfresco.util.schemacomp.model.DbObject;
import org.alfresco.util.schemacomp.model.Index;
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
    private Results validationResults;
    
    @Before
    public void setUp() throws Exception
    {
        validator = new NameValidator();
        validationResults = new Results();
        ctx = new DiffContext(new Oracle10gDialect(), validationResults, null, null);
    }

    @Test
    public void canSpecifyDefaultRequiredPattern()
    {
        validator.setPattern(Pattern.compile("SYS_[A-Z_]+"));
        validator.validate(null, indexForName("SYS_MYINDEX"), ctx);
        validator.validate(null, indexForName("SYS_"), ctx);
        validator.validate(null, indexForName("SYS_MY_INDEX"), ctx);
        validator.validate(null, indexForName("MY_INDEX"), ctx);
        
        assertEquals(2, validationResults.size());
        assertEquals("SYS_", ((ValidationResult) validationResults.get(0)).getValue());
        assertEquals("MY_INDEX", ((ValidationResult) validationResults.get(1)).getValue());
    }
    
    @Test
    public void canValidateAgainstPatternForDialect()
    {
        validator.setPattern(Pattern.compile("ORA_[A-Z_]+"));
        
        validator.validate(null, indexForName("ORA_MYINDEX"), ctx);
        validator.validate(null, indexForName("SYS_MYINDEX"), ctx);
        
        assertEquals(1, validationResults.size());
        assertEquals("SYS_MYINDEX", ((ValidationResult) validationResults.get(0)).getValue());
    }

    
    @Test
    public void canSetPatternUsingProperties()
    {
        validator.setProperty("pattern", "ORA_[A-Z_]+");
        assertEquals("ORA_[A-Z_]+", validator.getPattern().toString());
    }
    
    
    private DbObject indexForName(String name)
    {
        return new Index(null, name, new ArrayList<String>());
    }
}
