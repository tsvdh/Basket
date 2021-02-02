package common;

import javafx.scene.control.Button;

import java.util.ArrayList;
import java.util.List;

public class ButtonFactory {

    public static Button makeBigButton(String text) {
        Button button = new Button();
        button.setPrefSize(105, 30);
        button.setText(text);
        return button;
    }

    public static Button makeSmallButton(String text) {
        Button button = new Button();
        button.setPrefSize(75, 30);
        button.setText(text);
        return button;
    }

    public static Button makeInvisibleButton() {
        Button filler = makeBigButton("Invisible");
        filler.setVisible(false);

        return filler;
    }
}
