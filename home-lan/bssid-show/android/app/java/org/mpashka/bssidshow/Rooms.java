package org.mpashka.bssidshow;

/** Карта BSSID→комната и шкала уровня сигнала. */
final class Rooms {

    private Rooms() {}

    /** «Сигнала нет» — заведомо ниже любого настоящего RSSI. */
    static final int RSSI_NONE = -127;

    /**
     * Пороги dBm для палочек — те же, по которым Android рисует свой значок Wi-Fi
     * (значения по умолчанию из AOSP). Линейная шкала «на глаз» тут врала: −63 dBm,
     * то есть хороший сигнал в соседней комнате, давала две палочки из четырёх.
     * Это калибровочная ручка: если на живом телефоне шкала снова разойдётся с системной,
     * крутить здесь.
     */
    private static final int[] BARS_FROM = {-88, -77, -66, -55};

    /** 0..4 палочки. */
    static int level(int rssi) {
        int l = 0;
        for (int from : BARS_FROM) {
            if (rssi >= from) l++;
        }
        return l;
    }

    /**
     * @tag:ap-map Второй BSS радио — тот же MAC с выставленным битом локального
     * администрирования (0x02 первого байта): 40→42, d4→d6. Сбрасываем бит, и одна запись
     * покрывает и общую сеть, и именную. Сравнивать адрес только целиком: у спальни и кухни
     * совпадают первые пять байт.
     */
    static String normalize(String bssid) {
        if (bssid == null) return null;
        String b = bssid.trim().toLowerCase();
        if (b.length() != 17) return b;
        try {
            int first = Integer.parseInt(b.substring(0, 2), 16) & ~0x02;
            return String.format("%02x", first) + b.substring(2);
        } catch (NumberFormatException e) {
            return b;
        }
    }

    /** Имя комнаты из карты, или null если BSSID неизвестен. */
    static String nameFor(String mapping, String bssid) {
        String want = normalize(bssid);
        if (want == null) return null;
        for (String line : mapping.split("\n")) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#")) continue;
            String[] p = line.split("\\s+", 2);
            if (p.length == 2 && want.equals(normalize(p[0]))) return p[1].trim();
        }
        return null;
    }

    /**
     * Первое слово записи — то, что рисуется: либо встроенное имя комнаты, либо эмодзи.
     * Остальные слова — подпись для человека и для скринридера.
     */
    static String glyph(String name) {
        return name == null ? null : name.trim().split("\\s+", 2)[0];
    }

    /** Встроенная иконка по имени комнаты. 0 — такой нет, рисуем глиф текстом. */
    static int icon(String name) {
        String g = glyph(name);
        if (g == null) return 0;
        switch (g.toLowerCase()) {
            case "hall": case "коридор": case "прихожая": return R.drawable.ic_hall;
            case "kitchen": case "кухня": return R.drawable.ic_kitchen;
            case "bedroom": case "спальня": return R.drawable.ic_bedroom;
            case "work": case "работа": case "офис": return R.drawable.ic_work;
            default: return 0;
        }
    }

    /**
     * Человеческое имя комнаты: без ведущего эмодзи, если после него есть подпись.
     * «🚿 ванная» → «ванная», «спальня» → «спальня».
     */
    static String label(String name) {
        if (name == null) return null;
        String[] p = name.trim().split("\\s+", 2);
        return p.length == 2 && icon(name) == 0 ? p[1] : name.trim();
    }

    /** Комнаты из карты, по разу каждая, в порядке карты: готовый маршрут обхода. */
    static java.util.List<String> rooms(String mapping) {
        java.util.LinkedHashSet<String> out = new java.util.LinkedHashSet<>();
        for (String line : mapping.split("\n")) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#")) continue;
            String[] p = line.split("\\s+", 2);
            if (p.length == 2) out.add(label(p[1]));
        }
        return new java.util.ArrayList<>(out);
    }

    /** Сколько знаков в глифе: эмодзи — один, слово — много и в клетку 1x1 не влезет. */
    static int glyphLength(String name) {
        String g = glyph(name);
        return g == null ? 0 : g.codePointCount(0, g.length());
    }

    /** Самопроверка: {@code java Rooms} без Android. См. build.sh --check. */
    public static void main(String[] args) {
        String map = "# comment\n"
                + "40:31:3c:df:5c:3f hall\n"
                + "d4:da:21:71:10:21 спальня\n"
                + "d4:da:21:71:10:06 kitchen\n";
        // именная сеть точки отличается битом 0x02 — должна дать ту же комнату
        assertEq("hall", nameFor(map, "42:31:3C:DF:5C:3F"));
        assertEq("hall", nameFor(map, "40:31:3c:df:5c:3f"));
        assertEq("спальня", nameFor(map, "d6:da:21:71:10:21"));
        // спальня и кухня различаются только последним байтом — по префиксу совпадать нельзя
        assertEq("kitchen", nameFor(map, "d6:da:21:71:10:06"));
        assertEq(null, nameFor(map, "d4:da:21:71:10:07"));
        assertEq(null, nameFor(map, "02:00:00:00:00:00"));
        assertEq(null, nameFor(map, null));
        // глиф: первое слово рисуется, остальное — подпись
        assertEq("🚿", glyph("🚿 ванная"));
        assertEq("спальня", glyph("спальня"));
        assertEq(null, glyph(null));
        assertEq("1", "" + glyphLength("🚿 ванная"));   // эмодзи вне BMP — одна кодовая точка
        assertEq("6", "" + glyphLength("ванная"));
        assertEq("0", "" + icon("🚿 ванная"));          // встроенной иконки нет — будет глиф
        if (icon("кухня") == 0 || icon("🍳 кухня") != 0) {
            throw new AssertionError("иконка выбирается по первому слову");
        }
        assertEq("0", "" + level(-100));
        assertEq("0", "" + level(RSSI_NONE));
        assertEq("1", "" + level(-80));
        assertEq("2", "" + level(-72));
        assertEq("3", "" + level(-63));   // замерено на живом телефоне: спальня, 5 ГГц
        assertEq("4", "" + level(-30));
        System.out.println("Rooms: ok");
    }

    private static void assertEq(String want, String got) {
        if (want == null ? got != null : !want.equals(got)) {
            throw new AssertionError("ожидалось " + want + ", получено " + got);
        }
    }
}
