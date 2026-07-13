# myropay-java

Official Java library for the [MyroPay](https://myropay.com) API. Zero external dependencies — uses `java.net.http.HttpClient` (built into Java 11+) and a small internal JSON helper.

## Install

**Maven**

```xml
<dependency>
  <groupId>com.myropay</groupId>
  <artifactId>myropay-java</artifactId>
  <version>1.0.0</version>
</dependency>
```

**Gradle**

```groovy
implementation 'com.myropay:myropay-java:1.0.0'
```

## Usage

```java
import com.myropay.Myropay;
import com.myropay.model.Charge;
import java.util.HashMap;
import java.util.Map;

Myropay.apiKey = "sk_test_...";

Map<String, Object> params = new HashMap<>();
params.put("email", "customer@email.com");
params.put("amount", 500000);      // whole currency units — ₦500,000, not kobo
params.put("currency", "NGN");     // optional, defaults to NGN
params.put("reference", "ORD-1029"); // optional, your own order id

Charge charge = Charge.create(params);
response.sendRedirect(charge.getCheckoutUrl());
```

Retrieve a charge later:

```java
Charge charge = Charge.retrieve("cs_test_...");
if ("completed".equals(charge.getStatus())) {
    // fulfil the order
}
```

## Important

- `Myropay.apiKey` (`sk_live_...` / `sk_test_...`) must **never** reach a browser — use this library from your own server only. Get your keys from the MyroPay business dashboard under **API & Apps**.
- Amounts are in the currency's own whole units (e.g. `500000` NGN = ₦500,000), not subunits like kobo or cents.
- For a client-side "Pay with MyroPay" button/modal that never needs your secret key, use the [Checkout SDK](https://dev.myropay.com#sdk) (`myropay.js`) instead — this library is for server-side charge creation and verification.
- Requires Java 11+ (for `java.net.http.HttpClient`).

## Errors

All calls throw checked `MyropayException` subtypes:

```java
import com.myropay.exception.InvalidRequestException;
import com.myropay.exception.ApiException;

try {
    Map<String, Object> params = new HashMap<>();
    params.put("amount", 5000); // missing email
    Charge.create(params);
} catch (InvalidRequestException e) {
    // missing/invalid parameters, no request was sent
} catch (ApiException e) {
    // the API itself returned an error
    System.out.println(e.getMessage() + " (HTTP " + e.getHttpStatus() + ")");
}
```

## License

MIT
