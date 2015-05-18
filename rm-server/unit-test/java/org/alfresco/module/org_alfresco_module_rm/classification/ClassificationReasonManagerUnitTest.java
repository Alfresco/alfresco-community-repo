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

import org.alfresco.module.org_alfresco_module_rm.classification.ClassificationServiceException.ReasonIdNotFound;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for the {@link ClassificationReasonManager}.
 *
 * @author tpage
 */
public class ClassificationReasonManagerUnitTest
{
    private static final ClassificationReason REASON_1 = new ClassificationReason("id1", "displayLabelKey1");
    private static final ClassificationReason REASON_2 = new ClassificationReason("id2", "displayLabelKey2");
    private static final ClassificationReason REASON_3 = new ClassificationReason("id3", "displayLabelKey3");
    private static final List<ClassificationReason> REASONS = Arrays.asList(REASON_1, REASON_2, REASON_3);

    private ClassificationReasonManager classificationReasonManager;

    @Before public void setup()
    {
        classificationReasonManager = new ClassificationReasonManager();
        classificationReasonManager.setClassificationReasons(REASONS);
    }

    @Test public void findClassificationById_found()
    {
        ClassificationReason actual = classificationReasonManager.findReasonById("id2");
        assertEquals(REASON_2, actual);
    }

    @Test(expected = ReasonIdNotFound.class) public void findClassificationById_notFound()
    {
        classificationReasonManager.findReasonById("id_unknown");
    }
}
