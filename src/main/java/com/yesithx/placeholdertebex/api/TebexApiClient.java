package com.yesithx.placeholdertebex.api;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.yesithx.placeholdertebex.PlaceholderTebex;
import com.yesithx.placeholdertebex.model.Donor;
import com.yesithx.placeholdertebex.model.StoreInfo;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class TebexApiClient {

    private static final DateTimeFormatter[] TEBEX_DATE_FORMATS = {
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
    };

    private final PlaceholderTebex plugin;

    public TebexApiClient(PlaceholderTebex plugin) {
        this.plugin = plugin;
    }

    private String getSecretKey() {
        return plugin.getConfigManager().getSecretKey();
    }

    private String getBaseUrl() {
        return plugin.getConfigManager().getBaseUrl();
    }

    private String fetch(String endpoint) throws Exception {
        URI uri = URI.create(getBaseUrl() + endpoint);
        HttpURLConnection conn = (HttpURLConnection) uri.toURL().openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("X-Tebex-Secret", getSecretKey());
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(10000);

        int status = conn.getResponseCode();
        if (status != 200) {
            BufferedReader errReader = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
            StringBuilder errBody = new StringBuilder();
            String errLine;
            while ((errLine = errReader.readLine()) != null) errBody.append(errLine);
            throw new RuntimeException("Tebex API status " + status + " - " + errBody);
        }

        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        return sb.toString();
    }

    /**
     * Loads the newest {@code limit} completed payments ({@code limit <= 0} loads the whole history).
     * {@code /payments?limit=} rejects anything above 100, so this walks the paginated endpoint
     * until the limit or the last page is reached.
     */
    public List<Donor> fetchPayments(int limit) {
        List<Donor> donors = new ArrayList<>();
        try {
            int seen = 0;
            int page = 1;
            int lastPage = 1;
            do {
                String json = fetch("/payments?paged=1&page=" + page);

                JsonArray data = null;
                try {
                    data = JsonParser.parseString(json).getAsJsonArray();
                } catch (Exception e1) {
                    try {
                        JsonObject root = JsonParser.parseString(json).getAsJsonObject();
                        if (root.has("last_page")) lastPage = root.get("last_page").getAsInt();
                        if (root.has("data")) {
                            data = root.getAsJsonArray("data");
                        } else if (root.has("payments")) {
                            data = root.getAsJsonArray("payments");
                        }
                    } catch (Exception e2) {
                        plugin.getLogger().warning("[PlaceholderTebex] Could not parse payments response: " + e2.getMessage());
                    }
                }

                if (data == null) {
                    plugin.getLogger().warning("[PlaceholderTebex] No payment data found in response.");
                    return donors;
                }

                for (JsonElement el : data) {
                    JsonObject payment = el.getAsJsonObject();

                    // Unlike /payments?limit=, the paginated history also lists declined and refunded payments.
                    if (!getStringSafe(payment, "status").equalsIgnoreCase("Complete")) continue;
                    if (limit > 0 && seen >= limit) return donors;
                    seen++;

                    String username = getStringSafe(payment, "player", "name");
                    if (username.equals(plugin.getConfigManager().getNoDataPlaceholder())) {
                        username = getStringSafe(payment, "username");
                    }
                    if (username.equals(plugin.getConfigManager().getNoDataPlaceholder())) {
                        username = getStringSafe(payment, "name");
                    }

                    double amount = 0.0;
                    if (payment.has("amount")) amount = payment.get("amount").getAsDouble();
                    else if (payment.has("price")) amount = payment.get("price").getAsDouble();

                    String currency = getNestedStringSafe(payment, "currency", "iso_4217");
                    if (currency.equals(plugin.getConfigManager().getNoDataPlaceholder())) {
                        currency = getStringSafe(payment, "currency");
                    }

                    String dateStr = getStringSafe(payment, "date");
                    if (dateStr.equals(plugin.getConfigManager().getNoDataPlaceholder())) {
                        dateStr = getStringSafe(payment, "created_at");
                    }
                    LocalDateTime date = parseDateSafe(dateStr);

                    String packageName = getFirstPackageName(payment);
                    String transactionId = getStringSafe(payment, "transaction_id");
                    if (transactionId.equals(plugin.getConfigManager().getNoDataPlaceholder())) {
                        transactionId = getStringSafe(payment, "txn_id");
                    }
                    String email = getStringSafe(payment, "email");

                    donors.add(new Donor(username, amount, currency, date, packageName, transactionId, email));
                }
                page++;
            } while (page <= lastPage);

        } catch (Exception e) {
            plugin.getLogger().severe("[PlaceholderTebex] Failed to fetch payments: " + e.getMessage());
            e.printStackTrace();
        }
        return donors;
    }

    public StoreInfo fetchStoreInfo() {
        try {
            String json = fetch("/information");
            JsonObject root = JsonParser.parseString(json).getAsJsonObject();
            JsonObject account = root.getAsJsonObject("account");
            JsonObject server = root.getAsJsonObject("server");

            String storeName = server != null ? getStringSafe(server, "name") : getStringSafe(account, "name");
            String currency = getNestedStringSafe(account, "currency", "iso_4217");
            String domain = getStringSafe(account, "domain");
            String description = getStringSafe(account, "name");
            int totalPackages = 0;

            return new StoreInfo(storeName, currency, domain, description, totalPackages);
        } catch (Exception e) {
            plugin.getLogger().warning("[PlaceholderTebex] Failed to fetch store info: " + e.getMessage());
            return null;
        }
    }

    private String getStringSafe(JsonObject obj, String key) {
        if (obj != null && obj.has(key) && !obj.get(key).isJsonNull()) {
            return obj.get(key).getAsString();
        }
        return plugin.getConfigManager().getNoDataPlaceholder();
    }

    private String getStringSafe(JsonObject obj, String nestedKey, String key) {
        if (obj != null && obj.has(nestedKey) && obj.get(nestedKey).isJsonObject()) {
            JsonObject nested = obj.getAsJsonObject(nestedKey);
            return getStringSafe(nested, key);
        }
        return plugin.getConfigManager().getNoDataPlaceholder();
    }

    private String getNestedStringSafe(JsonObject obj, String nestedKey, String key) {
        return getStringSafe(obj, nestedKey, key);
    }

    private String getFirstPackageName(JsonObject payment) {
        if (payment.has("packages") && payment.get("packages").isJsonArray()) {
            JsonArray packages = payment.getAsJsonArray("packages");
            if (!packages.isEmpty()) {
                JsonObject pkg = packages.get(0).getAsJsonObject();
                return getStringSafe(pkg, "name");
            }
        }
        return plugin.getConfigManager().getNoDataPlaceholder();
    }

    private LocalDateTime parseDateSafe(String dateStr) {
        if (dateStr == null || dateStr.equals(plugin.getConfigManager().getNoDataPlaceholder())) {
            return LocalDateTime.now();
        }
        for (DateTimeFormatter fmt : TEBEX_DATE_FORMATS) {
            try {
                return LocalDateTime.parse(dateStr, fmt);
            } catch (Exception ignored) {}
        }
        return LocalDateTime.now();
    }
}
