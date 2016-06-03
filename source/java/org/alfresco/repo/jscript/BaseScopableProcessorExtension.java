package org.alfresco.repo.jscript;

import org.alfresco.api.AlfrescoPublicApi;     
import org.alfresco.repo.processor.BaseProcessorExtension;
import org.mozilla.javascript.Scriptable;

/**
 * Abstract base class for a script implementation that requires a script execution scope.
 * 
 * The scope is local to the currently executing script and therefore a ThreadLocal is required.
 * 
 * @author Kevin Roast
 */
@AlfrescoPublicApi
public class BaseScopableProcessorExtension extends BaseProcessorExtension implements Scopeable
{
    private static ThreadLocal<Scriptable> scope = new ThreadLocal<Scriptable>();
    
    /**
     * Set the Scriptable global scope
     * 
     * @param scope relative global scope
     */
    public void setScope(Scriptable scope)
    {
        BaseScopableProcessorExtension.scope.set(scope);
    }
    
    /**
     * @return script global scope
     */
    public Scriptable getScope()
    {
        return BaseScopableProcessorExtension.scope.get();
    }
}
