package com.halolight.controller;

import com.halolight.dto.ApiResponse;
import com.halolight.security.UserPrincipal;
import com.halolight.service.CalendarService;
import com.halolight.web.dto.calendar.CreateEventRequest;
import com.halolight.web.dto.calendar.EventResponse;
import com.halolight.web.dto.calendar.UpdateAttendeeStatusRequest;
import com.halolight.web.dto.calendar.UpdateEventRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Tag(name = "Calendar", description = "Calendar events management API endpoints")
@RestController
@RequestMapping("/api/calendar/events")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class CalendarController {

    private final CalendarService calendarService;

    @Operation(summary = "Get all events", description = "Retrieve all calendar events for the current user with optional date range filter")
    @GetMapping
    public ResponseEntity<ApiResponse<List<EventResponse>>> getAllEvents(
            @AuthenticationPrincipal UserPrincipal user,
            @Parameter(description = "Start date (ISO-8601 format)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant start,
            @Parameter(description = "End date (ISO-8601 format)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant end
    ) {
        List<EventResponse> events = calendarService.getAllEvents(user.getId(), start, end);
        return ResponseEntity.ok(ApiResponse.success(events));
    }

    @Operation(summary = "Get events by organizer", description = "Retrieve paginated calendar events organized by a specific user")
    @GetMapping("/organizer/{organizerId}")
    public ResponseEntity<ApiResponse<Page<EventResponse>>> getEventsByOrganizer(
            @PathVariable String organizerId,
            @PageableDefault(size = 20, sort = "startTime", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<EventResponse> events = calendarService.getEventsByOrganizer(organizerId, pageable);
        return ResponseEntity.ok(ApiResponse.success(events));
    }

    @Operation(summary = "Get events by team", description = "Retrieve paginated calendar events for a specific team")
    @GetMapping("/team/{teamId}")
    public ResponseEntity<ApiResponse<Page<EventResponse>>> getEventsByTeam(
            @PathVariable String teamId,
            @PageableDefault(size = 20, sort = "startTime", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<EventResponse> events = calendarService.getEventsByTeam(teamId, pageable);
        return ResponseEntity.ok(ApiResponse.success(events));
    }

    @Operation(summary = "Get upcoming events", description = "Retrieve upcoming calendar events")
    @GetMapping("/upcoming")
    public ResponseEntity<ApiResponse<List<EventResponse>>> getUpcomingEvents(
            @Parameter(description = "Maximum number of events to return")
            @RequestParam(defaultValue = "10") int limit
    ) {
        List<EventResponse> events = calendarService.getUpcomingEvents(limit);
        return ResponseEntity.ok(ApiResponse.success(events));
    }

    @Operation(summary = "Get event by ID", description = "Retrieve a specific calendar event by its ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EventResponse>> getEventById(@PathVariable String id) {
        EventResponse event = calendarService.getEventById(id);
        return ResponseEntity.ok(ApiResponse.success(event));
    }

    @Operation(summary = "Create event", description = "Create a new calendar event")
    @PostMapping
    public ResponseEntity<ApiResponse<EventResponse>> createEvent(
            @AuthenticationPrincipal UserPrincipal user,
            @Valid @RequestBody CreateEventRequest request
    ) {
        EventResponse event = calendarService.createEvent(user.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Event created successfully", event));
    }

    @Operation(summary = "Update event", description = "Update an existing calendar event")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<EventResponse>> updateEvent(
            @PathVariable String id,
            @AuthenticationPrincipal UserPrincipal user,
            @Valid @RequestBody UpdateEventRequest request
    ) {
        EventResponse event = calendarService.updateEvent(id, user.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Event updated successfully", event));
    }

    @Operation(summary = "Delete event", description = "Delete a calendar event")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteEvent(
            @PathVariable String id,
            @AuthenticationPrincipal UserPrincipal user
    ) {
        calendarService.deleteEvent(id, user.getId());
        return ResponseEntity.ok(ApiResponse.success("Event deleted successfully", null));
    }

    @Operation(summary = "Batch delete events", description = "Delete multiple calendar events")
    @PostMapping("/batch-delete")
    public ResponseEntity<ApiResponse<Void>> batchDeleteEvents(
            @AuthenticationPrincipal UserPrincipal user,
            @RequestBody Map<String, List<String>> body
    ) {
        List<String> ids = body.get("ids");
        if (ids == null || ids.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Event IDs are required"));
        }
        calendarService.batchDeleteEvents(ids, user.getId());
        return ResponseEntity.ok(ApiResponse.success("Events deleted successfully", null));
    }

    @Operation(summary = "Reschedule event", description = "Reschedule a calendar event to a new time")
    @PatchMapping("/{id}/reschedule")
    public ResponseEntity<ApiResponse<EventResponse>> rescheduleEvent(
            @PathVariable String id,
            @AuthenticationPrincipal UserPrincipal user,
            @RequestBody Map<String, Instant> body
    ) {
        Instant newStart = body.get("start");
        Instant newEnd = body.get("end");

        if (newStart == null || newEnd == null) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Both start and end times are required"));
        }

        EventResponse event = calendarService.rescheduleEvent(id, user.getId(), newStart, newEnd);
        return ResponseEntity.ok(ApiResponse.success("Event rescheduled successfully", event));
    }

    @Operation(summary = "Add attendees", description = "Add attendees to a calendar event")
    @PostMapping("/{id}/attendees")
    public ResponseEntity<ApiResponse<EventResponse>> addAttendees(
            @PathVariable String id,
            @AuthenticationPrincipal UserPrincipal user,
            @RequestBody Map<String, List<String>> body
    ) {
        List<String> attendeeIds = body.get("attendeeIds");

        if (attendeeIds == null || attendeeIds.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Attendee IDs are required"));
        }

        EventResponse event = calendarService.addAttendees(id, user.getId(), attendeeIds);
        return ResponseEntity.ok(ApiResponse.success("Attendees added successfully", event));
    }

    @Operation(summary = "Remove attendee", description = "Remove an attendee from a calendar event")
    @DeleteMapping("/{id}/attendees/{attendeeId}")
    public ResponseEntity<ApiResponse<EventResponse>> removeAttendee(
            @PathVariable String id,
            @PathVariable String attendeeId,
            @AuthenticationPrincipal UserPrincipal user
    ) {
        EventResponse event = calendarService.removeAttendee(id, user.getId(), attendeeId);
        return ResponseEntity.ok(ApiResponse.success("Attendee removed successfully", event));
    }

    @Operation(summary = "Update attendee status", description = "Update an attendee's response status (accept/decline/pending)")
    @PutMapping("/{id}/attendees/{userId}/status")
    public ResponseEntity<ApiResponse<EventResponse>> updateAttendeeStatus(
            @PathVariable String id,
            @PathVariable String userId,
            @Valid @RequestBody UpdateAttendeeStatusRequest request
    ) {
        EventResponse event = calendarService.updateAttendeeStatus(id, userId, request.getStatus());
        return ResponseEntity.ok(ApiResponse.success("Attendee status updated successfully", event));
    }
}
