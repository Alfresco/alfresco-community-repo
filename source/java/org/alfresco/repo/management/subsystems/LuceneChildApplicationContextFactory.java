package org.alfresco.repo.management.subsystems;

import java.io.IOException;

/**
 * @author Andy
 *
 */
public class LuceneChildApplicationContextFactory extends ChildApplicationContextFactory
{

    /* (non-Javadoc)
     * @see org.alfresco.repo.management.subsystems.ChildApplicationContextFactory#createInitialState()
     */
    @Override
    protected PropertyBackedBeanState createInitialState() throws IOException
    {
        return new ApplicationContextState(true);
    }
    
    protected void destroy(boolean isPermanent)
    {
        super.destroy(isPermanent);
        doInit();
    }

    
}
