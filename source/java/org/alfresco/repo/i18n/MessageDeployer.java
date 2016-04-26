package org.alfresco.repo.i18n;


/**
 * Message Deployer interface.
 * <p>
 * This interface allows the MessageService to be re-initialised, if for example the internal caches are invalidated in a cluster.
 * Message Deployer components will register with the MessageService.
 *
 */

public interface MessageDeployer
{        
    // callback for re-initialising the message caches (from the repository)
    public void initMessages();
}
