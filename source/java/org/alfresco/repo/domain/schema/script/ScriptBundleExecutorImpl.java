package org.alfresco.repo.domain.schema.script;

import java.io.File;

import org.alfresco.error.AlfrescoRuntimeException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * {@link ScriptBundleExecutor} implementation. Uses the supplied {@link ScriptExecutor}
 * to invoke multiple SQL scripts in a particular directory.
 * 
 * @author Matt Ward
 * @author Derek Hulley
 */
public class ScriptBundleExecutorImpl implements ScriptBundleExecutor
{
    private ScriptExecutor scriptExecutor;
    protected Log log = LogFactory.getLog(ScriptBundleExecutorImpl.class);
    
    public ScriptBundleExecutorImpl(ScriptExecutor scriptExecutor)
    {
        this.scriptExecutor = scriptExecutor;
    }

    @Override
    public void exec(boolean logOnly, String dir, String... scripts)
    {
        for (String name : scripts)
        {
            File file = new File(dir, name);
            try
            {
                scriptExecutor.executeScriptUrl(file.getPath());
            }
            catch (Exception e)
            {
                String msg = "Unable to run SQL script: dir=" + dir + ", name=" + name;
                if (logOnly)
                {
                    log.error(msg, e);
                    // Do not run any more scripts.
                    break;
                }
                else
                {
                    // Client opted to rethrow
                    throw new AlfrescoRuntimeException(msg, e);
                }
            }
        }
    }

    @Override
    public void exec(String dir, String... scripts)
    {
        this.exec(true, dir, scripts);
    }
    
    @Override
    public void execWithPostScript(String dir, String postScript, String... scripts)
    {
        try
        {
            exec(true, dir, scripts);
        }
        finally
        {            
            // Always run the post-script.
            exec(true, dir, postScript);
        }
    }
}
