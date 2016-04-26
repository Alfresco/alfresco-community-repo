package org.alfresco.repo.admin.patch.impl;

import org.springframework.extensions.surf.util.I18NUtil;
import org.alfresco.repo.admin.patch.AbstractPatch;

/**
 * Does nothing.
 * 
 * @author Derek Hulley
 * @since 2.2.2
 */
public class NoOpPatch extends AbstractPatch
{
    private static final String MSG_RESULT = "patch.noOpPatch.result";
    
    public NoOpPatch()
    {
    }
    
    @Override
    protected String applyInternal() throws Exception
    {
        return I18NUtil.getMessage(MSG_RESULT);
    }
}
