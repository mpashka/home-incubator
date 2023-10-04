package org.mpashka.tests.misc.retrofit;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import retrofit2.Call;
import retrofit2.Converter;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;
import retrofit2.converter.protobuf.ProtoConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Path;

import org.mpashka.tests.misc.retrofit.proto.AddressBookProtos;

/**
 * while true ; do  echo -e "HTTP/1.1 200 OK\n\n $(date)\n\n" | nc -l -p 1500  ; done
 */
@Slf4j
public class TestRetrofit {
    @Test
    public void testJsonCall() throws Exception {
        test(JacksonConverterFactory.create(), a -> a.listReposCall("octocat", new ReqObj("my-str", 100500))
                .execute()
                .body());
    }

    @Test
    public void testJsonFuture() throws Exception {
        test(JacksonConverterFactory.create(), a -> a.listReposFuture("octocat", new ReqObj("my-str", 100500))
                .get());
/*
POST /users/octocat/repos HTTP/1.1
Content-Type: application/json; charset=UTF-8
Content-Length: 30
Host: localhost:1500
Connection: Keep-Alive
Accept-Encoding: gzip
User-Agent: okhttp/3.14.9
*/
    }

    @Test
    public void testProtobufCall() throws Exception {
        test(ProtoConverterFactory.create(), service -> service.listReposProtoCall("octocat", AddressBookProtos.AddressBook.newBuilder().build())
                .execute()
                .body());
/*
POST /users/octocat/repos HTTP/1.1
Content-Type: application/x-protobuf
Content-Length: 0
Host: localhost:1500
Connection: Keep-Alive
Accept-Encoding: gzip
User-Agent: okhttp/3.14.9
 */
    }

    @Test
    public void testProtobufFuture() throws Exception {
        test(ProtoConverterFactory.create(), service -> service.listReposProtoFuture("octocat", AddressBookProtos.AddressBook.newBuilder().build())
                .get());
    }

    private void test(Converter.Factory factory, FunctionEx<MyApi, Object, Exception> fn) throws Exception {
        Retrofit retrofit = new Retrofit.Builder()
//                .baseUrl("https://api.github.com/")
                .baseUrl("http://localhost:1500/")
                .addConverterFactory(factory)
                .build();
        MyApi service = retrofit.create(MyApi.class);
        Object result = fn.apply(service);
        log.info("Repos: {}", result);
    }


    public interface MyApi {
        @POST("users/{user}/repos")
        Call<List<String>> listReposCall(@Path("user") String user, @Body ReqObj reqObj);

        @POST("users/{user}/repos")
        CompletableFuture<List<String>> listReposFuture(@Path("user") String user, @Body ReqObj reqObj);

        @POST("users/{user}/repos")
        Call<AddressBookProtos.MyResult> listReposProtoCall(@Path("user") String user, @Body AddressBookProtos.AddressBook reqObj);

        @POST("users/{user}/repos")
        CompletableFuture<AddressBookProtos.MyResult> listReposProtoFuture(@Path("user") String user, @Body AddressBookProtos.AddressBook reqObj);
    }

    record ReqObj(
            String str,
            int num1
    ) {
    }

    @FunctionalInterface
    public interface FunctionEx<T, R, E extends Throwable> {

        /**
         * Applies this function to the given argument.
         *
         * @param t the function argument
         * @return the function result
         */
        R apply(T t) throws E;

    }
}
