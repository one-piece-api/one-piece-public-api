package dev.onepieceapi.publicapi.web.dto.response;

import java.util.Map;

/**
 * The entry point of the API: where each part lives, as templates relative to the API
 * root (plan D7).
 *
 * @param languages the path of the language list
 * @param resources the path template of each resource, by name
 */
public record IndexResponse(String languages, Map<String, String> resources) {

}
