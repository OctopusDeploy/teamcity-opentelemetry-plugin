package com.octopus.teamcity.opentelemetry.server.endpoints;

import jetbrains.buildServer.serverSide.TeamCityNodes;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class OTELEndpointFactoryTest {

    // Resources are packaged under this directory, and handlers declare paths relative to it.
    private static final String RESOURCE_ROOT = "buildServerResources/";

    @Mock private PluginDescriptor pluginDescriptor;
    @Mock private TeamCityNodes teamCityNodes;

    private OTELEndpointFactory factory;

    @BeforeEach
    void setUp() {
        this.factory = new OTELEndpointFactory(pluginDescriptor, teamCityNodes);
    }

    @ParameterizedTest
    @EnumSource(OTELService.class)
    void everyServiceResolvesToAHandlerThatClaimsIt(OTELService service) {
        var handler = factory.getOTELEndpointHandler(service);

        assertNotNull(handler);
        assertEquals(service.getValue(), handler.getServiceName());
    }

    @ParameterizedTest
    @EnumSource(OTELService.class)
    void aServiceCanBeResolvedByItsName(OTELService service) {
        var handler = factory.getOTELEndpointHandler(service.getValue());

        assertEquals(service.getValue(), handler.getServiceName());
    }

    @Test
    void anUnknownServiceNameIsRejected() {
        assertThrows(RuntimeException.class, () -> factory.getOTELEndpointHandler("not.a.service"));
    }

    @Test
    void handlerServiceNamesAreUnique() {
        var names = factory.getOTELEndpointHandlers().stream()
                .map(IOTELEndpointHandler::getServiceName)
                .collect(Collectors.toList());

        assertEquals(names.size(), Set.copyOf(names).size(), "Duplicate service names in " + names);
    }

    @Test
    void handlersCoverEveryService() {
        var handled = factory.getOTELEndpointHandlers().stream()
                .map(IOTELEndpointHandler::getServiceName)
                .collect(Collectors.toSet());
        var expected = Stream.of(OTELService.values())
                .map(OTELService::getValue)
                .collect(Collectors.toSet());

        assertEquals(expected, handled);
    }

    @Test
    void everyDeclaredResourcePathExists() {
        var missing = new ArrayList<String>();
        for (var handler : factory.getOTELEndpointHandlers()) {
            var declared = new ArrayList<>(handler.getJsPaths());
            declared.addAll(handler.getCssPaths());
            declared.add(handler.getJspPath());

            declared.stream()
                    .filter(path -> getClass().getClassLoader().getResource(RESOURCE_ROOT + path) == null)
                    .forEach(path -> missing.add(handler.getServiceName() + " -> " + path));
        }

        assertEquals(List.of(), missing, "Handlers declare resources that are not on the classpath");
    }
}
