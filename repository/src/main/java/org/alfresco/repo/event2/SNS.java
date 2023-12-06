package org.alfresco.repo.event2;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.amazonaws.services.sns.model.PublishRequest;
public class SNS {
    public static void main(String[] args)
    {
        try {
            String TOPIC_ARN = "";
            String AWS_ACCESS_KEY = "";
            String AWS_SECRET_KEY = "";
            String AWS_REGION = "";
            String MESSAGE = "Hey! Manish Connection is established";

            AmazonSNSClient amazonSNSClient = (AmazonSNSClient) AmazonSNSClientBuilder
                    .standard()
                    .withRegion(AWS_REGION)
                    .withCredentials(new AWSStaticCredentialsProvider(
                            new BasicAWSCredentials(AWS_ACCESS_KEY, AWS_SECRET_KEY)))
                    .build();
            PublishRequest request = new PublishRequest(TOPIC_ARN, MESSAGE);
            amazonSNSClient.publish(request);
        }
        catch(Exception e)
        {
            System.out.print(e.getMessage());
        }
    }
}
