/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.repo.publishing.slideshare;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.benfante.jslideshare.DocumentParser;
import com.benfante.jslideshare.DocumentParserResult;
import com.benfante.jslideshare.SlideShareConnector;
import com.benfante.jslideshare.SlideShareErrorException;
import com.benfante.jslideshare.SlideShareException;
import com.benfante.jslideshare.messages.Group;
import com.benfante.jslideshare.messages.Slideshow;
import com.benfante.jslideshare.messages.SlideshowInfo;
import com.benfante.jslideshare.messages.Tag;
import com.benfante.jslideshare.messages.User;

/**
 * 
 * @author Brian
 * @since 4.0
 */
public class SlideShareApiImpl implements SlideShareApi
{
    private static final Log logger = LogFactory.getLog(SlideShareApiImpl.class);

    public static final String URL_GET_SLIDESHOW = "URL_GET_SLIDESHOW";
    public static final String URL_GET_SLIDESHOW_INFO = "URL_GET_SLIDESHOW_INFO";
    public static final String URL_GET_SLIDESHOW_BY_USER = "URL_GET_SLIDESHOW_BY_USER";
    public static final String URL_GET_SLIDESHOW_BY_TAG = "URL_GET_SLIDESHOW_BY_TAG";
    public static final String URL_GET_SLIDESHOW_BY_GROUP = "URL_GET_SLIDESHOW_BY_GROUP";
    public static final String URL_UPLOAD_SLIDESHOW = "URL_UPLOAD_SLIDESHOW";
    public static final String URL_DELETE_SLIDESHOW = "URL_DELETE_SLIDESHOW";

    private static Map<String, String> DEFAULT_API_URLS = new TreeMap<String, String>();

    static
    {
        DEFAULT_API_URLS.put(URL_GET_SLIDESHOW, "http://www.slideshare.net/api/2/get_slideshow");
        DEFAULT_API_URLS.put(URL_GET_SLIDESHOW_INFO, "http://www.slideshare.net/api/2/get_slideshow");
        DEFAULT_API_URLS.put(URL_GET_SLIDESHOW_BY_USER, "http://www.slideshare.net/api/2/get_slideshow_by_user");
        DEFAULT_API_URLS.put(URL_GET_SLIDESHOW_BY_TAG, "http://www.slideshare.net/api/2/get_slideshow_by_tag");
        DEFAULT_API_URLS.put(URL_GET_SLIDESHOW_BY_GROUP, "http://www.slideshare.net/api/2/get_slideshow_from_group");
        DEFAULT_API_URLS.put(URL_UPLOAD_SLIDESHOW, "http://www.slideshare.net/api/2/upload_slideshow");
        DEFAULT_API_URLS.put(URL_DELETE_SLIDESHOW, "http://www.slideshare.net/api/2/delete_slideshow");
    }

    private Map<String, String> apiUrls = new TreeMap<String, String>(DEFAULT_API_URLS);

    protected SlideShareConnector connector;

    private String username;

    private String password;

    public SlideShareApiImpl()
    {
    }

    public SlideShareApiImpl(SlideShareConnector connector)
    {
        this.connector = connector;
    }

    public SlideShareConnector getConnector()
    {
        return connector;
    }

    public void setConnector(SlideShareConnector connector)
    {
        this.connector = connector;
    }

    public void setApiUrls(Map<String, String> urls)
    {
        if (urls == null || !urls.keySet().containsAll(DEFAULT_API_URLS.keySet()))
        {
            throw new IllegalArgumentException("Specified URL set is missing one or more values. Expected "
                    + DEFAULT_API_URLS.keySet() + "; Received " + (urls == null ? urls : urls.keySet()));
        }
    }

    public Slideshow getSlideshow(String id) throws SlideShareException, SlideShareErrorException
    {
        logger.info("Called getSlideshow with id=" + id);
        Map<String, String> parameters = new HashMap<String, String>();
        addParameter(parameters, "slideshow_id", id);
        return sendMessage(URL_GET_SLIDESHOW, parameters).getSlideShow();
    }

    public SlideshowInfo getSlideshowInfo(String id, String url) throws SlideShareException, SlideShareErrorException
    {
        logger.info("Called getSlideshowInfo with id=" + id + ", url=" + url);
        Map<String, String> parameters = new HashMap<String, String>();
        addParameter(parameters, "slideshow_id", id);
        addParameter(parameters, "slideshow_url", url);
        return sendGetMessage(URL_GET_SLIDESHOW_INFO, parameters).getSlideShowInfo();
    }

    public User getSlideshowByUser(String username) throws SlideShareException, SlideShareErrorException
    {
        logger.info("Called getSlideshowByUser with username=" + username);
        return getSlideshowByUser(username, -1, -1);
    }

    public User getSlideshowByUser(String username, int offset, int limit) throws SlideShareException,
            SlideShareErrorException
    {
        logger.info("Called getSlideshowByUser with username=" + username + ", offset=" + offset + ", limit=" + limit);
        Map<String, String> parameters = new HashMap<String, String>();
        addParameter(parameters, "username_for", username);
        addLimits(parameters, offset, limit);
        return sendMessage(URL_GET_SLIDESHOW_BY_USER, parameters).getUser();
    }

    public Tag getSlideshowByTag(String tag) throws SlideShareException, SlideShareErrorException
    {
        logger.info("Called getSlideshowByTag with tag=" + tag);
        return getSlideshowByTag(tag, -1, -1);
    }

    public Tag getSlideshowByTag(String tag, int offset, int limit) throws SlideShareException,
            SlideShareErrorException
    {
        logger.info("Called getSlideshowByTag with tag=" + tag + ", offset=" + offset + ", limit=" + limit);
        Map<String, String> parameters = new HashMap<String, String>();
        addParameter(parameters, "tag", tag);
        addLimits(parameters, offset, limit);
        return sendMessage(URL_GET_SLIDESHOW_BY_TAG, parameters).getTag();
    }

    public Group getSlideshowByGroup(String groupName) throws SlideShareException, SlideShareErrorException
    {
        logger.info("Called getSlideshowByGroup with groupName=" + groupName);
        return getSlideshowByGroup(groupName, -1, -1);
    }

    public Group getSlideshowByGroup(String groupName, int offset, int limit) throws SlideShareException,
            SlideShareErrorException
    {
        logger
                .info("Called getSlideshowByGrop with groupName=" + groupName + ", offset=" + offset + ", limit="
                        + limit);
        Map<String, String> parameters = new HashMap<String, String>();
        addParameter(parameters, "group_name", groupName);
        addLimits(parameters, offset, limit);
        return sendMessage(URL_GET_SLIDESHOW_BY_GROUP, parameters).getGroup();
    }

    public String uploadSlideshow(String username, String password, String title, File src, String description,
            String tags, boolean makeSrcPublic, boolean makeSlideshowPrivate, boolean generateSecretUrl,
            boolean allowEmbeds, boolean shareWithContacts) throws SlideShareException, SlideShareErrorException
    {
        logger.info("Called uploadSlideshow with username=" + username + ", password=XXX, title=" + title
                + ", description=" + description + ", tags=" + tags + ", makeSrcPublic=" + makeSrcPublic
                + ", makeSlideshowPrivate=" + makeSlideshowPrivate + ", generateSecretUrl=" + generateSecretUrl
                + ", allowEmbeds=" + allowEmbeds + ", shareWithContacts=" + shareWithContacts);
        Map<String, String> parameters = new HashMap<String, String>();
        addParameter(parameters, "username", username);
        addParameter(parameters, "password", password);
        addParameter(parameters, "slideshow_title", title);
        addParameter(parameters, "slideshow_description", description);
        addParameter(parameters, "slideshow_tags", tags);
        addParameter(parameters, "make_src_public", makeSrcPublic);
        addParameter(parameters, "make_slideshow_private", makeSlideshowPrivate);
        addParameter(parameters, "generate_secret_url", generateSecretUrl);
        addParameter(parameters, "allow_embeds", allowEmbeds);
        addParameter(parameters, "share_with_contacts", shareWithContacts);
        Map<String, File> files = new HashMap<String, File>();
        files.put("slideshow_srcfile", src);
        return sendMessage(URL_UPLOAD_SLIDESHOW, parameters, files).getSlideShowId();
    }

    public String deleteSlideshow(String username, String password, String id) throws SlideShareException,
            SlideShareErrorException
    {
        logger.info("Called deleteSlideshow with username=" + username + ", password=XXX, id=" + id);
        Map<String, String> parameters = new HashMap<String, String>();
        addParameter(parameters, "username", username);
        addParameter(parameters, "password", password);
        addParameter(parameters, "slideshow_id", id);
        return sendGetMessage(URL_DELETE_SLIDESHOW, parameters).getSlideShowId();
    }

    private Map<String, String> addParameter(Map<String, String> parameters, String name, String value)
    {
        if (value != null)
        {
            parameters.put(name, value);
        }
        return parameters;
    }

    private Map<String, String> addParameter(Map<String, String> parameters, String name, boolean value)
    {
        parameters.put(name, value ? "Y" : "N");
        return parameters;
    }

    private Map<String, String> addLimits(Map<String, String> parameters, int offset, int limit)
    {
        if (offset >= 0)
        {
            parameters.put("offset", Integer.toString(offset));
        }
        if (limit >= 0)
        {
            parameters.put("limit", Integer.toString(limit));
        }
        return parameters;
    }

    private DocumentParserResult sendMessage(String url, Map<String, String> parameters)
            throws SlideShareErrorException
    {
        addCredentials(parameters);
        DocumentParserResult result;
        try
        {
            InputStream response = connector.sendMessage(apiUrls.get(url), parameters);
            result = DocumentParser.parse(response);
        }
        catch (IOException iOException)
        {
            throw new SlideShareErrorException(-1, "Error sending a message to the url " + apiUrls.get(url), iOException);
        }
        return result;
    }

    private DocumentParserResult sendGetMessage(String url, Map<String, String> parameters)
            throws SlideShareErrorException
    {
        addCredentials(parameters);
        DocumentParserResult result;
        try
        {
            InputStream response = connector.sendGetMessage(apiUrls.get(url), parameters);
            result = DocumentParser.parse(response);
        }
        catch (IOException iOException)
        {
            throw new SlideShareErrorException(-1, "Error sending a message to the url " + apiUrls.get(url), iOException);
        }
        return result;
    }

    private DocumentParserResult sendMessage(String url, Map<String, String> parameters, Map<String, File> files)
            throws SlideShareErrorException
    {
        addCredentials(parameters);
        DocumentParserResult result;
        try
        {
            InputStream response = connector.sendMultiPartMessage(apiUrls.get(url), parameters, files);
            result = DocumentParser.parse(response);
        }
        catch (IOException iOException)
        {
            throw new SlideShareErrorException(-1, "Error sending a multipart message to the url " + apiUrls.get(url), iOException);
        }
        return result;
    }

    private void addCredentials(Map<String, String> parameters)
    {
        if (username != null && password != null)
        {
            addParameter(parameters, "username", username);
            addParameter(parameters, "password", password);
        }
    }
    
    public void setUsername(String username)
    {
        this.username = username;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }
}
