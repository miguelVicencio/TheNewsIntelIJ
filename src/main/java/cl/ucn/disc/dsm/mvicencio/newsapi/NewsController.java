/*
 *
 * Copyright <2021> <Miguel Vicencio Gomez>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 *  of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following
 *   conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial
 * portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT
 * OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */

package cl.ucn.disc.dsm.mvicencio.newsapi;

import cl.ucn.disc.dsm.mvicencio.newsapi.model.News;
import com.kwabenaberko.newsapilib.models.Article;
import com.kwabenaberko.newsapilib.models.response.ArticleResponse;
import com.kwabenaberko.newsapilib.network.APIClient;
import com.kwabenaberko.newsapilib.network.APIService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZonedDateTime;
import retrofit2.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The controller of News
 *
 * @author Miguel Vicencio Gomez
 */
@Slf4j
@RestController
public class NewsController {

    /**
     * the repository of News
     */
    private final NewsRepository newsRepository;

    /**
     * the Constructor of NewsController
     * @param newsRepository to use
     */
    public NewsController(NewsRepository newsRepository){
        this.newsRepository = newsRepository;
    }
    /**
     * @return all the news in the backend
     */
    @GetMapping("/v1/news")

    public List<News> all(@RequestParam(required = false,defaultValue = "false") Boolean reload) {
        // is reload  --> get news from NewsAPI.org
        if(reload){
            // FIXME: Avoid the duplicated!!
            this.reloadNewsFromNewsApi();
        }

        // http://servidor.dominio:8080/v1/news?reload=true

        //Equals to select* from News
        final List<News> theNews = this.newsRepository.findAll();

        return theNews;
    }

    /**
     * get the News from NewsAPI and save into the database
     */
    private void reloadNewsFromNewsApi() {
        // WARNING: Just for test
        final String API_KEY = "3581e2fbd3064324956966591773df9a";

        // 1. Create the APIService from APIClient
        final APIService apiService = APIClient.getAPIService();

        //2. Build the request params
        final Map<String,String> reqParams = new HashMap<>();
        reqParams.put("apiKey",API_KEY);
        // Filter by category
        reqParams.put("category", "technology");
        // the number of request is 20 to 100
        reqParams.put("pageSize","100");
        // the language in spanish
        reqParams.put("language","es");
        // 3. call the request
        try {
            Response<ArticleResponse> articlesResponse =
                    apiService.getTopHeadlines(reqParams).execute();
            // Successful!
            if(articlesResponse.isSuccessful()){
                //TODO: Check for getArticles != null
                List<Article> articles = articlesResponse.body().getArticles();
                // List<Article> to List<News>
                List<News> news = new ArrayList<>();
                for(Article article : articles){
                    news.add(toNews(article));
                }
                //4. save into the local database
                this.newsRepository.saveAll(news);

            }
        } catch (IOException e) {
            log.error("Getting the Articles from NewsAPI", e);
        }

    }
    /**
     * Convert a article to News.
     * @param article to convert.
     *
     * @return The News
     */
    private static News toNews(final Article article){

        // Protection: author
        if(article.getAuthor() == null || article.getAuthor().length() < 3){
            article.setAuthor("No Author*");
        }
        // Protection: title
        if(article.getTitle() == null || article.getTitle().length() < 3){
            article.setTitle("No Title*");
        }
        //Protection : description
        if(article.getDescription() == null || article.getDescription().length() < 4){
            article.setDescription("No Description*");
        }
        // Parse the date and fix the zone
        ZonedDateTime publishedAt = ZonedDateTime.
                parse(article.getPublishedAt()).
                // correct from UTC to localTime (chile)
                withZoneSameInstant(ZoneId.of("-3"));
        // Construct a News form Article
        return new News(
                article.getTitle(),
                article.getSource().getName(),
                article.getAuthor(),
                article.getUrl(),
                article.getUrlToImage(),
                article.getDescription(),
                article.getDescription(),
                publishedAt
        );
    }

    /**
     * @param id of News to retrieve.
     * @return the News.
     */
    @GetMapping("/v1/news/{id}")
    public News one(@PathVariable final Long id) {
        return this.newsRepository.findById(id).orElseThrow(()-> new RuntimeException("News Not Found :( ERROR 404"));
    }


}
