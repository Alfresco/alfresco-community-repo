package org.alfresco.filesys;

import org.alfresco.jlan.server.SessionListener;
import org.alfresco.jlan.server.SrvSession;

/**
 * A benign implementation of the SessionListener interface. Allows the authentication subsystems to share a uniform
 * interface. Those without a need for a live session listeners can use this bean.
 * 
 * @author dward
 */
public class NullSessionListener implements SessionListener
{

    public void sessionClosed(SrvSession sess)
    {
    }

    public void sessionCreated(SrvSession sess)
    {
    }

    public void sessionLoggedOn(SrvSession sess)
    {
    }

}
