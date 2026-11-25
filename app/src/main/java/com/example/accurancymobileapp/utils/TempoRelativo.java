package com.example.accurancymobileapp.utils;

import androidx.annotation.Nullable;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

/** Datas da API escritas do jeito que as pessoas falam: "agora", "há 5 min", "ontem, 14:20". */
public final class TempoRelativo {

    private static final Locale PT_BR = Locale.forLanguageTag("pt-BR");
    private static final ZoneId FUSO_APP = ZoneId.of("America/Sao_Paulo");
    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm", PT_BR);
    private static final DateTimeFormatter DIA_MES = DateTimeFormatter.ofPattern("d 'de' MMM", PT_BR);
    private static final DateTimeFormatter DATA_COMPLETA = DateTimeFormatter.ofPattern("dd/MM/yyyy", PT_BR);

    private TempoRelativo() {}

    /**
     * Aceita "2026-11-25T14:03:00Z" e "2026-11-25 14:03:00[.ffffff]". Colunas sem fuso vêm do
     * banco em UTC (fuso do servidor), então as duas formas são convertidas para Brasília.
     * Texto que não for data volta null.
     */
    @Nullable
    public static ZonedDateTime interpretar(@Nullable String texto) {
        if (texto == null || texto.trim().isEmpty()) return null;
        String valor = texto.trim().replace(" ", "T");
        try {
            if (valor.endsWith("Z")) {
                return Instant.parse(valor).atZone(FUSO_APP);
            }
            if (valor.matches(".*[+-]\\d{2}:\\d{2}$")) {
                return OffsetDateTime.parse(valor).atZoneSameInstant(FUSO_APP);
            }
            String semFracao = valor.replaceFirst("\\.\\d+$", "");
            return LocalDateTime.parse(semFracao).atZone(ZoneOffset.UTC).withZoneSameInstant(FUSO_APP);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    public static String descrever(@Nullable String texto) {
        ZonedDateTime quando = interpretar(texto);
        if (quando == null) return "";
        ZonedDateTime agora = ZonedDateTime.now(FUSO_APP);

        long minutos = Duration.between(quando, agora).toMinutes();
        if (minutos < 1) return "agora";
        if (minutos < 60) return "há " + minutos + " min";

        LocalDate dia = quando.toLocalDate();
        LocalDate hoje = agora.toLocalDate();
        if (dia.equals(hoje)) return "hoje, " + quando.format(HORA);
        if (dia.equals(hoje.minusDays(1))) return "ontem, " + quando.format(HORA);
        if (ChronoUnit.DAYS.between(dia, hoje) < 300 && dia.getYear() == hoje.getYear()) {
            return quando.format(DIA_MES).replace(".", "");
        }
        return quando.format(DATA_COMPLETA);
    }

    /** "14:20", para "atualizado às 14:20". */
    public static String hora(@Nullable String texto) {
        ZonedDateTime quando = interpretar(texto);
        return quando == null ? "" : quando.format(HORA);
    }

    public static String dataCurta(@Nullable String texto) {
        ZonedDateTime quando = interpretar(texto);
        return quando == null ? "" : quando.format(DATA_COMPLETA);
    }
}
