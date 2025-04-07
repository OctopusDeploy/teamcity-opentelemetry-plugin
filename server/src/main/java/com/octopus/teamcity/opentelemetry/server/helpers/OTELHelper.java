package com.octopus.teamcity.opentelemetry.server.helpers;

import io.opentelemetry.api.trace.Span;

import javax.annotation.Nullable;
import java.util.Date;

public interface OTELHelper {
    boolean isReady();

    Date getLastUsed();

    Span getOrCreateParentSpan(String buildId);

    Span createSpan(String spanName, Span parentSpan, String parentSpanName);

    Span createTransientSpan(String spanName, Span parentSpan, long startTime);

    void removeSpan(String buildId);

    @Nullable
    Span getSpan(String buildId);

    void addAttributeToSpan(Span span, String attributeName, Object attributeValue);

    void release(String helperName);
}
