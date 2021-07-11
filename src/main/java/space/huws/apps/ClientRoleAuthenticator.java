package space.huws.apps;

import javax.ws.rs.core.Response;

import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.Authenticator;
import org.keycloak.events.Errors;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.AuthenticatorConfigModel;
import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RoleModel;
import org.keycloak.models.UserModel;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.services.messages.Messages;

public class ClientRoleAuthenticator implements Authenticator {

    public static final String CONFIG_FAIL_OPEN_NAME = "fail-open";
    public static final String CONFIG_ROLE_NAME_NAME = "client-role-name";

    @Override
    public void close() {
        // NOP
    }

    @Override
    public void authenticate(AuthenticationFlowContext context) {

        // authentication has already occurred with a previous provider, authorise here
        final boolean authorised = authorise(context);

        if (authorised) {
            context.success();
        } else {
            /**
             * If you don't provide a Response to context.failure, you'll eventually end up in
             * org.keycloak.authentication.AuthenticationProcessor#handleBrowserException(Exception). This doesn't have a
             * defined case for ACCESS_DENIED and will return the default INVALID_USER_CREDENTIALS error to the browser, which
             * might be confusing for users as their credentials are valid.
             * 
             * The audit event generated is also INVALID_USER_CREDENTIALS which is again not correct.
             * 
             * We create our own error page to return a more accurate error.
             */

            context.getEvent().error(Errors.ACCESS_DENIED);

            final LoginFormsProvider forms = context.form();
            forms.setError(Messages.ACCESS_DENIED);
            final Response errorResponse = forms.createErrorPage(Response.Status.FORBIDDEN);

            context.failure(AuthenticationFlowError.ACCESS_DENIED, errorResponse);
        }
    }

    @Override
    public void action(AuthenticationFlowContext context) {
        // never called as authenticate does not return a challenge
    }

    @Override
    public boolean requiresUser() {
        return true;
    }

    @Override
    public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
        return true;
    }

    @Override
    public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {
        // NOP
    }

    private boolean authorise(AuthenticationFlowContext context) {
        final ClientModel client = context.getAuthenticationSession().getClient();
        final UserModel user = context.getUser();

        // keycloak should throw an error before this code is invoked as requiresUser() is true
        if (user == null) {
            throw new ClientRoleAuthenticatorException("user was null during flow");
        }

        final String clientRoleName = safeGetConfigValue(CONFIG_ROLE_NAME_NAME, context);
        final boolean failOpen = safeGetConfigValue(CONFIG_FAIL_OPEN_NAME, context).equals("true");

        final RoleModel role = client.getRole(clientRoleName);
        return (role == null && failOpen) || user.hasRole(role);
    }

    private String safeGetConfigValue(String key, AuthenticationFlowContext context) {

        // this can be null if authenticator hasn't been configured yet. Defaults appear to be for the UI only.
        final AuthenticatorConfigModel confModel = context.getAuthenticatorConfig();

        String value = null;
        if (confModel == null) {
            // given there are so few elements and no class constructor to nicely convert it to a map this seems fine...
            for (final ProviderConfigProperty prop : ClientRoleAuthenticatorFactory.CONFIG_PROPERTIES) {
                if (prop.getName().equals(key)) {
                    value = prop.getDefaultValue() instanceof String ? (String) prop.getDefaultValue() : null;
                    break;
                }
            }
        } else {
            value = confModel.getConfig().get(key);
        }

        if (value == null) {
            throw new ClientRoleAuthenticatorException("default config missing value " + key);
        }

        return value;
    }

}
