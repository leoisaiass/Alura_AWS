package com.myorg;

import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.ec2.Vpc;
import software.amazon.awscdk.services.ecs.Cluster;
import software.amazon.awscdk.services.ecs.ContainerImage;
import software.amazon.awscdk.services.ecs.patterns.ApplicationLoadBalancedFargateService;
import software.amazon.awscdk.services.ecs.patterns.ApplicationLoadBalancedTaskImageOptions;
import software.constructs.Construct;

public class AluraServiceStack extends Stack {

    // Construtor da classe AluraServiceStack
    public AluraServiceStack(final Construct scope, final String id, final Cluster cluster) {
        this(scope, id, null, cluster);
    }

    public AluraServiceStack(final Construct scope, final String id, final StackProps props, final Cluster cluster) {
        super(scope, id, props);

        // Criando um serviço Fargate balanceado por load balancer
        ApplicationLoadBalancedFargateService.Builder.create(this, "AluraService")
                .serviceName("alura-service-ola") // Nome do serviço
                .cluster(cluster)                   // Referência ao cluster criado
                .cpu(512)                           // Alocação de CPU para o serviço
                .desiredCount(1)                    // Número desejado de instâncias do serviço
                .listenerPort(8080)                 // Porta que o serviço irá escutar
                .assignPublicIp(true)               // Atribui um IP público ao serviço
                .taskImageOptions(
                        ApplicationLoadBalancedTaskImageOptions.builder()
                                .image(ContainerImage.fromRegistry("jacquelineoliveira/ola:1.0")) // Imagem do container
                                .containerPort(8080) // Porta do container
                                .containerName("app_ola") // Nome do container
                                .build())
                .memoryLimitMiB(1024)               // Limite de memória para o serviço
                .publicLoadBalancer(true)           // Define que o load balancer é público
                .build();                           // Constrói o serviço
    }
}