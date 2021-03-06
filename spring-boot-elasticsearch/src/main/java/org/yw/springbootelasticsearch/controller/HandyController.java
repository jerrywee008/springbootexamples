package org.yw.springbootelasticsearch.controller;


import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.List;
import java.util.concurrent.ExecutionException;


import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.yw.springbootelasticsearch.model.Handy;
import org.yw.springbootelasticsearch.model.Handy;
import org.yw.springbootelasticsearch.model.User;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;


@Controller
@RequestMapping("/rest/handys")
public class HandyController implements MeterBinder {

    @Autowired
    Client client;

    private Counter handyCounter = null;

    @Override
    public void bindTo(MeterRegistry meterRegistry) {
        this.handyCounter = meterRegistry.counter("handy.count");
    }

    @PostMapping("/create")
    @ResponseBody
    public String create(@RequestBody Handy handy) throws IOException {

        IndexResponse response = client.prepareIndex("handys", "handy", handy.getId())
                .setSource(jsonBuilder()
                        .startObject()
                        .field("brand", handy.getBrand())
                        .field("details", handy.getDetails())
                        .endObject()
                )
                .get();
        System.out.println("response id:" + response.getId());
        handyCounter.increment();
        return response.getResult().toString();
    }


    @GetMapping("/view/{id}")
    @ResponseBody
    public Map<String, Object> view(@PathVariable final String id) {
        GetResponse getResponse = client.prepareGet("handys", "handy", id).get();
        System.out.println(getResponse.getSource());
        return getResponse.getSource();
    }



    @GetMapping("/view/brand/{field}")
    @ResponseBody
    public Map<String, Object> searchByName(@PathVariable final String field) {
        Map<String, Object> map = null;
        SearchResponse response = client.prepareSearch("handys")
                .setTypes("handy")
                .setSearchType(SearchType.QUERY_THEN_FETCH)
                .setQuery(QueryBuilders.matchQuery("brand", field))
                .get();
        List<SearchHit> searchHits = Arrays.asList(response.getHits().getHits());
        map = searchHits.get(0).getSourceAsMap();
        return map;

    }

    @GetMapping("/update/{id}")
    @ResponseBody
    public String update(@PathVariable final String id) throws IOException {

        UpdateRequest updateRequest = new UpdateRequest();
        updateRequest.index("handys")
                .type("handy")
                .id(id)
                .doc(jsonBuilder()
                        .startObject()
                        .field("brand", "XiaoMi")
                        .endObject());
        try {
            UpdateResponse updateResponse = client.update(updateRequest).get();
            System.out.println(updateResponse.status());
            return updateResponse.status().toString();
        } catch (InterruptedException | ExecutionException e) {
            System.out.println(e);
        }
        return "Exception";
    }

    @GetMapping("/delete/{id}")
    @ResponseBody
    public String delete(@PathVariable final String id) {
        DeleteResponse deleteResponse = client.prepareDelete("handys", "handy", id).get();
        System.out.println(deleteResponse.getResult().toString());
        return deleteResponse.getResult().toString();
    }
}
