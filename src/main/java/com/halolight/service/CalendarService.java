package com.halolight.service;

import com.halolight.domain.entity.CalendarEvent;
import com.halolight.domain.entity.EventAttendee;
import com.halolight.domain.entity.EventReminder;
import com.halolight.domain.entity.Team;
import com.halolight.domain.entity.User;
import com.halolight.domain.entity.enums.AttendeeStatus;
import com.halolight.domain.entity.id.EventAttendeeId;
import com.halolight.domain.repository.CalendarEventRepository;
import com.halolight.domain.repository.EventAttendeeRepository;
import com.halolight.domain.repository.EventReminderRepository;
import com.halolight.domain.repository.TeamRepository;
import com.halolight.domain.repository.UserRepository;
import com.halolight.web.dto.calendar.AttendeeRequest;
import com.halolight.web.dto.calendar.CreateEventRequest;
import com.halolight.web.dto.calendar.EventResponse;
import com.halolight.web.dto.calendar.ReminderRequest;
import com.halolight.web.dto.calendar.UpdateEventRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CalendarService {

    private final CalendarEventRepository eventRepository;
    private final EventAttendeeRepository attendeeRepository;
    private final EventReminderRepository reminderRepository;
    private final UserRepository userRepository;
    private final TeamRepository teamRepository;

    @Transactional(readOnly = true)
    public List<EventResponse> getAllEvents(String userId, Instant start, Instant end) {
        List<CalendarEvent> events;

        if (start != null && end != null) {
            events = eventRepository.findByUserIdAndDateRange(userId, start, end);
        } else {
            events = eventRepository.findByOrganizerId(userId, Pageable.unpaged()).getContent();
        }

        return events.stream()
                .map(this::toEventResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<EventResponse> getEventsByOrganizer(String organizerId, Pageable pageable) {
        return eventRepository.findByOrganizerId(organizerId, pageable)
                .map(this::toEventResponse);
    }

    @Transactional(readOnly = true)
    public Page<EventResponse> getEventsByTeam(String teamId, Pageable pageable) {
        return eventRepository.findByTeamId(teamId, pageable)
                .map(this::toEventResponse);
    }

    @Transactional(readOnly = true)
    public List<EventResponse> getUpcomingEvents(int limit) {
        Instant now = Instant.now();
        Pageable pageable = Pageable.ofSize(limit);
        return eventRepository.findUpcomingEvents(now, pageable).stream()
                .map(this::toEventResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public EventResponse getEventById(String id) {
        CalendarEvent event = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found with id: " + id));
        return toEventResponse(event);
    }

    @Transactional
    public EventResponse createEvent(String organizerId, CreateEventRequest request) {
        // Validate organizer exists
        User organizer = userRepository.findById(organizerId)
                .orElseThrow(() -> new RuntimeException("Organizer not found with id: " + organizerId));

        // Build event
        CalendarEvent event = CalendarEvent.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .allDay(request.getAllDay())
                .location(request.getLocation())
                .color(request.getColor())
                .organizerId(organizerId)
                .teamId(request.getTeamId())
                .build();

        event = eventRepository.save(event);
        log.info("Created calendar event: {}", event.getId());

        // Add attendees
        if (request.getAttendees() != null && !request.getAttendees().isEmpty()) {
            for (AttendeeRequest attendeeReq : request.getAttendees()) {
                addAttendeeToEvent(event, attendeeReq);
            }
        }

        // Add reminders
        if (request.getReminders() != null && !request.getReminders().isEmpty()) {
            for (ReminderRequest reminderReq : request.getReminders()) {
                addReminderToEvent(event, reminderReq);
            }
        }

        return toEventResponse(eventRepository.findById(event.getId()).orElseThrow());
    }

    @Transactional
    public EventResponse updateEvent(String id, String userId, UpdateEventRequest request) {
        CalendarEvent event = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found with id: " + id));

        // Verify user is organizer
        if (!event.getOrganizerId().equals(userId)) {
            throw new RuntimeException("Only the organizer can update the event");
        }

        // Update fields if provided
        if (request.getTitle() != null) {
            event.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            event.setDescription(request.getDescription());
        }
        if (request.getStartTime() != null) {
            event.setStartTime(request.getStartTime());
        }
        if (request.getEndTime() != null) {
            event.setEndTime(request.getEndTime());
        }
        if (request.getAllDay() != null) {
            event.setAllDay(request.getAllDay());
        }
        if (request.getLocation() != null) {
            event.setLocation(request.getLocation());
        }
        if (request.getColor() != null) {
            event.setColor(request.getColor());
        }
        if (request.getTeamId() != null) {
            event.setTeamId(request.getTeamId());
        }

        // Update attendees if provided
        if (request.getAttendees() != null) {
            attendeeRepository.deleteByIdEventId(id);
            for (AttendeeRequest attendeeReq : request.getAttendees()) {
                addAttendeeToEvent(event, attendeeReq);
            }
        }

        // Update reminders if provided
        if (request.getReminders() != null) {
            reminderRepository.deleteByEventId(id);
            for (ReminderRequest reminderReq : request.getReminders()) {
                addReminderToEvent(event, reminderReq);
            }
        }

        event = eventRepository.save(event);
        log.info("Updated calendar event: {}", event.getId());

        return toEventResponse(eventRepository.findById(event.getId()).orElseThrow());
    }

    @Transactional
    public void deleteEvent(String id, String userId) {
        CalendarEvent event = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found with id: " + id));

        // Verify user is organizer
        if (!event.getOrganizerId().equals(userId)) {
            throw new RuntimeException("Only the organizer can delete the event");
        }

        eventRepository.delete(event);
        log.info("Deleted calendar event: {}", id);
    }

    @Transactional
    public void batchDeleteEvents(List<String> ids, String userId) {
        for (String id : ids) {
            try {
                deleteEvent(id, userId);
            } catch (Exception e) {
                log.warn("Failed to delete event {}: {}", id, e.getMessage());
            }
        }
    }

    @Transactional
    public EventResponse rescheduleEvent(String id, String userId, Instant newStart, Instant newEnd) {
        CalendarEvent event = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found with id: " + id));

        // Verify user is organizer
        if (!event.getOrganizerId().equals(userId)) {
            throw new RuntimeException("Only the organizer can reschedule the event");
        }

        event.setStartTime(newStart);
        event.setEndTime(newEnd);

        event = eventRepository.save(event);
        log.info("Rescheduled calendar event: {}", event.getId());

        return toEventResponse(event);
    }

    @Transactional
    public EventResponse addAttendees(String eventId, String userId, List<String> attendeeIds) {
        CalendarEvent event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found with id: " + eventId));

        // Verify user is organizer
        if (!event.getOrganizerId().equals(userId)) {
            throw new RuntimeException("Only the organizer can add attendees");
        }

        for (String attendeeId : attendeeIds) {
            if (!attendeeRepository.existsByIdEventIdAndIdUserId(eventId, attendeeId)) {
                AttendeeRequest request = AttendeeRequest.builder()
                        .userId(attendeeId)
                        .build();
                addAttendeeToEvent(event, request);
            }
        }

        log.info("Added {} attendees to event: {}", attendeeIds.size(), eventId);
        return toEventResponse(eventRepository.findById(eventId).orElseThrow());
    }

    @Transactional
    public EventResponse removeAttendee(String eventId, String userId, String attendeeId) {
        CalendarEvent event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found with id: " + eventId));

        // Verify user is organizer
        if (!event.getOrganizerId().equals(userId)) {
            throw new RuntimeException("Only the organizer can remove attendees");
        }

        EventAttendeeId id = new EventAttendeeId(eventId, attendeeId);
        attendeeRepository.deleteById(id);

        log.info("Removed attendee {} from event: {}", attendeeId, eventId);
        return toEventResponse(eventRepository.findById(eventId).orElseThrow());
    }

    @Transactional
    public EventResponse updateAttendeeStatus(String eventId, String attendeeUserId, AttendeeStatus newStatus) {
        CalendarEvent event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found with id: " + eventId));

        EventAttendeeId id = new EventAttendeeId(eventId, attendeeUserId);
        EventAttendee attendee = attendeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Attendee not found for event: " + eventId));

        // Update status
        attendee.setStatus(newStatus);
        attendee.setRespondedAt(Instant.now());
        attendeeRepository.save(attendee);

        log.info("Updated attendee {} status to {} for event: {}", attendeeUserId, newStatus, eventId);
        return toEventResponse(eventRepository.findById(eventId).orElseThrow());
    }

    private void addAttendeeToEvent(CalendarEvent event, AttendeeRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found with id: " + request.getUserId()));

        EventAttendee attendee = EventAttendee.builder()
                .event(event)
                .user(user)
                .status(request.getStatus())
                .build();

        attendeeRepository.save(attendee);
    }

    private void addReminderToEvent(CalendarEvent event, ReminderRequest request) {
        EventReminder reminder = EventReminder.builder()
                .eventId(event.getId())
                .remindAt(request.getRemindAt())
                .type(request.getType())
                .sent(false)
                .build();

        reminderRepository.save(reminder);
    }

    private EventResponse toEventResponse(CalendarEvent event) {
        EventResponse.EventResponseBuilder builder = EventResponse.builder()
                .id(event.getId())
                .title(event.getTitle())
                .description(event.getDescription())
                .startTime(event.getStartTime())
                .endTime(event.getEndTime())
                .allDay(event.getAllDay())
                .location(event.getLocation())
                .color(event.getColor())
                .organizerId(event.getOrganizerId())
                .teamId(event.getTeamId())
                .createdAt(event.getCreatedAt())
                .updatedAt(event.getUpdatedAt());

        // Map organizer
        if (event.getOrganizer() != null) {
            User organizer = event.getOrganizer();
            builder.organizer(EventResponse.OrganizerInfo.builder()
                    .id(organizer.getId())
                    .username(organizer.getUsername())
                    .name(organizer.getName())
                    .email(organizer.getEmail())
                    .avatar(organizer.getAvatar())
                    .build());
        }

        // Map team
        if (event.getTeam() != null) {
            Team team = event.getTeam();
            builder.team(EventResponse.TeamInfo.builder()
                    .id(team.getId())
                    .name(team.getName())
                    .description(team.getDescription())
                    .build());
        }

        // Map attendees
        List<EventResponse.AttendeeInfo> attendees = attendeeRepository.findByIdEventId(event.getId())
                .stream()
                .map(attendee -> {
                    User user = attendee.getUser();
                    return EventResponse.AttendeeInfo.builder()
                            .userId(user.getId())
                            .username(user.getUsername())
                            .name(user.getName())
                            .email(user.getEmail())
                            .avatar(user.getAvatar())
                            .status(attendee.getStatus())
                            .respondedAt(attendee.getRespondedAt())
                            .build();
                })
                .collect(Collectors.toList());
        builder.attendees(attendees);

        // Map reminders
        List<EventResponse.ReminderInfo> reminders = reminderRepository.findByEventId(event.getId())
                .stream()
                .map(reminder -> EventResponse.ReminderInfo.builder()
                        .id(reminder.getId())
                        .remindAt(reminder.getRemindAt())
                        .type(reminder.getType())
                        .sent(reminder.getSent())
                        .createdAt(reminder.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
        builder.reminders(reminders);

        return builder.build();
    }
}
