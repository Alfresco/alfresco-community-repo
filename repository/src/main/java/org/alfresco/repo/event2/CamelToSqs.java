package org.alfresco.repo.event2;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
public class CamelToSqs {
    public static void main(String[] args) throws Exception {

        String AWS_ACCESS_KEY = "";
        String AWS_SECRET_KEY ="";

        SqsClient sqsClient = SqsClient.builder()
                .region(Region.of("us-east-1"))
                .credentialsProvider(StaticCredentialsProvider
                        .create(AwsBasicCredentials.create(AWS_ACCESS_KEY,AWS_SECRET_KEY)))
                .build();
        CamelContext context = new DefaultCamelContext();
        context.getRegistry().bind("myClient",sqsClient);
        context.addRoutes(
                new RouteBuilder() {
                    @Override
                    public void configure() throws Exception {
                        from("timer:first-timer?period=500000")
                                .setBody(constant("Camel rocks!"))
                                .to("aws2-sqs://apps-2484-queue?amazonSQSClient=#myClient");

                    }
                }
        );
        context.start();
        Thread.sleep(50000);
        context.stop();
    }

}
