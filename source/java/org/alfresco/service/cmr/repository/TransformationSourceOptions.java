package org.alfresco.service.cmr.repository;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.alfresco.api.AlfrescoPublicApi;
import org.alfresco.repo.rendition.executer.AbstractRenderingEngine;
import org.alfresco.service.cmr.rendition.RenditionService;
import org.alfresco.service.cmr.repository.PagedSourceOptions;

/**
 * Defines options and demarcations needed to describe the details of how
 * the source should be transformed, independent of the target requirements.
 * <p>
 * See {@link PagedSourceOptions} for an example implementation that
 * describes the page number that should be used from the source content.
 * 
 * @author Ray Gauss II
 */
@AlfrescoPublicApi
public interface TransformationSourceOptions
{
    
    /**
     * Gets the list of applicable mimetypes
     * 
     * @return the applicable mimetypes
     * @deprecated Use {@link #getApplicableMimetypes()} instead.
     */
    public List<String> getApplicabledMimetypes();
    
    /**
     * Gets the list of applicable mimetypes
     * 
     * @return the applicable mimetypes
     */
    public List<String> getApplicableMimetypes();

    /**
     * Gets whether or not these transformation source options apply for the
     * given mimetype
     * 
     * @param mimetype the mimetype of the source
     * @return if these transformation source options apply
     */
    public boolean isApplicableForMimetype(String mimetype);
    
    /**
     * Creates a new <code>TransformationSourceOptions</code> object from this
     * one, merging any non-null overriding fields in the given
     * <code>overridingOptions</code>
     * 
     * @param overridingOptions TransformationSourceOptions
     * @return a merged <code>TransformationSourceOptions</code> object
     */
    public TransformationSourceOptions mergedOptions(TransformationSourceOptions overridingOptions);
    
    /**
     * Gets the serializer for the source options.
     * 
     * @return the serializer
     */
    public TransformationSourceOptionsSerializer getSerializer();
    
    /**
     * Defines methods for serializing the source options into a parameter map and
     * deserializing from a serialized options accessor.
     * <p>
     * This is primarily used when interacting with the {@link RenditionService}
     * with {@link AbstractRenderingEngine}'s RenderContext being an implementer
     * of this interface.
     */
    @AlfrescoPublicApi
    public interface TransformationSourceOptionsSerializer
    {
        
        /**
         * Serializes the given transformation source options into the given parameter map.
         * 
         * @param transformationSourceOptions TransformationSourceOptions
         * @param parameters Map<String, Serializable>
         */
        public void serialize(TransformationSourceOptions transformationSourceOptions, Map<String, Serializable> parameters);
        
        /**
         * Gets the parameters from the serialized options accessor and builds a source options object.
         * 
         * @param serializedOptions SerializedTransformationOptionsAccessor
         * @return the deserialized source options
         */
        public TransformationSourceOptions deserialize(SerializedTransformationOptionsAccessor serializedOptions);
        
    }

}


