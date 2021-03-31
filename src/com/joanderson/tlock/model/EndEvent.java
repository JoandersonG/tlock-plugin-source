package com.joanderson.tlock.model;

import java.util.ArrayList;

public class EndEvent extends Component {
    private ArrayList<String> incomings;

    public EndEvent(String name, String originalName, String id, ArrayList<String> incomings) {
        super(name, originalName, id);
        this.incomings = incomings;
    }

    public ArrayList<String> getIncomings() {
        return incomings;
    }

    public void setIncomings(ArrayList<String> incomings) {
        this.incomings = incomings;
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        for (String incoming : incomings) {
            s.append(incoming).append("\t");
        }
        s.append("\n");
        return "    Name: '" + getComponentName() + "'    id: " + getId() + "    Incoming: " + s.toString();
    }

    public String toPiADL() {
        StringBuilder piADLcode = new StringBuilder();
        piADLcode.append("component ").append(getComponentName()).append(" is abstraction (){\n");
        for (int i = 0; i < incomings.size(); i++) {
            incomings.set(i, "entrada_" + (i+1));
            piADLcode.append("\tconnection ").append(incomings.get(i)).append(" is in (Integer)\n");
        }
        piADLcode.append("\tprotocol is {\n")
                .append("\t\t(");
        for (int i = 0; i < incomings.size(); i++) {
            piADLcode.append("via ").append(incomings.get(i)).append(" receive Integer");
            if (i == incomings.size() - 1) {
                piADLcode.append(")*\n");
            } else {
                piADLcode.append(" |\n\t\t");
            }
        }
        piADLcode.append("\t}\n")
                .append("\tbehavior is {\n");
        for (int i = 0; i < incomings.size(); i++) {
            piADLcode.append("\t\tvia ").append(incomings.get(i)).append(" receive x_").append(i).append(" : Integer\n");
        }
        piADLcode.append("\t\tbehavior()\n")
                .append("\t}\n")
                .append("}\n");
        return piADLcode.toString();
    }
}