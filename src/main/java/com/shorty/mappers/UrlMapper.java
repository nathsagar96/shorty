package com.shorty.mappers;

import com.shorty.dtos.responses.UrlResponse;
import com.shorty.dtos.responses.UrlStatsResponse;
import com.shorty.entities.UrlMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Value;

@Mapper(componentModel = "spring")
public abstract class UrlMapper {
    @Value("${app.base-url:http://localhost:8080}")
    protected String baseUrl;

    @Mapping(target = "shortUrl", source = "shortCode", qualifiedByName = "buildShortUrl")
    public abstract UrlResponse toUrlResponse(UrlMapping urlMapping);

    @Mapping(target = "shortUrl", source = "shortCode", qualifiedByName = "buildShortUrl")
    public abstract UrlStatsResponse toUrlStatsResponse(UrlMapping urlMapping);

    @Named("buildShortUrl")
    protected String buildShortUrl(String shortCode) {
        return baseUrl + "/" + shortCode;
    }
}
