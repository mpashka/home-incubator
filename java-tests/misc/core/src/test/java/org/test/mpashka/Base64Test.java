package org.test.mpashka;


import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class Base64Test {


    @Test
    public void testEncode() {
        String src = "{\"firstName\":\"Ангелина\",\"lastName\":\"Иванова\",\"orgType\":\"B\",\"orgName\":\"ООО Яркое\",\"orgOGRN\":5448454444444,\"globalRole\":\"P\",\"orgOid\":1078941035,\"authToken\":\"1078941035@@AL20@@1\",\"personSNILS\":null,\"middleName\":null,\"userName\":null,\"userId\":1078941035}";
        String dst = Base64.getEncoder().encodeToString(src.getBytes());
        System.out.println("dst = " + dst);
        String dst2 = new String(Base64.getDecoder().decode(dst.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
        System.out.println("dst2 = " + dst2);
    }
}
