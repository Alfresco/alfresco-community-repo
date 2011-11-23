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
package org.alfresco.util.schemacomp;


import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.surf.util.I18NUtil;


/**
 * Tests for the {@link ValidationResult} class.
 * 
 * @author Matt Ward
 */
public class ValidationResultTest
{
    @Before
    public void setUp()
    {
        I18NUtil.registerResourceBundle("alfresco.messages.system-messages");
    }
    
    @Test
    public void describe()
    {        
        DbProperty targetDbProp = mock(DbProperty.class);
        when(targetDbProp.getPath()).thenReturn("alfresco.some_table.some_index.name");
        when(targetDbProp.getPropertyValue()).thenReturn("ibx_my_index");
        
        ValidationResult validation = new ValidationResult(targetDbProp, "value must be 'xyz'");
        
        assertEquals("Validation: target path:alfresco.some_table.some_index.name (value: ibx_my_index, rule: value must be 'xyz')",
                    validation.describe());
    }
}
