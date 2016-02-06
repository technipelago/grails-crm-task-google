package grails.plugins.crm.task.google

import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.auth.oauth2.CredentialRefreshListener
import com.google.api.client.auth.oauth2.TokenErrorResponse
import com.google.api.client.auth.oauth2.TokenResponse
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeRequestUrl
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.googleapis.json.GoogleJsonResponseException
import com.google.api.client.http.HttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.DateTime
import com.google.api.services.calendar.CalendarScopes
import com.google.api.services.calendar.model.CalendarListEntry
import com.google.api.services.calendar.model.Event
import com.google.api.services.calendar.model.EventDateTime
import grails.events.Listener
import grails.plugins.crm.security.CrmUser
import grails.plugins.crm.task.CrmTask
import grails.transaction.Transactional
import org.apache.commons.lang.StringUtils
import org.springframework.beans.factory.InitializingBean

/**
 * Created by goran on 2016-02-06.
 */
class CrmGoogleCalendarService implements InitializingBean {

    private static final String APPLICATION_NAME = "GR8CRM-Calendar/0.2"
    private static final String GOOGLE_CALENDAR_API = 'google.calendar'
    public static final String OPTION_CALENDAR_ID = 'google.calendar.id'

    /** Global instance of the scopes required by this quickstart. */
    private static final List<String> CALENDAR_SCOPES = [CalendarScopes.CALENDAR].asImmutable()

    protected static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance()

    protected static HttpTransport HTTP_TRANSPORT

    static {
        try {
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport()
        } catch (Throwable t) {
            t.printStackTrace()
        }
    }

    def grailsApplication
    def grailsLinkGenerator
    def crmSecurityService
    def crmTaskService

    private GoogleApi api = new GoogleApi()

    @Override
    void afterPropertiesSet() throws Exception {
        def config = grailsApplication.config.crm.calendar.google ?: [:]

        api.clientId = getClientId()
        api.clientSecret = getClientSecret()
        api.clientEmail = (String) config.client.email
        api.privateKeyPem = (String) config.key.private
        api.privateKeyId = (String) config.key.id
        api.serviceName = GOOGLE_CALENDAR_API
        api.serviceUser = (String) config.user
    }

    protected String getClientId() {
        grailsApplication.config.crm.calendar.google.client.id ?: ''
    }

    protected String getClientSecret() {
        grailsApplication.config.crm.calendar.google.client.secret ?: ''
    }

    protected String getServiceName() {
        GOOGLE_CALENDAR_API
    }

    protected List<String> getScopes() {
        CALENDAR_SCOPES
    }

    @Transactional(readOnly = true)
    public String getAccessToken(CrmUser crmUser, String service) {
        if (!crmUser?.enabled) {
            throw new IllegalArgumentException("User not enabled: $crmUser")
        }
        crmUser.getOption("oauth2.${service}.token")
    }

    @Transactional
    public String setAccessToken(CrmUser crmUser, String service, String token) {
        if (!crmUser?.enabled) {
            throw new IllegalArgumentException("User not enabled: $crmUser")
        }
        crmUser.setOption("oauth2.${service}.token", token)

        return token
    }

    @Transactional(readOnly = true)
    public String getRefreshToken(CrmUser crmUser, String service) {
        if (!crmUser?.enabled) {
            throw new IllegalArgumentException("User not enabled: $crmUser")
        }
        crmUser.getOption("oauth2.${service}.token")
    }

    @Transactional
    public String setRefreshToken(CrmUser crmUser, String service, String token) {
        if (!crmUser?.enabled) {
            throw new IllegalArgumentException("User not enabled: $crmUser")
        }
        crmUser.setOption("oauth2.${service}.refresh", token)

        return token
    }

    public GoogleTokenResponse requestAccessToken(String code, String callbackUri) {
        new GoogleAuthorizationCodeTokenRequest(HTTP_TRANSPORT, JSON_FACTORY,
                getClientId(), getClientSecret(), code, callbackUri)
                .setScopes(getScopes())
                .execute()
    }

    public void extractTokensFromResponse(CrmUser user, String code, String callbackUri) {
        GoogleTokenResponse response = requestAccessToken(code, callbackUri)
        setAccessToken(user, getServiceName(), response.getAccessToken())
        if (StringUtils.isNotBlank(response.getRefreshToken())) {
            setRefreshToken(user, getServiceName(), response.getRefreshToken())
        }
    }

    protected Credential getCredential(final CrmUser crmUser, final GoogleApi api) {
        final String serviceName = api.getServiceName()
        String accessToken = getAccessToken(crmUser, serviceName)
        if (!accessToken) {
            throw new IllegalStateException("User [$crmUser] is not authorized to connect to the [$serviceName] service")
        }
        String refreshToken = getRefreshToken(crmUser, serviceName)
        Credential cred = new GoogleCredential.Builder()
                .setTransport(HTTP_TRANSPORT)
                .setJsonFactory(JSON_FACTORY)
                .setClientSecrets(api.getClientId(), api.getClientSecret())
                .setServiceAccountUser(api.getServiceUser())
                .setServiceAccountId(api.getClientEmail())
                .setServiceAccountScopes(getScopes())
                .setServiceAccountPrivateKey(api.getPrivateKey())
                .setServiceAccountPrivateKeyId(api.getPrivateKeyId())
                .addRefreshListener(
                new CredentialRefreshListener()
                {
                    @Override
                    void onTokenResponse(Credential credential, TokenResponse tokenResponse) throws IOException {
                        println "Great we got a token response in the listener! ${tokenResponse.getAccessToken()}"
                        setAccessToken(crmUser, serviceName, tokenResponse.getAccessToken())
                        if (StringUtils.isNotBlank(tokenResponse.getRefreshToken())) {
                            setRefreshToken(crmUser, serviceName, tokenResponse.getRefreshToken())
                        }
                    }

                    @Override
                    void onTokenErrorResponse(Credential credential, TokenErrorResponse tokenErrorResponse) throws IOException {
                        println "ERRRRRRRRRRRR ${tokenErrorResponse.getError()} ${tokenErrorResponse.getErrorDescription()}"
                    }
                })
                .build()
                .setAccessToken(accessToken)
                .setRefreshToken(refreshToken)

        return cred
    }

    public String getAuthorizationUri(String state, String callbackUri) {
        new GoogleAuthorizationCodeRequestUrl(getClientId(), callbackUri, getScopes())
                .setState(state)
                .setAccessType("offline")
                .setApprovalPrompt("force")
                .build()
    }

    public boolean isCalendarConfigured(CrmUser crmUser) {
        getAccessToken(crmUser, GOOGLE_CALENDAR_API) != null
    }

    @Transactional(readOnly = true)
    public String getCalendarId(CrmUser crmUser) {
        crmUser.getOption(OPTION_CALENDAR_ID)
    }

    @Transactional
    public void setCalendarId(CrmUser crmUser, String id) {
        crmUser.setOption(OPTION_CALENDAR_ID, id)
    }

    /**
     * Build and return an authorized Calendar client service.
     * @return an authorized Calendar client service
     * @throws IOException
     */
    public com.google.api.services.calendar.Calendar getCalendarService(CrmUser crmUser) {
        Credential credential = getCredential(crmUser, api)
        new com.google.api.services.calendar.Calendar.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build()
    }

    public com.google.api.services.calendar.model.Calendar addCalendar(com.google.api.services.calendar.Calendar client,
                                                                       String name, String description, String location, String timezone) {
        com.google.api.services.calendar.model.Calendar entry = new com.google.api.services.calendar.model.Calendar()

        entry.setSummary(name)
        entry.setLocation(location)
        entry.setDescription(description)
        entry.setTimeZone(timezone)

        client.calendars().insert(entry).execute()
    }

    private String createEventId(CrmTask crmTask) {
        return "crm${crmTask.tenantId}id${crmTask.id}".toString()
    }

    @Listener(namespace = 'crmTask', topic = 'created')
    def taskCreated(data) {
        Thread.sleep(3000) // Wait for transaction to complete, and a little extra.
        crmSecurityService.runAs(data.user, data.tenant) {
            def crmTask = crmTaskService.getTask(data.id)
            if (crmTask?.username) {
                def user = crmSecurityService.getUser(crmTask.username)
                if (user) {
                    syncTask(crmTask, user)
                }
            }
        }
    }

    @Listener(namespace = 'crmTask', topic = 'updated')
    def taskUpdated(data) {
        Thread.sleep(3000) // Wait for transaction to complete, and a little extra.
        crmSecurityService.runAs(data.user, data.tenant) {
            def crmTask = crmTaskService.getTask(data.id)
            if (crmTask?.username) {
                def user = crmSecurityService.getUser(crmTask.username)
                if (user) {
                    syncTask(crmTask, user)
                }
            }
        }
    }

    @Listener(namespace = 'crmTask', topic = 'delete')
    def taskAboutToBeDeleted(data) {
        crmSecurityService.runAs(data.user, data.tenant) {
            def crmTask = crmTaskService.getTask(data.id)
            if (crmTask?.username) {
                def user = crmSecurityService.getUser(crmTask.username)
                if (user) {
                    try {
                        deleteTask(crmTask, user)
                    } catch (Exception e) {
                        log.error("Could not delete calendar event for crmTask@${crmTask.id}", e)
                    }
                }
            }
        }
    }

    public void syncTask(CrmTask crmTask, CrmUser crmUser) {
        final String calendarId = crmUser.getOption(OPTION_CALENDAR_ID)
        if (!calendarId) {
            log.debug("User $crmUser has no connected calendar")
            return
        }
        com.google.api.services.calendar.Calendar client = getCalendarService(crmUser)
        log.debug "Syncing crmTask@${crmTask.id} with Google Calendar..."
        CalendarListEntry calendar = client.calendarList().get(calendarId).execute()
        if (calendar?.getAccessRole() == 'owner') {
            final String eventId = createEventId(crmTask)
            boolean insert = false
            Event event

            try {
                event = client.events().get(calendarId, eventId).execute()
            } catch (GoogleJsonResponseException e) {
                if (e.getStatusCode() != 404) {
                    throw e
                }
            }

            if (event) {
                // Event was found
                if (crmTask.hidden) {
                    // Event should be removed from the calendar!
                    client.events().delete(calendarId, eventId).execute()
                    log.debug "crmTask@${crmTask.id}'s visibility changed to hidden, removed from $calendarId"
                    return
                }
            } else if (!crmTask.hidden) {
                // Event is marked visible but was not found in the calendar, create a new event.
                event = new Event().setId(eventId)
                insert = true
            } else {
                return // not found and hidden.
            }

            event.setSummary(crmTask.name)
                    .setLocation(crmTask.location)
                    .setDescription(crmTask.description)

            DateTime startDateTime = new DateTime(crmTask.startTime)
            EventDateTime start = new EventDateTime()
                    .setDateTime(startDateTime)
                    .setTimeZone(calendar.getTimeZone())
            event.setStart(start)

            DateTime endDateTime = new DateTime(crmTask.endTime)
            EventDateTime end = new EventDateTime()
                    .setDateTime(endDateTime)
                    .setTimeZone(calendar.getTimeZone())
            event.setEnd(end)

            Event.Source source = event.getSource()
            if (source == null) {
                source = new Event.Source()
                event.setSource(source)
            }
            source.setTitle(crmTask.name)
            source.setUrl(grailsLinkGenerator.link(controller: 'crmTask', action: 'show', id: crmTask.id, absolute: true))

            event.setStatus('confirmed')
            //event.setTransparency('transparent')

            if (insert) {
                event = client.events().insert(calendarId, event).execute()
                log.debug "Calendar event created: ${event.getHtmlLink()}"
            } else {
                event = client.events().update(calendarId, eventId, event).execute()
                log.debug "Calendar event updated: ${event.getHtmlLink()}"
            }
        } else {
            log.warn("Not calendar owner")
        }
    }

    public void deleteTask(CrmTask crmTask, CrmUser crmUser) {
        final String calendarId = crmUser.getOption(OPTION_CALENDAR_ID)
        if (!calendarId) {
            log.debug("User $crmUser has no connected calendar")
            return
        }
        com.google.api.services.calendar.Calendar client = getCalendarService(crmUser)
        CalendarListEntry calendar = client.calendarList().get(calendarId).execute()
        if (calendar?.getAccessRole() == 'owner') {
            final String eventId = createEventId(crmTask)
            client.events().delete(calendarId, eventId).execute()
            log.debug "Deleted crmTask@${crmTask.id} from Google Calendar..."
        }
    }
}
