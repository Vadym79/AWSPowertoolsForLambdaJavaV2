// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0

package software.amazonaws.example.product.handler;

import java.util.Map;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.crac.Core;
import org.crac.Resource;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent.ProxyRequestContext;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;

import software.amazon.awssdk.http.HttpStatusCode;
import software.amazon.lambda.powertools.logging.CorrelationIdPaths;
import software.amazon.lambda.powertools.logging.Logging;
import software.amazon.lambda.powertools.tracing.CaptureMode;
import software.amazon.lambda.powertools.tracing.Tracing;
import software.amazon.lambda.powertools.tracing.TracingUtils;
import software.amazonaws.example.product.dao.DynamoProductDao;
import software.amazonaws.example.product.dao.ProductDao;
import software.amazonaws.example.product.entity.Product;

public class GetProductByIdWithObservabilityPrimingHandler implements 
                 RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent>, Resource {

	private static final ProductDao productDao = new DynamoProductDao();
	private final ObjectMapper objectMapper = new ObjectMapper();
	private final static Logger logger = LogManager.getLogger(GetProductByIdWithObservabilityPrimingHandler.class);

	
	
	public GetProductByIdWithObservabilityPrimingHandler () {
		Core.getGlobalContext().register(this);
	}
	
	@Override
	public void beforeCheckpoint(org.crac.Context<? extends Resource> context) throws Exception {
		APIGatewayProxyRequestEvent requestEvent = new APIGatewayProxyRequestEvent();
		requestEvent.setPathParameters(Map.of("id","0"));
		ProxyRequestContext requestContext= new ProxyRequestContext();
	    requestContext.setRequestId("1234");
		requestEvent.setRequestContext(requestContext);
		this.handleRequest(requestEvent, new MockLambdaContext());
    }

	@Override
	public void afterRestore(org.crac.Context<? extends Resource> context) throws Exception {	
	
	}

	@Override
	@Logging(logEvent = true, logResponse = true, samplingRate = 0.5, correlationIdPath = CorrelationIdPaths.API_GATEWAY_REST)
    @Tracing(namespace ="ProductAPIWithPowerTools", captureMode = CaptureMode.RESPONSE_AND_ERROR)
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent requestEvent, Context context) {
		String id = requestEvent.getPathParameters().get("id");
		Optional<Product> optionalProduct = productDao.getProduct(id);
        try {
			if (optionalProduct.isEmpty()) {
				logger.info(" product with id " + id + " not found ");
				return new APIGatewayProxyResponseEvent().withStatusCode(HttpStatusCode.NOT_FOUND)
						.withBody("Product with id = " + id + " not found");
			}
			TracingUtils.putAnnotation("correlation_id", requestEvent.getRequestContext().getRequestId());

            logger.debug("result of the product search: ");
            logger.info(" product " + optionalProduct.get() + " found ");
			return new APIGatewayProxyResponseEvent().withStatusCode(HttpStatusCode.OK)
					.withBody(objectMapper.writeValueAsString(optionalProduct.get()));
		} catch (Exception je) {
			je.printStackTrace();
			return new APIGatewayProxyResponseEvent().withStatusCode(HttpStatusCode.INTERNAL_SERVER_ERROR)
					.withBody("Internal Server Error :: " + je.getMessage());
		}
	}

}