package com.shorty.urls;

import com.shorty.common.exception.ResourceNotFoundException;
import com.shorty.common.exception.ValidationException;
import com.shorty.urls.dto.*;
import com.shorty.urls.dto.BulkCreateUrlRequest;
import com.shorty.urls.dto.BulkError;
import com.shorty.urls.dto.BulkOperationResponse;
import com.shorty.users.User;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class BulkUrlService {

  private final UrlService urlService;
  private final UrlRepository urlRepository;

  public BulkOperationResponse<UrlResponse> bulkCreateUrls(
      BulkCreateUrlRequest request, User user, String baseUrl) {
    List<UrlResponse> successful = new ArrayList<>();
    List<BulkError> errors = new ArrayList<>();

    for (int i = 0; i < request.urls().size(); i++) {
      CreateUrlRequest urlRequest = request.urls().get(i);

      try {
        Url createdUrl =
            urlService.createShortUrl(
                urlRequest.url(),
                urlRequest.customCode(),
                urlRequest.visibility(),
                urlRequest.expiresAt(),
                urlRequest.clickLimit(),
                user);

        successful.add(UrlResponse.from(createdUrl, baseUrl));

      } catch (Exception e) {
        errors.add(new BulkError(i, urlRequest.url(), e.getMessage()));
      }
    }

    return new BulkOperationResponse<>(
        successful, errors, request.urls().size(), successful.size(), errors.size());
  }

  public BulkOperationResponse<UUID> bulkDeleteUrls(List<UUID> urlIds, UUID userId) {
    List<UUID> successful = new ArrayList<>();
    List<BulkError> errors = new ArrayList<>();

    for (int i = 0; i < urlIds.size(); i++) {
      UUID urlId = urlIds.get(i);

      try {
        Url url =
            urlRepository
                .findById(urlId)
                .orElseThrow(
                    () -> new ResourceNotFoundException("URL not found with id: " + urlId));

        if (!url.getUser().getId().equals(userId)) {
          throw new ValidationException("You don't have permission to delete this URL");
        }

        urlRepository.delete(url);
        successful.add(urlId);

      } catch (Exception e) {
        errors.add(new BulkError(i, urlId.toString(), e.getMessage()));
      }
    }

    return new BulkOperationResponse<>(
        successful, errors, urlIds.size(), successful.size(), errors.size());
  }

  public BulkOperationResponse<UrlResponse> bulkUpdateVisibility(
      List<UUID> urlIds, UrlVisibility visibility, UUID userId, String baseUrl) {
    List<UrlResponse> successful = new ArrayList<>();
    List<BulkError> errors = new ArrayList<>();

    for (int i = 0; i < urlIds.size(); i++) {
      UUID urlId = urlIds.get(i);

      try {
        Url url =
            urlRepository
                .findById(urlId)
                .orElseThrow(
                    () -> new ResourceNotFoundException("URL not found with id: " + urlId));

        if (!url.getUser().getId().equals(userId)) {
          throw new ValidationException("You don't have permission to update this URL");
        }

        url.setVisibility(visibility);
        Url savedUrl = urlRepository.save(url);
        successful.add(UrlResponse.from(savedUrl, baseUrl));

      } catch (Exception e) {
        errors.add(new BulkError(i, urlId.toString(), e.getMessage()));
      }
    }

    return new BulkOperationResponse<>(
        successful, errors, urlIds.size(), successful.size(), errors.size());
  }

  public BulkOperationResponse<UrlResponse> bulkToggleStatus(
      List<UUID> urlIds, boolean active, UUID userId, String baseUrl) {
    List<UrlResponse> successful = new ArrayList<>();
    List<BulkError> errors = new ArrayList<>();

    for (int i = 0; i < urlIds.size(); i++) {
      UUID urlId = urlIds.get(i);

      try {
        Url url =
            urlRepository
                .findById(urlId)
                .orElseThrow(
                    () -> new ResourceNotFoundException("URL not found with id: " + urlId));

        if (!url.getUser().getId().equals(userId)) {
          throw new ValidationException("You don't have permission to update this URL");
        }

        url.setActive(active);
        Url savedUrl = urlRepository.save(url);
        successful.add(UrlResponse.from(savedUrl, baseUrl));

      } catch (Exception e) {
        errors.add(new BulkError(i, urlId.toString(), e.getMessage()));
      }
    }

    return new BulkOperationResponse<>(
        successful, errors, urlIds.size(), successful.size(), errors.size());
  }
}
