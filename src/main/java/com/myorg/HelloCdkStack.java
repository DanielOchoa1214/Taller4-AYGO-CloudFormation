package com.myorg;

import software.amazon.awscdk.CfnOutput;
import software.amazon.awscdk.services.iam.Role;
import software.amazon.awscdk.services.lambda.Code;
import software.amazon.awscdk.services.lambda.Function;
import software.amazon.awscdk.services.lambda.FunctionUrl;
import software.amazon.awscdk.services.lambda.FunctionUrlAuthType;
import software.amazon.awscdk.services.lambda.FunctionUrlOptions;
import software.amazon.awscdk.services.lambda.Runtime;
import software.constructs.Construct;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;

public class HelloCdkStack extends Stack {
    public HelloCdkStack(final Construct scope, final String id) {
        this(scope, id, null);
    }

    public HelloCdkStack(final Construct scope, final String id, final StackProps props) {
        super(scope, id, props);

        Function myFunction = Function.Builder.create(this, "HelloWorldFunction")
                .runtime(Runtime.NODEJS_20_X)
                .handler("index.handler")
                .code(Code.fromInline("""
                        exports.handler = async function(event) {
                            return {
                                statusCode: 200,
                                body: JSON.stringify('Hello from a Lambda in the cloud :D')
                            };
                        };
                        """))
                .role(Role.fromRoleArn(this, "LabRole", "arn:aws:iam::520046576100:role/LabRole"))
                .build();

        FunctionUrl myFunctionUrl = myFunction.addFunctionUrl(FunctionUrlOptions.builder()
                .authType(FunctionUrlAuthType.NONE)
                .build());

        CfnOutput.Builder.create(this, "myFunctionUrlOutput")
                .value(myFunctionUrl.getUrl())
                .build();
    }
}
