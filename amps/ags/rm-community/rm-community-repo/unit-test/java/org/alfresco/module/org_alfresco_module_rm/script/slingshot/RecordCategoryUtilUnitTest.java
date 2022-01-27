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

package org.alfresco.module.org_alfresco_module_rm.script.slingshot;

import static org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel.PROP_IDENTIFIER;
import static org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel.TYPE_NON_ELECTRONIC_DOCUMENT;
import static org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel.TYPE_RECORD_CATEGORY;
import static org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel.TYPE_RECORD_FOLDER;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.repository.Path.ChildAssocElement;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Tests for methods in the RecordsCategoryUtil class
 * 
 * @author Ross Gale
 * @since 2.7
 */
public class RecordCategoryUtilUnitTest
{
    @Mock
    private NodeService nodeService;

    @Mock
    private ChildAssocElement element;

    @Mock
    private ChildAssociationRef childAssociationRef;

    @InjectMocks
    private RecordCategoryUtil recordCategoryUtil;

    private Path path;

    private NodeRef recordNodeRef;

    private NodeRef recordFolderNodeRef;

    private NodeRef categoryNodeRef;

    @Before
    public void setUp()
    {
        MockitoAnnotations.initMocks(this);
        recordNodeRef = new NodeRef("test://recordNode/");
        recordFolderNodeRef = new NodeRef("test://recordFolderNode/");
        categoryNodeRef = new NodeRef("test://categoryNode/");
        path = new Path();
        path.append(element);
        when(nodeService.getType(recordFolderNodeRef)).thenReturn(TYPE_RECORD_FOLDER);
        when(nodeService.getType(recordNodeRef)).thenReturn(TYPE_NON_ELECTRONIC_DOCUMENT);
        when(nodeService.getPath(recordNodeRef)).thenReturn(path);
        when(nodeService.getPath(recordFolderNodeRef)).thenReturn(path);
        when(element.getRef()).thenReturn(childAssociationRef);
        when(childAssociationRef.getChildRef()).thenReturn(categoryNodeRef);
        when(nodeService.getType(categoryNodeRef)).thenReturn(TYPE_RECORD_CATEGORY);
        when(nodeService.getProperty(categoryNodeRef, PROP_IDENTIFIER)).thenReturn("RecordCategoryId");
    }

    /**
     * Tests an id is returned from a valid node ref
     */
    @Test
    public void testGetIdFromNodeRef()
    {
        assertEquals("RecordCategoryId",recordCategoryUtil.getCategoryIdFromNodeId(recordNodeRef,false));
    }

    /**
     * Tests an id can be returned for a non record with the correct option selected
     */
    @Test
    public void testGetIdFromNodeRefReturnsForNonRecordWhenOptionSelected()
    {
        assertEquals("RecordCategoryId", recordCategoryUtil.getCategoryIdFromNodeId(recordFolderNodeRef, true));
    }

    /**
     * Tests no id is returned for a folder if option isn't selected
     */
    @Test
    public void testGetIdFromNodeRefReturnsNullForNonRecordWhenOptionSelected()
    {
        assertNull(recordCategoryUtil.getCategoryIdFromNodeId(recordFolderNodeRef,false));
    }

    /**
     * Tests no id is returned when a categories isn't found on the path
     */
    @Test
    public void testGetIdFromNodeRefReturnsNullWithNoCategory()
    {
        when(nodeService.getPath(recordNodeRef)).thenReturn(new Path());
        assertNull(recordCategoryUtil.getCategoryIdFromNodeId(recordNodeRef, false));
    }
}
