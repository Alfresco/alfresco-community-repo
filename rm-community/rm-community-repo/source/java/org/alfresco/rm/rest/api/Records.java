/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
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

package org.alfresco.rm.rest.api;

import org.alfresco.rest.api.model.Node;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.rm.rest.api.model.TargetContainer;

/**
 * Records API
 * 
 * @author Ana Bozianu
 * @since 2.6
 */
public interface Records
{
    public static final String PARAM_HIDE_RECORD = "hideRecord";

    /**
     * Creates a record from a file
     * 
     * @param fileId the id of a non record file
     * @param parameters  the {@link Parameters} object to get the parameters passed into the request
     * @return information about the created record
     */
    public Node declareFileAsRecord(String fileId, Parameters parameters);

    /**
     * Files a record into th fileplan.
     * If the record is already filed it links the record to the target folder
     * 
     * @param recordId the id of the record do file/link
     * @param target the target parent folder
     * @param parameters the {@link Parameters} object to get the parameters passed into the request
     * @return information about the new state of the record
     */
    public Node fileOrLinkRecord(String recordId, TargetContainer target, Parameters parameters);
}
