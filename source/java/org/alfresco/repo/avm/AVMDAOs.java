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
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>. */

package org.alfresco.repo.avm;

import org.alfresco.repo.domain.contentdata.ContentDataDAO;
import org.alfresco.repo.domain.permissions.AclDAO;

/**
 * This is the (shudder) global context for AVM.  It a rendezvous
 * point for access to needed global instances.
 * @author britt
 */
public class AVMDAOs
{
    /**
     * The single instance of an AVMContext.
     */
    private static final AVMDAOs fgInstance = new AVMDAOs();
    
    private AVMDAOs()
    {
    }

    /**
     * Get the instance of this.
     * @return
     */
    public static AVMDAOs Instance()
    {
        return fgInstance;
    }
    
    /**
     * The AVMNodeDAO.
     */
    public AVMNodeDAO fAVMNodeDAO;
    
    public org.alfresco.repo.domain.avm.AVMNodeDAO newAVMNodeDAO;
    public org.alfresco.repo.domain.avm.AVMNodeLinksDAO newAVMNodeLinksDAO;
    public ContentDataDAO contentDataDAO;
    
    /**
     *  The AVMStore DAO.
     */
    public AVMStoreDAO fAVMStoreDAO;
    
    public org.alfresco.repo.domain.avm.AVMStoreDAO newAVMStoreDAO;
    
    /**
     * The VersionRootDAO.
     */
    public VersionRootDAO fVersionRootDAO;
    
    public org.alfresco.repo.domain.avm.AVMVersionRootDAO newAVMVersionRootDAO;
    
    /**
     * The ChildEntryDAO.
     */
    public ChildEntryDAO fChildEntryDAO;
    
    /**
     * The AVMStorePropertyDAO
     */
    public AVMStorePropertyDAO fAVMStorePropertyDAO;
    
    public VersionLayeredNodeEntryDAO fVersionLayeredNodeEntryDAO;
    
    public AclDAO fAclDAO;
    
    /**
     * @param nodeDAO the fAVMNodeDAO to set
     */
    public void setNodeDAO(AVMNodeDAO nodeDAO)
    {
        fAVMNodeDAO = nodeDAO;
    }
    
    public void setNewAvmNodeDAO(org.alfresco.repo.domain.avm.AVMNodeDAO newAVMNodeDAO)
    {
        this.newAVMNodeDAO = newAVMNodeDAO;
    }
    
    public void setNewAvmNodeLinksDAO(org.alfresco.repo.domain.avm.AVMNodeLinksDAO newAVMNodeLinksDAO)
    {
        this.newAVMNodeLinksDAO = newAVMNodeLinksDAO;
    }

    public void setContentDataDAO(ContentDataDAO contentDataDAO)
    {
        this.contentDataDAO = contentDataDAO;
    }

    /**
     * @param childEntryDAO the fChildEntryDAO to set
     */
    public void setChildEntryDAO(ChildEntryDAO childEntryDAO)
    {
        fChildEntryDAO = childEntryDAO;
    }
    
    /**
     * @param aVMStoreDAO The fAVMStoreDAO to set
     */
    public void setAvmStoreDAO(AVMStoreDAO aVMStoreDAO)
    {
        fAVMStoreDAO = aVMStoreDAO;
    }
    
    public void setNewAvmStoreDAO(org.alfresco.repo.domain.avm.AVMStoreDAO newAVMStoreDAO)
    {
        this.newAVMStoreDAO = newAVMStoreDAO;
    }

    /**
     * @param versionRootDAO the fVersionRootDAO to set
     */
    public void setVersionRootDAO(VersionRootDAO versionRootDAO)
    {
        fVersionRootDAO = versionRootDAO;
    }
    
    public void setNewAvmVersionRootDAO(org.alfresco.repo.domain.avm.AVMVersionRootDAO newAVMVersionRootDAO)
    {
        this.newAVMVersionRootDAO = newAVMVersionRootDAO;
    }
    
    public void setAvmStorePropertyDAO(AVMStorePropertyDAO avmStorePropertyDAO)
    {
        fAVMStorePropertyDAO = avmStorePropertyDAO;
    }
    
    public void setVersionLayeredNodeEntryDAO(VersionLayeredNodeEntryDAO dao)
    {
        fVersionLayeredNodeEntryDAO = dao;
    }
    
    public void setAclDAO(AclDAO dao)
    {
        fAclDAO = dao;
    }
}
