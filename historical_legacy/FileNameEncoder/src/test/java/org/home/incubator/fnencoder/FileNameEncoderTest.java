/*
 * Copyright (c) 2005-2008 jNetX.
 * http://www.jnetx.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * jNetX. You shall not disclose such Confidential Information and
 * shall use it only in accordance with the terms of the license
 * agreement you entered into with jNetX.
 *
 * $Id: FileNameEncoderTest $
 */
package org.home.incubator.fnencoder;

import org.junit.Test;

import java.io.FileReader;
import java.io.LineNumberReader;
import java.util.logging.Logger;

import static org.junit.Assert.assertEquals;

/**
 * todo [!] Create javadocs for org.mpn.fnencoder.FileNameEncoderTest here
 *
 * @author <a href="mailto:pmoukhataev@jnetx.ru">Pavel Moukhataev</a>
 * @version $Revision: 1.1 $
 */
public class FileNameEncoderTest {

    static final Logger log = Logger.getLogger("slee.FileNameEncoderTest");

    private FileNameEncoder fileNameEncoder = new FileNameEncoder();
    private static final String RUS_CHARS = "йцукенгшщзхъфывапролджэячсмитьбюё.`~!@#$%^&*()_+-=[]{};':\"\\|,./<>?qwertyuiopasdfghjklzxcvbnm";

    @Test
    public void testNameEncode() {
        assertEncodeDecode("(r)privet.mvi", "привет.mvi");
        assertEncodeDecode("(r)pri_vet.mvi", "при_вет.mvi");
        assertEncodeDecode("(r)privet(e)world.mvi", "приветworld.mvi");
        assertEncodeDecode("world(r)privet.mvi", "worldпривет.mvi");

        assertEncodeDecode(RUS_CHARS);
        assertEncodeDecode("ч");
        assertEncodeDecode("куча презентаций по НЕЕ.rar");
        assertEncodeDecode("КАМАСУТРА ДЛЯ ОРАТОРА.ZIP");
        assertEncodeDecode("шарики.tif");
    }

    @Test
    public void testFile() throws Exception {
        LineNumberReader in = new LineNumberReader(new FileReader("Errors.txt"));
        String inLine;
        while ((inLine = in.readLine()) != null) {
            assertEncodeDecode(inLine);
        }
        in.close();
    }

    @Test
    public void testGlobal() {
        int minRus = 1000;
        int maxRus = 1200;
        for (int c1 = minRus; c1 < maxRus; c1++) {
            for (int c2 = minRus; c2 < maxRus; c2++) {
                for (int c3 = minRus; c3 < maxRus; c3++) {
                    assertEncodeDecode("" + ((char) c1) + ((char)c2) + ((char)c3));
                }
            }
        }

    }

    private void assertEncodeDecode(String eng, String rus) {
        assertEquals(eng, fileNameEncoder.encode(rus));
        assertEquals(rus, fileNameEncoder.decode(eng));
    }

    private void assertEncodeDecode(String rus) {
        String eng = fileNameEncoder.encode(rus);
        String rusNew = fileNameEncoder.decode(eng);
        assertEquals("English - " + eng, rus, rusNew);
    }

    @Test
    public void testRegexp() {
        String name = "(r)ВиниПух";
        name = FileNameEncoder.invalid_rus.matcher(name).replaceAll("$1");
        assertEquals("ВиниПух", name);

        String name2 = "(r)VinniPuh";
        name = FileNameEncoder.invalid_rus.matcher(name2).replaceAll("$1");
        assertEquals(name2, name);
    }
}
