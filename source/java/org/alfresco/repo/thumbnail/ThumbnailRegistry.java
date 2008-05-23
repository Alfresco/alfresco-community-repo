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
package org.alfresco.repo.thumbnail;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.thumbnail.ThumbnailException;

/**
 * @author Roy Wetherall
 */
public class ThumbnailRegistry
{   
    /** Map of thumbnail details */
    private Map<String, ThumbnailDetails> thumbnailDetails = new HashMap<String, ThumbnailDetails>(10);
       
    /**
     * Add a number of thumbnail details
     * 
     * @param thumbnailDetails  list of thumbnail details
     */
    public void setThumbnailDetails(List<ThumbnailDetails> thumbnailDetails)
    {
        for (ThumbnailDetails value : thumbnailDetails)
        {
            addThumbnailDetails(value);
        }
    }
    
    /**
     * Add a thumnail details
     * 
     * @param thumbnailDetails  thumbnail details
     */
    public void addThumbnailDetails(ThumbnailDetails thumbnailDetails)
    {
        String thumbnailName = thumbnailDetails.getName();
        if (thumbnailName == null)
        {
            throw new ThumbnailException("When adding a thumbnail details object make sure the name is set.");
        }
        
        this.thumbnailDetails.put(thumbnailName, thumbnailDetails);
    }
    
    /**
     * Get the details of a named thumbnail
     * 
     * @param  thumbnailNam         the thumbnail name
     * @return ThumbnailDetails     the details of the thumbnail
     */
    public ThumbnailDetails getThumbnailDetails(String thumbnailName)
    {
        return this.thumbnailDetails.get(thumbnailName);
    }
    
//    /**
//     * 
//     * @param node
//     * @param contentProperty
//     * @param thumbnailName
//     * @return
//     */
//    public NodeRef createThumbnail(NodeRef node, QName contentProperty, String thumbnailName)
//    {
//        // Check to see if we have details of the thumbnail in our list
//        ThumbnailDetails details = getThumbnailDetails(thumbnailName);
//        if (details == null)
//        {
//            throw new ThumbnailException("The thumbnail name '" + thumbnailName + "' is not recognised");
//        }
//        
//        // Create the thumbnail
//        return this.thumbnailService.createThumbnail(
//                node, 
//                contentProperty, 
//                details.getMimetype(), 
//                details.getTransformationOptions(), 
//                thumbnailName);
//    }
}
