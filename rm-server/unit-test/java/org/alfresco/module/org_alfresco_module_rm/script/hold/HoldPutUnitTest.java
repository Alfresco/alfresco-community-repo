/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
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
package org.alfresco.module.org_alfresco_module_rm.script.hold;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Collections;

import org.json.JSONObject;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.springframework.extensions.webscripts.DeclarativeWebScript;

/**
 * Hold ReST API PUT implementation unit test.
 * 
 * @author Roy Wetherall
 * @since 2.2
 */
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
    @SuppressWarnings("unchecked")
    @Test
    public void removeRecordFromHolds() throws Exception
    {
        // build json to send to server
        String content = buildContent(record, holds);        
        
        // execute web script
        JSONObject json = executeJSONWebScript(Collections.EMPTY_MAP, content);        
        assertNotNull(json);
        
        // verify that the record was removed from holds
        verify(mockedHoldService, times(1)).removeFromHolds(holds, record);
    }
    
    /**
     * Test that a record folder can be removed from holds.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void removeRecordFolderFromHolds() throws Exception
    {
        // build json to send to server
        String content = buildContent(recordFolder, holds);        
        
        // execute web script
        JSONObject json = executeJSONWebScript(Collections.EMPTY_MAP, content);        
        assertNotNull(json);
        
        // verify that the record was removed from holds
        verify(mockedHoldService, times(1)).removeFromHolds(holds, recordFolder);       
    }
}
