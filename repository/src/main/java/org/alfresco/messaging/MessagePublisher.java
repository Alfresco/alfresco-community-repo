package org.alfresco.messaging;

import java.util.Map;

public interface MessagePublisher
{
    void send(String endpointUri, String body, Map<String, Object> headers);
}