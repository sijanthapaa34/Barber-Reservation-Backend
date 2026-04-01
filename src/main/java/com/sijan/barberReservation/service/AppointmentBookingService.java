package com.sijan.barberReservation.service;

import com.sijan.barberReservation.model.Appointment;
import com.sijan.barberReservation.model.PaymentTransaction;

public interface AppointmentBookingService {
    Appointment bookPaidAppointment(PaymentTransaction tx);
}
