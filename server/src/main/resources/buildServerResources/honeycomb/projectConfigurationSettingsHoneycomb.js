'use strict';

BS.ProjectConfigurationSettingsHoneycomb = OO.extend(BS.PluginPropertiesForm, OO.extend(BS.AbstractPasswordForm, {
    serviceType: "honeycomb.io",

    honeycombModeChanged: function(dropdown) {
        if ($j('#service').val() === 'honeycomb.io') {
            if ($j(dropdown).val() === 'environments') {
                if ($j('#honeycombMetricsEnabled').is(':checked'))
                    $j('#honeycombDataset').closest('tr').show();
                else
                    $j('#honeycombDataset').closest('tr').hide();
            } else {
                $j('#honeycombDataset').closest('tr').show();
            }
        } else {
            $j('#honeycombDataset').closest('tr').hide();
        }
    },

    honeycombMetricsEnabledChanged: function(dropdown) {
        if ($j('#service').val() === 'honeycomb.io') {
            if ($j('#honeycombMetricsEnabled').is(':checked'))
                $j('#honeycombDataset').closest('tr').show();
            else
                $j('#honeycombDataset').closest('tr').hide();
        } else {
            $j('#honeycombDataset').closest('tr').hide();
        }
    },

    show: function() {
        $j('#endpoint').val('https://api.honeycomb.io:443');
        $j('#honeycombTeam').closest('tr').show();
        $j('#honeycombApiKey').closest('tr').show();
        $j('#honeycombMode').closest('tr').show();
        this.honeycombModeChanged($j('#honeycombMode'));
    },

    hide: function() {
        $j('#honeycombTeam').closest('tr').hide();
        $j('#honeycombApiKey').closest('tr').hide();
        $j('#honeycombDataset').closest('tr').hide();
        $j('#honeycombMode').closest('tr').hide();
    },
}));

registerPlugin(BS.ProjectConfigurationSettingsHoneycomb);
