package org.alfresco.rest.framework.tests.api.mocks3;

import org.alfresco.rest.framework.resource.content.BinaryProperty;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * A small Flock
 *
 * @author Gethinjames
 */
public class Flocket extends Flock
{
    @JsonProperty (value="album")
    CollectionWithPagingInfo<BinaryProperty> photoAlbum;
    
    public Flocket()
    {
        super();
        setQuantity(6);
    }

    public CollectionWithPagingInfo<BinaryProperty> getPhotoAlbum()
    {
        return this.photoAlbum;
    }

    public void setPhotoAlbum(CollectionWithPagingInfo<BinaryProperty> photoAlbum)
    {
        this.photoAlbum = photoAlbum;
    }
}
