'use strict';

registerPlugin(BS.ProjectConfigurationSettingsCustom);

BS.ProjectConfigurationSettingsCustom = OO.extend(BS.PluginPropertiesForm, OO.extend(BS.AbstractPasswordForm, {
    serviceType: "custom",

    show: function() {
        $j('#endpoint').closest('tr').show();
        $j('#customHeaders').closest('tr').show();
    },

    hide: function() {
        $j('#endpoint').closest('tr').hide();
        $j('#customHeaders').closest('tr').hide();
    },
}));
