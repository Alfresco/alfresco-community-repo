package org.alfresco.repo.publishing.linkedin.springsocial.api.impl;

import java.util.List;

import org.alfresco.repo.publishing.JaxbHttpMessageConverter;
import org.alfresco.repo.publishing.linkedin.springsocial.api.Activity;
import org.alfresco.repo.publishing.linkedin.springsocial.api.AlfrescoLinkedIn;
import org.alfresco.repo.publishing.linkedin.springsocial.api.Share;
import org.alfresco.repo.publishing.linkedin.springsocial.api.impl.xml.JaxbActivityImpl;
import org.alfresco.repo.publishing.linkedin.springsocial.api.impl.xml.JaxbShareImpl;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.social.linkedin.api.impl.LinkedInTemplate;
import org.springframework.social.support.URIBuilder;

/**
 * 
 * @author Brian
 * @since 4.0
 */
public class AlfrescoLinkedInTemplate extends LinkedInTemplate implements AlfrescoLinkedIn
{
    private static String JAXB_CONTEXT_PATH = "org.alfresco.repo.publishing.linkedin.springsocial.api.impl.xml:"
            + "org.alfresco.repo.publishing.linkedin.springsocial.api";

    public AlfrescoLinkedInTemplate(String consumerKey, String consumerSecret, String accessToken,
            String accessTokenSecret)
    {
        super(consumerKey, consumerSecret, accessToken, accessTokenSecret);
    }

    protected List<HttpMessageConverter<?>> getMessageConverters()
    {
        List<HttpMessageConverter<?>> messageConverters = super.getMessageConverters();
        messageConverters.add(new JaxbHttpMessageConverter(JAXB_CONTEXT_PATH));
        return messageConverters;
    }

    @Override
    public void postNetworkUpdate(String update)
    {
        if (update == null || update.trim().length() == 0)
            return;

        URIBuilder uriBuilder = URIBuilder.fromUri("http://api.linkedin.com/v1/people/~/person-activities");

        Activity activity = new JaxbActivityImpl();
        activity.setBody(update);

        HttpEntity<?> entity = buildEntity(activity);
        getRestTemplate().postForObject(uriBuilder.build(), entity, String.class);
    }

    @Override
    public void shareComment(String comment)
    {
        if (comment == null || comment.trim().length() == 0)
            return;

        URIBuilder uriBuilder = URIBuilder.fromUri("http://api.linkedin.com/v1/people/~/shares");

        Share share = new JaxbShareImpl();
        share.setComment(comment);

        HttpEntity<?> entity = buildEntity(share);
        getRestTemplate().postForLocation(uriBuilder.build(), entity);
    }

    private HttpEntity<?> buildEntity(Object body)
    {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_XML);

        return new HttpEntity<Object>(body, headers);
    }
}
