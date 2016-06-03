package org.alfresco.util.debug;

import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.test_category.BaseSpringTestsCategory;
import org.alfresco.util.BaseSpringTest;
import org.junit.experimental.categories.Category;

/**
 * @author Roy Wetherall
 */
@Category(BaseSpringTestsCategory.class)
public class OutputSpacesStoreSystemTest extends BaseSpringTest
{
    /**
     * Dump the contents of the spaces store to standard out
     */
    public void testDumpSpacesStore()
    {
        NodeService nodeService = (NodeService)this.applicationContext.getBean("nodeService");
        StoreRef spacesStore = new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore");
        System.out.println(NodeStoreInspector.dumpNodeStore(nodeService, spacesStore));
    }    
}
