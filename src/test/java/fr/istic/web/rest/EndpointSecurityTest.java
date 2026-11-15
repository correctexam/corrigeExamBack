package fr.istic.web.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.junit.jupiter.api.Test;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;

import io.quarkus.security.Authenticated;
import jakarta.annotation.security.DenyAll;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;

/**
 * Every REST endpoint declares who may call it ({@code @RolesAllowed}, {@code @Authenticated}, {@code @PermitAll}
 * or {@code @DenyAll}, on the method or its class), unless it is listed below with the reason.
 * <p>
 * The lists must match exactly: a new endpoint without annotation fails the build, and so does a listed endpoint
 * that has been secured since (remove it from the list). Plain reflection, no Quarkus boot.
 */
class EndpointSecurityTest {

    /** Public by design. */
    private static final Set<String> PUBLIC = Set.of(
        "GET /api/question-types",
        "GET /api/question-types/{id}",
        "GET /management/info",
        "POST /api/account/reset-password/finish",
        "POST /api/account/reset-password/init"
    );

    /** Restricted to ROLE_ADMIN by the path policy quarkus.http.auth.permission.admin (application.properties). */
    private static final Set<String> ADMIN_PATH_POLICY = Set.of(
        "GET /api/users",
        "GET /api/users/{login}",
        "GET /management/jhimetrics",
        "GET /management/threaddump"
    );

    @Test
    void everyEndpointDeclaresWhoMayCallIt() {
        Set<String> expected = new TreeSet<>();
        expected.addAll(PUBLIC);
        expected.addAll(ADMIN_PATH_POLICY);

        assertEquals(expected, undeclaredEndpoints());
    }

    private static Set<String> undeclaredEndpoints() {
        Set<String> undeclared = new TreeSet<>();
        var classes = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("fr.istic.web.rest");
        for (JavaClass javaClass : classes) {
            Class<?> resource = javaClass.reflect();
            Path root = resource.getAnnotation(Path.class);
            if (root == null) {
                continue;
            }
            boolean classDeclared = declaresAccess(resource.getAnnotations());
            for (Method method : resource.getDeclaredMethods()) {
                String verb = httpVerb(method);
                if (verb == null || classDeclared || declaresAccess(method.getAnnotations())) {
                    continue;
                }
                Path path = method.getAnnotation(Path.class);
                String route = root.value().replaceAll("/+$", "") + (path == null ? "" : "/" + path.value().replaceFirst("^/+", ""));
                undeclared.add(verb + " " + route);
            }
        }
        return undeclared;
    }

    private static boolean declaresAccess(Annotation[] annotations) {
        for (Annotation annotation : annotations) {
            Class<? extends Annotation> type = annotation.annotationType();
            if (type == RolesAllowed.class || type == Authenticated.class || type == PermitAll.class || type == DenyAll.class) {
                return true;
            }
        }
        return false;
    }

    private static String httpVerb(Method method) {
        for (Class<? extends Annotation> verb : List.of(GET.class, POST.class, PUT.class, DELETE.class, PATCH.class)) {
            if (method.isAnnotationPresent(verb)) {
                return verb.getSimpleName();
            }
        }
        return null;
    }
}
