package com.example;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;


public class BookingSystemTest {

    @Test
    void bookRoomFailsWhenRoomIdIsNull() {
        TimeProvider timeProvider = mock(TimeProvider.class);
        RoomRepository roomRepository = mock(RoomRepository.class);
        NotificationService notificationService = mock(NotificationService.class);

        BookingSystem system = new BookingSystem(timeProvider, roomRepository, notificationService);

        LocalDateTime start = LocalDateTime.now().plusHours(1);
        LocalDateTime end = LocalDateTime.now().plusHours(2);

        assertThatThrownBy(() -> system.bookRoom(null, start, end))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Bokning kräver giltiga start- och sluttider samt rum-id");
    }

    @Test
    void bookRoomFailsWhenStartTimeIsNull() {
        TimeProvider timeProvider = mock(TimeProvider.class);
        RoomRepository roomRepository = mock(RoomRepository.class);
        NotificationService notificationService = mock(NotificationService.class);

        BookingSystem system = new BookingSystem(timeProvider, roomRepository, notificationService);

        LocalDateTime end = LocalDateTime.now().plusHours(2);

        assertThatThrownBy(() -> system.bookRoom("room1", null, end))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Bokning kräver giltiga start- och sluttider samt rum-id");
    }

    @Test
    void bookRoomFailsWhenEndTimeIsNull() {
        TimeProvider timeProvider = mock(TimeProvider.class);
        RoomRepository roomRepository = mock(RoomRepository.class);
        NotificationService notificationService = mock(NotificationService.class);

        BookingSystem system = new BookingSystem(timeProvider, roomRepository, notificationService);

        LocalDateTime start = LocalDateTime.now().plusHours(1);

        assertThatThrownBy(() -> system.bookRoom("room1", start, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Bokning kräver giltiga start- och sluttider samt rum-id");
    }
}
