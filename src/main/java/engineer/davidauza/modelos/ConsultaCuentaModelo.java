package engineer.davidauza.modelos;

/**
 * Esta clase representa una consulta de cuenta.
 */
public class ConsultaCuentaModelo {

    /**
     * Almacena el número de la cuenta que se va a consultar.
     */
    private long mNumeroCuenta;

    /**
     * Este método retorna el número de cuenta que se va a consultar.
     *
     * @return el número de cuenta que se va a consultar.
     */
    public long getNumeroCuenta() {
        return mNumeroCuenta;
    }

    /**
     * Este método fija el número de cuenta que se va a consultar.
     *
     * @param mNumeroCuenta es el número de cuenta que se va a consultar.
     */
    public void setNumeroCuenta(long mNumeroCuenta) {
        this.mNumeroCuenta = mNumeroCuenta;
    }
}
