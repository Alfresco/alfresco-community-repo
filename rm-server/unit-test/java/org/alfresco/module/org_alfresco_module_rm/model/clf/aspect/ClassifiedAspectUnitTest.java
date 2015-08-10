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
package org.alfresco.module.org_alfresco_module_rm.model.clf.aspect;

import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.Date;

import org.alfresco.module.org_alfresco_module_rm.classification.ClassificationException.MissingDowngradeInstructions;
import org.alfresco.module.org_alfresco_module_rm.classification.model.ClassifiedContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

/**
 * Unit tests for the {@link ClassifiedAspect}.
 *
 * @author Tom Page
 * @since 3.0.a
 */
public class ClassifiedAspectUnitTest implements ClassifiedContentModel
{
    private static final NodeRef NODE_REF = new NodeRef("node://Ref/");

    @InjectMocks ClassifiedAspect classifiedAspect;
    @Mock NodeService mockNodeService;

    @Before
    public void setUp()
    {
        initMocks(this);
    }

    /** Check that providing an event and instructions is valid. */
    @Test
    public void testCheckConsistencyOfProperties_success()
    {
        when(mockNodeService.hasAspect(NODE_REF, ASPECT_CLASSIFIED)).thenReturn(true);
        when(mockNodeService.getProperty(NODE_REF, PROP_DOWNGRADE_DATE)).thenReturn(null);
        when(mockNodeService.getProperty(NODE_REF, PROP_DOWNGRADE_EVENT)).thenReturn("Event");
        when(mockNodeService.getProperty(NODE_REF, PROP_DOWNGRADE_INSTRUCTIONS)).thenReturn("Instructions");

        classifiedAspect.checkConsistencyOfProperties(NODE_REF);
    }

    /** Check that omitting all downgrade fields is valid. */
    @Test
    public void testCheckConsistencyOfProperties_notSpecified()
    {
        when(mockNodeService.hasAspect(NODE_REF, ASPECT_CLASSIFIED)).thenReturn(true);
        when(mockNodeService.getProperty(NODE_REF, PROP_DOWNGRADE_DATE)).thenReturn(null);
        when(mockNodeService.getProperty(NODE_REF, PROP_DOWNGRADE_EVENT)).thenReturn(null);
        when(mockNodeService.getProperty(NODE_REF, PROP_DOWNGRADE_INSTRUCTIONS)).thenReturn(null);

        classifiedAspect.checkConsistencyOfProperties(NODE_REF);
    }

    /** Check that a date without instructions throws an exception. */
    @Test(expected = MissingDowngradeInstructions.class)
    public void testCheckConsistencyOfProperties_dateMissingInstructions()
    {
        when(mockNodeService.hasAspect(NODE_REF, ASPECT_CLASSIFIED)).thenReturn(true);
        when(mockNodeService.getProperty(NODE_REF, PROP_DOWNGRADE_DATE)).thenReturn(new Date(123));
        when(mockNodeService.getProperty(NODE_REF, PROP_DOWNGRADE_EVENT)).thenReturn(null);
        when(mockNodeService.getProperty(NODE_REF, PROP_DOWNGRADE_INSTRUCTIONS)).thenReturn(null);

        classifiedAspect.checkConsistencyOfProperties(NODE_REF);
    }

    /** Check that an event without instructions throws an exception. */
    @Test(expected = MissingDowngradeInstructions.class)
    public void testCheckConsistencyOfProperties_eventMissingInstructions()
    {
        when(mockNodeService.hasAspect(NODE_REF, ASPECT_CLASSIFIED)).thenReturn(true);
        when(mockNodeService.getProperty(NODE_REF, PROP_DOWNGRADE_DATE)).thenReturn(null);
        when(mockNodeService.getProperty(NODE_REF, PROP_DOWNGRADE_EVENT)).thenReturn("Event");
        when(mockNodeService.getProperty(NODE_REF, PROP_DOWNGRADE_INSTRUCTIONS)).thenReturn(null);

        classifiedAspect.checkConsistencyOfProperties(NODE_REF);
    }

    /** Check that blank instructions are treated in the same way as null instructions. */
    @Test(expected = MissingDowngradeInstructions.class)
    public void testCheckConsistencyOfProperties_emptyStringsSupplied()
    {
        when(mockNodeService.hasAspect(NODE_REF, ASPECT_CLASSIFIED)).thenReturn(true);
        when(mockNodeService.getProperty(NODE_REF, PROP_DOWNGRADE_DATE)).thenReturn("");
        when(mockNodeService.getProperty(NODE_REF, PROP_DOWNGRADE_EVENT)).thenReturn("Event");
        when(mockNodeService.getProperty(NODE_REF, PROP_DOWNGRADE_INSTRUCTIONS)).thenReturn("");

        classifiedAspect.checkConsistencyOfProperties(NODE_REF);
    }
}
