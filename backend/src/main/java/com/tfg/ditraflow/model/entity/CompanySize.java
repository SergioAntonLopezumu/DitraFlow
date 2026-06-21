package com.tfg.ditraflow.model.entity;

/**
 * Enumeración para clasificar el tamaño de las empresas
 * Basado en criterios europeos: número de empleados e ingresos
 */
public enum CompanySize {
    MICROENTERPRISE("Microempresa", "1-9 empleados", 1.2), // Multiplicador de urgencia para TOPSIS
    SMALL("Pequeña", "10-49 empleados", 1.0),
    MEDIUM("Mediana", "50-249 empleados", 0.9),
    LARGE("Grande", "250+ empleados", 0.8);

    private final String displayName;
    private final String description;
    private final double topsisWeightMultiplier; // Factor de peso para TOPSIS

    CompanySize(String displayName, String description, double topsisWeightMultiplier) {
        this.displayName = displayName;
        this.description = description;
        this.topsisWeightMultiplier = topsisWeightMultiplier;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public double getTopsisWeightMultiplier() {
        return topsisWeightMultiplier;
    }
}
