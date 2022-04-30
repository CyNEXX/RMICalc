import launcher.UILauncher;

import static launcher.UILauncher.launch;

/**
 * The main class that launches the specified launcher.
 */
public class ClientMain {
    public static void main(String... args) {
        launch(UILauncher.class);
    }
}
