package fr.xephi.authme.service.bedrock;

import fr.xephi.authme.process.register.RegisterSecondaryArgument;
import fr.xephi.authme.process.register.RegistrationType;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 * Test for {@link BedrockFormService}.
 */
class BedrockFormServiceTest {

    @Test
    void shouldCreateRegisterTemplates() {
        assertThat(BedrockFormService.createRegisterTemplate(RegistrationType.PASSWORD, RegisterSecondaryArgument.NONE),
            equalTo("register $(password)"));
        assertThat(BedrockFormService.createRegisterTemplate(RegistrationType.PASSWORD,
            RegisterSecondaryArgument.CONFIRMATION), equalTo("register $(password) $(confirm)"));
        assertThat(BedrockFormService.createRegisterTemplate(RegistrationType.PASSWORD,
            RegisterSecondaryArgument.EMAIL_MANDATORY), equalTo("register $(password) $(email)"));
        assertThat(BedrockFormService.createRegisterTemplate(RegistrationType.EMAIL,
            RegisterSecondaryArgument.CONFIRMATION), equalTo("register $(email) $(confirm)"));
        assertThat(BedrockFormService.createRegisterTemplate(RegistrationType.EMAIL, RegisterSecondaryArgument.NONE),
            equalTo("register $(email)"));
    }

    @Test
    void shouldFillTemplate() {
        String command = BedrockFormService.fillTemplate("register $(password) $(confirm)",
            Map.of("password", "secret", "confirm", "secret"));
        assertThat(command, equalTo("register secret secret"));
    }
}
