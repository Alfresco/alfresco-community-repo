/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2018 Alfresco Software Limited
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
package org.alfresco.repo.content.transform;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.TransformationOptions;
import org.alfresco.util.Pair;
import org.apache.commons.logging.Log;

/**
 * Optionally sends transformations to a remote transformer if a {@link RemoteTransformerClient} is set and
 * the ".url" Alfresco global property is set.
 *
 * @deprecated The transformations code is being moved out of the codebase and replaced by the new async RenditionService2 or other external libraries.
 */
@Deprecated
public abstract class AbstractRemoteContentTransformer extends AbstractContentTransformer2
{
    private boolean enabled = true;

    private RemoteTransformerClient remoteTransformerClient;

    private boolean available = false;

    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }

    /**
     * Sets the optional remote transformer client which will be used in preference to a local command if available.
     *
     * @param remoteTransformerClient may be null;
     */
    public void setRemoteTransformerClient(RemoteTransformerClient remoteTransformerClient)
    {
        this.remoteTransformerClient = remoteTransformerClient;
    }

    boolean remoteTransformerClientConfigured()
    {
        return remoteTransformerClient != null && remoteTransformerClient.getBaseUrl() != null;
    }

    protected abstract Log getLogger();

    /**
     * THIS IS A CUSTOM SPRING INIT METHOD
     */
    @Override
    public void register()
    {
        super.register();
        afterPropertiesSet();
    }

    public void afterPropertiesSet()
    {
        if (enabled)
        {
            // check availability
            if (remoteTransformerClientConfigured())
            {
                Log logger = getLogger();
                try
                {
                    Pair<Boolean, String> result = remoteTransformerClient.check(logger);
                    Boolean isAvailable = result.getFirst();
                    String msg = result.getSecond() == null ? "" : result.getSecond();
                    if (isAvailable != null && isAvailable)
                    {
                        String versionString = msg;
                        setAvailable(true);
                        logger.debug("Using legacy " + getName() + ": " + versionString);
                    }
                    else
                    {
                        setAvailable(false);
                        String message = "Legacy " + getName() + " is not available for transformations. " + msg;
                        if (isAvailable == null)
                        {
                            logger.debug(message);
                        }
                        else
                        {
                            logger.error(message);
                        }
                    }
                }
                catch (Throwable e)
                {
                    setAvailable(false);
                    logger.error("Remote " + getName() + " is not available: " + (e.getMessage() != null ? e.getMessage() : ""));
                    // debug so that we can trace the issue if required
                    logger.debug(e);
                }
            }
            else
            {
                available = true;
            }
        }
    }

    public boolean isAvailable()
    {
        if (remoteTransformerClientConfigured() && !remoteTransformerClient.isAvailable())
        {
            afterPropertiesSet();
        }

        return available;
    }

    protected void setAvailable(boolean available)
    {
        this.available = available;
    }

    @Override
    public boolean isTransformable(String sourceMimetype, long sourceSize, String targetMimetype, TransformationOptions options)
    {
        if (!isAvailable())
        {
            return false;
        }

        return super.isTransformable(sourceMimetype, sourceSize, targetMimetype, options);
    }

    public void transformInternal(ContentReader reader, ContentWriter writer, TransformationOptions options)
            throws Exception
    {
        if (remoteTransformerClientConfigured())
        {
            String sourceMimetype = getMimetype(reader);
            String targetMimetype = writer.getMimetype();
            String targetEncoding = writer.getEncoding();

            MimetypeService mimetypeService = getMimetypeService();
            String sourceExtension = mimetypeService.getExtension(sourceMimetype);
            String targetExtension = mimetypeService.getExtension(targetMimetype);
            if (sourceExtension == null || targetExtension == null)
            {
                throw new AlfrescoRuntimeException("Unknown extensions for mimetypes: \n" +
                        "   source mimetype: " + sourceMimetype + "\n" +
                        "   source extension: " + sourceExtension + "\n" +
                        "   target mimetype: " + targetMimetype + "\n" +
                        "   target extension: " + targetExtension + "\n" +
                        "   target encoding: " + targetEncoding);
            }

            transformRemote(remoteTransformerClient, reader, writer, options, sourceMimetype, targetMimetype,
                    sourceExtension, targetExtension, targetEncoding);
        }
        else
        {
            transformLocal(reader, writer, options);
        }

        Log logger = getLogger();
        if (logger.isDebugEnabled())
        {
            logger.debug("Transformation completed: \n" +
                    "   source: " + reader + "\n" +
                    "   target: " + writer + "\n" +
                    "   options: " + options);
        }
    }

    protected abstract void transformLocal(ContentReader reader, ContentWriter writer, TransformationOptions options)
            throws Exception;

    protected abstract void transformRemote(RemoteTransformerClient remoteTransformerClient, ContentReader reader,
                                            ContentWriter writer, TransformationOptions options,
                                            String sourceMimetype, String targetMimetype,
                                            String sourceExtension, String targetExtension,
                                            String targetEncoding) throws Exception;
}
