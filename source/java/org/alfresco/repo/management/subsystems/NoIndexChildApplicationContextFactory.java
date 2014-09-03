package org.alfresco.repo.management.subsystems;

public class NoIndexChildApplicationContextFactory extends ChildApplicationContextFactory
{
    protected void destroy(boolean isPermanent)
    {
        super.destroy(isPermanent);
        doInit();
    }
}
