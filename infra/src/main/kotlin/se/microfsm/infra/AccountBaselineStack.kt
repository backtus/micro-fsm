package se.microfsm.infra

import software.amazon.awscdk.Stack
import software.amazon.awscdk.StackProps
import software.amazon.awscdk.Tags
import software.amazon.awscdk.services.budgets.CfnBudget
import software.constructs.Construct

/**
 * Billing metadata only (no workload, no data). AWS Budgets is not available in eu-north-1, so this stack lives in
 * us-east-1 (region exception, PROPOSED in docs/DECISIONS.md). Deploy it BEFORE the spike stack.
 */
class MicroFsmAccountBaselineStack(scope: Construct, id: String, props: StackProps, budgetEmail: String) :
    Stack(scope, id, props) {
    init {
        Tags.of(this).add("project", "micro-fsm")
        Tags.of(this).add("env", "spike")

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
    }
}
