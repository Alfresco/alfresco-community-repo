package org.alfresco.rest.framework.tests.api.mocks3;

import org.alfresco.rest.framework.resource.content.BinaryProperty;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.codehaus.jackson.annotate.JsonIgnore;

/**
 * A small Flock that doesn't want to have a Photo.
 *
 * @author Gethinjames
 */
public class CameraShyFlocket extends Flocket
{

    @Override
    @JsonIgnore
    public BinaryProperty getPhoto()
    {
        return super.getPhoto();
    }

    @Override
    @JsonIgnore
    public CollectionWithPagingInfo<BinaryProperty> getPhotoAlbum()
    {
        return super.getPhotoAlbum();
    }
    
    
}
