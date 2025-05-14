package com.octopus.teamcity.opentelemetry.server;

import jetbrains.buildServer.serverSide.IOGuard;
import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.SRunningBuild;
import org.apache.log4j.Logger;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class BuildStorageManagerImpl implements BuildStorageManager {
    static Logger LOG = Logger.getLogger(BuildStorageManagerImpl.class.getName());
    public static final String OTEL_TRACE_ID_FILENAME = "otel-trace-id";
    static final String OTEL_TRACE_SPAN_ID_SEPARATOR = ",";

    @Override
    @Nullable
    public String getTraceId(SBuild build) {
        var traceId = getData(build);
        if (traceId != null && traceId.contains(OTEL_TRACE_SPAN_ID_SEPARATOR)) {
            traceId = traceId.split(OTEL_TRACE_SPAN_ID_SEPARATOR)[0];
        }
        return traceId;
    }

    @Nullable
    private static String getData(SBuild build) {
        File artifactsDir = build.getArtifactsDirectory();
        File pluginFile = new File(artifactsDir, jetbrains.buildServer.ArtifactsConstants.TEAMCITY_ARTIFACTS_DIR + File.separatorChar + OTEL_TRACE_ID_FILENAME);

        LOG.debug(String.format("Reading trace & span id for build %d.", build.getBuildId()));

        if (!pluginFile.exists()) {
            LOG.info(String.format("Unable to find build artifact %s for build %d.", OTEL_TRACE_ID_FILENAME, build.getBuildId()));
            return null;
        }

        String traceId;
        try (Scanner fileReader = new Scanner(pluginFile)) {
            traceId = fileReader.nextLine();

            LOG.debug(String.format("Retrieved trace/span data '%s' for build %d.", traceId, build.getBuildId()));
        } catch (FileNotFoundException e) {
            LOG.warn(String.format("Unable to find trace/span data file for build %d.", build.getBuildId()));
            return null;
        }
        return traceId;
    }

    @Nullable
    @Override
    public String getSpanId(SBuild build) {
        var spanId = getData(build);
        if (spanId != null && spanId.contains(OTEL_TRACE_SPAN_ID_SEPARATOR)) {
            spanId = spanId.split(OTEL_TRACE_SPAN_ID_SEPARATOR)[1];
        }
        return spanId;
    }

    @Override
    public void saveTraceId(SRunningBuild build, String traceId, String spanId) {
        IOGuard.allowDiskWrite(() -> {
            File artifactsDir = build.getArtifactsDirectory();
            File pluginFile = new File(artifactsDir, jetbrains.buildServer.ArtifactsConstants.TEAMCITY_ARTIFACTS_DIR + File.separatorChar + OTEL_TRACE_ID_FILENAME);
            LOG.debug(String.format("Saving trace id %s to %s for build %d.", traceId, OTEL_TRACE_ID_FILENAME, build.getBuildId()));
            try (FileWriter fileWriter = new FileWriter(pluginFile)) {
                fileWriter.write(traceId);
                fileWriter.write(OTEL_TRACE_SPAN_ID_SEPARATOR);
                fileWriter.write(spanId);
            } catch (IOException e) {
                LOG.warn(String.format("Error trying to save trace id for build %d.", build.getBuildId()));
            }
        });
    }
}
