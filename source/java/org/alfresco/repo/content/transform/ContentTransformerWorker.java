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

import org.alfresco.api.AlfrescoPublicApi;     
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.TransformationOptions;

/**
 * An interface that allows separation between the content transformer registry and the various third party subsystems
 * performing the transformation.
 * 
 * @author dward
 */
// TODO Modify ContentTransformerWorker to understand transformer limits. At the moment no workers use them
@AlfrescoPublicApi
public interface ContentTransformerWorker
{
    /**
     * Checks if this worker is available.
     * 
     * @return true if it is available
     */
    public boolean isAvailable();

    /**
     * Gets a string returning product and version information.
     * 
     * @return the version string
     */
    public String getVersionString();

    /**
     * Unlike {@link ContentTransformer#isTransformable(String, String, TransformationOptions)} 
     * should not include the transformer name, as that is added by the ContentTransformer in
     * the parent context.
     */
    public boolean isTransformable(String sourceMimetype, String targetMimetype, TransformationOptions options);    

    /**
     * @see ContentTransformer#getComments(boolean)
     */
    public String getComments(boolean available);

    /**
     * @see ContentTransformer#transform(ContentReader, ContentWriter, TransformationOptions)
     */
    public void transform(ContentReader reader, ContentWriter writer, TransformationOptions options) throws Exception;
}
