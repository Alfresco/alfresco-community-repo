/*
 * Copyright (C) 2005-2015 Alfresco Software Limited.
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
package org.alfresco.module.org_alfresco_module_rm.model.clf.aspect;

import static java.util.Arrays.asList;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.model.RenditionModel;
import org.alfresco.module.org_alfresco_module_rm.classification.ClassificationException.MissingDowngradeInstructions;
import org.alfresco.module.org_alfresco_module_rm.classification.ClassificationLevel;
import org.alfresco.module.org_alfresco_module_rm.classification.ClassificationSchemeService;
import org.alfresco.module.org_alfresco_module_rm.classification.model.ClassifiedContentModel;
import org.alfresco.module.org_alfresco_module_rm.util.CoreServicesExtras;
import org.alfresco.service.cmr.rendition.RenditionService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

/**
 * Unit tests for the {@link ClassifiedAspect}.
 *
 * @author Tom Page
 * @since 2.4.a
 */
public class ClassifiedAspectUnitTest implements ClassifiedContentModel
{
    private static final NodeRef NODE_REF    = new NodeRef("node://Ref/");
    private static final NodeRef RENDITION_1 = new NodeRef("node://rendition1/");
    private static final NodeRef RENDITION_2 = new NodeRef("node://rendition2/");
    private static final ClassificationLevel TOP_SECRET = new ClassificationLevel("Top Secret", "Top Secret");
    private static final ClassificationLevel SECRET     = new ClassificationLevel("Secret", "Secret");

    @InjectMocks ClassifiedAspect classifiedAspect;
    @Mock ClassificationSchemeService mockClassificationSchemeService;
    @Mock CoreServicesExtras          mockCoreServicesExtras;
    @Mock NodeService                 mockNodeService;
    @Mock RenditionService            mockRenditionService;

    @Before
    public void setUp()
    {
        initMocks(this);
    }

    /** Check that providing an event and instructions is valid. */
    @Test
    public void testCheckConsistencyOfProperties_success()
    {
        when(mockNodeService.hasAspect(NODE_REF, ASPECT_CLASSIFIED)).thenReturn(true);
        when(mockNodeService.getProperty(NODE_REF, PROP_DOWNGRADE_DATE)).thenReturn(null);
        when(mockNodeService.getProperty(NODE_REF, PROP_DOWNGRADE_EVENT)).thenReturn("Event");
        when(mockNodeService.getProperty(NODE_REF, PROP_DOWNGRADE_INSTRUCTIONS)).thenReturn("Instructions");

        classifiedAspect.checkConsistencyOfProperties(NODE_REF);
    }

    /** Check that omitting all downgrade fields is valid. */
    @Test
    public void testCheckConsistencyOfProperties_notSpecified()
    {
        when(mockNodeService.hasAspect(NODE_REF, ASPECT_CLASSIFIED)).thenReturn(true);
        when(mockNodeService.getProperty(NODE_REF, PROP_DOWNGRADE_DATE)).thenReturn(null);
        when(mockNodeService.getProperty(NODE_REF, PROP_DOWNGRADE_EVENT)).thenReturn(null);
        when(mockNodeService.getProperty(NODE_REF, PROP_DOWNGRADE_INSTRUCTIONS)).thenReturn(null);

        classifiedAspect.checkConsistencyOfProperties(NODE_REF);
    }

    /** Check that a date without instructions throws an exception. */
    @Test(expected = MissingDowngradeInstructions.class)
    public void testCheckConsistencyOfProperties_dateMissingInstructions()
    {
        when(mockNodeService.hasAspect(NODE_REF, ASPECT_CLASSIFIED)).thenReturn(true);
        when(mockNodeService.getProperty(NODE_REF, PROP_DOWNGRADE_DATE)).thenReturn(new Date(123));
        when(mockNodeService.getProperty(NODE_REF, PROP_DOWNGRADE_EVENT)).thenReturn(null);
        when(mockNodeService.getProperty(NODE_REF, PROP_DOWNGRADE_INSTRUCTIONS)).thenReturn(null);

        classifiedAspect.checkConsistencyOfProperties(NODE_REF);
    }

    /** Check that an event without instructions throws an exception. */
    @Test(expected = MissingDowngradeInstructions.class)
    public void testCheckConsistencyOfProperties_eventMissingInstructions()
    {
        when(mockNodeService.hasAspect(NODE_REF, ASPECT_CLASSIFIED)).thenReturn(true);
        when(mockNodeService.getProperty(NODE_REF, PROP_DOWNGRADE_DATE)).thenReturn(null);
        when(mockNodeService.getProperty(NODE_REF, PROP_DOWNGRADE_EVENT)).thenReturn("Event");
        when(mockNodeService.getProperty(NODE_REF, PROP_DOWNGRADE_INSTRUCTIONS)).thenReturn(null);

        classifiedAspect.checkConsistencyOfProperties(NODE_REF);
    }

    /** Check that blank instructions are treated in the same way as null instructions. */
    @Test(expected = MissingDowngradeInstructions.class)
    public void testCheckConsistencyOfProperties_emptyStringsSupplied()
    {
        for (NodeRef n : asList(NODE_REF, RENDITION_1, RENDITION_2))
        {
            when(mockNodeService.hasAspect(n, ASPECT_CLASSIFIED)).thenReturn(true);
        }
        when(mockNodeService.getProperty(NODE_REF, PROP_DOWNGRADE_DATE)).thenReturn("");
        when(mockNodeService.getProperty(NODE_REF, PROP_DOWNGRADE_EVENT)).thenReturn("Event");
        when(mockNodeService.getProperty(NODE_REF, PROP_DOWNGRADE_INSTRUCTIONS)).thenReturn("");

        classifiedAspect.checkConsistencyOfProperties(NODE_REF);
    }

    /** Check that when a node is classified, its renditions are also classified. */
    @Test public void classificationOfNodeShouldClassifyRenditions()
    {
        for (NodeRef n : asList(NODE_REF, RENDITION_1, RENDITION_2))
        {
            when(mockNodeService.hasAspect(n, ASPECT_CLASSIFIED)).thenReturn(true);
        }
        when(mockClassificationSchemeService.getClassificationLevelById(eq("Top Secret"))).thenReturn(TOP_SECRET);
        when(mockClassificationSchemeService.getClassificationLevelById(eq("Secret"))).thenReturn(SECRET);
        when(mockClassificationSchemeService.getReclassification(any(), any())).thenReturn(ClassificationSchemeService.Reclassification.DOWNGRADE);
        when(mockRenditionService.getRenditions(eq(NODE_REF)))
                .thenReturn(asList(rendition(NODE_REF, RENDITION_1), rendition(NODE_REF, RENDITION_2)));

        classifiedAspect.onAddAspect(NODE_REF, ASPECT_CLASSIFIED);

        for (NodeRef rendition : asList(RENDITION_1, RENDITION_2))
        {
            verify(mockCoreServicesExtras).copyAspect(NODE_REF, rendition, ClassifiedContentModel.ASPECT_CLASSIFIED);
        }
    }

    @Test public void reclassificationOfNodeShouldReclassifyRenditions()
    {
        for (NodeRef n : asList(NODE_REF, RENDITION_1, RENDITION_2))
        {
            when(mockNodeService.hasAspect(n, ASPECT_CLASSIFIED)).thenReturn(true);
        }
        when(mockClassificationSchemeService.getClassificationLevelById("Top Secret")).thenReturn(TOP_SECRET);
        when(mockClassificationSchemeService.getClassificationLevelById("Secret")).thenReturn(SECRET);
        when(mockClassificationSchemeService.getReclassification(any(), any())).thenReturn(ClassificationSchemeService.Reclassification.DOWNGRADE);
        when(mockRenditionService.getRenditions(eq(NODE_REF)))
                .thenReturn(asList(rendition(NODE_REF, RENDITION_1), rendition(NODE_REF, RENDITION_2)));

        Map<QName, Serializable> oldProps = new HashMap<>();
        oldProps.put(PROP_CLASSIFIED_BY, "userone");
        oldProps.put(PROP_CURRENT_CLASSIFICATION, "Top Secret");
        Map<QName, Serializable> newProps = new HashMap<>(oldProps);
        newProps.put(PROP_CURRENT_CLASSIFICATION, "Secret");

        classifiedAspect.onUpdateProperties(NODE_REF, oldProps, newProps);

        for (NodeRef rendition : asList(RENDITION_1, RENDITION_2))
        {
            verify(mockCoreServicesExtras).copyAspect(NODE_REF, rendition, ClassifiedContentModel.ASPECT_CLASSIFIED);
        }
    }

    /** Creates a test Rendition ChildAssociationRef. */
    private ChildAssociationRef rendition(NodeRef source, NodeRef rendition)
    {
        return new ChildAssociationRef(RenditionModel.ASSOC_RENDITION, source, RenditionModel.ASSOC_RENDITION, rendition);
    }
}
