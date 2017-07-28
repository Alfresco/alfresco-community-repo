/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */

package org.alfresco.repo.virtual.ref;

/**
 * Hash encoding constants interface.
 */
public interface HashEncodingArtefact
{

    static final String VANILLA_PROTOCOL_CODE = "1";

    static final String VIRTUAL_PROTOCOL_CODE = "2";

    static final String NODE_PROTOCOL_CODE = "3";

    static final String REPOSITORY_NODEREF_RESOURCE_CODE = "1";

    static final String REPOSITORY_PATH_CODE = "2";

    static final String HASHED_REPOSITORY_PATH_CODE = "3";

    static final String MIXED_REPOSITORY_PATH_CODE = "4";

    static final String CLASSPATH_RESOUCE_CODE = "5";

    static final String HASHED_CLASSPATH_RESOUCE_CODE = "6";

    static final String MIXED_CLASSPATH_RESOUCE_CODE = "7";

    static final String NUMERIC_ROOT_PATH_CODE = "1";

    static final String NUMERIC_PATH_CODE = "2";

    static final String HASHED_NUMERIC_PATH_CODE = "3";

    static final String MIXED_NUMERIC_PATH_CODE = "4";

}
