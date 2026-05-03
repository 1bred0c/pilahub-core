package fpt.edu.sep490.pilahub.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for GHN (Giao Hàng Nhanh) shipping API.
 * <p>
 * Set these values via environment variables:
 * <pre>
 *   GHN_API_KEY    — shop-level token from GHN developer portal
 *   GHN_SHOP_ID    — integer shop ID from GHN developer portal
 *   GHN_CLIENT_ID  — client ID assigned by GHN when registering the webhook
 *   GHN_BASE_URL   — override to switch between sandbox and production
 * </pre>
 *
 * <h3>Webhook registration fields (GHN portal)</h3>
 * <ul>
 *   <li><b>Client ID</b>  — use the value of {@code GHN_CLIENT_ID} / {@code ghn.client-id}</li>
 *   <li><b>URL webhook</b> — {@code https://<your-domain>/api/ghn/webhook}</li>
 *   <li><b>Environment</b> — Staging (sandbox) or Production</li>
 *   <li><b>Name</b>        — any label, e.g. {@code pilahub-webhook}</li>
 * </ul>
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "ghn")
public class GhnProperties {

    /** Shop-level API token (required in {@code Token} header for all calls). */
    private String apiKey;

    /** GHN Shop ID (required in {@code ShopId} header for order operations). */
    private Integer shopId;

    /**
     * Client ID assigned by GHN when the webhook is registered.
     * GHN includes this value as {@code ClientId} in every webhook push;
     * the server rejects calls where this value does not match.
     */
    private String clientId;

    /**
     * Base URL of the GHN gateway.
     * Sandbox:    {@code https://dev-online-gateway.ghn.vn}
     * Production: {@code https://online-gateway.ghn.vn}
     */
    private String baseUrl = "https://dev-online-gateway.ghn.vn";
}
