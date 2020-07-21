/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2018 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
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
 * #L%
 */

package org.alfresco.messaging.camel.configuration;

import java.security.SecureRandom;

import javax.jms.ConnectionFactory;

import org.alfresco.encryption.AlfrescoKeyStore;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.ActiveMQSslConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configures the ActiveMQ connection factory for use.
 *
 * @author Gethin James
 */

@Configuration
public class ConnectionFactoryConfiguration
{

    @Value("${messaging.broker.url}")
    private String brokerUrl = "notset"; //defaults to an invalid notset value

    @Value("${messaging.broker.ssl}")
    private boolean useSSL = false; //defaults to false

    @Autowired(required = false)
    @Qualifier("ssl.keyStore")
    private AlfrescoKeyStore keyStore;

    @Autowired(required = false)
    @Qualifier("ssl.trustStore")
    private AlfrescoKeyStore trustStore;

    @Value("${messaging.broker.username}")
    private String username;

    @Value("${messaging.broker.password}")
    private String password;

    @Bean
    public ConnectionFactory activeMqConnectionFactory()
    {
        if (useSSL)
        {
            return createSecureConnectionFactory();
        }
        //Default is not SSL
        return createConnectionFactory();
    }

    protected ConnectionFactory createConnectionFactory()
    {
        return new ActiveMQConnectionFactory(username, password, brokerUrl);
    }

    protected ConnectionFactory createSecureConnectionFactory()
    {
        ActiveMQSslConnectionFactory factory = new ActiveMQSslConnectionFactory(brokerUrl);
        factory.setKeyAndTrustManagers(keyStore.createKeyManagers(), trustStore.createTrustManagers(), new SecureRandom());
        factory.setUserName(username);
        factory.setPassword(password);
        return factory;
    }
}