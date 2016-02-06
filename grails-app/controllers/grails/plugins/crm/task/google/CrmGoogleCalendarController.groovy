package grails.plugins.crm.task.google

import com.google.api.client.googleapis.json.GoogleJsonResponseException
import grails.plugins.crm.security.CrmUser
import grails.transaction.Transactional
import org.apache.commons.lang.StringUtils

import java.security.SecureRandom

/**
 * Google Calendar admin controller.
 */
class CrmGoogleCalendarController {

    private static final String SESSION_KEY_STATE = 'state'

    static allowedMethods = [create: 'POST', attach: 'POST', remove: 'POST']

    def crmSecurityService
    def crmGoogleCalendarService

    /**
     * Create a state token to prevent request forgery.
     */
    private String createStateToken() {
        new BigInteger(130, new SecureRandom()).toString(32)
    }

    private String getCallbackUri() {
        createLink(mapping: 'crm-google-calendar-auth', absolute: true)
    }

    def index() {
        final CrmUser user = crmSecurityService.getCurrentUser()

        if (!crmGoogleCalendarService.isCalendarConfigured(user)) {
            redirect action: 'setup'
            return
        }

        final Map model = [current: crmGoogleCalendarService.getCalendarId(user)]
        try {
            model.result = crmGoogleCalendarService.getCalendarService(user).calendarList().list().execute().getItems().findAll {
                it.getAccessRole() == 'owner'
            }
        } catch (GoogleJsonResponseException e) {
            redirect action: 'setup'
            return
        } catch (IllegalStateException e) {
            redirect action: 'setup'
            return
        }
        catch (Exception e) {
            log.error "Error when connecting to Google API", e
            flash.error = e.message
        }

        model
    }

    def setup() {
        String stateToken = createStateToken()
        request.session.setAttribute(SESSION_KEY_STATE, stateToken)
        redirect uri: crmGoogleCalendarService.getAuthorizationUri(stateToken, getCallbackUri())
    }

    def callback(String code, String state, String error) {
        if (request.session.getAttribute(SESSION_KEY_STATE) != state) {
            log.warn "Invalid state $state != ${request.session.getAttribute(SESSION_KEY_STATE)}"
        }
        request.session.removeAttribute(SESSION_KEY_STATE)

        def user = crmSecurityService.getCurrentUser()

        if (StringUtils.isNotBlank(error)) {
            log.warn "User $user did not authorize the application"
            redirect mapping: 'home' // bye bye.
            return
        }

        crmGoogleCalendarService.extractTokensFromResponse(user, code, getCallbackUri())

        redirect action: 'index'
    }

    @Transactional
    def remove(String id) {
        def user = crmSecurityService.getCurrentUser()
        crmGoogleCalendarService.setCalendarId(user, null)
        flash.warning = message(code: 'crm.task.google.calendar.disconnected.message')
        redirect action: 'index'
    }

    def create(String name, String location, String description, String timezone) {
        if (!name) {
            def applicationName = grails.util.Metadata.current.applicationName ?: 'GR8CRM'
            def randomNumber = new Random(System.currentTimeMillis()).nextInt(9000) + 1000
            name = message(code: 'crm.task.google.calendar.name', default: '{0}-{1}',
                    args: [applicationName, randomNumber])
        }
        if (!location) {
            location = message(code: 'crm.task.google.calendar.location', default: '')
        }
        if (!description) {
            description = message(code: 'crm.task.google.calendar.description', default: '')
        }
        if (!timezone) {
            timezone = message(code: 'crm.task.google.calendar.timezone', default: '')
        }

        def user = crmSecurityService.getCurrentUser()
        def next = 'index'
        try {
            com.google.api.services.calendar.Calendar client = crmGoogleCalendarService.getCalendarService(user)
            com.google.api.services.calendar.model.Calendar calendar = crmGoogleCalendarService.addCalendar(client, name, description, location, timezone)
            flash.success = message(code: 'crm.task.google.calendar.created.message', args: [calendar.getSummary()])
        } catch (GoogleJsonResponseException e) {
            next = 'setup'
        } catch (Exception e) {
            log.error "Exception when connecting to Google API", e
            flash.error = e.message
        }

        redirect action: next
    }

    @Transactional
    def attach(String id) {
        def user = crmSecurityService.getCurrentUser()
        def next = 'index'

        try {
            com.google.api.services.calendar.Calendar client = crmGoogleCalendarService.getCalendarService(user)
            com.google.api.services.calendar.model.Calendar calendar = client.calendarList().get(id).execute()
            if (calendar) {
                crmGoogleCalendarService.setCalendarId(user, calendar.getId())
                flash.success = message(code: 'crm.task.google.calendar.attached.message', args: [calendar.getSummary()])
            } else {
                flash.error = message(code: 'crm.task.google.calendar.not.found.error', args: [id])
            }
        } catch (GoogleJsonResponseException e) {
            next = 'setup'
        } catch (Exception e) {
            log.error "Exception when connecting to Google API", e
            flash.error = e.message
        }

        redirect action: next
    }
}
