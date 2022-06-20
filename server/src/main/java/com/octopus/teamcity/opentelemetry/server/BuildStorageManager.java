package com.octopus.teamcity.opentelemetry.server;

import jetbrains.buildServer.log.Loggers;
import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.SRunningBuild;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class BuildStorageManager {

    public static final String OTEL_TRACE_ID = "otel-trace-id";

    @Nullable
    public String getTraceId(SBuild build) {
        File artifactsDir = build.getArtifactsDirectory();
        File pluginFile = new File(artifactsDir, jetbrains.buildServer.ArtifactsConstants.TEAMCITY_ARTIFACTS_DIR + File.separatorChar + OTEL_TRACE_ID);

        if (!pluginFile.exists())
            return null;

        String traceId;
        try {
            var fileReader = new Scanner(pluginFile);
            traceId = fileReader.nextLine();
            fileReader.close();
        } catch (FileNotFoundException e) {
            Loggers.SERVER.warn(String.format("OTEL_PLUGIN: Unable to find trace id data file for build %d.", build.getBuildId()));
            return null;
        }
        return traceId;
    }

    public void saveTraceId(SRunningBuild build, String traceId) {
        File artifactsDir = build.getArtifactsDirectory();
        File pluginFile = new File(artifactsDir, jetbrains.buildServer.ArtifactsConstants.TEAMCITY_ARTIFACTS_DIR + File.separatorChar + OTEL_TRACE_ID);
        try {
            if (pluginFile.createNewFile()){
                var fileWriter = new FileWriter(pluginFile);
                fileWriter.write(traceId);
                fileWriter.close();
            }
        } catch (IOException e) {
            Loggers.SERVER.warn(String.format("OTEL_PLUGIN: Error trying to save trace id for build %d.", build.getBuildId()));
        }
    }
}
