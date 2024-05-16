package org.mpashka.test.proto;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Optional;
import java.util.logging.Logger;

import com.google.protobuf.util.JsonFormat;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.mpashka.test.protobuf.AddressBookProtos;

@Slf4j
public class TestMigration {

    private static final File dir = new File("/home/ya-pashka/Projects/github/m_pashka/home-incubator/java-tests/misc/proto/src/main/resources");
    private static final File old0 = new File(dir, "old.bin");

/*
    @Test
    public void generateOld() throws Exception {
        AddressBookProtos.Person person = AddressBookProtos.Person.newBuilder()
                .setId(10)
                .setName("name_old")
                .setEmail("email_old")
                .setOptionalOld10("old10")
                .setRequiredOld11("old11")
                .build();

        try (FileOutputStream out = new FileOutputStream(old0)) {
            person.writeTo(out);
        }
    }
*/

    @Test
    public void readOld() throws Exception {
        try (InputStream in = new FileInputStream(old0)) {
            AddressBookProtos.Person person = AddressBookProtos.Person.parseFrom(in);
            log.info("Person: {}", person);
            log.info("14: {}", person.hasOptionalNew14());
            log.info("15: {}", person.hasRequiredNew15());
        }
    }

    @Test
    public void testOptional() throws Exception {
        try (InputStream in = new FileInputStream(old0)) {
            AddressBookProtos.Person person = AddressBookProtos.Person.parseFrom(in);
            log.info("Person JSON: {}", JsonFormat.printer().preservingProtoFieldNames().includingDefaultValueFields().print(person));
            log.info("Person: {}", person);
            log.info("Has full name: {}", person.hasFullName());
            AddressBookProtos.Name fullName = person.getFullName();
            log.info("Full name: {}", fullName);
            log.info("  First: {}", fullName.getFirst());
//            log.info("Person JSON: {}", JsonFormat.printer().preservingProtoFieldNames().includingDefaultValueFields().print(person));
        }
    }
}
