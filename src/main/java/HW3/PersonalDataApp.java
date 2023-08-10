package HW3;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

// Создаем новый тип исключения для некорректных данных
class InvalidDataException extends Exception {
    public InvalidDataException(String message) {
        super(message);
    }
}

public class PersonalDataApp {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("Введите данные в формате: Фамилия Имя Отчество дата_рождения (dd.MM.yyyy) номер_телефона (только цифры) пол (m/f)");
            String input = scanner.nextLine();

            try {
                processInput(input);
            } catch (InvalidDataException e) {
                System.out.println("Ошибка валидации данных: " + e.getMessage());
            } catch (IOException e) {
                System.out.println("Ошибка при работе с файлом: " + e.getMessage());
                e.printStackTrace(); // Вывод стека вызовов
            }

            // Предложение продолжить или выйти из приложения
            System.out.println("Хотите продолжить ввод данных? (y/n)");
            String choice = scanner.nextLine();
            if (!choice.equalsIgnoreCase("y")) {
                break;
            }
        }

        System.out.println("Завершение приложения.");
    }

    private static void processInput(String input) throws IOException, InvalidDataException {
        String[] dataParts = input.split(" ");

        if (dataParts.length != 6) {
            throw new InvalidDataException("Неверное количество данных. Пожалуйста, введите все требуемые данные.");
        }

        String lastName = dataParts[0];
        String firstName = dataParts[1];
        String middleName = dataParts[2];
        String birthDateStr = dataParts[3];
        String phoneNumberStr = dataParts[4];
        String genderStr = dataParts[5];

        List<String> errors = new ArrayList<>();

        if (!isValidName(lastName) || !isValidName(firstName) || !isValidName(middleName)) {
            errors.add("Некорректные значения для фамилии, имени или отчества.");
        }

        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
            Date birthDate = validateAndParseDate(birthDateStr);

            long phoneNumber = parsePhoneNumber(phoneNumberStr);

            validateGender(genderStr);
        } catch (NumberFormatException e) {
            errors.add("Ошибка парсинга числового значения: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            errors.add("Ошибка валидации данных: " + e.getMessage());
        } catch (ParseException e) {
            errors.add("Ошибка ввода даты рождения.");
        }

        if (!errors.isEmpty()) {
            throw new InvalidDataException(String.join(" ", errors));
        }

        String record = String.format("<%s><%s><%s><%s> <%s><%s>",
                lastName, firstName, middleName, birthDateStr, phoneNumberStr, genderStr);

        if (!isDuplicateRecord(record, lastName + ".txt")) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(lastName + ".txt", true))) {
                writer.write(record);
                writer.newLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("Данные успешно записаны в файл.");
        } else {
            System.out.println("Такая запись уже существует. Данные не будут записаны.");
        }
    }

    private static boolean isDuplicateRecord(String newRecord, String fileName) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.equals(newRecord)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean isValidName(String name) {
        return Pattern.matches("^[а-яА-Яa-zA-Z]+$", name);
    }

    private static long parsePhoneNumber(String phoneNumberStr) throws InvalidDataException {
        try {
            return Long.parseLong(phoneNumberStr);
        } catch (NumberFormatException e) {
            throw new InvalidDataException("Некорректный номер телефона: " + e.getMessage());
        }
    }

    private static void validateGender(String genderStr) throws InvalidDataException {
        if (!genderStr.equalsIgnoreCase("m") && !genderStr.equalsIgnoreCase("f")) {
            throw new InvalidDataException("Некорректное значение пола. Введите 'm' для мужчины или 'f' для женщины.");
        }
    }

    private static Date validateAndParseDate(String dateStr) throws ParseException, InvalidDataException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
        dateFormat.setLenient(false);  // Отключаем "гибкий" режим парсинга даты

        Date date;
        try {
            date = dateFormat.parse(dateStr);
        } catch (ParseException e) {
            throw new InvalidDataException("Ошибка ввода даты рождения.");
        }

        if (!isValidDayAndMonth(date)) {
            throw new InvalidDataException("Ошибка ввода даты рождения.");
        }

        return date;
    }

    private static boolean isValidDayAndMonth(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        boolean isValidMonth = (month >= 0 && month <= 11);
        boolean isValidDay = (day > 0 && day <= 31);

        boolean isValidNumberOfDays = true;
        if ((day > 30 && (month == 3 || month == 5 || month == 8 || month == 10)) ||
                (day > 29 && month == 1 && (year % 4 == 0 && (year % 100 != 0 || year % 400 == 0))) ||
                (day > 28 && month == 1)) {
            isValidNumberOfDays = false;
        }

        return isValidMonth && isValidDay && isValidNumberOfDays;
    }
}






