/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
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
