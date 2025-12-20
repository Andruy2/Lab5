package utils;

import java.util.regex.Pattern;

public class ValidationUtils {

    public static boolean isValidPassport(String passport) {
        if (passport == null) return false;
        return Pattern.matches("\\d{10}", passport.trim());
    }

    public static boolean isValidName(String name) {
        if (name == null || name.trim().isEmpty()) return false;
        return Pattern.matches("[а-яА-ЯёЁa-zA-Z\\s\\-]+", name.trim());
    }

    // Новая валидация для суммы
    public static boolean isValidDeposit(String depositStr) {
        if (depositStr == null || depositStr.trim().isEmpty()) {
            return false;
        }

        // Проверяем на наличие посторонних символов
        String cleanStr = depositStr.trim();
        if (!cleanStr.matches("[\\d.,]+")) {
            return false;
        }

        // Заменяем запятую на точку
        cleanStr = cleanStr.replace(',', '.');

        try {
            double deposit = Double.parseDouble(cleanStr);
            return deposit > 0 && deposit <= 1000000000; // Макс 1 млрд
        } catch (NumberFormatException e) {
            return false;
        }
    }

    // Парсинг суммы с обработкой запятых
    public static double parseDeposit(String depositStr) throws NumberFormatException {
        if (depositStr == null || depositStr.trim().isEmpty()) {
            throw new NumberFormatException("Пустая строка");
        }

        String cleanStr = depositStr.trim().replace(',', '.');

        // Удаляем все нецифровые символы, кроме точки
        cleanStr = cleanStr.replaceAll("[^\\d.]", "");

        return Double.parseDouble(cleanStr);
    }

    // Проверка на валидные символы (только цифры, точка, запятая)
    public static boolean containsOnlyValidChars(String str) {
        if (str == null) return false;
        return str.matches("[\\d.,]*");
    }

    // Проверка на максимальное количество знаков после запятой
    public static boolean isValidDecimalPlaces(String str, int maxDecimals) {
        if (str == null) return false;

        String cleanStr = str.replace(',', '.');
        if (!cleanStr.contains(".")) return true;

        String[] parts = cleanStr.split("\\.");
        if (parts.length != 2) return false;

        return parts[1].length() <= maxDecimals;
    }

    public static boolean isValidClientType(String type) {
        if (type == null) return false;
        String lowerType = type.trim().toLowerCase();
        return lowerType.equals("обычный") ||
                lowerType.equals("пенсионер") ||
                lowerType.equals("вип") ||
                lowerType.equals("regular") ||
                lowerType.equals("pensioner") ||
                lowerType.equals("vip");
    }

    public static String normalizeClientType(String type) {
        if (type == null) return "Обычный";

        String lowerType = type.trim().toLowerCase();
        switch (lowerType) {
            case "regular":
            case "обычный":
            case "обычный клиент":
                return "Обычный";
            case "pensioner":
            case "пенсионер":
                return "Пенсионер";
            case "vip":
            case "вип":
                return "ВИП";
            default:
                return "Обычный";
        }
    }

    // Получить человеко-читаемое сообщение об ошибке для суммы
    public static String getDepositErrorMessage(String depositStr) {
        if (depositStr == null || depositStr.trim().isEmpty()) {
            return "Введите сумму вклада";
        }

        if (!containsOnlyValidChars(depositStr)) {
            return "Сумма может содержать только цифры, точку или запятую";
        }

        try {
            double deposit = parseDeposit(depositStr);

            if (deposit <= 0) {
                return "Сумма должна быть больше 0";
            }

            if (deposit > 1000000000) {
                return "Сумма слишком велика (максимум 1 000 000 000 руб.)";
            }

            if (!isValidDecimalPlaces(depositStr, 2)) {
                return "Максимум 2 знака после запятой";
            }

            return null; // Ошибок нет

        } catch (NumberFormatException e) {
            return "Неверный формат числа";
        }
    }
}
