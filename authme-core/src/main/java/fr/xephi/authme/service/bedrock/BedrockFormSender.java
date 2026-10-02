package fr.xephi.authme.service.bedrock;

import fr.xephi.authme.platform.DialogInputSpec;
import fr.xephi.authme.platform.DialogWindowSpec;
import org.geysermc.cumulus.form.CustomForm;
import org.geysermc.cumulus.response.CustomFormResponse;
import org.geysermc.floodgate.api.FloodgateApi;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Builds native Bedrock forms (Cumulus) from AuthMe dialog specs and sends them through Floodgate.
 * Isolates all references to the Cumulus/Floodgate API so they are only loaded when Floodgate is installed.
 */
final class BedrockFormSender {

    private BedrockFormSender() {
    }

    /**
     * Sends a custom form with one text input per dialog input to the given Bedrock player.
     *
     * @param uuid the player's UUID
     * @param spec the dialog spec to render
     * @param onSubmit called with the submitted values, keyed by input id
     * @param onClose called when the player closes the form without submitting
     * @return true if the form was sent
     */
    static boolean send(UUID uuid, DialogWindowSpec spec, Consumer<Map<String, String>> onSubmit,
                        Runnable onClose) {
        FloodgateApi api = FloodgateApi.getInstance();
        if (api == null) {
            return false;
        }
        CustomForm.Builder builder = CustomForm.builder().title(spec.title());
        if (spec.body() != null && !spec.body().isEmpty()) {
            builder.label(spec.body());
        }
        for (DialogInputSpec input : spec.inputs()) {
            builder.input(input.label());
        }
        builder.validResultHandler(response -> onSubmit.accept(readInputs(spec, response)));
        builder.closedOrInvalidResultHandler(onClose);
        return api.sendForm(uuid, builder.build());
    }

    private static Map<String, String> readInputs(DialogWindowSpec spec, CustomFormResponse response) {
        response.includeLabels(false);
        Map<String, String> values = new LinkedHashMap<>();
        for (DialogInputSpec input : spec.inputs()) {
            Object value = response.hasNext() ? response.next() : null;
            values.put(input.id(), value == null ? "" : value.toString().trim());
        }
        return values;
    }
}
