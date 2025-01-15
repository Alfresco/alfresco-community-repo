/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2025 - 2025 Alfresco Software Limited
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
package org.alfresco.repo.event2;

import static org.junit.Assert.assertEquals;

import java.util.Set;
import java.util.UUID;

import org.junit.Test;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.event2.mapper.PropertyMapper;
import org.alfresco.repo.event2.mapper.ReplaceSensitivePropertyWithTextMapper;
import org.alfresco.repo.transfer.TransferModel;

public class PropertyMapperUnitTest
{
    private static final String DEFAULT_REPLACEMENT_TEXT = "SENSITIVE_DATA_REMOVED";
    private static final String USER_CONFIGURED_REPLACEMENT_TEXT = "HIDDEN_BY_SECURITY_CONFIG";

    private final PropertyMapper defaultPropertyMapper = new ReplaceSensitivePropertyWithTextMapper(null,null);
    private final PropertyMapper userConfiguredpropertyMapper = new ReplaceSensitivePropertyWithTextMapper(
            Set.of(ContentModel.PROP_PASSWORD, TransferModel.PROP_PASSWORD),
            USER_CONFIGURED_REPLACEMENT_TEXT
    );

    @Test
    public void shouldReplacePropertyValueWhenItsOneOfTheDefaultSensitivePropertiesWhenUsingDefaultConfig()
    {
        assertEquals(DEFAULT_REPLACEMENT_TEXT, defaultPropertyMapper.map(ContentModel.PROP_PASSWORD, "test_pass"));
        assertEquals(DEFAULT_REPLACEMENT_TEXT, defaultPropertyMapper.map(ContentModel.PROP_SALT, UUID.randomUUID().toString()));
        assertEquals(DEFAULT_REPLACEMENT_TEXT, defaultPropertyMapper.map(ContentModel.PROP_PASSWORD_HASH, "r4nD0M_h4sH"));
        assertEquals(DEFAULT_REPLACEMENT_TEXT, defaultPropertyMapper.map(TransferModel.PROP_PASSWORD, "pyramid"));
    }

    @Test
    public void shouldNotReplacePropertyValueWhenItsNotOneOfTheDefaultSensitivePropertiesWhenUsingDefaultConfig()
    {
        assertEquals("Bob", defaultPropertyMapper.map(ContentModel.PROP_USERNAME, "Bob"));
    }

    @Test
    public void shouldReplacePropertyValueWhenItsOneOfTheDefaultSensitivePropertiesWhenUsingUserConfig()
    {
        assertEquals(USER_CONFIGURED_REPLACEMENT_TEXT, userConfiguredpropertyMapper.map(ContentModel.PROP_PASSWORD, "test_pass"));

        assertEquals(USER_CONFIGURED_REPLACEMENT_TEXT, userConfiguredpropertyMapper.map(TransferModel.PROP_PASSWORD, "pyramid"));
    }

    @Test
    public void shouldNotReplacePropertyValueWhenItsNotOneOfTheDefaultSensitivePropertiesWhenUsingUserConfig()
    {
        String randomUuid = UUID.randomUUID().toString();
        assertEquals(randomUuid, userConfiguredpropertyMapper.map(ContentModel.PROP_SALT, randomUuid));
        assertEquals("r4nD0M_h4sH", userConfiguredpropertyMapper.map(ContentModel.PROP_PASSWORD_HASH, "r4nD0M_h4sH"));
    }
}
