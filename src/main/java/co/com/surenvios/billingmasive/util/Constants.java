package co.com.surenvios.billingmasive.util;

public final class Constants {

    public static final String PREFIX_NAME_THREAD_PROCESS = "P-%s";
    public static final String PREFIX_NAME_THREAD_REPROCESS = "R-%s";
    public static final String FACTURA_VENTA = "FV";
    public static final String NOTA_CREDITO = "CV";
    public static final String NOTA_DEBITO = "DV";

    private Constants() {

    }

    public static String createNameThread(String prefix, String origen) {
        return String.format(prefix, origen);
    }
}
