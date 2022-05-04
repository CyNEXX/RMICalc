package utilities;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Validation class that holds patterns to be used when validating data
 */
public class Validation {

    private static final Pattern ipPattern = Pattern.compile("^(((25[0-5]|2[0-4]\\d|[1]\\d{2}|\\d{2}|\\d)\\.){3}(25[0-5]|2[0-4]\\d|[1]\\d{2}|\\d{2}|\\d){1})$");
    private static final Pattern portPattern = Pattern.compile("^\\d{1,5}$");
    private static final Pattern simpleInt = Pattern.compile("^-?((\\d)+\\.0+)$");
    private static final Pattern noPeriod = Pattern.compile("^-?\\d+$");
    private static final Pattern digitalInput = Pattern.compile("^\\d+\\.?\\d*$");

    private static final Map<String, Pattern> patternMap = new HashMap<>();

    static {
        patternMap.put("ipPattern", ipPattern);
        patternMap.put("portPattern", portPattern);
        patternMap.put("simpleInt", simpleInt);
        patternMap.put("noPeriod", noPeriod);
        patternMap.put("digitalInput", digitalInput);
    }

    static class AbstractValidation {
        Pattern p;

        public AbstractValidation(String patternName) {
            this.p = patternMap.get(patternName);
        }

        public boolean check(String textToCheck) {
            Matcher m = p.matcher(textToCheck.trim());
            return m.find();
        }
    }

    /**
     * Calls validation for a string against a series of different types of validation
     * @param text The text that should be validated
     * @param validationTypes A string with the names of the validation. See patternMap
     * @return A boolean representing that the input string passed the validation or not
     */
    public static boolean validate(String text, String[] validationTypes) {
        List<String> checkList = Arrays.asList(validationTypes);
        return checkList.stream().allMatch(s -> new AbstractValidation(s).check(text));
    }
}
