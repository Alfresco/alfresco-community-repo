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
