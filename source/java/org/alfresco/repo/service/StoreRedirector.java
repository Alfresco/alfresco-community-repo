package org.alfresco.repo.service;

import java.util.Collection;

import org.alfresco.service.cmr.repository.StoreRef;

public interface StoreRedirector
{
    /**
     * @return the names of the protocols supported
     */
    public Collection<String> getSupportedStoreProtocols();
    
    /**
     * @return the Store Refs of the stores supported
     */
    public Collection<StoreRef> getSupportedStores();
}
