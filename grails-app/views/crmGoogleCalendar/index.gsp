<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="main">
    <title>Google kalender</title>
    <style type="text/css">
    #calendars td > form {
        margin: 0;
    }
    </style>
</head>

<body>

<h1>Google kalender</h1>

<div class="row-fluid">
    <div class="span8">
        <p class="lead">
            Genom att ansluta din Google-kalender kan du se dina aktiviteter i samma
            kalendervy som du är van vid, på datorn eller i mobilen.
        </p>

        <p>
            Om du vill att de aktiviteter du är anvarig för ska synas i din Google-kalender ansluter du kalendern här nedan.
            Dina aktiviteter kommer då automatiskt att synkroniseras med din Google-kalender.
        </p>

        <p>
            Om du vill se dina kollegors aktiviteter eller om du vill att dina kollegor ska kunna se
            dina aktiviteter använder du bara delningsfunktionen som är standard i Google kalender.
        </p>
    </div>

    <div class="span4">
        <p class="alert alert-danger">
            <strong>OBS!</strong>
            Anslut inte din privata Google-kalender. Om du senare vill dela ut kalendern med dina
            kollegor så vill du förmodligen inte dela ut din privata kalender. Skapa istället en ny kalender
            och anslut den till tjänsten. Knappen <strong>Skapa ny kalender</strong> gör precis det.
        Den skapar en ny kalender i ditt Google-konto.
        </p>
    </div>
</div>

<h2>Mina kalendrar</h2>

<table id="calendars" class="table">
    <thead>
    <tr>
        <th>Kalender</th>
        <th>Beskrivning</th>
        <th>Tidszon</th>
        <th style="min-width:80px;"></th>
    </tr>
    </thead>
    <tbody>
    <g:each in="${result}" var="cal">
        <tr class="${cal.getId() == current ? 'current' : ''}">
            <td><a href="https://calendar.google.com/calendar/embed?src=${cal.getId().encodeAsIsoURL()}&ctz=${cal.getTimeZone()}/"
                   target="_blank">${cal.getSummary()}</a></td>
            <td>${cal.getDescription()}</td>
            <td>${cal.getTimeZone()}</td>
            <td style="min-width:100px;">
                <g:if test="${cal.getId() == current}">
                    <g:form action="remove">
                        <input type="hidden" name="id" value="${cal.getId()}"/>
                        <crm:button action="remove" label="Koppla bort" class="btn-small" icon="icon-trash icon-white"
                                    visual="danger" style="width: 100%;"
                                    confirm="Bekräfta att du vill ta bort anslutningen till kalendern. Aktiviteter och påminnelser kommer inte längre att synkroniseras med denna kalender"/>

                    </g:form>
                </g:if>
                <g:else>
                    <g:form action="attach">
                        <input type="hidden" name="id" value="${cal.getId()}"/>
                        <crm:button action="attach" label="Anslut" class="btn-small" icon="icon-resize-small icon-white"
                                    visual="primary" style="width: 100%;"
                                    confirm="Bekräfta att du vill ansluta till Google-kalendern ${cal.getSummary()}"/>
                    </g:form>
                </g:else>
            </td>
        </tr>
    </g:each>
    <tr>
        <td colspan="3"></td>
        <td>
            <g:form action="create">
                <crm:button action="create" label="Skapa ny kalender" class="btn-small" icon="icon-plus icon-white"
                            visual="success" style="width: 100%;"
                            confirm="Bekräfta att du vill skapa en ny kalender i ditt Google-konto"/>
            </g:form>
        </td>
    </tr>
    </tbody>
</table>

</body>
</html>