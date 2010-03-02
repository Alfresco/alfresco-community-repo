/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
