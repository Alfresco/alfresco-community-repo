package org.alfresco.repo.publishing.slideshare;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.node.encryption.MetadataEncryptor;
import org.alfresco.repo.publishing.PublishingModel;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;

import com.benfante.jslideshare.SlideShareAPI;
import com.benfante.jslideshare.SlideShareConnector;

/**
 * 
 * @author Brian
 * @since 4.0
 */
public class SlideSharePublishingHelper
{
    private final static Map<String,String> DEFAULT_MIME_TYPES = new TreeMap<String,String>(); 
    static
    {
        DEFAULT_MIME_TYPES.put(MimetypeMap.MIMETYPE_PPT, ".ppt");
        DEFAULT_MIME_TYPES.put(MimetypeMap.MIMETYPE_PDF, ".pdf");
        DEFAULT_MIME_TYPES.put(MimetypeMap.MIMETYPE_OPENDOCUMENT_PRESENTATION, ".odp");
        DEFAULT_MIME_TYPES.put(MimetypeMap.MIMETYPE_OPENXML_PRESENTATION, ".pptx");
        DEFAULT_MIME_TYPES.put(MimetypeMap.MIMETYPE_IWORK_KEYNOTE, "");
        DEFAULT_MIME_TYPES.put(MimetypeMap.MIMETYPE_IWORK_PAGES, "");
        DEFAULT_MIME_TYPES.put(MimetypeMap.MIMETYPE_TEXT_PLAIN, ".txt");
        DEFAULT_MIME_TYPES.put(MimetypeMap.MIMETYPE_OPENDOCUMENT_TEXT, ".odt");
        DEFAULT_MIME_TYPES.put(MimetypeMap.MIMETYPE_TEXT_CSV, ".csv");
        DEFAULT_MIME_TYPES.put(MimetypeMap.MIMETYPE_EXCEL, ".xls");
        DEFAULT_MIME_TYPES.put(MimetypeMap.MIMETYPE_OPENXML_WORDPROCESSING, ".docx");
        DEFAULT_MIME_TYPES.put(MimetypeMap.MIMETYPE_OPENDOCUMENT_SPREADSHEET, ".ods");
    }
    
    private Map<String, String> allowedMimeTypes = Collections.unmodifiableMap(DEFAULT_MIME_TYPES);
    private SlideShareConnector slideshareConnector;
    private MetadataEncryptor encryptor;
    
    public void setSlideshareConnector(SlideShareConnector slideshareConnector)
    {
        this.slideshareConnector = slideshareConnector;
    }

    public Map<String, String> getAllowedMimeTypes()
    {
        return allowedMimeTypes;
    }

    public void setAllowedMimeTypes(Map<String, String> allowedMimeTypes)
    {
        this.allowedMimeTypes = Collections.unmodifiableMap(allowedMimeTypes);
    }

    public void setEncryptor(MetadataEncryptor encryptor)
    {
        this.encryptor = encryptor;
    }

    public SlideShareAPI getSlideShareApi()
    {
        return createApiObject();
    }
    
    private SlideShareApiImpl createApiObject()
    {
        return new SlideShareApiImpl(slideshareConnector);
    }
    
    public Pair<String, String> getSlideShareCredentialsFromChannelProperties(Map<QName, Serializable> channelProperties)
    {
        Pair<String, String> result = null;
        String username = (String) encryptor.decrypt(PublishingModel.PROP_CHANNEL_USERNAME, 
                channelProperties.get(PublishingModel.PROP_CHANNEL_USERNAME));
        String password = (String) encryptor.decrypt(PublishingModel.PROP_CHANNEL_PASSWORD, 
                channelProperties.get(PublishingModel.PROP_CHANNEL_PASSWORD));
        if (username != null && password != null)
        {
            result = new Pair<String, String>(username, password);
        }
        return result;
    }

    public SlideShareApi getSlideShareApi(String username, String password)
    {
        SlideShareApiImpl api = createApiObject();
        api.setUsername(username);
        api.setPassword(password);
        return api;
    }

}
