package com.cobblemon.mdks.cobblepass.config;

import com.cobblemon.mdks.cobblepass.util.LangManager;

public class TestConfigGeneration {
    public static void main(String[] args) {
        System.out.println("Testing configuration file generation...");
        
        try {
            // Test Config generation
            Config config = new Config();
            config.load();
            System.out.println("✓ Config loaded successfully");
            
            // Test GuiConfig generation
            GuiConfig guiConfig = new GuiConfig();
            guiConfig.load();
            System.out.println("✓ GuiConfig loaded successfully");
            
            // Test LangManager generation
            LangManager.load();
            System.out.println("✓ LangManager loaded successfully");
            
            System.out.println("All configuration files should now be generated!");
            
        } catch (Exception e) {
            System.err.println("Error during configuration generation: " + e.getMessage());
            e.printStackTrace();
        }
    }
}