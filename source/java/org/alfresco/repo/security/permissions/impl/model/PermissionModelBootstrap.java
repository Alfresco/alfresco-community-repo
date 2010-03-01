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
