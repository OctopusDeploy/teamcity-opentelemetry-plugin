package com.octopus.teamcity.opentelemetry.server;

import jetbrains.buildServer.serverSide.ParametersPreprocessor;
import jetbrains.buildServer.serverSide.SRunningBuild;
import org.apache.log4j.Logger;
import org.apache.logging.log4j.CloseableThreadContext;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class BuildParameterPreProcessor implements ParametersPreprocessor  {
    private static final Logger LOG = Logger.getLogger(BuildParameterPreProcessor.class.getName());

    @NotNull
    private final BuildStorageManager buildStorageManager;

    BuildParameterPreProcessor(@NotNull BuildStorageManager buildStorageManager) {
        this.buildStorageManager = buildStorageManager;
    }

    @Override
    public void fixRunBuildParameters(@NotNull SRunningBuild build, @NotNull Map<String, String> runParameters, @NotNull Map<String, String> buildParams) {
        try (var ignored = CloseableThreadContext.put("teamcity.build.id", String.valueOf(build.getBuildId()))) {
            LOG.debug(String.format("Looking up trace id for for build id %d, to add to the build parameters", build.getBuildId()));
            var traceId = buildStorageManager.getTraceId(build);

            if (traceId == null) {
                LOG.warn(String.format("Unable to get trace id for build id %d; we cant set build parameter 'TEAMCITY_OTEL_PLUGIN_TRACE_ID'", build.getBuildId()));
            } else {
                LOG.debug(String.format("Adding trace id '%s' to build parameters for build %d", traceId, build.getBuildId()));
                buildParams.put("env.TEAMCITY_OTEL_PLUGIN_TRACE_ID", traceId);
            }

            var spanId = buildStorageManager.getSpanId(build);

            if (spanId == null) {
                LOG.warn(String.format("Unable to get span id for build id %d; we cant set build parameter 'TEAMCITY_OTEL_PLUGIN_SPAN_ID'", build.getBuildId()));
            } else {
                LOG.debug(String.format("Adding span id '%s' to build parameters for build %d", spanId, build.getBuildId()));
                buildParams.put("env.TEAMCITY_OTEL_PLUGIN_SPAN_ID", spanId);
            }
        }
    }
}
