package secsys.services;

import secsys.dto.PlanningActivityDTO;
import secsys.dto.PlanningUploadDTO;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class PlanningCsvParser {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final String HEADER =
            "fecha_inicio_actividad;fecha_fin_actividad;actividad;descripcion;estado";

    public PlanningUploadDTO parse(InputStream csvStream, PlanningUploadDTO base) {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(csvStream, StandardCharsets.UTF_8))) {

            // Cabecera A:
            // TIPO_SERVICIO;X
            // VERSION;vX.Y
            // ESTADO;Activa|Inactiva
            // COLOR;#RRGGBB
            String l1 = readNonEmptyLine(br);
            String l2 = readNonEmptyLine(br);
            String l3 = readNonEmptyLine(br);
            String l4 = readNonEmptyLine(br);

            if (l1 == null) throw new IllegalArgumentException("El CSV está vacío.");
            if (l2 == null || l3 == null || l4 == null) {
                throw new IllegalArgumentException(
                        "Faltan líneas de cabecera. Deben existir 4 líneas: " +
                        "TIPO_SERVICIO;..., VERSION;..., ESTADO;..., COLOR;#RRGGBB"
                );
            }

            String[] p1 = split(l1);
            String[] p2 = split(l2);
            String[] p3 = split(l3);
            String[] p4 = split(l4);

            // Quita BOM si viene pegado al primer token
            p1[0] = stripBom(p1[0]);
            p2[0] = stripBom(p2[0]);
            p3[0] = stripBom(p3[0]);
            p4[0] = stripBom(p4[0]);

            if (p1.length < 2 || !p1[0].trim().equalsIgnoreCase("TIPO_SERVICIO")) {
                throw new IllegalArgumentException("La primera línea debe ser: TIPO_SERVICIO;Valor");
            }
            base.tipoServicio = safeTrim(p1[1]);
            if (base.tipoServicio.isBlank()) throw new IllegalArgumentException("TIPO_SERVICIO no puede estar vacío.");

            if (p2.length < 2 || !p2[0].trim().equalsIgnoreCase("VERSION")) {
                throw new IllegalArgumentException("La segunda línea debe ser: VERSION;vX.Y");
            }
            base.version = safeTrim(p2[1]);
            if (base.version.isBlank()) throw new IllegalArgumentException("VERSION no puede estar vacío.");

            if (p3.length < 2 || !p3[0].trim().equalsIgnoreCase("ESTADO")) {
                throw new IllegalArgumentException("La tercera línea debe ser: ESTADO;Activa|Inactiva");
            }
            base.estado = safeTrim(p3[1]);
            if (base.estado.isBlank()) throw new IllegalArgumentException("ESTADO de planificación no puede estar vacío.");
            if (!isValidEstadoPlanificacion(base.estado)) {
                throw new IllegalArgumentException("ESTADO de planificación inválido: [" + base.estado + "]. Use: Activa o Inactiva.");
            }

            if (p4.length < 2 || !p4[0].trim().equalsIgnoreCase("COLOR")) {
                throw new IllegalArgumentException("La cuarta línea debe ser: COLOR;#RRGGBB");
            }
            base.colorHex = safeTrim(p4[1]);
            if (!base.colorHex.matches("^#[0-9A-Fa-f]{6}$")) {
                throw new IllegalArgumentException("COLOR inválido. Use formato #RRGGBB (ej. #2F80ED).");
            }

            String header = readNonEmptyLine(br);
            if (header == null) throw new IllegalArgumentException("Falta el encabezado del CSV.");
            if (!header.trim().equalsIgnoreCase(HEADER)) {
                throw new IllegalArgumentException("Encabezado inválido. Debe ser:\n" + HEADER);
            }

            int row = 5; // 4 cabecera + 1 header
            String line;
            while ((line = br.readLine()) != null) {
                row++;
                if (line.isBlank()) continue;

                String[] parts = line.split(";", -1);
                if (parts.length < 5) {
                    throw new IllegalArgumentException("Fila " + row + " inválida: se esperan 5 columnas.");
                }

                String iniStr = safeTrim(parts[0]);
                String finStr = safeTrim(parts[1]);
                String actividad = safeTrim(parts[2]);
                String desc = safeTrim(parts[3]);
                String estadoAct = safeTrim(parts[4]);

                if (actividad.isBlank()) {
                    throw new IllegalArgumentException("Fila " + row + ": el campo 'actividad' es obligatorio.");
                }

                LocalDateTime ini, fin;
                try {
                    ini = LocalDateTime.parse(iniStr, FMT);
                    fin = LocalDateTime.parse(finStr, FMT);
                } catch (Exception e) {
                    throw new IllegalArgumentException("Fila " + row + ": formato de fecha inválido. Use YYYY-MM-DD HH:MM.");
                }

                if (!fin.isAfter(ini)) {
                    throw new IllegalArgumentException("Fila " + row + ": la fecha fin debe ser mayor a la fecha inicio.");
                }

                if (estadoAct.isBlank()) {
                    throw new IllegalArgumentException("Fila " + row + ": el campo 'estado' es obligatorio.");
                }

                if (!isValidEstadoActividad(estadoAct)) {
                    throw new IllegalArgumentException(
                            "Fila " + row + ": estado inválido. Use: Pendiente, En_progreso, Completada, Cancelada."
                    );
                }

                PlanningActivityDTO a = new PlanningActivityDTO();
                a.fechaInicio = ini;
                a.fechaFin = fin;
                a.actividad = actividad;
                a.descripcion = desc.isBlank() ? null : desc;
                a.estado = estadoAct;

                base.actividades.add(a);
            }

            if (base.actividades.isEmpty()) {
                throw new IllegalArgumentException("El CSV no contiene actividades.");
            }

            return base;

        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("No se pudo leer el CSV: " + e.getMessage(), e);
        }
    }

    private static boolean isValidEstadoPlanificacion(String s) {
        if (s == null) return false;
        return s.equals("Activa") || s.equals("Inactiva");
    }

    private static boolean isValidEstadoActividad(String s) {
        if (s == null) return false;
        return s.equals("Pendiente") || s.equals("En_progreso") || s.equals("Completada") || s.equals("Cancelada");
    }

    private static String readNonEmptyLine(BufferedReader br) throws Exception {
        String line;
        while ((line = br.readLine()) != null) {
            if (!line.isBlank()) return line;
        }
        return null;
    }

    private static String[] split(String line) {
        return line.split(";", -1);
    }

    private static String stripBom(String s) {
        if (s == null) return null;
        return s.replace("\uFEFF", "");
    }

    private static String safeTrim(String s) {
        if (s == null) return "";
        return s.replace("\uFEFF", "").trim();
    }
}
