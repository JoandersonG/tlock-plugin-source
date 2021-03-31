package com.joanderson.tlock.model;

public class Connector {
    private String name;
    private String instanceName;
    private String id;
    private Component from;
    private String fromId;
    private Component to;
    private String toId;
    private static String idGenerator = "%connId_0";


    public Connector(String name, String id, Component from, Component to) {
        this.name = name;
        this.id = id;
        this.from = from;
        this.to = to;
    }

    public static String getValidId() {
        idGenerator = idGenerator.replaceFirst("[0-9].*", "" + (Integer.parseInt(idGenerator.substring(idGenerator.lastIndexOf("_")+1)) + 1));
        return idGenerator;
    }

    public String getInstanceName() {
        return instanceName;
    }

    public void setInstanceName(String instanceName) {
        this.instanceName = instanceName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Component getFrom() {
        return from;
    }

    public void setFrom(Component from) {
        this.from = from;
    }

    public Component getTo() {
        return to;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String toPiADL(String paramType) {
        StringBuilder piADLcode = new StringBuilder();
        piADLcode.append("connector ").append(name).append(" is abstraction (){\n");
        piADLcode.append("\tconnection ").append("de").append((from != null ? from.getComponentName() : "Sem id")).append(" is in (").append(paramType).append(")\n");
        piADLcode.append("\tconnection ").append("para").append((to != null ? to.getComponentName() : "Sem id")).append(" is out (").append(paramType).append(")\n");
        piADLcode.append("\tprotocol is {\n")
                .append("\t\t(via ").append("de").append((from != null ? from.getComponentName() : "Sem id")).append(" receive ").append(paramType).append(" |")
                .append(" via ").append("para").append((to != null ? to.getComponentName() : "Sem id")).append(" send ").append(paramType).append(")*\n")
                .append("\t}\n")
                .append("\tbehavior is {\n")
                .append("\t\tvia ").append("de").append((from != null ? from.getComponentName() : "Sem id")
        ).append(" receive x : ").append(paramType).append("\n")
                .append("\t\tvia ").append("para").append((to != null ? to.getComponentName() : "Sem id")).append(" send x\n")
                .append("\t\tbehavior()\n")
                .append("\t}\n")
                .append("}\n");
        return piADLcode.toString();
    }

    public void setTo(Component to) {
        this.to = to;
    }

    @Override
    public String toString() {
        return "Id:" + id + "    From: " + (from != null ? from.getComponentName() : "Sem nome") +
                "    To: " + (to != null ? to.getComponentName() : "Sem nome") ;
    }

    public static String participantConnectorToPiADL() {
        StringBuilder piADLcode = new StringBuilder();
        piADLcode.append("connector ParticipantConnector is abstraction() {\n")
                .append("\tconnector entrada is in(String)\n")
                .append("\tconnector saida is out(String)\n");
        piADLcode.append("\tprotocol is {\n")
                .append("\t\tvia entrada receive String | via saida send String)*\n")
                .append("\t}\n")
                .append("\tbehavior is {...\n");
        return piADLcode.toString();
    }


}
