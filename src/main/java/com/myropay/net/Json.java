package com.myropay.net;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A deliberately minimal JSON reader/writer — just enough to serialize
 * the flat/nested Map request bodies this SDK sends, and parse the
 * flat JSON object responses the MyroPay API returns. Not a general-
 * purpose JSON library; kept internal so this SDK has zero external
 * dependencies (no Jackson/Gson just for a handful of fields).
 */
public final class Json {
    private Json() {}

    public static String write(Object value) {
        StringBuilder sb = new StringBuilder();
        writeValue(sb, value);
        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    private static void writeValue(StringBuilder sb, Object value) {
        if (value == null) {
            sb.append("null");
        } else if (value instanceof String) {
            writeString(sb, (String) value);
        } else if (value instanceof Number || value instanceof Boolean) {
            sb.append(value);
        } else if (value instanceof Map) {
            sb.append('{');
            boolean first = true;
            for (Map.Entry<?, ?> e : ((Map<?, ?>) value).entrySet()) {
                if (e.getValue() == null) continue; // omit nulls, matching how the other SDKs skip unset optional fields
                if (!first) sb.append(',');
                first = false;
                writeString(sb, String.valueOf(e.getKey()));
                sb.append(':');
                writeValue(sb, e.getValue());
            }
            sb.append('}');
        } else if (value instanceof List) {
            sb.append('[');
            boolean first = true;
            for (Object item : (List<?>) value) {
                if (!first) sb.append(',');
                first = false;
                writeValue(sb, item);
            }
            sb.append(']');
        } else {
            writeString(sb, value.toString());
        }
    }

    private static void writeString(StringBuilder sb, String s) {
        sb.append('"');
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"': sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default:
                    if (c < 0x20) sb.append(String.format("\\u%04x", (int) c));
                    else sb.append(c);
            }
        }
        sb.append('"');
    }

    /** Parses a JSON object into a Map. Only handles what the MyroPay API
     * actually returns (flat objects, occasional nesting, strings,
     * numbers, booleans, null) — not a general-purpose parser. */
    public static Map<String, Object> parseObject(String json) {
        Parser p = new Parser(json);
        p.skipWhitespace();
        Object result = p.parseValue();
        if (!(result instanceof Map)) {
            throw new IllegalArgumentException("Expected a JSON object at the top level");
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> map = (Map<String, Object>) result;
        return map;
    }

    private static final class Parser {
        private final String s;
        private int i = 0;

        Parser(String s) { this.s = s; }

        void skipWhitespace() {
            while (i < s.length() && Character.isWhitespace(s.charAt(i))) i++;
        }

        Object parseValue() {
            skipWhitespace();
            char c = s.charAt(i);
            if (c == '{') return parseObjectInternal();
            if (c == '[') return parseArray();
            if (c == '"') return parseString();
            if (c == 't') { i += 4; return Boolean.TRUE; }
            if (c == 'f') { i += 5; return Boolean.FALSE; }
            if (c == 'n') { i += 4; return null; }
            return parseNumber();
        }

        Map<String, Object> parseObjectInternal() {
            Map<String, Object> map = new LinkedHashMap<>();
            i++; // {
            skipWhitespace();
            if (s.charAt(i) == '}') { i++; return map; }
            while (true) {
                skipWhitespace();
                String key = parseString();
                skipWhitespace();
                i++; // :
                Object value = parseValue();
                map.put(key, value);
                skipWhitespace();
                if (s.charAt(i) == ',') { i++; continue; }
                i++; // }
                break;
            }
            return map;
        }

        List<Object> parseArray() {
            java.util.ArrayList<Object> list = new java.util.ArrayList<>();
            i++; // [
            skipWhitespace();
            if (s.charAt(i) == ']') { i++; return list; }
            while (true) {
                list.add(parseValue());
                skipWhitespace();
                if (s.charAt(i) == ',') { i++; skipWhitespace(); continue; }
                i++; // ]
                break;
            }
            return list;
        }

        String parseString() {
            i++; // opening quote
            StringBuilder sb = new StringBuilder();
            while (s.charAt(i) != '"') {
                char c = s.charAt(i);
                if (c == '\\') {
                    i++;
                    char esc = s.charAt(i);
                    switch (esc) {
                        case 'n': sb.append('\n'); break;
                        case 'r': sb.append('\r'); break;
                        case 't': sb.append('\t'); break;
                        case 'u':
                            sb.append((char) Integer.parseInt(s.substring(i + 1, i + 5), 16));
                            i += 4;
                            break;
                        default: sb.append(esc);
                    }
                } else {
                    sb.append(c);
                }
                i++;
            }
            i++; // closing quote
            return sb.toString();
        }

        Object parseNumber() {
            int start = i;
            while (i < s.length() && "-+.eE0123456789".indexOf(s.charAt(i)) >= 0) i++;
            String numStr = s.substring(start, i);
            if (numStr.contains(".") || numStr.contains("e") || numStr.contains("E")) {
                return Double.parseDouble(numStr);
            }
            try {
                return Long.parseLong(numStr);
            } catch (NumberFormatException e) {
                return Double.parseDouble(numStr);
            }
        }
    }
}
