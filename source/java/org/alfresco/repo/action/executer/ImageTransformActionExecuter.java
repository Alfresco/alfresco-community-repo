/*
 * Copyright (C) 2005 Alfresco, Inc.
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
package org.alfresco.repo.action.executer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.repo.content.transform.magick.ImageMagickContentTransformer;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NoTransformerException;

/**
 * Transfor action executer
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
	
	private ImageMagickContentTransformer imageMagickContentTransformer;
	
	/**
	 * Set the image magick content transformer
	 * 
	 * @param imageMagickContentTransformer		the conten transformer
	 */
	public void setImageMagickContentTransformer(ImageMagickContentTransformer imageMagickContentTransformer) 
	{
		this.imageMagickContentTransformer = imageMagickContentTransformer;
	}
	
	/**
	 * Add parameter definitions
	 */
	@Override
	protected void addParameterDefinitions(List<ParameterDefinition> paramList) 
	{
		super.addParameterDefinitions(paramList);
		paramList.add(new ParameterDefinitionImpl(PARAM_CONVERT_COMMAND, DataTypeDefinition.TEXT, false, getParamDisplayLabel(PARAM_CONVERT_COMMAND)));
	}
	
	/**
	 * @see org.alfresco.repo.action.executer.TransformActionExecuter#doTransform(org.alfresco.service.cmr.action.Action, org.alfresco.service.cmr.repository.ContentReader, org.alfresco.service.cmr.repository.ContentWriter)
	 */
	protected void doTransform(Action ruleAction, ContentReader contentReader, ContentWriter contentWriter)
	{
        // check if the transformer is going to work, i.e. is available
        if (!this.imageMagickContentTransformer.isAvailable())
        {
            throw new NoTransformerException(contentReader.getMimetype(), contentWriter.getMimetype());
        }
		// Try and transform the content
        String convertCommand = (String)ruleAction.getParameterValue(PARAM_CONVERT_COMMAND);
        // create some options for the transform
        Map<String, Object> options = new HashMap<String, Object>(5);
        options.put(ImageMagickContentTransformer.KEY_OPTIONS, convertCommand);
        
        this.imageMagickContentTransformer.transform(contentReader, contentWriter, options);    
	}
}
