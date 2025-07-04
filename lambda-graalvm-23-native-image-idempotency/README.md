# Experimenting with observability feature of the Powertools for AWS Lambda (Java) v2

This repository provides some example of using Powertools for AWS Lambda (Java) v2 and especially idempotency feature @Idempotent.  
I also provide ideas how to prime Powertools idempotency functionality with Lambda SnapStart as using such a feature increases the startup time/cold start of such Lambda functions.   
This artifact is deployable as a Lambda Custom Runtime consisting of GraalVM Native Image.  


## Architecture

<p align="center">
  <img src="/lambda-graalvm-23-native-image-idempotency/src/main/resources/img/app_arch.png" alt="Application Architecture"/>
</p>


## Prerequisites

You need at least Java 11, Maven, AWS CLI (configured), AWS SAM, GraalVM and Native Image installed to build and deploy this application.

## Deployment

 
Deploy the demo to your AWS account using [AWS SAM](https://aws.amazon.com/serverless/sam/).


```bash

Clone git repositoy localy
git clone https://github.com/Vadym79/AWSPowertoolsForLambdaJavaV2.git

Compile and package the Java application with Maven from the root (where pom.xml is located) of the project
mvn clean package -Pnative



Deploy your application with AWS SAM
sam deploy -g
```
SAM will create an output of the API Gateway endpoint URL for future use in our load tests.
Please also check you API Key. I'll need both: API Gateway Endpoint URL and API Key to use the application properly.


## Create some Demo Products

1) Login into your AWS account and to to the API Gateway Service

2) Select AWSJava21LambdaPTIdempotencyAPI and in the category Resources under /products click one of the PUT Method execution

3) Click on Test 
 

3.1) In the "Request Body" below enter

    {  
      "id": 1,  
      "name": "Print 10x13",  
      "price": 0.15  
    }  

3.2) Click on the "Test" button and check that the result was HTTP 200. Also go into the DynamoDB ProductTable and check the created items there
3.3) All the CreateProduct* Lambda functions from the package software.amazonaws.example.product.handler use different priming technique of Lambda SnpaStart  
to decrease the cold start time of the Lambda function and have @Idempotency annotation to prevent creating the products with the same id  
 with the short period of time 
3.4) Use GET method with different priming techniques with Lambda SnapStart. For their concrete implementation see the   
implementation of the Lambda functions in the software.amazonaws.example.product.handler package.
