/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
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
package org.alfresco.repo.template;

import java.io.Serializable;

import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.TemplateImageResolver;
import org.alfresco.service.namespace.QName;


/**
 * Object representing an association
 */
public class TemplateAssociation implements Serializable
{
    /** Serial version UUID*/
    private static final long serialVersionUID = -2903588739741433082L;
    
    /** Service registry **/
    private ServiceRegistry services;
    
    /** Association reference **/
    private AssociationRef assocRef;
    
    /** Image Resolver **/
    private TemplateImageResolver resolver;
    
    /**
     * Construct
     * 
     * @param services
     * @param assocRef
     */
    public TemplateAssociation(AssociationRef assocRef, ServiceRegistry services, TemplateImageResolver resolver)
    {
        this.assocRef = assocRef;
        this.services = services;
        this.resolver = resolver;
    }

    public AssociationRef getAssociationRef()
    {
        return this.assocRef;
    }
    
    public String getType()
    {
        return assocRef.getTypeQName().toString();
    }
    
    public QName getTypeQName()
    {
        return assocRef.getTypeQName();
    }
    
    public TemplateNode getSource()
    {
        return new TemplateNode(assocRef.getSourceRef(), services, resolver);
    }
    
    public TemplateNode getTarget()
    {
        return new TemplateNode(assocRef.getTargetRef(), services, resolver);
    }
}
