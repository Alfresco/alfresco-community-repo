/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
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
	protected void addParameterDefintions(List<ParameterDefinition> paramList) 
	{
		super.addParameterDefintions(paramList);
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
