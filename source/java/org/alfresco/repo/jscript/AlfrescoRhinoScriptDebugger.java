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
public class AlfrescoRhinoScriptDebugger extends Dim 
{
    // Logger
    private static final Log logger = LogFactory.getLog(AlfrescoRhinoScriptDebugger.class);

    private boolean active = false;
    private boolean visible = false;
    private ContextFactory factory = null; 
    private Global global = null; 
    private AlfrescoRhinoScriptDebugger dim = null;
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
        global = new Global();
        global.init(factory);
        dim = new AlfrescoRhinoScriptDebugger();
        dim.setScopeProvider(IProxy.newScopeProvider((Scriptable)global));
        gui = new SwingGui(dim, "Alfresco JavaScript Debugger");
        gui.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        active = true;
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
        dim.setBreakOnEnter(true);
        dim.setBreakOnReturn(true);
        dim.attachTo(factory);
        gui.pack();
        gui.setSize(600, 460);
        gui.setVisible(true);
        visible = true;
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
            visible = false;
        }
    }
    
    /**
     * Is Debugger visible?
     * 
     * @return
     */
    public boolean isVisible()
    {
        return visible;
    }
    
    /**
     * Is Debugger active?
     * 
     * @return
     */
    public boolean isActive()
    {
        return active;
    }
    

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
                return AlfrescoRhinoScriptDebugger.super.objectToString(arg0);
            }
        }, AuthenticationUtil.getSystemUserName());
    }

    
    /**
     * Class to consolidate all internal implementations of interfaces to avoid
     * class generation bloat.
     */
    private static class IProxy implements Runnable, ScopeProvider
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
            IProxy scopeProvider = new IProxy(SCOPE_PROVIDER);
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
