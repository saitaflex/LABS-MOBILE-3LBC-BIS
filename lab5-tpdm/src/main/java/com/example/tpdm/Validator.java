package com.example.tpdm;

/** Form rules shared by registration, editing and password reset. */
final class Validator {

    static final int CIN_LENGTH = 8;
    static final int MIN_PASSWORD_LENGTH = 4;

    private Validator() {
    }

    /** A CIN is exactly 8 letters or digits, for example 12345678 or AB123456. */
    static boolean isValidCin(String cin) {
        return cin != null && cin.matches("[A-Za-z0-9]{" + CIN_LENGTH + "}");
    }

    static boolean isValidPassword(String password) {
        return password != null && password.length() >= MIN_PASSWORD_LENGTH;
    }
}
