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

import static org.alfresco.model.ContentModel.ASSOC_CHILDREN;
import static org.alfresco.model.ContentModel.PROP_NAME;
import static org.alfresco.module.org_alfresco_module_rm.script.slingshot.ClassificationReasonsUtil.CLASSIFICATION_REASONS_CONTAINER;
import static org.alfresco.module.org_alfresco_module_rm.script.slingshot.ClassificationReasonsUtil.PROP_CLASSIFICATION_REASON_CODE;
import static org.alfresco.service.cmr.repository.StoreRef.STORE_REF_WORKSPACE_SPACESSTORE;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * @author Ross Gale
 * @since 2.7
 */
public class ClassificationReasonsUtilUnitTest
{

    @Mock
    private NodeService nodeService;

    @Mock
    private ChildAssociationRef childAssociationRef;

    @Mock
    private ChildAssociationRef reason;

    @Mock
    private Map<QName, Serializable> properties;

    @InjectMocks
    private ClassificationReasonsUtil classificationReasonsUtil;

    private NodeRef childNodeRef;

    @Before
    public void setUp()
    {
        MockitoAnnotations.initMocks(this);
        NodeRef rootNodeRef = new NodeRef("workspace://SpacesStore/rootNodeRef");
        NodeRef containerNodeRef = new NodeRef("workspace://SpacesStore/containerNodeRef");
        childNodeRef = new NodeRef("workspace://SpacesStore/childNodeRef");
        List<ChildAssociationRef> assocRefs = new ArrayList<>();
        List<ChildAssociationRef> childAssocRefs = new ArrayList<>();
        assocRefs.add(childAssociationRef);
        childAssocRefs.add(reason);
        when(reason.getChildRef()).thenReturn(childNodeRef);
        when(nodeService.getRootNode(STORE_REF_WORKSPACE_SPACESSTORE)).thenReturn(rootNodeRef);
        when(nodeService.getChildAssocs(rootNodeRef, ASSOC_CHILDREN, CLASSIFICATION_REASONS_CONTAINER)).thenReturn(assocRefs);
        when(childAssociationRef.getChildRef()).thenReturn(containerNodeRef);
        when(nodeService.getChildAssocs(containerNodeRef)).thenReturn(childAssocRefs);
    }

    /**
     * Check no modifications are made to non matching parts of the query string
     */
    @Test
    public void testNoChangeMadeToStringIfKeyNotFound()
    {
        String stringToTest = "noChangeMadeToString";
        assertEquals("Change made to string",stringToTest, classificationReasonsUtil.replaceReasonWithNodeRef(stringToTest).trim());
    }

    /**
     * Check no modifications made if the plain text parameter doesn't have a stored match
     */
    @Test
    public void testNoChangeMadeToStringIfMatchNotFound()
    {
        when(nodeService.getProperties(childNodeRef)).thenReturn(properties);
        when(properties.get(PROP_CLASSIFICATION_REASON_CODE)).thenReturn("not a match!");
        String stringToTest = "clf:classificationReasons:noChangeMadeToString";
        assertEquals("Change made to string", stringToTest, classificationReasonsUtil.replaceReasonWithNodeRef(stringToTest).trim());
    }

    /**
     * Check the query is updated correctly when a match is found
     */
    @Test
    public void testChangeMadeToStringIfMatchFound()
    {
        when(nodeService.getProperties(childNodeRef)).thenReturn(properties);
        when(properties.get(PROP_CLASSIFICATION_REASON_CODE)).thenReturn("stringToChange");
        when(properties.get(PROP_NAME)).thenReturn("newString");
        String stringToTest = "clf:classificationReasons:stringToChange";
        assertEquals("No change made to string", "clf:classificationReasons:newString", classificationReasonsUtil.replaceReasonWithNodeRef(stringToTest).trim());
    }
}
