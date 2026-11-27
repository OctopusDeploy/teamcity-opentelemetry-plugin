package com.octopus.teamcity.opentelemetry.server.endpoints.honeycomb;

import jetbrains.buildServer.controllers.ActionErrors;
import jetbrains.buildServer.serverSide.crypt.EncryptUtil;
import jetbrains.buildServer.serverSide.crypt.RSACipher;
import org.junit.jupiter.api.Test;

import javax.servlet.http.HttpServletRequest;

import java.util.ArrayList;
import java.util.HashMap;

import static com.octopus.teamcity.opentelemetry.common.PluginConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class SetHoneycombProjectConfigurationSettingsRequestTest {

    private final HttpServletRequest mockRequest;

    SetHoneycombProjectConfigurationSettingsRequestTest() {
        mockRequest = mock(HttpServletRequest.class, RETURNS_DEEP_STUBS);
        setupDefaultInputs();
    }

    @Test
    void teamIsRequired() {
        when(mockRequest.getParameter("honeycombTeam")).thenReturn(null);
        verifyValidationFails("honeycombTeam", "Team must be set!");
    }

    @Test
    void apiKeyIsRequired() {
        when(mockRequest.getParameter("encryptedHoneycombApiKey")).thenReturn(null);
        verifyValidationFails("honeycombApiKey", "ApiKey must be set!");
    }

    @Test
    void honeycombModeIsRequired() {
        when(mockRequest.getParameter("honeycombMode")).thenReturn(null);
        verifyValidationFails("honeycombMode", "Mode must be set to one of classic or environments!");
    }

    @Test
    void whenModeIsEnvironmentsThenEnvironmentIsRequired() {
        when(mockRequest.getParameter("honeycombEnvironment")).thenReturn(null);
        verifyValidationFails("honeycombEnvironment", "Environment must be set!");
    }

    @Test
    void whenModeIsEnvironmentsAndMetricsEnabledThenDataSetIsRequired() {
        //note: "honeycombMetricsEnabled" defaults to false
        when(mockRequest.getParameter("honeycombMetricsEnabled")).thenReturn("true");
        when(mockRequest.getParameter("honeycombDataset")).thenReturn(null);
        verifyValidationFails("honeycombDataset", "Dataset must be set to send metrics!");
    }

    @Test
    void whenModeIsEnvironmentsAndMetricsEnabledThenDataSetProvidedIsOkay() {
        //note: "honeycombMetricsEnabled" defaults to false
        when(mockRequest.getParameter("honeycombMetricsEnabled")).thenReturn("true");
        verifyValidationPasses();
    }

    @Test
    void whenModeIsEnvironmentsAndMetricsDisabledThenDataSetIsNotRequired() {
        when(mockRequest.getParameter("honeycombMetricsEnabled")).thenReturn("false");
        verifyValidationPasses();
    }

    @Test
    void whenModeIsEnvironmentsAndMetricsParamMissingThenDataSetIsNotRequired() {
        when(mockRequest.getParameter("honeycombMetricsEnabled")).thenReturn(null);
        verifyValidationPasses();
    }

    @Test
    void whenModeIsClassicThenDataSetIsRequired() {
        when(mockRequest.getParameter("honeycombMode")).thenReturn("classic");
        when(mockRequest.getParameter("honeycombDataset")).thenReturn(null);
        verifyValidationFails("honeycombDataset", "In classic mode, dataset must be set!");
    }

    @Test
    void whenModeIsClassicAndDataSetIsProvided() {
        when(mockRequest.getParameter("honeycombMode")).thenReturn("classic");
        verifyValidationPasses();
    }

    private void setupDefaultInputs() {
        when(mockRequest.getParameter("mode")).thenReturn("save");
        when(mockRequest.getParameter("service")).thenReturn("honeycomb.io");
        when(mockRequest.getParameter("endpoint")).thenReturn("https://honeycomb.example.com");
        when(mockRequest.getParameter("honeycombMode")).thenReturn("environments");
        when(mockRequest.getParameter("honeycombTeam")).thenReturn("MyTeam");
        when(mockRequest.getParameter("honeycombDataset")).thenReturn("MyDataset");
        when(mockRequest.getParameter("honeycombEnvironment")).thenReturn("MyEnvironment");
        when(mockRequest.getParameter("encryptedHoneycombApiKey")).thenReturn(RSACipher.encryptDataForWeb("myApiKey"));
        when(mockRequest.getParameter("honeycombMetricsEnabled")).thenReturn("false");
    }

    private void verifyValidationFails(String id, String expected) {
        var result = new SetHoneycombProjectConfigurationSettingsRequest(mockRequest);
        var errors = new ActionErrors();
        result.validate(errors);
        assertFalse(errors.hasNoErrors());
        assertEquals(1, errors.getErrors().size());
        assertEquals(id, errors.getErrors().get(0).getId());
        assertEquals(expected, errors.getErrors().get(0).getMessage());
    }

    private void verifyValidationPasses() {
        var result = new SetHoneycombProjectConfigurationSettingsRequest(mockRequest);
        var errors = new ActionErrors();
        result.validate(errors);
        assertTrue(errors.hasNoErrors());
    }

    @Test
    void mapsServiceSpecificParams() {
        var result = new HashMap<String, String>();
        var request = new SetHoneycombProjectConfigurationSettingsRequest(mockRequest);
        request.mapServiceSpecificParams(result, new ArrayList<>());

        assertEquals(HoneycombMode.ENVIRONMENTS.getValue(), result.get(PROPERTY_KEY_HONEYCOMB_MODE));
        assertEquals("MyTeam", result.get(PROPERTY_KEY_HONEYCOMB_TEAM));
        assertEquals("MyDataset", result.get(PROPERTY_KEY_HONEYCOMB_DATASET));
        assertEquals("MyEnvironment", result.get(PROPERTY_KEY_HONEYCOMB_ENVIRONMENT));
        assertEquals("myApiKey", EncryptUtil.unscramble(result.get(PROPERTY_KEY_HONEYCOMB_APIKEY)));
        assertEquals("false", result.get(PROPERTY_KEY_HONEYCOMB_METRICS_ENABLED));
    }

    @Test
    void whenModeIsNotPresentThenModeDefaultsToClassic() {
        var result = new HashMap<String, String>();
        when(mockRequest.getParameter("honeycombMode")).thenReturn(null);
        var request = new SetHoneycombProjectConfigurationSettingsRequest(mockRequest);
        request.mapServiceSpecificParams(result, new ArrayList<>());

        assertEquals(HoneycombMode.CLASSIC.getValue(), result.get(PROPERTY_KEY_HONEYCOMB_MODE));
        assertEquals("MyTeam", result.get(PROPERTY_KEY_HONEYCOMB_TEAM));
        assertEquals("MyDataset", result.get(PROPERTY_KEY_HONEYCOMB_DATASET));
        assertEquals("MyEnvironment", result.get(PROPERTY_KEY_HONEYCOMB_ENVIRONMENT));
        assertEquals("myApiKey", EncryptUtil.unscramble(result.get(PROPERTY_KEY_HONEYCOMB_APIKEY)));
        assertEquals("false", result.get(PROPERTY_KEY_HONEYCOMB_METRICS_ENABLED));
    }
}
