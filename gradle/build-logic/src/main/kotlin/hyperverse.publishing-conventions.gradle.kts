import org.incendo.cloudbuildlogic.city

plugins {
    id("org.incendo.cloud-build-logic.publishing")
}

indra {
    github("Incendo", "hyperverse") {
        ci(true)
    }
    mitLicense()

    configurePublications {
        pom {
            developers {
                city()
            }
        }
    }
}
