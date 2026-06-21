package com.tfg.ditraflow.model.entity;

/**
 * Enumeración para clasificar el sector industrial de las empresas
 * Basado en la Clasificación Nacional de Actividades Económicas (CNAE)
 */
public enum IndustrySector {
    A("A", "Agricultura, ganadería, silvicultura y pesca"),
    B("B", "Industrias extractivas"),
    C("C", "Industria manufacturera"),
    D("D", "Suministro de energía eléctrica, gas, vapor y aire acondicionado"),
    E("E", "Suministro de agua, actividades de saneamiento, gestión de residuos y descontaminación"),
    F("F", "Construcción"),
    G("G", "Comercio al por mayor y al por menor; reparación de vehículos"),
    H("H", "Transporte y almacenamiento"),
    I("I", "Hostelería"),
    J("J", "Información y comunicaciones"),
    K("K", "Actividades financieras y de seguros"),
    L("L", "Actividades inmobiliarias"),
    M("M", "Actividades profesionales, científicas y técnicas"),
    N("N", "Actividades administrativas y servicios auxiliares"),
    O("O", "Administración Pública y defensa; Seguridad Social obligatoria"),
    P("P", "Educación"),
    Q("Q", "Actividades sanitarias y de servicios sociales"),
    R("R", "Actividades artísticas, recreativas y de entretenimiento"),
    S("S", "Otros servicios"),
    T("T", "Actividades de los hogares como empleadores de personal doméstico"),
    U("U", "Actividades de organizaciones y organismos extraterritoriales");

    private final String code;
    private final String displayName;

    IndustrySector(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }

    public String getCode() {
        return code;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static IndustrySector fromCode(String code) {
        for (IndustrySector sector : values()) {
            if (sector.code.equalsIgnoreCase(code)) {
                return sector;
            }
        }
        throw new IllegalArgumentException("Sector no válido: " + code);
    }
}
