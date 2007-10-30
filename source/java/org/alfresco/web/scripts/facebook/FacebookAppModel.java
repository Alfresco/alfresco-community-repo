package org.alfresco.web.scripts.facebook;

public class FacebookAppModel
{
    String appId;
    String apiKey;    
    String secretKey;
    
    
    public FacebookAppModel(String apiKey)
    {
        this.apiKey = apiKey;
    }
    
    public String getApiKey()
    {
        return apiKey;
    }

    public String getSecret()
    {
        return secretKey;
    }

    public void setSecret(String secretKey)
    {
        this.secretKey = secretKey;
    }

    public String getId()
    {
        return appId;
    }

    public void setId(String appId)
    {
        this.appId = appId;
    }
    
}
