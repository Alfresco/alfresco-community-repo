package org.alfresco.repo.node;

import java.util.Set;

import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;

/**
 * GetNodesWithAspectCannedQuery CQ parameters - for query context and filtering
 *
 * @author Nick Burch
 * @since 4.1
 */
public class GetNodesWithAspectCannedQueryParams
{
    private StoreRef storeRef;
    
    private Set<QName> aspectQNames = null;
    
    public GetNodesWithAspectCannedQueryParams(
            StoreRef storeRef,
            Set<QName> aspectQNames)
    {
        this.storeRef = storeRef;

        if (aspectQNames != null && !aspectQNames.isEmpty()) 
        { 
            this.aspectQNames = aspectQNames; 
        }
        else
        {
            throw new IllegalArgumentException("At least one Aspect must be given");
        }
    }
    
    public StoreRef getStoreRef()
    {
        return storeRef;
    }
    
    public Set<QName> getAspectQNames()
    {
        return aspectQNames;
    }
}
