package com.shorty.mappers;

import com.shorty.dtos.responses.UrlResponse;
import com.shorty.entities.UrlMapping;
import org.mapstruct.*;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UrlMapper {

    @Mapping(target = "shortUrl", expression = "java(buildShortUrl(mapping.getShortCode(), baseUrl))")
    UrlResponse toResponse(UrlMapping mapping, @Context String baseUrl);

    default String buildShortUrl(String shortCode, String baseUrl) {
        return baseUrl + "/" + shortCode;
    }
}
