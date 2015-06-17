/*
 * Copyright (C) 2005-2015 Alfresco Software Limited.
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
package org.alfresco.module.org_alfresco_module_rm.classification;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.alfresco.module.org_alfresco_module_rm.classification.ClassificationException.ExemptionCategoryIdNotFound;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for the {@link ExemptionCategoryManager}.
 *
 * @author tpage
 */
public class ExemptionCategoryManagerUnitTest
{
    private static final ExemptionCategory CATEGORY_1 = new ExemptionCategory("id1", "displayLabelKey1");
    private static final ExemptionCategory CATEGORY_2 = new ExemptionCategory("id2", "displayLabelKey2");
    private static final ExemptionCategory CATEGORY_3 = new ExemptionCategory("id3", "displayLabelKey3");
    private static final List<ExemptionCategory> CATEGORIES = Arrays.asList(CATEGORY_1, CATEGORY_2, CATEGORY_3);

    private ExemptionCategoryManager exemptionCategoryManager;

    @Before public void setup()
    {
        exemptionCategoryManager = new ExemptionCategoryManager();
        exemptionCategoryManager.setExemptionCategories(CATEGORIES);
    }

    @Test public void findClassificationById_found()
    {
        ExemptionCategory actual = exemptionCategoryManager.findCategoryById("id2");
        assertEquals(CATEGORY_2, actual);
    }

    @Test(expected = ExemptionCategoryIdNotFound.class) public void findClassificationById_notFound()
    {
        exemptionCategoryManager.findCategoryById("id_unknown");
    }
}
