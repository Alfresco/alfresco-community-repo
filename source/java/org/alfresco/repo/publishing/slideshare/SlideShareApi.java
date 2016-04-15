package org.alfresco.repo.publishing.slideshare;

import com.benfante.jslideshare.SlideShareAPI;
import com.benfante.jslideshare.SlideShareErrorException;
import com.benfante.jslideshare.SlideShareException;

/**
 * 
 * @author Brian
 * @since 4.0
 */
public interface SlideShareApi extends SlideShareAPI
{
    String deleteSlideshow(String username, String password, String id) throws SlideShareException,
            SlideShareErrorException;
}
