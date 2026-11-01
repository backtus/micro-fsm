package se.microfsm.infra

import software.amazon.awscdk.Duration
import software.amazon.awscdk.Fn
import software.amazon.awscdk.CfnOutput
import software.amazon.awscdk.RemovalPolicy
import software.amazon.awscdk.Stack
import software.amazon.awscdk.StackProps
import software.amazon.awscdk.Tags
import software.amazon.awscdk.CfnTag
import software.amazon.awscdk.aws_apigatewayv2_integrations.HttpLambdaIntegration
import software.amazon.awscdk.services.apigatewayv2.HttpApi
import software.amazon.awscdk.services.budgets.CfnBudget
import software.amazon.awscdk.services.dsql.CfnCluster
import software.amazon.awscdk.services.iam.PolicyStatement
import software.amazon.awscdk.services.lambda.Alias
import software.amazon.awscdk.services.lambda.Architecture
import software.amazon.awscdk.services.lambda.Code
import software.amazon.awscdk.services.lambda.Function
import software.amazon.awscdk.services.lambda.Runtime
import software.amazon.awscdk.services.lambda.SnapStartConf
import software.amazon.awscdk.services.logs.LogGroup
import software.amazon.awscdk.services.logs.RetentionDays
import software.constructs.Construct

/**
 * Spike stack: Aurora DSQL + Quarkus Lambda (SnapStart, arm64) behind an HTTP API, plus a 10 USD budget.
 * @param code Lambda package (backend `function.zip`, built with QUARKUS_PROFILE=dsql).
 */
class MicroFsmSpikeStack(scope: Construct, id: String, props: StackProps, code: Code, budgetEmail: String) :
    Stack(scope, id, props) {

    init {
        Tags.of(this).add("project", "micro-fsm")
        Tags.of(this).add("env", "spike")

        val cluster = CfnCluster.Builder.create(this, "Dsql")
            .deletionProtectionEnabled(false)
            .tags(listOf(CfnTag.builder().key("env").value("spike").build()))
            .build()
        val endpoint = Fn.join("", listOf(cluster.attrIdentifier, ".dsql.", region, ".on.aws"))

        val env = mapOf(
            "QUARKUS_PROFILE" to "dsql",
            "QUARKUS_DATASOURCE_JDBC_URL" to Fn.join("", listOf("jdbc:aws-dsql:postgresql://", endpoint, "/postgres")),
        )
        val handler = "io.quarkus.amazon.lambda.runtime.QuarkusStreamHandler::handleRequest"

        fun logGroup(name: String) = LogGroup.Builder.create(this, "${name}Logs")
            .retention(RetentionDays.ONE_WEEK)
            .removalPolicy(RemovalPolicy.DESTROY)
            .build()

        val api = Function.Builder.create(this, "Api")
            .runtime(Runtime.JAVA_25)
            .architecture(Architecture.ARM_64)
            .memorySize(1024)
            .timeout(Duration.seconds(30))
            .handler(handler)
            .code(code)
            .snapStart(SnapStartConf.ON_PUBLISHED_VERSIONS)
            .environment(env)
            .logGroup(logGroup("Api"))
            .build()
        val alias = Alias.Builder.create(this, "Live")
            .aliasName("live")
            .version(api.currentVersion)
            .build()

        // One-off migration invoke: same zip; only this function enables POST /internal/migrate and it is invoked
        // directly (not behind the HTTP API) with a synthetic event. No SnapStart; migrations never run at init.
        val migrate = Function.Builder.create(this, "Migrate")
            .runtime(Runtime.JAVA_25)
            .architecture(Architecture.ARM_64)
            .memorySize(1024)
            .timeout(Duration.minutes(5))
            .handler(handler)
            .code(code)
            .environment(env + ("APP_MIGRATE_ENDPOINT_ENABLED" to "true"))
            .logGroup(logGroup("Migrate"))
            .build()

        val connect = PolicyStatement.Builder.create()
            .actions(listOf("dsql:DbConnectAdmin"))
            .resources(listOf(cluster.attrResourceArn))
            .build()
        api.addToRolePolicy(connect)
        migrate.addToRolePolicy(connect)

        val httpApi = HttpApi.Builder.create(this, "HttpApi")
            .defaultIntegration(HttpLambdaIntegration("LiveIntegration", alias))
            .build()

        CfnBudget.Builder.create(this, "Budget")
            .budget(
                CfnBudget.BudgetDataProperty.builder()
                    .budgetType("COST")
                    .timeUnit("MONTHLY")
                    .budgetLimit(CfnBudget.SpendProperty.builder().amount(10).unit("USD").build())
                    .build(),
            )
            .notificationsWithSubscribers(
                listOf(50, 80, 100).map { pct ->
                    CfnBudget.NotificationWithSubscribersProperty.builder()
                        .notification(
                            CfnBudget.NotificationProperty.builder()
                                .notificationType("ACTUAL")
                                .comparisonOperator("GREATER_THAN")
                                .threshold(pct)
                                .thresholdType("PERCENTAGE")
                                .build(),
                        )
                        .subscribers(listOf(CfnBudget.SubscriberProperty.builder().subscriptionType("EMAIL").address(budgetEmail).build()))
                        .build()
                },
            )
            .build()

        CfnOutput(this, "ApiUrl", software.amazon.awscdk.CfnOutputProps.builder().value(httpApi.apiEndpoint).build())
        CfnOutput(this, "MigrateFunctionName", software.amazon.awscdk.CfnOutputProps.builder().value(migrate.functionName).build())
        CfnOutput(this, "ClusterEndpoint", software.amazon.awscdk.CfnOutputProps.builder().value(endpoint).build())
    }
}
