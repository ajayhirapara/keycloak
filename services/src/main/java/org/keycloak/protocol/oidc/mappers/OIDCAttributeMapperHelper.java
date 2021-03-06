package org.keycloak.protocol.oidc.mappers;

import org.keycloak.models.ProtocolMapperModel;
import org.keycloak.models.RealmModel;
import org.keycloak.protocol.ProtocolMapperUtils;
import org.keycloak.protocol.oidc.OIDCLoginProtocol;
import org.keycloak.representations.AccessToken;

import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class OIDCAttributeMapperHelper {
    public static final String TOKEN_CLAIM_NAME = "Token Claim Name";
    public static final String JSON_TYPE = "Claim JSON Type";

    public static Object mapAttributeValue(ProtocolMapperModel mappingModel, Object attributeValue) {
        if (attributeValue == null) return null;
        String type = mappingModel.getConfig().get(JSON_TYPE);
        if (type == null) return attributeValue;
        if (type.equals("boolean")) {
            if (attributeValue instanceof Boolean) return attributeValue;
            if (attributeValue instanceof String) return Boolean.valueOf((String)attributeValue);
            throw new RuntimeException("cannot map type for token claim");
        } else if (type.equals("String")) {
            if (attributeValue instanceof String) return attributeValue;
            return attributeValue.toString();
        } else if (type.equals("long")) {
            if (attributeValue instanceof Long) return attributeValue;
            if (attributeValue instanceof String) return Long.valueOf((String)attributeValue);
            throw new RuntimeException("cannot map type for token claim");
        } else if (type.equals("int")) {
            if (attributeValue instanceof Integer) return attributeValue;
            if (attributeValue instanceof String) return Integer.valueOf((String)attributeValue);
            throw new RuntimeException("cannot map type for token claim");
        }
        return attributeValue;
    }

    public static void mapClaim(AccessToken token, ProtocolMapperModel mappingModel, Object attributeValue) {
        if (attributeValue == null) return;
        attributeValue = mapAttributeValue(mappingModel, attributeValue);
        String protocolClaim = mappingModel.getConfig().get(TOKEN_CLAIM_NAME);
        String[] split = protocolClaim.split("\\.");
        Map<String, Object> jsonObject = token.getOtherClaims();
        for (int i = 0; i < split.length; i++) {
            if (i == split.length - 1) {
                jsonObject.put(split[i], attributeValue);
            } else {
                Map<String, Object> nested = (Map<String, Object>)jsonObject.get(split[i]);
                if (nested == null) {
                    nested = new HashMap<String, Object>();
                    jsonObject.put(split[i], nested);
                    jsonObject = nested;
                }
            }
        }
    }

    public static void addClaimMapper(RealmModel realm, String name,
                                  String userAttribute,
                                  String tokenClaimName, String claimType,
                                  boolean consentRequired, String consentText,
                                  boolean appliedByDefault,
                                  String mapperId) {
        ProtocolMapperModel mapper = realm.getProtocolMapperByName(OIDCLoginProtocol.LOGIN_PROTOCOL, name);
        if (mapper != null) return;
        mapper = new ProtocolMapperModel();
        mapper.setName(name);
        mapper.setProtocolMapper(mapperId);
        mapper.setProtocol(OIDCLoginProtocol.LOGIN_PROTOCOL);
        mapper.setConsentRequired(consentRequired);
        mapper.setConsentText(consentText);
        mapper.setAppliedByDefault(appliedByDefault);
        Map<String, String> config = new HashMap<String, String>();
        config.put(ProtocolMapperUtils.USER_ATTRIBUTE, userAttribute);
        config.put(TOKEN_CLAIM_NAME, tokenClaimName);
        config.put(JSON_TYPE, claimType);
        mapper.setConfig(config);
        realm.addProtocolMapper(mapper);
    }
}
