package org.alfresco.messaging;

public interface TextMessageHandler
{
    void process(String body);
}