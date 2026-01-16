package com.pos.main;

import com.formdev.flatlaf.FlatLightLaf; // The "Smooth" theme
import javax.swing.UIManager;
import com.pos.view.LoginFrame;

public class MainApp {
    public static void main(String[] args) {
        try {
            // APPLY THE MODERN LOOK
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception ex) {
            System.err.println("FlatLaf failed to load.");
        }

        // Start the application
        new LoginFrame().setVisible(true);
    }
}
