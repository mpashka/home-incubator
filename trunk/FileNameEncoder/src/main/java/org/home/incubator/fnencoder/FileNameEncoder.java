package org.home.incubator.fnencoder;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: moukhataevs
 * Time: 3:21:04
 */
public class FileNameEncoder {

    private static final Logger log = Logger.getLogger("FileNameEncoder");

    private static final String[] ENCODE_PATTERNS = {
            "а-a" ,
            "б-b" ,
            "в-v" ,
            "г-g" ,
            "д-d" ,
            "е-e" ,
            "ё-ioq" ,
            "ж-zhq" ,
            "з-z" ,
            "и-i" ,
            "й-i'" ,
            "к-k" ,
            "л-l" ,
            "м-m" ,
            "н-n" ,
            "о-o" ,
            "п-p" ,
            "р-r" ,
            "с-s" ,
            "т-t" ,
            "у-y" ,
            "ф-f" ,
            "х-h" ,
            "ц-c" ,
            "ч-chcq" ,
            "ш-shq" ,
            "щ-shchq" ,
            "ъ-tq" ,
            "ы-iiq" ,
            "ь-mq" ,
            "э-ieq" ,
            "ю-iuq" ,
            "я-iaq" ,
    };
    private static final char LANGUAGE_SELECTOR_START = '(';
    private static final char LANGUAGE_SELECTOR_STOP = ')';
    private static final Map<Character, Language> LANGUAGE_BY_SELECTOR = new HashMap<Character, Language>();

    private static final class TranslitInfo {
        private char rusChar;
        private String translitString;

        private TranslitInfo(char rusChar, String translitString) {
            this.rusChar = rusChar;
            this.translitString = translitString;
        }

        public Character getRusChar() {
            return rusChar;
        }

        public String getTranslitString() {
            return translitString;
        }
    }

    private static final Comparator<TranslitInfo> TRANSLIT_LENGTH_COMPARATOR = new Comparator<TranslitInfo>() {
        public int compare(TranslitInfo o1, TranslitInfo o2) {
            return -(o1.getTranslitString().length() - o2.getTranslitString().length());
        }
    };
    
    private Map<Character, TranslitInfo> rus_map = new HashMap<Character, TranslitInfo>();
    private TranslitInfo[] eng_map;

    public FileNameEncoder() {
        for (String encodePattern : ENCODE_PATTERNS) {
            Character rusChar = encodePattern.charAt(0);
            String translitString = encodePattern.substring(2);
            TranslitInfo translitInfo = new TranslitInfo(rusChar, translitString);
            rus_map.put(rusChar, translitInfo);
        }
        eng_map = rus_map.values().toArray(new TranslitInfo[rus_map.size()]);
        Arrays.sort(eng_map, TRANSLIT_LENGTH_COMPARATOR);

        for (Language language : Language.values()) {
            LANGUAGE_BY_SELECTOR.put(language.getIndicator(), language);
        }
    }

    public String encode(String name) {
        StringBuilder result = new StringBuilder();
        int extensionIndex = name.lastIndexOf('.');
        int lastChar = extensionIndex > 0 ? extensionIndex : name.length();
        Language language = Language.English;
        for (int i = 0; i < lastChar; i++) {
            char myChar = name.charAt(i);
            Language newLanguage = Language.getLanguage(myChar);
            if (newLanguage != null && language != newLanguage) {
                language = newLanguage;
                result.append("" + LANGUAGE_SELECTOR_START + language.getIndicator() + LANGUAGE_SELECTOR_STOP);
            }
            if (language == Language.Russian) {
                translitRussianChar(myChar, result);
            } else {
                result.append(myChar);
            }
        }
        if (extensionIndex > 0) {
            result.append(name.substring(extensionIndex));
        }
        return result.toString();
    }

/*
    private String checkInvalidRussian(String name) {
        int pos = 0;
        int rusPos = name.indexOf("(r)", pos);
    }
*/

    public String decode(String name) {
        StringBuilder result = new StringBuilder();
        int extensionIndex = name.lastIndexOf('.');
        int endIndex = extensionIndex > 0 ? extensionIndex : name.length();
        int pos = 0;
        Language language = Language.English;
        String nameLowerCase = name.toLowerCase();
        while (pos < endIndex) {
            char myChar = name.charAt(pos);
            if (myChar == LANGUAGE_SELECTOR_START && pos + 2 < endIndex && name.charAt(pos + 2) == LANGUAGE_SELECTOR_STOP) {
                char languageChar = name.charAt(pos + 1);
                Language newLanguage = LANGUAGE_BY_SELECTOR.get(languageChar);
                if (newLanguage != null) {
                    language = newLanguage;
                    pos += 3;
                    continue;
                }
            }
            if (language == Language.English) {
                result.append(myChar);
            } else {
                int specialChar = Language.SPECIAL_CHARS.indexOf(myChar);
                if (specialChar != -1) {
                    result.append(myChar);
                } else {
                    TranslitInfo translitInfo = retranslitEnglish(nameLowerCase, pos);
                    if (translitInfo != null) {
                        result.append(Character.isLowerCase(myChar) ? translitInfo.getRusChar() : Character.toUpperCase(translitInfo.getRusChar()));
                        pos += translitInfo.getTranslitString().length();
                        continue;
                    } else {
                        result.append(myChar);
                    }
                }
            }
            pos++;
        }
        if (extensionIndex > 0) {
            result.append(name.substring(extensionIndex));
        }
        return result.toString();
    }

    static final Pattern invalid_rus = Pattern.compile("\\(r\\)(\\p{InCYRILLIC})");
    public String fixRusBug(String name) {
        name = invalid_rus.matcher(name).replaceAll("$1");
        return name;
    }

    private void translitRussianChar(char myChar, StringBuilder result) {
        boolean upperCase = Character.isUpperCase(myChar);
        char myLChar = Character.toLowerCase(myChar);
        TranslitInfo translitInfo = rus_map.get(myLChar);
        if (translitInfo == null) {
//            log.severe("Can't find translit for '" + myLChar + "'");
            result.append(myChar);
        } else {
            result.append(upperCase ? translitInfo.getTranslitString().toUpperCase() : translitInfo.getTranslitString());
        }
    }

    private TranslitInfo retranslitEnglish(String englishString, int pos) {
        for (TranslitInfo translitInfo : eng_map) {
            if (englishString.startsWith(translitInfo.getTranslitString(), pos)) return translitInfo;
        }
//        log.warning("Can't translate string : " + englishString.substring(pos));
        return null;
    }

    private enum Executor {
        encode {
            public String processFile(String fileName, FileNameEncoder encoder) {
                String newFileName = encoder.encode(fileName);
                String reoldFileName = encoder.decode(newFileName);
                if (!reoldFileName.equals(fileName)) {
                    log.severe("Invalid conversion. " + fileName + " != " + reoldFileName + " (" + newFileName + ")");
                    return null;
                }
                return newFileName;
            }}, decode {
            public String processFile(String fileName, FileNameEncoder encoder) {
                return encoder.decode(fileName);
            }}, test {
            public String processFile(String fileName, FileNameEncoder encoder) {
                String newFileName = encoder.encode(fileName);
                String reoldFileName = encoder.decode(newFileName);
                if (!reoldFileName.equals(fileName)) {
                    log.severe("Invalid conversion. " + fileName + " != " + reoldFileName + " (" + newFileName + ")");
                }
                return null;
            }}, fixBug {
            @Override
            public String processFile(String fileName, FileNameEncoder encoder) throws IOException {
                return encoder.fixRusBug(fileName);
            }};

        public abstract String processFile(String fileName, FileNameEncoder encoder) throws IOException;

        public File processFile(File file, FileNameEncoder encoder) throws IOException {
            String oldFileName = file.getName();
            String newFileName = processFile(oldFileName, encoder);
            if (newFileName == null || oldFileName.equals(newFileName)) return null;
            File newFile = new File(file.getParent(), newFileName);
            file.renameTo(newFile);
            return newFile;
        }
    }

    private File currentFile;
    private long count;
    private void processAllFiles(Executor executor, File file) throws IOException {
        currentFile = file;
        count++;
        File newFile = executor.processFile(file, this);
        if (newFile != null) file = newFile;
        if (file.isDirectory()) {
            for (File file1 : file.listFiles()) {
                processAllFiles(executor, file1);
            }
        }
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 2) throw new RuntimeException("Invalid args. First -encode, -decode, -test, second - dir : " + Arrays.toString(args));
        Executor executor = Executor.valueOf(args[0]);
        File file = new File(args[1]);
        if (!file.exists()) {
            throw new RuntimeException("File '" + file.getCanonicalPath() + "' doesn't exist");
        }

        final FileNameEncoder encoder = new FileNameEncoder();

        Thread checkThread = new Thread() {
            @Override
            public void run() {
                while (true) {
                    try {
                        System.out.println(encoder.count  + " / " + encoder.currentFile.getCanonicalPath());
                        Thread.sleep(1000 * 60);
                    } catch (Exception e) {
                    }
                }
            }
        };
        checkThread.setDaemon(true);
        checkThread.start();

        encoder.processAllFiles(executor, file);
    }
}
