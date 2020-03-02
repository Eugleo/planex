plugins {
    application
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.apache.poi:poi:4.1.2") // xls reading
    implementation("org.jsoup:jsoup:1.13.1") // html parsing
    implementation(files("lib/com.google.ortools.jar")) // cp solver
    implementation(files("lib/protobuf.jar")) // ortools dependency
}

application {
    mainClassName = "com.wybitul.planex.Main"
    applicationDefaultJvmArgs = listOf("-Djava.library.path=lib")
}
