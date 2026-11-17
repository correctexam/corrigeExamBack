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
    }

    @Test
    void noPrintRunMeansAnEmptyListNeverNull() throws Exception {
        // the worker reads a list: null would make the job fail
        JsonNode json = mapper.readTree(mapper.writeValueAsString(new ScanRequestDto()));
        assertTrue(json.get("page_codes").isArray());
        assertEquals(0, json.get("page_codes").size());
    }
}
