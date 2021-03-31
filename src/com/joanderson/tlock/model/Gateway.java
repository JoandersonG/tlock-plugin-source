package com.joanderson.tlock.model;

import java.util.ArrayList;

public class Gateway extends Component {
    public enum Type { EXCLUSIVE_GATEWAY, PARALLEL_GATEWAY }

    private ArrayList<String> incomings;
    private ArrayList<String> outgoings;
    private Type type;

    public Gateway(String name, String originalName, String id, ArrayList<String> incomings, ArrayList<String> outgoings, Type type) {
        super(name, originalName, id);
        this.incomings = incomings;
        this.outgoings = outgoings;
        this.type = type;
    }

    public ArrayList<String> getIncomings() {
        return incomings;
    }

    public void setIncomings(ArrayList<String> incomings) {
        this.incomings = incomings;
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
        s.append("    Name: '").append(getComponentName()).append("'    Type: ").append(type).append("    id: ").append(getId());
        s.append("\n\t    Incoming: ");
        for (String inc : incomings) {
            s.append(inc).append("\t");
        }
        s.append("\n\t    Outgoing: ");
        for (String out : outgoings) {
            s.append(out).append("\t");
        }
        s.append("\n");
        return s.toString();
    }

    public String toPiADL() {
        if (type == Type.EXCLUSIVE_GATEWAY) {
            return exclusiveGatewayToPiADL();
        }
        return parallelGatewayToPiADL();
    }

    private String parallelGatewayToPiADL() {
        StringBuilder s = new StringBuilder();
        s.append("component ").append(getComponentName()).append(" is abstraction (){\n");
        for (int i = 0; i < incomings.size(); i++) {
            incomings.set(i, "entrada" + (i+1));
            s.append("\tconnection ").append(incomings.get(i)).append(" is in (Integer)\n");
        }
        for (int i = 0; i < outgoings.size(); i++) {
            outgoings.set(i, "saida" + (i+1));
            s.append("\tconnection ").append(outgoings.get(i)).append(" is out (Integer)\n");
        }
        s.append("\tprotocol is {\n")
                .append("\t\t(");
        for (int i = 0; i < incomings.size(); i++) {
            s.append("via ").append(incomings.get(i)).append(" receive Integer |\n\t\t");
        }
        for (int i = 0; i < outgoings.size(); i++) {
            s.append("via ").append(outgoings.get(i)).append(" send Integer");
            if (i == outgoings.size() - 1) {
                s.append(")*\n");
            } else {
                s.append(" |\n\t\t");
            }
        }
        s.append("\t}\n");
        s.append("\tbehavior is {\n");
        for (int i = 0; i < incomings.size(); i++) {
                s.append("\t\tvia ").append(incomings.get(i)).append(" receive x").append(i+1).append(" : Integer\n");
        }
        if (outgoings.size() > 1) {
            s.append("\t\tcompose {\n");
            for (int i = 0; i < outgoings.size(); i++) {
                if (i != 0) {
                    s.append("\t\t\tand ");
                } else {
                    s.append("\t\t\t");
                }
                s.append("via ").append(outgoings.get(i)).append(" send x1\n");
            }
            s.append("\t\t}\n");
        } else {
            s.append("\t\tvia ").append("saida").append(1).append(" send x1\n");
        }
        s.append("\t\tbehavior()\n");
        s.append("\t}\n");
        s.append("}\n");
        return s.toString();
    }

    private String exclusiveGatewayToPiADL() {
        StringBuilder s = new StringBuilder();
        s.append("component ").append(getComponentName()).append(" is abstraction (){\n");
        for (int i = 0; i < incomings.size(); i++) {
            incomings.set(i, "entrada" + (i+1));
            s.append("\tconnection ").append(incomings.get(i)).append(" is in (Integer)\n");
        }
        for (int i = 0; i < outgoings.size(); i++) {
            outgoings.set(i, "saida" + (i+1));
            s.append("\tconnection ").append(outgoings.get(i)).append(" is out (Integer)\n");
        }
        s.append("\tprotocol is {\n")
                .append("\t\t(");
        for (int i = 0; i < incomings.size(); i++) {
            s.append("via ").append(incomings.get(i)).append(" receive Integer |\n\t\t");
        }
        for (int i = 0; i < outgoings.size(); i++) {
            s.append("via ").append(outgoings.get(i)).append(" send Integer");
            if (i == outgoings.size() - 1) {
                s.append(")*\n");
            } else {
                s.append(" |\n\t\t");
            }
        }
        s.append("\t}\n");
        s.append("\tbehavior is {\n");
        //uma entrada
        if (incomings.size() == 1) {
            s.append("\t\tvia ").append("entrada").append(1).append(" receive x").append(1).append(" : Integer\n");
            //uma saída
            if (outgoings.size() == 1) {
                s.append("\t\tvia ").append("saida").append(1).append(" send x1\n");
                s.append("\t\tbehavior()\n");
            } else {
                //uma entrada, múltiplas saídas
                s.append("\t\tchoose {\n");
                for (int j = 0; j < outgoings.size(); j++) {
                    s.append("\t\t\tvia ").append("saida").append(j+1).append(" send x1\n");
                    s.append("\t\t\tbehavior()\n");
                    if (j+1 == outgoings.size()) {
                        s.append("\t\t}\n");
                    } else {
                        s.append("\t\tor\n");
                    }
                }
            }
        } else {
            //múltiplas entradas
            s.append("\t\tchoose {\n");
            for (int i = 0; i < incomings.size(); i++) {
                s.append("\t\t\tvia ").append("entrada").append(i+1).append(" receive x").append(i+1).append(" : Integer\n");
                //múltiplas entradas, uma saída
                if (outgoings.size() == 1) {
                    StringBuilder valForSending = new StringBuilder("x" + (i+1));
//                    valForSending.append("x1");
//                    for (int k = 1; k < incomings.size(); k++) {
//                        valForSending.append(" + x").append(k+1);
//                    }
                    s.append("\t\t\tvia ").append("saida").append(1).append(" send ").append(valForSending).append("\n");
                    s.append("\t\t\tbehavior()\n");
                } else {
                    //múltiplas entradas, múltiplas saídas
                    /*s.append("\t\t\tchoose {\n");
                    for (int j = 0; j < outgoings.size(); j++) {
                        s.append("\t\t\t\tvia ").append("saida").append(j+1).append(" send x").append(i+1).append("\n");
                        s.append("\t\t\t\tbehavior()\n");
                        if (j+1 == outgoings.size()) {
                            s.append("\t\t\t}\n");
                        } else {
                            s.append("\t\t\tor\n");
                        }
                    }*/
                    s.append("\t\t\tif x").append(i + 1).append(" < 10 then {\n");
                    for (int j = 0; j < outgoings.size(); j++) {
                        s.append("\t\t\t\tvia ").append("saida").append(j+1).append(" send x").append(i+1).append("\n");
                        s.append("\t\t\t\tbehavior()\n");
                        if (j+2 == outgoings.size()) {
                            s.append("\t\t\t} else{\n");
                        }else if (j+1 == outgoings.size()) {
                            s.append("\t\t\t}\n");
                        } else {
                            s.append("\t\t\t} else if x").append(i + 1).append(" < 10 then{\n");
                        }
                    }
                }
                if (i+1 == incomings.size()) {
                    s.append("\t\t}\n");
                } else {
                    s.append("\t\tor\n");
                }
            }
        }
        s.append("\t}\n");
        s.append("}\n");
        return s.toString();
    }
}
