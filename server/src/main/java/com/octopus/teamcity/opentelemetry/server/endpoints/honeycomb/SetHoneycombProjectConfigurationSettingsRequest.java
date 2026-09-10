package com.octopus.teamcity.opentelemetry.server.endpoints.honeycomb;

import com.octopus.teamcity.opentelemetry.server.HeaderDto;
import com.octopus.teamcity.opentelemetry.server.SetProjectConfigurationSettingsRequest;
import jetbrains.buildServer.controllers.ActionErrors;
import jetbrains.buildServer.serverSide.crypt.EncryptUtil;
import jetbrains.buildServer.serverSide.crypt.RSACipher;
import jetbrains.buildServer.util.StringUtil;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.Optional;

import static com.octopus.teamcity.opentelemetry.common.PluginConstants.*;

public class SetHoneycombProjectConfigurationSettingsRequest extends SetProjectConfigurationSettingsRequest {
    public final String honeycombTeam;
    public final String honeycombDataset;
    public final String honeycombApiKey;
    public final String honeycombMetricsEnabled;
    private final Optional<HoneycombMode> honeycombMode;
    public final String honeycombEnvironment;

    public SetHoneycombProjectConfigurationSettingsRequest(HttpServletRequest request) {
        super(request);
        this.honeycombTeam = request.getParameter("honeycombTeam");
        this.honeycombDataset = request.getParameter("honeycombDataset");
        this.honeycombMode = HoneycombMode.get(request.getParameter("honeycombMode"));
        this.honeycombEnvironment = request.getParameter("honeycombEnvironment");
        this.honeycombMetricsEnabled = request.getParameter("honeycombMetricsEnabled");
        this.honeycombApiKey = RSACipher.decryptWebRequestData(request.getParameter("encryptedHoneycombApiKey"));
    }

    @Override
    protected void serviceSpecificValidate(ActionErrors errors) {
        if (StringUtil.isEmptyOrSpaces(honeycombTeam))
            errors.addError("honeycombTeam", "Team must be set!");
        if (StringUtil.isEmptyOrSpaces(honeycombApiKey))
            errors.addError("honeycombApiKey", "ApiKey must be set!");
        if (!this.honeycombMode.isPresent()) {
            errors.addError("honeycombMode", "Mode must be set to one of " + HoneycombMode.readableJoin() + "!");
        } else {
            if (honeycombMode.get().equals(HoneycombMode.ENVIRONMENTS)) {
                if (StringUtil.isEmptyOrSpaces(honeycombEnvironment))
                    errors.addError("honeycombEnvironment", "Environment must be set!");
                if (Objects.equals(this.honeycombMetricsEnabled, "true") && StringUtil.isEmptyOrSpaces(honeycombDataset))
                    errors.addError("honeycombDataset", "Dataset must be set to send metrics!");
            } else {
                if (StringUtil.isEmptyOrSpaces(honeycombDataset))
                    errors.addError("honeycombDataset", "In classic mode, dataset must be set!");
            }
        }
    }

    @Override
    protected void mapServiceSpecificParams(HashMap<String, String> params, ArrayList<HeaderDto> headers) {
        params.put(PROPERTY_KEY_HONEYCOMB_DATASET, honeycombDataset);
        params.put(PROPERTY_KEY_HONEYCOMB_TEAM, honeycombTeam);
        params.put(PROPERTY_KEY_HONEYCOMB_METRICS_ENABLED, honeycombMetricsEnabled);

        if (honeycombMode.isPresent())
            params.put(PROPERTY_KEY_HONEYCOMB_MODE, honeycombMode.get().getValue());
        else
            params.put(PROPERTY_KEY_HONEYCOMB_MODE, HoneycombMode.getDefault().getValue());

        params.put(PROPERTY_KEY_HONEYCOMB_ENVIRONMENT, honeycombEnvironment);
        params.put(PROPERTY_KEY_HONEYCOMB_APIKEY, EncryptUtil.scramble(honeycombApiKey));
    }
}
