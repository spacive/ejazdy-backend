package net.spacive.apps.ejazdybackend.config;

import com.amazonaws.auth.*;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import org.springframework.boot.context.properties.ConfigurationProperties;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * Configs related to DynamoDB service.
 *
 * @author  Juraj Haluska
 */
@Component
@ConfigurationProperties(prefix = "dynamo")
public class DynamoConfiguration {

    /**
     * AWS access key.
     */
    private String accessKey;

    /**
     * AWS secret key.
     */
    private String secretKey;

    /**
     * AWS region.
     */
    private String region;

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    /**
     * AmazonDynamoDB bean definition - this will allow us to
     * use AmazonDynamoDB with DI.
     *
     * @return new dynamo client.
     */
    @Bean
    public AmazonDynamoDB dynamoDB() {

        final AWSCredentials credentials = new BasicAWSCredentials(
                accessKey,
                secretKey
        );

        return AmazonDynamoDBClientBuilder.standard()
                .withCredentials(
                        new AWSStaticCredentialsProvider(credentials)
                )
                .withRegion(region)
                .build();
    }

    /**
     * DynamoDBMapper bean definition - this will allow us to
     * use DynamoDBMapper with DI.
     *
     * @return new dynamo mapper.
     */
    @Bean
    public DynamoDBMapper dynamoDBMapper() {
        return new DynamoDBMapper(dynamoDB());
    }
}
