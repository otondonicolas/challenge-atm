package com.challenge.atm.cli;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

public final class FormatoMoneda {

    private FormatoMoneda() {
    }

    public static String formatear(BigDecimal importe) {
        DecimalFormatSymbols simbolos = new DecimalFormatSymbols();
        simbolos.setDecimalSeparator(',');
        simbolos.setMinusSign('-');
        DecimalFormat formato = new DecimalFormat("0.00", simbolos);
        formato.setGroupingUsed(false);
        formato.setRoundingMode(RoundingMode.HALF_EVEN);
        return "$ " + formato.format(importe);
    }
}
