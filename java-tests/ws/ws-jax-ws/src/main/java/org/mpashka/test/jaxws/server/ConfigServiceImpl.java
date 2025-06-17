package org.mpashka.test.jaxws.server;


import org.mpashka.test.jaxws.gen.AbstractResponse;
import org.mpashka.test.jaxws.gen.CheckServiceStatusRequest;
import org.mpashka.test.jaxws.gen.CheckServiceStatusResponse;
import org.mpashka.test.jaxws.gen.ConfigService;
import org.mpashka.test.jaxws.gen.GetDateParameterResponse;
import org.mpashka.test.jaxws.gen.GetDecimalParameterResponse;
import org.mpashka.test.jaxws.gen.GetIntParameterResponse;
import org.mpashka.test.jaxws.gen.GetLocksRequest;
import org.mpashka.test.jaxws.gen.GetLocksResponse;
import org.mpashka.test.jaxws.gen.GetParameterRequest;
import org.mpashka.test.jaxws.gen.GetStrParameterResponse;
import org.mpashka.test.jaxws.gen.ListParametersRequest;
import org.mpashka.test.jaxws.gen.ListParametersResponse;
import org.mpashka.test.jaxws.gen.SetDateParameterRequest;
import org.mpashka.test.jaxws.gen.SetDecimalParameterRequest;
import org.mpashka.test.jaxws.gen.SetIntParameterRequest;
import org.mpashka.test.jaxws.gen.SetStrParameterRequest;

import jakarta.jws.WebMethod;
import jakarta.jws.WebService;
import jakarta.xml.ws.Endpoint;
import jakarta.xml.ws.soap.SOAPBinding;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@WebService(endpointInterface = "org.mpashka.test.jaxws.gen.ConfigService")
public class ConfigServiceImpl implements ConfigService {
    @Override
    public AbstractResponse setIntParameter(SetIntParameterRequest setIntParameterRequest) {
        return null;
    }

    @Override
    public GetIntParameterResponse getIntParameter(GetParameterRequest getIntParameterRequest) {
        return null;
    }

    @Override
    public AbstractResponse setDecimalParameter(SetDecimalParameterRequest setDecimalParameterRequest) {
        return null;
    }

    @Override
    public GetDecimalParameterResponse getDecimalParameter(GetParameterRequest getDecimalParameterRequest) {
        return null;
    }

    @Override
    public AbstractResponse setDateParameter(SetDateParameterRequest setDateParameterRequest) {
        return null;
    }

    @Override
    public GetDateParameterResponse getDateParameter(GetParameterRequest getDateParameterRequest) {
        return null;
    }

    @Override
    public AbstractResponse setStrParameter(SetStrParameterRequest setStrParameterRequest) {
        return null;
    }

    @Override
    public GetStrParameterResponse getStrParameter(GetParameterRequest getStrParameterRequest) {
        return null;
    }

    @Override
    public AbstractResponse setTextParameter(SetStrParameterRequest setTextParameterRequest) {
        return null;
    }

    @Override
    public GetStrParameterResponse getTextParameter(GetParameterRequest getTextParameterRequest) {
        return null;
    }

    @WebMethod
    @Override
    public ListParametersResponse listParameters(ListParametersRequest listParametersRequest) {
        log.info("List parameters: {}", listParametersRequest);
        return null;
    }

    @Override
    public GetLocksResponse getLocks(GetLocksRequest getLocksRequest) {
        return null;
    }

    @Override
    public CheckServiceStatusResponse checkServiceStatus(CheckServiceStatusRequest checkServiceStatusRequest) {
        return null;
    }

    public static void main(String[] args) {
/*
        System.setProperty("com.sun.xml.ws.transport.http.client.HttpTransportPipe.dump", "true");
        System.setProperty("com.sun.xml.internal.ws.transport.http.client.HttpTransportPipe.dump", "true");
        System.setProperty("com.sun.xml.ws.transport.http.HttpAdapter.dump", "true");
        System.setProperty("com.sun.xml.internal.ws.transport.http.HttpAdapter.dump", "true");
        System.setProperty("com.sun.xml.internal.ws.transport.http.HttpAdapter.dumpTreshold", "999999");
*/

        log.info("Starting server...");

        Endpoint endpoint = Endpoint.create(SOAPBinding.SOAP11HTTP_BINDING, new ConfigServiceImpl());
        endpoint.publish("http://localhost:8080/");
//        Endpoint.publish(
//                "http://localhost:8080/",
//                new ConfigServiceImpl());
    }
}
