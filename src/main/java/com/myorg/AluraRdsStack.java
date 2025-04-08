package com.myorg;

import software.amazon.awscdk.*;
import software.amazon.awscdk.services.ec2.*;
import software.amazon.awscdk.services.ec2.InstanceType;
import software.amazon.awscdk.services.rds.*;
import software.constructs.Construct;

import java.util.Collections;

public class AluraRdsStack extends Stack { // Classe que representa a stack do RDS, estendendo a classe Stack
    public AluraRdsStack(final Construct scope, final String id, final Vpc vpc) { // Construtor que recebe o escopo, id e a VPC
        this(scope, id, null, vpc); // Chama o outro construtor que contém o construtor da classe pai com os parâmetros fornecidos
    } // Fim do construtor

    public AluraRdsStack(final Construct scope, final String id, final StackProps props, final Vpc vpc) { // Outro construtor que aceita propriedades adicionais
        super(scope, id, props); // Chama o construtor da classe pai com os parâmetros fornecidos

        // Criando um parâmetro do CloudFormation para a senha do banco de dados
        CfnParameter senha = CfnParameter.Builder.create(this, "senha") // Cria um parâmetro chamado "senha"
                .type("String") // Define o tipo do parâmetro como String
                .description("Senha do database pedidos-ms") // Adiciona uma descrição ao parâmetro
                .build(); // Constrói o parâmetro

        // Obtendo o security group padrão da VPC
        ISecurityGroup iSecurityGroup = SecurityGroup.fromSecurityGroupId(this, id, vpc.getVpcDefaultSecurityGroup()); // Obtém o security group padrão da VPC
        // Permitindo acesso à porta 3306 para qualquer IP dentro da VPC
        iSecurityGroup.addIngressRule(Peer.anyIpv4(), Port.tcp(3306)); // Adiciona uma regra de entrada para permitir acesso à porta 3306

        // Criando a instância do banco de dados
        DatabaseInstance database = DatabaseInstance.Builder // Inicia a construção da instância do banco de dados
                .create(this, "Rds-pedidos") // Define o escopo e o id da instância
                .instanceIdentifier("alura-aws-pedido-db") // Define um identificador único para a instância
                .engine(DatabaseInstanceEngine.mysql(MySqlInstanceEngineProps.builder() // Define o tipo de banco de dados como MySQL
                        .version(MysqlEngineVersion.VER_8_0) // Define a versão do MySQL
                        .build())) // Constrói as propriedades do MySQL
                .vpc(vpc) // Define a VPC onde a instância será criada
                .credentials(Credentials.fromUsername("admin", // Define as credenciais de acesso ao banco
                CredentialsFromUsernameOptions.builder() // Inicia a construção das opções de credenciais
                        .password(SecretValue.unsafePlainText(senha.getValueAsString())) // Define a senha usando o parâmetro criado
                        .build())) // Constrói as opções de credenciais
                .instanceType(InstanceType.of(InstanceClass.BURSTABLE2, InstanceSize.MICRO)) // Define o tipo e tamanho da instância
                .multiAz(false) // Define que a instância não será multi-AZ
                .allocatedStorage(10) // Define o tamanho do armazenamento em 10 GB
                .securityGroups(Collections.singletonList(iSecurityGroup)) // Adiciona o security group à instância
                .vpcSubnets(SubnetSelection.builder() // Inicia a seleção das sub-redes da VPC
                        .subnets(vpc.getPrivateSubnets()) // Define que usaremos as sub-redes privadas da VPC
                        .build()) // Constrói a seleção das sub-redes
                .build(); // Constrói a instância do banco de dados

        // Exportando o endpoint do banco de dados
        CfnOutput.Builder.create(this, "pedidos-db-endpoint") // Inicia a construção do "output" para o endpoint
                .exportName("pedidos-db-endpoint") // Define o nome do export
                .value(database.getDbInstanceEndpointAddress()) // Define o valor como o endpoint da instância do banco
                .build(); // Constrói o "output"

        // Exportando a senha do banco de dados
        CfnOutput.Builder.create(this, "pedidos-db-senha") // Inicia a construção do "output" para a senha
                .exportName("pedidos-db-senha") // Define o nome do export
                .value(senha.getValueAsString()) // Define o valor como a senha do parâmetro
                .build(); // Constrói o "output"
    } // Fim do construtor

} // Fim da classe
