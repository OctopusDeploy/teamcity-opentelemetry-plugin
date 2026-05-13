'use strict';

registerPlugin(BS.ProjectConfigurationSettingsZipkin);

BS.ProjectConfigurationSettingsZipkin = OO.extend(BS.PluginPropertiesForm, OO.extend(BS.AbstractPasswordForm, {

    serviceType: "zipkin.io",

    show: function() {
        $j('#endpoint').closest('tr').show();
    },

    hide: function() {
        $j('#endpoint').closest('tr').hide();
    },
}));
