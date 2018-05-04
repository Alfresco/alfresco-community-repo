/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2018 Alfresco Software Limited
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
import static org.alfresco.model.ContentModel.PROP_NODE_UUID;
import static org.alfresco.module.org_alfresco_module_rm.script.slingshot.ClassificationReasonsUtil.PROP_CLASSIFICATION_REASON_CODE;
import static org.alfresco.module.org_alfresco_module_rm.script.slingshot.ClassificationSourcesUtil.CLASSIFICATION_SOURCES_CONTAINER;
import static org.alfresco.module.org_alfresco_module_rm.script.slingshot.ClassificationSourcesUtil.PROP_CLASSIFICATION_SOURCE_NAME;
import static org.alfresco.module.org_alfresco_module_rm.script.slingshot.ClassificationSourcesUtil.SOURCES_KEY;
import static org.alfresco.service.cmr.repository.StoreRef.STORE_REF_WORKSPACE_SPACESSTORE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
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
public class ClassificationSourcesUtilUnitTest
{

    @Mock
    private NodeService nodeService;

    @Mock
    private ChildAssociationRef childAssociationRef;

    @Mock
    private ChildAssociationRef source, secondSource;

    @Mock
    private Map<QName, Serializable> properties;

    @Mock
    private Map<QName, Serializable> secondSetOfProperties;

    @InjectMocks
    private ClassificationSourcesUtil classificationSourcesUtil;

    private List<ChildAssociationRef> childAssocRefs;

    private NodeRef childNodeRef;

    private NodeRef childNodeRef2;

    @Before
    public void setUp()
    {
        MockitoAnnotations.initMocks(this);
        NodeRef rootNodeRef = new NodeRef("workspace://SpacesStore/rootNodeRef");
        NodeRef containerNodeRef = new NodeRef("workspace://SpacesStore/containerNodeRef");
        childNodeRef = new NodeRef("workspace://SpacesStore/childNodeRef");
        childNodeRef2 = new NodeRef("workspace://SpacesStore/childNodeRef2");
        List<ChildAssociationRef> assocRefs = new ArrayList<>();
        childAssocRefs = new ArrayList<>();
        assocRefs.add(childAssociationRef);
        childAssocRefs.add(source);
        when(source.getChildRef()).thenReturn(childNodeRef);
        when(nodeService.getRootNode(STORE_REF_WORKSPACE_SPACESSTORE)).thenReturn(rootNodeRef);
        when(nodeService.getChildAssocs(rootNodeRef, ASSOC_CHILDREN, CLASSIFICATION_SOURCES_CONTAINER)).thenReturn(assocRefs);
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
        assertEquals("Change made to string",stringToTest, classificationSourcesUtil.replaceSourceNameWithNodeRef(stringToTest));
    }

    /**
     * Check no modifications made if the plain text parameter doesn't have a stored match
     */
    @Test
    public void testNoChangeMadeToStringIfMatchNotFound()
    {
        when(nodeService.getProperties(childNodeRef)).thenReturn(properties);
        when(properties.get(PROP_CLASSIFICATION_REASON_CODE)).thenReturn("not a match!");
        String stringToTest = SOURCES_KEY + "noChangeMadeToString";
        assertEquals("Change made to string", stringToTest, classificationSourcesUtil.replaceSourceNameWithNodeRef(stringToTest));
    }

    /**
     * Check the query is updated correctly when a match is found
     */
    @Test
    public void testChangeMadeToStringIfMatchFound()
    {
        when(nodeService.getProperties(childNodeRef)).thenReturn(properties);
        when(properties.get(PROP_CLASSIFICATION_SOURCE_NAME)).thenReturn("stringToChange");
        when(properties.get(PROP_NODE_UUID)).thenReturn("newString");
        String stringToTest = SOURCES_KEY + "\"stringToChange\"";
        assertEquals("No change made to string", "(cs:appliedSources:\"newString\")", classificationSourcesUtil.replaceSourceNameWithNodeRef(stringToTest));
    }

    /**
     * Check the query is updated correctly when multiple matches are found
     *
     * This is required as the source name isn't unique to the container.
     */
    @Test
    public void testChangeMadeToStringIfMultipleMatchesFound()
    {
        childAssocRefs.add(secondSource);
        when(secondSource.getChildRef()).thenReturn(childNodeRef2);
        when(nodeService.getProperties(childNodeRef)).thenReturn(properties);
        when(nodeService.getProperties(childNodeRef2)).thenReturn(secondSetOfProperties);
        when(properties.get(PROP_CLASSIFICATION_SOURCE_NAME)).thenReturn("stringToChange");
        when(properties.get(PROP_NODE_UUID)).thenReturn("newString");
        when(secondSetOfProperties.get(PROP_CLASSIFICATION_SOURCE_NAME)).thenReturn("stringToChange");
        when(secondSetOfProperties.get(PROP_NODE_UUID)).thenReturn("secondNewString");
        String stringToTest = SOURCES_KEY + "\"stringToChange\"";
        String actual = classificationSourcesUtil.replaceSourceNameWithNodeRef(stringToTest);
        assertTrue(actual.contains("cs:appliedSources:\"newString\""));
        assertTrue(actual.contains("cs:appliedSources:\"secondNewString\""));
    }
}