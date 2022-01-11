/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.module.org_alfresco_module_rm.disposition;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Date;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.Period;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link DispositionServiceImpl}.
 *
 * @author Tom Page
 * @since 2.3.1
 */
public class DispositionServiceImplUnitTest
{
    /** The node being subject to the disposition step. */
    NodeRef CONTENT_NODE_REF = new NodeRef("content://node/");

    /** The class under test. */
    private DispositionServiceImpl dispositionService = new DispositionServiceImpl();

    private NodeService mockNodeService = mock(NodeService.class);

    @Before
    public void setUp()
    {
        dispositionService.setNodeService(mockNodeService);
    }

    /**
     * Check that the relevant information is retrieved from the DispositionActionDefinition in order to determine the
     * "disposition as of" date.
     */
    @Test
    public void testCalculateAsOfDate()
    {
        // Set up a mock for the disposition action definition.
        DispositionActionDefinition mockDispositionActionDefinition = mock(DispositionActionDefinition.class);
        Period mockPeriod = mock(Period.class);
        when(mockDispositionActionDefinition.getPeriod()).thenReturn(mockPeriod);
        when(mockDispositionActionDefinition.getPeriodProperty()).thenReturn(ContentModel.PROP_CREATED);
        // Set up a created date and another date that is some Period later.
        Date createdDate = new Date(1234567890);
        when(mockNodeService.getProperty(CONTENT_NODE_REF, ContentModel.PROP_CREATED)).thenReturn(createdDate);
        Date nextDate = new Date(1240000000);
        when(mockPeriod.getNextDate(createdDate)).thenReturn(nextDate);

        // Call the method under test.
        Date asOfDate = dispositionService.calculateAsOfDate(CONTENT_NODE_REF, mockDispositionActionDefinition);

        assertEquals("Unexpected calculation for 'as of' date", nextDate, asOfDate);
    }

    /** Check that the calculated "disposition as of" date is null if a null period is given. */
    @Test
    public void testCalculateAsOfDate_nullPeriod()
    {
        DispositionActionDefinition mockDispositionActionDefinition = mock(DispositionActionDefinition.class);
        when(mockDispositionActionDefinition.getPeriod()).thenReturn(null);

        // Call the method under test.
        Date asOfDate = dispositionService.calculateAsOfDate(CONTENT_NODE_REF, mockDispositionActionDefinition);

        assertNull("It should not be possible to determine the 'as of' date.", asOfDate);
    }
}
