/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2019 Alfresco Software Limited
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

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentServiceTransientException;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;

import java.util.Map;

/**
 * @author adavis
 */
public class TestSynchronousTransformClient<T> implements SynchronousTransformClient
{
    public static final String TEST_FAILING_MIME_TYPE = "application/vnd.alfresco.test.transientfailure";
    public static final String TEST_LONG_RUNNING_MIME_TYPE = "application/vnd.alfresco.test.longrunning";
    public static final String TEST_USER_MIME_TYPE = "image/alfresco.test.user";

    public static final long TEST_LONG_RUNNING_TRANSFORM_TIME = 5000;
    public static final String TEST_LONG_RUNNING_PROPERTY_VALUE = "NewValue";
    public static final String EXPECTED_USER = "UserOne";

    private SynchronousTransformClient delegate;

    public TestSynchronousTransformClient(SynchronousTransformClient delegate)
    {
        this.delegate = delegate;
    }

    @Override
    public boolean isSupported(String sourceMimetype, long sourceSizeInBytes, String contentUrl, String targetMimetype,
                               Map<String, String> actualOptions, String transformName, NodeRef sourceNodeRef)
    {
        boolean supported = true;
        if (!sourceMimetype.equals(TEST_FAILING_MIME_TYPE) && !sourceMimetype.equals(TEST_LONG_RUNNING_MIME_TYPE) &&
            !targetMimetype.equals(TEST_FAILING_MIME_TYPE) && !targetMimetype.equals(TEST_LONG_RUNNING_MIME_TYPE))
        {
            supported = delegate.isSupported(sourceMimetype, sourceSizeInBytes, contentUrl, targetMimetype, actualOptions,
                    transformName, sourceNodeRef);
        }
        return supported;
    }

    @Override
    public void transform(ContentReader reader, ContentWriter writer, Map<String, String> actualOptions, String transformName, NodeRef sourceNodeRef)
    {
        String sourceMimetype = reader.getMimetype();
        String targetMimetype = writer.getMimetype();
        if (sourceMimetype.equals(TEST_FAILING_MIME_TYPE) || targetMimetype.equals(TEST_FAILING_MIME_TYPE))
        {
            throw new ContentServiceTransientException("Transformation intentionally failed for test purposes.");
        }
        else if (sourceMimetype.equals(TEST_LONG_RUNNING_MIME_TYPE) || targetMimetype.equals(TEST_LONG_RUNNING_MIME_TYPE))
        {
            try
            {
                Thread.sleep(TEST_LONG_RUNNING_TRANSFORM_TIME);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
            writer.putContent("SUCCESS");
        }
        else if (sourceMimetype.equals(TEST_USER_MIME_TYPE) || targetMimetype.equals(TEST_USER_MIME_TYPE))
        {
            String username = AuthenticationUtil.getFullyAuthenticatedUser();
            if (!EXPECTED_USER.equals(username))
            {
                throw new ContentIOException(
                        "Expected username '" + EXPECTED_USER + "' but found '" + username + "'");
            }
            writer.putContent("SUCCESS");
        }
        else
        {
            delegate.transform(reader, writer, actualOptions, transformName, sourceNodeRef);
        }
    }

    @Override
    public String getName()
    {
        return delegate.getName();
    }
}
