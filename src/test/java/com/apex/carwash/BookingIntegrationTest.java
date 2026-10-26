
package com.apex.carwash;

import com.apex.carwash.entity.BookingStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "app.api-key=assessment-test-key"
)
@AutoConfigureMockMvc
@Testcontainers
class BookingIntegrationTest {

    private static final String KEY = "assessment-test-key";
    private static final String TIME = "2030-01-15T10:00:00Z";

    @Container
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void database(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private JdbcTemplate jdbc;

    @LocalServerPort
    private int port;

    @BeforeEach
    void clearBookings() {
        jdbc.update("DELETE FROM bookings");
    }

    @Test
    void listsBranches() throws Exception {
        mvc.perform(get("/api/branches"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void createsBooking() throws Exception {
        createBooking(1, 1, "demo", TIME)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.customerAlias").value("demo"));
    }

    @Test
    void rejectsMissingFields() throws Exception {
        mvc.perform(post("/api/bookings")
                        .header("X-API-Key", KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void preventsDoubleBooking() throws Exception {
        createBooking(1, 1, "first", TIME)
                .andExpect(status().isCreated());

        createBooking(1, 2, "second", TIME)
                .andExpect(status().isConflict());
    }

    @Test
    void updatesBookingStatus() throws Exception {
        long id = createBookingId(1, TIME);

        changeStatus(id, BookingStatus.CONFIRMED)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"));

        changeStatus(id, BookingStatus.COMPLETED)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    void rejectsInvalidStatusTransition() throws Exception {
        long id = createBookingId(1, TIME);

        changeStatus(id, BookingStatus.COMPLETED)
                .andExpect(status().isConflict());
    }

    @Test
    void rejectsMissingApiKey() throws Exception {
        mvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());

        mvc.perform(get("/api/bookings"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void filtersBookings() throws Exception {
        long id = createBookingId(1, TIME);

        changeStatus(id, BookingStatus.CONFIRMED)
                .andExpect(status().isOk());

        createBookingId(2, TIME);

        mvc.perform(get("/api/bookings")
                        .header("X-API-Key", KEY)
                        .param("branchId", "1")
                        .param("status", "CONFIRMED")
                        .param("date", "2030-01-15"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(id));
    }

    @Test
    void cancellationFreesSlot() throws Exception {
        long id = createBookingId(1, TIME);

        changeStatus(id, BookingStatus.CANCELLED)
                .andExpect(status().isOk());

        createBooking(1, 1, "another", TIME)
                .andExpect(status().isCreated());
    }

    @Test
    void concurrentBookingsPreventDoubleBooking() throws Exception {
        String body = bookingJson(1, 1, "demo", TIME);

        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(
                        "http://localhost:" + port + "/api/bookings"))
                .timeout(Duration.ofSeconds(10))
                .header("X-API-Key", KEY)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        CyclicBarrier barrier = new CyclicBarrier(2);

        Callable<Integer> task = () -> {
            barrier.await(10, TimeUnit.SECONDS);

            HttpResponse<String> response = client.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            return response.statusCode();
        };

        try (ExecutorService executor =
                     Executors.newFixedThreadPool(2)) {

            Future<Integer> first = executor.submit(task);
            Future<Integer> second = executor.submit(task);

            int firstStatus = first.get(20, TimeUnit.SECONDS);
            int secondStatus = second.get(20, TimeUnit.SECONDS);

            List<Integer> results =
                    List.of(firstStatus, secondStatus);

            assertEquals(1,
                    java.util.Collections.frequency(results, 201));

            assertEquals(1,
                    java.util.Collections.frequency(results, 409));
        }

        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM bookings",
                Integer.class
        );

        assertEquals(1, count);
    }

    
    @Test
    void rejectsNonexistentBranch() throws Exception {
        createBooking(99999, 1, "demo", TIME)
                .andExpect(status().isNotFound());
    }

    @Test
    void rejectsNonexistentService() throws Exception {
        createBooking(1, 99999, "demo", TIME)
                .andExpect(status().isNotFound());
    }

    @Test
    void retrievesCreatedBooking() throws Exception {
        long id = createBookingId(1, TIME);

    mvc.perform(get("/api/bookings/{id}", id)
                    .header("X-API-Key", KEY))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(id))
            .andExpect(jsonPath("$.status").value("PENDING"));
}

    private String bookingJson(
            long branchId,
            long serviceId,
            String alias,
            String time
    ) throws Exception {

        return mapper.writeValueAsString(Map.of(
                "branchId", branchId,
                "serviceId", serviceId,
                "customerAlias", alias,
                "startTime", time
        ));
    }

    private ResultActions createBooking(
            long branchId,
            long serviceId,
            String alias,
            String time
    ) throws Exception {

        return mvc.perform(post("/api/bookings")
                .header("X-API-Key", KEY)
                .contentType(MediaType.APPLICATION_JSON)
                .content(bookingJson(
                        branchId, serviceId, alias, time)));
    }

    private long createBookingId(
            long branchId,
            String time
    ) throws Exception {

        MvcResult result = createBooking(
                branchId, 1, "demo", time
        )
                .andExpect(status().isCreated())
                .andReturn();

        return mapper.readTree(
                result.getResponse().getContentAsString()
        ).get("id").asLong();
    }

    private ResultActions changeStatus(
            long id,
            BookingStatus status
    ) throws Exception {

        String body = mapper.writeValueAsString(
                Map.of("status", status)
        );

        return mvc.perform(
                patch("/api/bookings/{id}/status", id)
                        .header("X-API-Key", KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
        );
    }
}
