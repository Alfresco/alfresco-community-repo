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

import org.alfresco.util.testing.category.DebugTests;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/**
 * Disables local transform and repeats the RenditionTests
 *
 * @author adavis
 */
public class NoLocalTransformRenditionTest extends RenditionTest
{
    @BeforeClass
    public static void before()
    {
        AbstractRenditionIntegrationTest.before();
        System.setProperty("local.transform.service.enabled", "false");
    }

    @AfterClass
    public static void after()
    {
        AbstractRenditionIntegrationTest.after();
        System.clearProperty("local.transform.service.enabled");
    }


    @Test
    @Override
    public void testTasRestApiRenditions() throws Exception
    {
        internalTestTasRestApiRenditions(0, 0);
    }

    @Category(DebugTests.class)
    @Test
    @Override
    public void testAllSourceExtensions() throws Exception
    {
        internalTestAllSourceExtensions(0, 0);
    }

    @Test
    @Override
    public void testGifRenditions() throws Exception
    {
        internalTestGifRenditions(0, 0);
    }
}
