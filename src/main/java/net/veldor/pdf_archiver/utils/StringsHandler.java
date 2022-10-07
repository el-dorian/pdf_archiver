package net.veldor.pdf_archiver.utils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringsHandler {

    public static String superTrim(String s) {
        return s.replaceAll("\\s", "");
    }

    public static String getStringFrom(String s, int prefixLength) {
        return trim(s.substring(prefixLength));
    }

    private static String trim(String s) {
        return s.trim();
    }

    public static String getExecutionNumber(String value) throws FileNotHandledException {
        String[] mStrings = value.split("\n");
        for (String s : mStrings) {
            if (s.length() != 0) {
                if (s.trim().startsWith("Номер исследования:")) {
                    return new Grammar().clearExecutionNumber(superTrim(getStringFrom(s, 19).toUpperCase().replace("А", "A").replace("Т", "T")));
                }

                if (s.trim().startsWith("ID исследования:")) {
                    return new Grammar().clearExecutionNumber(superTrim(getStringFrom(s, 17).toUpperCase().replace("А", "A").replace("Т", "T")));
                }
                if (s.trim().startsWith("Patient ID")) {
                    return new Grammar().clearExecutionNumber(superTrim(getStringFrom(s, 10).toUpperCase().replace("А", "A").replace("Т", "T")));
                }
            }
        }
        return null;
    }

    public static String removeSlashes(String s) {
        return s.replaceAll("\\\\", "").replaceAll("/", "");
    }

    public static String collapseSpaces(String value) {
        return value.trim().replaceAll("\\s+", " ");
    }

    public static String getOutExecutionArea(String value) {
        String[] mStrings = value.split("\n");
        for (String s : mStrings) {
                if (s.length() != 0 && s.trim().startsWith("Область исследования:")) {
                    String area = StringsHandler.getStringFrom(s, 21);
                    return StringsHandler.clearExecutionArea(area);
                }
        }
        return "";
    }

    private static String clearExecutionArea(String area) {
        return StringsHandler.removeSlashes(StringsHandler.collapseSpaces(area.toLowerCase(Locale.ROOT)));
    }

    public static String getPatientName(String value) {
        String[] mStrings = value.split("\n");
        for (String s : mStrings) {
            if (s.length() != 0) {
                if (s.trim().startsWith("Фамилия, имя, отчество:")) {
                    return getStringFrom(s, 23);
                }
                else if (s.trim().startsWith("Ф.И.О. пациента:")) {
                    return getStringFrom(s, 16);
                }
                else if (s.trim().startsWith("Фамилия, имя отчество:")) {
                    return getStringFrom(s, 22);
                }
            }
        }
        return "";
    }

    public static String getPatientBirthdate(String text) {
        String[] mStrings = text.split("\n");
        for (String s : mStrings) {
            if (s.length() != 0) {
                if (s.trim().startsWith("Дата рождения:")) {
                    return getStringFrom(s, 14);
                }
                else if (s.trim().startsWith("Год рождения:")) {
                    return getStringFrom(s, 13);
                }
                else if (s.trim().startsWith("Дата рождения (возраст):")) {
                    return getStringFrom(s, 24);
                }
            }
        }
        return "";
    }

    public static String getDoctorName(String text) {
        Pattern sSignPattern = Pattern.compile("^\\s*(\\d{1,2}[/|.]\\d{1,2}[/|.]\\d{2,4}).*(врач)?.+\\s([а-я]\\.)*\\s*([а-я]{5,})", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
        Pattern sSignPattern1 = Pattern.compile("^\\s*(врач)?.+\\s([а-я]\\.)*\\s*([а-я]{5,})", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
        String[] strings = text.split("\n");
        String[] reversed = reverseArray(strings);
        for (String s : reversed) {
            if (s.length() != 0) {
                if(s.startsWith("Данное заключение не является диагнозом")){
                    continue;
                }
                Matcher matcher = sSignPattern.matcher(s.trim());
                if (matcher.find()) {
                    if (!matcher.group(4).equals("Врач")) {
                        return matcher.group(4);
                    }
                }
                Matcher matcher1 = sSignPattern1.matcher(s.trim());
                if (matcher1.find()) {
                    if (!matcher1.group(3).equals("Врач")) {
                        return matcher1.group(3);
                    }
                }
            }
        }
        return null;
    }

    public static String getExecutionDate(String text) {
        Pattern sSignPattern = Pattern.compile(
                "^(Дата исследования:)?\\s*(\\d{1,2}[/|.]\\d{1,2}[/|.]\\d{2,4}).*",
                Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
        String[] strings = text.split("\n");
        String[] reversed = reverseArray(strings);
        for (String s : reversed) {
            if (s.length() != 0) {
                Matcher matcher = sSignPattern.matcher(s.trim());
                if (matcher.find()) {
                    // первая найденная группа- дата, тут всё просто, она уже отформатирована
                    return matcher.group(2);
                }
            }
        }
        return null;
    }

    public static String[] reverseArray(String[] arr) {
        // Converting Array to List
        List<String> list = Arrays.asList(arr);
        // Reversing the list using Collections.reverse() method
        Collections.reverse(list);
        // Converting list back to Array
        return list.toArray(arr);
    }

    public static String getContrastInfo(String text) {
        String[] mStrings = text.split("\n");
        for (String s : mStrings) {
            if (s.length() != 0) {
                if (s.trim().toLowerCase().startsWith("в/в контрастное усиление")) {
                    return getStringFrom(s, 25);
                }
                else if (s.trim().toLowerCase().startsWith("в/в контрастирование")) {
                    return getStringFrom(s, 21);
                }
                else if (s.trim().toLowerCase().startsWith("в/венное контрастное усиление")) {
                    return getStringFrom(s, 30);
                }
                else if (s.trim().toLowerCase().startsWith("в/в динамическое контрастное усиление")) {
                    return getStringFrom(s, 38);
                }
                else if (s.trim().toLowerCase().startsWith("контрастирование")) {
                    return getStringFrom(s, 17);
                }
            }
        }
        return "";
    }

    /**
     * Удалю всё, кроме букв и переведу в нижний регистр
     *
     * @param s <p>Грязная строка</p>
     * @return <p>Чистая строка</p>
     */
    public static String clearText(String s) {
        return s.toLowerCase().replaceAll("[^а-я]", "");
    }

    public static String clearWhitespaces(String s) {
        if (s != null) {
            return s.replaceAll("\\s", " ").replaceAll("\\s+", " ").trim();
        }
        return null;
    }


    public static String getSex(String text) {
        String[] mStrings = text.split("\n");
        for (String s : mStrings) {
            if (s.length() != 0) {
                if (s.trim().startsWith("Пол:")) {
                    return getStringFrom(s, 4);
                }
            }
        }
        return "";
    }

    private static String dropWhitespaces(String s) {
        return s.replaceAll("\\s", "");
    }

    public static String cleanExecutionNumber(String num) {
        num = dropWhitespaces(num);
        String[] ary = num.split("");
        int counter = 0;
        for (String s :
                ary) {
            if(counter > 0 && s.matches("\\D")){
                break;
            }
            counter++;
        }
        return num.substring(0, counter).toUpperCase(Locale.ROOT).replace("А", "A").replace("Т", "T");
    }
}