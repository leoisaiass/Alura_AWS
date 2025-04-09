package com.myorg;

import software.amazon.awscdk.Fn;
import software.amazon.awscdk.RemovalPolicy;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.ec2.Vpc;
import software.amazon.awscdk.services.ecr.IRepository;
import software.amazon.awscdk.services.ecr.Repository;
import software.amazon.awscdk.services.ecs.AwsLogDriverProps;
import software.amazon.awscdk.services.ecs.Cluster;
import software.amazon.awscdk.services.ecs.ContainerImage;
import software.amazon.awscdk.services.ecs.LogDriver;
import software.amazon.awscdk.services.ecs.patterns.ApplicationLoadBalancedFargateService;
import software.amazon.awscdk.services.ecs.patterns.ApplicationLoadBalancedTaskImageOptions;
import software.amazon.awscdk.services.logs.LogGroup;
import software.constructs.Construct;

import java.util.HashMap;
import java.util.Map;

public class AluraServiceStack extends Stack {

    // Construtor da classe AluraServiceStack
    public AluraServiceStack(final Construct scope, final String id, final Cluster cluster) {
        this(scope, id, null, cluster);
    }

    public AluraServiceStack(final Construct scope, final String id, final StackProps props, final Cluster cluster) {
        super(scope, id, props);

        // Criação de um mapa para armazenar as informações de autenticação
        Map<String, String> autenticacao = new HashMap<>();
        // Adiciona a URL do banco de dados ao mapa, concatenando o endpoint e a porta
        autenticacao.put("SPRING_DATASOURCE_URL", "jdbc:mysql://" + Fn.importValue("pedidos-db-endpoint") + ":3306/alurafood-pedidos?createDatabaseIfNotExist=true");
        // Adiciona o nome de usuário ao mapa
        autenticacao.put("SPRING_DATASOURCE_USERNAME", "admin");
        // Adiciona a senha do banco de dados ao mapa, importando o valor de uma variável
        autenticacao.put("SPRING_DATASOURCE_PASSWORD", Fn.importValue("pedidos-db-senha"));

        // Cria uma instância do repositório ECR a partir do nome do repositório especificado, permitindo que a aplicação acesse a imagem armazenada de forma privada.
        IRepository iRepository = Repository.fromRepositoryName(this, "repositorio", "img-pedidos-ms");

        // Criando um serviço Fargate balanceado por load balancer
        ApplicationLoadBalancedFargateService.Builder.create(this, "AluraService")
                .serviceName("alura-service-ola") // Nome do serviço
                .cluster(cluster)                   // Referência ao cluster criado
                .cpu(512)                           // Alocação de CPU para o serviço
                .desiredCount(3)                    // Número desejado de instâncias do serviço
                .listenerPort(8080)                 // Porta que o serviço irá escutar
                .assignPublicIp(true)               // Atribui um IP público ao serviço
                .taskImageOptions(
                        ApplicationLoadBalancedTaskImageOptions.builder()
//                                .image(ContainerImage.fromRegistry("leoisaiass/pedidos-ms")) // Imagem do container
                                .image(ContainerImage.fromEcrRepository(iRepository))
                                .containerPort(8080) // Porta do container
                                .containerName("app_ola") // Nome do container
                                .environment(autenticacao) // Configura as variáveis de ambiente da aplicação com as informações de autenticação
                                .logDriver(LogDriver.awsLogs(AwsLogDriverProps.builder() // Configura o driver de log para usar o AWS Logs
                                        .logGroup(LogGroup.Builder.create(this, "PedidosMsLogGroup") // Cria um grupo de logs com o nome "PedidosMsLogGroup"
                                                .logGroupName("PedidosMsLog") // Define o nome do grupo de logs como "PedidosMsLog"
                                                .removalPolicy(RemovalPolicy.DESTROY) // Define a política de remoção para destruir os logs ao apagar a stack
                                                .build())
                                        .streamPrefix("PedidosMS") // Define um prefixo para os streams de log como "PedidosMS"
                                        .build()))
                                .build())
                .memoryLimitMiB(1024)               // Limite de memória para o serviço
                .publicLoadBalancer(true)           // Define que o load balancer é público
                .build();                           // Constrói o serviço
    }
}