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

package org.alfresco.rest.rm.community.search;

import static org.testng.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.alfresco.rest.rm.community.base.BaseRMRestTest;
import org.alfresco.rest.v0.SearchAPI;
import org.alfresco.test.AlfrescoTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

public class ShareLiveSearchTests extends BaseRMRestTest
{
    @Autowired
    SearchAPI searchApi;

    /**
     * Given the RM site has been created When I search for "vital" Then the "Vital Records Due for Review" search
     * object should not appear as a link in the quick search results drop down
     */
    @Test
    @AlfrescoTest(jira = "RM-5882")
    public void liveSearchForVitalWord()
    {
        List<String> results = searchApi.liveSearchForDocumentsAsUser(getAdminUser().getUsername(), getAdminUser().getPassword(), "vital");
        assertTrue(results.isEmpty() || results.stream().noneMatch("Vital Records due for Review"::equalsIgnoreCase),
                    "Share Live Search should return 0 results when searching for RM Saved Search filter words, but it returned:"
                                + Arrays.toString(results.toArray()));
    }
}
