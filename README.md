# Workshop 4 AYGO - Daniel Sebastian Ochoa Urrego

This project is a simple demonstration on how to use the AWS CDK with Java to create a Lambda function in code.

## Getting started

This instructions will help you have a functional copy of the project on your machine

### Prerequisites

* Git 
* Java
* Maven
* AWS CLI
* AWS CDK

### Installing the project

To have a local copy of the project run the following command

```
git clone https://github.com/DanielOchoa1214/Taller4-AYGO-CloudFormation.git
```

Then, with a configured AWS account and Stack when you run the following command a Lambda function should be deployed in AWS 

```
cdk deploy -r <Your-Lab-Role>
```

And you should see something like the following output:

```bash
 ✅  HelloCdkStack

✨  Deployment time: 67.13s

Outputs:
HelloCdkStack.myFunctionUrlOutput = https://owrzadkgxvz7l3lbgiesssdrgy0ikujz.lambda-url.us-east-1.on.aws/
Stack ARN:
arn:aws:cloudformation:us-east-1:520046576100:stack/HelloCdkStack/de1f2820-bce2-11f0-80dd-0affc20bec61

✨  Total time: 69.12s
```

And when you go to the URL the the last command shows, you should see something like this

<img width="634" height="169" alt="Screenshot 2025-11-08 at 3 42 44 PM" src="https://github.com/user-attachments/assets/7a6ffe59-9faa-440f-9e3d-b1a222dbbdd2" />


## Design

### Architecture

This project uses the AWS Cloud Development Kit (CDK) with a small Java application to define and deploy a single CloudFormation stack that provisions a minimal serverless HTTP endpoint.

Key components:

- CDK App (Java): the entry point is `HelloCdkApp` which constructs and synthesizes the `HelloCdkStack` with the target AWS account and region.
- CloudFormation Stack (`HelloCdkStack`): contains the resources defined by the CDK code:
  - AWS Lambda Function: a Node.js 20.x function created with inline source code that returns a simple JSON response.
  - Lambda Function URL: a Function URL attached to the Lambda with `authType` set to `NONE` (public, unauthenticated access).
  - IAM role reference: the Lambda assumes an existing role referenced with `Role.fromRoleArn(...)`; the project does not create new IAM roles in the stack.
  - CloudFormation Output: the function URL is exported using `CfnOutput` so the endpoint is printed after deployment.
- Bootstrap template: an optional `bootstrap-template.yaml` is used to bootstrap the environment when required by the CDK deployment (this project includes a custom template adapted for a lab account).

Simple data flow:

Client (browser/curl)
  -> Function URL (HTTPS)
    -> Lambda (executes inline handler)
      -> returns JSON response

### Deployment 

To deploy we have in this project to AWS we had to do several steps. First we had to set the correct AWS account id and region on the `HelloCdkApp` class: 

```java
public class HelloCdkApp {
    public static void main(final String[] args) {
        App app = new App();

        new HelloCdkStack(app, "HelloCdkStack", StackProps.builder()
                .env(Environment.builder()
                        .account(System.getenv("520046576100"))
                        .region(System.getenv("us-east-1"))
                        .build())
                .build());
        
        app.synth();
    }
}
```

Then we need to bootstrap our app. To do so, we first created the template in the `bootstrap-template.yaml` file. And commented out all of the Role resources since we have a Lab account we wont have any access to create any IAM resources. Then with the following command we bootstraped our app. 

```
cdk bootstrap --template bootstrap-template.yaml
```

The last command with create the Stack in CloudFormation in AWS. You can check ut was created successfuly in the AWS Console

<img width="1486" height="320" alt="Screenshot 2025-11-08 at 4 08 50 PM" src="https://github.com/user-attachments/assets/ce13cf86-b363-48bf-be9f-20bbccfe5aab" />

Then we add all of the Lambda Configs in our stack class

```java
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
```

Here we are defining 3 major things:

- The Lambda config in the `myFunction` object. Setting things like the technology it will use to run our code, the role it needs to access when created and the code that it will run when invoked.
- The Lambda URL in the `myFunctionUrl` object. Here we configure authentication method when the function is invoked. In this case none.
- And lastly we define what the output will be when the CloudFormation is called. In this case the Lambda URL.

When you are satisfied with the stack, you can run the following command: 

```
cdk synth
```

This will validate your code and if its correct it will create the CloudFormation template. When this step finishes you can go ahead and deploy your code! To do so run the following command: 

```
cdk deploy -r arn:aws:iam::520046576100:role/LabRole
```

Here in the `-r` flag be sure to replace your acount ID and the role you want to use to deploy the resources. Since we are using a lab account we use the LabRole. 
When the comand finishes running you can go to the URL it gives you in the output and you should be able to see you Lambda in action! 

<img width="593" height="161" alt="Screenshot 2025-11-08 at 4 21 56 PM" src="https://github.com/user-attachments/assets/6534c384-8b1d-40c3-ab58-e15730815b54" />
