package org.alfresco.repo.web.scripts;

import org.springframework.extensions.webscripts.ScriptProcessor;
import org.springframework.extensions.webscripts.ScriptProcessorFactory;

/**
 * @author Kevin Roast
 */
public class RepositoryScriptProcessorFactory implements ScriptProcessorFactory
{
    private ScriptProcessor scriptProcessor;
    
    
    /**
     * @param scriptProcessor       the ScriptProcessor to set
     */
    public void setScriptProcessor(ScriptProcessor scriptProcessor)
    {
        this.scriptProcessor = scriptProcessor;
    }

    /* (non-Javadoc)
     * @see org.springframework.extensions.webscripts.ScriptProcessorFactory#newInstance()
     */
    public ScriptProcessor newInstance()
    {
        return scriptProcessor;
    }
}