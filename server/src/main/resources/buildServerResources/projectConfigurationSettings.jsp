<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%--magical import that you're just supposed to know about: gives css and js includes--%>
<%@ include file="/include.jsp" %>

<jsp:useBean id="currentProject" type="jetbrains.buildServer.serverSide.SProject" scope="request"/>
<jsp:useBean id="allServices" scope="request" type="java.lang.Object"/>
<jsp:useBean id="allServiceJspFiles" scope="request" type="java.lang.Object[]"/>

<html>
<body>
<h2>
    OpenTelemetry
</h2>
<div class="grayNote">
    Send build trace data to an OpenTelemetry collector, helping you visualize how to optimize your builds and their dependency trees
</div>

<bs:messages key="featureReset"/>
<bs:messages key="featureUpdated"/>

<div class="editSettingsPage">
    <form id="editOpenTelemetrySettingsPage" onsubmit="return BS.ProjectConfigurationSettings.save();" method="post" autocomplete="off">
        <table class="runnerFormTable">
            <c:if test='${isInherited || isOverridden}'>
                <tr>
                    <td colspan="2">
                        <c:if test='${isInherited}'>
                            <i>Inherited from project <a href="/admin/editProject.html?projectId=${inheritedFromProjectExternalId}&tab=Octopus.TeamCity.OpenTelemetry#"><c:out value="${inheritedFromProjectName}" /></a></i>
                        </c:if>
                        <c:if test='${isOverridden}'>
                            <i>Overrides configuration from project <a href="/admin/editProject.html?projectId=${overwritesInheritedFromProjectExternalId}&tab=Octopus.TeamCity.OpenTelemetry#"><c:out value="${overwritesInheritedFromProjectName}" /></a></i>
                            <forms:button onclick="BS.ProjectConfigurationSettings.reset()">Reset</forms:button>
                        </c:if>
                    </td>
                </tr>
            </c:if>
            <tr>
                <th><label for="enabled">Enabled:&nbsp;</label></th>
                <td>
                    <forms:checkbox name="enabled" checked="${otelEnabled}" >&nbsp;</forms:checkbox>
                    <span class="error" id="error_enabled"></span>
                </td>
            </tr>
            <tr>
                <th><label for="service">Service:&nbsp;<span class="mandatoryAsterix" title="Mandatory field">*</span></label></th>
                <td>
                    <%-- todo: add options for jaeger --%>
                    <%-- todo: add options for sumo --%>
                    <select name="service" id="service" onchange="BS.ProjectConfigurationSettings.serviceChanged(this)">
                        <c:forEach var="service" items="${allServices}">
                            <option value="${service}" <c:if test='${otelService == service}'>selected="selected"</c:if>>${service}</option>
                        </c:forEach>
                    </select>
                    <span class="error" id="error_service"></span>
                </td>
            </tr>
            <c:forEach var="service" items="${allServiceJspFiles}">
                <jsp:include page="${service}" />
            </c:forEach>

        </table>

        <div class="saveButtonsBlock" id="saveButtons">
            <forms:saving id="saveProgress"/>
            <forms:submit label="Save"/>
            <input type="hidden" name="projectId" value="${currentProject.externalId}"/>
            <input type="hidden" name="mode" value="save" />
            <input type="hidden" id="publicKey" name="publicKey" value="<c:out value='${publicKey}'/>"/>
        </div>
    </form>
</div>
<script>
    BS.ProjectConfigurationSettings.serviceChanged($j('#service'));
</script>
</body>
</html>
