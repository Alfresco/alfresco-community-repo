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
package org.alfresco.repo.domain.propval;

import static org.junit.Assert.assertEquals;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import org.alfresco.repo.domain.dialect.MySQLInnoDBDialect;
import org.alfresco.repo.domain.schema.SchemaBootstrap;
import org.alfresco.test_category.OwnJVMTestsCategory;

/**
 * @see PropertyTypeConverter
 * 
 * @author Cristian Turlica
 * @since 5.1
 */
@Category(OwnJVMTestsCategory.class)
public class PropertyTypeConverterTest
{
    private int stringLen;

    @Before
    public void setMaxStringLength()
    {
        stringLen = SchemaBootstrap.getMaxStringLength();
        SchemaBootstrap.setMaxStringLength(2000, new MySQLInnoDBDialect());
    }

    @After
    public void resetMaxStringLength()
    {
        SchemaBootstrap.setMaxStringLength(stringLen, new MySQLInnoDBDialect());
    }

    @Test
    public void testGetPersistentTypeForStrings()
    {
        DefaultPropertyTypeConverter defaultPropertyTypeConverter = new DefaultPropertyTypeConverter();

        // Check string.
        PropertyValueEntity.PersistedType persistedType = PropertyValueEntity.getPersistedTypeEnum("test", defaultPropertyTypeConverter);
        assertEquals(PropertyValueEntity.PersistedType.STRING, persistedType);

        // String value with length greater than the DB supported threshold.
        String stringValue = RandomStringUtils.randomAlphanumeric(2001);
        // ... persisted as blobs (see MNT-17523 for details).
        persistedType = PropertyValueEntity.getPersistedTypeEnum(stringValue, defaultPropertyTypeConverter);
        assertEquals(PropertyValueEntity.PersistedType.SERIALIZABLE, persistedType);

        // String value with length less than the DB supported threshold.
        stringValue = RandomStringUtils.randomAlphanumeric(1999);
        // ... persisted as strings
        persistedType = PropertyValueEntity.getPersistedTypeEnum(stringValue, defaultPropertyTypeConverter);
        assertEquals(PropertyValueEntity.PersistedType.STRING, persistedType);
    }

}
