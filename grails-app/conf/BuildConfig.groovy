grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"
grails.project.target.level = 1.6

grails.project.fork = [
    //  compile: [maxMemory: 256, minMemory: 64, debug: false, maxPerm: 256, daemon:true],
    test: [maxMemory: 768, minMemory: 64, debug: false, maxPerm: 256, forkReserve:false],
    run: [maxMemory: 768, minMemory: 64, debug: false, maxPerm: 256, forkReserve:false],
    war: [maxMemory: 768, minMemory: 64, debug: false, maxPerm: 256, forkReserve:false],
    console: [maxMemory: 768, minMemory: 64, debug: false, maxPerm: 256]
]

grails.project.dependency.resolver = "maven"
grails.project.dependency.resolution = {
    inherits "global"
    log "warn"
    repositories {
        grailsCentral()
        mavenLocal()
        mavenCentral()
    }
    dependencies {
        compile 'com.google.api-client:google-api-client:1.21.0'
        compile 'com.google.api-client:google-api-client-java6:1.21.0'
        compile 'com.google.oauth-client:google-oauth-client-jetty:1.21.0'
        compile 'com.google.apis:google-api-services-calendar:v3-rev160-1.21.0'
        compile 'org.apache.httpcomponents:httpclient:4.5.3'

        // See https://jira.grails.org/browse/GPHIB-30
        test("javax.validation:validation-api:1.1.0.Final") { export = false }
        test("org.hibernate:hibernate-validator:5.0.3.Final") { export = false }
    }

    plugins {
        build(":release:3.1.2",
                ":rest-client-builder:2.1.1") {
            export = false
        }
        test(":hibernate4:4.3.6.1") {
            excludes "net.sf.ehcache:ehcache-core"  // remove this when http://jira.grails.org/browse/GPHIB-18 is resolved
            export = false
        }

        //test(":codenarc:0.24.1") { export = false }
        //test(":code-coverage:2.0.3-3") { export = false }

        compile ":crm-task:2.5.0"
    }
}
/*
codenarc.reports = {
    xmlReport('xml') {
        outputFile = 'target/CodeNarcReport.xml'
    }
    htmlReport('html') {
        outputFile = 'target/CodeNarcReport.html'
    }
}
*/
