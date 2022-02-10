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

import static org.alfresco.module.org_alfresco_module_rm.test.util.WebScriptExceptionMatcher.badRequest;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.junit.Test;
import org.springframework.extensions.webscripts.WebScriptException;

/**
 * Base hold web script with content unit test.
 *
 * @author Roy Wetherall
 * @since 2.2
 */
public abstract class BaseHoldWebScriptWithContentUnitTest extends BaseHoldWebScriptUnitTest
{
    /**
     * Helper method to build JSON content to send to hold webscripts.
     */
    protected String buildContent(List<NodeRef> nodeRefs, List<NodeRef> holds)
    {
        StringBuilder builder = new StringBuilder(255);
        builder.append("{");

        if (nodeRefs != null)
        {
            builder.append("'nodeRefs':[");

            boolean bFirst = true;
            for (NodeRef nodeRef : nodeRefs)
            {
                if (!bFirst)
                {
                    builder.append(",");
                }
                else
                {
                    bFirst = false;
                }

                builder.append("'" + nodeRef.toString() + "'");
            }

            builder.append("]");
        }

        if (nodeRefs != null && holds != null)
        {
            builder.append(",");
        }

        if (holds != null)
        {
            builder.append("'holds':[");

            boolean bFirst = true;
            for (NodeRef hold : holds)
            {
                if (!bFirst)
                {
                    builder.append(",");
                }
                else
                {
                    bFirst = false;
                }

                builder.append("'" + hold.toString() + "'");
            }

            builder.append("]");
        }

        builder.append("}");

        return builder.toString();
    }

    /**
     * Test for expected exception when invalid JSON sent
     */
    @SuppressWarnings("unchecked")
    @Test
    public void sendInvalidJSON() throws Exception
    {
        // invalid JSON
        String content = "invalid JSON";

        // expected exception
        exception.expect(WebScriptException.class);
        exception.expect(badRequest());

        // execute web script
        executeWebScript(Collections.EMPTY_MAP, content);
    }

    /**
     * Test for expected exception when one of the holds doesn't exist.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void holdDoesNotExist() throws Exception
    {
        // setup interactions
        when(mockedNodeService.exists(eq(hold1NodeRef))).thenReturn(false);

        // build content
        String content = buildContent(records, holds);

        // expected exception
        exception.expect(WebScriptException.class);
        exception.expect(badRequest());

        // execute web script
        executeWebScript(Collections.EMPTY_MAP, content);
    }

    /**
     * Test for expected exception when the item being added to the hold
     * does not exist.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void nodeRefDoesNotExist() throws Exception
    {
        // setup interactions
        when(mockedNodeService.exists(eq(record))).thenReturn(false);

        // build content
        String content = buildContent(records, holds);

        // expected exception
        exception.expect(WebScriptException.class);
        exception.expect(badRequest());

        // execute web script
        executeWebScript(Collections.EMPTY_MAP, content);
    }

    /**
     * Test for expected exception when hold information is missing from
     * sent JSON.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void holdMissingFromContent() throws Exception
    {
        // build content
        String content = buildContent(records, null);

        // expected exception
        exception.expect(WebScriptException.class);
        exception.expect(badRequest());

        // execute web script
        executeWebScript(Collections.EMPTY_MAP, content);
    }

    /**
     * Test for expected exception when noderef information is missing
     * from send JSON.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void nodeRefMissingFromContent() throws Exception
    {
        // build content
        String content = buildContent(null, holds);

        // expected exception
        exception.expect(WebScriptException.class);
        exception.expect(badRequest());

        // execute web script
        executeWebScript(Collections.EMPTY_MAP, content);
    }

    /**
     * Test for expected exception when adding an item to something
     * that isn't a hold.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void holdIsNotAHold() throws Exception
    {
        // build json content to send to server
        List<NodeRef> notAHold = Collections.singletonList(recordFolder);
        String content = buildContent(records, notAHold);

        // expected exception
        exception.expect(WebScriptException.class);
        exception.expect(badRequest());

        // execute web script
        executeWebScript(Collections.EMPTY_MAP, content);
    }

    /**
     * Test for expected exception when adding an item to a hold
     * that isn't a record, record folder or active content.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void nodeRefIsNotARecordOrRecordFolderOrActiveContent() throws Exception
    {
        // build json content to send to server
        String content = buildContent(filePlanComponents, holds);

        // expected exception
        exception.expect(WebScriptException.class);
        exception.expect(badRequest());

        // execute web script
        executeWebScript(Collections.EMPTY_MAP, content);
    }
}
