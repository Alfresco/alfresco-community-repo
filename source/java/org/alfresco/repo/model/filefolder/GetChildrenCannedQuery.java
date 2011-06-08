/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
 */
package org.alfresco.repo.model.filefolder;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import net.sf.acegisecurity.Authentication;
import net.sf.acegisecurity.ConfigAttributeDefinition;
import net.sf.acegisecurity.context.Context;
import net.sf.acegisecurity.context.ContextHolder;
import net.sf.acegisecurity.context.security.SecureContext;

import org.alfresco.model.ContentModel;
import org.alfresco.query.AbstractCannedQuery;
import org.alfresco.query.CannedQueryParameters;
import org.alfresco.query.CannedQuerySortDetails;
import org.alfresco.query.CannedQuerySortDetails.SortOrder;
import org.alfresco.repo.security.permissions.impl.acegi.MethodSecurityInterceptor;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Canned query to get children of a parent node
 *
 * @author janv
 * @since 4.0
 */
public class GetChildrenCannedQuery extends AbstractCannedQuery<NodeRef>
{
    private Log logger = LogFactory.getLog(getClass());
    
    private NodeService nodeService;
    private MethodSecurityInterceptor methodSecurityInterceptor;
    
    public GetChildrenCannedQuery(
            NodeService nodeService,
            MethodSecurityInterceptor methodSecurityInterceptor,
            CannedQueryParameters params,
            String queryExecutionId)
    {
        super(params, queryExecutionId);
        
        this.nodeService = nodeService;
        this.methodSecurityInterceptor = methodSecurityInterceptor;
    }
    
    @Override
    protected List<NodeRef> query(CannedQueryParameters parameters)
    {
        long start = System.currentTimeMillis();
        
        GetChildrenCannedQueryParams paramBean = (GetChildrenCannedQueryParams)parameters.getParameterBean();
        List<ChildAssociationRef> childAssocRefs = nodeService.getChildAssocs(paramBean.getParentRef(), paramBean.getSearchTypeQNames());
        
        List<NodeRef> result = new ArrayList<NodeRef>(childAssocRefs.size());
        for (ChildAssociationRef assocRef : childAssocRefs)
        {
            result.add(assocRef.getChildRef());
        }
        
        if (logger.isDebugEnabled())
        {
            logger.debug("Raw query: "+result.size()+" in "+(System.currentTimeMillis()-start)+" msecs");
        }
        
        return result;
    }

    @Override
    protected boolean isApplyPostQuerySorting()
    {
        return true;
    }

    @Override
    protected List<NodeRef> applyPostQuerySorting(List<NodeRef> results, CannedQuerySortDetails sortDetails)
    {
        if (sortDetails.getSortPairs().size() == 0)
        {
            // Nothing to sort on
            return results;
        }
        
        long start = System.currentTimeMillis();
        
        List<NodeRef> ret = new ArrayList<NodeRef>(results);
        
        List<Pair<? extends Object, SortOrder>> sortPairs = sortDetails.getSortPairs();
        
        Collections.sort(ret, new PropComparatorAsc(sortPairs));
        
        if (logger.isDebugEnabled())
        {
            logger.debug("Post query sort: "+ret.size()+" in "+(System.currentTimeMillis()-start)+" msecs");
        }
        
        return ret;
    }
    
    private class PropComparatorAsc implements Comparator<NodeRef>
    {
        private List<Pair<? extends Object, SortOrder>> sortProps;
        
        public PropComparatorAsc(List<Pair<? extends Object, SortOrder>> sortProps)
        {
            this.sortProps = sortProps;
        }
        
        public int compare(NodeRef n1, NodeRef n2)
        {
            return compareImpl(n1, n2, sortProps);
        }
        
        private int compareImpl(NodeRef node1In, NodeRef node2In, List<Pair<? extends Object, SortOrder>> sortProps)
        {
            Object pv1 = null;
            Object pv2 = null;
            
            QName sortPropQName = (QName)sortProps.get(0).getFirst();
            boolean sortAscending = (sortProps.get(0).getSecond() == SortOrder.ASCENDING);
            
            NodeRef node1 = node1In; 
            NodeRef node2 = node2In; 
            
            if (sortAscending == false)
            {
                node1 = node2In;
                node2 = node1In;
            }
            
            int result = 0;
            
            if (sortPropQName.equals(QName.createQName(".size")) || sortPropQName.equals(QName.createQName(".mimetype")))
            {
                // content data properties (size or mimetype)
                
                ContentData cd1 = (ContentData)nodeService.getProperty(node1, ContentModel.PROP_CONTENT);
                ContentData cd2 = (ContentData)nodeService.getProperty(node2, ContentModel.PROP_CONTENT);
                
                if (cd1 == null)
                {
                    return (cd2 == null ? 0 : 1);
                }
                else if (cd2 == null)
                {
                    return -1;
                }
                
                if (sortPropQName.equals(QName.createQName(".size")))
                {
                    result = ((Long)cd1.getSize()).compareTo((Long)cd2.getSize());
                }
                else if (sortPropQName.equals(QName.createQName(".mimetype")))
                {
                    result = (cd1.getMimetype()).compareTo(cd2.getMimetype());
                }
            }
            else
            {
                // property other than content size / mimetype
                pv1 = nodeService.getProperty(node1, sortPropQName);
                pv2 = nodeService.getProperty(node2, sortPropQName);
                
                if (pv1 == null)
                {
                    return (pv2 == null ? 0 : 1);
                }
                else if (pv2 == null)
                {
                    return -1;
                }
                
                if (pv1 instanceof String)
                {
                    result = (((String)pv1).compareTo((String)pv2));
                }
                else if (pv1 instanceof MLText) // eg. title, description
                {
                    String sv1 = DefaultTypeConverter.INSTANCE.convert(String.class, (MLText)pv1);
                    String sv2 = DefaultTypeConverter.INSTANCE.convert(String.class, (MLText)pv2);
                    
                    result = (sv1.compareTo(sv2));
                }
                else if (pv1 instanceof Date)
                {
                    result = (((Date)pv1).compareTo((Date)pv2));
                }
                else
                {
                    // TODO other comparisons
                    throw new RuntimeException("Unsupported sort type");
                }
            }
            
            if ((result == 0) && (sortProps.size() > 1))
            {
                return compareImpl(node1In, node2In, sortProps.subList(1, sortProps.size()-1));
            }
            
            return result;
        }
    }
    @Override
    protected boolean isApplyPostQueryPermissions()
    {
        return true;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    protected List<NodeRef> applyPostQueryPermissions(List<NodeRef> results, String authenticationToken, int requestedCount)
    {
        long start = System.currentTimeMillis();
        
        // TODO push down cut-off (as option within permission interceptor)
        //boolean cutoffAllowed = !getParameters().isReturnTotalResultCount();
        
        List<NodeRef> ret = new ArrayList<NodeRef>(results.size());
        
        Context context = ContextHolder.getContext();
        if ((context == null) || !(context instanceof SecureContext))
        {
            return ret; // empty list
        }
        
        Authentication authentication = (((SecureContext) context).getAuthentication());
        
        Method listMethod = null;
        for (Method method : FileFolderServiceImpl.class.getMethods())
        {
            if (method.getName().equals("list"))
            {
                // found one of the list methods
                listMethod = method;
                break;
            }
        }
        
        if (listMethod == null)
        {
            return ret; // empty list
        }
        
        // TODO ALF-8419
        ConfigAttributeDefinition listCad = methodSecurityInterceptor.getObjectDefinitionSource().getAttributes(new InternalMethodInvocation(listMethod));
        ret = (List<NodeRef>)methodSecurityInterceptor.getAfterInvocationManager().decide(authentication, null, listCad, results); // need to be able to cut-off etc ...
        
        if (logger.isDebugEnabled())
        {
            logger.debug("Post query perms: "+ret.size()+" in "+(System.currentTimeMillis()-start)+" msecs");
        }
        
        return ret;
    }
    
    class InternalMethodInvocation implements MethodInvocation {
        Method method;

        public InternalMethodInvocation(Method method) {
            this.method = method;
        }

        protected InternalMethodInvocation() {
            throw new UnsupportedOperationException();
        }

        public Object[] getArguments() {
            throw new UnsupportedOperationException();
        }

        public Method getMethod() {
            return this.method;
        }

        public AccessibleObject getStaticPart() {
            throw new UnsupportedOperationException();
        }

        public Object getThis() {
            throw new UnsupportedOperationException();
        }

        public Object proceed() throws Throwable {
            throw new UnsupportedOperationException();
        }
    }
}
