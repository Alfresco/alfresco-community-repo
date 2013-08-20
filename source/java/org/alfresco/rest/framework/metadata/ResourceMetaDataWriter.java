package org.alfresco.rest.framework.metadata;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import org.alfresco.rest.framework.core.ResourceWithMetadata;

/**
 * Writes out the metadata for resources in the required format.  The resources will already be selected based on api version
 *
 * @author Gethin James
 */
public interface ResourceMetaDataWriter
{
    /**
     * Write the metadata to the OutputStream
     * @param out OutputStream
     * @param resource - the selected resource
     * @param allApiResources - all resources for the API version
     * @throws IOException
     */
    public void writeMetaData(OutputStream out, ResourceWithMetadata resource, Map<String, ResourceWithMetadata> allApiResources) throws IOException;
}
