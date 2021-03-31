package com.joanderson.tlock.model;

import java.util.ArrayList;

public class ChoreographyTask extends Component{
    private String incoming;
    private String outgoing;
    private ArrayList<String> incomings;
    private ArrayList<String> outgoings;
    private Participant initParticipant;
    private ArrayList<Participant> participants;
    private ArrayList<String> messageFlowIds;

    public ChoreographyTask(
            String id,
            String name,
            String originalName,
            Participant initParticipant,
            ArrayList<Participant> participants,
            ArrayList<String> messageFlowIds
    ) {
        super(name, originalName, id);
        this.initParticipant = initParticipant;
        this.participants = participants;
        this.messageFlowIds = messageFlowIds;
        this.incomings = new ArrayList<>();
        this.outgoings = new ArrayList<>();
    }

    public String getIncoming() {
        return incoming;
    }

    public void deleteIncoming(String in){
        incomings.remove(in);
    }


    public ArrayList<String> getIncomings() {
        return incomings;
    }

    public void setIncoming(String incoming) {
        this.incoming = incoming;
    }

    public void setOutgoing(String outgoing) {
        this.outgoing = outgoing;
    }

    public String getOutgoing() {
        return outgoing;
    }

    public Participant getInitParticipant() {
        return initParticipant;
    }

    public void setInitParticipant(Participant initParticipant) {
        this.initParticipant = initParticipant;
    }

    public ArrayList<Participant> getParticipants() {
        return participants;
    }

    public void setParticipants(ArrayList<Participant> participants) {
        this.participants = participants;
    }

    public ArrayList<String> getMessageFlowIds() {
        return messageFlowIds;
    }

    public void setMessageFlowIds(ArrayList<String> messageFlowIds) {
        this.messageFlowIds = messageFlowIds;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Task id: ").append(getId()).append("\n    Name: ")
                .append(getComponentName() != null ? getComponentName() : "Sem Nome")
                .append("\n    Incoming: ").append(incoming)
                .append("\n    Outgoing: ").append(outgoing)
                .append("\n    InitiatingId: ").append(initParticipant)
                .append("\n    Participants:\n");
        for (Participant p : participants) {
            sb.append("        model.Participant: ").append(p.getId()).append("\n");
        }
        sb.append("    MessageFlows: \n");
        for (String mId : messageFlowIds) {
            sb.append("        model.MessageFlow: ").append(mId).append("\n");
        }

        return sb.toString();
    }


    public String toPiADL() {
        StringBuilder piADLcode = new StringBuilder();
        incoming = "entrada";
        outgoing = "saida";
        piADLcode.append("component ").append(getComponentName()).append(" is abstraction (){\n")
                .append("\tconnection ").append(incoming).append(" is in (Integer)\n")
                .append("\tconnection ").append(outgoing).append(" is out (Integer)\n");

//        piADLcode.append("\tconnection ").append(initParticipant.getNewName()).append(" is in (String)\n")
//                 .append("\tconnection ").append(initParticipant.getNewName()).append("2 is out (String)\n");

        incomings.add(initParticipant.getConnectionName(this.getId(), Connection.Type.OUT));
        outgoings.add(initParticipant.getConnectionName(this.getId(), Connection.Type.IN));
        piADLcode.append("\tconnection ").append(initParticipant.getConnectionName(this.getId(), Connection.Type.OUT)).append(" is in (String)\n")
                 .append("\tconnection ").append(initParticipant.getConnectionName(this.getId(), Connection.Type.IN)).append(" is out (String)\n");
        for (Participant p : participants) {
            if (p.equals(initParticipant)) {
                continue;
            }
//            piADLcode.append("\tconnection ").append(p.getNewName()).append(" is out (String)\n")
//                    .append("\tconnection ").append(p.getNewName()).append("2 is in (String)\n");
            outgoings.add(p.getConnectionName(this.getId(), Connection.Type.IN));
            piADLcode.append("\tconnection ").append(p.getConnectionName(this.getId(), Connection.Type.IN)).append(" is out (String)\n")
                    .append("\tconnection ").append(p.getConnectionName(this.getId(), Connection.Type.OUT)).append(" is in (String)\n");
            incomings.add(p.getConnectionName(this.getId(), Connection.Type.OUT));
        }
        piADLcode.append("\tprotocol is {\n")
                .append("\t\t(via ").append(incoming).append(" receive Integer |")
                .append(" via ").append(outgoing).append(" send Integer |\n");
        for (int i = 0; i < participants.size(); i++) {
            Participant p = participants.get(i);
            piADLcode.append("\t\tvia ").append(p.getConnectionName(this.getId(), Connection.Type.OUT)).append(" receive").append(" String |\n")
                    .append("\t\tvia ").append(p.getConnectionName(this.getId(), Connection.Type.IN)).append(" send").append(" String");
            if (i+1 == participants.size()) {
                piADLcode.append(")*\n");
            } else {
                piADLcode.append(" |\n");
            }
        }
        piADLcode.append("\t}\n")
            .append("\tbehavior is {\n")
            .append("\t\tvia ").append(incoming).append(" receive x : Integer\n")
            .append("\t\tvia ").append(initParticipant.getConnectionName(this.getId(), Connection.Type.OUT)).append(" receive msg1 : String\n");
        ArrayList<Integer> toSend = new ArrayList<>();
        toSend.add(1);
        for (int i = 0; i < participants.size(); i++) {
            Participant p = participants.get(i);
            if (p.equals(initParticipant)) {
                continue;
            }
            piADLcode.append("\t\tvia ").append(p.getConnectionName(this.getId(), Connection.Type.IN)).append(" send msg").append(toSend.get(0)).append("\n");
            piADLcode.append("\t\tvia ").append(p.getConnectionName(this.getId(), Connection.Type.OUT)).append(" receive msg").append(i+2).append(" : String\n");
            toSend.remove(0);
            toSend.add(i+2);
        }
        piADLcode.append("\t\tvia ").append(initParticipant.getConnectionName(this.getId(), Connection.Type.IN)).append(" send msg").append(toSend.get(0)).append("\n");
        piADLcode.append("\t\tvia ").append(outgoing)
            .append(" send x\n")
            .append("\t\tbehavior()\n")
            .append("\t}\n")
            .append("}\n");
        return piADLcode.toString();
    }

    public String toPiADL2() {
        StringBuilder piADLcode = new StringBuilder();
        incoming = "entrada";
        outgoing = "saida";
        piADLcode.append("component ").append(getComponentName()).append(" is abstraction (){\n")
                .append("\tconnection ").append(incoming).append(" is in (Integer)\n")
                .append("\tconnection ").append(outgoing).append(" is out (Integer)\n");

//        piADLcode.append("\tconnection ").append(initParticipant.getNewName()).append(" is in (String)\n")
//                .append("\tconnection ").append(initParticipant.getNewName()).append("2 is out (String)\n");
//        for (Participant p : participants) {
//            if (p.equals(initParticipant)) {
//                continue;
//            }
//            piADLcode.append("\tconnection ").append(p.getNewName()).append(" is out (String)\n")
//                    .append("\tconnection ").append(p.getNewName()).append("2 is in (String)\n");
//        }
        piADLcode.append("\tprotocol is {\n")
                .append("\t\t(via ").append(incoming).append(" receive Integer |")
                .append(" via ").append(outgoing).append(" send Integer)*\n");
//        for (int i = 0; i < participants.size(); i++) {
//            Participant p = participants.get(i);
//            piADLcode.append("\t\tvia ").append(p.getNewName()).append(p.equals(initParticipant) ? " receive" : " send").append(" String |\n")
//                    .append("\t\tvia ").append(p.getNewName()).append("2").append(p.equals(initParticipant) ? " send" : " receive").append(" String");
//            if (i+1 == participants.size()) {
//                piADLcode.append(")*\n");
//            } else {
//                piADLcode.append(" |\n");
//            }
//        }
        piADLcode.append("\t}\n")
                .append("\tbehavior is {\n")
                .append("\t\tvia ").append(incoming).append(" receive x : Integer\n");
//            .append("\t\tvia ").append(initParticipant.getNewName()).append(" receive msg : String\n");
//        for (int i = 0; i < participants.size(); i++) {
//            Participant p = participants.get(i);
//            if (p.equals(initParticipant)) {
//                continue;
//            }
//            piADLcode.append("\t\tvia ").append(p.getNewName()).append(" send msg\n")
//                    .append("\t\tvia ").append(p.getNewName()).append("2 receive msg").append(i+1).append(" : String\n");
//            piADLcode.append("\t\tvia ").append(initParticipant.getNewName()).append("2 send msg").append(i+1).append("\n");
//        }
        piADLcode.append("\t\tvia ").append(outgoing)
                .append(" send x\n")
                .append("\t\tbehavior()\n")
                .append("\t}\n")
                .append("}\n");
        return piADLcode.toString();
    }

    public ArrayList<String> getOutgoings() {
        return outgoings;
    }
}
