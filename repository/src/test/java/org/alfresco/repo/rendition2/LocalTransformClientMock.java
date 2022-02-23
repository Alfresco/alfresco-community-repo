/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2022 Alfresco Software Limited
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

package org.alfresco.repo.rendition2;

import static org.alfresco.model.ContentModel.PROP_CONTENT;

import java.io.File;
import java.util.Map;

import org.alfresco.repo.content.transform.LocalTransform;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.transform.client.util.RequestParamMap;
import org.springframework.beans.factory.annotation.Autowired;

public class LocalTransformClientMock extends LocalTransformClient
{
//    @Autowired
//    protected NodeService nodeService;

    @Override
    protected void setDirectAccessUrlIfEnabled(Map<String, String> actualOptions,
                                               NodeRef sourceNodeRef,
                                               LocalTransform localTransform)
    {
//        ContentData contentData = DefaultTypeConverter.INSTANCE.convert(ContentData.class, nodeService.getProperty(sourceNodeRef, PROP_CONTENT));

//        actualOptions.put(RequestParamMap.DIRECT_ACCESS_URL, "https://drive.google.com/uc?export=download&id=14e7NVO0L87cmSTOm2qiCiP20tGQ4S5Oq");
//        actualOptions.put(RequestParamMap.DIRECT_ACCESS_URL, contentData.getContentUrl());
        super.setDirectAccessUrlIfEnabled(actualOptions, sourceNodeRef, localTransform);
    }

}
