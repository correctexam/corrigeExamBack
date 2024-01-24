package fr.istic.config;

import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;

import io.vertx.ext.web.Router;

@ApplicationScoped
public class SPARouting {
    private static final String[] PATH_PREFIXES = { "/api/", "/q/", "/management" };
    private static final Predicate<String> FILE_NAME_PREDICATE = Pattern.compile(".*[.][a-zA-Z\\d]+").asMatchPredicate();

    public void init(@Observes Router router) {
        router.get("/*").handler(rc -> {
            final String path = rc.normalizedPath();
            if (!path.equals("/")
                    && Stream.of(PATH_PREFIXES).noneMatch(path::startsWith)
                    && !FILE_NAME_PREDICATE.test(path)) {
                rc.reroute("/");
            } else {
                rc.next();
            }
        });
    }
}
