package utilities;

import static utilities.Validation.validate;

public class Manipulation {
    public static Number trimNumberIfPossible(Number nr) {
        String s = nr.toString();
        System.out.println("? " + s);
        boolean isSimpleInt = validate(s, new String[]{"simpleInt"});
        if (isSimpleInt) {
            return nr.intValue();
        } else {
            return nr;
        }
    }

    public static Double parseInputNumber(String input) {
        Double d = null;
        try {
            d = Double.parseDouble(input);
        } catch (NumberFormatException ignored) {
            System.out.println("Cannot parse 'number': " + input);
        }
        return d;
    }

    public static String trimIfPossible(String s) {
        boolean isSimpleInt = validate(s, new String[]{"simpleInt"});
        if (isSimpleInt) {
            return s.split("\\.")[0];
        }
        return s;
    }

    public static Number trimIfPossible(Number nr) {
        String s = nr.toString();
        boolean isSimpleInt = validate(s, new String[]{"simpleInt"});
        if (isSimpleInt) {
            return nr.intValue();
        } else {
            return nr;
        }
    }
}
