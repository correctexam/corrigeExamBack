package fr.istic.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import fr.istic.service.customdto.ScanStatusDto;

/**
 * Unit tests of {@link ScanStreamService}: the first event is "connected" and statuses
 * are filtered by exam.
 */
class ScanStreamServiceTest {

    private static byte[] status(int examId, int page, String status, int progress) {
        return ("{\"exam_id\":" + examId + ",\"page\":" + page + ",\"status\":\"" + status + "\",\"progress\":"
                + progress + ",\"details\":\"\"}").getBytes(StandardCharsets.UTF_8);
    }

    @Test
    void firstEventIsConnectedThenOnlyTheStatusesOfTheExam() {
        ScanStreamService service = new ScanStreamService();
        List<ScanStatusDto> received = new ArrayList<>();
        service.streamStatus(12).subscribe().with(received::add);

        service.consumeRabbitMQ(status(12, 1, ScanStatusDto.STATUS_RUNNING, 50));
        service.consumeRabbitMQ(status(99, 1, ScanStatusDto.STATUS_RUNNING, 50));
        service.consumeRabbitMQ(status(12, 0, ScanStatusDto.STATUS_ERROR, 0));

        assertEquals(List.of(ScanStatusDto.STATUS_CONNECTED, ScanStatusDto.STATUS_RUNNING, ScanStatusDto.STATUS_ERROR),
                received.stream().map(s -> s.status).toList());
        received.forEach(s -> assertEquals(12, s.examId));
    }

    @Test
    void invalidOrUnknownMessagesAreIgnored() {
        ScanStreamService service = new ScanStreamService();
        List<ScanStatusDto> received = new ArrayList<>();
        service.streamStatus(1).subscribe().with(received::add);

        service.consumeRabbitMQ("not json".getBytes(StandardCharsets.UTF_8));
        service.consumeRabbitMQ(42);

        assertEquals(1, received.size(), "only the connected event");
    }
}
