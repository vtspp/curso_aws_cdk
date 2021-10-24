package com.vtspp.stacks.services;

import software.amazon.awscdk.core.*;
import software.amazon.awscdk.services.applicationautoscaling.EnableScalingProps;
import software.amazon.awscdk.services.ecs.*;
import software.amazon.awscdk.services.ecs.patterns.ApplicationLoadBalancedFargateService;
import software.amazon.awscdk.services.ecs.patterns.ApplicationLoadBalancedTaskImageOptions;
import software.amazon.awscdk.services.elasticloadbalancingv2.HealthCheck;
import software.amazon.awscdk.services.elasticloadbalancingv2.ApplicationTargetGroup;
import software.amazon.awscdk.services.logs.LogGroup;

public class Service01 extends Stack {

    private static final String SERVICE_NAME = "loadBalance-01";
    private static final String LOAD_BALANCE_NAME = "loadBalance-01";
    private static final int QUANTITY_CPU_VIRTUAL = 512;
    private static final int MEMORY_LIMIT = 1024;
    private static final int QUANTITY_INIT_INSTANCE = 2;
    private static final int PORT = 8080;
    private static final boolean IS_PUBLIC = true;

    private static final String CONTAINER_NAME = "aws_project_01";
    private static final String IMAGE_NAME = "vtspp/projeto-spring-com-aws:0.0.1";
    private static final String LOG_GROUP = "service-01-logGroup";
    private static final String LOG_GROUP_NAME = "service-01";
    private static final String STREAM_PREFIX = "service-01";

    private static final String HEALTH_CHECK_PATH = "/actuator/health";
    private static final String CODE_HEALTH_CHECK_UP = "200";

    private static final int AUTO_SCALE_MIN_CAPACITY = 2;
    private static final int AUTO_SCALE_MAX_CAPACITY = 4;

    private static final String AUTO_SCALE_ID = "service-01-autoScale";
    private static final int SCALE_DURATION = 60;
    private static final int SCALE_UTILIZATION_PERCENT = 50;

    public Service01(final Construct scope, final String id, Cluster cluster) {
        this(scope, id, null, cluster);
    }

    public Service01(final Construct scope, final String id, final StackProps props, Cluster cluster) {
        super(scope, id, props);
        ApplicationLoadBalancedFargateService loadBalancedFargateService = loadBalancedFargateService(id, cluster);

        ApplicationTargetGroup applicationTargetGroup = loadBalancedFargateService.getTargetGroup();
        configureHealthCheck(applicationTargetGroup);

        FargateService fargateService = loadBalancedFargateService.getService();

        ScalableTaskCount scalableTaskCount = autoScaleTaskCount(fargateService);
        scaleOnCpuUtilization(scalableTaskCount);
    }

    private ApplicationLoadBalancedFargateService loadBalancedFargateService (String id, Cluster cluster) {
        return ApplicationLoadBalancedFargateService.Builder.create(this, id)
        .serviceName(SERVICE_NAME)
        .loadBalancerName(LOAD_BALANCE_NAME)
        .cpu(QUANTITY_CPU_VIRTUAL)
        .memoryLimitMiB(MEMORY_LIMIT)
        .desiredCount(QUANTITY_INIT_INSTANCE)
        .listenerPort(PORT)
        .taskImageOptions(taskImageOptions())
        .publicLoadBalancer(IS_PUBLIC)
        .cluster(cluster)
        .build();
    }

    private ApplicationLoadBalancedTaskImageOptions taskImageOptions () {
        return ApplicationLoadBalancedTaskImageOptions.builder()
        .containerName(CONTAINER_NAME)
        .image(ContainerImage.fromRegistry(IMAGE_NAME))
        .containerPort(PORT)
        .logDriver(LogDriver.awsLogs(awsLogDriverProps()))
        .build();
    }

    private AwsLogDriverProps awsLogDriverProps () {
        return AwsLogDriverProps.builder()
        .logGroup(LogGroup.Builder.create(this, LOG_GROUP)
        .logGroupName(LOG_GROUP_NAME)
        .removalPolicy(RemovalPolicy.DESTROY)
        .build())
        .streamPrefix(STREAM_PREFIX)
        .build();
    }

    private void configureHealthCheck (ApplicationTargetGroup applicationTargetGroup) {
        applicationTargetGroup.configureHealthCheck(new HealthCheck.Builder()
        .path(HEALTH_CHECK_PATH)
        .port(String.valueOf(PORT))
        .healthyHttpCodes(CODE_HEALTH_CHECK_UP)
        .build());
    }

    private ScalableTaskCount autoScaleTaskCount (FargateService fargateService) {
        return fargateService.autoScaleTaskCount(EnableScalingProps
        .builder()
        .minCapacity(AUTO_SCALE_MIN_CAPACITY)
        .maxCapacity(AUTO_SCALE_MAX_CAPACITY)
        .build());
    }

    private void scaleOnCpuUtilization (ScalableTaskCount scalableTaskCount) {
        scalableTaskCount.scaleOnCpuUtilization(AUTO_SCALE_ID, CpuUtilizationScalingProps
        .builder()
        .targetUtilizationPercent(SCALE_UTILIZATION_PERCENT)
        .scaleInCooldown(Duration.seconds(SCALE_DURATION))
        .scaleOutCooldown(Duration.seconds(SCALE_DURATION))
        .build());
    }
}
