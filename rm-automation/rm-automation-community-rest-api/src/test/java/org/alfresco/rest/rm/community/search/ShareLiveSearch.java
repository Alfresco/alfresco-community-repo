/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2017 Alfresco Software Limited
 * %%
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
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

public class ShareLiveSearch extends BaseRMRestTest
{
    @Autowired
    SearchAPI searchApi;

    /**
     * Given the RM site has been created When I search for "vital" Then the "Vital Records Due for Review" search
     * object should not appear as a link in the quick search results drop down
     */
    @Test
    @AlfrescoTest(jira = "RM-5882")
    public void liveSearchForVitalWord() throws Exception
    {
        createRMSiteIfNotExists();
        List<String> results = searchApi.liveSearchForDocumentsAsUser(getAdminUser().getUsername(), getAdminUser().getPassword(), "vital");
        assertTrue(results.isEmpty() || !results.stream().anyMatch("Vital Records due for Review"::equalsIgnoreCase),
                    "Share Live Search should return 0 results when searching for RM Saved Search filter words, but it returned:"
                                + Arrays.toString(results.toArray()));
    }
}
