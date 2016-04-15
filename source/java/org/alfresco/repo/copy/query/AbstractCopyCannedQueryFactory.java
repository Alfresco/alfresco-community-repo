package org.alfresco.repo.copy.query;

import org.alfresco.query.AbstractCannedQueryFactory;
import org.alfresco.query.CannedQueryParameters;
import org.alfresco.repo.domain.node.NodeDAO;
import org.alfresco.repo.domain.qname.QNameDAO;
import org.alfresco.repo.domain.query.CannedQueryDAO;
import org.alfresco.repo.security.permissions.impl.acegi.MethodSecurityBean;
import org.alfresco.service.cmr.repository.CopyService.CopyInfo;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Support for Canned Queries for copy
 * 
 * @author Derek Hulley
 * @since 4.0
 */
public abstract class AbstractCopyCannedQueryFactory<R> extends AbstractCannedQueryFactory<R>
{
    protected NodeDAO nodeDAO;
    protected QNameDAO qnameDAO;
    protected CannedQueryDAO cannedQueryDAO;
    protected MethodSecurityBean<CopyInfo> methodSecurity;

    public void setNodeDAO(NodeDAO nodeDAO)
    {
        this.nodeDAO = nodeDAO;
    }
    public void setQnameDAO(QNameDAO qnameDAO)
    {
        this.qnameDAO = qnameDAO;
    }
    public void setCannedQueryDAO(CannedQueryDAO cannedQueryDAO)
    {
        this.cannedQueryDAO = cannedQueryDAO;
    }

    public void setMethodSecurity(MethodSecurityBean<CopyInfo> methodSecurity)
    {
        this.methodSecurity = methodSecurity;
    }

    /**
     * Parameter bean to use for copy queries
     * 
     * @author Derek Hulley
     * @since 4.0
     */
    public static class CopyCannedQueryDetail
    {
        /*package*/ final NodeRef originalNodeRef;
        /*package*/ final NodeRef copyParentNodeRef;
        /**
         * @param originalNodeRef               the original node
         */
        public CopyCannedQueryDetail(NodeRef originalNodeRef)
        {
            this(originalNodeRef, null);
        }
        /**
         * @param originalNodeRef               the original node
         * @param copyParentNodeRef             the copied node's primary parent (optional)
         */
        public CopyCannedQueryDetail(NodeRef originalNodeRef, NodeRef copyParentNodeRef)
        {
            super();
            if (originalNodeRef == null)
            {
                throw new IllegalArgumentException("Must supply an originalNodeRef");
            }
            this.originalNodeRef = originalNodeRef;
            this.copyParentNodeRef = copyParentNodeRef;
        }
    }

    protected CopyCannedQueryDetail getDetail(CannedQueryParameters parameters)
    {
        try
        {
            return (CopyCannedQueryDetail) parameters.getParameterBean();
        }
        catch (ClassCastException e)
        {
            throw new IllegalArgumentException("Expected " + CopyCannedQueryDetail.class);
        }
    }
}