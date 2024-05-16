package org.mpashka.test.misc.retrofit;

import java.util.List;

import retrofit2.Call;
import retrofit2.Retrofit;

public class GitHubService {
    public void doRetrofit() {
        Retrofit retrofit = new Retrofit.Builder()
//                .baseUrl("https://api.github.com/")
                .baseUrl("http://localhost:1500/")
                .build();
        GitHubApi service = retrofit.create(GitHubApi.class);
        Call<List<String>> repos = service.listRepos("octocat", new GitHubApi.ReqObj("aaa", 2));


    }
}
