package com.cobblemon.mdks.cobblepass.util;

import com.cobblemon.mdks.cobblepass.CobblePass;

import java.math.BigDecimal;
import java.util.UUID;

public class EconomyUtils {
    private static Boolean economyAvailable = null;
    private static Object economyService = null;
    
    /**
     * Safely checks if the economy system is available without throwing exceptions.
     * Uses reflection to avoid static initialization issues.
     */
    public static boolean isEconomyAvailable() {
        if (economyAvailable == null) {
            try {
                // Check if the classes exist
                Class<?> impactorClass = Class.forName("net.impactdev.impactor.api.Impactor");
                Class<?> serviceProviderClass = Class.forName("net.impactdev.impactor.api.ImpactorServiceProvider");
                Class<?> economyServiceClass = Class.forName("net.impactdev.impactor.api.economy.EconomyService");
                
                // Try to get the service provider
                Object serviceProvider = serviceProviderClass.getMethod("get").invoke(null);
                if (serviceProvider == null) {
                    throw new IllegalStateException("Service provider is null");
                }
                
                // Try to get the economy service
                economyService = economyServiceClass.getMethod("instance").invoke(null);
                if (economyService == null) {
                    throw new IllegalStateException("Economy service is null");
                }
                
                economyAvailable = true;
                CobblePass.LOGGER.info("Economy integration enabled - Impactor detected and initialized");
            } catch (Exception e) {
                economyAvailable = false;
                CobblePass.LOGGER.warn("Economy integration disabled - Impactor not available or not initialized: " + e.getMessage());
            }
        }
        return economyAvailable;
    }

    public static boolean hasBalance(UUID uuid, double amount) {
        if (!isEconomyAvailable()) {
            return false;
        }
        
        try {
            Class<?> economyServiceClass = economyService.getClass();
            
            // Get account future
            Object accountFuture = economyServiceClass.getMethod("account", UUID.class)
                    .invoke(economyService, uuid);
            
            // Get the actual account
            Object account = accountFuture.getClass().getMethod("join").invoke(accountFuture);
            
            // Get balance
            Object balance = account.getClass().getMethod("balance").invoke(account);
            
            // Compare with requested amount
            return ((BigDecimal) balance).compareTo(BigDecimal.valueOf(amount)) >= 0;
        } catch (Exception e) {
            CobblePass.LOGGER.error("Failed to check balance for player " + uuid + ": " + e.getMessage());
            return false;
        }
    }

    public static boolean withdraw(UUID uuid, double amount) {
        if (!isEconomyAvailable()) {
            return false;
        }
        
        try {
            Class<?> economyServiceClass = economyService.getClass();
            
            // Get account future
            Object accountFuture = economyServiceClass.getMethod("account", UUID.class)
                    .invoke(economyService, uuid);
            
            // Get the actual account
            Object account = accountFuture.getClass().getMethod("join").invoke(accountFuture);
            
            // Perform withdrawal
            Object transaction = account.getClass().getMethod("withdraw", BigDecimal.class)
                    .invoke(account, BigDecimal.valueOf(amount));
            
            // Check if successful
            return (Boolean) transaction.getClass().getMethod("successful").invoke(transaction);
        } catch (Exception e) {
            CobblePass.LOGGER.error("Failed to withdraw " + amount + " from player " + uuid + ": " + e.getMessage());
            return false;
        }
    }
    
    public static boolean deposit(UUID uuid, double amount) {
        if (!isEconomyAvailable()) {
            return false;
        }
        
        try {
            Class<?> economyServiceClass = economyService.getClass();
            
            // Get account future
            Object accountFuture = economyServiceClass.getMethod("account", UUID.class)
                    .invoke(economyService, uuid);
            
            // Get the actual account
            Object account = accountFuture.getClass().getMethod("join").invoke(accountFuture);
            
            // Perform deposit
            Object transaction = account.getClass().getMethod("deposit", BigDecimal.class)
                    .invoke(account, BigDecimal.valueOf(amount));
            
            // Check if successful
            return (Boolean) transaction.getClass().getMethod("successful").invoke(transaction);
        } catch (Exception e) {
            CobblePass.LOGGER.error("Failed to deposit " + amount + " to player " + uuid + ": " + e.getMessage());
            return false;
        }
    }

    public static String formatCurrency(double amount) {
        if (!isEconomyAvailable()) {
            return amount + " coins";
        }
        
        try {
            Class<?> economyServiceClass = economyService.getClass();
            
            // Get currencies
            Object currencies = economyServiceClass.getMethod("currencies").invoke(economyService);
            
            // Get primary currency
            Object primaryCurrency = currencies.getClass().getMethod("primary").invoke(currencies);
            
            // Format the amount
            Object component = primaryCurrency.getClass()
                    .getMethod("format", BigDecimal.class)
                    .invoke(primaryCurrency, BigDecimal.valueOf(amount));
            
            // Convert to plain text
            Class<?> serializerClass = Class.forName("net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer");
            Object serializer = serializerClass.getMethod("plainText").invoke(null);
            Class<?> componentClass = Class.forName("net.kyori.adventure.text.Component");
            
            return (String) serializer.getClass().getMethod("serialize", componentClass)
                    .invoke(serializer, component);
        } catch (Exception e) {
            CobblePass.LOGGER.error("Failed to format currency " + amount + ": " + e.getMessage());
            return amount + " coins";
        }
    }
}
