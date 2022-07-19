package org.alfresco.rest.requests;

import org.alfresco.rest.core.RestRequest;
import org.alfresco.rest.core.RestWrapper;
import org.alfresco.rest.model.RestDownloadsModel;
import org.springframework.http.HttpMethod;


/**
 * Methods for Rest API under the /downloads path
 */

public class Downloads extends ModelRequest<Downloads> {

    RestDownloadsModel downloadsModel;

    public Downloads(RestWrapper restWrapper)
    {
        super(restWrapper);
    }

    public Downloads(RestDownloadsModel downloadsModel, RestWrapper restWrapper)
    {
        super(restWrapper);
        this.downloadsModel = downloadsModel;
    }

    /**
     * Get download details using POST call on "downloads"
     */
    public RestDownloadsModel createDownload(String postBody)
    {
        RestRequest request = RestRequest.requestWithBody(HttpMethod.POST, postBody, "downloads");
        return restWrapper.processModel(RestDownloadsModel.class, request);
    }

    /**
     * Get download details using GET call on "downloads/{downloadId}"
     */
    public RestDownloadsModel getDownload()
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, "downloads/{downloadId}", downloadsModel.getId());
        return restWrapper.processModel(RestDownloadsModel.class, request);
    }

    /**
     * Cancel download using DELETE call on "downloads/{downloadId}"
     */
    public void cancelDownload()
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.DELETE, "downloads/{downloadId}", downloadsModel.getId());
        restWrapper.processEmptyModel(request);;
    }
}
