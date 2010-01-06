/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
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
package org.alfresco.repo.web.scripts;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.tools.debugger.Dim;
import org.mozilla.javascript.tools.debugger.SwingGui;
import org.springframework.extensions.webscripts.ScriptDebugger;


/**
 * Alfresco implementation of Rhino JavaScript debugger
 * 
 * Provides support for authenticated access to object inspection.
 * 
 * @author davidc
 */
public class AlfrescoRhinoScriptDebugger extends ScriptDebugger
{
    private static final Log logger = LogFactory.getLog(AlfrescoRhinoScriptDebugger.class);
    
    // Logger
    private ContextFactory factory = null;
    private SwingGui gui = null;
    
    
    @Override
    protected void initDebugger()
    {
        dim = new AlfrescoDim();
    }
    
    @Override
    public void start()
    {
        if (logger.isDebugEnabled())
        {
            activate();
            show();
        }
    }
    
    @Override
    protected String getTitle()
    {
        return "Alfresco Repository JavaScript Debugger";
    }


    public static class AlfrescoDim extends Dim
    {
        /* (non-Javadoc)
         * @see org.mozilla.javascript.tools.debugger.Dim#objectToString(java.lang.Object)
         */
        @Override
        public String objectToString(final Object arg0)
        {
            // execute command in context of currently selected user
            return AuthenticationUtil.runAs(new RunAsWork<String>()
            {
                @SuppressWarnings("synthetic-access")
                public String doWork() throws Exception
                {
                    return AlfrescoDim.super.objectToString(arg0);
                }
            }, AuthenticationUtil.getSystemUserName());
        }
    }
}
