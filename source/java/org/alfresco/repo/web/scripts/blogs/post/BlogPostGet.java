package org.alfresco.repo.web.scripts.blogs.post;

import java.util.HashMap;
import java.util.Map;

import org.alfresco.repo.web.scripts.blogs.AbstractBlogWebScript;
import org.alfresco.repo.web.scripts.blogs.BlogPostLibJs;
import org.alfresco.service.cmr.blog.BlogPostInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.site.SiteInfo;
import org.json.simple.JSONObject;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * This class is the controller for the blog-posts.get web script.
 * 
 * @author Neil Mc Erlean (based on existing JavaScript webscript controllers)
 * @since 4.0
 */
public class BlogPostGet extends AbstractBlogWebScript
{
    @Override
    protected Map<String, Object> executeImpl(SiteInfo site, NodeRef nodeRef,
         BlogPostInfo blog, WebScriptRequest req, JSONObject json, Status status, Cache cache) 
    {
        if (blog == null)
        {
           throw new WebScriptException(Status.STATUS_NOT_FOUND, "Blog Post Not Found");
        }

        // Build the response
        Map<String, Object> model = new HashMap<String, Object>();
        
        // TODO Fetch this from the BlogPostInfo object
        NodeRef node = blog.getNodeRef();
        Map<String, Object> item = BlogPostLibJs.getBlogPostData(node, services);
        model.put(ITEM, item);
        model.put(POST, blog);
        
        model.put("externalBlogConfig", BlogPostLibJs.hasExternalBlogConfiguration(node, services));
        
        int contentLength = -1;
        String arg = req.getParameter("contentLength");
        if (arg != null)
        {
            try
            {
                contentLength = Integer.parseInt(arg);
            }
            catch (NumberFormatException ignored)
            {
                // Intentionally empty
            }
        }
        
        model.put("contentLength", contentLength);
        
        return model;
    }
}
