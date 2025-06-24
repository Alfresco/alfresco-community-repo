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
package org.alfresco.util.schemacomp;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.surf.util.I18NUtil;

import org.alfresco.util.schemacomp.model.Index;

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
        when(targetDbProp.getPath()).thenReturn("alfresco.some_table.idx_table_id.name");
        when(targetDbProp.getPropertyValue()).thenReturn("idx_table_id");
        when(targetDbProp.getDbObject()).thenReturn(new Index(""));

        ValidationResult validation = new ValidationResult(targetDbProp, "value must be 'xyz'");

        assertEquals("Validation: index alfresco.some_table.idx_table_id.name=\"idx_table_id\" fails to " +
                "match rule: value must be 'xyz'",
                validation.describe());
    }
}
