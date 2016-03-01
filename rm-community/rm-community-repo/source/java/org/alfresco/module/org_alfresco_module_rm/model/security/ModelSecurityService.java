 
package org.alfresco.module.org_alfresco_module_rm.model.security;

import java.util.Set;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * Model security service interface.
 * 
 * @author Roy Wetherall
 * @since 2.1
 */
public interface ModelSecurityService
{
    /**
     * Sets whether model security is enabled globally or not.
     * 
     * @param enabled
     */
    void setEnabled(boolean enabled);
    
    /**
     * Indicates whether model security is enabled or not.
     * 
     * @return
     */
    boolean isEnabled();
    
    /**
     * Disable model security checks for the current thread.
     */
    void disable();
    
    /**
     * Enable model security checks for the current thread.
     */
    void enable();
    
    /**
     * Registers a protected model artifact with the service.
     * 
     * @param atrifact  protected model artifact
     */
    void register(ProtectedModelArtifact atrifact);  
    
    /**
     * Indicates whether a property is protected or not.
     * 
     * @param name  name of property
     * @return boolean  true if property is protected, false otherwise
     */
    boolean isProtectedProperty(QName property);
    
    /**
     * Get the protected properties
     * 
     * @return  {@link Set}<{@link QName}>  all the protected properties
     */
    Set<QName> getProtectedProperties();
    
    /**
     * Get the details of the protected property, returns null if property
     * is not protected.
     * 
     * @param name  name of the protected property
     * @return {@link ProtectedProperty}    protected property details, null otherwise
     */
    ProtectedProperty getProtectedProperty(QName name);
    
    /**
     * Indicates whether the current user can edit a protected property in the context of
     * a given node.
     * <p> 
     * If the property is not protected then returns true.
     * 
     * @param nodeRef   node reference
     * @param property  name of the property
     * @return boolean  true if the current user can edit the protected property or the property
     *                  is not protected, false otherwise
     */
    boolean canEditProtectedProperty(NodeRef nodeRef, QName property);
    
    /**
     * Indicates whether an aspect is protected or not.
     * 
     * @param aspect    aspect name
     * @return boolean  true if aspect is protected, false otherwise
     */
    boolean isProtectedAspect(QName aspect);
    
    /**
     * Get the protected aspects.
     * 
     * @return  {@link Set}<{@link QName}>  all the protected aspects
     */
    Set<QName> getProtectedAspects();  
    
    /**
     * Get the details of the protected aspect, returns null if aspect is
     * not protected.
     * 
     * @param name  name of the aspect
     * @return {@link ProtectedAspect}  protected aspect details, null otherwise
     */
    ProtectedAspect getProtectedAspect(QName name);
    
    /**
     * Indicates whether the current user can edit (ie add or remove) a protected 
     * aspect in the context of a given node.
     * <p>
     * If the aspect is not protected then returns true.
     * 
     * @param nodeRef   node reference
     * @param aspect    name of the of aspect
     * @return boolean  true if the current user can edit the protected aspect or the the
     *                  aspect is not protected, false otherwise
     */
    boolean canEditProtectedAspect(NodeRef nodeRef, QName aspect);
}
