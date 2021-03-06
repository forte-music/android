// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    repositories {
        google()
        jcenter()
    }

    dependencies {
        classpath("com.android.tools.build:gradle:3.3.0")
        classpath("com.apollographql.apollo:apollo-gradle-plugin:1.0.0-alpha5")
        classpath(kotlin("gradle-plugin", version = "1.3.20"))

        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle.kts files
    }
}

allprojects {
    repositories {
        google()
        jcenter()

        maven {
            setUrl("https://dl.bintray.com/apollographql/android")
        }
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
