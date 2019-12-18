package engineer.davidauza.modelos;

/**
 * Esta clase representa un retiro.
 */
public class RetiroModelo {

    /**
     * Almacena el número de cuenta del Retiro.
     */
    private long mNumeroCuenta;

    /**
     * Almacena el monto a retirar de la cuenta del Cliente.
     */
    private long mMontoARetirar;

    /**
     * Este método retorna el número de cuenta almacenado en el Retiro.
     *
     * @return el número de cuenta almacenado en el Retiro.
     */
    public long getNumeroCuenta() {
        return mNumeroCuenta;
    }

    /**
     * Este método fija el número de cuenta de la cual se va a realizar el Retiro.
     *
     * @param mNumeroCuenta es el número de cuenta del cual se va a realizar el Retiro.
     */
    public void setNumeroCuenta(long mNumeroCuenta) {
        this.mNumeroCuenta = mNumeroCuenta;
    }

    /**
     * Este método retorna el monto a retirar de la cuenta del Cliente.
     *
     * @return el monto a retirar de la cuenta del Cliente.
     */
    public long getMontoARetirar() {
        return mMontoARetirar;
    }

    /**
     * Este método fija el monto a retirar de la cuenta del Cliente.
     *
     * @param mMontoARetirar es el monto a retirar de la cuenta del Cliente.
     */
    public void setMontoARetirar(long mMontoARetirar) {
        this.mMontoARetirar = mMontoARetirar;
    }
}
