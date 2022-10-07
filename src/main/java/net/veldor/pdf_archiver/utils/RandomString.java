package net.veldor.pdf_archiver.utils;

import java.security.SecureRandom;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;

public class RandomString {
    public static final String lower;
    public static final String alphanum;
    private final Random random;
    private final char[] symbols;
    private final char[] buf;

    public String nextString() {
        for(int idx = 0; idx < this.buf.length; ++idx) {
            this.buf[idx] = this.symbols[this.random.nextInt(this.symbols.length)];
        }

        return new String(this.buf);
    }

    public RandomString(int length, Random random, String symbols) {
        if (length < 1) {
            throw new IllegalArgumentException();
        } else if (symbols.length() < 2) {
            throw new IllegalArgumentException();
        } else {
            this.random = Objects.requireNonNull(random);
            this.symbols = symbols.toCharArray();
            this.buf = new char[length];
        }
    }

    public RandomString(int length, Random random) {
        this(length, random, alphanum);
    }

    public RandomString(int length) {
        this(length, new SecureRandom());
    }

    public RandomString() {
        this(21);
    }

    static {
        lower = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toLowerCase(Locale.ROOT);
        alphanum = "ABCDEFGHIJKLMNOPQRSTUVWXYZ" + lower + "0123456789";
    }
}
