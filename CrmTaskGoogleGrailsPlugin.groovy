/*
 * Copyright (c) 2016 Goran Ehrsson.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

class CrmTaskGoogleGrailsPlugin {
    def version = "2.4.0-SNAPSHOT"
    def grailsVersion = "2.4 > *"
    def dependsOn = [:]
    def loadAfter = ['crmTask']
    def pluginExcludes = [
        "grails-app/views/error.gsp"
    ]
    def title = "GR8 CRM Google Calendar Sync"
    def author = "Goran Ehrsson"
    def authorEmail = "goran@technipelago.se"
    def description = '''\
Provides synchronization between GR8 CRM tasks and Google Calendar events.
'''
    def documentation = "http://gr8crm.github.io/plugins/crm-task-google/"
    def license = "APACHE"
    def organization = [ name: "Technipelago AB", url: "http://www.technipelago.se/" ]
    def issueManagement = [system: "github", url: "https://github.com/technipelago/grails-crm-task-google/issues"]
    def scm = [url: "https://github.com/technipelago/grails-crm-task-google"]

    def doWithWebDescriptor = { xml ->
        // TODO Implement additions to web.xml (optional), this event occurs before
    }

    def doWithSpring = {
        // TODO Implement runtime spring config (optional)
    }

    def doWithDynamicMethods = { ctx ->
        // TODO Implement registering dynamic methods to classes (optional)
    }

    def doWithApplicationContext = { ctx ->
        // TODO Implement post initialization spring config (optional)
    }

    def onChange = { event ->
        // TODO Implement code that is executed when any artefact that this plugin is
        // watching is modified and reloaded. The event contains: event.source,
        // event.application, event.manager, event.ctx, and event.plugin.
    }

    def onConfigChange = { event ->
        // TODO Implement code that is executed when the project configuration changes.
        // The event is the same as for 'onChange'.
    }

    def onShutdown = { event ->
        // TODO Implement code that is executed when the application shuts down (optional)
    }
}
