package com.gateway.services;

import org.springframework.stereotype.Service;

import java.time.YearMonth;
import java.util.regex.Pattern;

@Service
public class ValidationService {

    private static final Pattern VPA_PATTERN = Pattern.compile("^[a-zA-Z0-9._-]+@[a-zA-Z0-9]+$");

    public boolean validateVPA(String vpa) {
        if (vpa == null)
            return false;
        return VPA_PATTERN.matcher(vpa).matches();
    }

    public boolean validateLuhn(String cardNumber) {
        if (cardNumber == null)
            return false;
        String cleanNumber = cardNumber.replaceAll("[\\s-]", "");
        if (!cleanNumber.matches("\\d{13,19}"))
            return false;

        int sum = 0;
        boolean alternate = false;
        for (int i = cleanNumber.length() - 1; i >= 0; i--) {
            int n = Integer.parseInt(cleanNumber.substring(i, i + 1));
            if (alternate) {
                n *= 2;
                if (n > 9) {
                    n = (n % 10) + 1;
                }
            }
            sum += n;
            alternate = !alternate;
        }
        return (sum % 10 == 0);
    }

    public String detectCardNetwork(String cardNumber) {
        if (cardNumber == null)
            return "unknown";
        String cleanNumber = cardNumber.replaceAll("[\\s-]", "");

        if (cleanNumber.startsWith("4"))
            return "visa";
        if (cleanNumber.matches("^5[1-5].*"))
            return "mastercard";
        if (cleanNumber.matches("^(34|37).*"))
            return "amex";
        if (cleanNumber.matches("^(60|65|8[1-9]).*"))
            return "rupay";

        return "unknown";
    }

    public boolean validateExpiry(String expMonth, String expYear) {
        if (expMonth == null || expYear == null)
            return false;
        try {
            int month = Integer.parseInt(expMonth);
            int year = Integer.parseInt(expYear);

            if (month < 1 || month > 12)
                return false;

            // Handle 2-digit year
            if (year < 100) {
                year += 2000;
            }

            YearMonth expiryDate = YearMonth.of(year, month);
            YearMonth currentMonth = YearMonth.now();

            return !expiryDate.isBefore(currentMonth);
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
