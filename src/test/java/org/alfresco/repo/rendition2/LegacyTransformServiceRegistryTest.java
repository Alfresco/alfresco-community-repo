/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2018 Alfresco Software Limited
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
package org.alfresco.repo.rendition2;

import org.alfresco.transform.client.model.config.TransformServiceRegistry;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;

import static org.alfresco.repo.content.MimetypeMap.MIMETYPE_OPENXML_WORDPROCESSING;
import static org.alfresco.repo.content.MimetypeMap.MIMETYPE_PDF;

/**
 * Integration tests for {@link LegacyTransformServiceRegistry}
 */
@Deprecated
public class LegacyTransformServiceRegistryTest extends LocalTransformServiceRegistryTest
{
    @Autowired
    private LegacyTransformServiceRegistry legacyTransformServiceRegistry;

    @Before
    public void setUp() throws Exception
    {
        super.setUp();
        transformServiceRegistry = legacyTransformServiceRegistry;
    }

    @BeforeClass
    public static void before()
    {
        AbstractRenditionIntegrationTest.before();
        legacy();
    }

    @AfterClass
    public static void after()
    {
        AbstractRenditionIntegrationTest.after();
    }

    protected void setEnabled(boolean enabled)
    {
        legacyTransformServiceRegistry.setEnabled(enabled);
    }
}
