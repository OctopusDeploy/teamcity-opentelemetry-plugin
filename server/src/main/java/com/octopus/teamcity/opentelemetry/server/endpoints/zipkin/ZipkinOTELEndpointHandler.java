package com.octopus.teamcity.opentelemetry.server.endpoints.zipkin;

import com.octopus.teamcity.opentelemetry.server.SetProjectConfigurationSettingsRequest;
import com.octopus.teamcity.opentelemetry.server.endpoints.IOTELEndpointHandler;
import io.opentelemetry.exporter.zipkin.ZipkinSpanExporter;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.trace.SpanProcessor;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import jetbrains.buildServer.serverSide.BuildPromotion;
import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

import static com.octopus.teamcity.opentelemetry.common.PluginConstants.*;
import static com.octopus.teamcity.opentelemetry.server.endpoints.OTELService.HONEYCOMB;
import static com.octopus.teamcity.opentelemetry.server.endpoints.OTELService.ZIPKIN;

public class ZipkinOTELEndpointHandler implements IOTELEndpointHandler {
    private final PluginDescriptor pluginDescriptor;

    public ZipkinOTELEndpointHandler(
            PluginDescriptor pluginDescriptor) {
        this.pluginDescriptor = pluginDescriptor;
    }

    @NotNull
    public ModelAndView getBuildOverviewModelAndView(SBuild build, Map<String, String> params, String traceId) {
        final ModelAndView mv = new ModelAndView(pluginDescriptor.getPluginResourcesPath("zipkin/buildOverviewZipkinExtension.jsp"));

        var model = mv.getModel();
        model.put("traceId", traceId);
        model.put("endpoint", params.get(PROPERTY_KEY_ENDPOINT).replaceAll("/$", ""));
        return mv;
    }

    @Override
    public Pair<SpanProcessor, SdkMeterProvider> buildSpanProcessorAndMeterProvider(BuildPromotion buildPromotion, String endpoint, Map<String, String> params) {
        return Pair.of(buildZipkinSpanProcessor(endpoint), null);
    }

    private SpanProcessor buildZipkinSpanProcessor(String exporterEndpoint) {
        String endpoint = String.format("%s/api/v2/spans", exporterEndpoint);
        ZipkinSpanExporter zipkinExporter = ZipkinSpanExporter.builder()
                .setEndpoint(endpoint)
                .build();

        return BatchSpanProcessor.builder(zipkinExporter)
                .setMaxQueueSize(BATCH_SPAN_PROCESSOR_MAX_QUEUE_SIZE)
                .setScheduleDelay(BATCH_SPAN_PROCESSOR_MAX_SCHEDULE_DELAY)
                .setMaxExportBatchSize(BATCH_SPAN_PROCESSOR_MAX_EXPORT_BATCH_SIZE)
                .build();
    }

    @Override
    public SetProjectConfigurationSettingsRequest getSetProjectConfigurationSettingsRequest(HttpServletRequest request) {
        return new SetZipkinProjectConfigurationSettingsRequest(request);
    }

    @Override
    public void mapParamsToModel(Map<String, String> params, Map<String, Object> model) {
        model.put("otelEndpoint", params.get(PROPERTY_KEY_ENDPOINT));
    }

    @Override
    public String getServiceName() {
        return ZIPKIN.getValue();
    }

    @Override
    public List<String> getJsPaths() {
        return List.of("zipkin/projectConfigurationSettingsZipkin.js");
    }

    @Override
    public List<String> getCssPaths() {
        return List.of();
    }

    @Override
    public String getJspPath() {
        return "zipkin/projectConfigurationSettingsZipkin.jspf";
    }
}
