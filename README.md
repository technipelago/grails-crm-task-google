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
