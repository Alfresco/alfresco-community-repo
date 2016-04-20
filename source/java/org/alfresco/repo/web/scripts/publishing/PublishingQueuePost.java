
package org.alfresco.repo.web.scripts.publishing;

import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.alfresco.repo.web.scripts.WebScriptUtil;
import org.alfresco.service.cmr.publishing.PublishingEvent;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * @author Nick Smith
 * @since 4.0
 */
public class PublishingQueuePost extends PublishingWebScript
{
    /**
    * {@inheritDoc}
    */
    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
    {
        String content = null;
        try
        {
            content = WebScriptUtil.getContent(req);
            if (content == null || content.isEmpty())
            {
                throw new WebScriptException(HttpServletResponse.SC_BAD_REQUEST, "No publishing event was posted!");
            }
            String eventId = jsonParser.schedulePublishingEvent(publishingService, content);
            PublishingEvent event = publishingService.getPublishingEvent(eventId);
            Map<String, Object> eventModel = builder.buildPublishingEvent(event, channelService);
            return WebScriptUtil.createBaseModel(eventModel);
        }
        catch (WebScriptException we)
        {
            throw we;
        }
        catch (Exception e)
        {
            String msg = "Failed to schedule publishing event. POST body: " + content;
            throw new WebScriptException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, msg, e);
        }
    }
}
