package controllers;

import javafx.beans.property.*;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.net.URL;
import java.rmi.RemoteException;
import java.util.*;

import model.Operation;
import model.OperationTypes;
import repository.OperationsManager;
import connection.ClientToServerConnection;

import static controllers.ClientStatuses.*;
import static utilities.Validation.validate;
import static controllers.LogScreenController.updateCustomLogScreen;


import static utilities.Manipulation.*;

enum Colors {
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

public class CalculatorController implements Initializable {

    private static final String default_hostAddress = "127.0.0.1";
    private static final String default_objectName = "Calculator";
    private static final String default_hostPort = "5000";
    private Service<Void> backgroundTask;

    private final StringProperty addressProperty = new SimpleStringProperty();
    private final StringProperty portProperty = new SimpleStringProperty();

    private final BooleanProperty isValidAddressProperty = new SimpleBooleanProperty();
    private final BooleanProperty isValidPortProperty = new SimpleBooleanProperty();

    private final BooleanProperty optionsEnabledProperty = new SimpleBooleanProperty();
    private final BooleanProperty operationsEnabledProperty = new SimpleBooleanProperty();

    private final IntegerProperty appStatus = new SimpleIntegerProperty();
    private final StringProperty calcInputTextProp = new SimpleStringProperty();

    private final BooleanProperty headTextInputsEnabled = new SimpleBooleanProperty();
    private final BooleanProperty connectButtonEnabled = new SimpleBooleanProperty();
    private final BooleanProperty portFocusedProperty = new SimpleBooleanProperty();

    private static boolean newInput = true;
    private static List<Button> buttonsWithKeys;
    private static Double resultSoFar = 0.0;


    private Operation operation;
    private Operation operationPreview;
    private Operation tempEagerOperation;
    private OperationsManager opManager;

    ClientStatuses cs = OFFLINE;
    ClientToServerConnection c;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("Initializing...");
        Font.loadFont(getClass().getClassLoader().getResource("fonts/software_tester_7.ttf").toExternalForm(), 10);

        this.port.disableProperty().bind(optionsEnabledProperty());
        this.port.textProperty().bindBidirectional(portProperty());

        this.address.disableProperty().bind(optionsEnabledProperty());
        this.address.textProperty().bindBidirectional(addressProperty());

        this.setAddress(default_hostAddress);
        this.setPort(default_hostPort);

        this.calcInput.textProperty().bindBidirectional(calcInputTextProp());
        this.scrollableContent.vvalueProperty().bind(logScreenTextFlow.heightProperty());
        this.calcInput.setEditable(false);
        calcInput.setText("0");

        toggleButtonConnect.disableProperty().bind(isValidAddressProperty().not().or(isValidPortProperty().not()));

        address.setOnKeyReleased(event -> {
            String[] arr = {"ipPattern"};
            boolean res = validate(getAddress(), arr);

            if (res) {
                address.getStyleClass().removeIf(s -> s.equals(Colors.ERROR.toString()));
            } else {
                if (!address.getStyleClass().contains(Colors.ERROR.toString())) {
                    address.getStyleClass().add(Colors.ERROR.toString());
                }
            }
            setIsValidAddress(res);
        });

        port.setOnKeyReleased(event -> {
            String[] arr = {"portPattern"};
            boolean res = validate(getPort(), arr);
            /* res = true;*/
            if (res) {
                port.getStyleClass().removeIf(s -> s.equals(Colors.ERROR.toString()));
            } else {
                if (!port.getStyleClass().contains(Colors.ERROR.toString())) {
                    port.getStyleClass().add(Colors.ERROR.toString());
                }
            }
            setIsValidPort(res);
        });

        mainPane.setOnKeyReleased(event -> {
            if (!address.isFocused() && !address.isFocused()) {
                if (event.getCode().isDigitKey()) {
                    Button digitButton = buttonsWithKeys.stream().filter((b) -> {
                        return b.getId().equals("buttonDigit" + Integer.parseInt(event.getText()));
                    }).findFirst().get();
                    digitButton.fire();
                } else {
                    String typedCode = event.getCode().toString();
                    switch (typedCode) {
                        case "BACK_SPACE": {
                            buttonBkspc.fire();
                            break;
                        }
                        case "DECIMAL": {
                            buttonDot.fire();
                            break;
                        }
                        case "ADD": {
                            buttonAdd.fire();
                            break;
                        }
                        case "SUBTRACT": {
                            buttonSubstract.fire();
                            break;
                        }
                        case "MULTIPLY": {
                            buttonMultiply.fire();
                            break;
                        }
                        case "DIVIDE": {
                            buttonDivide.fire();
                            break;
                        }
                        case "ENTER": {
                            buttonEquals.fire();
                            break;
                        }
                        default: {
                        }
                    }
                }
            }
        });

        appStatus.addListener((ObservableValue<? extends Number> prop,
                               Number oldValue,
                               Number newValue) -> {

            setOptionsEnabled((int) newValue != 0);
            setConnectButtonEnabled((int) newValue != 2);
            setOperationsEnabled((int) newValue != 1);
        });

        operation = new Operation();
        operationPreview = new Operation();
        updateStatus(cs.ordinal());

        Button[] buttonArray = {buttonPercentage, buttonCe, buttonC, buttonBkspc, buttonAPowB, buttonXpow2,
                buttonSqrt, buttonDivide, buttonDigit7, buttonDigit8, buttonDigit9, buttonMultiply, buttonDigit4,
                buttonDigit5, buttonDigit6, buttonSubstract, buttonDigit1, buttonDigit2, buttonDigit3, buttonAdd,
                buttonChangeSign, buttonDigit0, buttonDot, buttonEquals, buttonMemClear, buttonMemRecall, buttonMemAdd,
                buttonMemSubstract, buttonMemStore, buttonCnk, buttonNFact};

        buttonsWithKeys = Arrays.asList(buttonArray);

        buttonsWithKeys.forEach((button) -> {
            button.disableProperty().bind(this.operationsEnabledProperty());
            //max 25 chars!
            button.setOnAction(event -> {

                        String buttonID = ((Button) event.getSource()).getId().toLowerCase();
                        if (buttonID.contains("digit")) {
                            String pressed = buttonID.split("digit")[1];
                            digitAction(Integer.parseInt(pressed));
                        } else if (buttonID.contains("mem")) {
                            String pressed = buttonID.split("mem")[1];
                            System.out.println("Memory command: " + pressed);
                        } else {
                            String pressed = buttonID.split("button")[1];
                            System.out.println("Command: " + pressed);
                            switch (pressed) {
                                case "dot": {
                                    decimalAction();
                                    break;
                                }
                                case "bkspc": {
                                    backSpaceAction();
                                    break;
                                }
                                case "changesign": {
                                    changeSignAction();
                                    break;
                                }
                                case "ce": {
                                    clearEntryAction();
                                    break;
                                }
                                case "c": {
                                    clearAllAction();
                                    break;
                                }
                                case "equals": {
                                    equalsAction();
                                    break;
                                }
                                default: {
                                    operationAction(pressed);
                                }
                            }
                        }
                    }
            );
        });

        port.setFocusTraversable(false);
        address.setFocusTraversable(false);
        setConnectButtonEnabled(cs.ordinal() != 2);
        setOperationsEnabled(cs.ordinal() != 1);
        setIsValidAddress(validate(getAddress(), new String[]{"ipPattern"}));
        setIsValidPort(validate(getPort(), new String[]{"portPattern"}));
        /*operation.setLocked(true);*/
    }

    @FXML
    public TextField port;

    @FXML
    public TextField address;

    @FXML
    public TextFlow logScreenTextFlow;

    @FXML
    public Button buttonPercentage, buttonCe, buttonC, buttonBkspc, buttonAPowB, buttonXpow2,
            buttonSqrt, buttonDivide, buttonDigit7, buttonDigit8, buttonDigit9, buttonMultiply, buttonDigit4,
            buttonDigit5, buttonDigit6, buttonSubstract, buttonDigit1, buttonDigit2, buttonDigit3, buttonAdd,
            buttonChangeSign, buttonDigit0, buttonDot, buttonEquals, buttonMemClear, buttonMemRecall, buttonMemAdd,
            buttonMemSubstract, buttonMemStore, buttonCnk, buttonNFact;

    @FXML
    public ToggleButton toggleButtonConnect;

    @FXML
    public TextField calcInput;

    @FXML
    private ScrollPane scrollableContent;

    @FXML
    private Pane mainPane;

    @FXML
    private void handleToggleConnect() {
        updateStatus(2);
        if (toggleButtonConnect.isSelected()) {
            updateLogScreen("Connecting...", Colors.SYSTEM.toString());
            backgroundTask = new Service<Void>() {
                @Override
                protected Task<Void> createTask() {
                    return new Task<Void>() {
                        @Override
                        protected Void call() throws Exception {
                            c = new ClientToServerConnection(getAddress(), Integer.parseInt(getPort()), default_objectName);
                            opManager = new OperationsManager(c);
                            operation.setLocked(true);
                            return null;
                        }
                    };
                }
            };
            backgroundTask.setOnSucceeded(event -> {
                updateStatus(1);
                updateLogScreen("Connected!", Colors.SYSTEM.toString());
            });

            backgroundTask.setOnFailed(event -> {
                updateLogScreen("Failed to connect. Check IP, port and if server is online", Colors.ERROR.toString());
                updateStatus(0);
                toggleButtonConnect.setSelected(false);
            });
            backgroundTask.start();
        } else {
            try {
                disconnect();
            } catch (NullPointerException ignored) {
            } finally {
                updateStatus(0);
                updateLogScreen("Disconnected", Colors.SYSTEM.toString());
            }
        }
    }

    private void disconnect() {
        backgroundTask.cancel();
        this.c.close();
        this.c = null;
    }

    private BooleanProperty connectButtonEnabled() {
        return this.connectButtonEnabled;
    }

    private void setConnectButtonEnabled(boolean value) {
        this.connectButtonEnabled.set(value);
    }

    private void updateStatus(int value) {
        updateToggleButtonStyle(toggleButtonConnect, value);
        this.appStatus.setValue(value);
        this.cs = ClientStatuses.getClientStatus(value);
    }

    private void updateToggleButtonStyle(ToggleButton b, int clientStatus) {
        b.setText(clientStatus == 0 ? "↔" : (clientStatus == 1) ? "X" : "...");
        b.getStyleClass().removeIf(style -> style.equals(Colors.values()[0].toString()) || style.equals(Colors.values()[1].toString()) || style.equals(Colors.values()[2].toString()));
        String styleClass = Colors.values()[0].toString();
        if (clientStatus == 1) {
            styleClass = Colors.values()[2].toString();
        } else if (clientStatus == 2) {
            styleClass = Colors.values()[1].toString();
        }
        b.getStyleClass().add(styleClass);
    }

    private StringProperty addressProperty() {
        return this.addressProperty;
    }

    private void setAddress(String text) {
        this.addressProperty.set(text);
    }

    private String getAddress() {
        return this.addressProperty.get();
    }

    private StringProperty portProperty() {
        return this.portProperty;
    }

    private void setPort(String text) {
        this.portProperty.set(text);
    }

    private String getPort() {
        return this.portProperty.get();
    }

    private StringProperty calcInputTextProp() {
        return this.calcInputTextProp;
    }

    private BooleanProperty portFocusedProperty() {
        return this.portFocusedProperty;
    }

    private void setPortFocused(boolean value) {
        portFocusedProperty.set(value);
    }

    private BooleanProperty isValidAddressProperty() {
        return this.isValidAddressProperty;
    }

    private boolean getIsValidAddress() {
        return this.isValidAddressProperty.getValue();
    }

    private void setIsValidAddress(boolean value) {
        this.isValidAddressProperty.set(value);
    }

    private BooleanProperty isValidPortProperty() {
        return this.isValidPortProperty;
    }

    private boolean getIsValidPort() {
        return this.isValidPortProperty.getValue();
    }

    private void setIsValidPort(boolean value) {
        this.isValidPortProperty.set(value);
    }

    private BooleanProperty optionsEnabledProperty() {
        return this.optionsEnabledProperty;
    }

    private boolean getOptionsEnabled() {
        return this.optionsEnabledProperty.get();
    }

    private void setOptionsEnabled(boolean value) {
        this.optionsEnabledProperty.set(value);
    }

    private BooleanProperty operationsEnabledProperty() {
        return this.operationsEnabledProperty;
    }

    private boolean getOperationsEnabled() {
        return this.operationsEnabledProperty.get();
    }

    private void setOperationsEnabled(boolean value) {
        this.operationsEnabledProperty.set(value);
    }

    private String getCalcInputText() {
        return this.calcInputTextProp.get();
    }

    private void setCalcInputText(String value) {
        this.calcInputTextProp.set(value);
    }

    private void resetCalcInputText() {
        setCalcInputText("");
    }

    private void reset(boolean all) {
        if (all) {
            resultSoFar = 0.0;
            operation.reset();
            operationPreview.reset();
            operation.setX(0.0);
            operation.setType(OperationTypes.ADD);
            operationPreview.setX(0.0);

            setCalcInputText("0");
        }
        newInput = true;
        setCalcInputText("0");
    }

    private void updateLogScreen(String appendedText, String style) {
        Text t = new Text(appendedText + "\n");
        if (style != null && !style.equals("")) {
            t.getStyleClass().add(style);
        } else t.getStyleClass().add(Colors.NORMAL.toString());
        logScreenTextFlow.getChildren().add(t);
    }


    private void equalsAction() {
        try {
            if (operation.hasX()) {
                if (!operation.hasY() || !operation.isLocked()) {
                    operation.setY(parseInputNumber(getCalcInputText()));
                    System.out.println(">> " + operation.getName());
                    if(operation.getName() == null || Objects.equals(operation.getName(), "")) {
                        operation.setResult(opManager.resolve(tempEagerOperation));
                    } else {
                        operation.setResult(opManager.resolve(operation));
                    }

                    if (operationPreview.getYLabel() == null) {
                        operationPreview.setYLabel(trimIfPossible(getCalcInputText()));
                    }
                    operationPreview.setResult(operation.getResult());
                } else {
                    operation.setX(parseInputNumber(getCalcInputText()));

                    operationPreview.setXLabel(trimIfPossible(parseInputNumber(getCalcInputText()).toString()));
                    if(operation.getName() == null || Objects.equals(operation.getName(), "")) {
                        operation.setResult(opManager.resolve(tempEagerOperation));
                    } else {
                        operation.setResult(opManager.resolve(operation));
                    }
                    operationPreview.setResult(operation.getResult());
                }
                /*operationPreview.setResult(operation.getResult());*/
                updateLogScreen(operationPreview.toString(), Colors.NORMAL.toString());
                setCalcInputText(trimIfPossible(operation.getResult()).toString());
            }
            operation.setLocked(true);
            newInput = true;
        } catch (RemoteException e) {
            forceDisconnect();
        }
    }

    private void digitAction(int a) {
        if (getCalcInputText().length() < 23) {
            /*       updateCalcInputText(String.valueOf(a));*/
            if (newInput || getCalcInputText().equals("0")) {
                setCalcInputText(trimNumberIfPossible(a).toString());
            } else {
                setCalcInputText(trimNumberIfPossible(Double.parseDouble(getCalcInputText() + a)).toString());
            }

            operation.setLocked(false);
            newInput = false;
        }
    }

    private void operationAction(String type) {
        switch (type) {
            case "add": {
                makeOperation(OperationTypes.ADD);
                break;
            }
            case "substract": {
                makeOperation(OperationTypes.SUBSTRACT);
                break;
            }
            case "multiply": {
                makeOperation(OperationTypes.MULTIPLY);
                break;
            }
            case "divide": {
                makeOperation(OperationTypes.DIVIDE);
                break;
            }
            case "xpow2": {
                makeOperation(OperationTypes.POW2);
                break;
            }
            case "apowb": {
                makeOperation(OperationTypes.A_POW_B);
                break;
            }
            case "cnk": {
                makeOperation(OperationTypes.COMB_N_OF_K);
                break;
            }
            case "nfact": {
                makeOperation(OperationTypes.N_FACT);
                break;
            }
            case "sqrt": {
                makeOperation(OperationTypes.SQRT);
                break;
            }
            case "percentage": {
                makeOperation(OperationTypes.PERCENTAGE);
                break;
            }
        }
    }

    private void decimalAction() {
        if (validate(getCalcInputText(), new String[]{"noPeriod"}))
            setCalcInputText(getCalcInputText() + ".");
    }

    private void backSpaceAction() {
        String currentContent = getCalcInputText();
        if (currentContent.length() == 1) {
            setCalcInputText("0");
        } else {
            setCalcInputText(getCalcInputText().substring(0, getCalcInputText().length() - 1));
        }
    }

    private void changeSignAction() {
        /*        String result = "";*/
        if (getCalcInputText().length() < 23) {
            double a = Double.parseDouble(getCalcInputText());
            /*            result = String.valueOf(a * (-1));*/
            setCalcInputText(trimNumberIfPossible(a * -1).toString());
        }
    }

    private void clearEntryAction() {
        reset(false);
    }

    private void clearAllAction() {
        reset(true);
    }

    private void memoryClearAction() {
        System.out.println("memoryClearAction not implemented yet");
    }

    private void memoryStoreAction() {
        System.out.println("memoryStoreAction not implemented yet");
    }

    private void memoryAddAction() {
        System.out.println("memoryAddAction not implemented yet");
    }

    private void memorySubstractAction() {
        System.out.println("memorySubstractAction not implemented yet");
    }

    private void memoryRecallAction() {
        System.out.println("memoryRecallAction not implemented yet");
    }

    private void combinationsAction() {
        System.out.println("combinationsAction not implemented yet");
    }



    private void updateCalcInputText(String s) {
        String actualContent = getCalcInputText();
        if (actualContent.equals("")) setCalcInputText(s);
        else
            try {
                Double result = Double.parseDouble(actualContent);
                if (actualContent.equals("0")) {
                    setCalcInputText(s);
                } else {
                    setCalcInputText(actualContent + s);
                }
            } catch (NumberFormatException nfe) {
                System.out.println("Could not parse that number");
            }
    }

    private void makeOperation(OperationTypes opType) {
        try {
            if (!opType.isEager()) {
                if (!operation.isLocked() && operation.hasX()) {
                    operation.setY(parseInputNumber(getCalcInputText()));
                    operation.setYLabel(trimNumberIfPossible(parseInputNumber(getCalcInputText())).toString());
                    Double result = opManager.resolve(operation);
                    operation.setResult(result);
                    updateCustomLogScreen(logScreenTextFlow, operation.toString(), null);
                    operationPreview.reset();
                    operationPreview.setXLabel(operation.getResultLabel());
                    resultSoFar = operation.getResult();
                } else {
                    operation.setX(parseInputNumber(getCalcInputText()));
                    operation.setXLabel(trimIfPossible(parseInputNumber(getCalcInputText())).toString());
                    if (operation.hasResult()) {
                        operationPreview.reset();
                    }
                    operationPreview.setY(null);
                    operationPreview.setYLabel(null);
                    operationPreview.setXLabel(trimIfPossible(operationPreview.getXLabel() != null ? operationPreview.getXLabel() : operation.getX().toString()));
                    resultSoFar = operation.getX();
                }
                operation.setX(resultSoFar);
                operation.setY(null);
                operation.setXLabel(trimIfPossible(resultSoFar).toString());
                operation.setLocked(true);
                operation.setType(opType);
                operationPreview.setType(opType);

            } else {
                System.out.println("Eager operation");
                tempEagerOperation = new Operation();
                tempEagerOperation.setX(parseInputNumber(getCalcInputText()));

                tempEagerOperation.setType(opType);
                tempEagerOperation.setResult(opManager.resolve(tempEagerOperation));
                tempEagerOperation.setXLabel(tempEagerOperation.getType().getCustomLabel(trimIfPossible(tempEagerOperation.getX()).toString().split(" ")));
                if (!operation.hasX()) {
                    System.out.println("B1");
                    operationPreview.setXLabel(tempEagerOperation.getXLabel());
                    operationPreview.setX(tempEagerOperation.getResult());
                    operation.setX(tempEagerOperation.getResult());
                    operation.setLocked(true);
                } else {
                    System.out.println("B2");
                    operationPreview.setYLabel(tempEagerOperation.getXLabel());
                    operation.setY(tempEagerOperation.getResult());
                }
                updateCustomLogScreen(logScreenTextFlow, tempEagerOperation.toString(), null);
                resultSoFar = tempEagerOperation.getResult();
            }

            newInput = true;
            setCalcInputText(trimNumberIfPossible(resultSoFar).toString());
        } catch (RemoteException re) {
            forceDisconnect();
        }
    }

    private void forceDisconnect() {
        System.err.println("Connection lost. Please reconnect.");
        updateLogScreen("Connection lost. Please reconnect.", Colors.ERROR.toString());
        if (toggleButtonConnect.isSelected()) {
            toggleButtonConnect.fire();
        }
    }
}



