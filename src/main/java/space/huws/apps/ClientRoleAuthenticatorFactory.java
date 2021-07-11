package space.huws.apps;

import java.util.ArrayList;
import java.util.List;

import org.keycloak.Config.Scope;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.models.AuthenticationExecutionModel.Requirement;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderConfigProperty;

public class ClientRoleAuthenticatorFactory implements AuthenticatorFactory {

    // Using static blocks and not constructors to match the rest of the keycloak codebase.

    public static final String PROVIDER_ID = "auth-client-role";

    protected static final List<ProviderConfigProperty> CONFIG_PROPERTIES = new ArrayList<>();

    private static final Authenticator SINGLETON = new ClientRoleAuthenticator();
    private static final AuthenticationExecutionModel.Requirement[] REQUIREMENTS = {
            AuthenticationExecutionModel.Requirement.REQUIRED, AuthenticationExecutionModel.Requirement.DISABLED };

    static {
        final String defaultFailOpenValue = "true";
        final ProviderConfigProperty failOpen = new ProviderConfigProperty();
        failOpen.setType(ProviderConfigProperty.BOOLEAN_TYPE);
        failOpen.setName(ClientRoleAuthenticator.CONFIG_FAIL_OPEN_NAME);
        failOpen.setLabel("Fail Open");
        failOpen.setHelpText("Fail open (allow access) if the named client role has not been created on the client");
        failOpen.setDefaultValue(defaultFailOpenValue);
        CONFIG_PROPERTIES.add(failOpen);

        final String defaultRoleNameValue = "access";
        final ProviderConfigProperty roleName = new ProviderConfigProperty();
        roleName.setType(ProviderConfigProperty.STRING_TYPE);
        roleName.setName(ClientRoleAuthenticator.CONFIG_ROLE_NAME_NAME);
        roleName.setLabel("Client Role Name");
        roleName.setHelpText("The name of the client role to use for the authz check.");
        roleName.setDefaultValue(defaultRoleNameValue);
        CONFIG_PROPERTIES.add(roleName);
    }

    @Override
    public Authenticator create(KeycloakSession session) {
        return SINGLETON;
    }

    @Override
    public void init(Scope config) {
        // NOP
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
        // NOP
    }

    @Override
    public void close() {
        // NOP
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public String getDisplayType() {
        return "Client Role";
    }

    @Override
    public String getReferenceCategory() {
        return null;
    }

    @Override
    public boolean isConfigurable() {
        return true;
    }

    @Override
    public Requirement[] getRequirementChoices() {
        return REQUIREMENTS;
    }

    @Override
    public boolean isUserSetupAllowed() {
        return false;
    }

    @Override
    public String getHelpText() {
        return "Require the user to be a member of specific client role.";
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return CONFIG_PROPERTIES;
    }

}
