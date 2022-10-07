package net.veldor.pdf_archiver.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Grammar {
    public String clearExecutionNumber(String executionNumber) throws FileNotHandledException {
        String patternString = "^(\\w?\\d+)[.-]?\\d*$";
        Pattern pattern = Pattern.compile(patternString);
        Matcher matcher = pattern.matcher(executionNumber);
        if (matcher.find()) {
            if (matcher.groupCount() > 0) {
                return matcher.group(1);
            }
        }
        throw new FileNotHandledException("Номер обследования не соответствует правилам: " + executionNumber);
    }
}
