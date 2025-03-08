// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0

package software.amazonaws.example.product.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;

import software.amazon.awssdk.http.HttpStatusCode;
import software.amazon.lambda.powertools.idempotency.Idempotency;
import software.amazon.lambda.powertools.idempotency.IdempotencyConfig;
import software.amazon.lambda.powertools.idempotency.Idempotent;
import software.amazon.lambda.powertools.idempotency.persistence.dynamodb.DynamoDBPersistenceStore;
import software.amazonaws.example.product.dao.DynamoDbClientInitializer;
import software.amazonaws.example.product.dao.DynamoProductDao;
import software.amazonaws.example.product.dao.ProductDao;
import software.amazonaws.example.product.entity.Product;

public class CreateProductHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

	private static final ProductDao productDao = new DynamoProductDao();
	private static final ObjectMapper objectMapper = new ObjectMapper();
	
	public CreateProductHandler() {
		 Idempotency.config().withConfig(
                 IdempotencyConfig.builder()
                         .withEventKeyJMESPath(
                                 "powertools_json(body).id") // will retrieve the "id" field in the body which is a string transformed to json with `powertools_json`
                         .build())
         .withPersistenceStore(
                 DynamoDBPersistenceStore.builder()
                  .withDynamoDbClient(DynamoDbClientInitializer.DYNAMO_DB_CLIENT)
                  .withTableName(System.getenv("IDEMPOTENCY_TABLE_NAME"))
                         .build()
         ).configure();
	}

	@Override
	@Idempotent
	public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent requestEvent, Context context) {
		try {
			String requestBody = requestEvent.getBody();
			//String id = JsonConfig.get().getObjectMapper().readTree(requestEvent.getBody()).get("id").asText();
			//context.getLogger().log("parsed product id: "+id+ " idempotency table name: "+System.getenv("IDEMPOTENCY_TABLE_NAME"));
			Product product = objectMapper.readValue(requestBody, Product.class);
			context.getLogger().log("create product: "+product);
			productDao.putProduct(product);
			return new APIGatewayProxyResponseEvent().withStatusCode(HttpStatusCode.CREATED)
					.withBody("Product with id = " + product.id() + " created");
		} catch (Exception e) {
			e.printStackTrace();
			return new APIGatewayProxyResponseEvent().withStatusCode(HttpStatusCode.INTERNAL_SERVER_ERROR)
					.withBody("Internal Server Error :: " + e.getMessage());
		}
	}
}