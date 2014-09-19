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
package org.alfresco.repo.content.transform;

import java.util.List;

import org.alfresco.api.AlfrescoPublicApi;   
import org.alfresco.service.cmr.repository.TransformationOptions;

/**
 * Selects a transformer from a supplied list of transformers that appear
 * able to handle a given transformation.
 * 
 * @author Alan Davis
 */
@AlfrescoPublicApi
public interface TransformerSelector
{
    /**
     * Returns a sorted list of transformers that identifies the order in which transformers
     * should be tried.
     * @param sourceMimetype
     * @param sourceSize
     * @param targetMimetype
     * @param options transformation options
     * @return a sorted list of transformers, with the best one first.
     */
    List<ContentTransformer> selectTransformers(String sourceMimetype, long sourceSize,
            String targetMimetype, TransformationOptions options);
}
