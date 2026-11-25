package gestor_reservas;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;

public class ReservaDAO {

    private final String url;
    private final String user;
    private final String password;

    public ReservaDAO() {
        this.url = System.getenv().getOrDefault("DB_URL", "jdbc:postgresql://localhost:5432/reservas");
        this.user = System.getenv().getOrDefault("DB_USER", "alumno");
        this.password = System.getenv().getOrDefault("DB_PASSWORD", "1234");
    }

    private Connection obtenerConexion() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    private void imprimirResultados(ResultSet rs, String mensajeVacio) throws SQLException {
        boolean hayResultados = false;
        while (rs.next()) {
            hayResultados = true;
            System.out.println("ID: " + rs.getLong("id")
                    + " | Alumno: " + rs.getString("alumno")
                    + " | Aula: " + rs.getString("aula")
                    + " | Fecha: " + rs.getDate("fecha"));
        }
        if (!hayResultados) {
            System.out.println(mensajeVacio);
        }
    }

    // 9
    public boolean existeReservaPorId(long id) {
        String sql = "SELECT COUNT(*) FROM reserva WHERE id = ?";
        try (Connection conn = obtenerConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.out.println("Error al comprobar la reserva: " + e.getMessage());
        }
        return false;
    }

    // 8.2
    public boolean existeReservaEnAulaYFecha(String aula, LocalDate fecha) {
        String sql = "SELECT COUNT(*) FROM reserva WHERE LOWER(aula) = LOWER(?) AND fecha = ?";
        try (Connection conn = obtenerConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, aula);
            pstmt.setDate(2, Date.valueOf(fecha));

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.out.println("Error al comprobar disponibilidad: " + e.getMessage());
        }
        return false;
    }

    // 10.1 / 13.2
    public boolean insertarReserva(String alumno, String aula, LocalDate fecha) {
        String sql = "INSERT INTO reserva (alumno, aula, fecha) VALUES (?, ?, ?)";
        try (Connection conn = obtenerConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, alumno);
            pstmt.setString(2, aula);
            pstmt.setDate(3, Date.valueOf(fecha));

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error al registrar la reserva: " + e.getMessage());
            return false;
        }
    }

    // 8.1 / 13.1
    public void mostrarTodasLasReservas() {
        String sql = "SELECT id, alumno, aula, fecha FROM reserva ORDER BY fecha ASC, id ASC";
        try (Connection conn = obtenerConexion();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            System.out.println("\n--- LISTADO DE RESERVAS ---");
            imprimirResultados(rs, "No hay reservas registradas.");
        } catch (SQLException e) {
            System.out.println("Error al consultar las reservas: " + e.getMessage());
        }
    }

    // 9 / 12.5
    public void mostrarReservaPorId(long id) {
        String sql = "SELECT id, alumno, aula, fecha FROM reserva WHERE id = ?";
        try (Connection conn = obtenerConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                imprimirResultados(rs, "No se ha encontrado la reserva con ID " + id + ".");
            }
        } catch (SQLException e) {
            System.out.println("Error al consultar la reserva: " + e.getMessage());
        }
    }

    // 8.1
    public void buscarReservasPorAula(String aula) {
        String sql = "SELECT id, alumno, aula, fecha FROM reserva WHERE LOWER(aula) = LOWER(?) ORDER BY fecha ASC, id ASC";
        try (Connection conn = obtenerConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, aula);
            try (ResultSet rs = pstmt.executeQuery()) {
                System.out.println("\n--- RESERVAS DEL AULA " + aula + " ---");
                imprimirResultados(rs, "No existen reservas para el aula " + aula + ".");
            }
        } catch (SQLException e) {
            System.out.println("Error al buscar reservas por aula: " + e.getMessage());
        }
    }

    // 8.1
    public void buscarReservasPorAlumno(String alumno) {
        String sql = "SELECT id, alumno, aula, fecha FROM reserva WHERE LOWER(alumno) = LOWER(?) ORDER BY fecha ASC, id ASC";
        try (Connection conn = obtenerConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, alumno);
            try (ResultSet rs = pstmt.executeQuery()) {
                System.out.println("\n--- RESERVAS DE " + alumno + " ---");
                imprimirResultados(rs, "No existen reservas para el alumno " + alumno + ".");
            }
        } catch (SQLException e) {
            System.out.println("Error al buscar reservas por alumno: " + e.getMessage());
        }
    }

    // 11
    public void buscarReservasPorAlumnoParcial(String texto) {
        String sql = "SELECT id, alumno, aula, fecha FROM reserva WHERE LOWER(alumno) LIKE LOWER(?) ORDER BY fecha ASC, id ASC";
        String patron = "%" + texto + "%";
        try (Connection conn = obtenerConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, patron);
            try (ResultSet rs = pstmt.executeQuery()) {
                System.out.println("\n--- RESERVAS CON ALUMNO QUE CONTIENE \"" + texto + "\" ---");
                imprimirResultados(rs, "No se encontraron reservas que coincidan con \"" + texto + "\".");
            }
        } catch (SQLException e) {
            System.out.println("Error al buscar reservas por coincidencia: " + e.getMessage());
        }
    }

    // 8.1
    public void buscarReservasPorFecha(LocalDate fecha) {
        String sql = "SELECT id, alumno, aula, fecha FROM reserva WHERE fecha = ? ORDER BY fecha ASC, id ASC";
        try (Connection conn = obtenerConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDate(1, Date.valueOf(fecha));
            try (ResultSet rs = pstmt.executeQuery()) {
                System.out.println("\n--- RESERVAS DEL DIA " + fecha + " ---");
                imprimirResultados(rs, "No existen reservas para la fecha " + fecha + ".");
            }
        } catch (SQLException e) {
            System.out.println("Error al buscar reservas por fecha: " + e.getMessage());
        }
    }

    // 8.2
    public void buscarPorAulaYFecha(String aula, LocalDate fecha) {
        String sql = "SELECT id, alumno, aula, fecha FROM reserva WHERE LOWER(aula) = LOWER(?) AND fecha = ? ORDER BY fecha ASC, id ASC";
        try (Connection conn = obtenerConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, aula);
            pstmt.setDate(2, Date.valueOf(fecha));
            try (ResultSet rs = pstmt.executeQuery()) {
                System.out.println("\n--- RESERVAS EN " + aula + " EL " + fecha + " ---");
                imprimirResultados(rs, "No existen reservas para esa aula y fecha.");
            }
        } catch (SQLException e) {
            System.out.println("Error al buscar reservas: " + e.getMessage());
        }
    }

    // 8.2
    public void buscarPorDosAulas(String aula1, String aula2) {
        String sql = "SELECT id, alumno, aula, fecha FROM reserva WHERE LOWER(aula) = LOWER(?) OR LOWER(aula) = LOWER(?) ORDER BY fecha ASC, id ASC";
        try (Connection conn = obtenerConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, aula1);
            pstmt.setString(2, aula2);
            try (ResultSet rs = pstmt.executeQuery()) {
                System.out.println("\n--- RESERVAS EN " + aula1 + " O " + aula2 + " ---");
                imprimirResultados(rs, "No existen reservas para esas aulas.");
            }
        } catch (SQLException e) {
            System.out.println("Error al buscar reservas: " + e.getMessage());
        }
    }

    // 8.2
    public void buscarExcluyendoAula(String aula) {
        String sql = "SELECT id, alumno, aula, fecha FROM reserva WHERE NOT LOWER(aula) = LOWER(?) ORDER BY fecha ASC, id ASC";
        try (Connection conn = obtenerConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, aula);
            try (ResultSet rs = pstmt.executeQuery()) {
                System.out.println("\n--- RESERVAS EXCLUYENDO AULA " + aula + " ---");
                imprimirResultados(rs, "No existen reservas fuera del aula " + aula + ".");
            }
        } catch (SQLException e) {
            System.out.println("Error al buscar reservas: " + e.getMessage());
        }
    }

    // 8.2
    public void buscarPorRangoFechas(LocalDate fechaInicio, LocalDate fechaFin) {
        String sql = "SELECT id, alumno, aula, fecha FROM reserva WHERE fecha BETWEEN ? AND ? ORDER BY fecha ASC, id ASC";
        try (Connection conn = obtenerConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDate(1, Date.valueOf(fechaInicio));
            pstmt.setDate(2, Date.valueOf(fechaFin));
            try (ResultSet rs = pstmt.executeQuery()) {
                System.out.println("\n--- RESERVAS ENTRE " + fechaInicio + " Y " + fechaFin + " ---");
                imprimirResultados(rs, "No existen reservas en ese rango de fechas.");
            }
        } catch (SQLException e) {
            System.out.println("Error al buscar reservas por rango: " + e.getMessage());
        }
    }

    // 8.2
    public void buscarPorVariasAulas(List<String> aulas) {
        if (aulas.isEmpty()) {
            System.out.println("No se han indicado aulas.");
            return;
        }
        StringBuilder placeholders = new StringBuilder();
        for (int i = 0; i < aulas.size(); i++) {
            placeholders.append("LOWER(?)");
            if (i < aulas.size() - 1) {
                placeholders.append(", ");
            }
        }
        String sql = "SELECT id, alumno, aula, fecha FROM reserva WHERE LOWER(aula) IN (" + placeholders + ") ORDER BY fecha ASC, id ASC";
        try (Connection conn = obtenerConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            for (int i = 0; i < aulas.size(); i++) {
                pstmt.setString(i + 1, aulas.get(i));
            }
            try (ResultSet rs = pstmt.executeQuery()) {
                System.out.println("\n--- RESERVAS EN LAS AULAS INDICADAS ---");
                imprimirResultados(rs, "No existen reservas en esas aulas.");
            }
        } catch (SQLException e) {
            System.out.println("Error al buscar reservas por lista de aulas: " + e.getMessage());
        }
    }

    // 9 / 13.2
    public boolean modificarReserva(long id, String alumno, String aula, LocalDate fecha) {
        String sql = "UPDATE reserva SET alumno = ?, aula = ?, fecha = ? WHERE id = ?";
        try (Connection conn = obtenerConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, alumno);
            pstmt.setString(2, aula);
            pstmt.setDate(3, Date.valueOf(fecha));
            pstmt.setLong(4, id);

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error al modificar la reserva: " + e.getMessage());
            return false;
        }
    }

    // 10.2 / 13.2
    public boolean eliminarReserva(long id) {
        String sql = "DELETE FROM reserva WHERE id = ?";
        try (Connection conn = obtenerConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error al eliminar la reserva: " + e.getMessage());
            return false;
        }
    }

    // 12
    public void ejecutarTransaccionConSavepoint(long id, LocalDate nuevaFecha, String nuevaAula, boolean cancelarCambioAula) {
        String sqlFecha = "UPDATE reserva SET fecha = ? WHERE id = ?";
        String sqlAula = "UPDATE reserva SET aula = ? WHERE id = ?";

        Connection conn = null;
        try {
            conn = obtenerConexion();
            // 12.1
            conn.setAutoCommit(false);

            try (PreparedStatement psFecha = conn.prepareStatement(sqlFecha)) {
                psFecha.setDate(1, Date.valueOf(nuevaFecha));
                psFecha.setLong(2, id);
                int filasFecha = psFecha.executeUpdate();
                if (filasFecha == 0) {
                    System.out.println("No existe ninguna reserva con ID " + id + ".");
                    conn.rollback();
                    return;
                }
            }

            // 12.4
            Savepoint punto = conn.setSavepoint("punto_1");

            try (PreparedStatement psAula = conn.prepareStatement(sqlAula)) {
                psAula.setString(1, nuevaAula);
                psAula.setLong(2, id);
                psAula.executeUpdate();
            }

            // 12.4 / 12.5
            if (cancelarCambioAula) {
                conn.rollback(punto);
                System.out.println("Cambio de aula cancelado mediante ROLLBACK al SAVEPOINT.");
            }

            // 12.2
            conn.commit();
            System.out.println("Transaccion confirmada con COMMIT.");
            System.out.println("Estado final de la reserva:");
            mostrarReservaPorId(id);

        } catch (SQLException e) {
            System.out.println("Error durante la transaccion: " + e.getMessage());
            if (conn != null) {
                try {
                    // 12.3
                    conn.rollback();
                    System.out.println("Se ha ejecutado ROLLBACK completo.");
                } catch (SQLException ex) {
                    System.out.println("Error al ejecutar ROLLBACK: " + ex.getMessage());
                }
            }
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    System.out.println("Error al cerrar la conexion: " + e.getMessage());
                }
            }
        }
    }
}