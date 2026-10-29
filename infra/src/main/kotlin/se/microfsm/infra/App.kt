package se.microfsm.infra

import software.amazon.awscdk.App
import software.amazon.awscdk.Environment
import software.amazon.awscdk.StackProps
import software.amazon.awscdk.services.lambda.Code

const val REGION = "eu-north-1"

fun main() {
    val app = App()
    val email = app.node.tryGetContext("budgetEmail") as? String
    require(!email.isNullOrBlank()) {
        "Missing required context 'budgetEmail'. Run e.g.: npx cdk synth -c budgetEmail=you@example.com"
    }
    val zip = (app.node.tryGetContext("lambdaZip") as? String) ?: "../backend/build/function.zip"
    // Account comes from the CLI environment (credentials); not hard-coded.
    val account = System.getenv("CDK_DEFAULT_ACCOUNT")
    MicroFsmSpikeStack(
        app, "MicroFsmSpikeStack",
        StackProps.builder().env(Environment.builder().account(account).region(REGION).build()).build(),
        Code.fromAsset(zip), email,
    )
    app.synth()
}
