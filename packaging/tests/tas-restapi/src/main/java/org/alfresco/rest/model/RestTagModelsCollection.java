/*-
 * #%L
 * alfresco-tas-restapi
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
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
package org.alfresco.rest.model;

import org.alfresco.rest.core.RestModels;


/**
 * Handles collection of Tags
 * 
 * "entries":
 * [
 * {"entry":
 *      { "tag":"addedtag-c7444-1474370805346",
 *        "id":"f45c4d06-f4df-42d7-a118-29121557d284"}
 *  },
 *  {"entry":
 *      {"tag":"addedtag-c7444-1474370863151",
 *      "id":"c05bdec5-1051-4413-9db6-0d3797f1cce5"}
 *  }
 * 
 * @author Corina Nechifor
 *
 */
public class RestTagModelsCollection extends RestModels<RestTagModel, RestTagModelsCollection>
{

}    
