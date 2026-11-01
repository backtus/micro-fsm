package se.microfsm.infra

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import software.amazon.awscdk.App
import software.amazon.awscdk.Environment
import software.amazon.awscdk.StackProps
import software.amazon.awscdk.assertions.Match
import software.amazon.awscdk.assertions.Template
import software.amazon.awscdk.services.lambda.Code

class MicroFsmSpikeStackTest {
    private val dummyAsset: String = java.nio.file.Files.createTempDirectory("lambda-dummy").also { java.nio.file.Files.writeString(it.resolve("x.txt"), "x") }.toString()
    private val template: Template = Template.fromStack(
        MicroFsmSpikeStack(
            App(), "T",
            StackProps.builder().env(Environment.builder().account("111111111111").region(REGION).build()).build(),
            Code.fromAsset(dummyAsset),
        ),
    )

    @Test
    fun `api function is java25 arm64 with SnapStart and a live alias`() {
        template.hasResourceProperties(
            "AWS::Lambda::Function",
            mapOf("Runtime" to "java25", "Architectures" to listOf("arm64"), "MemorySize" to 1024, "SnapStart" to mapOf("ApplyOn" to "PublishedVersions")),
        )
        template.hasResourceProperties("AWS::Lambda::Alias", mapOf("Name" to "live"))
        template.resourceCountIs("AWS::Lambda::Version", 1)
    }

    private fun functions() = template.findResources("AWS::Lambda::Function")

    @Suppress("UNCHECKED_CAST")
    private fun env(f: Map<String, Any>) =
        (((f["Properties"] as Map<String, Any>)["Environment"] as Map<String, Any>)["Variables"] as Map<String, Any>)

    @Test
    fun `exactly two functions, only Migrate has the migrate flag`() {
        assertThat(functions()).hasSize(2)
        assertThat(functions().filterKeys { it.startsWith("Api") }.values.single().let(::env)["APP_MIGRATE_ENDPOINT_ENABLED"]).isEqualTo("false")
        assertThat(functions().filterKeys { it.startsWith("Migrate") }.values.single().let(::env)["APP_MIGRATE_ENDPOINT_ENABLED"]).isEqualTo("true")
        assertThat(functions().values.filter { env(it)["APP_MIGRATE_ENDPOINT_ENABLED"] == "true" }).hasSize(1)
    }

    @Test
    fun `integrations and invoke permissions target only the Api alias`() {
        val aliasId = template.findResources("AWS::Lambda::Alias").keys.single()
        val ref = mapOf("Ref" to aliasId)
        val integrations = template.findResources("AWS::ApiGatewayV2::Integration").values
        assertThat(integrations).hasSize(1)
        @Suppress("UNCHECKED_CAST")
        assertThat((integrations.single()["Properties"] as Map<String, Any>)["IntegrationUri"]).isEqualTo(ref)
        val permissions = template.findResources("AWS::Lambda::Permission").values
        assertThat(permissions).isNotEmpty()
        permissions.forEach {
            @Suppress("UNCHECKED_CAST")
            assertThat((it["Properties"] as Map<String, Any>)["FunctionName"]).isEqualTo(ref)
        }
        val alias = template.findResources("AWS::Lambda::Alias").values.single().toString()
        assertThat(alias).contains("Api").doesNotContain("Migrate")
    }

    @Test
    fun `dsql cluster without deletion protection`() {
        template.hasResourceProperties("AWS::DSQL::Cluster", mapOf("DeletionProtectionEnabled" to false))
    }

    @Test
    fun `iam roles use the micro-fsm prefix`() {
        val names = template.findResources("AWS::IAM::Role").values.map { (it["Properties"] as Map<*, *>)["RoleName"] }
        assertThat(names).containsExactlyInAnyOrder("micro-fsm-api", "micro-fsm-migrate")
    }

    @Test
    fun `log retention is 7 days`() {
        template.hasResourceProperties("AWS::Logs::LogGroup", mapOf("RetentionInDays" to 7))
    }

    @Test
    fun `dsql permissions are scoped to the cluster, never wildcard`() {
        val policies = template.findResources("AWS::IAM::Policy").values
        val dsql = policies.flatMap { p ->
            @Suppress("UNCHECKED_CAST")
            val doc = (p["Properties"] as Map<String, Any>)["PolicyDocument"] as Map<String, Any>
            @Suppress("UNCHECKED_CAST")
            (doc["Statement"] as List<Map<String, Any>>).filter { s -> s["Action"].toString().contains("dsql:") }
        }
        assertThat(dsql).isNotEmpty()
        dsql.forEach { assertThat(it["Resource"].toString()).isNotEqualTo("*").doesNotContain("[*]") ; assertThat(it["Resource"].toString()).contains("Dsql") }
    }

    @Test
    fun `project and env tags are applied`() {
        template.hasResourceProperties(
            "AWS::Lambda::Function",
            mapOf("Tags" to Match.arrayWith(listOf(mapOf("Key" to "env", "Value" to "spike"), mapOf("Key" to "project", "Value" to "micro-fsm")))),
        )
        template.hasResourceProperties("AWS::DSQL::Cluster", mapOf("Tags" to Match.arrayWith(listOf(mapOf("Key" to "project", "Value" to "micro-fsm")))))
    }
}

class AccountBaselineStackTest {
    private val stack = MicroFsmAccountBaselineStack(
        App(), "B",
        StackProps.builder().env(Environment.builder().account("111111111111").region(BASELINE_REGION).build()).build(),
        "x@example.com",
    )
    private val template: Template = Template.fromStack(stack)

    @Test
    fun `lives in us-east-1 and contains only the budget`() {
        assertThat(stack.region).isEqualTo("us-east-1")
        assertThat(template.toJSON()["Resources"] as Map<*, *>).hasSize(1)
        template.resourceCountIs("AWS::Budgets::Budget", 1)
    }

    @Test
    fun `budget of 10 USD notifies at 50 80 100 percent actual`() {
        template.hasResourceProperties(
            "AWS::Budgets::Budget",
            mapOf("Budget" to Match.objectLike(mapOf("BudgetLimit" to mapOf("Amount" to 10, "Unit" to "USD"))),
                "NotificationsWithSubscribers" to Match.arrayWith(listOf(50, 80, 100).map {
                    Match.objectLike(mapOf("Notification" to Match.objectLike(mapOf("Threshold" to it, "NotificationType" to "ACTUAL"))))
                }),
                "ResourceTags" to Match.arrayWith(listOf(mapOf("Key" to "project", "Value" to "micro-fsm")))),
        )
    }
}
