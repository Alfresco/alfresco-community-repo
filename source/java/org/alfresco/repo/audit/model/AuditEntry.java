/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.alfresco.repo.audit.model;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.alfresco.repo.audit.AuditConfiguration;
import org.alfresco.repo.audit.AuditMode;
import org.alfresco.repo.audit.AuditModel;
import org.alfresco.repo.audit.RecordOptions;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.springframework.beans.factory.InitializingBean;

public class AuditEntry extends AbstractAuditEntry implements InitializingBean, AuditModel
{
    private static Log s_logger = LogFactory.getLog(AuditEntry.class);
    
    private Map<String, ServiceAuditEntry> services = new HashMap<String, ServiceAuditEntry>();

    private Map<String, ApplicationAuditEntry> applications = new HashMap<String, ApplicationAuditEntry>();

    private AuditConfiguration auditConfiguration;

    private NamespacePrefixResolver namespacePrefixResolver;

    public AuditEntry()
    {
        super();
    }

    public AuditConfiguration getAuditConfiguration()
    {
        return auditConfiguration;
    }

    public void setAuditConfiguration(AuditConfiguration auditConfiguration)
    {
        this.auditConfiguration = auditConfiguration;
    }

    public void setNamespacePrefixResolver(NamespacePrefixResolver namespacePrefixResolver)
    {
        this.namespacePrefixResolver = namespacePrefixResolver;
    }

    public void afterPropertiesSet() throws Exception
    {
        Document document = createDocument();
        Element root = document.getRootElement();
        // Check it is the correct thing
        configure(null, root, namespacePrefixResolver);
    }

 

    @Override
    void configure(AbstractAuditEntry parent, Element element, NamespacePrefixResolver namespacePrefixResolver)
    {
        if (!element.getNamespaceURI().equals(AuditModel.NAME_SPACE))
        {
            throw new AuditModelException("Audit model has incorrect name space");
        }
        if (!element.getName().equals(AuditModel.EL_AUDIT))
        {
            throw new AuditModelException("Audit model has incorrect root node");
        }
        if(s_logger.isDebugEnabled())
        {
            s_logger.debug("Audit configuration");
        }
        super.configure(parent, element, namespacePrefixResolver);

        // Add services

        if(s_logger.isDebugEnabled())
        {
            s_logger.debug("Adding services ...");
        }
        for (Iterator nsit = element.elementIterator(AuditModel.EL_SERVICE); nsit.hasNext(); /**/)
        {
            Element serviceElement = (Element) nsit.next();
            ServiceAuditEntry service = new ServiceAuditEntry();
            service.configure(this, serviceElement, namespacePrefixResolver);
            services.put(service.getName(), service);
        }

        // Add Applications

        if(s_logger.isDebugEnabled())
        {
            s_logger.debug("Adding applications ...");
        }
        for (Iterator nsit = element.elementIterator(AuditModel.EL_APPLICATION); nsit.hasNext(); /**/)
        {
            Element applicationElement = (Element) nsit.next();
            ApplicationAuditEntry application = new ApplicationAuditEntry();
            application.configure(this, applicationElement, namespacePrefixResolver);
            applications.put(application.getName(), application);
        }
    }

    public AuditMode beforeExecution(AuditMode auditMode, MethodInvocation mi)
    {
        String serviceName = getPublicServiceIdentifier().getPublicServiceName(mi);
        ServiceAuditEntry service = services.get(serviceName);
        if(service != null)
        {
            return service.beforeExecution(auditMode, mi);
        }
        else
        {
            if(s_logger.isDebugEnabled())
            {
                s_logger.debug("No specific audit entry for service "+serviceName);
            }
            return getEffectiveAuditMode();
            
        }
    }
    
    public AuditMode afterExecution(AuditMode auditMode, MethodInvocation mi)
    {
        throw new UnsupportedOperationException();
    }

    public RecordOptions getAuditRecordOptions(MethodInvocation mi)
    {
        throw new UnsupportedOperationException();
    }

    public AuditMode onError(AuditMode auditMode, MethodInvocation mi)
    {
        throw new UnsupportedOperationException();
    }

    private Document createDocument()
    {
        InputStream is = auditConfiguration.getInputStream();
        if (is == null)
        {
            throw new AuditModelException("Audit configuration could not be opened");
        }
        SAXReader reader = new SAXReader();
        try
        {
            Document document = reader.read(is);
            is.close();
            return document;
        }
        catch (DocumentException e)
        {
            throw new AuditModelException("Failed to create audit model document ", e);
        }
        catch (IOException e)
        {
            throw new AuditModelException("Failed to close audit model document ", e);
        }

    }

    public AuditMode beforeExecution(AuditMode auditMode, String application, String description, NodeRef key, Object... args)
    {
        ApplicationAuditEntry aae = applications.get(application);
        if(aae != null)
        {
            return aae.beforeExecution(auditMode, application, description, key, args);
        }
        else
        {
            if(s_logger.isDebugEnabled())
            {
                s_logger.debug("No specific audit entry for application "+application);
            }
            return getEffectiveAuditMode();
            
        }
    }

    public AuditMode afterExecution(AuditMode auditMode, String application, String description, NodeRef key, Object... args)
    {
        throw new UnsupportedOperationException();
    }

    public AuditMode onError(AuditMode auditMode, String application, String description, NodeRef key, Object... args)
    {
        throw new UnsupportedOperationException();
    }

    public RecordOptions getAuditRecordOptions(String application)
    {
        throw new UnsupportedOperationException();
    }

    public TrueFalseUnset getAuditInternalServiceMethods( MethodInvocation mi)
    {
        String serviceName = getPublicServiceIdentifier().getPublicServiceName(mi);
        ServiceAuditEntry service = services.get(serviceName);
        if(service != null)
        {
            return service.getAuditInternalServiceMethods( mi);
        }
        else
        {
            if(s_logger.isDebugEnabled())
            {
                s_logger.debug("No specific audit entry for service "+serviceName);
            }
            return getEffectiveAuditInternal();
            
        }
    }

}
