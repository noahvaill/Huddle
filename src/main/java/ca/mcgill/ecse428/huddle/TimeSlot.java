package ca.mcgill.ecse428.huddle;

import java.time.LocalDate;
import java.time.LocalTime;

public record TimeSlot(String court, Sport sport, LocalDate date, LocalTime start, LocalTime end, boolean available) {}
