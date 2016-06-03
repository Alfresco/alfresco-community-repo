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
