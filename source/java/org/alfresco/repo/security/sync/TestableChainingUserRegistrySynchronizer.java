package org.alfresco.repo.security.sync;

import org.alfresco.repo.security.authentication.AuthenticationException;

public interface TestableChainingUserRegistrySynchronizer
{
    /**
     * runs read only diagnostic tests upon the specified user directory, does not actually do any synchronization
     * 
     * @param authenticatorName, name of the user directory to test
     * @return diagnostic information @see org.alfresco.repo.security.sync.SynchronizeDiagnostic
     * @throws AuthenticationException
     */
    public SynchronizeDiagnostic testSynchronize(String authenticatorName);
  
}
