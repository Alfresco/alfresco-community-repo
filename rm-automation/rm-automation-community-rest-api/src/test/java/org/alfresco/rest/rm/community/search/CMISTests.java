/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2020 Alfresco Software Limited
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
package org.alfresco.rest.rm.community.search;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import org.alfresco.dataprep.ContentActions;
import org.alfresco.rest.rm.community.base.BaseRMRestTest;
import org.alfresco.test.AlfrescoTest;
import org.apache.chemistry.opencmis.client.api.ItemIterable;
import org.apache.chemistry.opencmis.client.api.QueryResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

public class CMISTests extends BaseRMRestTest
{
    @Autowired
    private ContentActions contentActions;

    /**
     * <pre>
     * Given the RM site created
     * When I execute cmis query
     * Then I get the correct response
     * </pre>
     */
    @Test
    @AlfrescoTest (jira="MNT-19442")
    public void executeCmisQuery()
    {
        // execute the cmis query
        String cql = "SELECT cmis:name FROM cmis:document";
        ItemIterable<QueryResult> results =
                contentActions.getCMISSession(getAdminUser().getUsername(), getAdminUser().getPassword()).query(cql,
                false);

        // check the total number of items is 100 and has more items is true
        assertTrue("Has more items not true. ", results.getHasMoreItems());
        assertTrue("Total number of items is greater than 100 ", results.getTotalNumItems() > 100);
        assertEquals("Pagination supports only 100 items per page", results.getPageNumItems(), 100);
    }

}
