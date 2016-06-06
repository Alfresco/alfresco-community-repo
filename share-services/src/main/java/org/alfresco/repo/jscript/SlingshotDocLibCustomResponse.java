package org.alfresco.repo.jscript;

import org.alfresco.repo.jscript.app.CustomResponse;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.Map;

/**
 * Populates DocLib webscript response with custom metadata output
 *
 * @author mikeh
 */
public final class SlingshotDocLibCustomResponse extends BaseScopableProcessorExtension
{
    private Map<String, Object> customResponses;

    /**
     * Set the custom response beans
     *
     * @param customResponses
     */
    public void setCustomResponses(Map<String, Object> customResponses)
    {
        this.customResponses = customResponses;
    }

    /**
     * Returns a JSON string to be added to the DocLib webscript response.
     *
     * @return The JSON string
     */
    public String getJSON()
    {
        return this.getJSONObj().toString();
    }

    /**
     * Returns a JSON object to be added to the DocLib webscript response.
     *
     * @return The JSON object
     */
    protected Object getJSONObj()
    {
        JSONObject json = new JSONObject();


        for (Map.Entry<String, Object> entry : this.customResponses.entrySet())
        {
            try
            {
                Serializable response = ((CustomResponse) entry.getValue()).populate();
                json.put(entry.getKey(), response == null ? JSONObject.NULL: response);
            }
            catch (JSONException error)
            {
                error.printStackTrace();
            }
        }

        return json;
    }
}
