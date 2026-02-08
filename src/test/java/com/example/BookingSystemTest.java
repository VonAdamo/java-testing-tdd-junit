package com.example;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class BookingSystemTest {

    // -------> bookRoom tests <-------
    @ParameterizedTest(name = "{0} {1} {2}")
    @MethodSource("invalidBookRoomInputs")
    void bookRoomFailsWhenAnyRequiredValueIsNull(String roomId, LocalDateTime startTime, LocalDateTime endTime) {
        TimeProvider timeProvider = mock(TimeProvider.class);
        RoomRepository roomRepository = mock(RoomRepository.class);
        NotificationService notificationService = mock(NotificationService.class);

        BookingSystem system = new BookingSystem(timeProvider, roomRepository, notificationService);

        assertThatThrownBy(() -> system.bookRoom(roomId, startTime, endTime))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Bokning kräver giltiga start- och sluttider samt rum-id");
    }

    static Stream<Arguments> invalidBookRoomInputs() {
        LocalDateTime startTime = LocalDateTime.now().plusHours(1);
        LocalDateTime endTime = LocalDateTime.now().plusHours(2);

        return Stream.of(
                Arguments.of(null, startTime, endTime),
                Arguments.of("room1", null, endTime),
                Arguments.of("room1", startTime, null)
        );
    }

    @Test
    void bookRoomFailsWhenStartTimeIsInThePast() {
        TimeProvider timeProvider = mock(TimeProvider.class);
        RoomRepository roomRepository = mock(RoomRepository.class);
        NotificationService notificationService = mock(NotificationService.class);

        BookingSystem system = new BookingSystem(timeProvider, roomRepository, notificationService);

        LocalDateTime now = LocalDateTime.of(2026, 2, 8, 12, 0);
        when(timeProvider.getCurrentTime()).thenReturn(now);

        LocalDateTime startTime = now.minusMinutes(1);
        LocalDateTime endTime = now.plusHours(1);

        assertThatThrownBy(() -> system.bookRoom("room1", startTime, endTime))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Kan inte boka tid i dåtid");
    }

    @Test
    void bookRoomFailsWhenEndTimeIsInTheFuture() {
        TimeProvider timeProvider = mock(TimeProvider.class);
        RoomRepository roomRepository = mock(RoomRepository.class);
        NotificationService notificationService = mock(NotificationService.class);

        BookingSystem system = new BookingSystem(timeProvider, roomRepository, notificationService);

        LocalDateTime now = LocalDateTime.of(2026, 2, 8, 12, 0);
        when(timeProvider.getCurrentTime()).thenReturn(now);

        LocalDateTime startTime = now.plusHours(2);
        LocalDateTime endTime = now.plusHours(1);

        assertThatThrownBy(() -> system.bookRoom("room1", startTime, endTime))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Sluttid måste vara efter starttid");
    }

    @Test
    void bookRoomFailsWhenRoomDoesNotExist() {
        TimeProvider timeProvider = mock(TimeProvider.class);
        RoomRepository roomRepository = mock(RoomRepository.class);
        NotificationService notificationService = mock(NotificationService.class);

        BookingSystem system = new BookingSystem(timeProvider, roomRepository, notificationService);

        LocalDateTime now = LocalDateTime.of(2026, 2, 8, 12, 0);
        when(timeProvider.getCurrentTime()).thenReturn(now);

        LocalDateTime startTime = now.plusHours(1);
        LocalDateTime endTime = now.plusHours(2);

        when(roomRepository.findById("room1")).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> system.bookRoom("room1", startTime, endTime)
        );
        assertThat(exception.getMessage())
                .contains("Rummet existerar inte");
    }

    @Test
    void bookRoomReturnsFalseWhenRoomIsNotAvailable() throws NotificationException {
        TimeProvider timeProvider = mock(TimeProvider.class);
        RoomRepository roomRepository = mock(RoomRepository.class);
        NotificationService notificationService = mock(NotificationService.class);
        Room room = mock(Room.class);

        BookingSystem system = new BookingSystem(timeProvider, roomRepository, notificationService);

        LocalDateTime now = LocalDateTime.of(2026, 2, 8, 12, 0);
        when(timeProvider.getCurrentTime()).thenReturn(now);

        LocalDateTime startTime = now.plusHours(1);
        LocalDateTime endTime = now.plusHours(2);

        when(roomRepository.findById("room1")).thenReturn(Optional.of(room));
        when(room.isAvailable(startTime, endTime)).thenReturn(false);

        boolean result = system.bookRoom("room1", startTime, endTime);

        assertThat(result).isFalse();

        verify(roomRepository, never()).save(any());
        verify(notificationService, never()).sendBookingConfirmation(any());
    }

    @Test
    void bookRoomCreatesBookingWithCorrectDataWhenRoomIsAvailable() throws NotificationException {
        TimeProvider timeProvider = mock(TimeProvider.class);
        RoomRepository roomRepository = mock(RoomRepository.class);
        NotificationService notificationService = mock(NotificationService.class);
        Room room = mock(Room.class);

        BookingSystem system = new BookingSystem(timeProvider, roomRepository, notificationService);

        LocalDateTime now = LocalDateTime.of(2026, 2, 8, 12, 0);
        when(timeProvider.getCurrentTime()).thenReturn(now);

        LocalDateTime startTime = now.plusHours(1);
        LocalDateTime endTime = now.plusHours(2);

        when(roomRepository.findById("room1")).thenReturn(Optional.of(room));
        when(room.isAvailable(startTime, endTime)).thenReturn(true);

        boolean result = system.bookRoom("room1", startTime, endTime);

        assertThat(result).isTrue();

        ArgumentCaptor<Booking> bookingCaptor = ArgumentCaptor.forClass(Booking.class);
        verify(room).addBooking(bookingCaptor.capture());

        Booking booking = bookingCaptor.getValue();
        assertThat(booking.getRoomId()).isEqualTo("room1");
        assertThat(booking.getStartTime()).isEqualTo(startTime);
        assertThat(booking.getEndTime()).isEqualTo(endTime);
        assertThat(booking.getId()).isNotBlank();

        verify(roomRepository).save(room);
        verify(notificationService).sendBookingConfirmation(any());
    }

    // -------> getAvailableRooms tests <-------
    @ParameterizedTest(name = "{0} {1}")
    @MethodSource("getAvailableRoomsInvalidInputs")
    void getAvailableRoomsFailsWhenStartOrEndIsNull(LocalDateTime startTime, LocalDateTime endTime) {

        RoomRepository roomRepository = mock(RoomRepository.class);
        BookingSystem system = new BookingSystem(
                mock(TimeProvider.class),
                roomRepository,
                mock(NotificationService.class)
        );

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> system.getAvailableRooms(startTime, endTime)
        );
        assertThat(exception.getMessage())
                .contains("Måste ange både start- och sluttid");
        verifyNoInteractions(roomRepository);
    }

    static Stream<Arguments> getAvailableRoomsInvalidInputs() {
        LocalDateTime validTime = LocalDateTime.of(2026, 2, 8, 12, 0);
        return Stream.of(
                Arguments.of(null, validTime),
                Arguments.of(validTime, null)
        );
    }

    @Test
    void getAvailableRoomsFailsWhenEndTimeIsBeforeStartTime() {
        BookingSystem system = new BookingSystem(
                mock(TimeProvider.class),
                mock(RoomRepository.class),
                mock(NotificationService.class)
        );

        LocalDateTime startTime = LocalDateTime.now().plusHours(2);
        LocalDateTime endTime = LocalDateTime.now().plusHours(1);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> system.getAvailableRooms(startTime, endTime)
        );
        assertThat(exception.getMessage())
                .contains("Sluttid måste vara efter starttid");
    }

    @Test
    void getAvailableRoomsOnlyWhenRoomsAreAvailable() {

        RoomRepository roomRepository = mock(RoomRepository.class);
        BookingSystem system = new BookingSystem(
                mock(TimeProvider.class),
                roomRepository,
                mock(NotificationService.class)
        );
        LocalDateTime startTime = LocalDateTime.of(2026, 2, 8, 12, 0);
        LocalDateTime endTime = LocalDateTime.of(2026, 2, 8, 13, 0);

        Room room1 = mock(Room.class);
        Room room2 = mock(Room.class);
        Room room3 = mock(Room.class);

        when(roomRepository.findAll()).thenReturn(List.of(room1, room2, room3));

        when(room1.isAvailable(startTime, endTime)).thenReturn(true);
        when(room2.isAvailable(startTime, endTime)).thenReturn(false);
        when(room3.isAvailable(startTime, endTime)).thenReturn(true);

        List<Room> result = system.getAvailableRooms(startTime, endTime);

        assertThat(result)
                .containsExactly(room1, room3);
    }

    // -------> cancelBooking tests <-------
    @Test
    void cancelBookingFailsWhenBookingIdIsNull() {
        BookingSystem system = new BookingSystem(
                mock(TimeProvider.class),
                mock(RoomRepository.class),
                mock(NotificationService.class)
        );

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> system.cancelBooking(null)
        );
        assertThat(exception.getMessage())
                .contains("Boknings-id kan inte vara null");
    }

    @Test
    void cancelBookingReturnsFalseWhenBookingDoesNotExist() {
        RoomRepository roomRepository = mock(RoomRepository.class);
        NotificationService notificationService = mock(NotificationService.class);

        Room room1 = mock(Room.class);
        Room room2 = mock(Room.class);

        when(roomRepository.findAll()).thenReturn(List.of(room1, room2));
        when(room1.hasBooking("booking1")).thenReturn(false);
        when(room2.hasBooking("booking2")).thenReturn(false);

        BookingSystem system = new BookingSystem(
                mock(TimeProvider.class),
                roomRepository,
                notificationService
        );

        boolean result = system.cancelBooking("booking1");

        assertThat(result).isFalse();

        verify(roomRepository, never()).save(any());
        verifyNoInteractions(notificationService);
    }

    @Test
    void cancelBookingsFailsWhenBookingHasAlreadyStarted() {

        RoomRepository roomRepository = mock(RoomRepository.class);
        NotificationService notificationService = mock(NotificationService.class);
        TimeProvider timeProvider = mock(TimeProvider.class);

        Room room = mock(Room.class);
        Booking booking = mock(Booking.class);

        when(roomRepository.findAll()).thenReturn(List.of(room));
        when(room.hasBooking("booking1")).thenReturn(true);
        when(room.getBooking("booking1")).thenReturn(booking);

        LocalDateTime now = LocalDateTime.of(2026, 2, 8, 12, 0);
        when(timeProvider.getCurrentTime()).thenReturn(now);
        when(booking.getStartTime()).thenReturn(now.minusMinutes(10));

        BookingSystem system = new BookingSystem(
                timeProvider,
                roomRepository,
                notificationService
        );

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> system.cancelBooking("booking1")
        );
        assertThat(exception.getMessage())
                .contains("Kan inte avboka påbörjad eller avslutad bokning");

        verify(room, never()).removeBooking(any());
        verify(roomRepository, never()).save(any());
        verifyNoInteractions(notificationService);
    }

    @Test
    void cancelBookingReturnsTrueAndRemovesBookingWhenValid() throws NotificationException {
        RoomRepository roomRepository = mock(RoomRepository.class);
        NotificationService notificationService = mock(NotificationService.class);
        TimeProvider timeProvider = mock(TimeProvider.class);

        Room room = mock(Room.class);
        Booking booking = mock(Booking.class);

        when(roomRepository.findAll()).thenReturn(List.of(room));
        when(room.hasBooking("booking1")).thenReturn(true);
        when(room.getBooking("booking1")).thenReturn(booking);

        LocalDateTime now = LocalDateTime.of(2026, 2, 8, 12, 0);
        when(timeProvider.getCurrentTime()).thenReturn(now);
        when(booking.getStartTime()).thenReturn(now.plusHours(1));

        BookingSystem system = new BookingSystem(
                timeProvider,
                roomRepository,
                notificationService
        );

        boolean result = system.cancelBooking("booking1");

        assertThat(result).isTrue();

        verify(room).removeBooking("booking1");
        verify(roomRepository).save(room);
        verify(notificationService).sendCancellationConfirmation(booking);
    }
}

