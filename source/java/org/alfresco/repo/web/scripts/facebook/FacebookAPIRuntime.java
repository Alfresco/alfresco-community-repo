/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.repo.web.scripts.facebook;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.extensions.config.ServerProperties;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.RuntimeContainer;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.servlet.ServletAuthenticatorFactory;
import org.springframework.extensions.webscripts.servlet.WebScriptServletRuntime;


/**
 * Runtime to support requests from Facebook
 * 
 * @author davidc
 */
public class FacebookAPIRuntime extends WebScriptServletRuntime
{

    /**
     * Construct
     * 
     * @param container
     * @param authFactory
     * @param req
     * @param res
     * @param serverProperties
     */
    public FacebookAPIRuntime(RuntimeContainer container, ServletAuthenticatorFactory authFactory, HttpServletRequest req, HttpServletResponse res, ServerProperties serverProperties)
    {
        super(container, authFactory, req, res, serverProperties);
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.servlet.WebScriptServletRuntime#createRequest(org.alfresco.web.scripts.Match)
     */
    @Override
    protected WebScriptRequest createRequest(Match match)
    {
        servletReq = new FacebookServletRequest(this, req, match, serverProperties, getScriptUrl());
        return servletReq;
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.servlet.WebScriptServletRuntime#getScriptParameters()
     */
    @Override
    public Map<String, Object> getScriptParameters()
    {
        Map<String, Object> model = new HashMap<String, Object>();
        model.putAll(super.getScriptParameters());
        model.put("facebook", new FacebookModel((FacebookServletRequest)servletReq));
        return model;
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.AbstractRuntime#getTemplateParameters()
     */
    @Override
    public Map<String, Object> getTemplateParameters()
    {
        Map<String, Object> model = new HashMap<String, Object>();
        model.putAll(super.getTemplateParameters());
        model.put("facebook", new FacebookModel((FacebookServletRequest)servletReq));
        return model;
    }

}
