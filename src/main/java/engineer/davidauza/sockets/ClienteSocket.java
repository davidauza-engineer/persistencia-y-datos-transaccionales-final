package engineer.davidauza.sockets;

import engineer.davidauza.modelos.ClienteModelo;
import engineer.davidauza.modelos.ConsultaCuentaModelo;
import engineer.davidauza.modelos.RetiroModelo;

import java.net.*;
import java.io.*;
import java.util.Scanner;
import java.util.HashMap;

/**
 * Esta clase contiene el socket que actúa como cliente en el simulador de transacciones.
 */
public class ClienteSocket {

    /**
     * Esta constante contiene el número de puerto por el que se realizará la conexión con el socket
     * server.
     */
    private static final int PORT = 4444;

    /**
     * Esta constante almacena la expresión regular para validar el formato de un correo
     * electrónico.
     */
    private static final String REGEX_EMAIL = "^[\\w!#$%&'*+/=?`{|}~^-]+" +
            "(?:\\.[\\w!#$%&'*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$";

    /**
     * enum que contiene los tipos de transacciones que puede realizar el cliente.
     */
    private enum Transaciones {
        CREACION_CLIENTE,
        CONSULTAR_SALDO_CUENTA,
        RETIRAR_CUENTA_CLIENTE
    }

    /**
     * Varuable global que almacena el cliente que se creará si se selecciona la opción 1 en el
     * menú principal.
     */
    private static ClienteModelo mNuevoCliente = null;

    /**
     * HashMap que contiene los datos del nuevo cliente creado.
     */
    private static HashMap<String, String> mNuevoClienteHash = null;

    /**
     * Socket para realizar la conexión con el servidor.
     */
    private static Socket mSocketCliente = null;

    /**
     * BufferedReader que sirve como entrada.
     */
    private static BufferedReader mEntrada = null;

    /**
     * PrintWriter que sirve como salida.
     */
    private static PrintWriter mSalida = null;

    /**
     * Método main de la clase ClienteSocket encargado de ejecutar el programa.
     *
     * @param args argumento por defecto del método main.
     */
    public static void main(String[] args) {
        System.out.println("\n¡Bienvenido al Banco XYZ!" +
                "\n\nPresione:" +
                "\n\n1 Para crear un cliente." +
                "\n2 Para consultar el saldo de una cuenta." +
                "\n3 Para retirar dinero de la cuenta de un cliente." +
                "\n4 Para terminar el programa.");

        Scanner sc = new Scanner(System.in);

        byte seleccionUsuario = -1;
        boolean entradaValida = false;

        while (!entradaValida) {
            System.out.println("\nIngrese su selección y la tecla enter/return:");
            if (sc.hasNextByte()) {
                seleccionUsuario = sc.nextByte();
            } else {
                sc.nextLine();
            }
            switch (seleccionUsuario) {
                case 1:
                    mNuevoCliente = crearCliente();
                    mNuevoClienteHash = crearHashCliente();
                    conectarseAlServidor();
                    enviarMensajeAlServidor(mNuevoClienteHash.toString());
                    liberarRecursos();
                    entradaValida = true;
                    break;
                case 2:
                    ConsultaCuentaModelo consulta = crearConsulta();
                    HashMap<String, String> hashConsulta = crearHashConsulta(consulta);
                    conectarseAlServidor();
                    enviarMensajeAlServidor(hashConsulta.toString());
                    liberarRecursos();
                    entradaValida = true;
                    break;
                case 3:
                    RetiroModelo retiro = crearRetiro();
                    HashMap<String, String> hashRetiro = crearHashRetiro(retiro);
                    conectarseAlServidor();
                    enviarMensajeAlServidor(hashRetiro.toString());
                    liberarRecursos();
                    entradaValida = true;
                    break;
                case 4:
                    System.out.println("\nVuelva pronto.\n\nHasta luego.");
                    System.exit(0);
                    break;
                default:
                    System.out.println("\nEntrada no válida.");
                    break;
            }
        }
        sc.close();
    }

    /**
     * Este método se encarga de realizar la conexión con el socket server.
     */
    private static void conectarseAlServidor() {
        // Se crea el socket del lado del cliente, enlazado con el servidor en la misma máquina
        // del cliente, escuchando en el puerto 4444.
        try {
            mSocketCliente = new Socket("localhost", PORT);
            // Se obtiene el canal de entrada
            mEntrada = new BufferedReader(new InputStreamReader(mSocketCliente.getInputStream()));
            // Se obtiene el canal de salida
            mSalida = new PrintWriter(new BufferedWriter(
                    new OutputStreamWriter(mSocketCliente.getOutputStream())), true);
        } catch (IOException e) {
            System.err.println("\nNo se pudieron establecer canales de entrada y " +
                    "salida para la conexión.");
            System.exit(-1);
        }
    }

    /**
     * Este método envía el mensaje al socket server según las elecciones del usuario.
     *
     * @param mensaje es el String que será enviado al socket server.
     */
    private static void enviarMensajeAlServidor(String mensaje) {
        try {
            // Se envía al servidor
            mSalida.println(mensaje);
            // Se envía a la entrada estándar la respuesta del servidor
            mensaje = mEntrada.readLine();
            System.out.println("\nEsperando confirmación servidor...");
            System.out.println("\nRespuesta: " + mensaje);
            mensaje = mEntrada.readLine();
            System.out.println("\n" + mensaje);
            mensaje = mEntrada.readLine();
            System.out.println(mensaje.replaceAll("\\*", "\n"));
        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        }
    }

    /**
     * Este método libera los recursos utilizados para conectarse al socket server, es decir cierra
     * las conexiones pertinentes.
     */
    private static void liberarRecursos() {
        // Se liberan los recursos utilizados
        mSalida.close();
        try {
            mEntrada.close();
            mSocketCliente.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Este método se encarga de crear un nuevo cliente, pidiendo los datos correspodientes al
     * usuario.
     *
     * @return el nuevo cliente creado.
     */
    private static ClienteModelo crearCliente() {
        Scanner sc = new Scanner(System.in);
        ClienteModelo nuevoCliente = new ClienteModelo();
        System.out.println("\nSe procederá a crear un nuevo cliente en la base de datos. " +
                "Por favor ingrese los datos solicitados:\n");
        System.out.print("Nombres: ");
        String nombres = sc.nextLine();
        while (nombres.length() < 1) {
            System.out.println("Nombres no válidos. Ingrese nuevamente los nombres: ");
            nombres = sc.nextLine();
        }
        nuevoCliente.setNombres(nombres);
        System.out.print("Apellidos: ");
        String apellidos = sc.nextLine();
        while (apellidos.length() < 1) {
            System.out.println("Apellidos no válidos. Ingrese nuevamente los apellidos: ");
            apellidos = sc.nextLine();
        }
        nuevoCliente.setApellidos(apellidos);
        System.out.print("Ingrese el ID Ciudad de la ciudad correspondiente:" +
                "\n1 Bogotá " +
                "\n2 Medellín " +
                "\n3 Barranquilla " +
                "\n4 Cali " +
                "\n5 Cartagena" +
                "\n6 Bucaramanga" +
                "\n7 Santa Marta" +
                "\n8 Cúcuta\n");
        byte ciudadSeleccionada = -1;
        String entradaNoValida = "\nEntrada no válida.\n\nIngrese un número de ciudad válido:";
        while (ciudadSeleccionada < 1 || ciudadSeleccionada > 8) {
            if (sc.hasNextByte()) {
                byte valorTemporal = sc.nextByte();
                if (valorTemporal >= 1 && valorTemporal <= 8) {
                    ciudadSeleccionada = valorTemporal;
                } else {
                    System.out.println(entradaNoValida);
                }
            } else {
                System.out.println(entradaNoValida);
            }
            sc.nextLine();
        }
        nuevoCliente.setIDCiudad(ciudadSeleccionada);
        System.out.print("Dirección: ");
        String direccion = sc.nextLine();
        while (direccion.length() < 1) {
            System.out.println("Dirección no válida. Ingrese nuevamente la dirección: ");
            direccion = sc.nextLine();
        }
        nuevoCliente.setDireccion(direccion);
        System.out.print("Email: ");
        String emailIngresado = sc.nextLine();
        while (!emailIngresado.matches(REGEX_EMAIL)) {
            System.out.print("Ingrese un email válido: ");
            emailIngresado = sc.nextLine();
        }
        nuevoCliente.setEmail(emailIngresado);
        sc.close();
        return nuevoCliente;
    }

    /**
     * Este método crea el HashMap que contiene los datos del cliente creado.
     *
     * @return el HashMap con los datos del cliente creado.
     */
    private static HashMap<String, String> crearHashCliente() {
        HashMap<String, String> nuevoClienteHash = new HashMap<String, String>();
        nuevoClienteHash.put("transaccion_id", Transaciones.CREACION_CLIENTE.toString());
        nuevoClienteHash.put("nombres", mNuevoCliente.getNombres());
        nuevoClienteHash.put("apellidos", mNuevoCliente.getApellidos());
        nuevoClienteHash.put("ciudad_id", Integer.toString(mNuevoCliente.getIDCiudad()));
        nuevoClienteHash.put("direccion", mNuevoCliente.getDireccion());
        nuevoClienteHash.put("email", mNuevoCliente.getEmail());
        return nuevoClienteHash;
    }

    /**
     * Este método se encarga de crear el Retiro para ser enviado al socket server.
     */
    private static RetiroModelo crearRetiro() {
        Scanner sc = new Scanner(System.in);
        RetiroModelo retiro = new RetiroModelo();
        System.out.println("Ingrese el número de cuenta de donde se va a retirar el dinero: ");
        long numeroCuenta = 0;
        boolean numeroCuentaValido = false;
        while (!numeroCuentaValido) {
            if (sc.hasNextLong()) {
                long temp = sc.nextLong();
                // El número de cuenta es un número de 10 dígitos entre 1_000_000_000 y
                // 2_000_000_000
                if (temp >= 1000000000 && temp < 2000000000) {
                    numeroCuenta = temp;
                    numeroCuentaValido = true;
                } else {
                    System.out.println("Número de cuenta no válido, ingrese un número de 10 " +
                            "dígitos entre 1_000_000_000 y 2_000_000_000:");
                }
            } else {
                sc.next();
                System.out.println("Número de cuenta no válido, ingrese un número de 10 " +
                        "dígitos entre 1_000_000_000 y 2_000_000_000:");
            }
            sc.nextLine();
        }
        retiro.setNumeroCuenta(numeroCuenta);
        System.out.println("Ingrese el monto a retirar: ");
        long montoARetirar = 0;
        boolean montoValido = false;
        while (!montoValido) {
            if (sc.hasNextLong()) {
                long temp = sc.nextLong();
                // El monto a retirar debe estar entre 10_000 y 900_000
                if (temp >= 10000 && temp <= 900000) {
                    montoARetirar = temp;
                    montoValido = true;
                } else {
                    System.out.println("Monto a retirar no válido, ingrese un número entre " +
                            "10_000 y 900_000:");
                }
            } else {
                sc.next();
                System.out.println("Monto a retirar no válido, ingrese un número entre 10_000 y " +
                        "900_000:");
            }
            sc.nextLine();
        }
        retiro.setMontoARetirar(montoARetirar);
        sc.close();
        return retiro;
    }

    /**
     * Este método crea un HashMap<String, String> con los datos contenidos en un objeto tipo
     * RetiroModelo.
     *
     * @param retiro es el objeto RetiroModelo del cual se obtienen los datos para la creación del
     *               HashMap<String, String>
     * @return el HashMap<String, String> con los datos del objeto RetiroModelo.
     */
    private static HashMap<String, String> crearHashRetiro(RetiroModelo retiro) {
        HashMap<String, String> hashRetiro = new HashMap<String, String>();
        hashRetiro.put("transaccion_id", Transaciones.RETIRAR_CUENTA_CLIENTE.toString());
        hashRetiro.put("numero_cuenta", Long.toString(retiro.getNumeroCuenta()));
        hashRetiro.put("monto_a_retirar", Long.toString(retiro.getMontoARetirar()));
        return hashRetiro;
    }

    /**
     * Este método crea una nueva ConsultaCuentaModelo.
     *
     * @return una nueva ConsultaCuentaModelo con los datos de la cuenta ingresados por el usuario.
     */
    private static ConsultaCuentaModelo crearConsulta() {
        Scanner sc = new Scanner(System.in);
        ConsultaCuentaModelo consulta = new ConsultaCuentaModelo();
        System.out.println("Ingrese el número de cuenta que desea consultar: ");
        long numeroCuenta = 0;
        boolean numeroCuentaValido = false;
        while (!numeroCuentaValido) {
            if (sc.hasNextLong()) {
                long temp = sc.nextLong();
                if (temp > 1000000000 && temp < 2000000000) {
                    numeroCuenta = temp;
                    numeroCuentaValido = true;
                } else {
                    System.out.println("Número de cuenta no válido. Por favor ingrese un número " +
                            "de cuenta de 10 dígitos entre 1_000_000_000 y 2_000_000_000:");
                }
            } else {
                sc.next();
                System.out.println("Número de cuenta no válido. Por favor ingrese un número de " +
                        "cuenta de 10 dígitos entre 1_000_000_000 y 2_000_000_000:");
            }
            sc.nextLine();
        }
        consulta.setNumeroCuenta(numeroCuenta);
        sc.close();
        return consulta;
    }

    /**
     * Este método crea un HashMap<String, String> con base en el objeto ConsultaCuentaModelo
     * otorgado.
     *
     * @param consulta es el objeto ConsultaCuentaModelo que se recibe como parámetro.
     * @return un HashMap<String, String> basado en el objeto ConsultaCuentaModelo que se recibió
     * como parámetro.
     */
    private static HashMap<String, String> crearHashConsulta(ConsultaCuentaModelo consulta) {
        HashMap<String, String> hashConsulta = new HashMap<String, String>();
        hashConsulta.put("transaccion_id", Transaciones.CONSULTAR_SALDO_CUENTA.toString());
        hashConsulta.put("numero_cuenta", Long.toString(consulta.getNumeroCuenta()));
        return hashConsulta;
    }
}
