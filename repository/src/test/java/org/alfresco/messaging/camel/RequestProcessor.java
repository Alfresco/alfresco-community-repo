package org.alfresco.messaging.camel;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

/**
 * Processor which saves the last message received
 * and replies with predefined message
 */
public class RequestProcessor implements Processor {
    private Object lastMessage;

    public Object getLastMessage() {
        return lastMessage;
    }

    @Override
    public void process(Exchange exchange) {
        lastMessage = exchange.getMessage().getBody();

        // change the message that's sent back
        exchange.getMessage().setBody("Here is the reply to the following request: " + lastMessage);
    }
}
