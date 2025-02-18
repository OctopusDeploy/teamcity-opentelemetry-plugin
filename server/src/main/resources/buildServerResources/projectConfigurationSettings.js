'use strict';

BS.ProjectConfigurationSettings = OO.extend(BS.PluginPropertiesForm, OO.extend(BS.AbstractPasswordForm, {
    formElement: function () {
        return $('editOpenTelemetrySettingsPage');
    },

    savingIndicator: function () {
        return $('saveProgress');
    },

    addHeader: function(button, index) {
        $j(button).closest('tr').before($j("<tr>\n" +
                                           "    <td>\n" +
                                           "        <input type=\"text\" name=\"headerKey_" + index + "\" value=\"\" class=\"textField\">\n" +
                                           "    </td>\n" +
                                           "    <td>\n" +
                                           "       <select name=\"headerType_" + index + "\" onchange=\"BS.ProjectConfigurationSettings.headerTypeChanged(this)\">\n" +
                                           "           <option value='plaintext'>Text</option>\n" +
                                           "           <option value='password'>Password</option>\n" +
                                           "       </select>\n" +
                                           "    </td>\n" +
                                           "    <td>\n" +
                                           "        <input type=\"text\" name=\"headerValue_" + index + "\" value=\"\" class=\"textField longField\">\n" +
                                           "    </td>\n" +
                                           "    <td>\n" +
                                           "        <a class=\"btn \" href=\"#\" onclick=\"BS.ProjectConfigurationSettings.removeHeader(this)\">Remove</a>\n" +
                                           "    </td>\n" +
                                           "</tr>"));
        $j(button).attr("onclick", "BS.ProjectConfigurationSettings.addHeader(this, " + (index + 1) + ")");
    },

    removeHeader: function(button) {
        $j(button).closest('tr').remove();
    },

    serviceChanged: function(dropdown) {
        //todo: this needs to move into the service-specific files
        if ($j(dropdown).val() === 'honeycomb.io') {
            $j('#endpoint').val('https://api.honeycomb.io:443');
            $j('#endpoint').closest('tr').hide();
            $j('#customHeaders').closest('tr').hide();
            $j('#honeycombTeam').closest('tr').show();
            $j('#honeycombApiKey').closest('tr').show();
            this.honeycombModeChanged($j('#honeycombMode'));
        } else if ($j(dropdown).val() === 'zipkin.io') {
            $j('#endpoint').closest('tr').show();
            $j('#customHeaders').closest('tr').hide();
            $j('#honeycombTeam').closest('tr').hide();
            $j('#honeycombDataset').closest('tr').hide();
            $j('#honeycombMode').closest('tr').hide();
            $j('#honeycombEnvironment').closest('tr').hide();
            $j('#honeycombApiKey').closest('tr').hide();
        } else {
            $j('#endpoint').closest('tr').show();
            $j('#customHeaders').closest('tr').show();
            $j('#honeycombTeam').closest('tr').hide();
            $j('#honeycombDataset').closest('tr').hide();
            $j('#honeycombMode').closest('tr').hide();
            $j('#honeycombEnvironment').closest('tr').hide();
            $j('#honeycombApiKey').closest('tr').hide();
        }
    },

    //todo: this needs to move into the honeycomb-specific file
    honeycombModeChanged: function(dropdown) {
        if ($j('#service').val() === 'honeycomb.io') {
            if ($j(dropdown).val() === 'environments') {
                if ($j('#honeycombMetricsEnabled').is(':checked'))
                    $j('#honeycombDataset').closest('tr').show();
                else
                    $j('#honeycombDataset').closest('tr').hide();
                $j('#honeycombEnvironment').closest('tr').show();
            } else {
                $j('#honeycombDataset').closest('tr').show();
                $j('#honeycombEnvironment').closest('tr').hide();
            }
        } else {
            $j('#honeycombDataset').closest('tr').hide();
            $j('#honeycombEnvironment').closest('tr').hide();
        }
    },

    //todo: this needs to move into the honeycomb-specific file
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

    headerTypeChanged: function(dropdown) {
        const valueField = $j(dropdown).closest('tr').find('input').last();
        if ($j(dropdown).val() === 'plaintext') {
            valueField.attr('type', 'text');
        } else {
            valueField.attr('type', 'password');
        }
    },

    postBackToServer: function (mode) {
        $j("input[name='mode']").val(mode);
        BS.FormSaver.save(this, "/admin/teamcity-opentelemetry/settings.html", OO.extend(BS.ErrorsAwareListener, {
            onCompleteSave: function (form, responseXML, err) {
                err = BS.XMLResponse.processErrors(responseXML, {}, BS.PluginPropertiesForm.propertiesErrorsHandler);
                form.setSaving(false);
                if (err) {
                    form.enable();
                    form.focusFirstErrorField();
                } else {
                    BS.reload(true);
                }
            }
        }));
        return false;
    },

    save: function () {
        return this.postBackToServer("save");
    },

    reset: function () {
        return this.postBackToServer("reset");
    }
}));
