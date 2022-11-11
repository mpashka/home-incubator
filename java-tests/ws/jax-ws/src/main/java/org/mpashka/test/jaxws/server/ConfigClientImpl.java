package org.mpashka.test.jaxws.server;

import org.mpashka.test.jaxws.gen.ConfigService;
import org.mpashka.test.jaxws.gen.ConfigService_Service;
import org.mpashka.test.jaxws.gen.ListParametersRequest;

import jakarta.xml.ws.BindingProvider;

public class ConfigClientImpl {
    public static void main(String[] args) {
        ConfigService_Service service = new ConfigService_Service();
        ConfigService configServiceSOAP = service.getConfigServiceSOAP();

        BindingProvider provider = (BindingProvider) configServiceSOAP;
        provider.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, "http://localhost:8080/");

        configServiceSOAP.listParameters(new ListParametersRequest());
    }
}
