package com.myropay;

/**
 * Myropay.apiKey = "sk_test_...";
 * Charge charge = Charge.create(params);
 */
public final class Myropay {
    private Myropay() {}

    /** Your MyroPay secret key (sk_live_... or sk_test_...). Never expose
     * this in client-side/frontend code. */
    public static String apiKey;

    public static final String API_BASE = "https://checkout.myropay.com/api/sessions.php";
    public static final String VERSION = "1.0.0";
}
