package org.alfresco.repo.web.scripts.blogs.posts;

import java.util.Date;

import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.blog.BlogPostInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.site.SiteInfo;

/**
 * This class is the controller for the blog-posts-mypublished.get web script.
 * 
 * @author Neil Mc Erlean (based on existing JavaScript webscript controllers)
 * @since 4.0
 */
public class BlogPostsMyPublishedGet extends AbstractGetBlogWebScript
{
    @Override
    protected PagingResults<BlogPostInfo> getBlogResultsImpl(
          SiteInfo site, NodeRef node, Date fromDate, Date toDate, PagingRequest pagingReq)
    {
        String fullyAuthenticatedUser = AuthenticationUtil.getFullyAuthenticatedUser();
        if(site != null)
        {
           return blogService.getPublished(site.getShortName(), fromDate, toDate, fullyAuthenticatedUser, pagingReq);
        }
        else
        {
           return blogService.getPublished(node, fromDate, toDate, fullyAuthenticatedUser, pagingReq);
        }
    }
}
