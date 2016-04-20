
package org.alfresco.repo.rendition;

import org.alfresco.repo.action.ActionDefinitionImpl;
import org.alfresco.service.cmr.rendition.RenderingEngineDefinition;

/**
 * @author Nick Smith
 * @since 3.3
 */
public class RenderingEngineDefinitionImpl extends ActionDefinitionImpl implements RenderingEngineDefinition
{
    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;

    public RenderingEngineDefinitionImpl(String name)
    {
        super(name);
    }
}
