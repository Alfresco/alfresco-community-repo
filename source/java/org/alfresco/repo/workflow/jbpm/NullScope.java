
package org.alfresco.repo.workflow.jbpm;

import org.mozilla.javascript.NativeObject;

/**
 * @since 3.4
 * @author Nick Smith
 *
 */
public class NullScope extends NativeObject
{
    private static final long serialVersionUID = 423800883354854893L;

    private static final NullScope INSTANCE = new NullScope();
    
    public static NullScope instance()
    {
        return INSTANCE;
    }
    
    @Override public Object getDefaultValue(@SuppressWarnings("rawtypes") Class hint)
    {
        return INSTANCE.toString();
    }
}
