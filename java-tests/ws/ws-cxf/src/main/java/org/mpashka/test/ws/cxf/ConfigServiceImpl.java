package org.mpashka.test.ws.cxf;

import javax.xml.ws.Endpoint;

import org.apache.cxf.feature.LoggingFeature;

import lombok.extern.slf4j.Slf4j;
import ru.atc.idecs.common.util.ws.type.AbstractResponse;
import ru.atc.idecs.config.ws.CheckServiceStatusRequest;
import ru.atc.idecs.config.ws.CheckServiceStatusResponse;
import ru.atc.idecs.config.ws.ConfigParameter;
import ru.atc.idecs.config.ws.ConfigService;
import ru.atc.idecs.config.ws.GetDateParameterResponse;
import ru.atc.idecs.config.ws.GetDecimalParameterResponse;
import ru.atc.idecs.config.ws.GetIntParameterResponse;
import ru.atc.idecs.config.ws.GetLocksRequest;
import ru.atc.idecs.config.ws.GetLocksResponse;
import ru.atc.idecs.config.ws.GetParameterRequest;
import ru.atc.idecs.config.ws.GetStrParameterResponse;
import ru.atc.idecs.config.ws.ListParametersRequest;
import ru.atc.idecs.config.ws.ListParametersResponse;
import ru.atc.idecs.config.ws.ParameterList;
import ru.atc.idecs.config.ws.SetDateParameterRequest;
import ru.atc.idecs.config.ws.SetDecimalParameterRequest;
import ru.atc.idecs.config.ws.SetIntParameterRequest;
import ru.atc.idecs.config.ws.SetStrParameterRequest;

@Slf4j
public class ConfigServiceImpl implements ConfigService {
    @Override
    public GetLocksResponse getLocks(GetLocksRequest getLocksRequest) {
        return null;
    }

    @Override
    public GetDecimalParameterResponse getDecimalParameter(GetParameterRequest getDecimalParameterRequest) {
        return null;
    }

    @Override
    public AbstractResponse setStrParameter(SetStrParameterRequest setStrParameterRequest) {
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
    public AbstractResponse setDateParameter(SetDateParameterRequest setDateParameterRequest) {
        return null;
    }

    @Override
    public GetStrParameterResponse getTextParameter(GetParameterRequest getTextParameterRequest) {
        return null;
    }

    @Override
    public AbstractResponse setIntParameter(SetIntParameterRequest setIntParameterRequest) {
        return null;
    }

    @Override
    public GetStrParameterResponse getStrParameter(GetParameterRequest getStrParameterRequest) {
        return null;
    }

    @Override
    public CheckServiceStatusResponse checkServiceStatus(CheckServiceStatusRequest checkServiceStatusRequest) {
        return null;
    }

    @Override
    public AbstractResponse setTextParameter(SetStrParameterRequest setTextParameterRequest) {
        return null;
    }

    @Override
    public GetDateParameterResponse getDateParameter(GetParameterRequest getDateParameterRequest) {
        return null;
    }

    @Override
    public ListParametersResponse listParameters(ListParametersRequest listParametersRequest) {
        log.info("Request: {} -> {}, {}", listParametersRequest, listParametersRequest.getFilter(), listParametersRequest.getLocation());
        ListParametersResponse listParametersResponse = new ListParametersResponse();
        ParameterList parameterList = new ParameterList();
        ConfigParameter param = new ConfigParameter();
        param.setDescription("my-descr");
        parameterList.getParam().add(param);
        listParametersResponse.setList(parameterList);
        return listParametersResponse;
    }

    public static void main(String[] args) {
        log.info("Starting server...");
        ConfigService implementor = new ConfigServiceImpl();
        Endpoint.publish("http://localhost:8080/", implementor);
//                new LoggingFeature()
    }
}
