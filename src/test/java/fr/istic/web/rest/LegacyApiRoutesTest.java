package fr.istic.web.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;

import org.junit.jupiter.api.Test;

/**
 * Guards the REST routes of the former {@code ExtendedAPI} class, split by domain in T3.
 * <p>
 * The Angular front calls these URLs directly: moving an endpoint to another class must not change its
 * HTTP method or its path. Plain reflection, no Quarkus boot.
 */
class LegacyApiRoutesTest {

    /** Resources that share the {@code /api} root and replace {@code ExtendedAPI}. */
    private static final List<Class<?>> SPLIT_RESOURCES = List.of(
        AnswerReviewResource.class,
        CourseImportExportResource.class,
        CourseSharingResource.class,
        CourseStudentResource.class,
        ExamCleanupResource.class,
        ExamFileResource.class,
        ExamResultResource.class,
        ExportPdfResource.class,
        MarkingStatusResource.class,
        NotebookResource.class,
        SameCommentZoneResource.class,
        StudentResponseUpdateResource.class
    );

    /** Other resources rooted at {@code /api}: their routes must not collide with the split ones. */
    private static final List<Class<?>> OTHER_API_ROOT_RESOURCES = List.of(
        AccountResource.class,
        AuthorityResource.class,
        UserJWTController.class
    );

    /** Routes exposed by {@code ExtendedAPI} before the split (develop, November 2026). */
    private static final Set<String> LEGACY_ROUTES = Set.of(
        "DELETE /api/cleanExam/{examId}",
        "DELETE /api/cleanExamSheet/{examId}",
        "DELETE /api/deleteAllAnswerAndComment/{examId}",
        "DELETE /api/deleteAnswerAndUnsetComment/{studentResponseId}",
        "DELETE /api/deletegroupstudents/{courseid}",
        "GET /api/exportCourse/{courseId}",
        "GET /api/exportCourseWithoutStudentData/{courseId}",
        "GET /api/exportExam/{courseId}/{examId}",
        "GET /api/exportExamWithoutStudentData/{courseId}/{examId}",
        "GET /api/exportpdf/{examId}",
        "GET /api/exportpdf4sheet/{examId}/{sheetuuid}",
        "GET /api/getAllEmailProfs4course/{courseId}",
        "GET /api/getAllStarAnswer/{examId}",
        "GET /api/getBestAnswer/{examId}/{nume}",
        "GET /api/getCache/{fileName}",
        "GET /api/getCacheAlignPage/{examId}/{pageId}",
        "GET /api/getCacheNonAlignPage/{examId}/{pageId}",
        "GET /api/getCachePageInTemplate/{examId}",
        "GET /api/getCacheTimeStamp/{examId}/{cacheType}",
        "GET /api/getComments/{examId}",
        "GET /api/getExamStatus/{examId}",
        "GET /api/getExamStatusFinish/{examId}",
        "GET /api/getLibelleQuestions/{examId}",
        "GET /api/getScanPdf/{scanId}",
        "GET /api/getTemplatePdf/{templateId}",
        "GET /api/getUsers/{courseId}",
        "GET /api/getZone4GradedComment/{examId}/{gradedCommentId}",
        "GET /api/getZone4HybridComment/{examId}/{hybridCommentId}/{stepValue}",
        "GET /api/getZone4Mark/{examId}/{respid}",
        "GET /api/getZone4Numero/{examId}/{qid}",
        "GET /api/getZone4TextComment/{examId}/{textCommentId}",
        "GET /api/getallcommentsandprediction4qId/{qId}",
        "GET /api/getstudentcours/{courseid}",
        "GET /api/showResult/{examId}",
        "POST /api/cleanResponse",
        "POST /api/computeNode/{examId}",
        "POST /api/createNoteBookExamStructure",
        "POST /api/createstudentmasse",
        "POST /api/importCourse",
        "POST /api/importCourseWithoutStudentData",
        "POST /api/sendResult/{examId}",
        "POST /api/updateStudentResponse4Cluster/{examId}/{qid}",
        "POST /api/uploadAndMergeScan/{scanId}",
        "POST /api/uploadCache",
        "POST /api/uploadExportFinalStudent/{examId}",
        "POST /api/uploadScan/{scanId}",
        "PUT /api/deletestudentgroup/{courseid}",
        "PUT /api/toggleAsAbJ/{studentId}/{examId}/{abi}",
        "PUT /api/update-2-hybrid-graded-comments-with-stepvalue/{responseId}/{hybridCommentId}/{stepValue}",
        "PUT /api/update-2-hybrid-graded-comments/{responseId}/{hybridCommentId}",
        "PUT /api/updateProfs/{courseId}",
        "PUT /api/updatestudent/{courseid}",
        "PUT /api/updatestudentgroup/{courseid}",
        "PUT /api/updatestudentine/{courseid}"
    );

    @Test
    void splitResourcesExposeExactlyTheLegacyRoutes() {
        List<String> routes = routesOf(SPLIT_RESOURCES);

        assertEquals(new TreeSet<>(LEGACY_ROUTES), new TreeSet<>(routes));
        assertEquals(LEGACY_ROUTES.size(), routes.size(), "a route is declared twice: " + routes);
    }

    @Test
    void noRouteIsDeclaredTwiceUnderTheApiRoot() {
        List<Class<?>> all = new ArrayList<>(SPLIT_RESOURCES);
        all.addAll(OTHER_API_ROOT_RESOURCES);
        List<String> routes = routesOf(all);

        Set<String> seen = new TreeSet<>();
        for (String route : routes) {
            assertTrue(seen.add(route), "duplicate route: " + route);
        }
    }

    private static List<String> routesOf(List<Class<?>> resources) {
        List<String> routes = new ArrayList<>();
        for (Class<?> resource : resources) {
            Path root = resource.getAnnotation(Path.class);
            assertEquals("/api", root == null ? null : root.value(), resource.getSimpleName() + " must be rooted at /api");
            for (Method method : resource.getDeclaredMethods()) {
                String verb = httpVerb(method);
                Path path = method.getAnnotation(Path.class);
                if (verb != null && path != null) {
                    routes.add(verb + " /api/" + stripLeadingSlash(path.value()));
                }
            }
        }
        return routes;
    }

    private static String httpVerb(Method method) {
        for (Class<? extends Annotation> verb : List.of(GET.class, POST.class, PUT.class, DELETE.class)) {
            if (method.isAnnotationPresent(verb)) {
                return verb.getSimpleName();
            }
        }
        return null;
    }

    private static String stripLeadingSlash(String path) {
        return path.startsWith("/") ? path.substring(1) : path;
    }
}
