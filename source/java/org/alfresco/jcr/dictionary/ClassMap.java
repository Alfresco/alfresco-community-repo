/*
 * Copyright (C) 2005 Alfresco, Inc.
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.alfresco.jcr.dictionary;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.jcr.RepositoryException;

import org.alfresco.jcr.session.SessionImpl;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;


/**
 * Responsible for mapping Alfresco Classes to JCR Types / Mixins and vice versa.
 * 
 * @author David Caruana
 */
public class ClassMap
{
    /** Map of Alfresco Class to JCR Class */
    private static Map<QName, QName> JCRToAlfresco = new HashMap<QName, QName>();
    static
    {
        JCRToAlfresco.put(NodeTypeImpl.MIX_REFERENCEABLE, ContentModel.ASPECT_REFERENCEABLE);
        JCRToAlfresco.put(NodeTypeImpl.MIX_LOCKABLE, ContentModel.ASPECT_LOCKABLE);
        JCRToAlfresco.put(NodeTypeImpl.MIX_VERSIONABLE, ContentModel.ASPECT_VERSIONABLE);
    }

    /** Map of JCR Class to Alfresco Class */
    private static Map<QName, QName> AlfrescoToJCR = new HashMap<QName, QName>();
    static
    {
        AlfrescoToJCR.put(ContentModel.ASPECT_REFERENCEABLE, NodeTypeImpl.MIX_REFERENCEABLE);
        AlfrescoToJCR.put(ContentModel.ASPECT_LOCKABLE, NodeTypeImpl.MIX_LOCKABLE);
        AlfrescoToJCR.put(ContentModel.ASPECT_VERSIONABLE, NodeTypeImpl.MIX_VERSIONABLE);
    }

    /** Map of JCR to Alfresco "Add Aspect" Behaviours */
    private static Map<QName, AddMixin> addMixin = new HashMap<QName, AddMixin>();
    static
    {
        addMixin.put(ContentModel.ASPECT_VERSIONABLE, new VersionableMixin());
    }
    
    /** Map of JCR to Alfresco "Remove Aspect" Behaviours */
    private static Map<QName, RemoveMixin> removeMixin = new HashMap<QName, RemoveMixin>();
    static
    {
        removeMixin.put(ContentModel.ASPECT_VERSIONABLE, new VersionableMixin());
    }

    /** Default Mixin behaviour **/
    private static DefaultMixin defaultMixin = new DefaultMixin();
    
    
    /**
     * Convert an Alfresco Class to a JCR Type
     * 
     * @param jcrType  JCR Type
     * @return  Alfresco Class
     * @throws RepositoryException
     */
    public static QName convertTypeToClass(QName jcrType)
    {
        return JCRToAlfresco.get(jcrType);
    }

    /**
     * Convert an Alfresco Class to a JCR Type
     * 
     * @param  alfrescoClass  Alfresco Class
     * @return  JCR Type
     * @throws RepositoryException
     */
    public static QName convertClassToType(QName alfrescoClass)
    {
        return JCRToAlfresco.get(alfrescoClass);
    }

    /**
     * Get 'Add Mixin' JCR behaviour
     * 
     * @param alfrescoClass
     * @return  AddMixin behaviour
     */
    public static AddMixin getAddMixin(QName alfrescoClass)
    {
        AddMixin mixin = addMixin.get(alfrescoClass);
        return (mixin == null) ? defaultMixin : mixin;
    }
    
    /**
     * Get 'Remove Mixin' JCR behaviour
     * 
     * @param alfrescoClass
     * @return RemoveMixin behaviour
     */
    public static RemoveMixin getRemoveMixin(QName alfrescoClass)
    {
        RemoveMixin mixin = removeMixin.get(alfrescoClass);
        return (mixin == null) ? defaultMixin : mixin;
    }
    
    /**
     * Add Mixin Behaviour
     * 
     * Encapsulates mapping of JCR behaviour to Alfresco
     */
    public interface AddMixin
    {
        public Map<QName, Serializable> preAddMixin(SessionImpl session, NodeRef nodeRef);
        public void postAddMixin(SessionImpl session, NodeRef nodeRef);
    }

    /**
     * Remove Mixin Behaviour
     * 
     * Encapsulates mapping of JCR behaviour to Alfresco
     */
    public interface RemoveMixin
    {
        public void preRemoveMixin(SessionImpl session, NodeRef nodeRef);
        public void postRemoveMixin(SessionImpl session, NodeRef nodeRef);
    }
    
    
    /**
     * Default NOOP Mixin behaviour
     */
    private static class DefaultMixin implements AddMixin, RemoveMixin
    {
        /*
         *  (non-Javadoc)
         * @see org.alfresco.jcr.dictionary.ClassMap.AddMixin#preAddMixin(org.alfresco.jcr.session.SessionImpl, org.alfresco.service.cmr.repository.NodeRef)
         */
        public Map<QName, Serializable> preAddMixin(SessionImpl session, NodeRef nodeRef)
        {
            return null;
        }

        /*
         *  (non-Javadoc)
         * @see org.alfresco.jcr.dictionary.ClassMap.AddMixin#postAddMixin(org.alfresco.jcr.session.SessionImpl, org.alfresco.service.cmr.repository.NodeRef)
         */
        public void postAddMixin(SessionImpl session, NodeRef nodeRef)
        {
        }

        /*
         *  (non-Javadoc)
         * @see org.alfresco.jcr.dictionary.ClassMap.RemoveMixin#preRemoveMixin(org.alfresco.jcr.session.SessionImpl, org.alfresco.service.cmr.repository.NodeRef)
         */
        public void preRemoveMixin(SessionImpl session, NodeRef nodeRef)
        {
        }

        /*
         *  (non-Javadoc)
         * @see org.alfresco.jcr.dictionary.ClassMap.RemoveMixin#postRemoveMixin(org.alfresco.jcr.session.SessionImpl, org.alfresco.service.cmr.repository.NodeRef)
         */
        public void postRemoveMixin(SessionImpl session, NodeRef nodeRef)
        {
        }
    }
}
