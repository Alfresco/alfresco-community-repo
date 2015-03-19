/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
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
package org.alfresco.messaging.camel.configuration;

import org.alfresco.encryption.AlfrescoKeyStore;
import org.alfresco.encryption.ssl.SSLEncryptionParameters;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.ActiveMQSslConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.jms.ConnectionFactory;
import java.security.SecureRandom;

/**
 * Configures the ActiveMQ connection factory for use.
 * @author Gethin James
 */

@Configuration
public class ConnectionFactoryConfiguration {

    @Value("${messaging.broker.url}")
    private String brokerUrl = "notset"; //defaults to an invalid notset value

    @Value("${messaging.broker.ssl}")
    private boolean useSSL = false; //defaults to false

    @Autowired(required = false) @Qualifier("ssl.keyStore")
    private AlfrescoKeyStore keyStore;

    @Autowired(required = false) @Qualifier("ssl.trustStore")
    private AlfrescoKeyStore trustStore;

    @Bean
    public ConnectionFactory activeMqConnectionFactory() {
        if (useSSL) {
            return createSecureConnectionFactory();
        }
        //Default is not SSL
        return createConnectionFactory();
    }

    protected ConnectionFactory createConnectionFactory() {
        return new ActiveMQConnectionFactory(brokerUrl);
    }

    protected ConnectionFactory createSecureConnectionFactory() {
        ActiveMQSslConnectionFactory factory = new ActiveMQSslConnectionFactory(brokerUrl);
        factory.setKeyAndTrustManagers(keyStore.createKeyManagers(),
                                       trustStore.createTrustManagers(), new SecureRandom());
        return factory;
    }
}