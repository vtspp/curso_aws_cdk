package com.vtspp;

import com.vtspp.stacks.clusters.ClusterStack;
import com.vtspp.stacks.services.Service01;
import com.vtspp.stacks.vpcs.VpcStack;
import software.amazon.awscdk.core.App;
import software.amazon.awscdk.core.StackProps;

public class CursoAwsCdkApp {
    public static void main(final String[] args) {
        App app = new App();

        VpcStack vpc = new VpcStack(app, "vpc-01", StackProps.builder().build());

        ClusterStack cluster = new ClusterStack(app, "cluster-01", StackProps.builder().build(), vpc.getVpc());
        cluster.addDependency(vpc);

        Service01 service = new Service01(app, "service-01", StackProps.builder().build(), cluster.getCluster());
        service.addDependency(cluster);

        app.synth();

        //new CursoAwsCdkStack(app, "CursoAwsCdkStack", StackProps.builder()
                // If you don't specify 'env', this stack will be environment-agnostic.
                // Account/Region-dependent features and context lookups will not work,
                // but a single synthesized template can be deployed anywhere.

                // Uncomment the next block to specialize this stack for the AWS Account
                // and Region that are implied by the current CLI configuration.
                /*
                .env(Environment.builder()
                        .account(System.getenv("CDK_DEFAULT_ACCOUNT"))
                        .region(System.getenv("CDK_DEFAULT_REGION"))
                        .build())
                */

                // Uncomment the next block if you know exactly what Account and Region you
                // want to deploy the stack to.
                /*
                .env(Environment.builder()
                        .account("123456789012")
                        .region("us-east-1")
                        .build())
                */

                // For more information, see https://docs.aws.amazon.com/cdk/latest/guide/environments.html
                //.build());
    }
}
