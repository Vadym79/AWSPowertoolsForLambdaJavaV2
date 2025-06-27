// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0

package software.amazonaws.example.product.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;

import software.amazon.awssdk.http.HttpStatusCode;
import software.amazon.lambda.powertools.logging.CorrelationIdPaths;
import software.amazon.lambda.powertools.logging.Logging;
import software.amazon.lambda.powertools.metrics.FlushMetrics;
import software.amazon.lambda.powertools.metrics.Metrics;
import software.amazon.lambda.powertools.metrics.MetricsFactory;
import software.amazon.lambda.powertools.metrics.model.MetricUnit;
import software.amazon.lambda.powertools.tracing.CaptureMode;
import software.amazon.lambda.powertools.tracing.Tracing;
import software.amazonaws.example.product.dao.DynamoProductDao;
import software.amazonaws.example.product.dao.ProductDao;
import software.amazonaws.example.product.entity.Product;

public class CreateProductHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

	private static final ProductDao productDao = new DynamoProductDao();
	private static final ObjectMapper objectMapper = new ObjectMapper();
	private static final Metrics metrics = MetricsFactory.getMetricsInstance();
	
	@Override
	@Logging(logEvent = true, logResponse = true, samplingRate = 0.5, correlationIdPath = CorrelationIdPaths.API_GATEWAY_REST)
    @Tracing(namespace ="ProductAPINativeWithPowerTools", captureMode = CaptureMode.RESPONSE_AND_ERROR)
    @FlushMetrics(namespace = "ProductAPINativeWithPowerTools", service = "product-native", captureColdStart = true)
	public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent requestEvent, Context context) {
		try {
			String requestBody = requestEvent.getBody();
			Product product = objectMapper.readValue(requestBody, Product.class);
			context.getLogger().log("create product: "+product);
			productDao.putProduct(product);
			metrics.addMetric("SuccessfulProductCreation", 1, MetricUnit.COUNT);
            metrics.addMetadata("correlation_id", requestEvent.getRequestContext().getRequestId());            context.getLogger().log("pushed metrics: ");
			return new APIGatewayProxyResponseEvent().withStatusCode(HttpStatusCode.CREATED)
					.withBody("Product with id = " + product.id() + " created");
		} catch (Exception e) {
			e.printStackTrace();
			return new APIGatewayProxyResponseEvent().withStatusCode(HttpStatusCode.INTERNAL_SERVER_ERROR)
					.withBody("Internal Server Error :: " + e.getMessage());
		}
	}
}