package com.joanderson.tlock.model;

public class MessageFlow {
    private String id;
    private Participant sender;
    private Participant receiver;

    public MessageFlow(String id, Participant sender, Participant receiver) {
        if (sender == null) {
            throw new IllegalArgumentException("Send participant cannot be null");
        }
        if (receiver == null) {
            throw new IllegalArgumentException("Receive participant cannot be null");
        }

        this.id = id;
        this.sender = sender;
        this.receiver = receiver;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Participant getSender() {
        return sender;
    }

    public void setSender(Participant sender) {
        this.sender = sender;
    }

    public Participant getReceiver() {
        return receiver;
    }

    public void setReceiver(Participant receiver) {
        this.receiver = receiver;
    }

    @Override
    public String toString() {
        return "Id: " + id + "    Sender:" + sender.getName() + "    Receiver: " + receiver.getName();
    }
}
