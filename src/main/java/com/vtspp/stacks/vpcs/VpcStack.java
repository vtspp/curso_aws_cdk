package com.vtspp.stacks;

import software.amazon.awscdk.core.Construct;
import software.amazon.awscdk.core.Stack;
import software.amazon.awscdk.core.StackProps;
import software.amazon.awscdk.services.ec2.Vpc;

public class VpcStack extends Stack {

    public VpcStack(final software.amazon.awscdk.core.Construct scope, final String id) {
        this(scope, id, null);
    }

    public VpcStack(final Construct scope, final String id, final StackProps props) {
        super(scope, id, props);
        Vpc.Builder.create(this, "VPC-01")
                .maxAzs(3)
                .build();
    }
}
