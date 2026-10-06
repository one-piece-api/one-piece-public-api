package dev.onepieceapi.publicapi.web.dto.response;

import java.util.List;

/** The page envelope every list of this API shares. */
public record PageResponse<T>(List<T> content, int page, int size, long totalElements, int totalPages) {

}
