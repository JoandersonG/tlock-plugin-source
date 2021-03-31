package com.joanderson.tlock.model;

import java.util.Objects;

public class ParticipantTask extends Component{

    private Participant participant;

    public ParticipantTask(Participant participant, String componentName, String originalName, String id) {
        super(componentName, originalName, id);
        this.participant = participant;
    }

    public Participant getParticipant() {
        return participant;
    }

    @Override
    public String toPiADL() {
        return participant.toPiADL();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ParticipantTask that = (ParticipantTask) o;
        return participant.equals(that.participant);
    }

    @Override
    public int hashCode() {
        return Objects.hash(participant);
    }

    public boolean hasConnection(String port) {
        for (Connection c : participant.getAllConnections()) {
            if (c.getName().equals(port)) {
                return true;
            }
        }
        return false;
    }
}
