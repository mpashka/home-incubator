package org.mpashka.vocabulary.core;

/**
 * Склонение существительных: тип склонения и родительный падеж единственного числа.
 *
 * <p>Родительный падеж выбран опорной формой не случайно: именно его исходная база
 * указывает у существительных сразу после заглавного слова
 * ({@code аба‛жу_р, абажу’ра}, {@code бе‛збедн||о_ст, ~ости}). Это даёт готовый
 * образец для сверки — правила можно проверить, ничего не размечая руками.
 *
 * <p><b>Ударение здесь не выводится.</b> Порождаются только буквы. Ударение в словоформе
 * с заглавного слова не переносится: измерено на 14 871 паре «заглавное слово → выписанная
 * форма» — оно совпадает лишь в 16,4 % случаев. См. [docs/implementation/word-forms.md].
 */
// @tag:word-forms
public final class NounDeclension {

    /** Тип склонения. */
    public enum Type {
        /** Мужской род на согласный: {@code абажу’р, абажу’ра}. */
        MASCULINE_CONSONANT,
        /** Мужской род на гласную: {@code Ма’рко, Ма’рка}; {@code Де’лхи, Де’лхија}. */
        MASCULINE_VOWEL,
        /** Женский род на {@code -а}: {@code во’да, во’де}. */
        FEMININE_A,
        /** Женский род на согласный (основа на {@code -и}): {@code безбе’дност, безбе’дности}. */
        FEMININE_CONSONANT,
        /** Средний род на {@code -о}: {@code се’ло, се’ла}. */
        NEUTER_O,
        /** Средний род на {@code -е}: {@code по’ље, по’ља}. */
        NEUTER_E,
        /** Средний род с наращением основы: {@code де’те, де’тета}; {@code и’ме, и’мена}. */
        NEUTER_EXTENDED,
        /** Тип определить не удалось. */
        UNKNOWN
    }

    private static final String VOWELS = "аеиоу";

    private NounDeclension() {
    }

    /**
     * Определяет тип склонения по заглавному слову и роду.
     *
     * @param headword заглавное слово кириллицей, знаки ударения допустимы
     * @param gender   род; если {@code null}, тип определить нельзя
     */
    public static Type typeOf(String headword, Gender gender) {
        if (gender == null) {
            return Type.UNKNOWN;
        }
        String bare = bare(headword);
        if (bare.isEmpty()) {
            return Type.UNKNOWN;
        }
        char last = bare.charAt(bare.length() - 1);
        return switch (gender) {
            case FEMININE -> last == 'а' ? Type.FEMININE_A : Type.FEMININE_CONSONANT;
            case MASCULINE -> endsWithVowel(bare) ? Type.MASCULINE_VOWEL : Type.MASCULINE_CONSONANT;
            case NEUTER -> switch (last) {
                case 'о' -> Type.NEUTER_O;
                case 'е' -> Type.NEUTER_E;
                default -> Type.UNKNOWN;
            };
        };
    }

    /**
     * Все правдоподобные варианты родительного падежа единственного числа.
     *
     * <p>Варианта несколько потому, что часть чередований сербского языка словарная,
     * а не выводимая: беглое «а» есть не у всех слов подходящего строения
     * ({@code зна̑к → зна̑ка}, но {@code пе̏так → пе̏тка}), переход {@code л → о} — тем
     * более ({@code бе̏дилац → бе̏диоца}, но {@code ба́јалац → ба́јалца}).
     *
     * <p>Для <b>поиска по словоформам</b> это не беда: в указатель кладутся все варианты,
     * лишние формы лишь чуть расширяют его и ни с чем не сталкиваются. А вот для
     * <b>показа</b> словоформы нужен ровно один верный вариант — его выбор остаётся
     * за языковой моделью (этап 9).
     */
    public static java.util.List<String> genitiveCandidates(String headword, Gender gender) {
        java.util.LinkedHashSet<String> candidates = new java.util.LinkedHashSet<>();
        String bare = bare(headword);
        addIfPresent(candidates, genitiveSingular(headword, gender));
        if (gender == Gender.NEUTER) {
            addIfPresent(candidates, genitiveExtended(headword));
            // Второе наращение основы, на -ен-: бре̏ме → бре̏мена, и̏ме → и̏мена.
            addIfPresent(candidates, dropLast(bare) + "ена");
            // Заимствования на -е ничего не теряют: дефиле̑ → дефиле̑а, деколте̑ → деколте̑а.
            addIfPresent(candidates, bare + "а");
        }
        if (gender == Gender.FEMININE) {
            // Беглое «а» при основе на -и: боја̏зан → боја̏зни.
            String fleeting = genitiveFleeting(headword);
            if (fleeting != null && fleeting.endsWith("а")) {
                addIfPresent(candidates, dropLast(fleeting) + "и");
            }
        }
        // Беглое «а» бывает и в женском роде на согласную: боја̏зан → боја̏зни.
        if (gender == Gender.MASCULINE || gender == Gender.FEMININE) {
            addIfPresent(candidates, genitiveFleeting(headword));
            addIfPresent(candidates, genitiveFleetingWithLToO(headword));
        }
        if (gender == Gender.MASCULINE) {
            // анђе̏о → анђе̏ла, би̏во → би̏вола: в именительном «л» основы перешло в «о».
            if (bare.endsWith("о")) {
                addIfPresent(candidates, dropLast(bare) + "ла");
                addIfPresent(candidates, bare + "ла");
            }
            // Заимствования на -и берут -ја: Де̏лхи → Де̏лхија.
            if (bare.endsWith("и")) {
                addIfPresent(candidates, bare + "ја");
            }
        }
        return java.util.List.copyOf(candidates);
    }

    /**
     * Полная регулярная парадигма для типов с однозначными окончаниями.
     * Исключения не угадываются: сохранённые словарные формы должны накладываться сверху.
     */
    public static java.util.List<Form> regularParadigm(String headword, Gender gender) {
        String word = bare(headword);
        java.util.List<Form> forms = new java.util.ArrayList<>();
        switch (typeOf(word, gender)) {
            case MASCULINE_CONSONANT -> {
                add(forms, word, "", "а", "у", "", "е", "ом", "у",
                        "ови", "ова", "овима", "ове", "ови", "овима", "овима");
            }
            case FEMININE_A -> {
                String stem = dropLast(word);
                add(forms, stem, "а", "е", "и", "у", "о", "ом", "и",
                        "е", "а", "ама", "е", "е", "ама", "ама");
            }
            case NEUTER_O, NEUTER_E -> {
                String stem = dropLast(word);
                String ending = word.substring(word.length() - 1);
                add(forms, stem, ending, "а", "у", ending, ending, "ом", "у",
                        "а", "а", "има", "а", "а", "има", "има");
            }
            default -> {
                // Для остальных типов окончание не определяется только по написанию.
            }
        }
        return java.util.List.copyOf(forms);
    }

    private static void add(java.util.List<Form> forms, String stem,
                            String nomSg, String genSg, String datSg, String accSg,
                            String vocSg, String insSg, String locSg,
                            String nomPl, String genPl, String datPl, String accPl,
                            String vocPl, String insPl, String locPl) {
        String[] grammar = {"nom.sg", "gen.sg", "dat.sg", "acc.sg", "voc.sg", "ins.sg", "loc.sg",
                "nom.pl", "gen.pl", "dat.pl", "acc.pl", "voc.pl", "ins.pl", "loc.pl"};
        String[] endings = {nomSg, genSg, datSg, accSg, vocSg, insSg, locSg,
                nomPl, genPl, datPl, accPl, vocPl, insPl, locPl};
        for (int i = 0; i < grammar.length; i++) {
            forms.add(new Form(grammar[i], stem + endings[i]));
        }
    }

    private static void addIfPresent(java.util.Set<String> target, String value) {
        if (value != null && !value.isEmpty()) {
            target.add(value);
        }
    }

    /**
     * Родительный падеж единственного числа — основной вариант, без знаков ударения.
     *
     * @return предполагаемая форма либо {@code null}, если тип склонения неизвестен
     */
    public static String genitiveSingular(String headword, Gender gender) {
        String bare = bare(headword);
        Type type = typeOf(headword, gender);
        return switch (type) {
            case MASCULINE_CONSONANT -> bare + "а";
            // Заимствования на гласную окончание просто прибавляют, ничего не теряя
            // и не вставляя: Ба’ку → Ба’куа, Фру’нзе → Фру’нзеа, ате’ље → ате’љеа.
            case MASCULINE_VOWEL -> bare + "а";
            case FEMININE_A -> dropLast(bare) + "е";
            case FEMININE_CONSONANT -> bare + "и";
            case NEUTER_O, NEUTER_E -> dropLast(bare) + "а";
            case NEUTER_EXTENDED -> dropLast(bare) + "ета";
            case UNKNOWN -> null;
        };
    }

    /**
     * Родительный падеж для среднего рода с наращением основы ({@code де’те → де’тета}).
     *
     * <p>Какие именно слова наращивают основу, по написанию не видно, поэтому для
     * среднего рода на {@code -е} приходится проверять оба варианта.
     */
    public static String genitiveExtended(String headword) {
        return dropLast(bare(headword)) + "ета";
    }

    /**
     * Родительный падеж с <b>беглым «а»</b> — гласной последнего слога, выпадающей
     * в косвенных падежах: {@code Абисина́ц → Абисинца}, {@code пе́так → петка}.
     *
     * <p>Признак — сочетание «согласная + а + согласная» в конце слова. Беглое «а»
     * есть далеко не у всех таких слов ({@code знак → знака}), поэтому правило даёт
     * <i>дополнительный</i> вариант, а не заменяет основной.
     *
     * @return форма с выпавшей гласной либо {@code null}, если слово под признак не подходит
     */
    public static String genitiveFleeting(String headword) {
        String bare = bare(headword);
        if (bare.length() < 3) {
            return null;
        }
        int last = bare.length() - 1;
        if (isConsonant(bare.charAt(last)) && bare.charAt(last - 1) == 'а'
                && isConsonant(bare.charAt(last - 2))) {
            // Основа и окончание оказываются встык, поэтому согласные подстраиваются:
            // безнож + ца → безношца, баб + ка → бапка.
            return Alternations.join(bare.substring(0, last - 1), bare.charAt(last) + "а");
        }
        return null;
    }

    /** Беглое «а» вместе со словарным переходом {@code л → о}: {@code бе’дилац → бе’диоца}. */
    public static String genitiveFleetingWithLToO(String headword) {
        String bare = bare(headword);
        if (bare.length() < 3) {
            return null;
        }
        int last = bare.length() - 1;
        if (isConsonant(bare.charAt(last)) && bare.charAt(last - 1) == 'а'
                && isConsonant(bare.charAt(last - 2))) {
            return Alternations.lToO(bare.substring(0, last - 1), bare.charAt(last) + "а");
        }
        return null;
    }

    private static boolean isConsonant(char letter) {
        return Character.isLetter(letter) && VOWELS.indexOf(letter) < 0;
    }

    /** Убирает знаки ударения и служебный разделитель основы. */
    private static String bare(String headword) {
        return Serbian.stripCombiningAccents(
                Serbian.stripAccents(Serbian.stripStemMarker(headword))).trim();
    }

    private static String dropLast(String value) {
        return value.isEmpty() ? value : value.substring(0, value.length() - 1);
    }

    /** Оканчивается ли слово на гласную. */
    static boolean endsWithVowel(String bare) {
        return !bare.isEmpty() && VOWELS.indexOf(bare.charAt(bare.length() - 1)) >= 0;
    }
}
