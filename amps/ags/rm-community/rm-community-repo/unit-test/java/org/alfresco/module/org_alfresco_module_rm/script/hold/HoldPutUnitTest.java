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

package org.alfresco.module.org_alfresco_module_rm.script.hold;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.extensions.webscripts.DeclarativeWebScript;

/**
 * Hold ReST API PUT implementation unit test.
 *
 * @author Roy Wetherall
 * @since 2.2
 */
@RunWith(MockitoJUnitRunner.class)
public class HoldPutUnitTest extends BaseHoldWebScriptWithContentUnitTest
{
    /** classpath location of ftl template for web script */
    private static final String WEBSCRIPT_TEMPLATE = WEBSCRIPT_ROOT_RM + "hold.put.json.ftl";

    /** HoldPut webscript instance */
    protected @Spy @InjectMocks HoldPut webScript;

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.test.util.BaseWebScriptUnitTest#getWebScript()
     */
    @Override
    protected DeclarativeWebScript getWebScript()
    {
        return webScript;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.test.util.BaseWebScriptUnitTest#getWebScriptTemplate()
     */
    @Override
    protected String getWebScriptTemplate()
    {
        return WEBSCRIPT_TEMPLATE;
    }

    /**
     * Test that a record can be removed from holds.
     */

    @Before
    public void setUp(){
        webScript.setHoldService(mockedHoldService);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void removeRecordFromHolds() throws Exception
    {
        // build json to send to server
        String content = buildContent(records, holds);

        // execute web script
        JSONObject json = executeJSONWebScript(Collections.EMPTY_MAP, content);
        assertNotNull(json);

        // verify that the record was removed from holds
        verify(mockedHoldService, times(1)).removeFromHolds(holds, records);
    }

    /**
     * Test that a record folder can be removed from holds.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void removeRecordFolderFromHolds() throws Exception
    {
        // build json to send to server
        String content = buildContent(recordFolders, holds);

        // execute web script
        JSONObject json = executeJSONWebScript(Collections.EMPTY_MAP, content);
        assertNotNull(json);

        // verify that the record was removed from holds
        verify(mockedHoldService, times(1)).removeFromHolds(holds, recordFolders);
    }

    /**
     * Test that active content can be removed from holds.
     */
    @Test
    public void removeActiveContentFromHolds() throws Exception
    {
        // build json to send to server
        String content = buildContent(activeContents, holds);

        // execute web script
        JSONObject json = executeJSONWebScript(Collections.EMPTY_MAP, content);
        assertNotNull(json);

        // verify that the active content was removed from holds
        verify(mockedHoldService, times(1)).removeFromHolds(holds, activeContents);
    }

    /**
     * Test that active content can be removed from holds along with records and record folders.
     */
    @Test
    public void removeActiveContentAndRecordsAndRecordFoldersToHolds() throws Exception
    {
        List<NodeRef> items = new ArrayList<>(3);
        Collections.addAll(items, dmNodeRef, record, recordFolder);
        // build json to send to server
        String content = buildContent(items, holds);

        // execute web script
        JSONObject json = executeJSONWebScript(Collections.EMPTY_MAP, content);
        assertNotNull(json);

        // verify that the active content was removed from holds along with records and record folders
        verify(mockedHoldService, times(1)).removeFromHolds(holds, items);
    }
}
