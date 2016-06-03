package org.alfresco.repo.admin;

import java.util.Collections;
import java.util.List;

import org.alfresco.service.cmr.repository.StoreRef;

/**
 * @author Andy
 *
 */
public class DummyIndexConfigurationCheckerImpl implements IndexConfigurationChecker
{

    /* (non-Javadoc)
     * @see org.alfresco.repo.admin.IndexConfigurationChecker#checkIndexConfiguration()
     */
    @Override
    public List<StoreRef> checkIndexConfiguration()
    {
        return Collections.<StoreRef>emptyList();
    }

}
