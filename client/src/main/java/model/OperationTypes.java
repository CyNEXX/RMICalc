package model;

public enum OperationTypes {
    NONE(0), ADD(1), SUBSTRACT(2), MULTIPLY(3), DIVIDE(4), POW2(5), A_POW_B(6), SQRT(7), N_FACT(8), COMB_N_OF_K(9), PERCENTAGE(10);

    private int type;
    private String sign;
    private String name;
    private boolean eager;

    OperationTypes(int type) {
        this.type = type;
        switch (type) {
            case 0: {
                this.sign = "";
                this.name = "";
                this.eager = false;
                break;
            }
            case 1: {
                this.sign = "+";
                this.name = "add";
                this.eager = false;
                break;
            }
            case 2: {
                this.sign = "-";
                this.name = "substract";
                this.eager = false;
                break;
            }
            case 3: {
                this.sign = "x";
                this.name = "multiply";
                this.eager = false;
                break;
            }
            case 4: {
                this.sign = "/";
                this.name = "divide";
                this.eager = false;
                break;
            }
            case 5: {
                this.sign = "x²";
                this.name = "pow2";
                this.eager = true;
                break;
            }
            case 6: {
                this.sign = "xⁿ";
                this.name = "apowb";
                this.eager = false;
                break;
            }
            case 7: {
                this.sign = "√x";
                this.name = "sqrt";
                this.eager = true;
                break;
            }
            case 8: {
                this.sign = "n!";
                this.name = "nfact";
                this.eager = true;
                break;
            }
            case 9: {
                this.sign = "C(n, k)";
                this.name = "combnofk1";
                this.eager = false;
                break;
            }
            case 10: {
                this.sign = "%";
                this.name = "percentage";
                this.eager = true;
                break;
            }
        }
    }

    String getSign() {
        return this.sign;
    }

    public int getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public boolean isEager() {
        return eager;
    }

    public String getCustomLabel(String[] textValues) {
        switch (this.type) {
            case 6: {
                return textValues[0].concat("+").concat(textValues[1]);
            }
            case 7: {
                return "√( " + textValues[0] + " )";
            }
            case 8: {
                return textValues[0] + "!";
            }
            case 9: {
                return "C ( ".concat(textValues[0]).concat(", ").concat(textValues[1]).concat(" )");
            }
            case 10: {
                return textValues[0] + "%";
            }
        }
        StringBuilder str = new StringBuilder();
        for (String textValue : textValues) {
            str.append(textValue);
        }
        return str.toString();
    }
}
