package org.mpashka.tests.misc.retrofit;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface GitHubApi {
    @POST("users/{user}/repos")
    Call<List<String>> listRepos(@Path("user") String user, @Body ReqObj reqObj);


    record ReqObj(
            String str,
            int num1
    ) {}
}
