/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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

package org.alfresco.repo.rendition.executer;

import java.util.Collection;

import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.repo.content.transform.magick.ImageTransformationOptions;
import org.alfresco.repo.rendition.executer.AbstractRenderingEngine.RenderingContext;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.rendition.RenditionService;
import org.alfresco.service.cmr.rendition.RenditionServiceException;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.TransformationOptions;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class is the implementation of the {@link RenditionService}'s "reformat"
 * action/rendering. This action renders a piece of content in the specified
 * target MIME type. This is achieved using one of the standard transformers
 * within the {@link ContentService}.
 * <P/>
 * Reformatting in this way is a simple conversion of one MIME type to another
 * MIME type, without any other changes to the content. Therefore there is no
 * support within this action for altering the content e.g. image
 * cropping/resizing.
 * 
 * @author Neil McErlean
 * @since 3.3
 */
public class ReformatRenderingEngine extends AbstractTransformationRenderingEngine
{
    private static Log logger = LogFactory.getLog(ReformatRenderingEngine.class);

    /**
     * This optional {@link String} parameter is only necessary when converting
     * from pdf to Flash and is used to specify which Flash version to convert
     * to.
     */
    public static final String PARAM_FLASH_VERSION = "flashVersion";

    /*
     * Action constants
     */
    public static final String NAME = "reformat";

    @Override
    protected String getTargetMimeType(RenderingContext context)
    {
        String targetMimeType = context.getCheckedParam(PARAM_MIME_TYPE, String.class);
        if (targetMimeType == null)
        {
            String msg = "The parameter: " + PARAM_MIME_TYPE + " must be explicitly set for this rendering engine!";
            logger.warn(msg);
            throw new RenditionServiceException(msg);
        }
        return targetMimeType;
    }

    @Override
    protected TransformationOptions getTransformOptions(RenderingContext context)
    {
        return getTransformOptionsImpl(new TransformationOptions(), context);
    }

    @Override
    protected TransformationOptions getTransformOptionsImpl(TransformationOptions options, RenderingContext context)
    {
        options.setSourceNodeRef(context.getSourceNode());
        options.setTargetNodeRef(context.getDestinationNode());
        
        return super.getTransformOptionsImpl(options, context);
    }

    /*
     * @see org.alfresco.repo.rendition.executer.AbstractRenderingEngine#
     * getParameterDefinitions()
     */
    @Override
    protected Collection<ParameterDefinition> getParameterDefinitions()
    {
        Collection<ParameterDefinition> paramList = super.getParameterDefinitions();
        paramList.add(new ParameterDefinitionImpl(PARAM_MIME_TYPE, DataTypeDefinition.TEXT, true,
                    getParamDisplayLabel(PARAM_MIME_TYPE)));
        paramList.add(new ParameterDefinitionImpl(PARAM_FLASH_VERSION, DataTypeDefinition.TEXT, false,
                    getParamDisplayLabel(PARAM_FLASH_VERSION)));
        return paramList;
    }
}