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

package org.alfresco.opencmis.tck.tests.query;

import java.util.Map;

import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.tck.impl.TestParameters;
import org.apache.chemistry.opencmis.tck.tests.query.QueryLikeTest;

/**
 * Fix for MNT-14432 Create test folders with temporary aspect for QueryLikeTest
 * 
 * @author Andreea Dragoi
 * @since 4.2.5
 */

public class QueryLikeTestCustom extends QueryLikeTest
{

    private static final String TEMPORARY_ASPECT = "P:sys:temporary";

    public void init(Map<String, String> parameters)
    {
        parameters.put(SessionParameter.OBJECT_FACTORY_CLASS, "org.alfresco.cmis.client.impl.AlfrescoObjectFactoryImpl");

        String objectTypeId = parameters.get(TestParameters.DEFAULT_FOLDER_TYPE);
        if (objectTypeId == null)
        {
            objectTypeId = TestParameters.DEFAULT_FOLDER_TYPE_VALUE;
        }
        objectTypeId = objectTypeId + "," + TEMPORARY_ASPECT;
        parameters.put(TestParameters.DEFAULT_FOLDER_TYPE, objectTypeId);
        super.init(parameters);
    }
}
