/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */

package org.alfresco.repo.thumbnail;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.ActionImpl;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.content.transform.magick.ImageResizeOptions;
import org.alfresco.repo.content.transform.magick.ImageTransformationOptions;
import org.alfresco.repo.rendition.PerformRenditionActionExecuter;
import org.alfresco.repo.rendition.RenditionServiceImpl;
import org.alfresco.repo.rendition.executer.AbstractRenderingEngine;
import org.alfresco.repo.rendition.executer.ImageRenderingEngine;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.rendition.RenditionDefinition;
import org.alfresco.service.cmr.rendition.RenditionService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.thumbnail.ThumbnailParentAssociationDetails;
import org.alfresco.service.cmr.thumbnail.ThumbnailService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

/**
 * Thumbnail service implementation unit test
 * 
 * @author Neil McErlean
 */
public class ThumbnailServiceImplParameterTest
{
    // Mocked services.
    private ActionService mockActionService = mock(ActionService.class);

    // Real services - backed by mocked services.
    private RenditionServiceImpl renditionService;
    private ThumbnailService thumbnailService;

    private final NodeRef dummyNodeRef1 = new NodeRef("workspace", "dummy", "dummyID_1");
    private final NodeRef dummyNodeRef2 = new NodeRef("workspace", "dummy", "dummyID_2");
    private final NodeRef dummyNodeRef3 = new NodeRef("workspace", "dummy", "dummyID_3");

    @Before
    public void initMockObjects()
    {
        when(mockActionService.createAction(PerformRenditionActionExecuter.NAME))
            .thenReturn(new ActionImpl(dummyNodeRef2, "id", PerformRenditionActionExecuter.NAME, new HashMap<String, Serializable>()));
        renditionService = new RenditionServiceImpl()
        {
            @Override
            public RenditionDefinition loadRenditionDefinition(QName renderingActionName)
            {
                // We're intentionally returning null for this test.
                return null;
            }
        };

        renditionService.setActionService(mockActionService);

        ThumbnailServiceImpl thumbs = new ThumbnailServiceImpl()
        {
            @Override
            public NodeRef getThumbnailByName(NodeRef node,
                    QName contentProperty, String thumbnailName)
            {
                return null;
            }
            /**
             * In this test the thumbnailRef will be null, so we need to ensure
             * it is not dereferenced here.
             */
            @Override
            public NodeRef getThumbnailNode(ChildAssociationRef thumbnailRef)
            {
                return null;
            }
        };
        thumbs.setRenditionService(renditionService);
        thumbs.setThumbnailRegistry(new ThumbnailRegistry() {
            @Override
            public ThumbnailRenditionConvertor getThumbnailRenditionConvertor()
            {
                return new ThumbnailRenditionConvertor();
            }
        });
        thumbs.setNodeService(mock(NodeService.class));
        thumbnailService = thumbs;
    }

    /**
     * This test method ensures that the parameters on thumbnail-create are
     * passed through the RenditionService to the ActionService
     */
    @Test
    public void createThumbnailPassesParametersToActionService()
    {
        // As most of the services are mocked out, the actual values used here
        // don't matter.
        final Map<String, Serializable> parametersUnderTest = new HashMap<String, Serializable>();
        parametersUnderTest.put(ImageRenderingEngine.PARAM_RESIZE_WIDTH, new Integer(42));
        parametersUnderTest.put(ImageRenderingEngine.PARAM_RESIZE_HEIGHT, new Integer(93));
        parametersUnderTest.put(ImageRenderingEngine.PARAM_COMMAND_OPTIONS, "foo");
        parametersUnderTest.put(ImageRenderingEngine.PARAM_MAINTAIN_ASPECT_RATIO, Boolean.TRUE);
        parametersUnderTest.put(ImageRenderingEngine.PARAM_RESIZE_TO_THUMBNAIL, Boolean.FALSE);
        parametersUnderTest.put(AbstractRenderingEngine.PARAM_TARGET_CONTENT_PROPERTY, ContentModel.PROP_CONTENT);
        parametersUnderTest.put(RenditionService.PARAM_DESTINATION_NODE, dummyNodeRef2);
        

        ImageTransformationOptions imageTransOpts = new ImageTransformationOptions();
        imageTransOpts.setTargetNodeRef(dummyNodeRef2);
        
        imageTransOpts.setTargetContentProperty((QName) parametersUnderTest.get(ImageRenderingEngine.PARAM_TARGET_CONTENT_PROPERTY));
        imageTransOpts.setCommandOptions((String) parametersUnderTest.get(ImageRenderingEngine.PARAM_COMMAND_OPTIONS));

        ImageResizeOptions resizeOptions = new ImageResizeOptions();
        resizeOptions.setHeight((Integer) parametersUnderTest.get(ImageRenderingEngine.PARAM_RESIZE_HEIGHT));
        resizeOptions.setWidth((Integer) parametersUnderTest.get(ImageRenderingEngine.PARAM_RESIZE_WIDTH));
        resizeOptions.setMaintainAspectRatio((Boolean) parametersUnderTest.get(ImageRenderingEngine.PARAM_MAINTAIN_ASPECT_RATIO));
        resizeOptions.setResizeToThumbnail((Boolean) parametersUnderTest.get(ImageRenderingEngine.PARAM_RESIZE_TO_THUMBNAIL));
        imageTransOpts.setResizeOptions(resizeOptions);
        
        ThumbnailParentAssociationDetails assocDetails = new ThumbnailParentAssociationDetails(dummyNodeRef3,
                    ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI,
                                "homerSimpson"));
        
        // Now request the creation of the the thumbnail.
        thumbnailService.createThumbnail(dummyNodeRef1, ContentModel.PROP_CONTENT, MimetypeMap.MIMETYPE_IMAGE_JPEG,
                    imageTransOpts, "bartSimpson", assocDetails);

        
        ArgumentCaptor<Action> argument = ArgumentCaptor.forClass(Action.class);
        verify(mockActionService).executeAction(argument.capture(), any(NodeRef.class), anyBoolean(), anyBoolean());
        final Action performRenditionAction = argument.getValue();
        final RenditionDefinition renditionDefn = (RenditionDefinition) performRenditionAction.getParameterValue(PerformRenditionActionExecuter.PARAM_RENDITION_DEFINITION);
        Map<String, Serializable> parameters = renditionDefn.getParameterValues();
        
        for (String s : parametersUnderTest.keySet())
        {
            if (parameters.keySet().contains(s) == false || parameters.get(s) == null || parameters.get(s).toString().length() == 0)
            {
                fail("Missing parameter " + s);
            }
            assertEquals("Parameter " + s + " had wrong value.",
                    parametersUnderTest.get(s), parameters.get(s));
        }
    }
}
