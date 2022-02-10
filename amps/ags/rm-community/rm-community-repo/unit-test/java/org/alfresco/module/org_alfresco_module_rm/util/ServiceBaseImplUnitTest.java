/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
package org.alfresco.module.org_alfresco_module_rm.util;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationContext;

/**
 * Service Base unit test.
 *
 * @author Roxana Lucanu
 * @since 2.4
 */
public class ServiceBaseImplUnitTest
{
    @InjectMocks private ServiceBaseImpl serviceBase;

    @Mock (name = "nodeService")
    private NodeService mockedNodeService;
    @Mock (name = "transactionalResourceHelper")
    private TransactionalResourceHelper mockedTransactionalResourceHelper;
    @Mock (name = "applicationContext")
    protected ApplicationContext mockedApplicationContext;
    @Mock (name = "nodeTypeUtility")
    protected NodeTypeUtility mockedNodeTypeUtility;
    @Mock private Map<Object, Object> mockedCache;

    /**
     * Test method setup
     */
    @Before
    public void before() throws Exception
    {
        MockitoAnnotations.initMocks(this);
        // setup application context
        doReturn(mockedNodeService).when(mockedApplicationContext).getBean("dbNodeService");
        
    }
    
    /**
     * Given a node that is not a record
     * When retrieving the file plan for it
     * Then never put null in cache
     */
    @Test
    public void getFilePlan()
    {
        NodeRef nodeRef = new NodeRef("test://node/");

        when(mockedNodeService.getType(nodeRef))
            .thenReturn(ContentModel.TYPE_CONTENT);
        when(mockedNodeTypeUtility.instanceOf(ContentModel.TYPE_CONTENT, RecordsManagementModel.TYPE_FILE_PLAN))
            .thenReturn(false);
        when(mockedTransactionalResourceHelper.getMap("rm.servicebase.getFilePlan"))
            .thenReturn(mockedCache);
        when(mockedCache.containsKey(nodeRef)).thenReturn(false);

        serviceBase.getFilePlan(nodeRef);

        verify(mockedCache, never()).put(nodeRef, null);
    }

}
