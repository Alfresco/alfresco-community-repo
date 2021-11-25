/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2021 Alfresco Software Limited
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

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.transform.client.registry.TransformerDebugBase;
import org.alfresco.util.LogTee;
import org.alfresco.util.PropertyCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Deque;
import java.util.Map;

/**
 * Debugs transformers selection and activity.<p>
 *
 * As transformations are frequently composed of lower level transformations, log
 * messages include a prefix to identify the transformation. A numeric dot notation
 * is used (such as {@code 123.1.2} indicating the second third level transformation
 * of the 123rd top level transformation).
 * @author Alan Davis
 */
public class TransformerDebug extends TransformerDebugBase
{
    protected static final String TRANSFORM_SERVICE_NAME = "TransformService";

    protected NodeService nodeService;
    protected MimetypeService mimetypeService;

    protected static class SimpleLogAdaptor implements SimpleLog
    {
        private final Log log;

        public SimpleLogAdaptor(Log log)
        {
            this.log = log;
        }

        @Override
        public boolean isDebugEnabled()
        {
            return log.isDebugEnabled();
        }

        @Override
        public boolean isTraceEnabled()
        {
            return log.isTraceEnabled();
        }

        @Override
        public void debug(Object message, Throwable throwable)
        {
            log.debug(message, throwable);
        }

        @Override
        public void trace(Object message, Throwable throwable)
        {
            log.trace(message, throwable);
        }
    }

    public void setTransformerLog(Log transformerLog)
    {
        super.setSingleLineLog(new SimpleLogAdaptor(new LogTee(LogFactory.getLog(TransformerLog.class), transformerLog)));
    }

    public void setTransformerDebugLog(Log transformerDebugLog)
    {
        super.setMultiLineLog(new SimpleLogAdaptor(new LogTee(LogFactory.getLog(TransformerDebug.class), transformerDebugLog)));
    }

    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    public void setMimetypeService(MimetypeService mimetypeService)
    {
        this.mimetypeService = mimetypeService;
        setExtensionLookup(mimetype -> mimetypeService.getExtension(mimetype));
    }

    public void afterPropertiesSet() throws Exception
    {
        PropertyCheck.mandatory(this, "nodeService", nodeService);
        PropertyCheck.mandatory(this, "mimetypeService", mimetypeService);
        PropertyCheck.mandatory(this, "transformerLog", getSingleLineLog());
        PropertyCheck.mandatory(this, "transformerDebugLog", getMultiLineLog());
    }

    public void pushTransform(String transformerName, String fromUrl, String sourceMimetype,
                              String targetMimetype, long sourceSize, Map<String, String> options,
                              String renditionName, NodeRef sourceNodeRef)
    {
        if (isEnabled())
        {
            String sourceNodeRefStr = sourceNodeRef.toString();
            Deque<Frame> ourStack = ThreadInfo.getStack();
            boolean firstLevel = ourStack.size() == 0;
            String filename = getFileName(sourceNodeRef, firstLevel, sourceSize);
            pushTransform(transformerName, fromUrl, sourceMimetype, targetMimetype, sourceSize,
                          options, renditionName, sourceNodeRefStr, filename);
        }
    }

    /**
     * @deprecated Not used in TransformerDebug any more now Legacy transforms have been removed.
     */
    @Deprecated
    public void debug(String sourceMimetype, String targetMimetype, NodeRef sourceNodeRef, long sourceSize,
                      Map<String, String> options, String renditionName, String message)
    {
        String filename = getFileName(sourceNodeRef, true, -1);
        log("              "+ getSourceAndTargetExt(sourceMimetype, targetMimetype) +
                ((filename != null) ? filename+' ' : "")+
                ((sourceSize >= 0) ? fileSize(sourceSize)+' ' : "") +
                (getRenditionName(renditionName)) + message);
        log(options);
    }

    public String getFileName(NodeRef sourceNodeRef, boolean firstLevel, long sourceSize)
    {
        String result = null;
        if (sourceNodeRef != null)
        {
            try
            {
                result = (String)nodeService.getProperty(sourceNodeRef, ContentModel.PROP_NAME);
            }
            catch (RuntimeException e)
            {
                // ignore (InvalidNodeRefException/MalformedNodeRefException) but we should ignore other RuntimeExceptions too
            }
        }
        if (result == null && !firstLevel)
        {
            result = "<<TemporaryFile>>";
        }
        return result;
    }

    /**
     * Debugs a request to the Transform Service
     */
    public int debugTransformServiceRequest(String sourceMimetype, long sourceSize, NodeRef sourceNodeRef,
                                            int contentHashcode, String filename, String targetMimetype,
                                            Map<String, String> options, String renditionName)
    {
        if (isEnabled())
        {
            pushMisc();
            String sourceAndTargetExt = getSourceAndTargetExt(sourceMimetype, targetMimetype);
            debug(sourceAndTargetExt +
                    ((filename != null) ? filename + ' ' : "") +
                    ((sourceSize >= 0) ? fileSize(sourceSize) + ' ' : "") +
                    getRenditionName(renditionName) + " "+ TRANSFORM_SERVICE_NAME);
            log(options);
            log(sourceNodeRef.toString() + ' ' + contentHashcode);
            String reference = getReference(true, false, false);
            infoLog(reference, sourceAndTargetExt, null, filename, sourceSize, TRANSFORM_SERVICE_NAME,
                    renditionName, null, "", true);
        }
        return pop(Call.AVAILABLE, true, false);
    }

    /**
     * Debugs a response to the Transform Service
     */
    public void debugTransformServiceResponse(NodeRef sourceNodeRef, int contentHashcode,
                                              long requested, int id, String sourceExt, String targetExt, String msg)
    {
        pushMisc();
        Frame frame = ThreadInfo.getStack().getLast();
        frame.setId(id);
        boolean suppressFinish = id == -1 || requested == -1;
        if (!suppressFinish)
        {
            frame.setStart(requested);
        }
        debug(msg);
        debug(sourceNodeRef.toString() + ' ' +contentHashcode);
        pop(Call.AVAILABLE, suppressFinish, true);
    }
}
