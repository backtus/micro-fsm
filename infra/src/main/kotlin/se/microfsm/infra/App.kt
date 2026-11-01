package se.microfsm.infra

import software.amazon.awscdk.App
import software.amazon.awscdk.Environment
import software.amazon.awscdk.StackProps
import software.amazon.awscdk.services.lambda.Code

const val REGION = "eu-north-1"

/** AWS Budgets is unavailable in eu-north-1; billing metadata only. */
const val BASELINE_REGION = "us-east-1"

fun main() {
    val app = App()
    val email = app.node.tryGetContext("budgetEmail") as? String
    require(!email.isNullOrBlank()) {
        "Missing required context 'budgetEmail'. Run e.g.: npx cdk synth -c budgetEmail=you@example.com"
    }
    val zip = (app.node.tryGetContext("lambdaZip") as? String) ?: "../backend/build/function.zip"
    // Account comes from the CLI environment (credentials); not hard-coded.
    val account = System.getenv("CDK_DEFAULT_ACCOUNT")
    fun props(region: String) = StackProps.builder().env(Environment.builder().account(account).region(region).build()).build()
    val baseline = MicroFsmAccountBaselineStack(app, "MicroFsmAccountBaselineStack", props(BASELINE_REGION), email)
    val spike = MicroFsmSpikeStack(app, "MicroFsmSpikeStack", props(REGION), Code.fromAsset(zip))
    spike.addDependency(baseline) // deploy the budget first
    app.synth()
}
