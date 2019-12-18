package engineer.davidauza.sockets;

import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.sql.*;

/**
 * Esta clase contiene el socket que actúa como servidor en el simulador de transacciones.
 */
public class ServidorSocket {

    /**
     * Esta constante contiene el número de puerto por el cual escucha el socket server.
     */
    private static final int PORT = 4444;

    /**
     * enum que contiene los tipos de transacciones que puede realizar el servidor.
     */
    private enum Transaciones {
        CREACION_CLIENTE,
        CONSULTAR_SALDO_CUENTA,
        RETIRAR_CUENTA_CLIENTE
    }

    /**
     * Esta constante contiene la URL para acceder a la base de datos.
     */
    private static final String URL_BASE_DE_DATOS =
            "jdbc:mysql://localhost:3306/mydb?autoReconnect=true&useSSL=false";

    /**
     * Esta constante contiene el usuario para acceder a la base de datos.
     */
    private static final String USUARIO_BASE_DE_DATOS = "root";

    /**
     * Esta constante contiene la contraseña para acceder a la base de datos.
     */
    private static final String CONTRASENA_BASE_DE_DATOS = "BancoPoligran";

    /**
     * Esta variable almacena los resultados de la consulta realizada.
     */
    private static String mResultadosConsulta;

    /**
     * Método main de la clase ServidorSocket encargado de ejecutar el programa.
     *
     * @param args parámetro por defecto método main.
     * @throws IOException puede producir un IOException.
     */
    public static void main(String[] args) throws IOException {
        // Contiene el puerto en el que se escuchan las peticiones.
        ServerSocket socketServidor = null;
        try {
            socketServidor = new ServerSocket(PORT);
        } catch (IOException e) {
            System.out.println("No es posible escuchar en el puerto: " + PORT);
            System.exit(-1);
        }

        Socket socketCliente = null;
        BufferedReader entrada = null;
        PrintWriter salida = null;

        System.out.println("\nEscuchando: " + socketServidor);
        try {
            // Bloqueo hasta recibir la petición del cliente, abriendo un socket para este.
            socketCliente = socketServidor.accept();
            System.out.println("\nConexión aceptada: " + socketCliente);
            // Se establece el canal de entrada
            entrada = new BufferedReader(new InputStreamReader(socketCliente.getInputStream()));
            // Se establece el canal de salida
            salida = new PrintWriter(new BufferedWriter(
                    new OutputStreamWriter(socketCliente.getOutputStream())), true);

            String mensajeCliente = entrada.readLine();
            System.out.println("\nMensaje Cliente: " + mensajeCliente);
            salida.println("Mensaje recibido.");

            HashMap<String, String> hashMensaje = stringAHashMap(mensajeCliente);

            String codigoTransaccion = (String) hashMensaje.get("transaccion_id");

            accederBaseDeDatos(codigoTransaccion, hashMensaje);

            String mensajeConfirmacion = "Transacción realizada: " + codigoTransaccion + ": " +
                    mensajeCliente;

            System.out.println("\nEnviando mensaje al cliente: \n" + mensajeConfirmacion);

            salida.println(mensajeConfirmacion);

            if (codigoTransaccion.equals(Transaciones.CONSULTAR_SALDO_CUENTA.toString())) {
                System.out.println(mResultadosConsulta.replaceAll("\\*", "\n"));
                salida.println(mResultadosConsulta);
            }

            System.out.println("\nCliente notificado.");
        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        }
        salida.close();
        entrada.close();
        socketCliente.close();
        socketServidor.close();
    }

    /**
     * Este método accede a la base de datos y ejecuta una operación de acuerdo al código de
     * transacción indicado.
     *
     * @param codigoTransaccion es el código de la transacción que se va a realizar.
     * @param hashMensaje       es el HashMap con el mensaje recibido del socket cliente.
     */
    private static void accederBaseDeDatos(String codigoTransaccion,
                                           HashMap<String, String> hashMensaje) {
        try {
            System.out.println("\nEstableciendo conexión con la base de datos...");
            Connection conexion =
                    DriverManager.getConnection(URL_BASE_DE_DATOS, USUARIO_BASE_DE_DATOS,
                            CONTRASENA_BASE_DE_DATOS);
            System.out.println("\nConexión exitosa.");
            Statement sentencia = conexion.createStatement();
            StringBuilder sql = new StringBuilder();
            if (codigoTransaccion.equals(Transaciones.CREACION_CLIENTE.toString())) {
                sql.append("INSERT INTO mydb.cliente").
                        append(" (nombres, apellidos, ciudad_id, direccion, email) VALUES (\'").
                        append(hashMensaje.get("nombres")).append("\', \'").
                        append(hashMensaje.get("apellidos")).append("\', ").
                        append(hashMensaje.get("ciudad_id")).append(", \'").
                        append(hashMensaje.get("direccion")).append("\', \'").
                        append(hashMensaje.get("email")).append("\');");
                sentencia.executeUpdate(sql.toString());
            } else if (codigoTransaccion.equals(Transaciones.CONSULTAR_SALDO_CUENTA.toString())) {
                sql.append("SELECT * FROM mydb.cuenta WHERE numero = ").
                        append(hashMensaje.get("numero_cuenta")).append(";");
                ResultSet setResultados = sentencia.executeQuery(sql.toString());
                while (setResultados.next()) {
                    long numeroCuenta = setResultados.getLong("numero");
                    long idCliente = setResultados.getLong("cliente_id");
                    float saldo = setResultados.getFloat("saldo");
                    Date fechaApertura = setResultados.getDate("fecha_apertura");
                    mResultadosConsulta = "*Resultados Consulta: " +
                            "*Número de Cuenta: " + numeroCuenta +
                            "*ID Cliente: " + idCliente +
                            "*Saldo: " + saldo +
                            "*Fecha de Apertura: " + fechaApertura;
                }
                setResultados.close();
            } else if (codigoTransaccion.equals(Transaciones.RETIRAR_CUENTA_CLIENTE.toString())) {
                sql.append("UPDATE mydb.cuenta SET saldo = saldo - ").
                        append(hashMensaje.get("monto_a_retirar")).append(" WHERE numero = ").
                        append(hashMensaje.get("numero_cuenta")).append(";");
                sentencia.executeUpdate(sql.toString());
            }
            System.out.println("\nComando ingresado a la base de datos: " + sql.toString());
            System.out.println("\nOperación realizada con éxito.");
            sentencia.close();
            conexion.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Este método convierte un String recibido como mensaje del socket cliente en un HashMap.
     *
     * @param str es el String recibido como mensaje del socket cliente.
     * @return el HashMap producto de la transformación.
     */
    private static HashMap<String, String> stringAHashMap(String str) {
        HashMap<String, String> hash = new HashMap<String, String>();
        // Eliminar las llaves del String
        str = str.replaceAll("[{}]", "");
        // Eliminar los espacios después de las comas
        str = str.replaceAll(",\\s+", ",");
        String[] pares = str.split(",");
        for (int i = 0; i < pares.length; i++) {
            String par = pares[i];
            String[] keyValue = par.split("=");
            hash.put(keyValue[0], keyValue[1]);
        }
        return hash;
    }
}
