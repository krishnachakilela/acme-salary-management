package com.acme.salary.domain;

import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

public final class CountryCode {
    private static final Pattern P = Pattern.compile("^[A-Z]{2}$");
    private final String value;

    private CountryCode(String value) {this.value = value;}

    public static CountryCode of(String raw) {
        if (raw == null || raw.isBlank())
            throw new IllegalArgumentException("Country code required");
        String n = raw.trim().toUpperCase(Locale.ROOT);
        if (!P.matcher(n).matches())
            throw new IllegalArgumentException("Invalid country code");
        return new CountryCode(n);
    }

    public String value() {return value;}

    @Override
    public boolean equals(Object o) {return this == o || (o instanceof CountryCode c && value.equals(c.value));}

    @Override
    public int hashCode() {return Objects.hash(value);}
}
