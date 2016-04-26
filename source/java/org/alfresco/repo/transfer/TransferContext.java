package org.alfresco.repo.transfer;

import java.util.HashMap;
import java.util.Map;

import org.alfresco.repo.transfer.manifest.ManifestCategory;
import org.alfresco.service.cmr.repository.NodeRef;

public class TransferContext
{

    private Map<NodeRef, ManifestCategory> categoriesCache = new HashMap<NodeRef, ManifestCategory>();
    /**
      * 
      * @return Map
      */
    public Map<NodeRef, ManifestCategory> getManifestCategoriesCache()
    {
      	return this.categoriesCache;
    }
}
