package com.joanderson.tlock.model;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.Normalizer;
import java.util.ArrayList;
import static java.lang.System.exit;

public class YaoqiangXMLParser {
	
	private static YaoqiangXMLParser instance;
    ArrayList<Participant> participants;
    ArrayList<ParticipantTask> participantTasks;
    ArrayList<Message> messages;
    ArrayList<MessageFlow> messageFlows;
    ArrayList<Connector> connectors;
    ArrayList<ChoreographyTask> tasks;
    ArrayList<StartEvent> startEvents;
    ArrayList<EndEvent> endEvents;
    ArrayList<Gateway> gateways;
    DocumentBuilder builder;
    
    public static YaoqiangXMLParser getNewInstance() throws ParserConfigurationException {
    	instance = new YaoqiangXMLParser();
    	return instance;
    }
    
    public static YaoqiangXMLParser getInstance() throws ParserConfigurationException {
    	if (instance == null) {
    		instance = new YaoqiangXMLParser();
    	}
    	return instance;
    }

    private YaoqiangXMLParser() throws ParserConfigurationException {
        participants = new ArrayList<>();
        participantTasks = new ArrayList<>();
        messages = new ArrayList<>();
        messageFlows = new ArrayList<>();
        connectors = new ArrayList<>();
        tasks = new ArrayList<>();
        startEvents = new ArrayList<>();
        endEvents = new ArrayList<>();
        gateways = new ArrayList<>();
        builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
    }

    //This method parses Yaoqiang's BPMN 2.0's coreography diagram
    public void parseBPMN(String filePath) throws IOException, SAXException {
        try {
            Document doc = builder.parse(filePath);
            parseParticipants(doc);
            parseMessage(doc);
            parseMessageAssociation(doc);
            parseMessageFlows(doc);
            parseConnectors(doc);
            parseChoreographyTasks(doc);
            parseSubChoreographyTasks(doc);
            parseStartEvents(doc);
            parseEndEvents(doc);
            parseGateways(doc);
            System.out.println();
        } catch (FileNotFoundException exception) {
            throw new FileNotFoundException();
        }
    }

    private void parseParticipants(Document doc) {
        NodeList participantsNodes = doc.getElementsByTagName("participant");
        for (int i = 0; i < participantsNodes.getLength(); i++) {
            Node node = participantsNodes.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element participant = (Element) node;
                String participantId = participant.getAttribute("id");
                String participantName = participant.getAttribute("name");
                participants.add(new Participant(participantId, participantName, removeNonAlphanumericSymbols(participantName)));
            }
        }
    }

    private String removeNonAlphanumericSymbols(String str) {
        return str.replaceAll("[^a-zA-Z0-9]", "");
    }

    private void parseMessage(Document doc) {
        NodeList messageNodes = doc.getElementsByTagName("message");
        for (int i = 0; i < messageNodes.getLength(); i++) {
            Node node = messageNodes.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element message = (Element) node;
                String id = message.getAttribute("id");
                String name = message.getAttribute("name");
                messages.add(new Message(id, name));
            }
        }
    }

    private void parseMessageAssociation(Document doc) {
        NodeList associationNodes = doc.getElementsByTagName("association");
        for (int i = 0; i < associationNodes.getLength(); i++) {
            Node node = associationNodes.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element association = (Element) node;
                String messageId = association.getAttribute("sourceRef");
                String participantId = association.getAttribute("targetRef");
                addParticipantToMessage(messageId,participantId);
            }
        }
    }

    private void parseMessageFlows(Document doc) {
        NodeList messageFlowNodes = doc.getElementsByTagName("messageFlow");
        for (int i = 0; i < messageFlowNodes.getLength(); i++) {
            Node node = messageFlowNodes.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element messageFlow = (Element) node;
                String id = messageFlow.getAttribute("id");
                String sendId = messageFlow.getAttribute("sourceRef");
                String receiveId = messageFlow.getAttribute("targetRef");
                try {
                    messageFlows.add(new MessageFlow(id,getParticipant(sendId),getParticipant(receiveId)));
                } catch (IllegalArgumentException e) {
                    System.out.println("Erro: um dos participantes envolvidos em  model.Message flow não foi encontrado");
                }
            }
        }
    }

    private Component getComponent(String id) {
        for (StartEvent s : startEvents) {
            if (s.getId().equals(id)) {
                return s;
            }
        }
        for (EndEvent e : endEvents) {
            if (e.getId().equals(id)) {
                return e;
            }
        }
        for (ChoreographyTask ct : tasks) {
            if (ct.getId().equals(id)) {
                return ct;
            }
        }
        for (Gateway g : gateways) {
            if (g.getId().equals(id)) {
                return g;
            }
        }
        return null;
    }

    private void parseConnectors(Document doc) {
        NodeList connectorNodes = doc.getElementsByTagName("sequenceFlow");
        for (int i = 0; i < connectorNodes.getLength(); i++) {
            Node node = connectorNodes.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element connector = (Element) node;
                String id = connector.getAttribute("id");
                String name = "Fluxo_" + (i+1);
                String fromId = connector.getAttribute("sourceRef");
                String toId = connector.getAttribute("targetRef");
                connectors.add(new Connector(name, id, getComponent(fromId), getComponent(toId)));
            }
        }
    }

    private void parseChoreographyTasks(Document doc) {
        NodeList tasksList = doc.getElementsByTagName("choreographyTask");
        for (int i = 0; i < tasksList.getLength(); i++) {
            Node node = tasksList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element task = (Element) node;
                Participant initiating = getParticipant(task.getAttribute("initiatingParticipantRef"));
                String originalName = task.getAttribute("name");
                String componentName = originalName.equals("") ? getUniqueComponentName("Task", String.valueOf(i + 1)) : getUniqueComponentName(originalName, String.valueOf(i + 1));
                String id = task.getAttribute("id");
//                if ( initiating != null) {
//                    initiating.addConnections(Participant.Role.START, id);
//                }
                String incoming = "";
                String outgoing = "";
                ArrayList<Participant> choreoParticipants = new ArrayList<>();
                ArrayList<String> messageFlowIds = new ArrayList<>();
                NodeList insideInfoNode = task.getChildNodes();
                for (int j = 0; j < insideInfoNode.getLength(); j++) {
                    Node itemNode = insideInfoNode.item(j);
                    if (itemNode.getNodeType() == Node.ELEMENT_NODE) {
                        Element item = (Element) itemNode;
                        String itemName = item.getTagName();
                        switch (itemName) {
                            case "incoming":
                                incoming = item.getTextContent();
                                break;
                            case "outgoing":
                                outgoing = item.getTextContent();
                                break;
                            case "participantRef":
                                choreoParticipants.add(getParticipant(item.getTextContent()));
                                break;
                            case "messageFlowRef":
                                messageFlowIds.add(item.getTextContent());
                                break;
                        }
                    }
                }
                for (Participant p : choreoParticipants) {
                    p.addConnections(p.equals(initiating) ? Participant.Role.START : Participant.Role.SECONDARY, id);
                }
                Connector in = getConnector(incoming);
                Connector out = getConnector(outgoing);
                ChoreographyTask ct = new ChoreographyTask(
                        id,
                        componentName,
                        originalName,
                        initiating,
                        choreoParticipants,
                        messageFlowIds
                        );
                tasks.add(ct);
                if (in != null) {
                    in.setTo(ct);
                }
                if (out != null) {
                    out.setFrom(ct);
                }
            }
        }
    }

    private void parseSubChoreographyTasks(Document doc) {
        NodeList tasksList = doc.getElementsByTagName("subChoreography");
        for (int i = 0; i < tasksList.getLength(); i++) {
            Node node = tasksList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element task = (Element) node;
                Participant initiating = getParticipant(task.getAttribute("initiatingParticipantRef"));
                String originalName = task.getAttribute("name");
                String componentName = originalName.equals("") ? getUniqueComponentName("SubTask", String.valueOf(i + 1)) : getUniqueComponentName(originalName, String.valueOf(i + 1));
                String id = task.getAttribute("id");
                String incoming = "";
                String outgoing = "";
                ArrayList<Participant> choreoParticipants = new ArrayList<>();
                ArrayList<String> messageFlowIds = new ArrayList<>();
                NodeList insideInfoNode = task.getChildNodes();
                for (int j = 0; j < insideInfoNode.getLength(); j++) {
                    Node itemNode = insideInfoNode.item(j);
                    if (itemNode.getNodeType() == Node.ELEMENT_NODE) {
                        Element item = (Element) itemNode;
                        String itemName = item.getTagName();
                        switch (itemName) {
                            case "incoming":
                                incoming = item.getTextContent();
                                break;
                            case "outgoing":
                                outgoing = item.getTextContent();
                                break;
                            case "participantRef":
                                choreoParticipants.add(getParticipant(item.getTextContent()));
                                break;
                            case "messageFlowRef":
                                messageFlowIds.add(item.getTextContent());
                                break;
                        }
                    }
                }
                for (Participant p : choreoParticipants) {
                    p.addConnections(p.equals(initiating) ? Participant.Role.START : Participant.Role.SECONDARY, id);
                }
                Connector in = getConnector(incoming);
                Connector out = getConnector(outgoing);
                ChoreographyTask ct = new ChoreographyTask(
                        id,
                        componentName,
                        originalName,
                        initiating,
                        choreoParticipants,
                        messageFlowIds
                );
                tasks.add(ct);
                if (in != null) {
                    in.setTo(ct);
                }
                if (out != null) {
                    out.setFrom(ct);
                }
            }
        }
    }

    private void parseGateways(Document doc) {
        parseGatewayByType(doc, "exclusiveGateway");
        parseGatewayByType(doc, "parallelGateway");
    }
    private void parseGatewayByType(Document doc, String gatewayType) {
        NodeList gatewayList = doc.getElementsByTagName(gatewayType);
        for (int i = 0; i < gatewayList.getLength(); i++) {
            Node node = gatewayList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element gateway = (Element) node;
                String originalName = gateway.getAttribute("name");
                String componentName = getUniqueComponentName(originalName.equals("")? "Gateway" : originalName, String.valueOf(i + 1));
                String id = gateway.getAttribute("id");
                ArrayList<String> incomings = new ArrayList<>();
                ArrayList<String> outgoings = new ArrayList<>();
                NodeList insideInfoNode = gateway.getChildNodes();
                for (int j = 0; j < insideInfoNode.getLength(); j++) {
                    Node itemNode = insideInfoNode.item(j);
                    if (itemNode.getNodeType() == Node.ELEMENT_NODE) {
                        Element item = (Element) itemNode;
                        String itemName = item.getTagName();
                        switch (itemName) {
                            case "incoming":
                                    incomings.add(item.getTextContent());
                                break;
                            case "outgoing":
                                outgoings.add(item.getTextContent());
                                break;
                        }
                    }
                }
                Gateway eg = new Gateway(
                        componentName,
                        originalName,
                        id,
                        incomings,
                        outgoings,
                        gatewayType.equals("exclusiveGateway") ? Gateway.Type.EXCLUSIVE_GATEWAY : Gateway.Type.PARALLEL_GATEWAY
                );
                gateways.add(eg);
                for (String in : incomings) {
                    Connector c = getConnector(in);
                    if (c!= null) {
                        c.setTo(eg);
                    }
                }
                for (String out : outgoings) {
                    Connector c = getConnector(out);
                    if (c!= null) {
                        c.setFrom(eg);
                    }
                }
            }
        }
    }

    public static String removerAcentos(String str) {
        return Normalizer.normalize(str, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "");
    }

    /*
    * Method for creating a valid task name given a name possibly with blank spaces and special characters
    */
    private String getUniqueComponentName(String name, String id) {
        name = removerAcentos(name);
        String[] splitName = name.split("[^a-zA-Z0-9_]");
        StringBuilder sb = new StringBuilder();
        if (splitName[0].matches("[0-9].*")) {
            sb.append("c");
        }
        for (String piece : splitName) {
            if (piece.equals("e")){
                continue;
            }
            if (!piece.matches("[0-9].*") && !piece.equals("") && piece.charAt(0) >= 97) { // turning first letter of each word capital
                piece = piece.replaceFirst("[a-zA-Z]", String.valueOf((char) (piece.charAt(0) - 32)));
            }
            sb.append(piece);
        }
        if (thereIsSuchComponentName(sb.toString())) {
            sb.append(id);
        }
        int i = 1;
        while (thereIsSuchComponentName(sb.toString())) {
            sb.append(i);
            i++;
        }
        return sb.toString();
    }

    private boolean thereIsSuchComponentName(String id) {
        for (ChoreographyTask t : tasks) {
            if (t.getComponentName().equals(id)) {
                return true;
            }
        }
        for (StartEvent s : startEvents) {
            if (s.getComponentName().equals(id)) {
                return true;
            }
        }
        for (EndEvent e : endEvents) {
            if (e.getComponentName().equals(id)) {
                return true;
            }
        }
        for (Gateway g : gateways) {
            if (g.getComponentName().equals(id)) {
                return true;
            }
        }
        return false;
    }

    private void parseStartEvents(Document doc) {

        NodeList nodes = doc.getElementsByTagName("startEvent");
        if (nodes.getLength() < 1) {
          System.out.println("Erro: quantidade de eventos de começo inválida. Forneça ao menos um startEvent.");
          exit(1);
        }
        for (int j = 0; j < nodes.getLength(); j++) {

            Element start = (Element) nodes.item(j);
            String originalName = start.getAttribute("name");
            if (originalName == null || originalName.isEmpty()) {
                originalName = "Start";
            }
            String id = start.getAttribute("id");
            String componentName = getUniqueComponentName(originalName, String.valueOf(j+1));
            NodeList outgoingNodeList = start.getElementsByTagName("outgoing");
            ArrayList<String> outgoingIds = new ArrayList<>();
            for (int i = 0; i < outgoingNodeList.getLength(); i++) {
                Node outgoingNode = outgoingNodeList.item(i);
                if (outgoingNode.getNodeType() == Node.ELEMENT_NODE) {
                    outgoingIds.add(outgoingNode.getTextContent());
                }
            }
            StartEvent newStart = new StartEvent(componentName, originalName, id, outgoingIds);
            startEvents.add(newStart);
            for (String cId : outgoingIds) {
                Connector cnn = getConnector(cId);
                if (cnn != null) {
                cnn.setFrom(newStart);
                }
            }
        }
    }

    private Participant getParticipant(String id) {
        for (Participant p : participants) {
            if (p.getId().equals(id)) {
                return p;
            }
        }
        return null;
    }

    private void parseEndEvents(Document doc) {

        NodeList nodes = doc.getElementsByTagName("endEvent");
        if (nodes.getLength() < 1) {
            System.out.println("Erro: quantidade de eventos de fim inválida. Forneça ao menos um endEvent.");
            exit(1);
        }
        for (int j = 0; j < nodes.getLength(); j++) {

            Element end = (Element) nodes.item(j);
            String originalName = end.getAttribute("name");
            if (originalName == null || originalName.isEmpty()) {
                originalName = "End";
            }
            String componentName = getUniqueComponentName(originalName, String.valueOf(j+1));
            String id = end.getAttribute("id");
            NodeList incomingNodeList = end.getElementsByTagName("incoming");
            ArrayList<String> incomingIds = new ArrayList<>();
            for (int i = 0; i < incomingNodeList.getLength(); i++) {
                Node incomingNode = incomingNodeList.item(i);
                if (incomingNode.getNodeType() == Node.ELEMENT_NODE) {
                    incomingIds.add(incomingNode.getTextContent());
                }
            }
            EndEvent newEnd = new EndEvent(componentName, originalName, id, incomingIds);
            endEvents.add(newEnd);
            for (String cId : incomingIds) {
                Connector cnn = getConnector(cId);
                if (cnn != null) {
                    cnn.setTo(newEnd);
                }
            }
        }
    }

    private Connector getConnector(String id) {
        for (Connector c : connectors) {
            if (c.getId().equals(id)) {
                return c;
            }
        }
        return null;
    }

    //this method adds participantId into the message on messages array with messageId id
    private void addParticipantToMessage(String messageId, String participantId) {
        Participant participant = getParticipant(participantId);
        if (participant == null) {
            return;
        }
        for (Message m : messages) {
            if (m.getId().equals(messageId)) {
                m.setParticipant(participant);
                return;
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append("Participants: \n");
        for (Participant p : participants) {
            s.append("\t");
            s.append(p.toString());
            s.append("\n");
        }
        s.append("Messages: \n");
        for (Message m : messages) {
            s.append("\t");
            s.append(m.toString());
            s.append("\n");
        }
        s.append("MessageFlows: \n");
        for (MessageFlow mf : messageFlows) {
            s.append("\t");
            s.append(mf.toString());
            s.append("\n");
        }
        s.append("Connectors: \n");
        for (Connector c : connectors) {
            s.append("\t");
            s.append(c.toString());
            s.append("\n");
        }
        s.append("ChoreographyTasks: \n");
        for (ChoreographyTask ct : tasks) {
            s.append("\t");
            s.append(ct.toString());
            s.append("\n");
        }
        s.append("StartEvents: \n");
        for (StartEvent se : startEvents) {
            s.append("\t");
            s.append(se.toString());
            s.append("\n");
        }
        s.append("EndEvents: \n");
        for (EndEvent ee : endEvents) {
            s.append("\t");
            s.append(ee.toString());
            s.append("\n");
        }
        s.append("Gateways: \n");
        for (Gateway eg : gateways) {
            s.append("\t");
            s.append(eg.toString());
            s.append("\n");
        }
        return s.toString();
    }



    public String generatePiADL(String archName) {
        StringBuilder piADLcode = new StringBuilder();
        //Gerar código navegando por elementos:
        ArrayList<Component> alreadyRead = new ArrayList<>();
        int connCont = 1;
        for (Participant p : participants) {
            for (int i = 0; i < p.getAllConnections().size(); i++) {
                Connection conn = p.getAllConnections().get(i);
                if (conn.getType() == Connection.Type.IN) {
                    ParticipantTask newPart = new ParticipantTask(p, p.getNewName(),p.getName(), p.getValidId());
                    if (!participantTasks.contains(newPart)) {
                        participantTasks.add(newPart);
                    }
                    Component comp = getComponent(conn.getTaskId());
                    ParticipantTask pt = participantTasks.get(participantTasks.indexOf(newPart));
                    Connector c = new Connector(
                            "Conexao_" + connCont++,
                            Connector.getValidId(),
                            comp,
                            pt
                    );
                    connectors.add(c);
                } else {
                    ParticipantTask newPart = new ParticipantTask(p, p.getNewName(),p.getName(), p.getValidId());
                    if (!participantTasks.contains(newPart)) {
                        participantTasks.add(newPart);
                    }
                    ParticipantTask pt = participantTasks.get(participantTasks.indexOf(newPart));
                    Component comp = getComponent(conn.getTaskId());
                    Connector c = new Connector(
                    		"Conexao_" + connCont++,
                            Connector.getValidId(),
                            pt,
                            comp
                    );
                    connectors.add(c);
                }
            }
        }
        for (StartEvent s : startEvents) {
            piADLcode.append(toPiADL(alreadyRead, s));
        }
        piADLcode.append(generateArchitecture(archName));
        return piADLcode.toString();
    }

    private String toPiADL(ArrayList<Component> alreadyRead, Component component) {
        StringBuilder s = new StringBuilder();
        s.append(component.toPiADL());
        ArrayList<Connector> connectors = findConnectorFrom(component.getId());
        for (Connector conn : connectors) {
            if ( conn.getTo() instanceof ParticipantTask || conn.getFrom() instanceof ParticipantTask) {
                s.append(conn.toPiADL("String"));
            } else {
                s.append(conn.toPiADL("Integer"));
            }
            if (!alreadyRead.contains(conn.getTo())) {
                alreadyRead.add(conn.getTo());
                s.append(toPiADL(alreadyRead, conn.getTo()));
            }
        }
        return s.toString();
    }

    /*Finds Connector which has from field with a component that has cId as it's Id*/
    private ArrayList<Connector> findConnectorFrom(String cId) {
        ArrayList<Connector> conns = new ArrayList<>();
        for (Connector con : connectors) {
            if (con.getFrom().getId().equals(cId)) {
                conns.add(con);
            }
        }
        return conns;
    }

    private String generateArchitecture(String archName) {
        StringBuilder s = new StringBuilder();
        s.append("architecture ").append(archName).append(" is abstraction () {\n");
        s.append("\tbehavior is {\n")
         .append("\t\tcompose {\n");
        for (int i = 0; i < startEvents.size(); i++) {
            startEvents.get(i).setInstanceName(i == 0? "i": "i" + (i+1));
            s.append(i == 0? "\t\t\t" : "\t\t\tand ").append(startEvents.get(i).getInstanceName()).append(" is ").append(startEvents.get(i).getComponentName()).append("()\n");
        }
        for (int i = 0; i < tasks.size(); i++) {
            tasks.get(i).setInstanceName(i == 0? "t": "t" + (i+1));
            s.append("\t\t\tand ").append(tasks.get(i).getInstanceName()).append(" is ").append(tasks.get(i).getComponentName()).append("()\n");
        }
        for (int i = 0; i < participantTasks.size(); i++) {
            participantTasks.get(i).setInstanceName(i == 0? "pt": "pt" + (i+1));
            s.append("\t\t\tand ").append(participantTasks.get(i).getInstanceName()).append(" is ").append(participantTasks.get(i).getComponentName()).append("()\n");
        }
        for (int i = 0; i < gateways.size(); i++) {
            gateways.get(i).setInstanceName(i == 0? "gw": "gw" + (i+1));
            s.append("\t\t\tand ").append(gateways.get(i).getInstanceName()).append(" is ").append(gateways.get(i).getComponentName()).append("()\n");
        }
        for (int i = 0; i < endEvents.size(); i++) {
            endEvents.get(i).setInstanceName(i == 0? "f": "f" + (i+1));
            s.append("\t\t\tand ").append(endEvents.get(i).getInstanceName()).append(" is ").append(endEvents.get(i).getComponentName()).append("()\n");
        }
        for (int i = 0; i < connectors.size(); i++) {
            connectors.get(i).setInstanceName(i == 0? "c": "c" + (i+1));
            s.append("\t\t\tand ").append(connectors.get(i).getInstanceName()).append(" is ").append(connectors.get(i).getName()).append("()\n");
        }
        s.append("\t\t} where {\n");

        ArrayList<Unification> unifications = new ArrayList<>();

        for (Connector c : connectors) {
            unifications.add(new Unification(c.getFrom(), c.getTo(), c));
        }

        for (StartEvent se : startEvents) {
            for (int i = 0; i < se.getOutgoings().size(); i++) {
                ArrayList<Unification> us = findUnificationByFrom(unifications, se);
                if (us.size() == 0) { //there is no connections in se[i]
                    continue;
                }
                us.get(i).setFromPort(se.getOutgoings().get(i));
            }
        }
        for (ParticipantTask pt : participantTasks) {
            ArrayList<Unification> us = findUnificationByFrom(unifications, pt);




            ArrayList <Connection> froms = pt.getParticipant().getAllFromConnections();
            for (int i = 0; i < froms.size() && i < us.size(); i++) {
                Unification u = us.get(i);
                u.setFromPort(froms.get(i).getName());
            }
            us = findUnificationByTo(unifications,pt);
            for (Unification u : us) {
                if (u.getToPort() == null || u.getToPort().equals("")) {
                    u.setToPort(pt.getParticipant().getConnectionName(u.getFromComp().getId(), Connection.Type.IN));
                }
            }
        }
        for (ChoreographyTask ct : tasks) {
            //from
            ArrayList<Unification> us = findUnificationByFrom(unifications, ct);
            for (Unification u : us) {
                if (! (u.getToComp() instanceof ParticipantTask)) {
                    u.setFromPort(ct.getOutgoing());
                } else {
                    for (String out : ct.getOutgoings()) {
                        ParticipantTask pt = getComponentByPort(out);
                        if (pt != null && pt.equals(u.getToComp())) {
                            u.setFromPort(out);
                        }
                    }
                }

            }

            //to
            us = findUnificationByTo(unifications, ct);
            for (Unification u : us) {
                if (! (u.getFromComp() instanceof ParticipantTask)) {
                    u.setToPort(ct.getIncoming());
                } else {
                    for (String in : ct.getIncomings()) {
                        ParticipantTask pt = getComponentByPort(in);
                        if (pt != null && pt.equals(u.getFromComp())) {
                            u.setToPort(in);
                        }
                    }
                }

            }
        }
        for (Gateway g : gateways) {
            for (int i = 0; i < g.getIncomings().size(); i++) {
                ArrayList<Unification> us = findUnificationByTo(unifications, g);
                //a quantidade de incomings é igual à quantidade de Unifications obtida
                us.get(i).setToPort(g.getIncomings().get(i));
            }
            for (int i = 0; i < g.getOutgoings().size(); i++) {
                ArrayList<Unification> us = findUnificationByFrom(unifications, g);
                us.get(i).setFromPort(g.getOutgoings().get(i));
            }
        }
        for (EndEvent ee : endEvents) {
            for (int i = 0; i < ee.getIncomings().size(); i++) {
                ArrayList<Unification> us = findUnificationByTo(unifications, ee);
                us.get(i).setToPort(ee.getIncomings().get(i));
            }
        }
        for (Unification u : unifications) {
            s.append("\t\t\t").append(u).append("\n");
        }
        s.append("\t\t}\n");
        s.append("\t}\n");
        s.append("}\n");
        s.append("behavior is {\n");
        s.append("\tbecome(").append(archName).append("())\n");
        s.append("}\n");

        return s.toString();
    }

    private ParticipantTask getComponentByPort(String port) {
        for (ParticipantTask pt : participantTasks) {
            if (pt.hasConnection(port)) {
                return pt;
            }
        }
        return null;
    }

    private Connector getConnectorByFrom(ParticipantTask pt) {
        for (Connector c : connectors) {
            if (c.getFrom().equals(pt)) {
                return c;
            }
        }
        return null;
    }

    private Connector getConnectorByTo(ParticipantTask pt) {
        for (Connector c : connectors) {
            if (c.getTo().equals(pt)) {
                return c;
            }
        }
        return null;
    }

    private ArrayList<Unification> findUnificationByFrom(ArrayList<Unification> unifications, Component comp) {
        ArrayList<Unification> unificationsMatch = new ArrayList<>();
        for (Unification u : unifications) {
            if (u.getFromComp() != null && u.getFromComp().getId().equals(comp.getId())
            ) {
                unificationsMatch.add(u);
            }
        }
        return unificationsMatch;
    }
    private ArrayList<Unification> findUnificationByTo(ArrayList<Unification> unifications, Component comp) {
        ArrayList<Unification> unificationsMatch = new ArrayList<>();
        for (Unification u : unifications) {
            if (u.getToComp() != null && u.getToComp().getId().equals(comp.getId())) {
                unificationsMatch.add(u);
            }
        }
        return unificationsMatch;
    }

    public String getComponentsOriginalName(String instanceName) {
        for (StartEvent s : startEvents) {
            if (s.getInstanceName().equals(instanceName)) {
                return s.getOriginalName();
            }
        }
        for (EndEvent e : endEvents) {
            if (e.getInstanceName().equals(instanceName)) {
                return e.getOriginalName();
            }
        }
        for (ChoreographyTask ct : tasks) {
            if (ct.getInstanceName().equals(instanceName)) {
                return ct.getOriginalName();
            }
        }
        for (Gateway g : gateways) {
            if (g.getInstanceName().equals(instanceName)) {
                return g.getOriginalName();
            }
        }
        return null;
//        Component c = getComponent(componentId);
//        if (c == null) {
//            return null;
//        }
//        return c.getName();
    }

    //method that returns more advanced Component or connection from elementIds, i.e, the component closest to the end components
    public Component getMoreAdvancedElementByListOfId(ArrayList<String> elementIds) {
        String longestId = elementIds.get(0);
        int longestNum = -1;
        for (int i = 0; i < elementIds.size(); i++) {
            int aux = findElement(startEvents.get(0), elementIds.get(i), 0); //todo: modify for multiple start events
            if (aux > longestNum) {
                longestNum = aux;
                longestId = elementIds.get(i);
            }
        }

        Connector connector = getConnectorByInstanceName(longestId);
        if (connector != null) return connector.getTo();

        else {
            Component component = getComponentByInstanceName(longestId);
            if (component == null) return null;
            //gets next component, which was the one where deadlock occurred
            connector = findConnectorFrom(component.getId()).get(0); //there will be only one connector
            return connector.getTo();
        }
    }

    private Component getComponentByInstanceName(String id) {
        for (StartEvent s : startEvents) {
            if (s.getInstanceName().equals(id)) {
                return s;
            }
        }
        for (EndEvent e : endEvents) {
            if (e.getInstanceName().equals(id)) {
                return e;
            }
        }
        for (ChoreographyTask ct : tasks) {
            if (ct.getInstanceName().equals(id)) {
                return ct;
            }
        }
        for (Gateway g : gateways) {
            if (g.getInstanceName().equals(id)) {
                return g;
            }
        }
        return null;
    }

    private Connector getConnectorByInstanceName(String id) {
        for (Connector c : connectors) {
            if (c.getInstanceName().equals(id)) {
                return c;
            }
        }
        return null;
    }

    private int findElement(Connector current, String targetId, int i) {
        //recursion base cases
        if (current.getInstanceName().equals(targetId)) return i;
        if (current.getTo() instanceof EndEvent) return -1;

        //current connector is not the element being searched
        return findElement(current.getTo(), targetId, i + 1);
    }

    private int findElement(Component current, String targetId, int i) {
        //recursion base cases
        if (current.getInstanceName().equals(targetId)) return i;
        if (current instanceof EndEvent) return -1;

        //current connector is not the element being searched
        ArrayList<Connector> connectors = findConnectorFrom(current.getId());
        int longestSize = -1;
        for (Connector c : connectors) {
            //don't go into message exchange components
            if (c.getTo() instanceof ParticipantTask) continue;

            int aux = findElement(c, targetId, i + 1);
            if (aux > longestSize) {
                longestSize = aux;
            }
        }
        return longestSize;
    }
}
