package org.alfresco.repo.jscript;

import java.text.MessageFormat;

import org.alfresco.repo.jscript.app.JSONConversionComponent;
import org.springframework.extensions.surf.util.URLEncoder;

/**
 * Utility functions specifically for external application use.
 * 
 * @author Mike Hatfield
 */

public final class ApplicationScriptUtils extends BaseScopableProcessorExtension
{
    /** Content download API URL */
    private final static String CONTENT_DOWNLOAD_API_URL = "/api/node/content/{0}/{1}/{2}/{3}";
    
    /** JSON conversion component */
    private JSONConversionComponent jsonConversionComponent;

    /**
     * @param jsonConversionComponent   JSON conversion component
     */
    public void setJsonConversionComponent(JSONConversionComponent jsonConversionComponent)
    {
        this.jsonConversionComponent = jsonConversionComponent;
    }
    
    /**
     * Returns the JSON representation of a node. Long-form QNames are used in the
     * result.
     * 
     * @param node the node to convert to JSON representation.
     * @return The JSON representation of this node
     */
    public String toJSON(ScriptNode node)
    {
        return this.toJSON(node, false);
    }

    /**
     * Returns the JSON representation of this node.
     * 
     * @param node the node to convert to JSON representation.
     * @param useShortQNames if true short-form qnames will be returned, else long-form.
     * @return The JSON representation of this node
     */
    public String toJSON(ScriptNode node, boolean useShortQNames)
    {
        return jsonConversionComponent.toJSON(node.getNodeRef(), useShortQNames);        
    }

    /**
     * @param  node the node to construct the download URL for
     * @return For a content document, this method returns the URL to the /api/node/content
     *         API for the default content property
     *         <p>
     *         For a container node, this method returns an empty string
     *         </p>
     */
    public String getDownloadAPIUrl(ScriptNode node)
    {
        if (node.getIsDocument())
        {
           return MessageFormat.format(CONTENT_DOWNLOAD_API_URL, new Object[]{
                   node.nodeRef.getStoreRef().getProtocol(),
                   node.nodeRef.getStoreRef().getIdentifier(),
                   node.nodeRef.getId(),
                   URLEncoder.encode(node.getName())});
        }
        else
        {
            return "";
        }
    }

}
