pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "openai-kmp"

include(":openai-core")
include(":openai-client")
include(":openai-responses")
include(":openai-chat")
include(":openai-embeddings")
include(":openai-files")
include(":openai-batches")
include(":openai-models")
include(":openai-moderations")
include(":openai-images")
include(":openai-audio")
include(":openai-finetuning")
include(":openai-vectorstores")
include(":openai-uploads")
include(":samples:basic")
