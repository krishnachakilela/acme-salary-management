package com.acme.salary.domain;

import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

public final class Money {
    private static final Pattern P = Pattern.compile("^[A-Z]{3}$");
    private static final long MIN = 1L, MAX = 100_000_000_000L;
    private final long amountMinor;
    private final String currencyCode;

    public Money(long amountMinor, String currencyCode) {
        if (amountMinor < MIN || amountMinor > MAX)
            throw new IllegalArgumentException("Amount out of range");
        if (currencyCode == null || currencyCode.isBlank())
            throw new IllegalArgumentException("Currency required");
        String n = currencyCode.trim().toUpperCase(Locale.ROOT);
        if (!P.matcher(n).matches())
            throw new IllegalArgumentException("Invalid currency");
        this.amountMinor = amountMinor;
        this.currencyCode = n;
    }

    public long amountMinor() {return amountMinor;}

    public String currencyCode() {return currencyCode;}

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof Money m && amountMinor == m.amountMinor && currencyCode.equals(m.currencyCode));
    }

    @Override
    public int hashCode() {return Objects.hash(amountMinor, currencyCode);}
}
