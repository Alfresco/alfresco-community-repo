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
package org.alfresco.repo.thumbnail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.service.cmr.rendition.RenditionDefinition;
import org.alfresco.service.cmr.rendition.RenditionService;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.thumbnail.ThumbnailException;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ApplicationContextEvent;
import org.springframework.extensions.surf.util.AbstractLifecycleBean;

/**
 * Registry of all the thumbnail details available
 * 
 * @author Roy Wetherall
 * @author Neil McErlean
 */
public class ThumbnailRegistry implements ApplicationContextAware, ApplicationListener<ApplicationContextEvent>
{   
    /** Content service */
    private ContentService contentService;
    
    /** Rendition service */
    private RenditionService renditionService;
    
    /** Map of thumbnail definition */
    private Map<String, ThumbnailDefinition> thumbnailDefinitions = new HashMap<String, ThumbnailDefinition>();
    
    /** Cache to store mimetype to thumbnailDefinition mapping */
    private Map<String, List<ThumbnailDefinition>> mimetypeMap = new HashMap<String, List<ThumbnailDefinition>>(17);

    private ThumbnailRenditionConvertor thumbnailRenditionConvertor;
    
    private RegistryLifecycle lifecycle = new RegistryLifecycle();
    
    public void setThumbnailRenditionConvertor(
            ThumbnailRenditionConvertor thumbnailRenditionConvertor)
    {
        this.thumbnailRenditionConvertor = thumbnailRenditionConvertor;
    }

    public ThumbnailRenditionConvertor getThumbnailRenditionConvertor()
    {
        return thumbnailRenditionConvertor;
    }
    
    /**
     * Content service
     * 
     * @param contentService    content service
     */
    public void setContentService(ContentService contentService)
    {
        this.contentService = contentService;
    }
    
    /**
     * Rendition service
     * 
     * @param renditionService    rendition service
     */
    public void setRenditionService(RenditionService renditionService)
    {
        this.renditionService = renditionService;
    }

    /**
     * This method is used to inject the thumbnail definitions.
     * @param thumbnailDefinitions
     */
    public void setThumbnailDefinitions(final List<ThumbnailDefinition> thumbnailDefinitions)
    {
        for (ThumbnailDefinition td : thumbnailDefinitions)
        {
            String thumbnailName = td.getName();
            if (thumbnailName == null)
            {
                throw new ThumbnailException("When adding a thumbnail details object make sure the name is set.");
            }
            
            this.thumbnailDefinitions.put(thumbnailName, td);
        }
    }
    
    /**
     * Those thumbnail definitions that are injected by Spring are converted
     * to rendition definitions and saved.
     */
    private void initThumbnailDefinitions()
    {
        AuthenticationUtil.runAs(new RunAsWork<Void>() {
            public Void doWork() throws Exception
            {
                for (String thumbnailDefName : thumbnailDefinitions.keySet())
                {
                    final ThumbnailDefinition thumbnailDefinition = thumbnailDefinitions.get(thumbnailDefName);
                    
                    // Built-in thumbnailDefinitions do not provide any non-standard values
                    // for the ThumbnailParentAssociationDetails object. Hence the null
                    RenditionDefinition renditionDef = thumbnailRenditionConvertor.convert(thumbnailDefinition, null);
                    
                    renditionService.saveRenditionDefinition(renditionDef);
                }

                return null;
            }
        }, AuthenticationUtil.getSystemUserName());
    }
    
    /**
     * Get a list of all the thumbnail definitions
     * 
     * @return Collection<ThumbnailDefinition>  collection of thumbnail definitions
     */
    public List<ThumbnailDefinition> getThumbnailDefinitions()
    {
        return new ArrayList<ThumbnailDefinition>(this.thumbnailDefinitions.values());
    }
    
    public List<ThumbnailDefinition> getThumbnailDefinitions(String mimetype)
    {
        List<ThumbnailDefinition> result = this.mimetypeMap.get(mimetype);
        
        if (result == null)
        {
            result = new ArrayList<ThumbnailDefinition>(7);
            
            for (ThumbnailDefinition thumbnailDefinition : this.thumbnailDefinitions.values())
            {
                if (this.contentService.getTransformer(
                        mimetype, 
                        thumbnailDefinition.getMimetype(), 
                        thumbnailDefinition.getTransformationOptions()) != null)
                {
                    result.add(thumbnailDefinition);
                }
            }
            
            this.mimetypeMap.put(mimetype, result);
        }
        
        return result;
    }
    
    /**
     * 
     * @param mimetype
     * @return
     * @deprecated Use {@link #getThumbnailDefinitions(String)} instead.
     */
    @Deprecated
    public List<ThumbnailDefinition> getThumnailDefintions(String mimetype)
    {
        return this.getThumbnailDefinitions(mimetype);
    }
    
    /**
     * Add a thumbnail details
     * 
     * @param thumbnailDetails  thumbnail details
     */
    public void addThumbnailDefinition(ThumbnailDefinition thumbnailDetails)
    {
        String thumbnailName = thumbnailDetails.getName();
        if (thumbnailName == null)
        {
            throw new ThumbnailException("When adding a thumbnail details object make sure the name is set.");
        }
        
        this.thumbnailDefinitions.put(thumbnailName, thumbnailDetails);
    }
    
    /**
     * Get the definition of a named thumbnail
     * 
     * @param  thumbnailNam         the thumbnail name
     * @return ThumbnailDetails     the details of the thumbnail
     */
    public ThumbnailDefinition getThumbnailDefinition(String thumbnailName)
    {
        return this.thumbnailDefinitions.get(thumbnailName);
    }

    /* (non-Javadoc)
     * @see org.springframework.context.ApplicationContextAware#setApplicationContext(org.springframework.context.ApplicationContext)
     */
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException
    {
        lifecycle.setApplicationContext(applicationContext);
    }

    /* (non-Javadoc)
     * @see org.springframework.context.ApplicationListener#onApplicationEvent(org.springframework.context.ApplicationEvent)
     */
    public void onApplicationEvent(ApplicationContextEvent event)
    {
        lifecycle.onApplicationEvent(event);
    }
    
    /**
     * This class hooks in to the spring application lifecycle and ensures that any
     * ThumbnailDefinitions injected by spring are converted to RenditionDefinitions
     * and saved.
     */
    private class RegistryLifecycle extends AbstractLifecycleBean
    {
        /* (non-Javadoc)
         * @see org.alfresco.util.AbstractLifecycleBean#onBootstrap(org.springframework.context.ApplicationEvent)
         */
        @Override
        protected void onBootstrap(ApplicationEvent event)
        {
            initThumbnailDefinitions();
        }
    
        /* (non-Javadoc)
         * @see org.alfresco.util.AbstractLifecycleBean#onShutdown(org.springframework.context.ApplicationEvent)
         */
        @Override
        protected void onShutdown(ApplicationEvent event)
        {
            // Intentionally empty
        }
    }
}
