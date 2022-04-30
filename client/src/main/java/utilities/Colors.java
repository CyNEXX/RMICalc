package utilities;

public enum Colors {
    NORMAL("normal-text"), WARNING("warning-text"), ERROR("error-text"), SYSTEM("warning-text");

    private final String colorName;

    Colors(final String s) {
        this.colorName = s;
    }

    @Override
    public String toString() {
        return colorName;
    }
}