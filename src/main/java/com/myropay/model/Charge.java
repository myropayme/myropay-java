package com.myropay.model;

import com.myropay.exception.InvalidRequestException;
import com.myropay.exception.MyropayException;
import com.myropay.net.ApiRequestor;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A charge — a MyroPay hosted checkout session — for a customer to pay.
 *
 * <pre>{@code
 * Myropay.apiKey = "sk_test_...";
 *
 * Map<String, Object> params = new HashMap<>();
 * params.put("email", "customer@email.com");
 * params.put("amount", 500000); // whole currency units — ₦500,000, not kobo
 *
 * Charge charge = Charge.create(params);
 * response.sendRedirect(charge.getCheckoutUrl());
 * }</pre>
 */
public final class Charge {
    private final Map<String, Object> data;

    private Charge(Map<String, Object> data) {
        this.data = data;
    }

    /**
     * Create a charge.
     *
     * Recognized keys in {@code params}: {@code email} (required),
     * {@code amount} (required, whole currency units — e.g. 500000 for
     * ₦500,000, not kobo), {@code currency} (defaults to "NGN"),
     * {@code reference}, {@code description}, {@code successUrl},
     * {@code cancelUrl}, {@code metadata} (a Map), {@code name},
     * {@code apiKey} (overrides {@link com.myropay.Myropay#apiKey} for
     * this call only).
     */
    @SuppressWarnings("unchecked")
    public static Charge create(Map<String, Object> params) throws MyropayException {
        Object amount = params.get("amount");
        Object email = params.get("email");
        if (amount == null) throw new InvalidRequestException("amount is required");
        if (email == null || email.toString().isEmpty()) throw new InvalidRequestException("email is required");

        Map<String, Object> customer = new LinkedHashMap<>();
        customer.put("email", email);
        customer.put("name", params.get("name"));

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("amount", amount);
        body.put("currency", params.getOrDefault("currency", "NGN"));
        body.put("order_ref", params.containsKey("reference") ? params.get("reference") : params.get("order_ref"));
        body.put("description", params.get("description"));
        body.put("customer", customer);
        body.put("success_url", params.containsKey("successUrl") ? params.get("successUrl") : params.get("success_url"));
        body.put("cancel_url", params.containsKey("cancelUrl") ? params.get("cancelUrl") : params.get("cancel_url"));
        body.put("metadata", params.get("metadata"));

        String apiKeyOverride = (String) params.get("apiKey");
        Map<String, String> query = new HashMap<>();
        query.put("action", "create");

        Map<String, Object> res = ApiRequestor.request("POST", query, body, apiKeyOverride);
        return new Charge(res);
    }

    /** Retrieve a charge by id (the session_id returned from create()). */
    @SuppressWarnings("unchecked")
    public static Charge retrieve(String id) throws MyropayException {
        return retrieve(id, null);
    }

    @SuppressWarnings("unchecked")
    public static Charge retrieve(String id, String apiKeyOverride) throws MyropayException {
        if (id == null || id.isEmpty()) throw new InvalidRequestException("id is required");
        Map<String, String> query = new HashMap<>();
        query.put("session_id", id);
        Map<String, Object> res = ApiRequestor.request("GET", query, null, apiKeyOverride);
        Object session = res.get("session");
        return new Charge(session instanceof Map ? (Map<String, Object>) session : res);
    }

    public String getId() {
        return str("session_id");
    }

    public String getCheckoutUrl() {
        return str("checkout_url");
    }

    public String getStatus() {
        return str("status");
    }

    public String getCurrency() {
        return str("currency");
    }

    public Double getAmount() {
        Object v = data.get("amount");
        return v == null ? null : ((Number) v).doubleValue();
    }

    public String getExpiresAt() {
        return str("expires_at");
    }

    /** Escape hatch for any field not exposed by a typed getter above. */
    public Object get(String key) {
        return data.get(key);
    }

    public Map<String, Object> asMap() {
        return data;
    }

    private String str(String key) {
        Object v = data.get(key);
        return v == null ? null : v.toString();
    }

    @Override
    public String toString() {
        return "Charge" + data;
    }
}
