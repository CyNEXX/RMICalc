package controllers;

import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import model.Operation;

import java.util.Arrays;
import java.util.List;

public class LogScreenController {
    public static void updateScreenWithStyle(TextFlow screen, String[] texts, List<String[]> allStyles) {
        if (texts.length > 0) {
            for (int i = 0; i < texts.length; i++) {
                String textToStyle = texts[i] + (i == (texts.length - 1) ? "" : " ");
                Text textEl = new Text(textToStyle);
                if (allStyles == null) {
                    textEl.getStyleClass().add(Colors.NORMAL.toString());
                } else {
                    if (allStyles.size() == 0) {
                        textEl.getStyleClass().add(Colors.NORMAL.toString());
                    } else {
                        List<String> specificStyles = Arrays.asList(allStyles.get(i));
                        if (specificStyles.size() == 0) {
                            textEl.getStyleClass().add(Colors.NORMAL.toString());
                        } else {
                            specificStyles.forEach(style -> {
                                if (style.equals("sup")) {
                                    textEl.setTranslateY(textEl.getFont().getSize() * -0.3);
                                } else if (style.equals("sub")) {
                                    textEl.setTranslateY(textEl.getFont().getSize() * 0.3);
                                } else {
                                    textEl.getStyleClass().add(style);
                                }
                            });
                        }
                    }
                }
                screen.getChildren().add(textEl);
            }
            screen.getChildren().add(new Text("\n"));
        }
    }

    public static void updateCustomLogScreen(TextFlow t, String s, List<String[]> aS) {
        System.out.println("Custom display");
        String[] tokens = s.split(" ");
        updateScreenWithStyle(t, tokens, aS);
    }
}
