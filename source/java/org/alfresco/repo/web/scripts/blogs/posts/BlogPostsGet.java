package org.alfresco.repo.web.scripts.blogs.posts;

import java.util.Date;

import org.alfresco.query.EmptyPagingResults;
import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.service.cmr.blog.BlogPostInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.site.SiteInfo;

/**
 * This class is the controller for the blog-posts.get web script.
 * 
 * @author Neil Mc Erlean (based on existing JavaScript webscript controllers)
 * @since 4.0
 */
public class BlogPostsGet extends AbstractGetBlogWebScript
{
    @SuppressWarnings("deprecation")
    @Override
    protected PagingResults<BlogPostInfo> getBlogResultsImpl(
          SiteInfo site, NodeRef node, Date fromDate, Date toDate, PagingRequest pagingReq)
    {
        // As it uses deprecated methods, this bit can be a bit hacky...
        if (node == null)
        {
           // Site based request, but no container exists yet
           return new EmptyPagingResults<BlogPostInfo>();
        }
       
        // This intentionally uses the deprecated method in the foundation service.
        // In fact the method is there specifically for this class.
        return blogService.getMyDraftsAndAllPublished(node, fromDate, toDate, pagingReq);
    }
}
