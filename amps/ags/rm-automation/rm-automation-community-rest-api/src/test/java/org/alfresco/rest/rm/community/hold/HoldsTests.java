/*-
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2026 Alfresco Software Limited
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
package org.alfresco.rest.rm.community.hold;

import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentAlias.FILE_PLAN_ALIAS;
import static org.alfresco.utility.data.RandomData.getRandomAlphanumeric;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.rest.rm.community.base.BaseRMRestTest;
import org.alfresco.rest.rm.community.model.hold.Hold;
import org.alfresco.rest.rm.community.model.hold.HoldDeletionReason;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

/**
 * This class contains the tests for the Holds CRUD V1 API
 *
 * @author Damian Ujma
 */
public class HoldsTests extends BaseRMRestTest
{

    private final List<String> nodeRefs = new ArrayList<>();

    @Test
    public void testGetHold()
    {
        String holdName = "Hold" + getRandomAlphanumeric();
        String holdDescription = "Description" + getRandomAlphanumeric();
        String holdReason = "Reason" + getRandomAlphanumeric();

        // Create the hold
        Hold hold = Hold.builder()
            .name(holdName)
            .description(holdDescription)
            .reason(holdReason)
            .build();
        Hold createdHold = getRestAPIFactory().getFilePlansAPI()
            .createHold(hold, FILE_PLAN_ALIAS);

        // Get the hold
        Hold receivedHold = getRestAPIFactory().getHoldsAPI().getHold(createdHold.getId());
        nodeRefs.add(receivedHold.getId());

        // Verify the status code
        assertStatusCode(OK);

        assertEquals(receivedHold.getName(), holdName);
        assertEquals(receivedHold.getDescription(), holdDescription);
        assertEquals(receivedHold.getReason(), holdReason);
        assertNotNull(receivedHold.getId());
    }

    @Test
    public void testUpdateHold()
    {
        String holdName = "Hold" + getRandomAlphanumeric();
        String holdDescription = "Description" + getRandomAlphanumeric();
        String holdReason = "Reason" + getRandomAlphanumeric();

        // Create the hold
        Hold hold = Hold.builder()
            .name(holdName)
            .description(holdDescription)
            .reason(holdReason)
            .build();
        Hold createdHold = getRestAPIFactory().getFilePlansAPI()
            .createHold(hold, FILE_PLAN_ALIAS);
        nodeRefs.add(createdHold.getId());

        Hold holdModel = Hold.builder()
            .name("Updated" + holdName)
            .description("Updated" + holdDescription)
            .reason("Updated" + holdReason)
            .build();

        // Update the hold
        Hold updatedHold = getRestAPIFactory().getHoldsAPI().updateHold(holdModel, createdHold.getId());

        // Verify the status code
        assertStatusCode(OK);

        assertEquals(updatedHold.getName(), "Updated" + holdName);
        assertEquals(updatedHold.getDescription(), "Updated" + holdDescription);
        assertEquals(updatedHold.getReason(), "Updated" + holdReason);
        assertNotNull(updatedHold.getId());
    }

    @Test
    public void testDeleteHold()
    {
        String holdName = "Hold" + getRandomAlphanumeric();
        String holdDescription = "Description" + getRandomAlphanumeric();
        String holdReason = "Reason" + getRandomAlphanumeric();

        // Create the hold
        Hold hold = Hold.builder()
            .name(holdName)
            .description(holdDescription)
            .reason(holdReason)
            .build();
        Hold createdHold = getRestAPIFactory().getFilePlansAPI()
            .createHold(hold, FILE_PLAN_ALIAS);
        nodeRefs.add(createdHold.getId());

        // Delete the hold
        getRestAPIFactory().getHoldsAPI().deleteHold(createdHold.getId());

        // Verify the status code
        assertStatusCode(NO_CONTENT);

        // Try to get the hold
        getRestAPIFactory().getHoldsAPI().getHold(createdHold.getId());

        // Verify the status code
        assertStatusCode(NOT_FOUND);
    }

    @Test
    public void testDeleteHoldWithReason()
    {
        String holdName = "Hold" + getRandomAlphanumeric();
        String holdDescription = "Description" + getRandomAlphanumeric();
        String holdReason = "Reason" + getRandomAlphanumeric();

        // Create the hold
        Hold hold = Hold.builder()
            .name(holdName)
            .description(holdDescription)
            .reason(holdReason)
            .build();
        Hold createdHold = getRestAPIFactory().getFilePlansAPI()
            .createHold(hold, FILE_PLAN_ALIAS);
        nodeRefs.add(createdHold.getId());

        // Delete the hold with the reason
        getRestAPIFactory().getHoldsAPI()
            .deleteHoldWithReason(HoldDeletionReason.builder().reason("Example reason").build(), createdHold.getId());

        // Verify the status code
        assertStatusCode(OK);

        // Try to get the hold
        getRestAPIFactory().getHoldsAPI().getHold(createdHold.getId());

        // Verify the status code
        assertStatusCode(NOT_FOUND);
    }

    @AfterClass(alwaysRun = true)
    public void cleanUpHoldsTests()
    {
        nodeRefs.forEach(nodeRef -> getRestAPIFactory().getHoldsAPI(getAdminUser()).deleteHold(nodeRef));
    }
}
