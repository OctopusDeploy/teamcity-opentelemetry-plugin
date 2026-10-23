package com.octopus.teamcity.opentelemetry.server;

import com.octopus.teamcity.opentelemetry.server.endpoints.IOTELEndpointHandler;
import com.octopus.teamcity.opentelemetry.server.endpoints.OTELEndpointFactory;
import jetbrains.buildServer.controllers.admin.projects.EditProjectTab;
import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.serverSide.crypt.RSACipher;
import jetbrains.buildServer.web.openapi.PagePlaces;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import org.jetbrains.annotations.NotNull;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.octopus.teamcity.opentelemetry.common.PluginConstants.*;

public class ProjectConfigurationTab extends EditProjectTab {
    @NotNull
    private final PluginDescriptor pluginDescriptor;
    @NotNull
    private final ProjectManager projectManager;
    @NotNull
    private final OTELEndpointFactory otelEndpointFactory;

    public ProjectConfigurationTab(
            @NotNull PagePlaces pagePlaces,
            @NotNull PluginDescriptor pluginDescriptor,
            @NotNull ProjectManager projectManager,
            @NotNull OTELEndpointFactory otelEndpointFactory
        ) {
        super(pagePlaces, "Octopus.TeamCity.OpenTelemetry", "projectConfigurationSettings.jsp", "OpenTelemetry");
        this.pluginDescriptor = pluginDescriptor;
        this.projectManager = projectManager;
        this.otelEndpointFactory = otelEndpointFactory;

        register();
    }

    @Override
    public void fillModel(@NotNull Map<String, Object> model, @NotNull HttpServletRequest request) {
        super.fillModel(model, request);

        SProject project = getProject(request);

        var features = project.getAvailableFeaturesOfType(PLUGIN_NAME);
        model.put("publicKey", RSACipher.getHexEncodedPublicKey());

        // The form is built from these regardless of whether the project has been configured yet;
        // the JSP declares them with jsp:useBean, which fails the whole page if either is absent.
        var handlers = otelEndpointFactory.getOTELEndpointHandlers();
        model.put("allServices", handlers.stream().map(IOTELEndpointHandler::getServiceName).toArray());
        model.put("allServiceJspFiles", handlers.stream()
                .map(handler -> pluginDescriptor.getPluginResourcesPath(handler.getJspPath())).toArray());

        if ((long) features.size() == 0) {
            model.put("isEnabled", false);
        }
        else {
            var feature = features.stream().findFirst().get();
            if (feature.getProjectId().equals(project.getProjectId())) {
                model.put("isInherited", false);
                if (features.size() > 1) {
                    model.put("isOverridden", true);
                    var projectId = features.stream().skip(1).findFirst().get().getProjectId();
                    var sourceProject = projectManager.findProjectById(projectId);
                    model.put("overwritesInheritedFromProjectExternalId", sourceProject.getExternalId());
                    model.put("overwritesInheritedFromProjectName", sourceProject.getName());
                }
            } else {
                model.put("isInherited", true);
                var sourceProject = projectManager.findProjectById(feature.getProjectId());
                model.put("inheritedFromProjectName", sourceProject.getName());
                model.put("inheritedFromProjectExternalId", sourceProject.getExternalId());
                model.put("isOverridden", false);
            }
            var params = feature.getParameters();

            var service = otelEndpointFactory.getOTELEndpointHandler(params.get(PROPERTY_KEY_SERVICE));

            model.put("otelEnabled", params.get(PROPERTY_KEY_ENABLED));
            model.put("otelService", params.get(PROPERTY_KEY_SERVICE));

            service.mapParamsToModel(params, model);
        }
    }

    @NotNull
    @Override
    public List<String> getJsPaths() {
        var list = new ArrayList<>(List.of(pluginDescriptor.getPluginResourcesPath("projectConfigurationSettings.js")));

        otelEndpointFactory.getOTELEndpointHandlers()
                .stream()
                .map(IOTELEndpointHandler::getJsPaths)
                .forEach(paths -> {
                    paths.forEach(path -> list.add(pluginDescriptor.getPluginResourcesPath(path)));
                });
        return list;
    }

    @NotNull
    @Override
    public List<String> getCssPaths() {
        var list = new ArrayList<>(List.of(pluginDescriptor.getPluginResourcesPath("projectConfigurationSettings.css")));

        otelEndpointFactory.getOTELEndpointHandlers()
                .stream()
                .map(IOTELEndpointHandler::getCssPaths)
                .forEach(paths -> {
                    paths.forEach(path -> list.add(pluginDescriptor.getPluginResourcesPath(path)));
                });
        return list;
    }
}
