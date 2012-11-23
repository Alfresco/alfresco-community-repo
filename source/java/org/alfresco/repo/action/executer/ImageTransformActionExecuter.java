/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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
package org.alfresco.repo.action.executer;

import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.repo.content.transform.magick.ImageTransformationOptions;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.TransformationOptions;

/**
 * Transform action executer. Modified original code to use any transformer rather than just 
 * ImageMagick. The original, allowed additional command line options to be supplied (added for
 * backward compatibility in 15/04/2008). Now removed. Did not find any internal usage (22/11/2012) but it
 * is possible that there may be some in Javascript calling ScriptNode or custom code using
 * the context passed to the ImageRenderingEngine. That said, it will almost always be ImagMagick
 * that does the transform so it will still be used. These command line options were to overcome issues with
 * ImageMagick. Other transformers will just ignore them.
 * 
 * @author Roy Wetherall
 */
public class ImageTransformActionExecuter extends TransformActionExecuter 
{
    /**
     * Action constants
     */
    public static final String NAME = "transform-image";
    public static final String PARAM_CONVERT_COMMAND = "convert-command";

    /**
     * Add parameter definitions
     */
    @Override
    protected void addParameterDefinitions(List<ParameterDefinition> paramList)
    {
        super.addParameterDefinitions(paramList);
        paramList.add(new ParameterDefinitionImpl(PARAM_CONVERT_COMMAND, DataTypeDefinition.TEXT, false, getParamDisplayLabel(PARAM_CONVERT_COMMAND)));
    }

    @Override
    protected TransformationOptions newTransformationOptions(Action ruleAction, NodeRef sourceNodeRef)
    {
        ImageTransformationOptions options = new ImageTransformationOptions();
        options.setSourceNodeRef(sourceNodeRef);
        options.setSourceContentProperty(ContentModel.PROP_NAME);
        options.setTargetContentProperty(ContentModel.PROP_NAME);
        
        String convertCommand = (String) ruleAction.getParameterValue(PARAM_CONVERT_COMMAND);
        options.setCommandOptions(convertCommand);

        return options;
    }
}
