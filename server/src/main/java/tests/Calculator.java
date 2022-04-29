package tests;

import services.CalculatorInterface;

import java.rmi.RemoteException;

public class Calculator {
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_RESET = "\033[0m";
    public static final String ANSI_YELLOW = "\033[0;33m";


    services.Calculator c;


    public static void test(CalculatorInterface ci) {
        double result = 0;
        int expectedResult = 0;
        String methodName = "";
        int errors = 0;
        int totalTests = 0;
        try {
            System.out.println("Running tests...");
            try {
                ++totalTests;
                methodName = "add";
                expectedResult = 55;
                result = ci.add(50, 5);
                assert result == expectedResult : getFailMessage(methodName, expectedResult, result);
                System.out.println(getSuccessMessage(methodName));
            } catch (AssertionError ae) {
                ++errors;
                System.out.println(ae.getMessage());
            }

            try {
                ++totalTests;
                methodName = "substract";
                expectedResult = 45;
                result = ci.substract(50, 5);
                assert result == expectedResult : getFailMessage(methodName, expectedResult, result);
                System.out.println(getSuccessMessage(methodName));
            } catch (AssertionError ae) {
                ++errors;
                System.out.println(ae.getMessage());
            }

            try {
                ++totalTests;
                methodName = "multiply";
                expectedResult = 250;
                result = ci.multiply(50, 5);
                assert result == expectedResult : getFailMessage(methodName, expectedResult, result);
                System.out.println(getSuccessMessage(methodName));
            } catch (AssertionError ae) {
                ++errors;
                System.out.println(ae.getMessage());
            }

            try {
                ++totalTests;
                methodName = "divide";
                expectedResult = 10;
                result = ci.divide(50, 5);
                assert result == expectedResult : getFailMessage(methodName, expectedResult, result);
                System.out.println(getSuccessMessage(methodName));
            } catch (AssertionError ae) {
                ++errors;
                System.out.println(ae.getMessage());
            }

            try {
                ++totalTests;
                methodName = "pow2";
                expectedResult = 25;
                result = ci.pow2(5);
                assert result == expectedResult : getFailMessage(methodName, expectedResult, result);
                System.out.println(getSuccessMessage(methodName));
            } catch (AssertionError ae) {
                ++errors;
                System.out.println(ae.getMessage());
            }

            try {
                ++totalTests;
                methodName = "aPowB";
                expectedResult = 125;
                result = ci.aPowB(5, 3);
                assert result == expectedResult : getFailMessage(methodName, expectedResult, result);
                System.out.println(getSuccessMessage(methodName));
            } catch (AssertionError ae) {
                ++errors;
                System.out.println(ae.getMessage());
            }

            try {
                ++totalTests;
                methodName = "sqrt";
                expectedResult = 11;
                result = ci.sqrt(121);
                assert result == expectedResult : getFailMessage(methodName, expectedResult, result);
                System.out.println(getSuccessMessage(methodName));
            } catch (AssertionError ae) {
                ++errors;
                System.out.println(ae.getMessage());
            }

            try {
                ++totalTests;
                methodName = "nFact";
                expectedResult = 5040;
                result = ci.nFact(7);
                assert result == expectedResult : getFailMessage(methodName, expectedResult, result);
                System.out.println(getSuccessMessage(methodName));
            } catch (AssertionError ae) {
                ++errors;
                System.out.println(ae.getMessage());
            }

            try {
                ++totalTests;
                methodName = "percentage";
                expectedResult = 2;
                        result = ci.percentage(200);
                assert result == expectedResult : getFailMessage(methodName, expectedResult, result);
                System.out.println(getSuccessMessage(methodName));
            } catch (AssertionError ae) {
                ++errors;
                System.out.println(ae.getMessage());
            }

            try {
                ++totalTests;
                methodName = "combNofK1";
                expectedResult = 45;
                result = ci.combNofK1(10, 2);
                assert result == expectedResult : getFailMessage(methodName, expectedResult, result);
                System.out.println(getSuccessMessage(methodName));
            } catch (AssertionError ae) {
                ++errors;
                System.out.println(ae.getMessage());
            }

            try {
                ++totalTests;
                methodName = "combNofK2";
                expectedResult = 45;
                result = ci.combNofK2(10, 2);
                assert result == expectedResult : getFailMessage(methodName, expectedResult, result);
                System.out.println(getSuccessMessage(methodName));
            } catch (AssertionError ae) {
                ++errors;
                System.out.println(ae.getMessage());
            }


        } catch (RemoteException e) {
            e.printStackTrace();
        }
        String message = "Tests complete";
        if (errors == 0) {
            message = greenText(message) + ("-> All" + greenText(String.valueOf(totalTests)) + "tests successful");
        } else {
            message = redText(message) + "->" + (redText(String.valueOf(errors)) + " failed | " + greenText(String.valueOf(totalTests - errors)) + " successful\n");
        }
        System.out.println(message);
    }

    private static String redText(String text) {
        return ANSI_RED + " " + text + " " + ANSI_RESET;
    }

    private static String greenText(String text) {
        return ANSI_GREEN + " " + text + " " + ANSI_RESET;
    }

    private static String yellowText(String text) {
        return ANSI_YELLOW + " " + text + " " + ANSI_RESET;
    }

    private static String getSuccessMessage(String methodName) {
        return greenText(" ✓ ") + methodName;
    }

    private static String getFailMessage(String methodName, double expectedResult, double receivedResult) {
        return redText(" ✗ ") + methodName + " | expected: " + greenText(String.valueOf(expectedResult)) + " | received: " + yellowText(String.valueOf(receivedResult));
    }
}
