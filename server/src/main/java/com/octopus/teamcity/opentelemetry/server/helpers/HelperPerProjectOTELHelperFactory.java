package com.octopus.teamcity.opentelemetry.server.helpers;

import com.octopus.teamcity.opentelemetry.server.endpoints.OTELEndpointFactory;
import jetbrains.buildServer.serverSide.BuildPromotion;
import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.serverSide.TeamCityProperties;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

import static com.octopus.teamcity.opentelemetry.common.PluginConstants.*;

public class HelperPerProjectOTELHelperFactory implements OTELHelperFactory {
    static Logger LOG = Logger.getLogger(HelperPerProjectOTELHelperFactory.class.getName());
    private final ConcurrentHashMap<String, OTELHelper> otelHelpers;
    @NotNull
    private final ProjectManager projectManager;
    @NotNull
    private final OTELEndpointFactory otelEndpointFactory;
    private static final String DEFAULT_HELPER_EVICTION_PERIOD_MS = "300000"; // 5 minutes
    private static final String DEFAULT_HELPER_INACTIVE_THRESHOLD_MS = "3600000"; // 1 hour
    private final java.util.Timer cleanupTimer;

    public HelperPerProjectOTELHelperFactory(
        @NotNull ProjectManager projectManager,
        @NotNull OTELEndpointFactory otelEndpointFactory
    ) {
        this.projectManager = projectManager;
        this.otelEndpointFactory = otelEndpointFactory;

        this.otelHelpers = new ConcurrentHashMap<>();

        // Set up periodic cleanup task
        var timerDelay = Long.parseLong(TeamCityProperties.getProperty("teamcity.otel.helper.cleanup.timer.delay.ms", DEFAULT_HELPER_EVICTION_PERIOD_MS));
        this.cleanupTimer = new java.util.Timer("OTELHelperCleanupTimer", true);
        this.cleanupTimer.scheduleAtFixedRate(new java.util.TimerTask() {
            @Override
            public void run() {
                evictInactiveHelpers();
            }
        }, timerDelay, timerDelay);
    }

    public OTELHelper getOTELHelper(BuildPromotion buildPromotion) {
        var projectId = buildPromotion.getProjectId();

        return otelHelpers.computeIfAbsent(projectId, key -> {
            LOG.debug(String.format("Creating OTELHelper for project '%s'.", projectId));
            var project = projectManager.findProjectById(projectId);

            assert project != null;
            var features = project.getAvailableFeaturesOfType(PLUGIN_NAME);
            if (!features.isEmpty()) {
                var feature = features.stream().findFirst().get();
                var params = feature.getParameters();
                if (params.get(PROPERTY_KEY_ENABLED).equals("true")) {
                    var endpoint = params.get(PROPERTY_KEY_ENDPOINT);

                    var otelHandler = otelEndpointFactory.getOTELEndpointHandler(params.get(PROPERTY_KEY_SERVICE));
                    var spanProcessorMeterProviderPair = otelHandler.buildSpanProcessorAndMeterProvider(buildPromotion, endpoint, params);

                    long startTime = System.nanoTime();
                    var spanProcessor = spanProcessorMeterProviderPair.getLeft();
                    var meterProvider = spanProcessorMeterProviderPair.getRight();
                    var otelHelper = new OTELHelperImpl(spanProcessor, meterProvider, String.valueOf(projectId));
                    long endTime = System.nanoTime();

                    long duration = (endTime - startTime);
                    LOG.debug(String.format("Created OTELHelper for project '%s' in %d milliseconds.", projectId, duration / 1000000));

                    return otelHelper;
                }
            }
            LOG.debug(String.format("Using NullOTELHelper for project '%s'.", projectId));
            return new NullOTELHelperImpl();
        });
    }

    @Override
    public void settingsUpdated(SProject project) {
        var helper = otelHelpers.get(project.getProjectId());
        if (helper != null) {
            helper.release(project.getProjectId());
            otelHelpers.remove(project.getProjectId());
        }
    }

    private void evictInactiveHelpers() {
        long currentTime = System.currentTimeMillis();
        LOG.debug("Running periodic cleanup of inactive OTEL helpers");
        var cacheDuration = Long.parseLong(TeamCityProperties.getProperty("teamcity.otel.helper.cache.duration.ms", DEFAULT_HELPER_INACTIVE_THRESHOLD_MS));

        otelHelpers.forEach((projectId, helper) -> {
            Date lastUsed = helper.getLastUsed();
            long timeSinceLastUse = currentTime - lastUsed.getTime();

            if (timeSinceLastUse > cacheDuration) {
                LOG.debug(String.format("Evicting inactive OTELHelper for project '%s' (inactive for %d minutes)",
                        projectId, timeSinceLastUse / 60000));
                helper.release(projectId);
                otelHelpers.remove(projectId);
            }
        });
    }
}
