# GR8 CRM - Task Synchronization with Google Calendar

CRM = [Customer Relationship Management](http://en.wikipedia.org/wiki/Customer_relationship_management)

GR8 CRM is a set of [Grails Web Application Framework](http://www.grails.org/)
plugins that makes it easy to develop web application with CRM functionality.
With CRM we mean features like:

- Contact Management
- Task/Todo Lists
- Project Management


## Task Synchronization with Google Calendar

This plugin adds an event listener that is triggered when CrmTask instances are created or updated in a GR8 CRM application.
The tasks are synchronized with Google Calendar. It will create/update an event in a calendar owned by the user that owns the CrmTask instance.

### Configuration

First you must create a Google API project in [Google Developers Console.](https://console.developers.google.com/).
Then add a **Web Application** credential to the API project.
After that you have a couple of parameters that you must add to your Grails configuration (Config.groovy or external config).

    crm.calendar.google.client.id = "******googleusercontent.com"
    crm.calendar.google.client.secret = "*********"
    crm.calendar.google.key.id = "**************"
    crm.calendar.google.client.email = "user@*******gserviceaccount.com"
    crm.calendar.google.key.private = "-----BEGIN PRIVATE KEY-----\n***********\n-----END PRIVATE KEY-----\n"
    crm.calendar.google.user = "serviceaccount@yourdomain.com"

A named URL mapping must be added to your Grails application's URLMappings.groovy.
This mapping should point to the OAuth2 callback URL that you want to use
when authorizing your users with Google. A default callback is provided
by the plugin at `CrmGoogleCalendarController#callback()`.

    name 'crm-google-calendar-auth': "/oauth2/callback" {
        controller = 'crmGoogleCalendar'
        action = 'callback'
    }

The path to this action must be configured in the Web Application credential in the Google API project.

Now you should be able to start your Grails application and visit http://localhost:8080/myapp/crmGoogleCalendar.
You should be redirected to your Google Login and you will have to authorized your Grails application
to access your Google calendar. Once you have authorized the app you can attach one of your calendars to
your Grails application and CrmTask instances created with the crm-task/crm-task-ui plugin will be synchronized with your Google calendar.
