package com.octopus.teamcity.opentelemetry.server.endpoints;

import com.octopus.teamcity.opentelemetry.server.endpoints.custom.CustomOTELEndpointHandler;
import com.octopus.teamcity.opentelemetry.server.endpoints.honeycomb.HoneycombOTELEndpointHandler;
import com.octopus.teamcity.opentelemetry.server.endpoints.zipkin.ZipkinOTELEndpointHandler;
import jetbrains.buildServer.serverSide.TeamCityNodes;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

public class OTELEndpointFactory {

    @NotNull
    private final PluginDescriptor pluginDescriptor;
    @NotNull
    private final TeamCityNodes teamcityNodesService;

    public OTELEndpointFactory(
            @NotNull PluginDescriptor pluginDescriptor,
            @NotNull TeamCityNodes teamcityNodesService)
    {
        this.pluginDescriptor = pluginDescriptor;
        this.teamcityNodesService = teamcityNodesService;
    }

    public IOTELEndpointHandler getOTELEndpointHandler(String otelService)
    {
        return getOTELEndpointHandler(OTELService.get(otelService).get());
    }

    public IOTELEndpointHandler getOTELEndpointHandler(OTELService otelService)
    {
        return getOTELEndpointHandlers().stream()
                .filter(handler -> handler.getServiceName().equals(otelService.getValue()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid service name " + otelService));
    }

    public Collection<IOTELEndpointHandler> getOTELEndpointHandlers() {
        return List.of(
            new HoneycombOTELEndpointHandler(pluginDescriptor, teamcityNodesService),
            new ZipkinOTELEndpointHandler(pluginDescriptor),
            new CustomOTELEndpointHandler(pluginDescriptor)
        );
    }
}

