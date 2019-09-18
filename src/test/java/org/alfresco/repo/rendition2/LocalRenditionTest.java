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

import org.alfresco.transform.client.registry.AbstractTransformRegistry;
import org.alfresco.transform.client.registry.SupportedTransform;
import org.alfresco.util.testing.category.DebugTests;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.alfresco.repo.content.MimetypeMap.MIMETYPE_TEXT_PLAIN;

/**
 * Repeats quick file rendition tests with local transforms enabled but legacy transformers disabled.
 * The Transform Service does not exist for the Community edition.
 * Should be the same result as with legacy transforms only.
 *
 * @author adavis
 */
public class LocalRenditionTest extends AbstractRenditionTest
{
    public static final List<String> ALL_SOURCE_EXTENSIONS_EXCLUDE_LIST_LOCAL = Arrays.asList(
        // - textToPdf returned a 400 status Miscellaneous Transformers - U+0628 ('afii57416') is not available in this font Helvetica encoding: WinAnsiEncoding
        //   This is because the wrong transformer is being used due bug. The priority in the transform config is currently ignored.
        "txt pdf pdf",

        "tiff jpg imgpreview",
        "tiff jpg medium",
        "tiff png doclib",
        "tiff png avatar",
        "tiff png avatar32"
    );

    @BeforeClass
    public static void before()
    {
        AbstractRenditionIntegrationTest.before();
        local();
    }

    @AfterClass
    public static void after()
    {
        AbstractRenditionIntegrationTest.after();
    }

    // TODO this method will be removed when Local transformers the same transforms as legacy
    @Override
    @Category(DebugTests.class)
    @Test
    public void testAllSourceExtensions() throws Exception
    {
        internalTestAllSourceExtensions(196, 0, ALL_SOURCE_EXTENSIONS_EXCLUDE_LIST_LOCAL);
    }

    @Override
    protected AbstractTransformRegistry getAbstractTransformRegistry()
    {
        return (AbstractTransformRegistry) localTransformServiceRegistry;
    }
}
