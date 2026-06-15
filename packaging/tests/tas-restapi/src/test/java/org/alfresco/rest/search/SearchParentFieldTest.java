/*
 * #%L
 * Alfresco Search Services E2E Test
 * %%
 * Copyright (C) 2005 - 2020 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail. Otherwise, the software is
 * provided under the following open source license terms:
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.rest.search;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import org.alfresco.utility.data.DataGroup;
import org.alfresco.utility.model.GroupModel;

/**
 * Test class tests PARENT field is including all the PARENT Nodes Created for SEARCH-2378
 */
public class SearchParentFieldTest extends AbstractSearchServicesE2ETest
{
    @Autowired
    protected DataGroup dataGroup;

    List<GroupModel> groups;

    @BeforeClass(alwaysRun = true)
    public void dataPreparation()
    {

        groups = new ArrayList<>();
        groups.add(dataGroup.createRandomGroup());
        groups.add(dataGroup.createRandomGroup());

        dataGroup.addListOfUsersToGroup(groups.get(0), testUser);
        dataGroup.addListOfUsersToGroup(groups.get(1), testUser);

        waitForIndexing(
                "TYPE:'cm:authorityContainer' AND cm:authorityName:'GROUP_" + groups.get(1).getGroupIdentifier() + "'",
                true);

    }

    /**
     * Test users in groups can be found using PARENT expressions.
     */
    @Test(priority = 1)
    public void testSearchParentForPerson() throws Exception
    {

        for (GroupModel group : groups)
        {

            // Find groupId to be used in the PARENT expression
            String queryGroup = "TYPE:'cm:authorityContainer' AND cm:authorityName:'GROUP_" + group.getGroupIdentifier()
                    + "'";
            SearchResponse response = queryAsUser(dataUser.getAdminUser(), queryGroup);
            String groupId = response.getEntries().getFirst().getModel().getId();

            // Find the user assigned as member of this group with PARENT clause
            String queryParentGroup = "(TYPE:'cm:person' OR TYPE:'cm:authorityContainer') AND PARENT:'workspace://SpacesStore/"
                    + groupId + "'";

            response = queryAsUser(dataUser.getAdminUser(), queryParentGroup);
            restClient.assertStatusCodeIs(HttpStatus.OK);
            Assert.assertEquals(response.getPagination().getCount(), 1, "Expecting 1 user (" + testUser.getUsername()
                    + ") as member of this group (" + group.getGroupIdentifier() + ")");

        }

    }

}
