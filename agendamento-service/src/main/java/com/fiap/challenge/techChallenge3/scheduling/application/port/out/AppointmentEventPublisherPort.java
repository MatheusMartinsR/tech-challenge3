package com.fiap.challenge.techChallenge3.scheduling.application.port.out;

import com.fiap.challenge.techChallenge3.scheduling.domain.model.Appointment;

public interface AppointmentEventPublisherPort {

    void publishAppointmentCreated(Appointment appointment);

    void publishAppointmentUpdated(Appointment appointment);
}
