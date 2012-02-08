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
package org.alfresco.repo.web.scripts.content;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.MimetypeService;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;


/**
 * Returns the descriptions of all the mimetypes known to the system.
 * This is normally used so that things outside of the repo tier (eg Share)
 *  can display user-facing descriptions of the mimetypes in the system.
 * 
 * @author Nick Burch
 * @since 4.0.1
 */
public class MimetypeDescriptionsGet extends DeclarativeWebScript
{
    public static final String MODEL_MIMETYPES = "mimetypes";
    public static final String MODEL_DEFAULT_EXTENSIONS = "defaultExtensions";
    public static final String MODEL_OTHER_EXTENSIONS = "otherExtensions";
   
    private MimetypeService mimetypeService;

    /**
     * Sets the Mimetype Service to be used to get the
     *  list of mime types
     */
    public void setMimetypeService(MimetypeService mimetypeService) {
       this.mimetypeService = mimetypeService;
    }
    
    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
    {
       // Get the mimetypes and their descriptions
       Map<String,String> mimetypes = mimetypeService.getDisplaysByMimetype();
       
       // Fetch all the extensions known to the system
       Map<String,String> extensions = mimetypeService.getMimetypesByExtension();
       //  And the default extensions
       Map<String,String> defaultExtensions = mimetypeService.getExtensionsByMimetype();
       
       // For each mimetype, work out the non-default extensions
       Map<String, List<String>> otherExtensions = new HashMap<String, List<String>>();
       for (String extension : extensions.keySet())
       {
           String mimetype = extensions.get(extension);
           
           // If this isn't the default, record it
           if (! extension.equals(defaultExtensions.get(mimetype)))
           {
               if (! otherExtensions.containsKey(mimetype))
               {
                   otherExtensions.put(mimetype, new ArrayList<String>());
               }
               otherExtensions.get(mimetype).add(extension);
           }
       }

       // Return the model
       Map<String, Object> model = new HashMap<String, Object>();
       model.put(MODEL_MIMETYPES, mimetypes);
       model.put(MODEL_DEFAULT_EXTENSIONS, defaultExtensions);
       model.put(MODEL_OTHER_EXTENSIONS, otherExtensions);
       return model;
    }
}