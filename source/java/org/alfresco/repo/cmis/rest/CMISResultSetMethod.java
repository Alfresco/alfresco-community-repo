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
package org.alfresco.repo.cmis.rest;

import java.util.List;

import org.alfresco.cmis.CMISResultSet;
import org.alfresco.repo.web.scripts.RepositoryImageResolver;
import org.alfresco.repo.web.util.paging.Cursor;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.TemplateImageResolver;

import freemarker.ext.beans.BeanModel;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;

/**
 * Custom FreeMarker Template language method.
 * <p>
 * Creates a CMIS result set that builds TemplateNode on iteration
 * <p>
 * Usage: cmisresultset(CMISResultSet resultset)
 *        
 * @author davidc
 */
public final class CMISResultSetMethod implements TemplateMethodModelEx
{
    private ServiceRegistry serviceRegistry;
    private TemplateImageResolver imageResolver;
    
    /**
     * Construct
     */
    public CMISResultSetMethod(ServiceRegistry serviceRegistry, RepositoryImageResolver imageResolver)
    {
        this.serviceRegistry = serviceRegistry;
        this.imageResolver = imageResolver.getImageResolver();
    }
    
    /**
     * @see freemarker.template.TemplateMethodModel#exec(java.util.List)
     */
    @SuppressWarnings("unchecked")
    public Object exec(List args) throws TemplateModelException
    {
        CMISTemplateResultSet resultSet = null;
        
        if (args.size() > 0)
        {
            Object arg0 = args.get(0);
            if (arg0 instanceof BeanModel)
            {
                // extract cmis result set
                Object wrapped = ((BeanModel)arg0).getWrappedObject();
                if (wrapped != null)
                {
                    if (wrapped instanceof CMISResultSet)
                    {
                        Cursor cursor = null;
                        if (args.size() == 2)
                        {
                            Object arg1 = args.get(1);
                            if (arg1 instanceof BeanModel)
                            {
                                Object wrapped1 = ((BeanModel)arg1).getWrappedObject();
                                if (wrapped1 != null && wrapped1 instanceof Cursor)
                                {
                                    cursor = (Cursor)wrapped1;
                                }
                            }
                        }
                        resultSet = new CMISTemplateResultSet((CMISResultSet)wrapped, cursor, serviceRegistry, imageResolver);
                    }
                }
            }
        }
        
        return resultSet;
    }
}
