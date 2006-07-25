/*
 * JBoss, Home of Professional Open Source
 * Copyright 2005, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jbpm.webapp.filter;

import java.io.IOException;
import java.io.Serializable;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.TransactionUtil;
import org.alfresco.service.transaction.TransactionService;
import org.jbpm.JbpmConfiguration;
import org.jbpm.JbpmContext;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

public class JbpmContextFilter implements Filter, Serializable
{

    private static final long serialVersionUID = 1L;

    private ServletContext context;

    public void init(FilterConfig filterConfig) throws ServletException
    {
        this.context = filterConfig.getServletContext();
    }

    public void doFilter(final ServletRequest servletRequest, final ServletResponse servletResponse, final FilterChain filterChain)
            throws IOException, ServletException
    {
        WebApplicationContext wc = WebApplicationContextUtils.getRequiredWebApplicationContext(context);
        final JbpmConfiguration jbpmConfig = (JbpmConfiguration) wc.getBean("jbpm_configuration");
        TransactionService trx = (TransactionService) wc.getBean("TransactionService");

        TransactionUtil.executeInUserTransaction(trx, new TransactionUtil.TransactionWork<Object>()
        {
            public Object doWork() throws Exception
            {
                JbpmContext jbpmContext = jbpmConfig.createJbpmContext();
                try
                {
                    String actorId = AuthenticationUtil.getCurrentUserName();
                    if (actorId != null)
                    {
                        jbpmContext.setActorId(actorId);
                    }
                    filterChain.doFilter(servletRequest, servletResponse);
                }
                finally
                {
                    jbpmContext.close();
                }
                return null;
            }
        });                
    }

    public void destroy()
    {
    }
}
