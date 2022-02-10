/*
 * #%L
 * Alfresco Repository
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
package org.alfresco.repo.rendition2;

import org.alfresco.transform.client.model.config.CoreFunction;
import org.alfresco.transform.client.registry.TransformServiceRegistry;

import java.util.Map;

import static org.alfresco.repo.rendition2.TestSynchronousTransformClient.*;
import static org.alfresco.repo.rendition2.TestSynchronousTransformClient.TEST_FAILING_MIME_TYPE;

/**
 * @author adavis
 */
public class TestTransformServiceRegistry implements TransformServiceRegistry
{
    private TransformServiceRegistry delegate;

    public TestTransformServiceRegistry(TransformServiceRegistry delegate)
    {
        this.delegate = delegate;
    }

    @Override
    public boolean isSupported(String sourceMimetype, long sourceSizeInBytes, String targetMimetype,
                               Map<String, String> actualOptions, String transformName)
    {
        return sourceMimetype.equals(TEST_FAILING_MIME_TYPE) ||
                sourceMimetype.equals(TEST_LONG_RUNNING_MIME_TYPE)
                ? true
                : delegate.isSupported(sourceMimetype, sourceSizeInBytes, targetMimetype, actualOptions, transformName);
    }

    @Override
    public long findMaxSize(String sourceMimetype, String targetMimetype, Map<String, String> actualOptions, String transformName)
    {
        return sourceMimetype.equals(TEST_FAILING_MIME_TYPE) ||
                sourceMimetype.equals(TEST_LONG_RUNNING_MIME_TYPE)
                ? -1
                : delegate.findMaxSize(sourceMimetype, targetMimetype, actualOptions, transformName);
    }

    @Override
    public String findTransformerName(String sourceMimetype, long sourceSizeInBytes,
                                      String targetMimetype, Map<String, String> actualOptions, String renditionName)
    {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public boolean isSupported(CoreFunction function, String transformerName)
    {
        return true;
    }
}
