package gestor_reservas;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        ReservaDAO reservaDAO = new ReservaDAO();
        int opcion = -1;

        try (Scanner scanner = new Scanner(System.in)) {
            do {
                mostrarMenu();
                String entrada = scanner.nextLine().trim();

                try {
                    opcion = Integer.parseInt(entrada);
                } catch (NumberFormatException e) {
                    System.out.println("Introduce un numero valido.");
                    continue;
                }

                switch (opcion) {
                    case 1 -> registrarReserva(scanner, reservaDAO);
                    case 2 -> reservaDAO.mostrarTodasLasReservas();
                    case 3 -> buscarPorAula(scanner, reservaDAO);
                    case 4 -> eliminarReserva(scanner, reservaDAO);
                    case 5 -> modificarReserva(scanner, reservaDAO);
                    case 6 -> buscarPorAlumnoParcial(scanner, reservaDAO);
                    case 7 -> menuConsultasAvanzadas(scanner, reservaDAO);
                    case 8 -> probarTransaccion(scanner, reservaDAO);
                    case 0 -> System.out.println("Saliendo del programa...");
                    default -> System.out.println("Opcion no valida.");
                }

                System.out.println();
            } while (opcion != 0);
        }
    }

    private static void mostrarMenu() {
        System.out.println("     GESTOR DE RESERVAS\n");
        System.out.println("1. Registrar una reserva");
        System.out.println("2. Mostrar todas las reservas");
        System.out.println("3. Buscar reservas por aula");
        System.out.println("4. Eliminar una reserva");
        System.out.println("5. Modificar una reserva");
        System.out.println("6. Buscar reservas por alumno (LIKE)");
        System.out.println("7. Consultas avanzadas y filtros");
        System.out.println("8. Transaccion con SAVEPOINT, ROLLBACK y COMMIT");
        System.out.println("0. Salir");
        System.out.println();
        System.out.print("Selecciona una opcion: ");
    }

    // 10.1
    private static void registrarReserva(Scanner scanner, ReservaDAO dao) {
        String alumno = leerTexto(scanner, "Nombre del alumno: ");
        String aula = leerTexto(scanner, "Aula: ");
        LocalDate fecha = leerFecha(scanner, "Fecha de la reserva (AAAA-MM-DD): ");

        if (dao.existeReservaEnAulaYFecha(aula, fecha)) {
            System.out.println("Ya existe una reserva para esa aula en esa fecha.");
            return;
        }

        if (dao.insertarReserva(alumno, aula, fecha)) {
            System.out.println("Reserva registrada correctamente.");
        } else {
            System.out.println("No se pudo registrar la reserva.");
        }
    }

    // 8.1
    private static void buscarPorAula(Scanner scanner, ReservaDAO dao) {
        String aula = leerTexto(scanner, "Introduce el aula: ");
        dao.buscarReservasPorAula(aula);
    }

    // 10.2
    private static void eliminarReserva(Scanner scanner, ReservaDAO dao) {
        long id = leerId(scanner, "Introduce el ID de la reserva a eliminar: ");
        if (dao.eliminarReserva(id)) {
            System.out.println("Reserva eliminada correctamente.");
        } else {
            System.out.println("No se ha encontrado la reserva con ID " + id + ".");
        }
    }

    // 9
    private static void modificarReserva(Scanner scanner, ReservaDAO dao) {
        long id = leerId(scanner, "Introduce el ID de la reserva a modificar: ");
        if (!dao.existeReservaPorId(id)) {
            System.out.println("No existe ninguna reserva con ID " + id + ".");
            return;
        }

        System.out.println("Reserva actual:");
        dao.mostrarReservaPorId(id);

        String alumno = leerTexto(scanner, "Nuevo nombre del alumno: ");
        String aula = leerTexto(scanner, "Nueva aula: ");
        LocalDate fecha = leerFecha(scanner, "Nueva fecha (AAAA-MM-DD): ");

        if (dao.modificarReserva(id, alumno, aula, fecha)) {
            System.out.println("Reserva modificada correctamente.");
        } else {
            System.out.println("No se pudo modificar la reserva.");
        }
    }

    // 11
    private static void buscarPorAlumnoParcial(Scanner scanner, ReservaDAO dao) {
        String texto = leerTexto(scanner, "Introduce el nombre o parte del nombre del alumno: ");
        dao.buscarReservasPorAlumnoParcial(texto);
    }

    // 8.1 / 8.2
    private static void menuConsultasAvanzadas(Scanner scanner, ReservaDAO dao) {
        System.out.println("\n--- CONSULTAS AVANZADAS ---");
        System.out.println("1. Buscar por alumno exacto (WHERE)");
        System.out.println("2. Buscar por fecha concreta (WHERE)");
        System.out.println("3. Buscar por aula y fecha (AND)");
        System.out.println("4. Buscar en dos aulas diferentes (OR)");
        System.out.println("5. Buscar excluyendo un aula (NOT)");
        System.out.println("6. Buscar por intervalo de fechas (BETWEEN)");
        System.out.println("7. Buscar en varias aulas (IN)");
        System.out.print("Elige una consulta: ");

        String entrada = scanner.nextLine().trim();
        int subOpcion;
        try {
            subOpcion = Integer.parseInt(entrada);
        } catch (NumberFormatException e) {
            System.out.println("Opcion no valida.");
            return;
        }

        switch (subOpcion) {
            case 1 -> {
                String alumno = leerTexto(scanner, "Nombre exacto del alumno: ");
                dao.buscarReservasPorAlumno(alumno);
            }
            case 2 -> {
                LocalDate fecha = leerFechaSinRestriccion(scanner, "Fecha (AAAA-MM-DD): ");
                dao.buscarReservasPorFecha(fecha);
            }
            case 3 -> {
                String aula = leerTexto(scanner, "Aula: ");
                LocalDate fecha = leerFechaSinRestriccion(scanner, "Fecha (AAAA-MM-DD): ");
                dao.buscarPorAulaYFecha(aula, fecha);
            }
            case 4 -> {
                String aula1 = leerTexto(scanner, "Primera aula: ");
                String aula2 = leerTexto(scanner, "Segunda aula: ");
                dao.buscarPorDosAulas(aula1, aula2);
            }
            case 5 -> {
                String aula = leerTexto(scanner, "Aula a excluir: ");
                dao.buscarExcluyendoAula(aula);
            }
            case 6 -> {
                LocalDate inicio = leerFechaSinRestriccion(scanner, "Fecha inicio (AAAA-MM-DD): ");
                LocalDate fin = leerFechaSinRestriccion(scanner, "Fecha fin (AAAA-MM-DD): ");
                dao.buscarPorRangoFechas(inicio, fin);
            }
            case 7 -> {
                String linea = leerTexto(scanner, "Introduce las aulas separadas por comas (ej. A101, A102, B201): ");
                List<String> aulas = new ArrayList<>();
                for (String parte : linea.split(",")) {
                    String limpia = parte.trim();
                    if (!limpia.isEmpty()) {
                        aulas.add(limpia);
                    }
                }
                dao.buscarPorVariasAulas(aulas);
            }
            default -> System.out.println("Opcion no valida.");
        }
    }

    // 12.5
    private static void probarTransaccion(Scanner scanner, ReservaDAO dao) {
        long id = leerId(scanner, "Introduce el ID de la reserva para la transaccion: ");
        if (!dao.existeReservaPorId(id)) {
            System.out.println("No existe ninguna reserva con ID " + id + ".");
            return;
        }

        System.out.println("Estado inicial:");
        dao.mostrarReservaPorId(id);

        LocalDate nuevaFecha = leerFecha(scanner, "Nueva fecha (AAAA-MM-DD): ");
        String nuevaAula = leerTexto(scanner, "Nueva aula: ");
        String respuesta = leerTexto(scanner, "Cancelar el cambio de aula usando SAVEPOINT? (s/n): ");
        boolean cancelarAula = respuesta.equalsIgnoreCase("s");

        dao.ejecutarTransaccionConSavepoint(id, nuevaFecha, nuevaAula, cancelarAula);
    }

    private static long leerId(Scanner scanner, String mensaje) {
        while (true) {
            System.out.print(mensaje);
            try {
                return Long.parseLong(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("El ID debe ser un numero valido.");
            }
        }
    }

    private static String leerTexto(Scanner scanner, String mensaje) {
        while (true) {
            System.out.print(mensaje);
            String texto = scanner.nextLine().trim();
            if (!texto.isEmpty()) {
                return texto;
            }
            System.out.println("El campo no puede estar vacio.");
        }
    }

    private static LocalDate leerFecha(Scanner scanner, String mensaje) {
        while (true) {
            System.out.print(mensaje);
            String entrada = scanner.nextLine().trim();
            try {
                LocalDate fecha = LocalDate.parse(entrada);
                if (fecha.isBefore(LocalDate.now())) {
                    System.out.println("La fecha no puede ser anterior a hoy.");
                } else {
                    return fecha;
                }
            } catch (DateTimeParseException e) {
                System.out.println("Formato incorrecto. Usa AAAA-MM-DD (ej. 2026-11-15).");
            }
        }
    }

    private static LocalDate leerFechaSinRestriccion(Scanner scanner, String mensaje) {
        while (true) {
            System.out.print(mensaje);
            String entrada = scanner.nextLine().trim();
            try {
                return LocalDate.parse(entrada);
            } catch (DateTimeParseException e) {
                System.out.println("Formato incorrecto. Usa AAAA-MM-DD (ej. 2026-11-15).");
            }
        }
    }
}