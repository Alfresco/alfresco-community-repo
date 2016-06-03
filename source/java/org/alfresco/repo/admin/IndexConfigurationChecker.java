package org.alfresco.repo.admin;

import java.util.List;

import org.alfresco.service.cmr.repository.StoreRef;

/**
 * @author Andy
 *
 */
public interface IndexConfigurationChecker
{
    /**
     * Check that the index contains root entries for all the stores that would be expected
     * @return - the stores with missing indexes
     */
    public List<StoreRef> checkIndexConfiguration();
}
