/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2019 Alfresco Software Limited
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
package org.alfresco.module.org_alfresco_module_rm.model.rma.aspect;

import static org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel.ASPECT_HELD_CHILDREN;
import static org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel.ASPECT_RECORD;
import static org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel.PROP_HELD_CHILDREN_COUNT;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationContext;

/**
 * Test class for frozen aspect
 * @author Ross Gale
 * @since 3.2
 */
public class FrozenAspectUnitTest
{
    @Mock
    private NodeService nodeService;

    @Mock
    private ApplicationContext applicationContext;

    @Mock
    private ChildAssociationRef childAssociationRef;

    @Mock
    private DictionaryService dictionaryService;

    @InjectMocks
    private FrozenAspect frozenAspect;

    private NodeRef record = new NodeRef("workspace://record/node");
    private NodeRef folder = new NodeRef("workspace://folder/node");
    private NodeRef content = new NodeRef("workspace://content/node");

    @Before
    public void setUp()
    {
        MockitoAnnotations.initMocks(this);
        when(nodeService.exists(record)).thenReturn(true);
        when(nodeService.exists(content)).thenReturn(true);
        when(nodeService.hasAspect(folder, ASPECT_HELD_CHILDREN)).thenReturn(true);
        when(nodeService.getProperty(folder, PROP_HELD_CHILDREN_COUNT)).thenReturn(1);
        when(applicationContext.getBean("dbNodeService")).thenReturn(nodeService);
        when(nodeService.hasAspect(folder, ASPECT_HELD_CHILDREN)).thenReturn(true);
        when(nodeService.getProperty(folder, PROP_HELD_CHILDREN_COUNT)).thenReturn(1);
    }

    /**
     * Test that the held count is reduced on a record folder after the frozen aspect is removed from a record
     */
    @Test
    public void testRemoveAspectForRecords()
    {
        when(nodeService.hasAspect(record, ASPECT_RECORD)).thenReturn(true);
        when(nodeService.getPrimaryParent(record)).thenReturn(childAssociationRef);
        when(childAssociationRef.getParentRef()).thenReturn(folder);
        frozenAspect.onRemoveAspect(record, null);
        verify(nodeService, times(1)).setProperty(folder, PROP_HELD_CHILDREN_COUNT, 0);
    }

    /**
     * Test that the held count is reduced on a folder after the frozen aspect is removed from a piece of content
     */
    @Test
    public void testRemoveAspectForContent()
    {
        when(nodeService.hasAspect(content, ASPECT_RECORD)).thenReturn(false);
        when(nodeService.getType(content)).thenReturn(ContentModel.TYPE_CONTENT);
        when(nodeService.getPrimaryParent(content)).thenReturn(childAssociationRef);
        when(childAssociationRef.getParentRef()).thenReturn(folder);
        frozenAspect.onRemoveAspect(content, null);
        verify(nodeService, times(1)).setProperty(folder, PROP_HELD_CHILDREN_COUNT, 0);
    }

    /**
     * Test that the remove code is only ran for records or active content
     */
    @Test
    public void testRemoveAspectForContentDoesntUpdateForOtherTypes()
    {
        when(nodeService.hasAspect(content, ASPECT_RECORD)).thenReturn(false);
        when(nodeService.getType(content)).thenReturn(ContentModel.TYPE_FOLDER);
        when(dictionaryService.isSubClass(ContentModel.TYPE_FOLDER, ContentModel.TYPE_CONTENT)).thenReturn(false);
        frozenAspect.onRemoveAspect(content, null);
        verify(nodeService, times(0)).setProperty(folder, PROP_HELD_CHILDREN_COUNT, 0);
    }
}
