package model;

import java.util.Locale;

import static utilities.Validation.validate;
import static utilities.Manipulation.*;

public class Operation {

    private Double x;
    private Double y;
    private Double result;
    private String xLabel;
    private String yLabel;
    private String resultLabel;
    private boolean locked;

    private boolean unstartedOperation = true;
    private OperationTypes type;


    public Operation() {
        this.type = OperationTypes.values()[0];
    }

    public Operation(Integer type) {
        this.type = OperationTypes.values()[type + 1];
    }

    public Double getX() {
        return x;
    }

    public void setX(Double x) {
        this.x = x;
        this.xLabel = trimAndString(x);
    }

    public Double getY() {
        return y;
    }

    public void setY(Double y) {
        this.y = y;
        this.yLabel = trimAndString(y);
    }

    public String getSign() {
        return type.getSign();
    }

    public String getName() {
        return type.getName();
    }

    public boolean isUnstartedOperation() {
        return unstartedOperation;
    }

    public void setUnstartedOperation(boolean unstartedOperation) {
        this.unstartedOperation = unstartedOperation;
    }

    public String getXLabel() {
        return xLabel;
    }

    public void setXLabel(String xLabel) {
        this.xLabel = xLabel;
    }

    public String getYLabel() {
        return yLabel;
    }

    public void setYLabel(String yLabel) {
        this.yLabel = yLabel;
    }

    public String getResultLabel() {
        return resultLabel;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public void setType(OperationTypes opType) {
        this.type = opType;
    }

    public boolean isEager() {
        return type.isEager();
    }

    public Double getResult() {
        return result;
    }

    public boolean hasX() {
        return x != null;
    }

    public boolean hasY() {
        return y != null;
    }

    public boolean hasResult() {
        return result != null;
    }

    public boolean isResolvable() {
        return !locked;
    }

    public void setResolvable(boolean value) {
        locked = !value;
    }

    public void setResult(Double result) {
        this.result = result;
        this.resultLabel = trimAndString(result);
    }

    public OperationTypes getType() {
        return type;
    }

    public void reset() {
        this.type = OperationTypes.NONE;
        this.x = null;
        this.y = null;
        this.xLabel = null;
        this.yLabel = null;
        this.result = null;
        this.locked = true;
    }

    public boolean hasType() {
        return this.getType() != null && this.getType().getType() != 0;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("");
        if (this.getX() != null || this.getXLabel() != null) {
            sb.append(this.getXLabel() != null ? this.getXLabel() : this.getX());
        }
        if (!this.getType().isEager()) {
            if (this.type != null && this.type.getType() >= 0) {
                sb.append(" ")
                        .append(type.getSign())
                        .append(" ");
            }

            if (this.hasY() || this.getYLabel() != null) {
                sb.append(this.getYLabel() != null ? this.getYLabel() : this.getY());
            }

        }
        if (this.getResult() != null) {
            sb.append(" = ")
                    .append(this.getResultLabel());
        }

        return sb.toString();
    }

}
