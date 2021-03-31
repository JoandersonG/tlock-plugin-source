package com.joanderson.tlock.model;

import java.util.ArrayList;
import java.util.List;

public class Participant {

    public enum Role {
        START,
        SECONDARY;

    }
    private String id;
    private String name;
    private String newName;
    private ArrayList<Connection> connections;
    private static String idGenerator = "%particId_0";

    public Participant(String id, String name, String newName) {
        this.id = id;
        this.name = name;
        this.newName = newName;
        connections = new ArrayList<>();
    }
    public ArrayList<Connection> getAllFromConnections() {
        ArrayList<Connection> conns = new ArrayList<>();
        for (Connection c : connections) {
            if (c.getType() == Connection.Type.OUT) {
                conns.add(c);
            }
        }
        return conns;
    }

    public String getValidId() {
        idGenerator = idGenerator.replaceFirst("[0-9].*", "" + (Integer.parseInt(idGenerator.substring(idGenerator.lastIndexOf("_")+1)) + 1));
        return idGenerator;
    }

    public List<Connection> getAllConnections() {
        return connections;
    }

    public String getConnectionName(String id, Connection.Type type) {
        for (Connection c : connections) {
            if (c.getTaskId().equals(id) && c.getType() == type) {
                return c.getName();
            }
        }
        return "Connection name not found";
    }

    public void addConnections(Role type, String taskId) {
        if (type == Role.START) {
            connections.add(new Connection(newName + (connectionCount() + 1), Connection.Type.OUT, taskId));
            connections.add(new Connection(newName + (connectionCount() + 1), Connection.Type.IN, taskId));
        } else {
            connections.add(new Connection(newName + (connectionCount() + 1), Connection.Type.IN, taskId));
            connections.add(new Connection(newName + (connectionCount() + 1), Connection.Type.OUT, taskId));;
        }
    }

    public int connectionCount() {
        return connections.size();
    }
    public String getNewName() {
        return newName;
    }

    public void setNewName(String newName) {
        this.newName = newName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Id: " + id + "  Name: " + name;
    }

    @Override
    public boolean equals(Object obj) {
        return obj.getClass() == this.getClass() && ((Participant)obj).getId().equals(id);
    }

    public String toPiADL() {
        StringBuilder piADLcode = new StringBuilder();
        piADLcode.append("component ").append(newName).append(" is abstraction (){\n");

        for (Connection c : connections) {
            if (c.getType() == Connection.Type.IN) {
                piADLcode.append("\tconnection ").append(c.getName()).append(" is in (String)\n");
            } else {
                piADLcode.append("\tconnection ").append(c.getName()).append(" is out (String)\n");
            }
        }
//        piADLcode.append("\tconnection ").append("de").append((from != null ? from.getComponentName() : "Sem id")).append(" is in (Integer)\n");
//        piADLcode.append("\tconnection ").append("para").append((to != null ? to.getComponentName() : "Sem id")).append(" is out (Integer)\n");
        piADLcode.append("\tprotocol is {\n");
        for (int i = 0; i < connections.size(); i++) {
            Connection c = connections.get(i);
            piADLcode.append("\t\t");
            if (i == 0) {
                piADLcode.append("(");
            }
            if (c.getType() == Connection.Type.IN) {
                piADLcode.append("via ").append(c.getName()).append(" receive String");
            } else {
                piADLcode.append("via ").append(c.getName()).append(" send String");
            }
            if (i != connections.size() -1) {
                piADLcode.append(" |\n");
            } else {
                piADLcode.append(")*\n");
            }
        }
        piADLcode.append("\t}\n")
                .append("\tbehavior is {\n");
        StringBuilder sbIn = new StringBuilder();
        StringBuilder sbOut = new StringBuilder();
        for (int i = 0; i < connections.size(); i++) {
            Connection c = connections.get(i);
            if (c.getType() == Connection.Type.IN) {
                sbIn.append("\t\tvia ").append(c.getName()).append(" receive msg").append(i + 1).append(" : String\n");
            } else {
                sbOut.append("\t\tvia ").append(c.getName()).append(" send \"TODO: message here\"\n");
            }
        }
        piADLcode.append(sbOut)
                .append(sbIn);
//                .append("\t\tvia ").append("de").append((from != null ? from.getComponentName() : "Sem id")
//        ).append(" receive x : Integer\n")
//                .append("\t\tvia ").append("para").append((to != null ? to.getComponentName() : "Sem id")).append(" send x\n")
                piADLcode.append("\t\tbehavior()\n")
                .append("\t}\n")
                .append("}\n");
        return piADLcode.toString();
    }
}
