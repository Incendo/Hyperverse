repositories {
    maven("https://nexus.proximyst.com/repository/maven-public/")
}

dependencies {
    compileOnly(projects.hyperverseNmsCommon)
    compileOnly("org.spigotmc:spigot:1.14.4-R0.1-SNAPSHOT")
}
