/*
 * Copyright (C) 2005-2008 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.forms;

import org.alfresco.util.BaseAlfrescoSpringTest;

/**
 * Form service implementation unit test.
 * 
 * @author Gavin Cornwell
 */
public class FormServiceImplTest extends BaseAlfrescoSpringTest 
{
    private FormService formService;
    
    /**
     * Called during the transaction setup
     */
    protected void onSetUpInTransaction() throws Exception
    {
        super.onSetUpInTransaction();
        
        // Get the required services
        this.formService = (FormService)this.applicationContext.getBean("FormService");
    }
	
    public void testGetForm() throws Exception
    {
        Form form = this.formService.getForm("");
        assertNull(form);
    }
    
    // == Test the JavaScript API ==
    
//    public void testJSAPI() throws Exception
//    {
//        ScriptLocation location = new ClasspathScriptLocation("org/alfresco/repo/forms/script/test_formService.js");
//        this.scriptService.executeScript(location, new HashMap<String, Object>(0));
//    }
}
