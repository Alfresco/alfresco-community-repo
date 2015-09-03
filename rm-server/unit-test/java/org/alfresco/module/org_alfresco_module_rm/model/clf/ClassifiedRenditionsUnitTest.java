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
package org.alfresco.module.org_alfresco_module_rm.model.clf;

import static java.util.Arrays.asList;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import org.alfresco.model.RenditionModel;
import org.alfresco.module.org_alfresco_module_rm.classification.ContentClassificationService;
import org.alfresco.module.org_alfresco_module_rm.classification.model.ClassifiedContentModel;
import org.alfresco.module.org_alfresco_module_rm.model.clf.aspect.ClassifiedAspect;
import org.alfresco.module.org_alfresco_module_rm.test.util.MockAuthenticationUtilHelper;
import org.alfresco.module.org_alfresco_module_rm.util.AuthenticationUtil;
import org.alfresco.module.org_alfresco_module_rm.util.CoreServicesExtras;
import org.alfresco.service.cmr.rendition.RenditionService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

/**
 * Unit tests for {@link ClassifiedRenditions}.
 *
 * @since 2.4.a
 */
public class ClassifiedRenditionsUnitTest implements ClassifiedContentModel
{
    private static final NodeRef SOURCE_NODE = new NodeRef("node://ref/");
    private static final NodeRef RENDITION_1 = new NodeRef("node://rendition1/");
    private static final NodeRef RENDITION_2 = new NodeRef("node://rendition2/");

    @InjectMocks ClassifiedAspect classifiedAspect;

    @Mock AuthenticationUtil           mockAuthenticationUtil;
    @Mock ContentClassificationService mockContentClassificationService;
    @Mock CoreServicesExtras           mockCoreServicesExtras;
    @Mock NodeService                  mockNodeService;
    @Mock RenditionService             mockRenditionService;

    @Before
    public void setUp()
    {
        initMocks(this);

        MockAuthenticationUtilHelper.setup(mockAuthenticationUtil);
    }

    @Test public void newRenditionOfClassifiedNodeShouldItselfBeClassified()
    {
        when(mockRenditionService.getRenditions(SOURCE_NODE))
                .thenReturn(asList(rendition(SOURCE_NODE, RENDITION_1), rendition(SOURCE_NODE, RENDITION_2)));
        when(mockRenditionService.getSourceNode(RENDITION_1)).thenReturn(rendition(SOURCE_NODE, RENDITION_1));
        when(mockRenditionService.getSourceNode(RENDITION_2)).thenReturn(rendition(SOURCE_NODE, RENDITION_2));
        when(mockContentClassificationService.isClassified(SOURCE_NODE)).thenReturn(true);

        final ClassifiedRenditions behaviour = new ClassifiedRenditions();
        behaviour.setAuthenticationUtil(mockAuthenticationUtil);
        behaviour.setContentClassificationService(mockContentClassificationService);
        behaviour.setCoreServicesExtras(mockCoreServicesExtras);
        behaviour.setNodeService(mockNodeService);
        behaviour.setRenditionService(mockRenditionService);

        behaviour.onAddAspect(RENDITION_2, RenditionModel.ASPECT_RENDITION);

        verify(mockCoreServicesExtras).copyAspect(SOURCE_NODE, RENDITION_2, ClassifiedContentModel.ASPECT_CLASSIFIED);
    }

    /** Creates a test Rendition ChildAssociationRef. */
    private ChildAssociationRef rendition(NodeRef source, NodeRef rendition)
    {
        return new ChildAssociationRef(RenditionModel.ASSOC_RENDITION, source, RenditionModel.ASSOC_RENDITION, rendition);
    }
}
