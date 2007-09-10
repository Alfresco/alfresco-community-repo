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
package org.alfresco.repo.jscript;

import java.awt.event.ActionEvent;

import javax.swing.WindowConstants;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.Kit;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.tools.debugger.Dim;
import org.mozilla.javascript.tools.debugger.ScopeProvider;
import org.mozilla.javascript.tools.debugger.SwingGui;
import org.mozilla.javascript.tools.shell.Global;


/**
 * Alfresco implementation of Rhino JavaScript debugger
 * 
 * Provides support for authenticated access to object inspection.
 * 
 * @author davidc
 */
public class AlfrescoRhinoScriptDebugger 
{
    // Logger
    private static final Log logger = LogFactory.getLog(AlfrescoRhinoScriptDebugger.class);
    
    private ContextFactory factory = null;
    private AlfrescoDim dim = null;
    private SwingGui gui = null;
    

    
    /**
     * Start the Debugger
     */
    public void start()
    {
        if (logger.isDebugEnabled())
        {
            activate();
            show();
        }
    }

    /**
     * Activate the Debugger
     */
    public synchronized void activate()
    {
        factory = ContextFactory.getGlobal();
        Global global = new Global();
        global.init(factory);
        global.setIn(System.in);
        global.setOut(System.out);
        global.setErr(System.err);        
        dim = new AlfrescoDim();
        ScopeProvider sp = IProxy.newScopeProvider((Scriptable)global);
        dim.setScopeProvider(sp);
        gui = new AlfrescoGui(dim, "Alfresco JavaScript Debugger", this);
        gui.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        gui.setExitAction((Runnable)sp);
    }

    
    /**
     * Show the debugger
     */
    public synchronized void show()
    {
        if (!isActive())
        {
            activate();
        }
        
        dim.setBreakOnExceptions(true);
        dim.setBreak();
        dim.attachTo(factory);
        gui.pack();
        gui.setSize(600, 460);
        gui.setVisible(true);
    }
    

    /**
     * Hide the Debugger
     */
    public synchronized void hide()
    {
        if (isVisible())
        {
            dim.detach();
            gui.dispose();
        }
    }
    
    /**
     * Is Debugger visible?
     * 
     * @return
     */
    public boolean isVisible()
    {
        return isActive() && gui.isVisible();
    }
    
    /**
     * Is Debugger active?
     * 
     * @return
     */
    public boolean isActive()
    {
        return gui != null;
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
    
    
    private static class AlfrescoGui extends SwingGui
    {
        private static final long serialVersionUID = 5053205080777378416L;
        private AlfrescoRhinoScriptDebugger debugger;
        
        public AlfrescoGui(Dim dim, String title, AlfrescoRhinoScriptDebugger debugger)
        {
            super(dim, title);
            this.debugger = debugger;
        }

        public void actionPerformed(ActionEvent e)
        {
            String cmd = e.getActionCommand();
            if (cmd.equals("Exit"))
            {
                debugger.hide();
            }
            else
            {
                super.actionPerformed(e);
            }
        }
    }
    
    
    public static class IProxy implements Runnable, ScopeProvider
    {
        // Constants for 'type'.
        public static final int EXIT_ACTION = 1;
        public static final int SCOPE_PROVIDER = 2;

        /**
         * The type of interface.
         */
        private final int type;

        /**
         * The scope object to expose when {@link #type} =
         * {@link #SCOPE_PROVIDER}.
         */
        private Scriptable scope;
        
        
        /**
         * Creates a new IProxy.
         */
        public IProxy(int type)
        {
            this.type = type;
        }

        /**
         * Creates a new IProxy that acts as a {@link ScopeProvider}.
         */
        public static ScopeProvider newScopeProvider(Scriptable scope)
        {
            IProxy scopeProvider = new IProxy(EXIT_ACTION);
            scopeProvider.scope = scope;
            return scopeProvider;
        }

        // ContextAction

        /**
         * Exit action.
         */
        public void run()
        {
            if (type != EXIT_ACTION)
                Kit.codeBug();
        }

        // ScopeProvider

        /**
         * Returns the scope for script evaluations.
         */
        public Scriptable getScope()
        {
            if (type != SCOPE_PROVIDER)
                Kit.codeBug();
            if (scope == null)
                Kit.codeBug();
            return scope;
        }
    }
}
