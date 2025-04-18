package com.myorg;

import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.ec2.Vpc;
import software.constructs.Construct;

public class AluraVpcStack extends Stack {

    private Vpc vpc;

    public AluraVpcStack(final Construct scope, final String id) {
        this(scope, id, null);
    }

    public AluraVpcStack(final Construct scope, final String id, final StackProps props) {
        super(scope, id, props);
        vpc = Vpc.Builder.create(this, "MyVpc")
                .maxAzs(3)  // Default is all AZs (zones) in region
                .build();
    }

    public Vpc getVpc() {
        return vpc;
    }
}
