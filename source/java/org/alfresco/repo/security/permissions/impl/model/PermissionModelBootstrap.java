package org.alfresco.repo.security.permissions.impl.model;

/**
 * Bootstrap bean used to add additional permission models
 * 
 * @author Roy Wetherall
 */
public class PermissionModelBootstrap
{
    /** Permission model */
    private PermissionModel permissionModel;
    
    /** Path to the permission model */
    private String model;
    
    /** 
     * Set the permission model
     * 
     * @param permissionModel   the permission model
     */
    public void setPermissionModel(PermissionModel permissionModel)
    {
        this.permissionModel = permissionModel;
    }
    
    /**
     * Sets the path to the permission model
     * 
     * @param model     path to the permission model
     */
    public void setModel(String model)
    {
        this.model = model;
    }
    
    /**
     * Initialise method that adds the permission models
     */    
    public void init()
    {
        // Initialise the permission model
        this.permissionModel.addPermissionModel(this.model);
    }
}
