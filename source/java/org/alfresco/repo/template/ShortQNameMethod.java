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
package org.alfresco.repo.template;

import java.util.List;

import org.alfresco.repo.processor.BaseProcessorExtension;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.namespace.NamespaceException;
import org.alfresco.service.namespace.QName;

import freemarker.ext.beans.BeanModel;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateScalarModel;

/**
 * @author Mike Hatfield
 * 
 * FreeMarker custom method to return the short (prefix) version of a QName.
 * <p>
 * Usage: String shortQname(String longQName)
 */
public final class ShortQNameMethod extends BaseProcessorExtension implements TemplateMethodModelEx
{
    private final static String NAMESPACE_BEGIN = "" + QName.NAMESPACE_BEGIN;

    /* Repository Service Registry */
    private ServiceRegistry services;

    /**
     * Set the service registry
     * 
     * @param serviceRegistry	the service registry
     */
    public void setServiceRegistry(ServiceRegistry serviceRegistry)
    {
    	this.services = serviceRegistry;
    }

    /**
     * @see freemarker.template.TemplateMethodModel#exec(java.util.List)
     */
    public Object exec(List args) throws TemplateModelException
    {
        String result = null;
        
        if (args.size() == 1)
        {
            // arg 0 can be either wrapped QName object or a String 
            String arg0String = null;
            Object arg0 = args.get(0);
            if (arg0 instanceof BeanModel)
            {
                arg0String = ((BeanModel)arg0).getWrappedObject().toString();
            }
            else if (arg0 instanceof TemplateScalarModel)
            {
                arg0String = ((TemplateScalarModel)arg0).getAsString();
            }

            try
            {
            result = createQName(arg0String).toPrefixString(services.getNamespaceService());
        }
            catch (NamespaceException e) 
            {
                // not valid qname -> return original value
                result = arg0String;
            }
        }
        
        return result != null ? result : "";
    }

    /**
     * Helper to create a QName from either a fully qualified or short-name QName string
     * 
     * @param s    Fully qualified or short-name QName string
     * 
     * @return QName
     */
    private QName createQName(String s)
    {
        QName qname;
        if (s.indexOf(NAMESPACE_BEGIN) != -1)
        {
            qname = QName.createQName(s);
        }
        else
        {
            qname = QName.createQName(s, this.services.getNamespaceService());
        }
        return qname;
    }
}
