'use strict';

const pluginRegistry = new Map();

function registerPlugin(implementation) {
    if (pluginRegistry.has(implementation.serviceType)) {
        console.warn(`Plugin "${implementation.serviceType}" is already registered.`);
        return;
    }
    pluginRegistry.set(implementation.serviceType, implementation);
    console.log(`Registered plugin: ${implementation.serviceType}`);
}

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
        pluginRegistry.forEach(plugin => {
            if ($j(dropdown).val() !== plugin.serviceType) plugin.hide();
        });
        pluginRegistry.forEach(plugin => {
            if ($j(dropdown).val() === plugin.serviceType) plugin.show();
        });
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
