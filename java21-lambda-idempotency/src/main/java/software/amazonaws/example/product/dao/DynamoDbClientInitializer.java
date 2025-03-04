package software.amazonaws.example.product.dao;

import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

public class DynamoDbClientInitializer {
	 public static final DynamoDbClient DYNAMO_DB_CLIENT = DynamoDbClient.builder()
			    .credentialsProvider(DefaultCredentialsProvider.create())
			    .region(Region.EU_CENTRAL_1)
			    //.httpClient(UrlConnectionHttpClient.create())
			    .overrideConfiguration(ClientOverrideConfiguration.builder()
			      .build())
			    .build();

}
