plugins {
    alias(libs.plugins.kotlin.jvm)
    application
}

kotlin { jvmToolchain(25) }

dependencies {
    implementation(libs.aws.cdk.lib)
    implementation(libs.constructs)
    testImplementation(platform("org.junit:junit-bom:5.13.4"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("org.assertj:assertj-core:${libs.versions.assertj.get()}")
}

application { mainClass.set("se.microfsm.infra.AppKt") }

tasks.test { useJUnitPlatform() }
