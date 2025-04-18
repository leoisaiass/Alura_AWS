package com.myorg;

import software.amazon.awscdk.App;

public class AluraAwsInfraApp {
    public static void main(final String[] args) {
        App app = new App();

        AluraVpcStack vpcStack = new AluraVpcStack(app, "Vpc");
        AluraClusterStack clusterStack = new AluraClusterStack(app, "Cluster", vpcStack.getVpc());
        clusterStack.addDependency(vpcStack);

        AluraRdsStack rdsStack = new AluraRdsStack(app, "Rds", vpcStack.getVpc());
        rdsStack.addDependency(vpcStack);

        AluraServiceStack serviceStack = new AluraServiceStack(app, "Service", clusterStack.getCluster());
        serviceStack.addDependency(clusterStack);
        serviceStack.addDependency(rdsStack);
        app.synth();
    }
}

