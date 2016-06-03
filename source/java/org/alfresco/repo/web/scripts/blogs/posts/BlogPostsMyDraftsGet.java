package org.alfresco.repo.web.scripts.blogs.posts;

import java.util.Date;

import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.blog.BlogPostInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.site.SiteInfo;

/**
 * This class is the controller for the blog-posts-mydrafts.get web script.
 * Based on the original JavaScript webscript controller
 * 
 * @author Neil Mc Erlean (based on existing JavaScript webscript controllers)
 * @since 4.0
 */
public class BlogPostsMyDraftsGet extends AbstractGetBlogWebScript
{
    @Override
    protected PagingResults<BlogPostInfo> getBlogResultsImpl(SiteInfo site, NodeRef node, Date fromDate, Date toDate, PagingRequest pagingReq)
    {
        String user = AuthenticationUtil.getFullyAuthenticatedUser();
        if(site != null)
        {
           return blogService.getDrafts(site.getShortName(), user, pagingReq);
        }
        else
        {
           return blogService.getDrafts(node, user, pagingReq);
        }
    }
}
