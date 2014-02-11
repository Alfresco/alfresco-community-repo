/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.util.debug;

import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.test_category.OwnJVMTestsCategory;
import org.alfresco.util.BaseSpringTest;
import org.junit.experimental.categories.Category;

/**
 * @author Roy Wetherall
 */
@Category(OwnJVMTestsCategory.class)
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
