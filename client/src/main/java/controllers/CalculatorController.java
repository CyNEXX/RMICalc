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

    private final BooleanProperty optionsDisabledProperty = new SimpleBooleanProperty();
    private final BooleanProperty operationsDisabledProperty = new SimpleBooleanProperty();

    private final IntegerProperty appStatus = new SimpleIntegerProperty();
    private final StringProperty calcInputTextProp = new SimpleStringProperty();

    private final BooleanProperty memoryButtonsDisabledProp = new SimpleBooleanProperty();
    private final BooleanProperty connectButtonDisabled = new SimpleBooleanProperty();
    private final BooleanProperty portFocusedProperty = new SimpleBooleanProperty();

    private static boolean newInput = false;
    private static List<Button> buttonsWithKeys;

    private Operation eagerOp;
    private Operation currentOp;
    private OperationsManager opManager;

    private static Double memoryNumber = 0.0;

    ClientStatuses cs = OFFLINE;
    ClientToServerConnection c;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("Initializing...");
        Font.loadFont(Objects.requireNonNull(getClass().getClassLoader().getResource("fonts/software_tester_7.ttf")).toExternalForm(), 10);

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
                    Button digitButton = buttonsWithKeys.stream().filter((b) -> b.getId().equals("buttonDigit" + Integer.parseInt(event.getText()))).findFirst().get();
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

            setOptionsDisabled((int) newValue != 0);
            setConnectButtonDisabled((int) newValue != 2);
            setOperationsDisabled((int) newValue != 1);
        });

        currentOp = new Operation();
        updateStatus(cs.ordinal());

        /**
         * Create an array of buttons so they can be mass manipulated
         */
        Button[] buttonArray = {buttonPercentage, buttonCe, buttonC, buttonBkspc, buttonAPowB, buttonXpow2,
                buttonSqrt, buttonDivide, buttonDigit7, buttonDigit8, buttonDigit9, buttonMultiply, buttonDigit4,
                buttonDigit5, buttonDigit6, buttonSubstract, buttonDigit1, buttonDigit2, buttonDigit3, buttonAdd,
                buttonChangeSign, buttonDigit0, buttonDot, buttonEquals, buttonMemClear, buttonMemRecall, buttonMemAdd,
                buttonMemSubstract, buttonMemStore, buttonCnk, buttonNFact};

        buttonsWithKeys = Arrays.asList(buttonArray);

        /**
         * Set specific behaviour for each button in the list
         */
        buttonsWithKeys.forEach((button) -> {
            String tempID = button.getId();
            String[] memButtons = tempID.split("Mem");
            if (memButtons.length > 1) {
                String specificMemButton = memButtons[1].toLowerCase();
                if (!specificMemButton.equals("store") && !specificMemButton.equals("add") && !specificMemButton.equals("substract")) {
                    button.disableProperty().bind(this.memoryButtonsDisabledProp().or(this.operationsDisabledProperty()));
                } else {
                    button.disableProperty().bind(this.operationsDisabledProperty());
                }
            } else {
                button.disableProperty().bind(this.operationsDisabledProperty());
            }

            button.setOnAction(event -> {

                /**
                 * Adds an action for each type of button based on its ID
                 */
                        String buttonID = ((Button) event.getSource()).getId().toLowerCase();
                        if (buttonID.contains("digit")) {
                            String pressed = buttonID.split("digit")[1];
                            digitAction(Integer.parseInt(pressed));
                        } else if (buttonID.contains("mem")) {
                            String pressed = buttonID.split("mem")[1];
                            switch (pressed) {
                                case "add": {
                                    memoryAddAction();
                                    break;
                                }
                                case "substract": {
                                    memorySubstractAction();
                                    break;
                                }
                                case "store": {
                                    memoryStoreAction();
                                    break;
                                }
                                case "clear": {
                                    memoryClearAction();
                                    break;
                                }
                                case "recall": {
                                    memoryRecallAction();
                                    break;
                                }
                            }
                            newInput = true;
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
                                    processEqualsOperation();
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

        /**
         * Sets some initial settings for the fields
         */

        port.setFocusTraversable(false);
        address.setFocusTraversable(false);
        setConnectButtonDisabled(cs.ordinal() != 2);
        setOperationsDisabled(cs.ordinal() != 1);
        setIsValidAddress(validate(getAddress(), new String[]{"ipPattern"}));
        setIsValidPort(validate(getPort(), new String[]{"portPattern"}));
        setMemoryButtonsDisabled(true);
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
                            /*operation.setLocked(true);*/
                            currentOp.setLocked(true);
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

    private void setConnectButtonDisabled(boolean value) {
        this.connectButtonDisabled.set(value);
    }

    /**
     * Updates status based on the passed int. Also updates client status
     * @param value
     */
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

    private BooleanProperty isValidAddressProperty() {
        return this.isValidAddressProperty;
    }

    private void setIsValidAddress(boolean value) {
        this.isValidAddressProperty.set(value);
    }

    private BooleanProperty isValidPortProperty() {
        return this.isValidPortProperty;
    }

    private void setIsValidPort(boolean value) {
        this.isValidPortProperty.set(value);
    }

    private BooleanProperty optionsEnabledProperty() {
        return this.optionsDisabledProperty;
    }

    private void setOptionsDisabled(boolean value) {
        this.optionsDisabledProperty.set(value);
    }

    private BooleanProperty operationsDisabledProperty() {
        return this.operationsDisabledProperty;
    }

    private void setOperationsDisabled(boolean value) {
        this.operationsDisabledProperty.set(value);
    }

    private String getCalcInputText() {
        return this.calcInputTextProp.get();
    }

    private void setCalcInputText(String value) {
        this.calcInputTextProp.set(value);
    }

    private BooleanProperty memoryButtonsDisabledProp() {
        return this.memoryButtonsDisabledProp;
    }

    private void setMemoryButtonsDisabled(boolean value) {
        memoryButtonsDisabledProp.set(value);
    }

    private void reset(boolean all) {
        if (all) {
            newInput = false;
            try {
                eagerOp.reset();
            } catch (NullPointerException ignored) {
            }
            try {
                currentOp.reset();
            } catch (NullPointerException ignored) {
            }
        }
        setCalcInputText("0");
    }

    private void updateLogScreen(String appendedText, String style) {
        Text t = new Text(appendedText + "\n");
        if (style != null && !style.equals("")) {
            t.getStyleClass().add(style);
        } else t.getStyleClass().add(Colors.NORMAL.toString());
        logScreenTextFlow.getChildren().add(t);
    }

    private void digitAction(int a) {
        if (getCalcInputText().length() < 23) {
            if (newInput || getCalcInputText().equals("0")) {
                setCalcInputText(trimNumberIfPossible(a).toString());
            } else {
                setCalcInputText(trimNumberIfPossible(Double.parseDouble(getCalcInputText() + a)).toString());
            }
            if (newInput) {
                currentOp.setResolvable(true);
                newInput = false;
            }
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
        if (getCalcInputText().length() < 23) {
            double a = Double.parseDouble(getCalcInputText());
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
        memoryNumber = null;
        setMemoryButtonsDisabled(true);
    }

    private void memoryStoreAction() {
        memoryNumber = getDoubleFromInput();
        setMemoryButtonsDisabled(false);
    }

    private void memoryAddAction() {
        if (memoryNumber == null) {
            memoryNumber = 0.0;
        }
        memoryNumber = memoryNumber + getDoubleFromInput();
        setMemoryButtonsDisabled(false);
    }

    private void memorySubstractAction() {
        if (memoryNumber == null) {
            memoryNumber = 0.0;
        }
        memoryNumber = memoryNumber - getDoubleFromInput();
        setMemoryButtonsDisabled(false);
    }

    private void memoryRecallAction() {
        if (memoryNumber != null) {
            System.out.println(memoryNumber);
            setCalcInputText(trimIfPossible(memoryNumber).toString());
        }
    }

    private void makeOperation(OperationTypes opType) {
        if (opType.getName().equals("none")) return;
        try {
            if (opType.isEager()) {
                eagerOp = new Operation();
                eagerOp.setX(getDoubleFromInput());
                eagerOp.setType(opType);
                eagerOp.setXLabel(opType.getCustomLabel(new String[]{trimIfPossible(eagerOp.getX()).toString()}));
                eagerOp.setResult(opManager.resolve(eagerOp));
                updateCustomLogScreen(logScreenTextFlow, eagerOp.toString(), new ArrayList<>());
                setCalcInputText(eagerOp.getResultLabel());
            } else {
                if (currentOp.isResolvable()) {
                    currentOp.setY(getDoubleFromInput());
                    currentOp.setResult(opManager.resolve(currentOp));
                    updateCustomLogScreen(logScreenTextFlow, currentOp.toString(), new ArrayList<>());
                    setCalcInputText(currentOp.getResultLabel());
                    currentOp.setX(currentOp.getResult());
                    currentOp.setY(null);
                    currentOp.setResolvable(false);
                } else {
                    currentOp.setX(getDoubleFromInput());
                    currentOp.setY(null);
                }
                currentOp.setType(opType);
            }
            newInput = true;
        } catch (RemoteException e) {
            forceDisconnect();
        }
    }

    public void processEqualsOperation() {
        try {
            if (!currentOp.hasType() || currentOp.isEager()) {
                return;
            }
            if (!currentOp.hasY()) {
                currentOp.setY(getDoubleFromInput());
            } else {
                currentOp.setX(getDoubleFromInput());
            }
            currentOp.setResult(opManager.resolve(currentOp));
            currentOp.setResolvable(false);
            updateCustomLogScreen(logScreenTextFlow, currentOp.toString(), new ArrayList<>());
            setCalcInputText(currentOp.getResultLabel());
        } catch (RemoteException e) {
            forceDisconnect();
        }
    }

    /**
     *  Forces a click on the ConnectToggle button to unselect it thus to disconnect
     */
    private void forceDisconnect() {
        System.err.println("Connection lost. Please reconnect.");
        updateLogScreen("Connection lost. Please reconnect.", Colors.ERROR.toString());
        if (toggleButtonConnect.isSelected()) {
            toggleButtonConnect.fire();
        }
    }

    private Double getDoubleFromInput() {
        return parseInputNumber(getCalcInputText());
    }
}



