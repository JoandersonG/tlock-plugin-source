package com.joanderson.tlock.model;

import java.util.ArrayList;

public class StartEvent extends Component{
    private ArrayList<String> outgoings;

    public StartEvent(String name, String originalName, String id, ArrayList<String> outgoings) {
        super(name, originalName, id);
        this.outgoings = outgoings;
    }

    public ArrayList<String> getOutgoings() {
        return outgoings;
    }

    public void setOutgoings(ArrayList<String> outgoings) {
        this.outgoings = outgoings;
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        for (String outgoing : outgoings) {
            s.append(outgoing).append("\t");
        }
        s.append("\n");
        return "    Name: '" + getComponentName() + "'    id: " + getId() + "    Outgoing: " + s.toString();
    }

    public String toPiADL() {
        StringBuilder piADLcode = new StringBuilder();
        piADLcode.append("component ").append(getComponentName()).append(" is abstraction (){\n");
        for (int i = 0; i < outgoings.size(); i++) {
            outgoings.set(i,"saida_" + (i+1));
            piADLcode.append("\tconnection ").append(outgoings.get(i)).append(" is out (Integer)\n");
        }
        piADLcode.append("\tprotocol is {\n")
        .append("\t\t(");
        for (int i = 0; i < outgoings.size(); i++) {
            piADLcode.append("via ").append(outgoings.get(i)).append(" send Integer");
            if (i == outgoings.size() - 1) {
                piADLcode.append(")*\n");
            } else {
                piADLcode.append(" |\n\t\t");
            }
        }
        piADLcode.append("\t}\n")
                .append("\tbehavior is {\n");
        for (int i = 0; i < outgoings.size(); i++) {
            piADLcode.append("\t\tvia ").append(outgoings.get(i)).append(" send 0\n");
        }
        piADLcode.append("\t\tbehavior()\n")
                .append("\t}\n")
                .append("}\n");
        return piADLcode.toString();
    }
}
