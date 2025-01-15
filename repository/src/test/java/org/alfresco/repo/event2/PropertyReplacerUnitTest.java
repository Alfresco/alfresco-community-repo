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

import java.util.UUID;

import org.junit.Test;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.event2.replacer.PropertyReplacer;
import org.alfresco.repo.transfer.TransferModel;

public class PropertyReplacerUnitTest
{
    private final PropertyReplacer propertyReplacer = new PropertyReplacer();

    @Test
    public void shouldReplacePropertyValueWhenItsOneOfTheDefaultSensitiveProperties()
    {
        assertEquals("SENSITIVE_DATA_REMOVED", propertyReplacer.replace(ContentModel.PROP_PASSWORD, "test_pass"));
        assertEquals("SENSITIVE_DATA_REMOVED", propertyReplacer.replace(ContentModel.PROP_SALT, UUID.randomUUID().toString()));
        assertEquals("SENSITIVE_DATA_REMOVED", propertyReplacer.replace(ContentModel.PROP_PASSWORD_HASH, "r4nD0M_h4sH"));
        assertEquals("SENSITIVE_DATA_REMOVED", propertyReplacer.replace(TransferModel.PROP_PASSWORD, "pyramid"));
    }

    @Test
    public void shouldNotReplacePropertyValueWhenItsNotOneOfTheDefaultSensitiveProperties()
    {
        assertEquals("Bob", propertyReplacer.replace(ContentModel.PROP_USERNAME, "Bob"));
    }
}
