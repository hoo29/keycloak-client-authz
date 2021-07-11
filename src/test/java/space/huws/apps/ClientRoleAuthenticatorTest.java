package space.huws.apps;

import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.events.EventBuilder;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.AuthenticatorConfigModel;
import org.keycloak.models.ClientModel;
import org.keycloak.models.RoleModel;
import org.keycloak.models.UserModel;
import org.keycloak.sessions.AuthenticationSessionModel;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ClientRoleAuthenticatorTest {

    @Mock
    private AuthenticationFlowContext context;

    @Mock
    private ClientModel client;

    @Mock
    private UserModel user;

    // silence log4j / jboss no config file complaints
    @BeforeAll
    public static void beforeAll() {
        Logger rootLogger = Logger.getRootLogger();
        rootLogger.setLevel(Level.OFF);
    }

    @BeforeEach
    public void before(@Mock AuthenticationSessionModel asm) {
        when(context.getAuthenticationSession()).thenReturn(asm);
        when(context.getUser()).thenReturn(user);

        when(asm.getClient()).thenReturn(client);
    }

    // separate failure and success tests so we only mock when necessary

    @Nested
    class ClientRoleAuthenticatorSuccessTests {
        @Test
        void defaultConfigNoClientRole() {
            when(client.getRole(anyString())).thenReturn(null);

            ClientRoleAuthenticator cra = new ClientRoleAuthenticator();
            cra.authenticate(context);

            verify(context, times(1)).success();
        }

        @Test
        void defaultConfigMemberClientRole(@Mock RoleModel role) {
            when(client.getRole(anyString())).thenReturn(role);
            when(user.hasRole(any(RoleModel.class))).thenReturn(true);

            ClientRoleAuthenticator cra = new ClientRoleAuthenticator();
            cra.authenticate(context);

            verify(context, times(1)).success();
        }

    }

    @Nested
    class ClientRoleAuthenticatorFailureTests {

        @BeforeEach
        public void mockErrorPath() {
            EventBuilder eb = mock(EventBuilder.class);
            LoginFormsProvider lfp = mock(LoginFormsProvider.class);

            when(context.getEvent()).thenReturn(eb);
            when(context.form()).thenReturn(lfp);
        }

        @Test
        void failSafeNoClientRole() {
            Map<String, String> config = new HashMap<>();
            config.put(ClientRoleAuthenticator.CONFIG_FAIL_OPEN_NAME, "false");
            config.put(ClientRoleAuthenticator.CONFIG_ROLE_NAME_NAME, "access");
            AuthenticatorConfigModel confModel = mock(AuthenticatorConfigModel.class);
            when(context.getAuthenticatorConfig()).thenReturn(confModel);
            when(confModel.getConfig()).thenReturn(config);

            when(client.getRole(anyString())).thenReturn(null);
            mockErrorPath();

            ClientRoleAuthenticator cra = new ClientRoleAuthenticator();
            cra.authenticate(context);

            verify(context, times(1)).failure(any(), any());
        }

        @Test
        void defaultConfigNotMemberClientRole(@Mock RoleModel role) {
            when(client.getRole(anyString())).thenReturn(role);
            when(user.hasRole(any(RoleModel.class))).thenReturn(false);
            mockErrorPath();

            ClientRoleAuthenticator cra = new ClientRoleAuthenticator();
            cra.authenticate(context);

            verify(context, times(1)).failure(any(), any());
        }
    }
}
