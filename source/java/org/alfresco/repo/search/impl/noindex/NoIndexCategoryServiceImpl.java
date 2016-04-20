package org.alfresco.repo.search.impl.noindex;

import java.util.Collections;
import java.util.List;

import org.alfresco.repo.search.impl.lucene.LuceneCategoryServiceImpl;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;

/**
 * @author Andy
 *
 */
public class NoIndexCategoryServiceImpl extends LuceneCategoryServiceImpl
{

    @Override
    public List<Pair<NodeRef, Integer>> getTopCategories(StoreRef storeRef, QName aspectName, int count)
    {
        return Collections.<Pair<NodeRef, Integer>>emptyList();
    }

}
