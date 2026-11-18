package fr.istic.service.customdto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/** Contract of the alignment job message with the Rust worker (ADR 0001, T25d3 page codes). */
class ScanRequestDtoTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void pageCodesAreSentWithTheFieldNamesOfTheWorker() throws Exception {
        ScanRequestDto request = new ScanRequestDto();
        request.pagesToManage = "1-6";
        request.examID = 1;
        request.pageCodes = List.of(new ScanRequestDto.PageCodeEdition(41, 30, 2));

        JsonNode json = mapper.readTree(mapper.writeValueAsString(request));

        assertEquals(41, json.get("page_codes").get(0).get("id").asLong());
        assertEquals(30, json.get("page_codes").get(0).get("copies").asInt());
        assertEquals(2, json.get("page_codes").get(0).get("pages_per_copy").asInt());
        assertEquals(1, json.get("page_codes").get(0).get("versions").asInt());
    }

    @Test
    void versionsAreSentWithTheFieldNamesOfTheWorker() throws Exception {
        ScanRequestDto request = new ScanRequestDto();
        request.pageCodes = List.of(new ScanRequestDto.PageCodeEdition(41, 30, 2, 3));
        JsonNode layout = mapper.readTree("{\"format\":1,\"version\":1,\"label\":\"B\",\"pages\":2,\"parts\":[],\"boxes\":[],\"identity\":{}}");
        request.versions = List.of(new ScanRequestDto.VersionLayout(1, "B", layout));

        JsonNode json = mapper.readTree(mapper.writeValueAsString(request));

        assertEquals(3, json.get("page_codes").get(0).get("versions").asInt());
        assertEquals(1, json.get("versions").get(0).get("index").asInt());
        assertEquals("B", json.get("versions").get(0).get("label").asText());
        // the layout is sent as JSON, not as a string
        assertEquals(1, json.get("versions").get(0).get("layout").get("version").asInt());
    }

    @Test
    void noPrintRunMeansAnEmptyListNeverNull() throws Exception {
        // the worker reads a list: null would make the job fail
        JsonNode json = mapper.readTree(mapper.writeValueAsString(new ScanRequestDto()));
        assertTrue(json.get("page_codes").isArray());
        assertEquals(0, json.get("page_codes").size());
        assertTrue(json.get("versions").isArray());
        assertEquals(0, json.get("versions").size());
    }
}
