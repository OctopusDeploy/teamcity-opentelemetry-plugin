package com.octopus.teamcity.opentelemetry.server.helpers;

import jetbrains.buildServer.serverSide.BuildPromotion;
import jetbrains.buildServer.serverSide.SProject;

public interface OTELHelperFactory {
    OTELHelper getOTELHelper(BuildPromotion build);

    void settingsUpdated(SProject project);
}
