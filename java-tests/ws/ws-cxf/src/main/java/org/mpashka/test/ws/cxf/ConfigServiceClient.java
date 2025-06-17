package org.mpashka.test.ws.cxf;

import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;

import lombok.extern.slf4j.Slf4j;
import ru.atc.idecs.config.ws.ConfigService;
import ru.atc.idecs.config.ws.ConfigService_Service;
import ru.atc.idecs.config.ws.ListParametersRequest;
import ru.atc.idecs.config.ws.ListParametersResponse;

@Slf4j
public class ConfigServiceClient {
    public static void main(String[] args) {
        ConfigService configServiceSOAP = new ConfigService_Service().getConfigServiceSOAP();

        BindingProvider provider = (BindingProvider) configServiceSOAP;
        provider.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, "http://localhost:8080/");

        ListParametersRequest listParametersRequest = new ListParametersRequest();
        listParametersRequest.setFilter("my-filt");
        listParametersRequest.setLocation("my-loc");
        ListParametersResponse listParametersResponse = configServiceSOAP.listParameters(listParametersRequest);
        log.info("Response: {}", listParametersResponse);
        listParametersResponse.getList().getParam()
                .forEach(p -> log.info("  Params: {}", p.getDescription()));
    }
}
